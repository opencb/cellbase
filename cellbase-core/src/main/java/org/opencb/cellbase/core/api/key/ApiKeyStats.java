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

import java.util.Map;

public class ApiKeyStats {
    private String apiKey;
    private String date; // date consists of year + month, e.g.: 202304
    private long numQueries;
    private long numAnnotatedVariants;
    private long duration;
    private long outputBytes;
    private Map<String, Object> token;

    public ApiKeyStats() {
    }

    public ApiKeyStats(String apiKey, String date, Map<String, Object> token) {
        this(apiKey, date, 0, 0, 0, 0, token);
    }

    public ApiKeyStats(String apiKey, String date, long numQueries, long numAnnotatedVariants, long duration, long outputBytes,
                       Map<String, Object> token) {
        this.apiKey = apiKey;
        this.date = date;
        this.numQueries = numQueries;
        this.numAnnotatedVariants = numAnnotatedVariants;
        this.duration = duration;
        this.outputBytes = outputBytes;
        this.token = token;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApiKeyStats{");
        sb.append("apiKey='").append(apiKey).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append(", numQueries=").append(numQueries);
        sb.append(", numAnnotatedVariants=").append(numAnnotatedVariants);
        sb.append(", duration=").append(duration);
        sb.append(", outputBytes=").append(outputBytes);
        sb.append(", token=").append(token);
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

    public long getNumAnnotatedVariants() {
        return numAnnotatedVariants;
    }

    public ApiKeyStats setNumAnnotatedVariants(long numAnnotatedVariants) {
        this.numAnnotatedVariants = numAnnotatedVariants;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public ApiKeyStats setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public long getOutputBytes() {
        return outputBytes;
    }

    public ApiKeyStats setOutputBytes(long outputBytes) {
        this.outputBytes = outputBytes;
        return this;
    }

    public Map<String, Object> getToken() {
        return token;
    }

    public ApiKeyStats setToken(Map<String, Object> token) {
        this.token = token;
        return this;
    }
}
