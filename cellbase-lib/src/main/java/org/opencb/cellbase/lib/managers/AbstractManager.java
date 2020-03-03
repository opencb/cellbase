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

import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AbstractManager {

    protected CellBaseConfiguration configuration;
    protected CellBaseManagerFactory managers;
    protected MongoDBAdaptorFactory dbAdaptorFactory;
    protected static ObjectWriter jsonObjectWriter;

    protected String species;
    protected String assembly;

    protected Logger logger;

//    protected int histogramIntervalSize = 200000;
//    private static final int SKIP_DEFAULT = 0;
//    private static final int LIMIT_DEFAULT = 10;
    private static final int MAX_RECORDS = 5000;

    public AbstractManager(CellBaseConfiguration configuration) {
        this.configuration = configuration;

        this.init();
    }

    public AbstractManager(String species, CellBaseConfiguration configuration) {
        this(species, null, configuration);
    }

    public AbstractManager(String species, String assembly, CellBaseConfiguration configuration) {
        this.species = species;
        this.assembly = assembly;
        this.configuration = configuration;

        this.init();
    }

    private void init() {
        managers = new CellBaseManagerFactory(this.configuration);
        dbAdaptorFactory = new MongoDBAdaptorFactory(this.configuration);

        logger = LoggerFactory.getLogger(this.getClass());
    }

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
}
