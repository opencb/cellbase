package org.opencb.cellbase.core.variant_annotation;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.formats.annotation.io.JsonAnnotationWriter;
import org.opencb.biodata.formats.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VariantVcfReader;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
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

    private final Path inputFile;
    private final Path outputFile;
    private static final int QUEUE_CAPACITY = 10;
    public static final int BATCH_SIZE = 1000;
    private final Logger logger;
    protected BlockingQueue<List<GenomicVariant>> variantQueue;
    protected BlockingQueue<List<VariantAnnotation>> variantAnnotationQueue;
    public static final List<GenomicVariant> VARIANT_POISON_PILL = new ArrayList<>();
    public static final List<VariantAnnotation> ANNOTATION_POISON_PILL = new ArrayList<>();
    private final int threadsNumber;
    private int annotatorsNumber;
    CellBaseClient cellBaseClient;

    public VariantAnnotatorRunner(Path inputFile, Path outputFile, CellBaseClient cellBaseClient, int threadsNumber) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.variantQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.variantAnnotationQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.cellBaseClient = cellBaseClient;
        this.threadsNumber = threadsNumber;
    }

    public void run() throws ExecutionException, InterruptedException {
        VariantAnnotationWriterThread variantAnnotationWriterThread = new VariantAnnotationWriterThread(outputFile, variantAnnotationQueue);
        List<VariantAnnotator> variantAnnotatorList = createVariantAnnotators();
        ExecutorService annotatorExecutorService = Executors.newFixedThreadPool(annotatorsNumber);
        ExecutorService writerExecutorService = Executors.newSingleThreadExecutor();
        Future<Integer> futureWrittenVariants = writerExecutorService.submit(variantAnnotationWriterThread);
        List<Future<Integer>> futureAnnotatedVariants = startAnnotators(annotatorExecutorService, variantAnnotatorList);
        int inputRecords = readInputFile();
        int annotatedRecords = getAnnotatedRecords(futureAnnotatedVariants);
        int writtendRecords = futureWrittenVariants.get();
        this.checkNumberProcessedRecords(inputRecords, annotatedRecords, writtendRecords);
        annotatorExecutorService.shutdown();
        writerExecutorService.shutdown();
    }

    protected List<VariantAnnotator> createVariantAnnotators() {
        annotatorsNumber = threadsNumber - 1;
//        annotatorsNumber = threadsNumber > 2 ? threadsNumber - 1 : 1;

        List<VariantAnnotator> variantAnnotatorList = new ArrayList<>();
        for (int i=0; i < annotatorsNumber; i++) {
            variantAnnotatorList.add(new VariantAnnotator(variantQueue, variantAnnotationQueue, cellBaseClient));
        }
        logger.debug(annotatorsNumber + " annotator threads created");
        return variantAnnotatorList;
    }

    private List<Future<Integer>> startAnnotators(ExecutorService executorService, List<VariantAnnotator> variantAnnotatorList) {
        List<Future<Integer>> futures = new ArrayList<>(annotatorsNumber);
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            futures.add(executorService.submit(variantAnnotator));
        }
        return futures;
    }

    private int readInputFile() {
//    private int readInputFile() {
        int inputFileRecords = 0;
        VariantVcfReader vcfReader = new VariantVcfReader(new VariantSource(inputFile.toString(), "", "", ""),
                inputFile.toString());
        if (vcfReader.open()) {
            vcfReader.pre();
            List<Variant> vcfBatch = vcfReader.read(BATCH_SIZE);
            List<GenomicVariant> variantBatch;
            try {
                while (!vcfBatch.isEmpty()) {
                    variantBatch = convertVcfRecordsToGenomicVariants(vcfBatch);
                    variantQueue.put(variantBatch);
                    inputFileRecords += variantBatch.size();
                    logger.info(inputFileRecords + " variants queued for annotation");
                    vcfBatch = vcfReader.read(BATCH_SIZE);
                }
                logger.info(inputFileRecords + " records read from " + inputFile);
                // Poison Pill to consumers so they know that there are no more batchs to consume
                for (int i=0; i < annotatorsNumber; i++) {
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

    private int getAnnotatedRecords(List<Future<Integer>> futures) throws InterruptedException, ExecutionException {
        int writtenRecords = 0;
        for (Future<Integer> future : futures) {
            writtenRecords += future.get();
        }
        return writtenRecords;
    }

    protected void checkNumberProcessedRecords(int inputRecords, int annotatedRecords, int writtenRecords) {
        if (inputRecords == annotatedRecords) {
            logger.info("All records have been annotated");
        } else {
            logger.warn("Just " + annotatedRecords + " of " + inputRecords + " have been annotated");
        }
        if (inputRecords == writtenRecords) {
            logger.info("All records have been annotated and their annotations written");
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
            logger.info(outputFile.toString());
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
            Integer writtenObjects = 0;
            Integer finishedAnnotators = 0;
            boolean finished = false;
            while (!finished) {
                try {
                    logger.info("Writer waits for new variants/annotations");
                    List<VariantAnnotation> batch = queue.take();
                    logger.info("Writer receives " + batch.size() + " variants/annotations");
                    if (batch == VariantAnnotatorRunner.ANNOTATION_POISON_PILL) {
                        finishedAnnotators++;
                        if(finishedAnnotators==annotatorsNumber) {
                            logger.info("Writer receives last POISON PILL. Finishes");
                            finished = true;
                        }
                    } else {
                        logger.info("Writer calls writer for " + batch.size() + " variants/annotations");
                        writer.write(batch);
                        writtenObjects += batch.size();
                        logger.info("Annotation written for " + writtenObjects + " variants");
                    }
                } catch (InterruptedException e) {
                    logger.error("Writer thread interrupted: " + e.getMessage());
                } catch (Exception e) {
                    logger.error("Error Loading batch: " + e.getMessage());
                }
            }
            logger.debug("'writing' finished. " + writtenObjects + " records written");
            this.post();
            return writtenObjects;
        }
    }

}
