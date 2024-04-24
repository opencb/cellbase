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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by imedina on 30/08/14.
 */
public abstract class CellBaseBuilder {

    protected CellBaseSerializer serializer;
    protected ObjectReader dataSourceReader = new ObjectMapper().readerFor(DataSource.class);

    protected Logger logger;

    public static final String BUILDING_LOG_MESSAGE = "Building {} ...";
    public static final String BUILDING_DONE_LOG_MESSAGE = "Building {} done.";

    public static final String PARSING_LOG_MESSAGE = "Parsing file {} ...";
    public static final String PARSING_DONE_LOG_MESSAGE = "Parsing file {} done.";


    public CellBaseBuilder(CellBaseSerializer serializer) {
        logger = LoggerFactory.getLogger(this.getClass());

        this.serializer = serializer;
        //this.serializer.open();
    }

    public abstract void parse() throws Exception;

    public void disconnect() {
        try {
            serializer.close();
        } catch (Exception e) {
            logger.error("Disconnecting serializer: " + e.getMessage());
        }
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
