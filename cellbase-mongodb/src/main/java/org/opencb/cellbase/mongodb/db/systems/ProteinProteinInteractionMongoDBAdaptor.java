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

package org.opencb.cellbase.mongodb.db.systems;

import com.mongodb.QueryBuilder;
import org.bson.Document;
import org.opencb.cellbase.core.db.api.systems.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProteinProteinInteractionMongoDBAdaptor extends MongoDBAdaptor implements ProteinProteinInteractionDBAdaptor {


    public ProteinProteinInteractionMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("protein_protein_interaction");

        logger.info("ProteinProteinInteractionMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult first() {
        return mongoDBCollection.find(new Document(), new QueryOptions("limit", 1));
    }

    @Override
    public QueryResult count() {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult stats() {
        return null;
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
//        QueryBuilder builder = new QueryBuilder();
//
//        // Fiilter all by interactor ID
//        List<Object> interactors = options.getList("interactor", null);
//        if (interactors != null && interactors.size() > 0) {
//            BasicDBList interactorDBList = new BasicDBList();
//            interactorDBList.addAll(interactors);
//
//            BasicDBList or = new BasicDBList();
//            Document orA = new Document("interactorA.xrefs.id", new Document("$in", interactorDBList));
//            Document orB = new Document("interactorB.xrefs.id", new Document("$in", interactorDBList));
//            or.add(orA);
//            or.add(orB);
//            builder = builder.and(new Document("$or", or));
//        }
//
//        // Filter all by Interaction Type (name and PSIMI)
//        List<Object> type = options.getList("type", null);
//        if (type != null && type.size() > 0) {
//            BasicDBList typeDBList = new BasicDBList();
//            typeDBList.addAll(type);
//
//            BasicDBList or = new BasicDBList();
//            Document orName = new Document("type.name", new Document("$in", typeDBList));
//            Document orPsimi = new Document("type.psimi", new Document("$in", typeDBList));
//            or.add(orName);
//            or.add(orPsimi);
//            builder = builder.and(new Document("$or", or));
//        }
//
//        // Filter all by source database
//        List<Object> database = options.getList("database", null);
//        if (database != null && database.size() > 0) {
//            BasicDBList databaseDBList = new BasicDBList();
//            databaseDBList.addAll(database);
//            builder = builder.and("source.name").in(databaseDBList);
//        }
//
//        // Filter all by detection method (name and PSIMI)
//        List<Object> detectionMethod = options.getList("detectionMethod", null);
//        if (detectionMethod != null && detectionMethod.size() > 0) {
//            BasicDBList detectionMethodDBList = new BasicDBList();
//            detectionMethodDBList.addAll(detectionMethod);
//
//            BasicDBList or = new BasicDBList();
//            Document orName = new Document("detectionMethod.name", new Document("$in", detectionMethodDBList));
//            Document orPsimi = new Document("detectionMethod.psimi", new Document("$in", detectionMethodDBList));
//            or.add(orName);
//            or.add(orPsimi);
//            builder = builder.and(new Document("$or", or));
//        }
//
//        // Filter all by status
//        List<Object> status = options.getList("status", null);
//        if (status != null && status.size() > 0) {
//            BasicDBList statusDBList = new BasicDBList();
//            statusDBList.addAll(status);
//            builder = builder.and("status").in(statusDBList);
//        }
//
//        System.out.println(new Document(builder.get().toMap()).toString());
//        return executeQuery("result", new Document(builder.get().toMap()), options);
        return null;
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("xrefs.id").is(id);
            queries.add(new Document(builder.get().toMap()));
        }
//        options = addExcludeReturnFields("transcripts", options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllByInteractorId(String id, QueryOptions options) {
        return getAllByInteractorIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByInteractorIdList(List<String> idList, QueryOptions options) {
        List<QueryResult> resultList = new ArrayList<>(idList.size());
        for (String id : idList) {
            options.put("interactor", Arrays.asList(id));
            resultList.add(getAll(options));
        }
        return resultList;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
