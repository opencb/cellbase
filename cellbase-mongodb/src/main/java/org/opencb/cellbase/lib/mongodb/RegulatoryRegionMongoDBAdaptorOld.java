package org.opencb.cellbase.lib.mongodb;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.cellbase.core.common.GenericFeatureChunk;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.RegulationDBAdaptor;

import java.util.ArrayList;
import java.util.List;

public class RegulatoryRegionMongoDBAdaptorOld extends MongoDBAdaptor implements RegulationDBAdaptor {


    public RegulatoryRegionMongoDBAdaptorOld(DB db) {
        super(db);
    }

    public RegulatoryRegionMongoDBAdaptorOld(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("regulation");
    }

    private int getChunk(int position) {
        return (position / applicationProperties.getIntProperty("CELLBASE." + version.toUpperCase()
                + ".REGULATION.CHUNK_SIZE", 2000));
    }

    private List<GenericFeatureChunk> executeQuery(DBObject query) {
        List<GenericFeatureChunk> result = null;
        logger.info(query);
        DBCursor cursor = mongoDBCollection.find(query);
        try {
            if (cursor != null) {
                result = new ArrayList<GenericFeatureChunk>(cursor.size());
//                Gson jsonObjectMapper = new Gson();
                GenericFeatureChunk chunk = null;
                while (cursor.hasNext()) {
//                    chunk = (GenericFeatureChunk) jsonObjectMapper.fromJson(cursor.next().toString(), GenericFeatureChunk.class);
                    result.add(chunk);
                }
            }
        } finally {
            cursor.close();
        }
        return result;
    }


    @Override
    public List<GenericFeature> getByRegion(String chromosome, int start, int end, List<String> types) {
        // positions below 1 are not allowed
        if (start < 1) {
            start = 1;
        }
        if (end < 1) {
            end = 1;
        }
        QueryBuilder builder = QueryBuilder.start("chromosome").is(chromosome.trim()).and("chunkId")
                .greaterThanEquals(getChunk(start)).lessThanEquals(getChunk(end));


        System.out.println(builder.get().toString());
        List<GenericFeatureChunk> chunkList = executeQuery(builder.get());
        List<GenericFeature> featureList = new ArrayList<>();
        for (GenericFeatureChunk chunk : chunkList) {
            if(types.isEmpty()){
                featureList.addAll(chunk.getFeatures());
            }else{
                for(GenericFeature genericFeature: chunk.getFeatures()){
                    if(types.contains(genericFeature.getFeatureType())){
                        featureList.add(genericFeature);
                    }
                }
            }
        }
        return featureList;
    }

    @Override
    public List<List<GenericFeature>> getByRegionList(List<Region> regions) {
        return this.getByRegionList(regions, new ArrayList<String>());
    }

    @Override
    public List<List<GenericFeature>> getByRegionList(List<Region> regions, List<String> types) {
        List<List<GenericFeature>> result = new ArrayList<List<GenericFeature>>(regions.size());
        for (Region region : regions) {
            result.add(getByRegion(region.getChromosome(), region.getStart(), region.getEnd(), types));
        }
        return result;
    }

}
