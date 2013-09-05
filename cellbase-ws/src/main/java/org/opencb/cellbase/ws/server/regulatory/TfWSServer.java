package org.opencb.cellbase.ws.server.regulatory;

import org.bioinfo.commons.utils.StringUtils;
import org.bioinfo.formats.parser.uniprot.v140jaxb.DbReferenceType;
import org.bioinfo.formats.parser.uniprot.v140jaxb.FeatureType;
import org.bioinfo.formats.parser.uniprot.v140jaxb.Protein;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.common.core.Transcript;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.ProteinDBAdaptor;
import org.opencb.cellbase.core.lib.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.ws.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Path("/{version}/{species}/regulatory/tf")
@Produces("text/plain")
public class TfWSServer extends RegulatoryWSServer {

	public TfWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}

	
	
	@GET
	@Path("/{tfId}/info")
	public Response getTfInfo(@PathParam("tfId") String query) {
		try {
			checkVersionAndSpecies();
			ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.version);
			return generateResponse(query, adaptor.getAllByGeneNameList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getTfInfo", e.toString());
		}
	}
	

	@GET
	@Path("/{tfId}/fullinfo") // Devuelve los TFBSs para el TFId que le das
	public Response getTfFullInfo(@PathParam("tfId") String query) {
		try {
			checkVersionAndSpecies();
			ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.version);
			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
			TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.version);
			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
			
			List<List<Gene>> geneListList = geneDBAdaptor.getAllByTfNameList(StringUtils.toList(query, ","));
			List<String> ensemblGeneList = new ArrayList<String>();
			List<String> externalNameList = new ArrayList<String>();
			for(List<Gene> geneList : geneListList) {
				if(geneList != null && geneList.size() > 0) {
					ensemblGeneList.add(geneList.get(0).getId());
					externalNameList.add(geneList.get(0).getName());
				}else {
					ensemblGeneList.add("");
					externalNameList.add("");
				}
			}
			
			List<List<Protein>> proteinList = proteinDBAdaptor.getAllByGeneNameList(externalNameList);
			List<List<Transcript>> transcriptList = transcriptDBAdaptor.getAllByProteinNameList(externalNameList);
			List<List<Gene>> targetGeneList = geneDBAdaptor.getAllByTfList(StringUtils.toList(query, ","));
//			List<List<Pwm>> pwmGeneList =  tfbsDBAdaptor.getAllPwmByTfGeneNameList(StringUtils.toList(query, ","));
			
			List<List<DbReferenceType>> proteinXrefList = proteinDBAdaptor.getAllProteinXrefsByProteinNameList(externalNameList);
			List<List<FeatureType>> proteinFeature = proteinDBAdaptor.getAllProteinFeaturesByProteinXrefList(externalNameList);
			
			StringBuilder response = new StringBuilder();
			response.append("[");
			for (int i = 0; i < geneListList.size(); i++) {
				if(geneListList.get(i).size() > 0){
					response.append("{");
//					response.append("\"proteins\":"+gson.toJson(proteinList.get(i))+",");
//					response.append("\"gene\":"+gson.toJson(geneListList.get(i).get(0))+",");
//					response.append("\"transcripts\":"+gson.toJson(transcriptList.get(i))+",");
//					response.append("\"pwm\":"+gson.toJson(pwmGeneList.get(i))+",");
//					response.append("\"targetGenes\":"+gson.toJson(targetGeneList.get(i))+",");
//					response.append("\"protein_xref\":"+gson.toJson(proteinXrefList.get(i))+",");
//					response.append("\"protein_feature\":"+gson.toJson(proteinFeature.get(i))+"");
					response.append("},");
				}else{
					response.append("null,");
				}
			}
			response.append("]");
			//Remove the last comma
			response.replace(response.length()-2, response.length()-1, "");
			
			return  generateResponse(query,Arrays.asList(response));
			
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getTfFullInfo", e.toString());
		}
	}
	
	@GET
	@Path("/{tfId}/tfbs")
	public Response getAllByTfbs(@PathParam("tfId") String query, @DefaultValue("")@QueryParam("celltype") String celltype, @DefaultValue("-2500")@QueryParam("start") String start, @DefaultValue("500")@QueryParam("end") String end) {
		try {
			checkVersionAndSpecies();
			TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
			if(celltype == null || celltype.equals("")) {
				celltype = null;
			}
			int iStart = -2500;
			int iEnd = 500;
			try {
				if (start != null && end  != null) {
					iStart = Integer.parseInt(start);
					iEnd = Integer.parseInt(end);
				}
			}catch(NumberFormatException e) {
				e.printStackTrace();
				return createErrorResponse("getAllByTfbs", e.toString());
			}
//			return createOkResponse(adaptor.getAllByIdList(StringUtils.toList(query, ","), celltype, iStart, iEnd));
			return createOkResponse(adaptor.getAllByIdList(StringUtils.toList(query, ","), queryOptions));
//			return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAllByTfbs", e.toString());
		}
	}
	
	
	@GET
	@Path("/{tfId}/gene")
	public Response getEnsemblGenes(@PathParam("tfId") String query) {
		try {
			checkVersionAndSpecies();
			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
			return  generateResponse(query, "GENE", geneDBAdaptor.getAllByTfList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getEnsemblGenes", e.toString());
		}
	}
	
	@GET
	@Path("/{tfId}/target_gene")
	public Response getTargetGenes(@PathParam("tfId") String query) {
		try {
			checkVersionAndSpecies();
			GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
			return  generateResponse(query, "GENE", geneDBAdaptor.getAllTargetsByTfList(StringUtils.toList(query, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getEnsemblGenes", e.toString());
		}
	}
	
//	@GET
//	@Path("/{tfId}/pwm")
//	public Response getAllPwms(@PathParam("tfId") String query) {
//		try {
//			checkVersionAndSpecies();
//			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
//			return generateResponse(query, tfbsDBAdaptor.getAllPwmByTfGeneNameList(StringUtils.toList(query, ",")));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return createErrorResponse("getAllPwms", e.toString());
//		}
//	}
	
	
	@GET
	@Path("/annotation")
	public Response getAnnotation(@DefaultValue("")@QueryParam("celltype") String celltype) {
		try {
			checkVersionAndSpecies();
			TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
			List<Object> results;
			if (celltype.equals("")){
				results = tfbsDBAdaptor.getAllAnnotation();
			}
			else{
				results = tfbsDBAdaptor.getAllAnnotationByCellTypeList(StringUtils.toList(celltype, ","));
			}
			List<String> lista = new ArrayList<String>();			
			
			for (Object result : results) {
				lista.add(((Object [])result)[0].toString()+"\t" + ((Object [])result)[1].toString());
			}
			return  generateResponse(new String(), lista);
			
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAnnotation", e.toString());
		}
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
		sb.append("All id formats are accepted.\n\n\n");
		sb.append("Resources:\n");
		sb.append("- info: Get information about this transcription factor (TF).\n\n");
		sb.append("- tfbs: Get all transcription factor binding sites (TFBSs) for this TF.\n");
		sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
		sb.append("- gene: Get all genes regulated by this TF.\n");
		sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
		sb.append("- pwm: Get all position weight matrices associated to this TF.\n");
		sb.append(" Output columns: TF Name, type, frequency_matrix, description, source, length, jaspar_accession.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Regulatory_rest_ws_api#Transcription-Factor");
		
		return createOkResponse(sb.toString());
	}
}
