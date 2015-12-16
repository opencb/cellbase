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

package org.opencb.cellbase.server.ws.genomic;

import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

@Path("/{version}/{species}/genomic/position")
@Produces(MediaType.APPLICATION_JSON)
@Deprecated
public class PositionWSServer extends GenericRestWSServer {

    public PositionWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                            @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{geneId}/gene")
    public Response getGeneByPosition(@PathParam("geneId") String id) {
        try {
            parseQueryParams();
            List<Position> positionList = Position.parsePositions(id);
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getGeneByPosition", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/transcript")
    public Response getTranscriptByPosition(@PathParam("geneId") String id) {
        try {
            parseQueryParams();
            List<Position> positionList = Position.parsePositions(id);
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
            return createOkResponse(transcriptDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptByPosition", e.toString());
        }
    }

    @GET
    @Path("/{position}/snp")
    public Response getSNPByPositionByGet(@PathParam("position") String query) {
        return this.getSNPByPosition(query);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/snp")
    public Response getSNPByPositionByPost(@FormParam("position") String query) {
        return this.getSNPByPosition(query);
    }

    private Response getSNPByPosition(String query) {
        try {
            parseQueryParams();
            List<Position> positionList = Position.parsePositions(query);
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.nativeGet(positionList, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    public Response defaultMethod() {
        return help();
    }

    @GET
    @Path("/help")
    public Response help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input:\n");
        sb.append("Chr. position format: chr:position (i.e.: 3:10056).\n\n\n");
        sb.append("Resources:\n");
        sb.append("- gene: Suppose we are interested in a particular position in the genome, for instance Chromosome 1 position 150193064, "
                + "and we want to know if there is any gene including this position.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- snp: Imagine now that we have a list of positions and we are interested in identifying those that are known SNPs.\n");
        sb.append(" Output columns: rsID, chromosome, position, Ensembl consequence type, SO consequence type, sequence.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Position");

        return createOkResponse(sb.toString());
    }
}
