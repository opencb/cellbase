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

package org.opencb.cellbase.mongodb.db.core;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.cellbase.core.db.api.core.XRefsDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class XRefsMongoDBAdaptor extends MongoDBAdaptor implements XRefsDBAdaptor {


    public XRefsMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
//        mongoDBCollection = db.getCollection("gene");
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.info("XrefsMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult getAllDBNames() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        return null;
    }


    @Override
    public QueryResult getByStartsWithQuery(String id, QueryOptions options) {
        return getByStartsWithQueryList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getByStartsWithQueryList(List<String> ids, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();

        for (String id : ids) {
            QueryBuilder qb = QueryBuilder.start("transcripts.xrefs.id").regex(Pattern.compile("^" + id));
            queries.add(qb.get());
        }
        int limit = options.getInt("limit", 50);
        if (limit > 50) {
            options.put("limit", 50);
        }
        System.out.println(options.getInt("limit"));
        options.put("include", Arrays.asList("chromosome", "start", "end", "id", "name"));

        return executeQueryList2(ids, queries, options);
    }


    @Override
    public QueryResult getByContainsQuery(String likeQuery, QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<QueryResult> getByContainsQueryList(List<String> likeQuery, QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public QueryResult getByDBName(String id, QueryOptions options) {
        return getAllByDBNameList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByDBNameList(List<String> ids, QueryOptions options) {

        // Mel de romer
//db.core.aggregate(
//{$match: {"transcripts.xrefs.id": "ENST00000544455"}},
//{$unwind: "$transcripts"},
//{$unwind: "$transcripts.xrefs"},
//{$match: {"transcripts.xrefs.dbNameShort":{$in:["go"]}}},
//{$group:{_id:{id:"$transcripts.xrefs.id", dbNameShort:"$transcripts.xrefs.dbNameShort", description:"$transcripts.xrefs.description"}}},
//{$project:{"_id":0,"id":"$_id.id","dbNameShort":"$_id.dbNameShort","description":"$_id.description"}})

// Biotype if gene given: db.core.find({"transcripts.xrefs.id": "BRCA2"}, {"biotype":1})
// Biotype if protein/transcript given: db.core.aggregate({$match: {"transcripts.xrefs.id": "ENST00000470094"}},
// {$unwind: "$transcripts"}, {$match: {"transcripts.xrefs.id": "ENST00000470094"}}, {$group:{_id:{biotype:"$transcripts.biotype"}}},
// {$project:{"transcripts.biotype":1}})
        List<List<DBObject>> commandsList = new ArrayList<>(ids.size());
        for (String id : ids) {
            List<DBObject> commands = new ArrayList<>(ids.size());

            DBObject match = new BasicDBObject("$match", new BasicDBObject("transcripts.xrefs.id", id));
            DBObject unwind = new BasicDBObject("$unwind", "$transcripts");
            DBObject unwind2 = new BasicDBObject("$unwind", "$transcripts.xrefs");

            commands.add(match);
            commands.add(unwind);
            commands.add(match);
            commands.add(unwind2);

            //Check dbname option exists
            List<Object> list = options.getList("dbname", null);
            if (list != null && list.size() > 0) {
                BasicDBList dbnameDBList = new BasicDBList();
                dbnameDBList.addAll(list);
                DBObject dbnameMatch = new BasicDBObject("$match",
                        new BasicDBObject("transcripts.xrefs.dbName", new BasicDBObject("$in", dbnameDBList)));
                commands.add(dbnameMatch);
            }

            DBObject group = new BasicDBObject("$group", new BasicDBObject("_id",
                    new BasicDBObject("id", "$transcripts.xrefs.id")
                            .append("dbName", "$transcripts.xrefs.dbName")
                            .append("dbDisplayName", "$transcripts.xrefs.dbDisplayName")
                            .append("description", "$transcripts.xrefs.description")
            ));
            commands.add(group);

            DBObject project = new BasicDBObject("$project",
                    new BasicDBObject("_id", 0)
                            .append("id", "$_id.id")
                            .append("dbName", "$_id.dbName")
                            .append("dbDisplayName", "$_id.dbDisplayName")
                            .append("description", "$_id.description")
            );
            commands.add(project);

            //ArrayList to array
//            DBObject[] commandsArray = commands.toArray(new DBObject[0]);

            commandsList.add(commands);
        }
        return executeAggregationList2(ids, commandsList, options);
    }

}
