package org.opencb.cellbase.core.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
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

    private final Path inputJsonFile;
    private static final int QUEUE_CAPACITY = 10;
    private static final int BATCH_SIZE = 1000;
    public static final List<String> POISON_PILL = new ArrayList<>();
    private final int threadsNumber;
    private final Logger logger;
    private final String data;
    protected BlockingQueue<List<String>> queue;
    private int consumersNumber;

    private String loader;
    Map<String, String> loaderParams;

    public LoadRunner (Path inputJsonFile, int threadsNumber, String data, Map<String, String> loaderParams) {
        this(inputJsonFile, threadsNumber, data, "org.opencb.cellbase.mongodb.loader.MongoDBCellBaseLoader", loaderParams);
    }

    public LoadRunner (Path inputJsonFile, int threadsNumber, String data, String loader, Map<String, String> loaderParams) {
        this.inputJsonFile = inputJsonFile;
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.threadsNumber = threadsNumber;

        this.data = data;
        this.loader = loader;
        this.loaderParams = loaderParams;

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void run() throws ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
            List<CellBaseLoader> consumers = createConsumers();
            ExecutorService executorService = Executors.newFixedThreadPool(consumersNumber);
            List<Future<Integer>> futures = startConsumers(executorService, consumers);
            int inputRecords = readInputJsonFile();
            int loadedRecords = getLoadedRecords(futures);
            disconnectConsumers(consumers);
            this.checkLoadedRecords(inputRecords, loadedRecords);
            executorService.shutdown();
        } catch (LoaderException e) {
            logger.error("Error executing Load: " + e.getMessage());
        }

    }

    protected List<CellBaseLoader> createConsumers() throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        consumersNumber = threadsNumber > 2 ? threadsNumber - 1 : 1;

        List<CellBaseLoader> consumers = new ArrayList<>();
        for (int i=0; i < consumersNumber; i++) {
            consumers.add(createCellBaseLoader());
        }
        logger.debug(consumersNumber + " consumer threads created");
        return consumers;
    }

    private CellBaseLoader createCellBaseLoader() throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        /**
         * This code use Java reflection to create a data serializer for a specific database engine,
         * only a default JSON and MongoDB serializers have been implemented so far, this DI pattern
         * may be applied to get other database outputs.
         * This is in charge of creating the specific data model for the database backend.
         */
        return (CellBaseLoader) Class.forName(loader).getConstructor(BlockingQueue.class, data.getClass(), Map.class).newInstance(queue, data, loaderParams);
    }

    private List<Future<Integer>> startConsumers(ExecutorService executorService, List<CellBaseLoader> consumers) throws LoaderException {
        List<Future<Integer>> futures = new ArrayList<>(consumersNumber);
        for (CellBaseLoader consumer : consumers) {
            consumer.init();
            futures.add(executorService.submit(consumer));
        }
        return futures;
    }

    private int readInputJsonFile() {
        int inputFileRecords = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputJsonFile.toFile()))))) {

            List<String> batch = new ArrayList<>(BATCH_SIZE);
            String jsonLine;
            while ((jsonLine = br.readLine()) != null) {
                batch.add(jsonLine);
                inputFileRecords++;
                if (inputFileRecords % BATCH_SIZE == 0) {
                    queue.put(batch);
                    batch = new ArrayList<>(BATCH_SIZE);
                }
            }
            // last batch
            if (!batch.isEmpty()) {
                queue.put(batch);
            }

            logger.info(inputFileRecords + " records read from " + inputJsonFile);

            // Poison Pill to consumers so they know that there are no more batchs to consume
            for (int i=0; i < consumersNumber; i++) {
                queue.put(POISON_PILL);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return inputFileRecords;
    }

    private int getLoadedRecords(List<Future<Integer>> futures) throws InterruptedException, ExecutionException {
        int loadedRecords = 0;
        for (Future<Integer> future : futures) {
            loadedRecords += future.get();
        }
        return loadedRecords;
    }

    private void disconnectConsumers(List<CellBaseLoader> consumers) {
        for (CellBaseLoader consumer : consumers) {
            consumer.disconnect();
        }
    }

    protected void checkLoadedRecords(int inputRecords, int loadedRecords) {
        if (inputRecords == loadedRecords) {
            logger.info("All records have been loaded");
        } else {
            logger.warn("Just " + loadedRecords + " of " + inputRecords + " have been loaded");
        }
    }
}
