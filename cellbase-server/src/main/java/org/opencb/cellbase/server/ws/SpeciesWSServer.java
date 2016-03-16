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
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.datastore.core.QueryResponse;

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

    public SpeciesWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves genome info for current species", response = QueryResponse.class)
    public Response getSpeciesInfo() {
        try {
            GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, this.assembly);
            return createOkResponse(genomeDBAdaptor.getGenomeInfo(queryOptions));
        } catch (com.mongodb.MongoException e) {
            e.printStackTrace();
            return null;
        }
    }

}
