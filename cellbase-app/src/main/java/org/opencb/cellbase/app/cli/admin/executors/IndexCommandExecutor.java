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
import org.opencb.cellbase.lib.indexer.IndexManager;

import java.io.IOException;


public class IndexCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.IndexCommandOptions indexCommandOptions;

    public IndexCommandExecutor(AdminCliOptionsParser.IndexCommandOptions indexCommandOptions) {
        super(indexCommandOptions.commonOptions.logLevel, indexCommandOptions.commonOptions.verbose,
                indexCommandOptions.commonOptions.conf);

        this.indexCommandOptions = indexCommandOptions;
    }

    public void execute() {
        try {
            IndexManager indexManager = new IndexManager(configuration);
            indexManager.createMongoDBIndexes(indexCommandOptions.data, indexCommandOptions.species,
                    indexCommandOptions.assembly, indexCommandOptions.dropIndexesFirst);
        } catch (CellbaseException | IOException e) {
            logger.error("Error creating indexes:" + e.toString());
        }
    }
}
