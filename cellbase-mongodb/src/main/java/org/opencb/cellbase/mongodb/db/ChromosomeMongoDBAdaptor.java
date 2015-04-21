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

package org.opencb.cellbase.mongodb.db;

import com.mongodb.*;
import org.opencb.cellbase.core.lib.api.core.ChromosomeDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ChromosomeMongoDBAdaptor extends MongoDBAdaptor implements ChromosomeDBAdaptor {

    public ChromosomeMongoDBAdaptor(DB db) {
        super(db);
    }

    public ChromosomeMongoDBAdaptor(DB db, String species, String assembly) {
        super(db, species, assembly);
        mongoDBCollection = db.getCollection("genome_info");
    }

    public ChromosomeMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection2 = mongoDataStore.getCollection("genome_info");

        logger.info("GeneMongoDBAdaptor: in 'constructor'");
    }

    public QueryResult speciesInfoTmp(String id, QueryOptions options){
        // reading application.properties file

//        String[] speciesArray = applicationProperties.getProperty("SPECIES").split(",");

//        List<DBObject> queries = new ArrayList<>(1);
//        for (String id : idList) {
        QueryBuilder builder = QueryBuilder.start("species").is(id);

//        queries.add(builder.get());
//        }

//        options = addExcludeReturnFields("transcripts", options);
        return executeQuery(id, builder.get(), options);

    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        QueryResult qr = executeQuery("result", new BasicDBObject(), options);
//        BasicDBList list = (BasicDBList)qr.getResult();
//        qr.setResult(list.get(0));
        return qr;
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<QueryResult> qrList = new ArrayList<>(idList.size());
//        List<DBObject[]> commandList = new ArrayList<>();
        if(options == null) {
            options = new QueryOptions("include", Arrays.asList("chromosomes.$"));
        }else {
//            options = new QueryOptions("include", Arrays.asList("chromosomes.$"));
            options.addToListOption("include", "chromosomes.$");
        }
        for (String id : idList) {
            DBObject dbObject = new BasicDBObject("chromosomes", new BasicDBObject("$elemMatch", new BasicDBObject("name", id)));
            QueryResult queryResult = executeQuery(id, dbObject, options);
            qrList.add(queryResult);
//            DBObject[] commands = new DBObject[3];
//            DBObject match = new BasicDBObject("$match", new BasicDBObject("chromosomes.name", id));
//            DBObject unwind = new BasicDBObject("$unwind", "$chromosomes");
//            commands[0] = match;
//            commands[1] = unwind;
//            commands[2] = match;
//            commandList.add(commands);
        }
//        List<QueryResult> qrList = executeAggregationList(idList, commandList, options);
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
