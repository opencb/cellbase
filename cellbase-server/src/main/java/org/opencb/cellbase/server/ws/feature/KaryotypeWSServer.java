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
import org.opencb.cellbase.core.db.api.CytobandDBAdaptor;
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

@Deprecated
@Path("/{version}/{species}/feature/karyotype")
@Produces("text/plain")
public class KaryotypeWSServer extends GenericRestWSServer {


    public KaryotypeWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                             @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{chromosomeName}/cytoband")
    public Response getByChromosomeName(@PathParam("chromosomeName") String chromosome) {
        try {
            parseQueryParams();
            CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.assembly);
            return generateResponse(chromosome, dbAdaptor.getAllByChromosomeList(Splitter.on(",").splitToList(chromosome)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByChromosomeName", e.toString());
        }
    }

    @GET
    @Path("/chromosome")
    public Response getChromosomes() {
        try {
            parseQueryParams();
            CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.assembly);
            return generateResponse("", dbAdaptor.getAllChromosomeNames());
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getChromosomes", e.toString());
        }
    }

    @GET
    @Path("/{chromosomeName}/chromosome")
    public Response getChromosomes(@PathParam("chromosomeName") String query) {
        return getChromosomes();
    }

    @GET
    public Response defaultMethod() {
        return help();
    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }

}
