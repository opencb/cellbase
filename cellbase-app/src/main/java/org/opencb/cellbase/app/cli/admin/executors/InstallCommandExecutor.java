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
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.install.InstallManager;

import java.io.IOException;


public class InstallCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.InstallCommandOptions installCommandOptions;

    public InstallCommandExecutor(AdminCliOptionsParser.InstallCommandOptions installCommandOptions) {
        super(installCommandOptions.commonOptions.logLevel, installCommandOptions.commonOptions.verbose,
                installCommandOptions.commonOptions.conf);

        this.installCommandOptions = installCommandOptions;
    }

    public void execute() throws CellbaseException {
        try {
            logger.info("Starting installation ...");
            InstallManager installManager = new InstallManager(configuration);
            installManager.install(installCommandOptions.species, installCommandOptions.assembly);
        } catch (CellbaseException e) {
            logger.error("Error installing:" + e.toString());
        }
    }
}
