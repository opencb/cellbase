#!/usr/bin/env bash

# Variables defined in main script
# BASEDIR
# PRGDIR
# JAVA_OPTS
# CLASSPATH_PREFIX

MONITOR_AGENT=""
if [ -e "monitor/dd-java-agent.jar" ]; then
    MONITOR_AGENT="-javaagent monitor/dd-java-agent.jar"
fi

JAVA_HEAP="2048m"
LOG_CONFIG="log4j2.xml"
#CELLBASE_LOG_FILE=${CELLBASE_LOG_FILE:-`grep logFile ` +  ${BASEDIR} + `/conf/configuration.yml | cut -d ':' -f 2`}
#CELLBASE_LOG_LEVEL=${CELLBASE_LOG_LEVEL:-`grep logLevel ` +  ${BASEDIR} + `/conf/configuration.yml | cut -d ':' -f 2`}

CELLBASE_LOG_FILE="cellbase.json"
CELLBASE_LOG_LEVEL="info"

if [ $PRG = "cellbase-admin.sh" ]; then
    JAVA_HEAP="8192m"
fi

#Set log4j properties file
export JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configuration=file:${BASEDIR}/conf/${LOG_CONFIG}"
export JAVA_OPTS="${JAVA_OPTS} -Dcellbase.log.file=${CELLBASE_LOG_FILE} -Dcellbase.log.level=${CELLBASE_LOG_LEVEL}"
export JAVA_OPTS="${JAVA_OPTS} ${MONITOR_AGENT}"
export JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8"
export JAVA_OPTS="${JAVA_OPTS} -Xms256m -Xmx${JAVA_HEAP}"

