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

import java.util.*;

public class Release {
    private int release;
    private String date;
    private List<String> activeByDefaultIn;
    private Map<String, String> collections;
    private List<DataSource> sources;

    public Release() {
        this.activeByDefaultIn = Collections.emptyList();
        this.collections = Collections.emptyMap();
        this.sources = Collections.emptyList();
    }

    public Release(int release, String date, List<String> activeByDefaultIn, Map<String, String> collections,
                   List<DataSource> sources) {
        this.release = release;
        this.date = date;
        this.activeByDefaultIn = activeByDefaultIn;
        this.collections = collections;
        this.sources = sources;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataRelease{");
        sb.append("release=").append(release);
        sb.append(", date='").append(date).append('\'');
        sb.append(", activeByDefaultIn=").append(activeByDefaultIn);
        sb.append(", collections=").append(collections);
        sb.append(", sources=").append(sources);
        sb.append('}');
        return sb.toString();
    }

    public int getRelease() {
        return release;
    }

    public Release setRelease(int release) {
        this.release = release;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Release setDate(String date) {
        this.date = date;
        return this;
    }

    public List<String> getActiveByDefaultIn() {
        return activeByDefaultIn;
    }

    public Release setActiveByDefaultIn(List<String> activeByDefaultIn) {
        this.activeByDefaultIn = activeByDefaultIn;
        return this;
    }

    public Map<String, String> getCollections() {
        return collections;
    }

    public Release setCollections(Map<String, String> collections) {
        this.collections = collections;
        return this;
    }

    public List<DataSource> getSources() {
        return sources;
    }

    public Release setSources(List<DataSource> sources) {
        this.sources = sources;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Release that = (Release) o;
        return release == that.release
                && Objects.equals(activeByDefaultIn, that.activeByDefaultIn)
                && Objects.equals(date, that.date)
                && Objects.equals(collections, that.collections)
                && Objects.equals(sources, that.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(release, date, activeByDefaultIn, collections, sources);
    }
}
