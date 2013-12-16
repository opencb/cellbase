package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 25/11/13
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
public class MutationMongoDBAdaptor extends MongoDBAdaptor implements MutationDBAdaptor {

    public MutationMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("mutation");
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

        return executeQuery("result", builder.get(), options);
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("id").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(idList, queries, options);
    }

    @Override
    public QueryResult getAllDiseases(QueryOptions options) {
//        List<String> diseases = mongoDBCollection.distinct("primaryHistology");
//        DBObject distinct = new BasicDBObject("distinct", "primaryHistology");
//        System.out.println(distinct.toString());
        return executeDistinct("distinct", "primaryHistology");
    }

    @Override
    public QueryResult getAllByDisease(String id, QueryOptions options) {
        return getAllByDiseaseList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByDiseaseList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("primaryHistology").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(idList, queries, options);
    }

    @Override
    public QueryResult getByGeneName(String geneName, QueryOptions options) {
        return getAllByIdList(Arrays.asList(geneName), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByGeneNameList(List<String> geneNameList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : geneNameList) {
            QueryBuilder builder = QueryBuilder.start("gene").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(geneNameList, queries, options);
    }

    @Override
    public QueryResult getByProteinId(String proteinId, QueryOptions options) {
        return getAllByIdList(Arrays.asList(proteinId), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByProteinIdList(List<String> proteinIdList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : proteinIdList) {
            QueryBuilder builder = QueryBuilder.start("protein").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(proteinIdList, queries, options);
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
        List<DBObject> queries = new ArrayList<>();

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("start").greaterThanEquals(region.getStart()).lessThanEquals(region.getEnd());
            queries.add(builder.get());
            ids.add(region.toString());
        }

        return executeQueryList(ids, queries, options);
    }


    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return null;
    }

}
