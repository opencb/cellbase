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

package org.opencb.cellbase.mongodb.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.Clinvar;
import org.opencb.biodata.models.variant.annotation.Cosmic;
import org.opencb.biodata.models.variant.annotation.Gwas;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Position;

import org.opencb.cellbase.core.lib.api.variation.ClinicalDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

import javax.management.Query;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by antonior on 11/18/14.
 * @author Javier Lopez fjlopez@ebi.ac.uk
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements ClinicalDBAdaptor {


    public ClinicalMongoDBAdaptor(DB db) {
        super(db);
    }

    public ClinicalMongoDBAdaptor(DB db, String species, String assembly) {
        super(db, species, assembly);
        mongoDBCollection = db.getCollection("clinical");
        logger.info("ClinicalVarMongoDBAdaptor: in 'constructor'");
    }

    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection2 = mongoDataStore.getCollection("clinical");

        logger.info("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        if(includeContains(options.getAsStringList("include"), "clinvar")) {
            return getAllClinvar(options);
        } else {
            // TODO implement!
            return new QueryResult();
        }
    }

    @Override
    public QueryResult getAllClinvar(QueryOptions options) {
        options.addToListOption("include", "clinvarSet");
        options.addToListOption("include", "chromosome");
        options.addToListOption("include", "start");
        options.addToListOption("include", "end");
        options.addToListOption("include", "reference");
        options.addToListOption("include", "alternate");
        options.addToListOption("include", "annot");
        QueryBuilder builder = QueryBuilder.start();

        builder = addClinvarFilters(builder, options);

//        List<DBObject> pipeline = new ArrayList<>();
//        pipeline = addClinvarAggregationFilters(pipeline, options);
//        DBObject fields = new BasicDBObject();
//        fields.put("clinvarSet", 1);
//        fields.put("chromosome", 1);
//        fields.put("start", 1);
//        fields.put("end", 1);
//        fields.put("reference", 1);
//        fields.put("alternate", 1);
//        fields.put("annot", 1);
//        pipeline.add(new BasicDBObject("$project", fields));



//        return executeAggregation2("", pipeline, options);
        return executeQuery("result", builder.get(), options);
//        return prepareClinvarQueryResultList(Collections.singletonList(executeQuery("result", builder.get(), options))).get(0);
    }


    private QueryBuilder addClinvarFilters(QueryBuilder builder, QueryOptions options) {
        List<DBObject> filterSteps = new ArrayList<>();
        builder.and(new BasicDBObject("clinvarSet", new BasicDBObject("$exists", 1)));
        builder = addClinvarRcvFilter(builder, options);
        builder = addClinvarRsFilter(builder, options);
        builder = addClinvarSoTermFilter(builder, options);
        builder = addClinvarTypeFilter(builder, options);
        builder = addClinvarReviewFilter(builder, options);
        builder = addClinvarClinicalSignificanceFilter(builder, options);
        builder = addClinvarRegionFilter(builder, options);
        builder = addClinvarGeneFilter(builder, options);
        builder = addClinvarPhenotypeFilter(builder, options);
//
//        // Filtering steps repeated twice to avoid undwind over all clinical records
//        pipeline.addAll(filterSteps);
////        pipeline.add(new BasicDBObject("$unwind", "$clinvarList"));
////        pipeline.addAll(filterSteps);
//        pipeline.add(new BasicDBObject("$limit", 100));

        return builder;
    }

    private QueryBuilder addClinvarPhenotypeFilter(QueryBuilder builder, QueryOptions options) {
        List<String> phenotypeList = (List<String>) options.getAsStringList("phenotype");
        if (phenotypeList != null && phenotypeList.size() > 0) {
            logger.info("phenotype filter activated, phenotype list: "+phenotypeList.toString());

            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value",
                    new BasicDBObject("$in", getClinvarPhenotypeRegex(phenotypeList))));

        }
        return builder;
    }

    private QueryBuilder addClinvarGeneFilter(QueryBuilder builder, QueryOptions options) {
        List<String> geneList = (List<String>) options.get("gene");
//        List<String> geneList = options.getAsStringList("gene", null);
        if (geneList != null && geneList.size() > 0) {
            logger.info("gene filter activated, gene list: " + geneList.toString());
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value",
                    new BasicDBObject("$in", geneList)));
        }
        return builder;
    }

    private QueryBuilder addClinvarRegionFilter(QueryBuilder builder, QueryOptions options) {
        List<Region> regionList = (List<Region>) options.get("region");
        if (regionList != null && regionList.size() > 0) {
            logger.info("region filter activated, region list: " + regionList.toString());
            builder = builder.and(getClinvarRegionFilterDBObject(regionList));

        }
        return builder;
    }

    private DBObject getClinvarRegionFilterDBObject(List<Region> regionList) {
        BasicDBList orDBList = new BasicDBList();
        for(Region region : regionList) {
            BasicDBList andDBList = new BasicDBList();
            andDBList.add(new BasicDBObject("chromosome", region.getChromosome()));
            andDBList.add(new BasicDBObject("end", new BasicDBObject("$gte", region.getStart())));
            andDBList.add(new BasicDBObject("start", new BasicDBObject("$lte", region.getEnd())));
            orDBList.add(new BasicDBObject("$and",andDBList));
        }

        return new BasicDBObject("$or", orDBList);
    }


    private QueryBuilder addClinvarClinicalSignificanceFilter(QueryBuilder builder, QueryOptions options) {
        List<String> clinicalSignificanceList = (List<String>) options.get("significance");
        if (clinicalSignificanceList != null && clinicalSignificanceList.size() > 0) {
            for(int i=0; i<clinicalSignificanceList.size(); i++) {
                clinicalSignificanceList.set(i, clinicalSignificanceList.get(i).replace("_"," "));
            }
            logger.info("Clinical significance filter activated, clinical significance list: " + clinicalSignificanceList.toString());
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinicalSignificance.description",
                    new BasicDBObject("$in", clinicalSignificanceList)));
        }
        return builder;
    }

    private QueryBuilder addClinvarReviewFilter(QueryBuilder builder, QueryOptions options) {
        List<String> reviewStatusList = (List<String>) options.get("review");
        if (reviewStatusList != null && reviewStatusList.size() > 0) {
            for(int i=0; i<reviewStatusList.size(); i++) {
                reviewStatusList.set(i, reviewStatusList.get(i).toUpperCase());
            }
            logger.info("Review staus filter activated, review status list: " + reviewStatusList.toString());
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus",
                    new BasicDBObject("$in", reviewStatusList)));
        }
        return builder;
    }


    private QueryBuilder addClinvarTypeFilter(QueryBuilder builder, QueryOptions options) {
        List<String> typeList = (List<String>) options.get("type");
        if (typeList != null && typeList.size() > 0) {
            for(int i=0; i<typeList.size(); i++) {
                typeList.set(i, typeList.get(i).replace("_"," "));
            }
            logger.info("Type filter activated, type list: " + typeList.toString());
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.type",
                    new BasicDBObject("$in", typeList)));
        }
        return builder;
    }

    private QueryBuilder addClinvarSoTermFilter(QueryBuilder builder, QueryOptions options) {
        List<String> soList = (List<String>) options.get("so");
        if (soList != null && soList.size() > 0) {
            logger.info("So filter activated, SO list: " + soList.toString());
            builder = builder.and(new BasicDBObject("annot.consequenceTypes.soTerms.soName", new BasicDBObject("$in", soList)));
        }
        return builder;
    }

    private QueryBuilder addClinvarRsFilter(QueryBuilder builder, QueryOptions options) {
//        List<String> rsStringList = options.getAsStringList("rs", null);
        List<String> rsStringList = (List<String>) options.get("rs");
        if (rsStringList != null && rsStringList.size() > 0) {
            logger.info("rs filter activated, res list: "+rsStringList.toString());
            List<String> rsList = new ArrayList<>(rsStringList.size());
            for(String rsString : rsStringList) {
                rsList.add(rsString.substring(2));
            }
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                            new BasicDBObject("$in", rsList)));
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type",
                    "rs"));
        }
        return builder;
    }

    private QueryBuilder addClinvarRcvFilter(QueryBuilder builder, QueryOptions options) {
        List<String> rcvList = (List<String>) options.get("rcv");
        if (rcvList != null && rcvList.size() > 0) {
            logger.info("rcv filter activated, rcv list: "+rcvList.toString());
            builder = builder.and(new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                            new BasicDBObject("$in", rcvList)));
        }
        return builder;
    }

    @Deprecated
    private List<DBObject> addClinvarAggregationFilters(List<DBObject> pipeline, QueryOptions options) {
        List<DBObject> filterSteps = new ArrayList<>();
        filterSteps.add(new BasicDBObject("$match", new BasicDBObject("clinvarSet", new BasicDBObject("$exists", 1))));
        filterSteps = addClinvarRcvAggregationFilter(filterSteps, options);
        filterSteps = addClinvarRsAggregationFilter(filterSteps, options);
        filterSteps = addClinvarSoTermAggregationFilter(filterSteps, options);
        filterSteps = addClinvarTypeAggregationFilter(filterSteps, options);
        filterSteps = addClinvarReviewAggregationFilter(filterSteps, options);
        filterSteps = addClinvarClinicalSignificanceAggregationFilter(filterSteps, options);
        filterSteps = addClinvarRegionAggregationFilter(filterSteps, options);
        filterSteps = addClinvarGeneAggregationFilter(filterSteps, options);
        filterSteps = addClinvarPhenotypeAggregationFilter(filterSteps, options);

        // Filtering steps repeated twice to avoid undwind over all clinical records
        pipeline.addAll(filterSteps);
//        pipeline.add(new BasicDBObject("$unwind", "$clinvarList"));
//        pipeline.addAll(filterSteps);
        pipeline.add(new BasicDBObject("$limit", 100));

        return pipeline;
    }

    @Deprecated
    private List<DBObject> addClinvarRcvAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> rcvList = (List<String>) options.get("rcv");
        if (rcvList != null && rcvList.size() > 0) {
            logger.info("rcv filter activated, rcv list: "+rcvList.toString());
            filterSteps.add(new BasicDBObject("$match",
//                    new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                            new BasicDBObject("$in", rcvList))));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarRsAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
//        List<String> rsStringList = options.getAsStringList("rs", null);
        List<String> rsStringList = (List<String>) options.get("rs");
        if (rsStringList != null && rsStringList.size() > 0) {
            logger.info("rs filter activated, res list: "+rsStringList.toString());
            List<String> rsList = new ArrayList<>(rsStringList.size());
            for(String rsString : rsStringList) {
                rsList.add(rsString.substring(2));
            }
            filterSteps.add(new BasicDBObject("$match",
//                    new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                            new BasicDBObject("$in", rsList))));
            filterSteps.add(new BasicDBObject("$match",
//                    new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type",
                            "rs")));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarSoTermAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> soList = (List<String>) options.get("so");
        if (soList != null && soList.size() > 0) {
            logger.info("So filter activated, SO list: " + soList.toString());
            filterSteps.add(new BasicDBObject("$match",
                    new BasicDBObject("annot.consequenceTypes.soTerms.soName", new BasicDBObject("$in", soList))));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarTypeAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> typeList = (List<String>) options.get("type");
        if (typeList != null && typeList.size() > 0) {
            for(int i=0; i<typeList.size(); i++) {
                typeList.set(i, typeList.get(i).replace("_"," "));
            }
            logger.info("Type filter activated, type list: " + typeList.toString());
            filterSteps.add(new BasicDBObject("$match",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.type", new BasicDBObject("$in", typeList))));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarReviewAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> reviewStatusList = (List<String>) options.get("review");
        if (reviewStatusList != null && reviewStatusList.size() > 0) {
            for(int i=0; i<reviewStatusList.size(); i++) {
                reviewStatusList.set(i, reviewStatusList.get(i).toUpperCase());
            }
            logger.info("Review staus filter activated, review status list: " + reviewStatusList.toString());
            filterSteps.add(new BasicDBObject("$match",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus",
                            new BasicDBObject("$in", reviewStatusList))));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarClinicalSignificanceAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> clinicalSignificanceList = (List<String>) options.get("significance");
        if (clinicalSignificanceList != null && clinicalSignificanceList.size() > 0) {
            for(int i=0; i<clinicalSignificanceList.size(); i++) {
                clinicalSignificanceList.set(i, clinicalSignificanceList.get(i).replace("_"," "));
            }
            logger.info("Clinical significance filter activated, clinical significance list: " + clinicalSignificanceList.toString());
            filterSteps.add(new BasicDBObject("$match",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinicalSignificance.description",
                            new BasicDBObject("$in", clinicalSignificanceList))));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarGeneAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> geneList = (List<String>) options.get("gene");
//        List<String> geneList = options.getAsStringList("gene", null);
        if (geneList != null && geneList.size() > 0) {
            logger.info("gene filter activated, gene list: " + geneList.toString());
            filterSteps.add(new BasicDBObject("$match",
//                    new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value",
                    new BasicDBObject("clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value",
                            new BasicDBObject("$in", geneList))));
        }
        return filterSteps;
    }

    @Deprecated
    private List<DBObject> addClinvarPhenotypeAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> phenotypeList = (List<String>) options.getAsStringList("phenotype");
        if (phenotypeList != null && phenotypeList.size() > 0) {
            logger.info("phenotype filter activated, phenotype list: "+phenotypeList.toString());

//            filterSteps.add(new BasicDBObject("$match", new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value",
            filterSteps.add(new BasicDBObject("$match", new BasicDBObject("clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value",
                    new BasicDBObject("$in", getClinvarPhenotypeRegex(phenotypeList)))));

        }
        return filterSteps;
    }

    private List<Pattern> getClinvarPhenotypeRegex(List<String> phenotypeList) {
        List<Pattern> patternList = new ArrayList<>(phenotypeList.size());
        for(String keyword : phenotypeList) {
            patternList.add(Pattern.compile(".*" + keyword + ".*", Pattern.CASE_INSENSITIVE));
        }

        return patternList;
    }

    @Deprecated
    private List<DBObject> addClinvarRegionAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<Region> regionList = (List<Region>) options.get("region");
        if (regionList != null && regionList.size() > 0) {
            logger.info("region filter activated, region list: " + regionList.toString());
            filterSteps.add(getClinvarRegionAggregationFilterDBObject(regionList));

        }
        return filterSteps;
    }

    @Deprecated
    private DBObject getClinvarRegionAggregationFilterDBObject(List<Region> regionList) {
        BasicDBList orDBList = new BasicDBList();
        for(Region region : regionList) {
            BasicDBList andDBList = new BasicDBList();
            andDBList.add(new BasicDBObject("chromosome", region.getChromosome()));
            andDBList.add(new BasicDBObject("end", new BasicDBObject("$gte", region.getStart())));
            andDBList.add(new BasicDBObject("start", new BasicDBObject("$lte", region.getEnd())));
            orDBList.add(new BasicDBObject("$and",andDBList));
        }

        return new BasicDBObject("$match", new BasicDBObject("$or", orDBList));
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        //return getAllByRegion(new Region(chromosome, position, position), options);
        return new QueryResult();
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        //return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
        return new QueryResult();
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        //List<Region> regions = new ArrayList<>();
        //for (Position position : positionList) {
        //    regions.add(new Region(position.getChromosome(), position.getPosition(), position.getPosition()));
        //}
        //return getAllByRegionList(regions, options);
        return new ArrayList<>();
    }

//    @Override
//    public QueryResult getById(String id, QueryOptions options) {
//        return getAllByIdList(Arrays.asList(id), options).get(0);
//    }

//    @Override
//    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
////        if(includeContains((List<String>) options.get("include"), "clinvar")) {
////            return getAllClinvarByIdList(idList, options);
////        } else {
//            // TODO implement!
//            return new ArrayList<>();
////        }
//    }

    private Boolean includeContains(List<String> includeContent, String feature) {
        if(includeContent!=null) {
            int i = 0;
            while (i < includeContent.size() && !includeContent.get(i).equals(feature)) {
                i++;
            }
            if (i < includeContent.size()) {
                includeContent.remove(i);  // Avoid term "clinvar" (for instance) to be passed to datastore
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public QueryResult getAllByGenomicVariant(GenomicVariant variant, QueryOptions options) {
        return getAllByGenomicVariantList(Arrays.asList(variant), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByGenomicVariantList(List<GenomicVariant> variantList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(variantList.size());
        List<QueryResult> queryResultList;
        for (GenomicVariant genomicVariant : variantList){
            QueryBuilder builder = QueryBuilder.start("chromosome").is(genomicVariant.getChromosome()).
                    and("start").is(genomicVariant.getPosition()).and("alternate").is(genomicVariant.getAlternative());
            if (genomicVariant.getReference() != null){
                builder = builder.and("reference").is(genomicVariant.getReference());
            }
                    queries.add(builder.get());
            ids.add(genomicVariant.toString());
        }

        queryResultList = executeQueryList2(ids, queries, options);

        for (QueryResult queryResult : queryResultList){
            List<BasicDBObject> clinicalList = (List<BasicDBObject>) queryResult.getResult();

            List<Cosmic> cosmicList = null;
            List<Gwas> gwasList = null;
            List<Clinvar> clinvarList = null;

            for(Object clinicalObject: clinicalList) {
                BasicDBObject clinical = (BasicDBObject) clinicalObject;

                if (isCosmic(clinical)) {
                    Cosmic cosmic = getCosmic(clinical);
                    if (cosmicList == null) {
                        cosmicList = new ArrayList<>();
                    }
                    cosmicList.add(cosmic);
                } else if (isGwas(clinical)) {
                    Gwas gwas = getGwas(clinical);
                    if (gwasList == null) {
                        gwasList = new ArrayList<>();
                    }
                    gwasList.add(gwas);

                } else if (isClinvar(clinical)) {
                    Clinvar clinvar = getClinvar(clinical);
                    if (clinvarList == null) {
                        clinvarList = new ArrayList<>();
                    }
                    clinvarList.add(clinvar);
                }
            }
            Map<String, Object> clinicalData = new HashMap<>();
            clinicalData.put("Cosmic", cosmicList);
            clinicalData.put("Gwas", gwasList);
            clinicalData.put("Clinvar", clinvarList);
            // FIXME quick solution to compile
//            queryResult.setResult(clinicalData);
            queryResult.setResult(Arrays.asList(clinicalData));
            queryResult.setNumResults(1);
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
                histologySubtype, sampleSource, tumourOrigin ,geneName, mutationSomaticStatus);
    }

    private Gwas getGwas(BasicDBObject clinical) {
        String snpIdCurrent = (String) clinical.get("snpIdCurrent");
        Double riskAlleleFrequency =  (Double) clinical.get("riskAlleleFrequency");
        String reportedGenes = (String) clinical.get("reportedGenes");
        List<BasicDBObject> studiesObj = (List<BasicDBObject>) clinical.get("studies");
        Set<String> traitsSet = new HashSet<>();

        for (BasicDBObject studieObj: studiesObj) {
            List<BasicDBObject> traitsObj = (List<BasicDBObject>) studieObj.get("traits");
            for (BasicDBObject traitObj : traitsObj) {
                String trait =(String) traitObj.get("diseaseTrait");
                traitsSet.add(trait);
            }
        }

        List<String>  traits = new ArrayList<>();
        traits.addAll(traitsSet);
        return new Gwas(snpIdCurrent,traits,riskAlleleFrequency,reportedGenes);
    }

    private Clinvar getClinvar(BasicDBObject clinical) {
        BasicDBObject clinvarSet = (BasicDBObject) clinical.get("clinvarSet");
        BasicDBObject referenceClinVarAssertion = (BasicDBObject) clinvarSet.get("referenceClinVarAssertion");
        BasicDBObject clinVarAccession = (BasicDBObject) referenceClinVarAssertion.get("clinVarAccession");
        BasicDBObject clinicalSignificance = (BasicDBObject) referenceClinVarAssertion.get("clinicalSignificance");
        BasicDBObject measureSet = (BasicDBObject) referenceClinVarAssertion.get("measureSet");
        List<BasicDBObject> measures = (List<BasicDBObject>) measureSet.get("measure");
        BasicDBObject traitSet = (BasicDBObject) referenceClinVarAssertion.get("traitSet");
        List<BasicDBObject> traits = (List<BasicDBObject>) traitSet.get("trait");


        String acc = (String)  clinVarAccession.get("acc");
        String clinicalSignificanceName = (String) clinicalSignificance.get("description");
        String reviewStatus = (String) clinicalSignificance.get("reviewStatus");
        List <String> traitNames = new ArrayList<>();
        Set<String> geneNameSet = new HashSet<>();

        for (BasicDBObject measure : measures){
            List <BasicDBObject> measureRelationships;
            if((measureRelationships = (List<BasicDBObject>) measure.get("measureRelationship"))!=null) {
                for (BasicDBObject measureRelationship : measureRelationships) {
                    List<BasicDBObject> symbols = (List<BasicDBObject>) measureRelationship.get("symbol");
                    for (BasicDBObject symbol : symbols) {
                        BasicDBObject elementValue = (BasicDBObject) symbol.get("elementValue");
                        geneNameSet.add((String) elementValue.get("value"));
                    }
                }
            }
        }

        for (BasicDBObject trait : traits){
            List <BasicDBObject> names = (List<BasicDBObject>) trait.get("name");
            for (BasicDBObject name: names){
                BasicDBObject elementValue = (BasicDBObject) name.get("elementValue");
                traitNames.add((String) elementValue.get("value"));
            }
        }

        List<String>  geneNameList = new ArrayList<>();
        geneNameList.addAll(geneNameSet);
        return new Clinvar(acc,clinicalSignificanceName, traitNames, geneNameList, reviewStatus);
    }

    public QueryResult getListClinvarAccessions(QueryOptions queryOptions) {
        QueryBuilder builder = QueryBuilder.start("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc").exists(true);
        queryOptions.put("include", Arrays.asList("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc"));
        QueryResult queryResult = executeQuery("", builder.get(), queryOptions);
        List accInfoList = (List) queryResult.getResult();
        List<String> accList = new ArrayList<>(accInfoList.size());
        BasicDBObject accInfo;
        QueryResult listAccessionsToReturn = new QueryResult();

        for(Object accInfoObject: accInfoList) {
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
        ObjectWriter writer = jsonObjectMapper.writer();

        long start = System.nanoTime();
        for (VariantAnnotation variantAnnotation : variantAnnotations) {
            QueryBuilder builder = QueryBuilder.start("chromosome").is(variantAnnotation.getChromosome())
                    .and("start").is(variantAnnotation.getStart()).and("reference")
                    .is(variantAnnotation.getReferenceAllele())
                    .and("alternate").is(variantAnnotation.getAlternativeAllele());
            DBObject update = null;
            try {
                update = new BasicDBObject("$set", new BasicDBObject("annot",
                        JSON.parse(writer.writeValueAsString(variantAnnotation))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
//            DBObject update = new BasicDBObject("$set", new BasicDBObject("annotation",
//                    convertVariantAnnotation(variantAnnotation)));
            mongoDBCollection2.update(builder.get(), update, queryOptions);
        }

        return new QueryResult<>("", ((int) (System.nanoTime() - start)), 1, 1, "", "", new ArrayList());
    }

//    private DBObject convertVariantAnnotation(VariantAnnotation variantAnnotation) {
//        BasicDBObject basicDBObject = new BasicDBObject();
//
//        basicDBObject.put("")
//    }

    public List<QueryResult> getPhenotypeGeneRelations(QueryOptions queryOptions) {

        List<QueryResult> queryResultList = new ArrayList<>();
        if(!queryOptions.containsKey("include") || queryOptions.getAsStringList("include").equals("") ||
                includeContains(queryOptions.getAsStringList("include"), "clinvar")) {
            queryResultList.add(getClinvarPhenotypeGeneRelations(queryOptions));

        }
        if(!queryOptions.containsKey("include") || queryOptions.getAsStringList("include").equals("") ||
                includeContains(queryOptions.getAsStringList("include"), "cosmic")) {
            queryResultList.add(getCosmicPhenotypeGeneRelations(queryOptions));
        }

        return queryResultList;
    }

    private QueryResult getClinvarPhenotypeGeneRelations(QueryOptions queryOptions) {

        List<DBObject> pipeline = new ArrayList<>();
        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc", new BasicDBObject("$exists", 1))));
//        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarSet", new BasicDBObject("$exists", 1))));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.traitSet.trait"));
        pipeline.add(new BasicDBObject("$unwind", "$clinvarSet.referenceClinVarAssertion.traitSet.trait.name"));
        DBObject groupFields = new BasicDBObject();
        groupFields.put("_id","$clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value");
        groupFields.put("associatedGenes", new BasicDBObject("$addToSet", "$clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value"));
        pipeline.add(new BasicDBObject("$group", groupFields));
        DBObject fields = new BasicDBObject();
        fields.put("_id", 0);
        fields.put("phenotype", "$_id");
        fields.put("associatedGenes", 1);
        pipeline.add(new BasicDBObject("$project", fields));

        return executeAggregation2("", pipeline, queryOptions);

    }

    private QueryResult getCosmicPhenotypeGeneRelations(QueryOptions queryOptions) {

        List<DBObject> pipeline = new ArrayList<>();
        pipeline.add(new BasicDBObject("$match", new BasicDBObject("snpIdCurrent", new BasicDBObject("$exists", 1)))); // Select only GWAS documents
        pipeline.add(new BasicDBObject("$unwind", "$studies"));
        pipeline.add(new BasicDBObject("$unwind", "$studies.traits"));
        DBObject groupFields = new BasicDBObject();
        groupFields.put("_id","$studies.traits.diseaseTrait");
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
