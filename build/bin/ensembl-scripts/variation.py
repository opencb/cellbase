#!/usr/bin/python

import os
import argparse

def executedSortedGziped(filename,query):
	if verbose:
		print(mysql_command_line + query + chromClause + "\" > " + outDir + filename);
	os.system(mysql_command_line + query + chromClause + "\" > " + outDir + filename);	
	
	if verbose:
		print("(head -n 1; sort -k 1,1) < " + outDir + filename + " | uniq > " + outDir + filename+".sort ");
	os.system("(head -n 1; sort -k 1,1) < " + outDir + filename + " | uniq > " + outDir + filename+".sort ");

	if verbose:
		print("gzip " + outDir + filename+".sort");
	os.system("gzip " + outDir + filename+".sort");

	if verbose:
		print("rm " + outDir + filename);
	os.system("rm " + outDir + filename);
	
def sortedToRealName(specie):
	return{
		'human': "homo_sapiens", 
		'mouse': "mus_musculus",
		'rat': "rattus_norvegicus",
		}.get(specie,"specie not found")

	
homo_sapiensChromosomes=['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', 'X', 'Y', 'MT'];
mus_musculusChromosomes=['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', 'X', 'Y', 'MT'];
rattus_norvegicusChromosomes=['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', 'X', 'MT'];

#familyGenderQuerys=['variation','transcript_variation','phenotype','xref','regulatory','frequency_allele','frequency_genotype'];
#true if you want to execute that query
#false if you doesnt want to execute that query

release = 70;
assembly = 37;


parser = argparse.ArgumentParser(prog="variation");
parser.add_argument("-o", "--outdir", action="store", dest="outDirectory", help="input directory to save the results");
parser.add_argument("-c", "--chromosome",type=int,nargs='+', action="store", dest="chromosome", help="select the chromosomes if you want to data download");
parser.add_argument("-a", "--assembly",type=int, action="store", dest="assembly", help="");
parser.add_argument("-r", "--release",type=int, action="store", dest="release", help="");
parser.add_argument("-s", "--species", action="store", dest="species", help="the species if you want selected")
parser.add_argument('-v', action='store_true', default=False, dest='verbose', help='Verbose')
parser.add_argument("--host", action="store", dest="host", help="database host");
parser.add_argument("-u", "--user", action="store", dest="user", help="user or database");
parser.add_argument("--database", action="store", dest="database", help="name of database");
parser.add_argument("-p", "--password", action="store", dest="password", help="password of database");
parser.add_argument("-P", "--port", action="store", dest="port", help="port of database");
parser.add_argument("--ip", action="store", dest="ip", help="ip of database");

parser.set_defaults(verbose = False);
parser.set_defaults(host="localhost");
parser.set_defaults(user="anonymous");

parser.set_defaults(outDirectory="/tmp/variation_snp");
parser.set_defaults(password="");
parser.set_defaults(port="3306");
parser.set_defaults(ip="127.0.0.1");

args = parser.parse_args();
species = args.species;

print "--->" + species;

if(species == ""):
	species = sortedToRealName("human");
else:
	species = sortedToRealName(args.species);

if(args.chromosome == None):
	chromosome = eval(species+"Chromosomes");
else:
	chromosome = args.chromosome;
	
if(args.release != None):
	release = args.release;

print args.assembly;

if(args.assembly != None):
	assembly = args.assembly;
	
if(args.database == None):
	database = species+"_variation_"+str(release)+"_"+str(assembly);
else:
	database = args.database;
		

print species;
print chromosome;
print database;

host = args.host;
ip = args.ip;
port = args.port;

user = args.user;
password = args.password;


outDirectory = args.outDirectory + "/" + database;
verbose = args.verbose;

mysql_command_line = "mysql -u " + user + " -h " + host + " -P " + port + " --database=" + database + " -e \""; #no password




variation = "select v.name, sq.name, vf.seq_region_start, vf.seq_region_end, vf.seq_region_strand, vf.allele_string, v.ancestral_allele, vf.map_weight, vf.validation_status, vf.consequence_types, vf.somatic, vf.minor_allele, vf.minor_allele_freq, vf.minor_allele_count from variation v, variation_feature vf, seq_region sq where v.variation_id=vf.variation_id and vf.seq_region_id=sq.seq_region_id and sq.name=";
transcript_variation = "select vf.variation_name, tv.feature_stable_id, tv.allele_string, tv.somatic, tv.consequence_types, tv.cds_start, tv.cds_end, tv.cdna_start,tv.cdna_end,tv.translation_start,tv.translation_end, tv.distance_to_transcript, tv.codon_allele_string, tv.pep_allele_string, tv.hgvs_genomic, tv.hgvs_transcript, tv.hgvs_protein, tv.polyphen_prediction, tv.polyphen_score, tv.sift_prediction, tv.sift_score from transcript_variation tv, variation_feature vf, seq_region sq where tv.variation_feature_id=vf.variation_feature_id and vf.seq_region_id=sq.seq_region_id and sq.name=";
phenotype = "select v.name, va.associated_variant_risk_allele, va.risk_allele_freq_in_controls, va.p_value, va.associated_gene, ph.name, ph.description, sr.name, sr.version,  st.name, st.study_type, st.url, st.description from variation_annotation va, variation v, phenotype ph, study st, source sr, variation_feature vf, seq_region sq where va.variation_id=v.variation_id and va.phenotype_id=ph.phenotype_id and va.study_id=st.study_id and st.source_id=st.source_id and v.variation_id=vf.variation_id and vf.seq_region_id=sq.seq_region_id and sq.name=";
xref = "select v.name as snp_id, vs.name as syn_id, sr.name as source, sr.version from variation_synonym vs, variation v, source sr, variation_feature vf, seq_region sq where vs.variation_id=v.variation_id and vs.source_id=sr.source_id and vs.variation_id=vf.variation_id and vf.seq_region_id=sq.seq_region_id and sq.name=";
regulatory = "select vf.variation_name, mfv.feature_stable_id, mfv.consequence_types, sq.name, mf.seq_region_start, mf.seq_region_end, mf.seq_region_strand, mf.display_label, mf.score from motif_feature_variation mfv, "+species+"_funcgen_"+str(release)+"_"+str(assembly)+".motif_feature mf, variation_feature vf, seq_region sq where mfv.motif_feature_id=mf.motif_feature_id and mfv.variation_feature_id=vf.variation_feature_id and vf.seq_region_id=sq.seq_region_id and sq.name="; 
frequency_allele = "select v.name, ac.allele, a.frequency, a.count, s.name as sample from allele a, allele_code ac, variation v, sample s, variation_feature vf, seq_region sq where a.allele_code_id=ac.allele_code_id and a.variation_id=v.variation_id and a.sample_id=s.sample_id and v.variation_id=vf.variation_id and vf.seq_region_id=sq.seq_region_id and sq.name=";
frequency_genotype = "select v.name, ac1.allele as allele1, ac2.allele as allele2, pg.frequency, pg.count, s.name from population_genotype pg, genotype_code gc1, genotype_code gc2, allele_code ac1, allele_code ac2, variation v, variation_feature vf, seq_region sq, sample s where v.variation_id=pg.variation_id and pg.genotype_code_id=gc1.genotype_code_id and gc1.allele_code_id=ac1.allele_code_id and gc1.haplotype_id=1 and pg.genotype_code_id=gc2.genotype_code_id and gc2.allele_code_id=ac2.allele_code_id and gc2.haplotype_id=2 and pg.sample_id=s.sample_id and v.variation_id=vf.variation_id and vf.seq_region_id=sq.seq_region_id and sq.name=";

select_test = "select * from table where id=";

outputfile_array = ['variation.txt','transcript_variation.txt','phenotype.txt','xref.txt','regulatory.txt','frequency_allele.txt','frequency_genotype.txt'];
query_array = [variation,transcript_variation,phenotype,xref,regulatory,frequency_allele,frequency_genotype];

for chromosomeNumber in chromosome:
	if not os.path.exists(outDirectory):
		os.makedirs(outDirectory);
	if not os.path.exists(os.path.join(outDirectory, "chromosome_" + str(chromosomeNumber))):
		os.makedirs(os.path.join(outDirectory, "chromosome_" + str(chromosomeNumber)));
	else:
		chromClause = "'" + str(chromosomeNumber) + "'";
		outDir = outDirectory + "/chromosome_" + str(chromosomeNumber) + "/";
		
		for i in range(len(query_array)):
			executedSortedGziped(outputfile_array[i],query_array[i]);
		
