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

package org.opencb.cellbase.mongodb.db.regulatory;

import com.mongodb.BasicDBList;
import com.mongodb.QueryBuilder;
import org.bson.Document;
import org.opencb.biodata.models.core.Position;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert and mbleda :)
 * Date: 7/18/13
 * Time: 5:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegulatoryRegionMongoDBAdaptor extends MongoDBAdaptor implements RegulatoryRegionDBAdaptor {

    private static int regulatoryRegionChunkSize = MongoDBCollectionConfiguration.REGULATORY_REGION_CHUNK_SIZE;


    public RegulatoryRegionMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("regulatory_region");

        logger.debug("RegulationMongoDBAdaptor: in 'constructor'");
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
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("name").is(id);
//          System.out.println("Query: " + new Document(builder.get().toMap()));
            queries.add(new Document(builder.get().toMap()));
        }
//        options = addExcludeReturnFields("chunkIds", options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByPositionList(Arrays.asList(position), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        //  db.regulatory_region.find({"chunkIds": {$in:["1_200", "1_300"]}, "start": 601156})

        String featureType = options.getString("featureType", null);
        String featureClass = options.getString("featureClass", null);

        List<Document> queries = new ArrayList<>();
        for (Position position : positionList) {
            String chunkId = position.getChromosome() + "_" + getChunkId(position.getPosition(), regulatoryRegionChunkSize)
                    + "_" + regulatoryRegionChunkSize / 1000 + "k";
            BasicDBList chunksId = new BasicDBList();
            chunksId.add(chunkId);
            QueryBuilder builder = QueryBuilder.start("_chunkIds").in(chunksId).and("start").is(position.getPosition());
            if (featureType != null) {
                builder.and("featureType").is(featureType);
            }
            if (featureClass != null) {
                builder.and("featureClass").is(featureClass);
            }

//        System.out.println("Query: " + new Document(builder.get().toMap()));
            queries.add(new Document(builder.get().toMap()));
        }

        System.out.println("Query: " + queries);

//        options = addExcludeReturnFields("chunkIds", options);
        return executeQueryList2(positionList, queries, options);
    }


    @Override
    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options) {
        return getAllByRegion(new Region(chromosome, start, end), options);
    }

    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regionList, QueryOptions options) {
        //  db.regulatory_region.find({"chunkIds": {$in:["1_200", "1_300"]}, "start": 601156})
        QueryBuilder builder = new QueryBuilder();

        List<Object> featureType = options.getAsList("featureType");
        List<Object> featureClass = options.getAsList("featureClass");

//        options = addExcludeReturnFields("chunkIds", options);

        List<Document> queries = new ArrayList<>();
        for (Region region : regionList) {
            int firstChunkId = getChunkId(region.getStart(), regulatoryRegionChunkSize);
            int lastChunkId = getChunkId(region.getEnd(), regulatoryRegionChunkSize);
            BasicDBList chunksId = new BasicDBList();
            for (int j = firstChunkId; j <= lastChunkId; j++) {
                String chunkId = region.getChromosome() + "_" + j + "_" + regulatoryRegionChunkSize / 1000 + "k";
                chunksId.add(chunkId);
            }

//            logger.info(chunksId.toString());

            builder = builder.start("_chunkIds").in(chunksId)
                    .and("start").lessThanEquals(region.getEnd())
                    .and("end").greaterThanEquals(region.getStart());

            if (featureType != null && featureType.size() > 0) {
                BasicDBList featureTypeDBList = new BasicDBList();
                featureTypeDBList.addAll(featureType);
                builder = builder.and("featureType").in(featureTypeDBList);
            }

            if (featureClass != null && featureClass.size() > 0) {
                BasicDBList featureClassDBList = new BasicDBList();
                featureClassDBList.addAll(featureClass);
                builder = builder.and("featureClass").in(featureClassDBList);
            }

            queries.add(new Document(builder.get().toMap()));
        }
//        System.out.println(">>"+regionList);
//        System.out.println(">>"+new Document(builder.get().toMap()).toString());
        return executeQueryList2(regionList, queries, options);
    }

    public QueryResult next(String id, QueryOptions options) {
        QueryOptions options1 = new QueryOptions();
        options1.put("include", Arrays.asList("chromosome", "start"));
        QueryResult queryResult = getAllById(id, options1);
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

        String featureType = options.getString("featureType", null);
        String featureClass = options.getString("featureClass", null);

        BasicDBList chunksId = new BasicDBList();
        String chunkId = chromosome + "_" + getChunkId(position, regulatoryRegionChunkSize) + "_" + regulatoryRegionChunkSize / 1000 + "k";
        chunksId.add(chunkId);

        // TODO: Add query to find next item considering next chunk
        // db.regulatory_region.find({ "chromosome" : "19" , "start" : { "$gt" : 62005} , "featureType"
        // : "TF_binding_site_motif"}).sort({start:1}).limit(1)

        QueryBuilder builder;
        if (options.getString("strand") == null || (options.getString("strand").equals("1") || options.getString("strand").equals("+"))) {
            // db.core.find({chromosome: "1", start: {$gt: 1000000}}).sort({start: 1}).limit(1)
            builder = QueryBuilder.start("_chunkIds").in(chunksId).and("chromosome").is(chromosome).and("start").greaterThan(position);
            options.put("sort", new Document("start", 1));
            options.put("limit", 1);
        } else {
            builder = QueryBuilder.start("_chunkIds").in(chunksId).and("chromosome").is(chromosome).and("end").lessThan(position);
            options.put("sort", new Document("end", -1));
            options.put("limit", 1);
        }

        if (featureType != null) {
            builder.and("featureType").is(featureType);
        }
        if (featureClass != null) {
            builder.and("featureClass").is(featureClass);
        }
        System.out.println(new Document(builder.get().toMap()));
        return executeQuery("result", new Document(builder.get().toMap()), options);
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return null;
    }


    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


//    private int getChunkId(int position, int chunksize) {
//        if (chunksize <= 0) {
//            return position / regulatoryRegionChunkSize;
//        } else {
//            return position / chunksize;
//        }
//    }

    private static int getChunkStart(int id, int chunksize) {
        if (chunksize <= 0) {
            return (id == 0) ? 1 : id * regulatoryRegionChunkSize;
        } else {
            return (id == 0) ? 1 : id * chunksize;
        }
    }

    private static int getChunkEnd(int id, int chunksize) {
        if (chunksize <= 0) {
            return (id * regulatoryRegionChunkSize) + regulatoryRegionChunkSize - 1;
        } else {
            return (id * chunksize) + chunksize - 1;
        }
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
