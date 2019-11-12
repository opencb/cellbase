/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.impl;

import com.mongodb.BasicDBList;
import com.mongodb.QueryBuilder;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.api.ConservationDBAdaptor;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by swaathi on 26/11/15.
 */
@Deprecated
public class ConservationMongoDBAdaptor extends MongoDBAdaptor implements ConservationDBAdaptor {

    public ConservationMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("conservation");

        logger.debug("ConservationMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(Query query) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.count(bson));
    }

    @Override
    public CellBaseDataResult distinct(Query query, String field) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bson));
    }

    @Override
    public CellBaseDataResult stats(Query query) {
        return null;
    }

    @Override
    public CellBaseDataResult get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, options));
    }

    @Override
    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, ConservationDBAdaptor.QueryParams.REGION.key(), andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

//    @Override
    public List<CellBaseDataResult> getAllByRegionList(List regionList, QueryOptions options) {
        //TODO not finished yet
        List<Document> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regionList.size());
        List<String> integerChunkIds;

        List<Region> regions = regionList;
        for (Region region : regions) {
            integerChunkIds = new ArrayList<>();
            // positions below 1 are not allowed
            if (region.getStart() < 1) {
                region.setStart(1);
            }
            if (region.getEnd() < 1) {
                region.setEnd(1);
            }

            // Max region size is 10000bp
            if (region.getEnd() - region.getStart() > 10000) {
                region.setEnd(region.getStart() + 10000);
            }

            QueryBuilder builder;
            int regionChunkStart = getChunkId(region.getStart(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            int regionChunkEnd = getChunkId(region.getEnd(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            if (regionChunkStart == regionChunkEnd) {
                builder = QueryBuilder.start("_chunkIds")
                        .is(getChunkIdPrefix(region.getChromosome(), region.getStart(),
                                MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
            } else {
                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            }
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }

        List<CellBaseDataResult> cellBaseDataResults = executeQueryList2(ids, queries, options);
        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            CellBaseDataResult cellBaseDataResult = cellBaseDataResults.get(i);
            List list = cellBaseDataResult.getResults();

            Map<String, List<Float>> typeMap = new HashMap<>();

            for (int j = 0; j < list.size(); j++) {
                Document chunk = (Document) list.get(j);
                String source = chunk.getString("source");
                List<Float> valuesList;
                if (!typeMap.containsKey(source)) {
                    valuesList = new ArrayList<>(region.getEnd() - region.getStart() + 1);
                    for (int val = 0; val < region.getEnd() - region.getStart() + 1; val++) {
                        valuesList.add(null);
                    }
                    typeMap.put(source, valuesList);
                } else {
                    valuesList = typeMap.get(source);
                }

//                BasicDBList valuesChunk = (BasicDBList) chunk.get("values");
                ArrayList valuesChunk = chunk.get("values", ArrayList.class);

                int pos = 0;
                if (region.getStart() > chunk.getInteger("start")) {
                    pos = region.getStart() - chunk.getInteger("start");
                }


                for (; pos < valuesChunk.size() && (pos + chunk.getInteger("start") <= region.getEnd()); pos++) {
                    valuesList.set(pos + chunk.getInteger("start") - region.getStart(), new Float((Double) valuesChunk.get(pos)));
                }
            }

            BasicDBList resultList = new BasicDBList();
            GenomicScoreRegion<Float> conservedRegionChunk;
            for (Map.Entry<String, List<Float>> elem : typeMap.entrySet()) {
                conservedRegionChunk = new GenomicScoreRegion<>(region.getChromosome(), region.getStart(),
                        region.getEnd(), elem.getKey(), elem.getValue());
                resultList.add(conservedRegionChunk);
            }
            cellBaseDataResult.setResults(resultList);
        }

        return cellBaseDataResults;
    }

    @Override
    @Deprecated
    public List<CellBaseDataResult> getAllScoresByRegionList(List regionList, QueryOptions options) {
        //TODO not finished yet
        List<Document> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regionList.size());
        List<Integer> integerChunkIds;

        List<Region> regions = regionList;
        for (Region region : regions) {
            integerChunkIds = new ArrayList<>();
            // positions below 1 are not allowed
            if (region.getStart() < 1) {
                region.setStart(1);
            }
            if (region.getEnd() < 1) {
                region.setEnd(1);
            }

            QueryBuilder builder;
            int regionChunkStart = getChunkId(region.getStart(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            int regionChunkEnd = getChunkId(region.getEnd(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            if (regionChunkStart == regionChunkEnd) {
                builder = QueryBuilder.start("_chunkIds")
                        .is(getChunkIdPrefix(region.getChromosome(), region.getStart(),
                                MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
            } else {
                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            }
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }
        List<CellBaseDataResult> cellBaseDataResults = executeQueryList2(ids, queries, options);

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            CellBaseDataResult cellBaseDataResult = cellBaseDataResults.get(i);
            List<Document> list = (List<Document>) cellBaseDataResult.getResults();

            Map<String, List<Float>> typeMap = new HashMap();


            for (int j = 0; j < list.size(); j++) {
                Document chunk = list.get(j);

                if (!chunk.isEmpty()) {
                    ArrayList valuesChunk = chunk.get("values", ArrayList.class);

                    if (valuesChunk != null) {  // TODO: temporary patch to skip empty chunks - remove as soon as conservation is reloaded
                        String source = chunk.getString("source");
                        List<Float> valuesList;
                        if (!typeMap.containsKey(source)) {
                            valuesList = new ArrayList<>(region.getEnd() - region.getStart() + 1);
                            for (int val = 0; val < region.getEnd() - region.getStart() + 1; val++) {
                                valuesList.add(null);
                            }
                            typeMap.put(source, valuesList);
                        } else {
                            valuesList = typeMap.get(source);
                        }
                        valuesChunk = chunk.get("values", ArrayList.class);
                        int pos = 0;
                        if (region.getStart() > chunk.getInteger("start")) {
                            pos = region.getStart() - chunk.getInteger("start");
                        }

                        for (; pos < valuesChunk.size() && (pos + chunk.getInteger("start") <= region.getEnd()); pos++) {
                            valuesList.set(pos + chunk.getInteger("start") - region.getStart(), new Float((Double) valuesChunk.get(pos)));
                        }
                    } else {
                        logger.error("values field not present in conservation chunk document. This "
                                + "should not be happening - every conservation chunk must have a list of values."
                                + " Please check. Chunk id: " + chunk.get("_chunkIds"));
                        continue;
                    }
                }

                BasicDBList resultList = new BasicDBList();
                for (Map.Entry<String, List<Float>> elem : typeMap.entrySet()) {
                    for (Float value : elem.getValue()) {
                        if (value != null) {
                            resultList.add(new Score(new Double(value), elem.getKey(), null));
                        }
                    }
                }
                if (!resultList.isEmpty()) {
                    cellBaseDataResult.setResults(resultList);
                } else {
                    cellBaseDataResult.setResults(null);
                }
            }

        }
        return cellBaseDataResults;
    }

}
