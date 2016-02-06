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

package org.opencb.cellbase.core.loader;

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by parce on 18/02/15.
 */
public abstract class CellBaseLoader implements Callable<Integer> {

    protected final BlockingQueue<List<String>> blockingQueue;
    protected String data;
    protected String database;

    protected String field;

    protected CellBaseConfiguration cellBaseConfiguration;

    protected final Logger logger;


    public CellBaseLoader(BlockingQueue<List<String>> blockingQueue, String data, String database, String field,
                          CellBaseConfiguration configuration) {
        this.blockingQueue = blockingQueue;
        this.data = data;
        this.database = database;
        this.field = field;

        if (configuration != null) {
            this.cellBaseConfiguration = configuration;
        } else {
            try {
                this.cellBaseConfiguration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void init() throws LoaderException;

    @Override
    public abstract Integer call();

    public abstract void createIndex(String data) throws LoaderException;

    public abstract void close();

}
