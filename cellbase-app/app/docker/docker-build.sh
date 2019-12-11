#!/bin/bash

BUILD_FOLDER=$2

build () {
  echo "Building images ..."
  docker build -t opencb/cellbase-base:next   -f $BUILD_FOLDER/docker/cellbase-base/Dockerfile   $BUILD_FOLDER
  docker build -t opencb/cellbase-rest:next   -f $BUILD_FOLDER/docker/cellbase-rest/Dockerfile   $BUILD_FOLDER
  docker build -t opencb/cellbase-build:next  -f $BUILD_FOLDER/docker/cellbase-build/Dockerfile  $BUILD_FOLDER
}

if [ $1 = "build" ]; then
  build
fi

if [ $1 = "push" ]; then
  build

  echo "Pushing images to DockerHub..."

fi

