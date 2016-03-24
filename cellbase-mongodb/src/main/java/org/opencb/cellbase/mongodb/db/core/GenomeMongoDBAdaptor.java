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
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GenomeMongoDBAdaptor extends MongoDBAdaptor implements GenomeDBAdaptor {

    private MongoDBCollection genomeSequenceCollection;

    private int chunkSize = MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE;


    public GenomeMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("genome_info");
        genomeSequenceCollection = mongoDataStore.getCollection("genome_sequence");

        logger.debug("GeneMongoDBAdaptor: in 'constructor'");
    }

    public QueryResult first() {
        return null;
    }

    public QueryResult count() {
        return null;
    }

    public QueryResult stats() {
        return null;
    }

    @Deprecated
    public QueryResult speciesInfoTmp(String id, QueryOptions options) {
        // reading application.properties file

//        String[] speciesArray = applicationProperties.getProperty("SPECIES").split(",");

//        List<Document> queries = new ArrayList<>(1);
//        for (String id : idList) {
        QueryBuilder builder = QueryBuilder.start("species").is(id);

//        queries.add(builder.get());
//        }

//        options = addExcludeReturnFields("transcripts", options);
        return executeQuery(id, new Document(builder.get().toMap()), options);

    }

    @Override
    public QueryResult getGenomeInfo(QueryOptions options) {
        return executeQuery(species, new Document(), options);
    }


    @Override
    public QueryResult getChromosomeById(String id, QueryOptions options) {
        return getAllByChromosomeIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByChromosomeIdList(List<String> idList, QueryOptions options) {
        List<QueryResult> qrList = new ArrayList<>(idList.size());
//        List<Document[]> commandList = new ArrayList<>();
        if (options == null) {
            options = new QueryOptions("include", Arrays.asList("chromosomes.$"));
        } else {
//            options = new QueryOptions("include", Arrays.asList("chromosomes.$"));
            options.addToListOption("include", "chromosomes.$");
        }
        for (String id : idList) {
            Document dbObject = new Document("chromosomes", new Document("$elemMatch", new Document("name", id)));
            QueryResult queryResult = executeQuery(id, dbObject, options);
            qrList.add(queryResult);
//            Document[] commands = new Document[3];
//            Document match = new Document("$match", new Document("chromosomes.name", id));
//            Document unwind = new Document("$unwind", "$chromosomes");
//            commands[0] = match;
//            commands[1] = unwind;
//            commands[2] = match;
//            commandList.add(commands);
        }
//        List<QueryResult> qrList = executeAggregationList(idList, commandList, options);
        return qrList;

    }

    @Override
    public QueryResult getSequenceByRegion(String chromosome, int start, int end, QueryOptions options) {
        Region region = new Region(chromosome, start, end);
        return getAllSequencesByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllSequencesByRegionList(List<Region> regions, QueryOptions options) {
        /****/
        String chunkIdSuffix = this.chunkSize / 1000 + "k";
        /****/

        List<Document> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regions.size());
        List<String> chunkIds;
        List<Integer> integerChunkIds;
        for (Region region : regions) {
            chunkIds = new ArrayList<>();
            integerChunkIds = new ArrayList<>();
            // positions below 1 are not allowed
            if (region.getStart() < 1) {
                region.setStart(1);
            }
            if (region.getEnd() < 1) {
                region.setEnd(1);
            }

            /****/
            int regionChunkStart = getChunkId(region.getStart(), this.chunkSize);
            int regionChunkEnd = getChunkId(region.getEnd(), this.chunkSize);
            for (int chunkId = regionChunkStart; chunkId <= regionChunkEnd; chunkId++) {
                String chunkIdStr = region.getChromosome() + "_" + chunkId + "_" + chunkIdSuffix;
                chunkIds.add(chunkIdStr);
                integerChunkIds.add(chunkId);
            }
//            QueryBuilder builder = QueryBuilder.start("sequenceName").is(region.getChromosomeInfo()).and("_chunkIds").in(chunkIds);
            QueryBuilder builder = QueryBuilder.start("_chunkIds").in(chunkIds);
            /****/
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());

            logger.info(new Document(builder.get().toMap()).toString());
        }

        List<QueryResult> queryResults = executeQueryList2(ids, queries, options, genomeSequenceCollection);
        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            QueryResult queryResult = queryResults.get(i);

            List list = queryResult.getResult();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < list.size(); j++) {
                Document chunk = (Document) list.get(j);
                sb.append(chunk.get("sequence"));
            }

            int startStr = getOffset(region.getStart());
            int endStr = getOffset(region.getStart()) + (region.getEnd() - region.getStart()) + 1;

            String subStr = "";

            if (getChunkId(region.getStart(), this.chunkSize) > 0) {
                if (sb.toString().length() > 0 && sb.toString().length() >= endStr) {
                    subStr = sb.toString().substring(startStr, endStr);
                }
            } else {
                if (sb.toString().length() > 0 && sb.toString().length() + 1 >= endStr) {
                    subStr = sb.toString().substring(startStr - 1, endStr - 1);
                }
            }
            logger.info("((Document)list.get(0)).getString(\"sequenceType\") = {}",
                    ((Document) list.get(0)).getString("sequenceType"));
            logger.info("((Document)list.get(0)).getString(\"assembly\") = {}", ((Document) list.get(0)).getString("assembly"));
            GenomeSequenceFeature genomeSequenceFeature =
                    new GenomeSequenceFeature(region.getChromosome(), region.getStart(), region.getEnd(), 1,
                            ((Document) list.get(0)).getString("sequenceType"),
                            ((Document) list.get(0)).getString("assembly"), subStr);

            queryResult.setResult(Arrays.asList(genomeSequenceFeature));
        }

        return queryResults;
    }


    @Override
    public QueryResult getAllFeaturesByRegion(Region region, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public List<QueryResult> getAllFeaturesByRegionList(List<Region> positionList, QueryOptions queryOptions) {
        return null;
    }


    @Override
    public QueryResult getAllCytobandsById(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllCytobandsByIdList(List<String> idList, QueryOptions options) {
        return null;
    }


    private int getOffset(int position) {
        return (position % this.chunkSize);
    }

    public static String getComplementarySequence(String sequence) {
        sequence = sequence.replace("A", "1");
        sequence = sequence.replace("T", "2");
        sequence = sequence.replace("C", "3");
        sequence = sequence.replace("G", "4");
        sequence = sequence.replace("1", "T");
        sequence = sequence.replace("2", "A");
        sequence = sequence.replace("3", "G");
        sequence = sequence.replace("4", "C");
        return sequence;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
