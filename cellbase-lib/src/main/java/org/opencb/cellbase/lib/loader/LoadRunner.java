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

package org.opencb.cellbase.lib.loader;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.lib.managers.DataReleaseManager;
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

    private static final String PROTEIN_FUNCTIONAL_PREDICTION = "protein_functional_prediction";
    private String database;
    private String loader;

    private final int numThreads;
    private CellBaseConfiguration cellBaseConfiguration;

    protected DataReleaseManager dataReleaseManager;

    protected BlockingQueue<List<String>> blockingQueue;

    private final Logger logger;

    private static final int QUEUE_CAPACITY = 10;
    private int batchSize;
    public static final List<String> POISON_PILL = new ArrayList<>();


    public LoadRunner(String loader, String database, int numThreads, DataReleaseManager dataReleaseManager,
                      CellBaseConfiguration cellBaseConfiguration) {
        this.loader = loader;
        this.database = database;
        this.numThreads = numThreads;
        this.dataReleaseManager = dataReleaseManager;
        this.cellBaseConfiguration = cellBaseConfiguration;

        this.blockingQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Deprecated
    public void load(Path filePath, String data) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ExecutionException, InterruptedException, IOException, CellBaseException,
            LoaderException {
        load(filePath, data, 0, null, null);
    }

    public void load(Path filePath, String data, int dataRelease) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ExecutionException, InterruptedException,
            IOException, CellBaseException, LoaderException {
        load(filePath, data, dataRelease, null, null);
    }

    public void load(Path filePath, String data, int dataRelease, String field, String[] innerFields)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            ExecutionException, InterruptedException, IOException, CellBaseException, LoaderException {

        if (filePath == null || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new IOException("File '" + filePath + "' does not exist or is a directory");
        }

        // Check data release
        checkDataRelease(dataRelease, dataReleaseManager);

        // protein_functional_prediction documents are extremely big. Increasing the batch size will probably
        // lead to an OutOfMemory error for this collection. Batch size can be much higher for the rest of
        // collections though
        if (data.equals(PROTEIN_FUNCTIONAL_PREDICTION)) {
            batchSize = 50;
        } else {
            batchSize = 200;
        }

        // One CellBaseLoader is created for each thread in 'numThreads' variable
        List<CellBaseLoader> cellBaseLoaders = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            cellBaseLoaders.add((CellBaseLoader) Class.forName(loader)
                    .getConstructor(BlockingQueue.class, String.class, Integer.class, String.class, String.class,
                            String[].class, CellBaseConfiguration.class)
                    .newInstance(blockingQueue, data, dataRelease, database, field, innerFields, cellBaseConfiguration));
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
    }

    private void checkDataRelease(int release, DataReleaseManager dataReleaseManager) throws CellBaseException {
        // Check data release
        if (release < 1) {
            throw new CellBaseException("Invalid data release " + release);
        }

        DataRelease currDataRelease = dataReleaseManager.get(release);
        if (currDataRelease == null) {
            throw new CellBaseException("Loading data is not permitted since no data release " + release + " is found");
        }
        if (CollectionUtils.isNotEmpty(currDataRelease.getActiveByDefaultIn())) {
            throw new CellBaseException("Loading data is not permitted for data release " + currDataRelease.getRelease() + " since it has"
                    + " already assigned CellBase versions: " + StringUtils.join(currDataRelease.getActiveByDefaultIn(), ","));
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

            List<String> batch = new ArrayList<>(batchSize);
            String jsonLine;
            while ((jsonLine = br.readLine()) != null) {
                batch.add(jsonLine);
                inputFileRecords++;
                if (inputFileRecords % batchSize == 0) {
                    blockingQueue.put(batch);
                    batch = new ArrayList<>(batchSize);
                }
                if (inputFileRecords % batchSize == 0) {
                    logger.info("{} records read from {}", inputFileRecords, inputFile.toString());
                }
            }
            br.close();

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
}
