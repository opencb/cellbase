#!/bin/sh

MONGODB_VERSION = 4.2 (check $1)

# public key
wget -qO - https://www.mongodb.org/static/pgp/server-$MONGODB_VERSION.asc | sudo apt-key add -

# add repo
echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/$MONGODB_VERSION multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-$MONGODB_VERSION.list

sudo apt-get update

# install mongo
sudo apt-get install -y mongodb-org

sudo service mongod start
