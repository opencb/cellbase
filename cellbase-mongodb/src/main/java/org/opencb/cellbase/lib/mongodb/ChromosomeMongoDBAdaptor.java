package org.opencb.cellbase.lib.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import org.opencb.cellbase.core.lib.api.ChromosomeDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResponse;

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
    public QueryResponse getAll(QueryOptions options) {
        return executeQuery("result", new BasicDBObject(), options);
    }

    @Override
    public QueryResponse getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id),options);
    }

    @Override
    public QueryResponse getAllByIdList(List<String> idList, QueryOptions options) {
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
        if(idList != null && idList.size() == 1){
            idList = Arrays.asList("result");
        }
        return executeAggregationList(idList, commandList, options);

    }

    @Override
    public QueryResponse getAllCytobandsById(String id, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResponse getAllCytobandsByIdList(List<String> id, QueryOptions options) {
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

//	public Chromosome getById(String name) {
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
