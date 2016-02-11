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

package org.opencb.cellbase.mongodb.db.variation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.BasicDBList;

import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by antonior on 11/18/14.
 *
 * @author Javier Lopez fjlopez@ebi.ac.uk
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements ClinicalDBAdaptor {


    private static Set<String> noFilteringQueryParameters = new HashSet<>(Arrays.asList("assembly", "include", "exclude",
            "skip", "limit", "of", "count", "json"));

    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical");

        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult first() {
        return mongoDBCollection.find(new Document(), new QueryOptions("limit", 1));
    }

    @Override
    public QueryResult count() {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult stats() {
        return null;
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        System.out.println("options = " + options.get("exclude"));
        QueryBuilder builder = QueryBuilder.start();
        return executeQuery("result", getFilters(options), options);
    }

    private Document getFilters(QueryOptions options) {
        Document filtersDBObject = new Document();
        if (filteringOptionsEnabled(options)) {
            Document commonFiltersDBObject = getCommonFilters(options);
            BasicDBList sourceSpecificFilterList = new BasicDBList();
            List<String> sourceContent = options.getAsStringList("source");
            if (sourceContent == null || sourceContent.isEmpty() || includeContains(sourceContent, "clinvar")) {
                sourceSpecificFilterList.add(getClinvarFilters(options));
            }
            if (sourceContent == null || sourceContent.isEmpty() || includeContains(sourceContent, "cosmic")) {
                sourceSpecificFilterList.add(getCosmicFilters(options));
            }
            if (sourceContent == null || sourceContent.isEmpty() || includeContains(sourceContent, "gwas")) {
                sourceSpecificFilterList.add(getGwasFilters(options));
            }
            if (sourceSpecificFilterList.size() > 0 && commonFiltersDBObject != null) {
                BasicDBList filtersDBList = new BasicDBList();
                filtersDBList.add(commonFiltersDBObject);
                filtersDBList.add(new Document("$or", sourceSpecificFilterList));
                filtersDBObject.put("$and", filtersDBList);
            } else if (commonFiltersDBObject != null) {
                filtersDBObject = commonFiltersDBObject;
            } else if (sourceSpecificFilterList.size() > 0) {
                filtersDBObject = new Document("$or", sourceSpecificFilterList);
            }
        }
        return filtersDBObject;
    }

    private Document getCommonFilters(QueryOptions options) {
        BasicDBList filterList = new BasicDBList();
        addIfNotNull(filterList, getSoTermFilter(options.getAsStringList("so")));
        addIfNotNull(filterList, getRegionFilter(Region.parseRegions((String) options.get("region"))));
        addIfNotNull(filterList, getGeneFilter(options.getAsStringList("gene")));
        addIfNotNull(filterList, getPhenotypeFilter(options.getString("phenotype")));

        if (filterList.size() > 0) {
            return new Document("$and", filterList);
        } else {
            return null;
        }
    }

    private void addIfNotNull(BasicDBList basicDBList, Document document) {
        if (document != null) {
            basicDBList.add(document);
        }
    }

    private Document getCosmicFilters(QueryOptions options) {
        // TODO add more filtering options
        return new Document("source", "cosmic");
//        builder = addClinvarRcvFilter(builder, options.getAsStringList("rcv"));
//        builder = addClinvarRsFilter(builder, options.getAsStringList("rs"));
//        builder = addClinvarTypeFilter(builder, options.getAsStringList("type"));
//        builder = addClinvarReviewFilter(builder, options.getAsStringList("review"));
//        builder = addClinvarClinicalSignificanceFilter(builder, options.getAsStringList("significance"));
//        builder = addCosmicPhenotypeFilter(builder, options.getAsStringList("phenotype", "\\|"));
    }

//    private QueryBuilder addCosmicPhenotypeFilter(QueryBuilder builder, List<String> phenotypeList) {
//        if (phenotypeList != null && phenotypeList.size() > 0) {
//            BasicDBList orDBList = new BasicDBList();
//            logger.info("phenotype filter activated, phenotype list: "+phenotypeList.toString());
//            List<Pattern> phenotypeRegexList = getClinvarPhenotypeRegex(phenotypeList);
//            orDBList.add(new Document("primarySite",
//                    new Document("$in", phenotypeRegexList)));
//            orDBList.add(new Document("siteSubtype",
//                    new Document("$in", phenotypeRegexList)));
//            orDBList.add(new Document("primaryHistology",
//                    new Document("$in", phenotypeRegexList)));
//            orDBList.add(new Document("histologySubtype",
//                    new Document("$in", phenotypeRegexList)));
//            builder = builder.and(orDBList);
//
//        }
//        return builder;
//    }

    private Document getGwasFilters(QueryOptions options) {
        // TODO add more filtering options
        return new Document("source", "gwas");
//        builder = addClinvarRcvFilter(builder, options.getAsStringList("rcv"));
//        builder = addClinvarRsFilter(builder, options.getAsStringList("rs"));
//        builder = addSoTermFilter(builder, options.getAsStringList("so"));
//        builder = addClinvarTypeFilter(builder, options.getAsStringList("type"));
//        builder = addClinvarReviewFilter(builder, options.getAsStringList("review"));
//        builder = addClinvarClinicalSignificanceFilter(builder, options.getAsStringList("significance"));
//        builder = addRegionFilter(builder, Region.parseRegions((String) options.get("region")));
//        builder = addGeneFilter(builder, options.getAsStringList("gene"));
//        builder = addPhenotypeFilter(builder, options.getString("phenotype"));
//        builder = addGwasPhenotypeFilter(builder, options.getAsStringList("phenotype", "\\|"));
    }

//    private QueryBuilder addGwasPhenotypeFilter(QueryBuilder builder, List<String> phenotypeList) {
//        if (phenotypeList != null && phenotypeList.size() > 0) {
//            logger.info("phenotype filter activated, phenotype list: " + phenotypeList.toString());
//
//            builder = builder.and(new Document("studies.traits.diseaseTrait",
//                    new Document("$in", getClinvarPhenotypeRegex(phenotypeList))));
//        }
//        return builder;
//    }

    private boolean filteringOptionsEnabled(QueryOptions queryOptions) {
        int i = 0;
        Object[] keys = queryOptions.keySet().toArray();
        while ((i < queryOptions.size()) && noFilteringQueryParameters.contains(keys[i])) {
            i++;
        }
        return (i < queryOptions.size());
    }

    @Override
    public QueryResult getByGeneId(String geneId, QueryOptions options) {
        options.add("gene", geneId);
//        builder = addGeneFilter(builder, Collections.singletonList(geneId));
        return executeQuery("result", getFilters(options), options);
    }

//    public QueryResult getClinvarByGeneId(String geneId, QueryOptions queryOptions) {
//        QueryBuilder builder = QueryBuilder.start();
//          builder = addGeneFilter(builder, Collections.singletonList(geneId));
//        builder = addClinvarFilters(builder, queryOptions);

//        return executeQuery("result", new Document(builder.get().toMap()), queryOptions);
//    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return null;
    }

//    @Override
//    public QueryResult getAllClinvar(QueryOptions options) {
//        QueryBuilder builder = QueryBuilder.start();
//        builder = addClinvarFilters(builder, options);
//
//        return executeQuery("result", new Document(builder.get().toMap()), options);
//    }

    private Document getClinvarFilters(QueryOptions options) {
        BasicDBList filterList = new BasicDBList();
        filterList.add(new Document("source", "clinvar"));
        addIfNotNull(filterList, getClinvarRcvFilter(options.getAsStringList("rcv")));
        addIfNotNull(filterList, getClinvarRsFilter(options.getAsStringList("rs")));
        addIfNotNull(filterList, getClinvarTypeFilter(options.getAsStringList("type")));
        addIfNotNull(filterList, getClinvarReviewFilter(options.getAsStringList("review")));
        addIfNotNull(filterList, getClinvarClinicalSignificanceFilter(options.getAsStringList("significance")));

        return new Document("$and", filterList);
    }

//    private QueryBuilder addClinvarPhenotypeFilter(QueryBuilder builder, List<String> phenotypeList) {
////        List<String> phenotypeList = options.getAsStringList("phenotype", "\\|");
//        if (phenotypeList != null && phenotypeList.size() > 0) {
//            logger.info("phenotype filter activated, phenotype list: "+phenotypeList.toString());
//
//            builder = builder.and(new Document("clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value",
//                    new Document("$in", getClinvarPhenotypeRegex(phenotypeList))));
//
//        }
//        return builder;
//    }

    private Document getGeneFilter(List<String> geneList) {
        if (geneList != null && geneList.size() > 0) {
            logger.info("gene filter activated, gene list: " + geneList.toString());
            return new Document("_geneIds", new Document("$in", geneList));
        }
        return null;
    }

    private Document getPhenotypeFilter(String phenotype) {
        if (phenotype != null && !phenotype.isEmpty()) {
            logger.info("phenotype filter activated, phenotype: {} ", phenotype);
            Document document = new Document("$text", new Document("$search", phenotype));
//            Document.put("search", phenotype);
//                    new Document("$search", phenotype);
//            searchBasicDBObject.put("$language", "en");
//            builder = builder.and(new Document("_phenotypes", new Document("$text", searchBasicDBObject)));
            return document;
        }
        return null;
    }

    private Document getRegionFilter(List<Region> regionList) {
        if (regionList != null && regionList.size() > 0) {
            logger.info("region filter activated, region list: " + regionList.toString());
            return getClinvarRegionFilterDBObject(regionList);

        }
        return null;
    }

    private Document getClinvarRegionFilterDBObject(List<Region> regionList) {
        BasicDBList orDBList = new BasicDBList();
        for (Region region : regionList) {
            BasicDBList andDBList = new BasicDBList();
            andDBList.add(new Document("chromosome", region.getChromosome()));
            andDBList.add(new Document("end", new Document("$gte", region.getStart())));
            andDBList.add(new Document("start", new Document("$lte", region.getEnd())));
            orDBList.add(new Document("$and", andDBList));
        }

        return new Document("$or", orDBList);
    }

    private Document getClinvarClinicalSignificanceFilter(List<String> clinicalSignificanceList) {
//        List<String> clinicalSignificanceList = (List<String>) options.getAsStringList("significance");
//        List<String> clinicalSignificanceList = (List<String>) options.get("significance");
        if (clinicalSignificanceList != null && clinicalSignificanceList.size() > 0) {
            for (int i = 0; i < clinicalSignificanceList.size(); i++) {
                clinicalSignificanceList.set(i, clinicalSignificanceList.get(i).replace("_", " "));
            }
            logger.info("Clinical significance filter activated, clinical significance list: " + clinicalSignificanceList.toString());
            return new Document("clinvarSet.referenceClinVarAssertion.clinicalSignificance.description",
                    new Document("$in", clinicalSignificanceList));
        }
        return null;
    }

    private Document getClinvarReviewFilter(List<String> reviewStatusList) {
//        List<String> reviewStatusList = (List<String>) options.getAsStringList("review");
//        List<String> reviewStatusList = (List<String>) options.get("review");
        if (reviewStatusList != null && reviewStatusList.size() > 0) {
            for (int i = 0; i < reviewStatusList.size(); i++) {
                reviewStatusList.set(i, reviewStatusList.get(i).toUpperCase());
            }
            logger.info("Review staus filter activated, review status list: " + reviewStatusList.toString());
            return new Document("clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus",
                    new Document("$in", reviewStatusList));
        }
        return null;
    }

    private Document getClinvarTypeFilter(List<String> typeList) {
//        List<String> typeList = (List<String>) options.getAsStringList("type");
//        List<String> typeList = (List<String>) options.get("type");
        if (typeList != null && typeList.size() > 0) {
            for (int i = 0; i < typeList.size(); i++) {
                typeList.set(i, typeList.get(i).replace("_", " "));
            }
            logger.info("Type filter activated, type list: " + typeList.toString());
            return new Document("clinvarSet.referenceClinVarAssertion.measureSet.measure.type",
                    new Document("$in", typeList));
        }
        return null;
    }

    private Document getSoTermFilter(List<String> soList) {
//        List<String> soList = (List<String>) options.getAsStringList("so");
//        List<String> soList = (List<String>) options.get("so");
        if (soList != null && soList.size() > 0) {
            logger.info("So filter activated, SO list: " + soList.toString());
            return new Document("annot.consequenceTypes.soTerms.soName", new Document("$in", soList));
        }
        return null;
    }

    private Document getClinvarRsFilter(List<String> rsStringList) {
//        List<String> rsStringList = options.getAsStringList("rs");
//        List<String> rsStringList = (List<String>) options.get("rs");
        if (rsStringList != null && rsStringList.size() > 0) {
            logger.info("rs filter activated, res list: " + rsStringList.toString());
            List<String> rsList = new ArrayList<>(rsStringList.size());
            for (String rsString : rsStringList) {
                rsList.add(rsString.substring(2));
            }
            BasicDBList filterList = new BasicDBList();
            filterList.add(new Document("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                    new Document("$in", rsList)));
            filterList.add(new Document("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type",
                    "rs"));
            return new Document("$and", filterList);
        }
        return null;
    }

    private Document getClinvarRcvFilter(List<String> rcvList) {
//        List<String> rcvList = (List<String>) options.get("rcv");
//        List<String> rcvList = (List<String>) options.getAsStringList("rcv");
        if (rcvList != null && rcvList.size() > 0) {
            logger.info("rcv filter activated, rcv list: " + rcvList.toString());
            return new Document("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                    new Document("$in", rcvList));
        }
        return null;
    }

    private List<Pattern> getClinvarPhenotypeRegex(List<String> phenotypeList) {
        List<Pattern> patternList = new ArrayList<>(phenotypeList.size());
        for (String keyword : phenotypeList) {
            patternList.add(Pattern.compile(".*" + keyword + ".*", Pattern.CASE_INSENSITIVE));
        }

        return patternList;
    }


    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        List<Document> queries = new ArrayList<>();

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome())
                    .and("end").greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());

            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }
        return executeQueryList2(ids, queries, options);
    }

    private Boolean includeContains(List<String> includeContent, String feature) {
        if (includeContent != null) {
            int i = 0;
            while (i < includeContent.size() && !includeContent.get(i).equals(feature)) {
                i++;
            }
            //                includeContent.remove(i);  // Avoid term "clinvar" (for instance) to be passed to datastore
            return i < includeContent.size();
        } else {
            return false;
        }
    }

    @Override
    public QueryResult getAllByGenomicVariant(Variant variant, QueryOptions options) {
        return getAllByGenomicVariantList(Arrays.asList(variant), options).get(0);
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

    public QueryResult getListClinvarAccessions(QueryOptions queryOptions) {
        QueryBuilder builder = QueryBuilder.start("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc").exists(true);
        queryOptions.put("include", Arrays.asList("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc"));
        QueryResult queryResult = executeQuery("", new Document(builder.get().toMap()), queryOptions);
        List accInfoList = (List) queryResult.getResult();
        List<String> accList = new ArrayList<>(accInfoList.size());
        Document accInfo;
        QueryResult listAccessionsToReturn = new QueryResult();

        for (Object accInfoObject : accInfoList) {
            accInfo = (Document) accInfoObject;
            accInfo = (Document) accInfo.get("clinvarSet");
            accList.add((String) ((Document) ((Document) ((Document) accInfo
                    .get("referenceClinVarAssertion"))).get("clinVarAccession")).get("acc"));
        }

        // setting listAccessionsToReturn fields
        listAccessionsToReturn.setId(queryResult.getId());
        listAccessionsToReturn.setDbTime(queryResult.getDbTime());
        listAccessionsToReturn.setNumResults(queryResult.getNumResults());
        listAccessionsToReturn.setNumTotalResults(queryResult.getNumTotalResults());
        listAccessionsToReturn.setResult(accList);

        return listAccessionsToReturn;
    }

    public QueryResult updateAnnotations(List<VariantAnnotation> variantAnnotations, QueryOptions queryOptions) {

        /**
         * Multiple documents may contain the same annotation
         */
        queryOptions.put("multi", true);

        /**
         * Prepare jackson to generate json strings
         */
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        ObjectWriter writer = jsonObjectMapper.writer();

        long start = System.nanoTime();
        for (VariantAnnotation variantAnnotation : variantAnnotations) {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(variantAnnotation.getChromosome())
                    .and("start").is(variantAnnotation.getStart()).and("reference")
                    .is(variantAnnotation.getReference())
                    .and("alternate").is(variantAnnotation.getAlternate());
            Document update = null;
            try {
                update = new Document("$set", new Document("annot",
                        JSON.parse(writer.writeValueAsString(variantAnnotation))));
                update.put("$addToSet",
                        new Document("_geneIds", new Document("$each", getGeneIds(variantAnnotation))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
//            Document update = new Document("$set", new Document("annotation",
//                    convertVariantAnnotation(variantAnnotation)));
            mongoDBCollection.update(new Document(builder.get().toMap()), update, queryOptions);
        }

        return new QueryResult<>("", ((int) (System.nanoTime() - start)), 1, 1, "", "", new ArrayList());
    }

    private List<String> getGeneIds(VariantAnnotation variantAnnotation) {
        Set<String> geneIdSet = new HashSet<>();
        for (ConsequenceType consequenceType : variantAnnotation.getConsequenceTypes()) {
            geneIdSet.add(consequenceType.getGeneName());
            geneIdSet.add(consequenceType.getEnsemblGeneId());
        }

        return new ArrayList<>(geneIdSet);
    }

//    private Document convertVariantAnnotation(VariantAnnotation variantAnnotation) {
//        Document Document = new Document();
//
//        Document.put("")
//    }

    public List<QueryResult> getPhenotypeGeneRelations(QueryOptions queryOptions) {

        List<QueryResult> queryResultList = new ArrayList<>();
        if (!queryOptions.containsKey("include") || queryOptions.getAsStringList("include").size() == 0
                || includeContains(queryOptions.getAsStringList("include"), "clinvar")) {
            queryResultList.add(getClinvarPhenotypeGeneRelations(queryOptions));

        }
        if (!queryOptions.containsKey("include") || queryOptions.getAsStringList("include").size() == 0
                || includeContains(queryOptions.getAsStringList("include"), "gwas")) {
            queryResultList.add(getGwasPhenotypeGeneRelations(queryOptions));
        }

        return queryResultList;
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

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
