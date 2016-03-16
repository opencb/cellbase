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

import org.bson.Document;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.api.core.ExonDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class ExonMongoDBAdaptor extends MongoDBAdaptor implements ExonDBAdaptor {


    public ExonMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.info("ExonMongoDBAdaptor: in 'constructor'");
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
        return null;
    }

    @Override
    public QueryResult next(String id, QueryOptions options) {
        QueryOptions options1 = new QueryOptions();
        options1.put("include", Arrays.asList("chromosome", "start"));
        QueryResult queryResult = getById(id, options1);
        if (queryResult != null && queryResult.getResult() != null) {
            Document gene = (Document) queryResult.getResult().get(0);
            String chromosome = gene.get("chromosome").toString();
            int start = Integer.parseInt(gene.get("start").toString());
            return next(chromosome, start, options);
        }
        return null;
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return next(chromosome, position + 1, options, mongoDBCollection);
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
    public QueryResult getAllByGeneId(String geneId) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByGeneIdList(List<String> geneIdList) {
        return null;
    }

    @Override
    public QueryResult getAllByTranscriptId(String transcriptId) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByTranscriptIdList(List<String> transcriptIdList) {
        return null;
    }


    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        return null;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

    //    private List<Gene> executeQuery(Document query, List<String> excludeFields) {
//        List<Gene> result = null;
//
//        DBCursor cursor = null;
//        if (excludeFields != null && excludeFields.size() > 0) {
//            Document returnFields = new Document("_id", 0);
//            for (String field : excludeFields) {
//                returnFields.put(field, 0);
//            }
//            cursor = mongoDBCollection.find(query, returnFields);
//        } else {
//            cursor = mongoDBCollection.find(query);
//        }
//
//        try {
//            if (cursor != null) {
//                result = new ArrayList<Gene>(cursor.size());
////                Gson jsonObjectMapper = new Gson();
//                Gene gene = null;
//                while (cursor.hasNext()) {
////                    gene = (Gene) jsonObjectMapper.fromJson(cursor.next().toString(), Gene.class);
//                    result.add(gene);
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//        return result;
//    }

}
