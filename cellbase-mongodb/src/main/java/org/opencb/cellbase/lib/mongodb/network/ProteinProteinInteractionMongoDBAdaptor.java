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

        List<Object> interactors = options.getList("interactor", null);
        if (interactors != null && interactors.size() > 0) {
            BasicDBList interactorDBList = new BasicDBList();
            interactorDBList.addAll(interactors);

            BasicDBList or = new BasicDBList();
            DBObject orA = new BasicDBObject("interactorA.xrefs.id", new BasicDBObject("$in", interactorDBList));
            DBObject orB = new BasicDBObject("interactorB.xrefs.id", new BasicDBObject("$in", interactorDBList));
            or.add(orA);
            or.add(orB);
            builder = builder.or(orA, orB);
//            builder = builder.and(new BasicDBObject("$or", or));
        }

        String type = options.getString("type", null);
        if (type != null && !type.equals("")) {
            builder = builder.and("type.name").is(type);
        }



        System.out.println(builder.get().toString());
        //		options = addExcludeReturnFields("transcripts", options);
        return executeQuery("result", builder.get(), options);
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAllByInteractorId(String id, QueryOptions options) {
        return getAllByInteractorIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByInteractorIdList(List<String> idList, QueryOptions options) {
        List<QueryResult> resultList = new ArrayList<>(idList.size());
        for(String id: idList) {
            options.put("interactor", id);
            resultList.add(getAll(options));
        }
        return resultList;
    }
}
