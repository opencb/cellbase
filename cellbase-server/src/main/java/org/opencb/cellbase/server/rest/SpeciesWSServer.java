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
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.GenomeQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.GenomeManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by imedina on 04/08/15.
 */
@Path("/{apiVersion}/{species}")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Species", description = "Species RESTful Web Services API")
public class SpeciesWSServer extends GenericRestWSServer {

    private GenomeManager genomeManager;

    public SpeciesWSServer(@PathParam("apiVersion")
                           @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                   defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                           @PathParam("species")
                           @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                           @ApiParam(name = "assembly", value = ParamConstants.ASSEMBLY_DESCRIPTION)
                           @DefaultValue("")
                           @QueryParam("assembly") String assembly,
                           @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws QueryException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        genomeManager = cellBaseManagerFactory.getGenomeManager(species, assembly);
    }

    @GET
    @Path("/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves info about current species chromosomes.", response = Chromosome.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getSpeciesInfo() {
        try {
            GenomeQuery query = new GenomeQuery(uriParams);
            CellBaseDataResult queryResults = genomeManager.getGenomeInfo(query.toQueryOptions());
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
