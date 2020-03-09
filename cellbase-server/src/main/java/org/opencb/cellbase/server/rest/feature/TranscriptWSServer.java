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
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.ProteinQuery;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.ProteinManager;
import org.opencb.cellbase.lib.managers.TranscriptManager;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

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
import java.util.Map;

/**
 * @author imedina
 */
@Path("/{apiVersion}/{species}/feature/transcript")
@Api(value = "Transcript", description = "Transcript RESTful Web Services API")
@Produces(MediaType.APPLICATION_JSON)
public class TranscriptWSServer extends GenericRestWSServer {

    private TranscriptManager transcriptManager;
    private GeneManager geneManager;
    private ProteinManager proteinManager;

    public TranscriptWSServer(@PathParam("apiVersion")
                              @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                      defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                              @PathParam("species")
                              @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                              @ApiParam(name = "assembly", value = ParamConstants.ASSEMBLY_DESCRIPTION)
                              @DefaultValue("")
                              @QueryParam("assembly") String assembly,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }
        transcriptManager = cellBaseManagerFactory.getTranscriptManager(species, assembly);
        geneManager = cellBaseManagerFactory.getGeneManager(species, assembly);
        proteinManager = cellBaseManagerFactory.getProteinManager(species, assembly);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = ParamConstants.DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Transcript.class);
    }

//    @GET
//    @Path("/first")
//    @Override
//    @Deprecated
//    @ApiOperation(httpMethod = "GET", value = "Get the first transcript in the database", response = Transcript.class,
//        responseContainer = "QueryResponse", hidden = true)
//    public Response first() throws Exception {
//        parseQueryParams();
//        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
//        return createOkResponse(transcriptDBAdaptor.first(queryOptions));
//    }

//    @GET
//    @Path("/count")
//    @Deprecated
//    @ApiOperation(httpMethod = "GET", value = "Get the number of transcripts in the database", response = Integer.class,
//        responseContainer = "QueryResponse", hidden = true)
//    public Response count(@DefaultValue("")
//                          @QueryParam("region")
//                          @ApiParam(name = "region",
//                                  value = ParamConstants.REGION_DESCRIPTION,
//                                  required = false) String region,
//                          @DefaultValue("")
//                          @QueryParam("biotype")
//                          @ApiParam(name = "biotype",
//                                  value = ParamConstants.GENE_BIOTYPES,
//                                  required = false) String biotype,
//                          @DefaultValue("")
//                          @QueryParam("xrefs")
//                          @ApiParam(name = "xrefs",
//                                  value = ParamConstants.TRANSCRIPT_XREFS,
//                                  required = false) String xrefs) throws Exception {
//        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
//        query.append(TranscriptDBAdaptor.QueryParams.REGION.key(), region);
//        query.append(TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), biotype);
//        query.append(TranscriptDBAdaptor.QueryParams.XREFS.key(), xrefs);
//        return createOkResponse(transcriptDBAdaptor.count(query));
//    }

//    @GET
//    @Path("/stats")
//    @Override
//    @ApiOperation(httpMethod = "GET", value = "Not implemented yet", hidden = true)
//    public Response stats() {
//        return super.stats();
//    }

    @GET
    @Path("/{transcripts}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified transcripts(s)", response = Transcript.class,
            responseContainer = "QueryResponse")
    public Response getInfo(@PathParam("transcriptId") String id) {
        try {
//            parseQueryParams();
//            List<CellBaseDataResult> queryResults = transcriptManager.info(query, queryOptions, id);
            List<TranscriptQuery> queries = new ArrayList<>();
            String[] identifiers =  id.split(",");
            for (String identifier : identifiers) {
                TranscriptQuery query = new TranscriptQuery(uriParams);
                query.setTranscriptsXrefs(Arrays.asList(identifier));
                queries.add(query);
                logger.info("REST TranscriptQuery: " + query.toString());
            }
            List<CellBaseDataResult<Transcript>> queryResults = transcriptManager.info(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcripts}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all gene objects for given ENSEMBL transcript ids.",
            response = Gene.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "biotype",  value = ParamConstants.GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
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
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getGeneById(@PathParam("transcripts") @ApiParam(name = "transcripts",
                                    value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS, required = true) String id) {
        try {
            GeneQuery geneQuery = new GeneQuery(uriParams);
            geneQuery.setTranscriptsId(Arrays.asList(id.split(",")));
            CellBaseDataResult<Gene> queryResults = geneManager.search(geneQuery);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Retrieves all transcript objects", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.TRANSCRIPT_NAMES,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "xrefs", value = ParamConstants.TRANSCRIPT_XREFS,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotationFlags", value = ParamConstants.TRANSCRIPT_ANNOTATION_FLAGS,
                    dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getAll() {
        try {
            TranscriptQuery query = new TranscriptQuery(uriParams);
            logger.info("/search TranscriptQuery: " + query.toString());
            CellBaseDataResult<Transcript> queryResult = transcriptManager.search(query);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // FIXME: 28/04/16 must look for the transcript id within the consequence type object. Requires previous loading of
    // the annoation into th evariation collection
//    @GET
//    @Path("/{transcripts}/variation")
//    @ApiOperation(httpMethod = "GET", value = "To be fixed", hidden = true)
//    public Response getVariationsBytranscripts(@PathParam("transcripts") String id) {
//        try {
//            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
//            List<Query> queries = createQueries(id, TranscriptDBAdaptor.QueryParams.XREFS.key());
//            List<CellBaseDataResult> queryResultList = variationDBAdaptor.nativeGet(queries, queryOptions);
//            for (int i = 0; i < queries.size(); i++) {
//                queryResultList.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.XREFS.key()));
//            }
//            return createOkResponse(queryResultList);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{transcripts}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Retrieve transcript cDNA sequence", response = String.class,
        responseContainer = "QueryResponse")
    public Response getSequencesByIdList(@PathParam("transcripts") @ApiParam(name = "transcripts", value = ParamConstants.TRANSCRIPT_IDS,
            required = true) String id) {
        try {
            parseQueryParams();
            List<CellBaseDataResult<String>> queryResults = transcriptManager.getSequence(id);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcripts}/protein")
    @ApiOperation(httpMethod = "GET", value = "Get the protein info for the given transcript(s)", response = Entry.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = ParamConstants.PROTEIN_KEYWORD, required = false,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getProtein(@PathParam("transcripts") @ApiParam(name = "transcripts",
            value = ParamConstants.TRANSCRIPT_ENSEMBL_IDS, required = true) String transcripts) {
        try {
            ProteinQuery query = new ProteinQuery(uriParams);
            query.setXrefs(Arrays.asList(transcripts.split(",")));
            logger.info("REST proteinQuery: " + query.toString());
            CellBaseDataResult<Entry> queryResults = proteinManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcripts}/functionPrediction")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding substitution scores for the protein of a certain transcript",
            notes = ParamConstants.SUBSTITUTION_SCORE_NOTE, response = List.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "position", value = ParamConstants.POSITION_DESCRIPTION, required = false, dataType = "Integer",
                    paramType = "query"),
            @ApiImplicitParam(name = "aa", value = ParamConstants.AA_DESCRIPTION, required = false, dataType = "String",
                    paramType = "query")
    })
    public Response getProteinFunctionPredictionBytranscripts(@PathParam("transcript") @ApiParam(name = "transcript",
            value = ParamConstants.TRANSCRIPT_XREF, required = true) String id) {
        try {
            parseQueryParams();
            CellBaseDataResult queryResults = proteinManager.getSubstitutionScores(query, queryOptions, id);
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
        sb.append("- info: Get transcript information: name, position, biotype.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
        sb.append("- gene: Get the corresponding gene for this transcript.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- sequence: Get transcript sequence.\n\n");
        sb.append("- exon: Get transcript's exons.\n");
        sb.append(" Output columns: Ensembl ID, chromosome, start, end, strand.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Transcript");
        return createOkResponse(sb.toString());
    }

}
