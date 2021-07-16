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
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.monitor.Monitor;
import org.opencb.commons.datastore.core.Event;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

    protected Query query;
//    protected QueryOptions queryOptions;
    private static final int MAX_RECORDS = 5000;
    protected Map<String, String> uriParams;
    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;
    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;
    protected String SERVICE_START_DATE;
    protected StopWatch WATCH;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    protected long startTime;
    protected static Logger logger;

    /**
     * Loading properties file just one time to be more efficient. All methods
     * will check parameters so to avoid extra operations this config can load
     * versions and species
     */
    protected static CellBaseConfiguration cellBaseConfiguration;
    protected static CellBaseManagerFactory cellBaseManagerFactory;
    protected static org.opencb.cellbase.lib.monitor.Monitor monitor;
    private static final String ERROR = "error";
    private static final String OK = "ok";
    // this webservice has no species, do not validate
    private static final String DONT_CHECK_SPECIES = "do not validate species";

    public GenericRestWSServer(@PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws QueryException, IOException, CellBaseException {
        this(version, DONT_CHECK_SPECIES, uriInfo, hsr);
    }

    public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr)
            throws QueryException, IOException, CellBaseException {

        this.version = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;
        this.species = species;

        init();
        initQuery();
    }

    private void init() throws IOException, CellBaseException {
        // we need to make sure we only init one single time
        if (INITIALIZED.compareAndSet(false, true)) {
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
                    throw new CellBaseException("No CELLBASE_HOME found");
                }
            }

            logger.info("CELLBASE_HOME set to: {}", cellbaseHome);

            logger.info("***************************************************");
            logger.info("cellbaseHome = " + cellbaseHome);
            cellBaseConfiguration = CellBaseConfiguration.load(Paths.get(cellbaseHome).resolve("conf").resolve("configuration.yml"));
            cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
            logger.info("***************************************************");

            // Initialize Monitor
            monitor = new Monitor(cellBaseManagerFactory.getMetaManager());
        }
    }

    private void initQuery() throws CellBaseException {
        startTime = System.currentTimeMillis();
        query = new Query();
        uriParams = convertMultiToMap(uriInfo.getQueryParameters());

        // assembly isn't needed in the query, only in the database connection which we already have.
        if (uriParams.get("assembly") != null) {
            uriParams.remove("assembly");
        }

        checkLimit();

        // check version. species is validated later
        checkVersion();
    }

    /**
     * If limit is empty, then set to be 10. If limit is set, check that it is less than maximum allowed limit.
     *
     * @throws CellBaseException if limit is higher than max allowed values
     */
    private void checkLimit() throws CellBaseException {
        if (uriParams.get("limit") == null) {
            uriParams.put("limit", ParamConstants.DEFAULT_LIMIT);
        } else {
            int limit = Integer.parseInt(uriParams.get("limit"));
            if (limit > MAX_RECORDS) {
                throw new CellBaseException("Limit cannot exceed " + MAX_RECORDS + " but is : '" + uriParams.get("limit") + "'");
            }
        }
    }

    private void checkVersion() throws CellBaseException {
        if (version == null) {
            throw new CellBaseException("Version not valid: '" + version + "'");
        }

//        System.out.println("*************************************");
//        System.out.println("cellBaseConfiguration = " + cellBaseConfiguration);
//        System.out.println("cellBaseConfiguration.getVersion() = " + cellBaseConfiguration.getVersion());
//        System.out.println("version = " + version);
//        System.out.println("*************************************");
        if (!cellBaseConfiguration.getVersion().equalsIgnoreCase(version)) {
            logger.error("Version '{}' does not match configuration '{}'", this.version, cellBaseConfiguration.getVersion());
            throw new CellBaseException("Version not valid: '" + version + "'");
        }
    }

    private Map<String, String> convertMultiToMap(MultivaluedMap<String, String> multivaluedMap) {
        Map<String, String> convertedMap = new HashMap<String, String>();
        if (multivaluedMap == null) {
            return convertedMap;
        }
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            List<String> values = entry.getValue();
            if (CollectionUtils.isNotEmpty(values)) {
                convertedMap.put(entry.getKey(), String.join(",", entry.getValue()));
            }
        }
        return convertedMap;
    }

    // "fields" is not part of genequery, will throw an exception, rename to be "facet"
    public void copyToFacet(String columnName, String fields) {
        uriParams.keySet().removeIf(columnName::equals);
        uriParams.put("facet", fields);
    }

    protected void logQuery(String status) {
        StringBuilder params = new StringBuilder();
        uriParams.forEach((key, value) -> params.append(key).append(": ").append(value + ", "));

        try {
            logger.info("{}\t{}\t{}\t{}\t{}",
                    uriInfo.getAbsolutePath().toString(),
                    jsonObjectWriter.writeValueAsString(query),
                    params.toString(),
//                    jsonObjectWriter.writeValueAsString(queryOptions),
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
        logger.error("Catch error: " + e.getMessage(), e);

        // Now we prepare the response to client
        CellBaseDataResponse queryResponse = new CellBaseDataResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);
//        queryResponse.setParams(new ObjectMap(queryOptions));
        queryResponse.addEvent(new Event(Event.Type.ERROR, e.toString()));

        CellBaseDataResult<ObjectMap> result = new CellBaseDataResult();
        List<Event> events = new ArrayList<>();
//        events.add(new Event(Event.Type.WARNING, "Future errors will ONLY be shown in the QueryResponse body"));
        events.add(new Event(Event.Type.ERROR, e.toString()));
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

        ObjectMap params = new ObjectMap();
        params.put("species", species);
//        params.putAll(query);
//        params.putAll(queryOptions);
        params.putAll(uriParams);
        queryResponse.setParams(params);

        // Guarantee that the QueryResponse object contains a list of data results
        List list;
        if (obj instanceof List) {
            list = (List) obj;
        } else {
            list = new ArrayList(1);
            list.add(obj);
        }

//        CellBaseDataResult dataResults = new CellBaseDataResult("id", 0, Collections.emptyList(), list.size(), list,
//                list.size());
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
//            if (CollectionUtils.isNotEmpty(queryResponse.getResponses()) && queryResponse.getResponses().get(0) != null) {
//                System.out.println("queryResponse.getResponses().get(0).toString() = " + queryResponse.getResponses().get(0).toString());
//            }
            String value = jsonObjectWriter.writeValueAsString(queryResponse);
            ResponseBuilder ok = Response.ok(value, MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8"));
            return buildResponse(ok);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing queryResponse object", e);
            return createErrorResponse("", "Error parsing QueryResponse object:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    protected Response createJsonResponse(Object obj) {
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(obj),
                    MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")));
        } catch (JsonProcessingException e) {
            logger.error("Error parsing object", e);
            return createErrorResponse("", "Error parsing object:\n" + Arrays.toString(e.getStackTrace()));
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


}
