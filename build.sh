#!/bin/bash

## Check if 'build' folder exists to delete the content. If not it is created.
if [ -d build ]; then
    rm -rf build/*
else
    mkdir build
fi

## Copy all the binaries, dependencies and files
cp -r cellbase-app/app/* build/
cp -r cellbase-app/target/appassembler/* build/
cp cellbase-server/target/cellbase.war build/
cp README.md build/

tar -zcvf cellbase.tar.gz build

