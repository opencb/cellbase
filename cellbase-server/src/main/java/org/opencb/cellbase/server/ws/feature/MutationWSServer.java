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

package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.db.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 25/11/13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
@Path("/{version}/{species}/feature/mutation")
@Produces("application/json")
@Deprecated
public class MutationWSServer extends GenericRestWSServer {

    public MutationWSServer(@PathParam("version") String version, @PathParam("species") String species,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/list")
    public Response getMutations(@DefaultValue("") @QueryParam("disease") String disease) {
        try {
            parseQueryParams();
            MutationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
            queryOptions.put("disease", Splitter.on(",").splitToList(disease));
            return createOkResponse(variationDBAdaptor.getAll(queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/diseases")
    public Response getMutationDiseases(@PathParam("mutationId") String query) {
        try {
            parseQueryParams();
            MutationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.getAllDiseases(queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{mutationId}/info")
    public Response getByEnsemblId(@PathParam("mutationId") String query) {
        try {
            parseQueryParams();
            MutationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}
