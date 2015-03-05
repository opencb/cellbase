package org.opencb.cellbase.server.ws.genomic;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
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

	public PositionWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}

//	@GET
//	@Path("/{positionId}/consequence_type")
//	public Response getConsequenceTypeByPositionGet(@PathParam("positionId") String positionId, @DefaultValue("") @QueryParam("gene") String gene, @DefaultValue("") @QueryParam("transcript") String transcript) {
//		String chromosome = Arrays.asList(positionId.split(":")).get(0);
//		int position = Integer.valueOf(Arrays.asList(positionId.split(":")).get(1));
//		
//		System.out.println("Position: " + position);
//		
//		
//		
//		try {
//			List todo = new ArrayList();
////			List<GenomicRegionFeatures> result = new ArrayList<GenomicRegionFeatures>();
////			
////			
//			GenomicRegionFeatureDBAdaptor adaptor = dbAdaptorFactory.getFeatureMapDBAdaptor(this.species);
////			GenomicRegionFeatures maps = adaptor.getByRegion(new Region(chromosome, position, position));
////			System.out.println(maps.getGenes().size());
////			result.add(maps);
////			
////			todo.addAll(maps.getGenes());
////			todo.addAll(maps.getTranscripts());
////			todo.addAll(maps.getExons());
////			todo.addAll(maps.getSnp());
//			
//			Gson gson = new Gson();
//			todo.add(adaptor.getConsequenceTypeSoAccession(chromosome, position));
//			return generateResponse(positionId, todo);
//			
//			
//			
//		} catch (Exception e) {
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
//	
	
	@GET
	@Path("/{geneId}/gene")
	public Response getGeneByPosition(@PathParam("geneId") String query) {
		try {
			checkParams();
			List<Position> positionList = Position.parsePositions(query);
			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
			QueryOptions queryOptions = new QueryOptions("exclude", null);
			return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
//			return generateResponse(query, geneDBAdaptor.getAllByPositionList(positionList));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getGeneByPosition", e.toString());
		}
	}
	
	@GET
	@Path("/{geneId}/transcript")
	public Response getTranscriptByPosition(@PathParam("geneId") String query) {
		try {
			checkParams();
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
//    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
    @Path("/snp")
    public Response getSNPByPositionByPost(@FormParam("position") String query) {
        return this.getSNPByPosition(query);
    }

    private Response getSNPByPosition(String query) {
        try {
            checkParams();
            List<Position> positionList = Position.parsePositions(query);
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.getAllByPositionList(positionList, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSNPByPosition", e.toString());
        }
    }



//
//    @GET
//    @Path("/{variants}/consequence_type")
//    public Response getConsequenceTypeByPositionByGet(@PathParam("variants") String variants,
//                                                      @DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {
//        try {
//            //			return getConsequenceTypeByPosition(query, features, variation, regulatory, diseases);
//            return getConsequenceTypeByPosition(variants, excludeSOTerms);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
//        }
//    }
//
//    @POST
//    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
//    @Path("/consequence_type")
//    public Response getConsequenceTypeByPositionByPost(@FormDataParam("of") String outputFormat,
//                                                       @FormDataParam("variants") String postQuery,
//                                                       @DefaultValue("") @FormDataParam("exclude") String excludeSOTerms) {
//        //		return getConsequenceTypeByPosition(postQuery, features, variation, regulatory, diseases);
//        return getConsequenceTypeByPosition(postQuery, excludeSOTerms);
//    }


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
	public Response getFunctionalByPositionGet(@PathParam("positionId") String positionId, @DefaultValue("") @QueryParam("source") String source) {
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
	public Response getHelp() {
		return help();
	}
	
	@GET
	@Path("/help")
	public Response help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Input:\n");
		sb.append("Chr. position format: chr:position (i.e.: 3:10056).\n\n\n");
		sb.append("Resources:\n");
		sb.append("- gene: Suppose we are interested in a particular position in the genome, for instance Chromosome 1 position 150193064, and we want to know if there is any gene including this position.\n");
		sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
		sb.append("- snp: Imagine now that we have a list of positions and we are interested in identifying those that are known SNPs.\n");
		sb.append(" Output columns: rsID, chromosome, position, Ensembl consequence type, SO consequence type, sequence.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Position");
		
		return createOkResponse(sb.toString());
	}
}
