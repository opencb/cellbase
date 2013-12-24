package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.common.variation.MutationPhenotypeAnnotation;
import org.opencb.cellbase.core.lib.api.*;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.bioformats.protein.uniprot.v140jaxb.FeatureType;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/{version}/{species}/feature/transcript")
@Produces(MediaType.APPLICATION_JSON)
public class TranscriptWSServer extends GenericRestWSServer {

    private List<String> exclude = new ArrayList<>();

    public TranscriptWSServer(@PathParam("version") String version, @PathParam("species") String species,
                              @DefaultValue("") @QueryParam("exclude") String exclude,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
        this.exclude = Arrays.asList(exclude.trim().split(","));
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try {
            checkVersionAndSpecies();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.version);
            return createOkResponse(Arrays.asList(transcriptDBAdaptor.getAll(queryOptions)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAll", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/info")
    public Response getByEnsemblId(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species,
                    this.version);
            return createOkResponse(transcriptDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/fullinfo")
    public Response getFullInfoByEnsemblId(@PathParam("transcriptId") String query) {
        try {
//		return createJsonResponse("{}");

//			checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
            List<String> queryStrList = Splitter.on(",").splitToList(query);
//			List<List<Gene>> queryGeneList = geneDBAdaptor.getAllByIdList(queryStrList, queryOptions);
//
//			JsonArray qlist = new JsonArray();
//
//			int i = 0;
//			boolean found = false;
//			for (List<Gene> geneList : queryGeneList) {
//				String queryName = queryStrList.get(i);
//				JsonArray jsonTranscripts = new JsonArray();
//				logger.info(geneList.size());
//				for (Gene gene : geneList) {
//					for (Transcript transcript : gene.getTranscripts()) {
//						for (Xref xref : transcript.getXrefs()) {
//							if (xref.getId().equals(queryName)) {
//								found = true;
//								break;
//							}
//						}
//						if(found){
//							JsonObject jsonTranscript = gson.fromJson(gson.toJson(transcript), JsonElement.class).getAsJsonObject();
//							JsonObject jsonGene = gson.fromJson(gson.toJson(gene), JsonElement.class).getAsJsonObject();
//							jsonGene.remove("transcripts");
//							jsonTranscript.add("gene", jsonGene);
//							jsonTranscripts.add(jsonTranscript);
//							found = false;
//						}
//					}
//				}
//				qlist.add(jsonTranscripts);
//				i++;
//			}

//			return createOkResponse(qlist.toString());

            return createOkResponse(geneDBAdaptor.getAllByIdList(queryStrList, queryOptions));


            // TranscriptDBAdaptor return createOkResponse(qlist.toString());transcriptDBAdaptor =
            // dbAdaptorFactory.getTranscriptDBAdaptor(this.species,
            // this.version);
            // return generateResponse(query, "TRANSCRIPT",
            // transcriptDBAdaptor.getAllByNameList(StringUtils.toList(query,
            // ",")));

            // List<Transcript> result = new ArrayList<Transcript>();
            // List<Transcript> transcripts = executeQuery(query);
            // for (Transcript transcript : transcripts) {
            // // List<Xref> xrefs = transcript.getXrefs();
            // for (Xref xref : transcript.getXrefs()) {
            // if (xref.getId().equals(name)) {
            // result.add(transcript);
            // break;
            // }
            // }
            // }
            // return result;

            // return generateResponse(query, "GENE",
            // geneDBAdaptor.getAllByNameList(StringUtils.toList(query, ","),
            // true));

            // if Gene, fitlar el bueno y poner el gene:{}

            // GeneDBAdaptor geneDBAdaptor =
            // dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
            // ExonDBAdaptor exonDBAdaptor =
            // dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
            // XRefsDBAdaptor xRefsDBAdaptor =
            // dbAdaptorFactory.getXRefDBAdaptor(this.species, this.version);
            // SnpDBAdaptor snpDBAdaptor =
            // dbAdaptorFactory.getSnpDBAdaptor(this.species, this.version);
            // MutationDBAdaptor mutDBAdaptor =
            // dbAdaptorFactory.getMutationDBAdaptor(this.species,
            // this.version);
            // TfbsDBAdaptor tfbsDBAdaptor =
            // dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
            // MirnaDBAdaptor mirnaDBAdaptor =
            // dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
            // ProteinDBAdaptor proteinDBAdaptor =
            // dbAdaptorFactory.getProteinDBAdaptor(this.species, this.version);
            //
            // List<Transcript> transcripts =
            // transcriptDBAdaptor.getAllByEnsemblIdList(StringUtils.toList(query,
            // ","));
            //
            // List<Gene> genes =
            // geneDBAdaptor.getAllByEnsemblTranscriptIdList(StringUtils.toList(query,
            // ","));
            // List<List<Exon>> exonLists =
            // exonDBAdaptor.getByEnsemblTranscriptIdList(StringUtils.toList(query,
            // ","));
            // List<List<Xref>> goLists =
            // xRefsDBAdaptor.getAllByDBName(StringUtils.toList(query,
            // ","),"go");
            // List<List<Xref>> interproLists =
            // xRefsDBAdaptor.getAllByDBName(StringUtils.toList(query,
            // ","),"interpro");
            // List<List<Xref>> reactomeLists =
            // xRefsDBAdaptor.getAllByDBName(StringUtils.toList(query,
            // ","),"reactome");
            // List<List<Snp>> snpLists =
            // snpDBAdaptor.getAllByEnsemblTranscriptIdList(StringUtils.toList(query,
            // ","));
            // List<List<MutationPhenotypeAnnotation>> mutLists =
            // mutDBAdaptor.getAllMutationPhenotypeAnnotationByEnsemblTranscriptList(StringUtils.toList(query,
            // ","));
            // List<List<Tfbs>> tfbsLists =
            // tfbsDBAdaptor.getAllByTargetGeneNameList(StringUtils.toList(query,
            // ","));
            // List<List<MirnaTarget>> mirnaTargetsList =
            // mirnaDBAdaptor.getAllMiRnaTargetsByGeneNameList(StringUtils.toList(query,
            // ","));
            // List<List<ProteinFeature>> proteinFeaturesList =
            // proteinDBAdaptor.getAllProteinFeaturesByProteinXrefList(StringUtils.toList(query,
            // ","));
            //
            // StringBuilder response = new StringBuilder();
            // response.append("[");
            // for (int i = 0; i < transcripts.size(); i++) {
            // if(transcripts.get(i) != null){
            // response.append("{");
            // response.append("\"stableId\":"+"\""+transcripts.get(i).getStableId()+"\",");
            // response.append("\"externalName\":"+"\""+transcripts.get(i).getExternalName()+"\",");
            // response.append("\"externalDb\":"+"\""+transcripts.get(i).getExternalDb()+"\",");
            // response.append("\"biotype\":"+"\""+transcripts.get(i).getBiotype()+"\",");
            // response.append("\"status\":"+"\""+transcripts.get(i).getStatus()+"\",");
            // response.append("\"chromosome\":"+"\""+transcripts.get(i).getChromosome()+"\",");
            // response.append("\"start\":"+transcripts.get(i).getStart()+",");
            // response.append("\"end\":"+transcripts.get(i).getEnd()+",");
            // response.append("\"strand\":"+"\""+transcripts.get(i).getStrand()+"\",");
            // response.append("\"codingRegionStart\":"+transcripts.get(i).getCodingRegionStart()+",");
            // response.append("\"codingRegionEnd\":"+transcripts.get(i).getCodingRegionEnd()+",");
            // response.append("\"cdnaCodingStart\":"+transcripts.get(i).getCdnaCodingStart()+",");
            // response.append("\"cdnaCodingEnd\":"+transcripts.get(i).getCdnaCodingEnd()+",");
            // response.append("\"description\":"+"\""+transcripts.get(i).getDescription()+"\",");
            // response.append("\"gene\":"+gson.toJson(genes.get(i))+",");
            // response.append("\"exons\":"+gson.toJson(exonLists.get(i))+",");
            // response.append("\"snps\":"+gson.toJson(snpLists.get(i))+",");
            // response.append("\"mutations\":"+gson.toJson(mutLists.get(i))+",");
            // response.append("\"go\":"+gson.toJson(goLists.get(i))+",");
            // response.append("\"interpro\":"+gson.toJson(interproLists.get(i))+",");
            // response.append("\"tfbs\":"+gson.toJson(tfbsLists.get(i))+",");
            // response.append("\"mirnaTargets\":"+gson.toJson(mirnaTargetsList.get(i))+",");
            // response.append("\"proteinFeatures\":"+gson.toJson(proteinFeaturesList.get(i))+",");
            // response.append("\"reactome\":"+gson.toJson(reactomeLists.get(i))+"");
            // response.append("},");
            // }else{
            // response.append("{},");
            // }
            // }
            // response.replace(response.length()-1, response.length(), "");
            // response.append("]");
            //
            // //Remove the last comma
            // return generateResponse(query,Arrays.asList(response));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getFullInfoByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/gene")
    public Response getGeneById(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);

            QueryOptions queryOptions = new QueryOptions("exclude", exclude);

            return createJsonResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));

//			return generateResponse(query, "GENE",
//					geneDBAdaptor.getAllByNameList(StringUtils.toList(query, ","), exclude));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getGeneByTranscriptIdList", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/exon")
    public Response getExonsByTranscriptId(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            ExonDBAdaptor dbAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
            return generateResponse(query, "EXON",
                    dbAdaptor.getByEnsemblTranscriptIdList(Splitter.on(",").splitToList(query)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getExonsByTranscriptId", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/sequence")
    public Response getSequencesByIdList(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species,
                    this.version);
//            return generateResponse(query, transcriptDBAdaptor.getAllSequencesByIdList(Splitter.on(",").splitToList(query)));
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSequencesByIdList", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/region")
    public Response getRegionsByIdList(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species,
                    this.version);
//            return generateResponse(query, transcriptDBAdaptor.getAllRegionsByIdList(Splitter.on(",").splitToList(query)));
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getRegionsByIdList", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/mutation")
    public Response getMutationByTranscriptId(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor mutationAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
//            List<List<MutationPhenotypeAnnotation>> geneList = mutationAdaptor
//                    .getAllMutationPhenotypeAnnotationByGeneNameList(Splitter.on(",").splitToList(query));
            List<QueryResult> queryResults = mutationAdaptor.getAllByGeneNameList(Splitter.on(",").splitToList(query), queryOptions);
//            return generateResponse(query, "MUTATION", queryResults);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByGene", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/function_prediction")
    public Response getProteinFunctionPredictionByTranscriptId(@PathParam("transcriptId") String query,
                                                               @DefaultValue("") @QueryParam("aaPosition") String aaPosition,
                                                               @DefaultValue("") @QueryParam("aaChange") String aaChange) {
        try {
            checkVersionAndSpecies();
            queryOptions.put("aaPosition", aaPosition);
            queryOptions.put("aaChange", aaChange);
            ProteinFunctionPredictorDBAdaptor mutationAdaptor = dbAdaptorFactory.getProteinFunctionPredictorDBAdaptor(this.species, this.version);
            List<QueryResult> queryResults = mutationAdaptor.getAllByEnsemblTranscriptIdList(Splitter.on(",").splitToList(query), queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByGene", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/protein_feature")
    public Response getProteinFeaturesByTranscriptId(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            ProteinDBAdaptor proteinAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.version);
            List<List<FeatureType>> geneList = proteinAdaptor.getAllProteinFeaturesByProteinXrefList(Splitter.on(",").splitToList(query));
            return generateResponse(query, "PROTEIN_FEATURE", geneList);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByGene", e.toString());
        }
    }

    @GET
    @Path("/{transcriptId}/cdna")
    public Response getCdnaByTranscriptId(@PathParam("transcriptId") String query) {
        try {
            checkVersionAndSpecies();
            ExonDBAdaptor dbAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
//			List<String> transcripts = StringUtils.toList(query, ",");
//			List<List<Exon>> exonListList = dbAdaptor.getByEnsemblTranscriptIdList(transcripts);
//			List<String> cdnaSequenceList = new ArrayList<String>(transcripts.size());
//			for (List<Exon> exonList : exonListList) {
//
//			}
//			return generateResponse(query, "", cdnaSequenceList);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByGene", e.toString());
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
        sb.append("all id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get transcript information: name, position, biotype.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
        sb.append("- gene: Get the corresponding gene for this transcript.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
        sb.append("- sequence: Get transcript sequence.\n\n");
        sb.append("- exon: Get transcript's exons.\n");
        sb.append(" Output columns: Ensembl ID, chromosome, start, end, strand.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Transcript");
        return createOkResponse(sb.toString());
    }

}
