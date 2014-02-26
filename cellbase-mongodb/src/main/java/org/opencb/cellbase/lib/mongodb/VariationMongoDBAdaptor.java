package org.opencb.cellbase.lib.mongodb;

import com.mongodb.*;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.*;

public class VariationMongoDBAdaptor extends MongoDBAdaptor implements VariationDBAdaptor {

    private DBCollection mongoVariationPhenotypeDBCollection;

    public VariationMongoDBAdaptor(DB db) {
        super(db);
    }

    public VariationMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("variation");
        mongoVariationPhenotypeDBCollection = db.getCollection("variation_phenotype");
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
    public QueryResult getAllConsequenceTypes(QueryOptions options) {
        String[] consquenceTypes = applicationProperties.getProperty("CELLBASE."+version.toUpperCase()+".CONSEQUENCE_TYPES").split(",");
        QueryResult queryResult = new QueryResult();
        queryResult.setId("result");
        DBObject result = new BasicDBObject("consequenceTypes", consquenceTypes);
        queryResult.setResult(Arrays.asList(result));
        queryResult.setDBTime(0);
        return queryResult;
    }

    @Override
    public QueryResult getAllPhenotypes(QueryOptions options) {
//        return executeDistinct("distinct", "phenotype", mongoVariationPhenotypeDBCollection);
        QueryBuilder builder = new QueryBuilder();
        if(options.containsKey("phenotype")) {
            String pheno = options.getString("phenotype");
            if(pheno != null && !pheno.equals("")) {
                builder = builder.start("phenotype").is(pheno);
            }
        }
        return executeQuery("result", builder.get(), options, mongoVariationPhenotypeDBCollection);
    }

    @Override
    public List<QueryResult> getAllPhenotypeByRegion(List<Region> regions, QueryOptions options) {
        QueryBuilder builder = null;
        List<DBObject> queries = new ArrayList<>();

//        List<Region> regions = Region.parseRegions(options.getString("region"));
        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {
            if(region != null && !region.equals("")) {
                // If regions is 1 position then query can be optimize using chunks
                if (region.getStart() == region.getEnd()) {
                    String chunkId = getChunkPrefix(region.getChromosome(), region.getStart(), Integer.parseInt(applicationProperties.getProperty("VARIATION_CHUNK_SIZE", "1000")));
                    System.out.println(chunkId);
                    builder = QueryBuilder.start("chunkIds").is(chunkId).and("end")
                            .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
                } else {
                    builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                            .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
                }

                queries.add(builder.get());
                ids.add(region.toString());
            }
        }
        return executeQueryList(ids, queries, options, db.getCollection("variation_phenotype_annotation"));
    }


    @Override
    public QueryResult getAllByPhenotype(String phenotype, QueryOptions options) {
        QueryBuilder builder = QueryBuilder.start("phenotype").is(phenotype);

        List<QueryResult> queryResults = new ArrayList<>();
        if(options.containsKey("variants")) {
            List<Object> variantList = options.getList("variants");
            List<GenomicVariant> variants = new ArrayList<>(variantList.size());
            for (int i = 0; i < variantList.size(); i++) {
                GenomicVariant genomicVariant = (GenomicVariant) variantList.get(i);
                variants.add(genomicVariant);
            }
        }

        return null;
    }

    @Override
    public List<QueryResult> getAllByPhenotypeList(List<String> phenotypeList, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options) {
        QueryBuilder builder = QueryBuilder.start("phenotype").is(phenotype);
        return executeQuery(phenotype, builder.get(), options, mongoVariationPhenotypeDBCollection);
    }

    @Override
    public List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(phenotypeList.size());
        for (String id : phenotypeList) {
            QueryBuilder builder = QueryBuilder.start("phenotype").is(id);
            queries.add(builder.get());
        }
        return executeQueryList(phenotypeList, queries, options, mongoVariationPhenotypeDBCollection);
    }


    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return getAllByRegion(chromosome, position, position, options);
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
    }

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
    //  public QueryResponse getAllByRegion(String chromosome, int start, int end, List<String> consequence_types, List<String> exclude) {
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regions.size());

        String phenotype = options.getString("phenotype");
        if(phenotype != null && !phenotype.equals("")) {
            for (Region region : regions) {
                QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("start").greaterThanEquals(region.getStart()).lessThanEquals(region.getEnd());
                builder = builder.and("phenotype").is(phenotype);
                queries.add(builder.get());
                ids.add(region.toString());
            }
            return executeQueryList(ids, queries, options, db.getCollection("variation_phenotype_annotation"));
        }else {
            String consequenceTypes = options.getString("consequence_type", null);
            BasicDBList consequenceTypeDBList = new BasicDBList();
            if (consequenceTypes != null && !consequenceTypes.equals("")) {
                for (String ct : consequenceTypes.split(",")) {
                    consequenceTypeDBList.add(ct);
                }
            }

            for (Region region : regions) {
                //			QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end").greaterThan(region.getStart()).and("start").lessThan(region.getEnd());
                QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("start").greaterThanEquals(region.getStart()).lessThanEquals(region.getEnd());
                if (consequenceTypeDBList.size() > 0) {
                    builder = builder.and("transcriptVariations.consequenceTypes").in(consequenceTypeDBList);
                }
                queries.add(builder.get());
                ids.add(region.toString());
            }

            return executeQueryList(ids, queries, options);
        }
    }
    @Override
    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(region, queryOptions);
    }

    @Override
    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(regions, queryOptions);
    }
}
