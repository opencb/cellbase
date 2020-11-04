#!/usr/bin/env bash

# Variables defined in main script
# BASEDIR
# PRGDIR
# JAVA_OPTS

MONITOR_AGENT=""
## TODO We must make sure we load any existing JAR file, only one can exist.
if [ -e "${BASEDIR}/monitor/dd-java-agent.jar" ]; then
    MONITOR_AGENT="-javaagent:${BASEDIR}/monitor/dd-java-agent.jar"
fi

JAVA_HEAP="16384m"
CELLBASE_LOG_DIR=${CELLBASE_LOG_DIR}
CELLBASE_LOG_LEVEL=${CELLBASE_LOG_LEVEL:-"INFO"}
CELLBASE_LOG_CONFIG="log4j2.file.xml"

#Set log4j properties file
export JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configurationFile=${BASEDIR}/${CELLBASE_LOG_CONFIG}"
export JAVA_OPTS="${JAVA_OPTS} -Dcellbase.log.level=${CELLBASE_LOG_LEVEL}"
export JAVA_OPTS="${JAVA_OPTS} ${MONITOR_AGENT}"
export JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8"
export JAVA_OPTS="${JAVA_OPTS} -Xms256m -Xmx${JAVA_HEAP}"

if [ -n "$CELLBASE_LOG_DIR" ]; then
    export JAVA_OPTS="${JAVA_OPTS} -Dcellbase.log.dir=${CELLBASE_LOG_DIR}"
fi
