#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;

use JSON;

use DB_CONFIG;


my $species = 'Homo sapiens';
my $transcript_file = 'Homo sapiens';
my $outdir = "/tmp/$species";
my $verbose = '0';
my $help = '0';

####################################################################
## Parsing command line options ####################################
####################################################################
# USAGE: ./info_stats.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'o|outdir=s' => \$outdir, 'trans-file|transcript-file=s' => \$transcript_file,
            'ensembl-libs=s' => \$ENSEMBL_LIBS, 'ensembl-registry=s' => \$ENSEMBL_REGISTRY,
            'ensembl-host=s' => \$ENSEMBL_HOST, 'ensembl-port=s' => \$ENSEMBL_PORT,
            'ensembl-user=s' => \$ENSEMBL_USER, 'ensembl-pass=s' => \$ENSEMBL_PASS,
            'v|verbose' => \$verbose, 'h|help' => \$help);

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
use Bio::EnsEMBL::Funcgen::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Compara::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Variation::VariationFeatureOverlap;
use Bio::EnsEMBL::Variation::Utils::Constants qw(%OVERLAP_CONSEQUENCES);

## loading the registry with the adaptors 
Bio::EnsEMBL::Registry->load_all("$ENSEMBL_REGISTRY");
####################################################################

my %info_stats = ();
my @chromosomes = ();
my @cytobands = ();

my $slice_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Slice");
my $karyotype_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species,"core","KaryotypeBand");
my $gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Gene");


$species = ucfirst($species);
$info_stats{'species'} = $species;


my @all_chroms = @{$slice_adaptor->fetch_all('chromosome')};
my @chrom_ids = ();
foreach my $chrom(@all_chroms) {
	
	my %chromosome = ();
	$chromosome{'name'} = $chrom->seq_region_name();  
	$chromosome{'start'} = int($chrom->start());
	$chromosome{'end'} = int($chrom->end());
	$chromosome{'size'} = int($chrom->seq_region_length());
	$chromosome{'numberGenes'} = scalar @{$chrom->get_all_Genes()};
	$chromosome{'isCircular'} = $chrom->is_circular();
	
	my @cytobands = ();
	foreach my $cyto(@{$karyotype_adaptor->fetch_all_by_chr_name($chrom->seq_region_name)}) {
#		print $cytoband->name."\n";
        my %cytoband = ();
        $cytoband{'name'} = $cyto->name();
        $cytoband{'start'} = int($cyto->start());
        $cytoband{'end'} = int($cyto->end());
        $cytoband{'stain'} = $cyto->stain;
        
		push(@cytobands, \%cytoband);
	}
	
	## check if any cytoband has been added
	## If not a unique cytoband covering all chromosome is added.
	if(@cytobands == 0) {
		my %cytoband = ();
        $cytoband{'name'} = '';
        $cytoband{'start'} = int($chrom->start());
        $cytoband{'end'} = int($chrom->end());
        $cytoband{'stain'} = 'gneg';
        
        push(@cytobands, \%cytoband);
	}
	
	$chromosome{'cytobands'} = \@cytobands;
	
	push(@chromosomes, \%chromosome);
	
    push(@chrom_ids, $chrom->seq_region_name);
}
$info_stats{'chromosomes'} = \@chromosomes;


print encode_json \%info_stats;


