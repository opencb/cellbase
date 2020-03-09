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

package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.cellbase.core.api.queries.ProteinQuery;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.ProteinCoreDBAdaptor;
import org.opencb.cellbase.lib.impl.core.TranscriptCoreDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProteinManager extends AbstractManager {

    private ProteinCoreDBAdaptor proteinDBAdaptor;
    private TranscriptCoreDBAdaptor transcriptDBAdaptor;

    public ProteinManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
    }

//    public CellBaseDataResult<Protein> search(Query query, QueryOptions queryOptions) {
//        return proteinDBAdaptor.nativeGet(query, queryOptions);
//    }

    public CellBaseDataResult<Entry> search(ProteinQuery query) throws QueryException, IllegalAccessException {
        query.setDefaults();
        query.validate();
        return proteinDBAdaptor.query(query);
    }

//    public CellBaseDataResult<Protein> groupBy(Query query, QueryOptions queryOptions, String fields) {
//        return proteinDBAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
//    }

//    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String id) {
//        List<Query> queries = createQueries(query, id, ProteinDBAdaptor.QueryParams.XREFS.key());
//        List<CellBaseDataResult> queryResults = proteinDBAdaptor.query(queries);
//        for (int i = 0; i < queries.size(); i++) {
//            queryResults.get(i).setId((String) queries.get(i).get(ProteinDBAdaptor.QueryParams.XREFS.key()));
//        }
//        return queryResults;
//    }

    public List<CellBaseDataResult<Entry>> info(List<ProteinQuery> queries) {
        List<CellBaseDataResult<Entry>> queryResults = proteinDBAdaptor.query(queries);
        return queryResults;
    }

    public Iterator<Entry> iterator(ProteinQuery query) {
        return proteinDBAdaptor.iterator(query);
    }

    public CellBaseDataResult getSubstitutionScores(TranscriptQuery query, Integer position, String aa)
            throws JsonProcessingException {
        // Fetch Ensembl transcriptId to query substiturion scores
        logger.info("Searching transcripts for {}", query.getTranscriptsXrefs());
//        Query transcriptQuery = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), id);
//        QueryOptions transcriptQueryOptions = new QueryOptions("include", "transcripts.id");
        query.setIncludes(Collections.singletonList("transcripts.id"));
        CellBaseDataResult queryResult = transcriptDBAdaptor.query(query);
        logger.info("{} transcripts found", queryResult.getNumResults());
        logger.info("Transcript IDs: {}", jsonObjectWriter.writeValueAsString(queryResult.getResults()));

        // Get substitution scores for fetched transcript
        if (queryResult.getNumResults() > 0) {
            String transcriptId = (String) ((Map) queryResult.getResults().get(0)).get("id");
            query.setTranscriptsId(Collections.singletonList(transcriptId));
//            query.put("transcript", ((Map) queryResult.getResults().get(0)).get("id"));
            logger.info("Getting substitution scores for query {}", jsonObjectWriter.writeValueAsString(query));
            logger.info("queryOptions {}", jsonObjectWriter.writeValueAsString(query.toQueryOptions()));
            CellBaseDataResult scoresCellBaseDataResult = proteinDBAdaptor.getSubstitutionScores(query, position, aa);
            scoresCellBaseDataResult.setId(query.getTranscriptsXrefs().get(0));
            return scoresCellBaseDataResult;
        } else {
            return queryResult;
        }
    }

//    public CellBaseDataResult getSubstitutionScores(Query query, QueryOptions queryOptions, String id)
//            throws JsonProcessingException {
//        // Fetch Ensembl transcriptId to query substiturion scores
//        logger.info("Searching transcripts for protein {}", id);
////        Query transcriptQuery = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), id);
////        QueryOptions transcriptQueryOptions = new QueryOptions("include", "transcripts.id");
//        TranscriptQuery transcriptQuery = new TranscriptQuery();
//        transcriptQuery.setTranscriptsXrefs(new ArrayList<>(Arrays.asList(id)));
//        CellBaseDataResult queryResult = transcriptDBAdaptor.query(transcriptQuery);
//        logger.info("{} transcripts found", queryResult.getNumResults());
//        logger.info("Transcript IDs: {}", jsonObjectWriter.writeValueAsString(queryResult.getResults()));
//
//        // Get substitution scores for fetched transcript
//        if (queryResult.getNumResults() > 0) {
//            query.put("transcript", ((Map) queryResult.getResults().get(0)).get("id"));
//            logger.info("Getting substitution scores for query {}", jsonObjectWriter.writeValueAsString(query));
//            logger.info("queryOptions {}", jsonObjectWriter.writeValueAsString(queryOptions));
//            CellBaseDataResult scoresCellBaseDataResult = proteinDBAdaptor.getSubstitutionScores(query, queryOptions);
//            scoresCellBaseDataResult.setId(id);
//            return scoresCellBaseDataResult;
//        } else {
//            return queryResult;
//        }
//    }

//    public CellBaseDataResult<String> getSequence(Query query, QueryOptions queryOptions, String proteins) {
//        query.put(ProteinDBAdaptor.QueryParams.ACCESSION.key(), proteins);
//        queryOptions.put("include", "sequence.value");
//        CellBaseDataResult<Entry> queryResult = proteinDBAdaptor.get(query, queryOptions);
//        CellBaseDataResult<String> queryResult1 = new CellBaseDataResult<>(queryResult.getId(), queryResult.getTime(),
//                queryResult.getEvents(), queryResult.getNumResults(), Collections.emptyList(), 1);
//        queryResult1.setResults(Collections.singletonList(queryResult.first().getSequence().getValue()));
//        queryResult1.setId(proteins);
//        return queryResult1;
//    }

    public CellBaseDataResult<String> getSequence(ProteinQuery query) {
        query.getIncludes().add("sequence.value");
        CellBaseDataResult<Entry> queryResult = proteinDBAdaptor.query(query);
        CellBaseDataResult<String> queryResult1 = new CellBaseDataResult<>(queryResult.getId(), queryResult.getTime(),
                queryResult.getEvents(), queryResult.getNumResults(), Collections.emptyList(), 1);
        queryResult1.setResults(Collections.singletonList(queryResult.getResults().get(0).getSequence().getValue()));
        queryResult1.setId(StringUtils.join(query.getAccessions()));
        return queryResult1;
    }

    public CellBaseDataResult<ProteinVariantAnnotation> getVariantAnnotation(String ensemblTranscriptId, int position, String aaReference,
                                                                             String aaAlternate, QueryOptions options) {
        return proteinDBAdaptor.getVariantAnnotation(ensemblTranscriptId, position, aaReference, aaAlternate, options);
    }
}


