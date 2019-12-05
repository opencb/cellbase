FROM alpine/git AS Clone
ARG BRANCH_NAME="next"
WORKDIR /appl
RUN git clone -b ${BRANCH_NAME} --single-branch https://github.com/opencb/cellbase.git

FROM maven:3.6-jdk-8 AS Build
COPY --from=Clone /appl/cellbase /appl/cellbase
WORKDIR /appl/cellbase
RUN mvn clean install -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=Build /appl/cellbase/build /opt/cellbase
RUN adduser -D -u 1001 cellbase -h cellbase
ENTRYPOINT ["/opt/cellbase/bin/cellbase-admin.sh", "server", "--start"]
