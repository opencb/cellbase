FROM alpine/git AS Clone
ARG BRANCH_NAME="next"
WORKDIR /appl
RUN git clone -b ${BRANCH_NAME} --single-branch https://github.com/opencb/cellbase.git

FROM maven:3.6-jdk-8 AS Build
COPY --from=Clone /appl/cellbase /appl/cellbase
WORKDIR /appl/cellbase
RUN mvn clean install -DskipTests

FROM openjdk:8-jre-alpine
#ENV CELLBASE_HOME=/opt/cellbase
#ENV CELLBASE_CONFIG_DIR=${CELLBASE_HOME}/conf
COPY --from=Build /appl/cellbase/build /opt/cellbase
# make a cellbase user
ENTRYPOINT ["/opt/cellbase/bin/cellbase-admin.sh", "server", "--start"]
