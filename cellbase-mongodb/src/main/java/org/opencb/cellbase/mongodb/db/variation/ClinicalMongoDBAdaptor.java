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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

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
        return mongoDBCollection.find(new BasicDBObject(), new QueryOptions("limit", 1));
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

    private BasicDBObject getFilters(QueryOptions options) {
        BasicDBObject filtersDBObject = new BasicDBObject();
        if (filteringOptionsEnabled(options)) {
            BasicDBObject commonFiltersDBObject = getCommonFilters(options);
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
                filtersDBList.add(new BasicDBObject("$or", sourceSpecificFilterList));
                filtersDBObject.put("$and", filtersDBList);
            } else if (commonFiltersDBObject != null) {
                filtersDBObject = commonFiltersDBObject;
            } else if (sourceSpecificFilterList.size() > 0) {
                filtersDBObject = new BasicDBObject("$or", sourceSpecificFilterList);
            }
        }
        return filtersDBObject;
    }

    private BasicDBObject getCommonFilters(QueryOptions options) {
        BasicDBList filterList = new BasicDBList();
        addIfNotNull(filterList, getSoTermFilter(options.getAsStringList("so")));
        addIfNotNull(filterList, getRegionFilter(Region.parseRegions((String) options.get("region"))));
        addIfNotNull(filterList, getGeneFilter(options.getAsStringList("gene")));
        addIfNotNull(filterList, getPhenotypeFilter(options.getString("phenotype")));

        if (filterList.size() > 0) {
            return new BasicDBObject("$and", filterList);
        } else {
            return null;
        }
    }

    private void addIfNotNull(BasicDBList basicDBList, BasicDBObject basicDBObject) {
        if (basicDBObject != null) {
            basicDBList.add(basicDBObject);
        }
    }

    private BasicDBObject getCosmicFilters(QueryOptions options) {
        // TODO add more filtering options
        return new BasicDBObject("source", "cosmic");
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
//            orDBList.add(new BasicDBObject("primarySite",
//                    new BasicDBObject("$in", phenotypeRegexList)));
//            orDBList.add(new BasicDBObject("siteSubtype",
//                    new BasicDBObject("$in", phenotypeRegexList)));
//            orDBList.add(new BasicDBObject("primaryHistology",
//                    new BasicDBObject("$in", phenotypeRegexList)));
//            orDBList.add(new BasicDBObject("histologySubtype",
//                    new BasicDBObject("$in", phenotypeRegexList)));
//            builder = builder.and(orDBList);
//
//        }
//        return builder;
//    }

    private BasicDBObject getGwasFilters(QueryOptions options) {
        // TODO add more filtering options
        return new BasicDBObject("source", "gwas");
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
//            builder = builder.and(new BasicDBObject("studies.traits.diseaseTrait",
//                    new BasicDBObject("$in", getClinvarPhenotypeRegex(phenotypeList))));
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

//        return executeQuery("result", builder.get(), queryOptions);
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
//        return executeQuery("result", builder.get(), options);
//    }

    private BasicDBObject getClinvarFilters(QueryOptions options) {
        BasicDBList filterList = new BasicDBList();
        filterList.add(new BasicDBObject("source", "clinvar"));
        addIfNotNull(filterList, getClinvarRcvFilter(options.getAsStringList("rcv")));
        addIfNotNull(filterList, getClinvarRsFilter(options.getAsStringList("rs")));
        addIfNotNull(filterList, getClinvarTypeFilter(options.getAsStringList("type")));
        addIfNotNull(filterList, getClinvarReviewFilter(options.getAsStringList("review")));
        addIfNotNull(filterList, getClinvarClinicalSignificanceFilter(options.getAsStringList("significance")));

        return new BasicDBObject("$and", filterList);
    }

//    private QueryBuilder addClinvarPhenotypeFilter(QueryBuilder builder, List<String> phenotypeList) {
////        List<String> phenotypeList = options.getAsStringList("phenotype", "\\|");
//        if (phenotypeList != null && phenotypeList.size() > 0) {
//            logger.info("phenotype filter activated, phenotype list: "+phenotypeList.toString());
//
//            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value",
//                    new BasicDBObject("$in", getClinvarPhenotypeRegex(phenotypeList))));
//
//        }
//        return builder;
//    }

    private BasicDBObject getGeneFilter(List<String> geneList) {
        if (geneList != null && geneList.size() > 0) {
            logger.info("gene filter activated, gene list: " + geneList.toString());
            return new BasicDBObject("_geneIds", new BasicDBObject("$in", geneList));
        }
        return null;
    }

    private BasicDBObject getPhenotypeFilter(String phenotype) {
        if (phenotype != null && !phenotype.isEmpty()) {
            logger.info("phenotype filter activated, phenotype: {} ", phenotype);
            BasicDBObject basicDBObject = new BasicDBObject("$text", new BasicDBObject("$search", phenotype));
//            basicDBObject.put("search", phenotype);
//                    new BasicDBObject("$search", phenotype);
//            searchBasicDBObject.put("$language", "en");
//            builder = builder.and(new BasicDBObject("_phenotypes", new BasicDBObject("$text", searchBasicDBObject)));
            return basicDBObject;
        }
        return null;
    }

    private BasicDBObject getRegionFilter(List<Region> regionList) {
        if (regionList != null && regionList.size() > 0) {
            logger.info("region filter activated, region list: " + regionList.toString());
            return getClinvarRegionFilterDBObject(regionList);

        }
        return null;
    }

    private BasicDBObject getClinvarRegionFilterDBObject(List<Region> regionList) {
        BasicDBList orDBList = new BasicDBList();
        for (Region region : regionList) {
            BasicDBList andDBList = new BasicDBList();
            andDBList.add(new BasicDBObject("chromosome", region.getChromosome()));
            andDBList.add(new BasicDBObject("end", new BasicDBObject("$gte", region.getStart())));
            andDBList.add(new BasicDBObject("start", new BasicDBObject("$lte", region.getEnd())));
            orDBList.add(new BasicDBObject("$and", andDBList));
        }

        return new BasicDBObject("$or", orDBList);
    }

    private BasicDBObject getClinvarClinicalSignificanceFilter(List<String> clinicalSignificanceList) {
//        List<String> clinicalSignificanceList = (List<String>) options.getAsStringList("significance");
//        List<String> clinicalSignificanceList = (List<String>) options.get("significance");
        if (clinicalSignificanceList != null && clinicalSignificanceList.size() > 0) {
            for (int i = 0; i < clinicalSignificanceList.size(); i++) {
                clinicalSignificanceList.set(i, clinicalSignificanceList.get(i).replace("_", " "));
            }
            logger.info("Clinical significance filter activated, clinical significance list: " + clinicalSignificanceList.toString());
            return new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinicalSignificance.description",
                    new BasicDBObject("$in", clinicalSignificanceList));
        }
        return null;
    }

    private BasicDBObject getClinvarReviewFilter(List<String> reviewStatusList) {
//        List<String> reviewStatusList = (List<String>) options.getAsStringList("review");
//        List<String> reviewStatusList = (List<String>) options.get("review");
        if (reviewStatusList != null && reviewStatusList.size() > 0) {
            for (int i = 0; i < reviewStatusList.size(); i++) {
                reviewStatusList.set(i, reviewStatusList.get(i).toUpperCase());
            }
            logger.info("Review staus filter activated, review status list: " + reviewStatusList.toString());
            return new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus",
                    new BasicDBObject("$in", reviewStatusList));
        }
        return null;
    }

    private BasicDBObject getClinvarTypeFilter(List<String> typeList) {
//        List<String> typeList = (List<String>) options.getAsStringList("type");
//        List<String> typeList = (List<String>) options.get("type");
        if (typeList != null && typeList.size() > 0) {
            for (int i = 0; i < typeList.size(); i++) {
                typeList.set(i, typeList.get(i).replace("_", " "));
            }
            logger.info("Type filter activated, type list: " + typeList.toString());
            return new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.type",
                    new BasicDBObject("$in", typeList));
        }
        return null;
    }

    private BasicDBObject getSoTermFilter(List<String> soList) {
//        List<String> soList = (List<String>) options.getAsStringList("so");
//        List<String> soList = (List<String>) options.get("so");
        if (soList != null && soList.size() > 0) {
            logger.info("So filter activated, SO list: " + soList.toString());
            return new BasicDBObject("annot.consequenceTypes.soTerms.soName", new BasicDBObject("$in", soList));
        }
        return null;
    }

    private BasicDBObject getClinvarRsFilter(List<String> rsStringList) {
//        List<String> rsStringList = options.getAsStringList("rs");
//        List<String> rsStringList = (List<String>) options.get("rs");
        if (rsStringList != null && rsStringList.size() > 0) {
            logger.info("rs filter activated, res list: " + rsStringList.toString());
            List<String> rsList = new ArrayList<>(rsStringList.size());
            for (String rsString : rsStringList) {
                rsList.add(rsString.substring(2));
            }
            BasicDBList filterList = new BasicDBList();
            filterList.add(new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                    new BasicDBObject("$in", rsList)));
            filterList.add(new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type",
                    "rs"));
            return new BasicDBObject("$and", filterList);
        }
        return null;
    }

    private BasicDBObject getClinvarRcvFilter(List<String> rcvList) {
//        List<String> rcvList = (List<String>) options.get("rcv");
//        List<String> rcvList = (List<String>) options.getAsStringList("rcv");
        if (rcvList != null && rcvList.size() > 0) {
            logger.info("rcv filter activated, rcv list: " + rcvList.toString());
            return new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                    new BasicDBObject("$in", rcvList));
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
        List<DBObject> queries = new ArrayList<>();

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome())
                    .and("end").greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());

            queries.add(builder.get());
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
        List<DBObject> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(variantList.size());
        List<QueryResult> queryResultList;
        for (Variant genomicVariant : variantList) {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(genomicVariant.getChromosome()).
                    and("start").is(genomicVariant.getStart()).and("alternate").is(genomicVariant.getAlternate());
            if (genomicVariant.getReference() != null) {
                builder = builder.and("reference").is(genomicVariant.getReference());
            }
            queries.add(builder.get());
            ids.add(genomicVariant.toString());
        }

        queryResultList = executeQueryList2(ids, queries, options);

        for (QueryResult queryResult : queryResultList) {
            List<BasicDBObject> clinicalList = (List<BasicDBObject>) queryResult.getResult();

            List<Cosmic> cosmicList = new ArrayList<>();
            List<Gwas> gwasList = new ArrayList<>();
            List<ClinVar> clinvarList = new ArrayList<>();

            for (Object clinicalObject : clinicalList) {
                BasicDBObject clinical = (BasicDBObject) clinicalObject;

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

    private boolean isClinvar(BasicDBObject clinical) {
        return clinical.get("clinvarSet") != null;
    }

    private boolean isGwas(BasicDBObject clinical) {
        return clinical.get("snpIdCurrent") != null;
    }

    private boolean isCosmic(BasicDBObject clinical) {
        return clinical.get("mutationID") != null;
    }

    private Cosmic getCosmic(BasicDBObject clinical) {
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

    private Gwas getGwas(BasicDBObject clinical) {
        String snpIdCurrent = (String) clinical.get("snpIdCurrent");
        Double riskAlleleFrequency = clinical.getDouble("riskAlleleFrequency", 0.0d);
        String reportedGenes = (String) clinical.get("reportedGenes");
        List<BasicDBObject> studiesObj = (List<BasicDBObject>) clinical.get("studies");
        Set<String> traitsSet = new HashSet<>();

        for (BasicDBObject studieObj : studiesObj) {
            List<BasicDBObject> traitsObj = (List<BasicDBObject>) studieObj.get("traits");
            for (BasicDBObject traitObj : traitsObj) {
                String trait = (String) traitObj.get("diseaseTrait");
                traitsSet.add(trait);
            }
        }

        List<String> traits = new ArrayList<>();
        traits.addAll(traitsSet);
        return new Gwas(snpIdCurrent, traits, riskAlleleFrequency, reportedGenes);
    }

    private ClinVar getClinvar(BasicDBObject clinical) {
        BasicDBObject clinvarSet = (BasicDBObject) clinical.get("clinvarSet");
        BasicDBObject referenceClinVarAssertion = (BasicDBObject) clinvarSet.get("referenceClinVarAssertion");
        BasicDBObject clinVarAccession = (BasicDBObject) referenceClinVarAssertion.get("clinVarAccession");
        BasicDBObject clinicalSignificance = (BasicDBObject) referenceClinVarAssertion.get("clinicalSignificance");
        BasicDBObject measureSet = (BasicDBObject) referenceClinVarAssertion.get("measureSet");
        List<BasicDBObject> measures = (List<BasicDBObject>) measureSet.get("measure");
        BasicDBObject traitSet = (BasicDBObject) referenceClinVarAssertion.get("traitSet");
        List<BasicDBObject> traits = (List<BasicDBObject>) traitSet.get("trait");


        String acc = (String) clinVarAccession.get("acc");
        String clinicalSignificanceName = (String) clinicalSignificance.get("description");
        String reviewStatus = (String) clinicalSignificance.get("reviewStatus");
        List<String> traitNames = new ArrayList<>();
        Set<String> geneNameSet = new HashSet<>();

        for (BasicDBObject measure : measures) {
            List<BasicDBObject> measureRelationships = (List<BasicDBObject>) measure.get("measureRelationship");
            if (measureRelationships != null) {
                for (BasicDBObject measureRelationship : measureRelationships) {
                    List<BasicDBObject> symbols = (List<BasicDBObject>) measureRelationship.get("symbol");
                    for (BasicDBObject symbol : symbols) {
                        BasicDBObject elementValue = (BasicDBObject) symbol.get("elementValue");
                        geneNameSet.add((String) elementValue.get("value"));
                    }
                }
            }
        }

        for (BasicDBObject trait : traits) {
            List<BasicDBObject> names = (List<BasicDBObject>) trait.get("name");
            for (BasicDBObject name : names) {
                BasicDBObject elementValue = (BasicDBObject) name.get("elementValue");
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
        QueryResult queryResult = executeQuery("", builder.get(), queryOptions);
        List accInfoList = (List) queryResult.getResult();
        List<String> accList = new ArrayList<>(accInfoList.size());
        BasicDBObject accInfo;
        QueryResult listAccessionsToReturn = new QueryResult();

        for (Object accInfoObject : accInfoList) {
            accInfo = (BasicDBObject) accInfoObject;
            accInfo = (BasicDBObject) accInfo.get("clinvarSet");
            accList.add((String) ((BasicDBObject) ((BasicDBObject) ((BasicDBObject) accInfo
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
            DBObject update = null;
            try {
                update = new BasicDBObject("$set", new BasicDBObject("annot",
                        JSON.parse(writer.writeValueAsString(variantAnnotation))));
                update.put("$addToSet",
                        new BasicDBObject("_geneIds", new BasicDBObject("$each", getGeneIds(variantAnnotation))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
//            DBObject update = new BasicDBObject("$set", new BasicDBObject("annotation",
//                    convertVariantAnnotation(variantAnnotation)));
            mongoDBCollection.update(builder.get(), update, queryOptions);
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

//    private DBObject convertVariantAnnotation(VariantAnnotation variantAnnotation) {
//        BasicDBObject basicDBObject = new BasicDBObject();
//
//        basicDBObject.put("")
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

        List<DBObject> pipeline = new ArrayList<>();
        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                new BasicDBObject("$exists", 1))));
//        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarSet", new BasicDBObject("$exists", 1))));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.traitSet.trait"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.traitSet.trait.name"));
        DBObject groupFields = new BasicDBObject();
        groupFields.put("_id", "$clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value");
        groupFields.put("associatedGenes",
                new BasicDBObject("$addToSet",
                        "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value"));
        pipeline.add(new BasicDBObject("$group", groupFields));
        DBObject fields = new BasicDBObject();
        fields.put("_id", 0);
        fields.put("phenotype", "$_id");
        fields.put("associatedGenes", 1);
        pipeline.add(new BasicDBObject("$project", fields));

        return executeAggregation2("", pipeline, queryOptions);

    }

    private QueryResult getGwasPhenotypeGeneRelations(QueryOptions queryOptions) {

        List<DBObject> pipeline = new ArrayList<>();
        // Select only GWAS documents
        pipeline.add(new BasicDBObject("$match", new BasicDBObject("snpIdCurrent", new BasicDBObject("$exists", 1))));
        pipeline.add(new BasicDBObject("$unwind", "$studies"));
        pipeline.add(new BasicDBObject("$unwind", "$studies.traits"));
        DBObject groupFields = new BasicDBObject();
        groupFields.put("_id", "$studies.traits.diseaseTrait");
        groupFields.put("associatedGenes", new BasicDBObject("$addToSet", "$reportedGenes"));
        pipeline.add(new BasicDBObject("$group", groupFields));
        DBObject fields = new BasicDBObject();
        fields.put("_id", 0);
        fields.put("phenotype", "$_id");
        fields.put("associatedGenes", 1);
        pipeline.add(new BasicDBObject("$project", fields));

        return executeAggregation2("", pipeline, queryOptions);

    }

}
