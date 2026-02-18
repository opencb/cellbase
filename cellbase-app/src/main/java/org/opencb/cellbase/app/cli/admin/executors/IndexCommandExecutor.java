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

import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.Release;
import org.opencb.cellbase.lib.indexer.IndexManager;
import org.opencb.cellbase.lib.managers.DataReleaseManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class IndexCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.IndexCommandOptions indexCommandOptions;

    public IndexCommandExecutor(AdminCliOptionsParser.IndexCommandOptions indexCommandOptions) {
        super(indexCommandOptions.commonOptions.logLevel, indexCommandOptions.commonOptions.conf);

        this.indexCommandOptions = indexCommandOptions;
    }

    public void execute() {
        try (DataReleaseManager dataReleaseManager = new DataReleaseManager(indexCommandOptions.database, configuration)) {
            // Check data release
            boolean found = false;
            List<Release> releases = dataReleaseManager.getReleases().getResults();
            for (Release release : releases) {
                if (indexCommandOptions.dataRelease.equalsIgnoreCase(String.valueOf(release.getRelease()))) {
                    found = true;
                }
            }
            if (!found) {
                throw new CellBaseException("Data release " + indexCommandOptions.dataRelease + " not found in database "
                        + indexCommandOptions.database + ". Available releases: "
                        + StringUtils.join(releases.stream().map(Release::getRelease).collect(Collectors.toList()), ", "));
            }

            Path indexFile = Paths.get(this.appHome).resolve("conf").resolve("mongodb-indexes.json");
            logger.info("Using index configuration file: {}", indexFile.toAbsolutePath());
            IndexManager indexManager = new IndexManager(indexCommandOptions.database, indexFile, configuration);
            if (indexCommandOptions.validate) {
                indexManager.validateMongoDBIndexes(indexCommandOptions.data);
            } else {
                indexManager.createMongoDBIndexes(indexCommandOptions.data, indexCommandOptions.dataRelease,
                        indexCommandOptions.dropIndexesFirst);
            }

        } catch (CellBaseException | IOException e) {
            logger.error("Error creating indexes: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
