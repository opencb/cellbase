#!/bin/bash
set -e

## List containing the Docker images to be built or pushed
IMAGES="base rest python"


## The command line help #
display_help() {
    echo "Usage: {build|push} [tag] [build_folder]" >&2
    echo
    echo "   build            Build the three CellBase docker files"
    echo "   push             Publish the CellBase docker files on DockerHub"
    echo "   [tag]            Name of tag on GitHub [optional]"
    echo "   [build_folder]   Path of CellBase build folder [optional]"
    echo
    exit 1
}

## Check mandatory parameters 'action' and 'tag' exist
if [ -z "$1" ]; then
  echo "Error: action is required"
  display_help
else
  ACTION=$1
fi

## Get optional 'tag' value from cellbase-admin.sh help output
if [ -z "$2" ]; then
  BASEDIR=`dirname $0`
  TAG=`$BASEDIR/../../bin/cellbase-admin.sh 2>&1 | grep "Version" | sed 's/ //g' | cut -d ':' -f 2`
else
  TAG=$2
fi

## Get optional 'build' folder
if [ -z "$3" ]; then
  BASEDIR=`dirname $0`
  BUILD_FOLDER=`cd "$BASEDIR/../.." >/dev/null; pwd`
else
  BUILD_FOLDER=$3
fi

build () {
  for i in $IMAGES; do
    echo "Building cellbase-$i ..."
    if [ $i = "base" ]; then
      docker build -t opencb/cellbase-$i:$TAG -f $BUILD_FOLDER/cloud/docker/cellbase-$i/Dockerfile $BUILD_FOLDER
    else
      docker build -t opencb/cellbase-$i:$TAG -f $BUILD_FOLDER/cloud/docker/cellbase-$i/Dockerfile --build-arg TAG=$TAG $BUILD_FOLDER
    fi
    echo ""
  done
}

if [ $ACTION = "build" ]; then
  build
fi

if [ $ACTION = "push" ]; then
  build

  

  echo ""
  echo "Pushing images to DockerHub..."
  for i in $IMAGES; do
    docker push opencb/cellbase-$i:$TAG

    ALL_TAGS=$(wget -q https://registry.hub.docker.com/v1/repositories/opencb/cellbase-$i/tags -O -  | sed -e 's/[][]//g' -e 's/"//g' -e 's/ //g' -e 's/latest//g' | tr '}' '\n'  | awk -F: '{print $3}')
    LATEST_TAG=$(echo $ALL_TAGS | cut -d' ' -f1 | sort -h | head)

    # add 'latest' tag if appropriate
    if [ $TAG >= $LATEST_TAG ]; then
        docker tag opencb/cellbase-$i:$TAG opencb/cellbase-$i:latest
        docker push opencb/cellbase-$i:latest
    fi

  done
fi
