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
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/transcript")
@Api(value = "Transcript", description = "Transcript RESTful Web Services API")
@Produces(MediaType.APPLICATION_JSON)
public class TranscriptWSServer extends GenericRestWSServer {

    public TranscriptWSServer(@PathParam("version")
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
        return createModelResponse(Transcript.class);
    }

    @GET
    @Path("/first")
    @Override
    @Deprecated
    @ApiOperation(httpMethod = "GET", value = "Get the first transcript in the database", response = Transcript.class,
        responseContainer = "QueryResponse", hidden = true)
    public Response first() throws Exception {
        parseQueryParams();
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
        return createOkResponse(transcriptDBAdaptor.first(queryOptions));
    }

    @GET
    @Path("/count")
    @Deprecated
    @ApiOperation(httpMethod = "GET", value = "Get the number of transcripts in the database", response = Integer.class,
        responseContainer = "QueryResponse", hidden = true)
    public Response count(@DefaultValue("")
                          @QueryParam("region")
                          @ApiParam(name = "region",
                                  value = ParamConstants.REGION_DESCRIPTION,
                                  required = false) String region,
                          @DefaultValue("")
                          @QueryParam("biotype")
                          @ApiParam(name = "biotype",
                                  value = ParamConstants.GENE_BIOTYPES,
                                  required = false) String biotype,
                          @DefaultValue("")
                          @QueryParam("xrefs")
                          @ApiParam(name = "xrefs",
                                  value = ParamConstants.TRANSCRIPT_XREFS,
                                  required = false) String xrefs) throws Exception {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
        query.append(TranscriptDBAdaptor.QueryParams.REGION.key(), region);
        query.append(TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), biotype);
        query.append(TranscriptDBAdaptor.QueryParams.XREFS.key(), xrefs);
        return createOkResponse(transcriptDBAdaptor.count(query));
    }

    @GET
    @Path("/stats")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Not implemented yet", hidden = true)
    public Response stats() {
        return super.stats();
    }

    @GET
    @Path("/{transcriptId}/info")
    @ApiOperation(httpMethod = "GET", value = "Not implemented yet", hidden = true)
    public Response getByEnsemblId(@PathParam("transcriptId") String id) {
        try {
            parseQueryParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, TranscriptDBAdaptor.QueryParams.XREFS.key());
            return createOkResponse(transcriptDBAdaptor.nativeGet(queries, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all gene objects for given ENSEMBL transcript ids.",
            response = Gene.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
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
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getGeneById(@PathParam("transcriptId")
                                @ApiParam(name = "transcriptId", value = ParamConstants.TRANSCRIPT_IDS,
                                        required = true) String id,
                                @QueryParam("exclude")
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
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key());
            List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key()));
            }
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
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = ParamConstants.TRANSCRIPT_IDS,
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
    })
    public Response getAll(@QueryParam("exclude")
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
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
            CellBaseDataResult queryResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
            // Total number of results is always same as the number of results. As this is misleading, we set it as -1 until
            // properly fixed
            queryResult.setNumTotalResults(-1);
            queryResult.setNumMatches(-1);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // FIXME: 28/04/16 must look for the transcript id within the consequence type object. Requires previous loading of
    // the annoation into th evariation collection
    @GET
    @Path("/{transcriptId}/variation")
    @ApiOperation(httpMethod = "GET", value = "To be fixed", hidden = true)
    public Response getVariationsByTranscriptId(@PathParam("transcriptId") String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, TranscriptDBAdaptor.QueryParams.XREFS.key());
            List<CellBaseDataResult> queryResultList = variationDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResultList.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.XREFS.key()));
            }
            return createOkResponse(queryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Retrieve transcript cDNA sequence", response = String.class,
        responseContainer = "QueryResponse")
    public Response getSequencesByIdList(@PathParam("transcriptId")
                                         @ApiParam(name = "transcriptId", value = "String indicating one transcript ID,"
                                                 + " e.g:  ENST00000342992. Other transcript symbols such as HGNC symbols"
                                                 + " are allowed as well, e.g.: BRCA2-001", required = true) String id) {
        try {
            parseQueryParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
            List<String> transcriptIdList = Arrays.asList(id.split(","));
            List<CellBaseDataResult> queryResult = transcriptDBAdaptor.getCdna(transcriptIdList);
            for (int i = 0; i < transcriptIdList.size(); i++) {
                queryResult.get(i).setId(transcriptIdList.get(i));
            }
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/protein")
    @ApiOperation(httpMethod = "GET", value = "Get the protein info for the given transcript(s)", response = Entry.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = ParamConstants.PROTEIN_KEYWORD, required = false,
                    dataType = "java.util.List", paramType = "query")
    })
    public Response getProtein(@PathParam("transcriptId") @ApiParam(name = "transcriptId",
            value = ParamConstants.TRANSCRIPT_IDS, required = true) String transcriptId,
                               @QueryParam("exclude")
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
            ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(transcriptId, ProteinDBAdaptor.QueryParams.XREFS.key());
            List<CellBaseDataResult> queryResultList = proteinDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResultList.get(i).setId((String) queries.get(i).get(ProteinDBAdaptor.QueryParams.XREFS.key()));
            }
            return createOkResponse(queryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/functionPrediction")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding substitution scores for the protein of a"
            + " certain transcript",
            notes = "Schema of returned objects will vary depending on provided query parameters. If the amino acid "
                    + " position is provided, all scores will be returned for every possible amino acid"
                    + " change occurring at that position. If the alternate aminoacid is provided as well, Score objects as"
                    + " specified at "
                    + " https://github.com/opencb/biodata/blob/develop/biodata-models/src/main/resources/avro/variantAnnotation.avdl"
                    + " shall be returned. If none of these parameters are provided, the whole list of scores for every"
                    + " possible amino acid change in the protein shall be returned.",
            response = List.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "position",
                    value = "Integer indicating the aminoacid position to check",
                    required = false, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "aa",
                    value = "Alternate aminoacid to check. Please, use upper-case letters and three letter encoding"
                            + " of aminoacid names, e.g.: CYS",
                    required = false, dataType = "String", paramType = "query")
    })
    public Response getProteinFunctionPredictionByTranscriptId(@PathParam("transcriptId")
                                                               @ApiParam(name = "transcriptId",
                                                                value = "String indicating one ENSEMBL transcript id"
                                                                        + " e.g.: ENST00000536068. Exact text matches "
                                                                        + "will be returned",
                                                                required = true) String id) {
        try {
            parseQueryParams();
            ProteinDBAdaptor mutationAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
            query.put("transcript", id);
            CellBaseDataResult queryResults = mutationAdaptor.getSubstitutionScores(query, queryOptions);
            queryResults.setId(id);
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
