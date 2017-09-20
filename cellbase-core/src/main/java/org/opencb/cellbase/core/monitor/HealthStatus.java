package org.opencb.cellbase.core.monitor;

/**
 * Created by fjlopez on 20/09/17.
 */
public class HealthStatus {

    private ApplicationDetails applicationDetails;

    public HealthStatus() {
    }

    public ApplicationDetails getApplicationDetails() {
        return applicationDetails;
    }

    public void setApplicationDetails(ApplicationDetails applicationDetails) {
        this.applicationDetails = applicationDetails;
    }

    public class ApplicationDetails {

        private String maintainer;
        private String server;
        private String started;
        private String uptime;
        private Version version;

        public ApplicationDetails() {
        }

        public String getMaintainer() {
            return maintainer;
        }

        public void setMaintainer(String maintainer) {
            this.maintainer = maintainer;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getStarted() {
            return started;
        }

        public void setStarted(String started) {
            this.started = started;
        }

        public String getUptime() {
            return uptime;
        }

        public void setUptime(String uptime) {
            this.uptime = uptime;
        }

        public Version getVersion() {
            return version;
        }

        public void setVersion(Version version) {
            this.version = version;
        }

        private class Version {
            private String tagName;
            private String commit;

            public Version() {
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
}
