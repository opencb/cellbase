ARG TAG
FROM opencb/cellbase-base:$TAG

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-builder" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Ignacio Medina <igmecas@gmail.com>" \
      org.label-schema.schema-version="1.0"

## We need to be root to install dependencies
USER root
RUN apt-get update -y && \
    apt-get install -y git default-mysql-client libjson-perl libdbi-perl libdbd-mysql-perl libdbd-mysql-perl libtry-tiny-perl && \
    mkdir /opt/ensembl && chown cellbase:cellbase /opt/ensembl && \
    rm -rf /var/lib/apt/lists/*

## Become cellbase user again to install Ensembl
USER cellbase

## Install Ensembl Perl libraries
RUN cd /opt/ensembl && \
    git clone -b release-1-6-924 --depth 1 https://github.com/bioperl/bioperl-live.git && \
    git clone https://github.com/Ensembl/ensembl-git-tools.git && \
    git clone https://github.com/Ensembl/ensembl.git && \
    git clone https://github.com/Ensembl/ensembl-variation.git && \
    git clone https://github.com/Ensembl/ensembl-funcgen.git && \
    git clone https://github.com/Ensembl/ensembl-compara.git && \
    git clone https://github.com/Ensembl/ensembl-io.git

ENV PERL5LIB=$PERL5LIB:/opt/ensembl/bioperl-live:/opt/ensembl/ensembl/modules:/opt/ensembl/ensembl-variation/modules:/opt/ensembl/ensembl-funcgen/modules:/opt/ensembl/ensembl-compara/modules:/opt/ensembl/lib/perl/5.18.2:/opt/cellbase
