#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;
use Digest::MD5 qw(md5 md5_hex md5_base64);
use JSON;

use DB_CONFIG;

my $species = 'Homo sapiens';
my $assembly = 'GRCh38';
my $outdir = "/ensembl-data/$species";
my $chrom = '22';
my $verbose = '0';
my $help = '0';

####################################################################
## Parsing command line options ####################################
####################################################################
# USAGE: ./protein_function_prediction.pl --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'assembly=s' => \$assembly,  'chrom|chromosome=s' => \$chrom, 'outdir=s' => \$outdir,
			'ensembl-registry=s' => \$ENSEMBL_REGISTRY, 'ensembl-host=s' => \$ENSEMBL_HOST, 'ensembl-port=s' => \$ENSEMBL_PORT,
			'ensembl-user=s' => \$ENSEMBL_USER, 'v|verbose' => \$verbose, 'h|help' => \$help);

## Printing parameters
print_parameters() if $verbose;

## Checking outdir parameter exist, otherwise create it
if (-d $outdir){
    print "Writing files to directory '$outdir'...\n" if $verbose;
} else{
    print "Directory '$outdir' does not exist, creating directory...\n" if $verbose;
    mkdir $outdir or die "Couldn't create dir: [$outdir] ($!)";
}

####################################################################
## Ensembl APIs ####################################################
####################################################################
## creating ensembl adaptors
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Variation::DBSQL::DBAdaptor;

if ($species eq "Homo sapiens" && $assembly eq "GRCh38") {
	print ("Human selected, assembly ".$assembly." selected, connecting to port ".$ENSEMBL_PORT."\n");
	Bio::EnsEMBL::Registry->load_registry_from_db(
		-host     => $ENSEMBL_HOST,
		-user     => $ENSEMBL_USER,
		-port     => $ENSEMBL_PORT,
		-verbose  => $verbose
	);
} else {
	print ("Human selected, assembly ".$assembly." no supported\n");
}
####################################################################

my $slice_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Slice");
# my $transcript_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Transcript");
my $prot_function_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "variation", "ProteinFunctionPredictionMatrix");

####################################################################
## CODIFICATIONS
####################################################################
my @aa_code = split(",", "A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,W,Y,V");
my %effect_code = ("probably damaging" => 0,
				   "possibly damaging" => 1,
				   "benign" => 2,
				   "unknown" => 3,
				   "tolerated" => 0,
				   "deleterious" => 1);
####################################################################

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


my ($translation, $seq, $md5seq, @preds, @all_predictions);
#my @transcripts = @{$transcript_adaptor->fetch_all_by_biotype('protein_coding')};

##################################################################
## selecting chromosomes	######################################
##################################################################
my @chromosomes;
if ($chrom eq 'all') {
	@chromosomes = @{$slice_adaptor->fetch_all('chromosome')};
} else {
	my @chr_ids = split(",", $chrom);
	foreach my $id(@chr_ids) {
		push(@chromosomes, $slice_adaptor->fetch_by_region('chromosome', $id));
	}
}
##################################################################

print "Retrieving Chromosomes: ";
foreach my $c (@chromosomes){
	print $c->seq_region_name().", "
}
print "\n";

#my @all_chroms = @{$slice_adaptor->fetch_all('chromosome')};
foreach my $chr(@chromosomes) {
	my @transcripts = @{$chr->get_all_Transcripts()};
	open(FILE, ">".$outdir."/prot_func_pred_chr_".$chr->seq_region_name.".json") || die "error opening file\n";
	print @transcripts." transcripts fetched!\n";
	foreach my $trans(@transcripts) {
		if($trans->biotype eq 'protein_coding') {
	        $translation = $trans->translation();
	        $seq = $translation->seq();
	        my $md5 = Digest::MD5->new;
	        $md5->add($seq);
	        $md5seq = $md5->hexdigest;

	        ## HASH ##
			my $effect = {};
	        $effect->{"transcriptId"} = $trans->stable_id;
	        $effect->{"checksum"} = $md5seq;
	        $effect->{"size"} = length($seq);

	        foreach my $u (@{ $trans->get_all_xrefs('Uniprot/SWISSPROT') }){
		        $effect->{"uniprotId"} = $u->display_id();
	        }

	        my $polyphen2 = $prot_function_adaptor->fetch_polyphen_predictions_by_translation_md5($md5seq);
			for(my $i=1; $i<=length($seq); $i++) {
				foreach (my $j=0; $j < @aa_code; $j++) {
					if(defined $polyphen2) {
						@preds = $polyphen2->get_prediction($i, $aa_code[$j]);
						$effect->{"aaPositions"}->{$i}->{$aa_code[$j]}->{"pe"} = $effect_code{$preds[0]};
						$effect->{"aaPositions"}->{$i}->{$aa_code[$j]}->{"ps"} = $preds[1];
					}
				}
			}

			my $sift = $prot_function_adaptor->fetch_sift_predictions_by_translation_md5($md5seq);
			for(my $i=1; $i<=length($seq); $i++) {
	            foreach (my $j=0; $j < @aa_code; $j++) {
	            	if(defined $sift) {
	            		@preds = $sift->get_prediction($i, $aa_code[$j]);
						$effect->{"aaPositions"}->{$i}->{$aa_code[$j]}->{"se"} = $effect_code{$preds[0]};
						$effect->{"aaPositions"}->{$i}->{$aa_code[$j]}->{"ss"} = $preds[1];
	            	}
	            }
	        }
			print FILE to_json($effect)."\n";
		}
	}
	close(FILE);

	## GZip output to save space in Amazon AWS
#	exec("gzip prot_func_pred_chr_".$chrom->seq_region_name);
}

sub print_parameters {
	print "Parameters: ";
	print "species: $species, chrom: $chrom, outdir: $outdir, ";
	print "ensembl-registry: $ENSEMBL_REGISTRY, ";
	print "ensembl-host: $ENSEMBL_HOST, ensembl-port: $ENSEMBL_PORT, ";
	print "ensembl-user: $ENSEMBL_USER, verbose: $verbose, help: $help";
	print "\n";
}