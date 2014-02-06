package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.XRefs;
import org.opencb.cellbase.core.common.core.DBName;
import org.opencb.cellbase.core.common.core.Xref;
import org.opencb.cellbase.core.lib.api.XRefsDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.*;

public class XRefsMongoDBAdaptor extends MongoDBAdaptor implements XRefsDBAdaptor {

    public XRefsMongoDBAdaptor(DB db) {
        super(db);
    }

    public XRefsMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("core");
    }

//	private List<Xref> executeQuery(DBObject query) {
//		List<Xref> result = null;
//		Set<Xref> xrefSet = new LinkedHashSet<Xref>();
//		
//		BasicDBObject returnFields = new BasicDBObject("transcripts", 1);
//		DBCursor cursor = mongoDBCollection.find(query, returnFields);
//
//		try {
//			if (cursor != null) {
////				Gson jsonObjectMapper = new Gson();
//				Gene gene;
//				while (cursor.hasNext()) {
////					gene = (Gene) jsonObjectMapper.fromJson(cursor.next().toString(), Gene.class);
//					gene = (Gene) jsonObjectMapper.writeValueAsBytes(cursor.next().toString(), Gene.class);
//					for (Transcript transcript : gene.getTranscripts()) {
//						xrefSet.addAll(transcript.getXrefs());
//					}
//				}
//			}
//			result = new ArrayList<Xref>(xrefSet);
//		} finally {
//			cursor.close();
//		}
//
//		return result;
//	}

    @Override
    public List<DBName> getAllDBNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DBName> getAllDBNamesById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DBName> getAllDBNamesByType(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllIdsByDBName(String dbname) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Xref> getById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Xref>> getAllByIdList(List<String> idList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Xref> getByStartsWithQuery(String likeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Xref>> getByStartsWithQueryList(List<String> likeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Xref> getByStartsWithSnpQuery(String likeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Xref>> getByStartsWithSnpQueryList(List<String> likeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Xref> getByContainsQuery(String likeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Xref>> getByContainsQueryList(List<String> likeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XRefs getById(String id, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<XRefs> getAllByIdList(List<String> ids, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Xref> getByDBName(String id, String dbname) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<Xref>> getAllByDBName(List<String> ids, String dbname) {
        // TODO Auto-generated method stub
        return null;
    }

//	@Override
//	public List<Xref> getByDBNameList(String id, List<String> dbnames) {
//
//		QueryBuilder builder = QueryBuilder.start("transcripts.xrefs.id").is(id.toUpperCase());
//		List<Xref> xrefQuery = new ArrayList<>();//;executeQuery(builder.get());
//		logger.info("->>>>>>>>>>>>>>>>"+xrefQuery.size());
//		if(dbnames == null) {
//			dbnames = Collections.emptyList();
//		}
//		Set<String> dbnameSet = new HashSet<String>(dbnames);
//
//		List<Xref> xrefReturnList = new ArrayList<Xref>(xrefQuery.size());
//		for(Xref xref: xrefQuery) {
//			if(dbnameSet.size() == 0 || dbnameSet.contains(xref.getDbName())) {
//				logger.info("->>>>>>>>>>>>>>>>"+xref.getId());
//				xrefReturnList.add(xref);
//			}
//		}
//		return xrefReturnList;
//	}
//
//	@Override
//	public List<List<Xref>> getAllByDBNameList(List<String> ids, List<String> dbnames) {
//		List<List<Xref>> xrefs = new ArrayList<List<Xref>>(ids.size());
//		for (String id : ids) {
//			xrefs.add(getByDBNameList(id, dbnames));
//		}
//		return xrefs;
//	}

    @Override
    public QueryResult getByDBNameList(String id, QueryOptions options) {
        return getAllByDBNameList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByDBNameList(List<String> ids, QueryOptions options) {

        // Mel de romer
//db.core.aggregate(
// {$match: {"transcripts.xrefs.id": "ENST00000544455"}},
// {$unwind: "$transcripts"},
// {$unwind: "$transcripts.xrefs"},
// {$match: {"transcripts.xrefs.dbNameShort":{$in:["go"]}}},
// {$group:{_id:{id:"$transcripts.xrefs.id", dbNameShort:"$transcripts.xrefs.dbNameShort"}}},
// {$project:{"_id":0,"xref":"$_id"}})


        List<DBObject[]> commandsList = new ArrayList<>(ids.size());
        for (String id : ids) {
            List<DBObject> commands = new ArrayList<>(ids.size());

            DBObject match = new BasicDBObject("$match", new BasicDBObject("transcripts.xrefs.id", id));
            DBObject unwind = new BasicDBObject("$unwind", "$transcripts");
            DBObject unwind2 = new BasicDBObject("$unwind", "$transcripts.xrefs");

            commands.add(match);
            commands.add(unwind);
            commands.add(unwind2);

            //Check dbname option exists
            List<Object> list = options.getList("dbname", null);
            if (list != null && list.size() > 0) {
                BasicDBList dbnameDBList = new BasicDBList();
                dbnameDBList.addAll(list);
                DBObject dbnameMatch = new BasicDBObject("$match", new BasicDBObject("transcripts.xrefs.dbNameShort", new BasicDBObject("$in", dbnameDBList)));
                commands.add(dbnameMatch);
            }

            DBObject group = new BasicDBObject("$group", new BasicDBObject("_id", new BasicDBObject("id","$transcripts.xrefs.id").append("dbNameShort","$transcripts.xrefs.dbNameShort")));
            commands.add(group);

            DBObject project = new BasicDBObject("$project", new BasicDBObject("_id", 0).append("xref","$_id"));
            commands.add(project);

            //ArrayList to array
            DBObject[] commandsArray = commands.toArray(new DBObject[0]);

            commandsList.add(commandsArray);
        }
        return executeAggregationList(ids, commandsList, options);
    }


    @Override
    public XRefs getByDBName(String id, String dbname, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<XRefs> getAllByDBName(List<String> ids, String dbname, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XRefs getByDBNameList(String id, List<String> dbnames, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<XRefs> getAllByDBNameList(List<String> ids, List<String> dbnames, String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
