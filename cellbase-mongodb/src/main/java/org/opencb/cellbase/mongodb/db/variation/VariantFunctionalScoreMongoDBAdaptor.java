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

package org.opencb.cellbase.mongodb.db.variation;

import com.mongodb.BasicDBList;
import org.bson.Document;
import com.mongodb.QueryBuilder;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.db.api.variation.VariantFunctionalScoreDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 16/11/15.
 */
public class VariantFunctionalScoreMongoDBAdaptor extends MongoDBAdaptor implements VariantFunctionalScoreDBAdaptor {

    private static final float DECIMAL_RESOLUTION = 1000f;

    public VariantFunctionalScoreMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("variation_functional_score");

        logger.debug("VariantFunctionalScoreMongoDBAdaptor: in 'constructor'");
    }

    public QueryResult first() {
        return null;
    }

    public QueryResult count() {
        return null;
    }

    public QueryResult stats() {
        return null;
    }

    @Override
    public QueryResult getByVariant(String chromosome, int position, String reference, String alternate, QueryOptions queryOptions) {
        String chunkId = getChunkIdPrefix(chromosome, position, MongoDBCollectionConfiguration.VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE);
        QueryBuilder builder = QueryBuilder.start("_chunkIds").is(chunkId);
//                .and("chromosome").is(chromosome)
//                .and("start").is(position);
//        System.out.println(chunkId);
        QueryResult result = executeQuery(chromosome + "_" + position + "_" + reference + "_" + alternate,
                new Document(builder.get().toMap()), queryOptions, mongoDBCollection);

//        System.out.println("result = " + result);

        int offset = (position % MongoDBCollectionConfiguration.VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE) - 1;
        List<Score> scores = new ArrayList<>();
        for (Object object : result.getResult()) {
//            System.out.println("object = " + object);
            Document dbObject = (Document) object;
            BasicDBList basicDBList = (BasicDBList) dbObject.get("values");
            Long l1 = (Long) basicDBList.get(offset);
//            System.out.println("l1 = " + l1);
            if (dbObject.getString("source").equalsIgnoreCase("cadd_raw")) {
                float value = 0f;
                switch (alternate.toLowerCase()) {
                    case "a":
                        value = ((short) (l1 >> 48) - 10000) / DECIMAL_RESOLUTION;
                        break;
                    case "c":
                        value = ((short) (l1 >> 32) - 10000) / DECIMAL_RESOLUTION;
                        break;
                    case "g":
                        value = ((short) (l1 >> 16) - 10000) / DECIMAL_RESOLUTION;
                        break;
                    case "t":
                        value = ((short) (l1 >> 0) - 10000) / DECIMAL_RESOLUTION;
                        break;
                    default:
                        break;
                }
                scores.add(Score.newBuilder()
                        .setScore(value)
                        .setSource(dbObject.getString("source"))
                        .setDescription(null)
//                        .setDescription("")
                        .build());
            }

            if (dbObject.getString("source").equalsIgnoreCase("cadd_scaled")) {
                float value = 0f;
                switch (alternate.toLowerCase()) {
                    case "a":
                        value = ((short) (l1 >> 48)) / DECIMAL_RESOLUTION;
                        break;
                    case "c":
                        value = ((short) (l1 >> 32)) / DECIMAL_RESOLUTION;
                        break;
                    case "g":
                        value = ((short) (l1 >> 16)) / DECIMAL_RESOLUTION;
                        break;
                    case "t":
                        value = ((short) (l1 >> 0)) / DECIMAL_RESOLUTION;
                        break;
                    default:
                        break;
                }
                scores.add(Score.newBuilder()
                        .setScore(value)
                        .setSource(dbObject.getString("source"))
                        .setDescription(null)
//                        .setDescription("")
                        .build());
            }
        }

        result.setResult(scores);
        return result;
    }


    @Override
    public QueryResult getByVariant(Variant variant, QueryOptions queryOptions) {
        return getByVariant(variant.getChromosome(), variant.getStart(), variant.getReference(), variant.getAlternate(), queryOptions);
    }

    @Override
    public List<QueryResult> getAllByVariantList(List<Variant> variantList, QueryOptions queryOptions) {
        List<QueryResult> queryResultList = new ArrayList<>(variantList.size());
        for (Variant variant : variantList) {
            queryResultList.add(getByVariant(variant.getChromosome(), variant.getStart(), variant.getReference(),
                    variant.getAlternate(), queryOptions));
        }
        return queryResultList;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
