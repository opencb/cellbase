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

package org.opencb.cellbase.mongodb.db.variation;

import com.mongodb.BasicDBList;
import org.bson.Document;

import com.mongodb.QueryBuilder;
import org.opencb.biodata.models.core.Position;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 25/11/13
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class MutationMongoDBAdaptor extends MongoDBAdaptor implements MutationDBAdaptor {


    public MutationMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
//        mongoDBCollection = db.getCollection("mutation");
        mongoDBCollection = mongoDataStore.getCollection("mutation");

        logger.info("MutationMongoDBAdaptor: in 'constructor'");
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

        List<Object> biotypes = options.getList("disease", null);
        if (biotypes != null && biotypes.size() > 0) {
            BasicDBList biotypeIds = new BasicDBList();
            biotypeIds.addAll(biotypes);
            builder = builder.and("primaryHistology").in(biotypeIds);
        }

        return executeQuery("result", new Document(builder.get().toMap()), options);
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("id").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllDiseases(QueryOptions options) {
//        List<String> diseases = mongoDBCollection.distinct("primaryHistology");
//        Document distinct = new Document("distinct", "primaryHistology");
//        System.out.println(distinct.toString());
//        return executeDistinct("distinct", "primaryHistology");
        return null;
    }

    @Override
    public QueryResult getAllByDisease(String id, QueryOptions options) {
        return getAllByDiseaseList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByDiseaseList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("primaryHistology").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getByGeneName(String geneName, QueryOptions options) {
        return getAllByIdList(Arrays.asList(geneName), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByGeneNameList(List<String> geneNameList, QueryOptions options) {
        List<Document> queries = new ArrayList<>();
        for (String id : geneNameList) {
            QueryBuilder builder = QueryBuilder.start("gene").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

        return executeQueryList2(geneNameList, queries, options);
    }

    @Override
    public QueryResult getByProteinId(String proteinId, QueryOptions options) {
        return getAllByIdList(Arrays.asList(proteinId), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByProteinIdList(List<String> proteinIdList, QueryOptions options) {
        List<Document> queries = new ArrayList<>();
        for (String id : proteinIdList) {
            QueryBuilder builder = QueryBuilder.start("protein").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

        return executeQueryList2(proteinIdList, queries, options);
    }

    @Override
    public QueryResult getByProteinRegion(String proteinId, int start, int end, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return getAllByRegion(chromosome, position, position, options);
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
        List<Document> queries = new ArrayList<>();

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {
            QueryBuilder builder = QueryBuilder.start("chromosome")
                    .is(region.getChromosome())
                    .and("start").greaterThanEquals(region.getStart())
                    .lessThanEquals(region.getEnd());
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }

        return executeQueryList2(ids, queries, options);
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
        return null;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
