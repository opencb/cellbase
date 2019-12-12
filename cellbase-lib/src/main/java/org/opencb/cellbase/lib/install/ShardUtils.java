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

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import java.io.IOException;
public class ShardUtils {

    /**
     * Add shards.
     *
     * @param mongoDataStore Database name
     * @param cellBaseConfiguration config file with database details.
     * @throws IOException if index file can't be read
     */
    public static void shard(MongoDataStore mongoDataStore, CellBaseConfiguration cellBaseConfiguration)
            throws IOException {

    }

}
