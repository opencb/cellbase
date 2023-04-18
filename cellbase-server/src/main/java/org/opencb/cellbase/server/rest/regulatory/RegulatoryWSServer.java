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

package org.opencb.cellbase.server.rest.regulatory;

import io.swagger.annotations.*;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.api.RegulationQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.managers.RegulatoryManager;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static org.opencb.cellbase.core.ParamConstants.*;

@Path("/{apiVersion}/{species}/regulatory")
@Produces("text/plain")
@Api(value = "Regulation", description = "Gene expression regulation RESTful Web Services API")
public class RegulatoryWSServer extends GenericRestWSServer {

    private RegulatoryManager regulatoryManager;

    public RegulatoryWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION,
            defaultValue = DEFAULT_VERSION) String apiVersion,
                              @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                              @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly")
                                      String assembly,
                              @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0")
                              @QueryParam("dataRelease") int dataRelease,
                              @ApiParam(name = "token", value = DATA_ACCESS_TOKEN_DESCRIPTION) @DefaultValue("") @QueryParam("token")
                                      String token,
                              @Context UriInfo uriInfo,
                              @Context HttpServletRequest hsr) throws QueryException, IOException, CellBaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }

        regulatoryManager = cellBaseManagerFactory.getRegulatoryManager(species, assembly);
    }

    @GET
    @Path("/distinct")
    @ApiOperation(httpMethod = "GET", notes = "Gets a unique list of values, e.g. biotype or chromosome",
            value = "Get a unique list of values for a given field.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),

            @ApiImplicitParam(name = "featureType",
                    value = REGULATION_FEATURE_TYPES,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getUniqueValues(@QueryParam("field") @ApiParam(name = "field", required = true,
            value = "Name of column to return, e.g. featureType") String field) {
        try {
            copyToFacet("field", field);
            RegulationQuery query = new RegulationQuery(uriParams);
            CellBaseDataResult<String> queryResults = regulatoryManager.distinct(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/featureType")
    @ApiOperation(httpMethod = "GET", value = "Retrieves a list of available regulatory feature types",
            response = String.class, responseContainer = "QueryResponse", hidden = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getFeatureTypes() {
        try {
            RegulationQuery query = new RegulationQuery(uriParams);
            query.setFacet("featureType");
            CellBaseDataResult<String> queryResults = regulatoryManager.distinct(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/featureClass")
    @ApiOperation(httpMethod = "GET", value = "Retrieves a list of available regulatory feature classes",
            response = String.class, responseContainer = "QueryResponse", hidden = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "featureType",
                    value = REGULATION_FEATURE_TYPES,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getFeatureClasses() {
        try {
            RegulationQuery query = new RegulationQuery(uriParams);
            query.setFacet("featureClass");
            CellBaseDataResult queryResults = regulatoryManager.distinct(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Retrieves all regulatory elements", response = RegulatoryFeature.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "featureType",
                    value = REGULATION_FEATURE_TYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getAll() {
        try {
            RegulationQuery query = new RegulationQuery(uriParams);
            CellBaseDataResult<RegulatoryFeature> queryResults = regulatoryManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
