#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;

use JSON;

use DB_CONFIG;

my $species = 'Homo sapiens';
my $chrom = '10';
my $transcript_file = 'Homo sapiens';
my $outdir = "/tmp/$species";
my $verbose = '0';
my $help = '0';


####################################################################
## Parsing command line options	####################################
####################################################################
# USAGE: ./fetch_all_variations.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'chrom|chromosome=s' => \$chrom, 'o|outdir=s' => \$outdir, 'trans-file|transcript-file=s' => \$transcript_file,
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
## Ensembl APIs	####################################################
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
Bio::EnsEMBL::Registry->load_all("$ENSEMBL_REGISTRY");
####################################################################

my $slice_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Slice");

my $slice_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Slice");
my $gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Gene");
my $regfeat_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, 'funcgen', 'RegulatoryFeature');
my $af_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, 'funcgen', 'AnnotatedFeature');
my $variation_feature_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "variation","VariationFeature");


my ($chrom_name, $start, $end);
my %positions = ();

my @nts = ('A', 'T', 'C', 'G');

my @chroms = @{$slice_adaptor->fetch_all('chromosome')};
foreach my $chrom(@chroms) {
    $chrom_name = $chrom->seq_region_name;
    print "Processing chromosome $chrom_name ...\n";
    open(CHROM_OUTPUT, ">$outdir/tmp_file.txt") || die "";

    #############################
    ## Simulating gene mutations
    #############################
    my $num_genes = 0;
    open(TMP_FILE, ">$outdir/tmp_file.txt") || die "";
    foreach my $gene (@{$gene_adaptor->fetch_all_by_Slice($chrom)}) {
        $num_genes++;
        my $gene_start = $gene->seq_region_start,
        my $sequence = $chrom->subseq($gene->seq_region_start - 5000, $gene->seq_region_end + 5000);
print $gene->stable_id."\n";

        ## First we generate all genetic positions +/- 5000 bp
        ## Upstream
        $start = $gene->seq_region_start - 5000;
        $end = $gene->seq_region_start-1;
        print_simulated_variants($start, $end, $sequence, *CHROM_OUTPUT, 'u');

        ## Downstream
        $start = $gene->seq_region_end+1;
        $end = $gene->seq_region_end + 5000;
        print_simulated_variants($start, $end, $sequence, *CHROM_OUTPUT, 'd');

        ## Exons and introns
        my @introns = ();
        my ($intron_start, $intron_end);
        my $prev_exon;
        my @exons = @{$gene->get_all_Exons()};
        foreach my $exon(@exons) {
#            print $exon->stable_id."\n";
            $start = $exon->seq_region_start;
            $end = $exon->seq_region_end;
            print_all_simulated_variants($start, $end, $sequence, *CHROM_OUTPUT, 'e');

            if(defined $prev_exon) {
                push(@introns, ($prev_exon->end+1)."-".($start-1));
            }
            $prev_exon = $exon;
        }
#        print "Num. exons: ".@exons.", num. introns: ".@introns."\n";
        foreach my $intron(@introns) {
            ($start, $end) = split("-", $intron);
            print_simulated_variants($start, $end, $sequence, *CHROM_OUTPUT, 'i');
        }
    }

    close(TMP_FILE);

    ## Second we sort and uniq all this positions to avoid repetitions due to overlapping
#    system("sort -n '$outdir/tmp_file.txt' | uniq > '$outdir/tmp_file.txt.sort'");
#    ## Reading whole chromosome sequence
#    my $sequence = $chrom->seq();
##    print substr($sequence, 0, 10);
#    open(TMP_FILE, "<$outdir/tmp_file.txt.sort") || die "";
#    while(my $line = <TMP_FILE>) {
#        chomp($line);
#        print "$chrom_name\t$line\t$line\t+\t".substr($sequence, $line, 1)."\n";
#    }
#    close(TMP_FILE);


    #############################
    ## Simulating gene mutations
    #############################
#    my $num_reg_feats = 0;
#    my $num_reg_feats_bp = 0;
##    my @reg_feats = @{$regfeat_adaptor->fetch_all()};
##    my $chr_slice = $slice_adaptor->fetch_by_region( 'chromosome', $chrom );
#    my @reg_feats = @{$regfeat_adaptor->fetch_all_by_Slice($chrom)};
#    foreach my $reg_feat (@reg_feats) {
#        $num_reg_feats++;
#
#        ($start, $end) = ($reg_feat->seq_region_start, $reg_feat->seq_region_end);
#    #    print $start."-".$end, "\n";
#        for(my $i = $start; $i <= $end; $i++) {
#            $num_reg_feats_bp++;
#    #        print $reg_feat->seq_region_name."\t".$reg_feat->seq_region_start."\t".$reg_feat->seq_region_end."\t"."/"."\t+\t\n";
#        }
#
#        if($num_reg_feats % 50000 == 0) {
#            print $num_reg_feats."\n";
#        }
#    }
#    print $num_reg_feats."\n";
#    print $num_reg_feats_bp."\n";


    #############################
    ## Simulating SNP mutations
    #############################


    close(CHROM_OUTPUT);
    last;
}

sub print_simulated_variants {
    my $start = shift;
    my $end = shift;
    my $sequence = shift;
    local *FILE = shift;
    my $comment = shift;

    for(my $i = $start; $i <= $end; $i++) {
        my $c = substr($sequence, $i-$start, 1);
        foreach my $nt(@nts) {
            if($nt ne $c) {
                print FILE "$chrom_name\t$i\t$i\t$c/$nt\t+\t${chrom_name}_${i}_$c/*_${comment}\n";
                last;
            }
        }
#        print FILE "$chrom_name\t$i\t$i\t$c/-\t+\t${chrom_name}_${i}_$c/-_${comment}\n";
    }
}

sub print_all_simulated_variants {
    my $start = shift;
    my $end = shift;
    my $sequence = shift;
    local *FILE = shift;
    my $comment = shift;

    for(my $i = $start; $i <= $end; $i++) {
        my $c = substr($sequence, $i-$start, 1);
        foreach my $nt(@nts) {
            if($nt ne $c) {
                print FILE "$chrom_name\t$i\t$i\t$c/$nt\t+\t${chrom_name}_${i}_$c/${nt}_${comment}\n";
             }
        }
        print FILE "$chrom_name\t$i\t$i\t$c/-\t+\t${chrom_name}_${i}_$c/-_${comment}\n";
    }
}
