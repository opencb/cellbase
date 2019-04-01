# Building and running Cellbase in Docker

## Build Cellbase

To build cellbase run the following command from the root of the repository:

```
docker run -it --rm \
    -v "$PWD":/src \
    -w /src maven:3.5.3-jdk-8 \
    mvn clean install  \
    -DskipTests
```

## Build Cellbase image

This includes the cellbase build, but not a tomcat installation.

Run this command from the root of the repository:

```
docker build -t cellbase -f  ./cellbase-app/app/docker/cellbase/Dockerfile .
```

## Build Cellbase with tomcat image

Adds tomcat and healthcheck.

```
docker build -t cellbase-app  ./cellbase-app/app/docker/cellbase-app/
```   

## Run Cellbase server

The command below runs cellbase and mounts `configuration.json` from the specified path. This allows configuration to be persisted outside of the container and also shared between containers.

```
docker run --name cellbase-app-test --mount type=bind,src=/pathto/build/configuration.json,dst=/opt/cellbase/configuration.json -d -p 8080:8080 cellbase-app
```

Alterantively the following environment variables can be used to configure the database connection. They will override what is present in configuration.json.

```
CELLBASE_DATABASES_MONGODB_HOST
CELLBASE_DATABASES_MONGODB_USER
CELLBASE_DATABASES_MONGODB_PASSWORD
CELLBASE_DATABASES_MONGODB_OPTIONS_* 
CELLBASE_DATABASES_MONGODB_OPTIONS_AUTHENTICATION_DATABASE
CELLBASE_DATABASES_MONGODB_OPTIONS_REPLICA_SET
CELLBASE_DATABASES_MONGODB_OPTIONS_READ_PREFERENCE
CELLBASE_DATABASES_MONGODB_OPTIONS_CONNECTIONS_PER_HOST
CELLBASE_DATABASES_MONGODB_OPTIONS_SSL_ENABLED
```

and can be used as follows with docker run:

```
docker run --name cellbase-app-test -e CELLBASE_DATABASES_MONGODB_HOST="monogodnsaddress" -e CELLBASE_DATABASES_MONGODB_USER="mongouser" -e CELLBASE_DATABASES_MONGODB_PASSWORD="monogpassword" -e CELLBASE_DATABASES_MONGODB_OPTIONS_SSL_ENABLED="true" -d -p 8080:8080 cellbase-app
```