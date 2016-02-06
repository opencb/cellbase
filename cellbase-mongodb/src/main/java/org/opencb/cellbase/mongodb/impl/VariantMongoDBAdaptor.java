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
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by imedina on 26/11/15.
 */
public class VariantMongoDBAdaptor extends MongoDBAdaptor implements VariantDBAdaptor<Variation> {

    private static final String POP_FREQUENCIES_FIELD = "annotation.populationFrequencies";

    public VariantMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("variation");

        logger.debug("VariationMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult<Variation> next(Query query, QueryOptions options) {
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
    public QueryResult<Variation> get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        options.put(MongoDBCollection.SKIP_COUNT, true);
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public Iterator<Variation> iterator(Query query, QueryOptions options) {
        return null;
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
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.GENE.key(), "transcriptVariations.transcriptId", andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.CONSEQUENCE_TYPE.key(), "consequenceTypes", andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);

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
//            return longQueryResult;
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

}
