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

package org.opencb.cellbase.server.rest.clinical;

import io.swagger.annotations.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.ClinicalManager;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by fjlopez on 06/12/16.
 */
@Path("/{apiVersion}/{species}/clinical")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Clinical", description = "Clinical RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {

    ClinicalManager clinicalManager;

    public ClinicalWSServer(@PathParam("apiVersion")
                            @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                            @PathParam("species")
                            @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                                @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        clinicalManager = cellBaseManagers.getClinicalManager();
    }

    @GET
    @Path("/variant/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. ",
            value = "Retrieves all clinical variants", response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "source", value = ParamConstants.SOURCE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "so", value = ParamConstants.SEQUENCE_ONTOLOGY,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "feature", value = ParamConstants.FEATURE_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "trait", value = ParamConstants.TRAITS,
                    required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "accession", value = ParamConstants.VARIANT_ACCESSIONS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.VARIANT_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "type", value = ParamConstants.VARIANT_TYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "consistencyStatus", value = ParamConstants.CONSISTENCY_STATUS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "clinicalSignificance", value = ParamConstants.CLINICAL_SIGNFICANCE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "modeInheritance", value = ParamConstants.MODE_INHERITANCE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "alleleOrigin", value = ParamConstants.ALLELE_ORIGIN,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAll(@QueryParam("exclude") @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
                           @QueryParam("include") @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
                           @QueryParam("sort") @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort,
                           @QueryParam("limit") @DefaultValue("10") @ApiParam(value = ParamConstants.LIMIT_DESCRIPTION) Integer limit,
                           @QueryParam("skip") @DefaultValue("0") @ApiParam(value = ParamConstants.SKIP_DESCRIPTION)  Integer skip) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseLimitAndSkip(limit, skip);
            parseQueryParams();
            CellBaseDataResult<Variant> queryResults = clinicalManager.search(query, queryOptions, species, assembly);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/alleleOriginLabels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available allele origin labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getAlleleOriginLabels() {
        try {
            return createOkResponse(clinicalManager.getAlleleOriginLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/modeInheritanceLabels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available mode of inheritance labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getModeInheritanceLabels() {
        try {
            return createOkResponse(clinicalManager.getModeInheritanceLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/clinsigLabels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available clinical significance labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getClinicalSignificanceLabels() {
        try {
            return createOkResponse(clinicalManager.getClinsigLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/consistencyLabels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available consistency labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getConsistencyLabels() {
        try {
            return createOkResponse(clinicalManager.getConsistencyLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/type")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available variant types", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getVariantTypes() {
        try {
            return createOkResponse(clinicalManager.getVariantTypes());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}
