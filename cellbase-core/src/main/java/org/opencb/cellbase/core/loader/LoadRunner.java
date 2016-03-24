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

package org.opencb.cellbase.core.loader;

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by parce on 18/02/15.
 */
public class LoadRunner {

    private String database;
    private String loader;

    private final int numThreads;
    private CellBaseConfiguration cellBaseConfiguration;

    protected BlockingQueue<List<String>> blockingQueue;

    private final Logger logger;

    private static final int QUEUE_CAPACITY = 10;
    private static final int BATCH_SIZE = 1000;
    public static final List<String> POISON_PILL = new ArrayList<>();


    public LoadRunner(String loader, String database, int numThreads, CellBaseConfiguration cellBaseConfiguration) {
        this.loader = loader;
        this.database = database;
        this.numThreads = numThreads;
        this.cellBaseConfiguration = cellBaseConfiguration;

        this.blockingQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void load(Path filePath, String data) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ExecutionException, InterruptedException, IOException {
        load(filePath, data, null);
    }

    public void load(Path filePath, String data, String field) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ExecutionException, InterruptedException, IOException {
        try {

            if (filePath == null || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                throw new IOException("File '" + filePath + "' does not exist or is a directory");
            }

            // One CellBaseLoader is created for each thread in 'numThreads' variable
            List<CellBaseLoader> cellBaseLoaders = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++) {
                // Java reflection is used to create the CellBase data loaders for a specific database engine.
                cellBaseLoaders.add((CellBaseLoader) Class.forName(loader)
                        .getConstructor(BlockingQueue.class, String.class, String.class, String.class, CellBaseConfiguration.class)
                        .newInstance(blockingQueue, data, database, field, cellBaseConfiguration));
                logger.debug("CellBase loader thread '{}' created", i);
            }

            /*
             * ExecutorServices and Futures are created, all CellBaseLoaders are initialized and submitted to them.
             * After this the different loaders are blocked waiting for the blockingQueue to be populated.
             */
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            List<Future<Integer>> futures = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++) {
                cellBaseLoaders.get(i).init();
                futures.add(executorService.submit(cellBaseLoaders.get(i)));
                logger.debug("CellBaseLoader '{}' initialized and submitted to the ExecutorService", i);
            }

            /*
             * Execution starts by reading the file and loading batches to the blockingQueue. This makes the loaders
             * to start fetching and loading batches into the database. The number of records processed is returned.
             */
            int processedRecords = readInputJsonFile(filePath);
            // Check if all the records have been loaded
            int loadedRecords = 0;
            for (Future<Integer> future : futures) {
                loadedRecords += future.get();
            }
            if (processedRecords == loadedRecords) {
                logger.info("All the '{}' records have been loaded into the database", processedRecords);
            } else {
                logger.warn("Only '{}' out of '{}' have been loaded into the database", loadedRecords, processedRecords);
            }

            /*
             * For sanity database connection and other resources must be released. This close() call must be
             * implemented in the specific data loader.
             */
            for (int i = 0; i < numThreads; i++) {
                cellBaseLoaders.get(i).close();
                logger.debug("CellBaseLoader '{}' being closed", i);
            }

            executorService.shutdown();
        } catch (LoaderException e) {
            logger.error("Error executing CellBase Load: " + e.getMessage());
        }

    }

    private int readInputJsonFile(Path inputFile) {
        int inputFileRecords = 0;
        try {
            BufferedReader br;
            if (inputFile.toString().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile.toFile()))));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile.toFile())));
            }

            List<String> batch = new ArrayList<>(BATCH_SIZE);
            String jsonLine;
            while ((jsonLine = br.readLine()) != null) {
                batch.add(jsonLine);
                inputFileRecords++;
                if (inputFileRecords % BATCH_SIZE == 0) {
                    blockingQueue.put(batch);
                    batch = new ArrayList<>(BATCH_SIZE);
                }
                if (inputFileRecords % 1000 == 0) {
                    logger.info("{} records read from {}", inputFileRecords, inputFile.toString());
                }
            }
            // Last batch
            if (!batch.isEmpty()) {
                blockingQueue.put(batch);
            }

            logger.info("{} records read from '{}'", inputFileRecords, inputFile.toString());

            // Poison Pill to consumers so they know that there are no more batches to consume
            for (int i = 0; i < numThreads; i++) {
                blockingQueue.put(POISON_PILL);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return inputFileRecords;
    }

    public void index(String data) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException, LoaderException {
        CellBaseLoader cellBaseLoader = (CellBaseLoader) Class.forName(loader)
                .getConstructor(BlockingQueue.class, String.class, String.class, String.class, CellBaseConfiguration.class)
                .newInstance(blockingQueue, data, database, "", cellBaseConfiguration);
        cellBaseLoader.createIndex(data);
    }

}
