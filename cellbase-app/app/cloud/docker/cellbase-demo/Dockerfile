ARG TAG
FROM opencb/cellbase-tomcat:$TAG

USER root

# install mongodb
# Upgrade twice, to ensure that openjdk8-jre-lib and openjdk8-jre-base are in sync
# See https://github.com/opencb/opencga/issues/1653
RUN apk update && \
    apk upgrade && \
    apk upgrade && \
    apk add --no-cache bash && \
    apk add --no-cache mongodb

VOLUME ["/data/cellbase/mongodb"]
EXPOSE 27017 27017
EXPOSE 8080 8080

# Copy init.sh which perform initialization setps.
COPY ${BUILD_PATH}/cloud/docker/cellbase-demo/init.sh /opt/scripts/init.sh
COPY ${BUILD_PATH}/cloud/docker/cellbase-demo/mongo-init.js /opt/scripts/mongo-init.js
RUN chmod +x /opt/scripts/init.sh

WORKDIR /opt/cellbase/bin
ENTRYPOINT ["/bin/bash", "-c", "/opt/scripts/init.sh"]
