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

package org.opencb.cellbase.core.api.queries;

import java.util.Map;

@Deprecated
public class FeatureQuery {

    private Boolean histogram;
    private static final int HISTOGRAM_INTERVAL_SIZE = 200000;
    private Integer interval;

    public FeatureQuery() {
    }

    public FeatureQuery(Map<String, String> params) {
//        super(params);
    }

    public Boolean getHistogram() {
        return histogram;
    }

    public FeatureQuery setHistogram(Boolean histogram) {
        this.histogram = histogram;
        return this;
    }

    public Integer getInterval() {
        if (interval == null) {
            return HISTOGRAM_INTERVAL_SIZE;
        }
        return interval;
    }

    public FeatureQuery setInterval(Integer interval) {
        this.interval = interval;
        return this;
    }
}
