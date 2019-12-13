#!/bin/bash

#########################
# The command line help #
#########################
display_help() {
    echo "Usage: {build|push} tag_name [build_folder]" >&2
    echo
    echo "   build           Build the three CellBase docker files"
    echo "   push            Publish the CellBase docker files on DockerHub"
    echo "   tag_name        Name of tag on GitHub"
    echo "   build_folder    (optional) absolute path to location of build directory"
    echo
    exit 1
}

if [[ -z "$1" ]]; then
  display_help
fi

if [ -z "$2" ]; then
  echo "tag is required"
  display_help
fi

## tag
TAG="$2"

build () {
  echo "****************************"
  echo "Building cellbase-base ..."
  echo "***************************"
  docker build -t cellbase-base:$TAG   -f cellbase-app/app/cloud/docker/cellbase-base/Dockerfile .

  echo "***************************"
  echo "Building cellbase-rest ..."
  echo "***************************"
  docker build -t cellbase-rest:$TAG   -f cellbase-app/app/cloud/docker/cellbase-rest/Dockerfile  . --build-arg TAG=$TAG

  echo "***************************"
  echo "Building cellbase-build ..."
  echo "***************************"
  docker build -t cellbase-build:$TAG  -f cellbase-app/app/cloud/docker/cellbase-build/Dockerfile .
}

if [ $1 = "build" ]; then
  build
fi

if [ $1 = "push" ]; then
  build
  echo "Pushing images to DockerHub..."

fi
