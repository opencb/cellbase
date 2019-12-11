# OpenCB
#
# DESCRIPTION: All non-Java CellBase dependencies
# VERSION               0.0.1
# CREDITS Started as a fork of helios/ensembl-docker

FROM        ubuntu:18.04
MAINTAINER  Julie Sullivan <julie.sullivan@gmail.com>

RUN apt-get update --fix-missing && \
    apt-get install -y inotify-tools nano wget curl rsync bc git git-core apt-transport-https libxml2 libxml2-dev libcurl4-openssl-dev openssl sqlite3 libsqlite3-dev gawk libreadline6-dev libyaml-dev autoconf libgdbm-dev libncurses5-dev automake libtool bison libffi-dev make m4 git build-essential software-properties-common libdbi-perl libdbd-mysql-perl libdbd-sqlite3-perl libcatalyst-perl liblog-log4perl-perl libcatalyst-action-rest-perl libcatalyst-modules-perl libchi-perl libxml-writer-perl

## Ensembl Perl lib installation
RUN cd /opt && \
    git clone -b release-1-6-924 --depth 1 https://github.com/bioperl/bioperl-live.git && \
    git clone https://github.com/Ensembl/ensembl-git-tools.git && \
    export PATH=/opt/ensembl-git-tools/bin:$PATH && \
    git ensembl --clone api

VOLUME /ensembl-data

COPY ../../bin/ensembl-scripts/* /opt/
COPY entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
