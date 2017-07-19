package org.opencb.cellbase.lib.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
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

    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical_variants");

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
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
        return mongoDBCollection.find(bson, null, Variant.class, parsedOptions);
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options, query);
        parsedOptions = addPrivateExcludeOptions(parsedOptions, PRIVATE_CLINICAL_FIELDS);
        logger.info("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()).toJson());
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
        List<String> sortFields = options.getAsStringList(QueryOptions.SORT);
        if (sortFields != null) {
            Document sortDocument = new Document();
            for (String field : sortFields) {
                sortDocument.put(field, 1);
            }
            options.put(QueryOptions.SORT, sortDocument);
        }
        // TODO: Improve
        // numTotalResults cannot be enabled when searching by trait keywords
        // regex search is too slow and would otherwise raise timeouts
        if (StringUtils.isNotBlank(query.getString(QueryParams.TRAIT.key()))) {
            options.put(QueryOptions.SKIP_COUNT, true);
        } else {
            // TODO: Improve
            // numTotalResults cannot be enabled when including multiple clinsig values
            // search is too slow and would otherwise raise timeouts
            List<String> clinsigList = query.getAsStringList(QueryParams.CLINICALSIGNIFICANCE.key());
            if (clinsigList != null && clinsigList.size() > 1) {
                options.put(QueryOptions.SKIP_COUNT, true);
            }
        }

        return options;
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
//        for (String keyword : keywords) {
//            andBsonList.add(Filters.regex(PRIVATE_TRAIT_FIELD, keyword, "i"));
//        }
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

//    @Override
//    public List<QueryResult> getAllByGenomicVariantList(List<Variant> variantList, QueryOptions options) {
//        List<Document> queries = new ArrayList<>();
//        List<String> ids = new ArrayList<>(variantList.size());
//        List<QueryResult> queryResultList;
//        for (Variant genomicVariant : variantList) {
//            QueryBuilder builder = QueryBuilder.start("chromosome").is(genomicVariant.getChromosome()).
//                    and("start").is(genomicVariant.getStart()).and("alternate").is(genomicVariant.getAlternate());
//            if (genomicVariant.getReference() != null) {
//                builder = builder.and("reference").is(genomicVariant.getReference());
//            }
//            queries.add(new Document(builder.get().toMap()));
//            logger.debug(new Document(builder.get().toMap()).toJson());
//            ids.add(genomicVariant.toString());
//        }
//
//        queryResultList = executeQueryList2(ids, queries, options);
//
//        for (QueryResult queryResult : queryResultList) {
//            List<Document> clinicalList = (List<Document>) queryResult.getResult();
//
//            List<Cosmic> cosmicList = new ArrayList<>();
//            List<Gwas> gwasList = new ArrayList<>();
//            List<ClinVar> clinvarList = new ArrayList<>();
//
//            for (Object clinicalObject : clinicalList) {
//                Document clinical = (Document) clinicalObject;
//
//                if (isCosmic(clinical)) {
//                    Cosmic cosmic = getCosmic(clinical);
//                    cosmicList.add(cosmic);
//                } else if (isGwas(clinical)) {
//                    Gwas gwas = getGwas(clinical);
//                    gwasList.add(gwas);
//
//                } else if (isClinvar(clinical)) {
//                    ClinVar clinvar = getClinvar(clinical);
////                    if (clinvarList == null) {
////                        clinvarList = new ArrayList<>();
////                    }
//                    clinvarList.add(clinvar);
//                }
//            }
////            Map<String, Object> clinicalData = new HashMap<>();
////            if(cosmicList!=null && cosmicList.size()>0) {
////                clinicalData.put("cosmic", cosmicList);
////            }
////            if(gwasList!=null && gwasList.size()>0) {
////                clinicalData.put("gwas", gwasList);
////            }
////            if(clinvarList!=null && clinvarList.size()>0) {
////                clinicalData.put("clinvar", clinvarList);
////            }
//            VariantTraitAssociation variantTraitAssociation = new VariantTraitAssociation(clinvarList, gwasList,
//                    cosmicList, null, null);
//            if (!(variantTraitAssociation.getCosmic().isEmpty() && variantTraitAssociation.getGwas().isEmpty()
//                    && variantTraitAssociation.getClinvar().isEmpty())) {
//
//                // FIXME quick solution to compile
//                // queryResult.setResult(clinicalData);
//                queryResult.setResult(Collections.singletonList(variantTraitAssociation));
//                queryResult.setNumResults(variantTraitAssociation.getCosmic().size()
//                        + variantTraitAssociation.getGwas().size()
//                        + variantTraitAssociation.getClinvar().size());
//            } else {
//                queryResult.setResult(null);
//                queryResult.setNumResults(0);
//            }
//        }
//
//        return queryResultList;
//    }

//    private boolean isClinvar(Document clinical) {
//        return clinical.get("clinvarSet") != null;
//    }
//
//    private boolean isGwas(Document clinical) {
//        return clinical.get("snpIdCurrent") != null;
//    }
//
//    private boolean isCosmic(Document clinical) {
//        return clinical.get("mutationID") != null;
//    }
//
//    private Cosmic getCosmic(Document clinical) {
//        String mutationID = (String) clinical.get("mutationID");
//        String primarySite = (String) clinical.get("primarySite");
//        String siteSubtype = (String) clinical.get("siteSubtype");
//        String primaryHistology = (String) clinical.get("primaryHistology");
//        String histologySubtype = (String) clinical.get("histologySubtype");
//        String sampleSource = (String) clinical.get("sampleSource");
//        String tumourOrigin = (String) clinical.get("tumourOrigin");
//        String geneName = (String) clinical.get("geneName");
//        String mutationSomaticStatus = (String) clinical.get("mutationSomaticStatus");
//
//        return new Cosmic(mutationID, primarySite, siteSubtype, primaryHistology,
//                histologySubtype, sampleSource, tumourOrigin, geneName, mutationSomaticStatus);
//    }
//
//    private Gwas getGwas(Document clinical) {
//        String snpIdCurrent = (String) clinical.get("snpIdCurrent");
//        Double riskAlleleFrequency = clinical.getDouble("riskAlleleFrequency");
//        String reportedGenes = (String) clinical.get("reportedGenes");
//        List<Document> studiesObj = (List<Document>) clinical.get("studies");
//        Set<String> traitsSet = new HashSet<>();
//
//        for (Document studieObj : studiesObj) {
//            List<Document> traitsObj = (List<Document>) studieObj.get("traits");
//            for (Document traitObj : traitsObj) {
//                String trait = (String) traitObj.get("diseaseTrait");
//                traitsSet.add(trait);
//            }
//        }
//
//        List<String> traits = new ArrayList<>();
//        traits.addAll(traitsSet);
//        return new Gwas(snpIdCurrent, traits, riskAlleleFrequency, reportedGenes);
//    }
//
//    private ClinVar getClinvar(Document clinical) {
//        Document clinvarSet = (Document) clinical.get("clinvarSet");
//        Document referenceClinVarAssertion = (Document) clinvarSet.get("referenceClinVarAssertion");
//        Document clinVarAccession = (Document) referenceClinVarAssertion.get("clinVarAccession");
//        Document clinicalSignificance = (Document) referenceClinVarAssertion.get("clinicalSignificance");
//        Document measureSet = (Document) referenceClinVarAssertion.get("measureSet");
//        List<Document> measures = (List<Document>) measureSet.get("measure");
//        Document traitSet = (Document) referenceClinVarAssertion.get("traitSet");
//        List<Document> traits = (List<Document>) traitSet.get("trait");
//
//        String acc = null;
//        if (clinVarAccession != null) {
//            acc = (String) clinVarAccession.get("acc");
//        }
//        String clinicalSignificanceName = null;
//        String reviewStatus = null;
//        if (clinicalSignificance != null) {
//            clinicalSignificanceName = (String) clinicalSignificance.get("description");
//            reviewStatus = (String) clinicalSignificance.get("reviewStatus");
//        }
//        List<String> traitNames = new ArrayList<>();
//        Set<String> geneNameSet = new HashSet<>();
//
//        for (Document measure : measures) {
//            List<Document> measureRelationships = (List<Document>) measure.get("measureRelationship");
//            if (measureRelationships != null) {
//                for (Document measureRelationship : measureRelationships) {
//                    List<Document> symbols = (List<Document>) measureRelationship.get("symbol");
//                    if (symbols != null) {
//                        for (Document symbol : symbols) {
//                            Document elementValue = (Document) symbol.get("elementValue");
//                            geneNameSet.add((String) elementValue.get("value"));
//                        }
//                    }
//                }
//            }
//        }
//
//        for (Document trait : traits) {
//            List<Document> names = (List<Document>) trait.get("name");
//            for (Document name : names) {
//                Document elementValue = (Document) name.get("elementValue");
//                traitNames.add((String) elementValue.get("value"));
//            }
//        }
//
//        List<String> geneNameList = new ArrayList<>();
//        geneNameList.addAll(geneNameSet);
//        return new ClinVar(acc, clinicalSignificanceName, traitNames, geneNameList, reviewStatus);
//    }

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


}
