package org.opencb.cellbase.lib.mongodb.network;

import com.mongodb.*;
import org.opencb.cellbase.core.lib.api.network.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.lib.mongodb.MongoDBAdaptor;

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
public class ProteinProteinInteractionMongoDBAdaptor  extends MongoDBAdaptor implements ProteinProteinInteractionDBAdaptor {

    public ProteinProteinInteractionMongoDBAdaptor(DB db) {
        super(db);
    }

    public ProteinProteinInteractionMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("protein_protein_interaction");

        logger.info("ProteinProteinInteractionMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult getAll(QueryOptions options) {
        QueryBuilder builder = new QueryBuilder();

        // Fiilter all by interactor ID
        List<Object> interactors = options.getList("interactor", null);
        if (interactors != null && interactors.size() > 0) {
            BasicDBList interactorDBList = new BasicDBList();
            interactorDBList.addAll(interactors);

            BasicDBList or = new BasicDBList();
            DBObject orA = new BasicDBObject("interactorA.xrefs.id", new BasicDBObject("$in", interactorDBList));
            DBObject orB = new BasicDBObject("interactorB.xrefs.id", new BasicDBObject("$in", interactorDBList));
            or.add(orA);
            or.add(orB);
//            builder = builder.or(orA, orB);
            builder = builder.and(new BasicDBObject("$or", or));
        }

        // Filter all by Interaction Type (name and PSIMI)
        List<Object> type = options.getList("type", null);
        if (type != null && type.size() > 0) {
            BasicDBList typeDBList = new BasicDBList();
            typeDBList.addAll(type);

            BasicDBList or = new BasicDBList();
            DBObject orName = new BasicDBObject("type.name", new BasicDBObject("$in", typeDBList));
            DBObject orPsimi = new BasicDBObject("type.psimi", new BasicDBObject("$in", typeDBList));
            or.add(orName);
            or.add(orPsimi);
//            builder = builder.or(orName, orPsimi);
            builder = builder.and(new BasicDBObject("$or", or));
        }

        // Filter all by source database
        List<Object> database = options.getList("database", null);
        if (database != null && database.size() > 0) {
            BasicDBList databaseDBList = new BasicDBList();
            databaseDBList.addAll(database);
            builder = builder.and("source.name").in(databaseDBList);
        }

        // Filter all by detection method (name and PSIMI)
        List<Object> detectionMethod = options.getList("detectionMethod", null);
        if (detectionMethod != null && detectionMethod.size() > 0) {
            BasicDBList detectionMethodDBList = new BasicDBList();
            detectionMethodDBList.addAll(detectionMethod);

            BasicDBList or = new BasicDBList();
            DBObject orName = new BasicDBObject("detectionMethod.name", new BasicDBObject("$in", detectionMethodDBList));
            DBObject orPsimi = new BasicDBObject("detectionMethod.psimi", new BasicDBObject("$in", detectionMethodDBList));
            or.add(orName);
            or.add(orPsimi);
//            builder = builder.or(orName, orPsimi);
            builder = builder.and(new BasicDBObject("$or", or));
        }

        // Filter all by status
        List<Object> status = options.getList("status", null);
        if (status != null && status.size() > 0) {
            BasicDBList statusDBList = new BasicDBList();
            statusDBList.addAll(status);
            builder = builder.and("status").in(statusDBList);
        }

//        List<Object> type = options.getList("type", null);
//        if (type != null && type.size() > 0) {
//            BasicDBList typeDBList = new BasicDBList();
//            typeDBList.addAll(type);
//            builder = builder.and("type.name").in(typeDBList);
//        }

//        String type = options.getString("type", null);
//        if (type != null && !type.equals("")) {
//            builder = builder.and("type.name").is(type);
//        }



        System.out.println(builder.get().toString());
        //		options = addExcludeReturnFields("transcripts", options);
        return executeQuery("result", builder.get(), options);
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("xrefs.id").is(id);
            queries.add(builder.get());
        }
//        options = addExcludeReturnFields("transcripts", options);
        return executeQueryList(idList, queries, options);
    }

    @Override
    public QueryResult getAllByInteractorId(String id, QueryOptions options) {
        return getAllByInteractorIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByInteractorIdList(List<String> idList, QueryOptions options) {
        List<QueryResult> resultList = new ArrayList<>(idList.size());
        for(String id: idList) {
            options.put("interactor", Arrays.asList(id));
            resultList.add(getAll(options));
        }
        return resultList;
    }
}
