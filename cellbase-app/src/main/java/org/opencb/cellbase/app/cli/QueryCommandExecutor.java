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
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.nio.file.Path;
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

        objectMapper = new ObjectMapper();

        try {
            switch (queryCommandOptions.category) {
                case "gene":
                    executeGeneQuery(query, queryOptions);
                    break;
                case "variation":
                    executeVariationQuery(query, queryOptions);
                    break;
                case "region":
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    private void executeGeneQuery(Query query, QueryOptions queryOptions) throws JsonProcessingException {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(queryCommandOptions.species);

        switch (queryCommandOptions.resource) {
            case "count":
                System.out.println(geneDBAdaptor.count(query).getResult().get(0));
                break;
            case "info":
                query.append(GeneDBAdaptor.QueryParams.ID.key(), queryCommandOptions.id);
                Iterator iterator = geneDBAdaptor.nativeIterator(query, queryOptions);
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    System.out.println(objectMapper.writeValueAsString(next));
                }
                break;
            case "variation":
                VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(queryCommandOptions.species);
                query.append(VariantDBAdaptor.QueryParams.GENE.key(), queryCommandOptions.id);
                variantDBAdaptor.forEach(query, entry -> {
                    try {
                        System.out.println(objectMapper.writeValueAsString(entry));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }, queryOptions);
                break;
            default:
                break;
        }
    }

    private void executeVariationQuery(Query query, QueryOptions queryOptions) throws JsonProcessingException {
        VariantDBAdaptor variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(queryCommandOptions.species);

        switch (queryCommandOptions.resource) {
            case "count":
                System.out.println(variantDBAdaptor.count(query).getResult().get(0));
                break;
            case "info":
                query.append(GeneDBAdaptor.QueryParams.ID.key(), queryCommandOptions.id);
                Iterator iterator = variantDBAdaptor.nativeIterator(query, queryOptions);
                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    System.out.println(objectMapper.writeValueAsString(next));
                }
                break;
            default:
                break;
        }
    }

    private Query createQuery() {
        Query query = new Query();
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
//        queryOptions.append("count", queryCommandOptions.count);
        return queryOptions;
    }

//    private void checkParameters() {
//        // output file
//        if (queryCommandOptions.outputFile != null) {
//            outputFile = Paths.get(queryCommandOptions.outputFile);
//            Path outputDir = outputFile.getParent();
//            if (!outputDir.toFile().exists()) {
//                throw new ParameterException("Output directory " + outputDir + " doesn't exist");
//            } else if (outputFile.toFile().isDirectory()) {
//                throw new ParameterException("Output file cannot be a directory: " + outputFile);
//            }
//        } else {
//            throw new ParameterException("Please check command line sintax. Provide a valid output file name.");
//        }
//    }

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
