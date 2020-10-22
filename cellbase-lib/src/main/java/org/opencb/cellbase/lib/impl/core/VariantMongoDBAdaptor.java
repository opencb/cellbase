/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.impl.core;

import com.mongodb.BulkWriteException;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.StructuralVariantType;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.iterator.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.LogicalList;
import org.opencb.cellbase.core.api.queries.ProjectionQueryOptions;
import org.opencb.cellbase.core.api.queries.VariantQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.variant.PopulationFrequencyPhasedQueryManager;
import org.opencb.cellbase.lib.CellBaseMongoDBIterator;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.cellbase.lib.VariantMongoIterator;
import org.opencb.cellbase.lib.converters.VariantConverter;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by imedina on 26/11/15.
 */
public class VariantMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<VariantQuery, Variant> {

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

    public CellBaseDataResult<Variant> next(Query query, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        if (query.getString(ParamConstants.QueryParams.REGION.key()) != null) {
            Region region = Region.parseRegion(query.getString(ParamConstants.QueryParams.REGION.key()));
            Bson bsonDocument = parseQuery(query);
            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
        return null;
    }

//    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        CellBaseDataResult<Long> nLoadedObjects = null;
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

    public CellBaseDataResult<Long> count(Query query) {
        Bson document = parseQuery(query);
        return new CellBaseDataResult(mongoDBCollection.count(document));
    }

    public CellBaseDataResult distinct(Query query, String field) {
        Bson document = parseQuery(query);
        return new CellBaseDataResult(mongoDBCollection.distinct(field, document));
    }

    public CellBaseDataResult<Variant> get(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
//        options.put(MongoDBCollection.SKIP_COUNT, true);

        // FIXME: patch to exclude annotation.additionalAttributes from the results - restore the call to the common
        // FIXME: addPrivateExcludeOptions as soon as the variation collection is updated with the new form of the
        // FIXME: additionalAttributes field
        QueryOptions options = addVariantPrivateExcludeOptions(new QueryOptions(inputOptions));
//        options = addPrivateExcludeOptions(options);

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, null, Variant.class, options));
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

    public List<CellBaseDataResult> nativeGet(List<Query> queries, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
//        options.put(MongoDBCollection.SKIP_COUNT, true);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult(mongoDBCollection.find(bson, options));
    }

    public Iterator<Variant> iterator(Query query, QueryOptions inputOptions) {
        Bson bson = parseQuery(query);
        QueryOptions options = addPrivateExcludeOptions(new QueryOptions(inputOptions));
        return new VariantMongoIterator(mongoDBCollection.nativeQuery().find(bson, options));
    }

    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options);
    }

    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {
        Objects.requireNonNull(action);
        Iterator iterator = nativeIterator(query, options);
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, field, "name", options);
    }

    public CellBaseDataResult groupBy(Query query, List<String> fields, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, fields, "name", options);
    }

    @Deprecated
    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createOrQuery(query, ParamConstants.QueryParams.CHROMOSOME.key(), "chromosome", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.START.key(), "start", andBsonList, QueryValueType.INTEGER);
        createOrQuery(query, ParamConstants.QueryParams.END.key(), "end", andBsonList, QueryValueType.INTEGER);
        if (query.containsKey(ParamConstants.QueryParams.REFERENCE.key())) {
            createOrQuery(query.getAsStringList(ParamConstants.QueryParams.REFERENCE.key()), "reference", andBsonList);
        }
        if (query.containsKey(ParamConstants.QueryParams.ALTERNATE.key())) {
            createOrQuery(query.getAsStringList(ParamConstants.QueryParams.ALTERNATE.key()), "alternate", andBsonList);
        }
        createRegionQuery(query, ParamConstants.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE, andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.ID.key(), "id", andBsonList);

        createImprecisePositionQuery(query, ParamConstants.QueryParams.CI_START_LEFT.key(),
                ParamConstants.QueryParams.CI_START_RIGHT.key(),
                "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
        createImprecisePositionQuery(query, ParamConstants.QueryParams.CI_END_LEFT.key(), ParamConstants.QueryParams.CI_END_RIGHT.key(),
                "sv.ciEndLeft", "sv.ciEndRight", andBsonList);

        createTypeQuery(query, ParamConstants.QueryParams.TYPE.key(), ParamConstants.QueryParams.SV_TYPE.key(), "type",
                "sv.type", andBsonList);

        createOrQuery(query, ParamConstants.QueryParams.CONSEQUENCE_TYPE.key(),
                "annotation.consequenceTypes.sequenceOntologyTerms.name", andBsonList);
        createGeneOrQuery(query, ParamConstants.QueryParams.GENE.key(), andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    public Bson parseQuery(VariantQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "region":
                        createRegionQuery(query, query.getRegions(), MongoDBCollectionConfiguration.VARIATION_CHUNK_SIZE, andBsonList);
                        break;
                    case "svType":
                        // don't do anything, this is parsed later
                        break;
                    case "type":
                        createTypeQuery(query, "type", "sv.type", andBsonList);
                        break;
                    case "ciStartRight":
                        // don't do anything, this is parsed later
                        break;
                    case "ciStartLeft":
                        createImprecisePositionQueryStart(query, andBsonList);
                    case "ciEndRight":
                        // don't do anything, this is parsed later
                        break;
                    case "ciEndLeft":
                        createImprecisePositionQueryEnd(query, andBsonList);
                    case "gene":
                        createGeneOrQuery(query, andBsonList);
                        break;
                    case "consequenceType":
                        createAndOrQuery(value, "annotation.consequenceTypes.sequenceOntologyTerms.name",
                                QueryParam.Type.STRING, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.debug("variant parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    @Deprecated
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


    private void createTypeQuery(VariantQuery query, String typeMongoField, String svTypeMongoField, List<Bson> andBsonList) {
        if (query != null && StringUtils.isNotBlank(query.getType())) {
            List<Bson> orBsonList = new ArrayList<>();
            String variantTypeString = query.getType();
            if (variantTypeString.equals(VariantType.DELETION.toString())
                    || (StructuralVariantType.COPY_NUMBER_LOSS.toString().equals(query.getSvType()))) {
                orBsonList.add(Filters.eq(typeMongoField, VariantType.DELETION.toString()));
                orBsonList.add(Filters.eq(svTypeMongoField, StructuralVariantType.COPY_NUMBER_LOSS.toString()));
                andBsonList.add(Filters.or(orBsonList));
            } else if (variantTypeString.equals(VariantType.INSERTION.toString())
                    || variantTypeString.equals(VariantType.DUPLICATION.toString())
                    || StructuralVariantType.COPY_NUMBER_GAIN.toString().equals(query.getSvType())) {
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

    @Deprecated
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

    private void createImprecisePositionQueryStart(VariantQuery query, List<Bson> andBsonList) {
        if (query.getCiStartLeft() != null && query.getCiStartRight() != null) {
            andBsonList.add(Filters.lte("ciStartLeft", query.getCiStartRight()));
            andBsonList.add(Filters.gte("ciStartRight", query.getCiStartLeft()));
        }
    }

    private void createImprecisePositionQueryEnd(VariantQuery query, List<Bson> andBsonList) {
        if (query.getCiEndLeft() != null && query.getCiEndRight() != null) {
            andBsonList.add(Filters.lte("ciEndLeft", query.getCiEndRight()));
            andBsonList.add(Filters.gte("ciEndRight", query.getCiEndLeft()));
        }
    }

    @Deprecated
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

    private void createGeneOrQuery(VariantQuery query, List<Bson> andBsonList) {
        if (query != null) {
            LogicalList<String> geneList = query.getGenes();
            if (geneList != null && !geneList.isEmpty()) {
                if (geneList.size() == 1) {
                    andBsonList.add(getGeneQuery(geneList.get(0)));
                } else {
                    List<Bson> orBsonList = new ArrayList<>(geneList.size());
                    for (String geneId : geneList) {
                        orBsonList.add(getGeneQuery(geneId));
                    }
                    if (geneList.isAnd()) {
                        andBsonList.add(Filters.and(orBsonList));
                    } else {
                        andBsonList.add(Filters.or(orBsonList));
                    }
                }
            }
        }
    }

    private Bson getGeneQuery(String geneId) {
        // For some reason Mongo does not deal properly with OR queries and indexes. It is extremely slow to perform
        // the commented query above. On the contrary this query below provides instant results
        if (geneId.startsWith(ENSEMBL_GENE_ID_PATTERN)) {
            return Filters.eq("annotation.consequenceTypes.geneId", geneId);
        } else if (geneId.startsWith(ENSEMBL_TRANSCRIPT_ID_PATTERN)) {
            return Filters.eq("annotation.consequenceTypes.transcriptId", geneId);
        } else {
            return Filters.eq("annotation.consequenceTypes.geneName", geneId);
        }
    }

    private CellBaseDataResult<Long> updateAnnotation(List<Document> variantDocumentList, String[] innerFields) {
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

        CellBaseDataResult<BulkWriteResult> bulkWriteResult;
        if (!queries.isEmpty()) {
            logger.info("updating object");
            QueryOptions options = new QueryOptions("upsert", false);
            options.put("multi", false);
            try {
                bulkWriteResult = new CellBaseDataResult<>(mongoDBCollection.update(queries, updates, options));
            } catch (BulkWriteException e) {
                throw e;
            }
            logger.info("{} object updated", bulkWriteResult.first().getModifiedCount());

            CellBaseDataResult<Long> longCellBaseDataResult = new CellBaseDataResult<>(bulkWriteResult.getId(),
                    bulkWriteResult.getTime(), bulkWriteResult.getEvents(), bulkWriteResult .getNumResults(),
                    Collections.singletonList((long) (bulkWriteResult.first().getUpserts().size()
                            + bulkWriteResult.first().getModifiedCount())), bulkWriteResult.getNumMatches());
            return longCellBaseDataResult;
        }
        logger.info("no object updated");
        return null;

    }

    private CellBaseDataResult<Long> updatePopulationFrequencies(List<Document> variantDocumentList) {
        List<Bson> queries = new ArrayList<>(variantDocumentList.size());
        List<Bson> updates = new ArrayList<>(variantDocumentList.size());
//        CellBaseDataResult<Long> longCellBaseDataResult = null;

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

        CellBaseDataResult<BulkWriteResult> bulkWriteResult;
        if (!queries.isEmpty()) {
            logger.info("updating object");
            QueryOptions options = new QueryOptions("upsert", true);
            options.put("multi", false);
            try {
                bulkWriteResult = new CellBaseDataResult<>(mongoDBCollection.update(queries, updates, options));
            } catch (BulkWriteException e) {
                throw e;
            }
            logger.info("{} object updated", bulkWriteResult.first().getUpserts().size() + bulkWriteResult.first().getModifiedCount());

            CellBaseDataResult<Long> longCellBaseDataResult = new CellBaseDataResult<>(bulkWriteResult.getId(),
                    bulkWriteResult.getTime(), bulkWriteResult.getEvents(), bulkWriteResult.getNumResults(),
                    Collections.singletonList((long) (bulkWriteResult.first().getUpserts().size()
                            + bulkWriteResult.first().getModifiedCount())), bulkWriteResult.getNumMatches());
            return longCellBaseDataResult;
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

    public CellBaseDataResult<Score> getFunctionalScoreVariant(Variant variant, QueryOptions queryOptions) {
        String chromosome = variant.getChromosome();
        int position = variant.getStart();
        String reference = variant.getReference();
        String alternate = variant.getAlternate();

        String chunkId = getChunkIdPrefix(chromosome, position, MongoDBCollectionConfiguration.VARIATION_FUNCTIONAL_SCORE_CHUNK_SIZE);
        QueryBuilder builder = QueryBuilder.start("_chunkIds").is(chunkId);
//                .and("chromosome").is(chromosome)
//                .and("start").is(position);
//        System.out.println(chunkId);
        CellBaseDataResult result = executeQuery(chromosome + "_" + position + "_" + reference + "_" + alternate,
                new Document(builder.get().toMap()), queryOptions, caddDBCollection);

//        System.out.println("result = " + result);

        List<Score> scores = new ArrayList<>();
        for (Object object : result.getResults()) {
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

        result.setResults(scores);
        return result;
    }

    /**
     * Created an specific method for pop freqs here since in this case phase is managed at the Pop. freq level.
     * @param variants list of Variant objects to query
     * @param queryOptions query options, e.g. phased={true, false}
     * @return list of CellBaseDataResult of Variant objects, each of which contains the query result of each variant in the
     * input list ("variants"). Positions within the list of CellBaseDataResult must always correspond to the position Variant
     * objects occupy in the "variants" query list.
     */
    public List<CellBaseDataResult<Variant>> getPopulationFrequencyByVariant(List<Variant> variants, QueryOptions queryOptions) {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getByVariant(variant, queryOptions));
        }

        if (queryOptions.get(ParamConstants.QueryParams.PHASE.key()) != null && queryOptions.getBoolean(
                ParamConstants.QueryParams.PHASE.key())) {
            results = populationFrequencyPhasedQueryManager.run(variants, results);

        }
        return results;
    }

    CellBaseDataResult getByVariant(Variant variant, QueryOptions options) {
        Query query;
//        if (VariantType.CNV.equals(variant.getType())) {

        // Queries for CNVs,SVs are different from simple short variants queries
        if (variant.getSv() != null
                && variant.getSv().getCiStartLeft() != null
                && variant.getSv().getCiStartRight() != null
                && variant.getSv().getCiEndLeft() != null
                && variant.getSv().getCiEndRight() != null) {
            query = new Query(ParamConstants.QueryParams.CHROMOSOME.key(), variant.getChromosome());
            // Imprecise queries can just be enabled for structural variants providing CIPOS positions. Imprecise queries
            // can be disabled by using the imprecise=false query option
            if (options.get(ParamConstants.QueryParams.IMPRECISE.key()) == null || (Boolean) options.get(
                    ParamConstants.QueryParams.IMPRECISE.key())) {
                int ciStartLeft = variant.getSv().getCiStartLeft();
                int ciStartRight = variant.getSv().getCiStartRight();
                int ciEndLeft = variant.getSv().getCiEndLeft();
                int ciEndRight = variant.getSv().getCiEndRight();
                query.append(ParamConstants.QueryParams.CI_START_LEFT.key(), ciStartLeft)
                        .append(ParamConstants.QueryParams.CI_START_RIGHT.key(), ciStartRight)
                        .append(ParamConstants.QueryParams.CI_END_LEFT.key(), ciEndLeft)
                        .append(ParamConstants.QueryParams.CI_END_RIGHT.key(), ciEndRight);
                // Exact query for start/end
            } else {
                query.append(ParamConstants.QueryParams.START.key(), variant.getStart());
                query.append(ParamConstants.QueryParams.END.key(), variant.getStart());
            }
            // CNVs must always be matched against COPY_NUMBER_GAIN/COPY_NUMBER_LOSS when searching - if provided
            if (VariantType.CNV.equals(variant.getType()) && variant.getSv().getType() != null) {
                query.append(ParamConstants.QueryParams.SV_TYPE.key(), variant.getSv().getType().toString());
            }
            query.append(ParamConstants.QueryParams.TYPE.key(), variant.getType().toString());
            // simple short variant query; This will be the query run in more than 99% of the cases
        } else {
            query = new Query(ParamConstants.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                    .append(ParamConstants.QueryParams.START.key(), variant.getStart())
                    .append(ParamConstants.QueryParams.REFERENCE.key(), variant.getReference())
                    .append(ParamConstants.QueryParams.ALTERNATE.key(), variant.getAlternate());
        }
        return get(query, options);
    }

    private List<CellBaseDataResult> getByVariant(List<Variant> variants, QueryOptions options) {
        List<CellBaseDataResult> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getByVariant(variant, options));
        }
        return results;
    }

    @Override
    public CellBaseIterator<Variant> iterator(VariantQuery query) {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        VariantConverter converter = new VariantConverter();
        logger.info("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        MongoDBIterator<Variant> iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<Variant> aggregationStats(VariantQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Variant> groupBy(VariantQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(VariantQuery query) {
        return null;
    }

    @Override
    public List<CellBaseDataResult<Variant>> info(List<String> ids, ProjectionQueryOptions queryOptions) {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>();
        for (String id : ids) {
            Bson projection = getProjection(queryOptions);
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<Variant>(mongoDBCollection.find(bson, projection, Variant.class, new QueryOptions())));
        }
        return results;
    }
}


