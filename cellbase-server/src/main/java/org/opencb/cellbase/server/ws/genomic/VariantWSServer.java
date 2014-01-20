package org.opencb.cellbase.server.ws.genomic;

import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.core.Transcript;
import org.opencb.cellbase.core.common.variation.GenomicVariant;
import org.opencb.cellbase.core.common.variation.MutationPhenotypeAnnotation;
import org.opencb.cellbase.core.lib.api.SnpDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariantEffectDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;

@Path("/{version}/{species}/genomic/variant")
@Produces(MediaType.APPLICATION_JSON)
public class VariantWSServer extends GenericRestWSServer {

    protected static HashMap<String, List<Transcript>> CACHE_TRANSCRIPT = new HashMap<>();

    public VariantWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }
    
    @GET
    @Path("/{variants}/effect")
    public Response getEffectByPositionByGet(@PathParam("variants") String variants,
                                                      @DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {
        try {
            VariantEffectDBAdaptor variationMongoDBAdaptor = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(this.species, this.version);
            System.out.println("variants = [" + variants + "], excludeSOTerms = [" + excludeSOTerms + "]");
            return createOkResponse(variationMongoDBAdaptor.getAllEffectsByVariantList(GenomicVariant.parseVariants(variants), queryOptions));
//            return getConsequenceTypeByPosition(variants, excludeSOTerms);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
        }
    }


    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/effect")
    public Response getEffectByPositionByPost(@FormParam("variants") String variants,
                                                    @DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {
        try {
            VariantEffectDBAdaptor variationMongoDBAdaptor = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(this.species, this.version);
            System.out.println("variants = [" + variants+ "], excludeSOTerms = [" + excludeSOTerms + "]");
            return createOkResponse(variationMongoDBAdaptor.getAllEffectsByVariantList(GenomicVariant.parseVariants(variants), queryOptions));
//            return getConsequenceTypeByPosition(variants, excludeSOTerms);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
        }
    }



    @GET
    @Path("/{variants}/consequence_type")
    public Response getConsequenceTypeByPositionByGet(@PathParam("variants") String variants,
                                                      @DefaultValue("") @QueryParam("exclude") String excludeSOTerms) {
        try {
            //			return getConsequenceTypeByPosition(query, features, variation, regulatory, diseases);
            return getConsequenceTypeByPosition(variants, excludeSOTerms);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
        }
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
//    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
    @Path("/consequence_type")
    public Response getConsequenceTypeByPositionByPost(@FormParam("of") String outputFormat,
                                                       @FormParam("variants") String postQuery,
                                                       @DefaultValue("") @FormParam("exclude") String excludeSOTerms) {
        //		return getConsequenceTypeByPosition(postQuery, features, variation, regulatory, diseases);
        return getConsequenceTypeByPosition(postQuery, excludeSOTerms);
    }

    private Response getConsequenceTypeByPosition(String variants, String excludes) {
        List<GenomicVariant> genomicVariantList = null;
        String[] excludeArray = null;
        Set<String> excludeSet = null;
//		List<GenomicVariantEffect> genomicVariantConsequenceTypes = null;
        List<QueryResult> genomicVariantConsequenceTypes = null;
        VariantEffectDBAdaptor gv = null;
        try {
            checkVersionAndSpecies();
//			System.out.println("PAKO: "+ variants);
            genomicVariantList = GenomicVariant.parseVariants(variants);
            if (genomicVariantList != null && excludes != null) {
                logger.debug("VariantWSServer: number of variants: " + genomicVariantList.size());
                //			GenomicVariantEffect gv = new GenomicVariantEffect(this.species);
                gv = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(species, this.version);
                excludeArray = excludes.split(",");
                excludeSet = new HashSet<String>(Arrays.asList(excludeArray));
                //				return generateResponse(variants, gv.getAllConsequenceTypeByVariantList(genomicVariantList));
                long t0 = System.currentTimeMillis();
//				genomicVariantConsequenceTypes = gv.getAllConsequenceTypeByVariantList(genomicVariantList, excludeSet);
                genomicVariantConsequenceTypes = gv.getAllConsequenceTypesByVariantList(genomicVariantList, queryOptions);
                logger.debug("GenomicVariantEffect execution time: num. variants: " + genomicVariantList.size() + ", time to process: " + (System.currentTimeMillis() - t0) + "ms");
//				System.out.println("VariantWSServer: genomicVariantConsequenceTypes => "+genomicVariantConsequenceTypes);
//				return generateResponse(variants, "GENOMIC_VARIANT_EFFECT", genomicVariantConsequenceTypes);
                return createOkResponse(genomicVariantConsequenceTypes);
            } else {
                logger.error("ERRRORRRRRR EN VARIATNWSSERVER");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
//			System.out.println("VariantWSServer: response.status => "+Response.status(Status.INTERNAL_SERVER_ERROR));
//			System.out.println("ERROR: getConsequenceTypeByPosition: VARIANTS: "+variants);
//            System.out.println("ERROR: getConsequenceTypeByPosition: " + StringUtils.getStackTrace(e));
            if (genomicVariantList != null && excludes != null) {
                gv = dbAdaptorFactory.getGenomicVariantEffectDBAdaptor(species, this.version);
                excludeArray = excludes.split(",");
                excludeSet = new HashSet<String>(Arrays.asList(excludeArray));
//					genomicVariantConsequenceTypes = gv.getAllConsequenceTypeByVariantList(genomicVariantList, excludeSet);
                genomicVariantConsequenceTypes = gv.getAllConsequenceTypesByVariantList(genomicVariantList, queryOptions);
                logger.warn("VariantWSServer: in catch of genomicVariantConsequenceTypes => " + genomicVariantConsequenceTypes);
//					return generateResponse(variants, "GENOMIC_VARIANT_EFFECT", genomicVariantConsequenceTypes);
                return createOkResponse(genomicVariantConsequenceTypes);
            }
            return createErrorResponse("getConsequenceTypeByPositionByGet", e.toString());
        }
    }


    @GET
    @Path("/{variants}/snp_phenotype")
    public Response getSnpPhenotypesByPositionByGet(@PathParam("variants") String variants) {
        return getSnpPhenotypesByPosition(variants, outputFormat);
    }

    @Consumes("application/x-www-form-urlencoded")
//    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
    @Path("/snp_phenotype")
    public Response getSnpPhenotypesByPositionByPost(@FormParam("of") String outputFormat, @FormParam("variants") String variants) {
        return getSnpPhenotypesByPosition(variants, outputFormat);
    }

    public Response getSnpPhenotypesByPosition(String variants, String outputFormat) {
        try {
            checkVersionAndSpecies();
            SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(this.species, this.version);
            List<GenomicVariant> variantList = GenomicVariant.parseVariants(variants);
            List<Position> positionList = new ArrayList<Position>(variantList.size());
            for (GenomicVariant gv : variantList) {
                positionList.add(new Position(gv.getChromosome(), gv.getPosition()));
            }
//			return generateResponse(variants, "SNP_PHENOTYPE", snpDBAdaptor.getAllSnpPhenotypeAnnotationListByPositionList(positionList));
            return createOkResponse("Mongo TODO");
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSnpPhenotypesByPositionByGet", e.toString());
        }
    }


    @GET
    @Path("/{variants}/mutation_phenotype")
    public Response getMutationPhenotypesByPositionByGet(@PathParam("variants") String variants) {
        return getMutationPhenotypesByPosition(variants, outputFormat);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
//    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
    @Path("/mutation_phenotype")
    public Response getMutationPhenotypesByPositionByPost(@FormParam("of") String outputFormat, @FormParam("variants") String variants) {
        return getMutationPhenotypesByPosition(variants, outputFormat);
    }

    public Response getMutationPhenotypesByPosition(String variants, String outputFormat) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor mutationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
            List<GenomicVariant> variantList = GenomicVariant.parseVariants(variants);
            List<Position> positionList = new ArrayList<Position>(variantList.size());
            for (GenomicVariant gv : variantList) {
                positionList.add(new Position(gv.getChromosome(), gv.getPosition()));
            }
            long t0 = System.currentTimeMillis();
            List<QueryResult> queryResults = mutationDBAdaptor.getAllByPositionList(positionList, queryOptions);
            logger.debug("getMutationPhenotypesByPosition: " + (System.currentTimeMillis() - t0) + "ms");
//            return generateResponse(variants, "MUTATION", mutationPhenotypeAnnotList);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationPhenotypesByPositionByGet", e.toString());
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
        sb.append("Variant format: chr:position:new allele (i.e.: 1:150044250:G)\n\n\n");
        sb.append("Resources:\n");
        sb.append("- consequence_type: Suppose that we have obtained some variants from a resequencing analysis and we want to obtain the consequence type of a variant over the transcripts\n");
        sb.append(" Output columns: chromosome, start, end, feature ID, feature name, consequence type, biotype, feature chromosome, feature start, feature end, feature strand, snp ID, ancestral allele, alternative allele, gene Ensembl ID, Ensembl transcript ID, gene name, SO consequence type ID, SO consequence type name, consequence type description, consequence type category, aminoacid change, codon change.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Variant");

        return createOkResponse(sb.toString());
    }

}
