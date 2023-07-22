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

public class TokenQuota {

    private long maxNumQueries;

    public static final Long MAX_NUM_ANOYMOUS_QUERIES = 1000000L;
    public static final Long DEFAULT_MAX_NUM_QUERIES = 10000000L;

    public TokenQuota() {
        this(DEFAULT_MAX_NUM_QUERIES);
    }

    public TokenQuota(long maxNumQueries) {
        this.maxNumQueries = maxNumQueries;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TokenQuota{");
        sb.append("maxNumQueries=").append(maxNumQueries);
        sb.append('}');
        return sb.toString();
    }

    public long getMaxNumQueries() {
        return maxNumQueries;
    }

    public TokenQuota setMaxNumQueries(long maxNumQueries) {
        this.maxNumQueries = maxNumQueries;
        return this;
    }
}
