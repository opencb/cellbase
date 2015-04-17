package org.opencb.cellbase.mongodb.db;

import com.mongodb.*;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.Clinvar;
import org.opencb.biodata.models.variant.annotation.Cosmic;
import org.opencb.biodata.models.variant.annotation.Gwas;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Position;

import org.opencb.cellbase.core.lib.api.variation.ClinicalDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

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
//        options.addToListOption("include", "clinvarList");
//        options.addToListOption("include", "chromosome");
//        options.addToListOption("include", "start");
//        options.addToListOption("include", "end");
//        options.addToListOption("include", "reference");
//        options.addToListOption("include", "alternate");
        List<DBObject> pipeline = new ArrayList<>();
        pipeline = addClinvarAggregationFilters(pipeline, options);
        DBObject fields = new BasicDBObject();
        fields.put("clinvarSet", 1);
        fields.put("chromosome", 1);
        fields.put("start", 1);
        fields.put("end", 1);
        fields.put("reference", 1);
        fields.put("alternate", 1);
        fields.put("annot", 1);
        pipeline.add(new BasicDBObject("$project", fields));


//        List<DBObject> pipeline = new ArrayList<>();
//        List<Object> idList = options.getList("id", null);
//        String idString = (String) idList.get(0);
////        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc", idString)));
//        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value", "APOE")));
//        pipeline.add(new BasicDBObject("$unwind", "$clinvarList"));
//        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value", "APOE")));

//        pipeline.add(new BasicDBObject("$match", new BasicDBObject("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc", idString)));

//        addClinvarQueryFilters(builder, options);


        return executeAggregation2("", pipeline, options);
//        return prepareClinvarQueryResultList(Collections.singletonList(executeQuery("result", builder.get(), options))).get(0);
    }

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

    private List<DBObject> addClinvarSoTermAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<String> soList = (List<String>) options.get("so");
        if (soList != null && soList.size() > 0) {
            logger.info("So filter activated, SO list: " + soList.toString());
            filterSteps.add(new BasicDBObject("$match",
                    new BasicDBObject("annot.consequenceTypes.soTerms.soName", new BasicDBObject("$in", soList))));
        }
        return filterSteps;
    }

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

    private List<DBObject> addClinvarRegionAggregationFilter(List<DBObject> filterSteps, QueryOptions options) {
        List<Region> regionList = (List<Region>) options.get("region");
        if (regionList != null && regionList.size() > 0) {
            logger.info("region filter activated, region list: " + regionList.toString());
            filterSteps.add(getClinvarRegionFilterDBObject(regionList));

        }
        return filterSteps;
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

    @Override
    public QueryResult getById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        if(includeContains((List<String>) options.get("include"), "clinvar")) {
            return getAllClinvarByIdList(idList, options);
        } else {
            // TODO implement!
            return new ArrayList<>();
        }
    }

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
    public QueryResult getClinvarById(String id, QueryOptions options) {
        return getAllClinvarByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllClinvarByIdList(List<String> idList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(idList.size());
        options.addToListOption("include", "clinvarList");
        options.addToListOption("include", "chromosome");
        options.addToListOption("include", "start");
        options.addToListOption("include", "end");
        options.addToListOption("include", "reference");
        options.addToListOption("include", "alternate");
        for (String id : idList) {
            if(id.toLowerCase().startsWith("rcv")) {
                QueryBuilder builder = addClinvarQueryFilters(QueryBuilder.start("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc").is(id),
                        options);
                queries.add(builder.get());
            } else if(id.toLowerCase().startsWith("rs")) {
                QueryBuilder builder = addClinvarQueryFilters(QueryBuilder.start("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.attributeSet.xref.type")
                        .is("rs").and("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.attributeSet.xref.id")
                                .is(id), options);
                queries.add(builder.get());
            }
        }
        return prepareClinvarQueryResultList(executeQueryList2(idList, queries, options));
    }

    @Override
    public QueryResult getClinvarByGene(String gene, QueryOptions options) {
        return getAllClinvarByGeneList(Arrays.asList(gene), options).get(0);
    }

    @Override
    public List<QueryResult> getAllClinvarByGeneList(List<String> geneList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(geneList.size());
        options.addToListOption("include", "clinvarList");
        options.addToListOption("include", "chromosome");
        options.addToListOption("include", "start");
        options.addToListOption("include", "end");
        options.addToListOption("include", "reference");
        options.addToListOption("include", "alternate");
        for (String gene : geneList) {
            QueryBuilder builder = addClinvarQueryFilters(QueryBuilder
                            .start("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value")
                            .is(gene), options);
            queries.add(builder.get());
        }

        return prepareClinvarQueryResultList(executeQueryList2(geneList, queries, options));
    }

    private QueryBuilder addClinvarQueryFilters(QueryBuilder builder, QueryOptions options) {
        builder = addClinvarGeneFilter(builder, options);
        builder = addClinvarIdFilter(builder, options);
        builder = addClinvarRegionFilter(builder, options);
        builder = addClinvarPhenotypeFilter(builder, options);

        return builder;
    }

    private QueryBuilder addClinvarPhenotypeFilter(QueryBuilder builder, QueryOptions options) {
        List<Object> phenotypeList = options.getList("phenotype", null);
        if (phenotypeList != null && phenotypeList.size() > 0) {
            QueryBuilder phenotypeQueryBuilder = QueryBuilder.start();
            for(Object phenotype : phenotypeList) {
                String phenotypeString = (String) phenotype;
                phenotypeQueryBuilder = phenotypeQueryBuilder.or(QueryBuilder.start("referenceClinVarAssertion.traitSet.trait.name.elementValue.value")
                        .text(phenotypeString).get());
            }
            builder = builder.and(phenotypeQueryBuilder.get());
        }
        return builder;
    }

    private QueryBuilder addClinvarRegionFilter(QueryBuilder builder, QueryOptions options) {
        List<Object> regions = options.getList("region", null);
        BasicDBList regionList = new BasicDBList();
        if (regions != null) {
            Region region = (Region) regions.get(0);
            QueryBuilder regionQueryBuilder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                    .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            for(int i=1; i<regions.size(); i++) {
                region = (Region) regions.get(i);
                regionQueryBuilder = regionQueryBuilder.or(QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd()).get());
            }
            builder = builder.and(regionQueryBuilder.get());
        }
        return builder;
    }

    private QueryBuilder addClinvarIdFilter(QueryBuilder builder, QueryOptions options) {
        List<Object> idList = options.getList("id", null);
        if (idList != null && idList.size() > 0) {
            QueryBuilder idQueryBuilder = QueryBuilder.start();
            for(Object id : idList) {
                String idString = (String) id;
                if(idString.toLowerCase().startsWith("rs")) {
                    idQueryBuilder = idQueryBuilder.or(QueryBuilder.start("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.attributeSet.xref.type").
                            is("rs").and("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.attributeSet.xref.id")
                            .is(Integer.valueOf(idString.substring(2))).get());
                } else if(idString.toLowerCase().startsWith("rcv")) {
                    idQueryBuilder = idQueryBuilder.or(QueryBuilder.start("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc")
                            .is(idString).get());
                }
            }
            builder = builder.and(idQueryBuilder.get());
        }
        return builder;
    }

    private QueryBuilder addClinvarGeneFilter(QueryBuilder builder, QueryOptions options) {
        List<Object> genes = options.getList("gene", null);
        BasicDBList geneSymbols = new BasicDBList();
        if (genes != null && genes.size() > 0) {
            geneSymbols.addAll(genes);
            builder = builder.and("clinvarList.clinvarSet.referenceClinVarAssertion.measureSet.measure.measureRelationship.symbol.elementValue.value").
                    in(geneSymbols);
        }
        return builder;
    }

    private List<QueryResult> prepareClinvarQueryResultList(List<QueryResult> clinicalQueryResultList) {
        List<QueryResult> queryResultList = new ArrayList<>();
        for(QueryResult clinicalQueryResult: clinicalQueryResultList) {
            QueryResult queryResult = new QueryResult();
            queryResult.setId(clinicalQueryResult.getId());
            queryResult.setDbTime(clinicalQueryResult.getDbTime());
            BasicDBList basicDBList = new BasicDBList();
            int numResults = 0;
            for (BasicDBObject clinicalRecord : (List<BasicDBObject>) clinicalQueryResult.getResult()) {
                if(clinicalRecord.containsKey("clinvarList")) {
                    basicDBList.add(clinicalRecord);
                    numResults += 1;
                }
            }
            queryResult.setResult(basicDBList);
            queryResult.setNumResults(numResults);
            queryResultList.add(queryResult);
        }
        return queryResultList;
    }

    @Override
    public QueryResult getAllClinvarByRegion(String chromosome, int start, int end, QueryOptions options) {
        return getAllClinvarByRegion(new Region(chromosome, start, end), options);
    }

    @Override
    public QueryResult getAllClinvarByRegion(Region region, QueryOptions options) {
        return getAllClinvarByRegionList(Arrays.asList(region), options).get(0);
    }

    @Override
    public List<QueryResult> getAllClinvarByRegionList(List<Region> regions, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>();

        options.addToListOption("include", "clinvarList");
        options.addToListOption("include", "chromosome");
        options.addToListOption("include", "start");
        options.addToListOption("include", "end");
        options.addToListOption("include", "reference");
        options.addToListOption("include", "alternate");
        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            // If regions is 1 position then query can be optimize using chunks
            QueryBuilder builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end").greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            builder = addClinvarQueryFilters(builder, options);
            System.out.println(builder.get().toString());
            queries.add(builder.get());
            ids.add(region.toString());
        }
        return prepareClinvarQueryResultList(executeQueryList2(ids, queries, options));
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
            Cosmic cosmic;
            Gwas gwas;
            Clinvar clinvar;
            Map<String, Object> clinicalData = new HashMap<>();
            List<Cosmic> cosmicLisit = new ArrayList<>();
            List<Gwas> gwasList = new ArrayList<>();
            List<Clinvar> clinvarList = new ArrayList<>();

            for(Object clinicalObject: clinicalList) {
                BasicDBObject clinical = (BasicDBObject) clinicalObject;
                List<BasicDBObject> consmicObjList = (List<BasicDBObject>) clinical.get("cosmicList");
                List<BasicDBObject> gwasObjList = (List<BasicDBObject>) clinical.get("gwasList");
                List<BasicDBObject> clinvarObjList = (List<BasicDBObject>) clinical.get("clinvarList");

                if (consmicObjList != null){
                    for (BasicDBObject cosmicObj : consmicObjList){

                        String mutationID = (String) cosmicObj.get("mutationID");
                        String primarySite = (String) cosmicObj.get("primarySite");
                        String siteSubtype = (String) cosmicObj.get("siteSubtype");
                        String primaryHistology = (String) cosmicObj.get("primaryHistology");
                        String histologySubtype = (String) cosmicObj.get("histologySubtype");
                        String sampleSource = (String) cosmicObj.get("sampleSource");
                        String tumourOrigin = (String) cosmicObj.get("tumourOrigin");
                        String geneName = (String) cosmicObj.get("geneName");
                        String mutationSomaticStatus = (String) cosmicObj.get("mutationSomaticStatus");

                        cosmic = new Cosmic(mutationID, primarySite, siteSubtype, primaryHistology,
                                histologySubtype, sampleSource, tumourOrigin ,geneName, mutationSomaticStatus);
                        cosmicLisit.add(cosmic);
                        clinicalData.put("Cosmic",cosmicLisit);
                    }
                }else {
                    clinicalData.put("Cosmic",null);
                }
                if (gwasObjList != null) {
                    for (BasicDBObject gwasObj : gwasObjList) {
                        String snpIdCurrent = (String) gwasObj.get("snpIdCurrent");
                        Double riskAlleleFrequency =  (Double) gwasObj.get("riskAlleleFrequency");
                        String reportedGenes = (String) gwasObj.get("reportedGenes");
                        List<BasicDBObject> studiesObj = (List<BasicDBObject>) gwasObj.get("studies");
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
                        gwas = new Gwas(snpIdCurrent,traits,riskAlleleFrequency,reportedGenes);

                        gwasList.add(gwas);
                        clinicalData.put("Gwas",gwasList);
                    }
                }else {
                    clinicalData.put("Gwas",null);
                }

                if (clinvarObjList != null ){
                    for (BasicDBObject clinvarObj : clinvarObjList){

                        BasicDBObject clinvarSet = (BasicDBObject) clinvarObj.get("clinvarSet");
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
                        Set <String> geneNameSet = new HashSet<>();

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
                        clinvar = new Clinvar(acc,clinicalSignificanceName, traitNames, geneNameList, reviewStatus);
                        clinvarList.add(clinvar);
                    }

                    clinicalData.put("Clinvar", clinvarList);
                }else {
                    clinicalData.put("Clinvar", null);
                }
            }

            // FIXME quick solution to compile
//            queryResult.setResult(clinicalData);
            queryResult.setResult(Arrays.asList(clinicalData));
        }

        return queryResultList;
    }

    public QueryResult getListClinvarAccessions(QueryOptions queryOptions) {
//        QueryBuilder builder = QueryBuilder.start("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc").exists(true);
        QueryBuilder builder = QueryBuilder.start("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc").exists(true);
//        queryOptions.put("include", Arrays.asList("clinvarList.clinvarSet.referenceClinVarAssertion.clinVarAccession.acc"));
        queryOptions.put("include", Arrays.asList("clinvarSet.referenceClinVarAssertion.clinVarAccession.acc"));
        QueryResult queryResult = executeQuery("", builder.get(), queryOptions);
        List accInfoList = (List) queryResult.getResult();
        List<String> accList = new ArrayList<>(accInfoList.size());
        BasicDBObject accInfo;
        QueryResult listAccessionsToReturn = new QueryResult();

        for(Object accInfoObject: accInfoList) {
            accInfo = (BasicDBObject) accInfoObject;
//            if(accInfo.containsKey("clinvarList")) {
                accInfo = (BasicDBObject) accInfo.get("clinvarSet");
                accList.add((String) ((BasicDBObject) ((BasicDBObject) ((BasicDBObject) accInfo
                        .get("referenceClinVarAssertion"))).get("clinVarAccession")).get("acc"));
//            }
        }

        // setting listAccessionsToReturn fields
        listAccessionsToReturn.setId(queryResult.getId());
        listAccessionsToReturn.setDbTime(queryResult.getDbTime());
        listAccessionsToReturn.setNumResults(queryResult.getNumResults());
        listAccessionsToReturn.setNumTotalResults(queryResult.getNumTotalResults());
        listAccessionsToReturn.setResult(accList);

        return listAccessionsToReturn;
    }

}
