#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;
use Digest::MD5 qw(md5 md5_hex md5_base64);

use DB_CONFIG;


my $species = 'Homo sapiens';
my $outdir = "/tmp/$species";
my $verbose = '0';
my $help = '0';

####################################################################
## Parsing command line options ####################################
####################################################################
# USAGE: ./core.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'outdir=s' => \$outdir, 
            'ensembl-libs=s' => \$ENSEMBL_LIBS, 'ensembl-registry=s' => \$ENSEMBL_REGISTRY,
            'ensembl-host=s' => \$ENSEMBL_HOST, 'ensembl-port=s' => \$ENSEMBL_PORT,
            'ensembl-user=s' => \$ENSEMBL_USER, 'ensembl-pass=s' => \$ENSEMBL_PASS,
            'verbose' => \$verbose, 'help' => \$help);

## Checking help parameter
print_usage() if $help;

## Printing parameters
print_parameters() if $verbose;

## Checking outdir parameter exist, otherwise create it
if(-d $outdir){
    print "Writing files to directory '$outdir'...\n" if $verbose;
}else{
    print "Directory '$outdir' does not exist, creating directory...\n" if $verbose;
    mkdir $outdir or die "Couldn't create dir: [$outdir] ($!)";
}

####################################################################
## Ensembl APIs ####################################################
####################################################################
## loading ensembl libraries
use lib "$ENSEMBL_LIBS/ensembl/modules";
use lib "$ENSEMBL_LIBS/ensembl-variation/modules";
use lib "$ENSEMBL_LIBS/ensembl-compara/modules";
use lib "$ENSEMBL_LIBS/ensembl-functgenomics/modules";
use lib "$ENSEMBL_LIBS/bioperl-live";

## creating ensembl adaptors
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Variation::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Compara::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Funcgen::DBSQL::DBAdaptor;

## loading the registry with the adaptors 
Bio::EnsEMBL::Registry->load_all("$ENSEMBL_REGISTRY");
####################################################################

my $slice_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Slice");
my $transcript_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Transcript");
my $prot_function_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "variation", "ProteinFunctionPredictionMatrix");

#my @aa_code = split(",", "A,R,N,D,C,E,Q,G,H,I,L,K,M,F,P,S,T,W,Y,V");
my @aa_code = split(",", "A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,W,Y,V");

#my $md5 = Digest::MD5->new;
#$md5->add("MAAPWRRWPTGLLAVLRPLLTCRPLQGTTLQRDVLLFEHDRGRFFTILGLFCAGQGVFWASMAVAAVSRPPVPVQPLDAEVPNRGPFDLRSALWRYGLAVGCGAIGALVLGAGLLFSLRSVRSVVLRAGGQQVTLTTHAPFGLGAHFTVPLKQVSCMAHRGEVPAMLPLKVKGRRFYFLLDKTGHFPNTKLFDNTVGAYRSL");
#my $digest = $md5->hexdigest;
#my $polyphen2 = $prot_function_adaptor->fetch_polyphen_predictions_by_translation_md5($digest);
#my $sift = $prot_function_adaptor->fetch_sift_predictions_by_translation_md5($digest);
#print $digest."\n";
#my @arr = $polyphen2->get_prediction(1, 'G');
#print $arr[0]."\n";
#for my $aa(@arr) {
#    print $aa."\n";
#}
#print join("=", $polyphen2->get_prediction(1, 'G'))."\n";


my ($translation, $seq, $md5seq, @preds);
#my @transcripts = @{$transcript_adaptor->fetch_all_by_biotype('protein_coding')};

my @all_chroms = @{$slice_adaptor->fetch_all('chromosome')};
my @chrom_ids = ();
foreach my $chrom(@all_chroms) {
	my @transcripts = @{$chrom->get_all_Transcripts()};
	open(FILE, ">output_chr_".$chrom->seq_region_name) || die "error opening file\n";
	print @transcripts." transcripts fetched!\n";
	foreach my $trans(@transcripts) {
	#	$trans = $transcript_adaptor->fetch_by_stable_id($trans_id);
	    
		if($trans->biotype eq 'protein_coding') {
	        print FILE $trans->stable_id."\t";
	        
	        $translation = $trans->translation();
	        $seq = $translation->seq();
	        my $md5 = Digest::MD5->new;
	        $md5->add($seq);
	        $md5seq = $md5->hexdigest;
	        print FILE $md5seq."\t";
	        
	        my $polyphen2 = $prot_function_adaptor->fetch_polyphen_predictions_by_translation_md5($md5seq);
	        my $sift = $prot_function_adaptor->fetch_sift_predictions_by_translation_md5($md5seq);
	        
	#        if(defined $polyphen2) {
	#            print "\t".$polyphen2->get_prediction(2, 'G');    	
	#        }else {
	#        	print " - polyphen2 no ta \n";
	#        }
	#        if(defined $sift) {
	#        	print " - ".$sift->get_prediction(2, 'G')."\n";
	#        }else {
	#        	print " - sift no ta \n";
	#        }
	
	        
	#		for(my $i=1; $i<=scalar(split("", $seq)); $i++) {
			for(my $i=1; $i<=length($seq); $i++) {
				foreach (my $j=0; $j < @aa_code; $j++) {
					if(defined $polyphen2) {
						@preds = $polyphen2->get_prediction($i, $aa_code[$j]);
	    				if($preds[0] eq 'probably damaging') {
	    				    print FILE "prob_dam";	
	    				}else{
	    					if($preds[0] eq 'possibly damaging') {
	                            print FILE "poss_dam";   
	                        }else {
	                        	if($preds[0] eq 'benign') {
	                                print FILE "ben";   
	                            }else {
	                            	print FILE "unk";
	                            }
	                        }
	    				}
	    				print FILE "=".$preds[1];
					}
					if($j < @aa_code-1) {
	                    print FILE ",";					
					}
				}
	            if($i < length($seq)) {
	                print FILE ";";              
	            }
			}
			print "\t";
			for(my $i=1; $i<=length($seq); $i++) {
	            foreach (my $j=0; $j < @aa_code; $j++) {
	            	if(defined $sift) {
	            		@preds = $sift->get_prediction($i, $aa_code[$j]);
	            		if($preds[0] eq 'tolerated') {
	                        print FILE "tol";   
	                    }else{
	                        print FILE "del";
	                    }
	            		print FILE "=".$preds[1];
	            	}
	                if($j < @aa_code-1) {
	                    print FILE ",";                  
	                }
	            }
	            if($i < length($seq)) {
	                print FILE ";";            	
	            }
	        }
	        print FILE "\n";
		}
	}
	close(FILE);
	## GZip output to save space in Amazon AWS
	exec("gzip output_chr_".$chrom->seq_region_name);
}

