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

package org.opencb.cellbase.lib.impl;

import com.mongodb.BulkWriteException;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.StructuralVariantType;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.PopulationFrequencyPhasedQueryManager;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.cellbase.lib.VariantMongoIterator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by imedina on 26/11/15.
 */
public class VariantMongoDBAdaptor extends MongoDBAdaptor implements VariantDBAdaptor<Variant> {

    private static final String POP_FREQUENCIES_FIELD = "annotation.populationFrequencies";
    private static final String ANNOTATION_FIELD = "annotation";
    private static final float DECIMAL_RESOLUTION = 100f;
    private static final String ENSEMBL_GENE_ID_PATTERN = "ENSG00";
    private static final String ENSEMBL_TRANSCRIPT_ID_PATTERN = "ENST00";
    private static PopulationFrequencyPhasedQueryManager populationFrequencyPhasedQueryManager
            = new PopulationFrequencyPhasedQueryManager();


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
    public QueryResult<String> getConsequenceTypes(Query query) {
        // TODO we need to check if Query is empty!
        List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        QueryResult<String> queryResult = new QueryResult<>("consequence_types");
        queryResult.setNumResults(consequenceTypes.size());
        queryResult.setResult(consequenceTypes);
        return queryResult;
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
        if (query.getString(QueryParams.REGION.key()) != null) {
            Region region = Region.parseRegion(query.getString(QueryParams.REGION.key()));
            Bson bsonDocument = parseQuery(query);
            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
        return null;
    }

    @Override
    public QueryResult<Long> update(List objectList, String field, String[] innerFields) {
        QueryResult<Long> nLoadedObjects = null;
        switch (field) {
            case POP_FREQUENCIES_FIELD:
                nLoadedObjects = updatePopulationFrequencies((List<Document>) objectList);
                break;
            case ANNOTATION_FIELD:
                nLoadedObjects = updateAnnotation((List<Document>) objectList, innerFields);
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
    public QueryResult<Variant> get(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
//        options.put(MongoDBCollection.SKIP_COUNT, true);

        // FIXME: patch to exclude annotation.additionalAttributes from the results - restore the call to the common
        // FIXME: addPrivateExcludeOptions as soon as the variation collection is updated with the new form of the
        // FIXME: additionalAttributes field
        QueryOptions options = addVariantPrivateExcludeOptions(new QueryOptions(inputOptions));
//        options = addPrivateExcludeOptions(options);

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return mongoDBCollection.find(bson, null, Variant.class, options);
    }

    // FIXME: patch to exclude annotation.additionalAttributes from the results - to remove as soon as the variation
    // FIXME: collection is updated with the new form of the additionalAttributes field
    protected QueryOptions addVariantPrivateExcludeOptions(QueryOptions options) {
        if (options != null) {
            if (options.get("exclude") == null) {
                options.put("exclude", "_id,_chunkIds,annotation.additionalAttributes");
            } else {
                String exclude = options.getString("exclude");
                options.put("exclude", exclude + ",_id,_chunkIds,annotation.additionalAttributes");
            }
        } else {
            options = new QueryOptions("exclude", "_id,_chunkIds,annotation.additionalAttributes");
        }
        return options;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
//        options.put(MongoDBCollection.SKIP_COUNT, true);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public Iterator<Variant> iterator(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));
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

        createOrQuery(query, QueryParams.CHROMOSOME.key(), "chromosome", andBsonList);
        createOrQuery(query, QueryParams.START.key(), "start", andBsonList, QueryValueType.INTEGER);
        createOrQuery(query, QueryParams.END.key(), "end", andBsonList, QueryValueType.INTEGER);
        if (query.containsKey(QueryParams.REFERENCE.key())) {
            createOrQuery(query.getAsStringList(QueryParams.REFERENCE.key()), "reference", andBsonList);
        }
        if (query.containsKey(QueryParams.ALTERNATE.key())) {
            createOrQuery(query.getAsStringList(QueryParams.ALTERNATE.key()), "alternate", andBsonList);
        }
        createRegionQuery(query, VariantMongoDBAdaptor.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE, andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.ID.key(), "id", andBsonList);

        createImprecisePositionQuery(query, QueryParams.CI_START_LEFT.key(), QueryParams.CI_START_RIGHT.key(),
                "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
        createImprecisePositionQuery(query, QueryParams.CI_END_LEFT.key(), QueryParams.CI_END_RIGHT.key(),
                "sv.ciEndLeft", "sv.ciEndRight", andBsonList);

        createTypeQuery(query, QueryParams.TYPE.key(), QueryParams.SV_TYPE.key(), "type",
                "sv.type", andBsonList);

        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.CONSEQUENCE_TYPE.key(),
                "annotation.consequenceTypes.sequenceOntologyTerms.name", andBsonList);
        createGeneOrQuery(query, VariantMongoDBAdaptor.QueryParams.GENE.key(), andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createTypeQuery(Query query, String typeQueryParam, String svTypeQueryParam, String typeMongoField,
                                 String svTypeMongoField, List<Bson> andBsonList) {


        if (query != null && StringUtils.isNotBlank(query.getString(typeQueryParam))) {
            List<Bson> orBsonList = new ArrayList<>();
            String variantTypeString = query.getString(typeQueryParam);
            if (variantTypeString.equals(VariantType.DELETION.toString())
                    || (StructuralVariantType.COPY_NUMBER_LOSS.toString().equals(query.getString(svTypeQueryParam)))) {
                orBsonList.add(Filters.eq(typeMongoField, VariantType.DELETION.toString()));
                orBsonList.add(Filters.eq(svTypeMongoField, StructuralVariantType.COPY_NUMBER_LOSS.toString()));
                andBsonList.add(Filters.or(orBsonList));
            } else if (variantTypeString.equals(VariantType.INSERTION.toString())
                    || variantTypeString.equals(VariantType.DUPLICATION.toString())
                    || StructuralVariantType.COPY_NUMBER_GAIN.toString().equals(query.getString(svTypeQueryParam))) {
                orBsonList.add(Filters.eq(typeMongoField, VariantType.INSERTION.toString()));
                orBsonList.add(Filters.eq(typeMongoField, VariantType.DUPLICATION.toString()));
                orBsonList.add(Filters.eq(svTypeMongoField, StructuralVariantType.COPY_NUMBER_GAIN.toString()));
                andBsonList.add(Filters.or(orBsonList));
            // Inversion or just CNV (without subtype)
            } else {
                andBsonList.add(Filters.eq(typeMongoField, variantTypeString.toString()));
            }
        }
    }

    private void createImprecisePositionQuery(Query query, String leftQueryParam, String rightQueryParam,
                                              String leftLimitMongoField, String righLimitMongoField,
                                              List<Bson> andBsonList) {
        if (query != null && query.getString(leftQueryParam) != null && !query.getString(leftQueryParam).isEmpty()
                && query.getString(rightQueryParam) != null && !query.getString(rightQueryParam).isEmpty()) {
            int leftQueryValue = query.getInt(leftQueryParam);
            int rightQueryValue = query.getInt(rightQueryParam);
            andBsonList.add(Filters.lte(leftLimitMongoField, rightQueryValue));
            andBsonList.add(Filters.gte(righLimitMongoField, leftQueryValue));
        }
    }

//    private Bson getPositionWithinIntervalQuery(int value, String leftLimitMongoField,
//                                                String righLimitMongoField) {
//        List<Bson> andBsonList = new ArrayList<>(2);
//        andBsonList.add(Filters.lte(leftLimitMongoField, value));
//        andBsonList.add(Filters.gte(righLimitMongoField, value));
//
//        return Filters.and(andBsonList);
//    }
    private void createGeneOrQuery(Query query, String queryParam, List<Bson> andBsonList) {
        if (query != null) {
            List<String> geneList = query.getAsStringList(queryParam);
            if (geneList != null && !geneList.isEmpty()) {
                if (geneList.size() == 1) {
                    andBsonList.add(getGeneQuery(geneList.get(0)));
                } else {
                    List<Bson> orBsonList = new ArrayList<>(geneList.size());
                    for (String geneId : geneList) {
                        orBsonList.add(getGeneQuery(geneId));
                    }
                    andBsonList.add(Filters.or(orBsonList));
                }
            }
        }
    }

    private Bson getGeneQuery(String geneId) {
//        List<Bson> orBsonList = new ArrayList<>(3);
//        orBsonList.add(Filters.eq("annotation.consequenceTypes.geneName", geneId));
//        orBsonList.add(Filters.eq("annotation.consequenceTypes.ensemblGeneId", geneId));
//        orBsonList.add(Filters.eq("annotation.consequenceTypes.ensemblTranscriptId", geneId));

        // For some reason Mongo does not deal properly with OR queries and indexes. It is extremely slow to perform
        // the commented query above. On the contrary this query below provides instant results
        if (geneId.startsWith(ENSEMBL_GENE_ID_PATTERN)) {
            return Filters.eq("annotation.consequenceTypes.ensemblGeneId", geneId);
        } else if (geneId.startsWith(ENSEMBL_TRANSCRIPT_ID_PATTERN)) {
            return Filters.eq("annotation.consequenceTypes.ensemblTranscriptId", geneId);
        } else {
            return Filters.eq("annotation.consequenceTypes.geneName", geneId);
        }
    }

    private QueryResult<Long> updateAnnotation(List<Document> variantDocumentList, String[] innerFields) {
        List<Bson> queries = new ArrayList<>(variantDocumentList.size());
        List<Bson> updates = new ArrayList<>(variantDocumentList.size());

        for (Document variantDBObject : variantDocumentList) {
            Document annotationDBObject = (Document) variantDBObject.get(ANNOTATION_FIELD);
            Document toOverwrite = new Document();
            if (innerFields != null & innerFields.length > 0) {
                for (String field : innerFields) {
                    if (annotationDBObject.get(field) != null) {
                        toOverwrite.put(ANNOTATION_FIELD + "." + field, annotationDBObject.get(field));
                    }
                }
            } else {
                toOverwrite.put(ANNOTATION_FIELD, annotationDBObject);
            }

            Document update = new Document().append("$set", toOverwrite);
            updates.add(update);

            String chunkId = getChunkIdPrefix((String) variantDBObject.get("chromosome"),
                    (int) variantDBObject.get("start"), MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE);
            queries.add(new Document("_chunkIds", chunkId)
                    .append("chromosome", variantDBObject.get("chromosome"))
                    .append("start", variantDBObject.get("start"))
//                    .append("end", variantDBObject.get("end"))
                    .append("reference", variantDBObject.get("reference"))
                    .append("alternate", variantDBObject.get("alternate")));
        }

        QueryResult<BulkWriteResult> bulkWriteResult;
        if (!queries.isEmpty()) {
            logger.info("updating object");
            QueryOptions options = new QueryOptions("upsert", false);
            options.put("multi", false);
            try {
                bulkWriteResult = mongoDBCollection.update(queries, updates, options);
            } catch (BulkWriteException e) {
                throw e;
            }
            logger.info("{} object updated", bulkWriteResult.first().getModifiedCount());

            QueryResult<Long> longQueryResult = new QueryResult<>(bulkWriteResult.getId(), bulkWriteResult.getDbTime(), bulkWriteResult
                    .getNumResults(),
                    bulkWriteResult.getNumTotalResults(), bulkWriteResult.getWarningMsg(), bulkWriteResult.getErrorMsg(),
                    Collections.singletonList((long) (bulkWriteResult.first().getUpserts().size()
                            + bulkWriteResult.first().getModifiedCount())));
            return longQueryResult;
        }
        logger.info("no object updated");
        return null;

    }

    private QueryResult<Long> updatePopulationFrequencies(List<Document> variantDocumentList) {

        List<Bson> queries = new ArrayList<>(variantDocumentList.size());
        List<Bson> updates = new ArrayList<>(variantDocumentList.size());
//        QueryResult<Long> longQueryResult = null;

        for (Document variantDBObject : variantDocumentList) {
            Document annotationDBObject = (Document) variantDBObject.get(ANNOTATION_FIELD);
            Document push = new Document(POP_FREQUENCIES_FIELD, annotationDBObject.get("populationFrequencies"));

            // Remove annotation object from the DBObject so that push and setOnInsert do not update the same fields:
            // i.e. annotation.populationFrequencies and annotation
            variantDBObject.remove(ANNOTATION_FIELD);
            addChunkId(variantDBObject);

            Document update = new Document()
                    .append("$pushAll", push)
                    .append("$setOnInsert", variantDBObject);

            updates.add(update);

//            String chunkId = getChunkIdPrefix((String) variantDBObject.get("chromosome"),
//                    (int) variantDBObject.get("start"), MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE);
//            queries.add(new Document("_chunkIds", chunkId)
//                    .append("chromosome", variantDBObject.get("chromosome"))
            queries.add(new Document("chromosome", variantDBObject.get("chromosome"))
                    .append("start", variantDBObject.get("start"))
//                    .append("end", variantDBObject.get("end"))
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

    /**
     * Created an specific method for pop freqs here since in this case phase is managed at the Pop. freq level.
     * @param variants list of Variant objects to query
     * @param queryOptions query options, e.g. phased={true, false}
     * @return list of QueryResult<Variant> objects, each of which contains the query result of each variant in the
     * input list ("variants"). Positions within the list of QueryResult must always correspond to the position Variant
     * objects occupy in the "variants" query list.
     */
    public List<QueryResult<Variant>> getPopulationFrequencyByVariant(List<Variant> variants, QueryOptions queryOptions) {
        List<QueryResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getByVariant(variant, queryOptions));
        }

        if (queryOptions.get(QueryParams.PHASE.key()) != null && queryOptions.getBoolean(QueryParams.PHASE.key())) {
            results = populationFrequencyPhasedQueryManager.run(variants, results);

        }
        return results;
    }

}
