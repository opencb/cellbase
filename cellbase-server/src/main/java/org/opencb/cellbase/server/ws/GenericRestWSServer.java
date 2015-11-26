/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.server.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.google.common.base.Splitter;
import io.swagger.annotations.ApiParam;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.DBAdaptorFactory;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptorFactory;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Path("/{version}/{species}")
@Produces("text/plain")
//@Api(value = "Generic", description = "Generic RESTful Web Services API")
public class GenericRestWSServer implements IWSServer {

    @DefaultValue("")
    @PathParam("version")
    @ApiParam(name = "version", value = "Use 'latest' for last stable version", allowableValues = "v3,latest", defaultValue = "v3")
    protected String version;

    @DefaultValue("")
    @PathParam("species")
    @ApiParam(name = "species", value = "Name of the species to query", defaultValue = "hsapiens",
            allowableValues = "hsapiens,mmusculus,drerio,rnorvegicus,ptroglodytes,ggorilla,"
                    + "mmulatta,sscrofa,cfamiliaris,ggallus,btaurus,cintestinalis,celegans,dmelanogaster,agambiae,pfalciparum,"
                    + "scerevisiae,lmajor,athaliana,osativa,gmax,vvinifera,zmays,slycopersicum,csabeus,oaries,olatipes,sbicolor,afumigatus")
    protected String species;

    @ApiParam(name = "genome assembly", value = "Set the reference genome assembly, e.g.: grch38")
    @DefaultValue("")
    @QueryParam("assembly")
    protected String assembly;

    @ApiParam(name = "excluded fields", value = "Set which fields are excluded in the response, e.g.: transcripts.exons")
    @DefaultValue("")
    @QueryParam("exclude")
    protected String exclude;

    @DefaultValue("")
    @QueryParam("include")
    @ApiParam(name = "included fields", value = "Set which fields are included in the response, e.g.: transcripts.id")
    protected String include;

    @DefaultValue("-1")
    @QueryParam("limit")
    @ApiParam(name = "limit", value = "Max number of results to be returned. No limit applied when -1. No limit is set by default.")
    protected int limit;

    @DefaultValue("-1")
    @QueryParam("skip")
    @ApiParam(name = "skip", value = "Number of results to be skipped. No skip applied when -1. No skip by default.")
    protected int skip;

    @DefaultValue("false")
    @QueryParam("count")
    @ApiParam(name = "count", value = "Get a count of the number of results obtained. Deactivated by default.",
            defaultValue = "false", allowableValues = "false,true")
    protected String count;

    @DefaultValue("json")
    @QueryParam("of")
    @ApiParam(name = "Output format", value = "Output format, Protobuf is not yet implemented", defaultValue = "json",
            allowableValues = "json,pb (Not implemented yet)")
    protected String outputFormat;


    protected QueryResponse queryResponse;
    protected QueryOptions queryOptions;

    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;

    protected long startTime;
    protected long endTime;

    protected static Logger logger;

    /**
     * Loading properties file just one time to be more efficient. All methods
     * will check parameters so to avoid extra operations this config can load
     * versions and species
     */
    protected static CellBaseConfiguration cellBaseConfiguration; //= new CellBaseConfiguration()

    /**
     * DBAdaptorFactory creation, this object can be initialize with an
     * HibernateDBAdaptorFactory or an HBaseDBAdaptorFactory. This object is a
     * factory for creating adaptors like GeneDBAdaptor
     */
    protected static DBAdaptorFactory dbAdaptorFactory;
    protected static org.opencb.cellbase.core.api.DBAdaptorFactory dbAdaptorFactory2;

    private static final int LIMIT_DEFAULT = 1000;
    private static final int LIMIT_MAX = 5000;

    static {
        logger = LoggerFactory.getLogger("org.opencb.cellbase.server.ws.GenericRestWSServer");
        logger.info("Static block, creating MongoDBAdapatorFactory");
        try {
            if (System.getenv("CELLBASE_HOME") != null) {
                logger.info("Loading configuration from '{}'", System.getenv("CELLBASE_HOME") + "/configuration.json");
                cellBaseConfiguration = CellBaseConfiguration
                        .load(new FileInputStream(new File(System.getenv("CELLBASE_HOME") + "/configuration.json")));
            } else {
                logger.info("Loading configuration from '{}'",
                        CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json").toString());
                cellBaseConfiguration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            }

            // If Configuration has been loaded we can create the DBAdaptorFactory
            dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
            dbAdaptorFactory2 = new org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory(cellBaseConfiguration);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();
    }


    public GenericRestWSServer(@PathParam("version") String version, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr) throws VersionException, SpeciesException {
        this.version = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        logger.debug("Executing GenericRestWSServer constructor with no Species");
        init(false);
    }

    public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr) throws VersionException, SpeciesException {
        this.version = version;
        this.species = species;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        logger.debug("Executing GenericRestWSServer constructor");
        init(true);
    }


    protected void init(boolean checkSpecies) throws VersionException, SpeciesException {
        startTime = System.currentTimeMillis();

        queryResponse = new QueryResponse();
        queryOptions = new QueryOptions();

        checkPathParams(checkSpecies);
    }

    private void checkPathParams(boolean checkSpecies) throws VersionException, SpeciesException {
        if (version == null) {
            throw new VersionException("Version not valid: '" + version + "'");
        }

        if (checkSpecies && species == null) {
            throw new SpeciesException("Species not valid: '" + species + "'");
        }

        /**
         * Check version parameter, must be: v1, v2, ... If 'latest' then is
         * converted to appropriate version
         */
        if (version.equalsIgnoreCase("latest")) {
            version = cellBaseConfiguration.getVersion();
            logger.info("Version 'latest' detected, setting version parameter to '{}'", version);
        }

        if (!cellBaseConfiguration.getVersion().equalsIgnoreCase(this.version)) {
            logger.error("Version '{}' does not match configuration '{}'", this.version, cellBaseConfiguration.getVersion());
            throw new VersionException("Version not valid: '" + version + "'");
        }
    }

    @Override
    public void parseQueryParams() {
        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
        queryOptions.put("metadata", (multivaluedMap.get("metadata") != null)
                ? multivaluedMap.get("metadata").get(0).equals("true")
                : true);

        if (exclude != null && !exclude.equals("")) {
            queryOptions.put("exclude", new LinkedList<>(Splitter.on(",").splitToList(exclude)));
        } else {
            queryOptions.put("exclude", (multivaluedMap.get("exclude") != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get("exclude").get(0))
                    : null);
        }

        if (include != null && !include.equals("")) {
            queryOptions.put("include", new LinkedList<>(Splitter.on(",").splitToList(include)));
        } else {
            queryOptions.put("include", (multivaluedMap.get("include") != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get("include").get(0))
                    : null);
        }

        queryOptions.put("limit", (limit > 0) ? Math.min(limit, LIMIT_MAX) : LIMIT_DEFAULT);
        queryOptions.put("skip", (skip > 0) ? skip : -1);
        queryOptions.put("count", (count != null && !count.equals("")) ? Boolean.parseBoolean(count) : false);
//        outputFormat = (outputFormat != null && !outputFormat.equals("")) ? outputFormat : "json";

        // Now we add all the others QueryParams in the URL
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            if (!queryOptions.containsKey(entry.getKey())) {
                logger.info("Adding '{}' to queryOptions", entry);
                queryOptions.put(entry.getKey(), entry.getValue().get(0));
            }
        }
    }


    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("No help available");
    }

    @GET
    public Response defaultMethod() {
        switch (species) {
            case "echo":
                return createStringResponse("Status active");
            default:
                break;
        }
        return createOkResponse("Not valid option");
    }


    protected Response createModelResponse(Class clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(mapper.constructType(clazz), visitor);
            JsonSchema jsonSchema = visitor.finalSchema();

            return createOkResponse(jsonSchema);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    protected Response createErrorResponse(Exception e) {
        // First we print the exception in Server logs
        e.printStackTrace();

        // Now we prepare the response to client
        queryResponse = new QueryResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        queryResponse.setError(e.toString());

        QueryResult<ObjectMap> result = new QueryResult();
        result.setWarningMsg("Future errors will ONLY be shown in the QueryResponse body");
        result.setErrorMsg("DEPRECATED: " + e.toString());
        queryResponse.setResponse(Arrays.asList(result));

        return Response
                .fromResponse(createJsonResponse(queryResponse))
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .build();
    }

    protected Response createErrorResponse(String method, String errorMessage) {
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(new HashMap<>().put("[ERROR] " + method, errorMessage)),
                    MediaType.APPLICATION_JSON_TYPE));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    protected Response createOkResponse(Object obj) {
        queryResponse = new QueryResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);

        // Guarantee that the QueryResponse object contains a list of results
        List list;
        if (obj instanceof List) {
            list = (List) obj;
        } else {
            list = new ArrayList(1);
            list.add(obj);
        }
        queryResponse.setResponse(list);

        return createJsonResponse(queryResponse);
    }

    protected Response createOkResponse(Object obj, MediaType mediaType) {
        return buildResponse(Response.ok(obj, mediaType));
    }

    protected Response createOkResponse(Object obj, MediaType mediaType, String fileName) {
        return buildResponse(Response.ok(obj, mediaType).header("content-disposition", "attachment; filename =" + fileName));
    }

    protected Response createStringResponse(String str) {
        return buildResponse(Response.ok(str));
    }

    protected Response createJsonResponse(QueryResponse queryResponse) {
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing queryResponse object");
            return createErrorResponse("", "Error parsing QueryResponse object:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    private Response buildResponse(ResponseBuilder responseBuilder) {
        return responseBuilder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "x-requested-with, content-type")
                .build();
    }


    /*
     * TO DELETE
     */
    @Deprecated
    protected Response generateResponse(String queryString, List features) throws IOException {
        return createOkResponse("TODO: generateResponse is deprecated");
    }

    @Deprecated
    protected Response generateResponse(String queryString, String headerTag, List features) throws IOException {
        return createOkResponse("TODO: generateResponse is deprecated");
    }

    @Deprecated
    private boolean isSpecieAvailable(String species) {
        List<CellBaseConfiguration.SpeciesProperties.Species> speciesList = cellBaseConfiguration.getAllSpecies();
        for (int i = 0; i < speciesList.size(); i++) {
            // This only allows to show the information if species is in 3
            // letters format
            if (species.equalsIgnoreCase(speciesList.get(i).getId())) {
                return true;
            }
        }
        return false;
    }

    //    @Deprecated
//    protected Response createJsonResponse(List<QueryResult> obj) {
//        endTime = System.currentTimeMillis() - startTime;
//        queryResponse.setTime((int) endTime);
//        queryResponse.setApiVersion(version);
//        queryResponse.setQueryOptions(queryOptions);
//        queryResponse.setResponse(obj);
//
////        queryResponse.put("species", species);
////        queryResponse.put("queryOptions", queryOptions);
////        queryResponse.put("response", obj);
//
//        try {
//            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            logger.error("Error parsing queryResponse object");
//            return null;
//        }
//    }


//    @Deprecated
//    protected Response createErrorResponse(Object o) {
//        String objMsg = o.toString();
//        if (objMsg.startsWith("ERROR:")) {
//            return buildResponse(Response.ok("" + o));
//        } else {
//            return buildResponse(Response.ok("ERROR: " + o));
//        }
//    }

//    @Deprecated
//    protected Response createOkResponse(String message) {
//        return buildResponse(Response.ok(message));
//    }

//    @Deprecated
//    protected Response createOkResponse(QueryResult queryResult) {
//        return createOkResponse(Arrays.asList(queryResult));
//    }

//    @Deprecated
//    protected Response createOkResponse(List<QueryResult> queryResults) {
//        switch (outputFormat.toLowerCase()) {
//            case "json":
//                return createJsonResponse(queryResults);
//            case "xml":
//                return createOkResponse(queryResults, MediaType.APPLICATION_XML_TYPE);
//            default:
//                return buildResponse(Response.ok(queryResults));
//        }
//    }

    //    protected Response createResponse(String response, MediaType mediaType) throws IOException {
//        if (fileFormat == null || fileFormat.equalsIgnoreCase("")) {
//            if (outputCompress != null && outputCompress.equalsIgnoreCase("true")
//                    && !outputFormat.equalsIgnoreCase("jsonp") && !outputFormat.equalsIgnoreCase("jsontext")) {
//                response = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" ", "");
//            }
//        } else {
//            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
//            logger.debug("\t\t - Creating byte stream ");
//
//            if (outputCompress != null && outputCompress.equalsIgnoreCase("true")) {
//                OutputStream bos = new ByteArrayOutputStream();
//                bos.write(response.getBytes());
//
//                ZipOutputStream zipstream = new ZipOutputStream(bos);
//                zipstream.setLevel(9);
//
//                logger.debug("CellBase - CreateResponse, zipping... Final media Type: " + mediaType.toString());
//
//                return this.createOkResponse(zipstream, mediaType, filename + ".zip");
//
//            } else {
//                if (fileFormat.equalsIgnoreCase("xml")) {
//                    // mediaType = MediaType.valueOf("application/xml");
//                }
//
//                if (fileFormat.equalsIgnoreCase("excel")) {
//                    // mediaType =
//                    // MediaType.valueOf("application/vnd.ms-excel");
//                }
//                if (fileFormat.equalsIgnoreCase("txt") || fileFormat.equalsIgnoreCase("text")) {
//                    logger.debug("\t\t - text File ");
//
//                    byte[] streamResponse = response.getBytes();
//                    // return Response.ok(streamResponse,
//                    // mediaType).header("content-disposition","attachment; filename = "+
//                    // filename + ".txt").build();
//                    return this.createOkResponse(streamResponse, mediaType, filename + ".txt");
//                }
//            }
//        }
//        logger.debug("CellBase - CreateResponse, Final media Type: " + mediaType.toString());
//        // return Response.ok(response, mediaType).build();
//        return this.createOkResponse(response, mediaType);
//    }

}
