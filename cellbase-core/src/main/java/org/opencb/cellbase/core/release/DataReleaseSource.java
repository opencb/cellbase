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

package org.opencb.cellbase.core.release;

public class DataReleaseSource {
    private String name;
    private String version;
    private String data;
    private String url;
    private String date;

    public DataReleaseSource(String name, String version, String data, String url, String date) {
        this.name = name;
        this.version = version;
        this.data = data;
        this.url = url;
        this.date = date;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataReleaseSource{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", data='").append(data).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public DataReleaseSource setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public DataReleaseSource setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getData() {
        return data;
    }

    public DataReleaseSource setData(String data) {
        this.data = data;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DataReleaseSource setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDate() {
        return date;
    }

    public DataReleaseSource setDate(String date) {
        this.date = date;
        return this;
    }
}
