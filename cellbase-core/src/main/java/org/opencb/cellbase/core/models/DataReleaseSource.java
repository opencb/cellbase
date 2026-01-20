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

import java.util.List;
import java.util.Objects;

@Deprecated
public class DataReleaseSource {
    private String name;
    private String version;
    private String data;
    private String date;
    private List<String> url;

    public DataReleaseSource() {
    }

    public DataReleaseSource(String name, String version, String data, String date, List<String> url) {
        this.name = name;
        this.version = version;
        this.data = data;
        this.date = date;
        this.url = url;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DataReleaseSource{");
        sb.append("name='").append(this.name).append('\'');
        sb.append(", version='").append(this.version).append('\'');
        sb.append(", data='").append(this.data).append('\'');
        sb.append(", date='").append(this.date).append('\'');
        sb.append(", url=").append(this.url);
        sb.append('}');
        return sb.toString();
    }

    public String getName() {
        return this.name;
    }

    public DataReleaseSource setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public DataReleaseSource setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getData() {
        return this.data;
    }

    public DataReleaseSource setData(String data) {
        this.data = data;
        return this;
    }

    public String getDate() {
        return this.date;
    }

    public DataReleaseSource setDate(String date) {
        this.date = date;
        return this;
    }

    public List<String> getUrl() {
        return this.url;
    }

    public DataReleaseSource setUrl(List<String> url) {
        this.url = url;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DataReleaseSource that = (DataReleaseSource)o;
            return Objects.equals(this.name, that.name) && Objects.equals(this.version, that.version)
                    && Objects.equals(this.data, that.data) && Objects.equals(this.date, that.date)
                    && Objects.equals(this.url, that.url);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.version, this.data, this.date, this.url});
    }
}
