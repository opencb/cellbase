ARG TAG
FROM opencb/cellbase-base:$TAG
#FROM alpine

LABEL org.label-schema.vendor="OpenCB" \
      org.label-schema.name="cellbase-python" \
      org.label-schema.url="http://docs.opencb.org/display/cellbase" \
      org.label-schema.description="An Open Computational Genomics Analysis platform for big data processing and analysis in genomics" \
      maintainer="Julie Sullivan <julie.sullivan@gmail.com>" \
      org.label-schema.schema-version="1.0"


# This hack is widely applied to avoid python printing issues in docker containers.
ENV PYTHONUNBUFFERED=1

USER root
RUN apk add --no-cache python3 curl build-base zeromq py3-zmq python3-dev libffi-dev openssl-dev && \
    if [ ! -e /usr/bin/python ]; then ln -sf python3 /usr/bin/python ; fi && \
    python3 -m ensurepip && \
    rm -r /usr/lib/python*/ensurepip && \
    pip3 install --no-cache --upgrade pip setuptools wheel && \
    if [ ! -e /usr/bin/pip ]; then ln -s pip3 /usr/bin/pip ; fi && \
    pip3 install jupyterlab pycellbase && \
    chown -R $CELLBASE_USER:$CELLBASE_USER /opt/cellbase

EXPOSE 8888

USER $CELLBASE_USER
#ENTRYPOINT ["./bin/cellbase-admin.sh", "server", "--start"]