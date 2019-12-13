FROM openjdk:8-jre-alpine

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.url="http://docs.opencb.org/" \
      org.label-schema.name="cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics"

ENV CELLBASE_USER cellbase

RUN addgroup -S $CELLBASE_USER && adduser -S $CELLBASE_USER -G $CELLBASE_USER

ARG buildPath="./build"
ENV CELLBASE_HOME /opt/cellbase/

USER $CELLBASE_USER
COPY ${buildPath} ${CELLBASE_HOME}

WORKDIR /opt/cellbase