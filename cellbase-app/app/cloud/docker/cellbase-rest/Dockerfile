ARG TAG
FROM cellbase-base:$TAG

EXPOSE 9090
USER $CELLBASE_USER
ENTRYPOINT ["./bin/cellbase-admin.sh", "server", "--start"]