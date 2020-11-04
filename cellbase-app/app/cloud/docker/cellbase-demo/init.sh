#!/bin/bash

# ------------ Start MongoDB ----------------
mongod --dbpath /data/cellbase/mongodb --replSet rs0  &
status=$?
if [ $status -ne 0 ]; then
  echo "Failed to start mongoDB: $status"
  exit $status
fi
sleep 10

mongo /opt/scripts/mongo-init.js
sleep 20

echo 'demo' | /opt/cellbase/bin/cellbase-admin.sh server --start
