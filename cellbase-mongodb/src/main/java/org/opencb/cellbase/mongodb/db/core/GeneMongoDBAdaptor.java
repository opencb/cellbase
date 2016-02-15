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

package org.opencb.cellbase.mongodb.db.core;

import com.mongodb.BasicDBList;
import com.mongodb.QueryBuilder;
import org.bson.Document;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

public class GeneMongoDBAdaptor extends MongoDBAdaptor implements GeneDBAdaptor {

    private int geneChunkSize = MongoDBCollectionConfiguration.GENE_CHUNK_SIZE;
    private ClinicalDBAdaptor clinicalDBAdaptor;

//    public GeneMongoDBAdaptor(DB db) {
//        super(db);
//    }

//    @Deprecated
//    public GeneMongoDBAdaptor(DB db, String species, String assembly) {
//        super(db, species, assembly);
//        mongoDBCollection = db.getCollection("gene");
//
//        logger.info("GeneMongoDBAdaptor: in 'constructor'");
//    }

    public GeneMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("GeneMongoDBAdaptor: in 'constructor'");
    }

    public ClinicalDBAdaptor getClinicalDBAdaptor() {
        return clinicalDBAdaptor;
    }

    public void setClinicalDBAdaptor(ClinicalDBAdaptor clinicalDBAdaptor) {
        this.clinicalDBAdaptor = clinicalDBAdaptor;
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
        QueryBuilder builder = new QueryBuilder();

        List<String> biotypes = options.getAsStringList("biotype");
        if (biotypes != null && biotypes.size() > 0) {
            BasicDBList biotypeIds = new BasicDBList();
            biotypeIds.addAll(biotypes);
            builder = builder.and("biotype").in(biotypeIds);
        }

        return executeQuery("result", new Document(builder.get().toMap()), options);
    }

    public QueryResult next(String id, QueryOptions options) {
        QueryOptions options1 = new QueryOptions();
        options1.put("include", Arrays.asList("chromosome", "start", "strand"));
        QueryResult queryResult = getAllById(id, options1);
        if (queryResult != null && queryResult.getResult() != null) {
            Document gene = (Document) queryResult.getResult().get(0);
            String chromosome = gene.get("chromosome").toString();
//            options.put("strand", gene.get("strand").toString());
            int start = Integer.parseInt(gene.get("start").toString());
            return next(chromosome, start, options);
        }
        return null;
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return next(chromosome, position + 1, options, mongoDBCollection);
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return getAllByIdList(Arrays.asList(id), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        List<Document> queries = new ArrayList<>(idList.size());
        for (String id : idList) {
            QueryBuilder builder = QueryBuilder.start("transcripts.xrefs.id").is(id);
            queries.add(new Document(builder.get().toMap()));
        }

//        options = addExcludeReturnFields("transcripts", options);
//        return executeQueryList(idList, queries, options);
        return executeQueryList2(idList, queries, options);
    }

    @Override
    public QueryResult getStatsById(String id, QueryOptions options) {

        Map<String, Object> stats = new HashMap<>();
        QueryResult queryResult = new QueryResult();
        queryResult.setId(id);

        QueryBuilder geneBuilder = QueryBuilder.start("transcripts.xrefs.id").is(id);
        long dbTimeStart = System.currentTimeMillis();
        QueryResult geneQueryResult = executeQuery(id, new Document(geneBuilder.get().toMap()), new QueryOptions());
        // TODO: clinical variant summary is only provided for ClinVar (hardcoded below)
        QueryOptions clinicalQueryOptions = new QueryOptions("source", "clinvar");
        clinicalQueryOptions.put("include",
                "annot.consequenceTypes.soTerms,clinvarSet.referenceClinVarAssertion.clinicalSignificance.description");
        QueryResult clinicalQueryResult = clinicalDBAdaptor.getByGeneId(id, clinicalQueryOptions);
        long dbTimeEnd = System.currentTimeMillis();
        queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());

        if (geneQueryResult.getNumResults() > 0) {
            queryResult.setNumResults(1);
            stats = setCoreGeneStats(geneQueryResult, stats);
            stats = setVariantStats(clinicalQueryResult, stats);
            queryResult.setResult(Collections.singletonList(stats));
        }

        return queryResult;
//        gene name
//        ensembl gene id
//        chr
//        start
//        end
//        sequence length
//        num transcripts
//        breakdown num transcripts by biotype
//        num exons
//        num drug interactions
//        Clinical Variants {
//            #
//            Breakdown by clinical significance
//            Breakdown by SO
//        }


//        options = addExcludeReturnFields("transcripts", options);
//        return executeQueryList(idList, queries, options);

    }

    private Map<String, Object> setVariantStats(QueryResult queryResult, Map<String, Object> stats) {
        if (queryResult != null && queryResult.getNumResults() > 0) {
            Map<String, Map> clinicalVariantStats = new HashMap<>();
            Map<String, Integer> clinicalSignificanceSummary = new HashMap<>();
            Map<String, Map> soSummary = new HashMap<>();
            for (Object result : queryResult.getResult()) {
                clinicalSignificanceSummary = updateClinicalSignificanceSummary((Document) result,
                        clinicalSignificanceSummary);
                soSummary = updateSoSummary((Document) result, soSummary);
            }
            clinicalVariantStats.put("clinicalSignificanceSummary", clinicalSignificanceSummary);
            clinicalVariantStats.put("soSummary", soSummary);
            stats.put("clinicalVariantStats", clinicalVariantStats);
        }
        return stats;
    }

    private Map<String, Map> updateSoSummary(Document result, Map<String, Map> soSummary) {
        Document document = (Document) result.get("annot");
        if (document != null) {
            BasicDBList basicDBList = (BasicDBList) document.get("consequenceTypes");
            if (basicDBList != null) {
                document = getMostSevereSOTerm(basicDBList);
                if (document != null) {
                    String soAccesion = (String) document.get("soAccession");
                    if (soSummary.containsKey(soAccesion)) {
                        Integer currentCount = (Integer) soSummary.get(soAccesion).get("count");
                        soSummary.get(soAccesion).put("count", currentCount + 1);
                    } else {
                        String soName = (String) document.get("soName");
                        Map<String, Object> soSummaryMap = new HashMap<>(2);
                        soSummaryMap.put("soName", soName);
                        soSummaryMap.put("count", 1);
                        soSummary.put(soAccesion, soSummaryMap);
                    }
                }
            }
        }
        return soSummary;
    }

    private Document getMostSevereSOTerm(BasicDBList consequenceTypeDBList) {
        Document mostSevereSODBObject = null;
        Integer maxSeverity = 0;
        for (Object consequenceTypeObject : consequenceTypeDBList) {
            BasicDBList soDBList = (BasicDBList) ((Document) consequenceTypeObject).get("soTerms");
            if (soDBList != null) {
                for (Object soObject : soDBList) {
                    Document soDBObject = (Document) soObject;
                    String soName = (String) soDBObject.get("soName");
                    if (VariantAnnotationUtils.SO_SEVERITY.containsKey(soName)) {
                        Integer severity = VariantAnnotationUtils.SO_SEVERITY.get(soName);
                        if (severity > maxSeverity) {
                            maxSeverity = severity;
                            mostSevereSODBObject = soDBObject;
                        }
                    }
                }
            }
        }
        return mostSevereSODBObject;
    }

    private Map<String, Integer> updateClinicalSignificanceSummary(Document result, Map<String, Integer> clinicalSignificanceSummary) {
        Document document = (Document) result.get("clinvarSet");
        if (document != null) {
            document = (Document) document.get("referenceClinVarAssertion");
            if (document != null) {
                document = (Document) document.get("clinicalSignificance");
                if (document != null) {
                    String clinicalSignificance = (String) document.get("description");
                    if (clinicalSignificance != null) {
                        if (clinicalSignificanceSummary.containsKey(clinicalSignificance)) {
                            clinicalSignificanceSummary
                                    .put(clinicalSignificance, clinicalSignificanceSummary.get(clinicalSignificance) + 1);
                        } else {
                            clinicalSignificanceSummary.put(clinicalSignificance, 1);
                        }
                    }
                }
            }
        }
        return clinicalSignificanceSummary;
    }

    private Map<String, Object> setCoreGeneStats(QueryResult queryResult, Map<String, Object> stats) {

        Document resultDBObject = (Document) queryResult.getResult().get(0);
        stats.put("name", resultDBObject.get("name"));
        stats.put("id", resultDBObject.get("id"));
        stats.put("chromosome", resultDBObject.get("chromosome"));
        int start = (int) resultDBObject.get("start");
        stats.put("start", start);
        int end = (int) resultDBObject.get("end");
        stats.put("start", end);
        stats.put("length", end - start + 1);

        return stats;
    }

    @Override
    public QueryResult getAllByXref(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllBiotypes(QueryOptions options) {
        Document query = null;

        if (options != null && options.get("chromosome") != null) {
            query = new Document(QueryBuilder.start("chromosome").is(options.get("chromosome")).get().toMap());
        }
        return executeDistinct("distinct", "biotype", query);
    }

    @Override
    public QueryResult getAllTargetsByTf(String tfId, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public List<QueryResult> getAllTargetsByTfList(List<String> tfIdList, QueryOptions queryOptions) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
        List<Document> queries = new ArrayList<>();

        List<Object> biotypes = options.getList("biotype", null);
        BasicDBList biotypeIds = new BasicDBList();
        if (biotypes != null && biotypes.size() > 0) {
            biotypeIds.addAll(biotypes);
        }

        List<String> ids = new ArrayList<>(regions.size());
        for (Region region : regions) {

            QueryBuilder builder = null;
            // If regions is 1 position then query can be optimize using chunks
            if (region.getStart() == region.getEnd()) {
                builder = QueryBuilder.start("_chunkIds")
                        .is(getChunkIdPrefix(region.getChromosome(), region.getStart(), geneChunkSize)).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            } else {
                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            }

            if (biotypeIds.size() > 0) {
                builder = builder.and("biotype").in(biotypeIds);
            }
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }
        return executeQueryList2(ids, queries, options);
//        return executeQueryList(ids, queries, options);
    }

    @Override
    public QueryResult getIntervalFrequencies(Region region, QueryOptions queryOptions) {
        return super.getIntervalFrequencies(region, queryOptions);
    }

    @Override
    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions queryOptions) {
        return super.getAllIntervalFrequencies(regions, queryOptions);
    }

    public int insert(List objectList) {
        return -1;
    }

    public int update(List objectList, String field) {
        return -1;
    }

}
