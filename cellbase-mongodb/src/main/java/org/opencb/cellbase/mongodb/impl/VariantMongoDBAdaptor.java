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

package org.opencb.cellbase.mongodb.impl;

import com.mongodb.BulkWriteException;
import com.mongodb.QueryBuilder;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.VariantMongoIterator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Created by imedina on 26/11/15.
 */
public class VariantMongoDBAdaptor extends MongoDBAdaptor implements VariantDBAdaptor<Variant> {

    private static final String POP_FREQUENCIES_FIELD = "annotation.populationFrequencies";
    private static final float DECIMAL_RESOLUTION = 100f;

    private MongoDBCollection caddDBCollection;

    public VariantMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("variation");
        caddDBCollection = mongoDataStore.getCollection("variation_functional_score");

        logger.debug("VariationMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult startsWith(String id, QueryOptions options) {
        Bson regex = Filters.regex("ids", Pattern.compile("^" + id));
        Bson include = Projections.include("ids", "chromosome", "start", "end");
        return mongoDBCollection.find(regex, include, options);
    }

    @Override
    public QueryResult<Variant> next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        if (query.getString("region") != null) {
            Region region = Region.parseRegion(query.getString("region"));
            Bson bsonDocument = parseQuery(query);
            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
        return null;
    }

    @Override
    public QueryResult<Long> update(List objectList, String field) {
        QueryResult<Long> nLoadedObjects = null;
        switch (field) {
            case POP_FREQUENCIES_FIELD:
                nLoadedObjects = updatePopulationFrequencies((List<Document>) objectList);
                break;
            default:
                logger.error("Invalid field {}: no action implemented for updating this field.", field);
                break;
        }
        return nLoadedObjects;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        Bson document = parseQuery(query);
        return mongoDBCollection.count(document);
    }

    @Override
    public QueryResult distinct(Query query, String field) {
        Bson document = parseQuery(query);
        return mongoDBCollection.distinct(field, document);
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult<Variant> get(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        options.put(MongoDBCollection.SKIP_COUNT, true);
        options = addPrivateExcludeOptions(options);
        return mongoDBCollection.find(bson, null, Variant.class, options);
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        options.put(MongoDBCollection.SKIP_COUNT, true);
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public Iterator<Variant> iterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        options = addPrivateExcludeOptions(options);
        return new VariantMongoIterator(mongoDBCollection.nativeQuery().find(bson, options).iterator());
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {
        Objects.requireNonNull(action);
        Iterator iterator = nativeIterator(query, options);
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    @Override
    public QueryResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, String field, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, field, "name", options);
    }

    @Override
    public QueryResult groupBy(Query query, List<String> fields, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, fields, "name", options);
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, VariantMongoDBAdaptor.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE, andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.ID.key(), "id", andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.GENE.key(), "annotation.consequenceTypes.ensemblGeneId",
                andBsonList);
        createOrQuery(query, QueryParams.CHROMOSOME.key(), "chromosome", andBsonList);
        createOrQuery(query, QueryParams.REFERENCE.key(), "reference", andBsonList);
        createOrQuery(query, QueryParams.ALTERNATE.key(), "alternate", andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.CONSEQUENCE_TYPE.key(),
                "consequenceTypes.sequenceOntologyTerms.name", andBsonList);
//        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private QueryResult<Long> updatePopulationFrequencies(List<Document> variantDocumentList) {

        List<Bson> queries = new ArrayList<>(variantDocumentList.size());
        List<Bson> updates = new ArrayList<>(variantDocumentList.size());
//        QueryResult<Long> longQueryResult = null;

        for (Document variantDBObject : variantDocumentList) {
            Document annotationDBObject = (Document) variantDBObject.get("annotation");
            Document push = new Document(POP_FREQUENCIES_FIELD, annotationDBObject.get("populationFrequencies"));

            // Remove annotation object from the DBObject so that push and setOnInsert do not update the same fields:
            // i.e. annotation.populationFrequencies and annotation
            variantDBObject.remove("annotation");
            addChunkId(variantDBObject);

            Document update = new Document()
                    .append("$pushAll", push)
                    .append("$setOnInsert", variantDBObject);

            updates.add(update);

            String chunkId = getChunkIdPrefix((String) variantDBObject.get("chromosome"),
                    (int) variantDBObject.get("start"), MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE);
            queries.add(new Document("_chunkIds", chunkId)
                    .append("chromosome", variantDBObject.get("chromosome"))
                    .append("start", variantDBObject.get("start"))
                    .append("end", variantDBObject.get("end"))
                    .append("reference", variantDBObject.get("reference"))
                    .append("alternate", variantDBObject.get("alternate")));
        }

        QueryResult<BulkWriteResult> bulkWriteResult;
        if (!queries.isEmpty()) {
            logger.info("updating object");
            QueryOptions options = new QueryOptions("upsert", true);
            options.put("multi", false);
            try {
                bulkWriteResult = mongoDBCollection.update(queries, updates, options);
            } catch (BulkWriteException e) {
                throw e;
            }
            logger.info("{} object updated", bulkWriteResult.first().getUpserts().size() + bulkWriteResult.first().getModifiedCount());

            QueryResult<Long> longQueryResult = new QueryResult<>(bulkWriteResult.getId(), bulkWriteResult.getDbTime(), bulkWriteResult
                    .getNumResults(),
                    bulkWriteResult.getNumTotalResults(), bulkWriteResult.getWarningMsg(), bulkWriteResult.getErrorMsg(),
                    Collections.singletonList((long) (bulkWriteResult.first().getUpserts().size()
                            + bulkWriteResult.first().getModifiedCount())));

//            return bulkWriteResult.first().getUpserts().size() + bulkWriteResult.first().getModifiedCount();
            return longQueryResult;
        }
        logger.info("no object updated");
        return null;
    }

    // Method copied from MongoDBCellbaseLoader. In a near future only this one will stay. Insert work currently done
    // by MongoDBCellbaseLoader must be replaced by an appropriate method in this adaptor
    private void addChunkId(Document dbObject) {
        List<String> chunkIds = new ArrayList<>();
        int chunkStart = (Integer) dbObject.get("start") / MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE;
        int chunkEnd = (Integer) dbObject.get("end") / MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE;
        String chunkIdSuffix = MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE / 1000 + "k";
        for (int i = chunkStart; i <= chunkEnd; i++) {
            if (dbObject.containsKey("chromosome")) {
                chunkIds.add(dbObject.get("chromosome") + "_" + i + "_" + chunkIdSuffix);
            } else {
                chunkIds.add(dbObject.get("sequenceName") + "_" + i + "_" + chunkIdSuffix);
            }
        }
        dbObject.put("_chunkIds", chunkIds);
    }

    @Override
    public QueryResult<Score> getFunctionalScoreVariant(Variant variant, QueryOptions queryOptions) {
        String chromosome = variant.getChromosome();
        int position = variant.getStart();
        String reference = variant.getReference();
        String alternate = variant.getAlternate();

        String chunkId = getChunkIdPrefix(chromosome, position, MongoDBCollectionConfiguration.VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE);
        QueryBuilder builder = QueryBuilder.start("_chunkIds").is(chunkId);
//                .and("chromosome").is(chromosome)
//                .and("start").is(position);
//        System.out.println(chunkId);
        QueryResult result = executeQuery(chromosome + "_" + position + "_" + reference + "_" + alternate,
                new Document(builder.get().toMap()), queryOptions, caddDBCollection);

//        System.out.println("result = " + result);

        List<Score> scores = new ArrayList<>();
        for (Object object : result.getResult()) {
//            System.out.println("object = " + object);
            Document dbObject = (Document) object;
            int chunkStart = dbObject.getInteger("start");
            int chunkEnd = dbObject.getInteger("end");
            // CADD positions are not continuous through the whole chromosome. Several documents may be associated with
            // the same chunk id: we have to be sure that current document contains queried position. Only two documents
            // will contain queried position - one for raw and one for scaled values
            if (position >= chunkStart && position <= chunkEnd) {
                int offset = (position - chunkStart);
                ArrayList basicDBList = dbObject.get("values", ArrayList.class);

//                long l1 = 0L; // TODO: delete
//                try { // TODO: delete
                long l1 = Long.parseLong(basicDBList.get(offset).toString());
//                                 l1 = (Long) basicDBList.get(offset);
//                } catch (Exception e) {  // TODO: delete
//                    logger.error("problematic variant: {}", variant.toString());
//                    throw e;
//                }

                if (dbObject.getString("source").equalsIgnoreCase("cadd_raw")) {
                    float value = 0f;
                    switch (alternate.toLowerCase()) {
                        case "a":
//                            value = ((short) (l1 >> 48) - 10000) / DECIMAL_RESOLUTION;
                            value = (((short) (l1 >> 48)) / DECIMAL_RESOLUTION) - 10;
                            break;
                        case "c":
                            value = (((short) (l1 >> 32)) / DECIMAL_RESOLUTION) - 10;
                            break;
                        case "g":
                            value = (((short) (l1 >> 16)) / DECIMAL_RESOLUTION) - 10;
                            break;
                        case "t":
                            value = (((short) (l1 >> 0)) / DECIMAL_RESOLUTION) - 10;
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
        }

        result.setResult(scores);
        return result;
    }
}
