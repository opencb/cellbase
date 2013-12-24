package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.opencb.cellbase.core.common.variation.MutationPhenotypeAnnotation;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.MirnaDBAdaptor;
import org.opencb.cellbase.core.lib.api.ProteinDBAdaptor;
import org.opencb.cellbase.core.lib.api.XRefsDBAdaptor;
import org.opencb.cellbase.core.lib.api.network.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import org.bioinfo.cellbase.lib.common.variation.Snp;

@Path("/{version}/{species}/feature/gene")
@Produces("text/plain")
public class GeneWSServer extends GenericRestWSServer {


    public GeneWSServer(@PathParam("version") String version, @PathParam("species") String species,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/list")
    public Response getAll(@DefaultValue("") @QueryParam("biotype") List<String> biotypes) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);

//			QueryOptions queryOptions = new QueryOptions("biotypes", biotypes);
//			queryOptions.put("include", include );

            return createOkResponse(geneDBAdaptor.getAll(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAll", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/info")
    public Response getByEnsemblId(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);

//			QueryOptions queryOptions = new QueryOptions("exclude", exclude);
//			queryOptions.put("include", include );

            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
//			return generateResponse(query, "GENE", geneDBAdaptor.getAllByNameList(StringUtils.toList(query, ","),exclude));
            //	return generateResponse(query, Arrays.asList(this.getGeneDBAdaptor().getAllByEnsemblIdList(StringUtils.toList(query, ","))));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }


    @GET
    @Path("/{geneId}/transcript")
    public Response getTranscriptsByGeneId(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptsById", e.toString());
        }
    }

    @GET
    @Path("/biotypes")
    public Response getAllBiotypes() {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
            return createOkResponse(geneDBAdaptor.getAllBiotypes(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptsById", e.toString());
        }
    }


    @GET
    @Path("/{geneId}/snp")
    public Response getSNPByGeneId(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();

            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);

            List<QueryResult> qrList = geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions);
            List<QueryResult> queryResults = new ArrayList<>();
            for (QueryResult qr : qrList) {
                QueryResult queryResult = new QueryResult();
                queryResult.setId(qr.getId());

                BasicDBList genes = (BasicDBList) qr.getResult();
                BasicDBObject gene = (BasicDBObject) genes.get(0);
                QueryResult variationQueryResult = variationDBAdaptor.getAllByRegion(gene.getString("chromosome"), gene.getInt("start"), gene.getInt("end"), queryOptions);

                queryResult.setNumResults(variationQueryResult.getNumResults());
                queryResult.setResult(variationQueryResult.getResult());
                queryResults.add(queryResult);
            }

            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSNPByGene", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/mutation")
    public Response getMutationByGene(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor mutationAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
//            List<List<MutationPhenotypeAnnotation>> geneList = mutationAdaptor.getAllMutationPhenotypeAnnotationByGeneNameList(Splitter.on(",").splitToList(query));
            List<QueryResult> queryResults = mutationAdaptor.getAllByGeneNameList(Splitter.on(",").splitToList(query), queryOptions);
//            return generateResponse(query, "MUTATION", queryResults);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByGene", e.toString());
        }
    }


    @GET
    @Path("/{geneId}/tfbs")
    public Response getAllTfbs(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
            return createOkResponse(tfbsDBAdaptor.getAllByTargetGeneIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllTfbs", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/mirna_target")
    public Response getAllMirna(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
            return generateResponse(query, "MIRNA_TARGET", mirnaDBAdaptor.getAllMiRnaTargetsByGeneNameList(Splitter.on(",").splitToList(query)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllMirna", e.toString());
        }
    }


    @GET
    @Path("/{geneId}/protein_feature")
    public Response getProteinFeature(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.version);
            return generateResponse(query, "PROTEIN_FEATURE", proteinDBAdaptor.getAllProteinFeaturesByGeneNameList(Splitter.on(",").splitToList(query)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getProteinFeature", e.toString());
        }
    }


    @GET
    @Path("/{geneId}/exon")
    public Response getExonByGene(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);
            return createOkResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getExonByGene", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/reactome")
    public Response getReactomeByEnsemblId(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            XRefsDBAdaptor xRefsDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.version);
            return generateResponse(query, xRefsDBAdaptor.getAllByDBName(Splitter.on(",").splitToList(query), "reactome"));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getReactomeByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{geneId}/protein")
    public Response getPPIByEnsemblId(@PathParam("geneId") String query) {
        try {
            checkVersionAndSpecies();
            ProteinProteinInteractionDBAdaptor PPIDBAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.version);
            return createOkResponse(PPIDBAdaptor.getAllByInteractorIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getPPIByEnsemblId", e.toString());
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
        sb.append("- info: Get gene information: name, position, biotype.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
        sb.append("- transcript: Get all transcripts for this gene.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
        sb.append("- tfbs: Get transcription factor binding sites (TFBSs) that map to the promoter region of this gene.\n");
        sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
        sb.append("- mirna_target: Get all microRNA target sites for this gene.\n");
        sb.append(" Output columns: miRBase ID, gene target name, chromosome, start, end, strand, pubmed ID, source.\n\n");
        sb.append("- protein_feature: Get protein information related to this gene.\n");
        sb.append(" Output columns: feature type, aa start, aa end, original, variation, identifier, description.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Gene");

        return createOkResponse(sb.toString());
    }

}
