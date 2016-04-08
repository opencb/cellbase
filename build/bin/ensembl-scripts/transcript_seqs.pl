#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;

use JSON;

use DB_CONFIG;


my $species = 'Homo sapiens';
my $gene_file = "/tmp/transcript_seq.fa";
my $out_file = "/tmp/transcript_seq.fa";
####################################################################
## Parsing command line options	####################################
####################################################################
# USAGE: ./variation.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'o|outfile=s' => \$out_file, 'gene-file|gene-file=s' => \$gene_file,
			'ensembl-libs=s' => \$ENSEMBL_LIBS, 'ensembl-registry=s' => \$ENSEMBL_REGISTRY,
			'ensembl-host=s' => \$ENSEMBL_HOST, 'ensembl-port=s' => \$ENSEMBL_PORT,
			'ensembl-user=s' => \$ENSEMBL_USER, 'ensembl-pass=s' => \$ENSEMBL_PASS);


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

use Bio::SeqIO;
use Bio::PrimarySeq;
####################################################################

my $gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species,"core","Gene");
my $transcript_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species,"core","Transcript");

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
	if(defined $gene) {
        foreach my $trans(@{$gene->get_all_Transcripts}) {
            print $trans->stable_id."\t".$trans->seq_region_start."\t".$trans->seq_region_end."\t".$trans->coding_region_start."\t".$trans->coding_region_end."\t".$trans->strand."\n";
            $id =  $trans->stable_id;
            $desc = $gene->stable_id."@".$trans->biotype."@".$trans->seq_region_name."@".$trans->seq_region_start."@".$trans->seq_region_end."@".$trans->strand."@".$trans->status;
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
