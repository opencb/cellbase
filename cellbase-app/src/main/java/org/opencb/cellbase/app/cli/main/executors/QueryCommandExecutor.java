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

package org.opencb.cellbase.app.cli.main.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.main.CellBaseCliOptionsParser;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.AbstractQuery;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.RegulationQuery;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.*;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.RegulatoryManager;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by imedina on 20/02/15.
 *
 * @author Javier Lopez fjlopez@ebi.ac.uk;
 */
public class QueryCommandExecutor extends CommandExecutor {

    private MongoDBAdaptorFactory dbAdaptorFactory;
    private CellBaseManagerFactory cellBaseManagerFactory;

    private CellBaseCliOptionsParser.QueryCommandOptions queryCommandOptions;

    private ObjectMapper objectMapper;
    private Path outputFile;

    public QueryCommandExecutor(CellBaseCliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
        objectMapper = new ObjectMapper();
    }


    @Override
    public void execute() throws CellbaseException {
        dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
        cellBaseManagerFactory = new CellBaseManagerFactory(configuration);


        if (queryCommandOptions.limit == 0) {
            queryCommandOptions.limit = 10;
        }

        Query query = createQuery();
        QueryOptions queryOptions = createQueryOptions();

        PrintStream output = System.out;
        if (queryCommandOptions.output != null && !queryCommandOptions.output.isEmpty()) {
            outputFile = Paths.get(queryCommandOptions.output);
            if (Files.exists(outputFile.getParent()) && Files.isDirectory(outputFile.getParent())) {
                try {
                    output = new PrintStream(outputFile.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            switch (queryCommandOptions.category) {
                case "genome":
                    executeGenomeQuery(query, queryOptions, output);
                    break;
                case "gene":
                    executeGeneQuery(query, queryOptions, output);
                    break;
                case "variation":
                    executeVariationQuery(query, queryOptions, output);
                    break;
                case "protein":
                    executeProteinQuery(query, queryOptions, output);
                    break;
                case "regulatory_region":
                    executeRegulatoryRegionQuery(query, queryOptions, output);
                    break;
                case "transcript":
                    executeTranscriptQuery(query, queryOptions, output);
                    break;
                case "conservation":
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        output.close();
    }

    private void executeGenomeQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        GenomeMongoDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(queryCommandOptions.species);

        if (queryCommandOptions.resource != null) {
            switch (queryCommandOptions.resource) {
                case "info":
                    genomeDBAdaptor.getGenomeInfo(queryOptions);
                    break;
                case "sequence":
                    CellBaseDataResult<GenomeSequenceFeature> genomicSequence = genomeDBAdaptor.getGenomicSequence(query, queryOptions);
                    output.println(objectMapper.writeValueAsString(genomicSequence));
                    break;
                default:
                    break;
            }
        }
    }

    private void executeGeneQuery(Query query, QueryOptions queryOptions, PrintStream output)
            throws JsonProcessingException, CellbaseException {
        GeneMongoDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(queryCommandOptions.species);
        GeneManager geneManager = cellBaseManagerFactory.getGeneManager(queryCommandOptions.species, queryCommandOptions.assembly);

        executeFeatureAggregation(geneDBAdaptor, query, queryOptions, output);

        if (queryCommandOptions.resource != null) {
            switch (queryCommandOptions.resource) {
                case "info":
                    query.append(ParamConstants.QueryParams.ID.key(), queryCommandOptions.id);
                    //fix me
                    CellBaseIterator<Gene> iterator = geneManager.iterator(new GeneQuery());
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        output.println(objectMapper.writeValueAsString(next));
                    }
                    iterator.close();
                    break;
                case "variation":
                    VariantMongoDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(queryCommandOptions.species);
                    query.append(ParamConstants.QueryParams.GENE.key(), queryCommandOptions.id);
                    variantDBAdaptor.forEach(query, entry -> {
                        try {
                            output.println(objectMapper.writeValueAsString(entry));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }, queryOptions);
                    break;
                default:
                    break;
            }
        }
    }

    private void executeVariationQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        VariantMongoDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(queryCommandOptions.species);

        executeFeatureAggregation(variantDBAdaptor, query, queryOptions, output);

        if (queryCommandOptions.resource != null) {
            switch (queryCommandOptions.resource) {
                case "info":
                    query.append(ParamConstants.QueryParams.ID.key(), queryCommandOptions.id);
                    Iterator iterator = variantDBAdaptor.nativeIterator(query, queryOptions);
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        output.println(objectMapper.writeValueAsString(next));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void executeProteinQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        ProteinMongoDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(queryCommandOptions.species);

        if (queryCommandOptions.distinct != null && !queryCommandOptions.distinct.isEmpty()) {
//            CellBaseDataResult distinct = proteinDBAdaptor.distinct(query, queryCommandOptions.distinct);
//            output.println(objectMapper.writeValueAsString(distinct));
            return;
        }

//        if (queryCommandOptions.count) {
//            CellBaseDataResult count = proteinDBAdaptor.count(query);
//            output.println(objectMapper.writeValueAsString(count));
//            return;
//        }

        if (queryCommandOptions.resource != null) {
            switch (queryCommandOptions.resource) {
                case "info":
                    query.append(ParamConstants.QueryParams.NAME.key(), queryCommandOptions.id);
//                    Iterator iterator = proteinDBAdaptor.nativeIterator(query, queryOptions);
//                    while (iterator.hasNext()) {
//                        Object next = iterator.next();
//                        output.println(objectMapper.writeValueAsString(next));
//                    }
                    break;
                case "substitution-scores":
//                    CellBaseDataResult substitutionScores = proteinDBAdaptor.getSubstitutionScores(query, queryOptions);
//                    output.println(objectMapper.writeValueAsString(substitutionScores));
                    break;
                default:
                    break;
            }
        }
    }

    private void executeRegulatoryRegionQuery(Query query, QueryOptions queryOptions, PrintStream output)
            throws JsonProcessingException, CellbaseException {
        RegulationMongoDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(queryCommandOptions.species);
        RegulatoryManager regulatoryManager = cellBaseManagerFactory.getRegulatoryManager(queryCommandOptions.species);
        if (queryCommandOptions.resource != null) {
            switch (queryCommandOptions.resource) {
                case "info":
                    RegulationQuery regulationQuery = createQueryOptions(new RegulationQuery());
                    List<CellBaseDataResult<RegulatoryFeature>> results = regulatoryManager.info(Collections.singletonList(
                            queryCommandOptions.id), regulationQuery);
                    while (results.iterator().hasNext()) {
                        CellBaseDataResult<RegulatoryFeature> next = results.iterator().next();
                        output.println(objectMapper.writeValueAsString(next));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void executeTranscriptQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        TranscriptMongoDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(queryCommandOptions.species);

        if (queryCommandOptions.resource != null) {
            switch (queryCommandOptions.resource) {
                case "info":
                    query.append(ParamConstants.QueryParams.ID.key(), queryCommandOptions.id);
//                    Iterator iterator = transcriptDBAdaptor.nativeIterator(query, queryOptions);
//                    while (iterator.hasNext()) {
//                        Object next = iterator.next();
//                        output.println(objectMapper.writeValueAsString(next));
//                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void executeFeatureAggregation(CellBaseCoreDBAdaptor featureDBAdaptor, Query query, QueryOptions queryOptions,
                                           PrintStream output)
            throws JsonProcessingException {
// FIXME
//        if (queryCommandOptions.distinct != null && !queryCommandOptions.distinct.isEmpty()) {
//            CellBaseDataResult distinct = featureDBAdaptor.distinct(query, queryCommandOptions.distinct);
//            output.println(objectMapper.writeValueAsString(distinct));
//            return;
//        }

//        if (queryCommandOptions.groupBy != null && !queryCommandOptions.groupBy.isEmpty()) {
//            CellBaseDataResult groupBy = featureDBAdaptor.groupBy(query, queryCommandOptions.groupBy, queryOptions);
//            output.println(objectMapper.writeValueAsString(groupBy));
//            return;
//        }

//        if (queryCommandOptions.count) {
//            CellBaseDataResult count = featureDBAdaptor.count(query);
//            output.println(objectMapper.writeValueAsString(count));
//            return;
//        }

//        if (queryCommandOptions.histogram) {
//            CellBaseDataResult histogram = featureDBAdaptor.getIntervalFrequencies(query, queryCommandOptions.interval, queryOptions);
//            output.println(objectMapper.writeValueAsString(histogram));
//            return;
//        }
        return;
    }

    private Query createQuery() {
        Query query = new Query();

        // we first append region CLI parameter, if specified in 'options' it will be overwritten
        if (queryCommandOptions.region != null) {
            query.append("region", queryCommandOptions.region);
        }

        for (String key : queryCommandOptions.options.keySet()) {
            query.append(key, queryCommandOptions.options.get(key));
        }
        return query;
    }

    private QueryOptions createQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.append("include", queryCommandOptions.include);
        if (queryCommandOptions.exclude != null && !queryCommandOptions.exclude.isEmpty()) {
            queryOptions.append("exclude", queryCommandOptions.exclude + ",_id,_chunkIds");
        } else {
            queryOptions.append("exclude", "_id,_chunkIds");
        }
        queryOptions.append("skip", queryCommandOptions.skip);
        queryOptions.append("limit", queryCommandOptions.limit);
        queryOptions.append("count", queryCommandOptions.count);
        return queryOptions;
    }

    private <Q extends AbstractQuery> Q createQueryOptions(Q query) {
        query.setIncludes(Collections.singletonList(queryCommandOptions.include));
        if (queryCommandOptions.exclude != null && !queryCommandOptions.exclude.isEmpty()) {
            query.setExcludes(Collections.singletonList(queryCommandOptions.exclude + ",_id,_chunkIds"));
        } else {
            query.setExcludes(Collections.singletonList("_id,_chunkIds"));
        }
        query.setSkip(queryCommandOptions.skip);
        query.setLimit(queryCommandOptions.limit);
        query.setCount(queryCommandOptions.count);
        return query;
    }
}
