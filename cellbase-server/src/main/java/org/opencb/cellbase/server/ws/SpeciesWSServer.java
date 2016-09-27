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
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
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

/**
 * Created by imedina on 04/08/15.
 */
@Path("/{version}/{species}")
@Produces("application/json")
@Api(value = "Species", description = "Species RESTful Web Services API")
public class SpeciesWSServer extends GenericRestWSServer {

    public SpeciesWSServer(@PathParam("version")
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
    @Path("/info")
    @ApiOperation(httpMethod = "GET",
            value = "Retrieves info about current species chromosomes.", response = Chromosome.class,
            responseContainer = "QueryResponse")
    public Response getSpeciesInfo() {
        try {
            GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(species, this.assembly);
            QueryResult queryResult = genomeDBAdaptor.getGenomeInfo(queryOptions);
            queryResult.setId(species);
            return createOkResponse(queryResult);
        } catch (com.mongodb.MongoException e) {
            e.printStackTrace();
            return null;
        }
    }

}
