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

package org.opencb.cellbase.lib.monitor.status;

public class ApplicationDetails {

    private String maintainer;
    private String server;
    private String started;
    private String uptime;
    private Version version;
    private DependenciesStatus dependencies;

    public ApplicationDetails() {
    }

    public String getMaintainer() {
        return maintainer;
    }

    public ApplicationDetails setMaintainer(String maintainer) {
        this.maintainer = maintainer;
        return this;
    }

    public String getServer() {
        return server;
    }

    public ApplicationDetails setServer(String server) {
        this.server = server;
        return this;
    }

    public String getStarted() {
        return started;
    }

    public ApplicationDetails setStarted(String started) {
        this.started = started;
        return this;
    }

    public String getUptime() {
        return uptime;
    }

    public ApplicationDetails setUptime(String uptime) {
        this.uptime = uptime;
        return this;
    }

    public Version getVersion() {
        return version;
    }

    public ApplicationDetails setVersion(Version version) {
        this.version = version;
        return this;
    }

    public DependenciesStatus getDependencies() {
        return dependencies;
    }

    public ApplicationDetails setDependencies(DependenciesStatus dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public static class Version {
        private String tagName;
        private String commit;

        public Version() {
        }

        public Version(String tagName, String commit) {
            this.tagName = tagName;
            this.commit = commit;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public String getCommit() {
            return commit;
        }

        public void setCommit(String commit) {
            this.commit = commit;
        }
    }
}
