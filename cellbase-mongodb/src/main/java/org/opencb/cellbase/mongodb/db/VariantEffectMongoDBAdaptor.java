package org.opencb.cellbase.mongodb.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.*;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.variation.GenomicVariantEffect;
import org.opencb.cellbase.core.common.variation.GenomicVariantEffectPredictor;
import org.opencb.cellbase.core.lib.api.variation.VariantEffectDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class VariantEffectMongoDBAdaptor extends MongoDBAdaptor implements VariantEffectDBAdaptor {


    public VariantEffectMongoDBAdaptor(DB db) {
        super(db);
    }

    public VariantEffectMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("gene");
    }


    @Override
    public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options) {
        return getAllEffectsByVariantList(Arrays.asList(variant), options).get(0);
    }

    @Override
    public List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options) {
        List<QueryResult> queryResults = new ArrayList<>(variants.size());
        List<DBObject> queries = new ArrayList<>(variants.size());
        for (GenomicVariant genomicVariant : variants) {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(genomicVariant.getChromosome()).and("start").lessThanEquals(genomicVariant.getPosition()).and("end").greaterThanEquals(genomicVariant.getPosition());
            queries.add(builder.get());
        }

        options = addExcludeReturnFields("transcripts.xrefs", options);

        BasicDBObject returnFields = getReturnFields(options);
        BasicDBList list = executeFind(queries.get(0), returnFields, options, db.getCollection("core"));
        long dbTimeStart, dbTimeEnd;
        try {


            GenomicVariantEffectPredictor genomicVariantEffectPredictor = new GenomicVariantEffectPredictor();
            List<Gene> genes = jsonObjectMapper.readValue(list.toString(), new TypeReference<List<Gene>>() { });
            dbTimeStart = System.currentTimeMillis();
            List<GenomicVariantEffect> a = genomicVariantEffectPredictor.getAllEffectsByVariant(variants.get(0), genes, null);
            dbTimeEnd = System.currentTimeMillis();

            QueryResult queryResult = new QueryResult();
            queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());
            queryResult.setNumResults(list.size());
            queryResult.setResult(a);

            queryResults.add(queryResult);

        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(list.toString());


        return queryResults;
    }
}
