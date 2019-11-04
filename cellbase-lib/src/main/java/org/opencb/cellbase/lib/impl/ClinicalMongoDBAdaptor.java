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

package org.opencb.cellbase.lib.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.core.variant.ClinicalPhasedQueryManager;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by fjlopez on 06/12/16.
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements ClinicalDBAdaptor<Variant> {

    private static final String PRIVATE_TRAIT_FIELD = "_traits";
    private static final String PRIVATE_CLINICAL_FIELDS = "_featureXrefs,_traits";
    private static final String SEPARATOR = ",";
    private static ClinicalPhasedQueryManager phasedQueryManager
            = new ClinicalPhasedQueryManager();

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

    @Override
    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

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

    @Override
    public CellBaseDataResult stats(Query query) {
        return null;
    }

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

    public List<CellBaseDataResult> getPhenotypeGeneRelations(Query query, QueryOptions queryOptions) {
        Set<String> sourceContent = query.getAsStringList(QueryParams.SOURCE.key()) != null
                ? new HashSet<>(query.getAsStringList(QueryParams.SOURCE.key())) : null;
        List<CellBaseDataResult> cellBaseDataResultList = new ArrayList<>();
        if (sourceContent == null || sourceContent.contains("clinvar")) {
            cellBaseDataResultList.add(getClinvarPhenotypeGeneRelations(queryOptions));

        }
        if (sourceContent == null || sourceContent.contains("gwas")) {
            cellBaseDataResultList.add(getGwasPhenotypeGeneRelations(queryOptions));
        }

        return cellBaseDataResultList;
    }

    @Override
    public CellBaseDataResult<String> getAlleleOriginLabels() {
        List<String> alleleOriginLabels = Arrays.stream(AlleleOrigin.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("allele_origin_labels", 0, Collections.emptyList(),
                alleleOriginLabels.size(), alleleOriginLabels, alleleOriginLabels.size());
    }

    @Override
    public CellBaseDataResult<String> getModeInheritanceLabels() {
        List<String> modeInheritanceLabels = Arrays.stream(ModeOfInheritance.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("mode_inheritance_labels", 0, Collections.emptyList(),
                modeInheritanceLabels.size(), modeInheritanceLabels, modeInheritanceLabels.size());
    }

    @Override
    public CellBaseDataResult<String> getClinsigLabels() {
        List<String> clinsigLabels = Arrays.stream(ClinicalSignificance.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("clinsig_labels", 0, Collections.emptyList(),
                clinsigLabels.size(), clinsigLabels, clinsigLabels.size());
    }

    @Override
    public CellBaseDataResult<String> getConsistencyLabels() {
        List<String> consistencyLabels = Arrays.stream(ConsistencyStatus.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return  new CellBaseDataResult<String>("consistency_labels", 0, Collections.emptyList(),
                consistencyLabels.size(), consistencyLabels, consistencyLabels.size());
    }

    @Override
    public CellBaseDataResult<String> getVariantTypes() {
        List<String> variantTypes = Arrays.stream(VariantType.values())
                .map((value) -> value.name()).collect(Collectors.toList());
        return new CellBaseDataResult<String>("variant_types", 0, Collections.emptyList(),
                variantTypes.size(), variantTypes, variantTypes.size());
    }

    private CellBaseDataResult getClinvarPhenotypeGeneRelations(QueryOptions queryOptions) {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                new Document("$exists", 1))));
        pipeline.add(new Document("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure"));
        pipeline.add(new Document("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship"));
        pipeline.add(new Document("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol"));
        pipeline.add(new Document("$unwind", "$clinvarSet.referenceClinVarAssertion.traitSet.trait"));
        pipeline.add(new Document("$unwind", "$clinvarSet.referenceClinVarAssertion.traitSet.trait.name"));
        Document groupFields = new Document();
        groupFields.put("_id", "$clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value");
        groupFields.put("associatedGenes",
                new Document("$addToSet",
                        "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value"));
        pipeline.add(new Document("$group", groupFields));
        Document fields = new Document();
        fields.put("_id", 0);
        fields.put("phenotype", "$_id");
        fields.put("associatedGenes", 1);
        pipeline.add(new Document("$project", fields));

        return executeAggregation2("", pipeline, queryOptions);

    }

    private CellBaseDataResult getGwasPhenotypeGeneRelations(QueryOptions queryOptions) {
        List<Bson> pipeline = new ArrayList<>();
        // Select only GWAS documents
        pipeline.add(new Document("$match", new Document("snpIdCurrent", new Document("$exists", 1))));
        pipeline.add(new Document("$unwind", "$studies"));
        pipeline.add(new Document("$unwind", "$studies.traits"));
        Document groupFields = new Document();
        groupFields.put("_id", "$studies.traits.diseaseTrait");
        groupFields.put("associatedGenes", new Document("$addToSet", "$reportedGenes"));
        pipeline.add(new Document("$group", groupFields));
        Document fields = new Document();
        fields.put("_id", 0);
        fields.put("phenotype", "$_id");
        fields.put("associatedGenes", 1);
        pipeline.add(new Document("$project", fields));

        return executeAggregation2("", pipeline, queryOptions);
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, QueryOptions queryOptions) {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant : variants) {
            results.add(getByVariant(variant, queryOptions));
        }

        if (queryOptions.get(QueryParams.PHASE.key()) != null && (Boolean) queryOptions.get(QueryParams.PHASE.key())) {
            results = phasedQueryManager.run(variants, results);

        }
        return results;
    }
}
