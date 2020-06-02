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

import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
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
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.core.api.queries.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.GenomeQuery;
import org.opencb.cellbase.core.common.DNASequenceUtils;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
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
public class GenomeMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<GenomeQuery, Chromosome> {

    private MongoDBCollection genomeInfoMongoDBCollection;
    private MongoDBCollection conservationMongoDBCollection;
    private static final Object CYTOBANDS = "cytobands";
    private static final Object START = "start";
    private static final String END = "end";
    private static final String STAIN = "stain";
    private static final String NAME = "name";
    private static final Object CHROMOSOMES = "chromosomes";
    private Document genomeInfo = null;

    public GenomeMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);

        genomeInfoMongoDBCollection = mongoDataStore.getCollection("genome_info");
        mongoDBCollection = mongoDataStore.getCollection("genome_sequence");
        conservationMongoDBCollection = mongoDataStore.getCollection("conservation");

        logger.debug("GenomeMongoDBAdaptor: in 'constructor'");
    }

    public CellBaseDataResult getGenomeInfo(QueryOptions queryOptions) {
        return new CellBaseDataResult<>(genomeInfoMongoDBCollection.find(new Document(), queryOptions));
    }

    public CellBaseDataResult getChromosomeInfo(String chromosomeId, QueryOptions queryOptions) {
        if (queryOptions == null) {
            queryOptions = new QueryOptions("include", Collections.singletonList("chromosomes.$"));
        } else {
            queryOptions.addToListOption("include", "chromosomes.$");
        }
        Document dbObject = new Document("chromosomes", new Document("$elemMatch", new Document("name", chromosomeId)));
        return executeQuery(chromosomeId, dbObject, queryOptions, genomeInfoMongoDBCollection);
    }

    public CellBaseDataResult<Cytoband> getCytobands(Region region, QueryOptions queryOptions) {
        List<Cytoband> cytobandList = new ArrayList<>();
        long dbStartTime = System.currentTimeMillis();
        long dbTime = System.currentTimeMillis() - dbStartTime;
        GenomeQuery query = new GenomeQuery();
        query.setNames(Collections.singletonList(region.getChromosome()));
        CellBaseDataResult<Chromosome> chromosomeInfo = query(query);
        // May not have info for specified chromosome, e.g. 17_KI270729v1_random
        if (chromosomeInfo != null && chromosomeInfo.getResults() != null && !chromosomeInfo.getResults().isEmpty()) {
            Chromosome chromosome = chromosomeInfo.getResults().get(0);
            logger.error("chromosome " + chromosome.toString());
            List<Cytoband> results = chromosome.getCytobands();
            for (Cytoband cytoband : results) {
                if (cytoband.getEnd() >= region.getStart() && cytoband.getStart() <= region.getEnd()) {
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

    public List<CellBaseDataResult<Cytoband>> getCytobands(List<Region> regionList) {
        return getCytobands(regionList, null);
    }

    public List<CellBaseDataResult<Cytoband>> getCytobands(List<Region> regionList, QueryOptions queryOptions) {
        List<CellBaseDataResult<Cytoband>> cellBaseDataResultList = new ArrayList<>(regionList.size());
        for (Region region : regionList) {
            cellBaseDataResultList.add(getCytobands(region, queryOptions));
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
    public CellBaseDataResult<GenomeSequenceFeature> getGenomicSequence(Query query, QueryOptions queryOptions) {
        return getSequence(Region.parseRegion(query.getString("region")), queryOptions);
    }

    public CellBaseDataResult<GenomeSequenceFeature> getSequence(Region region, QueryOptions queryOptions) {
        Query query = new Query("region", region.toString());
        CellBaseDataResult<Document> cellBaseDataResult = nativeGet(query, queryOptions);
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
                sequence = DNASequenceUtils.reverseComplement(sequence);
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

    public List<CellBaseDataResult<GenomicScoreRegion<Float>>> getConservation(List<Region> regionList, QueryOptions options) {
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

            QueryBuilder builder;
            int regionChunkStart = getChunkId(region.getStart(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            int regionChunkEnd = getChunkId(region.getEnd(), MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE);
            if (regionChunkStart == regionChunkEnd) {
                builder = QueryBuilder.start("_chunkIds")
                        .is(getChunkIdPrefix(region.getChromosome(), region.getStart(),
                                MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE));
            } else {
                builder = QueryBuilder.start("chromosome").is(region.getChromosome()).and("end")
                        .greaterThanEquals(region.getStart()).and("start").lessThanEquals(region.getEnd());
            }
            queries.add(new Document(builder.get().toMap()));
            ids.add(region.toString());
        }

        List<CellBaseDataResult> cellBaseDataResults = executeQueryList2(ids, queries, options, conservationMongoDBCollection);
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
            conservationCellBaseDataResults.add(conservationCellBaseDataResult);
        }

        return conservationCellBaseDataResults;
    }

//    public CellBaseDataResult<Long> count(Query query) {
//        Bson bson = parseQuery(query);
//        return new CellBaseDataResult<>(mongoDBCollection.count(bson));
//    }
//
//    public CellBaseDataResult distinct(Query query, String field) {
//        Bson bson = parseQuery(query);
//        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bson));
//    }

//    @Override
//    public CellBaseDataResult stats(Query query) {
//        return null;
//    }

//    public CellBaseDataResult search(Query query) {
//        return null;
//    }

    @Deprecated
    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, options));
    }

//    public Iterator iterator(Query query, QueryOptions options) {
//        return null;
//    }

//    public Iterator nativeIterator(Query query, QueryOptions options) {
//        Bson bson = parseQuery(query);
//        return mongoDBCollection.nativeQuery().find(bson, options);
//    }

//    @Override
//    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
//        return null;
//    }


//    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
//        return null;
//    }
//
//
//    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
//        return null;
//    }

    @Deprecated
    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, GenomeDBAdaptor.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE, andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    @Override
    public CellBaseIterator<Chromosome> iterator(GenomeQuery query) {
        QueryOptions queryOptions = query.toQueryOptions();
        List<Bson> pipeline = unwind(query);
        GenericDocumentComplexConverter<Chromosome> converter = new GenericDocumentComplexConverter<>(Chromosome.class);
        MongoDBIterator<Chromosome> iterator = genomeInfoMongoDBCollection.iterator(pipeline, converter, queryOptions);
        return new CellBaseIterator<>(iterator);
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
    public CellBaseDataResult groupBy(GenomeQuery query) {
        Bson bsonQuery = parseQuery(query);
        logger.info("query: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return groupBy(bsonQuery, query, "name");
    }

    @Override
    public CellBaseDataResult<String> distinct(GenomeQuery query) {
        Bson bsonDocument = parseQuery(query);
        return new CellBaseDataResult<>(genomeInfoMongoDBCollection.distinct(query.getFacet(), bsonDocument));
    }

    public Bson parseQuery(GenomeQuery geneQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : geneQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "name":
                        createAndOrQuery(value, "chromosomes.name", QueryParam.Type.STRING, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("chromosome parsed query: {}", andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
