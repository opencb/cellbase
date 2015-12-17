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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.opencb.cellbase.grpc.GeneModel;
import org.opencb.cellbase.grpc.GeneServiceGrpc;
import org.opencb.cellbase.grpc.GenericServiceModel;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by imedina on 17/12/15.
 */
public class QueryGrpcCommandExecutor extends CommandExecutor {

    private CliOptionsParser.QueryGrpcCommandOptions queryGrpcCommandOptions;

    private ManagedChannel channel;
    private Path outputFile;

    public QueryGrpcCommandExecutor(CliOptionsParser.QueryGrpcCommandOptions queryGrpcCommandOptions) {
        super(queryGrpcCommandOptions.commonOptions.logLevel, queryGrpcCommandOptions.commonOptions.verbose,
                queryGrpcCommandOptions.commonOptions.conf);

        this.queryGrpcCommandOptions = queryGrpcCommandOptions;
    }

    @Override
    public void execute() {

        channel = ManagedChannelBuilder.forAddress(queryGrpcCommandOptions.host, queryGrpcCommandOptions.port)
                .usePlaintext(true)
                .build();

        Map<String, String> query = createQueryMap();
        QueryOptions queryOptions = createQueryOptions();

        PrintStream output = System.out;
        if (queryGrpcCommandOptions.output != null && !queryGrpcCommandOptions.output.isEmpty()) {
            outputFile = Paths.get(queryGrpcCommandOptions.output);
            if (Files.exists(outputFile.getParent()) && Files.isDirectory(outputFile.getParent())) {
                try {
                    output = new PrintStream(outputFile.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        GenericServiceModel.Request request = GenericServiceModel.Request.newBuilder()
                .setSpecies(queryGrpcCommandOptions.species)
                .setAssembly(queryGrpcCommandOptions.assembly)
                .putAllQuery(query)
//                .putAllOptions(queryOptions)
                .build();


        try {
            switch (queryGrpcCommandOptions.category) {
//                case "genome":
//                    executeGenomeQuery(query, queryOptions, output);
//                    break;
                case "gene":
                    executeGeneQuery(request, output);
                    break;
//                case "variation":
//                    executeVariationQuery(query, queryOptions, output);
//                    break;
//                case "protein":
//                    executeProteinQuery(query, queryOptions, output);
//                    break;
//                case "regulatory_region":
//                    executeRegulatoryRegionQuery(query, queryOptions, output);
//                    break;
//                case "transcript":
//                    executeTranscriptQuery(query, queryOptions, output);
//                    break;
//                case "conservation":
//                    break;
                default:
                    break;
            }

            channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
        } catch (JsonProcessingException | InterruptedException e) {
            e.printStackTrace();
        }

        output.close();
    }

    private void executeGeneQuery(GenericServiceModel.Request request, PrintStream output)
            throws JsonProcessingException {
//        executeFeatureAggregation(geneDBAdaptor, query, queryOptions, output);
        GeneServiceGrpc.GeneServiceBlockingStub geneServiceBlockingStub = GeneServiceGrpc.newBlockingStub(channel);

        if (queryGrpcCommandOptions.resource != null) {
            switch (queryGrpcCommandOptions.resource) {
                case "info":
                    Iterator<GeneModel.Gene> geneIterator = geneServiceBlockingStub.get(request);
                    while (geneIterator.hasNext()) {
                        GeneModel.Gene next = geneIterator.next();
                        output.println(next.toString());
                    }
//                    query.append(GeneDBAdaptor.QueryParams.ID.key(), queryCommandOptions.id);
//                    Iterator iterator = geneDBAdaptor.nativeIterator(query, queryOptions);
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

    private Map<String, String> createQueryMap() {
        Map<String, String> query = new HashMap<>();

        // we first append region CLI parameter, if specified in 'options' it will be overwritten
        if (queryGrpcCommandOptions.region != null) {
            query.put("region", queryGrpcCommandOptions.region);
        }

        for (String key : queryGrpcCommandOptions.options.keySet()) {
            query.put(key, queryGrpcCommandOptions.options.get(key));
        }
        return query;
    }

    private QueryOptions createQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.append("include", queryGrpcCommandOptions.include);
        if (queryGrpcCommandOptions.exclude != null && !queryGrpcCommandOptions.exclude.isEmpty()) {
            queryOptions.append("exclude", queryGrpcCommandOptions.exclude + ",_id,_chunkIds");
        } else {
            queryOptions.append("exclude", "_id,_chunkIds");
        }
        queryOptions.append("skip", queryGrpcCommandOptions.skip);
        queryOptions.append("limit", queryGrpcCommandOptions.limit);
        queryOptions.append("count", queryGrpcCommandOptions.count);
        return queryOptions;
    }

}
