ARG TAG
FROM opencb/cellbase-base:$TAG

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-rest" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Julie Sullivan <julie.sullivan@gmail.com>" \
      org.label-schema.schema-version="1.0"

EXPOSE 9090

USER $CELLBASE_USER
ENTRYPOINT ["./bin/cellbase-admin.sh", "server", "--start"]