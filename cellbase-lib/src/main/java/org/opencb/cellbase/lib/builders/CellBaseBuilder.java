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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    public static final String CHECKING_DONE_BEFORE_BUILDING_LOG_MESSAGE = "Checking done!";

    public static final String BUILDING_LOG_MESSAGE = "Building {} ...";
    public static final String BUILDING_DONE_LOG_MESSAGE = "Building done!";

    public static final String CATEGORY_BUILDING_LOG_MESSAGE = "Building {}/{} ...";
    public static final String CATEGORY_BUILDING_DONE_LOG_MESSAGE = "Building done!";

    public static final String PARSING_LOG_MESSAGE = "Parsing {} ...";
    public static final String PARSING_DONE_LOG_MESSAGE = "Parsing done!";


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
        logger.info("Checking {} folder and files", name);
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

        return files;
    }
}
