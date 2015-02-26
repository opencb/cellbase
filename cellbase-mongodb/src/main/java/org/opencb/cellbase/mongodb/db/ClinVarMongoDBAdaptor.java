package org.opencb.cellbase.mongodb.db;

import com.mongodb.*;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.lib.api.variation.ClinVarDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 26/09/14.
 */
@Deprecated
public class ClinVarMongoDBAdaptor extends MongoDBAdaptor implements ClinVarDBAdaptor {

    public ClinVarMongoDBAdaptor(DB db) {
        super(db);
    }

    public ClinVarMongoDBAdaptor(DB db, String species, String assembly) {
        super(db, species, assembly);
        mongoDBCollection = db.getCollection("clinvar");

        logger.info("ClinVarMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("referenceClinVarAssertion.clinVarAccession.acc").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(idList, queries, options);
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
        List<DBObject> queries = new ArrayList<>();

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            // If regions is 1 position then query can be optimize using chunks
            QueryBuilder builder = QueryBuilder.start("referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.chr").is(region.getChromosome()).and("referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.stop")
                    .greaterThanEquals(region.getStart()).and("referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.start").lessThanEquals(region.getEnd());
            System.out.println(builder.get().toString());
            queries.add(builder.get());
            ids.add(region.toString());
        }
        return executeQueryList(ids, queries, options);
    }

    public QueryResult getListAccessions(QueryOptions queryOptions) {
        QueryBuilder builder = QueryBuilder.start();
        queryOptions.put("include", Arrays.asList("referenceClinVarAssertion.clinVarAccession.acc"));
        QueryResult queryResult = executeQuery("", builder.get(), queryOptions);
        BasicDBList accInfoList = (BasicDBList) queryResult.getResult();
        List<String> accList = new ArrayList<>(accInfoList.size());
        BasicDBObject accInfo;
        QueryResult listAccessionsToReturn = new QueryResult();

        for(Object accInfoObject: accInfoList) {
            accInfo = (BasicDBObject) accInfoObject;
            accInfo = (BasicDBObject) accInfo.get("referenceClinVarAssertion");
            accInfo = (BasicDBObject) accInfo.get("clinVarAccession");
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
