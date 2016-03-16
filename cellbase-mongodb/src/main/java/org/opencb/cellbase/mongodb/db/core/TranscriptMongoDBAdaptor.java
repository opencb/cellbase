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

import com.mongodb.QueryBuilder;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Position;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.db.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements TranscriptDBAdaptor {


    public TranscriptMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.info("TranscriptMongoDBAdaptor: in 'constructor'");
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
        QueryBuilder builder = new QueryBuilder();

        Document[] commands = new Document[2];
        Document unwind = new Document("$unwind", "$transcripts");
        commands[0] = unwind;

        List<Object> biotypes = options.getList("biotypes", null);
        if (biotypes != null && biotypes.size() > 0) {

//            Document match = new Document("$match", new Document("chunkIds", id));
//            builder = builder.and("biotype").in(biotypeIds);

//            commands[0] = match;
            commands[1] = unwind;
        } else {
            commands[0] = unwind;
        }

        return executeAggregation2("result", Arrays.asList(commands), options);
    }

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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);

    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
//        db.core.aggregate({$match: {"transcripts.id": "ENST00000343281"}}, {$unwind: "$transcripts"},
// {$match: {"transcripts.id": "ENST00000343281"}})

        List<List<Bson>> commandsList = new ArrayList<>(idList.size());
        for (String id : idList) {
            List<Bson> commandList = new ArrayList<>(3);
            Document match = new Document("$match", new Document("transcripts.id", id));
            Document unwind = new Document("$unwind", "$transcripts");
//            Document project = new Document("$project", new Document("transcripts", 1));
            commandList.add(match);
            commandList.add(unwind);
            commandList.add(match);
//            commandList.add(project);
            commandsList.add(commandList);
        }
        return executeAggregationList2(idList, commandsList, options);
    }

    @Override
    public QueryResult getAllByXref(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options) {
//        db.core.aggregate({$match: {"transcripts.id": "ENST00000343281"}}, {$unwind: "$transcripts"},
// {$match: {"transcripts.id": "ENST00000343281"}})

        List<List<Bson>> commandsList = new ArrayList<>(idList.size());
        for (String id : idList) {
//            Document[] commands = new Document[3];
            List<Bson> commandList = new ArrayList<>(3);
            Document match = new Document("$match", new Document("transcripts.xrefs.id", id));
            Document unwind = new Document("$unwind", "$transcripts");
//            commands[0] = match;
//            commands[1] = unwind;
//            commands[2] = match;
            commandList.add(match);
            commandList.add(unwind);
            commandList.add(match);
            commandsList.add(commandList);
        }
        return executeAggregationList2(idList, commandsList, options);
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position,
                                        QueryOptions options) {
        return getAllByRegion(new Region(chromosome, position, position), options);
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        List<Region> regions = new ArrayList<>();
        for (Position position : positionList) {
            regions.add(new Region(position.getChromosome(), position.getPosition(), position.getPosition()));
        }
        return getAllByRegionList(regions, options);
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
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {


        List<Document[]> commandsList = new ArrayList<>(regions.size());
        for (Region region : regions) {
            Document geneMatch = new Document("$match", new Document("transcripts.chromosome", region.getChromosome()));
            Document regionMatch = new Document("$match", new Document("transcripts.start", region.getStart()));
            Document unwind = new Document("$unwind", "$transcripts");
            // biotype in, pero en aggregation

        }

//        List<Document> queries = new ArrayList<>();
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
//                builder = QueryBuilder.start("chunkIds").is(region.getSequenceName() + "_"
// + (region.getStart() / Integer.parseInt(applicationProperties.getProperty("CHUNK_SIZE", "4000")))).and("end")
//                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
//            } else {
//                builder = QueryBuilder.start("chromosome").is(region.getSequenceName()).and("end")
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
    public QueryResult getAllTargetsByTf(String tfId, QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<QueryResult> getAllTargetsByTfList(List<String> tfIdList,
                                                   QueryOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

//    @Override
//    public List<Transcript> getAllByProteinName(String proteinName) {
//        return null;
//    }
//
//    @Override
//    public List<List<Transcript>> getAllByProteinNameList(List<String> proteinNameList) {
//        return null;
//    }

    @Override
    public List<Transcript> getAllByMirnaMature(String mirnaID) {
        return null;
    }

    @Override
    public List<List<Transcript>> getAllByMirnaMatureList(List<String> mirnaIDList) {
        return null;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
