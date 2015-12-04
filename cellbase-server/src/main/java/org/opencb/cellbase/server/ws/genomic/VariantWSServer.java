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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.db.api.variation.*;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;

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

@Path("/{version}/{species}/genomic/variant")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Variant", description = "Variant RESTful Web Services API")
public class VariantWSServer extends GenericRestWSServer {

    protected static final HashMap<String, List<Transcript>> CACHE_TRANSCRIPT = new HashMap<>();

    public VariantWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
    public Response getModel() {
        return createModelResponse(Variant.class);
    }

    @GET
    @Path("/{phenotype}/phenotype")
    public Response getVariantsByPhenotype(@PathParam("phenotype") String phenotype) {
        try {
            parseQueryParams();
            VariationPhenotypeAnnotationDBAdaptor va =
                    dbAdaptorFactory.getVariationPhenotypeAnnotationDBAdaptor(this.species, this.assembly);
            return createOkResponse(va.getAllByPhenotype(phenotype, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variants}/snp_phenotype")
    public Response getSnpPhenotypesByPositionByGet(@PathParam("variants") String variants) {
        return getSnpPhenotypesByPosition(variants, outputFormat);
    }

    @Consumes("application/x-www-form-urlencoded")
    @Path("/snp_phenotype")
    public Response getSnpPhenotypesByPositionByPost(@FormParam("of") String outputFormat, @FormParam("variants") String variants) {
        return getSnpPhenotypesByPosition(variants, outputFormat);
    }

    public Response getSnpPhenotypesByPosition(String variants, String outputFormat) {
        try {
            parseQueryParams();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            List<Variant> variantList = Variant.parseVariants(variants);
            List<Position> positionList = new ArrayList<>(variantList.size());
            for (Variant gv : variantList) {
                positionList.add(new Position(gv.getChromosome(), gv.getStart()));
            }
            return createOkResponse("Mongo TODO");
        } catch (Exception e) {
            return createErrorResponse(e);
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
            parseQueryParams();
            MutationDBAdaptor mutationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
            List<Variant> variantList = Variant.parseVariants(variants);
            List<Position> positionList = new ArrayList<Position>(variantList.size());
            for (Variant gv : variantList) {
                positionList.add(new Position(gv.getChromosome(), gv.getStart()));
            }
            long t0 = System.currentTimeMillis();
            List<QueryResult> queryResults = mutationDBAdaptor.getAllByPositionList(positionList, queryOptions);
            logger.debug("getMutationPhenotypesByPosition: " + (System.currentTimeMillis() - t0) + "ms");
//            return generateResponse(variants, "MUTATION", mutationPhenotypeAnnotList);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    public Response defaultMethod() {
        return help();
    }

    @GET
    @Path("/{variants}/annotation")
    @ApiOperation(httpMethod = "GET",
            value = "Retrieves variant annotation for a list of variants. Results within response will contain a list "
                    + "of VariantAnnotation objects.",
            response = QueryResponse.class)
    public Response getAnnotationByVariantsGET(@ApiParam(value = "Comma-separated list of variants to annotate")
                                                   @DefaultValue("19:45411941:T:C,14:38679764:-:GATCTGAGAAGGGAAAAAGGG")
                                                   @PathParam("variants") String variants) {
        try {
            parseQueryParams();
            List<Variant> variantList = Variant.parseVariants(variants);
            logger.debug("queryOptions: " + queryOptions);

            VariantAnnotationDBAdaptor varAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor(this.species, this.assembly);
            List<QueryResult> clinicalQueryResultList = varAnnotationDBAdaptor.getAnnotationByVariantList(variantList, queryOptions);

            return createOkResponse(clinicalQueryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variants}/cadd")
    public Response getCaddScoreByVariant(@PathParam("variants") String variants) {
        try {
            parseQueryParams();
            List<Variant> variantList = Variant.parseVariants(variants);
            logger.debug("queryOptions: " + queryOptions);

            VariantFunctionalScoreDBAdaptor variantFunctionalScoreDBAdaptor =
                    dbAdaptorFactory.getVariantFunctionalScoreDBAdaptor(this.species, this.assembly);
            Variant variant = variantList.get(0);
            QueryResult byVariant = variantFunctionalScoreDBAdaptor.getByVariant(variant.getChromosome(),
                    variant.getStart(), variant.getReference(), variant.getAlternate(), queryOptions);

            return createOkResponse(byVariant);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @Deprecated
    @GET
    @Path("/{variants}/full_annotation")
    public Response getFullAnnotationByVariantsGET(@PathParam("variants") String variants) {
        return getAnnotationByVariantsGET(variants);
    }

    @POST
    @Consumes("text/plain")
    @Path("/annotation")
    @ApiOperation(httpMethod = "POST",
            value = "Retrieves variant annotation for a list of variants. Results within response will contain a list "
                    + "of VariantAnnotation objects.",
            response = QueryResponse.class)
    public Response getAnnotationByVariantsPOST(String variants) {
        try {
            parseQueryParams();
            List<Variant> variantList = Variant.parseVariants(variants);
            logger.debug("queryOptions: " + queryOptions);
            VariantAnnotationDBAdaptor varAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor(this.species, this.assembly);
            List<QueryResult> clinicalQueryResultList = varAnnotationDBAdaptor.getAnnotationByVariantList(variantList, queryOptions);

            return createOkResponse(clinicalQueryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @POST
    @Consumes("text/plain")
    @Path("/full_annotation")
    public Response getFullAnnotationByVariantsPOST(String variants) {
        return getAnnotationByVariantsPOST(variants);
    }

    @GET
    @Path("/help")
    public Response help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input:\n");
        sb.append("Variant format: chr:position:new allele (i.e.: 1:150044250:G)\n\n\n");
        sb.append("Resources:\n");
        sb.append("- consequence_type: Suppose that we have obtained some variants from a resequencing analysis and we want to obtain "
                + "the consequence type of a variant over the transcripts\n");
        sb.append(" Output columns: chromosome, start, end, feature ID, feature name, consequence type, biotype, feature chromosome, "
                + "feature start, feature end, feature strand, snp ID, ancestral allele, alternative allele, gene Ensembl ID, Ensembl "
                + "transcript ID, gene name, SO consequence type ID, SO consequence type name, consequence type description, "
                + "consequence type category, aminoacid change, codon change.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Variant");

        return createOkResponse(sb.toString());
    }

}
