/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.client.config;

import java.util.List;

/**
 * Created by imedina on 04/05/16.
 */
public class RestConfig {

    private List<String> hosts;
    private int timeout;

    public RestConfig() {
    }

    public RestConfig(List<String> hosts, int timeout) {
        this.hosts = hosts;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestConfig{");
        sb.append("hosts=").append(hosts);
        sb.append(", timeout=").append(timeout);
        sb.append('}');
        return sb.toString();
    }

    public List<String> getHosts() {
        return hosts;
    }

    public RestConfig setHosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public RestConfig setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }
}
