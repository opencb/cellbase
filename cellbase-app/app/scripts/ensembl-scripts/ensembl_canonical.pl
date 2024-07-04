#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;
use JSON;
use DB_CONFIG;

use BioMart::Initializer;
use BioMart::Query;
use BioMart::QueryRunner;

## Default values
my $species = 'hsapiens';
my $outdir = "./";

## Parsing command line
GetOptions ('species=s' => \$species, 'outdir=s' => \$outdir);


my $confFile = "/opt/cellbase/scripts/ensembl-scripts/martURLLocation.xml";

# NB: change action to 'clean' if you wish to start a fresh configuration
# and to 'cached' if you want to skip configuration step on subsequent runs from the same registry
my $action='clean';
my $initializer = BioMart::Initializer->new('registryFile'=>$confFile, 'action'=>$action);
my $registry = $initializer->getRegistry;

my $query = BioMart::Query->new('registry'=>$registry,'virtualSchemaName'=>'default');

$query->setDataset($species."_gene_ensembl");

$query->addAttribute("ensembl_gene_id");
$query->addAttribute("ensembl_transcript_id");
$query->addAttribute("transcript_is_canonical");

$query->formatter("TSV");

open (ENSEMBL_CANONICAL, ">$outdir/ensembl_canonical.txt") || die "Cannot open ensembl_canonical.txt file";

my $query_runner = BioMart::QueryRunner->new();

# to obtain unique rows only
$query_runner->uniqueRowsOnly(1);
$query_runner->execute($query);
#$query_runner->printHeader();
print ENSEMBL_CANONICAL $query_runner->printResults();
#$query_runner->printFooter();
