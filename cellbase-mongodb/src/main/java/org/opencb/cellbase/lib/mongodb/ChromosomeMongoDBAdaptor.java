package org.opencb.cellbase.lib.mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.opencb.cellbase.core.lib.api.ChromosomeDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ChromosomeMongoDBAdaptor extends MongoDBAdaptor implements ChromosomeDBAdaptor {

    public ChromosomeMongoDBAdaptor(DB db) {
        super(db);
    }

    public ChromosomeMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("info_stats");
    }


    @Override
    public QueryResult getAll(QueryOptions options) {
        QueryResult qr = executeQuery("result", new BasicDBObject(), options);
        BasicDBList list = (BasicDBList)qr.getResult();
        qr.setResult(list.get(0));
        return qr;
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject[]> commandList = new ArrayList<>();
        for (String id : idList) {
            DBObject[] commands = new DBObject[3];
            DBObject match = new BasicDBObject("$match", new BasicDBObject("chromosomes.name", id));
            DBObject unwind = new BasicDBObject("$unwind", "$chromosomes");
            commands[0] = match;
            commands[1] = unwind;
            commands[2] = match;
            commandList.add(commands);
        }

        List<QueryResult> qrList = executeAggregationList(idList, commandList, options);
        for (QueryResult qr : qrList) {
            BasicDBList list = (BasicDBList)qr.getResult();
            qr.setResult(list.get(0));
        }
        return qrList;

    }

    @Override
    public QueryResult getAllCytobandsById(String id, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllCytobandsByIdList(List<String> id, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

//
//    private List<Chromosome> executeQuery() {
////        Gson jsonObjectMapper = new Gson();
//        DBObject item = mongoDBCollection.findOne();
////		System.out.println(item.toString());
////        InfoStats infoStats = (InfoStats) jsonObjectMapper.fromJson(item.toString(), InfoStats.class);
//
////        return infoStats.getChromosomes();
//        return null;
//    }

//	public Chromosome getAllById(String name) {
//		for (Chromosome chromosome : executeQuery()) {
//			if (chromosome.getName().equals(name)) {
//				return chromosome;
//			}
//		}
//		return null;
//	}
//
//	public List<Chromosome> getAllByIdList(List<String> nameList) {
//		List<Chromosome> foundList = new ArrayList<Chromosome>(nameList.size());
//
//		for (Chromosome chromosome : executeQuery()) {
//			if (nameList.contains(chromosome.getName())) {
//				foundList.add(chromosome);
//			}
//		}
//		return foundList;
//	}
//
//	public List<Cytoband> getCytobandByName(String name) {
//		for (Chromosome chromosome : executeQuery()) {
//			if (chromosome.getName().equals(name)) {
//				return chromosome.getCytobands();
//			}
//		}
//		return null;
//	}
//
//	public List<List<Cytoband>> getCytobandByNameList(List<String> nameList) {
//		List<List<Cytoband>> foundLists = new ArrayList<List<Cytoband>>(nameList.size());
//
//		for (Chromosome chromosome : executeQuery()) {
//			if (nameList.contains(chromosome.getName())) {
//				foundLists.add(chromosome.getCytobands());
//			}
//		}
//		return foundLists;
//	}
//
//	public List<Chromosome> getAll() {
//		return executeQuery();
//	}
//
//	public List<String> getChromosomeNames() {
//		List<String> names = new ArrayList<String>();
//		for (Chromosome chromosome : executeQuery()) {
//			names.add(chromosome.getName());
//		}
//		return names;
//	}

}
