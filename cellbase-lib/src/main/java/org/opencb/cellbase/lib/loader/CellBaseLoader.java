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

package org.opencb.cellbase.lib.loader;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.managers.ReleaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by parce on 18/02/15.
 */
public abstract class CellBaseLoader implements Callable<Integer> {

    protected final BlockingQueue<List<String>> blockingQueue;
    protected String data;
    protected int dataRelease;
    protected List<Path> dataSourcePaths;
    protected ReleaseManager releaseManager;
    protected String database;

    protected String field;
    protected String[] innerFields;

    protected CellBaseConfiguration cellBaseConfiguration;

    protected final Logger logger;

    public CellBaseLoader(BlockingQueue<List<String>> blockingQueue, String data, int dataRelease, List<Path> dataSourcePaths,
                          String database, CellBaseConfiguration configuration) throws CellBaseException {
        this(blockingQueue, data, dataRelease, dataSourcePaths, database, null, null, configuration);
    }

    public CellBaseLoader(BlockingQueue<List<String>> blockingQueue, String data, int dataRelease, Path[] dataSourcePaths,
                          String database, String field, String[] innerFields, CellBaseConfiguration configuration)
            throws CellBaseException {
        this(blockingQueue, data, dataRelease, new ArrayList<>(Arrays.asList(dataSourcePaths)), database, field, innerFields,
                configuration);
    }

    public CellBaseLoader(BlockingQueue<List<String>> blockingQueue, String data, int dataRelease, List<Path> dataSourcePaths,
                          String database, String field, String[] innerFields, CellBaseConfiguration configuration)
            throws CellBaseException {
        this.blockingQueue = blockingQueue;
        this.data = data;
        this.dataRelease = dataRelease;
        this.dataSourcePaths = dataSourcePaths;
        this.database = database;
        this.field = field;
        this.innerFields = innerFields;

        if (configuration != null) {
            this.cellBaseConfiguration = configuration;
        } else {
            try {
                InputStream inputStream = CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.yml");
                this.cellBaseConfiguration = CellBaseConfiguration.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        releaseManager = new ReleaseManager(this.database, this.cellBaseConfiguration);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void init() throws LoaderException;

    @Override
    public abstract Integer call();

    public abstract void createIndex(String data) throws LoaderException;

    public abstract void close() throws LoaderException;
}
