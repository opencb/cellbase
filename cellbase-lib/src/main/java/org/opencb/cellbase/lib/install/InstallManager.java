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

package org.opencb.cellbase.lib.install;

import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class InstallManager {

    private CellBaseConfiguration configuration;
    private Logger logger;

    public InstallManager(CellBaseConfiguration configuration) {
        this.configuration = configuration;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void install(String speciesName, String assemblyName) throws CellbaseException, IOException {

    }

    /**
     * Add shard indexes and ranges in Mongo based on config file entries.
     *
     * @param speciesName name of species
     * @param assemblyName name of assembly
     * @throws IOException if configuration file can't be read
     * @throws CellbaseException if indexes file isn't found, or invalid input
     */
    public void shard(String speciesName, String assemblyName) throws CellbaseException, IOException {
        Species species = SpeciesUtils.getSpecies(configuration, speciesName, assemblyName);
        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(configuration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore(species.getSpecies(), species.getAssembly());
        MongoDBShardUtils.shard(mongoDataStore, configuration, species);
    }
}
