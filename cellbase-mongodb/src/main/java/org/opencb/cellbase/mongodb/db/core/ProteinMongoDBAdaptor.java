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

package org.opencb.cellbase.mongodb.db.core;

import com.mongodb.BasicDBList;
import com.mongodb.QueryBuilder;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.avro.ProteinFeature;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.db.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

/**
 * Created by imedina on 06/03/14.
 */
public class ProteinMongoDBAdaptor extends MongoDBAdaptor implements ProteinDBAdaptor {

    private MongoDBCollection proteinFunctionalPredictionCollection;

    private static Map<String, String> aaShortName = new HashMap<>();

    // Just two prediction methods are currently returned: SIFT and POLYPHEN
    private static final int NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS = 2;

    static {
        aaShortName.put("ALA", "A");
        aaShortName.put("ARG", "R");
        aaShortName.put("ASN", "N");
        aaShortName.put("ASP", "D");
        aaShortName.put("CYS", "C");
        aaShortName.put("GLN", "Q");
        aaShortName.put("GLU", "E");
        aaShortName.put("GLY", "G");
        aaShortName.put("HIS", "H");
        aaShortName.put("ILE", "I");
        aaShortName.put("LEU", "L");
        aaShortName.put("LYS", "K");
        aaShortName.put("MET", "M");
        aaShortName.put("PHE", "F");
        aaShortName.put("PRO", "P");
        aaShortName.put("SER", "S");
        aaShortName.put("THR", "T");
        aaShortName.put("TRP", "W");
        aaShortName.put("TYR", "Y");
        aaShortName.put("VAL", "V");
    }


    public ProteinMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);

        mongoDBCollection = mongoDataStore.getCollection("protein");
        proteinFunctionalPredictionCollection = mongoDataStore.getCollection("protein_functional_prediction");

        logger.debug("ProteinMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult first() {
        return null;
    }

    @Override
    public QueryResult count() {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult stats() {
        return null;
    }


    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {

        return null;
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("name").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

//        options = addExcludeReturnFields("transcripts", options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllByAccession(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByAccessionList(List<String> idList, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllByXref(String id, QueryOptions options) {
        return null;
    }


    @Override
    public List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("dbReference.id").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

//        options = addExcludeReturnFields("transcripts", options);
//        return executeQueryList(idList, queries, options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getAllFunctionPredictionByEnsemblTranscriptId(String transcriptId, QueryOptions options) {
        return getAllFunctionPredictionByEnsemblTranscriptIdList(Arrays.asList(transcriptId), options).get(0);
    }

    @Override
    public List<QueryResult> getAllFunctionPredictionByEnsemblTranscriptIdList(List<String> transcriptIdList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(transcriptIdList.size());

        if (options.containsKey("aaPosition")) {
            if (options.containsKey("aaChange")) {
                addIncludeReturnFields("aaPositions." + options.getString("aaPosition") + "." + options.getString("aaChange"), options);
            } else {
                addIncludeReturnFields("aaPositions." + options.getString("aaPosition"), options);
            }
        }


        for (String id : transcriptIdList) {
            QueryBuilder builder = QueryBuilder.start("transcriptId").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

//        options = addExcludeReturnFields("transcripts", options);
        return executeQueryList2(transcriptIdList, queries, options, proteinFunctionalPredictionCollection);
    }

    public QueryResult getFunctionPredictionByAaChange(String transcriptId, Integer aaPosition, String newAa, QueryOptions queryOptions) {

        QueryBuilder builder = QueryBuilder.start("transcriptId").is(transcriptId);
        QueryResult allChangesQueryResult = executeQuery(transcriptId,
                new Document(builder.get().toMap()), queryOptions, proteinFunctionalPredictionCollection);

        QueryResult proteinSubstitionScoresQueryResult = new QueryResult();
        proteinSubstitionScoresQueryResult.setDbTime(allChangesQueryResult.getDbTime());
        proteinSubstitionScoresQueryResult.setId(transcriptId + "-" + aaPosition + "-" + newAa);

//<<<<<<< HEAD
//        String currentAaShortName = aaShortName.get(newAa);
//        Map aaPositions = ((HashMap) ((Document) allChangesQueryResult.getResult().get(0)).get("aaPositions"));
//        if (allChangesQueryResult.getNumResults() > 0 && currentAaShortName != null && aaPositions != null) {
//            Document positionDBObject = (Document) aaPositions.get(Integer.toString(aaPosition));
//            if (positionDBObject != null) {
//                Object aaObject = positionDBObject.get(currentAaShortName);
//                if (aaObject != null) {
//                    proteinSubstitionScoresQueryResult.setNumResults(1);
//                    proteinSubstitionScoresQueryResult.setResult(Arrays.asList(aaObject));
//=======
        if (allChangesQueryResult.getNumResults() > 0) {
            String currentAaShortName = aaShortName.get(newAa);
            Map aaPositions = ((HashMap) ((Document) allChangesQueryResult.getResult().get(0)).get("aaPositions"));
            if (currentAaShortName != null && aaPositions != null) {
                Document positionDBObject = (Document) aaPositions.get(Integer.toString(aaPosition));
                if (positionDBObject != null) {
                    Object aaObject = positionDBObject.get(currentAaShortName);
                    if (aaObject != null) {
                        proteinSubstitionScoresQueryResult.setNumResults(1);
                        proteinSubstitionScoresQueryResult.setResult(Arrays.asList(aaObject));
                    } else {
                        proteinSubstitionScoresQueryResult.setErrorMsg("Unaccepted AA " + currentAaShortName
                                + ". Available AA changes for transcript " + transcriptId + ", position " + aaPosition
                                + ": " + positionDBObject.keySet().toString());
                        return proteinSubstitionScoresQueryResult;
                    }
//>>>>>>> develop
                } else {
                    proteinSubstitionScoresQueryResult.setErrorMsg("Unaccepted position " + Integer.toString(aaPosition)
                            + ". Available positions for transcript " + transcriptId + ": " + aaPositions.keySet().toString());
                    return proteinSubstitionScoresQueryResult;
                }
            } else {
                proteinSubstitionScoresQueryResult.setNumResults(0);
            }
        }

        return proteinSubstitionScoresQueryResult;
    }

    @Override
    public QueryResult getVariantAnnotation(String ensemblTranscriptId, Integer position, String aaReference,
                                            String aaAlternate, QueryOptions queryOptions) {

        QueryResult queryResult = new QueryResult();
        queryResult.setId(ensemblTranscriptId + "/" + position + "/" + aaAlternate);
        long dbTimeStart = System.currentTimeMillis();

        ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
        proteinVariantAnnotation.setPosition(position);
        proteinVariantAnnotation.setReference(aaReference);
        proteinVariantAnnotation.setAlternate(aaAlternate);
        proteinVariantAnnotation.setSubstitutionScores(getProteinSubstitutionScores(ensemblTranscriptId, position, aaAlternate));

        QueryResult proteinVariantData = null;
        String shortAlternativeAa = aaShortName.get(aaAlternate);
        if (shortAlternativeAa != null) {
            List<Bson> pipeline = new ArrayList<>();

//            BasicDBList andDBList1 = new BasicDBList();
//            andDBList1.add(new Document("dbReference.id", ensemblTranscriptId));
//            andDBList1.add(new Document("feature.location.position.position", position));
//            andDBList1.add(new Document("feature.variation", shortAlternativeAa));
//            pipeline.add(new Document("$match", new Document("$and", andDBList1)));

            pipeline.add(new Document("$match", new Document("dbReference.id", ensemblTranscriptId)));

            Document projection = new Document();
            projection.put("accession", 1);
            projection.put("keyword", 1);
            projection.put("feature", 1);
            pipeline.add(new Document("$project", projection));

            pipeline.add(new Document("$unwind", "$feature"));

            BasicDBList andDBList2 = new BasicDBList();
            andDBList2.add(new Document("feature.location.position.position", position));
            andDBList2.add(new Document("feature.variation", shortAlternativeAa));
            Document firstOr = new Document("$and", andDBList2);
            BasicDBList andDBList3 = new BasicDBList();
            andDBList3.add(new Document("feature.location.end.position", new Document("$gte", position)));
            andDBList3.add(new Document("feature.location.begin.position", new Document("$lte", position)));
            Document secondOr = new Document();
            secondOr.put("$and", andDBList3);
            BasicDBList orList = new BasicDBList();
            orList.add(firstOr);
            orList.add(secondOr);
            pipeline.add(new Document("$match", new Document("$or", orList)));
//            pipeline.add(new Document("$match", firstOr));
//
            Document groupFields = new Document();
            groupFields.put("_id", "$accession");
            groupFields.put("keyword", new Document("$addToSet", "$keyword"));
            groupFields.put("feature", new Document("$addToSet", "$feature"));
            pipeline.add(new Document("$group", groupFields));


            //TODO:terminar el pipeline de agregacion
//            QueryBuilder builder = QueryBuilder.start("dbReference.id").is(ensemblTranscriptId)
//                    .and("feature.location.position.position").is(position)
//                    .and("feature.variation").is(shortAlternativeAa);
//
//            Document firstOr = new Document();
//            firstOr.put("location.position.position", position);
//            firstOr.put("variation", shortAlternativeAa);
//
//            BasicDBList andList = new BasicDBList();
//            andList.add(new Document("location.end.position", new Document("$gte", position)));
//            andList.add(new Document("location.begin.position", new Document("$lte", position)));
//            Document secondOr = new Document();
//            secondOr.put("$and", andList);
//
//            BasicDBList orList = new BasicDBList();
//            orList.add(firstOr);
//            orList.add(secondOr);
//
//            Document elemMatch = new Document();
//            elemMatch.put("$elemMatch", new Document("$or", orList));
//
//            Document projection = new Document();
//            projection.put("feature", elemMatch);
//
//            QueryOptions localQueryOptions = new QueryOptions();
//            localQueryOptions.put("elemMatch",projection);
//            localQueryOptions.put("include","accession,keyword,feature");
//            proteinVariantData = executeQuery(ensemblTranscriptId + "_" + String.valueOf(position) + "_"
//                            + aaAlternate, new Document(builder.get().toMap()), localQueryOptions);
            proteinVariantData = executeAggregation2(ensemblTranscriptId + "_" + String.valueOf(position) + "_"
                    + aaAlternate, pipeline, new QueryOptions());
            if (proteinVariantData.getNumResults() > 0) {
                proteinVariantAnnotation = processProteinVariantData(proteinVariantAnnotation, shortAlternativeAa,
                        (Document) proteinVariantData.getResult().get(0));
            }
        }

        long dbTimeEnd = System.currentTimeMillis();
        queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());

        if (proteinVariantAnnotation.getSubstitutionScores() != null || proteinVariantAnnotation.getUniprotAccession() != null) {
            queryResult.setNumResults(1);
            queryResult.setResult(Collections.singletonList(proteinVariantAnnotation));
        }
        return queryResult;

    }

    private ProteinVariantAnnotation processProteinVariantData(ProteinVariantAnnotation proteinVariantAnnotation,
                                                               String shortAlternativeAa,
                                                               Document proteinVariantData) {
        proteinVariantAnnotation.setUniprotAccession((String) ((BasicDBList) proteinVariantData.get("_id")).get(0));

        proteinVariantAnnotation.setKeywords(new ArrayList<>());
        for (Object keywordObject : ((BasicDBList) ((BasicDBList) proteinVariantData.get("keyword")).get(0))) {
//            proteinVariantAnnotation.addUniprotKeyword((String)((Document) keywordObject).get("value"));
            proteinVariantAnnotation.getKeywords().add((String) ((Document) keywordObject).get("value"));
        }

        proteinVariantAnnotation.setFeatures(new ArrayList<>());
        for (Object featureObject : (BasicDBList) proteinVariantData.get("feature")) {
            Document featureDBObject = (Document) featureObject;
            String type = (String) featureDBObject.get("type");

            BasicDBList variationDBList = (BasicDBList) featureDBObject.get("variation");
            //Check and process protein variants within the "feature" list
//            if(type!=null && type.equals("sequence variant") &&
//                    ((int)((Document)((Document) featureDBObject.get("location")).get("position"))
//                            .get("position"))==proteinVariantAnnotation.getPosition()) {
            // Current feature corresponds to current variant
            if (variationDBList != null && variationDBList.contains(shortAlternativeAa)) {
                proteinVariantAnnotation.setUniprotVariantId((String) featureDBObject.get("id"));
                proteinVariantAnnotation.setFunctionalDescription((String) featureDBObject.get("description"));
                // Not a protein variant, another type of feature e.g. protein domain
            } else {
                ProteinFeature proteinFeature = new ProteinFeature();
                proteinFeature.setId((String) featureDBObject.get("id"));
                proteinFeature.setType((String) featureDBObject.get("type"));
                proteinFeature.setDescription((String) featureDBObject.get("description"));
//                proteinFeature.setRef((String) featureDBObject.get("ref"));
                if (featureDBObject.get("location") != null) {
                    if (((Document) featureDBObject.get("location")).get("begin") != null) {
                        proteinFeature.setStart((int) ((Document) ((Document) featureDBObject.get("location"))
                                .get("begin")).get("position"));
                        if (((Document) featureDBObject.get("location")).get("end") != null) {
                            proteinFeature.setEnd((int) ((Document) ((Document) featureDBObject.get("location"))
                                    .get("end")).get("position"));
                        } else {
                            proteinFeature.setEnd(proteinFeature.getStart());
                        }
                    } else if (((Document) featureDBObject.get("location")).get("position") != null) {
                        proteinFeature.setStart((int) ((Document) ((Document) featureDBObject.get("location"))
                                .get("position")).get("position"));
                        proteinFeature.setEnd(proteinFeature.getStart());
                    }
                }
//                proteinVariantAnnotation.addProteinFeature(proteinFeature);
                proteinVariantAnnotation.getFeatures().add(proteinFeature);
            }
        }

        return proteinVariantAnnotation;
    }

    private List<Score> getProteinSubstitutionScores(String ensemblTranscriptId, int aaPosition, String alternativeAa) {
        QueryResult proteinSubstitutionScoresQueryResult = getFunctionPredictionByAaChange(ensemblTranscriptId,
                aaPosition, alternativeAa, new QueryOptions());
        List<Score> scoreList = null;
        if (proteinSubstitutionScoresQueryResult.getNumResults() == 1) {
            scoreList = new ArrayList<>(NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS);
            Document proteinSubstitutionScores = (Document) proteinSubstitutionScoresQueryResult.getResult().get(0);
            if (proteinSubstitutionScores.get("ss") != null) {
                scoreList.add(new Score(Double.parseDouble("" + proteinSubstitutionScores.get("ss")),
                        "sift", VariantAnnotationUtils.SIFT_DESCRIPTIONS.get(proteinSubstitutionScores.get("se"))));
            }
            if (proteinSubstitutionScores.get("ps") != null) {
                scoreList.add(new Score(Double.parseDouble("" + proteinSubstitutionScores.get("ps")),
                        "polyphen", VariantAnnotationUtils.POLYPHEN_DESCRIPTIONS.get(proteinSubstitutionScores.get("pe"))));
            }
        }
        return scoreList;
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
