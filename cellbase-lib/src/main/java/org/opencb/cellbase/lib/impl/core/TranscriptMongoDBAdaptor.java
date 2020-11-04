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
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.ProjectionQueryOptions;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;

/**
 * Created by swaathi on 27/11/15.
 */
public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<TranscriptQuery, Transcript> {

    private MongoDBCollection refseqCollection = null;

    public TranscriptMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");
        refseqCollection = mongoDataStore.getCollection("refseq");
        logger.debug("TranscriptMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseIterator<Transcript> iterator(TranscriptQuery query) {
        QueryOptions queryOptions = query.toQueryOptions();
        List<Bson> pipeline = unwindAndMatchTranscripts(query, queryOptions);
        GenericDocumentComplexConverter<Transcript> converter = new GenericDocumentComplexConverter<>(Transcript.class);
        MongoDBIterator<Transcript> iterator = null;
        if (query.getSource() != null && !query.getSource().isEmpty() && "RefSeq".equalsIgnoreCase(query.getSource().get(0))) {
            iterator = refseqCollection.iterator(pipeline, converter, queryOptions);
        } else {
            iterator = mongoDBCollection.iterator(pipeline, converter, queryOptions);
        }
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<Transcript>> info(List<String> ids, ProjectionQueryOptions projectionQueryOptions) {
        return info(ids, projectionQueryOptions, null);
    }

    public List<CellBaseDataResult<Transcript>> info(List<String> ids, ProjectionQueryOptions projectionQueryOptions, String source) {
        List<CellBaseDataResult<Transcript>> results = new ArrayList<>();
        QueryOptions queryOptions = getInfoQueryOptions(projectionQueryOptions);
        for (String id : ids) {
            // make query to look in id OR name
            List<Bson> orBsonList = new ArrayList<>(2);
            orBsonList.add(Filters.eq("transcripts.id", id));
            if (id.contains("\\.")) {
                // transcript contains version, e.g. ENST00000671466.1
                orBsonList.add(Filters.eq("transcripts.id", id));
            } else {
                // transcript does not contain version, do a fuzzy query so that ENST00000671466 will match ENST00000671466.1
                orBsonList.add(Filters.regex("transcripts.id", "^" + id + "\\."));
            }
            orBsonList.add(Filters.eq("transcripts.name", id));
            Bson bson = Filters.or(orBsonList);

            // unwind results
            List<Bson> pipeline = unwindAndMatchTranscripts(bson, queryOptions);
            GenericDocumentComplexConverter<Transcript> converter = new GenericDocumentComplexConverter<>(Transcript.class);
            MongoDBIterator<Transcript> iterator = null;
            if (StringUtils.isNotEmpty(source) && ParamConstants.QueryParams.REFSEQ.key().equalsIgnoreCase(source)) {
                iterator = refseqCollection.iterator(pipeline, converter, queryOptions);
            } else {
                iterator = mongoDBCollection.iterator(pipeline, converter, queryOptions);
            }

            List<Transcript> transcripts = new ArrayList<>();
            while (iterator.hasNext()) {
                transcripts.add(iterator.next());
            }
            // one result per ID
            results.add(new CellBaseDataResult<>(id, 0, new ArrayList<>(), transcripts.size(), transcripts, -1));
        }
        return results;
    }

    private QueryOptions getInfoQueryOptions(ProjectionQueryOptions queryOptions) {
        if (queryOptions == null) {
            return new QueryOptions();
        } else {
            TranscriptQuery query = (TranscriptQuery) queryOptions;
            return query.toQueryOptions();
        }
    }

    @Deprecated
    public List<Bson> unwind(TranscriptQuery query) {
        Bson document = parseQuery(query);
        Bson match = Aggregates.match(document);

        List<String> includeFields = query.getIncludes();

        Bson include;
        if (includeFields != null && includeFields.size() > 0) {
            include = Aggregates.project(Projections.include(includeFields));
        } else {
            include = Aggregates.project(Projections.include("transcripts.id"));
        }

        Bson unwind = Aggregates.unwind("$transcripts");
        Bson match2 = Aggregates.match(document);
        Bson project = Aggregates.project(new Document("transcripts", "$transcripts.id"));
        return Arrays.asList(match, include, unwind, match2, project);
    }

    public CellBaseDataResult<Long> count(TranscriptQuery query) {
        List<Bson> projections = unwind(query);
        Bson group = Aggregates.group("transcripts", Accumulators.sum("count", 1));
        projections.add(group);
        CellBaseDataResult<Document> cellBaseDataResult = new CellBaseDataResult(mongoDBCollection.aggregate(projections, null));
        Number number = (Number) cellBaseDataResult.first().get("count");
        Long count = number.longValue();
        return new CellBaseDataResult<>(null, cellBaseDataResult.getTime(), cellBaseDataResult.getEvents(),
                cellBaseDataResult.getNumResults(), Collections.singletonList(count), cellBaseDataResult.getNumMatches());
    }

    @Override
    public CellBaseDataResult<Transcript> aggregationStats(TranscriptQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<Transcript> groupBy(TranscriptQuery query) {
        Bson bsonQuery = parseQuery(query);
        logger.info("transcriptQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return groupBy(bsonQuery, query, "name");
    }

    @Override
    public CellBaseDataResult<String> distinct(TranscriptQuery query) {
        Bson bsonDocument = parseQuery(query);
        logger.info("transcriptQuery: {}", bsonDocument.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument));
    }

    @Deprecated
    public CellBaseDataResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        if (query.getString("region") != null) {
            Region region = Region.parseRegion(query.getString("region"));
//            Bson bsonDocument = parseQuery(query);
//            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
        return null;
    }

    public Bson parseQuery(TranscriptQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        boolean visited = false;
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "region":
                    case "transcripts.id":
                        if (!visited) {
                           // parse region and ID at the same time
                            createRegionQuery(query.getRegions(), query.getTranscriptsId(), andBsonList);
                            visited = true;
                        }
                        break;
                    case "ontology":
                        createOntologyQuery(value, andBsonList);
                        break;
                    case "transcripts.supportLevel":
                        andBsonList.add(Filters.regex("transcripts.supportLevel", "^" + value));
                        break;
                    case "source":
                        // do nothing
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        logger.debug("transcript parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    // add regions and IDs to the query, joined with OR
    private void createRegionQuery(List<Region> regions, List<String> ids, List<Bson> andBsonList) {
        if (CollectionUtils.isEmpty(regions) && CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (CollectionUtils.isEmpty(ids) && regions.size() == 1) {
            Bson chromosome = Filters.eq("transcripts.chromosome", regions.get(0).getChromosome());
            Bson start = Filters.lte("transcripts.start", regions.get(0).getEnd());
            Bson end = Filters.gte("transcripts.end", regions.get(0).getStart());
            andBsonList.add(Filters.and(chromosome, start, end));
        } else if (CollectionUtils.isEmpty(regions) && ids.size() == 1) {
            String transcriptId = ids.get(0);
            if (transcriptId.contains(".")) {
                // transcript contains version, e.g. ENST00000671466.1
                andBsonList.add(Filters.eq("transcripts.id", transcriptId));
            } else {
                // transcript does not contain version, do a fuzzy query so that ENST00000671466 will match ENST00000671466.1
                andBsonList.add(Filters.regex("transcripts.id", "^" + transcriptId + "\\."));
            }
        } else {
            List<Bson> orBsonList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(regions)) {
                for (Region region : regions) {
                    Bson chromosome = Filters.eq("transcripts.chromosome", region.getChromosome());
                    Bson start = Filters.lte("transcripts.start", region.getEnd());
                    Bson end = Filters.gte("transcripts.end", region.getStart());
                    orBsonList.add(Filters.and(chromosome, start, end));
                }
            }
            if (CollectionUtils.isNotEmpty(ids)) {
                for (String id : ids) {
                    if (id.contains("\\.")) {
                        // transcript contains version, e.g. ENST00000671466.1
                        orBsonList.add(Filters.eq("transcripts.id", id));
                    } else {
                        // transcript does not contain version, do a fuzzy query so that ENST00000671466 will match ENST00000671466.1
                        orBsonList.add(Filters.regex("transcripts.id", "^" + id + "\\."));
                    }
                }
            }
            andBsonList.add(Filters.or(orBsonList));
        }
        logger.info("transcript parsed query: " + andBsonList.toString());
    }

    private List<Bson> unwindAndMatchTranscripts(TranscriptQuery query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return unwindAndMatchTranscripts(bson, options);
    }

    private List<Bson> unwindAndMatchTranscripts(Bson bson, QueryOptions options) {
        List<Bson> aggregateList = new ArrayList<>();
        Bson match = Aggregates.match(bson);

        Bson include = null;
        if (options != null && options.containsKey("include")) {
            List<String> includeList = new ArrayList<>();
            List<String> optionsAsStringList = options.getAsStringList("include");
            for (String s : optionsAsStringList) {
                if (s.startsWith("transcripts")) {
                    includeList.add(s);
                }
            }

            if (includeList.size() > 0) {
                include = Projections.include(includeList);
            }
        }

        if (include == null) {
            include = Projections.include("transcripts");
        }
        Bson excludeAndInclude = Aggregates.project(Projections.fields(Projections.excludeId(), include));
        Bson unwind = Aggregates.unwind("$transcripts");

        // This project the three fields of Xref to the top of the object
        Document document = new Document("id", "$transcripts.id");
        document.put("name", "$transcripts.name");
        document.put("biotype", "$transcripts.biotype");
        document.put("status", "$transcripts.status");
        document.put("supportLevel", "$transcripts.supportLevel");
        document.put("chromosome", "$transcripts.chromosome");
        document.put("start", "$transcripts.start");
        document.put("end", "$transcripts.end");
        document.put("strand", "$transcripts.strand");
        document.put("genomicCodingStart", "$transcripts.genomicCodingStart");
        document.put("genomicCodingEnd", "$transcripts.genomicCodingEnd");
        document.put("cdnaCodingStart", "$transcripts.cdnaCodingStart");
        document.put("cdnaCodingEnd", "$transcripts.cdnaCodingEnd");
        document.put("cdsLength", "$transcripts.cdsLength");
        document.put("proteinId", "$transcripts.proteinId");
        document.put("proteinSequence", "$transcripts.proteinSequence");
        document.put("cDnaSequence", "$transcripts.cDnaSequence");
        document.put("xrefs", "$transcripts.xrefs");
        document.put("exons", "$transcripts.exons");
        document.put("tfbs", "$transcripts.tfbs");
        document.put("flags", "$transcripts.flags");
        document.put("annotation", "$transcripts.annotation");
        Bson project = Aggregates.project(document);

        Bson match2 = Aggregates.match(bson);

        aggregateList.add(match);
        aggregateList.add(unwind);
        aggregateList.add(match2);
        aggregateList.add(excludeAndInclude);
        aggregateList.add(project);

        return aggregateList;
    }
}
