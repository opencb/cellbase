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

import com.mongodb.BasicDBList;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.variant.avro.ProteinFeature;
import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by imedina on 01/12/15.
 */
public class ProteinMongoDBAdaptor extends MongoDBAdaptor implements ProteinDBAdaptor<Entry> {

    private MongoDBCollection proteinSubstitutionMongoDBCollection;

    private static final int NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS = 2;

    private static Map<String, String> aaShortName = new HashMap<>();

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
        proteinSubstitutionMongoDBCollection = mongoDataStore.getCollection("protein_functional_prediction");

        logger.debug("ProteinMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult<Score> getSubstitutionScores(Query query, QueryOptions options) {
        QueryResult result;
        QueryResult<Score> scoreResult = null;

        // Ensembl transcript id is needed for this collection
        if (query.getString("transcript") != null) {
            Bson transcript = Filters.eq("transcriptId", query.getString("transcript"));

            // If position and aa change are provided we create a 'projection' to return only the required data from the database
            if (query.get("position") != null && !query.getString("position").isEmpty() && query.getInt("position", 0) != 0) {
                String projectionString = "aaPositions." + query.getInt("position");

                // If aa change is provided we only return that information
                if (query.getString("aa") != null && !query.getString("aa").isEmpty()) {
                    projectionString += "." + aaShortName.get(query.getString("aa").toUpperCase());
                }

                // Projection is used to minimize the returned data
                Bson position = Projections.include(projectionString);
                result = proteinSubstitutionMongoDBCollection.find(transcript, position, options);
            } else {
                // Return the whole transcript data
                result = proteinSubstitutionMongoDBCollection.find(transcript, options);
            }

            if (result != null && !result.getResult().isEmpty()) {
                // Return only the inner Document, not the whole document projected
                Document document = (Document) result.getResult().get(0);
                Document aaPositionsDocument = (Document) document.get("aaPositions");
                result.setResult(Collections.singletonList(aaPositionsDocument));


                List<Score> scoreList = null;
                if (result.getNumResults() == 1) {
                    scoreList = new ArrayList<>(NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS);
                    Document proteinSubstitutionScores = (Document) result.getResult().get(0);
                    if (proteinSubstitutionScores.get("ss") != null) {
                        scoreList.add(new Score(Double.parseDouble("" + proteinSubstitutionScores.get("ss")),
                                "sift", VariantAnnotationUtils.SIFT_DESCRIPTIONS.get(proteinSubstitutionScores.get("se"))));
                    }
                    if (proteinSubstitutionScores.get("ps") != null) {
                        scoreList.add(new Score(Double.parseDouble("" + proteinSubstitutionScores.get("ps")),
                                "polyphen", VariantAnnotationUtils.POLYPHEN_DESCRIPTIONS.get(proteinSubstitutionScores.get("pe"))));
                    }
                }
                scoreResult = new QueryResult<>(result.getId(), result.getDbTime(), result.getNumResults(),
                        result.getNumTotalResults(), result.getWarningMsg(), result.getErrorMsg(), scoreList);
            }
        }
        return scoreResult;
    }

//    private List<Score> getProteinSubstitutionScores(String ensemblTranscriptId, int aaPosition, String alternativeAa) {
//        QueryResult proteinSubstitutionScoresQueryResult = getFunctionPredictionByAaChange(ensemblTranscriptId,
//                aaPosition, alternativeAa, new QueryOptions());
//        List<Score> scoreList = null;
//        if (proteinSubstitutionScoresQueryResult.getNumResults() == 1) {
//            scoreList = new ArrayList<>(NUM_PROTEIN_SUBSTITUTION_SCORE_METHODS);
//            Document proteinSubstitutionScores = (Document) proteinSubstitutionScoresQueryResult.getResult().get(0);
//            if (proteinSubstitutionScores.get("ss") != null) {
//                scoreList.add(new Score(Double.parseDouble("" + proteinSubstitutionScores.get("ss")),
//                        "sift", VariantAnnotationUtils.SIFT_DESCRIPTIONS.get(proteinSubstitutionScores.get("se"))));
//            }
//            if (proteinSubstitutionScores.get("ps") != null) {
//                scoreList.add(new Score(Double.parseDouble("" + proteinSubstitutionScores.get("ps")),
//                        "polyphen", VariantAnnotationUtils.POLYPHEN_DESCRIPTIONS.get(proteinSubstitutionScores.get("pe"))));
//            }
//        }
//        return scoreList;
//    }

    @Override
    public QueryResult<ProteinVariantAnnotation> getVariantAnnotation(String ensemblTranscriptId, int position, String aaReference,
                                                                      String aaAlternate, QueryOptions options) {
        QueryResult<ProteinVariantAnnotation> queryResult = new QueryResult<>();
        queryResult.setId(ensemblTranscriptId + "/" + position + "/" + aaAlternate);
        long dbTimeStart = System.currentTimeMillis();

        ProteinVariantAnnotation proteinVariantAnnotation = new ProteinVariantAnnotation();
        proteinVariantAnnotation.setPosition(position);
        proteinVariantAnnotation.setReference(aaReference);
        proteinVariantAnnotation.setAlternate(aaAlternate);
//        proteinVariantAnnotation.setSubstitutionScores(getProteinSubstitutionScores(ensemblTranscriptId, position, aaAlternate));
        Query query = new Query("transcript", ensemblTranscriptId).append("position", position).append("aa", aaAlternate);
        proteinVariantAnnotation.setSubstitutionScores(getSubstitutionScores(query, null).getResult());

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

    @Override
    public QueryResult<Long> update(List objectList, String field) {
        return null;
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
    public QueryResult<Entry> get(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.find(bson, null, Entry.class, options);
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public Iterator<Entry> iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {

    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createOrQuery(query, QueryParams.ACCESSION.key(), "accession", andBsonList);
        createOrQuery(query, QueryParams.NAME.key(), "name", andBsonList);
        createOrQuery(query, QueryParams.GENE.key(), "gene", andBsonList);
        createOrQuery(query, QueryParams.XREF.key(), "xref", andBsonList);
        createOrQuery(query, QueryParams.KEYWORD.key(), "keyword", andBsonList);
        createOrQuery(query, QueryParams.FEATURE_ID.key(), "feature.id", andBsonList);
        createOrQuery(query, QueryParams.FEATURE_TYPE.key(), "feature.type", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

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
}
