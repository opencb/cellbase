#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;
use JSON;

use DB_CONFIG;

my $species = 'Homo sapiens';
my $assembly = 'GRCh38';
my $phylo = "";
my $gene_file = "/tmp/transcript_seq.fa";
my $out_file = "/tmp/transcript_seq.fa";
my $verbose = '0';
my $help = '0';

####################################################################
## Parsing command line options	####################################
####################################################################
# USAGE: ./transcript_seqs.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'assembly=s' => \$assembly, 'o|outfile=s' => \$out_file, 'gene-file|gene-file=s' => \$gene_file,
    'ensembl-registry=s' => \$ENSEMBL_REGISTRY, 'ensembl-host=s' => \$ENSEMBL_HOST, 'ensembl-port=s' => \$ENSEMBL_PORT,
    'ensembl-user=s' => \$ENSEMBL_USER, 'v|verbose' => \$verbose, 'h|help' => \$help);


####################################################################
## Ensembl APIs	####################################################
####################################################################
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::SeqIO;
use Bio::PrimarySeq;

## loading the registry with the adaptors
# Bio::EnsEMBL::Registry->load_all("$ENSEMBL_REGISTRY");
if ($phylo eq "" || $phylo eq "vertebrate") {
    print ("In vertebrates section\n");
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
} else {
    print ("In no-vertebrates section\n");
    Bio::EnsEMBL::Registry->load_registry_from_db(
        -host => 'mysql-eg-publicsql.ebi.ac.uk',
        -port => 4157,
        -user => 'anonymous'
    );
}

my $gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Gene");
####################################################################

my $id = "";
my $desc = "";
my $trans_seq;
my $gene;

open(GENE_FILE, "$gene_file");
my @genes=<GENE_FILE>;
chomp(@genes);
close(GENE_FILE);
print @genes."\n";

open (\*OUTFILE,">$out_file") || die "Can't open file: $out_file\n";
my $seq_fasta = Bio::SeqIO->new(
    -fh => \*OUTFILE,
    -format => 'fasta'
);

foreach my $gene_id(@genes) {
    print $gene_id."\n";
    $gene = $gene_adaptor->fetch_by_stable_id($gene_id);
    if (defined $gene) {
        foreach my $trans(@{$gene->get_all_Transcripts}) {
            print $trans->stable_id."\t".$trans->seq_region_start."\t".$trans->seq_region_end."\t".$trans->coding_region_start."\t".$trans->coding_region_end."\t".$trans->strand."\n";
            $id =  $trans->stable_id;
            $desc = $gene->stable_id."@".$trans->biotype."@".$trans->seq_region_name."@".$trans->seq_region_start."@".$trans->seq_region_end."@".$trans->strand;
            $trans_seq = Bio::PrimarySeq->new (
                -seq => $trans->seq->seq,
                -id => $id."@".$desc,
                #				-desc => $desc
            );

            $seq_fasta->write_seq($trans_seq);
        }
    }
}
close (\*OUTFILE);
