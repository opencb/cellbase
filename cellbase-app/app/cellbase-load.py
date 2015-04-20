#!/usr/bin/python

import os
import argparse
import logging
import re

parser = argparse.ArgumentParser(prog="cellbase-load.py")
parser.add_argument("-i", "--indir", help="A species path to a cellbase-app data folder", required=True)
parser.add_argument("--host", help="Database to install [localhost]")
parser.add_argument("--port", help="Database to install [27017]")
parser.add_argument("-d", "--database", help="Database to install", required=True)
parser.add_argument("-u", "--user", help="Database to install")
parser.add_argument("-p", "--password", help="Database to install")
parser.add_argument("-c", "--mongodb-collections", help="CellBase MongoDB collections to be installed, files are validated to exist. ['info_stats', 'genome_sequence', 'core', 'regulation', 'variation', 'protein', 'conserved_region']")
parser.add_argument("--mongodb-indexes-dir", help="Ensembl version number, i.e. '3'. [3]")
args = parser.parse_args()


## Init logging configuration
logging.basicConfig(level=logging.DEBUG)

###########################################################
## Validating and saving arguments
###########################################################
## Required arguments do not need any check
indir = args.indir
database = args.database

## Setting other optional arguments
host = 'localhost'
if args.host is not None:
    host = args.host

user = ''
if args.user is not None:
    user = args.user

password = ''
if args.password is not None:
    password = args.password

port = '27017'
if args.port is not None:
    port = args.port

mongodb_collections = ['genome_info', 'genome_sequence', 'conserved_region', 'gene', 'regulation', 'variation', 'clinvar',
                       'protein', 'protein_protein_interaction', 'protein_function_prediction']
if args.mongodb_collections is not None:
    mongodb_collections = args.mongodb_collections.split(',')

mongodb_indexes_dir = ''
if args.mongodb_indexes_dir is not None:
    mongodb_indexes_dir = args.mongodb_indexes_dir


def import_collection(user, password, database, collection, coll_file_json):
    if os.path.exists(coll_file_json+".gz"):
        os.system("gunzip "+coll_file_json+".gz")

    if os.path.exists(coll_file_json):
        if user != "" and password != "":
            mongoimport_cmd = "mongoimport --host {0} -u {1} -p {2} --directoryperdb -d {3} -c {4} --file {5}".format(host, user, password, database, collection, coll_file_json)
        else:
            mongoimport_cmd = "mongoimport --host {0} --directoryperdb -d {1} -c {2} --file {3}".format(host, database, collection, coll_file_json)
        logging.info("Importing: "+mongoimport_cmd)
        os.system(mongoimport_cmd)
    else:
        print "File '"+coll_file_json+"' does not exist"
        return False

    if os.path.exists(coll_file_json):
        os.system("gzip "+coll_file_json)

    return True

def create_index(user, password, database, index):
    if os.path.exists(index):
        if user != "" and password != "":
            mongo_index_cmd = "mongo --host {0} -u {1} -p {2} {3} {4}".format(host, user, password, database, index)
        else:
            mongo_index_cmd = "mongo --host {0} {1} {2}".format(host, database, index)
        logging.info("Creating indexes: "+mongo_index_cmd)
        os.system(mongo_index_cmd)
    else:
        print "Index File '"+index+"' does not exist"


for collection in mongodb_collections:
    print "Processing '"+collection+"' collection"

    if collection == 'genome_info':
        coll_file_json = indir+"/sequence/"+collection+".json"
        result = import_collection(user, password, database, collection, coll_file_json)
        if not result:
            break

    if collection == 'genome_sequence':
        coll_file_json = indir+"/"+collection+".json"
        result = import_collection(user, password, database, collection, coll_file_json)
        if result:
            coll_file_index = mongodb_indexes_dir+"/genome_sequence-indexes.js"
            create_index(user, password, database, coll_file_index)
        else:
            break

    if collection == 'gene':
        coll_file_json = indir+"/"+collection+".json"
        result = import_collection(user, password, database, collection, coll_file_json)
        if result:
            coll_file_index = mongodb_indexes_dir+"/gene-indexes.js"
            create_index(user, password, database, coll_file_index)
        else:
            break

    if collection == 'variation':
        ## we data first all files starting with "variation_chr"
        variation_files = [f for f in os.listdir(indir) if re.match(r'variation_chr*', f)]
        for variation_file in variation_files:
            result = import_collection(user, password, database, collection, indir+"/"+variation_file.replace(".gz", ""))
            result = result and True

        if result:
            coll_file_index = mongodb_indexes_dir+"/variation-indexes.js"
            create_index(user, password, database, coll_file_index)
        else:
            break


