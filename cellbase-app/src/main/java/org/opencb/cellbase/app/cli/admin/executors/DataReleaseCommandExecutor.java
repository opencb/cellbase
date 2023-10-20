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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.DataReleaseManager;

import java.util.Arrays;
import java.util.List;

public class DataReleaseCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DataReleaseCommandOptions dataReleaseCommandOptions;

    private String database;

    public DataReleaseCommandExecutor(AdminCliOptionsParser.DataReleaseCommandOptions dataReleaseCommandOptions) {
        super(dataReleaseCommandOptions.commonOptions.logLevel, dataReleaseCommandOptions.commonOptions.conf);

        this.dataReleaseCommandOptions = dataReleaseCommandOptions;

        if (dataReleaseCommandOptions.database != null) {
            database = dataReleaseCommandOptions.database;
        }
    }


    /**
     * Execute one of the selected actions according to the input parameters.
     */
    public void execute() {
        try {
            checkParameters();

            DataReleaseManager dataReleaseManager = new DataReleaseManager(database, configuration);

            DataRelease dataRelease;
            if (dataReleaseCommandOptions.create) {
                // Create release
                dataRelease = dataReleaseManager.createRelease();
                System.out.println("\nData release " + dataRelease.getRelease() + " was created.");
                System.out.println("Data release description (in JSON format):");
                System.out.println(new ObjectMapper().writerFor(DataRelease.class).writeValueAsString(dataRelease));
            } else if (dataReleaseCommandOptions.update > 0) {
                if (StringUtils.isEmpty(dataReleaseCommandOptions.versions)) {
                    throw new IllegalArgumentException("Missing CellBase versions to be added when updating data release");
                }

                // Versions to be added
                List<String> versions = Arrays.asList(dataReleaseCommandOptions.versions.split(","));

                // Update data release
                dataReleaseManager.update(dataReleaseCommandOptions.update, versions);

                dataRelease = dataReleaseManager.get(dataReleaseCommandOptions.update);
                System.out.println("\nData release " + dataRelease.getRelease() + " was updated.");
                System.out.println("Data release description (in JSON format):");
                System.out.println(new ObjectMapper().writerFor(DataRelease.class).writeValueAsString(dataRelease));
            } else if (dataReleaseCommandOptions.list) {
                // List releases
                CellBaseDataResult<DataRelease> dataReleases = dataReleaseManager.getReleases();
                System.out.println("\nNumber of data releases: " + dataReleases.getResults().size());
                System.out.println("List of data releases (in JSON format):");
                System.out.println(new ObjectMapper().writerFor(List.class).writeValueAsString(dataReleases.getResults()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkParameters() {
        int opts = 0;
        if (dataReleaseCommandOptions.create) {
            opts++;
        }
        if (dataReleaseCommandOptions.list) {
            opts++;
        }
        if (dataReleaseCommandOptions.update > 0) {
            opts++;
        }
        if (opts != 1) {
            throw new IllegalArgumentException("Please, select only one of these input parameters: create, update or list");
        }
    }
}
