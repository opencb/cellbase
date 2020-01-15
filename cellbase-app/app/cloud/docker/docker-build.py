#!/usr/bin/env python

import argparse
import os
import requests
import sys
import json
import pathlib
from pathlib import Path

parser = argparse.ArgumentParser()

# build, push or delete
parser.add_argument('action')

parser.add_argument('--images', help="comma separated list of images to be made, e.g. base,rest,python", default="base,rest,python")
parser.add_argument('--tag', help="the tag for this code, e.g. v5.0.0")
parser.add_argument('--build-folder', help="the location of the build folder, if not default location")
parser.add_argument('--username', help="credentials for dockerhub (REQUIRED if deleting from DockerHub)")
parser.add_argument('--password', help="credentials for dockerhub (REQUIRED if deleting from DockerHub)")

args = parser.parse_args()

# root of the cellbase repo
basedir = str(Path(__file__).resolve().parents[2])

# set tag to default value if not set
if args.tag is not None:
    tag = args.tag
else:
    stream = os.popen(basedir + "/build/bin/cellbase-admin.sh 2>&1 | grep Version | sed 's/ //g' | cut -d ':' -f 2")
    tag = stream.read()
    tag = tag.rstrip()

# set build folder to default value if not set
if args.build_folder is not None:
    build_folder = args.build_folder
else:
    build_folder = basedir

images = args.images.split(",")

def build():
    print("Building docker images")
    for image in images:
        print("*********************************************")
        print("Building opencb/cellbase-" + image + ":" + tag)
        print("*********************************************")
        print("docker build -t opencb/cellbase-" + image + ":" + tag + " -f " + build_folder + "/cloud/docker/cellbase-" + image + "/Dockerfile " + build_folder)
        if image == "base":
            os.system("docker build -t opencb/cellbase-" + image + ":" + tag + " -f " + build_folder + "/cloud/docker/cellbase-" + image + "/Dockerfile " + build_folder)
        else:
            os.system("docker build -t opencb/cellbase-" + image + ":" + tag + " -f " + build_folder + "/cloud/docker/cellbase-" + image + "/Dockerfile --build-arg TAG=" + tag + " " + build_folder)

def tag_latest(image):
    all_tags = os.popen("wget -q https://registry.hub.docker.com/v1/repositories/opencb/cellbase-" + image + "/tags -O - | sed -e 's/[][]//g' -e 's/\"//g' -e 's/[ ]//g' -e 's/latest//g' | tr '}' '\n' | awk -F: '{print $3}'")
    latest_tag = os.popen("echo " + all_tags.read() + " + | cut -d' ' -f1 | sort -h | head")
    if tag >= latest_tag.read():
        print("*********************************************")
        print("Pushing opencb/cellbase-" + i + ":latest")
        print("*********************************************")
        os.system("docker tag opencb/cellbase-" + image + ":" + tag + " opencb/cellbase-" + image + ":latest")
        os.system("docker push opencb/cellbase-" + image + ":latest")

if args.action == "build":
    build()

if args.action == "push":
    build()
    print("Pushing images to Docker hub")
    for i in images:
        print("*********************************************")
        print("Pushing opencb/cellbase-" + i + ":" + tag)
        print("*********************************************")
        os.system("docker push opencb/cellbase-" + i + ":" + tag)
        tag_latest(i)

def error(message):
    sys.stderr.write('error: %s\n' % message)
    #parser.print_help()
    sys.exit(2)

if args.action == "delete":
    if args.username is None or args.password is None:
        error("Username and password are required")
    headers = {
        'Content-Type': 'application/json',
    }
    data = '{"username": "' + args.username + '", "password": "' + args.password + '"}'
    response = requests.post('https://hub.docker.com/v2/users/login/', headers=headers, data=data)
    json_response = json.loads(response.content)
    if response.status_code != 200:
        error("dockerhub login failed")
    for i in images:
        print('Deleting image on Docker hub for opencb/cellbase-' + i + ':' + tag)
        headers = {
            'Authorization': 'JWT ' + json_response["token"]
        }
        requests.delete('https://hub.docker.com/v2/repositories/opencb/cellbase-' + i + '/tags/' + tag + '/', headers=headers)
