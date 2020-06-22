#!/usr/bin/env python3

import argparse
import os
import requests
import sys
import json
import pathlib
from pathlib import Path


## Configure command-line options
parser = argparse.ArgumentParser()
parser.add_argument('action', help="Action to execute", choices=["build", "push", "delete"], default="build")
parser.add_argument('--images', help="comma separated list of images to be made, e.g. tomcat,base,rest,python,builder", default="base,rest,python")
parser.add_argument('--tag', help="the tag for this code, e.g. v5.0.0")
parser.add_argument('--build-folder', help="the location of the build folder, if not default location")
parser.add_argument('--username', help="credentials for dockerhub (REQUIRED if deleting from DockerHub)")
parser.add_argument('--password', help="credentials for dockerhub (REQUIRED if deleting from DockerHub)")

## Some ANSI colors to print shell output
shell_colors = {
    'red': '\033[91m',
    'green': '\033[92m',
    'blue': '\033[94m',
    'magenta': '\033[95m',
    'bold': '\033[1m',
    'reset': '\033[0m'
}

def error(message):
    sys.stderr.write(shell_colors['red'] + 'ERROR: %s\n' % message + shell_colors['reset'])
    sys.exit(2)


def run(command):
    print(shell_colors['bold'] + command + shell_colors['reset'])
    code = os.system(command)
    if code != 0:
        error("Error executing: " + command)


def print_header(str):
    print(shell_colors['magenta'] + "*************************************************" + shell_colors['reset'])
    print(shell_colors['magenta'] + str + shell_colors['reset'])
    print(shell_colors['magenta'] + "*************************************************" + shell_colors['reset'])


# def login(loginRequired=False):
#     if args.username is None or args.password is None:
#         if loginRequired:
#             error("Username and password are required")
#         else:
#             return
#
#     code = os.system("docker login -u " + args.username + " --password " + args.password)
#     if code != 0:
#         error("Error executing: docker login")


def build():
    print_header('Building docker images: ' + ', '.join(images))
    for image in images:
        print()
        print(shell_colors['blue'] + "Building opencb/cellbase-" + image + ":" + tag + " ..." + shell_colors['reset'])
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
        print(shell_colors['blue'] + "Pushing opencb/cellbase-" + image + ":latest" + shell_colors['reset'])
        run("docker tag opencb/cellbase-" + image + ":" + tag + " opencb/cellbase-" + image + ":latest")
        run("docker push opencb/cellbase-" + image + ":latest")


def push():
    print_header('Pushing to DockerHub: ' + ', '.join(images))
    for i in images:
        print()
        print(shell_colors['blue'] + "Pushing opencb/cellbase-" + i + ":" + tag + " ..." + shell_colors['reset'])
        run("docker push opencb/cellbase-" + i + ":" + tag)
        tag_latest(i)


def delete():
    print_header('Deleting from DockerHub: ' + ', '.join(images))
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
        print()
        print(shell_colors['blue'] + 'Deleting image on Docker hub for opencb/cellbase-' + i + ':' + tag + shell_colors['reset'])
        headers = {
            'Authorization': 'JWT ' + json_response["token"]
        }
        requests.delete('https://hub.docker.com/v2/repositories/opencb/cellbase-' + i + '/tags/' + tag + '/', headers=headers)


## Parse command-line parameters and init basedir, tag and build_folder
args = parser.parse_args()

# 1. init basedir: root of the cellbase repo
basedir = str(Path(__file__).resolve().parents[2])

# 2. init tag: set tag to default value if not set
if args.tag is not None:
    tag = args.tag
else:
    stream = os.popen(basedir + "/bin/cellbase-admin.sh 2>&1 | grep Version | sed 's/ //g' | cut -d ':' -f 2")
    tag = stream.read()
    tag = tag.rstrip()

# 3. init build_folder: set build folder to default value if not set
if args.build_folder is not None:
    build_folder = args.build_folder
else:
    build_folder = basedir

if not os.path.isdir(build_folder):
    error("Build folder does not exist: " + build_folder)

# if not os.path.isdir(build_folder + "/libs") or not os.path.isdir(build_folder + "/conf") or not os.path.isdir(build_folder + "/bin"):
#     error("Not a build folder: " + build_folder)

# 4. init images: get a list with all images
if args.images is None:
    images = ["base", "rest", "python"]
else:
    images = args.images.split(",")


## Execute the action
if args.action == "build":
    # login(loginRequired=False)
    build()
elif args.action == "push":
    # login(loginRequired=False)
    build()
    push()
elif args.action == "delete":
    delete()
else:
    error("Unknown action: " + args.action)