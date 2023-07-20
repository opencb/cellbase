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

package org.opencb.cellbase.core.token;

import org.apache.commons.collections4.MapUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class QuotaPayload {

    private String version;
    private Map<String, Long> sources;
    private Long maxNumQueries;

    public static final String CURRENT_VERSION = "1.0";
    public static final Long MAX_NUM_ANOYMOUS_QUERIES = 1000000L;
    public static final Long DEFAULT_MAX_NUM_QUERIES = 10000000L;

    public QuotaPayload() {
        this(CURRENT_VERSION, new HashMap<>(), DEFAULT_MAX_NUM_QUERIES);
    }

    public QuotaPayload(String version, Map<String, Long> sources) {
        this(version, sources, DEFAULT_MAX_NUM_QUERIES);
    }

    public QuotaPayload(String version, Map<String, Long> sources, Long maxNumQueries) {
        this.version = version;
        this.sources = sources;
        this.maxNumQueries = maxNumQueries;
    }

    private static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    public static DateFormat dateFormatter() {
        return dateFormatter;
    }

    public static QuotaPayload parse(String sources) throws ParseException {
        return parse(sources, null);
    }

    public static QuotaPayload parse(String sources, Long maxNumQueries) throws ParseException {
        QuotaPayload dataAccessToken = new QuotaPayload();
        Map<String, Long> sourcesMap = new HashMap<>();
        String[] split = sources.split(",");
        for (String source : split) {
            String[] splits = source.split(":");
            if (splits.length == 1) {
                sourcesMap.put(splits[0], Long.MAX_VALUE);
            } else {
                sourcesMap.put(splits[0], dateFormatter.parse(splits[1]).getTime());
            }
        }

        dataAccessToken.setVersion(CURRENT_VERSION);
        if (MapUtils.isNotEmpty(sourcesMap)) {
            dataAccessToken.setSources(sourcesMap);
        }
        dataAccessToken.setMaxNumQueries(maxNumQueries);

        return dataAccessToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuotaPayload{");
        sb.append("version='").append(version).append('\'');
        sb.append(", sources=").append(sources);
        sb.append(", maxNumQueries=").append(maxNumQueries);
        sb.append('}');
        return sb.toString();
    }

    public String getVersion() {
        return version;
    }

    public QuotaPayload setVersion(String version) {
        this.version = version;
        return this;
    }

    public Map<String, Long> getSources() {
        return sources;
    }

    public QuotaPayload setSources(Map<String, Long> sources) {
        this.sources = sources;
        return this;
    }

    public Long getMaxNumQueries() {
        return maxNumQueries;
    }

    public QuotaPayload setMaxNumQueries(Long maxNumQueries) {
        this.maxNumQueries = maxNumQueries;
        return this;
    }
}
