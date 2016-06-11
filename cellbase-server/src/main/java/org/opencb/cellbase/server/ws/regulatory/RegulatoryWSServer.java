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

package org.opencb.cellbase.server.ws.regulatory;

import io.swagger.annotations.*;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/{species}/regulatory")
@Produces("text/plain")
@Api(value = "Regulation", description = "Gene expression regulation RESTful Web Services API")
public class RegulatoryWSServer extends GenericRestWSServer {

    public RegulatoryWSServer(@PathParam("version")
                              @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                      defaultValue = "latest") String version,
                              @PathParam("species")
                              @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                      + "of potentially available species ids, please refer to: "
                                      + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                              @Context UriInfo uriInfo,
                              @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/featureType")
    @ApiOperation(httpMethod = "GET", value = "Retrieves a list of available regulatory feature types",
            response = String.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "featureClass",
                    value = "Comma separated list of regulatory region classes, e.g.: "
                            + "Histone,Transcription Factor. Exact text matches will be returned. For a full"
                            + "list of available regulatory types: "
                            + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/hsapiens/regulatory/featureClass",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getFeatureTypes() {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory2.getRegulationDBAdaptor(this.species, this.assembly);
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "featureType",
                    value = "Comma separated list of regulatory region types, e.g.: "
                            + "TF_binding_site,histone_acetylation_site. Exact text matches will be returned. For a full"
                            + "list of available regulatory types: "
                            + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/hsapiens/regulatory/featureType\n ",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getFeatureClasses() {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory2.getRegulationDBAdaptor(this.species, this.assembly);
            return createOkResponse(regulationDBAdaptor.distinct(query, "featureClass"));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
