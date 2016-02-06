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
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.common.DNASequenceUtils;
import org.opencb.cellbase.core.common.GenomeSequenceFeature;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by imedina on 07/12/15.
 */
public class GenomeMongoDBAdaptor extends MongoDBAdaptor implements GenomeDBAdaptor {

    private MongoDBCollection genomeInfoMongoDBCollection;

    public GenomeMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        genomeInfoMongoDBCollection = mongoDataStore.getCollection("genome_info");
        mongoDBCollection = mongoDataStore.getCollection("genome_sequence");

        logger.debug("GenomeMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult getGenomeInfo(QueryOptions queryOptions) {
        return genomeInfoMongoDBCollection.find(new Document(), new QueryOptions());
    }

    @Override
    public QueryResult getChromosomeInfo(String chromosomeId, QueryOptions queryOptions) {
        if (queryOptions == null) {
            queryOptions = new QueryOptions("include", Arrays.asList("chromosomes.$"));
        } else {
            queryOptions.addToListOption("include", "chromosomes.$");
        }
        Document dbObject = new Document("chromosomes", new Document("$elemMatch", new Document("name", chromosomeId)));
        return executeQuery(chromosomeId, dbObject, queryOptions, genomeInfoMongoDBCollection);
    }

    @Override
    public QueryResult<GenomeSequenceFeature> getGenomicSequence(Query query, QueryOptions queryOptions) {
        QueryResult<Document> queryResult = nativeGet(query, queryOptions);
        List<Document> queryResultList = queryResult.getResult();

        QueryResult<GenomeSequenceFeature> result = new QueryResult<>();

        if (queryResultList != null && !queryResultList.isEmpty()) {
            Region region = Region.parseRegion(query.getString(QueryParams.REGION.key()));

            StringBuilder stringBuilder = new StringBuilder();
            for (Document document : queryResult.getResult()) {
                stringBuilder.append(document.getString("sequence"));
            }
            int startIndex = region.getStart() % MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE;
            int length = region.getEnd() - region.getStart() + 1;
            String sequence = stringBuilder.toString().substring(startIndex, startIndex + length);

            String strand = "1";
            String queryStrand= (query.getString("strand") != null) ? query.getString("strand") : "1";
            if (queryStrand.equals("-1") || queryStrand.equals("-")) {
                sequence = DNASequenceUtils.reverseComplement(sequence);
                strand = "-1";
            }

            String sequenceType = queryResult.getResult().get(0).getString("sequenceType");
            String assembly = queryResult.getResult().get(0).getString("assembly");

            result.setResult(Collections.singletonList(new GenomeSequenceFeature(
                    region.getChromosome(), region.getStart(), region.getEnd(), Integer.parseInt(strand), sequenceType, assembly, sequence)
            ));
        }

        return result;
    }

    @Override
    public QueryResult<Long> update(List objectList, String field) {
        return null;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.count(bson);
    }

    @Override
    public QueryResult distinct(Query query, String field) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.distinct(field, bson);
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public QueryResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, GenomeDBAdaptor.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE, andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

}
