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
import org.opencb.cellbase.core.api.ClinicalVariantQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.managers.ClinicalManager;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static org.opencb.cellbase.core.ParamConstants.*;

/**
 * Created by fjlopez on 06/12/16.
 */
@Path("/{apiVersion}/{species}/clinical")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Clinical", description = "Clinical RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {

    private ClinicalManager clinicalManager;

    public ClinicalWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION,
            defaultValue = DEFAULT_VERSION) String apiVersion,
                            @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                            @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly")
                                    String assembly,
                            @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0") @QueryParam("dataRelease")
                                    int dataRelease,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws QueryException, IOException, CellBaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }

        clinicalManager = cellBaseManagerFactory.getClinicalManager(species, assembly);
    }

    @GET
    @Path("/variant/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + DOT_NOTATION_NOTE,
            value = "Retrieves all clinical variants", response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = DATA_TOKEN_PARAM, value = DATA_TOKEN_DESCRIPTION,
                    required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = SOURCE_PARAM, value = SOURCE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = SEQUENCE_ONTOLOGY_PARAM, value = SEQUENCE_ONTOLOGY_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = FEATURE_IDS_PARAM, value = FEATURE_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRAITS_PARAM, value = TRAITS_DESCRIPTION,
                    required = false, dataType = "String", paramType = "query"),
//            @ApiImplicitParam(name = ParamConstants.VARIANT_ACCESSIONS_PARAM, value = ParamConstants.VARIANT_ACCESSIONS_DESCRIPTION,
//                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = VARIANT_IDS_PARAM, value = VARIANT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = VARIANT_TYPES_PARAM, value = VARIANT_TYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = CONSISTENCY_STATUS_PARAM, value = CONSISTENCY_STATUS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = CLINICAL_SIGNFICANCE_PARAM, value = CLINICAL_SIGNFICANCE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = MODE_INHERITANCE_PARAM, value = MODE_INHERITANCE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ALLELE_ORIGIN_PARAM, value = ALLELE_ORIGIN_DESCRIPTION,
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
            ClinicalVariantQuery query = new ClinicalVariantQuery(uriParams);
            CellBaseDataResult<Variant> queryResults = clinicalManager.search(query);

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
