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
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Snp;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.SnpQuery;
import org.opencb.cellbase.core.api.VariantQuery;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.cellbase.server.exception.CellBaseServerException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.core.ParamConstants.*;

@Path("/{apiVersion}/{species}/genomic/variant")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Variant", description = "Variant RESTful Web Services API")
public class VariantWSServer extends GenericRestWSServer {

    private VariantManager variantManager;

    public VariantWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION,
            defaultValue = DEFAULT_VERSION) String apiVersion,
                           @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                           @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly")
                           String assembly,
                           @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0") @QueryParam("dataRelease")
                           int dataRelease,
                           @ApiParam(name = "apiKey", value = API_KEY_DESCRIPTION) @DefaultValue("") @QueryParam("apiKey") String apiKey,
                           @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws CellBaseServerException {
        super(apiVersion, species, assembly, uriInfo, hsr);
        try {
            variantManager = cellBaseManagerFactory.getVariantManager(this.species, this.assembly);
        } catch (Exception e) {
            throw new CellBaseServerException(e.getMessage());
        }
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Variant.class);
    }

    @GET
    @Path("/{variants}/hgvs")
    @ApiOperation(httpMethod = "GET", value = "FIXME: description needed", response = List.class,
            responseContainer = "QueryResponse")
    public Response getHgvs(@PathParam("variants") @ApiParam(name = "variants", value = RS_IDS,
            required = true) String id) {
        try {
            DataRelease dataRelease = getDataRelease(getDataRelease(), species, assembly);
            List<CellBaseDataResult<String>> queryResults = variantManager.getHgvsByVariant(id, dataRelease);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{variants}/normalization")
    @ApiOperation(httpMethod = "GET", value = "FIXME: description needed", response = Map.class,
            responseContainer = "QueryResponse")
    public Response getNormalization(@PathParam("variants") @ApiParam(name = "variants", value = RS_IDS,
            required = true) String id,
                                     @QueryParam("decompose")
                                     @ApiParam(name = "decompose",
                                             value = "Boolean to indicate whether input MNVs should be "
                                                     + "decomposed or not as part of the normalisation step.",
                                             allowableValues = "false,true",
                                             defaultValue = "false") Boolean decompose,
                                     @QueryParam("leftAlign")
                                     @ApiParam(name = "leftAlign",
                                             value = "Boolean to indicate whether input ambiguous INDELS should be "
                                                     + "left aligned or not as part of the normalisation step.",
                                             allowableValues = "false,true",
                                             defaultValue = "false") Boolean leftAlign) {

        try {
            DataRelease dataRelease = getDataRelease(getDataRelease(), species, assembly);
            CellBaseDataResult<Variant> queryResults = variantManager.getNormalizationByVariant(id, Boolean.TRUE.equals(decompose),
                    Boolean.TRUE.equals(leftAlign), dataRelease);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @POST
    @Consumes("text/plain")
    @Path("/annotation")
    @ApiOperation(httpMethod = "POST",
            value = "Retrieves variant annotation for a list of variants.", notes = "Include and exclude lists take"
            + " values from the following set: {variation, traitAssociation, conservation, consequenceType,"
            + " expression, geneDisease, drugInteraction, populationFrequencies, repeats, hgvs, geneConstraints, mirnaTargets}.",
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
                                                                + "include decomposing MNV nor left alignment",
                                                        allowableValues = "false,true", defaultValue = "false") Boolean normalize,
                                                @QueryParam("decompose")
                                                @ApiParam(name = "decompose",
                                                        value = "Boolean to indicate whether input MNVs should be "
                                                                + "decomposed or not as part of the normalisation step.",
                                                        allowableValues = "false,true",
                                                        defaultValue = "false") Boolean decompose,
                                                @QueryParam("leftAlign")
                                                @ApiParam(name = "leftAlign",
                                                        value = "Boolean to indicate whether input ambiguous INDELS should be "
                                                                + "left aligned or not as part of the normalisation step.",
                                                        allowableValues = "false,true",
                                                        defaultValue = "false") Boolean leftAlign,
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
                                                        value = "true/false to specify whether variant match in the clinical variant"
                                                                + " collection should also be performed at the aminoacid change level",
                                                        allowableValues = "false,true",
                                                        defaultValue = "false", required = false) Boolean checkAminoAcidChange,
                                                @QueryParam("consequenceTypeSource")
                                                @ApiParam(name = "consequenceTypeSource", value = "Gene set, either ensembl (default) "
                                                        + "or refSeq", allowableValues = "ensembl,refseq", defaultValue = "ensembl",
                                                        required = false) String consequenceTypeSource
    ) {

        try {
            checkNormalizationConfig();
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e);
        }


        return getAnnotationByVariant(variants,
                normalize,
                decompose,
                leftAlign,
                ignorePhase,
                phased,
                imprecise,
                svExtraPadding,
                cnvExtraPadding,
                checkAminoAcidChange,
                consequenceTypeSource);
    }

    @GET
    @Path("/{variants}/annotation")
    @ApiOperation(httpMethod = "GET",
            value = "Retrieves variant annotation for a list of variants.", notes = "Include and exclude lists take"
            + " values from the following set: {variation, traitAssociation, conservation, consequenceType,"
            + " expression, geneDisease, drugInteraction, populationFrequencies, repeats, hgvs, geneConstraints, mirnaTargets}.",
            response = VariantAnnotation.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getAnnotationByVariantsGET(@PathParam("variants")
                                               @ApiParam(name = "variants", value = VARIANTS,
                                                       required = true) String variants,
                                               @QueryParam("normalize")
                                               @ApiParam(name = "normalize", value = NORMALISE,
                                                       allowableValues = "false,true",
                                                       defaultValue = "true", required = false) Boolean normalize,
                                               @QueryParam("decompose")
                                               @ApiParam(name = "decompose", value = DECOMPOSE,
                                                       allowableValues = "false,true",
                                                       defaultValue = "false") Boolean decompose,
                                               @QueryParam("leftAlign")
                                               @ApiParam(name = "leftAlign", value = LEFT_ALIGN,
                                                       allowableValues = "false,true",
                                                       defaultValue = "false") Boolean leftAlign,
                                               @QueryParam("ignorePhase")
                                               @ApiParam(name = "ignorePhase", value = IGNORE_PHASE,
                                                       allowableValues = "false,true",
                                                       required = false) Boolean ignorePhase,
                                               @Deprecated
                                               @QueryParam("phased")
                                               @ApiParam(name = "phased", value = PHASED,
                                                       allowableValues = "false,true", required = false) Boolean phased,
                                               @QueryParam("imprecise")
                                               @ApiParam(name = "imprecise",
                                                       value = IMPRECISE, allowableValues = "false,true",
                                                       defaultValue = "true", required = false) Boolean imprecise,
                                               @QueryParam("svExtraPadding")
                                               @ApiParam(name = "svExtraPadding",
                                                       value = SV_EXTRA_PADDING,
                                                       defaultValue = "0", required = false) Integer svExtraPadding,
                                               @QueryParam("cnvExtraPadding")
                                               @ApiParam(name = "cnvExtraPadding",
                                                       value = "Integer to optionally provide the size of the extra"
                                                               + " padding to be used when annotating imprecise (or not)"
                                                               + " CNVs",
                                                       defaultValue = "0", required = false) Integer cnvExtraPadding,
                                               @QueryParam("checkAminoAcidChange")
                                               @ApiParam(name = "checkAminoAcidChange", value = "<DESCRIPTION GOES HERE>",
                                                       allowableValues = "false,true", defaultValue = "false", required = false)
                                               Boolean checkAminoAcidChange,
                                               @QueryParam("consequenceTypeSource")
                                               @ApiParam(name = "consequenceTypeSource", value = "Gene set, either ensembl (default) "
                                                       + "or refseq", allowableValues = "ensembl,refseq", allowMultiple = true,
                                                       defaultValue = "ensembl", required = false) String consequenceTypeSource

    ) {
        try {
            checkNormalizationConfig();
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e);
        }

        return getAnnotationByVariant(variants,
                normalize,
                decompose,
                leftAlign,
                ignorePhase,
                phased,
                imprecise,
                svExtraPadding,
                cnvExtraPadding,
                checkAminoAcidChange,
                consequenceTypeSource);
    }

    private void checkNormalizationConfig() throws IllegalArgumentException {
        if (uriParams.containsKey("skipDecompose")) {
            throw new IllegalArgumentException("Param 'skipDecompose' is not supported anymore. Please, use 'decompose' instead");
        }
        if (uriParams.containsKey("normalize")) {
            if (!Boolean.parseBoolean(uriParams.get("normalize"))) {
                if (uriParams.containsKey("decompose") && Boolean.parseBoolean(uriParams.get("decompose"))) {
                    throw new IllegalArgumentException("Incompatible parameter usage: 'normalize'=false and 'decompose'=true");
                }
                if (uriParams.containsKey("leftAlign") && Boolean.parseBoolean(uriParams.get("leftAlign"))) {
                    throw new IllegalArgumentException("Incompatible parameter usage: 'normalize'=false and 'leftAlign'=true");
                }
            }
        }
    }

    private Response getAnnotationByVariant(String variants,
                                            Boolean normalize,
                                            Boolean decompose,
                                            Boolean leftAlign,
                                            Boolean ignorePhase,
                                            @Deprecated Boolean phased,
                                            Boolean imprecise,
                                            Integer svExtraPadding,
                                            Integer cnvExtraPadding,
                                            Boolean checkAminoAcidChange,
                                            String consequenceTypeSource) {
        try {
            VariantQuery query = new VariantQuery(uriParams);
            // use the processed value, as there may be more than one "consequenceTypeSource" in the URI
            String consequenceTypeSources = (StringUtils.isEmpty(uriParams.get("consequenceTypeSource")) ? consequenceTypeSource
                    : uriParams.get("consequenceTypeSource"));
            DataRelease dataRelease = getDataRelease(getDataRelease(), species, assembly);
            List<CellBaseDataResult<VariantAnnotation>> queryResults = variantManager.getAnnotationByVariant(query.toQueryOptions(),
                    variants, normalize, decompose, leftAlign, ignorePhase, phased, imprecise, svExtraPadding, cnvExtraPadding,
                    checkAminoAcidChange, consequenceTypeSources, dataRelease, getApiKey());

            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/stats")
//    @Override
//    @ApiOperation(httpMethod = "GET", value = "Not implemented yet.",
//            response = Integer.class, responseContainer = "QueryResponse", hidden = true)
//    public Response stats() {
//        return super.stats();
//    }

    @GET
    @Path("/{variants}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get information about a (list of) variants", notes = "An independent"
            + " database query will be issued for each region in id, meaning that results for each region will be"
            + " returned in independent CellBaseDataResult objects within the QueryResponse object.",
            response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getInfo(@PathParam("variants") @ApiParam(name = "variants", value = RS_IDS,
            required = true) String id) {
        try {
            VariantQuery query = new VariantQuery(uriParams);
            List<CellBaseDataResult<Variant>> queryResults = variantManager.info(Arrays.asList(id.split(",")), query,
                    getDataRelease(), getApiKey());
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
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = RS_IDS,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType", value = CONSEQUENCE_TYPE,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene", value = GENE_ENSEMBL_IDS,
                    dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "chromosome", value = ParamConstants.CHROMOSOMES,
//                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
//                    required = false, dataType = "java.util.List", paramType = "query",
//                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response search() {
        try {
            VariantQuery query = new VariantQuery(uriParams);
            query.setDataRelease(getDataRelease());
            logger.info("/search VariantQuery: {}", query.toString());
            CellBaseDataResult<Variant> queryResults = variantManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{id}/next")
//    @ApiOperation(httpMethod = "GET", value = "Get information about the next SNP", hidden = true)
//    public Response getNextById(@PathParam("id")
//                                @ApiParam(name = "id",
//                                        value = "Rs id, e.g.: rs6025",
//                                        required = true) String id) {
//        try {
//            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
//            query.put(VariantDBAdaptor.QueryParams.ID.key(), id.split(",")[0]);
//            CellBaseDataResult queryResult = variationDBAdaptor.next(query, queryOptions);
//            queryResult.setId(id);
//            return createOkResponse(queryResult);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/consequenceTypes")
    @ApiOperation(httpMethod = "GET", value = "Get all sequence ontology terms describing consequence types",
            response = String.class, responseContainer = "QueryResponse")
    public Response getAllConsequenceTypes() {
        try {
//            parseQueryParams();
            CellBaseDataResult<String> queryResult = variantManager.getConsequenceTypes();
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/snp")
    @ApiOperation(httpMethod = "GET", value = "Get SNPs", response = Snp.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List", paramType = "query")
    })
    public Response getSnps(@QueryParam("id") @ApiParam(name = "id", value = "ID") String id,
                            @QueryParam("chromosome") @ApiParam(name = "chromosome", value = "Chromosome") String chromosome,
                            @QueryParam("position") @ApiParam(name = "position", value = "Position") Integer position,
                            @QueryParam("reference") @ApiParam(name = "reference", value = "Reference") String reference) {
        try {
            SnpQuery query = new SnpQuery(uriParams);
            CellBaseDataResult<Snp> queryResult = variantManager.getSnps(query);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}
