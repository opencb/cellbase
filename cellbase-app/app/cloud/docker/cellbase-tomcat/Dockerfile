ARG TAG
FROM opencb/cellbase-base:$TAG

# Build args
ARG TOMCAT_VERSION="8.5.38"
ARG VERSION=""
ARG VCS_URL=""
ARG VCS_REF=""
ARG BUILD_DATE=""

# Metadata
LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.url="http://docs.opencb.org/" \
      org.label-schema.name="cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      org.label-schema.version=${VERSION} \
      org.label-schema.vcs-url=${VCS_URL} \
      org.label-schema.vcs-ref=${VCS_REF} \
      org.label-schema.build-date=${BUILD_DATE} \
      org.label-schema.docker.schema-version="1.0"

USER root
# Install local dependencies
#RUN apk update && apk install -y wget tar \
#  && rm -rf /var/lib/apt/lists/* \
#  && apk clean
RUN apk update && apk add --no-cache wget

# Download and install Tomcat
RUN wget --quiet --no-cookies https://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/tomcat.tgz && \
tar xzvf /tmp/tomcat.tgz -C /opt && \
mv /opt/apache-tomcat-${TOMCAT_VERSION} /opt/tomcat && \
rm /tmp/tomcat.tgz && \
rm -rf /opt/tomcat/webapps/examples && \
rm -rf /opt/tomcat/webapps/docs && \
rm -rf /opt/tomcat/webapps/ROOT && \
chown -R 1001:1001 /opt/tomcat/

# Copy cellbase build to Tomcat server
RUN cp /opt/cellbase/*.war /opt/tomcat/webapps/cellbase.war

# copy to /conf directory, just like v5
RUN mv /opt/cellbase/configuration.json /opt/cellbase/conf && ln -s /opt/cellbase/conf/configuration.json /opt/cellbase/configuration.json

ENV CELLBASE_HOME /opt/cellbase/

USER 1001:1001

#HEALTHCHECK --interval=20m --timeout=3s \
#    CMD  wget -q http://localhost:8080/cellbase/webservices/rest/v4/meta/about || killall java

EXPOSE 8080
EXPOSE 8443

# Launch Tomcat
CMD ["/opt/tomcat/bin/catalina.sh","run"]