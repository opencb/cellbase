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

package org.opencb.cellbase.server.ws.network;

import org.opencb.biodata.models.protein.Interaction;
import org.opencb.cellbase.core.api.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/{version}/{species}/network/protein")
@Produces(MediaType.APPLICATION_JSON)
public class ProteinProteinInteractionWSServer extends GenericRestWSServer {

    public ProteinProteinInteractionWSServer(@PathParam("version") String version, @PathParam("species") String species,
                                             @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    public Response getModel() {
        return createModelResponse(Interaction.class);
    }

    @GET
    @Path("/all")
    public Response getAllPPI(@DefaultValue("") @QueryParam("interactor") String interactor,
                              @DefaultValue("") @QueryParam("type") String type,
                              @DefaultValue("") @QueryParam("database") String database,
                              @DefaultValue("") @QueryParam("status") String status,
                              @DefaultValue("") @QueryParam("detectionMethod") String detectionMethod) {
        try {
            parseQueryParams();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor =
                    dbAdaptorFactory2.getProteinProteinInteractionDBAdaptor(this.species, this.assembly);
            return createOkResponse(ppiDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{interaction}/info")
    public Response getPPIByInteractionId(@PathParam("interaction") String interaction) {
        try {
            parseQueryParams();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor =
                    dbAdaptorFactory2.getProteinProteinInteractionDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(interaction, ProteinProteinInteractionDBAdaptor.QueryParams.INTERACTOR_A_XREFS.key());
            List<QueryResult> queryResults = ppiDBAdaptor.nativeGet(queries, queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // TODO Correct method
    @GET
    @Path("/{interaction}/interactors")
    public Response getInteractorsByInteractionId(@PathParam("interaction") String interaction,
                                                  @DefaultValue("interactorA,interactorB") @QueryParam("include") String include) {
        try {
            parseQueryParams();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor =
                    dbAdaptorFactory2.getProteinProteinInteractionDBAdaptor(this.species, this.assembly);
            queryOptions.put("include", "interactorA,interactorB");
            List<Query> queries = createQueries(interaction, ProteinProteinInteractionDBAdaptor.QueryParams.INTERACTOR_A_XREFS.key());
            List<QueryResult> queryResults = ppiDBAdaptor.nativeGet(queries, queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
