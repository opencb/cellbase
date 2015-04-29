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
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        createIndexes();
        List<ParallelTaskRunner.Task<Variant,VariantAnnotation>> variantAnnotatorRunnerList = new ArrayList<>(numThreads);
        for(int i = 0; i<numThreads; i++) {
            List<VariantAnnotator> variantAnnotatorList = createAnnotators();
            variantAnnotatorRunnerList.add(new VariantAnnotatorRunner(variantAnnotatorList));
        }

        ParallelTaskRunner.Config config = new ParallelTaskRunner.Config(numThreads, batchSize, QUEUE_CAPACITY, false);
        DataReader dataReader = new VariantVcfReader(new VariantSource(input.toString(), "", "", ""),
                input.toString());

        DataWriter dataWriter;
        if(output.toString().endsWith(".json")) {
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

        closeIndexes();

        logger.info("Variant annotation finished.");
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

    private List<VariantAnnotator> createAnnotators() {
        List<VariantAnnotator> variantAnnotatorList = new ArrayList<>(customFiles.size()+1);  // +1 because CellbaseAnnotator will always be included

        try {
            String path = "/cellbase/webservices/rest/";
            CellBaseClient cellBaseClient = new CellBaseClient(url, port, path,
                    configuration.getVersion(), species);
            variantAnnotatorList.add(new CellbaseWSVariantAnnotator(cellBaseClient));
            for (int i = 0; i < customFiles.size(); i++) {
                if (customFiles.get(i).toString().endsWith(".vcf")) {
                    variantAnnotatorList.add(new VcfVariantAnnotator(customFiles.get(i).toString(), dbIndexes.get(i),
                            customFileIds.get(i), customFileFields.get(i)));
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return variantAnnotatorList;
    }

    private void createIndexes() {
        dbIndexes = new ArrayList<>(customFiles.size());
        dbOptions = new ArrayList<>(customFiles.size());
        dbLocations = new ArrayList<>(customFiles.size());
        for(Path customFile : customFiles) {
            if(customFile.toString().endsWith(".vcf")) {
                Object[] dbConnection = indexCustomVcfFile(customFile);
                dbIndexes.add((RocksDB) dbConnection[0]);
                dbOptions.add((Options) dbConnection[1]);
                dbLocations.add((String) dbConnection[2]);
            }
        }
    }

    private Object[] indexCustomVcfFile(Path filePath) {
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

        logger.info("Indexing {} ", filePath);
        RandomAccessFile reader;
        try {
            reader = new RandomAccessFile(filePath.toString(), "r");
            String line;
            int lineCounter = 0;
            long filePosition = 0;
            while((line = reader.readLine())!=null && (line.trim().equals("") || line.startsWith("#"))) {
                filePosition = reader.getFilePointer();
                lineCounter++;
            }
            while(line!=null) {
                String[] fields = line.split("\t");
                // Reference positions will not be indexed
                if(!fields[4].equals(".")) {
                    for (String alt : line.split("\t")[4].split(",")) {
                        db.put((fields[0] + "_" + fields[1] + "_" + fields[3] + "_" + alt).getBytes(), ByteBuffer.allocate(8).putLong(filePosition).array());
                    }
                }
                filePosition = reader.getFilePointer();
                line = reader.readLine();
                lineCounter++;
                if(lineCounter%10000 == 0) {
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
            logger.warn("Incorrect number of numThreads, it must be a positive value. This has been set to '{}'", numThreads);
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
        if (variantAnnotationCommandOptions.port>0) {
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
                if(!customFilePath.toString().endsWith(".vcf")) {
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
