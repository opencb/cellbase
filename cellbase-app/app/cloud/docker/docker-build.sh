#!/bin/bash

## GET
TAG="$2"

## Get cellbase 'build' folder
if [ -z "$3" ]; then
  BASEDIR=`dirname $0`
  BUILD_FOLDER=`cd "$BASEDIR/../.." >/dev/null; pwd`
else
  BUILD_FOLDER=$3
fi

build () {
  echo "Building images ..."
  docker build -t cellbase-base:$TAG   -f $BUILD_FOLDER/cloud/docker/cellbase-base/Dockerfile   $BUILD_FOLDER
  docker build -t cellbase-rest:$TAG   -f $BUILD_FOLDER/cloud/docker/cellbase-rest/Dockerfile   $BUILD_FOLDER
  docker build -t cellbase-build:$TAG  -f $BUILD_FOLDER/cloud/docker/cellbase-build/Dockerfile  $BUILD_FOLDER
}

if [ $1 = "build" ]; then
  build
fi

if [ $1 = "push" ]; then
  build

  echo "Pushing images to DockerHub..."

fi

