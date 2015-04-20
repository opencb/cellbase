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
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by parce on 18/02/15.
 */
public class LoadRunner {

    private String database;
    private String loader;
    private Map<String, String> loaderParams;
    private final int numThreads;
    private CellBaseConfiguration cellBaseConfiguration;

    protected BlockingQueue<List<String>> blockingQueue;

    private final Logger logger;

    private static final int QUEUE_CAPACITY = 10;
    private static final int BATCH_SIZE = 1000;
    public static final List<String> POISON_PILL = new ArrayList<>();


    public LoadRunner(String loader, String database, Map<String, String> loaderParams, int numThreads,
                      CellBaseConfiguration cellBaseConfiguration) {
        this.loader = loader;
        this.database = database;
        this.loaderParams = loaderParams;
        this.numThreads = numThreads;
        this.cellBaseConfiguration = cellBaseConfiguration;

        this.blockingQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     *
     * @param filePath
     * @param data
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void load(Path filePath, String data) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, ExecutionException, InterruptedException, IOException {
        try {

            if (filePath == null || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                throw new IOException("File '" + filePath.toString() + "' does not exist or is a directory");
            }

            // One CellBaseLoader is created for each thread in 'numThreads' variable
            List<CellBaseLoader> cellBaseLoaders = new ArrayList<>(numThreads);
            for (int i=0; i < numThreads; i++) {
                // Java reflection is used to create the CellBase data loaders for a specific database engine.
                cellBaseLoaders.add((CellBaseLoader) Class.forName(loader)
                        .getConstructor(BlockingQueue.class, String.class, String.class, Map.class, CellBaseConfiguration.class)
                        .newInstance(blockingQueue, data, database, loaderParams, cellBaseConfiguration));
                logger.debug("CellBase loader thread '{}' created", i);
            }

            /*
             * ExecutorServices and Futures are created, all CellBaseLoaders are initialized and submitted to them.
             * After this the different loaders are blocked waiting for the blockingQueue to be populated.
             */
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            List<Future<Integer>> futures = new ArrayList<>(numThreads);
            for (int i=0; i < numThreads; i++) {
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
            for (int i=0; i < numThreads; i++) {
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
            if(inputFile.toString().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile.toFile()))));
            }else {
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
            }
            // Last batch
            if (!batch.isEmpty()) {
                blockingQueue.put(batch);
            }

            logger.info("{} records read from '{}'", inputFileRecords, inputFile.toString());

            // Poison Pill to consumers so they know that there are no more batches to consume
            for (int i=0; i < numThreads; i++) {
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
                .getConstructor(BlockingQueue.class, String.class, String.class, Map.class, CellBaseConfiguration.class)
                .newInstance(blockingQueue, data, database, loaderParams, cellBaseConfiguration);
        cellBaseLoader.createIndex(data);
    }

//    protected void checkLoadedRecords(int inputRecords, int loadedRecords) {
//        if (inputRecords == loadedRecords) {
//            logger.info("All records have been loaded");
//        } else {
//            logger.warn("Just " + loadedRecords + " of " + inputRecords + " have been loaded");
//        }
//    }

    //    protected List<CellBaseLoader> createCellBaseLoaders() throws ClassNotFoundException, NoSuchMethodException,
//            InvocationTargetException, InstantiationException, IllegalAccessException {
////        numCellBaseLoaders = numThreads > 2 ? numThreads - 1 : 1;
//        numCellBaseLoaders = Math.max(numThreads, 1);
//
//        List<CellBaseLoader> cellBaseLoaders = new ArrayList<>(numCellBaseLoaders);
//        for (int i=0; i < numCellBaseLoaders; i++) {
////            consumers.add(createCellBaseLoader());
//            /**
//             * This code use Java reflection to create a data serializer for a specific database engine,
//             * only a default JSON and MongoDB serializers have been implemented so far, this DI pattern
//             * may be applied to get other database outputs.
//             * This is in charge of creating the specific data model for the database backend.
//             */
//            cellBaseLoaders.add((CellBaseLoader) Class.forName(loader)
//                    .getConstructor(BlockingQueue.class, String.class, Map.class, CellBaseConfiguration.class)
//                    .newInstance(blockingQueue, data, loaderParams, cellBaseConfiguration));
//        }
//        logger.debug("Loader: {} CellBase loaders threads created", numCellBaseLoaders);
//        return cellBaseLoaders;
//    }

    //    private List<Future<Integer>> startCellBaseLoaders(ExecutorService executorService, List<CellBaseLoader> consumers) throws LoaderException {
//        List<Future<Integer>> futures = new ArrayList<>(numThreads);
//        for (CellBaseLoader cellBaseLoader : consumers) {
//            cellBaseLoader.init();
//            futures.add(executorService.submit(cellBaseLoader));
//        }
//        return futures;
//    }
}
