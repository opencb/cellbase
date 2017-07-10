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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by imedina on 25/11/15.
 */
public class GeneMongoDBAdaptor extends MongoDBAdaptor implements GeneDBAdaptor<Gene> {

    public GeneMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("GeneMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult<Gene> next(Query query, QueryOptions options) {
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
        return null;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        Bson bsonDocument = parseQuery(query);
        return mongoDBCollection.count(bsonDocument);
    }

    @Override
    public QueryResult<String> distinct(Query query, String field) {
        Bson bsonDocument = parseQuery(query);
        return mongoDBCollection.distinct(field, bsonDocument);
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult<Gene> get(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        options = addPrivateExcludeOptions(options);

        if (postDBFilteringParametersEnabled(query)) {
            QueryResult<Document> nativeQueryResult = postDBFiltering(query, mongoDBCollection.find(bson, options));
            QueryResult<Gene> queryResult = new QueryResult<>(nativeQueryResult.getId(),
                    nativeQueryResult.getDbTime(), nativeQueryResult.getNumResults(),
                    nativeQueryResult.getNumTotalResults(), nativeQueryResult.getWarningMsg(),
                    nativeQueryResult.getErrorMsg(), null);

            // Now we need to convert MongoDB Documents to Gene objects
            // TODO: maybe we could query Genes in the first stage
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            ObjectWriter objectWriter = jsonObjectMapper.writer();
            queryResult.setResult(nativeQueryResult.getResult().stream()
                    .map(document -> {
                        try {
                            return this.objectMapper.readValue(objectWriter.writeValueAsString(document), Gene.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }).collect(Collectors.toList()));
            return queryResult;
        } else {
            logger.info("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
            return mongoDBCollection.find(bson, null, Gene.class, options);
        }
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return postDBFiltering(query, mongoDBCollection.find(bson, options));
    }

    @Override
    public Iterator<Gene> iterator(Query query, QueryOptions options) {
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

    @Override
    public QueryResult startsWith(String id, QueryOptions options) {
        Bson regex = Filters.regex("transcripts.xrefs.id", Pattern.compile("^" + id));
        Bson include = Projections.include("id", "name", "chromosome", "start", "end");
        return mongoDBCollection.find(regex, include, options);
    }

    @Override
    public QueryResult getRegulatoryElements(Query query, QueryOptions queryOptions) {
        Bson bson = parseQuery(query);
        QueryResult<Document> queryResult = null;
        QueryResult<Document> gene = mongoDBCollection.find(bson, new QueryOptions(QueryOptions.INCLUDE, "chromosome,start,end"));
        if (gene != null) {
            MongoDBCollection regulatoryRegionCollection = mongoDataStore.getCollection("regulatory_region");
            for (Document document : gene.getResult()) {
                Bson eq = Filters.eq("chromosome", document.getString("chromosome"));
                Bson lte = Filters.lte("start", document.getInteger("end", Integer.MAX_VALUE));
                Bson gte = Filters.gte("end", document.getInteger("start", 1));
                queryResult = regulatoryRegionCollection.find(Filters.and(eq, lte, gte), queryOptions);
            }
        }
        return queryResult;
    }

    @Override
    public QueryResult getTfbs(Query query, QueryOptions queryOptions) {
        Bson bsonQuery = parseQuery(query);
        Bson match = Aggregates.match(bsonQuery);

        // We parse user's exclude options, ONLY _id can be added if exists
        Bson includeAndExclude;
        Bson exclude = null;
        if (queryOptions != null && queryOptions.containsKey("exclude")) {
            List<String> stringList = queryOptions.getAsStringList("exclude");
            if (stringList.contains("_id")) {
                exclude = Aggregates.project(Projections.exclude("_id"));
            }
        }
        if (exclude != null) {
            includeAndExclude = Aggregates.project(Projections.fields(Projections.excludeId(), Projections.include("transcripts.tfbs")));
        } else {
            includeAndExclude = Aggregates.project(Projections.include("transcripts.tfbs"));
        }

        Bson unwind = Aggregates.unwind("$transcripts");
        Bson unwind2 = Aggregates.unwind("$transcripts.tfbs");

        // This project the three fields of Xref to the top of the object
        Document document = new Document("tfName", "$transcripts.tfbs.tfName");
        document.put("pwm", "$transcripts.tfbs.pwm");
        document.put("chromosome", "$transcripts.tfbs.chromosome");
        document.put("start", "$transcripts.tfbs.start");
        document.put("end", "$transcripts.tfbs.end");
        document.put("strand", "$transcripts.tfbs.strand");
        document.put("relativeStart", "$transcripts.tfbs.relativeStart");
        document.put("relativeEnd", "$transcripts.tfbs.relativeEnd");
        document.put("score", "$transcripts.tfbs.score");
        Bson project = Aggregates.project(document);

        return mongoDBCollection.aggregate(Arrays.asList(match, includeAndExclude, unwind, unwind2, project), queryOptions);
    }

    @Override
    public QueryResult<String> getBiotypes(Query query) {
        return distinct(query, "biotypes");
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, QueryParams.REGION.key(), MongoDBCollectionConfiguration.GENE_CHUNK_SIZE, andBsonList);

        createOrQuery(query, QueryParams.ID.key(), "id", andBsonList);
        createOrQuery(query, QueryParams.NAME.key(), "name", andBsonList);
        createOrQuery(query, QueryParams.BIOTYPE.key(), "biotype", andBsonList);
        createOrQuery(query, QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);

        createOrQuery(query, QueryParams.TRANSCRIPT_ID.key(), "transcripts.id", andBsonList);
        createOrQuery(query, QueryParams.TRANSCRIPT_NAME.key(), "transcripts.name", andBsonList);
        createOrQuery(query, QueryParams.TRANSCRIPT_BIOTYPE.key(), "transcripts.biotype", andBsonList);
        createOrQuery(query, QueryParams.TRANSCRIPT_ANNOTATION_FLAGS.key(), "transcripts.annotationFlags", andBsonList);

        createOrQuery(query, QueryParams.TFBS_NAME.key(), "transcripts.tfbs.name", andBsonList);
        createOrQuery(query, QueryParams.ANNOTATION_DISEASE_ID.key(), "annotation.diseases.id", andBsonList);
        createOrQuery(query, QueryParams.ANNOTATION_DISEASE_NAME.key(), "annotation.diseases.name", andBsonList);
        createOrQuery(query, QueryParams.ANNOTATION_EXPRESSION_GENE.key(), "annotation.expression.geneName", andBsonList);

//        createOrQuery(query, QueryParams.ANNOTATION_EXPRESSION_TISSUE.key(), "annotation.expression.factorValue", andBsonList);
        createOrQuery(query, QueryParams.ANNOTATION_DRUGS_NAME.key(), "annotation.drugs.drugName", andBsonList);
        createOrQuery(query, QueryParams.ANNOTATION_DRUGS_GENE.key(), "annotation.drugs.geneName", andBsonList);

  //      createExpressionTissueQuery(query, QueryParams.ANNOTATION_EXPRESSION_TISSUE.key(), andBsonList);
        //      createExpressionValueQuery(query, QueryParams.ANNOTATION_EXPRESSION_VALUE.key(), andBsonList);

        createExpressionQuery(query, andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createExpressionQuery(Query query, List<Bson> andBsonList) {
        if (query != null) {
            String tissue = query.getString(QueryParams.ANNOTATION_EXPRESSION_TISSUE.key());
            if (tissue != null && !tissue.isEmpty()) {
                String value = query.getString(QueryParams.ANNOTATION_EXPRESSION_VALUE.key());
                if (value != null && !value.isEmpty()) {
                    Document elemMatchDocument = new Document();
                    andBsonList.add(Filters.elemMatch("annotation.expression",
                            Filters.and(Filters.regex("factorValue", "(.)*" + tissue + "(.)*", "i"), Filters.eq("expression", value))));
                }

//                if (tissueList.size() == 1) {
//                    andBsonList.add(Filters.regex("annotation.expression.factorValue", "(.)*" + tissueList.get(0) + "(.)*", "i"));
//                } else {
//                    List<Bson> orBsonList = new ArrayList<>(tissueList.size());
//                    for (String tissue : tissueList) {
//                        orBsonList.add(Filters.regex("annotation.expression.factorValue", "(.)*" + tissue + "(.)*", "i"));
//                    }
//                    andBsonList.add(Filters.or(orBsonList));
//                }
            }
        }
    }

    private void createExpressionTissueQuery(Query query, String queryParam, List<Bson> andBsonList) {
        if (query != null) {
            List<String> tissueList = query.getAsStringList(queryParam);
            if (tissueList != null && !tissueList.isEmpty()) {
                if (tissueList.size() == 1) {
                    andBsonList.add(Filters.regex("annotation.expression.factorValue", "(.)*" + tissueList.get(0) + "(.)*", "i"));
                } else {
                    List<Bson> orBsonList = new ArrayList<>(tissueList.size());
                    for (String tissue : tissueList) {
                        orBsonList.add(Filters.regex("annotation.expression.factorValue", "(.)*" + tissue + "(.)*", "i"));
                    }
                    andBsonList.add(Filters.or(orBsonList));
                }
            }
        }
    }

    private void createExpressionValueQuery(Query query, String queryParam, List<Bson> andBsonList) {
        if (query != null) {
            String value = query.getString(queryParam).toUpperCase();
            if (value != null && !value.isEmpty()) {
                andBsonList.add(Filters.eq("annotation.expression.expression", value));
            }
        }
    }

    private Boolean postDBFilteringParametersEnabled(Query query) {
        return StringUtils.isNotEmpty(query.getString("transcripts.annotationFlags"));
    }

    private QueryResult<Document> postDBFiltering(Query query, QueryResult<Document> documentQueryResult) {
        if (StringUtils.isNotEmpty(query.getString("transcripts.annotationFlags"))) {
            Set<String> flags = new HashSet<>(Arrays.asList(query.getString("transcripts.annotationFlags").split(",")));
            List<Document> documents = documentQueryResult.getResult();
            for (Document document : documents) {
                ArrayList<Document> transcripts = document.get("transcripts", ArrayList.class);
                ArrayList<Document> matchedTranscripts = new ArrayList<>();
                for (Document transcript : transcripts) {
                    ArrayList annotationFlags = transcript.get("annotationFlags", ArrayList.class);
                    if (annotationFlags != null && annotationFlags.size() > 0) {
                        if (CollectionUtils.containsAny(annotationFlags, flags)) {
                            matchedTranscripts.add(transcript);
                        }
                    }
                }
                document.put("transcripts", matchedTranscripts);
            }
            documentQueryResult.setResult(documents);
        }
        return documentQueryResult;
    }

}
