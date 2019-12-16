#!/bin/bash
set -e

# run this script in the root CellBase directory, next to the ./build directory
# otherwise the paths to the scripts, docker files and build directory

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
    echo " ** Script expects to be run in the root CellBase directory **  "
    exit 1
}

if [[ -z "$1" ]]; then
  display_help
fi

if [ -z "$2" ]; then
  echo "tag is required"
  display_help
fi

## set tag
TAG="$2"

build () {
  echo "****************************"
  echo "Building cellbase-base ..."
  echo "***************************"
  docker build -t opencb/cellbase-base:$TAG   -f cellbase-app/app/cloud/docker/cellbase-base/Dockerfile .

  echo "***************************"
  echo "Building cellbase-rest ..."
  echo "***************************"
  docker build -t opencb/cellbase-rest:$TAG   -f cellbase-app/app/cloud/docker/cellbase-rest/Dockerfile  . --build-arg TAG=$TAG

  echo "***************************"
  echo "Building cellbase-build ..."
  echo "***************************"
  docker build -t opencb/cellbase-build:$TAG  -f cellbase-app/app/cloud/docker/cellbase-build/Dockerfile .
}

if [ $1 = "build" ]; then
  build
fi

if [ $1 = "push" ]; then
  build
  echo "******************************"
  echo "Pushing images to DockerHub..."
  echo "******************************"
  docker push opencb/cellbase-base:$TAG
  docker push opencb/cellbase-rest:$TAG
  docker push opencb/cellbase-build:$TAG
fi
