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
import org.opencb.cellbase.grpc.*;

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
        Map<String, String> queryOptions = createQueryOptionsMap();

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
                .putAllOptions(queryOptions)
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
                case "regulation":
                    executeRegulationQuery(request, output);
                    break;
                case "transcript":
                    executeTranscriptQuery(request, output);
                    break;
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

//                    Iterator<GenericServiceModel.StringResponse> geneIterator = geneServiceBlockingStub.getJson(request);
//                    while (geneIterator.hasNext()) {
//                        GenericServiceModel.StringResponse next = geneIterator.next();
//                        output.println(next.toString());
//                    }

                    break;
                case "first":
                    GeneModel.Gene first = geneServiceBlockingStub.first(request);
                    output.println(first.toString());
                    break;
                default:
                    break;
            }
        }
        if (queryGrpcCommandOptions.count) {
            GenericServiceModel.LongResponse value = geneServiceBlockingStub.count(request);
            output.println(value);
        }
        if (queryGrpcCommandOptions.distinct != null) {
            GenericServiceModel.StringArrayResponse values = geneServiceBlockingStub.distinct(request);
            output.println(values);
        }
    }

    private void executeTranscriptQuery(GenericServiceModel.Request request, PrintStream output)
            throws JsonProcessingException {
        TranscriptServiceGrpc.TranscriptServiceBlockingStub transcriptServiceBlockingStub = TranscriptServiceGrpc.newBlockingStub(channel);

        if (queryGrpcCommandOptions.resource != null) {
            switch (queryGrpcCommandOptions.resource) {
                case "info":
                    Iterator<TranscriptModel.Transcript> transcriptIterator = transcriptServiceBlockingStub.get(request);
                    while (transcriptIterator.hasNext()) {
                        TranscriptModel.Transcript next = transcriptIterator.next();
                        output.println(next.toString());
                    }
                    break;
                default:
                    break;
            }
        }
        if (queryGrpcCommandOptions.count) {
            GenericServiceModel.LongResponse value = transcriptServiceBlockingStub.count(request);
            output.println(value);
        }
    }

    private void executeRegulationQuery(GenericServiceModel.Request request, PrintStream output)
            throws JsonProcessingException {
        RegulationServiceGrpc.RegulationServiceBlockingStub regulationServiceBlockingStub = RegulationServiceGrpc.newBlockingStub(channel);

        if (queryGrpcCommandOptions.resource != null) {
            switch (queryGrpcCommandOptions.resource) {
                case "info":
                    Iterator<RegulationModel.Regulation> regulationIterator = regulationServiceBlockingStub.get(request);
                    while (regulationIterator.hasNext()) {
                        RegulationModel.Regulation next = regulationIterator.next();
                        output.println(next.toString());
                    }
                    break;
                case "first":
                    RegulationModel.Regulation first = regulationServiceBlockingStub.first(request);
                    output.println(first.toString());
                    break;
                default:
                    break;
            }
        }
        if (queryGrpcCommandOptions.count) {
            GenericServiceModel.LongResponse value = regulationServiceBlockingStub.count(request);
            output.println(value);
        }
        if (queryGrpcCommandOptions.distinct != null) {
            GenericServiceModel.StringArrayResponse values = regulationServiceBlockingStub.distinct(request);
            output.println(values);
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

    private Map<String, String> createQueryOptionsMap() {
        Map<String, String> queryOptions = new HashMap<>();
        if (queryGrpcCommandOptions.include != null && !queryGrpcCommandOptions.include.isEmpty()) {
            queryOptions.put("include", queryGrpcCommandOptions.include);
        }
        if (queryGrpcCommandOptions.exclude != null && !queryGrpcCommandOptions.exclude.isEmpty()) {
            queryOptions.put("exclude", queryGrpcCommandOptions.exclude + ",_id,_chunkIds");
        } else {
            queryOptions.put("exclude", "_id,_chunkIds");
        }
        if (queryGrpcCommandOptions.skip != 0) {
            queryOptions.put("skip", String.valueOf(queryGrpcCommandOptions.skip));
        }
        if (queryGrpcCommandOptions.limit != 0) {
            queryOptions.put("limit", String.valueOf(queryGrpcCommandOptions.limit));
        }
        if (queryGrpcCommandOptions.count) {
            queryOptions.put("count", String.valueOf(queryGrpcCommandOptions.count));
        }
        if (queryGrpcCommandOptions.distinct != null && !queryGrpcCommandOptions.distinct.isEmpty()) {
            queryOptions.put("distinct", queryGrpcCommandOptions.distinct);
        }
        return queryOptions;
    }
}
