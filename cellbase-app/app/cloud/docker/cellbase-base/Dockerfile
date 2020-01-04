FROM openjdk:8-jre-alpine

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-base" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Julie Sullivan <julie.sullivan@gmail.com>" \
      org.label-schema.schema-version="1.0"


ENV CELLBASE_USER cellbase

RUN addgroup -S $CELLBASE_USER && adduser -S $CELLBASE_USER -G $CELLBASE_USER
USER $CELLBASE_USER

VOLUME /opt/cellbase/conf

COPY . /opt/cellbase
WORKDIR /opt/cellbase