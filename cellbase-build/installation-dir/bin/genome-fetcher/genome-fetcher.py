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
# parser.add_argument("-v", "--ensembl-version", help="Ensembl version number, i.e. '73'. [73]")
parser.add_argument("-o", "--outdir", help="Output directory for downloaded data, a folder with the species name will be created, i.e. '/tmp'. [/tmp]")
parser.add_argument("-u", "--ensembl-host", default='ftp://ftp.ensembl.org/pub/', help="Ensembl host, i.e. 'ftp://ftp.ensembl.org/pub/'. [ftp://ftp.ensembl.org/pub/]")
parser.add_argument("-c", "--compress", help="Whether downloaded data must be compressed [true]")
parser.add_argument("-q", "--sequence", help="Whether downloaded data must be compressed [true]")
parser.add_argument("-g", "--gene", help="Whether downloaded data must be compressed [true]")
parser.add_argument("-v", "--variation", help="Whether downloaded data must be compressed [true]")
parser.add_argument("-r", "--regulation", help="Whether downloaded data must be compressed [true]")
args = parser.parse_args()


## Loading chromosomes info by species
## from species_info.json file, it's mandatory!
f1 = open('species.json')
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

# ensembl_version = '73'
# if args.ensembl_version is not None:
#     ensembl_version = args.ensembl_version

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
# logging.debug(ensembl_version)
logging.debug(ensembl_host)

for sp in species:
    logging.info("Processing species \'"+sp+"\'")
    
    sp_obj = {}
    ## Looking up the Species Object in JSON data
    for phylo in species_info['items']:
        for j in phylo['items']:
            if j['text'] == sp:
                sp_obj = j
                sp_obj['phylo'] = phylo['text']
                sp_obj['databaseHost'] = phylo['databaseHost']
                sp_obj['databasePort'] = phylo['databasePort']

    logging.debug(sp_obj)

    ## preparing some variables
    sp_short = sp.lower().replace('.', '').replace('=', '').replace(')', '').replace('-', ' ').replace('(', ' ').replace('/', ' ').replace('  ', ' ').replace(' ', '_')
    logging.debug(sp_short)
    sp_folder = outdir+"/{0}".format(sp_short)
    sequence_folder = outdir+"/{0}/sequence".format(sp_short)
    gene_folder = outdir+"/{0}/gene".format(sp_short)
    variation_folder = outdir+"/{0}/variation".format(sp_short)
    regulation_folder = outdir+"/{0}/regulation".format(sp_short)

    ## Creating the directory for the species
    ## this creates 'species' folder
    if not os.path.exists(sp_folder):
        os.makedirs(sp_folder)

    if args.sequence is not None and args.sequence == '1':
        if not os.path.exists(sequence_folder):
            os.makedirs(sequence_folder)
        ## preparing URL for download
        #url_seq = sp_obj['sequence_url']+"{0}/dna/*.dna.toplevel.fa.gz".format(sp_short)
        url_seq = sp_obj['sequence_url']+"{0}/dna/*.dna.primary_assembly.fa.gz".format(sp_short)
        logging.debug(sp_obj['assembly'])
        outfile = sequence_folder+"/"+sp_short.capitalize() + ".{0}".format(sp_obj['assembly'])+".fa.gz"
        logging.debug(outfile)
        command = "wget --tries=10 " + url_seq +" -O '"+outfile+"' -o "+outfile+".log"
        logging.debug(command)
        os.system(command)
        cmd = "./genome_info.pl --species '{0}' -o {1}/genome_info.json".format(sp, sequence_folder)
        logging.debug(cmd)
        os.system(cmd)
        # for i in sp_obj['chromosomes']:
        #     outfile = seq_folder+"/chrom_"+i+".fa.gz"
        #     command = "wget --tries=10 " + url_seq+"/*.dna.chromosome."+i+".fa.gz -O '"+outfile+"' -o "+outfile+".log"
        #     logging.debug(command)
        #     os.system(command)

    if args.gene is not None and args.gene == '1':
        if not os.path.exists(gene_folder):
            os.makedirs(gene_folder)
        ## preparing URL for download
        # We have to change the param 'fasta' by 'gtf' in the URL
        url_gtf = sp_obj['sequence_url'].replace("fasta", "gtf")+"{0}".format(sp_short)
        logging.debug(url_gtf)
        outfile = gene_folder+"/"+sp_short+".gtf.gz"
        command = "wget --tries=10 " + url_gtf+"/*.gtf.gz -O '"+outfile+"' -o "+outfile+".log"
        logging.debug(command)
        os.system(command)
        if sp_obj['phylo'] is not 'Bacteria':
            # Gene description
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select g.stable_id, g.description from gene g\" > " + gene_folder+"/description.txt")
            # Gene Xrefs
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select t.stable_id, x.display_label, edb.db_name, edb.db_display_name from gene g, transcript t, object_xref ox, dependent_xref dx, xref x, external_db edb    where ox.object_xref_id=dx.object_xref_id and dx.master_xref_id=x.xref_id and x.external_db_id=edb.external_db_id and ox.ensembl_object_type='Gene' and ox.ensembl_id=g.gene_id and g.gene_id=t.gene_id\" > " + gene_folder+"/xrefs_dup.txt")
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select t.stable_id, x.display_label, edb.db_name, edb.db_display_name from gene g, transcript t, object_xref ox, dependent_xref dx, xref x, external_db edb    where ox.object_xref_id=dx.object_xref_id and dx.dependent_xref_id=x.xref_id and x.external_db_id=edb.external_db_id and ox.ensembl_object_type='Gene' and ox.ensembl_id=g.gene_id and g.gene_id=t.gene_id\" >> " + gene_folder+"/xrefs_dup.txt")
            # Transcript Xrefs
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select t.stable_id, x.display_label, edb.db_name, edb.db_display_name from transcript t, object_xref ox, dependent_xref dx, xref x, external_db edb    where ox.object_xref_id=dx.object_xref_id and dx.master_xref_id=x.xref_id and x.external_db_id=edb.external_db_id and ox.ensembl_object_type='Transcript' and ox.ensembl_id=t.transcript_id\" >> " + gene_folder+"/xrefs_dup.txt")
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select t.stable_id, x.display_label, edb.db_name, edb.db_display_name from transcript t, object_xref ox, dependent_xref dx, xref x, external_db edb    where ox.object_xref_id=dx.object_xref_id and dx.dependent_xref_id=x.xref_id and x.external_db_id=edb.external_db_id and ox.ensembl_object_type='Transcript' and ox.ensembl_id=t.transcript_id\" >> " + gene_folder+"/xrefs_dup.txt")
            # Translation Xrefs
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select tr.stable_id, x.display_label, edb.db_name, edb.db_display_name from translation t, transcript tr, object_xref ox, dependent_xref dx, xref x, external_db edb    where ox.object_xref_id=dx.object_xref_id and dx.master_xref_id=x.xref_id and x.external_db_id=edb.external_db_id and ox.ensembl_object_type='Translation' and ox.ensembl_id=t.translation_id and t.transcript_id=tr.transcript_id\" >> " + gene_folder+"/xrefs_dup.txt")
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select tr.stable_id, x.display_label, edb.db_name, edb.db_display_name from translation t, transcript tr, object_xref ox, dependent_xref dx, xref x, external_db edb    where ox.object_xref_id=dx.object_xref_id and dx.dependent_xref_id=x.xref_id and x.external_db_id=edb.external_db_id and ox.ensembl_object_type='Translation' and ox.ensembl_id=t.translation_id and t.transcript_id=tr.transcript_id\" >> " + gene_folder+"/xrefs.txt")
            # Ensembl gene and transcript ID
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select t.stable_id, g.stable_id, 'ensembl_gene', 'Ensembl Gene' from transcript t, gene g where t.gene_id=g.gene_id\" >> " + gene_folder+"/xrefs_dup.txt")
            os.system("mysql -u anonymous -h " + sp_obj['databaseHost'] + " -P " +sp_obj['databasePort']+ " --skip-column-names " + sp_obj['database'] + " -e \"select t.stable_id, t.stable_id, 'ensembl_transcript', 'Ensembl Transcript' from transcript t\" >> " + gene_folder+"/xrefs_dup.txt")
            # Remove duplicates
            os.system("sort " + gene_folder+"/xrefs_dup.txt | uniq > " + gene_folder+"/xrefs.txt")
            os.system("rm " + gene_folder+"/xrefs_dup.txt")


    ## Download VARIATION data
    if args.variation is not None and args.variation == '1':
        if not os.path.exists(variation_folder):
            os.makedirs(variation_folder)
        variation_files =  ['variation.txt.gz', 'variation_feature.txt.gz', 'transcript_variation.txt.gz',
                            'variation_synonym.txt.gz', 'seq_region.txt.gz', 'source.txt.gz', 'attrib.txt.gz',
                            'attrib_type.txt.gz', 'seq_region.txt.gz', 'structural_variation_feature.txt.gz',
                            'study.txt.gz', 'phenotype.txt.gz', 'phenotype_feature.txt.gz',
                            'phenotype_feature_attrib.txt.gz', 'motif_feature_variation.txt.gz', 'genotype_code.txt.gz',
                            'allele_code.txt.gz', 'population_genotype.txt.gz', 'population.txt.gz', 'allele.txt.gz']
        ## preparing URL for download
        if 'variation_url' in sp_obj and sp_obj['variation_url'] != '':
            variation_url = sp_obj['variation_url']
            for file in variation_files:
                outfile = variation_folder+"/"+file
                command = "wget --tries=10 " + variation_url+"/"+file+" -O '"+outfile+"' -o "+outfile+".log"
                logging.debug(command)
                os.system(command)

    if args.regulation is not None and args.regulation == '1':
        if not os.path.exists(regulation_folder):
            os.makedirs(regulation_folder)
        regulation_files =  ['AnnotatedFeatures.gff.gz', 'MotifFeatures.gff.gz', 'RegulatoryFeatures_MultiCell.gff.gz']
        ## preparing URL for download
        if 'regulation_url' in sp_obj and sp_obj['regulation_url'] != '':
            regulation_url = sp_obj['regulation_url']+sp_short
            for file in regulation_files:
                outfile = regulation_folder+"/"+file
                command = "wget --tries=10 " + regulation_url+"/"+file+" -O '"+outfile+"' -o "+outfile+".log"
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
