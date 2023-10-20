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

package org.opencb.cellbase.core.api.key;

public class ApiKeyStats {
    private String apiKey;
    private String date; // date consists of year + month, e.g.: 202304
    private long numQueries;
    private long duration;
    private long bytes;

    public ApiKeyStats() {
    }

    public ApiKeyStats(String apiKey, String date) {
        this(apiKey, date, 0, 0, 0);
    }

    public ApiKeyStats(String apiKey, String date, long numQueries, long duration, long bytes) {
        this.apiKey = apiKey;
        this.date = date;
        this.numQueries = numQueries;
        this.duration = duration;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TokenStats{");
        sb.append("apiKey='").append(apiKey).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append(", numQueries=").append(numQueries);
        sb.append(", duration=").append(duration);
        sb.append(", bytes=").append(bytes);
        sb.append('}');
        return sb.toString();
    }

    public String getApiKey() {
        return apiKey;
    }

    public ApiKeyStats setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getDate() {
        return date;
    }

    public ApiKeyStats setDate(String date) {
        this.date = date;
        return this;
    }

    public long getNumQueries() {
        return numQueries;
    }

    public ApiKeyStats setNumQueries(long numQueries) {
        this.numQueries = numQueries;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public ApiKeyStats setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public long getBytes() {
        return bytes;
    }

    public ApiKeyStats setBytes(long bytes) {
        this.bytes = bytes;
        return this;
    }
}
