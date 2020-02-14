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

package org.opencb.cellbase.lib.impl.core;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by swaathi on 27/11/15.
 */
public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements TranscriptDBAdaptor<Transcript> {

    public TranscriptMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("TranscriptMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseDataResult<String> getCdna(String id) {
        Bson bson = Filters.eq("transcripts.xrefs.id", id);
        Bson elemMatch = Projections.elemMatch("transcripts", Filters.eq("xrefs.id", id));
        Bson include = Projections.include("transcripts.cDnaSequence");
        // elemMatch and include are combined to reduce the data sent from the server
        Bson projection = Projections.fields(elemMatch, include);
        CellBaseDataResult<Document> result = new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, new QueryOptions()));

        String sequence = null;
        if (result != null && !result.getResults().isEmpty()) {
            List<Document> transcripts = (List<Document>) result.getResults().get(0).get("transcripts");
            sequence = transcripts.get(0).getString("cDnaSequence");
        }
        return new CellBaseDataResult<>(id, result.getTime(), result.getEvents(), result.getNumResults(),
                Collections.singletonList(sequence), 1);
    }

    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    public CellBaseDataResult<Long> count(Bson match, Bson include, Bson unwind, Bson match2, Bson project, Bson group) {
         CellBaseDataResult<Document> cellBaseDataResult =
                new CellBaseDataResult<>(mongoDBCollection.aggregate(Arrays.asList(match, include, unwind, match2, project, group),
                        null));
        Number number = (Number) cellBaseDataResult.first().get("count");
        Long count = number.longValue();
        return new CellBaseDataResult<>(null, cellBaseDataResult.getTime(), cellBaseDataResult.getEvents(),
                cellBaseDataResult.getNumResults(), Collections.singletonList(count), cellBaseDataResult.getNumMatches());
    }

    @Override
    public CellBaseDataResult distinct(Query query, String field) {
        Bson bsonDocument = parseQuery(query);
        return new CellBaseDataResult(mongoDBCollection.distinct(field, bsonDocument));
    }

    @Override
    public CellBaseDataResult stats(Query query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Transcript> get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        List<Bson> aggregateList = unwindAndMatchTranscripts(query, options);
        return new CellBaseDataResult(mongoDBCollection.aggregate(aggregateList, options));
    }

    @Override
    public Iterator<Transcript> iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        List<Bson> aggregateList = unwindAndMatchTranscripts(query, options);
        return mongoDBCollection.nativeQuery().aggregate(aggregateList, options).iterator();
//        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

    @Override
    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

//    @Override
//    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
//        Bson bsonQuery = parseQuery(query);
//        return groupBy(bsonQuery, field, "name", options);
//    }
//
//    @Override
//    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
//        Bson bsonQuery = parseQuery(query);
//        return groupBy(bsonQuery, fields, "name", options);
//    }

    @Override
    public CellBaseDataResult next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

//    @Override
//    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
//        if (query.getString("region") != null) {
//            Region region = Region.parseRegion(query.getString("region"));
//            Bson bsonDocument = parseQuery(query);
//            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
//        }
//        return null;
//    }


}
