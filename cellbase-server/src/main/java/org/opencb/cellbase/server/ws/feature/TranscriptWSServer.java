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

package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import io.swagger.annotations.*;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

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
                              @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                defaultValue = "latest") String version,
                              @PathParam("species")
                              @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                      + "of potentially available species ids, please refer to: "
                                      + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                              @DefaultValue("") @QueryParam("exclude") String exclude,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get JSON specification of transcript data model", response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Transcript.class);
    }

    @GET
    @Path("/first")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Get the first transcript in the database", response = Transcript.class,
        responseContainer = "QueryResponse")
    public Response first() {
        parseQueryParams();
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
        return createOkResponse(transcriptDBAdaptor.first(queryOptions));
    }

    @GET
    @Path("/count")
    @ApiOperation(httpMethod = "GET", value = "Get the number of transcripts in the database", response = Integer.class,
        responseContainer = "QueryResponse")
    public Response count(@DefaultValue("")
                          @QueryParam("region")
                          @ApiParam(name = "region",
                                  value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                                  required = false) String region,
                          @DefaultValue("")
                          @QueryParam("biotype")
                          @ApiParam(name = "biotype",
                                  value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                                          + " Exact text matches will be returned",
                                  required = false) String biotype,
                          @DefaultValue("")
                          @QueryParam("xrefs")
                          @ApiParam(name = "xrefs",
                                  value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                                          + " Exact text matches will be returned",
                                  required = false) String xrefs) throws Exception {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
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
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
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
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name",
                    value = "Comma separated list of TFBS names, e.g.: CTCF,Gabp."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id",
                    value = "Comma separated list of phenotype ids (OMIM, UMLS), e.g.: umls:C0030297,OMIM:613390,"
                            + "OMIM:613390. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name",
                    value = "Comma separated list of phenotypes, e.g.: Cryptorchidism,Absent thumb,Stage 5 chronic "
                            + "kidney disease. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue",
                    value = "Comma separated list of tissues for which expression values are available, "
                            + "e.g.: adipose tissue,heart atrium,tongue."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name",
                    value = "Comma separated list of drug names, "
                            + "e.g.: BMN673,OLAPARIB,VELIPARIB."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getGeneById(@PathParam("transcriptId")
                                @ApiParam(name = "transcriptId", value = "Comma separated list of ENSEMBL "
                                        + "transcript ids, e.g.: ENST00000342992,ENST00000380152,ENST00000544455. Exact "
                                        + "text matches will be returned",
                                        required = true) String id) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key());
            List<QueryResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key()));
            }
            return createOkResponse(queryResults);
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
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, TranscriptDBAdaptor.QueryParams.XREFS.key());
            List<QueryResult> queryResultList = variationDBAdaptor.nativeGet(queries, queryOptions);
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
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
            List<String> transcriptIdList = Arrays.asList(id.split(","));
            List<QueryResult> queryResult = transcriptDBAdaptor.getCdna(transcriptIdList);
            for (int i = 0; i < transcriptIdList.size(); i++) {
                queryResult.get(i).setId(transcriptIdList.get(i));
            }
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/mutation")
    @Deprecated
    @ApiOperation(httpMethod = "GET", value = "To be removed", hidden = true)
    public Response getMutationByTranscriptId(@PathParam("transcriptId") String query) {
        try {
            parseQueryParams();
            MutationDBAdaptor mutationAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
//            List<List<MutationPhenotypeAnnotation>> geneList = mutationAdaptor
//                    .getAllMutationPhenotypeAnnotationByGeneNameList(Splitter.on(",").splitToList(query));
            List<String> transcriptIdList = Splitter.on(",").splitToList(query);
            List<QueryResult> queryResults = mutationAdaptor.getAllByGeneNameList(transcriptIdList, queryOptions);
            for (int i = 0; i < transcriptIdList.size(); i++) {
                queryResults.get(i).setId(transcriptIdList.get(i));
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/protein")
    @ApiOperation(httpMethod = "GET", value = "Get the protein info for the given transcript(s)", response = Entry.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword",
                    value = "Comma separated list of keywords that may be associated with the protein(s), e.g.: "
                            + "Transcription,Zinc. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getProtein(@PathParam("transcriptId")
                                   @ApiParam(name = "transcriptId",
                                   value = "Comma-separated string with ENSEMBL transcript ids  e.g.: "
                                           + "ENST00000536068,ENST00000544455. Exact text matches will be returned",
                                   required = true) String transcriptId) {
        try {
            parseQueryParams();
            ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory2.getProteinDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(transcriptId, ProteinDBAdaptor.QueryParams.XREFS.key());
            List<QueryResult> queryResultList = proteinDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResultList.get(i).setId((String) queries.get(i).get(ProteinDBAdaptor.QueryParams.XREFS.key()));
            }
            return createOkResponse(queryResultList);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/function_prediction")
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
            ProteinDBAdaptor mutationAdaptor = dbAdaptorFactory2.getProteinDBAdaptor(this.species, this.assembly);
            query.put("transcript", id);
            QueryResult queryResults = mutationAdaptor.getSubstitutionScores(query, queryOptions);
            queryResults.setId(id);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{transcriptId}/protein_feature")
//    public Response getProteinFeaturesByTranscriptId(@PathParam("transcriptId") String query) {
//        try {
//            parseQueryParams();
//            ProteinDBAdaptor proteinAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//            List<List<FeatureType>> geneList = proteinAdaptor.getAllProteinFeaturesByProteinXrefList(Splitter.on(",").splitToList(query));
//            return generateResponse(query, "PROTEIN_FEATURE", geneList);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getMutationByGene", e.toString());
//        }
//    }


    @GET
    public Response defaultMethod() {
        return help();
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
