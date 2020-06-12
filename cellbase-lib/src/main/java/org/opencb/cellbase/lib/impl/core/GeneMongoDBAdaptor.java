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

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.LogicalList;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDBQueryUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

/**
 * Created by imedina on 25/11/15.
 */
public class GeneMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<GeneQuery, Gene> {

//    private static final String TRANSCRIPTS = "transcripts";
//    private static final String GENE = "gene";
//    private static final String ANNOTATION_FLAGS = "annotationFlags";
    private static final Set<String> CONSTRAINT_NAMES = new HashSet();

    public GeneMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");
        logger.debug("GeneMongoDBAdaptor: in 'constructor'" + mongoDBCollection.count());
    }

//    @Override
//    public CellBaseDataResult<Gene> next(Query geneQuery, QueryOptions queryOptions) {
//        return null;
//    }
//
//    @Override
//    public CellBaseDataResult nativeNext(Query geneQuery, QueryOptions queryOption) {
//        return null;
//    }
//
//    @Override
//    public CellBaseDataResult getIntervalFrequencies(Query geneQuery, int intervalSize, QueryOptions options) {
//        if (geneQuery.getString(QueryParams.REGION.key()) != null) {
//            Region region = Region.parseRegion(geneQuery.getString(QueryParams.REGION.key()));
//            Bson bsonDocument = parseQuery(geneQuery);
//            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
//        }
//        return null;
//    }

//    @Override
//    public CellBaseDataResult<Long> count(Query geneQuery) {
//        Bson bsonDocument = parseQuery(geneQuery);
//        return new CellBaseDataResult<>(mongoDBCollection.count(bsonDocument));
//    }

//    @Override
//    public CellBaseDataResult<Long> count(GeneQuery query) {
//        Bson bsonDocument = parseQuery(query);
//        return new CellBaseDataResult<>(mongoDBCollection.count(bsonDocument));
//    }

    @Override
    public CellBaseDataResult<Gene> aggregationStats(GeneQuery query) {
        return null;
    }

//    @Override
//    public CellBaseDataResult<Gene> query(GeneQuery query) {
//        logger.info(query.toString());
//        Bson bson = parseQuery(query);
//        QueryOptions queryOptions = query.toQueryOptions();
//        Bson projection = MongoDBQueryUtils.getProjection(queryOptions);
//        CellBaseDataResult<Gene> result = new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, Gene.class, queryOptions));
//        return result;
//    }

//    public List<CellBaseDataResult<Gene>> query(List<GeneQuery> queries) {
//        List<CellBaseDataResult<Gene>> results = new ArrayList<>();
//        for (GeneQuery query : queries) {
//            results.add(query(query));
//        }
//        return results;
//    }

    @Override
    public CellBaseIterator<Gene> iterator(GeneQuery query) {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<Gene> converter = new GenericDocumentComplexConverter<>(Gene.class);
        MongoDBIterator<Gene> iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<String> distinct(GeneQuery geneQuery) {
        Bson bsonDocument = parseQuery(geneQuery);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(geneQuery.getFacet(), bsonDocument));
    }

//    @Override
//    public CellBaseDataResult stats(GeneQuery geneQuery) {
//        return null;
//    }


/*    public CellBaseDataResult<Gene> get(Query geneQuery, QueryOptions options) {
        Bson bson = parseQuery(geneQuery);*/
//        QueryOptions queryOptions = geneQuery.toQueryOptions();
//        Bson projection = MongoDBQueryUtils.getProjection(queryOptions);
//
//
//        if (postDBFilteringParametersEnabled(geneQuery)) {
//            // FIXME
//            CellBaseDataResult<Document> dataResult = postDBFiltering(geneQuery,
//                    new CellBaseDataResult<>(mongoDBCollection.find(bson, options)));
//            CellBaseDataResult<Gene> cellBaseDataResult = new CellBaseDataResult<>(dataResult.getId(),
//                    dataResult.getTime(), dataResult.getEvents(), dataResult.getNumResults(), null,
//                    dataResult.getNumMatches());
//
//            // Now we need to convert MongoDB Documents to Gene objects
//            // TODO: maybe we could geneQuery Genes in the first stage
//            ObjectMapper jsonObjectMapper = new ObjectMapper();
//            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
//            ObjectWriter objectWriter = jsonObjectMapper.writer();
//            cellBaseDataResult.setResults(dataResult.getResults().stream()
//                    .map(document -> {
//                        try {
//                            return this.objectMapper.readValue(objectWriter.writeValueAsString(document), Gene.class);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            return null;
//                        }
//                    }).collect(Collectors.toList()));
//            return cellBaseDataResult;
//        } else {
//            logger.debug("geneQuery: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
//            return new CellBaseDataResult<>(mongoDBCollection.find(bson, null, Gene.class, options));
//        }
//    }

//    @Override
//    public CellBaseDataResult nativeGet(GeneQuery geneQuery) {
//        Bson bson = parseQuery((GeneQuery) geneQuery);
//        QueryOptions queryOptions = geneQuery.toQueryOptions();
//        Bson projection = MongoDBQueryUtils.getProjection(queryOptions);
//        logger.info("geneQuery: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
//        return postDBFiltering((GeneQuery) geneQuery, new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, queryOptions)));
//    }

//    public Iterator nativeIterator(Query geneQuery, QueryOptions options) {
//        Bson bson = parseQuery(geneQuery);
//        return mongoDBCollection.nativeQuery().find(bson, options);
//    }

//    @Override
//    public void forEach(Query geneQuery, Consumer<? super Object> action, QueryOptions options) {
//        Objects.requireNonNull(action);
//        Iterator iterator = nativeIterator(geneQuery, options);
//        while (iterator.hasNext()) {
//            action.accept(iterator.next());
//        }
//    }

//    @Override
//    public CellBaseDataResult rank(GeneQuery geneQuery, String field, int numResults, boolean asc) {
//        return null;
//    }

//    @Override
//    public CellBaseDataResult groupBy(Query geneQuery, String field, QueryOptions options) {
//        Bson bsonQuery = parseQuery(geneQuery);
//        logger.info("geneQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
//        return groupBy(bsonQuery, field, "name", options);
//    }
//
//    @Override
//    public CellBaseDataResult groupBy(Query geneQuery, List<String> fields, QueryOptions options) {
//        Bson bsonQuery = parseQuery(geneQuery);
//        logger.info("geneQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
//        return groupBy(bsonQuery, fields, "name", options);
//    }
//

    @Override
    public CellBaseDataResult groupBy(GeneQuery geneQuery) {
        Bson bsonQuery = parseQuery(geneQuery);
        logger.info("geneQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return groupBy(bsonQuery, geneQuery, "name");
    }

//    @Override
//    public CellBaseDataResult startsWith(String id, QueryOptions options) {
//        Bson regex = Filters.regex("transcripts.xrefs.id", Pattern.compile("^" + id));
//        Bson include = Projections.include("id", "name", "chromosome", "start", "end");
//        return new CellBaseDataResult<>(mongoDBCollection.find(regex, include, options));
//    }

//    @Override
//    public CellBaseDataResult getRegulatoryElements(Query geneQuery, QueryOptions queryOptions) {
//        Bson bson = parseQuery(geneQuery);
//        CellBaseDataResult<Document> cellBaseDataResult = null;
//        CellBaseDataResult<Document> gene = new CellBaseDataResult<>(
//                mongoDBCollection.find(bson, new QueryOptions(QueryOptions.INCLUDE, "chromosome,start,end")));
//        if (gene != null) {
//            MongoDBCollection regulatoryRegionCollection = mongoDataStore.getCollection("regulatory_region");
//            for (Document document : gene.getResults()) {
//                Bson eq = Filters.eq("chromosome", document.getString("chromosome"));
//                Bson lte = Filters.lte("start", document.getInteger("end", Integer.MAX_VALUE));
//                Bson gte = Filters.gte("end", document.getInteger("start", 1));
//                cellBaseDataResult = new CellBaseDataResult<>(regulatoryRegionCollection.find(Filters.and(eq, lte, gte), queryOptions));
//            }
//        }
//        return cellBaseDataResult;
//    }

//    @Override
//    public CellBaseDataResult getTfbs(Query geneQuery, QueryOptions queryOptions) {
//        Bson bsonQuery = parseQuery(geneQuery);
//        Bson match = Aggregates.match(bsonQuery);
//
//        // We parse user's exclude options, ONLY _id can be added if exists
//        Bson includeAndExclude;
//        Bson exclude = null;
//        if (queryOptions != null && queryOptions.containsKey("exclude")) {
//            List<String> stringList = queryOptions.getAsStringList("exclude");
//            if (stringList.contains("_id")) {
//                exclude = Aggregates.project(Projections.exclude("_id"));
//            }
//        }
//        if (exclude != null) {
//            includeAndExclude = Aggregates.project(Projections.fields(Projections.excludeId(), Projections.include("transcripts.tfbs")));
//        } else {
//            includeAndExclude = Aggregates.project(Projections.include("transcripts.tfbs"));
//        }
//
//        Bson unwind = Aggregates.unwind("$transcripts");
//        Bson unwind2 = Aggregates.unwind("$transcripts.tfbs");
//
//        // This project the three fields of Xref to the top of the object
//        Document document = new Document("tfName", "$transcripts.tfbs.tfName");
//        document.put("pwm", "$transcripts.tfbs.pwm");
//        document.put("chromosome", "$transcripts.tfbs.chromosome");
//        document.put("start", "$transcripts.tfbs.start");
//        document.put("end", "$transcripts.tfbs.end");
//        document.put("strand", "$transcripts.tfbs.strand");
//        document.put("relativeStart", "$transcripts.tfbs.relativeStart");
//        document.put("relativeEnd", "$transcripts.tfbs.relativeEnd");
//        document.put("score", "$transcripts.tfbs.score");
//        Bson project = Aggregates.project(document);
//
//        return new CellBaseDataResult<>(mongoDBCollection.aggregate(
//                Arrays.asList(match, includeAndExclude, unwind, unwind2, project), queryOptions));
//    }


//    @Override
//    public CellBaseDataResult<String> getBiotypes(GeneQuery geneQuery) {
//        return distinct(geneQuery, "biotypes");
//    }


//    private Bson parseQuery(Query geneQuery) {
//        List<Bson> andBsonList = new ArrayList<>();
//        createRegionQuery(geneQuery, GeneDBAdaptor.QueryParams.REGION.key(), MongoDBCollectionConfiguration.GENE_CHUNK_SIZE, andBsonList);
//
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.ID.key(), "id", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.NAME.key(), "name", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.BIOTYPE.key(), "biotype", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);
//
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key(), "transcripts.id", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.TRANSCRIPT_NAME.key(), "transcripts.name", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.TRANSCRIPT_BIOTYPE.key(), "transcripts.biotype", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.TRANSCRIPT_ANNOTATION_FLAGS.key(), "transcripts.annotationFlags", andBsonList);
//
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.TFBS_NAME.key(), "transcripts.tfbs.name", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.ANNOTATION_DISEASE_ID.key(), "annotation.diseases.id", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.ANNOTATION_DISEASE_NAME.key(), "annotation.diseases.name", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_GENE.key(), "annotation.expression.geneName",
//        andBsonList);
//
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.ANNOTATION_DRUGS_NAME.key(), "annotation.drugs.drugName", andBsonList);
//        createOrQuery(geneQuery, GeneDBAdaptor.QueryParams.ANNOTATION_DRUGS_GENE.key(), "annotation.drugs.geneName", andBsonList);
//
//        createExpressionQuery(geneQuery, andBsonList);
//
//        if (andBsonList.size() > 0) {
//            return Filters.and(andBsonList);
//        } else {
//            return new Document();
//        }
//    }

    public Bson parseQuery(GeneQuery geneQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        boolean visited = false;
        try {
            for (Map.Entry<String, Object> entry : geneQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "id":
                    case "region":
                        if (!visited) {
                            createIdRegionQuery(geneQuery.getRegions(), geneQuery.getIds(), andBsonList);
                            visited = true;
                        }
                        break;
                    case "transcripts.id":
                    case "transcripts.name":
                    case "transcripts.xrefs.id":
                        createAndOrQuery(value, "transcripts.xrefs.id", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "transcripts.annotationFlags":
                        // TODO use unwind to filter out unwanted transcripts
                        createAndOrQuery(value, "transcripts.annotationFlags", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "transcripts.supportLevel":
                        createSupportLevelQuery(value, andBsonList);
                        break;
                    case "annotation.expression.tissue":
                        createExpressionQuery(geneQuery, andBsonList);
                        break;
                    case "annotation.expression.value":
                        // don't do anything, this value is parsed with the expression tissue
                        break;
                    case "constraints":
                        createConstraintsQuery(geneQuery, andBsonList);
                        break;
                    case "annotation.targets":
                        createTargetQuery(value, andBsonList);
                        break;
                    case "ontology":
                        createOntologyQuery(value, andBsonList);
                        break;
                    case "disease":
                        createDiseaseQuery(value, andBsonList);
                        break;
                    case "mirna":
                        createMirnaQuery(value, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("gene parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createSupportLevelQuery(Object value, List<Bson> andBsonList) {
        if (value != null) {
            andBsonList.add(Filters.regex("transcripts.supportLevel", "^" + value));
        }
    }

    // check in both the id and accession field.
    private void createMirnaQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "mirna.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "mirna.accession"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    // check in both the id and accession field.
    private void createDiseaseQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.diseases.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.diseases.name"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    // check in both the id and name field.
    private void createOntologyQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "transcripts.annotation.ontologies.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "transcripts.annotation.ontologies.name"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    // check in both the id and sourceId field.
    private void createTargetQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.targets.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.targets.sourceId"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    private Bson getLogicalListFilter(Object queryValues, String mongoDbField) {
        MongoDBQueryUtils.LogicalOperator operator = ((LogicalList) queryValues).isAnd()
                ? MongoDBQueryUtils.LogicalOperator.AND
                : MongoDBQueryUtils.LogicalOperator.OR;
        Query query = new Query(mongoDbField, queryValues);
        return MongoDBQueryUtils.createAutoFilter(mongoDbField, mongoDbField, query, QueryParam.Type.STRING, operator);
    }

    static {
        CONSTRAINT_NAMES.add("exac_oe_lof");
        CONSTRAINT_NAMES.add("exac_pLI");
        CONSTRAINT_NAMES.add("oe_lof");
        CONSTRAINT_NAMES.add("oe_mis");
        CONSTRAINT_NAMES.add("oe_syn");
    }

    private void createConstraintsQuery(GeneQuery geneQuery, List<Bson> andBsonList) {
        if (geneQuery != null && geneQuery.getAnnotationConstraints() != null) {
            LogicalList<String> paramValues = geneQuery.getAnnotationConstraints();
            // copied from mongoqueryutils. matches all valid operators
            String legalOperators = "(<=?|>=?|!=|!?=?~|==?)";
            // get the constraint name and the numerical value, look behind and look ahead
            String delimiter = "((?<=" + legalOperators + ")|(?=" + legalOperators + "))";

            // exac_oe_lof<=1.0,oe_lof<0.85585
            for (String paramValue : paramValues) {
                String[] expressions = paramValue.split(",");
                // oe_lof<0.85585
                for (String expression : expressions) {
                    final String[] expressionParts = expression.split(delimiter);
                    logger.error("expression" + expressionParts.length);
                    if (expressionParts.length >= 3) {
                        String constraintName = expressionParts[0];
                        String operator = expressionParts[1];
                        String numericalValue = expressionParts[2];
                        if (expressionParts.length == 4) {
                            numericalValue += expressionParts[3];
                        }
                        Query query = new Query("value", operator + numericalValue);
                        Bson valueFilter = MongoDBQueryUtils.createAutoFilter("value",
                                "value", query, QueryParam.Type.DECIMAL,
                                MongoDBQueryUtils.LogicalOperator.OR);

                        andBsonList.add(Filters.elemMatch("annotation.constraints",
                                Filters.and(Filters.eq("name", constraintName), valueFilter)));
                    }
                }
            }
        }
    }

    private void createExpressionQuery(GeneQuery geneQuery, List<Bson> andBsonList) {
        if (geneQuery != null) {
            List<String> tissues = geneQuery.getAnnotationExpressionTissue();
            if (CollectionUtils.isNotEmpty(tissues)) {
                List<String> expressionValues = geneQuery.getAnnotationExpressionValue();
                if (CollectionUtils.isNotEmpty(expressionValues)) {
                    MongoDBQueryUtils.LogicalOperator tissuesOperator = ((LogicalList) tissues).isAnd()
                            ? MongoDBQueryUtils.LogicalOperator.AND
                            : MongoDBQueryUtils.LogicalOperator.OR;
                    List<Bson> orBsonList = new ArrayList<>();
                    for (String tissue : tissues) {
                        for (String expressionValue : expressionValues) {
                            if (tissuesOperator.equals(MongoDBQueryUtils.LogicalOperator.AND)) {
                                andBsonList.add(Filters.elemMatch("annotation.expression",
                                        Filters.and(Filters.regex("factorValue", "(.)*" + tissue
                                                        + "(.)*", "i"),
                                                Filters.eq("expression", expressionValue))));
                            } else {
                                orBsonList.add(Filters.elemMatch("annotation.expression",
                                        Filters.and(Filters.regex("factorValue", "(.)*" + tissue
                                                + "(.)*", "i"),
                                                Filters.eq("expression", expressionValue))));
                            }
                        }
                        if (!orBsonList.isEmpty()) {
                            andBsonList.add(Filters.or(orBsonList));
                        }
                    }
                }
            }
        }
    }

//    private void createExpressionQuery(Query geneQuery, List<Bson> andBsonList) {
//        if (geneQuery != null) {
//            String tissue = geneQuery.getString(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_TISSUE.key());
//            if (tissue != null && !tissue.isEmpty()) {
//                String value = geneQuery.getString(GeneDBAdaptor.QueryParams.ANNOTATION_EXPRESSION_VALUE.key());
//                if (value != null && !value.isEmpty()) {
//                    andBsonList.add(Filters.elemMatch("annotation.expression",
//                            Filters.and(Filters.regex("factorValue", "(.)*" + tissue + "(.)*", "i"),
//                                    Filters.eq("expression", value))));
//                }
//            }
//        }
//    }

//    private Boolean postDBFilteringParametersEnabled(Query geneQuery) {
//        return StringUtils.isNotEmpty(geneQuery.getString(GeneDBAdaptor.QueryParams.TRANSCRIPT_ANNOTATION_FLAGS.key()));
//    }
//
//    private CellBaseDataResult<Document> postDBFiltering(Query geneQuery, CellBaseDataResult<Document> documentCellBaseDataResult) {
//        String annotationFlagsString = geneQuery.getString(GeneDBAdaptor.QueryParams.TRANSCRIPT_ANNOTATION_FLAGS.key());
//        if (StringUtils.isNotEmpty(annotationFlagsString)) {
//            Set<String> flags = new HashSet<>(Arrays.asList(annotationFlagsString.split(",")));
//            List<Document> documents = documentCellBaseDataResult.getResults();
//            for (Document document : documents) {
//                ArrayList<Document> transcripts = document.get(TRANSCRIPTS, ArrayList.class);
//                ArrayList<Document> matchedTranscripts = new ArrayList<>();
//                for (Document transcript : transcripts) {
//                    ArrayList annotationFlags = transcript.get(ANNOTATION_FLAGS, ArrayList.class);
//                    if (annotationFlags != null && annotationFlags.size() > 0) {
//                        if (CollectionUtils.containsAny(annotationFlags, flags)) {
//                            matchedTranscripts.add(transcript);
//                        }
//                    }
//                }
//                document.put(TRANSCRIPTS, matchedTranscripts);
//            }
//            documentCellBaseDataResult.setResults(documents);
//        }
//        return documentCellBaseDataResult;
//    }
//
//    private CellBaseDataResult<Document> postDBFiltering(GeneQuery geneQuery, CellBaseDataResult<Document> documentCellBaseDataResult) {
//        LogicalList<String> flags = geneQuery.getTranscriptsAnnotationFlags();
//        if (flags != null && !flags.isEmpty()) {
//            List<Document> documents = documentCellBaseDataResult.getResults();
//            for (Document document : documents) {
//                ArrayList<Document> transcripts = document.get(TRANSCRIPTS, ArrayList.class);
//                ArrayList<Document> matchedTranscripts = new ArrayList<>();
//                for (Document transcript : transcripts) {
//                    ArrayList annotationFlags = transcript.get(ANNOTATION_FLAGS, ArrayList.class);
//                    if (annotationFlags != null && annotationFlags.size() > 0) {
//                        if (CollectionUtils.containsAny(annotationFlags, flags)) {
//                            matchedTranscripts.add(transcript);
//                        }
//                    }
//                }
//                document.put(TRANSCRIPTS, matchedTranscripts);
//            }
//            documentCellBaseDataResult.setResults(documents);
//        }
//        return documentCellBaseDataResult;
//    }

//    class Converter implements ComplexTypeConverter<Gene, Document> {
////        private Object
//        @Override
//        public Gene convertToDataModelType(Document document) {
//            try {
//                document.remove("_id");
//                document.remove("_chunkIds");
//                return objectMapper.readerFor(Gene.class).readValue(objectMapper.writeValueAsString(document));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        public Document convertToStorageType(Gene gene) {
//            return null;
//        }
//    }
}
