#!/bin/bash
set -e

## The command line help #
display_help() {
    echo "Usage: {build|push|push-test}" >&2
    echo
    echo "   build            Build the three CellBase docker files"
    echo "   push             Publish the CellBase PyPI"
    echo "   push-test        Publish the CellBase PyPI Test"
#    echo "   [tag]            Name of tag on GitHub"
#    echo "   [build_folder]   Path of CellBase build folder [optional]"
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
#if [ -z "$2" ]; then
#  BASEDIR=`dirname $0`
#  TAG=`grep version setup.py | cut -d ':' -f 2 | tr \' \" | sed 's/[", ]//g'`
#else
#  TAG=$2
#fi

## get optional 'build' folder
#if [ -z "$2" ]; then
#  BASEDIR=`dirname $0`
#  BUILD_FOLDER=`cd "$BASEDIR/../.." >/dev/null; pwd`
#else
#  BUILD_FOLDER=$3
#fi

build () {
  echo "****************************"
  echo "Building PyPI package ..."
  echo "***************************"
  cd clients/python
  python3 -m pip install --user --upgrade setuptools wheel
  python3 setup.py sdist bdist_wheel
}

if [ $ACTION = "build" ]; then
  build
fi

if [ $ACTION = "push" ]; then
  build

  echo "******************************"
  echo "Pushing to PyPI..."
  echo "******************************"
  python3 -m pip install --user --upgrade twine
  python3 -m twine upload dist/*
fi

if [ $ACTION = "push-test" ]; then
  build

  echo "******************************"
  echo "Pushing to test PyPI ..."
  echo "******************************"
  python3 -m pip install --user --upgrade twine
  python3 -m twine upload --repository-url https://test.pypi.org/legacy/ dist/*
fi
