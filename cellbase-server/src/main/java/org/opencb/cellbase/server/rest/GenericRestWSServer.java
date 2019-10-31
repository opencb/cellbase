/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.server.rest;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.Species;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.monitor.Monitor;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.datastore.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.opencb.commons.datastore.core.QueryOptions.*;

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

    @ApiParam(name = "assembly", value = "Set the reference genome assembly, e.g. grch38. For a full list of"
            + "potentially available assemblies, please refer to: "
            + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species")
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
    @QueryParam("skipCount")
    @ApiParam(name = "skipCount", value = "Skip counting the total number of results. In other words, will leave "
            + "numTotalResults in the QueryResult object to -1. This can make queries much faster."
            + " Please note that this option may not be available for all web services.")
    protected String skipCount;

    @DefaultValue("false")
    @QueryParam("count")
    @ApiParam(name = "count", value = "Get a count of the number of results obtained. Deactivated by default. "
            + " Please note that this option may not be available for all web services.",
            defaultValue = "false", allowableValues = "false,true")
    protected String count;

    @DefaultValue("")
    @QueryParam("sort")
    @ApiParam(name = "sort", value = "Sort returned results by a certain data model attribute."
            + " Please note that this option may not be available for all web services.")
    protected String sort;

    @DefaultValue("json")
    @QueryParam("of")
    @ApiParam(name = "Output format", value = "Output format, Protobuf is not yet implemented", defaultValue = "json",
            allowableValues = "json,pb (Not implemented yet)")
    protected String outputFormat;


    protected Query query;
    protected QueryOptions queryOptions;
    protected QueryResponse queryResponse;

    protected CellBaseDataResponse response;
    protected ObjectMap params;

    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;
    protected static final String SERVICE_START_DATE;
    protected static final StopWatch WATCH;

    protected static AtomicBoolean initialized;

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
//    protected static DBAdaptorFactory dbAdaptorFactory;
    protected static DBAdaptorFactory dbAdaptorFactory;
    protected static Monitor monitor;

    private static final int LIMIT_DEFAULT = 1000;
    private static final int LIMIT_MAX = 5000;
    private static final String ERROR = "error";
    private static final String OK = "ok";

    static {
        initialized = new AtomicBoolean(false);

        SERVICE_START_DATE = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        WATCH = new StopWatch();
        WATCH.start();



        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();

        // Initialize Monitor
        monitor = new Monitor(dbAdaptorFactory);
    }

    public GenericRestWSServer(@PathParam("version") String version, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException, CellbaseException {
        this.version = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        init();
        initQuery();
    }

    public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException, CellbaseException {
        this.version = version;
        this.species = species;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        init();
        initQuery();
    }

    protected void init() throws VersionException, SpeciesException, IOException, CellbaseException {
        // we need to make sure we only init one single time
        if (initialized.compareAndSet(false, true)) {
            logger = LoggerFactory.getLogger(this.getClass());

            // We must load the configuration file from CELLBASE_HOME, this must happen only the first time!
            ServletContext context = httpServletRequest.getServletContext();
            String cellbaseHome = context.getInitParameter("CELLBASE_HOME");
            if (StringUtils.isEmpty(cellbaseHome)) {
                // If not exists then we try the environment variable OPENCGA_HOME
                if (StringUtils.isNotEmpty(System.getenv("CELLBASE_HOME"))) {
                    cellbaseHome = System.getenv("CELLBASE_HOME");
                } else {
                    logger.error("No valid configuration directory provided!");
                    throw new CellbaseException("No CELLBASE_HOME found");
                }
            }
            logger.debug("CELLBASE_HOME set to: {}", cellbaseHome);

            cellBaseConfiguration = CellBaseConfiguration.load(Paths.get(cellbaseHome).resolve("conf").resolve("configuration.yml"));
            dbAdaptorFactory = new org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory(cellBaseConfiguration);
        }
    }

    private void initQuery() throws VersionException, SpeciesException {
        startTime = System.currentTimeMillis();
        query = new Query();
        // This needs to be an ArrayList since it may be added some extra fields later
        queryOptions = new QueryOptions("exclude", new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        queryResponse = new QueryResponse();

        params = new ObjectMap();
        response = new CellBaseDataResponse();

        checkPathParams(true);
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
//        if (version.equalsIgnoreCase("latest")) {
//            version = cellBaseConfiguration.getVersion();
//            logger.info("Version 'latest' detected, setting version parameter to '{}'", version);
//        } else {
//            // FIXME this will only work when no database schemas are done, in version 3 and 4 this can raise some problems
//            // we set the version from the URL, this will decide which database is queried,
//            cellBaseConfiguration.setVersion(version);
//        }

//        if (!version.equalsIgnoreCase("v3") && !cellBaseConfiguration.getVersion().equalsIgnoreCase(this.version)) {
//            logger.error("Version '{}' does not match configuration '{}'", this.version, cellBaseConfiguration.getVersion());
//            throw new VersionException("Version not valid: '" + version + "'");
//        }
    }

    @Override
    public void parseQueryParams() {
        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();

        queryOptions.put("metadata", multivaluedMap.get("metadata") == null || multivaluedMap.get("metadata").get(0).equals("true"));

        if (exclude != null && !exclude.isEmpty()) {
            // We add the user's 'exclude' fields to the default values _id and _chunks
            if (queryOptions.containsKey(EXCLUDE)) {
                queryOptions.getAsStringList(EXCLUDE).addAll(Splitter.on(",").splitToList(exclude));
            }
        }
//        else {
//            queryOptions.put("exclude", (multivaluedMap.get("exclude") != null)
//                    ? Splitter.on(",").splitToList(multivaluedMap.get("exclude").get(0))
//                    : null);
//        }

        if (include != null && !include.isEmpty()) {
            queryOptions.put(INCLUDE, new LinkedList<>(Splitter.on(",").splitToList(include)));
        } else {
            queryOptions.put(INCLUDE, (multivaluedMap.get(INCLUDE) != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get(INCLUDE).get(0))
                    : null);
        }

        if (sort != null && !sort.isEmpty()) {
            queryOptions.put(SORT, sort);
        }

        queryOptions.put(LIMIT, (limit > 0) ? Math.min(limit, LIMIT_MAX) : LIMIT_DEFAULT);
        queryOptions.put(SKIP, (skip >= 0) ? skip : -1);
        queryOptions.put(SKIP_COUNT, StringUtils.isNotBlank(skipCount) && Boolean.parseBoolean(skipCount));
        queryOptions.put(COUNT, StringUtils.isNotBlank(count) && Boolean.parseBoolean(count));
//        outputFormat = (outputFormat != null && !outputFormat.equals("")) ? outputFormat : "json";

        // Add all the others QueryParams from the URL
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            if (!queryOptions.containsKey(entry.getKey())) {
//                logger.info("Adding '{}' to queryOptions", entry);
                // FIXME delete this!!
//                queryOptions.put(entry.getKey(), entry.getValue().get(0));
                query.put(entry.getKey(), entry.getValue().get(0));
            }
        }


//        try {
//            logger.info("{}\t{}\t{}", uriInfo.getAbsolutePath().toString(),
//                    jsonObjectWriter.writeValueAsString(query), jsonObjectWriter.writeValueAsString(queryOptions));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
    }

    protected void logQuery(String status) {
        try {
            logger.info("{}\t{}\t{}\t{}\t{}",
                    uriInfo.getAbsolutePath().toString(),
                    jsonObjectWriter.writeValueAsString(query),
                    jsonObjectWriter.writeValueAsString(queryOptions),
                    new Long(System.currentTimeMillis() - startTime).intValue(),
                    status);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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

            return createOkResponse(new QueryResult<>(clazz.toString(), 0, 1, 1, null, null,
                    Collections.singletonList(jsonSchema)));
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
        logQuery(ERROR);

        return Response
                .fromResponse(createJsonResponse(queryResponse))
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .build();
    }

    protected Response createErrorResponse(String method, String errorMessage) {
        try {
            logQuery(ERROR);
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


        // Now:

        params.put("id", ((CellBaseDataResult) obj).getId());
        params.put("species", species);
        params.putAll(query);
        params.putAll(queryOptions);
        response.setParams(params);

        // Guarantee that the QueryResponse object contains a list of results
        List list;
        if (obj instanceof List) {
            list = (List) obj;
        } else {
            list = new ArrayList(1);
            list.add(obj);
        }
        queryResponse.setResponse(list);
        logQuery(OK);

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
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse),
                    MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")));
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
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
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
        List<Species> speciesList = cellBaseConfiguration.getAllSpecies();
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
        for (String id : ids) {
//            q = new Query(queryKey, id);
            Query q = new Query(query);
            q.put(queryKey, id);
            if (args != null && args.length > 0 && args.length % 2 == 0) {
                for (int i = 0; i < args.length; i += 2) {
                    q.put(args[i], args[i + 1]);
                }
            }
            queries.add(q);
        }
        return queries;
    }
}
