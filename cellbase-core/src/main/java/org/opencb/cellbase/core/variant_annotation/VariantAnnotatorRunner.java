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

package org.opencb.cellbase.core.variant_annotation;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.formats.annotation.io.JsonAnnotationWriter;
import org.opencb.biodata.formats.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.io.VariantVcfReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by fjlopez on 02/03/15.
 */
public class VariantAnnotatorRunner {

    private Path inputFile;
    private Path outputFile;
    protected BlockingQueue<List<GenomicVariant>> variantQueue;
    protected BlockingQueue<List<VariantAnnotation>> variantAnnotationQueue;
    public static List<GenomicVariant> VARIANT_POISON_PILL = new ArrayList<>();
    public static List<VariantAnnotation> ANNOTATION_POISON_PILL = new ArrayList<>();
    private int numThreads;
    private int batchSize;
    private CellBaseClient cellBaseClient;

    private static int NUM_THREADS = 4;
    private static int BATCH_SIZE = 200;
    private final int QUEUE_CAPACITY = 10;

    private Logger logger;

    public VariantAnnotatorRunner(Path inputFile, Path outputFile, CellBaseClient cellBaseClient) {
        this(inputFile, outputFile, cellBaseClient, NUM_THREADS, BATCH_SIZE);
    }

    public VariantAnnotatorRunner(Path inputFile, Path outputFile, CellBaseClient cellBaseClient, int numThreads, int batchSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.variantQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.variantAnnotationQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.cellBaseClient = cellBaseClient;
        this.numThreads = numThreads;
        this.batchSize = batchSize;

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void run() throws ExecutionException, InterruptedException {

        /*
         * ExecutorServices and Futures are created, all VariantAnnotators are initialized and submitted to them.
         * After this the different variant annotators are blocked waiting for the blockingQueue to be populated.
         */
        ExecutorService annotatorExecutorService = Executors.newFixedThreadPool(numThreads);
        List<VariantAnnotator> variantAnnotatorList = createVariantAnnotators();
        List<Future<Integer>> futureAnnotatedVariants = startAnnotators(annotatorExecutorService, variantAnnotatorList);

        ExecutorService writerExecutorService = Executors.newSingleThreadExecutor();
        VariantAnnotationWriterThread variantAnnotationWriterThread = new VariantAnnotationWriterThread(outputFile, variantAnnotationQueue);
        Future<Integer> futureWrittenVariants = writerExecutorService.submit(variantAnnotationWriterThread);

        /*
         * Execution starts by reading the file and loading batches to the blockingQueue. This makes the loaders
         * to start fetching and loading batches into the database. The number of records processed is returned.
         */
        int inputRecords = readInputFile();

        // Check everything has been precessed correctly
        int annotatedRecords = 0;
        for (Future<Integer> future : futureAnnotatedVariants) {
            annotatedRecords += future.get();
        }
        int writtenRecords = futureWrittenVariants.get();
        this.checkNumberProcessedRecords(inputRecords, annotatedRecords, writtenRecords);

        // Shutdown all services
        annotatorExecutorService.shutdown();
        writerExecutorService.shutdown();
    }

    protected List<VariantAnnotator> createVariantAnnotators() {
        List<VariantAnnotator> variantAnnotatorList = new ArrayList<>();
        for (int i=0; i < numThreads; i++) {
            variantAnnotatorList.add(new VariantAnnotator(variantQueue, variantAnnotationQueue, cellBaseClient));
            logger.debug("Variant annotator thread '{}' created", i);
        }
        return variantAnnotatorList;
    }

    private List<Future<Integer>> startAnnotators(ExecutorService executorService, List<VariantAnnotator> variantAnnotatorList) {
        List<Future<Integer>> futures = new ArrayList<>(numThreads);
        for (int i = 0; i < variantAnnotatorList.size(); i++) {
            futures.add(executorService.submit(variantAnnotatorList.get(i)));
            logger.debug("Variant annotator '{}' initialized and submitted to the ExecutorService", i);
        }
        logger.info("{} threads created and running", numThreads);
        return futures;
    }

    private int readInputFile() {
//    private int readInputFile() {
        int inputFileRecords = 0;
        VariantVcfReader vcfReader = new VariantVcfReader(new VariantSource(inputFile.toString(), "", "", ""),
                inputFile.toString());
        if (vcfReader.open()) {
            vcfReader.pre();
            List<Variant> vcfBatch = vcfReader.read(batchSize);
            List<GenomicVariant> variantBatch;
            try {
                while (!vcfBatch.isEmpty()) {
                    variantBatch = convertVcfRecordsToGenomicVariants(vcfBatch);
                    variantQueue.put(variantBatch);
                    inputFileRecords += variantBatch.size();
                    vcfBatch = vcfReader.read(batchSize);

                    if(inputFileRecords % 2000 == 0) {
                        logger.info("{} variants annotated", inputFileRecords);
                    }
                    logger.debug("{} variants queued for annotation", inputFileRecords);
                }
                logger.info("{} variants read and processed from {}", inputFileRecords, inputFile);

                // Poison Pill to consumers so they know that there are no more batchs to consume
                for (int i=0; i < numThreads; i++) {
                    variantQueue.put(VARIANT_POISON_PILL);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
            vcfReader.post();
            vcfReader.close();
        }
        return inputFileRecords;
    }

    private List<GenomicVariant> convertVcfRecordsToGenomicVariants(List<Variant> vcfBatch) {
        List<GenomicVariant> genomicVariants = new ArrayList<>(vcfBatch.size());
        for (Variant variant : vcfBatch) {
            GenomicVariant genomicVariant;
            if((genomicVariant = getGenomicVariant(variant))!=null) {
                genomicVariants.add(genomicVariant);
            }
        }
        return genomicVariants;
    }

    // TODO: use a external class for this (this method could be added to GenomicVariant class)
    private GenomicVariant getGenomicVariant(Variant variant) {
        if(variant.getAlternate().equals(".")) {  // reference positions are not variants
            return null;
        } else {
            String ref;
            if (variant.getAlternate().equals("<DEL>")) {  // large deletion
                int end = Integer.valueOf(variant.getSourceEntries().get("_").getAttributes().get("END"));  // .get("_") because studyId and fileId are empty strings when VariantSource is initialized at readInputFile
                ref = StringUtils.repeat("N", end - variant.getStart());
            } else {
                ref = variant.getReference().equals("") ? "-" : variant.getReference();
            }
            return new GenomicVariant(variant.getChromosome(), variant.getStart(),
                    ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
            //        return new GenomicVariant(variant.getChromosome(), ensemblPos, ref, alt);
        }
    }

    protected void checkNumberProcessedRecords(int inputRecords, int annotatedRecords, int writtenRecords) {
//        if (inputRecords == annotatedRecords) {
//            logger.info("All {} variants have been annotated", inputRecords);
//        } else {
//            logger.warn("Just " + annotatedRecords + " of " + inputRecords + " have been annotated");
//        }
        if (inputRecords == writtenRecords) {
            logger.info("All {} variants have been annotated and their annotations written", inputRecords);
        } else {
            logger.warn("Annotations for just " + writtenRecords + " of " + inputRecords + " have been written");
        }
    }

    private class VariantAnnotationWriterThread implements Callable<Integer>{
        private final BlockingQueue<List<VariantAnnotation>> queue;
        private Path outputFile;
        private DataWriter<VariantAnnotation> writer;

        public VariantAnnotationWriterThread(Path outputFile, BlockingQueue<List<VariantAnnotation>> queue) {
            this.outputFile = outputFile;
            this.queue = queue;
        }

        private void pre() {
            logger.info("Opening file {} for writing", outputFile.toString());
            if(outputFile.toString().endsWith(".json")) {
                this.writer = new JsonAnnotationWriter(outputFile.toString());
            } else {
                this.writer = new VepFormatWriter(outputFile.toString());
            }
            if(!this.writer.open()) {
                logger.error("Error opening output file: "+outputFile.toString());
            }
            this.writer.pre();
        }

        private  void post() {
            this.writer.post();
            this.writer.close();
        }

        @Override
        public Integer call() {
            this.pre();
            int writtenObjects = 0;
            int finishedAnnotators = 0;
            boolean finished = false;
            while (!finished) {
                try {
//                    logger.info("Writer waits for new variants/annotations");
                    List<VariantAnnotation> batch = queue.take();
                    logger.debug("Writer receives {} variants/annotations", batch.size());
                    if (batch == VariantAnnotatorRunner.ANNOTATION_POISON_PILL) {
                        finishedAnnotators++;
                        if(finishedAnnotators == numThreads) {
                            finished = true;
                            logger.debug("Writer receives last POISON PILL. Finishes");
                        }
                    } else {
//                        logger.info("Writer calls writer for " + batch.size() + " variants/annotations");
                        writer.write(batch);
                        writtenObjects += batch.size();
                        logger.debug("Annotation written for {} variants", writtenObjects);
                    }
                } catch (InterruptedException e) {
                    logger.error("Writer thread interrupted: " + e.getMessage());
                } catch (Exception e) {
                    logger.error("Error Loading batch: " + e.getMessage());
                }
            }
            logger.info("{} variants annotated and written into {}", writtenObjects, outputFile);
            this.post();
            return writtenObjects;
        }
    }

}
