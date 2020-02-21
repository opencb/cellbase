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
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.ProteinManager;
import org.opencb.cellbase.lib.managers.TranscriptManager;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public GeneWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                        @PathParam("species") @ApiParam(name = "species",
                                value = ParamConstants.SPECIES_DESCRIPTION) String species,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException,
            SpeciesException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            this.assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }
        geneManager = cellBaseManagerFactory.getGeneManager(species, assembly);
        transcriptManager = cellBaseManagerFactory.getTranscriptManager(species, assembly);
        variantManager = cellBaseManagerFactory.getVariantManager(species, assembly);
        proteinManager = cellBaseManagerFactory.getProteinManager(species, assembly);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = ParamConstants.DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Gene.class);
    }

//    @GET
//    @Path("/first")
//    @Override
//    @Deprecated
//    @ApiOperation(httpMethod = "GET", value = "Get the first object in the database", response = Gene.class,
//            responseContainer = "QueryResponse", hidden = true)
//    public Response first() {
////        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//        QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 1);
//        CellBaseDataResult<Gene> search = geneManager.search(new Query(), queryOptions);
//        return createOkResponse(search);
//    }

//    @GET
//    @Path("/count")
//    @Deprecated
//    @ApiOperation(httpMethod = "GET", value = "Get the number of genes in the database", response = Integer.class,
//            responseContainer = "QueryResponse", hidden = true)
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "id", value = ParamConstants.GENE_ENSEMBL_IDS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "name", value = ParamConstants.GENE_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
//                    required = false, dataType = "java.util.List", paramType = "query")
//    })
//    public Response count() {
//        try {
//            parseQueryParams();
//            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//            geneManager.search(new Query(), new QueryOptions(QueryOptions.COUNT, true));
//            return createOkResponse(geneDBAdaptor.count(query));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/stats")
//    @Override
//    @ApiOperation(httpMethod = "GET", value = "Not yet implemented ", response = Integer.class,
//            responseContainer = "QueryResponse", hidden = true)
//    public Response stats() {
//        return super.stats();
//    }

    @GET
    @Path("/groupby")
    @ApiOperation(httpMethod = "GET", value = "Groups gene HGNC symbols by a field(s). ", response = Integer.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response groupBy(@DefaultValue("")
                            @QueryParam("fields")
                            @ApiParam(name = "fields",
                                    value = "Comma separated list of field(s) to group by, e.g.: biotype.",
                                    required = true) String fields) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            CellBaseDataResult<Gene> queryResults = geneManager.groupBy(new Query(), fields);
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
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAggregationStats(@DefaultValue("") @QueryParam("fields")
            @ApiParam(name = "fields", value = ParamConstants.GROUP_BY_FIELDS, required = true) String fields) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            CellBaseDataResult<Gene> queryResults = geneManager.aggregationStats(new Query(), fields);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Retrieves all gene objects", response = Gene.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.annotationFlags", value = ParamConstants.TRANSCRIPT_ANNOTATION_FLAGS,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.value", value = ParamConstants.ANNOTATION_EXPRESSION_VALUE,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getAll() throws CellbaseException {
        GeneQuery geneQuery = new GeneQuery();
        geneQuery.updateParams(uriParams);
        CellBaseDataResult<Gene> queryResults = geneManager.search(geneQuery);
        return createOkResponse(queryResults);
    }

//    @GET
//    @Path("/list")
//    @Deprecated
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene Ensembl IDs", response = List.class,
//            responseContainer = "QueryResponse", hidden = true)
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "name", value = ParamConstants.GENE_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
//                    required = false, dataType = "java.util.List", paramType = "query")
//    })
//    public Response getAllIDs() {
//        try {
//            parseQueryParams();
//            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//            queryOptions.put("include", Collections.singletonList("id"));
//            return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{genes}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified gene(s)", response = Gene.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.annotationFlags", value = ParamConstants.TRANSCRIPT_ANNOTATION_FLAGS,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getByEnsemblId(@PathParam("genes")
                                       @ApiParam(name = "genes", value = ParamConstants.GENE_XREF_IDS, required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            List<CellBaseDataResult> queryResults = geneManager.info(new Query(), genes);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{genes}/next")
//    @ApiOperation(httpMethod = "GET", value = "Get information about the specified gene(s) - Not yet implemented", hidden = true)
//    public Response getNextByEnsemblId(@PathParam("genes") String genes) {
//        try {
//            parseQueryParams();
//            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//            return createOkResponse(geneDBAdaptor.next(query, queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{genes}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Get the transcripts of a list of gene IDs", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "transcripts.region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getTranscriptsByGenes(@PathParam("genes") @ApiParam(name = "genes",
            value = ParamConstants.GENE_XREF_IDS, required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            List<CellBaseDataResult> queryResults = transcriptManager.info(query, queryOptions, genes);
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
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.GENE_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.GENE_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getUniqueValues(@QueryParam("field") @ApiParam(name = "field", required = true,
            value = "Name of column to return, e.g. biotype") String field) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            CellBaseDataResult<Gene> queryResults = geneManager.distinct(new Query(), field);
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
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "reference", value = ParamConstants.REFERENCE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "alternate", value = ParamConstants.ALTERNATE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType", value = ParamConstants.CONSEQUENCE_TYPE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getSNPByGenes(@PathParam("genes")
                @ApiParam(name = "genes", value = ParamConstants.GENE_XREF_IDS) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            List<CellBaseDataResult> queryResults = variantManager.info(query, queryOptions, genes);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/regulation")
    @ApiOperation(httpMethod = "GET", value = "Get all transcription factor binding sites for this gene(s) - Not yet implemented",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse", hidden = true)
    public Response getAllRegulatoryElements(@PathParam("genes") @ApiParam(name = "genes", value = ParamConstants.GENE_XREF_IDS,
            required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            List<CellBaseDataResult> queryResults = geneManager.getRegulatoryElements(new Query(), genes);
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
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ParamConstants.ANNOTATION_EXPRESSION_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene", value = ParamConstants.ANNOTATION_DRUGS_GENE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getAllTfbs(@PathParam("genes") @ApiParam(name = "genes", value = ParamConstants.GENE_ENSEMBL_IDS,
                                       required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            List<CellBaseDataResult> queryResults = geneManager.getTfbs(new Query(), genes);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{genes}/protein")
    @ApiOperation(httpMethod = "GET", value = "Return info of the corresponding proteins", response = Entry.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = ParamConstants.PROTEIN_KEYWORD, required = false,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getProteinById(@PathParam("genes") @ApiParam(name = "genes", value = ParamConstants.GENE_IDS,
                                           required = true) String genes) {
        try {
            GeneQuery geneQuery = new GeneQuery();
            geneQuery.updateParams(uriParams);
            List<CellBaseDataResult> queryResults = proteinManager.info(query, queryOptions, genes);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{genes}/ppi")
//    @ApiOperation(httpMethod = "GET", value = "Get the protein-protein interactions in which this gene is involved",
//            hidden = true)
//    public Response getPPIByEnsemblId(@PathParam("genes") String gene) {
//        try {
//            parseQueryParams();
//            ProteinProteinInteractionDBAdaptor ppiDBAdaptor =
//                    dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.assembly);
//            Query query = new Query(ProteinProteinInteractionDBAdaptor.QueryParams.XREFs.key(), gene);
//            return createOkResponse(ppiDBAdaptor.nativeGet(query, queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/{genes}/clinical")
//    @ApiOperation(httpMethod = "GET", notes = "WARNING: this web service is currently deprecated, is no longer "
//            + " supported and will"
//            + " soon be removed. No more than 1000 objects are allowed to be returned at a time. "
//            + "Please note that ClinVar, COSMIC or GWAS objects may be returned as stored in the database. Please have "
//            + "a look at "
//            + "https://github.com/opencb/cellbase/wiki/MongoDB-implementation#clinical for further details.",
//            value = "[DEPRECATED] Use {version}/{species}/clinical/variant/search instead.", response = Document.class,
//            responseContainer = "QueryResponse", hidden = true)
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "source",
//                    value = "Comma separated list of database sources of the documents to be returned. Possible values "
//                            + " are clinvar,cosmic or gwas. E.g.: clinvar,cosmic",
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "so",
//                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
//                            + "matches will be returned.",
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "phenotype",
//                    value = "String to indicate the phenotypes to query. A text search will be run.",
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "type",
//                    value = "Comma separated list of variant types as stored in ClinVar (only enabled for ClinVar "
//                            + "variants, e.g. \"single nucleotide variant\" ",
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "review",
//                    value = "Comma separated list of review lables (only enabled for ClinVar variants), "
//                            + " e.g.: CRITERIA_PROVIDED_SINGLE_SUBMITTER",
//                    required = false, dataType = "java.util.List", paramType = "query"),
//            @ApiImplicitParam(name = "significance",
//                    value = "Comma separated list of clinical significance labels as stored in ClinVar (only enabled "
//                            + "for ClinVar variants), e.g.: Benign",
//                    required = false, dataType = "java.util.List", paramType = "query")
//    })
//    @Deprecated
//    public Response getAllClinvarByGene(@PathParam("genes")
//                                        @ApiParam(name = "genes", value = "String containing one gene symbol, e.g:"
//                                                + " BRCA2", required = true) String genes) {
//        try {
//            parseQueryParams();
////            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
//            ClinicalDBAdaptor clinicalDBAdaptor = geneManager.getClinicalDBAdaptor(this.species, this.assembly);
//            query.put("gene", genes);
//            CellBaseDataResult queryResult = clinicalDBAdaptor.nativeGet(query, queryOptions);
//            queryResult.setId(genes);
//            return createOkResponse(queryResult);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

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
