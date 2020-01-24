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
import javax.ws.rs.*;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Path("/{version}/{species}")
@Produces("text/plain")
public class GenericRestWSServer implements IWSServer {

    protected String version;
    protected String species;

    @ApiParam(name = "assembly", value = "Set the reference genome assembly, e.g. grch38. For a full list of"
            + "potentially available assemblies, please refer to: "
            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species")
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

//    @DefaultValue("false")
//    @QueryParam("skipCount")
//    @ApiParam(name = "skipCount", value = "Skip counting the total number of results. In other words, will leave "
//            + "numTotalResults in the QueryResult object to -1. This can make queries much faster."
//            + " Please note that this option may not be available for all web services.")
//    protected String skipCount;

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
    protected ObjectMap params;
    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;
    protected ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;
    protected String SERVICE_START_DATE;
    protected StopWatch WATCH;
    protected AtomicBoolean initialized;
    protected long startTime;
    protected Logger logger;

    /**
     * Loading properties file just one time to be more efficient. All methods
     * will check parameters so to avoid extra operations this config can load
     * versions and species
     */
    protected CellBaseConfiguration cellBaseConfiguration; //= new CellBaseConfiguration()

    /**
     * DBAdaptorFactory creation, this object can be initialize with an
     * HibernateDBAdaptorFactory or an HBaseDBAdaptorFactory. This object is a
     * factory for creating adaptors like GeneDBAdaptor
     */
    protected DBAdaptorFactory dbAdaptorFactory;
    protected Monitor monitor;

    private static final int LIMIT_DEFAULT = 10;
    private static final int LIMIT_MAX = 5000;
    private static final String ERROR = "error";
    private static final String OK = "ok";
    // this webservice has no species, do not validate
    private static final String DONT_CHECK_SPECIES = "do not validate species";

    public GenericRestWSServer(@PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        this(version, DONT_CHECK_SPECIES, uriInfo, hsr);
    }

    public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {

        this.version = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;
        this.species = species;

        init();
        if (DONT_CHECK_SPECIES.equals(species)) {
            logger.debug("Executing GenericRestWSServer constructor with no Species");
            initQuery(false);
        } else {
            logger.debug("Executing GenericRestWSServer constructor with Species");
            initQuery(true);
        }
    }

    private void init() throws IOException, CellbaseException {
        // we need to make sure we only init one single time
        if (initialized == null || initialized.compareAndSet(false, true)) {
            initialized = new AtomicBoolean(true);

            SERVICE_START_DATE = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            WATCH = new StopWatch();
            WATCH.start();

            jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            jsonObjectWriter = jsonObjectMapper.writer();

            logger = LoggerFactory.getLogger(this.getClass());

            // We must load the configuration file from CELLBASE_HOME, this must happen only the first time!
            String cellbaseHome = System.getenv("CELLBASE_HOME");
            if (StringUtils.isEmpty(cellbaseHome)) {
                // ENV variable isn't set, try the servlet context instead
                ServletContext context = httpServletRequest.getServletContext();
                if (StringUtils.isNotEmpty(context.getInitParameter("CELLBASE_HOME"))) {
                    cellbaseHome = context.getInitParameter("CELLBASE_HOME");
                } else {
                    logger.error("No valid configuration directory provided!");
                    throw new CellbaseException("No CELLBASE_HOME found");
                }
            }

            logger.debug("CELLBASE_HOME set to: {}", cellbaseHome);

            cellBaseConfiguration = CellBaseConfiguration.load(Paths.get(cellbaseHome).resolve("conf").resolve("configuration.yml"));
            dbAdaptorFactory = new org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory(cellBaseConfiguration);

            // Initialize Monitor
            monitor = new Monitor(dbAdaptorFactory);
        }
    }

    private void initQuery(boolean checkSpecies) throws VersionException, SpeciesException {
        startTime = System.currentTimeMillis();
        query = new Query();
        // This needs to be an ArrayList since it may be added some extra fields later
        queryOptions = new QueryOptions("exclude", new ArrayList<>(Arrays.asList("_id", "_chunkIds")));
        params = new ObjectMap();

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

        if (!cellBaseConfiguration.getVersion().equalsIgnoreCase(this.version)) {
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
            if (queryOptions.containsKey(QueryOptions.EXCLUDE)) {
                queryOptions.getAsStringList(QueryOptions.EXCLUDE).addAll(Splitter.on(",").splitToList(exclude));
            }
        }

        if (include != null && !include.isEmpty()) {
            queryOptions.put(QueryOptions.INCLUDE, new LinkedList<>(Splitter.on(",").splitToList(include)));
        } else {
            queryOptions.put(QueryOptions.INCLUDE, (multivaluedMap.get(QueryOptions.INCLUDE) != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get(QueryOptions.INCLUDE).get(0))
                    : null);
        }

        if (sort != null && !sort.isEmpty()) {
            queryOptions.put(QueryOptions.SORT, sort);
        }

        queryOptions.put(QueryOptions.LIMIT, (limit > 0) ? Math.min(limit, LIMIT_MAX) : LIMIT_DEFAULT);
        queryOptions.put(QueryOptions.SKIP, (skip >= 0) ? skip : -1);
//        queryOptions.put(QueryOptions.SKIP_COUNT, StringUtils.isNotBlank(skipCount) && Boolean.parseBoolean(skipCount));
        queryOptions.put(QueryOptions.COUNT, StringUtils.isNotBlank(count) && Boolean.parseBoolean(count));

        // Add all the others QueryParams from the URL
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            if (!queryOptions.containsKey(entry.getKey())) {
                query.put(entry.getKey(), entry.getValue().get(0));
            }
        }
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
    @ApiOperation(httpMethod = "GET", value = "To be implemented", response = CellBaseDataResponse.class, hidden = true)
    public Response help() {
        return createOkResponse("No help available");
    }

    protected Response createModelResponse(Class clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(mapper.constructType(clazz), visitor);
            JsonSchema jsonSchema = visitor.finalSchema();

            return createOkResponse(new CellBaseDataResult<>(clazz.toString(), 0, Collections.emptyList(), 1,
                    Collections.singletonList(jsonSchema), 1));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    protected Response createErrorResponse(Exception e) {
        // First we print the exception in Server logs
        e.printStackTrace();

        // Now we prepare the response to client
        CellBaseDataResponse queryResponse = new CellBaseDataResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setParams(new ObjectMap(queryOptions));
        queryResponse.addEvent(new Event(Event.Type.ERROR, e.toString()));

        CellBaseDataResult<ObjectMap> result = new CellBaseDataResult();
        List<Event> events = new ArrayList<>();
        events.add(new Event(Event.Type.WARNING, "Future errors will ONLY be shown in the QueryResponse body"));
        events.add(new Event(Event.Type.ERROR, "DEPRECATED: " + e.toString()));
        queryResponse.setEvents(events);
        queryResponse.setResponses(Arrays.asList(result));
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
        CellBaseDataResponse queryResponse = new CellBaseDataResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);

        params.put("species", species);
        params.putAll(query);
        params.putAll(queryOptions);
        queryResponse.setParams(params);

        // Guarantee that the QueryResponse object contains a list of data results
        List list;
        if (obj instanceof List) {
            list = (List) obj;
        } else {
            list = new ArrayList(1);
            list.add(obj);
        }
        queryResponse.setResponses(list);
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

    protected Response createJsonResponse(CellBaseDataResponse queryResponse) {
        try {
            System.out.println("queryResponse.getResponses().get(0).toString() = " + queryResponse.getResponses().get(0).toString());
            String value = jsonObjectWriter.writeValueAsString(queryResponse);
            ResponseBuilder ok = Response.ok(value, MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"));
            return buildResponse(ok);
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

    protected List<Query> createQueries(String csvField, String queryKey, String... args) {
        String[] ids = csvField.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
        for (String id : ids) {
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
