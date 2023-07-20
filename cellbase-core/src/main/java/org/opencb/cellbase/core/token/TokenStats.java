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

public class TokenStats {
    private String token;
    private String date; // date consists of year + month, e.g.: 202304
    private long numQueries;
    private long duration;
    private long bytes;

    public TokenStats() {
    }

    public TokenStats(String token, String date) {
        this(token, date, 0, 0, 0);
    }

    public TokenStats(String token, String date, long numQueries, long duration, long bytes) {
        this.token = token;
        this.date = date;
        this.numQueries = numQueries;
        this.duration = duration;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TokenStats{");
        sb.append("token='").append(token).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append(", numQueries=").append(numQueries);
        sb.append(", duration=").append(duration);
        sb.append(", bytes=").append(bytes);
        sb.append('}');
        return sb.toString();
    }

    public String getToken() {
        return token;
    }

    public TokenStats setToken(String token) {
        this.token = token;
        return this;
    }

    public String getDate() {
        return date;
    }

    public TokenStats setDate(String date) {
        this.date = date;
        return this;
    }

    public long getNumQueries() {
        return numQueries;
    }

    public TokenStats setNumQueries(long numQueries) {
        this.numQueries = numQueries;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public TokenStats setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public long getBytes() {
        return bytes;
    }

    public TokenStats setBytes(long bytes) {
        this.bytes = bytes;
        return this;
    }
}
