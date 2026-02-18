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

import java.util.ArrayList;
import java.util.List;

public class DataSource {

    private String id;
    private String name;
    private String category;
    private String version;
    private String downloadDate;
    private List<String> urls;

    public DataSource() {
        this.urls = new ArrayList<>();
    }

    public DataSource(String id, String name, String category, String version, String downloadDate, List<String> urls) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.version = version;
        this.downloadDate = downloadDate;
        this.urls = urls;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataSource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", downloadDate='").append(downloadDate).append('\'');
        sb.append(", urls=").append(urls);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public DataSource setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DataSource setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public DataSource setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public DataSource setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDownloadDate() {
        return downloadDate;
    }

    public DataSource setDownloadDate(String downloadDate) {
        this.downloadDate = downloadDate;
        return this;
    }

    public List<String> getUrls() {
        return urls;
    }

    public DataSource setUrls(List<String> urls) {
        this.urls = urls;
        return this;
    }
}
