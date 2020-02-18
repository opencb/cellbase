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

package org.opencb.cellbase.lib.monitor.status;

import org.opencb.cellbase.core.DatastoreStatus;

import java.util.Map;

public class DatastoreDependenciesStatus {
    private Map<String, DatastoreStatus> mongodb;

    public DatastoreDependenciesStatus() {
    }

    public Map<String, DatastoreStatus> getMongodb() {
        return mongodb;
    }

    public void setMongodb(Map<String, DatastoreStatus> mongodb) {
        this.mongodb = mongodb;
    }

}
