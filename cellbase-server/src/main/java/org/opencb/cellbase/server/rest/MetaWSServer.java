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

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.config.SpeciesProperties;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.managers.MetaManager;
import org.opencb.cellbase.lib.monitor.HealthStatus;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.clinical.ClinicalWSServer;
import org.opencb.cellbase.server.rest.feature.GeneWSServer;
import org.opencb.cellbase.server.rest.feature.IdWSServer;
import org.opencb.cellbase.server.rest.feature.ProteinWSServer;
import org.opencb.cellbase.server.rest.feature.TranscriptWSServer;
import org.opencb.cellbase.server.rest.genomic.ChromosomeWSServer;
import org.opencb.cellbase.server.rest.genomic.RegionWSServer;
import org.opencb.cellbase.server.rest.genomic.VariantWSServer;
import org.opencb.cellbase.server.rest.regulatory.RegulatoryWSServer;
import org.opencb.cellbase.server.rest.regulatory.TfWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created by imedina on 04/08/15.
 */
@Path("/{version}/meta")
@Produces("application/json")
@Api(value = "Meta", description = "Meta RESTful Web Services API")
public class MetaWSServer extends GenericRestWSServer {

    private MetaManager metaManager;

    private static final String PONG = "pong";
    private static final String STATUS = "status";
    private static final String HEALTH = "health";
    private static final String LOCALHOST_REST_API = "http://localhost:8080/cellbase";


    public MetaWSServer(@PathParam("version")
                        @ApiParam(name = "version", value = "Possible values: v4, v5",
                                defaultValue = "v5") String version,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(version, uriInfo, hsr);
        metaManager = cellBaseManagerFactory.getMetaManager();
    }

    @GET
    @Path("/{species}/versions")
    @ApiOperation(httpMethod = "GET", value = "Returns source version metadata, including source urls from which "
            + "data files were downloaded.", response = DownloadProperties.class, responseContainer = "QueryResponse")
    public Response getVersion(@PathParam("species")
                               @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION, required = true) String species) {
        try {
            MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
            String assemblyName = multivaluedMap.get("assembly").get(0);
            if (StringUtils.isEmpty(assemblyName)) {
                SpeciesConfiguration.Assembly assemblyObject = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species);
                if (assemblyObject != null) {
                    assemblyName = assemblyObject.getName();
                }
            }
            if (!SpeciesUtils.validateSpeciesAndAssembly(cellBaseConfiguration, species, assemblyName)) {
                return createErrorResponse("getVersion", "Invalid species: '" + species + "' or assembly: '"
                        + assemblyName + "'");
            }
            CellBaseDataResult queryResults = metaManager.getVersions(species, assemblyName);
            return createOkResponse(queryResults);
        } catch (CellbaseException e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/species")
    @ApiOperation(httpMethod = "GET", value = "Returns all potentially available species. Please note that not all of "
            + " them may be available in this particular CellBase installation.",
            response = SpeciesProperties.class, responseContainer = "QueryResponse")
    public Response getSpecies() {
        return getAllSpecies();
    }

    @GET
    @Path("/{category}")
    @ApiOperation(httpMethod = "GET", value = "Returns available subcategories for a given category",
            response = String.class, responseContainer = "QueryResponse")
    public Response getCategory(@PathParam("category")
                                @ApiParam(name = "category", value = "String containing the name of the category",
                                            allowableValues = "feature,genomic,network,regulatory", required = true)
                                            String category) {
        if ("feature".equalsIgnoreCase(category)) {
            return createOkResponse("exon\ngene\nkaryotype\nprotein\nsnp\ntranscript");
        }
        if ("genomic".equalsIgnoreCase(category)) {
            return createOkResponse("position\nregion\nvariant");
        }
        if ("network".equalsIgnoreCase(category)) {
            return createOkResponse("pathway");
        }
        if ("regulatory".equalsIgnoreCase(category)) {
            return createOkResponse("mirna_gene\nmirna_mature\ntf");
        }
        return createOkResponse("feature\ngenomic\nnetwork\nregulatory");
    }

    @GET
    @Path("/{category}/{subcategory}")
    @ApiOperation(httpMethod = "GET", value = "To be fixed",
            response = String.class, responseContainer = "QueryResponse", hidden = true)
    public Response getSubcategory(@PathParam("category") String category,
                                   @PathParam("subcategory") String subcategory) {
        return getCategory(category);
    }

    private Response getAllSpecies() {
        try {
            CellBaseDataResult queryResult = new CellBaseDataResult();
            queryResult.setId("species");
            queryResult.setTime(0);
            queryResult.setResults(Arrays.asList(cellBaseConfiguration.getSpecies()));
            return createOkResponse(queryResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/about")
    @ApiOperation(httpMethod = "GET", value = "Returns info about current CellBase code.",
            response = Map.class, responseContainer = "QueryResponse")
    public Response getAbout() {
        Map<String, String> info = new HashMap<>(3);
        info.put("Program", "CellBase (OpenCB)");
        info.put("Version", GitRepositoryState.get().getBuildVersion());
        info.put("Git branch", GitRepositoryState.get().getBranch());
        info.put("Git commit", GitRepositoryState.get().getCommitId());
        info.put("Description", "High-Performance NoSQL database and RESTful web services to access the most relevant biological data");
        CellBaseDataResult queryResult = new CellBaseDataResult();
        queryResult.setId("about");
        queryResult.setTime(0);
        queryResult.setResults(Collections.singletonList(info));

        return createOkResponse(queryResult);
    }

    @GET
    @Path("/ping")
    @ApiOperation(httpMethod = "GET", value = "Checks if the app is alive. Returns pong.",
            response = String.class, responseContainer = "QueryResponse")
    public Response ping() {
        CellBaseDataResult queryResult = new CellBaseDataResult();
        queryResult.setId(PONG);
        queryResult.setTime(0);
        queryResult.setResults(Collections.emptyList());

        return createOkResponse(queryResult);
    }

    @GET
    @Path("/{species}/status")
    @ApiOperation(httpMethod = "GET", value = "Reports on the overall system status based on the status of such things "
            + "as database connections and the ability to access other APIs.",
            response = DownloadProperties.class, responseContainer = "QueryResponse")
    public Response status(@PathParam("species") @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION, required = true)
                                       String species) {

        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
        String assemblyName = multivaluedMap.get("assembly").get(0);
        if (assemblyName == null) {
            try {
                assemblyName = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
            } catch (CellbaseException e) {
                return createErrorResponse("getVersion", "Invalid species: '" + species + "' or assembly: '"
                        + assemblyName + "'");
            }
        }
        if (!SpeciesUtils.validateSpeciesAndAssembly(cellBaseConfiguration, species, assemblyName)) {
            return createErrorResponse("getVersion", "Invalid species: '" + species + "' or assembly: '"
                    + assemblyName + "'");
        }

        HealthStatus health = monitor.run(species, assemblyName);
        CellBaseDataResult<HealthStatus> queryResult = new CellBaseDataResult();
        queryResult.setId(STATUS);
        queryResult.setTime(0);
        queryResult.setNumResults(1);
        queryResult.setResults(Collections.singletonList(health));

        return createOkResponse(queryResult);

    }

    @GET
    @Path("/api")
    @ApiOperation(value = "API", response = Map.class)
    public Response api(@ApiParam(value = "List of categories to get API from, e.g. Xref,Gene") @QueryParam("category") String categoryStr)
    {
        List<LinkedHashMap<String, Object>> api = new ArrayList<>(20);
        Map<String, Class> classes = new LinkedHashMap<>();
        classes.put("clinical", ClinicalWSServer.class);
        classes.put("gene", GeneWSServer.class);
        classes.put("genomeSequence", ChromosomeWSServer.class);
        classes.put("meta", MetaWSServer.class);
        classes.put("protein", ProteinWSServer.class);
        classes.put("region", RegionWSServer.class);
        classes.put("regulation", RegulatoryWSServer.class);
        classes.put("species", SpeciesWSServer.class);
        classes.put("tfbs", TfWSServer.class);
        classes.put("transcript", TranscriptWSServer.class);
        classes.put("variant", VariantWSServer.class);
        classes.put("xref", IdWSServer.class);

        if (StringUtils.isNotEmpty(categoryStr)) {
            for (String category : categoryStr.split(",")) {
                Class clazz = classes.get(category.toLowerCase());
                if (clazz == null) {
                    return createErrorResponse(new CellbaseException("Category not found: " + category));
                }
                LinkedHashMap<String, Object> help = getHelp(clazz);
                api.add(help);
            }
        } else {
            // Get API for all categories
            for (String category : classes.keySet()) {
                api.add(getHelp(classes.get(category)));
            }
        }
        return createOkResponse(new CellBaseDataResult<>(null, 0, Collections.emptyList(), 1,
                Collections.singletonList(api), 1));
    }

    private LinkedHashMap<String, Object> getHelp(Class clazz) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        map.put("name", ((Api) clazz.getAnnotation(Api.class)).value());
        map.put("path", ((Path) clazz.getAnnotation(Path.class)).value());

        List<LinkedHashMap<String, Object>> endpoints = new ArrayList<>(20);
        for (Method method : clazz.getMethods()) {
            Path pathAnnotation = method.getAnnotation(Path.class);
            String httpMethod = "GET";
            if (method.getAnnotation(POST.class) != null) {
                httpMethod = "POST";
            } else {
                if (method.getAnnotation(DELETE.class) != null) {
                    httpMethod = "DELETE";
                }
            }
            ApiOperation apiOperationAnnotation = method.getAnnotation(ApiOperation.class);
            if (pathAnnotation != null && apiOperationAnnotation != null && !apiOperationAnnotation.hidden()) {
                LinkedHashMap<String, Object> endpoint = new LinkedHashMap<>();
                endpoint.put("path", map.get("path") + pathAnnotation.value());
                endpoint.put("method", httpMethod);
                endpoint.put("response", StringUtils.substringAfterLast(apiOperationAnnotation.response().getName()
                        .replace("Void", ""), "."));

                String responseClass = apiOperationAnnotation.response().getName().replace("Void", "");
                endpoint.put("responseClass", responseClass.endsWith(";") ? responseClass : responseClass + ";");
                endpoint.put("notes", apiOperationAnnotation.notes());
                endpoint.put("description", apiOperationAnnotation.value());

                ApiImplicitParams apiImplicitParams = method.getAnnotation(ApiImplicitParams.class);
                List<LinkedHashMap<String, Object>> parameters = new ArrayList<>();
                if (apiImplicitParams != null) {
                    for (ApiImplicitParam apiImplicitParam : apiImplicitParams.value()) {
                        LinkedHashMap<String, Object> parameter = new LinkedHashMap<>();
                        parameter.put("name", apiImplicitParam.name());
                        parameter.put("param", apiImplicitParam.paramType());
                        parameter.put("type", apiImplicitParam.dataType());
                        parameter.put("typeClass", "java.lang." + StringUtils.capitalize(apiImplicitParam.dataType()));
                        parameter.put("allowedValues", apiImplicitParam.allowableValues());
                        parameter.put("required", apiImplicitParam.required());
                        parameter.put("defaultValue", apiImplicitParam.defaultValue());
                        parameter.put("description", apiImplicitParam.value());
                        parameters.add(parameter);
                    }
                }

                Parameter[] methodParameters = method.getParameters();
                if (methodParameters != null) {
                    for (Parameter methodParameter : methodParameters) {
                        ApiParam apiParam = methodParameter.getAnnotation(ApiParam.class);
                        if (apiParam != null && !apiParam.hidden()) {
                            LinkedHashMap<String, Object> parameter = new LinkedHashMap<>();
                            if (methodParameter.getAnnotation(PathParam.class) != null) {
                                parameter.put("name", methodParameter.getAnnotation(PathParam.class).value());
                                parameter.put("param", "path");
                            } else {
                                if (methodParameter.getAnnotation(QueryParam.class) != null) {
                                    parameter.put("name", methodParameter.getAnnotation(QueryParam.class).value());
                                    parameter.put("param", "query");
                                } else {
                                    parameter.put("name", "body");
                                    parameter.put("param", "body");
                                }
                            }

                            // Get type in lower case except for 'body' param
                            String type = methodParameter.getType().getName();
                            String typeClass = type;
                            if (typeClass.contains(".")) {
                                String[] split = typeClass.split("\\.");
                                type = split[split.length - 1];
                                if (!parameter.get("param").equals("body")) {
                                    type = type.toLowerCase();

                                    // Complex type different from body are enums
                                    if (type.contains("$")) {
                                        type = "enum";
                                    }
                                } else {
                                    type = "object";
                                }
                            }
                            parameter.put("type", type);
                            parameter.put("typeClass", typeClass.endsWith(";") ? typeClass : typeClass + ";");
                            parameter.put("allowedValues", apiParam.allowableValues());
                            parameter.put("required", apiParam.required());
                            parameter.put("defaultValue", apiParam.defaultValue());
                            parameter.put("description", apiParam.value());
                            parameters.add(parameter);
                        }
                    }
                }
                endpoint.put("parameters", parameters);
                endpoints.add(endpoint);
            }
        }
        map.put("endpoints", endpoints);
        return map;
    }

}
