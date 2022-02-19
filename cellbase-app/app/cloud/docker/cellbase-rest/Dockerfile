ARG TAG
FROM opencb/cellbase-base:$TAG

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-rest" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Ignacio Medina <igmecas@gmail.com>" \
      org.label-schema.schema-version="1.0"

## Expose REST port
EXPOSE 9090

## Run Docker images as non root
USER cellbase

ENTRYPOINT ["./bin/cellbase-admin.sh", "server", "--start"]
