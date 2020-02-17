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
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.core.ClinicalDBAdaptor;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 06/12/16.
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements ClinicalDBAdaptor<Variant> {

    private static final String PRIVATE_TRAIT_FIELD = "_traits";
    private static final String PRIVATE_CLINICAL_FIELDS = "_featureXrefs,_traits";
    private static final String SEPARATOR = ",";


    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical_variants");

        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseDataResult<Variant> next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

//    @Override
//    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
//        return null;
//    }

    @Override
    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, List<String> fields, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(Query query) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.count(bson));
    }

    @Override
    public CellBaseDataResult distinct(Query query, String field) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bson));
    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

    @Override
    public CellBaseDataResult<Variant> get(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        logger.debug("queryOptions: {}", options.toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, null, Variant.class, parsedOptions));


    }

    @Override
    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        logger.debug("queryOptions: {}", options.toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, parsedOptions));
    }

    @Override
    public Iterator<Variant> iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options);
    }

    @Override
    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {
        Objects.requireNonNull(action);
        Iterator iterator = nativeIterator(query, options);
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    private QueryOptions parseQueryOptions(QueryOptions options, Query query) {
        if (options != null && !options.isEmpty()) {
            QueryOptions parsedQueryOptions = new QueryOptions(options);
            List<String> sortFields = options.getAsStringList(QueryOptions.SORT);
            if (sortFields != null && !sortFields.isEmpty()) {
                Document sortDocument = new Document();
                for (String field : sortFields) {
                    sortDocument.put(field, 1);
                }
                parsedQueryOptions.put(QueryOptions.SORT, sortDocument);
            }
            // TODO: Improve
            // numTotalResults cannot be enabled when including multiple clinsig values
            // search is too slow and would otherwise raise timeouts
            List<String> clinsigList = query.getAsStringList(QueryParams.CLINICALSIGNIFICANCE.key());
            if (clinsigList != null && clinsigList.size() > 1) {
                parsedQueryOptions.put(QueryOptions.SKIP_COUNT, true);
            }
            // TODO: Improve
            // numTotalResults cannot be enabled when including multiple trait values
            // search is too slow and would otherwise raise timeouts
            List<String> traitList = query.getAsStringList(QueryParams.TRAIT.key());
            if (traitList != null && traitList.size() > 1) {
                parsedQueryOptions.put(QueryOptions.SKIP_COUNT, true);
            }
            return parsedQueryOptions;
        }
        return new QueryOptions();
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, QueryParams.REGION.key(), andBsonList);
        createOrQuery(query, VariantMongoDBAdaptor.QueryParams.ID.key(), "annotation.id", andBsonList);
        createOrQuery(query, QueryParams.CHROMOSOME.key(), "chromosome", andBsonList);
        createImprecisePositionQuery(query, QueryParams.CI_START_LEFT.key(), QueryParams.CI_START_RIGHT.key(),
                "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
        createImprecisePositionQuery(query, QueryParams.CI_END_LEFT.key(), QueryParams.CI_END_RIGHT.key(),
                "sv.ciEndLeft", "sv.ciEndRight", andBsonList);
        createOrQuery(query, QueryParams.START.key(), "start", andBsonList, QueryValueType.INTEGER);
        if (query.containsKey(QueryParams.REFERENCE.key())) {
            createOrQuery(query.getAsStringList(QueryParams.REFERENCE.key()), "reference", andBsonList);
        }
        if (query.containsKey(QueryParams.ALTERNATE.key())) {
            createOrQuery(query.getAsStringList(QueryParams.ALTERNATE.key()), "alternate", andBsonList);
        }

        createOrQuery(query, QueryParams.FEATURE.key(), "_featureXrefs", andBsonList);
        createOrQuery(query, QueryParams.SO.key(),
                "annotation.consequenceTypes.sequenceOntologyTerms.name", andBsonList);
        createOrQuery(query, QueryParams.SOURCE.key(),
                "annotation.traitAssociation.source.name", andBsonList);
        createOrQuery(query, QueryParams.ACCESSION.key(), "annotation.traitAssociation.id", andBsonList);
        createOrQuery(query, QueryParams.TYPE.key(), "type", andBsonList);
        createOrQuery(query, QueryParams.CONSISTENCY_STATUS.key(),
                "annotation.traitAssociation.consistencyStatus", andBsonList);
        createOrQuery(query, QueryParams.CLINICALSIGNIFICANCE.key(),
                "annotation.traitAssociation.variantClassification.clinicalSignificance", andBsonList);
        createOrQuery(query, QueryParams.MODE_INHERITANCE.key(),
                "annotation.traitAssociation.heritableTraits.inheritanceMode", andBsonList);
        createOrQuery(query, QueryParams.ALLELE_ORIGIN.key(),
                "annotation.traitAssociation.alleleOrigin", andBsonList);

        createTraitQuery(query.getString(QueryParams.TRAIT.key()), andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createTraitQuery(String keywordString, List<Bson> andBsonList) {
        // Avoid creating a text empty query, otherwise results will never be returned
        if (StringUtils.isNotBlank(keywordString)) {
            keywordString = keywordString.toLowerCase();
            createOrQuery(Arrays.asList(keywordString.split(SEPARATOR)), PRIVATE_TRAIT_FIELD, andBsonList);
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

}
