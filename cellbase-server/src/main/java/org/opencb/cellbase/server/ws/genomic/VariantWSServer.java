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

import io.swagger.annotations.*;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/{version}/{species}/genomic/variant")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Variant", description = "Variant RESTful Web Services API")
public class VariantWSServer extends GenericRestWSServer {

    protected static final HashMap<String, List<Transcript>> CACHE_TRANSCRIPT = new HashMap<>();

    public VariantWSServer(@PathParam("version")
                           @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                   defaultValue = "latest") String version,
                           @PathParam("species")
                           @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                   + "of potentially available species ids, please refer to: "
                                   + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                           @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get JSON specification of Variant data model", response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Variant.class);
    }

    @GET
    @Path("/{phenotype}/phenotype")
    @ApiOperation(httpMethod = "GET",
            value = "Not implemented yet",
            response = QueryResponse.class, hidden = true)
    public Response getVariantsByPhenotype(@PathParam("phenotype") String phenotype) {
        try {
            parseQueryParams();
//            VariationPhenotypeAnnotationDBAdaptor va =
//                    dbAdaptorFactory.getVariationPhenotypeAnnotationDBAdaptor(this.species, this.assembly);
//            return createOkResponse(va.getAllByPhenotype(phenotype, queryOptions));
            return Response.ok("Not implemented").build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{variants}/snp_phenotype")
//    public Response getSnpPhenotypesByPositionByGet(@PathParam("variants") String variants) {
//        return getSnpPhenotypesByPosition(variants, outputFormat);
//    }
//
//    @Consumes("application/x-www-form-urlencoded")
//    @Path("/snp_phenotype")
//    public Response getSnpPhenotypesByPositionByPost(@FormParam("of") String outputFormat, @FormParam("variants") String variants) {
//        return getSnpPhenotypesByPosition(variants, outputFormat);
//    }
//
//    public Response getSnpPhenotypesByPosition(String variants, String outputFormat) {
//        try {
//            parseQueryParams();
//            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
//            List<Variant> variantList = Variant.parseVariants(variants);
//            List<Position> positionList = new ArrayList<>(variantList.size());
//            for (Variant gv : variantList) {
//                positionList.add(new Position(gv.getChromosome(), gv.getStart()));
//            }
//            return createOkResponse("Mongo TODO");
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }


//    @GET
//    @Path("/{variants}/mutation_phenotype")
//    public Response getMutationPhenotypesByPositionByGet(@PathParam("variants") String variants) {
//        return getMutationPhenotypesByPosition(variants, outputFormat);
//    }
//
//    @POST
//    @Consumes("application/x-www-form-urlencoded")
////    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED})//MediaType.MULTIPART_FORM_DATA,
//    @Path("/mutation_phenotype")
//    public Response getMutationPhenotypesByPositionByPost(@FormParam("of") String outputFormat, @FormParam("variants") String variants) {
//        return getMutationPhenotypesByPosition(variants, outputFormat);
//    }
//
//    public Response getMutationPhenotypesByPosition(String variants, String outputFormat) {
//        try {
//            parseQueryParams();
//            MutationDBAdaptor mutationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
//            List<Variant> variantList = Variant.parseVariants(variants);
//            List<Position> positionList = new ArrayList<Position>(variantList.size());
//            for (Variant gv : variantList) {
//                positionList.add(new Position(gv.getChromosome(), gv.getStart()));
//            }
//            long t0 = System.currentTimeMillis();
//            List<QueryResult> queryResults = mutationDBAdaptor.getAllByPositionList(positionList, queryOptions);
//            logger.debug("getMutationPhenotypesByPosition: " + (System.currentTimeMillis() - t0) + "ms");
////            return generateResponse(variants, "MUTATION", mutationPhenotypeAnnotList);
//            return createOkResponse(queryResults);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }


    @GET
    public Response defaultMethod() {
        return help();
    }

    @POST
    @Consumes("text/plain")
    @Path("/annotation")
    @ApiOperation(httpMethod = "POST",
            value = "Retrieves variant annotation for a list of variants.", notes = "Include and exclude lists take"
            + " values from the following set: {variation, clinical, conservation, functionalScore, consequenceType,"
            + " expression, geneDisease, drugInteraction, populationFrequencies}", response = VariantAnnotation.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "normalize",
                    value = "Boolean to indicate whether input variants shall be normalized or not. Normalization"
                            + " process includes decomposing MNVs", allowableValues = "false,true", defaultValue = "false",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAnnotationByVariantsPOST(@ApiParam(name = "variants", value = "Comma separated list of variants to"
                                                        + "annotate, e.g. "
                                                        + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
                                                        + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT",
                                                        required = true) String variants) {

        try {
            parseQueryParams();
            List<Variant> variantList = Variant.parseVariants(variants);
            logger.debug("queryOptions: " + queryOptions);
//            VariantAnnotationDBAdaptor varAnnotationDBAdaptor =
//                    dbAdaptorFactory2.getVariantAnnotationDBAdaptor(this.species, this.assembly);
//            List<QueryResult> clinicalQueryResultList = varAnnotationDBAdaptor.getAnnotationByVariantList(variantList, queryOptions);

//            VariantAnnotationCalculator variantAnnotationCalculator =
//                    new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory2);

            VariantAnnotationCalculator variantAnnotationCalculator = null;
            if (queryOptions.get("normalize") != null && queryOptions.get("normalize").equals("true")) {
                variantAnnotationCalculator =
                        new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory2, true);
            } else {
                variantAnnotationCalculator =
                        new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory2, false);
            }
            List<QueryResult<VariantAnnotation>> clinicalQueryResultList =
                    variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);

            return createOkResponse(clinicalQueryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variants}/annotation")
    @ApiOperation(httpMethod = "GET",
            value = "Retrieves variant annotation for a list of variants.", notes = "Include and exclude lists take"
            + " values from the following set: {variation, clinical, conservation, functionalScore, consequenceType,"
            + " expression, geneDisease, drugInteraction, populationFrequencies", response = VariantAnnotation.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "normalize",
                    value = "Boolean to indicate whether input variants shall be normalized or not. Normalization"
                            + " process includes decomposing MNVs", allowableValues = "false,true", defaultValue = "false",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAnnotationByVariantsGET(@PathParam("variants")
                                               @ApiParam(name = "variants", value = "Comma separated list of variants to"
                                                       + "annotate, e.g. "
                                                       + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
                                                       + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT",
                                                       required = true) String variants) {
        try {
            parseQueryParams();
            List<Variant> variantList = Variant.parseVariants(variants);
            logger.debug("queryOptions: " + queryOptions);

//            VariantAnnotationDBAdaptor varAnnotationDBAdaptor =
// dbAdaptorFactory.getVariantAnnotationDBAdaptor(this.species, this.assembly);
//            List<QueryResult> clinicalQueryResultList = varAnnotationDBAdaptor.getAnnotationByVariantList(variantList, queryOptions);

//            VariantAnnotationDBAdaptor varAnnotationDBAdaptor =
//                    dbAdaptorFactory2.getVariantAnnotationDBAdaptor(this.species, this.assembly);
//            List<QueryResult> clinicalQueryResultList = varAnnotationDBAdaptor.getAnnotationByVariantList(variantList, queryOptions);

            VariantAnnotationCalculator variantAnnotationCalculator = null;
            if (queryOptions.get("normalize") != null && queryOptions.get("normalize").equals("true")) {
                variantAnnotationCalculator =
                        new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory2, true);
            } else {
                variantAnnotationCalculator =
                        new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory2, false);
            }
            List<QueryResult<VariantAnnotation>> clinicalQueryResultList =
                    variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);

            return createOkResponse(clinicalQueryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variants}/cadd")
    @ApiOperation(httpMethod = "GET", value = "Get CADD scores for a (list of) variant(s)", response = Score.class,
            responseContainer = "QueryResponse")
    public Response getCaddScoreByVariant(@PathParam("variants")
                                          @ApiParam(name = "variants", value = "Comma separated list of variants for"
                                                  + "which CADD socores will be returned, e.g. "
                                                  + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
                                                  + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT",
                                                    required = true) String variants) {
        try {
            parseQueryParams();
            VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            List<QueryResult<Score>> functionalScoreVariant =
                    variantDBAdaptor.getFunctionalScoreVariant(Variant.parseVariants(variants), queryOptions);
            return createOkResponse(functionalScoreVariant);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @Deprecated
//    @GET
//    @Path("/{variants}/full_annotation")
//    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
//    public Response getFullAnnotationByVariantsGET(@PathParam("variants") String variants) {
//        return getAnnotationByVariantsGET(variants);
//    }

    @GET
    @Path("/consequence_types")
    @ApiOperation(httpMethod = "GET", value = "Get all sequence ontology terms describing consequence types",
            response = String.class, responseContainer = "QueryResponse")
    public Response getAllConsequenceTypes() {
        try {
            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//            query.put(VariantDBAdaptor.QueryParams.REGION.key(), "22:1-50000000");
//            return createOkResponse(variationDBAdaptor.distinct(query, "displayConsequenceType"));

            List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            QueryResult<String> queryResult = new QueryResult<>("consequence_types");
            queryResult.setNumResults(consequenceTypes.size());
            queryResult.setResult(consequenceTypes);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/count")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene objects for the regions.",
            response = Integer.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "gene",
                    value = "Comma separated list ENSEMBL gene ids, e.g.: ENSG00000161905. Exact text matches will be "
                            + "returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of rs ids, e.g.: rs6025",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "chromosome",
                    value = "Comma separated list of chromosomes to be queried, e.g.: 1,X,MT",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "reference",
                    value = "Comma separated list of possible reference to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "alternate",
                    value = "Comma separated list of possible alternate to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response count() {
//    public Response count(@DefaultValue("")
//                          @QueryParam("region")
//                          @ApiParam(name = "region",
//                                  value = "Comma separated list of genomic regions to be queried, "
//                                          + "e.g.: 1:6635137-6635325", required = true) String region) {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//        query.append(VariantDBAdaptor.QueryParams.REGION.key(), region);
        return createOkResponse(variationDBAdaptor.count(query));
    }

    @GET
    @Path("/first")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Get the first object in the database", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response first() {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
        return createOkResponse(variationDBAdaptor.first());
    }

//    @POST
//    @Consumes("text/plain")
//    @Path("/full_annotation")
//    @Deprecated
//    public Response getFullAnnotationByVariantsPOST(String variants) {
//        return getAnnotationByVariantsPOST(variants);
//    }

    @GET
    @Path("/{id}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get information about a (list of) SNPs", notes = "An independent"
            + " database query will be issued for each region in regionStr, meaning that results for each region will be"
            + " returned in independent QueryResult objects within the QueryResponse object.",
            response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "gene",
                    value = "Comma separated list ENSEMBL gene ids, e.g.: ENSG00000161905. Exact text matches will be "
                            + "returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "chromosome",
                    value = "Comma separated list of chromosomes to be queried, e.g.: 1,X,MT",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "reference",
                    value = "Comma separated list of possible reference to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "alternate",
                    value = "Comma separated list of possible alternate to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getByEnsemblId(@PathParam("id")
                                   @ApiParam(name = "id",
                                           value = "Comma separated list of rs ids, e.g.: rs6025",
                                           required = true) String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            String[] ids = id.split(",");
            List<Query> queries = new ArrayList<>(ids.length);
            for (String s : ids) {
                queries.add(new Query(VariantDBAdaptor.QueryParams.ID.key(), s));
            }
            return createOkResponse(variationDBAdaptor.nativeGet(queries, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/help")
//    public Response help() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Input:\n");
//        sb.append("Variant format: chr:position:new allele (i.e.: 1:150044250:G)\n\n\n");
//        sb.append("Resources:\n");
//        sb.append("- consequence_type: Suppose that we have obtained some variants from a resequencing analysis and we want to obtain "
//                + "the consequence type of a variant over the transcripts\n");
//        sb.append(" Output columns: chromosome, start, end, feature ID, feature name, consequence type, biotype, feature chromosome, "
//                + "feature start, feature end, feature strand, snp ID, ancestral allele, alternative allele, gene Ensembl ID, Ensembl "
//                + "transcript ID, gene name, SO consequence type ID, SO consequence type name, consequence type description, "
//                + "consequence type category, aminoacid change, codon change.\n\n\n");
//        sb.append("Documentation:\n");
//        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Variant");
//
//        return createOkResponse(sb.toString());
//    }

}
