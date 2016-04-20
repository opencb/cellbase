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
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.opencb.biodata.formats.variant.annotation.io.JsonAnnotationWriter;
import org.opencb.biodata.formats.variant.annotation.io.VepFormatReader;
import org.opencb.biodata.formats.variant.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.FullVcfCodec;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.exceptions.NonStandardCompliantSampleField;
import org.opencb.biodata.tools.variant.converter.VariantContextToVariantConverter;
import org.opencb.cellbase.app.cli.variant.annotation.BenchmarkDataWriter;
import org.opencb.cellbase.app.cli.variant.annotation.BenchmarkTask;
import org.opencb.cellbase.app.cli.variant.annotation.VariantAnnotationDiff;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.cellbase.core.variant.annotation.*;
import org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.io.StringDataReader;
import org.opencb.commons.run.ParallelTaskRunner;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by fjlopez on 18/03/15.
 */
public class VariantAnnotationCommandExecutor extends CommandExecutor {

    public enum FileFormat {VCF, JSON, VEP};

    private CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions;

    private Path input;
    private Path output;
    private String url;
    private boolean local;
    private boolean cellBaseAnnotation;
    private boolean benchmark;
    private List<String> chromosomeList;
    private int port;
    private String species;
    private String assembly;
    private int numThreads;
    private int batchSize;
    private List<Path> customFiles;
    private List<RocksDB> dbIndexes;
    private List<Options> dbOptions;
    private List<String> dbLocations;
    private List<String> customFileIds;
    private List<List<String>> customFileFields;
    private FileFormat inputFormat;
    private FileFormat outputFormat;

    private QueryOptions queryOptions;

    private DBAdaptorFactory dbAdaptorFactory = null;

    private final int QUEUE_CAPACITY = 10;
    private final String TMP_DIR = "/tmp/";
    private static final String VARIATION_ANNOTATION_FILE_PREFIX = "variation_annotation_";

    public VariantAnnotationCommandExecutor(CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions) {
        super(variantAnnotationCommandOptions.commonOptions.logLevel, variantAnnotationCommandOptions.commonOptions.verbose,
                variantAnnotationCommandOptions.commonOptions.conf);

        this.variantAnnotationCommandOptions = variantAnnotationCommandOptions;
        this.queryOptions = new QueryOptions();

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
        }
    }

    private void runBenchmark() {
        try {

            DirectoryStream<Path> stream = Files.newDirectoryStream(input, entry -> {
                return entry.getFileName().toString().endsWith(".vep");
            });

            DataWriter dataWriter = getDataWriter(output.toString());
            ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
            List<ParallelTaskRunner.Task<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>>>
                    variantAnnotatorTaskList = getBenchmarkTaskList();
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

    private List<ParallelTaskRunner.Task<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>>> getBenchmarkTaskList()
            throws IOException {
        List<ParallelTaskRunner.Task<VariantAnnotation, Pair<VariantAnnotationDiff, VariantAnnotationDiff>>> benchmarkTaskList
                = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            // Benchmark variants are read from a VEP file, must not normalize
            benchmarkTaskList.add(new BenchmarkTask(createCellBaseAnnotator(false)));
        }
        return benchmarkTaskList;
    }

    private boolean runAnnotation() throws Exception {
        if (customFiles != null) {
            getIndexes();
        }

        if (variantAnnotationCommandOptions.variant != null && !variantAnnotationCommandOptions.variant.isEmpty()) {
            List<Variant> variants = Variant.parseVariants(variantAnnotationCommandOptions.variant);
            if (local) {
                DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
                VariantAnnotationCalculator variantAnnotationCalculator =
                        new VariantAnnotationCalculator(this.species, this.assembly, dbAdaptorFactory);
                List<QueryResult<VariantAnnotation>> annotationByVariantList =
                        variantAnnotationCalculator.getAnnotationByVariantList(variants, queryOptions);

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
            DataReader dataReader = new StringDataReader(input);
            List<ParallelTaskRunner.Task<String, Variant>> variantAnnotatorTaskList = getStringTaskList(false);
            DataWriter dataWriter = getDataWriter(output.toString());

            ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
            ParallelTaskRunner<String, Variant> runner =
                    new ParallelTaskRunner<>(dataReader, variantAnnotatorTaskList, dataWriter, config);
            runner.run();
        } else {
            // This will annotate the CellBase Variation collection
            if (cellBaseAnnotation) {
                // TODO: enable this query in the parseQuery method within VariantMongoDBAdaptor
//                    Query query = new Query("$match",
//                            new Document("annotation.consequenceTypes", new Document("$exists", 0)));
//                    Query query = new Query();
                QueryOptions options = new QueryOptions("include", "chromosome,start,reference,alternate,type");
                List<ParallelTaskRunner.Task<Variant, Variant>> variantAnnotatorTaskList = getVariantTaskList(false);
                ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);

                for (String chromosome : chromosomeList) {
                    logger.info("Annotating chromosome {}", chromosome);
                    Query query = new Query("chromosome", chromosome);
                    DataReader dataReader =
                            new VariationDataReader(dbAdaptorFactory.getVariationDBAdaptor(species), query, options);
                    DataWriter dataWriter = getDataWriter(output.toString() + "/"
                            + VARIATION_ANNOTATION_FILE_PREFIX + chromosome + ".json.gz");
                    ParallelTaskRunner<Variant, Variant> runner =
                            new ParallelTaskRunner<Variant, Variant>(dataReader, variantAnnotatorTaskList, dataWriter, config);
                    runner.run();
                }
            }
        }

        if (customFiles != null) {
            closeIndexes();
        }

        logger.info("Variant annotation finished.");
        return false;
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

    private DataWriter getDataWriter(String filename) {
        DataWriter dataWriter = null;
        if (benchmark) {
            dataWriter = new BenchmarkDataWriter("VEP", "CellBase", output);
        } else {
            if (outputFormat.equals(FileFormat.JSON)) {
                dataWriter = new JsonAnnotationWriter(filename);
            } else if (outputFormat.equals(FileFormat.VEP)) {
                dataWriter = new VepFormatWriter(filename);
            }
        }
        return dataWriter;
    }

    private List<ParallelTaskRunner.Task<String, Variant>> getStringTaskList(boolean normalize) throws IOException {
        List<ParallelTaskRunner.Task<String, Variant>> variantAnnotatorTaskList = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            List<VariantAnnotator> variantAnnotatorList = createAnnotators(normalize);
            switch (inputFormat) {
                case VCF:
                    logger.info("Using HTSJDK to read variants.");
                    FullVcfCodec codec = new FullVcfCodec();
                    try (InputStream fileInputStream = input.toString().endsWith("gz")
                            ? new GZIPInputStream(new FileInputStream(input.toFile()))
                            : new FileInputStream(input.toFile())) {
                        LineIterator lineIterator = codec.makeSourceFromStream(fileInputStream);
                        VCFHeader header = (VCFHeader) codec.readActualHeader(lineIterator);
                        VCFHeaderVersion headerVersion = codec.getVCFHeaderVersion();
                        variantAnnotatorTaskList.add(new VcfStringAnnotatorTask(header, headerVersion, variantAnnotatorList));
                    } catch (IOException e) {
                        throw new IOException("Unable to read VCFHeader");
                    }
                    break;
                default:
                    break;
            }
        }
        return variantAnnotatorTaskList;
    }

    private List<ParallelTaskRunner.Task<Variant, Variant>> getVariantTaskList(boolean normalize) throws IOException {
        List<ParallelTaskRunner.Task<Variant, Variant>> variantAnnotatorTaskList = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            List<VariantAnnotator> variantAnnotatorList = createAnnotators(normalize);
            variantAnnotatorTaskList.add(new VariantAnnotatorTask(variantAnnotatorList));
        }

        return variantAnnotatorTaskList;
    }

    private void closeIndexes() {
        for (int i = 0; i < dbIndexes.size(); i++) {
            dbIndexes.get(i).close();
            dbOptions.get(i).dispose();
        }
    }

    private List<VariantAnnotator> createAnnotators() {
        return this.createAnnotators(true);
    }

    private List<VariantAnnotator> createAnnotators(boolean normalize) {
        List<VariantAnnotator> variantAnnotatorList;
        variantAnnotatorList = new ArrayList<>();

        // CellBase annotator is always called
        variantAnnotatorList.add(createCellBaseAnnotator(normalize));

        // Include custom annotators if required
        if (customFiles != null) {
            for (int i = 0; i < customFiles.size(); i++) {
                if (customFiles.get(i).toString().endsWith(".vcf") || customFiles.get(i).toString().endsWith(".vcf.gz")) {
                    variantAnnotatorList.add(new VcfVariantAnnotator(customFiles.get(i).toString(), dbIndexes.get(i),
                            customFileIds.get(i), customFileFields.get(i)));
                }
            }
        }

        return variantAnnotatorList;
    }

    private VariantAnnotator createCellBaseAnnotator(boolean normalize) {
        // Assume annotation of CellBase variation collection will always be carried out from a local installation
        if (local || cellBaseAnnotation) {
            // dbAdaptorFactory may have been already initialized at execute if annotating CellBase variation collection
            if (dbAdaptorFactory == null) {
                dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
            }
//            return new CellBaseLocalVariantAnnotator(dbAdaptorFactory.getVariantAnnotationDBAdaptor(species, null), queryOptions);
            return new CellBaseLocalVariantAnnotator(new VariantAnnotationCalculator(species, assembly,
                    dbAdaptorFactory, normalize), queryOptions);
        } else {
            try {
                CellBaseClient cellBaseClient;
                cellBaseClient = new CellBaseClient(url, configuration.getVersion(), species);
                logger.debug("URL set to: {}", url + ":" + port);

                // TODO: enable normalize flag for the WS annotator
                return new CellBaseWSVariantAnnotator(cellBaseClient, queryOptions);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return null;

    }

    private void getIndexes() {
        dbIndexes = new ArrayList<>(customFiles.size());
        dbOptions = new ArrayList<>(customFiles.size());
        dbLocations = new ArrayList<>(customFiles.size());
        for (int i = 0; i < customFiles.size(); i++) {
            if (customFiles.get(i).toString().endsWith(".vcf") || customFiles.get(i).toString().endsWith(".vcf.gz")) {
                Object[] dbConnection = getDBConnection(customFiles.get(i).toString() + ".idx");
                RocksDB rocksDB = (RocksDB) dbConnection[0];
                Options dbOption = (Options) dbConnection[1];
                String dbLocation = (String) dbConnection[2];
                boolean indexingNeeded = (boolean) dbConnection[3];
                if (indexingNeeded) {
                    logger.info("Creating index DB at {} ", dbLocation);
                    indexCustomVcfFile(i, rocksDB);
                } else {
                    logger.info("Index found at {}", dbLocation);
                    logger.info("Skipping index creation");
                }
                dbIndexes.add(rocksDB);
                dbOptions.add(dbOption);
                dbLocations.add(dbLocation);
            }
        }
    }

    private Object[] getDBConnection(String dbLocation) {
        boolean indexingNeeded = !Files.exists(Paths.get(dbLocation));
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options().setCreateIfMissing(true);
        RocksDB db = null;
        try {
            // a factory method that returns a RocksDB instance
            if (indexingNeeded) {
                db = RocksDB.open(options, dbLocation);
            } else {
                db = RocksDB.openReadOnly(options, dbLocation);
            }
            // do something
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
            System.exit(1);
        }

        return new Object[]{db, options, dbLocation, indexingNeeded};

    }

    private void indexCustomVcfFile(int customFileNumber, RocksDB db) {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        ObjectWriter jsonObjectWriter = jsonObjectMapper.writer();

        try {
            VCFFileReader vcfFileReader = new VCFFileReader(customFiles.get(customFileNumber).toFile(), false);
            Iterator<VariantContext> iterator = vcfFileReader.iterator();
            VariantContextToVariantConverter converter = new VariantContextToVariantConverter("", "",
                    vcfFileReader.getFileHeader().getSampleNamesInOrder());
            VariantNormalizer normalizer = new VariantNormalizer(true, false, true);
            int lineCounter = 0;
            while (iterator.hasNext()) {
                VariantContext variantContext = iterator.next();
                // Reference positions will not be indexed
                if (variantContext.getAlternateAlleles().size() > 0) {
                    List<Variant> variantList = normalizer.normalize(converter.apply(Collections.singletonList(variantContext)), true);
                    for (Variant variant : variantList) {
                        db.put((variant.getChromosome() + "_" + variant.getStart() + "_" + variant.getReference() + "_"
                                        + variant.getAlternate()).getBytes(),
                                jsonObjectWriter.writeValueAsBytes(parseInfoAttributes(variant, customFileNumber)));
                    }
                }
                lineCounter++;
                if (lineCounter % 100000 == 0) {
                    logger.info("{} lines indexed", lineCounter);
                }
            }
            vcfFileReader.close();
        } catch (IOException | RocksDBException | NonStandardCompliantSampleField e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected Map<String, Object> parseInfoAttributes(Variant variant, int customFileNumber) {
        Map<String, String> infoMap = variant.getStudies().get(0).getFiles().get(0).getAttributes();
        Map<String, Object> parsedInfo = new HashMap<>();
        for (String attribute : infoMap.keySet()) {
            if (customFileFields.get(customFileNumber).contains(attribute)) {
                parsedInfo.put(attribute, getValueFromString(infoMap.get(attribute)));
            }
        }

        return parsedInfo;
    }

    @Deprecated
    protected List<Map<String, Object>> parseInfoAttributes(String info, int numAlleles, int customFileNumber) {
        List<Map<String, Object>> infoAttributes = new ArrayList<>(numAlleles);
        for (int i = 0; i < numAlleles; i++) {
            infoAttributes.add(new HashMap<>());
        }
        for (String var : info.split(";")) {
            String[] splits = var.split("=");
            if (splits.length == 2 && customFileFields.get(customFileNumber).contains(splits[0])) {
                // Managing values for the allele
                String[] values = splits[1].split(",");
                // numAlleles and values.length may be different. For example, in the Exac vcf AN presents just one
                // value even if there are multiple alleles or, for example, for the AC_Het provide counts for all posible
                // heterozigous genotypes. In those cases, the hole string is pasted to all alleles
                if (values.length == numAlleles) {
                    for (int i = 0; i < numAlleles; i++) {
                        infoAttributes.get(i).put(splits[0], getValueFromString(values[i]));
                    }
                } else {
                    for (int i = 0; i < numAlleles; i++) {
                        infoAttributes.get(i).put(splits[0], getValueFromString(splits[1]));
                    }
                }
            }
        }

        return infoAttributes;
    }

    private Object getValueFromString(String value) {
        if (NumberUtils.isNumber(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                try {
                    return Float.parseFloat(value);
                } catch (NumberFormatException e1) {
                    return Double.parseDouble(value);
                }
            }
        } else {
            return value;
        }
    }

    private void checkParameters() throws IOException {

        // Run benchmark
        benchmark = variantAnnotationCommandOptions.benchmark;

        // input file
        if (variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
            if (benchmark) {
                FileUtils.checkDirectory(input);
            } else {
                FileUtils.checkFile(input);
                String fileName = input.toFile().getName();
                if (!(fileName.endsWith(".vcf") || fileName.endsWith(".vcf.gz"))) {
                    throw new ParameterException("Only VCF format is currently accepted. Please provide a valid .vcf or .vcf.gz file");
                } else {
                    inputFormat = FileFormat.VCF;
                }
            }
        }
        // output file
        if (variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
//            Path outputDir = output.getParent();
            try {
                FileUtils.checkDirectory(output.getParent());
            } catch (IOException e) {
                throw new ParameterException(e);
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
                case "vep":
                    outputFormat = FileFormat.VEP;
                    break;
                default:
                    throw  new ParameterException("Only JSON and VEP output formats are currently available. Please, select one of them.");
            }

        }

        if (variantAnnotationCommandOptions.include != null && !variantAnnotationCommandOptions.include.isEmpty()) {
            queryOptions.add("include", variantAnnotationCommandOptions.include);
        }

        if (variantAnnotationCommandOptions.exclude != null && !variantAnnotationCommandOptions.exclude.isEmpty()) {
            queryOptions.add("exclude", variantAnnotationCommandOptions.exclude);
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
                        + "Please, provide one list of fields for each custom file in a colon separated list (no spaces in betwen).");
            }
            customFileFields = new ArrayList<>(customFileStrings.length);
            for (String fieldString : customFileFieldStrings) {
                customFileFields.add(Arrays.asList(fieldString.split(",")));
            }
        }

        // Annotate variation collection in CellBase
        cellBaseAnnotation = variantAnnotationCommandOptions.cellBaseAnnotation;

        // The list of chromosomes will only be used if annotating the variation collection
        if (cellBaseAnnotation) {
            // This will set chromosomeList with the list of chromosomes to annotate
            setChromosomeList();
        }

    }


}
