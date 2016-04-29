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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.DBAdaptorFactory;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.datastore.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Path("/{version}/{species}")
@Produces("text/plain")
//@Api(value = "Generic", description = "Generic RESTful Web Services API")
public class GenericRestWSServer implements IWSServer {

    //    @DefaultValue("")
//    @PathParam("version")
//    @ApiParam(name = "version", value = "Use 'latest' for last stable version",  defaultValue = "latest")
    protected String version;

    //    @DefaultValue("")
//    @PathParam("species")
//    @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens.")
    protected String species;

    @ApiParam(name = "genome assembly", value = "Set the reference genome assembly, e.g. grch38. For a full list of"
            + "potentially available assemblies, please refer to: "
            + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species")
    @DefaultValue("")
    @QueryParam("assembly")
    protected String assembly;

    @ApiParam(name = "exclude", value = "Set which fields are excluded in the response, e.g.: transcripts.exons. "
            + " Please note that this option may not be enabled for all web services.")
    @DefaultValue("")
    @QueryParam("exclude")
    protected String exclude;

    @DefaultValue("")
    @QueryParam("include")
    @ApiParam(name = "include", value = "Set which fields are included in the response, e.g.: transcripts.id. "
            + " Please note that this parameter may not be enabled for all web services.")
    protected String include;

    @DefaultValue("-1")
    @QueryParam("limit")
    @ApiParam(name = "limit", value = "Max number of results to be returned. No limit applied when -1."
            + " Please note that this option may not be available for all web services.")
    protected int limit;

    @DefaultValue("-1")
    @QueryParam("skip")
    @ApiParam(name = "skip", value = "Number of results to be skipped. No skip applied when -1. "
            + " Please note that this option may not be available for all web services.")
    protected int skip;

    @DefaultValue("false")
    @QueryParam("count")
    @ApiParam(name = "count", value = "Get a count of the number of results obtained. Deactivated by default. "
            + " Please note that this option may not be available for all web services.",
            defaultValue = "false", allowableValues = "false,true")
    protected String count;

    @DefaultValue("json")
    @QueryParam("of")
    @ApiParam(name = "Output format", value = "Output format, Protobuf is not yet implemented", defaultValue = "json",
            allowableValues = "json,pb (Not implemented yet)")
    protected String outputFormat;


    protected Query query;
    protected QueryOptions queryOptions;
    protected QueryResponse queryResponse;

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
//            dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
            dbAdaptorFactory2 = new org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory(cellBaseConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
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

        query = new Query();
        // This needs to be an ArrayList since it may be added some extra fields later
        queryOptions = new QueryOptions("exclude", new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        queryResponse = new QueryResponse();

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
        } else {
            // FIXME this will only work when no database schemas are done, in version 3 and 4 this can raise some problems
            // we set the version from the URL, this will decide which database is queried,
            cellBaseConfiguration.setVersion(version);
        }

        if (!version.equalsIgnoreCase("v3") && !cellBaseConfiguration.getVersion().equalsIgnoreCase(this.version)) {
            logger.error("Version '{}' does not match configuration '{}'", this.version, cellBaseConfiguration.getVersion());
            throw new VersionException("Version not valid: '" + version + "'");
        }
    }

    @Override
    public void parseQueryParams() {
        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();

        queryOptions.put("metadata", multivaluedMap.get("metadata") == null || multivaluedMap.get("metadata").get(0).equals("true"));

        if (exclude != null && !exclude.isEmpty()) {
            // We add the user's 'exclude' fields to the default values _id and _chunks
            if (queryOptions.containsKey("exclude")) {
                queryOptions.getAsStringList("exclude").addAll(Splitter.on(",").splitToList(exclude));
            }
        }
//        else {
//            queryOptions.put("exclude", (multivaluedMap.get("exclude") != null)
//                    ? Splitter.on(",").splitToList(multivaluedMap.get("exclude").get(0))
//                    : null);
//        }

        if (include != null && !include.isEmpty()) {
            queryOptions.put("include", new LinkedList<>(Splitter.on(",").splitToList(include)));
        } else {
            queryOptions.put("include", (multivaluedMap.get("include") != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get("include").get(0))
                    : null);
        }

        queryOptions.put("limit", (limit > 0) ? Math.min(limit, LIMIT_MAX) : LIMIT_DEFAULT);
        queryOptions.put("skip", (skip > 0) ? skip : -1);
        queryOptions.put("count", (count != null && !count.equals("")) && Boolean.parseBoolean(count));
//        outputFormat = (outputFormat != null && !outputFormat.equals("")) ? outputFormat : "json";

        // Add all the others QueryParams from the URL
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            if (!queryOptions.containsKey(entry.getKey())) {
//                logger.info("Adding '{}' to queryOptions", entry);
                // FIXME delete this!!
                queryOptions.put(entry.getKey(), entry.getValue().get(0));
                query.put(entry.getKey(), entry.getValue().get(0));
            }
        }
    }


    @GET
    @Path("/help")
    @ApiOperation(httpMethod = "GET", value = "To be implemented", response = QueryResponse.class, hidden = true)
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

//    protected List<Query> createQueries(String csvField, String queryKey) {
//        String[] ids = csvField.split(",");
//        List<Query> queries = new ArrayList<>(ids.length);
//        for (String s : ids) {
//            queries.add(new Query(queryKey, s));
//        }
//        return queries;
//    }

    protected List<Query> createQueries(String csvField, String queryKey, String... args) {
        String[] ids = csvField.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
        for (String s : ids) {
            Query query = new Query(queryKey, s);
            if (args != null && args.length > 0 && args.length % 2 == 0) {
                for (int i = 0; i < args.length; i += 2) {
                    query.put(args[i], args[i + 1]);
                }
            }
            queries.add(query);
        }
        return queries;
    }
}
