#!/usr/bin/env python

import argparse
import os
import requests
import sys
import json
import pathlib
from pathlib import Path


def error(message):
    sys.stderr.write('error: %s\n' % message)
    # parser.print_help()
    sys.exit(2)


def run(command):
    print(command)
    code = os.system(command)
    if code != 0:
        error("Error executing: " + command)


def login(loginRequired=False):
    if args.username is None or args.password is None:
        if loginRequired:
            error("Username and password are required")
        else:
            return

    code = os.system("docker login -u " + args.username + " --password " + args.password)
    if code != 0:
        error("Error executing: docker login")


def build():
    print("Building docker images")
    for image in images:
        print("*********************************************")
        print("Building opencb/cellbase-" + image + ":" + tag)
        print("*********************************************")
        print(
            "docker build -t opencb/cellbase-" + image + ":" + tag + " -f " + build_folder + "/cloud/docker/cellbase-" + image + "/Dockerfile " + build_folder)
        if image == "base":
            run("docker build -t opencb/cellbase-" + image + ":" + tag + " -f " + build_folder + "/cloud/docker/cellbase-" + image + "/Dockerfile " + build_folder)
        else:
            run("docker build -t opencb/cellbase-" + image + ":" + tag + " -f " + build_folder + "/cloud/docker/cellbase-" + image + "/Dockerfile --build-arg TAG=" + tag + " " + build_folder)


def tag_latest(image):
    latest_tag = os.popen(("curl -s https://registry.hub.docker.com/v1/repositories/opencb/cellbase-" + image + "/tags"
                           + " | jq -r .[].name"
                           + " | grep -v latest"
                           + " | sort -h"
                           + " | head"))
    if tag >= latest_tag.read():
        print("*********************************************")
        print("Pushing opencb/cellbase-" + image + ":latest")
        print("*********************************************")
        run("docker tag opencb/cellbase-" + image + ":" + tag + " opencb/cellbase-" + image + ":latest")
        run("docker push opencb/cellbase-" + image + ":latest")


def push():
    print("Pushing images to Docker hub")
    for i in images:
        print("*********************************************")
        print("Pushing opencb/cellbase-" + i + ":" + tag)
        print("*********************************************")
        run("docker push opencb/cellbase-" + i + ":" + tag)
        tag_latest(i)


def delete():
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


parser = argparse.ArgumentParser()

# build, push or delete
parser.add_argument('action', help="Action to execute", choices=["build", "push", "delete"], default="build")

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

if not os.path.isdir(build_folder):
    error("Build folder does not exist: " + build_folder)

if not os.path.isdir(build_folder + "/libs") or not os.path.isdir(build_folder + "/conf") or not os.path.isdir(build_folder + "/bin"):
    error("Not a build folder: " + build_folder)

# get a list with all images
if args.images is None:
    images = ["base", "rest", "python"]
else:
    images = args.images.split(",")

if args.action == "build":
    login(loginRequired=False)
    build()
elif args.action == "push":
    login(loginRequired=False)
    build()
    push()
elif args.action == "delete":
    delete()
else:
    error("Unknown action: " + args.action)