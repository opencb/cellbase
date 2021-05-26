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

import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.opencb.biodata.formats.variant.annotation.io.JsonAnnotationWriter;
import org.opencb.biodata.formats.variant.annotation.io.VariantAvroDataWriter;
import org.opencb.biodata.formats.variant.annotation.io.VepFormatReader;
import org.opencb.biodata.formats.variant.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantFileMetadata;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.tools.sequence.FastaIndexManager;
import org.opencb.biodata.tools.variant.VariantJsonReader;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.cellbase.app.cli.variant.annotation.*;
import org.opencb.cellbase.app.cli.variant.annotation.indexers.CustomAnnotationVariantIndexer;
import org.opencb.cellbase.app.cli.variant.annotation.indexers.PopulationFrequencyVariantIndexer;
import org.opencb.cellbase.app.cli.variant.annotation.indexers.VariantIndexer;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.rest.CellBaseClient;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.CellBaseNormalizerSequenceAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.run.ParallelTaskRunner;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;

/**
 * Created by fjlopez on 18/03/15.
 */
public class VariantAnnotationCommandExecutor extends CommandExecutor {

    public enum FileFormat {VCF, JSON, AVRO, VEP};

    private CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions;

    private Path input;
    private Path output;
    private String url;
    private boolean local;
    private boolean cellBaseAnnotation;
    private boolean benchmark;
    private Path referenceFasta;
    private boolean normalize;
    private boolean decompose;
    private boolean leftAlign;
    private List<String> chromosomeList;
    private int port;
    private String species;
    private String assembly;
    private int numThreads;
    private int batchSize;
    private List<Path> customFiles;
    private Path populationFrequenciesFile = null;
    private Boolean completeInputPopulation;
    private List<VariantIndexer> variantIndexerList;
    private List<String> customFileIds;
    private List<List<String>> customFileFields;
    private int maxOpenFiles = -1;
    private FileFormat inputFormat;
    private FileFormat outputFormat;

    // Only options meant to be sent to the server should be included in this serverQueryOptions
    private QueryOptions serverQueryOptions;

    private DBAdaptorFactory dbAdaptorFactory = null;

    private final int QUEUE_CAPACITY = 10;
    private final String TMP_DIR = "/tmp/";
    private static final String VARIATION_ANNOTATION_FILE_PREFIX = "variation_annotation_";

    public VariantAnnotationCommandExecutor(CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions) {
        super(variantAnnotationCommandOptions.commonOptions.logLevel, variantAnnotationCommandOptions.commonOptions.verbose,
                variantAnnotationCommandOptions.commonOptions.conf);

        this.variantAnnotationCommandOptions = variantAnnotationCommandOptions;
        this.serverQueryOptions = new QueryOptions();

        if (variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
        }

        if (variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
        }
    }

    @Override
    public void execute() {

        try {
            checkParameters();
            if (benchmark) {
                runBenchmark();
            } else {
                runAnnotation();
            }
            logger.info("Finished");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void runBenchmark() {
        try {

            FastaIndexManager fastaIndexManager = getFastaIndexManger();
            DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
                return entry.getFileName().toString().endsWith(".vep");
            });

            DataWriter<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> dataWriter = new BenchmarkDataWriter("VEP", "CellBase", output);
            ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
            List<ParallelTaskRunner.TaskWithException<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>, Exception>>
                    variantAnnotatorTaskList = getBenchmarkTaskList(fastaIndexManager);
            for (Path entry : stream) {
                logger.info("Processing file '{}'", entry.toString());
                DataReader dataReader = new VepFormatReader(input.resolve(entry.getFileName()).toString());
                ParallelTaskRunner<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>> runner
                        = new ParallelTaskRunner<>(dataReader, variantAnnotatorTaskList, dataWriter, config);
                runner.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FastaIndexManager getFastaIndexManger() {
        // Preparing the fasta file for fast accessing
        FastaIndexManager fastaIndexManager = null;
        try {
            fastaIndexManager = new FastaIndexManager(referenceFasta, true);
            if (!fastaIndexManager.isConnected()) {
                fastaIndexManager.index();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fastaIndexManager;
    }

    private List<ParallelTaskRunner.TaskWithException<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>, Exception>>
    getBenchmarkTaskList(FastaIndexManager fastaIndexManager) throws IOException {
        List<ParallelTaskRunner.TaskWithException<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>, Exception>>
                benchmarkTaskList = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            // Benchmark variants are read from a VEP file, must not normalize
            benchmarkTaskList.add(new BenchmarkTask(createCellBaseAnnotator(), fastaIndexManager));
        }
        return benchmarkTaskList;
    }

    private boolean runAnnotation() throws Exception {

        // Build indexes for custom files and/or population frequencies file
        getIndexes();
        try {
            if (variantAnnotationCommandOptions.variant != null && !variantAnnotationCommandOptions.variant.isEmpty()) {
                List<Variant> variants = Variant.parseVariants(variantAnnotationCommandOptions.variant);
                if (local) {
                    DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
                    VariantAnnotationCalculator variantAnnotationCalculator =
                            new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory);
                    List<QueryResult<VariantAnnotation>> annotationByVariantList =
                            variantAnnotationCalculator.getAnnotationByVariantList(variants, serverQueryOptions);

                    ObjectMapper jsonObjectMapper = new ObjectMapper();
                    jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
                    ObjectWriter objectWriter = jsonObjectMapper.writer();

                    Path outPath = Paths.get(variantAnnotationCommandOptions.output);
                    FileUtils.checkDirectory(outPath.getParent());
                    BufferedWriter bufferedWriter = FileUtils.newBufferedWriter(outPath);
                    for (QueryResult queryResult : annotationByVariantList) {
                        bufferedWriter.write(objectWriter.writeValueAsString(queryResult.getResult()));
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.close();
                }
                return true;
            }

            // If a variant file is provided then we annotate it. Lines in the input file can be computationally
            // expensive to parse, i.e.: multisample vcf with thousands of samples. A specific task is created to enable
            // parallel parsing of these lines
            if (input != null) {
                VariantReader variantReader = getVariantReader(input);
                List<ParallelTaskRunner.TaskWithException<Variant, Variant, Exception>> variantAnnotatorTaskList
                        = getVariantAnnotatorTaskList();
                DataWriter<Variant> dataWriter = getVariantDataWriter(output.toString());

                ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
                ParallelTaskRunner<Variant, Variant> runner =
                        new ParallelTaskRunner<Variant, Variant>(variantReader, variantAnnotatorTaskList, dataWriter, config);
                runner.run();
                // For internal use only - will only be run when -Dpopulation-frequencies is activated
                writeRemainingPopFrequencies();
            } else {
                // This will annotate the CellBase Variation collection
                if (cellBaseAnnotation) {
                    // TODO: enable this query in the parseQuery method within VariantMongoDBAdaptor
//                    Query query = new Query("$match",
//                            new Document("annotation.consequenceTypes", new Document("$exists", 0)));
//                    Query query = new Query();
                    QueryOptions options = new QueryOptions("include", "chromosome,start,reference,alternate,type");
                    List<ParallelTaskRunner.TaskWithException<Variant, Variant, Exception>> variantAnnotatorTaskList
                            = getVariantAnnotatorTaskList();
                    ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);

                    for (String chromosome : chromosomeList) {
                        logger.info("Annotating chromosome {}", chromosome);
                        Query query = new Query("chromosome", chromosome);
                        DataReader<Variant> dataReader =
                                new VariationDataReader(dbAdaptorFactory.getVariationDBAdaptor(species), query, options);
                        DataWriter<Variant> dataWriter = getVariantDataWriter(output.toString() + "/"
                                + VARIATION_ANNOTATION_FILE_PREFIX + chromosome + ".json.gz");
                        ParallelTaskRunner<Variant, Variant> runner =
                                new ParallelTaskRunner<Variant, Variant>(dataReader, variantAnnotatorTaskList, dataWriter, config);
                        runner.run();
                    }
                }
            }
        } finally {
            if (customFiles != null || populationFrequenciesFile != null) {
                closeIndexes();
            }
            if (dbAdaptorFactory != null) {
                dbAdaptorFactory.close();
            }
        }

        logger.info("Variant annotation finished.");
        return false;
    }

    private VariantReader getVariantReader(Path input) throws IOException {
        return getVariantReader(input, serverQueryOptions.getBoolean("ignorePhase"));
    }

    private VariantReader getVariantReader(Path input, boolean ignorePhase) throws IOException {
        // Leaving variantNormalizer = null if CLI indicates to skip normalisation. If no normalizer is provided to
        // the readers they will NOT perform normalisation
        VariantNormalizer variantNormalizer = normalize ? new VariantNormalizer(getNormalizerConfig()) : null;

        switch (getFileFormat(input)) {
            case VCF:
                logger.info("Using HTSJDK to read variants.");
                return (new VariantVcfHtsjdkReader(input,
                        new VariantFileMetadata(input.getFileName().toString(),
                                input.toAbsolutePath().toString()).toVariantStudyMetadata(input.getFileName()
                                .toString()), variantNormalizer)).setIgnorePhaseSet(ignorePhase);
            case JSON:
                logger.info("Using a JSON parser to read variants...");
                return new VariantJsonReader(input, variantNormalizer);
            default:
                throw new ParameterException("Only VCF and JSON formats are currently accepted. Please provide a "
                        + "valid .vcf, .vcf.gz, json or .json.gz file");
        }
    }

    private void writeRemainingPopFrequencies() throws IOException {
        // For internal use only - will only be run when -Dpopulation-frequencies is activated
        if (populationFrequenciesFile != null) {
            if (completeInputPopulation) {
                DataWriter dataWriter = new JsonAnnotationWriter(output.toString(), APPEND);
                dataWriter.open();
                dataWriter.pre();

                // Population frequencies rocks db will always be the last one in the list. DO NOT change the name of the
                // rocksIterator variable - for some unexplainable reason Java VM crashes if it's named "iterator"
                RocksIterator rocksIterator = variantIndexerList.get(variantIndexerList.size() - 1)
                        .getDbIndex()
                        .newIterator();

                ObjectMapper mapper = new ObjectMapper();
                logger.info("Writing variants with frequencies that were not found within the input file {} to {}",
                        populationFrequenciesFile.toString(), output.toString());
                int counter = 0;
                for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                    Variant variant = mapper.readValue(rocksIterator.value(), Variant.class);
                    // The additional attributes field initialized with an empty map is used as the flag to indicate that
                    // this variant was not visited during the annotation process
                    if (variant.getAnnotation().getAdditionalAttributes() == null) {
                        dataWriter.write(variant);
                    }

                    counter++;
                    if (counter % 10000 == 0) {
                        logger.info("{} written", counter);
                    }
                }
                dataWriter.post();
                dataWriter.close();
                logger.info("Done.");
            } else {
                logger.warn("complete-input-population set to false, variants in population frequencies file {} not in "
                        + "input file {} will not be appended to output file.", populationFrequenciesFile, input);
            }
        }
    }

    private void setChromosomeList() {

        if (variantAnnotationCommandOptions.chromosomeList != null
                && !variantAnnotationCommandOptions.chromosomeList.isEmpty()) {
            chromosomeList = Arrays.asList(variantAnnotationCommandOptions.chromosomeList.split(","));
            logger.info("Setting chromosomes {} for variant annotation", chromosomeList.toString());
        // If the user does not provide any chromosome, fill chromosomeList with all available chromosomes in the
        // database
        } else {
            logger.info("Getting full list of chromosome names in the database");
            dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
            GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
            QueryResult queryResult = genomeDBAdaptor.getGenomeInfo(new QueryOptions("include", "chromosomes.name"));

            List<Document> chromosomeDocumentList = (List<Document>) ((List<Document>) queryResult.getResult()).get(0).get("chromosomes");
            chromosomeList = new ArrayList<>(chromosomeDocumentList.size());
            for (Document chromosomeDocument : chromosomeDocumentList) {
                chromosomeList.add((String) chromosomeDocument.get("name"));
            }
            logger.info("Available chromosomes: {}", chromosomeList.toString());
        }
    }

    private DataWriter<Variant> getVariantDataWriter(String filename) {
        DataWriter<Variant> dataWriter = null;
        if (outputFormat.equals(FileFormat.JSON)) {
            dataWriter = new JsonAnnotationWriter(filename);
        } else if (outputFormat.equals(FileFormat.AVRO)) {
            ProgressLogger progressLogger = new ProgressLogger("Num written variants:");
            dataWriter = new VariantAvroDataWriter(Paths.get(filename), true)
                    .setProgressLogger(progressLogger);
        } else if (outputFormat.equals(FileFormat.VEP)) {
            dataWriter = new VepFormatWriter(filename);
        }

        return dataWriter;
    }

    private List<ParallelTaskRunner.TaskWithException<Variant, Variant, Exception>> getVariantAnnotatorTaskList() throws IOException {
        List<ParallelTaskRunner.TaskWithException<Variant, Variant, Exception>> variantAnnotatorTaskList = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            List<VariantAnnotator> variantAnnotatorList = createAnnotators();
            variantAnnotatorTaskList.add(new VariantAnnotatorTask(variantAnnotatorList, serverQueryOptions));
        }
        return variantAnnotatorTaskList;
    }

    private VariantNormalizer.VariantNormalizerConfig getNormalizerConfig() throws IOException {
        VariantNormalizer.VariantNormalizerConfig variantNormalizerConfig = (new VariantNormalizer.VariantNormalizerConfig())
                .setReuseVariants(true)
                .setNormalizeAlleles(false)
                .setDecomposeMNVs(decompose);

        // Enable left align
        if (leftAlign) {
            // WARN: If --reference-fasta is present will override CellBase reference genome even if --local was present
            if (referenceFasta != null) {
                return variantNormalizerConfig.enableLeftAlign(referenceFasta.toString());
            } else {
                // dbAdaptorFactory may have been already initialized while creating CellBase annotators or at execute if
                // annotating CellBase variation collection
                if (dbAdaptorFactory == null) {
                    dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
                }
                return variantNormalizerConfig
                        .enableLeftAlign(new CellBaseNormalizerSequenceAdaptor(dbAdaptorFactory
                                .getGenomeDBAdaptor(species, assembly)));
            }
        }
        return variantNormalizerConfig;
    }

    private void closeIndexes() throws IOException {
        for (VariantIndexer variantIndexer : variantIndexerList) {
            variantIndexer.close();
        }

        if (populationFrequenciesFile != null) {
            // Rocks db indexer for population frequencies  is always the last in the list
            org.apache.commons.io.FileUtils
                    .deleteDirectory(new File(variantIndexerList.get(variantIndexerList.size() - 1).getDbLocation()));
        }
    }

    private List<VariantAnnotator> createAnnotators() {
        List<VariantAnnotator> variantAnnotatorList;
        variantAnnotatorList = new ArrayList<>();

        // CellBase annotator is always called
        variantAnnotatorList.add(createCellBaseAnnotator());

        // Include custom annotators if required
        if (customFiles != null) {
            for (int i = 0; i < customFiles.size(); i++) {
                if (customFiles.get(i).toString().endsWith(".vcf") || customFiles.get(i).toString().endsWith(".vcf.gz")) {
                    variantAnnotatorList.add(new VcfVariantAnnotator(customFiles.get(i).toString(),
                            variantIndexerList.get(i).getDbIndex(),
                            customFileIds.get(i),
                            serverQueryOptions));
                }
            }
        }

        // Include population-frequencies file if required
        if (populationFrequenciesFile != null) {
            // Rocks db indexer for population frequencies  is always the last in the list
            int i = variantIndexerList.size() - 1;
            variantAnnotatorList.add(new PopulationFrequenciesAnnotator(populationFrequenciesFile.toString(),
                    variantIndexerList.get(i).getDbIndex(), serverQueryOptions));

        }

        return variantAnnotatorList;
    }

    private VariantAnnotator createCellBaseAnnotator() {
        // Assume annotation of CellBase variation collection will always be carried out from a local installation
        if (local || cellBaseAnnotation) {
            // dbAdaptorFactory may have been already initialized at execute if annotating CellBase variation collection
            if (dbAdaptorFactory == null) {
                dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
            }
            // Normalization should just be performed in one place: before calling the annotation calculator - within the
            // corresponding *AnnotatorTask since the AnnotatorTasks need that the number of sent variants coincides
            // equals the number of returned annotations
            return new CellBaseLocalVariantAnnotator(new VariantAnnotationCalculator(species, assembly,
                    dbAdaptorFactory), serverQueryOptions);
        } else {
            try {
                ClientConfiguration clientConfiguration = ClientConfiguration.load(getClass()
                        .getResourceAsStream("/client-configuration.yml"));
                if (url != null) {
                    clientConfiguration.getRest().setHosts(Collections.singletonList(url));
                }
                clientConfiguration.setDefaultSpecies(species);
                CellBaseClient cellBaseClient;
                cellBaseClient = new CellBaseClient(clientConfiguration);
                logger.debug("URL set to: {}", url);

                // TODO: normalization must be carried out in the client - phase set must be sent together with the
                // TODO: variant string to the server for proper phase annotation by REST
                return new CellBaseWSVariantAnnotator(cellBaseClient.getVariantClient(), serverQueryOptions);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    private void getIndexes() throws IOException, RocksDBException {
        variantIndexerList = new ArrayList<>();

        // Index custom files if provided
        if (customFiles != null) {
            for (int i = 0; i < customFiles.size(); i++) {
                // Setting ignorePhase=true since the reader for the custom annotation indexer does not care
                // about batches splitting phase sets
                VariantIndexer variantIndexer
                        = new CustomAnnotationVariantIndexer(getVariantReader(customFiles.get(i), true),
                        maxOpenFiles,
                        customFileFields.get(i));
                variantIndexer.open();
                variantIndexer.run();
                variantIndexerList.add(variantIndexer);
            }
        }

        // Index population frequencies file if provided
        if (populationFrequenciesFile != null) {
            VariantReader variantReader = getVariantReader(populationFrequenciesFile);

            // We force the creation of a new index even if there was one already - Annotation of frequencies from
            // these files implies deletions on the RocksDB database. Whatever is already there will probably be wrong
            VariantIndexer variantIndexer = new PopulationFrequencyVariantIndexer(variantReader,
                    maxOpenFiles,
                    true);
            variantIndexer.open();
            variantIndexer.run();
            variantIndexerList.add(variantIndexer);
        }
    }

    private void checkParameters() throws IOException {

        // Get reference genome
        if (org.apache.commons.lang.StringUtils.isNotBlank(variantAnnotationCommandOptions.referenceFasta)) {
            referenceFasta = Paths.get(variantAnnotationCommandOptions.referenceFasta);
            FileUtils.checkFile(referenceFasta);
        }

        // Run benchmark
        benchmark = variantAnnotationCommandOptions.benchmark;
        if (benchmark) {
            if (referenceFasta == null) {
                throw new ParameterException("Reference genome must be provided for running the benchmark. Please, "
                        + "provide a valid path to a fasta file with the reference genome sequence by using the "
                        + "--reference-fasta parameter.");
            }
        }

        // input file
        if (variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
            if (benchmark) {
                FileUtils.checkDirectory(input);
                normalize = false;
            } else {
                normalize =  !variantAnnotationCommandOptions.skipNormalize;
                FileUtils.checkFile(input);
                inputFormat = getFileFormat(input);
            }
        // Expected to read from variation collection - normalization must be avoided
        } else {
            normalize = false;
        }

        parsePhaseConfiguration();
        decompose = !variantAnnotationCommandOptions.skipDecompose;
        leftAlign = !variantAnnotationCommandOptions.skipLeftAlign;
        // Update serverQueryOptions
        serverQueryOptions.put("checkAminoAcidChange", variantAnnotationCommandOptions.checkAminoAcidChange);
        serverQueryOptions.put("filter", variantAnnotationCommandOptions.filter);
        // output file
        if (variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
            // output.getParent may be null if for example the output is specified with no path at all, i.e
            // -o test.vcf rather than -o ./test.vcf
            if (output.getParent() != null) {
                try {
                    FileUtils.checkDirectory(output.getParent());
                } catch (IOException e) {
                    throw new ParameterException(e);
                }
            }
//            if (!outputDir.toFile().exists()) {
//                throw new ParameterException("Output directory " + outputDir + " doesn't exist");
//            } else if (output.toFile().isDirectory()) {
//                throw new ParameterException("Output file cannot be a directory: " + output);
//            }
        } else {
            throw new ParameterException("Please check command line sintax. Provide a valid output file name.");
        }

        if (variantAnnotationCommandOptions.outputFormat != null) {
            switch (variantAnnotationCommandOptions.outputFormat.toLowerCase()) {
                case "json":
                    outputFormat = FileFormat.JSON;
                    break;
                case "avro":
                    outputFormat = FileFormat.AVRO;
                    break;
                case "vep":
                    outputFormat = FileFormat.VEP;
                    break;
                default:
                    throw  new ParameterException("Only JSON and VEP output formats are currently available. Please, select one of them.");
            }

        }

        // Normalisation nor decomposition are NEVER performed on the server. This QueryOptions is meant to be sent
        // to the server. Actual normalization and decomposition options are set and processed here in the server code
        // using this.decompose and this.normalize fields.
        serverQueryOptions.add("normalize", false);
        serverQueryOptions.add("skipDecompose", true);

        if (variantAnnotationCommandOptions.include != null && !variantAnnotationCommandOptions.include.isEmpty()) {
            serverQueryOptions.add("include", variantAnnotationCommandOptions.include);
        }

        if (variantAnnotationCommandOptions.exclude != null && !variantAnnotationCommandOptions.exclude.isEmpty()) {
            serverQueryOptions.add("exclude", variantAnnotationCommandOptions.exclude);
        }

        // Num threads
        if (variantAnnotationCommandOptions.numThreads > 1) {
            numThreads = variantAnnotationCommandOptions.numThreads;
        } else {
            numThreads = 1;
            logger.warn("Incorrect number of numThreads, it must be a positive value. This has been reset to '{}'", numThreads);
        }

        // Batch size
        if (variantAnnotationCommandOptions.batchSize >= 1 && variantAnnotationCommandOptions.batchSize <= 2000) {
            batchSize = variantAnnotationCommandOptions.batchSize;
        } else {
            batchSize = 1;
            logger.warn("Incorrect size of batch size, it must be a positive value between 1-1000. This has been set to '{}'", batchSize);
        }

        // Direct connection to local MongoDB
        local = variantAnnotationCommandOptions.local;
        if (!variantAnnotationCommandOptions.local) {
            // Url
            if (variantAnnotationCommandOptions.url != null) {
                url = variantAnnotationCommandOptions.url;
            } else {
                throw new ParameterException("Please check command line sintax. Provide a valid URL to access CellBase web services.");
            }
            // Left align in remote mode can only be enabled if a reference fasta is provided
            if (leftAlign) {
                if (referenceFasta == null) {
                    throw new ParameterException("Please provide a valid reference fasta file. Left align when annotating"
                            + " in remote mode (--local flag NOT present) can only be enabled if a fasta file with"
                            + " the reference genome sequence is provided within --reference-fasta. Alternatively"
                            + " you can disable left align by using --skip-left-align.");
                }
            }
        // --local flag enabled
        // Use of --reference-fasta and --local together will cause --reference-fasta to override the reference genome
        // in CellBase database (DISCOURAGED!)
        } else if (leftAlign && referenceFasta != null) {
            logger.warn("--reference-fasta and --local parameters found together. This is strongly discouraged. Please"
                    + " NOTE: the sequence within the fasta file will override CellBase reference sequence.");
        }

        // Species
        if (variantAnnotationCommandOptions.species != null) {
            species = variantAnnotationCommandOptions.species;
        } else {
            throw new ParameterException("Please check command line syntax. Provide a valid species name.");
        }

        // Assembly
        if (variantAnnotationCommandOptions.assembly != null) {
            assembly = variantAnnotationCommandOptions.assembly;
            // In case annotation is made through WS assembly must be set in the serverQueryOptions
            serverQueryOptions.put("assembly", variantAnnotationCommandOptions.assembly);
        } else {
            assembly = null;
            logger.warn("No assembly provided. Using default assembly for {}", species);
        }

        // Custom files
        if (variantAnnotationCommandOptions.customFiles != null) {
            String[] customFileStrings = variantAnnotationCommandOptions.customFiles.split(",");
            customFiles = new ArrayList<>(customFileStrings.length);
            for (String customFile : customFileStrings) {
                Path customFilePath = Paths.get(customFile);
                FileUtils.checkFile(customFilePath);
                if (!(customFilePath.toString().endsWith(".vcf") || customFilePath.toString().endsWith(".vcf.gz"))) {
                    throw new ParameterException("Only VCF format is currently accepted for custom annotation files.");
                }
                customFiles.add(customFilePath);
            }
            if (variantAnnotationCommandOptions.customFileIds == null) {
                throw new ParameterException("Parameter --custom-file-ids missing. Please, provide one short id for each custom file in "
                        + "a comma separated list (no spaces in betwen).");
            }
            customFileIds = Arrays.asList(variantAnnotationCommandOptions.customFileIds.split(","));
            if (customFileIds.size() != customFiles.size()) {
                throw new ParameterException("Different number of custom files and custom file ids. Please, "
                        + "provide one short id for each custom file in a comma separated list (no spaces in between).");
            }
            if (variantAnnotationCommandOptions.customFileFields == null) {
                throw new ParameterException("Parameter --custom-file-fields missing. Please, provide one list of fields for each "
                        + "custom file in a colon separated list (no spaces in betwen).");
            }
            String[] customFileFieldStrings = variantAnnotationCommandOptions.customFileFields.split(":");
            if (customFileFieldStrings.length != customFiles.size()) {
                throw new ParameterException("Different number of custom files and lists of custom file fields. "
                        + "Please, provide one list of fields for each custom file in a colon separated list (no spaces in between).");
            }
            customFileFields = new ArrayList<>(customFileStrings.length);
            for (String fieldString : customFileFieldStrings) {
                customFileFields.add(Arrays.asList(fieldString.split(",")));
            }
            // MaxOpenFiles parameter for RocksDB indexation of custom files
            maxOpenFiles = variantAnnotationCommandOptions.maxOpenFiles;
        }

        // Semi-private build parameter for us to build the variation collection including population frequencies
        if (variantAnnotationCommandOptions.buildParams.get("population-frequencies") != null) {
            populationFrequenciesFile = Paths.get(variantAnnotationCommandOptions.buildParams.get("population-frequencies"));
            FileUtils.checkFile(populationFrequenciesFile);
            if (!(populationFrequenciesFile.toString().endsWith(".json")
                    || populationFrequenciesFile.toString().endsWith(".json.gz"))) {
                throw new ParameterException("Population frequencies file must be a .json (.json.gz) file containing"
                        + " Variant objects.");
            }
            completeInputPopulation = Boolean.valueOf(variantAnnotationCommandOptions.buildParams.get("complete-input-population"));
        }

        // Enable/Disable imprecise annotation
        serverQueryOptions.put("imprecise", !variantAnnotationCommandOptions.noImprecision);

        // Parameter not expected to be very used - provide extra padding (bp) to be used for structural variant annotation
        if (variantAnnotationCommandOptions.buildParams.get("sv-extra-padding") != null) {
            Integer svExtraPadding = Integer.valueOf(variantAnnotationCommandOptions.buildParams.get("sv-extra-padding"));
            if (svExtraPadding < 0) {
                throw new ParameterException("Extra padding for SV annotation cannot be < 0, value provided: "
                        + svExtraPadding + ". Please provide a value >= 0");
            }
            serverQueryOptions.put("svExtraPadding", svExtraPadding);
        }

        // Parameter not expected to be very used - provide extra padding (bp) to be used for CNV annotation
        if (variantAnnotationCommandOptions.buildParams.get("cnv-extra-padding") != null) {
            Integer cnvExtraPadding = Integer.valueOf(variantAnnotationCommandOptions.buildParams.get("cnv-extra-padding"));
            if (cnvExtraPadding < 0) {
                throw new ParameterException("Extra padding for CNV annotation cannot be < 0, value provided: "
                        + cnvExtraPadding + ". Please provide a value >= 0");
            }
            serverQueryOptions.put("cnvExtraPadding", cnvExtraPadding);
        }

        // Annotate variation collection in CellBase
        cellBaseAnnotation = variantAnnotationCommandOptions.cellBaseAnnotation;

        // The list of chromosomes will only be used if annotating the variation collection
        if (cellBaseAnnotation) {
            // This will set chromosomeList with the list of chromosomes to annotate
            setChromosomeList();
        }

    }

    private void parsePhaseConfiguration() {
        // TODO: remove "phased" CLI parameter in next release. Default behavior from here onwards should be
        //  ignorePhase = false
        // If ignorePhase (new parameter) is present, then overrides presence of "phased"
        if (variantAnnotationCommandOptions.ignorePhase != null) {
            serverQueryOptions.put("ignorePhase", variantAnnotationCommandOptions.ignorePhase);
        // If the new parameter (ignorePhase) is not present but old one ("phased") is, then follow old one - probably
        // someone who has not moved to the new parameter yet
        } else if (variantAnnotationCommandOptions.phased != null) {
            serverQueryOptions.put("ignorePhase", !variantAnnotationCommandOptions.phased);
        // Default behavior is to perform phased annotation
        } else {
            serverQueryOptions.put("ignorePhase", false);
        }
    }

    private FileFormat getFileFormat(Path path) {
        String fileName = path.toFile().getName();
        if (fileName.endsWith(".vcf") || fileName.endsWith(".vcf.gz")) {
            return FileFormat.VCF;
        } else if (fileName.endsWith(".json") || fileName.endsWith(".json.gz")) {
            return FileFormat.JSON;
        } else {
            throw new ParameterException("Only VCF and JSON formats are currently accepted. Please provide a "
                    + "valid .vcf, .vcf.gz, json or .json.gz file");
        }
    }


}
