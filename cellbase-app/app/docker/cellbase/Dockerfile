FROM openjdk:8-jre-alpine

ARG buildPath="./build/"

ENV CELLBASE_HOME=/opt/cellbase

RUN apk --no-cache --update add openssh-client sshpass ca-certificates curl && \
    adduser -D -u 1001 cellbase -h cellbase

COPY ${buildPath} ${CELLBASE_HOME} 
RUN rm -fr /opt/cellbase/cellbase.war
WORKDIR ${CELLBASE_HOME}/bin
