#!/usr/bin/python

import os
import argparse
import logging
import urllib
import re
import json

## CLI arguments parser
parser = argparse.ArgumentParser(prog="cellbase-builder.py")
parser.add_argument("-s", "--species", help="Comma separated list of scientific names, i.e. 'Homo sapiens,Mus musculus'. Argument 'all' downloads of configured species. [Homo sapiens]")
parser.add_argument("-v", "--ensembl-version", help="Ensembl version number, i.e. '71'. [71]")
parser.add_argument("-i", "--indir", help="Output directory for downloaded data, a folder with the species name will be created, i.e. '/tmp'. [/tmp]")
parser.add_argument("-o", "--outdir", help="Output directory for downloaded data, a folder with the species name will be created, i.e. '/tmp'. [/tmp]")
parser.add_argument("-u", "--ensembl-host", default='ftp://ftp.ensembl.org/pub/', help="Ensembl host, i.e. 'ftp://ftp.ensembl.org/pub/'. [ftp://ftp.ensembl.org/pub/]")
args = parser.parse_args()


logging.basicConfig(level=logging.DEBUG)

## Loading chromosomes info by species
## from species_info.json file, it's mandatory!
f1 = open('../genome-fetcher/species_info.json')
text = f1.read()
f1.close()
species_info = json.loads(text)
# print species_info

###########################################################
## Validating and saving arguments
###########################################################
species = ['Homo sapiens']
if args.species is not None:
    if args.species == 'all':
        species = []
        for phylo in species_info['items']:
            for j in phylo['items']:
                species.append(j['text'])
    else:
        species = args.species.split(',')
        
indir = '/tmp'
if args.indir is not None:
    indir = args.indir
    
outdir = '/tmp'
if args.outdir is not None:
    outdir = args.outdir

###########################################################


for sp in species:
    logging.info("Processing species \'"+sp+"\'")
    
    ## preparing some variables
    sp_short = sp.lower().replace('.', '').replace('=', '').replace(')', '').replace('-', ' ').replace('(', ' ').replace('/', ' ').replace('  ', ' ').replace(' ', '_')
    logging.debug(sp_short)
    sp_folder = outdir+"/{0}".format(sp_short)

    ## Creating the directory for the species
    ## this creates 'species' folder
    if not os.path.exists(sp_folder):
        os.makedirs(sp_folder)
    
    cmd = "./genome_info.pl --species '{0}' > {1}/{2}/genome_info.json".format(sp, outdir, sp_short)
    logging.debug(cmd)
    os.system(cmd)
    
    cmd = "./gene_extra_info_cellbase.pl --species '{0}' --outdir {1}/{2}/gene/".format(sp, outdir, sp_short)
    logging.debug(cmd)
    os.system(cmd)