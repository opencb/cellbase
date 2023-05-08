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

import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.clinical.interpretation.ClinicalVariant;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.ClinicalVariantQuery;
import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.variant.ClinicalPhasedQueryManager;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.cellbase.lib.variant.hgvs.HgvsCalculator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 06/12/16.
 */
public class ClinicalMongoDBAdaptor extends CellBaseDBAdaptor implements CellBaseCoreDBAdaptor<ClinicalVariantQuery, ClinicalVariant> {

    private static final String PRIVATE_TRAIT_FIELD = "_traits";
    private static final String PRIVATE_CLINICAL_FIELDS = "_featureXrefs,_traits";
    private static final String SEPARATOR = ",";
    // TODO: watch out this prefix only works for ENSMBL hgvs strings!!
    private static final String PROTEIN_HGVS_PREFIX = "ENSP";
    private static ClinicalPhasedQueryManager phasedQueryManager = new ClinicalPhasedQueryManager();

    private GenomeManager genomeManager;

    public ClinicalMongoDBAdaptor(MongoDataStore mongoDataStore, GenomeManager genomeManager) throws CellBaseException {
        super(mongoDataStore);

        this.genomeManager = genomeManager;

        init();
    }

    private void init() {
        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap("clinical_variants");
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

    public CellBaseDataResult<Long> count(Query query) throws CellBaseException {
        Bson bson = parseQuery(query);

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease,
                (Integer) query.getOrDefault(AbstractQuery.DATA_RELEASE, 0));
        return new CellBaseDataResult<>(mongoDBCollection.count(bson));
    }

    public CellBaseDataResult distinct(Query query, String field) throws CellBaseException {
        Bson bson = parseQuery(query);

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease,
                (Integer) query.getOrDefault(AbstractQuery.DATA_RELEASE, 0));
        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bson));
    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

    public CellBaseDataResult<Variant> get(Query query, QueryOptions options) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument().toJson());
        logger.debug("queryOptions: {}", options.toJson());

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease,
                (Integer) query.getOrDefault(AbstractQuery.DATA_RELEASE, 0));
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, null, Variant.class, parsedOptions));
    }

    public CellBaseDataResult nativeGet(Query query, QueryOptions options) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument().toJson());
        logger.debug("queryOptions: {}", options.toJson());

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease,
                (Integer) query.getOrDefault(AbstractQuery.DATA_RELEASE, 0));
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, parsedOptions));
    }

    public Iterator<Variant> iterator(Query query, QueryOptions options) {
        return null;
    }

    public Iterator nativeIterator(Query query, QueryOptions options) throws CellBaseException {
        Bson bson = parseQuery(query);

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease,
                (Integer) query.getOrDefault(AbstractQuery.DATA_RELEASE, 0));
        return mongoDBCollection.nativeQuery().find(bson, options);
    }

    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) throws CellBaseException {
        Objects.requireNonNull(action);
        Iterator iterator = nativeIterator(query, options);
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    /**
     * This method has been added to make CB 5.1 compatible with CB 5.2 (which adds gwas).
     * TODO This must be removed in CB 6
     * @param queryOptions
     * @return queryOptions modified
     */
    private QueryOptions excludeAnnotationGwas(QueryOptions queryOptions) {
        if (queryOptions == null) {
            queryOptions = QueryOptions.empty();
        }
        if (!queryOptions.containsKey(QueryOptions.INCLUDE)) {
            if (queryOptions.containsKey(QueryOptions.EXCLUDE)) {
                String excludeString = queryOptions.getString(QueryOptions.EXCLUDE);
                queryOptions.put(QueryOptions.EXCLUDE, excludeString + ",annotation.gwas");
            } else {
                queryOptions.put(QueryOptions.EXCLUDE, "annotation.gwas");
            }
        }
        return queryOptions;
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
            List<String> clinsigList = query.getAsStringList(ParamConstants.QueryParams.CLINICALSIGNIFICANCE.key());
            if (clinsigList != null && clinsigList.size() > 1) {
                parsedQueryOptions.put(QueryOptions.SKIP_COUNT, true);
            }
            // TODO: Improve
            // numTotalResults cannot be enabled when including multiple trait values
            // search is too slow and would otherwise raise timeouts
            List<String> traitList = query.getAsStringList(ParamConstants.QueryParams.TRAIT.key());
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
                    case "token":
                    case "dataRelease":
                        // Do nothing
                        break;
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
//        createImprecisePositionQuery(query, ParamConstants.QueryParams.CI_START_LEFT.key(),
//                ParamConstants.QueryParams.CI_START_RIGHT.key(), "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
//        createImprecisePositionQuery(query, ParamConstants.QueryParams.CI_END_LEFT.key(),
//                ParamConstants.QueryParams.CI_END_RIGHT.key(), "sv.ciEndLeft", "sv.ciEndRight", andBsonList);

        logger.info("clinical variant parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, ParamConstants.QueryParams.REGION.key(), andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.ID.key(), "annotation.id", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.CHROMOSOME.key(), "chromosome", andBsonList);
        createImprecisePositionQuery(query, ParamConstants.QueryParams.CI_START_LEFT.key(),
                ParamConstants.QueryParams.CI_START_RIGHT.key(), "sv.ciStartLeft", "sv.ciStartRight", andBsonList);
        createImprecisePositionQuery(query, ParamConstants.QueryParams.CI_END_LEFT.key(),
                ParamConstants.QueryParams.CI_END_RIGHT.key(), "sv.ciEndLeft", "sv.ciEndRight", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.START.key(), "start", andBsonList, QueryValueType.INTEGER);
        if (query.containsKey(ParamConstants.QueryParams.REFERENCE.key())) {
            createOrQuery(query.getAsStringList(ParamConstants.QueryParams.REFERENCE.key()), "reference", andBsonList);
        }
        if (query.containsKey(ParamConstants.QueryParams.ALTERNATE.key())) {
            createOrQuery(query.getAsStringList(ParamConstants.QueryParams.ALTERNATE.key()), "alternate", andBsonList);
        }

        createOrQuery(query, ParamConstants.QueryParams.FEATURE.key(), "_featureXrefs", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.SO.key(),
                "annotation.consequenceTypes.sequenceOntologyTerms.name", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.SOURCE.key(),
                "annotation.traitAssociation.source.name", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.ACCESSION.key(), "annotation.traitAssociation.id", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.TYPE.key(), "type", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.CONSISTENCY_STATUS.key(),
                "annotation.traitAssociation.consistencyStatus", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.CLINICALSIGNIFICANCE.key(),
                "annotation.traitAssociation.variantClassification.clinicalSignificance", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.MODE_INHERITANCE.key(),
                "annotation.traitAssociation.heritableTraits.inheritanceMode", andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.ALLELE_ORIGIN.key(),
                "annotation.traitAssociation.alleleOrigin", andBsonList);

        createTraitQuery(query.getString(ParamConstants.QueryParams.TRAIT.key()), andBsonList);
        createOrQuery(query, ParamConstants.QueryParams.HGVS.key(), "annotation.hgvs", andBsonList);
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

    private CellBaseDataResult getClinvarPhenotypeGeneRelations(QueryOptions queryOptions, int dataRelease) throws CellBaseException {

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

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        return executeAggregation2("", pipeline, queryOptions, mongoDBCollection);

    }

    private CellBaseDataResult getGwasPhenotypeGeneRelations(QueryOptions queryOptions, int dataRelease) throws CellBaseException {

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

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        return executeAggregation2("", pipeline, queryOptions, mongoDBCollection);
    }

    private CellBaseDataResult<Variant> getClinicalVariant(Variant variant, GenomeManager genomeManager, List<Gene> geneList,
                                                           QueryOptions options, int dataRelease) throws CellBaseException {
        Query query;
        if (VariantType.CNV.equals(variant.getType())) {
            query = new Query(ParamConstants.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                    .append(ParamConstants.QueryParams.CI_START_LEFT.key(), variant.getSv().getCiStartLeft())
                    .append(ParamConstants.QueryParams.CI_START_RIGHT.key(), variant.getSv().getCiStartRight())
                    .append(ParamConstants.QueryParams.CI_END_LEFT.key(), variant.getSv().getCiEndLeft())
                    .append(ParamConstants.QueryParams.CI_END_RIGHT.key(), variant.getSv().getCiEndRight())
                    .append(ParamConstants.QueryParams.REFERENCE.key(), variant.getReference())
                    .append(ParamConstants.QueryParams.ALTERNATE.key(), variant.getAlternate());
        } else {
            query = new Query();
            if (options.get(ParamConstants.QueryParams.CHECK_AMINO_ACID_CHANGE.key()) != null
                    && (Boolean) options.get(ParamConstants.QueryParams.CHECK_AMINO_ACID_CHANGE.key())
                    && genomeManager != null
                    && geneList != null
                    && !geneList.isEmpty()) {
                HgvsCalculator hgvsCalculator = new HgvsCalculator(genomeManager, dataRelease);
                List<String> proteinHgvsList = getProteinHgvs(hgvsCalculator.run(variant, geneList));
                // Only add the protein HGVS query if it's a protein coding variant
                if (!proteinHgvsList.isEmpty()) {
                    query.append(ParamConstants.QueryParams.HGVS.key(), proteinHgvsList);
                }
            }

            // If checkAminoAcidChange IS enabled but it's not a protein coding variant we still MUST raise the
            // genomic query. However, the protein hgvs query must be enough to solve the variant match if it is a
            // protein coding variant and we therefore would not need the specific genomic query
            if (query.isEmpty()) {
                query = new Query(ParamConstants.QueryParams.CHROMOSOME.key(), variant.getChromosome())
                        .append(ParamConstants.QueryParams.START.key(), variant.getStart())
                        .append(ParamConstants.QueryParams.REFERENCE.key(), variant.getReference())
                        .append(ParamConstants.QueryParams.ALTERNATE.key(), variant.getAlternate());
            }

            // Add data release to query
            if (!query.containsKey(AbstractQuery.DATA_RELEASE)) {
                query.put(AbstractQuery.DATA_RELEASE, dataRelease);
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

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, QueryOptions queryOptions, int dataRelease)
            throws CellBaseException {
        return this.getByVariant(variants, null, queryOptions, dataRelease);
    }

    public List<CellBaseDataResult<Variant>> getByVariant(List<Variant> variants, List<Gene> geneList, QueryOptions queryOptions,
                                                          int dataRelease) throws CellBaseException {
        List<CellBaseDataResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getClinicalVariant(variant, genomeManager, geneList, queryOptions, dataRelease));
        }
        if (queryOptions.get(ParamConstants.QueryParams.PHASE.key()) != null
                && (Boolean) queryOptions.get(ParamConstants.QueryParams.PHASE.key())) {
            results = phasedQueryManager.run(variants, results);

        }
        return results;
    }

    @Override
    public CellBaseIterator iterator(ClinicalVariantQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<Variant> converter = new GenericDocumentComplexConverter<>(Variant.class);

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        MongoDBIterator<Variant> iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<ClinicalVariant>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease,
                                                          String token) {
        return null;
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
