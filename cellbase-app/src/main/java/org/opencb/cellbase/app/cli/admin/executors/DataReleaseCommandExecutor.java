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

import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.lib.managers.ReleaseManager;

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

        checkParameters();

        try {
            ReleaseManager releaseManager = new ReleaseManager(database, configuration);

            if (dataReleaseCommandOptions.create) {
                releaseManager.createRelease();
            } else if (dataReleaseCommandOptions.activeByDefault > 0) {
                releaseManager.activeByDefault(dataReleaseCommandOptions.activeByDefault);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkParameters() {
        if (dataReleaseCommandOptions.create && dataReleaseCommandOptions.activeByDefault > 0) {
            logger.error("Input parameters usage. Please, select only one action: --create or --set-active");
        }
    }
}
