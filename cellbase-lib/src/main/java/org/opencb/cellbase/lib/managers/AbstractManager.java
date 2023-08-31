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

package org.opencb.cellbase.lib.managers;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.api.key.ApiKeyManager;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.db.MongoDBManager;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.opencb.cellbase.core.ParamConstants.API_KEY_PARAM;

public class AbstractManager implements AutoCloseable {

    protected String species;
    protected String assembly;
    protected CellBaseConfiguration configuration;

    protected MongoDBManager mongoDBManager;
    protected MongoDataStore mongoDatastore;
    protected MongoDBAdaptorFactory dbAdaptorFactory;

    protected ApiKeyManager apiKeyManager;

    protected Logger logger;

    public AbstractManager(String databaseName, CellBaseConfiguration configuration) throws CellBaseException {
        this.configuration = configuration;

        logger = LoggerFactory.getLogger(this.getClass());

        // We create a MongoDB database connection for each Manager
        mongoDBManager = new MongoDBManager(configuration);
        mongoDatastore = mongoDBManager.createMongoDBDatastore(databaseName);
        dbAdaptorFactory = new MongoDBAdaptorFactory(mongoDatastore);

        apiKeyManager = new ApiKeyManager(configuration.getSecretKey());
    }

    public AbstractManager(String species, String assembly, CellBaseConfiguration configuration)
            throws CellBaseException {
        this.species = species;
        this.assembly = assembly;
        this.configuration = configuration;

        logger = LoggerFactory.getLogger(this.getClass());

        // If assembly is emtpy we take the default, typically the first and only one.
        if (StringUtils.isEmpty(assembly)) {
            assembly = SpeciesUtils.getSpecies(configuration, species, assembly).getAssembly();
        }

        // We create a MongoDB database connection for each Manager
        mongoDBManager = new MongoDBManager(configuration);
        mongoDatastore = mongoDBManager.createMongoDBDatastore(species, assembly);
        dbAdaptorFactory = new MongoDBAdaptorFactory(mongoDatastore);

        apiKeyManager = new ApiKeyManager(configuration.getSecretKey());
    }

    protected String getApiKey(QueryOptions queryOptions) {
        return queryOptions.getString(API_KEY_PARAM);
    }

    @Deprecated
    protected List<Query> createQueries(Query query, String csvField, String queryKey, String... args) {
        String[] ids = csvField.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
        for (String id : ids) {
            Query q = new Query(query);
            q.put(queryKey, id);
            if (args != null && args.length > 0 && args.length % 2 == 0) {
                for (int i = 0; i < args.length; i += 2) {
                    q.put(args[i], args[i + 1]);
                }
            }
            queries.add(q);
        }
        return queries;
    }

    @Override
    public void close()  {
        mongoDBManager.close();
    }
}
