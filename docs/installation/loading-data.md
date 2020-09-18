# Loading Data

Follow the steps below to load data into your CellBase instance. You must have all prerequistes installed, see \[Service configuration \] for details.

## Downloading the data

Run this command to download all the data:

$ ./build/bin/cellbase-admin.sh download -d gene -s hsapiens

See \[Download Sources\] for the details on all the data that's available to download.

## Building the data

Run this command to download all the data:

```text
$ ./build/bin/cellbase-admin.sh build -d gene -s hsapiens
```

See \[Building the CellBase database\] for the details on how to build.

## Loading the data

Run this command to download all the data:

```text
$ ./build/bin/cellbase-admin.sh load -d gene -s hsapiens
```

See \[Load Data\] for the details on all the data that's available to download.

## Web services

CellBase has two options:

### REST API

`$ ./build/bin/cellbase-admin.sh server --start` View the API here: [http://localhost:9090/cellbase-5.0.0-SNAPSHOT/webservices/](http://localhost:9090/cellbase-5.0.0-SNAPSHOT/webservices/)

### Tomcat

Make sure Tomcat is running, then copy the WAR file the build has created for you into Tomcat's webapps directory:

`cp ./build/cellbase-5.0.0-SNAPSHOT.war ~/apache-tomcat-8.5.47/webapps/`

You should see the generated API docs here:

[http://localhost:8080/cellbase-5.0.0-SNAPSHOT/webservices/](http://localhost:8080/cellbase-5.0.0-SNAPSHOT/webservices/)

