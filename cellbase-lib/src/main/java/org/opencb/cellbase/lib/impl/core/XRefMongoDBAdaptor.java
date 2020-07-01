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

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.CellBaseQueryOptions;
import org.opencb.cellbase.core.api.queries.XrefQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 07/12/15.
 */
public class XRefMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<XrefQuery, Xref> {

    public XRefMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("XRefMongoDBAdaptor: in 'constructor'");
    }


//    @Override
//    public CellBaseDataResult startsWith(String id, QueryOptions options) {
//        Bson regex = Filters.regex("transcripts.xrefs.id", Pattern.compile("^" + id));
//        Bson include = Projections.include("id", "name", "chromosome", "start", "end");
//        return new CellBaseDataResult(mongoDBCollection.find(regex, include, options));
//    }

//    @Override
//    public CellBaseDataResult contains(String id, QueryOptions options) {
//        Bson regex = Filters.regex("transcripts.xrefs.id", Pattern.compile("\\w*" + id + "\\w*"));
//        Bson include = Projections.include("id", "name", "chromosome", "start", "end");
//        return new CellBaseDataResult(mongoDBCollection.find(regex, include, options));
//    }

//    public CellBaseDataResult<Long> count(Query query) {
//        Bson bson = parseQuery(query);
//        return new CellBaseDataResult(mongoDBCollection.count(bson));
//    }
//
//    public CellBaseDataResult distinct(Query query, String field) {
//        Bson bson = parseQuery(query);
//        return new CellBaseDataResult(mongoDBCollection.distinct(field, bson));
//    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

//    public CellBaseDataResult<Xref> get(Query query, QueryOptions options) {
//        return null;
//    }
//
//    public List<CellBaseDataResult<Document>> nativeGet(List<Query> queries, QueryOptions options) {
////        return new CellBaseDataResult(mongoDBCollection.find(new BsonDocument(), null));
//        return null;
//    }

//    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
//        Bson bson = parseQuery(query);
//        Bson match = Aggregates.match(bson);
//
//        Bson project = Aggregates.project(Projections.include("transcripts.xrefs"));
//        Bson unwind = Aggregates.unwind("$transcripts");
//        Bson unwind2 = Aggregates.unwind("$transcripts.xrefs");
//
//        // This project the three fields of Xref to the top of the object
//        Document document = new Document("id", "$transcripts.xrefs.id");
//        document.put("dbName", "$transcripts.xrefs.dbName");
//        document.put("dbDisplayName", "$transcripts.xrefs.dbDisplayName");
//        Bson project1 = Aggregates.project(document);
//
//        if (query.containsKey(XRefDBAdaptor.QueryParams.DBNAME.key())) {
//            Bson bson2 = parseQuery(new Query(XRefDBAdaptor.QueryParams.DBNAME.key(), query.get(XRefDBAdaptor.QueryParams.DBNAME.key())));
//            Bson match2 = Aggregates.match(bson2);
//            return new CellBaseDataResult(mongoDBCollection.aggregate(
//                    Arrays.asList(match, project, unwind, unwind2, match2, project1), options));
//        }
//        return new CellBaseDataResult(mongoDBCollection.aggregate(Arrays.asList(match, project, unwind, unwind2, project1), options));
//    }

//    public Iterator<Xref> iterator(Query query, QueryOptions options) {
//        return null;
//    }
//
//    public Iterator nativeIterator(Query query, QueryOptions options) {
//        Bson bson = parseQuery(query);
//        return mongoDBCollection.nativeQuery().find(bson, options);
//    }

//    @Override
//    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
//        return null;
//    }

//    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
//        return groupBy(parseQuery(query), field, "name", options);
//    }
//
//    public CellBaseDataResult groupBy(Query query, List<String> fields, QueryOptions options) {
//        return groupBy(parseQuery(query), fields, "name", options);
//    }
//
//    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {
//
//    }

//    private Bson parseQuery(Query query) {
//        List<Bson> andBsonList = new ArrayList<>();
//        createOrQuery(query, XRefDBAdaptor.QueryParams.ID.key(), "transcripts.xrefs.id", andBsonList);
//        createOrQuery(query, XRefDBAdaptor.QueryParams.DBNAME.key(), "transcripts.xrefs.dbName", andBsonList);
//
//        if (andBsonList.size() > 0) {
//            return Filters.and(andBsonList);
//        } else {
//            return new Document();
//        }
//    }

    @Override
    public CellBaseIterator<Xref> iterator(XrefQuery query) {
        QueryOptions queryOptions = query.toQueryOptions();
        List<Bson> pipeline = unwind(query);
        GenericDocumentComplexConverter<Xref> converter = new GenericDocumentComplexConverter<>(Xref.class);
        MongoDBIterator<Xref> iterator = mongoDBCollection.iterator(pipeline, converter, queryOptions);
        return new CellBaseIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<Xref>> info(List<String> ids, CellBaseQueryOptions queryOptions) {
        List<CellBaseDataResult<Xref>> results = new ArrayList<>();
        for (String id : ids) {
            Bson projection = getProjection(queryOptions);
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            orBsonList.add(Filters.eq("name", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<Xref>(mongoDBCollection.find(bson, projection, Xref.class, new QueryOptions())));
        }
        return results;
    }

    public List<Bson> unwind(XrefQuery query) {
        Bson bson = parseQuery(query);
        Bson match = Aggregates.match(bson);

        Bson project = Aggregates.project(Projections.include("transcripts.xrefs"));
        Bson unwind = Aggregates.unwind("$transcripts");
        Bson unwind2 = Aggregates.unwind("$transcripts.xrefs");

        // This project the three fields of Xref to the top of the object
        Document document = new Document("id", "$transcripts.xrefs.id");
        document.put("dbName", "$transcripts.xrefs.dbName");
        document.put("dbDisplayName", "$transcripts.xrefs.dbDisplayName");
        Bson project1 = Aggregates.project(document);

        List<Bson> aggregateList = new ArrayList<>();

        aggregateList.add(match);
        aggregateList.add(project);
        aggregateList.add(unwind);
        aggregateList.add(unwind2);
        aggregateList.add(project1);

        return aggregateList;
    }

    @Override
    public CellBaseDataResult<Long> count(XrefQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(XrefQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(XrefQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(XrefQuery query) {
        return null;
    }

    public Bson parseQuery(XrefQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "id":
                        createAndOrQuery(value, "transcripts.xrefs.id", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "dbname":
                        createAndOrQuery(value, "transcripts.xrefs.dbName", QueryParam.Type.STRING, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
