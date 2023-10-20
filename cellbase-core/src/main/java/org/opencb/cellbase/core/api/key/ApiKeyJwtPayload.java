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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiKeyJwtPayload extends DefaultClaims {

    // CellBase API key claim names
    private static final String VERSION = "version";
    private static final String SOURCES = "sources";
    private static final String QUOTA = "quota";

    public static final String CURRENT_VERSION = "1.0";
    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

    public ApiKeyJwtPayload() {
        super();
    }

    public ApiKeyJwtPayload(Claims claims) {
        super(claims);
    }

    public String getVersion() {
        return get(VERSION, String.class);
    }

    public void setVersion(String version) {
        put(VERSION, version);
    }

    public Map<String, Date> getSources() {
        LinkedHashMap<String, Long> input = get(SOURCES, LinkedHashMap.class);
        Map<String, Date> output = new HashMap<>();
        if (input != null) {
            for (Entry<String, Long> entry : input.entrySet()) {
                output.put(entry.getKey(), new Date(entry.getValue()));
            }
        }
        return output;
    }

    public void setSources(Map<String, Date> sources) {
        put(SOURCES, sources);
    }

    public ApiKeyQuota getQuota() {
        LinkedHashMap input = get(QUOTA, LinkedHashMap.class);
        ApiKeyQuota output = new ApiKeyQuota();
        if (input != null) {
            output.setMaxNumQueries(((Integer) input.get("maxNumQueries")).longValue());
        }
        return output;
    }

    public void setQuota(ApiKeyQuota quota) {
        put(QUOTA, quota);
    }
}
