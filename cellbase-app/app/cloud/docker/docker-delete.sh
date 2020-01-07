#!/bin/bash
set -e

## List containing the Docker images to be built or pushed
IMAGES="base rest python"

## The command line help #
display_help() {
    echo "Usage: {username}" >&2
    echo
    echo "   username   credentials for dockerhub"
    echo "   password   credentials for dockerhub"
    echo "   tag   tag to delete"
    echo
    exit 1
}


if [ -z "$1" ]; then
  echo "Error: username is required"
  display_help
else
  USERNAME=$1
fi

if [ -z "$2" ]; then
  echo "Error: password is required"
  display_help
else
  PASSWORD=$2
fi

if [ -z "$3" ]; then
  echo "Error: tag is required"
  display_help
else
  TAG=$3
fi

TOKEN=$(curl -s -H "Content-Type: application/json" -X POST -d '{"username": "'$USERNAME'", "password": "'$PASSWORD'"}' https://hub.docker.com/v2/users/login/ | jq -r .token)

for i in $IMAGES; do
    echo "Deleting cellbase-$i ..."
    curl 'https://hub.docker.com/v2/repositories/opencb/cellbase-${i}/tags/${TAG}/' -X DELETE -H "Authorization: JWT ${TOKEN}"
done
