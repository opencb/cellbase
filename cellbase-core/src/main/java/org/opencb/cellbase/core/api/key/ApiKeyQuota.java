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

public class ApiKeyQuota {

    private long maxNumQueries;
    private long maxNumAnnotatedVariants;
    private long maxOutputBytes;

    public static final Long MAX_NUM_ANONYMOUS_QUERIES = 1000000L;
    public static final Long DEFAULT_MAX_NUM_QUERIES = 10000000L;

    public ApiKeyQuota() {
        this(DEFAULT_MAX_NUM_QUERIES, 0, 0);
    }

    public ApiKeyQuota(long maxNumQueries, long maxNumAnnotatedVariants, long maxOutputBytes) {
        this.maxNumQueries = maxNumQueries;
        this.maxNumAnnotatedVariants = maxNumAnnotatedVariants;
        this.maxOutputBytes = maxOutputBytes;
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

    public ApiKeyQuota setMaxNumQueries(long maxNumQueries) {
        this.maxNumQueries = maxNumQueries;
        return this;
    }

    public long getMaxNumAnnotatedVariants() {
        return maxNumAnnotatedVariants;
    }

    public ApiKeyQuota setMaxNumAnnotatedVariants(long maxNumAnnotatedVariants) {
        this.maxNumAnnotatedVariants = maxNumAnnotatedVariants;
        return this;
    }

    public long getMaxOutputBytes() {
        return maxOutputBytes;
    }

    public ApiKeyQuota setMaxOutputBytes(long maxOutputBytes) {
        this.maxOutputBytes = maxOutputBytes;
        return this;
    }
}
