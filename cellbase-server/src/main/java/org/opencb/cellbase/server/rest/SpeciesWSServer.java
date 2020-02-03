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

import com.mongodb.MongoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;

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
@Path("/{version}/{species}")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Species", description = "Species RESTful Web Services API")
public class SpeciesWSServer extends GenericRestWSServer {

    public SpeciesWSServer(@PathParam("version")
                           @ApiParam(name = "version", value = ParamConstants.VERSION_DESCRIPTION,
                                   defaultValue = ParamConstants.DEFAULT_VERSION) String version,
                           @PathParam("species")
                           @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                           @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException,
            CellbaseException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/info")
    @ApiOperation(httpMethod = "GET",
            value = "Retrieves info about current species chromosomes.", response = Chromosome.class,
            responseContainer = "QueryResponse")
    public Response getSpeciesInfo(@QueryParam("exclude")
                                   @ApiParam(value = "Set which fields are excluded in the response, "
                                           + "e.g.: transcripts.exons.") String exclude,
                                   @QueryParam("include")
                                   @ApiParam(value = "Set which fields are include in the response, "
                                           + "e.g.: transcripts.exons.") String include,
                                   @QueryParam("sort")
                                   @ApiParam(value = "Sort returned results by a certain data model attribute.")
                                               String sort) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseQueryParams();
            GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, this.assembly);
            CellBaseDataResult queryResult = genomeDBAdaptor.getGenomeInfo(queryOptions);
            queryResult.setId(species);
            return createOkResponse(queryResult);
        } catch (MongoException | CellbaseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
