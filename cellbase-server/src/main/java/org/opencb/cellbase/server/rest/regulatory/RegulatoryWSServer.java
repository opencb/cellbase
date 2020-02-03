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
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/{species}/regulatory")
@Produces("text/plain")
@Api(value = "Regulation", description = "Gene expression regulation RESTful Web Services API")
public class RegulatoryWSServer extends GenericRestWSServer {

    public RegulatoryWSServer(@PathParam("version")
                              @ApiParam(name = "version", value = ParamConstants.VERSION_DESCRIPTION,
                                      defaultValue = ParamConstants.DEFAULT_VERSION) String version,
                              @PathParam("species")
                              @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                              @Context UriInfo uriInfo,
                              @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException, CellbaseException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/featureType")
    @ApiOperation(httpMethod = "GET", value = "Retrieves a list of available regulatory feature types",
            response = String.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "featureClass",
                    value = "Comma separated list of regulatory region classes, e.g.: "
                            + "Histone,Transcription Factor. Exact text matches will be returned. For a full"
                            + "list of available regulatory types: "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/regulatory/featureClass",
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getFeatureTypes() {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(this.species, this.assembly);
            return createOkResponse(regulationDBAdaptor.distinct(query, "featureType"));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/featureClass")
    @ApiOperation(httpMethod = "GET", value = "Retrieves a list of available regulatory feature classes",
            response = String.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "featureType",
                    value = "Comma separated list of regulatory region types, e.g.: "
                            + "TF_binding_site,histone_acetylation_site. Exact text matches will be returned. For a full"
                            + "list of available regulatory types: "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/regulatory/featureType\n ",
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getFeatureClasses() {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(this.species, this.assembly);
            return createOkResponse(regulationDBAdaptor.distinct(query, "featureClass"));
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
            @ApiImplicitParam(name = "count",
                    value = "Get a count of the number of results obtained.",
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "featureType",
                    value = "Comma separated list of regulatory region types, e.g.: "
                            + "TF_binding_site,histone_acetylation_site. Exact text matches will be returned. For a full"
                            + "list of available regulatory types: "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/hsapiens/regulatory/featureType\n ",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "featureClass",
                    value = "Comma separated list of regulatory region classes, e.g.: "
                            + "Histone,Transcription Factor. Exact text matches will be returned. For a full"
                            + "list of available regulatory types: "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/hsapiens/regulatory/featureClass",
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAll(@QueryParam("exclude")
                           @ApiParam(value = "Set which fields are excluded in the response, e.g.: transcripts.exons.") String exclude,
                           @QueryParam("include")
                           @ApiParam(value = "Set which fields are include in the response, e.g.: transcripts.exons.") String include,
                           @QueryParam("sort")
                           @ApiParam(value = "Sort returned results by a certain data model attribute.") String sort,
                           @QueryParam("limit") @DefaultValue("10")
                           @ApiParam(value = "Max number of results to be returned. Cannot exceed 5,000.") Integer limit,
                           @QueryParam("skip") @DefaultValue("0")
                           @ApiParam(value = "Number of results to be skipped.")  Integer skip) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseLimitAndSkip(limit, skip);
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(this.species, this.assembly);
            return createOkResponse(regulationDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
