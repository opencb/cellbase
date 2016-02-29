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

import com.mongodb.QueryBuilder;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ClinVar;
import org.opencb.biodata.models.variant.avro.Cosmic;
import org.opencb.biodata.models.variant.avro.Gwas;
import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.core.common.clinical.ClinicalVariant;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by imedina on 01/12/15.
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements ClinicalDBAdaptor<ClinicalVariant> {

    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical");

        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult<ClinicalVariant> next(Query query, QueryOptions options) {
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
        return null;
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
    public QueryResult<ClinicalVariant> get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        QueryOptions parsedOptions = parseQueryOptions(options);
        return mongoDBCollection.find(bson, parsedOptions);
    }

    @Override
    public Iterator<ClinicalVariant> iterator(Query query, QueryOptions options) {
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

    private QueryOptions parseQueryOptions(QueryOptions options) {
        List<String> sortFields = options.getAsStringList("sort");
        if (sortFields != null) {
            Document sortDocument = new Document();
            for (String field : sortFields) {
                sortDocument.put(field, 1);
            }
            options.put("sort", sortDocument);
        }
        return options;
    }

    private Bson parseQuery(Query query) {
        logger.info("Parsing query...");
        Bson filtersBson = null;

        // No filtering parameters mean all records
        if (query.size() > 0) {
            Bson commonFiltersBson = getCommonFilters(query);
            Set<String> sourceContent = query.getAsStringList(QueryParams.SOURCE.key()) != null
                    ? new HashSet<>(query.getAsStringList(QueryParams.SOURCE.key())) : null;
            List<Bson> sourceSpecificFilterList = new ArrayList<>();
            getClinvarFilters(query, sourceContent, sourceSpecificFilterList);
            getCosmicFilters(query, sourceContent, sourceSpecificFilterList);
            getGwasFilters(query, sourceContent, sourceSpecificFilterList);

            if (sourceSpecificFilterList.size() > 0 && commonFiltersBson != null) {
                List<Bson> filtersBsonList = new ArrayList<>();
                filtersBsonList.add(commonFiltersBson);
                filtersBsonList.add(Filters.or(sourceSpecificFilterList));
                filtersBson = Filters.and(filtersBsonList);
            } else if (commonFiltersBson != null) {
                filtersBson = commonFiltersBson;
            } else if (sourceSpecificFilterList.size() > 0) {
                filtersBson = Filters.or(sourceSpecificFilterList);
            }
        }

        if (filtersBson != null) {
            return filtersBson;
        } else {
            return new Document();
        }
    }

    private void getGwasFilters(Query query, Set<String> sourceContent, List<Bson> sourceBson) {
        // If only clinvar-specific filters are provided it must be avoided to include the source=gwas condition since
        // sourceBson is going to be an OR list
        if (!(query.containsKey(QueryParams.CLINVARRCV.key()) || query.containsKey(QueryParams.CLINVARCLINSIG.key())
                || query.containsKey(QueryParams.CLINVARREVIEW.key())
                || query.containsKey(QueryParams.CLINVARTYPE.key())
                || query.containsKey(QueryParams.CLINVARRS.key()))) {
            if (sourceContent != null && sourceContent.contains("gwas")) {
                sourceBson.add(Filters.eq("source", "gwas"));
            }
        }
    }

    private void getCosmicFilters(Query query, Set<String> sourceContent, List<Bson> sourceBson) {
        // If only clinvar-specific filters are provided it must be avoided to include the source=cosmic condition since
        // sourceBson is going to be an OR list
        if (!(query.containsKey(QueryParams.CLINVARRCV.key()) || query.containsKey(QueryParams.CLINVARCLINSIG.key())
                || query.containsKey(QueryParams.CLINVARREVIEW.key())
                || query.containsKey(QueryParams.CLINVARTYPE.key())
                || query.containsKey(QueryParams.CLINVARRS.key()))) {
            if (sourceContent != null && sourceContent.contains("cosmic")) {
                sourceBson.add(Filters.eq("source", "cosmic"));
            }
        }
    }

    private void getClinvarFilters(Query query, Set<String> sourceContent, List<Bson> sourceBson) {
        List<Bson> andBsonList = new ArrayList<>();

        if (sourceContent != null && sourceContent.contains("clinvar")) {
            andBsonList.add(Filters.eq("source", "clinvar"));
        }

        createOrQuery(query, QueryParams.CLINVARRCV.key(), "clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                andBsonList);
        createClinvarRsQuery(query, andBsonList);
        createClinvarTypeQuery(query, andBsonList);
        createClinvarReviewQuery(query, andBsonList);
        createClinvarClinicalSignificanceQuery(query, andBsonList);

        if (andBsonList.size() == 1) {
            sourceBson.add(andBsonList.get(0));
        } else if (andBsonList.size() > 0) {
            sourceBson.add(Filters.and(andBsonList));
        }
    }

    private void createClinvarClinicalSignificanceQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARCLINSIG.key()) != null
                && !query.getString(QueryParams.CLINVARCLINSIG.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARCLINSIG.key()).stream()
                            .map((clinicalSignificanceString) -> clinicalSignificanceString.replace("_", " "))
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.clinicalSignificance.description", andBsonList);
        }
    }

    private void createClinvarReviewQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARREVIEW.key()) != null
                && !query.getString(QueryParams.CLINVARREVIEW.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARREVIEW.key()).stream()
                            .map(String::toUpperCase)
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus", andBsonList);
        }
    }

    private void createClinvarTypeQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARTYPE.key()) != null
                && !query.getString(QueryParams.CLINVARTYPE.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARTYPE.key()).stream()
                            .map((typeString) -> typeString.replace("_", " "))
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.measureSet.measure.type", andBsonList);
        }
    }

    private void createClinvarRsQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARRS.key()) != null
                && !query.getString(QueryParams.CLINVARRS.key()).isEmpty()) {
            List<String> queryList = query.getAsStringList(QueryParams.CLINVARRS.key());
            if (queryList.size() == 1) {
                andBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                        queryList.get(0).substring(2)));
                andBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type", "rs"));
            } else {
                List<Bson> orBsonList = new ArrayList<>(queryList.size());
                for (String queryItem : queryList) {
                    List<Bson> innerAndBsonList = new ArrayList<>();
                    innerAndBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                            queryList.get(0).substring(2)));
                    innerAndBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type", "rs"));
                    orBsonList.add(Filters.and(innerAndBsonList));
                }
                andBsonList.add(Filters.or(orBsonList));
            }
        }
    }

    private Bson getCommonFilters(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, QueryParams.REGION.key(), andBsonList);

        createOrQuery(query, QueryParams.SO.key(), "annot.consequenceTypes.sequenceOntologyTerms.name", andBsonList);
        createOrQuery(query, QueryParams.GENE.key(), "_geneIds", andBsonList);
        createPhenotypeQuery(query, andBsonList);

        if (andBsonList.size() == 1) {
            return andBsonList.get(0);
        } else if (andBsonList.size() > 1) {
            return Filters.and(andBsonList);
        } else {
            return null;
        }
    }

    private void createPhenotypeQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.PHENOTYPE.key()) != null
                && !query.getString(QueryParams.PHENOTYPE.key()).isEmpty()) {
            andBsonList.add(Filters.text(query.getString(QueryParams.PHENOTYPE.key())));
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
    public List<QueryResult> getAllByGenomicVariantList(List<Variant> variantList, QueryOptions options) {
        List<Document> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(variantList.size());
        List<QueryResult> queryResultList;
        for (Variant genomicVariant : variantList) {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(genomicVariant.getChromosome()).
                    and("start").is(genomicVariant.getStart()).and("alternate").is(genomicVariant.getAlternate());
            if (genomicVariant.getReference() != null) {
                builder = builder.and("reference").is(genomicVariant.getReference());
            }
            queries.add(new Document(builder.get().toMap()));
            ids.add(genomicVariant.toString());
        }

        queryResultList = executeQueryList2(ids, queries, options);

        for (QueryResult queryResult : queryResultList) {
            List<Document> clinicalList = (List<Document>) queryResult.getResult();

            List<Cosmic> cosmicList = new ArrayList<>();
            List<Gwas> gwasList = new ArrayList<>();
            List<ClinVar> clinvarList = new ArrayList<>();

            for (Object clinicalObject : clinicalList) {
                Document clinical = (Document) clinicalObject;

                if (isCosmic(clinical)) {
                    Cosmic cosmic = getCosmic(clinical);
                    cosmicList.add(cosmic);
                } else if (isGwas(clinical)) {
                    Gwas gwas = getGwas(clinical);
                    gwasList.add(gwas);

                } else if (isClinvar(clinical)) {
                    ClinVar clinvar = getClinvar(clinical);
//                    if (clinvarList == null) {
//                        clinvarList = new ArrayList<>();
//                    }
                    clinvarList.add(clinvar);
                }
            }
//            Map<String, Object> clinicalData = new HashMap<>();
//            if(cosmicList!=null && cosmicList.size()>0) {
//                clinicalData.put("cosmic", cosmicList);
//            }
//            if(gwasList!=null && gwasList.size()>0) {
//                clinicalData.put("gwas", gwasList);
//            }
//            if(clinvarList!=null && clinvarList.size()>0) {
//                clinicalData.put("clinvar", clinvarList);
//            }
            VariantTraitAssociation variantTraitAssociation = new VariantTraitAssociation(clinvarList, gwasList, cosmicList);
            if (!(variantTraitAssociation.getCosmic().isEmpty() && variantTraitAssociation.getGwas().isEmpty()
                    && variantTraitAssociation.getClinvar().isEmpty())) {

                // FIXME quick solution to compile
                // queryResult.setResult(clinicalData);
                queryResult.setResult(Collections.singletonList(variantTraitAssociation));
                queryResult.setNumResults(variantTraitAssociation.getCosmic().size()
                        + variantTraitAssociation.getGwas().size()
                        + variantTraitAssociation.getClinvar().size());
            } else {
                queryResult.setResult(null);
                queryResult.setNumResults(0);
            }
        }

        return queryResultList;
    }

    private boolean isClinvar(Document clinical) {
        return clinical.get("clinvarSet") != null;
    }

    private boolean isGwas(Document clinical) {
        return clinical.get("snpIdCurrent") != null;
    }

    private boolean isCosmic(Document clinical) {
        return clinical.get("mutationID") != null;
    }

    private Cosmic getCosmic(Document clinical) {
        String mutationID = (String) clinical.get("mutationID");
        String primarySite = (String) clinical.get("primarySite");
        String siteSubtype = (String) clinical.get("siteSubtype");
        String primaryHistology = (String) clinical.get("primaryHistology");
        String histologySubtype = (String) clinical.get("histologySubtype");
        String sampleSource = (String) clinical.get("sampleSource");
        String tumourOrigin = (String) clinical.get("tumourOrigin");
        String geneName = (String) clinical.get("geneName");
        String mutationSomaticStatus = (String) clinical.get("mutationSomaticStatus");

        return new Cosmic(mutationID, primarySite, siteSubtype, primaryHistology,
                histologySubtype, sampleSource, tumourOrigin, geneName, mutationSomaticStatus);
    }

    private Gwas getGwas(Document clinical) {
        String snpIdCurrent = (String) clinical.get("snpIdCurrent");
        Double riskAlleleFrequency = clinical.getDouble("riskAlleleFrequency");
        String reportedGenes = (String) clinical.get("reportedGenes");
        List<Document> studiesObj = (List<Document>) clinical.get("studies");
        Set<String> traitsSet = new HashSet<>();

        for (Document studieObj : studiesObj) {
            List<Document> traitsObj = (List<Document>) studieObj.get("traits");
            for (Document traitObj : traitsObj) {
                String trait = (String) traitObj.get("diseaseTrait");
                traitsSet.add(trait);
            }
        }

        List<String> traits = new ArrayList<>();
        traits.addAll(traitsSet);
        return new Gwas(snpIdCurrent, traits, riskAlleleFrequency, reportedGenes);
    }

    private ClinVar getClinvar(Document clinical) {
        Document clinvarSet = (Document) clinical.get("clinvarSet");
        Document referenceClinVarAssertion = (Document) clinvarSet.get("referenceClinVarAssertion");
        Document clinVarAccession = (Document) referenceClinVarAssertion.get("clinVarAccession");
        Document clinicalSignificance = (Document) referenceClinVarAssertion.get("clinicalSignificance");
        Document measureSet = (Document) referenceClinVarAssertion.get("measureSet");
        List<Document> measures = (List<Document>) measureSet.get("measure");
        Document traitSet = (Document) referenceClinVarAssertion.get("traitSet");
        List<Document> traits = (List<Document>) traitSet.get("trait");


        String acc = (String) clinVarAccession.get("acc");
        String clinicalSignificanceName = (String) clinicalSignificance.get("description");
        String reviewStatus = (String) clinicalSignificance.get("reviewStatus");
        List<String> traitNames = new ArrayList<>();
        Set<String> geneNameSet = new HashSet<>();

        for (Document measure : measures) {
            List<Document> measureRelationships = (List<Document>) measure.get("measureRelationship");
            if (measureRelationships != null) {
                for (Document measureRelationship : measureRelationships) {
                    List<Document> symbols = (List<Document>) measureRelationship.get("symbol");
                    for (Document symbol : symbols) {
                        Document elementValue = (Document) symbol.get("elementValue");
                        geneNameSet.add((String) elementValue.get("value"));
                    }
                }
            }
        }

        for (Document trait : traits) {
            List<Document> names = (List<Document>) trait.get("name");
            for (Document name : names) {
                Document elementValue = (Document) name.get("elementValue");
                traitNames.add((String) elementValue.get("value"));
            }
        }

        List<String> geneNameList = new ArrayList<>();
        geneNameList.addAll(geneNameSet);
        return new ClinVar(acc, clinicalSignificanceName, traitNames, geneNameList, reviewStatus);
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

}
