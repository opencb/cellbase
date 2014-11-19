package org.opencb.cellbase.lib.mongodb.db;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.variation.ClinicalVarDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by antonior on 11/18/14.
 */
public class ClinicalVarMongoDBAdaptor extends MongoDBAdaptor implements ClinicalVarDBAdaptor {


    public ClinicalVarMongoDBAdaptor(DB db) {
        super(db);
    }

    public ClinicalVarMongoDBAdaptor(DB db, String species, String assembly) {
        super(db, species, assembly);
        mongoDBCollection = db.getCollection("clinical");
        logger.info("ClinicalVarMongoDBAdaptor: in 'constructor'");
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
            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end").greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            System.out.println(builder.get().toString());
            queries.add(builder.get());
            ids.add(region.toString());
        }
        return executeQueryList(ids, queries, options);
    }


    @Override
    public QueryResult getAllByGenomicVariant(GenomicVariant variant, QueryOptions options) {
        return getAllByGenomicVariant(Arrays.asList(variant), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByGenomicVariant(List<GenomicVariant> variantList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(variantList.size());
        for (GenomicVariant genomicVariant : variantList){
            QueryBuilder builder = QueryBuilder.start("chromosome").is(genomicVariant.getChromosome()).
                    and("start").is(genomicVariant.getPosition()).and("alternate").is(genomicVariant.getAlternative());
            if (genomicVariant.getReference() != null){
                builder = builder.and("reference").is(genomicVariant.getReference());
            }
                    queries.add(builder.get());
            ids.add(genomicVariant.toString());
        }
        return executeQueryList(ids, queries, options);
    }
}
