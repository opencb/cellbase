package org.opencb.cellbase.lib.mongodb.regulatory;

import com.mongodb.*;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 7/17/13
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class TfbsMongoDBAdaptor extends RegulatoryRegionMongoDBAdaptor implements TfbsDBAdaptor {

    private static int CHUNKSIZE = 2000;

    public TfbsMongoDBAdaptor(DB db) {
        super(db);
    }

    public TfbsMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("regulatory_region");
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

    /**
     * PARTICULAR METHODS FOR TFBS CLASS
     */
    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("name").is(id).and("featureType").is("TF_binding_site_motif");
//            System.out.println("Query: " + builder.get());
            queries.add(builder.get());
        }
        options = addExcludeReturnFields("chunkIds", options);
        return executeQueryList(idList, queries, options);
    }

    @Override
    public QueryResult getAllByTargetGeneId(String targetGeneId, QueryOptions options) {
        return getAllByTargetGeneIdList(Arrays.asList(targetGeneId), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByTargetGeneIdList(List<String> targetGeneIdList, QueryOptions options) {
        DBCollection coreMongoDBCollection = db.getCollection("core");

        List<DBObject[]> commandList = new ArrayList<>();
        for (String targetGeneId : targetGeneIdList) {
            DBObject[] commands = new DBObject[3];
            DBObject match = new BasicDBObject("$match", new BasicDBObject("transcripts.xrefs.id", targetGeneId));
            DBObject unwind = new BasicDBObject("$unwind", "$transcripts");
            BasicDBObject projectObj = new BasicDBObject("_id", 0);
            projectObj.append("transcripts.id", 1);
            projectObj.append("transcripts.tfbs", 1);
            DBObject project = new BasicDBObject("$project", projectObj);
            commands[0] = match;
            commands[1] = unwind;
            commands[2] = project;
            commandList.add(commands);
        }

        List<QueryResult> queryResults = executeAggregationList(targetGeneIdList, commandList, options, coreMongoDBCollection);

        for (int i = 0; i < targetGeneIdList.size(); i++) {
            String targetGeneId = targetGeneIdList.get(0);
            QueryResult queryResult = queryResults.get(0);
            BasicDBList list = (BasicDBList) queryResult.get("result");

            for (int j = 0; j < list.size(); j++) {
                BasicDBObject gene = (BasicDBObject) list.get(j);
                BasicDBObject transcript = (BasicDBObject) gene.get("transcripts");
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

    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
