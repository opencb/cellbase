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
import org.forester.protein.Protein;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.cellbase.core.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.core.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProteinManager extends AbstractManager {

    public ProteinManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public CellBaseDataResult<Protein> search(Query query, QueryOptions queryOptions, String species, String assembly) {
        logger.debug("Searching proteins");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        return dbAdaptor.nativeGet(query, queryOptions);
    }

    public CellBaseDataResult<Protein> groupBy(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("Querying for groupby");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public CellBaseDataResult<Protein> aggregationStats(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("Querying for aggregation stats");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        queryOptions.put(QueryOptions.COUNT, true);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String species, String assembly, String id) {
        logger.debug("Querying for protein info");
        ProteinDBAdaptor dbAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        List<Query> queries = createQueries(query, id, ProteinDBAdaptor.QueryParams.XREFS.key());
        List<CellBaseDataResult> queryResults = dbAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(ProteinDBAdaptor.QueryParams.XREFS.key()));
        }
        return queryResults;
    }

    public CellBaseDataResult getSubstitutionScores(Query query, QueryOptions queryOptions, String species, String assembly, String id)
            throws JsonProcessingException {
        // Fetch Ensembl transcriptId to query substiturion scores
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        logger.info("Searching transcripts for protein {}", id);
        Query transcriptQuery = new Query(TranscriptDBAdaptor.QueryParams.XREFS.key(), id);
        QueryOptions transcriptQueryOptions = new QueryOptions("include", "transcripts.id");
        CellBaseDataResult queryResult = transcriptDBAdaptor.nativeGet(transcriptQuery, transcriptQueryOptions);
        logger.info("{} transcripts found", queryResult.getNumResults());
        logger.info("Transcript IDs: {}", jsonObjectWriter.writeValueAsString(queryResult.getResults()));

        // Get substitution scores for fetched transcript
        if (queryResult.getNumResults() > 0) {
            query.put("transcript", ((Map) queryResult.getResults().get(0)).get("id"));
            logger.info("Getting substitution scores for query {}", jsonObjectWriter.writeValueAsString(query));
            logger.info("queryOptions {}", jsonObjectWriter.writeValueAsString(queryOptions));
            ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
            CellBaseDataResult scoresCellBaseDataResult = proteinDBAdaptor.getSubstitutionScores(query, queryOptions);
            scoresCellBaseDataResult.setId(id);
            return scoresCellBaseDataResult;
        } else {
            return queryResult;
        }
    }

    public CellBaseDataResult<String> getSequence(Query query, QueryOptions queryOptions, String species, String assembly, String proteins) {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        query.put(ProteinDBAdaptor.QueryParams.ACCESSION.key(), proteins);
        queryOptions.put("include", "sequence.value");
        // split by comma
        CellBaseDataResult<Entry> queryResult = proteinDBAdaptor.get(query, queryOptions);
        CellBaseDataResult<String> queryResult1 = new CellBaseDataResult<>(queryResult.getId(), queryResult.getTime(),
                queryResult.getEvents(), queryResult.getNumResults(), Collections.emptyList(), 1);
        queryResult1.setResults(Collections.singletonList(queryResult.first().getSequence().getValue()));
        queryResult1.setId(proteins);
        return queryResult1;
    }
}


