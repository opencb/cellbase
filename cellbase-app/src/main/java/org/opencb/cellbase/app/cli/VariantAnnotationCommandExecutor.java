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
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.apache.commons.lang.math.NumberUtils;
import org.opencb.biodata.formats.variant.annotation.io.JsonAnnotationWriter;
import org.opencb.biodata.formats.variant.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.FullVcfCodec;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.exceptions.NonStandardCompliantSampleField;
import org.opencb.biodata.tools.variant.converter.VariantContextToVariantConverter;
import org.opencb.cellbase.core.api.DBAdaptorFactory;
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
    private int port;
    private String species;
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
            if (customFiles != null) {
                getIndexes();
            }

            if (variantAnnotationCommandOptions.variant != null && !variantAnnotationCommandOptions.variant.isEmpty()) {
                List<Variant> variants = Variant.parseVariants(variantAnnotationCommandOptions.variant);
                if (local) {
                    DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
                    VariantAnnotationCalculator variantAnnotationCalculator =
                            new VariantAnnotationCalculator(this.species, variantAnnotationCommandOptions.assembly, dbAdaptorFactory);
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
                return;
            }

            // If a variant file is provided then we annotate it. Lines in the input file can be computationally
            // expensive to parse, i.e.: multisample vcf with thousands of samples. A specific task is created to enable
            // parallel parsing of these lines
            if (input != null) {
                DataReader dataReader = new StringDataReader(input);
                List<ParallelTaskRunner.Task<String, Variant>> variantAnnotatorTaskList = getStringTaskList();
                DataWriter dataWriter = getDataWriter();

                ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
                ParallelTaskRunner<String, Variant> runner =
                        new ParallelTaskRunner<String, Variant>(dataReader, variantAnnotatorTaskList, dataWriter, config);
                runner.run();
            } else {
                // This will annotate the CellBase Variation collection
                if (cellBaseAnnotation) {
                    dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
                    // TODO: enable this query in the parseQuery method within VariantMongoDBAdaptor
//                    Query query = new Query("$match",
//                            new Document("annotation.consequenceTypes", new Document("$exists", 0)));
                    Query query = new Query();
                    QueryOptions options = new QueryOptions("include", "chromosome,start,reference,alternate,type");
                    DataReader dataReader =
                            new VariationDataReader(dbAdaptorFactory.getVariationDBAdaptor(species), query, options);
                    List<ParallelTaskRunner.Task<Variant, Variant>> variantAnnotatorTaskList = getVariantTaskList();
                    DataWriter dataWriter = getDataWriter();

                    ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
                    ParallelTaskRunner<Variant, Variant> runner =
                            new ParallelTaskRunner<Variant, Variant>(dataReader, variantAnnotatorTaskList, dataWriter, config);
                    runner.run();
                }
            }

            if (customFiles != null) {
                closeIndexes();
            }

            logger.info("Variant annotation finished.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataWriter getDataWriter() {
        DataWriter dataWriter = null;
        if (outputFormat.equals(FileFormat.JSON)) {
            dataWriter = new JsonAnnotationWriter(output.toString());
        } else if (outputFormat.equals(FileFormat.VEP)) {
            dataWriter = new VepFormatWriter(output.toString());
        }
        return dataWriter;
    }

    private List<ParallelTaskRunner.Task<String, Variant>> getStringTaskList() throws IOException {
        List<ParallelTaskRunner.Task<String, Variant>> variantAnnotatorTaskList = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            List<VariantAnnotator> variantAnnotatorList = createAnnotators();
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

    private List<ParallelTaskRunner.Task<Variant, Variant>> getVariantTaskList() throws IOException {
        List<ParallelTaskRunner.Task<Variant, Variant>> variantAnnotatorTaskList = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            List<VariantAnnotator> variantAnnotatorList = createAnnotators();
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
        List<VariantAnnotator> variantAnnotatorList;
        variantAnnotatorList = new ArrayList<>();

        // CellBase annotator is always called
        variantAnnotatorList.add(createCellBaseAnnotator());

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

    private VariantAnnotator createCellBaseAnnotator() {
        // Assume annotation of CellBase variation collection will always be carried out from a local installation
        if (local || cellBaseAnnotation) {
            // dbAdaptorFactory may have been already initialized at execute if annotating CellBase variation collection
            if (dbAdaptorFactory == null) {
                dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
            }
//            return new CellBaseLocalVariantAnnotator(dbAdaptorFactory.getVariantAnnotationDBAdaptor(species, null), queryOptions);
            return new CellBaseLocalVariantAnnotator(new VariantAnnotationCalculator(species, null, dbAdaptorFactory), queryOptions);
        } else {
            try {
                String path = "/cellbase/webservices/rest/";
                CellBaseClient cellBaseClient;
                if (url.contains(":")) {
                    String[] hostAndPort = url.split(":");
                    url = hostAndPort[0];
                    port = Integer.parseInt(hostAndPort[1]);
                    cellBaseClient = new CellBaseClient(url, port, path, configuration.getVersion(), species);
                } else {
                    cellBaseClient = new CellBaseClient(url, port, path, configuration.getVersion(), species);
                }
                logger.debug("URL set to: {}", url + ":" + port + path);

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

        try (InputStream fileInputStream = customFiles.get(customFileNumber).toString().endsWith("gz")
                    ? new GZIPInputStream(new FileInputStream(customFiles.get(customFileNumber).toFile()))
                    : new FileInputStream(customFiles.get(customFileNumber).toFile())) {
            FullVcfCodec codec = new FullVcfCodec();
            LineIterator lineIterator = codec.makeSourceFromStream(fileInputStream);
            VCFHeader header = (VCFHeader) codec.readActualHeader(lineIterator);
//            VCFHeaderVersion headerVersion = codec.getVCFHeaderVersion();
//            FullVcfCodec codec = new FullVcfCodec();
//            codec.setVCFHeader(header, headerVersion);
//
//            codec.setVCFHeader(header, headerVersion);
            VariantContextToVariantConverter converter = new VariantContextToVariantConverter("", "", header.getSampleNamesInOrder());
            VariantNormalizer normalizer = new VariantNormalizer(true);
            BufferedReader reader = FileUtils.newBufferedReader(customFiles.get(customFileNumber));
            String line;
            int lineCounter = 0;
            while ((line = reader.readLine()) != null) {   // && (line.trim().equals("") || line.startsWith("#"))
                if (line.trim().equals("") || line.startsWith("#")) {
                    lineCounter++;
                } else {
                    break;
                }
            }
            while (line != null) {
                String[] fields = line.split("\t");
                // Reference positions will not be indexed
                if (!fields[4].equals(".")) {
                    String[] alternates = line.split("\t")[4].split(",");
//                    List<Map<String, Object>> parsedInfo = parseInfoAttributes(fields[7], alternates.length, customFileNumber);
                    List<Variant> variantList = normalizer.normalize(converter.apply(Collections.singletonList(codec.decode(line))), true);
//                    for (int i = 0; i < alternates.length; i++) {
                    for (Variant variant : variantList) {
                        db.put((variant.getChromosome() + "_" + variant.getStart() + "_" + variant.getReference() + "_"
                                        + variant.getAlternate()).getBytes(),
                                jsonObjectWriter.writeValueAsBytes(parseInfoAttributes(variant, customFileNumber)));

                        // INDEL
//                        if (fields[3].length() > 1 || alternates[i].length() > 1) {
//                            db.put((fields[0] + "_" + (Integer.valueOf(fields[1]) + 1) + "_" + fields[3].substring(1) + "_"
//                                    + alternates[i].substring(1)).getBytes(),
//                                    jsonObjectWriter.writeValueAsBytes(parsedInfo.get(i)));
//                            // SNV
//                        } else {
//                            db.put((fields[0] + "_" + fields[1] + "_" + fields[3] + "_" + alternates[i]).getBytes(),
//                                    jsonObjectWriter.writeValueAsBytes(parsedInfo.get(i)));
//                        }
                    }
                }
                line = reader.readLine();
                lineCounter++;
                if (lineCounter % 100000 == 0) {
                    logger.info("{} lines indexed", lineCounter);
                }
            }
            reader.close();
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
        // input file
        if (variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
            FileUtils.checkFile(input);
            String fileName = input.toFile().getName();
            if (!(fileName.endsWith(".vcf") || fileName.endsWith(".vcf.gz"))) {
                throw new ParameterException("Only VCF format is currently accepted. Please provide a valid .vcf or .vcf.gz file");
            } else {
                inputFormat = FileFormat.VCF;
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
            // port
            if (variantAnnotationCommandOptions.port > 0) {
                port = variantAnnotationCommandOptions.port;
            } else {
                throw new ParameterException("Please check command line sintax. Provide a valid port to access CellBase web services.");
            }
        }

        // Annotate variation collection in CellBase
        cellBaseAnnotation = variantAnnotationCommandOptions.cellBaseAnnotation;

        // Species
        if (variantAnnotationCommandOptions.species != null) {
            species = variantAnnotationCommandOptions.species;
        } else {
            throw new ParameterException("Please check command line syntax. Provide a valid species name to access CellBase web services.");
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
    }
}
