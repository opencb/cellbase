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

package org.opencb.cellbase.app.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.cellbase.core.api.*;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by imedina on 20/02/15.
 *
 * @author Javier Lopez fjlopez@ebi.ac.uk;
 */
public class QueryCommandExecutor extends CommandExecutor {

    private DBAdaptorFactory dbAdaptorFactory;

    private CliOptionsParser.QueryCommandOptions queryCommandOptions;

    private ObjectMapper objectMapper;
    private Path outputFile;

    public QueryCommandExecutor(CliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.verbose,
                queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
    }


    @Override
    public void execute() {
        dbAdaptorFactory = new org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory(configuration);

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

        objectMapper = new ObjectMapper();

        try {
            switch (queryCommandOptions.category) {
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


    private void executeGeneQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(queryCommandOptions.species);

        if (queryCommandOptions.groupBy != null && !queryCommandOptions.groupBy.isEmpty()) {
            QueryResult queryResult = geneDBAdaptor.groupBy(query, queryCommandOptions.groupBy, queryOptions);
            output.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(queryResult));
        } else {
            switch (queryCommandOptions.resource) {
                case "count":
                    output.println(geneDBAdaptor.count(query));
                    break;
                case "info":
                    query.append(GeneDBAdaptor.QueryParams.ID.key(), queryCommandOptions.id);
                    Iterator iterator = geneDBAdaptor.nativeIterator(query, queryOptions);
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        output.println(objectMapper.writeValueAsString(next));
                    }
                    break;
                case "variation":
                    VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(queryCommandOptions.species);
                    query.append(VariantDBAdaptor.QueryParams.GENE.key(), queryCommandOptions.id);
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
        VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(queryCommandOptions.species);

        switch (queryCommandOptions.resource) {
            case "count":
                output.println(variantDBAdaptor.count(query).getResult().get(0));
                break;
            case "info":
                query.append(GeneDBAdaptor.QueryParams.ID.key(), queryCommandOptions.id);
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

    private void executeProteinQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(queryCommandOptions.species);

        if (queryCommandOptions.groupBy != null && !queryCommandOptions.groupBy.isEmpty()) {
            QueryResult queryResult = proteinDBAdaptor.groupBy(query, queryCommandOptions.groupBy, queryOptions);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(queryResult));
        } else {
            switch (queryCommandOptions.resource) {
                case "count":
                    System.out.println(proteinDBAdaptor.count(query).getResult().get(0));
                    break;
                case "info":
                    query.append(ProteinDBAdaptor.QueryParams.NAME.key(), queryCommandOptions.id);
                    Iterator iterator = proteinDBAdaptor.nativeIterator(query, queryOptions);
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        System.out.println(objectMapper.writeValueAsString(next));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void executeRegulatoryRegionQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor = dbAdaptorFactory.getRegulatoryRegionDBAdaptor(queryCommandOptions.species);

        if (queryCommandOptions.groupBy != null && !queryCommandOptions.groupBy.isEmpty()) {
            QueryResult queryResult = regulatoryRegionDBAdaptor.groupBy(query, queryCommandOptions.groupBy, queryOptions);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(queryResult));
        } else {
            switch (queryCommandOptions.resource) {
                case "count":
                    System.out.println(regulatoryRegionDBAdaptor.count(query).getResult().get(0));
                    break;
                case "info":
                    query.append(ProteinDBAdaptor.QueryParams.NAME.key(), queryCommandOptions.id);
                    Iterator iterator = regulatoryRegionDBAdaptor.nativeIterator(query, queryOptions);
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        System.out.println(objectMapper.writeValueAsString(next));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void executeTranscriptQuery(Query query, QueryOptions queryOptions, PrintStream output) throws JsonProcessingException {
        TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(queryCommandOptions.species);

        switch (queryCommandOptions.resource) {
            case "count":
                output.println(transcriptDBAdaptor.count(query).getResult().get(0));
                break;
            case "info":
                query.append(TranscriptDBAdaptor.QueryParams.ID.key(), queryCommandOptions.id);
                Iterator iterator = transcriptDBAdaptor.nativeIterator(query, queryOptions);
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    output.println(objectMapper.writeValueAsString(next));
                }
                break;
            default:
                break;
        }
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

//    private CellBaseClient getCellBaseClient() throws URISyntaxException {
//        CellBaseConfiguration.DatabaseProperties cellbaseDDBBProperties = configuration.getDatabase();
////        String host = cellbaseDDBBProperties.getHost();
////        int port = Integer.parseInt(cellbaseDDBBProperties.getPort());
//        // TODO: read path from configuration file?
//        // TODO: hardcoded port???
//        String path = "/cellbase/webservices/rest/";
//        return new CellBaseClient(queryCommandOptions.url, 8080, path, configuration.getVersion(), queryCommandOptions.species);
//    }
}
