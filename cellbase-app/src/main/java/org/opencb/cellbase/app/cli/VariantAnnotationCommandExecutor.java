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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.opencb.biodata.formats.annotation.io.JsonAnnotationWriter;
import org.opencb.biodata.formats.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.io.VariantVcfReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.cellbase.core.variant.annotation.CellbaseWSVariantAnnotator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotatorRunner;
import org.opencb.cellbase.core.variant.annotation.VcfVariantAnnotator;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.run.ParallelTaskRunner;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by fjlopez on 18/03/15.
 */
public class VariantAnnotationCommandExecutor extends CommandExecutor {

    private CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions;

    private Path input;
    private Path output;
    private String url;
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

    private final int QUEUE_CAPACITY = 10;
    private final String TMP_DIR = "/tmp/";

    public VariantAnnotationCommandExecutor(CliOptionsParser.VariantAnnotationCommandOptions variantAnnotationCommandOptions) {
        super(variantAnnotationCommandOptions.commonOptions.logLevel, variantAnnotationCommandOptions.commonOptions.verbose,
                variantAnnotationCommandOptions.commonOptions.conf);

        this.variantAnnotationCommandOptions = variantAnnotationCommandOptions;

        if(variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
        }
        if(variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
        }
    }

    @Override
    public void execute() {
        checkParameters();

        try {
            if (customFiles != null) {
                createIndexes();
            }

            String path = "/cellbase/webservices/rest/";
            CellBaseClient cellBaseClient;
            if (url.contains(":")) {
                String[] hostAndPort = url.split(":");
                url = hostAndPort[0];
                port = Integer.parseInt(hostAndPort[1]);
                cellBaseClient = new CellBaseClient(url, port, path,
                        configuration.getVersion(), species);
            } else {
                cellBaseClient = new CellBaseClient(url, port, path,
                        configuration.getVersion(), species);
            }
            logger.debug("URL set to: {}", url+":"+port+path);

            List<ParallelTaskRunner.Task<Variant, VariantAnnotation>> variantAnnotatorRunnerList = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++) {
                List<VariantAnnotator> variantAnnotatorList = createAnnotators(cellBaseClient);
                variantAnnotatorRunnerList.add(new VariantAnnotatorRunner(variantAnnotatorList));
            }

            ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
            DataReader dataReader = new VariantVcfReader(new VariantSource(input.toString(), "", "", ""),
                    input.toString());

            DataWriter dataWriter;
            if (output.toString().endsWith(".json")) {
                dataWriter = new JsonAnnotationWriter(output.toString());
            } else {
                dataWriter = new VepFormatWriter(output.toString());
            }

            ParallelTaskRunner<Variant, VariantAnnotation> runner = null;
            try {
                runner = new ParallelTaskRunner<>(dataReader, variantAnnotatorRunnerList, dataWriter, config);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            runner.run();

            if (customFiles != null) {
                closeIndexes();
            }

            logger.info("Variant annotation finished.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeIndexes() {
        try {
            for (int i = 0; i < dbIndexes.size(); i++) {
                dbIndexes.get(i).close();
                dbOptions.get(i).dispose();
                FileUtils.deleteDirectory(new File(dbLocations.get(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<VariantAnnotator> createAnnotators(CellBaseClient cellBaseClient) {
        List<VariantAnnotator> variantAnnotatorList;
        variantAnnotatorList = new ArrayList<>();

        // CellBase annotator is always called
        variantAnnotatorList.add(new CellbaseWSVariantAnnotator(cellBaseClient));

        // Include custom annotators if required
        if(customFiles!=null) {
            for (int i = 0; i < customFiles.size(); i++) {
                if (customFiles.get(i).toString().endsWith(".vcf") || customFiles.get(i).toString().endsWith(".vcf.gz")) {
                    variantAnnotatorList.add(new VcfVariantAnnotator(customFiles.get(i).toString(), dbIndexes.get(i),
                            customFileIds.get(i), customFileFields.get(i)));
                }
            }
        }

        return variantAnnotatorList;
    }

    private void createIndexes() {
        dbIndexes = new ArrayList<>(customFiles.size());
        dbOptions = new ArrayList<>(customFiles.size());
        dbLocations = new ArrayList<>(customFiles.size());
        for(int i=0; i<customFiles.size(); i++) {
            if(customFiles.get(i).toString().endsWith(".vcf") || customFiles.get(i).toString().endsWith(".vcf.gz")) {
                Object[] dbConnection = indexCustomVcfFile(i);
                dbIndexes.add((RocksDB) dbConnection[0]);
                dbOptions.add((Options) dbConnection[1]);
                dbLocations.add((String) dbConnection[2]);
            }
        }
    }

    private Object[] indexCustomVcfFile(int customFileNumber) {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        ObjectWriter jsonObjectWriter = jsonObjectMapper.writer();

        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options().setCreateIfMissing(true);
        RocksDB db = null;
        String dbLocation = null;
        try {
            dbLocation = TMP_DIR+System.currentTimeMillis();
            logger.info("Creating index DB at {} ", dbLocation);
            // a factory method that returns a RocksDB instance
            db = RocksDB.open(options, dbLocation);
            // do something
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Indexing {} ", customFiles.get(customFileNumber));
        BufferedReader reader;
        try {
            if (customFiles.get(customFileNumber).toFile().getName().endsWith(".gz")) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(customFiles.get(customFileNumber).toFile()))));
            } else {
                reader = Files.newBufferedReader(customFiles.get(customFileNumber), Charset.defaultCharset());
            }
            String line;
            int lineCounter = 0;
            while((line = reader.readLine())!=null && (line.trim().equals("") || line.startsWith("#"))) {
                lineCounter++;
            }
            while(line!=null) {
                String[] fields = line.split("\t");
                // Reference positions will not be indexed
                if(!fields[4].equals(".")) {
                    String[] alternates = line.split("\t")[4].split(",");
                    List<Map<String,Object>> parsedInfo = parseInfoAttributes(fields[7], alternates.length, customFileNumber);
                    for (int i=0; i<alternates.length; i++) {
                        db.put((fields[0] + "_" + fields[1] + "_" + fields[3] + "_" + alternates[i]).getBytes(),
                                jsonObjectWriter.writeValueAsBytes(parsedInfo.get(i)));
                    }
                }
                line = reader.readLine();
                lineCounter++;
                if(lineCounter%100000 == 0) {
                    logger.info("{} lines indexed", lineCounter);
                }
            }
            reader.close();
        } catch (IOException | RocksDBException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return new Object[] {db,options,dbLocation};
    }

    protected List<Map<String, Object>> parseInfoAttributes(String info, int numAlleles, int customFileNumber) {
        List<Map<String, Object>> infoAttributes = new ArrayList<>(numAlleles);
        for(int i=0; i<numAlleles; i++) {
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
                if(values.length==numAlleles) {
                    for (int i = 0; i < numAlleles; i++) {
                        infoAttributes.get(i).put(splits[0], getValueFromString(values[i]));
                    }
                } else {
                    for (int i=0; i<numAlleles; i++) {
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

    private void checkParameters() {
        // input file
        if (variantAnnotationCommandOptions.input != null) {
            input = Paths.get(variantAnnotationCommandOptions.input);
            fileExists(input);
        } else {
            throw new ParameterException("Please check command line syntax. Provide a valid input file name.");
        }
        // output file
        if (variantAnnotationCommandOptions.output != null) {
            output = Paths.get(variantAnnotationCommandOptions.output);
            Path outputDir = output.getParent();
            if (!outputDir.toFile().exists()) {
                throw new ParameterException("Output directory " + outputDir + " doesn't exist");
            } else if (output.toFile().isDirectory()) {
                throw new ParameterException("Output file cannot be a directory: " + output);
            }
        } else {
            throw new ParameterException("Please check command line sintax. Provide a valid output file name.");
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
        // Url
        if (variantAnnotationCommandOptions.url!=null) {
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
        // Species
        if (variantAnnotationCommandOptions.species!=null) {
            species = variantAnnotationCommandOptions.species;
        } else {
            throw new ParameterException("Please check command line sintax. Provide a valid species name to access CellBase web services.");
        }
        // Custom files
        if(variantAnnotationCommandOptions.customFiles != null) {
            String[] customFileStrings = variantAnnotationCommandOptions.customFiles.split(",");
            customFiles = new ArrayList<>(customFileStrings.length);
            for(String customFile : customFileStrings) {
                Path customFilePath = Paths.get(customFile);
                fileExists(customFilePath);
                if(!(customFilePath.toString().endsWith(".vcf") || customFilePath.toString().endsWith(".vcf.gz"))) {
                    throw new ParameterException("Only VCF format is currently accepted for custom annotation files.");
                }
                customFiles.add(customFilePath);
            }
            if(variantAnnotationCommandOptions.customFileIds == null) {
                throw new ParameterException("Parameter --custom-file-ids missing. Please, provide one short id for each custom file in a comma separated list (no spaces in betwen).");
            }
            customFileIds = Arrays.asList(variantAnnotationCommandOptions.customFileIds.split(","));
            if(customFileIds.size()!=customFiles.size()) {
                throw new ParameterException("Different number of custom files and custom file ids. Please, provide one short id for each custom file in a comma separated list (no spaces in betwen).");
            }
            if(variantAnnotationCommandOptions.customFileFields == null) {
                throw new ParameterException("Parameter --custom-file-fields missing. Please, provide one list of fields for each custom file in a colon separated list (no spaces in betwen).");
            }
            String[] customFileFieldStrings = variantAnnotationCommandOptions.customFileFields.split(":");
            if(customFileFieldStrings.length!=customFiles.size()) {
                throw new ParameterException("Different number of custom files and lists of custom file fields. Please, provide one list of fields for each custom file in a colon separated list (no spaces in betwen).");
            }
            customFileFields = new ArrayList<>(customFileStrings.length);
            for(String fieldString : customFileFieldStrings) {
                customFileFields.add(Arrays.asList(fieldString.split(",")));
            }
        }
    }

    private void fileExists(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new ParameterException("File " + filePath + " doesn't exist");
        } else if (Files.isDirectory(filePath)) {
            throw new ParameterException("File cannot be a directory: " + filePath);
        }
    }

}
