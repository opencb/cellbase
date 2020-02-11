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

import org.opencb.cellbase.core.api.core.DBAdaptorFactory;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractManager {

    protected CellBaseConfiguration configuration;
    protected CellBaseManagers managers;
    protected DBAdaptorFactory dbAdaptorFactory;

    protected Logger logger;

    public static final int DEFAULT_LIMIT = 10;

    public AbstractManager(CellBaseConfiguration configuration) {
        this.configuration = configuration;

        this.init();
    }

    private void init() {
        managers = new CellBaseManagers(this.configuration);
        dbAdaptorFactory = new MongoDBAdaptorFactory(this.configuration);

        logger = LoggerFactory.getLogger(this.getClass());
    }

}
