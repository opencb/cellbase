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
import com.mongodb.QueryBuilder;
import org.bson.Document;
import org.opencb.biodata.models.core.Position;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.api.variation.ClinVarDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 26/09/14.
 */
@Deprecated
public class ClinVarMongoDBAdaptor extends MongoDBAdaptor implements ClinVarDBAdaptor {


    public ClinVarMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
//        mongoDBCollection = db.getCollection("clinvar");
        mongoDBCollection = mongoDataStore.getCollection("clinvar");

        logger.info("ClinVarMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("referenceClinVarAssertion.clinVarAccession.acc").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
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
        List<Document> queries = new ArrayList<>();

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            // If regions is 1 position then query can be optimize using chunks
            QueryBuilder builder = QueryBuilder
                    .start("referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.chr")
                    .is(region.getChromosome())
                    .and("referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.stop")
                    .greaterThanEquals(region.getStart())
                    .and("referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.start")
                    .lessThanEquals(region.getEnd());
            System.out.println(new Document(builder.get().toMap()).toString());
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }
        return executeQueryList2(ids, queries, options);
    }

    public QueryResult getListAccessions(QueryOptions queryOptions) {
        QueryBuilder builder = QueryBuilder.start();
        queryOptions.put("include", Arrays.asList("referenceClinVarAssertion.clinVarAccession.acc"));
        QueryResult queryResult = executeQuery("", new Document(builder.get().toMap()), queryOptions);
        BasicDBList accInfoList = (BasicDBList) queryResult.getResult();
        List<String> accList = new ArrayList<>(accInfoList.size());
        Document accInfo;
        QueryResult listAccessionsToReturn = new QueryResult();

        for (Object accInfoObject : accInfoList) {
            accInfo = (Document) accInfoObject;
            accInfo = (Document) accInfo.get("referenceClinVarAssertion");
            accInfo = (Document) accInfo.get("clinVarAccession");
            accList.add((String) accInfo.get("acc"));
        }

        // setting listAccessionsToReturn fields
        listAccessionsToReturn.setId(queryResult.getId());
        listAccessionsToReturn.setDbTime(queryResult.getDbTime());
        listAccessionsToReturn.setNumResults(queryResult.getNumResults());
        listAccessionsToReturn.setResult(accList);

        return listAccessionsToReturn;
    }

}
