FROM openjdk:8-jre-alpine

# Metadata
LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.url="http://docs.opencb.org/" \
      org.label-schema.name="cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics"

# Install local dependencies
#RUN apt-get update && apt-get dist-upgrade \
#  && apt-get clean

RUN apk update && apk upgrade && \
    addgroup -S cellbase && adduser -S cellbase -G cellbase

USER cellbase
COPY . /opt/cellbase/
ENV CELLBASE_HOME /opt/cellbase/

WORKDIR /opt/cellbase
