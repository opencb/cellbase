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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.opencb.biodata.models.core.Exon;
import org.opencb.cellbase.core.db.api.core.ExonDBAdaptor;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/{version}/{species}/feature/exon")
@Produces("text/plain")
@Api(value = "Exon", description = "Exon RESTful Web Services API")
public class ExonWSServer extends GenericRestWSServer {


    public ExonWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }


    @GET
    @Path("/first")
    @Override
    public Response first() {
        ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
        return createOkResponse(exonDBAdaptor.first());
    }

    @GET
    @Path("/count")
    @Override
    public Response count() {
        ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
        return createOkResponse(exonDBAdaptor.count());
    }

    @GET
    @Path("/stats")
    @Override
    public Response stats() {
        return super.stats();
    }


    @GET
    @Path("/{exonId}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get exon information from exon ID")
    public Response getByEnsemblId(@PathParam("exonId") String query) {
        try {
            checkParams();
            ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
//            return  generateResponse(query,exonDBAdaptor.getAllByEnsemblIdList(Splitter.on(",").splitToList(query)));

            return createOkResponse(exonDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }


    @GET
    @Path("/{exonId}/aminos")
/*
    @ApiOperation(httpMethod = "GET", value = "Resource to get the aminoacid sequence from an exon ID")
*/
    public Response getAminoByExon(@PathParam("exonId") String query) {
        try{
            checkParams();
            ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
//            List<Exon> exons = exonDBAdaptor.getAllByEnsemblIdList(Splitter.on(",").splitToList(query));
            List<QueryResult> exons = exonDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions);

            List<String> sequenceList = null;
            if(exons != null) {
                sequenceList = new ArrayList<String>(exons.size());
                for(QueryResult exon : exons) {
//					if(exon != null && "-1".equals(exon.getStrand())) {
//						sequenceList = exonDBAdaptor.getAllSequencesByIdList(Splitter.on(",").splitToList(query), -1);
//					}else {
//						sequenceList = exonDBAdaptor.getAllSequencesByIdList(Splitter.on(",").splitToList(query), 1);
//					}
                }
            }
            return generateResponse(query, sequenceList);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAminoByExon", e.toString());
        }
    }

//	@GET
//	@Path("/{exonId}/sequence")
//	public Response getSequencesByIdList(@DefaultValue("1")@QueryParam("strand")String strand, @PathParam("exonId") String query) {
//		try {
//			if(strand.equals("-1")){
//				return generateResponse(query, Arrays.asList(this.getExonDBAdaptor().getAllSequencesByIdList(StringUtils.toList(query, ","), -1)));
//			}
//			else{
//				return generateResponse(query, Arrays.asList(this.getExonDBAdaptor().getAllSequencesByIdList(StringUtils.toList(query, ","))));
//				
//			}
//		} catch (IOException e) {
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}

    @SuppressWarnings("unchecked")
    @GET
    @Path("/{exonId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Resource to get the DNA sequence from an exon ID")
    public Response getSequencesByIdList(@PathParam("exonId") String query) {
        try {
            checkParams();
            ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
//			return generateResponse(query, Arrays.asList(exonDBAdaptor.getAllSequencesByIdList(Splitter.on(",").splitToList(query))));
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSequencesByIdList", e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/{exonId}/region")
    @ApiOperation(httpMethod = "GET", value = "Resource to get the genetic coordinates of an exon ID")
    public Response getRegionsByIdList(@PathParam("exonId") String query) {
        try {
            checkParams();
            ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
//			return generateResponse(query, Arrays.asList(exonDBAdaptor.getAllRegionsByIdList(Splitter.on(",").splitToList(query))));
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getRegionsByIdList", e.toString());
        }
    }

    @GET
    @Path("/{exonId}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Resource to get the transcripts that include an exon ID")
    public Response getTranscriptsByEnsemblId(@PathParam("exonId") String query) {
        try {
            checkParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
            return createOkResponse(transcriptDBAdaptor.getAllByEnsemblExonIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptsByEnsemblId", e.toString());
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
        sb.append("all id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get exon information: name and location.\n");
        sb.append(" Output columns: Ensembl ID, chromosome, start, end, strand.\n\n");
        sb.append("- sequence: Get exon sequence.\n\n");
        sb.append("- trancript: Get all transcripts which contain this exon.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Exon");

        return createOkResponse(sb.toString());
    }
}
