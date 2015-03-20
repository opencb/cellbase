package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.opencb.cellbase.core.lib.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/{species}/feature/protein")
@Produces("text/plain")
@Api(value = "Protein", description = "Protein RESTful Web Services API")
public class ProteinWSServer extends GenericRestWSServer {

	public ProteinWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}
	
//	@GET
//	@Path("/{proteinId}/info")
//	public Response getAllByAccessions(@PathParam("proteinId") String query) {
//		try {
//			checkParams();
//			ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//			return generateResponse(query, "PROTEIN", adaptor.getAllByGeneNameList(Splitter.on(",").splitToList(query)));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return createErrorResponse("getAllByAccessions", e.toString());
//		}
//	}
	
	@GET
	@Path("/{proteinId}/fullinfo")
	public Response getFullInfoByEnsemblId(@PathParam("proteinId") String query, @DefaultValue("") @QueryParam("sources") String sources) {
        try {
            checkParams();
            ProteinDBAdaptor geneDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptsById", e.toString());
        }
	}
	
	@GET
	@Path("/all")
/*
    @ApiOperation(httpMethod = "GET", value = "Get all proteins")
*/
    public Response getAll() {
		try {
			checkParams();
			ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
			
			return createJsonResponse(null);
//			return generateResponse("", "PROTEIN", adaptor.getAll());
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAllByAccessions", e.toString());
		}
	}

    @Deprecated
    @GET
    @Path("/{proteinId}/name")
    public Response getproteinByName(@PathParam("proteinId") String id) {
        try {
            checkParams();
            ProteinDBAdaptor geneDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(id), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptsById", e.toString());
        }
    }

	@GET
	@Path("/{proteinId}/gene")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding to the input protein")
	public Response getGene(@PathParam("proteinId") String query) {
		return null;
	}
	
	@GET
	@Path("/{proteinId}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Get the transcript corresponding to the input protein")
    public Response getTranscript(@PathParam("proteinId") String query) {
		return null;
	}
	
//	@GET
//	@Path("/{proteinId}/feature")
//	public Response getFeatures(@PathParam("proteinId") String query, @DefaultValue("") @QueryParam("type") String type) {
//		try {
//			checkParams();
//			ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//			return generateResponse(query, "PROTEIN_FEATURE", adaptor.getAllProteinFeaturesByProteinXrefList(Splitter.on(",").splitToList(query)));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return createErrorResponse("getFeatures", e.toString());
//		}
//	}

//    @GET
//    @Path("/{proteinName}/function_prediction")
//    public Response getFunctionalPredictions(@PathParam("proteinName") String query, @DefaultValue("") @QueryParam("source") String source) {
//        try {
//            checkParams();
//            queryOptions.put("disease", Splitter.on(",").splitToList(source));
//            ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//            return generateResponse(query, "PROTEIN_FEATURE", adaptor.getAllProteinFeaturesByProteinXrefList(Splitter.on(",").splitToList(query)));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getFeatures", e.toString());
//        }
//    }

//	@GET
//	@Path("/{proteinId}/association")
//	public Response getInteraction(@PathParam("proteinId") String query, @DefaultValue("") @QueryParam("type") String type) {
//		return null;
//	}
	
//	@GET
//	@Path("/{proteinId}/xref")
//	public Response getXrefs(@PathParam("proteinId") String proteinId, @DefaultValue("") @QueryParam("dbname") String dbname) {
//		try {
//			checkParams();
//			ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//			return generateResponse(proteinId, "XREF", adaptor.getAllProteinXrefsByProteinNameList(Splitter.on(",").splitToList(proteinId)));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return createErrorResponse("getXrefs", e.toString());
//		}
//	}

    @Deprecated
	@GET
	@Path("/{proteinId}/reference")
	public Response getReference(@PathParam("proteinId") String query) {
		return null;
	}
	
//	@GET
//	@Path("/{proteinId}/interaction")
//	public Response getInteraction(@PathParam("proteinId") String query, @DefaultValue("") @QueryParam("source") String source) {
//		try {
//			checkParams();
//			ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//			if(source != null && !source.equals("")) {
//				return generateResponse(query, "PROTEIN_INTERACTION", adaptor.getAllProteinInteractionsByProteinNameList(Splitter.on(",").splitToList(query), source));
//			}else{
//				return generateResponse(query, "PROTEIN_INTERACTION", adaptor.getAllProteinInteractionsByProteinNameList(Splitter.on(",").splitToList(query)));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return createErrorResponse("getInteraction", e.toString());
//		}
//	}
	
	@GET
	@Path("/{proteinId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Get the sequence for the given protein")
    public Response getSequence(@PathParam("proteinId") String query) {
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
		sb.append("all id formats are accepted.\n\n\n");
		sb.append("Resources:\n");
		sb.append("- info: Get protein information: name, UniProt ID and description.\n");
		sb.append(" Output columns: UniProt accession, protein name, full name, gene name, organism.\n\n");
		sb.append("- feature: Get particular features for the protein sequence: natural variants in the aminoacid sequence, mutagenesis sites, etc.\n");
		sb.append(" Output columns: feature type, aa start, aa end, original, variation, identifier, description.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Protein");
		
		return createOkResponse(sb.toString());
	}
	
}
