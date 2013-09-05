#!/usr/bin/python

import os
import argparse
import logging
import urllib
import re
import json

## CLI arguments parser
parser = argparse.ArgumentParser(prog="genome-seq.py")
parser.add_argument("-s", "--species", help="Comma separated list of scientific names, i.e. 'Homo sapiens,Mus musculus'. Argument 'all' downloads of configured species. [Homo sapiens]")
parser.add_argument("-v", "--ensembl-version", help="Ensembl version number, i.e. '73'. [73]")
parser.add_argument("-o", "--outdir", help="Output directory for downloaded data, a folder with the species name will be created, i.e. '/tmp'. [/tmp]")
parser.add_argument("-u", "--ensembl-host", default='ftp://ftp.ensembl.org/pub/', help="Ensembl host, i.e. 'ftp://ftp.ensembl.org/pub/'. [ftp://ftp.ensembl.org/pub/]")
parser.add_argument("-c", "--compress", help="Whether downloaded data must be compressed [true]")
parser.add_argument("-q", "--seq", help="Whether downloaded data must be compressed [true]")
parser.add_argument("-g", "--gtf", help="Whether downloaded data must be compressed [true]")
args = parser.parse_args()


## Loading chromosomes info by species
## from species_info.json file, it's mandatory!
f1 = open('species_info.json')
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

ensembl_version = '73'
if args.ensembl_version is not None:
    ensembl_version = args.ensembl_version

# ftp://ftp.ensemblgenomes.org/pub/fungi
ensembl_host = 'ftp://ftp.ensembl.org/pub/'
if args.ensembl_host is not None:
    ensembl_host = args.ensembl_host
    
outdir = '/tmp'
if args.outdir is not None:
    outdir = args.outdir
###########################################################
###########################################################

logging.basicConfig(level=logging.DEBUG)
logging.debug(species)
logging.debug(ensembl_version)
logging.debug(ensembl_host)

for sp in species:
    logging.info("Processing species \'"+sp+"\'")
    
    sp_obj = {}
    ## Looking up the Species Object in JSON data
    for phylo in species_info['items']:
        for j in phylo['items']:
            if j['text'] == sp:
                sp_obj = j

    logging.debug(sp_obj)

    ## preparing some variables
    sp_short = sp.lower().replace(' ', '_')
    sp_folder = outdir+"/{0}".format(sp_short)
    seq_folder = outdir+"/{0}/sequence".format(sp_short)
    
    ## Creating the directory for the species
    ## this creates 'species' folder
    if not os.path.exists(sp_folder):
        os.makedirs(sp_folder)

    if args.seq is not None and args.seq == '1':
        if not os.path.exists(seq_folder):
            os.makedirs(seq_folder)
        ## preparing URL for download
        url_seq = sp_obj['url']+"fasta/{0}/dna".format(sp_short)
        logging.debug(url_seq)
        for i in sp_obj['chromosomes']:
            outfile = seq_folder+"/chrom_"+i+".fa.gz"
            command = "wget --tries=10 " + url_seq+"/*.dna.chromosome."+i+".fa.gz -O '"+outfile+"' -o "+outfile+".log"
            logging.debug(command)
            os.system(command)

    if args.gtf is not None and args.gtf == '1':
        ## preparing URL for download 
        url_gtf = sp_obj['url']+"gtf/{0}".format(sp_short)
        logging.debug(url_gtf)
        outfile = sp_folder+"/"+sp_short+".gtf.gz"
        command = "wget --tries=10 " + url_gtf+"/*.gtf.gz -O '"+outfile+"' -o "+outfile+".log"
        logging.debug(command)
        os.system(command)


    ## Download CHECKSUMS files to interrogate which files to download
#     urllib.urlretrieve(url_seq+"/CHECKSUMS", "CHECKSUMS")
# pattern = re.compile("\w*.dna.chromosome.(\w+).fa.gz")
# f = open('CHECKSUMS')
# for line in f:
#     fields = line.split()
#     m = pattern.search(fields[2])
#     if m:
#         print fields[2].rstrip('\n')
#         print m.group(1)
# 
# f.close()

# species_info = {}
# f = open('species_info.txt')
# for line in f:
#     if not line.startswith('#'):
#         fields = line.split('\t')
#         species_info[fields[0]] = {'chroms': fields[2].split(','), 'url': fields[3].rstrip()} 
# 
# f.close()
# logging.debug(species_info)
# print json.dumps(species_info)

# chromosomes = {}
# chromosomes['Homo sapiens'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Mus musculus'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', 'X', 'Y', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Rattus norvegicus'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Pan troglodytes'] = {'chroms': ['1', '2A', '2B', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22','X', 'Y', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Gorilla gorilla'] = {'chroms': ['1', '2a', '2b', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22','X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Pongo abelii'] = {'chroms': ['1', '2a', '2b', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22','X', 'Un', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Macaca mulatta'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Sus scrofa'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', 'X', 'Y', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Canis familiaris'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31', '32', '33', '34', '35', '36', '37', '38', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Equus caballus'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Oryctolagus cuniculus'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Gallus gallus'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '32', 'W', 'Z', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Bos taurus'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Felis catus'] = {'chroms': ['A1', 'A2', 'A3', 'B1', 'B2', 'B3', 'B4', 'C1', 'C2', 'D1', 'D2', 'D3', 'D4', 'E1', 'E2', 'E3', 'F1', 'F2', 'X', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Danio rerio'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', 'X', 'Y', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# chromosomes['Ciona intestinalis'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', 'MT'], 'url': 'ftp://ftp.ensembl.org/pub/release-71/'}
# ## Metazoa
# chromosomes['Drosophila melanogaster'] = {'chroms': ['2L', '2LHet', '2R', '2RHet', '3L', '3LHet', '3R', '3RHet', '4', 'U', 'Uextra', 'X', 'XHet', 'YHet', 'dmel_mitochondrion_genome'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/metazoa/release-18/'}
# chromosomes['Drosophila simulans'] = {'chroms': ['2L', '2R', '3L', '3R', '4', 'X'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/metazoa/release-18/'}
# chromosomes['Drosophila yakuba'] = {'chroms': ['2L', '2R', '3L', '3R', '4', 'chr2h', 'chr3h', 'chrXh', 'chrYh','X'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/metazoa/release-18/'}
# chromosomes['Anopheles gambiae'] = {'chroms': ['2L', '2LHet', '2R', '2RHet', '3L', '3LHet', '3R', '3RHet', '4', 'U', 'Uextra', 'X', 'XHet', 'YHet', 'dmel_mitochondrion_genome'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/metazoa/release-18/'}
# chromosomes['Caenorhabditis elegans'] = {'chroms': ['I', 'II', 'III', 'IV', 'V', 'X', 'MtDNA'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/metazoa/release-18/fasta/'}
# ## Fungi
# chromosomes['Saccharomyces cerevisiae'] = {'chroms': ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII', 'XIII', 'XIV', 'XV', 'XVI', 'Mito'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/fungi/release-18/'}
# chromosomes['Schizosaccharomyces pombe'] = {'chroms': ['AB325691', 'I', 'II', 'III', 'MT', 'MTR'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/fungi/release-18/'}
# chromosomes['Aspergillus fumigatus'] = {'chroms': ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'MT'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/fungi/release-18/'}
# chromosomes['Aspergillus niger'] = {'chroms': ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/fungi/release-18/'}
# chromosomes['Aspergillus nidulans'] = {'chroms': ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/fungi/release-18/'}
# chromosomes['Aspergillus oryzae'] = {'chroms': ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/fungi/release-18/'}
# ## Plants
# chromosomes['Arabidopsis thaliana'] = {'chroms': ['1', '2', '3', '4', '5', 'Mt', 'Pt'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# chromosomes['Arabidopsis lyrata'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# chromosomes['Brachypodium distachyon'] = {'chroms': ['1', '2', '3', '4', '5'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# chromosomes['Oryza sativa'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', 'Mt', 'Pt', 'Sy', 'Un'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# chromosomes['Glycine max'] = {'chroms': ['Gm01', 'Gm02', 'Gm03', 'Gm04', 'Gm05', 'Gm06', 'Gm07', 'Gm08', 'Gm09', 'Gm10', 'Gm11', 'Gm12', 'Gm13', 'Gm14', 'Gm15', 'Gm16', 'Gm17', 'Gm18', 'Gm19', 'Gm20'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# chromosomes['Vitis vinifera'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', 'Un'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# chromosomes['Zea mays'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'Mt', 'Pt'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/plants/release-18/'}
# ## Protist
# chromosomes['Plasmodium falciparum'] = {'chroms': ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/protists/release-18/'}
# chromosomes['Leishmania major'] = {'chroms': ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31', '32', '33', '34', '35', '36'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/protists/release-18/'}
# chromosomes['Dictyostelium discoideum'] = {'chroms': ['1', '2', '3', '4', '5', '6'], 'url': 'ftp://ftp.ensemblgenomes.org/pub/protists/release-18/'}

