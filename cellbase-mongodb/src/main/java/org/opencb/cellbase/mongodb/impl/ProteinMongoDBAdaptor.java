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

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.ProteinDBAdaptor;
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
    public QueryResult getSubstitutionScores(Query query, QueryOptions options) {
        QueryResult result = null;

        // Ensembl transcript id is needed for this collection
        if (query.getString("transcript") != null) {
            Bson transcript = Filters.eq("transcriptId", query.getString("transcript"));

            // If position and aa change are provided we create a 'projection' to return only the required data from the database
            if (query.get("position") != null && query.getInt("position", 0) != 0) {
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
            }
        }
        return result;
    }

    @Override
    public QueryResult<Entry> next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeNext(Query query, QueryOptions options) {
        return null;
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
    public QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        if (query.getString("region") != null) {
            Region region = Region.parseRegion(query.getString("region"));
            Bson bsonDocument = parseQuery(query);
            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
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
        return null;
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

}
