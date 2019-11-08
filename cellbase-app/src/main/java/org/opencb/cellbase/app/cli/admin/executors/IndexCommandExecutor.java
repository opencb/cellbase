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

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.Species;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.mongodb.MongoDBIndexUtils;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.io.IOException;
import java.io.InputStream;

public class IndexCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.IndexCommandOptions indexCommandOptions;

    private String data;
    private Species species;
    private String assembly;

    public IndexCommandExecutor(AdminCliOptionsParser.IndexCommandOptions indexCommandOptions) {
        super(indexCommandOptions.commonOptions.logLevel, indexCommandOptions.commonOptions.verbose,
                indexCommandOptions.commonOptions.conf);

        this.indexCommandOptions = indexCommandOptions;

        if (indexCommandOptions.data != null) {
            data = indexCommandOptions.data;
        }
    }

    /**
     * Parse specific 'data' command options.
     */
    public void execute() {
        setSpecies();
        if (data != null) {
            String[] indexes;
            if (("all").equalsIgnoreCase(data)) {
                createIndexes(null, indexCommandOptions.dropIndexesFirst);
            } else {
                indexes = data.split(",");
                createIndexes(indexes, indexCommandOptions.dropIndexesFirst);
            }
        } else {
            createIndexes(null, indexCommandOptions.dropIndexesFirst);
        }
    }

    private void createIndexes(String[] indexes, boolean dropIndexesFirst) {
        InputStream resourceAsStream = getClass().getResourceAsStream("/mongodb-indexes.json");
        if (resourceAsStream == null) {
            logger.warn("Index file mongodb-indexes.json not found");
            return;
        }
        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(configuration);
        MongoDataStore mongoDataStore = factory.createMongoDBDatastore(species.getScientificName(), this.assembly);
        try {
            if (indexes == null || indexes.length == 0) {
                MongoDBIndexUtils.createAllIndexes(mongoDataStore, resourceAsStream, dropIndexesFirst);
            } else {
                for (String indexName : indexes) {
                    MongoDBIndexUtils.createIndexes(mongoDataStore, resourceAsStream, indexName, dropIndexesFirst);
                }
            }
        } catch (IOException e) {
            logger.warn("Error creating indexes:" + e.getMessage());
        }
    }

    private void setSpecies() {
        // We need to get the Species object from the CLI name
        // This can be the scientific or common name, or the ID
        //            Species speciesToDownload = null;
        for (Species sp : configuration.getAllSpecies()) {
            if (indexCommandOptions.species.equalsIgnoreCase(sp.getScientificName())
                    || indexCommandOptions.species.equalsIgnoreCase(sp.getCommonName())
                    || indexCommandOptions.species.equalsIgnoreCase(sp.getId())) {
                this.species = sp;

                if (StringUtils.isNotEmpty(indexCommandOptions.assembly)) {
                    for (Species.Assembly assembly : species.getAssemblies()) {
                        if (assembly.getName().equalsIgnoreCase(indexCommandOptions.assembly)) {
                            this.assembly = indexCommandOptions.assembly;
                            break;
                        }
                    }
                    if (StringUtils.isEmpty(this.assembly)) {
                        throw new ParameterException("Assembly '" + indexCommandOptions.assembly + "' is invalid");
                    }
                } else {
                    this.assembly = species.getAssemblies().get(0).getName();
                    return;
                }
            }
        }

        if (this.species == null) {
            throw new ParameterException("Species '" + indexCommandOptions.species + "' is invalid");
        }
    }
}
