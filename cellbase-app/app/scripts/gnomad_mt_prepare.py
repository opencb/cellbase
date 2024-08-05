#!/usr/bin/env python3

#  Copyright 2015-2020 OpenCB
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import argparse
import os
import requests
import sys
import json
import pathlib
from pathlib import Path


## Configure command-line options
parser = argparse.ArgumentParser()
parser.add_argument('-i', help="VCF file", required=True)


## Parse command-line parameters and init basedir, tag and build_folder
args = parser.parse_args()
print(args.i)

if os.path.isfile(args.i) == False:
    print("no existe")


# Opening file
vcf_file = open(args.i, 'r')
count = 0

# Using for loop
print("Using for loop")
for line in vcf_file:
    count += 1
    if not line.startswith("#"):
        line = line.strip()
        cols = line.split("\t")
        print(line)
        info_cols = cols[7].split(";")
        var = [x for x in info_cols if x.startswith("AN=")]
        print("{}".format(var))


# Closing files
vcf_file.close()