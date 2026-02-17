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

import com.mongodb.BasicDBList;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.ProteinSubstitutionPrediction;
import org.opencb.biodata.models.core.ProteinSubstitutionPredictionScore;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ProteinFeature;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.api.ProteinQuery;
import org.opencb.cellbase.core.api.TranscriptQuery;
import org.opencb.cellbase.core.api.query.CellBaseQueryOptions;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.*;

import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.ALPHAMISSENSE_DATA;
import static org.opencb.cellbase.lib.EtlCommons.REVEL_DATA;

/**
 * Created by imedina on 01/12/15.
 */
public class ProteinMongoDBAdaptor extends CellBaseDBAdaptor implements CellBaseCoreDBAdaptor<ProteinQuery, Entry> {

    private Map<Integer, MongoDBCollection> proteinSubstitutionMongoDBCollectionByRelease;

    private static final int NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS = 4;

    private static Map<String, String> aaShortNameMap;

    static {
        aaShortNameMap = new HashMap<>();

        aaShortNameMap.put("ALA", "A");
        aaShortNameMap.put("ARG", "R");
        aaShortNameMap.put("ASN", "N");
        aaShortNameMap.put("ASP", "D");
        aaShortNameMap.put("CYS", "C");
        aaShortNameMap.put("GLN", "Q");
        aaShortNameMap.put("GLU", "E");
        aaShortNameMap.put("GLY", "G");
        aaShortNameMap.put("HIS", "H");
        aaShortNameMap.put("ILE", "I");
        aaShortNameMap.put("LEU", "L");
        aaShortNameMap.put("LYS", "K");
        aaShortNameMap.put("MET", "M");
        aaShortNameMap.put("PHE", "F");
        aaShortNameMap.put("PRO", "P");
        aaShortNameMap.put("SER", "S");
        aaShortNameMap.put("THR", "T");
        aaShortNameMap.put("TRP", "W");
        aaShortNameMap.put("TYR", "Y");
        aaShortNameMap.put("VAL", "V");
    }


    public ProteinMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("ProteinMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap("protein");
        proteinSubstitutionMongoDBCollectionByRelease = buildCollectionByReleaseMap("protein_substitution_prediction");
    }

    public CellBaseDataResult<Score> getSubstitutionScores(TranscriptQuery query, String chromosome, Integer position, Integer aaPosition,
                                                           String aa) throws CellBaseException {
        CellBaseDataResult result = null;

        // Ensembl transcript id is needed for this collection
        if (query.getTranscriptsId() != null && query.getTranscriptsId().get(0) != null) {
            String transcriptId = query.getTranscriptsId().get(0).split("\\.")[0];
            Bson transcript = Filters.eq("transcriptId", transcriptId);
            MongoDBCollection mongoDBCollection = getCollectionByRelease(proteinSubstitutionMongoDBCollectionByRelease,
                    query.getDataRelease());

            String aaShortName = null;
            // If position and aa change are provided we create a 'projection' to return only the required data from the database
            if (aaPosition != null) {
                String projectionString = "aaPositions." + aaPosition;

                // If aa change is provided we only return that information
                if (StringUtils.isNotEmpty(aa)) {
                    aaShortName = aaShortNameMap.get(aa.toUpperCase());
                    projectionString += "." + aaShortName;
                }

                // Projection is used to minimize the returned data
                Bson positionProjection = Projections.include(projectionString);
                result = new CellBaseDataResult<>(mongoDBCollection.find(transcript, positionProjection, query.toQueryOptions()));
            } else {
                // Return the whole transcript data
                result = new CellBaseDataResult<>(mongoDBCollection.find(transcript, query.toQueryOptions()));
            }

            if (result != null && !result.getResults().isEmpty()) {
                Document document = (Document) result.getResults().get(0);
                Document aaPositionsDocument = (Document) document.get("aaPositions");

                // Position or aa change were not provided, returning whole transcript data
                if (aaPosition == null || aaPosition == -1 || aaShortName == null) {
                    // Return only the inner Document, not the whole document projected
                    result.setResults(Collections.singletonList(aaPositionsDocument));
                    // Position and aa were provided, return only corresponding Score objects
                } else {
                    List<Score> scoreList = new ArrayList<>(NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS);
                    if (result.getNumResults() == 1 && aaPositionsDocument != null) {
                        Document positionDocument = (Document) aaPositionsDocument.get(Integer.toString(aaPosition));
                        if (positionDocument != null) {
                            Document aaDocument = (Document) positionDocument.get(aaShortName);
                            if (aaDocument != null) {
                                if (aaDocument.get("ss") != null) {
                                    scoreList.add(new Score(Double.parseDouble("" + aaDocument.get("ss")),
                                            "sift", VariantAnnotationUtils.SIFT_DESCRIPTIONS.get(aaDocument.get("se"))));
                                }
                                if (aaDocument.get("ps") != null) {
                                    scoreList.add(new Score(Double.parseDouble("" + aaDocument.get("ps")),
                                            "polyphen", VariantAnnotationUtils.POLYPHEN_DESCRIPTIONS.get(aaDocument.get("pe"))));
                                }
                            }
                        }
                    }

                    // Query for REVEL and ALPHAMISSENSE scores (different data model)
                    scoreList.addAll(getRevelAndAlphaMissenseScores(mongoDBCollection, chromosome, position, transcriptId, aaPosition, aa));

                    result.setResults(scoreList);
                }
            }
        }

        // Return null if no transcript id is provided
        return result;
    }

//    private List<Score> getRevelAndAlphaMissenseScores(MongoDBCollection mongoDBCollection, String transcriptId,
//                                                       Integer position, String aa) {
//        List<Score> scoreList = new ArrayList<>();
//
//        if (position == null || StringUtils.isEmpty(aa)) {
//            return scoreList;
//        }
//
//        // Query for documents with source field (REVEL, ALPHAMISSENSE)
//        Bson filter = Filters.and(
//            Filters.eq("transcriptId", transcriptId),
//            Filters.eq("aaPosition", position),
//            Filters.in("source", "revel", "alphamissense")
//        );
//
//        try {
//            CellBaseDataResult<Document> documents = new CellBaseDataResult<>(
//                mongoDBCollection.find(filter, new QueryOptions())
//            );
//
//            if (documents != null && !documents.getResults().isEmpty()) {
//                for (Document document : documents.getResults()) {
//                    String source = (String) document.get("source");
//                    List<Document> scoresArray = document.getList("scores", Document.class);
//
//                    if (scoresArray != null) {
//                        for (Document scoreDoc : scoresArray) {
//                            String aaAlternate = (String) scoreDoc.get("aaAlternate");
//
//                            // Check if this score matches the requested alternate AA
//                            if (aa.equals(aaAlternate)) {
//                                Object scoreValue = scoreDoc.get("score");
//                                if (scoreValue != null) {
//                                    double score = Double.parseDouble(scoreValue.toString());
//                                    String description = (String) scoreDoc.get("effect");
//                                    scoreList.add(new Score(score, source, description));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.debug("Error retrieving REVEL and ALPHAMISSENSE scores for transcriptId: {}, position: {}",
//                    transcriptId, position, e);
//        }
//
//        return scoreList;
//    }


    private List<Score> getRevelAndAlphaMissenseScores(MongoDBCollection mongoDBCollection, String chromosome, Integer position,
                                                       String transcriptId, Integer aaPosition, String aa) {
        Map<String, Score> scoreSet = new HashMap<>();

        // aaPosition/position, aaAlternate are needed for this collection
        if (transcriptId != null && StringUtils.isNotEmpty(aa)) {
            // Sanity check, protein substitution predictions do not contain the transcript ID version
            transcriptId = transcriptId.split("\\.")[0];

            List<Bson> andBsonList = new ArrayList<>();
            // Query without transcriptId filter (will filter in Java for better performance)
            andBsonList.add(Filters.eq("chromosome", chromosome));
            andBsonList.add(Filters.in("source", REVEL_DATA, ALPHAMISSENSE_DATA));

            // Efficient single OR query: aaPosition for ALPHAMISSENSE, position for REVEL
            List<Bson> orBsonList = new ArrayList<>();
            if (aaPosition != null) {
                orBsonList.add(Filters.eq("aaPosition", aaPosition));  // ALPHAMISSENSE
            }
            if (position != null) {
                orBsonList.add(Filters.eq("position", position));  // REVEL
            }
            if (!orBsonList.isEmpty()) {
                andBsonList.add(Filters.or(orBsonList));
            }

            String aaAlternate = aaShortNameMap.get(aa.toUpperCase());
            if (aaAlternate != null) {
                andBsonList.add(Filters.eq("scores.aaAlternate", aaAlternate));
            }
            Bson bson = Filters.and(andBsonList);

            DataResult<ProteinSubstitutionPrediction> predictions = mongoDBCollection.find(bson, null, ProteinSubstitutionPrediction.class,
                    new QueryOptions());

            if (predictions != null && CollectionUtils.isNotEmpty(predictions.getResults())) {
                for (ProteinSubstitutionPrediction prediction : predictions.getResults()) {
                    // Filter by transcriptId in Java (handles both single and semicolon-separated values)
                    if (!isTranscriptIdMatch(prediction.getTranscriptId(), transcriptId)) {
                        continue;
                    }

                    for (ProteinSubstitutionPredictionScore predictionScore : prediction.getScores()) {
                        if (StringUtils.isNotEmpty(predictionScore.getAaAlternate()) && StringUtils.isNotEmpty(aaAlternate)
                                && predictionScore.getAaAlternate().equals(aaAlternate)) {
                            String key = prediction.getSource() + ":" + predictionScore.getScore() + ":" + predictionScore.getEffect();
                            if (!scoreSet.containsKey(key)) {
                                Score score = new Score(predictionScore.getScore(), prediction.getSource(), predictionScore.getEffect());
                                scoreSet.put(key, score);
                            }
                        }
                    }
                }
            }
        }

        if (MapUtils.isEmpty(scoreSet)) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(scoreSet.values());
        }
    }

    /**
     * Check if transcriptId matches the stored value.
     * Handles both single transcriptId and semicolon-separated list (REVEL format)
     * @param storedTranscriptId the value from the database (can be "ENST..." or "ENST...;ENST...;ENST...")
     * @param queryTranscriptId the transcript ID we're looking for
     * @return true if queryTranscriptId is found in storedTranscriptId
     */
    private boolean isTranscriptIdMatch(String storedTranscriptId, String queryTranscriptId) {
        if (StringUtils.isEmpty(storedTranscriptId) || StringUtils.isEmpty(queryTranscriptId)) {
            return false;
        }

        // For single value (ALPHAMISSENSE) or exact match
        if (storedTranscriptId.equals(queryTranscriptId)) {
            return true;
        }

        // For semicolon-separated list (REVEL)
        // Split and check if queryTranscriptId is in the list
        String[] transcriptIds = storedTranscriptId.split(";");
        for (String id : transcriptIds) {
            if (id.equals(queryTranscriptId)) {
                return true;
            }
        }

        return false;
    }

    public CellBaseDataResult<ProteinVariantAnnotation> getVariantAnnotation(Variant variant, String ensemblTranscriptId, int aaPosition,
                                                                             String aaReference, String aaAlternate, QueryOptions options,
                                                                             int dataRelease)
            throws CellBaseException {
        CellBaseDataResult<ProteinVariantAnnotation> cellBaseDataResult = new CellBaseDataResult<>();
        cellBaseDataResult.setId(ensemblTranscriptId + "/" + aaPosition + "/" + aaAlternate);
        long dbTimeStart = System.currentTimeMillis();

        ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
        proteinVariantAnnotation.setPosition(aaPosition);
        proteinVariantAnnotation.setReference(aaReference);
        proteinVariantAnnotation.setAlternate(aaAlternate);
        // Stop_gain/lost variants do not have SIFT/POLYPHEN scores
        if (!aaAlternate.equals("STOP") && !aaReference.equals("STOP")) {
            TranscriptQuery query = new TranscriptQuery();
            query.setTranscriptsId(Collections.singletonList(ensemblTranscriptId));
            query.setDataRelease(dataRelease);
            proteinVariantAnnotation.setSubstitutionScores(getSubstitutionScores(query, variant.getChromosome(), variant.getStart(),
                    aaPosition, aaAlternate).getResults());
        }

        CellBaseDataResult proteinVariantData;
        String shortAlternativeAa = aaShortNameMap.get(aaAlternate);
        if (shortAlternativeAa != null) {
            List<Bson> pipeline = new ArrayList<>();
            pipeline.add(new Document("$match", new Document("dbReference.id", ensemblTranscriptId)));

            Document projection = new Document();
            projection.put("accession", 1);
            projection.put("keyword", 1);
            projection.put("feature", 1);
            pipeline.add(new Document("$project", projection));

            pipeline.add(new Document("$unwind", "$feature"));

            BasicDBList andDBList2 = new BasicDBList();
            andDBList2.add(new Document("feature.location.position.position", aaPosition));
            andDBList2.add(new Document("feature.variation", shortAlternativeAa));
            Document firstOr = new Document("$and", andDBList2);
            BasicDBList andDBList3 = new BasicDBList();
            andDBList3.add(new Document("feature.location.end.position", new Document("$gte", aaPosition)));
            andDBList3.add(new Document("feature.location.begin.position", new Document("$lte", aaPosition)));
            Document secondOr = new Document();
            secondOr.put("$and", andDBList3);
            BasicDBList orList = new BasicDBList();
            orList.add(firstOr);
            orList.add(secondOr);
            pipeline.add(new Document("$match", new Document("$or", orList)));

            Document groupFields = new Document();
            groupFields.put("_id", "$accession");
            groupFields.put("keyword", new Document("$addToSet", "$keyword"));
            groupFields.put("feature", new Document("$addToSet", "$feature"));
            pipeline.add(new Document("$group", groupFields));

            MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
            proteinVariantData = executeAggregation2(ensemblTranscriptId + "_" + aaPosition + "_" + aaAlternate, pipeline,
                    new QueryOptions(), mongoDBCollection);
            if (proteinVariantData.getNumResults() > 0) {
                proteinVariantAnnotation = processProteinVariantData(proteinVariantAnnotation, shortAlternativeAa,
                        (Document) proteinVariantData.getResults().get(0));
            }
        }

        long dbTimeEnd = System.currentTimeMillis();
        cellBaseDataResult.setTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());
        cellBaseDataResult.setNumResults(1);
        cellBaseDataResult.setResults(Collections.singletonList(proteinVariantAnnotation));
        return cellBaseDataResult;
    }

    @Override
    public CellBaseIterator<Entry> iterator(ProteinQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<Entry> converter = new GenericDocumentComplexConverter<>(Entry.class);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        MongoDBIterator<Entry> iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<Entry>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey)
            throws CellBaseException {
        List<CellBaseDataResult<Entry>> results = new ArrayList<>();
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        for (String id : ids) {
            Bson projection = getProjection(queryOptions);
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("accession", id));
            orBsonList.add(Filters.eq("name", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<Entry>(mongoDBCollection.find(bson, projection, Entry.class, new QueryOptions())));
        }
        return results;
    }

    @Override
    public CellBaseDataResult<Entry> aggregationStats(ProteinQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Entry> groupBy(ProteinQuery query) throws CellBaseException {
        Bson bsonQuery = parseQuery(query);
        logger.info("proteinQuery: {}", bsonQuery.toBsonDocument().toJson());
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return groupBy(bsonQuery, query, "name", mongoDBCollection);
    }

    @Override
    public CellBaseDataResult<String> distinct(ProteinQuery query) throws CellBaseException {
        Bson bsonDocument = parseQuery(query);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument, String.class));
    }

//    public Iterator nativeIterator(Query query, QueryOptions options) {
//        Bson bson = parseQuery(query);
//        return mongoDBCollection.nativeQuery().find(bson, options);
//    }
//
//    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {
//
//    }

    public Bson parseQuery(ProteinQuery proteinQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        boolean visited = false;
        try {
            for (Map.Entry<String, Object> entry : proteinQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "accession":
                    case "name":
                    case "gene":
                    case "xrefs":
                        if (!visited) {
                            createProteinOrQuery(value, andBsonList);
                            visited = true;
                        }
                        break;
                    case "keyword":
                        createAndOrQuery(value, "keyword.value", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "token":
                    case "apiKey":
                    case "dataRelease":
                        // Do nothing
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createProteinOrQuery(Object queryValues, List<Bson> andBsonList) {
        List<Bson> orBsonList = new ArrayList<>();
        String mongoDbField = "dbReference.id";
        Query query = new Query(mongoDbField, queryValues);
        Bson filter = MongoDBQueryUtils.createAutoFilter(mongoDbField, mongoDbField, query,
                QueryParam.Type.STRING, MongoDBQueryUtils.LogicalOperator.OR);
        orBsonList.add(filter);

        mongoDbField = "gene.name.value";
        query = new Query(mongoDbField, queryValues);
        filter = MongoDBQueryUtils.createAutoFilter(mongoDbField, mongoDbField, query,
                QueryParam.Type.STRING, MongoDBQueryUtils.LogicalOperator.OR);
        orBsonList.add(filter);

        mongoDbField = "accession";
        query = new Query(mongoDbField, queryValues);
        filter = MongoDBQueryUtils.createAutoFilter(mongoDbField, mongoDbField, query,
                QueryParam.Type.STRING, MongoDBQueryUtils.LogicalOperator.OR);
        orBsonList.add(filter);

        mongoDbField = "name";
        query = new Query(mongoDbField, queryValues);
        filter = MongoDBQueryUtils.createAutoFilter(mongoDbField, mongoDbField, query,
                QueryParam.Type.STRING, MongoDBQueryUtils.LogicalOperator.OR);
        orBsonList.add(filter);

        andBsonList.add(Filters.or(orBsonList));
    }

//    private Bson parseQuery(Query query) {
//        List<Bson> andBsonList = new ArrayList<>();
//
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.ACCESSION.key(), "accession", andBsonList);
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.NAME.key(), "name", andBsonList);
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.GENE.key(), "gene.name.value", andBsonList);
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.XREFS.key(), "dbReference.id", andBsonList);
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.KEYWORD.key(), "keyword.value", andBsonList);
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.FEATURE_ID.key(), "feature.id", andBsonList);
//        createOrQuery(query, ProteinDBAdaptor.QueryParams.FEATURE_TYPE.key(), "feature.type", andBsonList);
//
//        if (andBsonList.size() > 0) {
//            return Filters.and(andBsonList);
//        } else {
//            return new Document();
//        }
//    }

    private ProteinVariantAnnotation processProteinVariantData(ProteinVariantAnnotation proteinVariantAnnotation,
                                                               String shortAlternativeAa, Document proteinVariantData) {

        proteinVariantAnnotation.setUniprotAccession(proteinVariantData.get("_id", ArrayList.class).get(0).toString());

        proteinVariantAnnotation.setKeywords(new ArrayList<>());
        ArrayList keywordList = (ArrayList) proteinVariantData.get("keyword", ArrayList.class).get(0);
        for (Object keywordObject : keywordList) {
            proteinVariantAnnotation.getKeywords().add((String) ((Document) keywordObject).get("value"));
        }

        proteinVariantAnnotation.setFeatures(new ArrayList<>());
        ArrayList featureList = proteinVariantData.get("feature", ArrayList.class);
        for (Object featureObject : featureList) {
            Document featureDocument = (Document) featureObject;
            String type = (String) featureDocument.get("type");


            ArrayList variationList = featureDocument.get("variation", ArrayList.class);
            //Check and process protein variants within the "feature" list
//            if(type!=null && type.equals("sequence variant") &&
//                    ((int)((Document)((Document) featureDBObject.get("location")).get("position"))
//                            .get("position"))==proteinVariantAnnotation.getPosition()) {
            // Current feature corresponds to current variant
            if (variationList != null && variationList.contains(shortAlternativeAa)) {
                proteinVariantAnnotation.setUniprotVariantId((String) featureDocument.get("id"));
                proteinVariantAnnotation.setFunctionalDescription((String) featureDocument.get("description"));
                // Not a protein variant, another type of feature e.g. protein domain
            } else {
                ProteinFeature proteinFeature = new ProteinFeature();
                proteinFeature.setId((String) featureDocument.get("id"));
                proteinFeature.setType((String) featureDocument.get("type"));
                proteinFeature.setDescription((String) featureDocument.get("description"));
//                proteinFeature.setRef((String) featureDBObject.get("ref"));
                if (featureDocument.get("location") != null) {
                    if (((Document) featureDocument.get("location")).get("begin") != null) {
                        proteinFeature.setStart((int) ((Document) ((Document) featureDocument.get("location"))
                                .get("begin")).get("position"));
                        if (((Document) featureDocument.get("location")).get("end") != null) {
                            proteinFeature.setEnd((int) ((Document) ((Document) featureDocument.get("location"))
                                    .get("end")).get("position"));
                        } else {
                            proteinFeature.setEnd(proteinFeature.getStart());
                        }
                    } else if (((Document) featureDocument.get("location")).get("position") != null) {
                        proteinFeature.setStart((int) ((Document) ((Document) featureDocument.get("location"))
                                .get("position")).get("position"));
                        proteinFeature.setEnd(proteinFeature.getStart());
                    }
                }
                proteinVariantAnnotation.getFeatures().add(proteinFeature);
            }
        }
        return proteinVariantAnnotation;
    }

    public CellBaseDataResult<Object> getProteinSubstitutionRawData(List<String> transcriptIds, CellBaseQueryOptions options,
                                                                    int dataRelease) throws CellBaseException {
        MongoDBCollection mongoDBCollection = getCollectionByRelease(proteinSubstitutionMongoDBCollectionByRelease, dataRelease);

        // Be sure to exclude the internal field "_id"
        Bson projection;
        if (options != null) {
            options.addExcludes("_id");
            projection = getProjection(options);
            options.getExcludes().remove("_id");
        } else {
            CellBaseQueryOptions queryOptions = new CellBaseQueryOptions();
            queryOptions.setExcludes(Collections.singletonList("_id"));
            projection = getProjection(queryOptions);
        }

        List<Bson> orBsonList = new ArrayList<>();
        for (String transcriptId : transcriptIds) {
            orBsonList.add(Filters.eq("transcriptId", transcriptId));
        }
        Bson bson = Filters.or(orBsonList);

        return new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, Object.class, new QueryOptions()));
    }
}
