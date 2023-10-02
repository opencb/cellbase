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

package org.opencb.cellbase.server.rest.feature;

import io.swagger.annotations.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.ProteinQuery;
import org.opencb.cellbase.core.api.TranscriptQuery;
import org.opencb.cellbase.core.api.VariantQuery;
import org.opencb.cellbase.core.api.query.LogicalList;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.ProteinManager;
import org.opencb.cellbase.lib.managers.TranscriptManager;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.cellbase.server.exception.CellBaseServerException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.security.InvalidParameterException;
import java.util.*;

import static org.opencb.cellbase.core.ParamConstants.*;

/**
 * @author imedina
 */
@Path("/{apiVersion}/{species}/feature/gene")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Gene", description = "Gene RESTful Web Services API")
public class GeneWSServer extends GenericRestWSServer {

    private GeneManager geneManager;
    private TranscriptManager transcriptManager;
    private VariantManager variantManager;
    private ProteinManager proteinManager;

    public GeneWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION,
            defaultValue = DEFAULT_VERSION) String apiVersion,
                        @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                        @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly")
                                String assembly,
                        @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0") @QueryParam("dataRelease")
                                int dataRelease,
                        @ApiParam(name = "token", value = DATA_ACCESS_TOKEN_DESCRIPTION) @DefaultValue("") @QueryParam("token")
                                String token,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws CellBaseServerException {
        super(apiVersion, species, uriInfo, hsr);
        try {
            List<String> assemblies = uriInfo.getQueryParameters().get("assembly");
            if (CollectionUtils.isNotEmpty(assemblies)) {
                assembly = assemblies.get(0);
            }
            if (StringUtils.isEmpty(assembly)) {
                assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
            }

            geneManager = cellBaseManagerFactory.getGeneManager(species, assembly);
            transcriptManager = cellBaseManagerFactory.getTranscriptManager(species, assembly);
            variantManager = cellBaseManagerFactory.getVariantManager(species, assembly);
            proteinManager = cellBaseManagerFactory.getProteinManager(species, assembly);
        } catch (Exception e) {
            throw new CellBaseServerException(e.getMessage());
        }
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Gene.class);
    }

    @GET
    @Path("/groupBy")
    @ApiOperation(httpMethod = "GET", value = "Groups gene HGNC symbols by a field(s). ", response = Integer.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = TRANSCRIPT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = TRANSCRIPT_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ANNOTATION_DISEASES_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ANNOTATION_DISEASES_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ANNOTATION_EXPRESSION_TISSUE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ANNOTATION_DRUGS_NAME_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response groupBy(@DefaultValue("") @QueryParam("field") @ApiParam(name = "field", value = "Comma separated list of "
            + "field(s) to group by, e.g.: biotype.", required = true) String field) {
        try {
            copyToFacet("field", field);
            GeneQuery geneQuery = new GeneQuery(uriParams);
            CellBaseDataResult<Gene> queryResults = geneManager.groupBy(geneQuery);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/aggregationStats")
    @ApiOperation(httpMethod = "GET", value = "Counts gene HGNC symbols by a field(s). ", response = Integer.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = TRANSCRIPT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = TRANSCRIPT_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ANNOTATION_DISEASES_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ANNOTATION_DISEASES_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ANNOTATION_EXPRESSION_TISSUE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ANNOTATION_DRUGS_NAME_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAggregationStats(@DefaultValue("") @QueryParam("field")
                                        @ApiParam(name = "field", value = GROUP_BY_FIELDS, required = true) String field) {
        try {
            copyToFacet("field", field);
            GeneQuery geneQuery = new GeneQuery(uriParams);
            CellBaseDataResult<Gene> queryResults = geneManager.aggregationStats(geneQuery);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + DOT_NOTATION_NOTE,
            value = "Retrieves all gene objects", response = Gene.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_XREFS_PARAM,
                    value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = GENE_SOURCE, value = GENE_SOURCE_DESCRIPTION, required = false,
                    allowableValues="ensembl,refseq", defaultValue = "ensembl", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_BIOTYPES_PARAM,
                    value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_IDS_PARAM, value = TRANSCRIPT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_NAMES_PARAM, value = TRANSCRIPT_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_ANNOTATION_FLAGS_PARAM,
                    value = TRANSCRIPT_ANNOTATION_FLAGS_DESCRIPTION,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_TFBS_IDS_PARAM, value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_TFBS_PFMIDS_PARAM, value = TRANSCRIPT_TFBS_PFMIDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = TRANSCRIPT_TRANSCRIPTION_FACTORS_PARAM,
                    value = TRANSCRIPT_TRANSCRIPTION_FACTORS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ONTOLOGY_PARAM, value = ONTOLOGY_DESCRIPTION,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = ANNOTATION_DISEASES_PARAM,
                    value = ANNOTATION_DISEASES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ANNOTATION_EXPRESSION_TISSUE_PARAM,
                    value = ANNOTATION_EXPRESSION_TISSUE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ANNOTATION_EXPRESSION_VALUE_PARAM,
                    value = ANNOTATION_EXPRESSION_VALUE_DESCRIPTION,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = ANNOTATION_DRUGS_NAME_PARAM, value = ANNOTATION_DRUGS_NAME_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ANNOTATION_CONSTRAINTS_PARAM, value = ANNOTATION_CONSTRAINTS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ANNOTATION_TARGETS_PARAM,
                    value = ANNOTATION_TARGETS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "mirna", value = MIRNA_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
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
    public Response getAll(@QueryParam(SPLIT_RESULT_PARAM) @ApiParam(name = SPLIT_RESULT_PARAM,
            value = SPLIT_RESULT_DESCRIPTION,
            defaultValue = "false", allowableValues = "false,true") boolean splitResultById) {
        try {
            if (splitResultById) {
                // if we are splitting, can only have ONE of the three identifier fields populated
                if (!validateGeneIdentifiers()) {
                    return createErrorResponse(new InvalidParameterException(
                            "When 'splitResultById' is TRUE, you can only have ONE identifier field populated: id, name or xref"));
                }
                List<GeneQuery> geneQueries = new ArrayList<>();
                // look in IDs, names and xrefs
                String[] identifiers = getGeneIdentifiers();
                logger.info("/search identifiers: {} ", identifiers);
                for (String identifier : identifiers) {
                    GeneQuery geneQuery = new GeneQuery(uriParams);
                    geneQuery.setTranscriptsXrefs(Collections.singletonList(identifier));
                    geneQueries.add(geneQuery);
                    logger.info("/search geneQuery: {}", geneQuery.toString());
                }
                List<CellBaseDataResult<Gene>> queryResults = geneManager.search(geneQueries);
                return createOkResponse(queryResults);
            } else {
                GeneQuery geneQuery = new GeneQuery(uriParams);
                logger.info("/search GeneQuery: {} ", geneQuery.toString());
                CellBaseDataResult<Gene> queryResults = geneManager.search(geneQuery);
                return createOkResponse(queryResults);
            }

        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    /**
     * Only ONE of the identifier fields can be provided. Otherwise we don't know which ID to split on. ONE of these can be
     * not null:
     *  id
     *  name
     *  xref
     *
     * @return TRUE if valid, only ONE param has a valud, false if invalid, more than one field is populated.
     */
    private boolean validateGeneIdentifiers() {
        String id = uriParams.get("id");
        String name = uriParams.get("name");
        String xref = uriParams.get(TRANSCRIPT_XREFS_PARAM);
        if (StringUtils.isNotEmpty(id)) {
            return StringUtils.isEmpty(name) && StringUtils.isEmpty(xref);
        }
        if (StringUtils.isNotEmpty(name)) {
            return StringUtils.isEmpty(id) && StringUtils.isEmpty(xref);
        }
        if (StringUtils.isNotEmpty(xref)) {
            return StringUtils.isEmpty(id) && StringUtils.isEmpty(name);
        }
        // nothing was not null, that's illegal too
        return false;
    }

    private String[] getGeneIdentifiers() {
        String id = uriParams.get("id");
        String name = uriParams.get("name");
        String xref = uriParams.get(TRANSCRIPT_XREFS_PARAM);
        if (StringUtils.isNotEmpty(id)) {
            return id.split(",");
        } else if (StringUtils.isNotEmpty(name)) {
            return name.split(",");
        } else if (StringUtils.isNotEmpty(xref)) {
            return xref.split(",");
        }
        // nothing found
        return null;
    }

    @GET
    @Path("/{genes}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified gene(s)", response = Gene.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = GENE_SOURCE, value = GENE_SOURCE_DESCRIPTION, required = false,
                    allowableValues="ensembl,refseq", defaultValue = "ensembl", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getInfo(@PathParam("genes") @ApiParam(name = "genes", value = GENE_IDS, required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery(uriParams);
            String source = "ensembl";
            if (geneQuery.getSource() != null && !geneQuery.getSource().isEmpty()) {
                source = geneQuery.getSource().get(0);
            }
            List<CellBaseDataResult<Gene>> queryResults = geneManager.info(Arrays.asList(genes.split(",")), geneQuery, source,
                    getDataRelease(), getToken());
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{gene}/startsWith")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified gene(s)", response = Gene.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = GENE_SOURCE, value = GENE_SOURCE_DESCRIPTION, required = false,
                    allowableValues="ensembl,refseq", defaultValue = "ensembl", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response startsWith(@PathParam("gene") @ApiParam(name = "gene", value = GENE_IDS, required = true) String gene) {
        try {
            GeneQuery geneQuery = new GeneQuery(uriParams);
            String source = "ensembl";
            if (geneQuery.getSource() != null && !geneQuery.getSource().isEmpty()) {
                source = geneQuery.getSource().get(0);
            }

            CellBaseDataResult<Gene> queryResults = geneManager.startsWith(gene, geneQuery.toQueryOptions(), getDataRelease());
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Get the transcripts of a list of gene IDs", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getTranscriptsByGenes(@PathParam("genes") @ApiParam(name = "genes",
            value = GENE_XREF_IDS, required = true) String genes) {
        try {
            List<TranscriptQuery> queries = new ArrayList<>();
            String[] ids = genes.split(",");
            for (String id : ids) {
                TranscriptQuery query = new TranscriptQuery(uriParams);
                query.setTranscriptsXrefs(new LogicalList<String>(Collections.singletonList(id)));
                queries.add(query);
            }
            List<CellBaseDataResult<Transcript>> queryResults = transcriptManager.search(queries);
            return createOkResponse(queryResults);

        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/distinct")
    @ApiOperation(httpMethod = "GET", notes = "Gets a unique list of values, e.g. biotype or chromosome",
            value = "Get a unique list of values for a given field.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = TRANSCRIPT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = TRANSCRIPT_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ANNOTATION_DISEASES_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ANNOTATION_DISEASES_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ANNOTATION_EXPRESSION_TISSUE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ANNOTATION_DRUGS_NAME_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getUniqueValues(@QueryParam("field") @ApiParam(name = "field", required = true,
            value = "Name of column to return, e.g. biotype") String field) {
        try {
            copyToFacet("field", field);
            GeneQuery geneQuery = new GeneQuery(uriParams);
            CellBaseDataResult<String> queryResults = geneManager.distinct(geneQuery);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/variant")
    @ApiOperation(httpMethod = "GET", value = "Get all variants within the specified genes", response = Variant.class,
            notes = "A large number of variants are usually associated to genes. Variant data tends to be heavy. Please,"
                    + "make use of the limit/exclude/include and the rest of query parameters to limit the size of your "
                    + "results.", responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "consequenceType", value = CONSEQUENCE_TYPE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getSNPByGenes(@PathParam("genes")
                                  @ApiParam(name = "genes", value = GENE_XREF_IDS) String genes) {
        try {
            List<VariantQuery> queries = new ArrayList<>();
            String[] ids = genes.split(",");
            for (String id : ids) {
                VariantQuery query = new VariantQuery(uriParams);
                query.setGenes(new LogicalList<String>(Collections.singletonList(id)));
                queries.add(query);
            }
            List<CellBaseDataResult<Variant>> queryResults = variantManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Get all transcription factor binding sites for this gene(s)",
            response = TranscriptTfbs.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAllTfbs(@PathParam("genes") @ApiParam(name = "genes", value = GENE_ENSEMBL_IDS,
            required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery(uriParams);
            geneQuery.setIds(Arrays.asList(genes.split(",")));
            List<CellBaseDataResult<TranscriptTfbs>> queryResults = geneManager.getTfbs(geneQuery);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/protein")
    @ApiOperation(httpMethod = "GET", value = "Return info for the corresponding proteins", response = Entry.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getProteinById(@PathParam("genes") @ApiParam(name = "genes", value = GENE_IDS,
            required = true) String genes) {
        try {
            ProteinQuery query = new ProteinQuery(uriParams);
            query.setGenes(Arrays.asList(genes.split(",")));
            logger.info("REST proteinQuery: {}", query.toString());
            CellBaseDataResult<Entry> queryResults = proteinManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Return sequences for specified genes", response = GenomeSequenceFeature.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getSequence(@PathParam("genes") @ApiParam(name = "genes", value = GENE_IDS,
            required = true) String genes) {
        try {
            List<GeneQuery> queries = new ArrayList<>();
            String[] identifiers =  genes.split(",");
            for (String identifier : identifiers) {
                GeneQuery query = new GeneQuery(uriParams);
                query.setTranscriptsXrefs(Arrays.asList(identifier));
                queries.add(query);
                logger.info("REST GeneQuery: {} ", query.toString());
            }
            List<CellBaseDataResult<GenomeSequenceFeature>> queryResults = geneManager.getSequence(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/help")
    public Response help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input:\n");
        sb.append("all id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get gene information: name, position, biotype.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- transcript: Get all transcripts for this gene.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
        sb.append("- tfbs: Get transcription factor binding sites (TFBSs) that map to the promoter region of this gene.\n");
        sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
        sb.append("- mirna_target: Get all microRNA target sites for this gene.\n");
        sb.append(" Output columns: miRBase ID, gene target name, chromosome, start, end, strand, pubmed ID, source.\n\n");
        sb.append("- protein_feature: Get protein information related to this gene.\n");
        sb.append(" Output columns: feature type, aa start, aa end, original, variation, identifier, description.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("https://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Gene");

        return createOkResponse(sb.toString());
    }

}
