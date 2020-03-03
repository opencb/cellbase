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

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.core.CellBaseMongoDBAdaptor;
import org.opencb.cellbase.core.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.FacetField;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

/**
 * Created by swaathi on 27/11/15.
 */
public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements CellBaseMongoDBAdaptor {

    public TranscriptMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("TranscriptMongoDBAdaptor: in 'constructor'");
    }



//    @Override
//    public CellBaseDataResult<String> getCdna(String id) {
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
//    }

    public CellBaseDataResult<Long> count(AbstractQuery query) {
        Bson document = parseQuery(query);
        Bson match = Aggregates.match(document);

        List<String> includeFields = query.getIncludes();

        Bson include;
        if (includeFields.size() > 0) {
            include = Aggregates.project(Projections.include(includeFields));
        } else {
            include = Aggregates.project(Projections.include("transcripts.id"));
        }

        Bson unwind = Aggregates.unwind("$transcripts");
        Bson match2 = Aggregates.match(document);
        Bson project = Aggregates.project(new Document("transcripts", "$transcripts.id"));
        Bson group = Aggregates.group("transcripts", Accumulators.sum("count", 1));

        CellBaseDataResult<Document> cellBaseDataResult =
                new CellBaseDataResult<>(mongoDBCollection.aggregate(Arrays.asList(match, include, unwind, match2, project, group), null));
        Number number = (Number) cellBaseDataResult.first().get("count");
        Long count = number.longValue();
        return new CellBaseDataResult<>(null, cellBaseDataResult.getTime(), cellBaseDataResult.getEvents(),
                cellBaseDataResult.getNumResults(), Collections.singletonList(count), cellBaseDataResult.getNumMatches());
    }

//    @Override
//    public CellBaseDataResult distinct(Query query, String field) {
//        Bson bsonDocument = parseQuery(query);
//        return new CellBaseDataResult(mongoDBCollection.distinct(field, bsonDocument));
//    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

    public CellBaseDataResult<Transcript> get(Query query, QueryOptions options) {
        return null;
    }

    public List<CellBaseDataResult> nativeGet(List<Query> query, QueryOptions options) {
        //return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), options));
        return null;
    }

    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        List<Bson> aggregateList = unwindAndMatchTranscripts(query, options);
        return new CellBaseDataResult(mongoDBCollection.aggregate(aggregateList, options));
    }

    public Iterator<Transcript> iterator(Query query, QueryOptions options) {
        return null;
    }

    public Iterator nativeIterator(Query query, QueryOptions options) {
        List<Bson> aggregateList = unwindAndMatchTranscripts(query, options);
        return mongoDBCollection.nativeQuery().aggregate(aggregateList, options).iterator();
//        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

//    public void forEach(Query query, Consumer action, QueryOptions options) {
//
//    }

    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, field, "name", options);
    }

    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, fields, "name", options);
    }

    public CellBaseDataResult next(Query query, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        if (query.getString("region") != null) {
            Region region = Region.parseRegion(query.getString("region"));
            Bson bsonDocument = parseQuery(query);
            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
        return null;
    }

    private Bson parseQuery(AbstractQuery query) {
        return new Document();
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, TranscriptDBAdaptor.QueryParams.REGION.key(), MongoDBCollectionConfiguration.GENE_CHUNK_SIZE, andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ID.key(), "transcripts.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.NAME.key(), "transcripts.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "transcripts.biotype", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.TFBS_NAME.key(), "transcripts.tfbs.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ANNOTATION_FLAGS.key(), "transcripts.annotationFlags", andBsonList);
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private Bson parseQueryUnwindTranscripts(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, TranscriptDBAdaptor.QueryParams.REGION.key(), andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ID.key(), "id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.NAME.key(), "name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "biotype", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.XREFS.key(), "xrefs.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.TFBS_NAME.key(), "tfbs.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ANNOTATION_FLAGS.key(), "annotationFlags", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private List<Bson> unwindAndMatchTranscripts(Query query, QueryOptions options) {
        List<Bson> aggregateList = new ArrayList<>();

        Bson bson = parseQuery(query);
        Bson match = Aggregates.match(bson);

        Bson include = null;
        if (options != null && options.containsKey("include")) {
            List<String> includeList = new ArrayList<>();
            List<String> optionsAsStringList = options.getAsStringList("include");
            for (String s : optionsAsStringList) {
                if (s.startsWith("transcripts")) {
                    includeList.add(s);
                }
            }

            if (includeList.size() > 0) {
                include = Projections.include(includeList);
            }
        }

        if (include == null) {
            include = Projections.include("transcripts");
        }
        Bson excludeAndInclude = Aggregates.project(Projections.fields(Projections.excludeId(), include));
        Bson unwind = Aggregates.unwind("$transcripts");

        // This project the three fields of Xref to the top of the object
        Document document = new Document("id", "$transcripts.id");
        document.put("name", "$transcripts.name");
        document.put("biotype", "$transcripts.biotype");
        document.put("status", "$transcripts.status");
        document.put("chromosome", "$transcripts.chromosome");
        document.put("start", "$transcripts.start");
        document.put("end", "$transcripts.end");
        document.put("strand", "$transcripts.strand");
        document.put("genomicCodingStart", "$transcripts.genomicCodingStart");
        document.put("genomicCodingEnd", "$transcripts.genomicCodingEnd");
        document.put("cdnaCodingStart", "$transcripts.cdnaCodingStart");
        document.put("cdnaCodingEnd", "$transcripts.cdnaCodingEnd");
        document.put("cdsLength", "$transcripts.cdsLength");
        document.put("proteinID", "$transcripts.proteinID");
        document.put("proteinSequence", "$transcripts.proteinSequence");
        document.put("cDnaSequence", "$transcripts.cDnaSequence");
        document.put("xrefs", "$transcripts.xrefs");
        document.put("exons", "$transcripts.exons");
        document.put("annotationFlags", "$transcripts.annotationFlags");
        Bson project = Aggregates.project(document);

        Bson match2 = Aggregates.match(bson);

        aggregateList.add(match);
        aggregateList.add(unwind);
        aggregateList.add(match2);
        aggregateList.add(excludeAndInclude);
        aggregateList.add(project);

        return aggregateList;
    }

    @Override
    public CellBaseDataResult query(Object query) {
        return null;
    }

    @Override
    public List<CellBaseDataResult> query(List queries) {
        return null;
    }

    @Override
    public Iterator iterator(Object query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(Object query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(String field, Object query) {
        return null;
    }

    @Override
    public CellBaseDataResult<FacetField> aggregationStats(List fields, Object query) {
        return null;
    }

//
//    private CellBaseDataResult<String> getCdna(String id) {
//        Bson bson = Filters.eq("transcripts.xrefs.id", id);
//        Bson elemMatch = Projections.elemMatch("transcripts", Filters.eq("xrefs.id", id));
//        Bson include = Projections.include("transcripts.cDnaSequence");
//        // elemMatch and include are combined to reduce the data sent from the server
//        Bson projection = Projections.fields(elemMatch, include);
////        CellBaseDataResult<Document> result = new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, new QueryOptions()));
//
//        CellBaseDataResult<Document> result = new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, new QueryOptions()));
//
//        String sequence = null;
//        if (result != null && !result.getResults().isEmpty()) {
//            List<Document> transcripts = (List<Document>) result.getResults().get(0).get("transcripts");
//            sequence = transcripts.get(0).getString("cDnaSequence");
//        }
//        return new CellBaseDataResult<>(id, result.getTime(), result.getEvents(), result.getNumResults(),
//                Collections.singletonList(sequence), 1);
//    }

//    private List<CellBaseDataResult<String>> getCdna(List<String> idList) {
//        List<CellBaseDataResult<String>> cellBaseDataResults = new ArrayList<>();
//        for (String id : idList) {
//            cellBaseDataResults.add(getCdna(id));
//        }
//        return cellBaseDataResults;
//    }
}
