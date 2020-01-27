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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/{version}/{species}/genomic/variant")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Variant", description = "Variant RESTful Web Services API")
public class VariantWSServer extends GenericRestWSServer {

    private static final String PHASE_DATA_URL_SEPARATOR = "\\+";
    private static final String VARIANT_STRING_FORMAT = "(chr)"
            + ":[(cipos_left)<](start)[<(cipos_right)]" + "[-[(ciend_left)<](end)[<(ciend_right)]]"
            + "[:(ref)]"
            + ":[(alt)|(left_ins_seq)...(right_ins_seq)]";

    public VariantWSServer(@PathParam("version")
                           @ApiParam(name = "version", value = "Possible values: v3, v4",
                                   defaultValue = "v4") String version,
                           @PathParam("species")
                           @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                   + "of potentially available species ids, please refer to: "
                                   + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species") String species,
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
            + " expression, geneDisease, drugInteraction, populationFrequencies, repeats}.",
            response = VariantAnnotation.class, responseContainer = "QueryResponse")
    public Response getAnnotationByVariantsPOST(@ApiParam(name = "variants", value = "Comma separated list of variants to"
                                                        + "annotate, e.g. "
                                                        + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
                                                        + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT",
                                                        required = true) String variants,
                                                @QueryParam("normalize")
                                                @ApiParam(name = "normalize",
                                                        value = "Boolean to indicate whether input variants shall be "
                                                                + "normalized or not. Normalization process does NOT "
                                                                + "include decomposing ", allowableValues = "false,true",
                                                        defaultValue = "false", required = false) Boolean normalize,
                                                @QueryParam("skipDecompose")
                                                @ApiParam(name = "skipDecompose",
                                                        value = "Boolean to indicate whether input MNVs should be "
                                                                + "decomposed or not as part of the normalisation step."
                                                                + " MNV decomposition is strongly encouraged.",
                                                        allowableValues = "false,true",
                                                        defaultValue = "false", required = false) Boolean skipDecompose,
                                                @QueryParam("ignorePhase")
                                                @ApiParam(name = "ignorePhase",
                                                        value = "Boolean to indicate whether phase data should be "
                                                                + "taken into account.", allowableValues = "false,true",
                                                        required = false) Boolean ignorePhase,
                                                @Deprecated
                                                @QueryParam("phased")
                                                @ApiParam(name = "phased",
                                                        value = "DEPRECATED. Will be removed in next release. "
                                                                + "Please, use ignorePhase instead. Boolean to "
                                                                + "indicate whether phase should be considered "
                                                                + "during the annotation process",
                                                        allowableValues = "false,true",
                                                        required = false) Boolean phased,
                                                @QueryParam("imprecise")
                                                @ApiParam(name = "imprecise",
                                                        value = "Boolean to indicate whether imprecise search must be"
                                                                + " used or not", allowableValues = "false,true",
                                                        defaultValue = "true", required = false) Boolean imprecise,
                                                @QueryParam("svExtraPadding")
                                                @ApiParam(name = "svExtraPadding",
                                                        value = "Integer to optionally provide the size of the extra"
                                                                + " padding to be used when annotating imprecise (or not)"
                                                                + " structural variants",
                                                        defaultValue = "0", required = false) Integer svExtraPadding,
                                                @QueryParam("cnvExtraPadding")
                                                @ApiParam(name = "cnvExtraPadding",
                                                        value = "Integer to optionally provide the size of the extra"
                                                                + " padding to be used when annotating imprecise (or not)"
                                                                + " CNVs",
                                                        defaultValue = "0", required = false) Integer cnvExtraPadding,
                                                @QueryParam("checkAminoAcidChange")
                                                @ApiParam(name = "checkAminoAcidChange",
                                                value = "<DESCRIPTION GOES HERE>",
                                                allowableValues = "false,true",
                                                defaultValue = "false", required = false) Boolean checkAminoAcidChange) {

        return getAnnotationByVariant(variants,
                normalize,
                skipDecompose,
                ignorePhase,
                phased,
                imprecise,
                svExtraPadding,
                cnvExtraPadding,
                checkAminoAcidChange);
    }

    @GET
    @Path("/{variants}/annotation")
    @ApiOperation(httpMethod = "GET",
            value = "Retrieves variant annotation for a list of variants.", notes = "Include and exclude lists take"
            + " values from the following set: {variation, clinical, conservation, functionalScore, consequenceType,"
            + " expression, geneDisease, drugInteraction, populationFrequencies, repeats}.",
            response = VariantAnnotation.class, responseContainer = "QueryResponse")
    public Response getAnnotationByVariantsGET(@PathParam("variants")
                                               @ApiParam(name = "variants", value = "Comma separated list of variants to"
                                                       + "annotate, e.g. "
                                                       + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
                                                       + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT,1:816505-825225:<CNV>",
                                                       required = true) String variants,
                                               @QueryParam("normalize")
                                               @ApiParam(name = "normalize",
                                                       value = "Boolean to indicate whether input variants shall be "
                                                               + "normalized or not. Normalization process does NOT "
                                                               + "include decomposing ", allowableValues = "false,true",
                                                       defaultValue = "true", required = false) Boolean normalize,
                                               @QueryParam("skipDecompose")
                                               @ApiParam(name = "skipDecompose",
                                                       value = "Boolean to indicate whether input MNVs should be "
                                                               + "decomposed or not as part of the normalisation step."
                                                               + " MNV decomposition is strongly encouraged.",
                                                       allowableValues = "false,true",
                                                       defaultValue = "false", required = false) Boolean skipDecompose,
                                               @QueryParam("ignorePhase")
                                               @ApiParam(name = "ignorePhase",
                                                       value = "Boolean to indicate whether phase data should be "
                                                               + "taken into account.", allowableValues = "false,true",
                                                       required = false) Boolean ignorePhase,
                                               @Deprecated
                                               @QueryParam("phased")
                                               @ApiParam(name = "phased",
                                                       value = "DEPRECATED. Will be removed in next release. "
                                                               + "Please, use ignorePhase instead. Boolean to "
                                                               + "indicate whether phase should be considered "
                                                               + "during the annotation process",
                                                       allowableValues = "false,true",
                                                       required = false) Boolean phased,
                                               @QueryParam("imprecise")
                                               @ApiParam(name = "imprecise",
                                                       value = "Boolean to indicate whether imprecise search must be"
                                                               + " used or not", allowableValues = "false,true",
                                                       defaultValue = "true", required = false) Boolean imprecise,
                                               @QueryParam("svExtraPadding")
                                               @ApiParam(name = "svExtraPadding",
                                                       value = "Integer to optionally provide the size of the extra"
                                                               + " padding to be used when annotating imprecise (or not)"
                                                               + " structural variants",
                                                       defaultValue = "0", required = false) Integer svExtraPadding,
                                               @QueryParam("cnvExtraPadding")
                                               @ApiParam(name = "cnvExtraPadding",
                                                       value = "Integer to optionally provide the size of the extra"
                                                               + " padding to be used when annotating imprecise (or not)"
                                                               + " CNVs",
                                                       defaultValue = "0", required = false) Integer cnvExtraPadding,
                                               @QueryParam("checkAminoAcidChange")
                                               @ApiParam(name = "checkAminoAcidChange",
                                                           value = "<DESCRIPTION GOES HERE>",
                                                           allowableValues = "false,true",
                                                           defaultValue = "false", required = false) Boolean checkAminoAcidChange) {
        return getAnnotationByVariant(variants,
                normalize,
                skipDecompose,
                ignorePhase,
                phased,
                imprecise,
                svExtraPadding,
                cnvExtraPadding,
                checkAminoAcidChange);
    }

    private Response getAnnotationByVariant(String variants,
                                            Boolean normalize,
                                            Boolean skipDecompose,
                                            Boolean ignorePhase,
                                            @Deprecated Boolean phased,
                                            Boolean imprecise,
                                            Integer svExtraPadding,
                                            Integer cnvExtraPadding,
                                            Boolean checkAminoAcidChange) {
        try {
            parseQueryParams();
            List<Variant> variantList = parseVariants(variants);
            logger.debug("queryOptions: " + queryOptions);

            // If ignorePhase (new parameter) is present, then overrides presence of "phased"
            if (ignorePhase != null) {
                queryOptions.put("ignorePhase", ignorePhase);
                // If the new parameter (ignorePhase) is not present but old one ("phased") is, then follow old one - probably
                // someone who has not moved to the new parameter yet
            } else if (phased != null) {
                queryOptions.put("ignorePhase", !phased);
                // Default behavior is to perform phased annotation
            } else {
                queryOptions.put("ignorePhase", false);
            }

            if (normalize != null) {
                queryOptions.put("normalize", normalize);
            }
            if (skipDecompose != null) {
                queryOptions.put("skipDecompose", skipDecompose);
            }
            if (imprecise != null) {
                queryOptions.put("imprecise", imprecise);
            }
            if (svExtraPadding != null) {
                queryOptions.put("svExtraPadding", svExtraPadding);
            }
            if (cnvExtraPadding != null) {
                queryOptions.put("cnvExtraPadding", cnvExtraPadding);
            }
            if (checkAminoAcidChange != null) {
                queryOptions.put("checkAminoAcidChange", checkAminoAcidChange);
            }
            VariantAnnotationCalculator variantAnnotationCalculator =
                    new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory);
            List<QueryResult<VariantAnnotation>> queryResultList =
                    variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);

            return createOkResponse(queryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    private List<Variant> parseVariants(String variantsString) {
        List<Variant> variants = null;
        if (variantsString != null && !variantsString.isEmpty()) {
            String[] variantItems = variantsString.split(",");
            variants = new ArrayList<>(variantItems.length);

            for (String variantString: variantItems) {
                variants.add(parseVariant(variantString));
            }
        }
        return variants;
    }

    private Variant parseVariant(String variantString) {
        String[] variantStringPartArray = variantString.split(PHASE_DATA_URL_SEPARATOR);

        VariantBuilder variantBuilder;
        if (variantStringPartArray.length > 0) {
            variantBuilder = new VariantBuilder(variantStringPartArray[0]);
            // Either 1 or 3 parts expected variant+GT+PS
            if (variantStringPartArray.length == 3) {
                List<String> formatList = new ArrayList<>(2);
                // If phase set tag is not provided not phase data is added at all to the Variant object
                if (!variantStringPartArray[2].isEmpty()) {
                    formatList.add(AnnotationBasedPhasedQueryManager.PHASE_SET_TAG);
                    List<String> sampleData = new ArrayList<>(2);
                    sampleData.add(variantStringPartArray[2]);
                    // Genotype field might be empty - just PS would be added to Variant object in that case
                    if (!variantStringPartArray[1].isEmpty()) {
                        formatList.add(AnnotationBasedPhasedQueryManager.GENOTYPE_TAG);
                        sampleData.add(variantStringPartArray[1]);
                    }
                    variantBuilder.setFormat(formatList);
                    variantBuilder.setSamplesData(Collections.singletonList(sampleData));
                }
            } else if (variantStringPartArray.length > 3) {
                throw new IllegalArgumentException("Malformed variant string " + variantString + ". "
                        + "variantString+GT+PS expected, where variantString needs 3 or 4 fields separated by ':'. "
                        + "Format: \"" + VARIANT_STRING_FORMAT + "\"");
            }
        } else {
            throw new IllegalArgumentException("Malformed variant string " + variantString + ". "
                    + "variantString+GT+PS expected, where variantString needs 3 or 4 fields separated by ':'. "
                    + "Format: \"" + VARIANT_STRING_FORMAT + "\"");
        }

        return variantBuilder.build();
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
            VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);

            List<QueryResult<Score>> functionalScoreVariant =
                    variantDBAdaptor.getFunctionalScoreVariant(Variant.parseVariants(variants), queryOptions);
            return createOkResponse(functionalScoreVariant);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
