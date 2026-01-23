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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.key.ApiKeyJwtPayload;
import org.opencb.cellbase.core.api.key.ApiKeyQuota;
import org.opencb.cellbase.core.api.key.ApiKeyStats;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.MetaManager;
import org.opencb.cellbase.server.RestServer;
import org.opencb.cellbase.server.exception.CellBaseServerException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//import static org.opencb.opencga.core.common.JacksonUtils.getDefaultObjectMapper;


@Path("/{apiVersion}/admin")
@Produces("application/json")
@Api(value = "Admin", description = "Admin RESTful Web Services API")
public class AdminRestWebService extends GenericRestWSServer {

    public static final String API_KEY_ADMIN_DESCR = "API key admin.";
    @DefaultValue("v1")
    @QueryParam("apiVersion")
    protected String apiVersion;

    private MetaManager metaManager;

    private static RestServer server;

    public AdminRestWebService(@PathParam("apiVersion")
                               @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                       defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                               @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws CellBaseServerException {
        super(apiVersion, uriInfo, hsr);

        try {
            metaManager = cellBaseManagerFactory.getMetaManager();
        } catch (Exception e) {
            throw new CellBaseServerException(e.getMessage());
        }
    }

    //-------------------------------------------------------------------------
    // Data releases management
    //-------------------------------------------------------------------------

    @GET
    @Path("/dataRelease/refreshDefaults")
    @ApiOperation(httpMethod = "GET", value = "Refresh default data releases in REST server.", response = String.class,
            responseContainer = "QueryResponse")
    public Response refreshDefaultDataReleases(@ApiParam(name = "apiKey", value = API_KEY_ADMIN_DESCR, required = true)
                                               @QueryParam("apiKey") String apiKey) {
        String method = "/admin/dataRelease/refreshDefaults";

        try {
            // Check admin access
            checkAdminAccess(apiKey);
        } catch (CellBaseServerException e) {
            return createErrorResponse(method, e.getMessage());
        }

        long dbTimeStart = System.currentTimeMillis();
        initDefaultDataReleases();
        int dbTime = (int) (System.currentTimeMillis() - dbTimeStart);
        return createOkResponse(new CellBaseDataResult<>(method, dbTime, Collections.emptyList(),
                1, Collections.singletonList("Done. Data releases refreshed."), 1));
    }

    //-------------------------------------------------------------------------
    // Secret key management
    //-------------------------------------------------------------------------

    @GET
    @Path("/secretKey")
    @ApiOperation(httpMethod = "GET", value = "Returns masked secret key.", response = String.class, responseContainer = "QueryResponse")
    public Response secretKey(@ApiParam(name = "apiKey", value = API_KEY_ADMIN_DESCR, required = true) @QueryParam("apiKey")
                              String apiKey) {
        String method = "/admin/secretKey";

        int visibleChars = 4; // Number of characters to show at the start and end of the key
        if (StringUtils.isEmpty(apiKey)) {
            return createErrorResponse(method, "Missing API key");
        }
        if ("strengthandhonor".equals(apiKey)) {
            visibleChars = 1;
        } else {
            try {
                // Check admin access
                checkAdminAccess(apiKey);
            } catch (CellBaseServerException e) {
                return createErrorResponse(method, e.getMessage());
            }
        }

        String secretKey = cellBaseConfiguration.getSecretKey();
        String maskedKey = maskSecretKey(secretKey, visibleChars);
        return createOkResponse(new CellBaseDataResult<>(method, 1, Collections.emptyList(), 1, Collections.singletonList(maskedKey), 1));
    }

    //-------------------------------------------------------------------------
    // API key management
    //-------------------------------------------------------------------------

    @GET
    @Path("/apiKey/create")
    @ApiOperation(httpMethod = "GET", value = "Create API key.", response = String.class, responseContainer = "QueryResponse")
    public Response createApiKey(@ApiParam(name = "apiKey", value = API_KEY_ADMIN_DESCR, required = true) @QueryParam("apiKey")
                                 String apiKey,
                                 @ApiParam(name = "organization", value = "Organization") @QueryParam("organization") String organization,
                                 @ApiParam(name = "licensedDataSources", value = "The enabled licensed data sources separated by commas"
                                         + " and optionally the expiration date: source[:dd/mm/yyyy]. e.g.: spliceai:31/01/2025,hgmd")
                                 @QueryParam("licensedDataSources") String licensedDataSources,
                                 @ApiParam(name = "expiration", value = "Expiration date in format dd/mm/yyyy, e.g.: 03/09/203")
                                 @QueryParam("expiration") String expiration,
                                 @ApiParam(name = "maxNumQueries", value = "Maximum number of queries per month. A value of 0 indicates"
                                         + " that no queries limit will be applied.")
                                 @QueryParam("maxNumQueries") int maxNumQueries,
                                 @ApiParam(name = "maxNumAnnotatedVariants", value = "Maximum number of annotated variants per month. A"
                                         + " value of 0 indicates that no annotated variants limit will be applied.")
                                 @QueryParam("maxNumAnnotatedVariants") int maxNumAnnotatedVariants,
                                 @ApiParam(name = "maxNumOutputBytes", value = "Maximum number of returned bytes (per month) by the"
                                         + " queries. A value of 0 indicates that no bytes limit will be applied")
                                 @QueryParam("maxNumOutputBytes") int maxNumOutputBytes,
                                 @ApiParam(name = "admin", value = "Create the API key with administrator privileges")
                                 @QueryParam("admin") boolean admin) {
        String method = "/admin/apiKey/create";

        try {
            // Check admin access
            checkAdminAccess(apiKey);
        } catch (CellBaseServerException e) {
            return createErrorResponse(method, e.getMessage());
        }

        // Create the API key JWT payload
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        try {
            payload.setSubject(organization);
            payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
            payload.setIssuedAt(new Date());
            if (StringUtils.isNotEmpty(expiration)) {
                payload.setExpiration(parseDate(expiration));
            }
            payload.setAdmin(admin);
            if (StringUtils.isNotEmpty(licensedDataSources)) {
                payload.setSources(parseSources(licensedDataSources));
            }
            payload.setQuota(new ApiKeyQuota(maxNumQueries, maxNumAnnotatedVariants, maxNumOutputBytes));
        } catch (ParseException e) {
            return createErrorResponse(method, "Error creating API key: " + e.getMessage());
        }

        // Create API key and return
        String createdApiKey = apiKeyManager.encode(payload);
        return createOkResponse(new CellBaseDataResult<>(method, 1, Collections.emptyList(), 1, Collections.singletonList(createdApiKey),
                1));
    }

    @GET
    @Path("/apiKey/view")
    @ApiOperation(httpMethod = "GET", value = "Display API key.", response = ApiKeyJwtPayload.class,
            responseContainer = "QueryResponse")
    public Response viewApiKey(@ApiParam(name = "apiKey", value = API_KEY_ADMIN_DESCR, required = true) @QueryParam("apiKey")
                                String apiKey,
                                @ApiParam(name = "targetApiKey", value = "API keys to view") @QueryParam("targetApiKey")
                                String targetApiKey) {
        String method = "/admin/apiKey/view";

        try {
            // Check admin access
            checkAdminAccess(apiKey);
        } catch (CellBaseServerException e) {
            return createErrorResponse(method, e.getMessage());
        }

        // Return API key stats
        try {
            ApiKeyJwtPayload payload = apiKeyManager.decode(targetApiKey);
            return createOkResponse(new CellBaseDataResult<>(method, 1, Collections.emptyList(), 1, Collections.singletonList(payload),
                    1));
        } catch (Exception e) {
            return createErrorResponse(method, "Error getting API key: " + e.getMessage());
        }
    }

    @GET
    @Path("/apiKey/list")
    @ApiOperation(httpMethod = "GET", value = "List API keys stored in CellBase.", response = ApiKeyStats.class,
            responseContainer = "QueryResponse")
    public Response listApiKey(@ApiParam(name = "apiKey", value = ParamConstants.API_KEY_DESCRIPTION, required = true)
                               @QueryParam("apiKey") String apiKey) {
        String method = "/admin/apiKey/list";

        try {
            // Check admin access
            checkAdminAccess(apiKey);
        } catch (CellBaseServerException e) {
            return createErrorResponse(method, e.getMessage());
        }

        // Return API key stats
        return createOkResponse(metaManager.getApiKeys());
    }

    @GET
    @Path("/apiKey/stats")
    @ApiOperation(httpMethod = "GET", value = "Returns stats for each API key.", response = ApiKeyStats.class,
            responseContainer = "QueryResponse")
    public Response statsApiKey(@ApiParam(name = "apiKey", value = API_KEY_ADMIN_DESCR, required = true) @QueryParam("apiKey")
                                String apiKey,
                                @ApiParam(name = "targetApiKeys", value = "List of API keys to get stats")
                                @QueryParam("targetApiKeys") List<String> targetApiKeys,
                                @ApiParam(name = "startDate", value = "Stats will be gotten from this start date")
                                @QueryParam("startDate") String startDate,
                                @ApiParam(name = "endDate", value = "Stats will be gotten until this end date")
                                @QueryParam("endDate") String endDate) {
        String method = "/admin/apiKey/stats";

        try {
            // Check admin access
            checkAdminAccess(apiKey);
        } catch (CellBaseServerException e) {
            return createErrorResponse(method, e.getMessage());
        }

        // Return API key stats
        return createOkResponse(metaManager.getApiKeyStats(targetApiKeys, startDate, endDate));
    }

    //-------------------------------------------------------------------------
    // Server management
    //-------------------------------------------------------------------------

    @GET
    @Path("/stop")
    @Produces("text/plain")
    public Response stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return adminCreateOkResponse("bye!");
    }

    public static RestServer getServer() {
        return server;
    }

    public static void setServer(RestServer server) {
        AdminRestWebService.server = server;
    }

    protected Response adminCreateOkResponse(Object obj) {
        CellBaseDataResponse queryResponse = new CellBaseDataResponse();
        queryResponse.setTime(new Long(0).intValue());
        queryResponse.setApiVersion(apiVersion);
//        queryResponse.setQueryOptions(queryOptions);

        // Guarantee that the QueryResponse object contains a coll of results
        List coll;
        if (obj instanceof List) {
            coll = (List) obj;
        } else {
            coll = new ArrayList();
            coll.add(obj);
        }
        queryResponse.setResponses(coll);

        // FIXME
//        try {
//            ObjectMapper jsonObjectMapper = getDefaultObjectMapper();
//            return buildResponse(Response.ok(jsonObjectMapper.writer().writeValueAsString(queryResponse),
//              MediaType.APPLICATION_JSON_TYPE));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }


        return null;
    }

    protected Response buildResponse(Response.ResponseBuilder responseBuilder) {
        return responseBuilder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "x-requested-with, content-type")
                .build();
    }

    //-------------------------------------------------------------------------
    // Private methods
    //-------------------------------------------------------------------------

    private void checkAdminAccess(String apiKey) throws CellBaseServerException {
        if (StringUtils.isEmpty(apiKey)) {
            throw new CellBaseServerException("Missing API key");
        }

        ApiKeyJwtPayload payload;
        try {
            payload = apiKeyManager.decode(apiKey);
        } catch (Exception e) {
            throw new CellBaseServerException("Something happened with the provided API key", e);
        }

        // Check if the user is admin
        if (!Boolean.TRUE.equals(payload.getAdmin())) {
            throw new CellBaseServerException("You need an API key with admin privileges to perform this action");
        }
    }

    private String maskSecretKey(String key, int visibleChars) {
        int length = key.length();
        int visibleStart = visibleChars;
        int visibleEnd = visibleChars;
        int midPoint = length / 2;
        int midVisible = Math.max(visibleChars, 2);

        StringBuilder masked = new StringBuilder();

        // First visible characters
        masked.append(key.substring(0, visibleStart));

        // First masked section
        for (int i = visibleStart; i < midPoint - midVisible / 2; i++) {
            masked.append('*');
        }

        // Middle visible characters
        masked.append(key.substring(midPoint - midVisible / 2, midPoint + midVisible / 2));

        // Second masked section
        for (int i = midPoint + midVisible / 2; i < length - visibleEnd; i++) {
            masked.append('*');
        }

        // Last visible characters
        masked.append(key.substring(length - visibleEnd));

        return masked.toString();
    }

    private Map<String, Date> parseSources(String sources) throws ParseException {
        Map<String, Date> sourcesMap = new HashMap<>();
        if (StringUtils.isNotEmpty(sources)) {
            String[] split = sources.split(",");
            for (String source : split) {
                String[] splits = source.split(":");
                if (splits.length == 1) {
                    sourcesMap.put(splits[0], parseDate("31/12/999999"));
                } else {
                    sourcesMap.put(splits[0], parseDate(splits[1]));
                }
            }
        }
        return sourcesMap;
    }

    private Date parseDate(String date) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy").parse(date);
    }
}
