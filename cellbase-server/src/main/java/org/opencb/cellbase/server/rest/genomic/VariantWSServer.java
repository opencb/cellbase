/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.server.rest.genomic;

import io.swagger.annotations.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

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
import java.util.stream.Collectors;

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
                           @ApiParam(name = "version", value = ParamConstants.VERSION_DESCRIPTION,
                                   defaultValue = ParamConstants.DEFAULT_VERSION) String version,
                           @PathParam("species")
                           @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                           @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = ParamConstants.DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Variant.class);
    }

    @GET
    @Path("/{phenotype}/phenotype")
    @ApiOperation(httpMethod = "GET",
            value = "Not implemented yet",
            response = CellBaseDataResponse.class, hidden = true)
    public Response getVariantsByPhenotype(@PathParam("phenotype") String phenotype) {
        try {
            parseQueryParams();
            return Response.ok("Not implemented").build();
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @POST
    @Consumes("text/plain")
    @Path("/annotation")
    @ApiOperation(httpMethod = "POST",
            value = "Retrieves variant annotation for a list of variants.", notes = "Include and exclude lists take"
            + " values from the following set: {variation, clinical, conservation, functionalScore, consequenceType,"
            + " expression, geneDisease, drugInteraction, populationFrequencies, repeats}.",
            response = VariantAnnotation.class, responseContainer = "QueryResponse", hidden = true)
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
                                                        defaultValue = "0", required = false) Integer cnvExtraPadding) {

        return getAnnotationByVariant(variants,
                normalize,
                skipDecompose,
                ignorePhase,
                phased,
                imprecise,
                svExtraPadding,
                cnvExtraPadding);
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
                                                       defaultValue = "0", required = false) Integer cnvExtraPadding) {
        return getAnnotationByVariant(variants,
                normalize,
                skipDecompose,
                ignorePhase,
                phased,
                imprecise,
                svExtraPadding,
                cnvExtraPadding);
    }

    private Response getAnnotationByVariant(String variants,
                                            Boolean normalize,
                                            Boolean skipDecompose,
                                            Boolean ignorePhase,
                                            @Deprecated Boolean phased,
                                            Boolean imprecise,
                                            Integer svExtraPadding,
                                            Integer cnvExtraPadding) {
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
            VariantAnnotationCalculator variantAnnotationCalculator =
                    new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory);
            List<CellBaseDataResult<VariantAnnotation>> queryResultList =
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
    @Deprecated
    @Path("/{variants}/cadd")
    @ApiOperation(httpMethod = "GET", value = "Get CADD scores for a (list of) variant(s)", response = Score.class,
            responseContainer = "QueryResponse", hidden = true)
    public Response getCaddScoreByVariant(@PathParam("variants")
                                          @ApiParam(name = "variants", value = "Comma separated list of variants for"
                                                  + "which CADD socores will be returned, e.g. "
                                                  + "19:45411941:T:C,14:38679764:-:GATCTG,1:6635210:G:-,"
                                                  + "2:114340663:GCTGGGCATCCT:ACTGGGCATCCT",
                                                    required = true) String variants) {
        try {
            parseQueryParams();
            VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);

            List<CellBaseDataResult<Score>> functionalScoreVariant =
                    variantDBAdaptor.getFunctionalScoreVariant(Variant.parseVariants(variants), queryOptions);
            return createOkResponse(functionalScoreVariant);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/stats")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Not implemented yet.",
            response = Integer.class, responseContainer = "QueryResponse", hidden = true)
    public Response stats() {
        return super.stats();
    }

    @GET
    @Path("/{id}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get information about a (list of) SNPs", notes = "An independent"
            + " database query will be issued for each region in id, meaning that results for each region will be"
            + " returned in independent CellBaseDataResult objects within the QueryResponse object.",
            response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType", value = ParamConstants.SNP_CONSEQUENCE_TYPE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene", value = ParamConstants.GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "chromosome", value = ParamConstants.CHROMOSOMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "reference", value = ParamConstants.SNP_REFERENCE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "alternate", value = ParamConstants.SNP_ALTERNATE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getByEnsemblId(@PathParam("id")
                                   @ApiParam(name = "id", value = ParamConstants.RS_IDS,
                                        required = true) String id,
                                   @QueryParam("exclude")
                                        @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
                                   @QueryParam("include")
                                        @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
                                   @QueryParam("sort")
                                        @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, VariantDBAdaptor.QueryParams.ID.key());
            List<CellBaseDataResult> queryResults = variationDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(VariantDBAdaptor.QueryParams.ID.key()));
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Retrieves all variation objects", response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.RS_IDS,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType", value = ParamConstants.SNP_CONSEQUENCE_TYPE,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene", value = ParamConstants.GENE_ENSEMBL_IDS,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "chromosome", value = ParamConstants.CHROMOSOMES,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "reference", value = ParamConstants.SNP_REFERENCE,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "alternate", value = ParamConstants.SNP_ALTERNATE,
                    dataType = "java.util.List", paramType = "query")
    })
    public Response search(@QueryParam("exclude")
                               @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
                           @QueryParam("include")
                               @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
                           @QueryParam("sort")
                               @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort,
                           @QueryParam("limit") @DefaultValue("10")
                               @ApiParam(value = ParamConstants.LIMIT_DESCRIPTION) Integer limit,
                           @QueryParam("skip") @DefaultValue("0")
                               @ApiParam(value = ParamConstants.SKIP_DESCRIPTION)  Integer skip) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseLimitAndSkip(limit, skip);
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{id}/next")
    @ApiOperation(httpMethod = "GET", value = "Get information about the next SNP", hidden = true)
    public Response getNextById(@PathParam("id")
                                @ApiParam(name = "id",
                                        value = "Rs id, e.g.: rs6025",
                                        required = true) String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            query.put(VariantDBAdaptor.QueryParams.ID.key(), id.split(",")[0]);
            CellBaseDataResult queryResult = variationDBAdaptor.next(query, queryOptions);
            queryResult.setId(id);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/consequenceTypes")
    @ApiOperation(httpMethod = "GET", value = "Get all sequence ontology terms describing consequence types",
            response = String.class, responseContainer = "QueryResponse")
    public Response getAllConsequenceTypes() {
        try {
            parseQueryParams();
            List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            CellBaseDataResult<String> queryResult = new CellBaseDataResult<>("consequence_types");
            queryResult.setNumResults(consequenceTypes.size());
            queryResult.setResults(consequenceTypes);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // FIXME: 29/04/16 GET and POST web services to be fixed
    @GET
    @Path("/{snpId}/consequenceType")
    @ApiOperation(httpMethod = "GET", value = "Get the biological impact of the SNP(s)", response = String.class,
            responseContainer = "QueryResponse")
    public Response getConsequenceTypeByGetMethod(@PathParam("snpId") String snpId) {
        return getConsequenceType(snpId);
    }

    private Response getConsequenceType(String snpId) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            query.put(VariantDBAdaptor.QueryParams.ID.key(), snpId);
            queryOptions.put(QueryOptions.INCLUDE, "annotation.displayConsequenceType");
            CellBaseDataResult<Variant> queryResult = variationDBAdaptor.get(query, queryOptions);
            CellBaseDataResult queryResult1 = new CellBaseDataResult<>(
                    queryResult.getId(), queryResult.getTime(), queryResult.getEvents(), queryResult.getNumResults(),
                    Collections.singletonList(queryResult.getResults().get(0).getAnnotation().getDisplayConsequenceType()), 1);
            return createOkResponse(queryResult1);
        } catch (Exception e) {
            return createErrorResponse("getConsequenceTypeByPostMethod", e.toString());
        }
    }

    // FIXME: 29/04/16 GET and POST methods to be fixed
    @GET
    @Path("/{snpId}/regulatory")
    @ApiOperation(httpMethod = "GET", value = "Get the regulatory impact of the SNP(s)", hidden = true)
    public Response getRegulatoryByGetMethod(@PathParam("snpId") String snpId) {
        return getRegulatoryType(snpId);
    }

    private Response getRegulatoryType(String snpId) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{snpId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Get the adjacent sequence to the SNP(s) - Not yet implemented",
            hidden = true)
    public Response getSequence(@PathParam("snpId") String query) {
        try {
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
