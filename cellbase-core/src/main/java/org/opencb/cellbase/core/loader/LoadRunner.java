package org.opencb.cellbase.core.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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

    public void run() {
        InputFileReaderThread producer = new InputFileReaderThread(inputJsonFile, queue);
        List<CellBaseLoader> consumers = createConsumers();
        new Thread(producer).start();
        for (CellBaseLoader consumer : consumers) {
            new Thread(consumer).start();
        }
    }

    protected List<CellBaseLoader> createConsumers() {
        consumersNumber = threadsNumber > 2 ? threadsNumber - 1 : 1;

        List<CellBaseLoader> consumers = new ArrayList<>();
        for (int i=0; i < consumersNumber; i++) {
            consumers.add(createConsumer());
        }

        return consumers;
    }

    protected abstract CellBaseLoader createConsumer();

    private class InputFileReaderThread implements Runnable{
        private final BlockingQueue<List<String>> queue;
        private Path inputJsonFile;

        public InputFileReaderThread(Path inputJsonFile, BlockingQueue<List<String>> queue) {
            this.inputJsonFile = inputJsonFile;
            this.queue = queue;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputJsonFile.toFile()))))) {
                int counter = 0;
                List<String> batch = new ArrayList<>(BATCH_SIZE);
                String jsonLine;
                while ((jsonLine = br.readLine()) != null) {
                    batch.add(jsonLine);
                    counter++;
                    if (counter % BATCH_SIZE == 0) {
                        queue.put(batch);
                        batch = new ArrayList<>(BATCH_SIZE);
                    }
                }
                // last batch
                if (!batch.isEmpty()) {
                    queue.put(batch);
                }

                logger.info(counter + " records read from " + inputJsonFile);

                // Poison Pill to consumers so they know that there are no more batchs to consume
                for (int i=0; i < consumersNumber; i++) {
                    queue.put(POISON_PILL);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

}
