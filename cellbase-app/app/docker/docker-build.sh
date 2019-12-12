#!/bin/bash

## Get cellbase 'build' folder
if [ -z "$2" ]; then
  BASEDIR=`dirname $0`
  BUILD_FOLDER=`cd "$BASEDIR/.." >/dev/null; pwd`
else
  BUILD_FOLDER=$2
fi

build () {
  echo "Building images ..."
  docker build -t cellbase-base:next   -f $BUILD_FOLDER/docker/cellbase-base/Dockerfile   $BUILD_FOLDER
  docker build -t cellbase-rest:next   -f $BUILD_FOLDER/docker/cellbase-rest/Dockerfile   $BUILD_FOLDER
  docker build -t cellbase-build:next  -f $BUILD_FOLDER/docker/cellbase-build/Dockerfile  $BUILD_FOLDER
}

if [ $1 = "build" ]; then
  build
fi

if [ $1 = "push" ]; then
  build

  echo "Pushing images to DockerHub..."

fi

