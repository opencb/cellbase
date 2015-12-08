#!/usr/bin/env perl

use strict;
use Getopt::Long;
use Data::Dumper;

use DB_CONFIG;


my $species = 'Homo sapiens';
my $phylo = "";
my $outdir = "/tmp/$species";
my $verbose = '0';
my $help = '0';

####################################################################
## Parsing command line options	####################################
####################################################################
# USAGE: ./core.pl --species "Homo sapiens" --outdir ../../appl_db/ird_v1/hsa ...

## Parsing command line
GetOptions ('species=s' => \$species, 'outdir=s' => \$outdir, 'phylo=s' => \$phylo,
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
use Bio::EnsEMBL::Compara::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Funcgen::DBSQL::DBAdaptor;

## loading the registry with the adaptors 
#Bio::EnsEMBL::Registry->load_all("$ENSEMBL_REGISTRY");
#Bio::EnsEMBL::Registry->load_registry_from_db(
#    -host => 'mysql-eg-publicsql.ebi.ac.uk',
#    -port => 4157,
#    -user => 'anonymous'
#);
#Bio::EnsEMBL::Registry->load_registry_from_db(
#  -host    => 'ensembldb.ensembl.org',
#  -user    => 'anonymous',
#  -verbose => '0'
#);
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

## variables definition
my (%dbnames, %dbnames_cont, %xrefs);

## everything is allright:
&createCoreTables();


my ($ngene, $ntrans, $nxref, $north, $ndbname) = (0, 0, 0, 0, 0);
my $trans_ext_name;
my @ordered_dbnames = ();
my @trans_ext_name = ();

sub create_orthologous {
    

}

sub createCoreTables {
	my ($gene, $exon, $dblink, $translation, %uniq, $gene_adaptor, $transcript_adaptor, $translation_adaptor, $probefeature_adaptor, $pba);
	my (@dbentries,@arr_dbnames);
	my $orth_gene;
	my %exon_trans = ();
	
	## reset dbnames and xrefs
	%dbnames = ();
	%dbnames_cont = ();
	%xrefs = ();

	$gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Gene");
	$transcript_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "Transcript");
	$translation_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "translation");
	$probefeature_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "funcgen", "ProbeFeature");

	$pba = Bio::EnsEMBL::Registry->get_adaptor($species, "funcgen", "ProbeSet");

    open (GENE_DESC, ">$outdir/description.txt") || die "Cannot open gene_description.txt file";
    open (XREFS, ">$outdir/xrefs.txt") || die "Cannot open xref.txt file";
	
	## for all the genes in the specie
	foreach my $gene_id(@{$gene_adaptor->list_stable_ids()}) {   #fetch_all_by_biotype('miRNA')
		$ngene++;
		$gene = $gene_adaptor->fetch_by_stable_id($gene_id);
		
		print $ngene."\t".$gene->stable_id."\t".$gene->description."\n" if $verbose;

		##### Gene Info and sequence ######################
        print GENE_DESC $gene->stable_id."\t".$gene->description."\n";


		########################################################################
		##### XREF TRANSCRIPTS #################################################
		########################################################################
		foreach my $trans(@{$gene->get_all_Transcripts}) {
			$ntrans++;
			
			print "\t".$ntrans."\t".$trans->stable_id."\t".$trans->description."\n" if $verbose;
			
			# Si el trancript es un microrna lo introduzco en xref tambien:
			if($trans->biotype eq 'miRNA') {
				print XREFS $trans->stable_id()."\t".$gene->external_name()."\t".&get_dbname_short("miRNA gene")."\t"."miRNA gene"."\t".$gene->description()."\n";
			}
			
			# Referencia al ensembl gene id
			print XREFS $trans->stable_id()."\t".$gene->stable_id()."\t".&get_dbname_short("Ensembl gene")."\t"."Ensembl gene"."\t".$gene->description()."\n";
			
#			print XREF $ntrans."\t".$gene->external_name."\t".&get_dbname($gene->get_all_DBEntries->[0]->{'db_display_name'})."\t".$gene->get_all_DBEntries->[0]->{'description'}."\n";
#			print XREFS $trans->stable_id()."\t".$gene->external_name()."\t".&get_dbname_short($gene->get_all_DBEntries->[0]->{'db_display_name'})."\t".$gene->get_all_DBEntries->[0]->{'db_display_name'}."\t".$gene->get_all_DBEntries->[0]->{'description'}."\n";
			foreach my $dbentry(@{$gene->get_all_DBEntries}){
					print XREFS $trans->stable_id()."\t".$dbentry->{'display_id'}."\t".&get_dbname_short($dbentry->{'db_display_name'})."\t".$dbentry->{'db_display_name'}."\t".$dbentry->{'description'}."\n";
			}

	
			# Referencia al ensembl transcript id
			print XREFS $trans->stable_id()."\t".$trans->stable_id()."\t".&get_dbname_short("Ensembl transcript")."\t"."Ensembl transcript"."\t".$trans->description()."\n";
	
			##### Translation ######################################################
			# Referencia al ensembl prot id
			$translation = $trans->translation();
			if(defined($translation)) {
				print XREFS $trans->stable_id()."\t".$translation->stable_id."\t".&get_dbname_short("Ensembl protein")."\t"."Ensembl protein"."\t".$trans->description()."\n";
#				foreach my $dblink(@{ $translation->get_all_xrefs }) {
#				    print XREFS $trans->stable_id()."\t".$dblink->display_id()."\t".&get_dbname_short($dblink->db_display_name())."\t".$dblink->db_display_name()."\t".$dblink->description()."\n";
#				}

				%uniq = ();
				foreach my $df(@{$translation->get_all_ProteinFeatures()}){
					if( $df->interpro_ac() ne "" && not defined($uniq{$df->interpro_ac()})) {
						print XREFS $trans->stable_id()."\t".$df->interpro_ac()."\t".&get_dbname_short("InterPro")."\t"."InterPro"."\t".$df->idesc()."\n";
						$uniq{$df->interpro_ac()} = 1;
					}
				}
			}

#			foreach my $dblink(@{ $trans->get_all_DBLinks }) {
			foreach my $dblink(@{ $trans->get_all_xrefs }) {
				print XREFS $trans->stable_id()."\t".$dblink->display_id()."\t".&get_dbname_short($dblink->db_display_name())."\t".$dblink->db_display_name()."\t".$dblink->description()."\n";
				
				if($dblink->display_id() =~ /(\w+)\.\d$/){
					print XREFS $trans->stable_id()."\t".$1."\t".&get_dbname_short($dblink->db_display_name())."\t".$dblink->db_display_name()."\t".$dblink->description()."\n";
				}
				if($dblink->display_id() =~ /(\w+)\-\d$/){
					print XREFS $trans->stable_id()."\t".$1."\t".&get_dbname_short($dblink->db_display_name())."\t".$dblink->db_display_name()."\t".$dblink->description()."\n";
				}
				if(defined ($trembl_species{$species}) && $dblink->display_id() =~ /(\w+)_$trembl_species{$species}/){
					print XREFS $trans->stable_id()."\t".$1."\t".&get_dbname_short($dblink->db_display_name())."\t".$dblink->db_display_name()."\t".$dblink->description()."\n";
				}
			}


			## MICROARRYAYS
			if(defined $pba) {
				my @probesets = @{$pba->fetch_all_by_external_name($trans->stable_id)};
				foreach my $probeset (@probesets){

	#              my $arrays_string = join(', ', (map $_->name, @{$probeset->get_all_Arrays}));
				  my @arrays_string = map $_->name, @{$probeset->get_all_Arrays};
				  my $dbe_info;

				  #Now get linkage_annotation
	#              foreach my $dbe(@{$probeset->get_all_Transcript_DBEntries($trans)}){
					#This will return all ProbeSet DBEntries for this transcript
					#There should really be one max per transcript per probeset/probe
	#            	$dbe_info = $dbe->linkage_annotation;
	#              }

	#              print "\t".$probeset->name." on arrays ".$arrays_string." with Probe hits $dbe_info\n";
				  foreach my $array_string(@arrays_string) {
					print XREFS $trans->stable_id()."\t".$probeset->name."\t".&get_dbname_short($array_string)."\t".$array_string."\t\n";
				  }

				}
			}


		}
		########################################################################
#		last;
	}
	close(GENE_DESC);
	close(XREFS);
}

sub get_dbname_short {
    my $display_name = shift;
    my $dbname = $display_name; 
    if ($display_name =~ /Affymx Microarray/) {
            $dbname =~ s/Affymx Microarray/affy/;
        }elsif ($display_name =~ /MIM/) {
            $dbname =~ s/MIM/omim/;
        }elsif ($display_name =~ /Codelink/) {
            $dbname = "codelink";
        }elsif ($display_name =~ /Swiss-Prot/) {
            $dbname =~ s/Swiss-Prot/swissprot/;
        }elsif ($display_name =~ /MEROPS/) {
            $dbname =~ s/MEROPS - the Peptidase Database/merops/;
        }
        $dbname =~ s/ /_/g;
        $dbname =~ s/-/_/g;
        $dbname =~ s/\(//g;
        $dbname =~ s/\)//g;
        $dbnames{$display_name} = lc($dbname);
    return lc($dbname);
}


sub print_parameters {
	print "Parameters: ";
	print "species: $species, outdir: $outdir, ";
	print "ensembl-libs: $ENSEMBL_LIBS, ensembl-registry: $ENSEMBL_REGISTRY, ";
	print "ensembl-host: $ENSEMBL_HOST, ensembl-port: $ENSEMBL_PORT, ";
	print "ensembl-user: $ENSEMBL_USER, ensembl-pass: $ENSEMBL_PASS, ";
	print "verbose: $verbose, help: $help";
	print "\n";
}

sub print_usage {
	print "Usage:   $0 [--species] [--outdir] [--ensembl-libs] [--ensembl-host] [--ensembl-port] ";
	print "[--ensembl-user] [--ensembl-pass] [--verbose] [--help]\n";
	print "\tDescription:\n";
	print "\t species: scientific name, i.e.: Homo sapiens\n";
	print "\t outdir: default directory /tmp/species\n";
	print "\t ensembl-libs: Ensembl libraries path\n";
	print "\t ensembl-host: Ensembl database host\n";
	print "\t ensembl-port: Ensembl database port\n";
	print "\t ensembl-user: Ensembl database user\n";
	print "\t ensembl-pass: Ensembl database pass\n";
	print "\t verbose: print logs\n";
	print "\t help: print this help\n";
	print "\n";
	exit(0);
}

########################################################################
########################################################################
########################################################################
