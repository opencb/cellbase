## Based on Debian 11 (bullseye)
FROM openjdk:11-jre

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-base" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Ignacio Medina <igmecas@gmail.com>" \
      org.label-schema.schema-version="1.0"

## Update and install dependencies
RUN apt-get update && apt-get -y upgrade && apt-get install -y openssl wget htop vim && \
    update-ca-certificates && \
    rm -rf /var/lib/apt/lists/* && \
    adduser --disabled-password --uid 1001 cellbase

## Run Docker images as non root
USER cellbase

## It is important to use --chown parameter in COPY command to reduce the size
COPY --chown=cellbase:cellbase . /opt/cellbase

## Declare the volume to be mounted later
VOLUME /opt/cellbase/conf

WORKDIR /opt/cellbase
