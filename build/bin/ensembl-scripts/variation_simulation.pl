#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;

use JSON;

use DB_CONFIG;

my $species = 'Homo sapiens';
my $chrom = '10';
my ($chrom_start, $chrom_end) = (0, 0);
my $transcript_file = 'Homo sapiens';
my $outdir = "/tmp/$species";
my $verbose = '0';
my $help = '0';


####################################################################
## Parsing command line options	####################################
####################################################################
# USAGE: ./fetch_all_variations.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'chrom|chromosome=s' => \$chrom, 'start=i' => \$chrom_start, 'end=i' => \$chrom_end,
            'o|outdir=s' => \$outdir, 'trans-file|transcript-file=s' => \$transcript_file,
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


my @nts = ('A', 'T', 'C', 'G');

##################################################################
## selecting chromosomes	######################################
##################################################################
if($chrom_start == 0) {
    $chrom_start = 0,
}
if($chrom_end == 0) {
    $chrom_end = 500000000,
}

my @chroms;
if($chrom eq 'all') {
    my @chroms = @{$slice_adaptor->fetch_all('chromosome')};
##	my @chroms_unsorted = @{$slice_adaptor->fetch_all('chromosome')};
#	my @chrom_ids_sorted = @{sort_chromosomes(\@chrom_ids)};
#	print ">>>>".@chrom_ids_sorted."\n";
#	foreach my $chrom_id(@chrom_ids_sorted) {
#		push(@chroms, $slice_adaptor->fetch_by_region( 'chromosome', $chrom_id));
#	}

print Dumper \@chroms if $verbose;
}else {
	my @chroms_ids = split(",", $chrom);
	foreach my $chroms_id(@chroms_ids) {
		push(@chroms, $slice_adaptor->fetch_by_region('chromosome', $chroms_id, $chrom_start, $chrom_end));
	}
}
##################################################################
my ($chrom_name, $start, $end);
my %positions = ();
my @introns = ();
my ($gene_start, $gene_end);
my ($intron_start, $intron_end);
my $prev_exon;
my $sequence;

foreach my $chrom(@chroms) {
    $chrom_name = $chrom->seq_region_name;
    print "Processing chromosome $chrom_name ...\t";
    print "$chrom_start - $chrom_end \t";
    print "$chrom_name length:".$chrom->length." \n";


    open(CHROM_OUTPUT, ">$outdir/${chrom_name}_sim.txt") || die "";

    #############################
    ## Simulating gene mutations
    #############################
    my $num_genes = 0;
    my @genes = @{$gene_adaptor->fetch_all_by_Slice($chrom)};
    foreach my $gene (@genes) {
        $num_genes++;

        $gene_start = $gene->seq_region_start;
        $gene_end = $gene->seq_region_end;
        $sequence = $chrom->subseq($gene->seq_region_start - 5000, $gene->seq_region_end + 5000 + 1);

#print "$gene_start -  $gene_end\n";

        if($num_genes % 100 == 0) {
            print "$num_genes ".$gene->stable_id.", ${gene_start} - ${gene_end} ".($gene_end - $gene_start)."\n";
        }

        ## First we generate all genetic positions +/- 5000 bp
        ## Upstream
        $start = $gene->seq_region_start - 5000;
        $end = $gene->seq_region_start - 1;
        print_common_simulated_variants($start, $end, $sequence, $gene->seq_region_start - 5000, *CHROM_OUTPUT, 'u');

        ## Downstream
        $start = $gene->seq_region_end + 1;
        $end = $gene->seq_region_end + 5000;
        print_common_simulated_variants($start, $end, $sequence, $gene->seq_region_start - 5000, *CHROM_OUTPUT, 'd');

        ## Exons and introns
        @introns = ();
        $prev_exon = undef;
        my @exons = @{$gene->get_all_Exons()};

        ## Exons are not sorted and repeated
        ## This code implements  Bubble Sort and avoid repeated
        my $swapped;
        do {
            $swapped = 0;
            for(my $i=1; $i < @exons; $i++) {
                if($exons[$i-1]->seq_region_start > $exons[$i]->seq_region_start) {
                    my $tmp = $exons[$i-1];
                    $exons[$i-1] = $exons[$i],
                    $exons[$i] = $tmp;
                    $swapped = 1;
                }
            }
        } until($swapped == 0);

        my %visited_exons = ();
        foreach my $exon(@exons) {
            $start = $exon->seq_region_start;
            $end = $exon->seq_region_end;

            if(!defined $visited_exons{"$start-$end"}) {
#                print "\t$start - $end  ".$gene->strand."\n";
                print_all_simulated_variants($start, $end, $sequence, $gene->seq_region_start - 5000, *CHROM_OUTPUT, 'e');

                if(defined $prev_exon) {
                    ## To avoid overlapping exon problems
                    if($prev_exon->seq_region_end < $start) {
                        push(@introns, ($prev_exon->seq_region_end+1)."-".($start-1));
                    }
                }
                $prev_exon = $exon;
                $visited_exons{"$start-$end"} = 1;
            }
        }

        foreach my $intron(@introns) {
            ($start, $end) = split("-", $intron);
#            print "\tintron: $start - $end\n";
            print_common_simulated_variants($start, $end, $sequence, $gene->seq_region_start - 5000, *CHROM_OUTPUT, 'i');
        }

        undef(@exons);
        undef(@introns);
    }

    #############################
    ## Simulating regulatory mutations
    #############################
    my $num_reg_feats = 0;
#    my @reg_feats = @{$regfeat_adaptor->fetch_all()};
#    my $chr_slice = $slice_adaptor->fetch_by_region( 'chromosome', $chrom );
    my @reg_feats = @{$regfeat_adaptor->fetch_all_by_Slice($chrom)};
    print @reg_feats."\n";
    my $total = 0;
    foreach my $reg_feat (@reg_feats) {
        $num_reg_feats++;

        ($start, $end) = ($reg_feat->seq_region_start, $reg_feat->seq_region_end);
        $total += $end-$start;
#print "$start, $end\n";
        if($num_reg_feats % 1000 == 0) {
            print "$num_reg_feats ".$reg_feat->stable_id.", $start - $end ".($end-$start)."\n";
        }

        $sequence = $chrom->subseq($start, $end+1);
        print_all_simulated_variants($start, $end, $sequence, $start, *CHROM_OUTPUT, 'r');

    }
#    print $total."\n";


    #############################
    ## Simulating SNP mutations
    #############################
    my $num_variations = 0;
    my @snps = @{$chrom->get_all_VariationFeatures()};
    foreach my $snp (@snps) {
        $num_variations++;

        ($start, $end) = ($snp->seq_region_start, $snp->seq_region_end);
        if($num_variations % 100000 == 0) {
            print "$num_variations ".$snp->variation()->stable_id.", $start - $end ".($end-$start)."\t";
            print $snp->strand."\n";
        }

        my $allele_string = $snp->allele_string;
        my $c = $allele_string =~ tr/\///;
#        print "$c\n";
        if($c == 1) {
            print CHROM_OUTPUT "$chrom_name\t$start\t$end\t$allele_string\t+\t${chrom_name}_${start}_${allele_string}\n";
        }

    }


    close(CHROM_OUTPUT);
}

sub print_common_simulated_variants {
    my $start = shift;
    my $end = shift;
    my $sequence = shift;
    my $seq_offset = shift;
    local *FILE = shift;
    my $comment = shift;

    for(my $i = $start; $i <= $end; $i++) {
#        my $c = substr($sequence, $i-$start+$seq_offset+1, 1);
        my $c = substr($sequence, $i-$seq_offset+1, 1);
        if($c ne 'N') {
            foreach my $nt(@nts) {
                if($nt ne $c) {
#                    print FILE "$chrom_name\t$i\t$i\t$c/$nt\t+\t${chrom_name}_${i}_$c/${nt}_${comment}\n";
                    print FILE "$chrom_name\t$i\t$i\t$c/$nt\t+\t${chrom_name}_${i}_$c/$nt\n";
                    last;
                }
            }
    #        print FILE "$chrom_name\t$i\t$i\t$c/-\t+\t${chrom_name}_${i}_$c/-_${comment}\n";
        }
    }
}

sub print_all_simulated_variants {
    my $start = shift;
    my $end = shift;
    my $sequence = shift;
    my $seq_offset = shift;
    local *FILE = shift;
    my $comment = shift;

    for(my $i = $start; $i <= $end; $i++) {
        my $c = substr($sequence, $i-$seq_offset+1, 1);
        if($c ne 'N') {
            foreach my $nt(@nts) {
                if($nt ne $c) {
    #                print FILE "$chrom_name\t$i\t$i\t$c/$nt\t+\t${chrom_name}_${i}_$c/${nt}_${comment}\n";
                    print FILE "$chrom_name\t$i\t$i\t$c/$nt\t+\t${chrom_name}_${i}_$c/$nt\n";
                 }
            }
    #        print FILE "$chrom_name\t$i\t$i\t$c/-\t+\t${chrom_name}_${i}_$c/-_${comment}\n";
            print FILE "$chrom_name\t$i\t$i\t$c/-\t+\t${chrom_name}_${i}_$c/-\n";
        }
    }
}


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

