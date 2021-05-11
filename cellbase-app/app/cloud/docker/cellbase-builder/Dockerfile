FROM ubuntu:20.04

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-builder" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Nacho Medina <igmecas@gmail.com>" \
      org.label-schema.schema-version="1.0"

ENV CELLBASE_USER cellbase
ENV CELLBASE_BUILD_DOCKER true

RUN apt-get update --fix-missing && \
    apt-get install -y git mysql-client libjson-perl libdbi-perl libdbd-mysql-perl libdbd-mysql-perl libtry-tiny-perl wget openjdk-8-jdk htop
RUN addgroup $CELLBASE_USER
RUN adduser --disabled-password --ingroup $CELLBASE_USER --uid 1001  $CELLBASE_USER

## Ensembl Perl lib installation
RUN mkdir /opt/ensembl && cd /opt/ensembl && \
    git clone -b release-1-6-924 --depth 1 https://github.com/bioperl/bioperl-live.git && \
    git clone https://github.com/Ensembl/ensembl-git-tools.git && \
    git clone https://github.com/Ensembl/ensembl.git && \
    git clone https://github.com/Ensembl/ensembl-variation.git && \
    git clone https://github.com/Ensembl/ensembl-funcgen.git && \
    git clone https://github.com/Ensembl/ensembl-compara.git && \
    git clone https://github.com/Ensembl/ensembl-io.git

ENV PERL5LIB=$PERL5LIB:/opt/ensembl/bioperl-live:/opt/ensembl/ensembl/modules:/opt/ensembl/ensembl-variation/modules:/opt/ensembl/ensembl-funcgen/modules:/opt/ensembl/ensembl-compara/modules:/opt/ensembl/lib/perl/5.18.2:/opt/cellbase

COPY scripts/ensembl-scripts/* /opt/cellbase/
COPY . /opt/cellbase/

WORKDIR /opt/cellbase