#!/usr/bin/python

import os
import argparse
import logging
import urllib
import re
import json
import string

## CLI arguments parser
parser = argparse.ArgumentParser(prog="cellbase-installer.py")
parser.add_argument("-s", "--species", help="A species name, no default species given, two formats i.e. 'Homo sapiens','mus_musculus'. []")
parser.add_argument("-d", "--database", help="Comma separated list of names, i.e. 'Homo sapiens,mus_musculus'. Argument 'all' downloads of configured species. [Homo sapiens]")

parser.add_argument("--species-info-file", help="JSON configuration file with name and assemblies for all species in CellBase []")
parser.add_argument("-c", "--mongodb-collections", help="CellBase MongoDB collections to be installed, files are validated to exist. ['info_stats', 'genome_sequence', 'core', 'regulation', 'variation', 'protein', 'conserved_region']")
parser.add_argument("--mongodb-scripts-dir", help="Ensembl version number, i.e. '3'. [3]")
parser.add_argument("-v", "--cellbase-version", help="Ensembl version number, i.e. '3'. [3]")
parser.add_argument("--create-user", help="Ensembl version number, i.e. '3'. [3]")
parser.add_argument("--create-indices", help="Ensembl version number, i.e. '3'. [3]")
parser.add_argument("-i", "--indir", help="Output directory for downloaded data, a folder with the species name will be created, i.e. '/tmp'. [/tmp]")
parser.add_argument("-o", "--outdir", help="Output directory for downloaded data, a folder with the species name will be created, i.e. '/tmp'. [/tmp]")
args = parser.parse_args()


## Init logging configuration
logging.basicConfig(level=logging.DEBUG)

###########################################################
## Validating and saving arguments
###########################################################
species = ''
if args.species is not None:
    species = args.species
        
database = ''
if args.database is not None:
    database = args.database
    
species_info_file = ''
if args.species_info_file is not None:
    species_info_file = args.species_info_file
    
mongodb_collections = ['info_stats', 'genome_sequence', 'core', 'regulation', 'variation', 'mutation', 'protein', 'conserved_region']
if args.mongodb_collections is not None:
    mongodb_collections = args.mongodb_collections.split(',')

mongodb_scripts_dir = ''
if args.mongodb_scripts_dir is not None:
    mongodb_scripts_dir = args.mongodb_scripts_dir

cellbase_version = '3'
if args.cellbase_version is not None:
    cellbase_version = args.cellbase_version

create_user = '1'
if args.create_user is not None:
    create_user = args.create_user

create_indices = '1'
if args.create_indices is not None:
    create_indices = args.create_indices

indir = ''
if args.indir is not None:
    indir = args.indir

outdir = '/tmp'
if args.outdir is not None:
    outdir = args.outdir

## Validating parameters values
if species == '' and database == '':
    logging.error("Either '--species' or '--database' parameter MUST be provided")
    exit()

if species != '' and database != '':
    logging.error("Only ONE of '--species' and '--database' parameters MUST be provided")
    exit()
    
if mongodb_scripts_dir == '':
    logging.error("'--mongodb-scripts-dir' parameter MUST be provided")
    exit()

if indir == '':
    logging.error("'--indir' parameter MUST be provided")
    exit()
###########################################################
###########################################################


if species != '':
    logging.info("Processing species \'"+species+"\'")
    sp_obj = {}

    ## Loading species info with the assembly
    ## from species_the info.json file.
    if os.path.exists(species_info_file):
        f1 = open(species_info_file)
        text = f1.read()
        f1.close()
        species_info = json.loads(text)
    else:
        logging.error("'--species-info-file' not provided")
        exit()    

    ## Looking up the Species Object in JSON data
    ## Species are capitalized and converted to scientific name
    ## If they are already in scientific name this line has no effect
    sp = species.capitalize().replace('_', ' ')
    for phylo in species_info['items']:
        for j in phylo['items']:
            if j['text'] == sp:
                sp_obj = j

    logging.debug(sp_obj)
    
    ## Generating database name given the species, version and assembly
    ## Database name consist of first letter and species name
    ## followed of 'cdb', cellbase version and the digits from genome assembly, 
    ## ie: Homo sapiens for cellbase  version '3' and 
    ## assembly GRCh37.p10 is converted to 'hsapiens_cdb_v3_3710'
    species_arr = sp.split(" ")

    # if(pair.length < 3){
    #     name = (pair[0].substring(0,1)+pair[1]).toLowerCase();
    # }else{
    #     name = (pair[0].substring(0,1)+pair[1]+pair[pair.length-1].replaceAll("[/_().-]", "")).toLowerCase();
    # }
    if len(species_arr) < 3:
        database_sp_name = species_arr[0].lower()[0]+species_arr[1]
    else:
        database_sp_name = (species_arr[0]+species_arr[1]+re.sub('[/_().-]','',species_arr[-1])).lower()
    # database_assembly = re.sub('[a-z._]', '', sp_obj["assembly"].lower(), 0)
    ## if not digits exist then assembly is the lower case
    # if database_assembly == '':
    #     database_assembly = sp_obj["assembly"].lower()
    # database = database_sp_name+"_cdb_v"+cellbase_version+"_"+database_assembly
    database = database_sp_name+"_cdb_v"+cellbase_version

    logging.debug(database)

    for collection in mongodb_collections:
        coll_file_json = indir+"/"+collection+".json"
        if os.path.exists(coll_file_json):
            mongoimport_cmd = "mongoimport -u root -p nuez.nosql --authenticationDatabase admin --directoryperdb -d {0} -c {1} --file {2}".format(database, collection, coll_file_json)
            logging.debug(mongoimport_cmd)
            os.system(mongoimport_cmd)
            if create_indices == '1' or create_indices == 'yes' or create_indices == 'true':
                create_indices_script_file = mongodb_scripts_dir+"/{0}-indexes.js".format(collection)
                if os.path.exists(create_indices_script_file):
                    mongo_index_cmd = "mongo -u root -p nuez.nosql --authenticationDatabase admin {0} {1}".format(database, create_indices_script_file)
                    logging.debug(mongo_index_cmd)
                    os.system(mongo_index_cmd)

    if create_user == '1' or create_user == 'yes' or create_user == 'true':
        create_user_script_file = mongodb_scripts_dir+"/create-biouser.js"
        if os.path.exists(create_user_script_file):
            mongo_biouser_cmd = "mongo -u root -p nuez.nosql --authenticationDatabase admin {0} {1}".format(database, create_user_script_file)
            logging.debug(mongo_biouser_cmd)
            os.system(mongo_biouser_cmd)
