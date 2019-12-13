ARG TAG
FROM opencb/cellbase-base:$TAG

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.url="http://docs.opencb.org/" \
      org.label-schema.name="cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics"

EXPOSE 9090
USER $CELLBASE_USER
ENTRYPOINT ["./bin/cellbase-admin.sh", "server", "--start"]