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
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.db.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 7/17/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class TfbsMongoDBAdaptor extends RegulatoryRegionMongoDBAdaptor implements TfbsDBAdaptor {

    private static int regulatoryRegionChunkSize = MongoDBCollectionConfiguration.REGULATORY_REGION_CHUNK_SIZE;


    public TfbsMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("regulatory_region");

        logger.info("RegulationMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByPositionList(Arrays.asList(position), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        options.put("featureType", "TF_binding_site_motif");
        return super.getAllByPositionList(positionList, options);
    }

    @Override
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
        options.put("featureType", "TF_binding_site_motif");
        return super.next(chromosome, position, options);
    }

//    @Override
//    public QueryResponse getAllByRegion(String chromosome, int start, int end, QueryOptions options) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regionList, QueryOptions options) {
        options.put("featureType", "TF_binding_site_motif");
        return super.getAllByRegionList(regionList, options);
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    /*
     * PARTICULAR METHODS FOR TFBS CLASS
     */
    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("name").is(id).and("featureType").is("TF_binding_site_motif");
//            System.out.println("Query: " + builder.get());
            queries.add(new Document(builder.get().toMap()));
        }
        options = addExcludeReturnFields("chunkIds", options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllByTargetGeneId(String targetGeneId, QueryOptions options) {
        return getAllByTargetGeneIdList(Arrays.asList(targetGeneId), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByTargetGeneIdList(List<String> targetGeneIdList, QueryOptions options) {
//        DBCollection coreMongoDBCollection = db.getCollection("gene");

        List<Document[]> commandList = new ArrayList<>();
        for (String targetGeneId : targetGeneIdList) {
            Document[] commands = new Document[3];
            Document match = new Document("$match", new Document("transcripts.xrefs.id", targetGeneId));
            Document unwind = new Document("$unwind", "$transcripts");
            Document projectObj = new Document("_id", 0);
            projectObj.append("transcripts.id", 1);
            projectObj.append("transcripts.tfbs", 1);
            Document project = new Document("$project", projectObj);
            commands[0] = match;
            commands[1] = unwind;
            commands[2] = project;
            commandList.add(commands);
        }

//        List<QueryResult> queryResults = executeAggregationList(targetGeneIdList, commandList, options, coreMongoDBCollection);
        List<QueryResult> queryResults = new ArrayList<>();
        for (int i = 0; i < targetGeneIdList.size(); i++) {
            String targetGeneId = targetGeneIdList.get(0);
//            QueryResult queryResult = queryResults.get(0);
            QueryResult queryResult = new QueryResult();
            BasicDBList list = (BasicDBList) queryResult.getResult();

            for (int j = 0; j < list.size(); j++) {
                Document gene = (Document) list.get(j);
                Document transcript = (Document) gene.get("transcripts");
                String transcriptId = transcript.getString("id");
                if (transcriptId.toUpperCase().equals(targetGeneId)) {
                    BasicDBList tfbs = (BasicDBList) transcript.get("tfbs");
                    queryResult.setResult(tfbs);
                    break;
                }
            }
        }

        return queryResults;
    }

    @Override
    public QueryResult getAllByJasparId(String jasparId, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllByJasparIdList(List<String> jasparIdList, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Object> getAllAnnotation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Object> getAllAnnotationByCellTypeList(List<String> cellTypes) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<IntervalFeatureFrequency> getAllTfIntervalFrequencies(Region region, int interval) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

//    @Override
//    public QueryResult getGenomeInfo(QueryOptions options) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

}
