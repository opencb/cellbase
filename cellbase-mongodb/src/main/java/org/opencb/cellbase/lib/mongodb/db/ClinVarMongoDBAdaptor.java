package org.opencb.cellbase.lib.mongodb.db;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.variation.ClinVarDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 26/09/14.
 */
public class ClinVarMongoDBAdaptor extends MongoDBAdaptor implements ClinVarDBAdaptor {

    public ClinVarMongoDBAdaptor(DB db) {
        super(db);
    }

    public ClinVarMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
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
}
