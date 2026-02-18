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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class DataRelease {
    private int release;
    private String date;
    /** @deprecated */
    @Deprecated
    private boolean active;
    private List<String> activeByDefaultIn;
    private Map<String, String> collections;
    private List<DataReleaseSource> sources;

    public DataRelease() {
        this.activeByDefaultIn = Collections.emptyList();
        this.collections = Collections.emptyMap();
        this.sources = Collections.emptyList();
    }

    public DataRelease(int release, String date, List<String> activeByDefaultIn, Map<String, String> collections,
                       List<DataReleaseSource> sources) {
        this.release = release;
        this.date = date;
        this.activeByDefaultIn = activeByDefaultIn;
        this.collections = collections;
        this.sources = sources;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DataRelease{");
        sb.append("release=").append(this.release);
        sb.append(", date='").append(this.date).append('\'');
        sb.append(", activeByDefaultIn=").append(this.activeByDefaultIn);
        sb.append(", collections=").append(this.collections);
        sb.append(", sources=").append(this.sources);
        sb.append('}');
        return sb.toString();
    }

    public int getRelease() {
        return this.release;
    }

    public DataRelease setRelease(int release) {
        this.release = release;
        return this;
    }

    public String getDate() {
        return this.date;
    }

    public DataRelease setDate(String date) {
        this.date = date;
        return this;
    }

    public boolean isActive() {
        return this.active;
    }

    public DataRelease setActive(boolean active) {
        this.active = active;
        return this;
    }

    public List<String> getActiveByDefaultIn() {
        return this.activeByDefaultIn;
    }

    public DataRelease setActiveByDefaultIn(List<String> activeByDefaultIn) {
        this.activeByDefaultIn = activeByDefaultIn;
        return this;
    }

    public Map<String, String> getCollections() {
        return this.collections;
    }

    public DataRelease setCollections(Map<String, String> collections) {
        this.collections = collections;
        return this;
    }

    public List<DataReleaseSource> getSources() {
        return this.sources;
    }

    public DataRelease setSources(List<DataReleaseSource> sources) {
        this.sources = sources;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DataRelease that = (DataRelease)o;
            return this.release == that.release && Objects.equals(this.activeByDefaultIn, that.activeByDefaultIn)
                    && Objects.equals(this.date, that.date) && Objects.equals(this.collections, that.collections)
                    && Objects.equals(this.sources, that.sources);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.release, this.date, this.activeByDefaultIn, this.collections, this.sources});
    }
}
