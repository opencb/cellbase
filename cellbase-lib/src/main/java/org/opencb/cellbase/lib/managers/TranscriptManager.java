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

package org.opencb.cellbase.lib.managers;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.core.TranscriptDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TranscriptManager extends AbstractManager {

    public TranscriptManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public CellBaseDataResult<Transcript> search(Query query, QueryOptions queryOptions, String species, String assembly) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        CellBaseDataResult<Transcript> queryResult = dbAdaptor.nativeGet(query, queryOptions);
        // Total number of results is always same as the number of results. As this is misleading, we set it as -1 until
        // properly fixed
        queryResult.setNumTotalResults(-1);
        queryResult.setNumMatches(-1);
        return queryResult;
    }

    public CellBaseDataResult<Transcript> groupBy(Query query, QueryOptions queryOptions, String species, String assembly, String fields) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        return dbAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String species, String assembly, String id) {
        logger.debug("blahh...");
        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        List<Query> queries = createQueries(query, id, TranscriptDBAdaptor.QueryParams.XREFS.key());
        List<CellBaseDataResult> queryResults = dbAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.XREFS.key()));
        }
        return queryResults;
    }

    public List<CellBaseDataResult> getSequence(String species, String assembly, String id) {
        logger.debug("blahh...");
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        List<String> transcriptsList = Arrays.asList(id.split(","));
        List<CellBaseDataResult> queryResult = transcriptDBAdaptor.getCdna(transcriptsList);
        for (int i = 0; i < transcriptsList.size(); i++) {
            queryResult.get(i).setId(transcriptsList.get(i));
        }
        return queryResult;
    }

    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String species, String assembly, String region) {
        logger.debug("blahh...");
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);
        List<Query> queries = createQueries(query, region, TranscriptDBAdaptor.QueryParams.REGION.key());
        List<CellBaseDataResult> queryResults = transcriptDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(TranscriptDBAdaptor.QueryParams.REGION.key()));
        }
        return queryResults;
    }

    public CellBaseDataResult count(String species, String assembly, Query query) {
        Bson document = parseQuery(query);
        Bson match = Aggregates.match(document);

        List<String> includeFields = new ArrayList<>();
        for (String s : query.keySet()) {
            if (StringUtils.isNotEmpty(query.getString(s))) {
                includeFields.add(s);
            }
        }

        Bson include;
        if (includeFields.size() > 0) {
            include = Aggregates.project(Projections.include(includeFields));
        } else {
            include = Aggregates.project(Projections.include("transcripts.id"));
        }

        Bson unwind = Aggregates.unwind("$transcripts");
        Bson match2 = Aggregates.match(document);
        Bson project = Aggregates.project(new Document("transcripts", "$transcripts.id"));
        Bson group = Aggregates.group("transcripts", Accumulators.sum("count", 1));

        TranscriptDBAdaptor dbAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(species, assembly);

        CellBaseDataResult<Long> cellBaseDataResult = dbAdaptor.count(match, include, unwind, match2, project, group);

        Number number = (Number) cellBaseDataResult.first().get("count");
        Long count = number.longValue();
        return new CellBaseDataResult<>(null, cellBaseDataResult.getTime(), cellBaseDataResult.getEvents(),
                cellBaseDataResult.getNumResults(), Collections.singletonList(count), cellBaseDataResult.getNumMatches());
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, TranscriptDBAdaptor.QueryParams.REGION.key(), MongoDBCollectionConfiguration.GENE_CHUNK_SIZE, andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ID.key(), "transcripts.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.NAME.key(), "transcripts.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "transcripts.biotype", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.TFBS_NAME.key(), "transcripts.tfbs.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ANNOTATION_FLAGS.key(), "transcripts.annotationFlags", andBsonList);
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private Bson parseQueryUnwindTranscripts(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, TranscriptDBAdaptor.QueryParams.REGION.key(), andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ID.key(), "id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.NAME.key(), "name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "biotype", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.XREFS.key(), "xrefs.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.TFBS_NAME.key(), "tfbs.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ANNOTATION_FLAGS.key(), "annotationFlags", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private List<Bson> unwindAndMatchTranscripts(Query query, QueryOptions options) {
        List<Bson> aggregateList = new ArrayList<>();

        Bson bson = parseQuery(query);
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
        document.put("chromosome", "$transcripts.chromosome");
        document.put("start", "$transcripts.start");
        document.put("end", "$transcripts.end");
        document.put("strand", "$transcripts.strand");
        document.put("genomicCodingStart", "$transcripts.genomicCodingStart");
        document.put("genomicCodingEnd", "$transcripts.genomicCodingEnd");
        document.put("cdnaCodingStart", "$transcripts.cdnaCodingStart");
        document.put("cdnaCodingEnd", "$transcripts.cdnaCodingEnd");
        document.put("cdsLength", "$transcripts.cdsLength");
        document.put("proteinID", "$transcripts.proteinID");
        document.put("proteinSequence", "$transcripts.proteinSequence");
        document.put("cDnaSequence", "$transcripts.cDnaSequence");
        document.put("xrefs", "$transcripts.xrefs");
        document.put("exons", "$transcripts.exons");
        document.put("annotationFlags", "$transcripts.annotationFlags");
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

