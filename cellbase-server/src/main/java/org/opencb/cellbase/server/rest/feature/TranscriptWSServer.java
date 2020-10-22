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
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.ProteinQuery;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.SpeciesUtils;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.ProteinManager;
import org.opencb.cellbase.lib.managers.TranscriptManager;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;

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
            throws QueryException, IOException, CellbaseException {
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

    @GET
    @Path("/{transcripts}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified transcripts(s)", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = ParamConstants.GENE_SOURCE, value = ParamConstants.GENE_SOURCE_DESCRIPTION, required = false,
                    allowableValues="ensembl,refseq", defaultValue = "ensembl", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getInfo(@PathParam("transcripts") @ApiParam(name = "transcripts", value = ParamConstants.TRANSCRIPT_DESCRIPTION,
            required = true) String transcripts) {

        try {
            TranscriptQuery query = new TranscriptQuery(uriParams);
            String source = "ensembl";
            if (query.getSource() != null && !query.getSource().isEmpty()) {
                source = query.getSource().get(0);
            }
            List<CellBaseDataResult<Transcript>> queryResults = transcriptManager.info(Arrays.asList(transcripts.split(",")), query,
                    source);
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
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getGeneById(@PathParam("transcripts") @ApiParam(name = "transcripts",
                                    value = ParamConstants.TRANSCRIPT_IDS_DESCRIPTION, required = true) String id) {
        try {
            List<GeneQuery> queries = new ArrayList<>();
            String[] ids = id.split(",");
            for (String transcriptId : ids) {
                GeneQuery query = new GeneQuery(uriParams);
                query.setTranscriptsXrefs(Collections.singletonList(transcriptId));
                queries.add(query);
            }
            List<CellBaseDataResult<Gene>> queryResults = geneManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + ParamConstants.DOT_NOTATION_NOTE, value = "Retrieves all transcript objects", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.TRANSCRIPT_IDS_DESCRIPTION,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.TRANSCRIPT_NAMES_DESCRIPTION,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ParamConstants.GENE_SOURCE, value = ParamConstants.GENE_SOURCE_DESCRIPTION, required = false,
                    allowableValues="ensembl,refseq", defaultValue = "ensembl", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "biotype", value = ParamConstants.TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ParamConstants.TRANSCRIPT_XREFS_PARAM, value = ParamConstants.TRANSCRIPT_XREFS_DESCRIPTION,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ParamConstants.TRANSCRIPT_TFBS_IDS_PARAM, value = ParamConstants.TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ParamConstants.TRANSCRIPT_TFBS_PFMIDS_PARAM,
                    value = ParamConstants.TRANSCRIPT_TFBS_PFMIDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ParamConstants.TRANSCRIPT_TRANSCRIPTION_FACTORS_PARAM,
                    value = ParamConstants.TRANSCRIPT_TRANSCRIPTION_FACTORS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = ParamConstants.ONTOLOGY_PARAM, value = ParamConstants.ONTOLOGY_DESCRIPTION,
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "flags", value = ParamConstants.TRANSCRIPT_ANNOTATION_FLAGS_DESCRIPTION,
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
                    required = false, defaultValue = ParamConstants.DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = ParamConstants.DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getAll() {
        try {
            TranscriptQuery query = new TranscriptQuery(uriParams);
            logger.info("/search TranscriptQuery: {}", query.toString());
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
    public Response getSequencesByIdList(@PathParam("transcripts") @ApiParam(name = "transcripts",
            value = ParamConstants.TRANSCRIPT_XREFS_DESCRIPTION,
            required = true) String id) {
        try {
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
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getProtein(@PathParam("transcripts") @ApiParam(name = "transcripts",
            value = ParamConstants.TRANSCRIPT_IDS_DESCRIPTION, required = true) String transcripts) {
        try {
            List<ProteinQuery> queries = new ArrayList<>();
            String[] ids = transcripts.split(",");
            for (String transcriptId : ids) {
                ProteinQuery query = new ProteinQuery(uriParams);
                query.setXrefs(Collections.singletonList(transcriptId));
                queries.add(query);
            }
            List<CellBaseDataResult<Entry>> queryResults = proteinManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcripts}/functionPrediction")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding substitution scores for the protein of a certain transcript",
            notes = ParamConstants.SUBSTITUTION_SCORE_NOTE, response = List.class, responseContainer = "QueryResponse")
    public Response getProteinFunctionPredictionBytranscripts(@PathParam("transcripts") @ApiParam(name = "transcripts",
            value = ParamConstants.TRANSCRIPT_XREF, required = true) String id,
                                                              @QueryParam("position") @ApiParam(name = "position",
                                                                      value = ParamConstants.POSITION_DESCRIPTION,
                                                                      required = false) Integer position,
                                                              @QueryParam("aa") @ApiParam(name = "aa",
                                                                      value = ParamConstants.AA_DESCRIPTION,
                                                                      required = false) String aa) {
        try {
            TranscriptQuery query = new TranscriptQuery(uriParams);
            query.setTranscriptsXrefs(Arrays.asList(id));
            CellBaseDataResult queryResults = proteinManager.getSubstitutionScores(query, position, aa);
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
