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

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.GenomeQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

/**
 * Created by imedina on 07/12/15.
 */
public class GenomeMongoDBAdaptor extends CellBaseDBAdaptor implements CellBaseCoreDBAdaptor<GenomeQuery, Chromosome> {

    private Map<Integer, MongoDBCollection> genomeInfoMongoDBCollectionByRelease;
    private Map<Integer, MongoDBCollection> conservationMongoDBCollectionByRelease;
    private static final Object CYTOBANDS = "cytobands";
    private static final Object START = "start";
    private static final String END = "end";
    private static final String STAIN = "stain";
    private static final String NAME = "name";
    private static final Object CHROMOSOMES = "chromosomes";
    private Document genomeInfo = null;

    public GenomeMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("GenomeMongoDBAdaptor: in 'constructor'");

        genomeInfoMongoDBCollectionByRelease = buildCollectionByReleaseMap("genome_info");
        mongoDBCollectionByRelease = buildCollectionByReleaseMap("genome_sequence");
        conservationMongoDBCollectionByRelease = buildCollectionByReleaseMap("conservation");
    }

    public CellBaseDataResult getGenomeInfo(QueryOptions queryOptions, int dataRelease) throws CellBaseException {
        MongoDBCollection mongoDBCollection = getCollectionByRelease(genomeInfoMongoDBCollectionByRelease, dataRelease);
        return new CellBaseDataResult<>(mongoDBCollection.find(new Document(), queryOptions));
    }

    public CellBaseDataResult getChromosomeInfo(String chromosomeId, QueryOptions queryOptions, int dataRelease) throws CellBaseException {
        if (queryOptions == null) {
            queryOptions = new QueryOptions("include", Collections.singletonList("chromosomes.$"));
        } else {
            queryOptions.addToListOption("include", "chromosomes.$");
        }
        Document dbObject = new Document("chromosomes", new Document("$elemMatch", new Document("name", chromosomeId)));
        MongoDBCollection mongoDBCollection = getCollectionByRelease(genomeInfoMongoDBCollectionByRelease, dataRelease);
        return executeQuery(chromosomeId, dbObject, queryOptions, mongoDBCollection);
    }

    public CellBaseDataResult<Cytoband> getCytobands(Region region, QueryOptions queryOptions, int dataRelease) throws CellBaseException {
        List<Cytoband> cytobandList = new ArrayList<>();
        long dbStartTime = System.currentTimeMillis();
        long dbTime = System.currentTimeMillis() - dbStartTime;
        GenomeQuery query = new GenomeQuery();
        query.setNames(Collections.singletonList(region.getChromosome()));
        query.setDataRelease(dataRelease);
        CellBaseDataResult<Chromosome> chromosomeInfo = query(query);
        // May not have info for specified chromosome, e.g. 17_KI270729v1_random
        if (chromosomeInfo != null && chromosomeInfo.getResults() != null && !chromosomeInfo.getResults().isEmpty()) {
            Chromosome chromosome = chromosomeInfo.getResults().get(0);
            List<Cytoband> results = chromosome.getCytobands();
            for (Cytoband cytoband : results) {
                if (cytoband.getEnd() >= region.getStart() && cytoband.getStart() <= region.getEnd()) {
                    cytoband.setChromosome(chromosome.getName());
                    cytobandList.add(cytoband);
                }
            }
//            int i = 0;
//            while (i < cytobandDocumentList.size() && ((int) cytobandDocumentList.get(i).get(START)) <= region.getEnd()) {
//                if (((int) cytobandDocumentList.get(i).get(END)) >= region.getStart()) {
//                    cytobandList.add(new Cytoband(region.getChromosome(),
//                            (String) cytobandDocumentList.get(i).get(STAIN),
//                            (String) cytobandDocumentList.get(i).get(NAME),
//                            (Integer) cytobandDocumentList.get(i).get(START),
//                            (Integer) cytobandDocumentList.get(i).get(END)));
//                }
//                i++;
//            }
        }
        return new CellBaseDataResult<>(region.toString(), (int) dbTime, Collections.emptyList(), cytobandList.size(), cytobandList,
                cytobandList.size());

    }

    public List<CellBaseDataResult<Cytoband>> getCytobands(List<Region> regionList, int dataRelease) throws CellBaseException {
        return getCytobands(regionList, null, dataRelease);
    }

    public List<CellBaseDataResult<Cytoband>> getCytobands(List<Region> regionList, QueryOptions queryOptions, int dataRelease)
            throws CellBaseException {
        List<CellBaseDataResult<Cytoband>> cellBaseDataResultList = new ArrayList<>(regionList.size());
        for (Region region : regionList) {
            cellBaseDataResultList.add(getCytobands(region, queryOptions, dataRelease));
        }
        return cellBaseDataResultList;
    }

//    private Document getOneChromosomeInfo(String chromosome) {
//        Document genomeInfoVariable = getGenomeInfoVariable();
//        if (genomeInfoVariable != null) {
//            for (Document document : (List<Document>) genomeInfoVariable.get(CHROMOSOMES)) {
//                if (document.get(NAME).equals(chromosome)) {
//                    return document;
//                }
//            }
//        }
//        return null;
//    }

//    private Document getGenomeInfoVariable() {
//        if (genomeInfo == null) {
//            CellBaseDataResult<Document> cellBaseDataResult = new CellBaseDataResult<>(
//                    genomeInfoMongoDBCollection.find(new Document(), null));
//            if (cellBaseDataResult.getNumResults() > 0) {
//                genomeInfo = genomeInfoMongoDBCollection.find(new Document(), null).getResults().get(0);
//                for (Document chromosomeDocument : (List<Document>) genomeInfo.get(CHROMOSOMES)) {
//                    ((List<Document>) chromosomeDocument.get(CYTOBANDS))
//                            .sort(Comparator.comparingInt(c -> (int) c.get(START)));
//                }
//            }
//        }
//        return genomeInfo;
//    }

    @Deprecated
    public CellBaseDataResult<GenomeSequenceFeature> getGenomicSequence(Query query, QueryOptions queryOptions, int dataRelease)
            throws CellBaseException {
        return getSequence(Region.parseRegion(query.getString("region")), queryOptions, dataRelease);
    }

    public CellBaseDataResult<GenomeSequenceFeature> getSequence(Region region, QueryOptions queryOptions, int dataRelease)
            throws CellBaseException {
        Query query = new Query("region", region.toString());
        CellBaseDataResult<Document> cellBaseDataResult = nativeGet(query, queryOptions, dataRelease);
        List<Document> cellBaseDataResultList = cellBaseDataResult.getResults();

        CellBaseDataResult<GenomeSequenceFeature> result = new CellBaseDataResult(region.toString());

        if (cellBaseDataResultList != null && !cellBaseDataResultList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Document document : cellBaseDataResult.getResults()) {
                stringBuilder.append(document.getString("sequence"));
            }

            // The first chunk does contain 1 nt less than the rest and is 0-indexed - The rest of chunks contain
            // GENOME_SEQUENCE_CHUNK_SIZE nts and are 1 indexed (position 0 contains the GENOME_SEQUENCE_CHUNK_SIZE) nt
            int startIndex = (region.getStart() < MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE)
                    ? (region.getStart() - 1) % MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE
                    : region.getStart() % MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE;
            int length = region.getEnd() - region.getStart() + 1;
            // If end is out of the right boundary, there will be no chunks containing the right boundary. This means the
            // length of stringBuilder will be < than "end", since the for above will have just appended the chunks
            // available
            String sequence = stringBuilder
                    .toString()
                    .substring(startIndex, Math.min(startIndex + length, stringBuilder.length()));

            String strand = "1";
            String queryStrand= (query.getString("strand") != null) ? query.getString("strand") : "1";
            if (queryStrand.equals("-1") || queryStrand.equals("-")) {
                sequence = VariantAnnotationUtils.reverseComplement(sequence);
                strand = "-1";
            }

            String sequenceType = cellBaseDataResult.getResults().get(0).getString("sequenceType");
            String assembly = cellBaseDataResult.getResults().get(0).getString("assembly");

            result.setResults(Collections.singletonList(new GenomeSequenceFeature(
                    region.getChromosome(), region.getStart(), region.getEnd(), Integer.parseInt(strand), sequenceType, assembly, sequence)
            ));
            result.setNumMatches(1);
            result.setNumResults(1);
        }
        return result;
    }

    public List<CellBaseDataResult<GenomicScoreRegion<Float>>> getConservation(List<Region> regionList, QueryOptions options,
                                                                               int dataRelease) throws CellBaseException {
        //TODO not finished yet
        List<Document> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regionList.size());
        List<String> integerChunkIds;

        List<Region> regions = regionList;
        for (Region region : regions) {
            integerChunkIds = new ArrayList<>();
            // positions below 1 are not allowed
            if (region.getStart() < 1) {
                region.setStart(1);
            }
            if (region.getEnd() < 1) {
                region.setEnd(1);
            }

            // Max region size is 10000bp
            if (region.getEnd() - region.getStart() > 10000) {
                region.setEnd(region.getStart() + 10000);
            }

//            QueryBuilder builder;
            Document query= new Document();
            int regionChunkStart = getChunkId(region.getStart(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            int regionChunkEnd = getChunkId(region.getEnd(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            if (regionChunkStart == regionChunkEnd) {
//                builder = QueryBuilder.start("_chunkIds")
//                        .is(getChunkIdPrefix(region.getChromosome(), region.getStart(),
//                                MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
                query.append("_chunkIds", getChunkIdPrefix(region.getChromosome(), region.getStart(),
                        MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
            } else {
//                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
//                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
                query.append("chromosome", region.getChromosome())
                        .append("end", new Document("$gte", region.getStart()))
                        .append("start", new Document("$lte", region.getEnd()));
            }
            queries.add(query);
            ids.add(region.toString());
        }

        MongoDBCollection mongoDBCollection = getCollectionByRelease(conservationMongoDBCollectionByRelease, dataRelease);
        List<CellBaseDataResult> cellBaseDataResults = executeQueryList2(ids, queries, options, mongoDBCollection);
        List<CellBaseDataResult<GenomicScoreRegion<Float>>> conservationCellBaseDataResults = new ArrayList<>();

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            CellBaseDataResult cellBaseDataResult = cellBaseDataResults.get(i);
            CellBaseDataResult<GenomicScoreRegion<Float>> conservationCellBaseDataResult = new CellBaseDataResult<>();
            List list = cellBaseDataResult.getResults();

            Map<String, List<Float>> typeMap = new HashMap<>();
            for (int j = 0; j < list.size(); j++) {
                Document chunk = (Document) list.get(j);
                String source = chunk.getString("source");
                List<Float> valuesList;
                if (!typeMap.containsKey(source)) {
                    valuesList = new ArrayList<>(region.getEnd() - region.getStart() + 1);
                    for (int val = 0; val < region.getEnd() - region.getStart() + 1; val++) {
                        valuesList.add(null);
                    }
                    typeMap.put(source, valuesList);
                } else {
                    valuesList = typeMap.get(source);
                }

                ArrayList valuesChunk = chunk.get("values", ArrayList.class);

                int pos = 0;
                if (region.getStart() > chunk.getInteger("start")) {
                    pos = region.getStart() - chunk.getInteger("start");
                }

                for (; pos < valuesChunk.size() && (pos + chunk.getInteger("start") <= region.getEnd()); pos++) {
                    valuesList.set(pos + chunk.getInteger("start") - region.getStart(), new Float((Double) valuesChunk.get(pos)));
                }
            }
            List<GenomicScoreRegion<Float>> resultList = new ArrayList<>();
            GenomicScoreRegion<Float> conservedRegionChunk;
            for (Map.Entry<String, List<Float>> elem : typeMap.entrySet()) {
                conservedRegionChunk = new GenomicScoreRegion<>(region.getChromosome(), region.getStart(), region.getEnd(),
                        elem.getKey(), elem.getValue());
                resultList.add(conservedRegionChunk);
            }
            conservationCellBaseDataResult.setResults(resultList);
            conservationCellBaseDataResult.setNumResults(resultList.size());
            conservationCellBaseDataResult.setNumMatches(-1);
            conservationCellBaseDataResults.add(conservationCellBaseDataResult);

        }

        return conservationCellBaseDataResults;
    }

    public List<CellBaseDataResult<Score>> getAllScoresByRegionList(List<Region> regionList, QueryOptions options, int dataRelease)
            throws CellBaseException {
        //TODO not finished yet
        List<Document> queries = new ArrayList<>();
        List<String> ids = new ArrayList<>(regionList.size());
        List<CellBaseDataResult<Score>> allScoresByRegionList = new ArrayList<>();

        List<Region> regions = regionList;
        for (Region region : regions) {
            // positions below 1 are not allowed
            if (region.getStart() < 1) {
                region.setStart(1);
            }
            if (region.getEnd() < 1) {
                region.setEnd(1);
            }

//            QueryBuilder builder;
            Document query = new Document();
            int regionChunkStart = getChunkId(region.getStart(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            int regionChunkEnd = getChunkId(region.getEnd(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            if (regionChunkStart == regionChunkEnd) {
//                builder = QueryBuilder.start("_chunkIds")
//                        .is(getChunkIdPrefix(region.getChromosome(), region.getStart(),
//                                MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
                query.append("_chunkIds", getChunkIdPrefix(region.getChromosome(), region.getStart(),
                        MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
            } else {
//                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
//                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
                query.append("chromosome", region.getChromosome())
                        .append("end", new Document("$gte", region.getStart()))
                        .append("start", new Document("$lte", region.getEnd()));
            }

            queries.add(query);
            ids.add(region.toString());
        }

        MongoDBCollection mongoDBCollection = getCollectionByRelease(conservationMongoDBCollectionByRelease, dataRelease);
        List<CellBaseDataResult> queryResults = executeQueryList2(ids, queries, options, mongoDBCollection);
//        List<QueryResult> queryResults = executeQueryList(ids, queries, options);

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            CellBaseDataResult queryResult = queryResults.get(i);
            List<Document> list = (List<Document>) queryResult.getResults();
            Map<String, List<Float>> typeMap = new HashMap();

            for (int j = 0; j < list.size(); j++) {
                Document chunk = list.get(j);
                if (!chunk.isEmpty()) {
                    ArrayList valuesChunk = chunk.get("values", ArrayList.class);

                    String source = chunk.getString("source");
                    List<Float> valuesList;
                    if (!typeMap.containsKey(source)) {
                        valuesList = new ArrayList<>(region.getEnd() - region.getStart() + 1);
                        for (int val = 0; val < region.getEnd() - region.getStart() + 1; val++) {
                            valuesList.add(null);
                        }
                        typeMap.put(source, valuesList);
                    } else {
                        valuesList = typeMap.get(source);
                    }

                    valuesChunk = chunk.get("values", ArrayList.class);
                    int pos = 0;
                    if (region.getStart() > chunk.getInteger("start")) {
                        pos = region.getStart() - chunk.getInteger("start");
                    }

                    for (; pos < valuesChunk.size() && (pos + chunk.getInteger("start") <= region.getEnd()); pos++) {
                        valuesList.set(pos + chunk.getInteger("start") - region.getStart(), new Float((Double) valuesChunk.get(pos)));
                    }
                } else {
                    logger.error("values field not present in conservation chunk document. This "
                            + "should not be happening - every conservation chunk must have a list of values."
                            + " Please check. Chunk id: " + chunk.get("_chunkIds"));
                }
            }
            List<Score> resultList = new ArrayList<>();
            for (Map.Entry<String, List<Float>> elem : typeMap.entrySet()) {
                for (Float score : elem.getValue()) {
                    if (score != null) {
                        resultList.add(new Score(new Double(score), elem.getKey(), null));
                    }
                }
            }
            CellBaseDataResult<Score> result = new CellBaseDataResult<>();
            if (!resultList.isEmpty()) {
                result.setResults(resultList);
            } else {
                result.setResults(null);
            }
            allScoresByRegionList.add(result);
        }

        return allScoresByRegionList;
    }

    @Deprecated
    public CellBaseDataResult nativeGet(Query query, QueryOptions options, int dataRelease) throws CellBaseException {
        Bson bson = parseQuery(query);
        logger.debug("query: {}", bson.toBsonDocument().toJson());
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, options));
    }

    @Deprecated
    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, ParamConstants.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE, andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    @Override
    public CellBaseIterator<Chromosome> iterator(GenomeQuery query) throws CellBaseException {
        QueryOptions queryOptions = query.toQueryOptions();
        List<Bson> pipeline = unwind(query);
        GenericDocumentComplexConverter<Chromosome> converter = new GenericDocumentComplexConverter<>(Chromosome.class);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(genomeInfoMongoDBCollectionByRelease, query.getDataRelease());
        MongoDBIterator<Chromosome> iterator = mongoDBCollection.iterator(pipeline, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    public List<Bson> unwind(GenomeQuery query) {
        Bson bson = parseQuery(query);
        Bson match = Aggregates.match(bson);

        Bson project = Aggregates.project(Projections.include("chromosomes"));
        Bson unwind = Aggregates.unwind("$chromosomes");

        // This project the fields of Chromosome to the top of the object
        Document document = new Document("name", "$chromosomes.name");
        document.put("start", "$chromosomes.start");
        document.put("end", "$chromosomes.end");
        document.put("size", "$chromosomes.size");
        document.put("isCircular", "$chromosomes.isCircular");
        document.put("numberGenes", "$chromosomes.numberGenes");
        document.put("cytobands", "$chromosomes.cytobands");

        Bson project1 = Aggregates.project(document);

        Bson match2 = Aggregates.match(bson);

        List<Bson> aggregateList = new ArrayList<>();

        aggregateList.add(match);
        aggregateList.add(project);
        aggregateList.add(unwind);
        aggregateList.add(match2);
        aggregateList.add(project1);

        return aggregateList;
    }

    @Override
    public CellBaseDataResult<Long> count(GenomeQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(GenomeQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(GenomeQuery query) throws CellBaseException {
        Bson bsonQuery = parseQuery(query);
        logger.info("query: {}", bsonQuery.toBsonDocument().toJson());
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return groupBy(bsonQuery, query, "name", mongoDBCollection);
    }

    @Override
    public CellBaseDataResult<String> distinct(GenomeQuery query) throws CellBaseException {
        Bson bsonDocument = parseQuery(query);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(genomeInfoMongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument, String.class));
    }

    @Override
    public List<CellBaseDataResult<Chromosome>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String token)
            throws CellBaseException {
        List<CellBaseDataResult<Chromosome>> results = new ArrayList<>();
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        for (String id : ids) {
            Bson projection = getProjection(queryOptions);
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            orBsonList.add(Filters.eq("name", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, Chromosome.class, new QueryOptions())));
        }
        return results;
    }

    public Bson parseQuery(GenomeQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "name":
                        createAndOrQuery(value, "chromosomes.name", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "dataRelease":
                    case "token":
                        // Do nothing
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.debug("genome region parsed query: {}", andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
