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

package org.opencb.cellbase.core.models;

import java.util.Map;

public class DataAccessTokenSources {

    private String version;
    private Map<String, Long> sources;

    public DataAccessTokenSources() {
    }

    public DataAccessTokenSources(String version, Map<String, Long> sources) {
        this.version = version;
        this.sources = sources;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataAccessTokenSources{");
        sb.append("version='").append(version).append('\'');
        sb.append(", sources=").append(sources);
        sb.append('}');
        return sb.toString();
    }

    public String getVersion() {
        return version;
    }

    public DataAccessTokenSources setVersion(String version) {
        this.version = version;
        return this;
    }

    public Map<String, Long> getSources() {
        return sources;
    }

    public DataAccessTokenSources setSources(Map<String, Long> sources) {
        this.sources = sources;
        return this;
    }
}
