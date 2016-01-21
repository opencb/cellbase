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

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryOptions;

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
public class PositionWSServer extends GenericRestWSServer {

    public PositionWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                            @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{geneId}/gene")
    public Response getGeneByPosition(@PathParam("geneId") String query) {
        try {
            parseQueryParams();
            List<Position> positionList = Position.parsePositions(query);
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
            QueryOptions queryOptions = new QueryOptions("exclude", null);
            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getGeneByPosition", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/transcript")
    public Response getTranscriptByPosition(@PathParam("geneId") String query) {
        try {
            parseQueryParams();
            List<Position> positionList = Position.parsePositions(query);
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
            return createOkResponse(transcriptDBAdaptor.getAllByPositionList(positionList, queryOptions));
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
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.getAllByPositionList(positionList, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{positionId}/consequence_type")
    public Response getConsequenceTypeByPositionGet(@PathParam("positionId") String positionId) {
        return getConsequenceTypeByPosition(positionId);
    }

    @POST
    @Path("/{positionId}/consequence_type")
    public Response getConsequenceTypeByPositionPost(@PathParam("positionId") String positionId) {
        return getConsequenceTypeByPosition(positionId);
    }

    @Deprecated
    public Response getConsequenceTypeByPosition(String positionId) {
        List<Position> positionList = Position.parsePositions(positionId);
        return null;
    }

    @GET
    @Path("/{positionId}/functional")
    public Response getFunctionalByPositionGet(@PathParam("positionId") String positionId,
                                               @DefaultValue("") @QueryParam("source") String source) {
        return getFunctionalByPosition(positionId);
    }

    @POST
    @Path("/{positionId}/functional")
    public Response getFunctionalTypeByPositionPost(@PathParam("positionId") String positionId) {
        return getFunctionalByPosition(positionId);
    }

    @Deprecated
    public Response getFunctionalByPosition(@PathParam("positionId") String positionId) {
        List<Position> positionList = Position.parsePositions(positionId);
        return null;
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
