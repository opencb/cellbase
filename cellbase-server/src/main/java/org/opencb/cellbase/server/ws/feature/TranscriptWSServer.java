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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/transcript")
@Api(value = "Transcript", description = "Transcript RESTful Web Services API")
@Produces(MediaType.APPLICATION_JSON)
public class TranscriptWSServer extends GenericRestWSServer {

    public TranscriptWSServer(@PathParam("version") String version, @PathParam("species") String species,
                              @DefaultValue("") @QueryParam("exclude") String exclude,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
    public Response getModel() {
        return createModelResponse(Transcript.class);
    }

    @GET
    @Path("/first")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Get the first object in the database")
    public Response first() {
        parseQueryParams();
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
        return createOkResponse(transcriptDBAdaptor.first(queryOptions));
    }

    @GET
    @Path("/count")
    @ApiOperation(httpMethod = "GET", value = "Get the number of objects in the database")
    public Response count(@DefaultValue("") @QueryParam("region") String region,
                          @DefaultValue("") @QueryParam("biotype") String biotype,
                          @DefaultValue("") @QueryParam("xrefs") String xrefs) throws Exception {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
        query.append(TranscriptDBAdaptor.QueryParams.REGION.key(), region);
        query.append(TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), biotype);
        query.append(TranscriptDBAdaptor.QueryParams.XREFS.key(), xrefs);
        return createOkResponse(transcriptDBAdaptor.count(query));
    }

    @GET
    @Path("/stats")
    @Override
    public Response stats() {
        return super.stats();
    }

    @GET
    @Path("/{transcriptId}/info")
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
    public Response getGeneById(@PathParam("transcriptId") String id) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            query.append(org.opencb.cellbase.core.api.GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key(), id);
            return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/variation")
    public Response getVariationsByTranscriptId(@PathParam("transcriptId") String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(id, TranscriptDBAdaptor.QueryParams.XREFS.key());
            return createOkResponse(variationDBAdaptor.nativeGet(queries, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/sequence")
    public Response getSequencesByIdList(@PathParam("transcriptId") String id) {
        try {
            parseQueryParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
            return createOkResponse(transcriptDBAdaptor.getCdna(Arrays.asList(id.split(","))));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/mutation")
    public Response getMutationByTranscriptId(@PathParam("transcriptId") String query) {
        try {
            parseQueryParams();
            MutationDBAdaptor mutationAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
//            List<List<MutationPhenotypeAnnotation>> geneList = mutationAdaptor
//                    .getAllMutationPhenotypeAnnotationByGeneNameList(Splitter.on(",").splitToList(query));
            List<QueryResult> queryResults = mutationAdaptor.getAllByGeneNameList(Splitter.on(",").splitToList(query), queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/protein")
    public Response getProtein(@PathParam("transcriptId") String transcriptId) {
        try {
            parseQueryParams();
            ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory2.getProteinDBAdaptor(this.species, this.assembly);
            query.put(ProteinDBAdaptor.QueryParams.XREFS.key(), transcriptId);
            return createOkResponse(proteinDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{transcriptId}/function_prediction")
    public Response getProteinFunctionPredictionByTranscriptId(@PathParam("transcriptId") String id,
                                                               @DefaultValue("") @QueryParam("aaPosition") String aaPosition,
                                                               @DefaultValue("") @QueryParam("aaChange") String aaChange) {
        try {
            parseQueryParams();
            ProteinDBAdaptor mutationAdaptor = dbAdaptorFactory2.getProteinDBAdaptor(this.species, this.assembly);
            query.put("transcript", id);
            query.put("position", aaPosition);
            query.put("aa", aaChange);
            QueryResult queryResults = mutationAdaptor.getSubstitutionScores(query, queryOptions);
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
