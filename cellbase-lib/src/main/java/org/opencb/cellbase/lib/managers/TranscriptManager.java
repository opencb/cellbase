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

import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.TranscriptMongoDBAdaptor;

import java.util.*;

public class TranscriptManager extends AbstractManager implements AggregationApi  {

    private TranscriptMongoDBAdaptor transcriptDBAdaptor;

    public TranscriptManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return transcriptDBAdaptor;
    }

    public CellBaseDataResult<String> getCdna(String id) {
        TranscriptQuery query = new TranscriptQuery();
        query.setTranscriptsXrefs(Arrays.asList(id));
        query.setIncludes(Arrays.asList("transcripts.id,transcripts.cDnaSequence"));

        CellBaseDataResult<Transcript> transcriptCellBaseDataResult = transcriptDBAdaptor.query(query);

//        if (gene.getResults().get(0).getTranscripts().size() != 1) {
//            // check id exists
//        }

        String cdnaSequence = null;
        for (Transcript transcript: transcriptCellBaseDataResult.getResults()) {
            if (transcript.getId().equals(id)) {
                cdnaSequence = transcript.getcDnaSequence();
                break;
            }
        }

        return new CellBaseDataResult<>(id, transcriptCellBaseDataResult.getTime(), transcriptCellBaseDataResult.getEvents(),
                transcriptCellBaseDataResult.getNumResults(), Collections.singletonList(cdnaSequence), 1);

//        Bson bson = Filters.eq("transcripts.xrefs.id", id);
//        Bson elemMatch = Projections.elemMatch("transcripts", Filters.eq("xrefs.id", id));
//        Bson include = Projections.include("transcripts.cDnaSequence");
//        // elemMatch and include are combined to reduce the data sent from the server
//        Bson projection = Projections.fields(elemMatch, include);
//        CellBaseDataResult<Document> result = new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, new QueryOptions()));
//
//        String sequence = null;
//        if (result != null && !result.getResults().isEmpty()) {
//            List<Document> transcripts = (List<Document>) result.getResults().get(0).get("transcripts");
//            sequence = transcripts.get(0).getString("cDnaSequence");
//        }
//        return new CellBaseDataResult<>(id, result.getTime(), result.getEvents(), result.getNumResults(),
//                Collections.singletonList(sequence), 1);
    }

    private List<CellBaseDataResult<String>> getCdna(List<String> idList) {
        List<CellBaseDataResult<String>> cellBaseDataResults = new ArrayList<>();
        for (String id : idList) {
            cellBaseDataResults.add(getCdna(id));
        }
        return cellBaseDataResults;
    }

//    public CellBaseDataResult<Transcript> search(TranscriptQuery query) throws QueryException, IllegalAccessException {
//        query.setDefaults();
//        query.validate();
//        return transcriptDBAdaptor.query(query);
//    }


//    public CellBaseDataResult<Transcript> search(Query query, QueryOptions queryOptions) {
//        CellBaseDataResult<Transcript> queryResult = transcriptDBAdaptor.nativeGet(query, queryOptions);
//        // Total number of results is always same as the number of results. As this is misleading, we set it as -1 until
//        // properly fixed
//        queryResult.setNumTotalResults(-1);
//        queryResult.setNumMatches(-1);
//        return queryResult;
//    }

//    public CellBaseDataResult<Transcript> groupBy(Query query, QueryOptions queryOptions, String fields) {
//        return transcriptDBAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
//    }

//    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String id) {
//        List<Query> queries = createQueries(query, id, TranscriptDBAdaptor.QueryParams.XREFS.key());
//        List<CellBaseDataResult> queryResults = transcriptDBAdaptor.nativeGet(queries, queryOptions);
//        for (int i = 0; i < queries.size(); i++) {
//            queryResults.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.XREFS.key()));
//        }
//        return queryResults;
//    }

//    public List<CellBaseDataResult<Transcript>> info(List<TranscriptQuery> queries) {
//        List<CellBaseDataResult<Transcript>> queryResults = transcriptDBAdaptor.query(queries);
//        return queryResults;
//    }

    public List<CellBaseDataResult<String>> getSequence(String id) {
        List<String> transcriptsList = Arrays.asList(id.split(","));
        List<CellBaseDataResult<String>> queryResult = getCdna(transcriptsList);
        for (int i = 0; i < transcriptsList.size(); i++) {
            queryResult.get(i).setId(transcriptsList.get(i));
        }
        return queryResult;
    }

//    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String region) {
//        List<Query> queries = createQueries(query, region, TranscriptDBAdaptor.QueryParams.REGION.key());
//        List<CellBaseDataResult> queryResults = transcriptDBAdaptor.nativeGet(queries, queryOptions);
//        for (int i = 0; i < queries.size(); i++) {
//            queryResults.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.REGION.key()));
//        }
//        return queryResults;
//    }

}

