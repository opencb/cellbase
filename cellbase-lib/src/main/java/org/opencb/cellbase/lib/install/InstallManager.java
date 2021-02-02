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
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class InstallManager {

    private CellBaseConfiguration configuration;
    private Logger logger;

    public InstallManager(CellBaseConfiguration configuration) {
        this.configuration = configuration;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Add shard indexes and ranges in Mongo based on config file entries.
     *
     * @param speciesName name of species
     * @param assemblyName name of assembly
     * @throws CellbaseException if invalid input
     */
    public void install(String speciesName, String assemblyName) throws CellbaseException {
        // TDDO check database credentials

        // user API perms

        // check repl sets

        Species species = SpeciesUtils.getSpecies(configuration, speciesName, assemblyName);

        SpeciesConfiguration speciesConfiguration = configuration.getSpeciesConfig(species.getSpecies());
        if (speciesConfiguration == null) {
            LoggerFactory.getLogger(MongoDBShardUtils.class).warn("No config found for '" + species.getSpecies() + "'");
            return;
        }

        List<SpeciesConfiguration.ShardConfig> shards = speciesConfiguration.getShards();
        if (shards != null) {
            // if sharding in config
            shard(species);
        }
    }

    private void shard(Species species) throws CellbaseException {
        MongoDBAdaptorFactory factory = new MongoDBAdaptorFactory(configuration);
        MongoDataStore mongoDataStore = factory.getMongoDBDatastore(species.getSpecies(), species.getAssembly());
        MongoDBShardUtils.shard(mongoDataStore, configuration, species);
    }
}
