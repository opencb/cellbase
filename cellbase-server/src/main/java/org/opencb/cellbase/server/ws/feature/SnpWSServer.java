package org.opencb.cellbase.server.ws.feature;


import com.google.common.base.Splitter;
import org.opencb.cellbase.core.lib.api.SnpDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import org.bioinfo.cellbase.lib.common.variation.Snp;

@Path("/{version}/{species}/feature/snp")
@Produces("text/plain")
public class SnpWSServer extends GenericRestWSServer {

    private List<String> exclude = new ArrayList<>();

	public SnpWSServer(@PathParam("version") String version, @PathParam("species") String species,
                       @DefaultValue("") @QueryParam("exclude") String exclude,
                       @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
        this.exclude = Arrays.asList(exclude.trim().split(","));

	}

	@GET
	@Path("/{snpId}/info")
	public Response getByEnsemblId(@PathParam("snpId") String query) {
		try {
			checkVersionAndSpecies();
			VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);
			return createOkResponse(variationDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getByEnsemblId", e.toString());
		}
	}

    @GET
    @Path("/consequence_types")
    public Response getAllConsequenceTypes() {
        try {
            checkVersionAndSpecies();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);
            return createOkResponse(variationDBAdaptor.getAllConsequenceTypes(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }



    @GET
    @Path("/phenotypes")
    public Response getAllPhenotypes(@QueryParam("phenotype") String phenotype) {
        try {
            checkVersionAndSpecies();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);

            queryOptions.put("phenotype", phenotype);

            return createOkResponse(variationDBAdaptor.getAllPhenotypes(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

	//	private int snpId;
	//	private String name;
	//	private String chromosome;
	//	private int start;
	//	private int end;
	//	private String strand;
	//	private int mapWeight;
	//	private String alleleString;
	//	private String ancestralAllele;
	//	private String source;
	//	private String displaySoConsequence;
	//	private String soConsequenceType;
	//	private String displayConsequence;
	//	private String sequence;
	@GET
	@Path("/{snpId}/fullinfo")
	public Response getFullInfoById(@PathParam("snpId") String query) {
		try {
			checkVersionAndSpecies();
			SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(this.species, this.version);
			
			
//			List<List<Snp>> snpListList = snpDBAdaptor.getAllBySnpIdList(StringUtils.toList(query, ","));
//			List<List<SnpToTranscript>> snpToTranscript = snpDBAdaptor.getAllSnpToTranscriptList(StringUtils.toList(query, ","));
//			List<List<SnpPopulationFrequency>> snpPopulation = snpDBAdaptor.getAllSnpPopulationFrequencyList(StringUtils.toList(query, ","));
//			List<List<VariationPhenotypeAnnotation>> snpPhenotype = snpDBAdaptor.getAllSnpPhenotypeAnnotationListBySnpNameList(StringUtils.toList(query, ","));
			
//			List<List<Transcript>> transcripts = new ArrayList<List<Transcript>>(StringUtils.toList(query, ",").size());
//			for (int i = 0; i < snpToTranscript.size(); i++) {
//				List<Transcript> transcript = new ArrayList<Transcript>();
//				for (int j = 0; j < snpToTranscript.get(i).size(); j++) {
//					transcript.add(snpToTranscript.get(i).get(j).getTranscriptId());
//				}
//				transcripts.add(transcript);
//			}
			
//			StringBuilder response = new StringBuilder();
//			response.append("[");
//			for (int i = 0; i < snpListList.size(); i++) {
//				response.append("[");
//				boolean removeComma = false;
//				for (int j = 0; j < snpListList.get(i).size(); j++) {
//					removeComma = true;
//					response.append("{");
//					response.append("\"name\":"+"\""+snpListList.get(i).get(j).getName()+"\",");
//					response.append("\"chromosome\":"+"\""+snpListList.get(i).get(j).getChromosome()+"\",");
//					response.append("\"start\":"+snpListList.get(i).get(j).getStart()+",");
//					response.append("\"end\":"+snpListList.get(i).get(j).getEnd()+",");
//					response.append("\"strand\":"+"\""+snpListList.get(i).get(j).getStrand()+"\",");
//					response.append("\"mapWeight\":"+snpListList.get(i).get(j).getEnd()+",");
//					response.append("\"alleleString\":"+"\""+snpListList.get(i).get(j).getAlleleString()+"\",");
//					response.append("\"ancestralAllele\":"+"\""+snpListList.get(i).get(j).getAncestralAllele()+"\",");
//					response.append("\"source\":"+"\""+snpListList.get(i).get(j).getSource()+"\",");
//					response.append("\"displaySoConsequence\":"+"\""+snpListList.get(i).get(j).getDisplaySoConsequence()+"\",");
//					response.append("\"soConsequenceType\":"+"\""+snpListList.get(i).get(j).getSoConsequenceType()+"\",");
//					response.append("\"displayConsequence\":"+"\""+snpListList.get(i).get(j).getDisplayConsequence()+"\",");
//					response.append("\"sequence\":"+"\""+snpListList.get(i).get(j).getSequence()+"\",");
//					response.append("\"population\":"+gson.toJson(snpPopulation.get(i))+",");
//
////					String aux = gson.toJson(snpToTranscript.get(i));
//////					System.out.println(aux);
////					for (int k = 0; k < snpToTranscript.get(i).size(); k++) {
////						aux = aux.replace("\"snpToTranscriptId\":"+snpToTranscript.get(i).get(k).getSnpToTranscriptId(), "\"transcript\":"+gson.toJson(snpToTranscript.get(i).get(k).getTranscriptId())+", \"consequenceTypeSoAccession\":"+gson.toJson(snpToTranscript.get(i).get(k).getConsequenceTypeSoAccession()));
////					}
////					response.append("\"snptotranscript\":"+aux+",");
////					System.out.println(aux);
//
////					response.append("\"phenotype\":"+gson.toJson(snpPhenotype.get(i))+"");//TODO
//					response.append("},");
//				}
//				if(removeComma){
//					response.replace(response.length()-1, response.length(), "");
//				}
//				response.append("],");
//			}
//			response.replace(response.length()-1, response.length(), "");
//			response.append("]");
//			return  generateResponse(query,Arrays.asList(response));
			return  null;
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getFullInfoById", e.toString());
		}
	}	
	
	@GET
	@Path("/{snpId}/consequence_type")
	public Response getConsequenceTypeByGetMethod(@PathParam("snpId") String snpId) {
		return getConsequenceType(snpId);
	}
	
	@POST
	@Path("/consequence_type")
	public Response getConsequenceTypeByPostMethod(@QueryParam("id") String snpId) {
		return getConsequenceType(snpId);
	}

	private Response getConsequenceType(String snpId) {
		try {
			checkVersionAndSpecies();
			SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(species, version);
//			return generateResponse(snpId, "SNP_CONSEQUENCE_TYPE", snpDBAdaptor.getAllConsequenceTypesBySnpIdList(StringUtils.toList(snpId, ",")));
			return generateResponse(snpId, "SNP_CONSEQUENCE_TYPE", Arrays.asList(""));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getConsequenceTypeByPostMethod", e.toString());
		}
	}
	
	
	
	@GET
	@Path("/{snpId}/regulatory")
	public Response getRegulatoryByGetMethod(@PathParam("snpId") String snpId) {
		return getRegulatoryType(snpId);
	}
	
	@POST
	@Path("/regulatory")
	public Response getRegulatoryTypeByPostMethod(@QueryParam("id") String snpId) {
		return getRegulatoryType(snpId);
	}

	private Response getRegulatoryType(String snpId) {
		try {
			checkVersionAndSpecies();
			SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(species, version);
//			return generateResponse(snpId, "SNP_REGULATORY", snpDBAdaptor.getAllSnpRegulatoryBySnpNameList(StringUtils.toList(snpId, ",")));
			return null;
//			return generateResponse(snpId, "", snpDBAdaptor.getAllSnpRegulatoryBySnpNameList(StringUtils.toList(snpId, ",")));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getConsequenceTypeByPostMethod", e.toString());
		}
	}
	
	
	
	@GET
	@Path("/{snpId}/phenotype")
	public Response getSnpPhenotypesByNameByGet(@PathParam("snpId") String snps) {
		return getSnpPhenotypesByName(snps, outputFormat);
	}

	@POST
    @Consumes("application/x-www-form-urlencoded")
//	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
	@Path("/phenotype")
	public Response getSnpPhenotypesByNameByPost(@FormParam("of") String outputFormat, @FormParam("snps") String snps) {
		return getSnpPhenotypesByName(snps, outputFormat);
	}
	
	public Response getSnpPhenotypesByName(String snps, String outputFormat) {
		try {
			checkVersionAndSpecies();
			SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(this.species, this.version);
//			List<List<VariationPhenotypeAnnotation>> snpPhenotypeAnnotList = snpDBAdaptor.getAllSnpPhenotypeAnnotationListBySnpNameList(StringUtils.toList(snps, ","));
//			logger.debug("getSnpPhenotypesByName: "+(System.currentTimeMillis()-t0)+"ms");
//			return generateResponse(snps, "SNP_PHENOTYPE", snpPhenotypeAnnotList);
			
			return createOkResponse("Mongo TODO");
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getSnpPhenotypesByPositionByGet", e.toString());
		}
	}
	
	
	
	
	@GET
	@Path("/{snpId}/sequence")
	public Response getSequence(@PathParam("snpId") String query) {
		try {
			return  null;
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getSequence", e.toString());
		}
	}
	

	@GET
	@Path("/{snpId}/population_frequency")
	public Response getPopulationFrequency(@PathParam("snpId") String snpId) {
		try {
			checkVersionAndSpecies();
			SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(species, version);
//			return generateResponse(snpId, "SNP_POPULATION_FREQUENCY", snpDBAdaptor.getAllSnpPopulationFrequencyList(StringUtils.toList(snpId, ",")));
			return generateResponse(snpId, "SNP_POPULATION_FREQUENCY", Arrays.asList(""));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getPopulationFrequency", e.toString());
		}
	}
	
	@GET
	@Path("/{snpId}/xref")
	public Response getXrefs(@PathParam("snpId") String query) {
		try {
			checkVersionAndSpecies();
			SnpDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(this.species, this.version);
//			return  createOkResponse(tfbsDBAdaptor.getAllByTargetGeneNameList(StringUtils.toList(query, ",")));
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getXrefs", e.toString());
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
		sb.append("SNP format: rsID.\n\n\n");
		sb.append("Resources:\n");
		sb.append("- info: Get SNP information: name, position, consequence type, adjacent nucleotides, ...\n");
		sb.append(" Output columns: rsID, chromosome, position, Ensembl consequence type, SO consequence type, sequence.\n\n");
		sb.append("- consequence_type: Get SNP effect on the transcript\n");
		sb.append(" Output columns: chromosome, start, end, feature ID, feature name, consequence type, biotype, feature chromosome, feature start, feature end, feature strand, snp ID, ancestral allele, alternative allele, gene Ensembl ID, Ensembl transcript ID, gene name, SO consequence type ID, SO consequence type name, consequence type description, consequence type category, aminoacid change, codon change.\n\n");
		sb.append("- population_frequency: Get the allelic and genotypic frequencies for this SNP acroos populations.\n\n");
		sb.append("- phenotype: Get the phenotypes that have been previously associated to this SNP.\n\n");
		sb.append("- xref: Get the external references for this SNP.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#SNP");
		
		return createOkResponse(sb.toString());
	}

}
