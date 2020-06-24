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
import org.opencb.biodata.models.clinical.interpretation.ClinicalVariant;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.core.ClinicalDBAdaptor;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.ClinicalVariantQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.variant.ClinicalPhasedQueryManager;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.cellbase.lib.variant.annotation.hgvs.HgvsCalculator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 06/12/16.
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<ClinicalVariantQuery, ClinicalVariant> {

    private static final String PRIVATE_TRAIT_FIELD = "_traits";
    private static final String PRIVATE_CLINICAL_FIELDS = "_featureXrefs,_traits";
    private static final String SEPARATOR = ",";
    // TODO: watch out this prefix only works for ENSMBL hgvs strings!!
    private static final String PROTEIN_HGVS_PREFIX = "ENSP";
    private static ClinicalPhasedQueryManager phasedQueryManager = new ClinicalPhasedQueryManager();
    private GenomeManager genomeManager;


    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore,
                                  CellBaseConfiguration configuration) throws CellbaseException {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical_variants");
        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
        CellBaseManagerFactory cellBaseManagerFactory = new CellBaseManagerFactory(configuration);
        genomeManager = cellBaseManagerFactory.getGenomeManager(species, assembly);
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

    public Bson parseQuery(ClinicalVariantQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "region":
                        createRegionQuery(query, value, andBsonList);
                        break;
                    case "trait":
                        createTraitQuery(String.valueOf(value), andBsonList);
                        break;
                    case "id":
                        createIdQuery(query, andBsonList);

                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // TODO implement these
//        createImprecisePositionQuery(query, ClinicalDBAdaptor.QueryParams.CI_START_LEFT.key(),
//                ClinicalDBAdaptor.QueryParams.CI_START_RIGHT.key(), "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
//        createImprecisePositionQuery(query, ClinicalDBAdaptor.QueryParams.CI_END_LEFT.key(),
//                ClinicalDBAdaptor.QueryParams.CI_END_RIGHT.key(), "sv.ciEndLeft", "sv.ciEndRight", andBsonList);

        logger.info("clinical variant parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
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
        createOrQuery(query, ClinicalDBAdaptor.QueryParams.HGVS.key(), "annotation.hgvs", andBsonList);
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

    // checking accessions OR IDs
    private void createIdQuery(ClinicalVariantQuery query, List<Bson> andBsonList) {
        if (query != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(Filters.eq("annotation.id", query.getId()));
            orBsonList.add(Filters.eq("annotation.traitAssociation.id", query.getId()));
            andBsonList.add(Filters.or(orBsonList));
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

    private CellBaseDataResult getClinvarPhenotypeGeneRelations(QueryOptions queryOptions) {

        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                new Document("$exists", 1))));
//        pipeline.add(new Document("$match", new Document("clinvarSet", new Document("$exists", 1))));
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

    private CellBaseDataResult<Variant> getClinicalVariant(Variant variant,
                                                    GenomeManager genomeManager,
                                                    List<Gene> geneList,
                                                    QueryOptions options) {
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
            query = new Query();
            if (options.get(ClinicalDBAdaptor.QueryParams.CHECK_AMINO_ACID_CHANGE.key()) != null
                    && (Boolean) options.get(ClinicalDBAdaptor.QueryParams.CHECK_AMINO_ACID_CHANGE.key())
                    && genomeManager != null
                    && geneList != null
                    && !geneList.isEmpty()) {
                HgvsCalculator hgvsCalculator = new HgvsCalculator(genomeManager);
                List<String> proteinHgvsList = getProteinHgvs(hgvsCalculator.run(variant, geneList));
                // Only add the protein HGVS query if it's a protein coding variant
                if (!proteinHgvsList.isEmpty()) {
                    query.append(ClinicalDBAdaptor.QueryParams.HGVS.key(), proteinHgvsList);
                }
            }

            // If checkAminoAcidChange IS enabled but it's not a protein coding variant we still MUST raise the
            // genomic query. However, the protein hgvs query must be enough to solve the variant match if it is a
            // protein coding variant and we therefore would not need the specific genomic query
            if (query.isEmpty()) {
                query = new Query(VariantDBAdaptor.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                        .append(VariantDBAdaptor.QueryParams.START.key(), variant.getStart())
                        .append(VariantDBAdaptor.QueryParams.REFERENCE.key(), variant.getReference())
                        .append(VariantDBAdaptor.QueryParams.ALTERNATE.key(), variant.getAlternate());
            }

        }

        CellBaseDataResult<Variant> queryResult = get(query, options);
        queryResult.setId(variant.toString());

        return queryResult;
    }

    private List<String> getProteinHgvs(List<String> hgvsList) {
        List<String> proteinHgvsList = new ArrayList<>(hgvsList.size());
        for (String hgvs : hgvsList) {
            if (hgvs.startsWith(PROTEIN_HGVS_PREFIX)) {
                proteinHgvsList.add(hgvs);
            }
        }

        return proteinHgvsList;
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, QueryOptions queryOptions) {
        return this.getByVariant(variants, null, queryOptions);
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, List<Gene> geneList,
                                                   QueryOptions queryOptions) {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getClinicalVariant(variant, genomeManager, geneList, queryOptions));
        }
        if (queryOptions.get(ClinicalDBAdaptor.QueryParams.PHASE.key()) != null
                && (Boolean) queryOptions.get(ClinicalDBAdaptor.QueryParams.PHASE.key())) {
            results = phasedQueryManager.run(variants, results);

        }
        return results;
    }

    @Override
    public CellBaseIterator iterator(ClinicalVariantQuery query) {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<org.opencb.biodata.models.clinical.interpretation.ClinicalVariant> converter
                = new GenericDocumentComplexConverter<>(
                org.opencb.biodata.models.clinical.interpretation.ClinicalVariant.class);
        MongoDBIterator<ClinicalVariant> iterator = mongoDBCollection.iterator(null, bson, projection,
                converter, queryOptions);
        return new CellBaseIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<Long> count(ClinicalVariantQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(ClinicalVariantQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(ClinicalVariantQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(ClinicalVariantQuery query) {
        return null;
    }

}
