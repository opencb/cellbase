package org.opencb.cellbase.lib.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.ClinicalPhasedQueryManager;
import org.opencb.cellbase.core.variant.annotation.hgvs.HgvsCalculator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
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
    // TODO: watch out this prefix only works for ENSMBL hgvs strings!!
    private static final String PROTEIN_HGVS_PREFIX = "ENSP";
    private static ClinicalPhasedQueryManager phasedQueryManager = new ClinicalPhasedQueryManager();
    private GenomeDBAdaptor genomeDBAdaptor;

    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore,
                                  GenomeDBAdaptor genomeDBAdaptor) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical_variants");
        this.genomeDBAdaptor = genomeDBAdaptor;
        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult<Variant> next(Query query, QueryOptions options) {
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
//        Bson bsonQuery = parseQuery(query);
//        return groupBy(bsonQuery, field, "name", options);
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, List<String> fields, QueryOptions options) {
//        Bson bsonQuery = parseQuery(query);
//        return groupBy(bsonQuery, fields, "name", options);
        return null;
    }

    @Override
    public QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult<Long> update(List objectList, String field, String[] innerFields) {
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
    public QueryResult<Variant> get(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.info("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        logger.debug("queryOptions: {}", options.toJson());
        return mongoDBCollection.find(bson, null, Variant.class, parsedOptions);
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        logger.debug("queryOptions: {}", options.toJson());
        return mongoDBCollection.find(bson, parsedOptions);
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
        createOrQuery(query, QueryParams.HGVS.key(), "annotation.hgvs", andBsonList);

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

    public List<QueryResult> getPhenotypeGeneRelations(Query query, QueryOptions queryOptions) {

        Set<String> sourceContent = query.getAsStringList(QueryParams.SOURCE.key()) != null
                ? new HashSet<>(query.getAsStringList(QueryParams.SOURCE.key())) : null;
        List<QueryResult> queryResultList = new ArrayList<>();
        if (sourceContent == null || sourceContent.contains("clinvar")) {
            queryResultList.add(getClinvarPhenotypeGeneRelations(queryOptions));

        }
        if (sourceContent == null || sourceContent.contains("gwas")) {
            queryResultList.add(getGwasPhenotypeGeneRelations(queryOptions));
        }

        return queryResultList;
    }

    @Override
    public QueryResult<String> getAlleleOriginLabels() {

        List<String> alleleOriginLabels = Arrays.stream(AlleleOrigin.values())
                .map((value) -> value.name()).collect(Collectors.toList());

        QueryResult<String> queryResult = new QueryResult<String>("allele_origin_labels", 0,
                alleleOriginLabels.size(), alleleOriginLabels.size(), null, null,
                alleleOriginLabels);

        return queryResult;

    }

    @Override
    public QueryResult<String> getModeInheritanceLabels() {

        List<String> modeInheritanceLabels = Arrays.stream(ModeOfInheritance.values())
                .map((value) -> value.name()).collect(Collectors.toList());

        QueryResult<String> queryResult = new QueryResult<String>("mode_inheritance_labels", 0,
                modeInheritanceLabels.size(), modeInheritanceLabels.size(), null, null,
                modeInheritanceLabels);

        return queryResult;

    }

    @Override
    public QueryResult<String> getClinsigLabels() {

        List<String> clinsigLabels = Arrays.stream(ClinicalSignificance.values())
                .map((value) -> value.name()).collect(Collectors.toList());

        QueryResult<String> queryResult = new QueryResult<String>("clinsig_labels", 0,
                clinsigLabels.size(), clinsigLabels.size(), null, null,
                clinsigLabels);

        return queryResult;

    }

    @Override
    public QueryResult<String> getConsistencyLabels() {

        List<String> consistencyLabels = Arrays.stream(ConsistencyStatus.values())
                .map((value) -> value.name()).collect(Collectors.toList());

        QueryResult<String> queryResult = new QueryResult<String>("consistency_labels", 0,
                consistencyLabels.size(), consistencyLabels.size(), null, null,
                consistencyLabels);

        return queryResult;

    }

    @Override
    public QueryResult<String> getVariantTypes() {

        List<String> variantTypes = Arrays.stream(VariantType.values())
                .map((value) -> value.name()).collect(Collectors.toList());

        QueryResult<String> queryResult = new QueryResult<String>("variant_types", 0,
                variantTypes.size(), variantTypes.size(), null, null,
                variantTypes);

        return queryResult;

    }

    private QueryResult getClinvarPhenotypeGeneRelations(QueryOptions queryOptions) {

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

    private QueryResult getGwasPhenotypeGeneRelations(QueryOptions queryOptions) {

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

    private QueryResult<Variant> getClinicalVariant(Variant variant,
                                                    GenomeDBAdaptor genomeDBAdaptor,
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
                if (options.get(QueryParams.CHECK_AMINO_ACID_CHANGE.key()) != null
                    && (Boolean) options.get(QueryParams.CHECK_AMINO_ACID_CHANGE.key())
                    && genomeDBAdaptor != null
                    && geneList != null
                    && !geneList.isEmpty()) {
                HgvsCalculator hgvsCalculator = new HgvsCalculator(genomeDBAdaptor);
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

        QueryResult<Variant> queryResult = get(query, options);
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

    public List<QueryResult<Variant>> getByVariant(List<Variant> variants, QueryOptions queryOptions) {
        return this.getByVariant(variants, null, queryOptions);
    }

    public List<QueryResult<Variant>> getByVariant(List<Variant> variants, List<Gene> geneList,
                                                   QueryOptions queryOptions) {
        List<QueryResult<Variant>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getClinicalVariant(variant, genomeDBAdaptor, geneList, queryOptions));
        }
        if (queryOptions.get(QueryParams.PHASE.key()) != null && (Boolean) queryOptions.get(QueryParams.PHASE.key())) {
            results = phasedQueryManager.run(variants, results);

        }
        return results;
    }
}
