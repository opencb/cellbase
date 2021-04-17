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

package org.opencb.cellbase.app.cli.admin.executors;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.lib.loader.LoadRunner;
import org.opencb.commons.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Created by imedina on 03/02/15.
 */
public class CustomiseCommandExecutor extends CommandExecutor {

    private static final String METADATA = "metadata";
    private LoadRunner loadRunner;
    private AdminCliOptionsParser.CustomiseCommandOptions customiseCommandOptions;

    private Path input;

    private String transcriptFlag;
    private String database;
    private String loader;
    private int numThreads;

    public CustomiseCommandExecutor(AdminCliOptionsParser.CustomiseCommandOptions customiseCommandOptions) {
        super(customiseCommandOptions.commonOptions.logLevel, customiseCommandOptions.commonOptions.conf);

        this.customiseCommandOptions = customiseCommandOptions;

        input = Paths.get(customiseCommandOptions.input);

        transcriptFlag = customiseCommandOptions.transcriptFlag;
        database = customiseCommandOptions.database;
        loader = customiseCommandOptions.loader;

//        if (customiseCommandOptions.database != null) {
//            database = customiseCommandOptions.database;
//        }
//
//        if (customiseCommandOptions.loader != null) {
//            loader = customiseCommandOptions.loader;
//        }
    }

    private void checkParameters() throws Exception {
        FileUtils.checkFile(input);

        if (StringUtils.isEmpty(database)) {
            logger.error("database parameter empty");
            throw new Exception("database parameter empty");
        }

        if (customiseCommandOptions.numThreads > 1) {
            numThreads = customiseCommandOptions.numThreads;
        } else {
            numThreads = 1;
            logger.warn("Incorrect number of numThreads, it must be a positive value. This has been set to '{}'", numThreads);
        }

        try {
            Class.forName(loader);
        } catch (ClassNotFoundException e) {
            logger.error("Loader Java class '{}' does not exist", loader);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Parse specific 'data' command options.
     */
    public void execute() {

        try {
            checkParameters();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If 'authenticationDatabase' is not passed by argument then we read it from configuration.json
        if (customiseCommandOptions.loaderParams.containsKey("authenticationDatabase")) {
            configuration.getDatabases().getMongodb().getOptions().put("authenticationDatabase",
                    customiseCommandOptions.loaderParams.get("authenticationDatabase"));
        }

        if (StringUtils.isNotEmpty(transcriptFlag)) {
//            loadRunner = new LoadRunner(loader, database, numThreads, configuration);
            System.out.println("WIP");
        }
    }

    private void loadIfExists(Path path, String collection) throws NoSuchMethodException, InterruptedException,
            ExecutionException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        File file = new File(path.toString());
        if (file.exists()) {
            if (file.isFile()) {
                loadRunner.load(path, collection);
            } else {
                logger.warn("{} is not a file - skipping", path.toString());
            }
        } else {
            logger.warn("{} does not exist - skipping", path.toString());
        }
    }



}
