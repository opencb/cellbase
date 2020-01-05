#!/bin/bash
set -e

# By default run this script from CellBase 'build' directory

#########################
# The command line help #
#########################
display_help() {
    echo "Usage: {build|push} [tag] [build_folder]" >&2
    echo
    echo "   build            Build the three CellBase docker files"
    echo "   push             Publish the CellBase docker files on DockerHub"
    echo "   [tag]            Name of tag on GitHub"
    echo "   [build_folder]   Path of CellBase build folder [optional]"
    echo
    echo " ** Script expects to be run in the root CellBase directory **  "
    exit 1
}

## check mandatory parameters 'action' and 'tag' exist
if [ -z "$1" ]; then
  echo "Error: action is required"
  display_help
else
  ACTION=$1
fi

## get optional 'tag' value from cellbase-admin.sh help output
if [ -z "$2" ]; then
  BASEDIR=`dirname $0`
  TAG=`$BASEDIR/../../bin/cellbase-admin.sh 2>&1 | grep "Version" | sed 's/ //g' | cut -d ':' -f 2`
else
  TAG=$2
fi

## get optional 'build' folder
if [ -z "$3" ]; then
  BASEDIR=`dirname $0`
  BUILD_FOLDER=`cd "$BASEDIR/../.." >/dev/null; pwd`
else
  BUILD_FOLDER=$3
fi

build () {
  echo "****************************"
  echo "Building cellbase-base ..."
  echo "***************************"
  docker build -t opencb/cellbase-base:$TAG     -f $BUILD_FOLDER/cloud/docker/cellbase-base/Dockerfile $BUILD_FOLDER

  echo "***************************"
  echo "Building cellbase-rest ..."
  echo "***************************"
  docker build -t opencb/cellbase-rest:$TAG     -f $BUILD_FOLDER/cloud/docker/cellbase-rest/Dockerfile  --build-arg TAG=$TAG $BUILD_FOLDER

#  echo "***************************"
#  echo "Building cellbase-python ..."
#  echo "***************************"
#  docker build -t opencb/cellbase-python:$TAG   -f $BUILD_FOLDER/cloud/docker/cellbase-python/Dockerfile --build-arg TAG=$TAG $BUILD_FOLDER

  echo "***************************"
  echo "Building cellbase-build ..."
  echo "***************************"
#  docker build -t opencb/cellbase-build:$TAG  -f $BUILD_FOLDER/cloud/docker/cellbase-build/Dockerfile $BUILD_FOLDER
}

if [ $ACTION = "build" ]; then
  build
fi

if [ $ACTION = "push" ]; then
  build

  echo "******************************"
  echo "Pushing images to DockerHub..."
  echo "******************************"
  docker push opencb/cellbase-base:$TAG
  docker push opencb/cellbase-rest:$TAG
#  docker push opencb/cellbase-python:$TAG
#  docker push opencb/cellbase-build:$TAG
fi
