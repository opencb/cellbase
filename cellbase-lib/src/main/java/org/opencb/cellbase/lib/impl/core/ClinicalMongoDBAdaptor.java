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
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.core.ClinicalDBAdaptor;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 06/12/16.
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor {

    private static final String PRIVATE_TRAIT_FIELD = "_traits";
    private static final String PRIVATE_CLINICAL_FIELDS = "_featureXrefs,_traits";
    private static final String SEPARATOR = ",";

    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical_variants");

        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    public CellBaseDataResult<Variant> next(Query query, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

//    @Override
//    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
//        return null;
//    }

    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult groupBy(Query query, List<String> fields, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        return null;
    }

    public CellBaseDataResult<Long> count(Query query) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.count(bson));
    }

    public CellBaseDataResult distinct(Query query, String field) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bson));
    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

    public CellBaseDataResult<Variant> get(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        logger.debug("queryOptions: {}", options.toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, null, Variant.class, parsedOptions));
    }

    public CellBaseDataResult nativeGet(AbstractQuery query) {
        return new CellBaseDataResult<>(mongoDBCollection.find(new BsonDocument(), null));
    }

    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        logger.debug("queryOptions: {}", options.toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, parsedOptions));
    }

    public Iterator<Variant> iterator(Query query, QueryOptions options) {
        return null;
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
            List<String> clinsigList = query.getAsStringList(ClinicalDBAdaptor.QueryParams.CLINICALSIGNIFICANCE.key());
            if (clinsigList != null && clinsigList.size() > 1) {
                parsedQueryOptions.put(QueryOptions.SKIP_COUNT, true);
            }
            // TODO: Improve
            // numTotalResults cannot be enabled when including multiple trait values
            // search is too slow and would otherwise raise timeouts
            List<String> traitList = query.getAsStringList(ClinicalDBAdaptor.QueryParams.TRAIT.key());
            if (traitList != null && traitList.size() > 1) {
                parsedQueryOptions.put(QueryOptions.SKIP_COUNT, true);
            }
            return parsedQueryOptions;
        }
        return new QueryOptions();
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, ClinicalDBAdaptor.QueryParams.REGION.key(), andBsonList);
        createOrQuery(query, VariantDBAdaptor.QueryParams.ID.key(), "annotation.id", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.CHROMOSOME.key(), "chromosome", andBsonList);
        createImprecisePositionQuery(query, ClinicalDBAdaptor.QueryParams.CI_START_LEFT.key(),
                ClinicalDBAdaptor.QueryParams.CI_START_RIGHT.key(), "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
        createImprecisePositionQuery(query, ClinicalDBAdaptor.QueryParams.CI_END_LEFT.key(),
                ClinicalDBAdaptor.QueryParams.CI_END_RIGHT.key(), "sv.ciEndLeft", "sv.ciEndRight", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.START.key(), "start", andBsonList, QueryValueType.INTEGER);
        if (query.containsKey(ClinicalDBAdaptor.QueryParams.REFERENCE.key())) {
            createOrQuery(query.getAsStringList(ClinicalDBAdaptor.QueryParams.REFERENCE.key()), "reference", andBsonList);
        }
        if (query.containsKey(ClinicalDBAdaptor.QueryParams.ALTERNATE.key())) {
            createOrQuery(query.getAsStringList(ClinicalDBAdaptor.QueryParams.ALTERNATE.key()), "alternate", andBsonList);
        }

        createOrQuery(query, ClinicalDBAdaptor.QueryParams.FEATURE.key(), "_featureXrefs", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.SO.key(),
                "annotation.consequenceTypes.sequenceOntologyTerms.name", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.SOURCE.key(),
                "annotation.traitAssociation.source.name", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.ACCESSION.key(), "annotation.traitAssociation.id", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.TYPE.key(), "type", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.CONSISTENCY_STATUS.key(),
                "annotation.traitAssociation.consistencyStatus", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.CLINICALSIGNIFICANCE.key(),
                "annotation.traitAssociation.variantClassification.clinicalSignificance", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.MODE_INHERITANCE.key(),
                "annotation.traitAssociation.heritableTraits.inheritanceMode", andBsonList);
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.ALLELE_ORIGIN.key(),
                "annotation.traitAssociation.alleleOrigin", andBsonList);

        createTraitQuery(query.getString(ClinicalDBAdaptor.QueryParams.TRAIT.key()), andBsonList);

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

    public CellBaseDataResult<Variant> getByVariant(Variant variant, QueryOptions options) {
        Query query;
        if (VariantType.CNV.equals(variant.getType())) {
            query = new Query(VariantDBAdaptor.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                    .append(VariantDBAdaptor.QueryParams.CI_START_LEFT.key(), variant.getSv().getCiStartLeft())
                    .append(VariantDBAdaptor.QueryParams.CI_START_RIGHT.key(), variant.getSv().getCiStartRight())
                    .append(VariantDBAdaptor.QueryParams.CI_END_LEFT.key(), variant.getSv().getCiEndLeft())
                    .append(VariantDBAdaptor.QueryParams.CI_END_RIGHT.key(), variant.getSv().getCiEndRight())
                    .append(VariantDBAdaptor.QueryParams.REFERENCE.key(), variant.getReference())
                    .append(VariantDBAdaptor.QueryParams.ALTERNATE.key(), variant.getAlternate());
        } else {
            query = new Query(VariantDBAdaptor.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                    .append(VariantDBAdaptor.QueryParams.START.key(), variant.getStart())
                    .append(VariantDBAdaptor.QueryParams.REFERENCE.key(), variant.getReference())
                    .append(VariantDBAdaptor.QueryParams.ALTERNATE.key(), variant.getAlternate());
        }
        CellBaseDataResult<Variant> queryResult = get(query, options);
        queryResult.setId(variant.toString());

        return queryResult;
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, QueryOptions options) {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getByVariant(variant, options));
        }
        return results;
    }

    @Override
    public CellBaseIterator iterator(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(AbstractQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(AbstractQuery query) {
        return null;
    }

}
