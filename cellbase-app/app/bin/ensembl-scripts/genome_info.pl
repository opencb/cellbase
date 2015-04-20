#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;

use JSON;

use DB_CONFIG;


my $species = 'Homo sapiens';
my $phylo = "";
my $outfile = "";
my $verbose = '0';
my $help = '0';

####################################################################
## Parsing command line options ####################################
####################################################################
# USAGE: ./info_stats.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'o|outfile=s' => \$outfile, 'phylo=s' => \$phylo,
            'ensembl-libs=s' => \$ENSEMBL_LIBS, 'ensembl-registry=s' => \$ENSEMBL_REGISTRY,
            'ensembl-host=s' => \$ENSEMBL_HOST, 'ensembl-port=s' => \$ENSEMBL_PORT,
            'ensembl-user=s' => \$ENSEMBL_USER, 'ensembl-pass=s' => \$ENSEMBL_PASS,
            'v|verbose' => \$verbose, 'h|help' => \$help);

## Checking help parameter
print_usage() if $help;

## Printing parameters
print_parameters() if $verbose;

if($outfile eq "") {
    $outfile = "/tmp/$species.json";
}

####################################################################
## Ensembl APIs ####################################################
####################################################################
## loading ensembl libraries
use lib "$ENSEMBL_LIBS/ensembl/modules";
use lib "$ENSEMBL_LIBS/ensembl-variation/modules";
use lib "$ENSEMBL_LIBS/ensembl-compara/modules";
use lib "$ENSEMBL_LIBS/ensembl-funcgen/modules";
use lib "$ENSEMBL_LIBS/bioperl-live";

## creating ensembl adaptors
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Variation::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Funcgen::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Compara::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Variation::VariationFeatureOverlap;
use Bio::EnsEMBL::Variation::Utils::Constants qw(%OVERLAP_CONSEQUENCES);

## loading the registry with the adaptors 
#Bio::EnsEMBL::Registry->load_all("$ENSEMBL_REGISTRY");
if($phylo eq "" || $phylo eq "vertebrate") {
    print ("In vertebrates section");
    Bio::EnsEMBL::Registry->load_registry_from_db(
      -host    => 'ensembldb.ensembl.org',
      -user    => 'anonymous',
      -verbose => '0'
    );
}else {
    print ("In no-vertebrates section");
    Bio::EnsEMBL::Registry->load_registry_from_db(
        -host => 'mysql-eg-publicsql.ebi.ac.uk',
        -port => 4157,
        -user => 'anonymous'
    );
}
####################################################################

my $slice_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Slice");
my $karyotype_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species,"core","KaryotypeBand");
my $gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Gene");

my %info_stats = ();
my @chromosomes = ();
my @supercontigs = ();
my @cytobands = ();

$species = ucfirst($species);
$info_stats{'species'} = $species;

my @all_chroms = @{$slice_adaptor->fetch_all('chromosome')};
#my @chrom_ids = ();
foreach my $chrom(@all_chroms) {

	my %chromosome = ();
	$chromosome{'name'} = $chrom->seq_region_name();
	$chromosome{'start'} = int($chrom->start());
	$chromosome{'end'} = int($chrom->end());
	$chromosome{'size'} = int($chrom->seq_region_length());
#	$chromosome{'numberGenes'} = scalar @{$chrom->get_all_Genes()};
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
#    push(@chrom_ids, $chrom->seq_region_name);
}
$info_stats{'chromosomes'} = \@chromosomes;

## Now we also add the supercontigs
my @all_supercontigs = @{$slice_adaptor->fetch_all('supercontig')};
my @contigs_ids = ();
foreach my $supercon(@all_supercontigs) {
    my %supercontig = ();

    if($supercon->seq_region_name() !~ /PATCH/) {
        $supercontig{'name'} = $supercon->seq_region_name();
        $supercontig{'start'} = int($supercon->start());
        $supercontig{'end'} = int($supercon->end());
        $supercontig{'size'} = int($supercon->seq_region_length());
#        $supercontig{'numberGenes'} = scalar @{$supercon->get_all_Genes()};
        $supercontig{'isCircular'} = $supercon->is_circular();

        ## Adding an unique cytoband covering all chromosome is added.
        my @cytobands = ();
        my %cytoband = ();
        $cytoband{'name'} = '';
        $cytoband{'start'} = int($supercon->start());
        $cytoband{'end'} = int($supercon->end());
        $cytoband{'stain'} = 'gneg';
        push(@cytobands, \%cytoband);
        $supercontig{'cytobands'} = \@cytobands;

        push(@supercontigs, \%supercontig);
    }
}
$info_stats{'supercontigs'} = \@supercontigs;

my $info_stats_json = encode_json \%info_stats;
open(OUTFILE, ">$outfile") || die "Cannot open $outfile\n";
print OUTFILE $info_stats_json;
close(OUTFILE);
