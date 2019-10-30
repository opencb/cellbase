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

package org.opencb.cellbase.lib.impl;

import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.common.DNASequenceUtils;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by imedina on 07/12/15.
 */
public class GenomeMongoDBAdaptor extends MongoDBAdaptor implements GenomeDBAdaptor {

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

    @Override
    public CellBaseDataResult getGenomeInfo(QueryOptions queryOptions) {
        return new CellBaseDataResult<>(genomeInfoMongoDBCollection.find(new Document(), queryOptions));
    }

    @Override
    public CellBaseDataResult getChromosomeInfo(String chromosomeId, QueryOptions queryOptions) {
        if (queryOptions == null) {
            queryOptions = new QueryOptions("include", Collections.singletonList("chromosomes.$"));
        } else {
            queryOptions.addToListOption("include", "chromosomes.$");
        }
        Document dbObject = new Document("chromosomes", new Document("$elemMatch", new Document("name", chromosomeId)));
        return executeQuery(chromosomeId, dbObject, queryOptions, genomeInfoMongoDBCollection);
    }

    @Override
    public CellBaseDataResult<Cytoband> getCytobands(Region region, QueryOptions queryOptions) {

        List<Cytoband> cytobandList = new ArrayList<>();
        long dbStartTime = System.currentTimeMillis();
        long dbTime = System.currentTimeMillis() - dbStartTime;
        Document chromosomeInfo = getOneChromosomeInfo(region.getChromosome());
        // May not have info for specified chromosome, e.g. 17_KI270729v1_random
        if (chromosomeInfo != null) {
            List<Document> cytobandDocumentList = (List<Document>) chromosomeInfo.get(CYTOBANDS);
            int i = 0;
            while (i < cytobandDocumentList.size() && ((int) cytobandDocumentList.get(i).get(START)) <= region.getEnd()) {
                if (((int) cytobandDocumentList.get(i).get(END)) >= region.getStart()) {
                    cytobandList.add(new Cytoband(region.getChromosome(),
                            (String) cytobandDocumentList.get(i).get(STAIN),
                            (String) cytobandDocumentList.get(i).get(NAME),
                            (Integer) cytobandDocumentList.get(i).get(START),
                            (Integer) cytobandDocumentList.get(i).get(END)));
                }
                i++;
            }
        }
        return new CellBaseDataResult(region.toString(), (int) dbTime, cytobandList.size(),
                cytobandList.size(), null, cytobandList);

    }

    private Document getOneChromosomeInfo(String chromosome) {
        Document genomeInfoVariable = getGenomeInfoVariable();
        if (genomeInfoVariable != null) {
            for (Document document : (List<Document>) genomeInfoVariable.get(CHROMOSOMES)) {
                if (document.get(NAME).equals(chromosome)) {
                    return document;
                }
            }
        }
        return null;
    }

    private Document getGenomeInfoVariable() {
        if (genomeInfo == null) {
            CellBaseDataResult<Document> cellBaseDataResult = new CellBaseDataResult<>(
                    genomeInfoMongoDBCollection.find(new Document(), null));
            if (cellBaseDataResult.getNumResults() > 0) {
                genomeInfo = genomeInfoMongoDBCollection.find(new Document(), null).getResults().get(0);
                for (Document chromosomeDocument : (List<Document>) genomeInfo.get(CHROMOSOMES)) {
                    ((List<Document>) chromosomeDocument.get(CYTOBANDS))
                            .sort((c1, c2) -> Integer.compare((int) c1.get(START), (int) c2.get(START)));
                }
            }
        }
        return genomeInfo;
    }

    @Deprecated
    @Override
    public CellBaseDataResult<GenomeSequenceFeature> getGenomicSequence(Query query, QueryOptions queryOptions) {
        return getSequence(Region.parseRegion(query.getString(QueryParams.REGION.key())), queryOptions);
    }

    @Override
    public CellBaseDataResult<GenomeSequenceFeature> getSequence(Region region, QueryOptions queryOptions) {
        Query query = new Query(QueryParams.REGION.key(), region.toString());
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
        }

        return result;
    }

    @Override
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

    @Override
    public CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    @Override
    public CellBaseDataResult<Long> count(Query query) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.count(bson));
    }

    @Override
    public CellBaseDataResult distinct(Query query, String field) {
        Bson bson = parseQuery(query);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(field, bson));
    }

    @Override
    public CellBaseDataResult stats(Query query) {
        return null;
    }

    @Override
    public CellBaseDataResult get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult<>(mongoDBCollection.find(bson, options));
    }

    @Override
    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public CellBaseDataResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

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

}
