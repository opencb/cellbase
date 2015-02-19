package org.opencb.cellbase.core.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by parce on 18/02/15.
 */
public abstract class LoadRunner {

    private final Path inputJsonFile;
    private static final int QUEUE_CAPACITY = 10;
    private static final int BATCH_SIZE = 1000;
    public static final List<String> POISON_PILL = new ArrayList<>();
    private final int threadsNumber;
    private final Logger logger;
    protected BlockingQueue<List<String>> queue;
    private int consumersNumber;

    public LoadRunner (Path inputJsonFile, int threadsNumber) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.inputJsonFile = inputJsonFile;
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.threadsNumber = threadsNumber;
    }

    public void run() throws ExecutionException, InterruptedException {
        List<CellBaseLoader> consumers = createConsumers();
        ExecutorService executorService = Executors.newFixedThreadPool(consumersNumber);
        List<Future<Integer>> futures = startConsumers(executorService, consumers);
        int inputRecords = readInputJsonFile();
        int loadedRecords = getLoadedRecords(futures);
        this.checkLoadedRecords(inputRecords, loadedRecords);
        executorService.shutdown();

    }

    protected List<CellBaseLoader> createConsumers() {
        consumersNumber = threadsNumber > 2 ? threadsNumber - 1 : 1;

        List<CellBaseLoader> consumers = new ArrayList<>();
        for (int i=0; i < consumersNumber; i++) {
            consumers.add(createConsumer());
        }
        logger.debug(consumersNumber + " consumer threads created");
        return consumers;
    }

    protected abstract CellBaseLoader createConsumer();

    private List<Future<Integer>> startConsumers(ExecutorService executorService, List<CellBaseLoader> consumers) {
        List<Future<Integer>> futures = new ArrayList<>(consumersNumber);
        for (CellBaseLoader consumer : consumers) {
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

    protected void checkLoadedRecords(int inputRecords, int loadedRecords) {
        if (inputRecords == loadedRecords) {
            logger.info("All records have been loaded");
        } else {
            logger.warn("Just " + loadedRecords + " of " + inputRecords + " have been loaded");
        }
    }
}
