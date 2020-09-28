  #!/usr/bin/env bash

# Variables defined in main script
# BASEDIR
# PRGDIR
# JAVA_OPTS

MONITOR_AGENT=""
# TODO is there any way to NOT add this the CLI?
if [ -e "${BASEDIR}/monitor/dd-java-agent.jar" ]; then
    MONITOR_AGENT="-javaagent:${BASEDIR}/monitor/dd-java-agent.jar"
fi

JAVA_HEAP="8192m"
CELLBASE_LOG_DIR=${CELLBASE_LOG_DIR:"./logs"}
CELLBASE_LOG_LEVEL=${CELLBASE_LOG_LEVEL:"INFO"}
CELLBASE_LOG_OUPUT=${CELLBASE_LOG_OUTPUT:"console"}

CELLBASE_LOG_CONFIG="log4j2.console.xml"
if [ CELLBASE_LOG_OUPUT = "file" ]; then
  CELLBASE_LOG_CONFIG="log4j2.file.xml"
fi

#Set log4j properties file
export JAVA_OPTS="${JAVA_OPTS} -Dlog4j.configurationFile=file:${BASEDIR}/conf/${CELLBASE_LOG_CONFIG}"
export JAVA_OPTS="${JAVA_OPTS} -Dcellbase.log.level=${CELLBASE_LOG_LEVEL}"
export JAVA_OPTS="${JAVA_OPTS} ${MONITOR_AGENT}"
export JAVA_OPTS="${JAVA_OPTS} -Dfile.encoding=UTF-8"
export JAVA_OPTS="${JAVA_OPTS} -Xms256m -Xmx${JAVA_HEAP}"

if [ -n "$CELLBASE_LOG_DIR" ]; then
    export JAVA_OPTS="${JAVA_OPTS} -Dcellbase.log.dir=${CELLBASE_LOG_DIR}"
fi