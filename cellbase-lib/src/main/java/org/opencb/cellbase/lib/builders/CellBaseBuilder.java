/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencb.cellbase.lib.EtlCommons.*;

/**
 * Created by imedina on 30/08/14.
 */
public abstract class CellBaseBuilder {

    protected CellBaseSerializer serializer;
    protected ObjectReader dataSourceReader = new ObjectMapper().readerFor(DataSource.class);

    protected boolean checked;

    protected Logger logger;

    public static final String CHECKING_BEFORE_BUILDING_LOG_MESSAGE = "Checking files before building {} ...";
    public static final String CHECKING_DONE_BEFORE_BUILDING_LOG_MESSAGE = "Checking {} done!";

    public static final String BUILDING_LOG_MESSAGE = "Building {} ...";
    public static final String BUILDING_DONE_LOG_MESSAGE = "Building done.";

    public static final String CATEGORY_BUILDING_LOG_MESSAGE = "Building {}/{} ...";
    public static final String CATEGORY_BUILDING_DONE_LOG_MESSAGE = "Building done.";

    public static final String PARSING_LOG_MESSAGE = "Parsing {} ...";
    public static final String PARSING_DONE_LOG_MESSAGE = "Parsing done.";

    public CellBaseBuilder(CellBaseSerializer serializer) {
        logger = LoggerFactory.getLogger(this.getClass());

        this.serializer = serializer;
        this.checked = false;
    }

    public abstract void parse() throws Exception;

    public void disconnect() {
        if (serializer != null) {
            try {
                serializer.close();
            } catch (Exception e) {
                logger.error("Error closing serializer:\n" + StringUtils.join(e.getStackTrace(), "\n"));
            }
        }
    }

    protected File checkFile(DownloadProperties.URLProperties props, String fileId, Path targetPath, String name) throws CellBaseException {
        logger.info("Checking file {} (file ID {} in config.) ...", name, fileId);
        String filename = Paths.get(props.getFiles().get(fileId)).getFileName().toString();
        if (filename.contains(MANUAL_PREFIX)) {
            filename = filename.replace(MANUAL_PREFIX, "");
        }
        Path filePath = targetPath.resolve(filename);
        if (!Files.exists(filePath)) {
            if (filename.contains(PUT_CAPITAL_SPECIES_HERE_MARK)) {
                // Check
                filename = filename.replace(PUT_CAPITAL_SPECIES_HERE_MARK + "." + PUT_ASSEMBLY_HERE_MARK + "." + PUT_RELEASE_HERE_MARK, "")
                        .replace(PUT_CAPITAL_SPECIES_HERE_MARK + "." + PUT_ASSEMBLY_HERE_MARK, "");
                boolean found = false;
                for (File file : targetPath.toFile().listFiles()) {
                    if (file.getName().endsWith(filename)) {
                        filePath = file.toPath();
                        found = true;
                    }
                }
                if (!found) {
                    throw new CellBaseException("Expected " + name + " file (configuration file ID = " + fileId + ") does not exist at "
                            + targetPath);
                }
            } else {
                throw new CellBaseException("Expected " + name + " file: " + filename + " does not exist at " + targetPath);
            }
        }
        logger.info("Ok.");
        return filePath.toFile();
    }

    protected File checkFile(String data, DownloadProperties.URLProperties props, String fileId, Path targetPath) throws CellBaseException {
        logger.info("Checking file {} (file ID {} in config.) ...", getDataName(data), fileId);
        if (!props.getFiles().containsKey(fileId)) {
            throw new CellBaseException("File ID " + fileId + " does not exist in the configuration file in the section '" + data + "'");
        }
        if (!Files.exists(targetPath)) {
            throw new CellBaseException("Folder does not exist " + targetPath);
        }

        String filename = Paths.get(props.getFiles().get(fileId)).getFileName().toString();
        Path filePath = targetPath.resolve(filename);
        if (!Files.exists(filePath)) {
            throw new CellBaseException(getDataName(data) + " file " + filePath + " does not exist");
        }
        logger.info("Ok.");
        return filePath.toFile();
    }

    protected List<File> checkFiles(String data, Path downloadPath, int expectedFiles) throws CellBaseException, IOException {
        return checkFiles(getDataName(data), data, downloadPath, expectedFiles);
    }

    protected List<File> checkFiles(String label, String data, Path downloadPath, int expectedFiles) throws CellBaseException, IOException {
        List<File> files = checkFiles(dataSourceReader.readValue(downloadPath.resolve(getDataVersionFilename(data)).toFile()),
                downloadPath, label);
        if (files.size() != expectedFiles) {
            throw new CellBaseException(expectedFiles + " " + label + " files are expected at " + downloadPath + ", but currently there"
                    + " are " + files.size() + " files");
        }
        return files;
    }

    protected List<File> checkFiles(DataSource dataSource, Path targetPath, String name) throws CellBaseException {
        logger.info("Checking {} folder and files ...", name);
        if (!targetPath.toFile().exists()) {
            throw new CellBaseException(name + " folder does not exist " + targetPath);
        }

        List<File> files = new ArrayList<>();

        List<String> filenames = dataSource.getUrls().stream().map(u -> Paths.get(u).getFileName().toString()).collect(Collectors.toList());
        for (String filename : filenames) {
            File file = targetPath.resolve(filename).toFile();
            if (!file.exists()) {
                throw new CellBaseException("File " + file + " does not exits");
            } else {
                files.add(file);
            }
        }
        logger.info("Ok.");
        return files;
    }

    protected Path getIndexFastaReferenceGenome(Path fastaPath) throws CellBaseException {
        Path indexFastaPath = Paths.get(fastaPath + FAI_EXTENSION);
        if (!Files.exists(indexFastaPath)) {
            // Index FASTA file
            logger.info("Indexing FASTA file {} ...", fastaPath);
            String errorMsg = "Error executing 'samtools faidx' for FASTA file ";
            try {
                List<String> params = Arrays.asList("faidx", fastaPath.toString());
                EtlCommons.runCommandLineProcess(null, "samtools", params, null);
            } catch (IOException e) {
                throw new CellBaseException(errorMsg + fastaPath, e);
            } catch (InterruptedException e) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                throw new CellBaseException(errorMsg + fastaPath, e);
            }
            if (!Files.exists(indexFastaPath)) {
                throw new CellBaseException("It could not index the FASTA file " + fastaPath + ". Please, try to do it manually!");
            }
        }
        return indexFastaPath;
    }
}
