package org.opencb.cellbase.lib.mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.variation.VariationPhenotypeAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 14/01/14.
 */
public class VariationPhenotypeAnnotationMongoDBAdaptor extends MongoDBAdaptor implements VariationPhenotypeAnnotationDBAdaptor {

    public VariationPhenotypeAnnotationMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("variation_phenotype_annotation");
    }

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("id").is(id);
            queries.add(builder.get());
        }

        return executeQueryList(idList, queries, options);
    }


    @Override
    public QueryResult getAllPhenotypes(QueryOptions options) {
        return executeDistinct("distinct", "phenotype");
    }


    @Override
    public QueryResult getAllByGene(String gene, QueryOptions options) {
        QueryBuilder builder = QueryBuilder.start("associatedGenes").is(gene);
        return executeQuery(gene, builder.get(), options);
    }

    @Override
    public List<QueryResult> getAllByGeneList(List<String> geneList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(geneList.size());
        for (String id : geneList) {
            QueryBuilder builder = QueryBuilder.start("associatedGenes").is(id);
            queries.add(builder.get());
        }
        return executeQueryList(geneList, queries, options);
    }


    @Override
    public QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options) {
        QueryBuilder builder = QueryBuilder.start("phenotype").is(phenotype);
        return executeQuery(phenotype, builder.get(), options);
    }

    @Override
    public List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(phenotypeList.size());
        for (String id : phenotypeList) {
            QueryBuilder builder = QueryBuilder.start("phenotype").is(id);
            queries.add(builder.get());
        }
        return executeQueryList(phenotypeList, queries, options);
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

}
