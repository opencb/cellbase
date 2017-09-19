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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.map.HashedMap;
import org.opencb.cellbase.core.api.CellBaseDBAdaptor;
import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.config.SpeciesProperties;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by imedina on 04/08/15.
 */
@Path("/{version}/meta")
@Produces("application/json")
@Api(value = "Meta", description = "Meta RESTful Web Services API")
public class MetaWSServer extends GenericRestWSServer {

    private static final String PONG = "pong";
    private static final String STATUS = "status";
    private static final String HEALTH = "health";
    private static final String LOCALHOST_REST_API = "http://localhost:8080/cellbase";

    public MetaWSServer(@PathParam("version")
                        @ApiParam(name = "version", value = "Possible values: v3, v4",
                                defaultValue = "v4") String version,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, uriInfo, hsr);
    }


    @GET
    @Path("/{species}/versions")
    @ApiOperation(httpMethod = "GET", value = "Returns source version metadata, including source urls from which "
            + "data files were downloaded.",
            response = DownloadProperties.class, responseContainer = "QueryResponse")
    public Response getVersion(@PathParam("species")
                               @ApiParam(name = "species",
                                       value = "Name of the species, e.g.: hsapiens. For a full list of potentially"
                                               + "available species ids, please refer to: "
                                               + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species",
                                        required = true) String species) {
        CellBaseDBAdaptor metaDBAdaptor = dbAdaptorFactory.getMetaDBAdaptor(species, this.assembly);
        return createOkResponse(metaDBAdaptor.nativeGet(new Query(), new QueryOptions()));
    }

    @GET
    @Path("/species")
    @ApiOperation(httpMethod = "GET", value = "Returns all potentially available species. Please note that not all of "
            + " them may be available in this particular CellBase installation.",
            response = SpeciesProperties.class, responseContainer = "QueryResponse")
    public Response getSpecies() {
        return getAllSpecies();
    }

    /*
     * Auxiliar methods
     */
    @GET
    @Path("/{category}")
    @ApiOperation(httpMethod = "GET", value = "Returns available subcategories for a given category",
            response = String.class, responseContainer = "QueryResponse")
    public Response getCategory(@PathParam("category")
                                @ApiParam(name = "category", value = "String containing the name of the caregory",
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
            response = SpeciesProperties.class, responseContainer = "QueryResponse", hidden = true)
    public Response getSubcategory(@PathParam("category") String category,
                                   @PathParam("subcategory") String subcategory) {
        return getCategory(category);
    }

    private Response getAllSpecies() {
        try {
            QueryResult queryResult = new QueryResult();
            queryResult.setId("species");
            queryResult.setDbTime(0);
            queryResult.setResult(Arrays.asList(cellBaseConfiguration.getSpecies()));
            return createOkResponse(queryResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GET
    @Path("/about")
    @ApiOperation(httpMethod = "GET", value = "Returns info about current CellBase code.",
            response = SpeciesProperties.class, responseContainer = "QueryResponse")
    public Response getAbout() {
        Map<String, String> info = new HashMap<>(3);
        info.put("Program: ", "CellBase (OpenCB)");
        info.put("Version: ", GitRepositoryState.get().getBuildVersion());
        info.put("Git branch: ", GitRepositoryState.get().getBranch());
        info.put("Git commit: ", GitRepositoryState.get().getCommitId());
        info.put("Description: ", "High-Performance NoSQL database and RESTful web services to access the most relevant biological data");
        QueryResult queryResult = new QueryResult();
        queryResult.setId("about");
        queryResult.setDbTime(0);
        queryResult.setResult(Collections.singletonList(info));

        return createOkResponse(queryResult);
    }

    @GET
    @Path("/ping")
    @ApiOperation(httpMethod = "GET", value = "Checks if the app is alive. Returns pong.",
            response = SpeciesProperties.class, responseContainer = "QueryResponse")
    public Response ping() {
        QueryResult queryResult = new QueryResult();
        queryResult.setId(PONG);
        queryResult.setDbTime(0);
        queryResult.setResult(Collections.emptyList());

        return createOkResponse(queryResult);
    }

    @GET
    @Path("/status")
    @ApiOperation(httpMethod = "GET", value = "Reports on the overall system status based on the status of such things "
            + "as database connections and the ability to access other API's.",
            response = SpeciesProperties.class, responseContainer = "QueryResponse")
    public Response status() {
        Monitor monitor = new Monitor(LOCALHOST_REST_API, dbAdaptorFactory);
        Health health = monitor.run();

        QueryResult queryResult = new QueryResult();
        queryResult.setId(STATUS);
        queryResult.setDbTime(-1);
        Map<String, Health> healthMap = new HashMap<String, Health>(1);
        healthMap.put(HEALTH, health);
        queryResult.setResult(healthMap);

        return createOkResponse(queryResult);
    }


}
