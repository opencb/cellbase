package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.core.Transcript;
import org.opencb.cellbase.core.lib.api.TranscriptDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.*;

public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements TranscriptDBAdaptor {

    public TranscriptMongoDBAdaptor(DB db) {
        super(db);
    }

    public TranscriptMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("core");
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        QueryBuilder builder = new QueryBuilder();

        DBObject[] commands = new DBObject[2];
        DBObject unwind = new BasicDBObject("$unwind", "$transcripts");
        commands[0] = unwind;

        List<Object> biotypes = options.getList("biotypes", null);
        if (biotypes != null && biotypes.size() > 0) {

//            DBObject match = new BasicDBObject("$match", new BasicDBObject("chunkIds", id));
//            builder = builder.and("biotype").in(biotypeIds);

//            commands[0] = match;
            commands[1] = unwind;
        }else {
            commands[0] = unwind;
        }

        //		options = addExcludeReturnFields("transcripts", options);
        return executeAggregation("result", commands, options);
    }

    @Override
    public QueryResult next(String id, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);

    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
//        db.core.aggregate({$match: {"transcripts.id": "ENST00000343281"}}, {$unwind: "$transcripts"}, {$match: {"transcripts.id": "ENST00000343281"}})

        List<DBObject[]> commandsList = new ArrayList<>(idList.size());
        for (String id : idList) {
            DBObject[] commands = new DBObject[3];
            DBObject match = new BasicDBObject("$match", new BasicDBObject("transcripts.id", id));
            DBObject unwind = new BasicDBObject("$unwind", "$transcripts");
            commands[0] = match;
            commands[1] = unwind;
            commands[2] = match;
            commandsList.add(commands);
        }
        return executeAggregationList(idList, commandsList, options);
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position,
                                        QueryOptions options) {
        return getAllByRegion(new Region(chromosome, position, position), options);
    }

    @Override
    public QueryResult getAllByPosition(Position position,
                                        QueryOptions options) {
        return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList,
                                                  QueryOptions options) {
        List<Region> regions = new ArrayList<>();
        for (Position position : positionList) {
            regions.add(new Region(position.getChromosome(), position.getPosition(), position.getPosition()));
        }
        return getAllByRegionList(regions, options);
    }

    @Override
    public QueryResult getAllByRegion(String chromosome, int start, int end,
                                      QueryOptions options) {
        return getAllByRegion(new Region(chromosome, start, end), options);
    }

    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions,
                                                QueryOptions options) {


        List<DBObject[]> commandsList = new ArrayList<>(regions.size());
        for(Region region: regions) {
            DBObject geneMatch = new BasicDBObject("$match", new BasicDBObject("transcripts.chromosome", region.getChromosome()));
            DBObject regionMatch = new BasicDBObject("$match", new BasicDBObject("transcripts.start", region.getStart()));
            DBObject unwind = new BasicDBObject("$unwind", "$transcripts");
            // biotype in, pero en aggregation

        }

//        List<DBObject> queries = new ArrayList<>();
//
//        List<Object> biotypes = options.getList("biotype", null);
//        BasicDBList biotypeIds = new BasicDBList();
//        if (biotypes != null && biotypes.size() > 0) {
//            biotypeIds.addAll(biotypes);
//        }
//
//        List<String> ids = new ArrayList<>(regions.size());
//        for (Region region : regions) {
//
//            QueryBuilder builder = null;
//            // If regions is 1 position then query can be optimize using chunks
//            if (region.getStart() == region.getEnd()) {
//                builder = QueryBuilder.start("chunkIds").is(region.getChromosome() + "_" + (region.getStart() / Integer.parseInt(applicationProperties.getProperty("CHUNK_SIZE", "4000")))).and("end")
//                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
//            } else {
//                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
//                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
//            }
//
//            if (biotypeIds.size() > 0) {
//                System.out.println("regions = [" + regions + "], options = [" + options + "]");
//                builder = builder.and("biotype").in(biotypeIds);
//            }
//            queries.add(builder.get());
//            ids.add(region.toString());
//        }
//
//        options = addExcludeReturnFields("transcripts", options);
//        return executeQueryList(ids, queries, options);
        return null;
    }


    @Override
    public QueryResult getAllByEnsemblExonId(String ensemblExonId,
                                             QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<QueryResult> getAllByEnsemblExonIdList(
            List<String> ensemblExonIdList, QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public QueryResult getAllByTFBSId(String tfbsId, QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<QueryResult> getAllByTFBSIdList(List<String> tfbsIdList,
                                                QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

//	private List<List<Transcript>> executeQuery(DBObject query) {
//		List<List<Transcript>> result = null;
//
//		BasicDBObject returnFields = new BasicDBObject("transcripts", 1);
//		DBCursor cursor = mongoDBCollection.find(query, returnFields);
//
//		try {
//			if (cursor != null) {
//				result = new ArrayList<List<Transcript>>();
////				Gson jsonObjectMapper = new Gson();
//				Gene gene = null;
//				while (cursor.hasNext()) {
////					gene = (Gene) jsonObjectMapper.fromJson(cursor.next().toString(), Gene.class);
//					result.add(gene.getTranscripts());
////					BasicDBList b = new BasicDBList();
////					b.addAll((BasicDBList)cursor.next().get("transcripts"));
////					trans = (Transcript) jsonObjectMapper.fromJson(cursor.next().get("transcripts").toString(), Transcript.class);
//				}
//			}
//		} finally {
//			cursor.close();
//		}
//
//		return result;
//	}


//	@Override
//	public List<List<Transcript>> getAllByName(String name, List<String> exclude) {
//		BasicDBObject query = new BasicDBObject("transcripts.xrefs.id", name.toUpperCase());
//        List<List<Transcript>> result = new ArrayList<List<Transcript>>();
//        List<List<Transcript>> transcriptsList = executeQuery(query);
//
//        boolean found = false;
//		for (List<Transcript> transcripts : transcriptsList) {
//
//            found = false;
//            for (Transcript transcript : transcripts) {
//                for (Xref xref : transcript.getXrefs()) {
//                    if (xref.getId().equals(name.toUpperCase())) {
//                        result.add(transcripts);
//                        found = true;
//                        break;
//                    }
//                }
//                if(found){
//                    break;
//                }
//            }
//
//
//		}
//		return result;
//	}


    @Override
    public List<Transcript> getAllByProteinName(String proteinName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Transcript>> getAllByProteinNameList(List<String> proteinNameList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Transcript> getAllByMirnaMature(String mirnaID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Transcript>> getAllByMirnaMatureList(List<String> mirnaIDList) {
        // TODO Auto-generated method stub
        return null;
    }



}
