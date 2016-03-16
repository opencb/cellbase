package DB_CONFIG;

use strict;
use Data::Dumper;

BEGIN {
	require Exporter;
# 	use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS,
	our @ISA = qw(Exporter);
	our @EXPORT = qw(
			$HOME
			$ENSEMBL_LIBS
			$ENSEMBL_REGISTRY
			$BIOMART_LIB
			$FILE_PATH
			$TEMP_PATH
			%species_alias
			%biomart_species
			%trembl_species
			@hsa_ext_refs
			$INFRARED_HOST
			$INFRARED_USER
			$INFRARED_PASS
			$ENSEMBL_HOST
			$ENSEMBL_USER
			$ENSEMBL_PASS
			$ENSEMBL_PORT
			$ENSEMBL_GENOMES_HOST
			$ENSEMBL_GENOMES_USER
			$ENSEMBL_GENOMES_PASS
			$ENSEMBL_GENOMES_PORT

			$HOMO_SAPIENS_CORE
			$HOMO_SAPIENS_VARIATION
			$HOMO_SAPIENS_FUNCTIONAL
			$MUS_MUSCULUS_CORE
			$MUS_MUSCULUS_VARIATION
			$MUS_MUSCULUS_FUNCTIONAL
			$RATTUS_NORVEGICUS_CORE
			$RATTUS_NORVEGICUS_VARIATION
			$RATTUS_NORVEGICUS_FUNCTIONAL
			$PAN_TROGLODYTES_CORE
            $PAN_TROGLODYTES_VARIATION
			$GORILLA_GORILLA_CORE
			$PONGO_ABELII_CORE
			$MACACA_MULATTA_CORE
			$CHLOROCEBUS_SABAEUS_CORE
			$SUS_SCROFA_CORE
			$SUS_SCROFA_VARIATION
			$CANIS_FAMILIARIS_CORE
			$CANIS_FAMILIARIS_VARIATION
			$EQUUS_CABALLUS_CORE
			$ORYCTOLAGUS_CUNICULUS_CORE
			$GALLUS_GALLUS_CORE
			$BOS_TAURUS_CORE
			$FELIS_CATUS_CORE
			$DANIO_RERIO_CORE
			$DANIO_RERIO_VARIATION
			$CIONA_INTESTINALIS_CORE
			$OVIS_ARIES_CORE
			$ORYZIAS_LATIPES_CORE

			$DROSOPHILA_MELANOGASTER_CORE
			$DROSOPHILA_MELANOGASTER_VARIATION
			$DROSOPHILA_MELANOGASTER_FUNCTIONAL
			$DROSOPHILA_SIMULANS_CORE
			$DROSOPHILA_YAKUBA_CORE
			$ANOPHELES_GAMBIAE_CORE
			$ANOPHELES_GAMBIAE_VARIATION
			$CAENORHABDITIS_ELEGANS_CORE

			$SACCHAROMYCES_CEREVISIAE_CORE
			$SACCHAROMYCES_CEREVISIAE_VARIATION
			$SCHIZOSACCHAROMYCES_POMBE_CORE
			$ASPERGILLUS_FUMIGATUS_CORE
			$ASPERGILLUS_NIGER_CORE
			$ASPERGILLUS_NIDULANS_CORE
			$ASPERGILLUS_ORYZAE_CORE

			$PLASMODIUM_FALCIPARUM_CORE
			$PLASMODIUM_FALCIPARUM_VARIATION
			$PLASMODIUM_FALCIPARUM_CORE
			$PLASMODIUM_FALCIPARUM_VARIATION
			$LEISHMANIA_MAJOR_CORE

			$ARABIDOPSIS_THALIANA_CORE
			$ARABIDOPSIS_LYRATA_CORE
			$BRACHYPODIUM_DISTACHYON_CORE
			$ORYZA_SATIVA_CORE
			$GLYCINE_MAX_CORE
			$VITIS_VINIFERA_CORE
			$ZEA_MAYS_CORE
			$SOLANUM_LYCOPERSICUM_CORE

			$GGO_CORE
			$CAVIA_PORCELLUS_CORE
			$PONGO_PYGMAEUS_CORE
			$ORNITHORHYNCHUS_ANATINUS_CORE
			$FUSARIUM_OXYSPORUM_CORE
			$COMPARA
			&sort
			&sort_chromosomes
			&get_sorted_chromosomes_indexes
			&unique
			&file_to_hash
			);
		### Add here symbols to be exported on request
    	our @EXPORT_OK   = qw( );
    	### Add here tags to export symbols as groups
    	our %EXPORT_TAGS = ( );
}

#########################################################
#####	ENSEMBL		#####################################
#########################################################
our $HOME = "/mnt/mysql/infrared_dbs";

my $user_home = $ENV{'HOME'};
our $ENSEMBL_LIBS = "$user_home/apis/ensembl/api_78";
our $ENSEMBL_REGISTRY = "$user_home/appl/cellbase/cellbase-build/installation-dir/bin/ensembl-scripts/registry.conf";

our $BIOMART_LIB = "/mnt/mysql/ensembl_apis/biomart-perl";
our $FILE_PATH = "$HOME/infrared_v2";
our $TEMP_PATH = "$HOME/temp";

our %species_alias = ("Arabidopsis thaliana"=>"ath", "Homo sapiens"=>"hsa", "Pan troglodytes"=>"ptr", "Gorilla gorilla"=>"ggo", "Danio rerio"=>"dre", "Mus musculus"=>"mmu","Rattus norvegicus"=>"rno","Canis familiaris"=>"cfa","Bos taurus"=>"bta","Gallus gallus"=>"gga","Danio rerio"=>"dre","Drosophila melanogaster"=>"dme","Saccharomyces cerevisiae"=>"sce","Caenorhabditis elegans"=>"cel","Anopheles gambiae"=>"aga", "Plasmodium falciparum"=>"pfa");
our %biomart_species = ("Anopheles gambiae"=>"agambiae_gene_ensembl","Arabidopsis thaliana"=>"athaliana_eg_gene","Bos taurus"=>"btaurus_gene_ensembl", "Caenorhabditis elegans"=>"celegans_gene_ensembl", "Canis familiaris"=>"cfamiliaris_gene_ensembl", "Danio rerio"=>"drerio_gene_ensembl", "Drosophila melanogaster"=>"dmelanogaster_gene_ensembl", "Gallus gallus"=>"ggallus_gene_ensembl", "Homo sapiens"=>"hsapiens_gene_ensembl","Mus musculus"=>"mmusculus_gene_ensembl","Rattus norvegicus"=>"rnorvegicus_gene_ensembl","Saccharomyces cerevisiae"=>"scerevisiae_gene_ensembl");
our %trembl_species = ("Homo sapiens"=>"HUMAN", "Mus musculus"=>"MOUSE", "Rattus norvegicus"=>"RAT");
#our @hsa_ext_refs = ("go_biological_process_id,Biological Process,Functional annotation","go_cellular_component_id","go_molecular_function_id","goslim_goa_accession","clone_based_ensembl_gene_name","clone_based_ensembl_transcript_name","clone_based_vega_gene_name","clone_based_vega_transcript_name","ccds","embl","ens_hs_gene","entrezgene","ottt","shares_cds_with_enst","shares_cds_with_ottt","shares_cds_and_utr_with_ottt","hgnc_id","hgnc_symbol","hgnc_automatic_gene_name","hgnc_curated_gene_name","hgnc_automatic_transcript_name","hgnc_curated_transcript_name","ipi","merops","imgt_gene_db","imgt_ligm_db","mim_morbid_accession","mim_morbid_description","mim_gene_accession","mim_gene_description","mirbase_accession","mirbase_id","pdb","protein_id","refseq_dna","refseq_dna_predicted","refseq_peptide","refseq_peptide_predicted","refseq_genomic","rfam","unigene","uniprot_sptrembl","uniprot_swissprot","uniprot_swissprot_accession","pubmed","wikigene_name","wikigene_description","hpa","dbass3_id","dbass3_name","dbass5_id","dbass5_name","affy_hc_g110","affy_hg_focus","affy_hg_u133_plus_2","affy_hg_u133a_2","affy_hg_u133a","affy_hg_u133b","affy_hg_u95av2","affy_hg_u95b","affy_hg_u95c","affy_hg_u95d","affy_hg_u95e","affy_hg_u95a","affy_hugenefl","affy_huex_1_0_st_v2","affy_hugene_1_0_st_v1","affy_u133_x3p","agilent_cgh_44b","agilent_wholegenome","codelink","illumina_humanwg_6_v1","illumina_humanwg_6_v2","illumina_humanwg_6_v3","phalanx_onearray","interpro");
our @hsa_ext_refs = ("go", "go_biological_process_id","go_cellular_component_id","go_molecular_function_id","goslim_goa_accession","clone_based_ensembl_gene_name","clone_based_ensembl_transcript_name","clone_based_vega_gene_name","clone_based_vega_transcript_name","ccds","embl","ens_hs_gene","entrezgene","ottt","shares_cds_with_enst","shares_cds_with_ottt","shares_cds_and_utr_with_ottt","hgnc_id","hgnc_symbol","hgnc_automatic_gene_name","hgnc_curated_gene_name","hgnc_automatic_transcript_name","hgnc_curated_transcript_name","ipi","merops","imgt_gene_db","imgt_ligm_db","mim_morbid_accession","mim_morbid_description","mim_gene_accession","mim_gene_description","mirbase_accession","mirbase_id","pdb","protein_id","refseq_dna","refseq_dna_predicted","refseq_peptide","refseq_peptide_predicted","refseq_genomic","rfam","unigene","uniprot_sptrembl","uniprot_swissprot","uniprot_swissprot_accession","pubmed","wikigene_name","wikigene_description","hpa","dbass3_id","dbass3_name","dbass5_id","dbass5_name","affy_hc_g110","affy_hg_focus","affy_hg_u133_plus_2","affy_hg_u133a_2","affy_hg_u133a","affy_hg_u133b","affy_hg_u95av2","affy_hg_u95b","affy_hg_u95c","affy_hg_u95d","affy_hg_u95e","affy_hg_u95a","affy_hugenefl","affy_huex_1_0_st_v2","affy_hugene_1_0_st_v1","affy_u133_x3p","agilent_cgh_44b","agilent_wholegenome","codelink","illumina_humanwg_6_v1","illumina_humanwg_6_v2","illumina_humanwg_6_v3","phalanx_onearray","interpro");
	
our $ENSEMBL_HOST = "ensembldb.ensembl.org";
our $ENSEMBL_USER = "anonymous";
our $ENSEMBL_PASS = "";
our $ENSEMBL_PORT = "5306";

our $ENSEMBL_GENOMES_HOST = "mysql-eg-publicsql.ebi.ac.uk";
our $ENSEMBL_GENOMES_USER = "anonymous";
our $ENSEMBL_GENOMES_PASS = "";
our $ENSEMBL_GENOMES_PORT = "4157";

our $INFRARED_HOST = "mysqlweb";
our $INFRARED_USER = "biouser";
our $INFRARED_PASS = "biopass";

#our $ENS_HOST = "ensembldb.ensembl.org";
#our $ENS_USER = "anonymous";
#our $ENS_PORT = "5306";


## Vertebrates
our $HOMO_SAPIENS_CORE = "homo_sapiens_core_75_37";
our $HOMO_SAPIENS_VARIATION = "homo_sapiens_variation_75_37";
our $HOMO_SAPIENS_FUNCTIONAL = "homo_sapiens_funcgen_75_37";
#our $HOMO_SAPIENS_CORE = "homo_sapiens_core_78_38";
#our $HOMO_SAPIENS_VARIATION = "homo_sapiens_variation_78_38";
#our $HOMO_SAPIENS_FUNCTIONAL = "homo_sapiens_funcgen_78_38";
our $MUS_MUSCULUS_CORE = "mus_musculus_core_78_38";
our $MUS_MUSCULUS_VARIATION = "mus_musculus_variation_78_38";
our $MUS_MUSCULUS_FUNCTIONAL = "mus_musculus_funcgen_78_38";
our $RATTUS_NORVEGICUS_CORE = "rattus_norvegicus_core_78_5";
our $RATTUS_NORVEGICUS_VARIATION = "rattus_norvegicus_variation_78_5";
our $RATTUS_NORVEGICUS_FUNCTIONAL = "rattus_norvegicus_funcgen_78_5";
our $PAN_TROGLODYTES_CORE = "pan_troglodytes_core_78_214";
our $PAN_TROGLODYTES_VARIATION = "pan_troglodytes_variation_78_214";
our $GORILLA_GORILLA_CORE = "gorilla_gorilla_core_78_31";
our $PONGO_ABELII_CORE = "pongo_abelii_core_78_1";
our $MACACA_MULATTA_CORE = "macaca_mulatta_core_78_10";
our $CHLOROCEBUS_SABAEUS_CORE = "chlorocebus_sabaeus_core_78_1";
our $SUS_SCROFA_CORE = "sus_scrofa_core_78_102";
our $SUS_SCROFA_VARIATION = "sus_scrofa_variation_78_102";
our $CANIS_FAMILIARIS_CORE = "canis_familiaris_core_78_31";
our $CANIS_FAMILIARIS_VARIATION = "canis_familiaris_variation_78_31";
our $EQUUS_CABALLUS_CORE = "equus_caballus_core_78_2";
our $ORYCTOLAGUS_CUNICULUS_CORE = "oryctolagus_cuniculus_core_78_3";
our $GALLUS_GALLUS_CORE = "gallus_gallus_core_78_4";
our $BOS_TAURUS_CORE = "bos_taurus_core_78_31";
our $FELIS_CATUS_CORE = "felis_catus_core_78_62";
our $DANIO_RERIO_CORE = "danio_rerio_core_78_9";
our $DANIO_RERIO_VARIATION = "danio_rerio_variation_78_9";
our $CIONA_INTESTINALIS_CORE = "ciona_intestinalis_core_78_3";
our $OVIS_ARIES_CORE = "ovis_aries_core_78_31";
our $ORYZIAS_LATIPES_CORE = "oryzias_latipes_core_78_1";

## Metazoa
our $DROSOPHILA_MELANOGASTER_CORE = "drosophila_melanogaster_core_24_77_546";
our $DROSOPHILA_MELANOGASTER_VARIATION = "drosophila_melanogaster_variation_24_77_546";
our $DROSOPHILA_MELANOGASTER_FUNCTIONAL = "drosophila_melanogaster_funcgen_24_77_546";
our $DROSOPHILA_SIMULANS_CORE = "drosophila_simulans_core_24_77_14";
our $DROSOPHILA_YAKUBA_CORE = "drosophila_yakuba_core_24_77_13";
our $ANOPHELES_GAMBIAE_CORE = "anopheles_gambiae_core_24_77_4";
our $ANOPHELES_GAMBIAE_VARIATION = "anopheles_gambiae_variation_24_77_4";
our $CAENORHABDITIS_ELEGANS_CORE = "caenorhabditis_elegans_core_24_77_240";

## Fungi
our $SACCHAROMYCES_CEREVISIAE_CORE = "saccharomyces_cerevisiae_core_24_77_4";
our $SACCHAROMYCES_CEREVISIAE_VARIATION = "saccharomyces_cerevisiae_variation_24_77_4";
our $SCHIZOSACCHAROMYCES_POMBE_CORE = "schizosaccharomyces_pombe_core_24_77_2";
our $ASPERGILLUS_FUMIGATUS_CORE = "aspergillus_fumigatus_core_24_77_2";
our $ASPERGILLUS_NIGER_CORE = "aspergillus_niger_core_24_77_1";
our $ASPERGILLUS_NIDULANS_CORE = "aspergillus_nidulans_core_24_77_6";
our $ASPERGILLUS_ORYZAE_CORE = "aspergillus_oryzae_core_24_77_2";

## Protist
our $PLASMODIUM_FALCIPARUM_CORE = "plasmodium_falciparum_core_24_77_3";
our $PLASMODIUM_FALCIPARUM_VARIATION = "plasmodium_falciparum_variation_24_77_3";
our $LEISHMANIA_MAJOR_CORE = "leishmania_major_core_24_77_2";

## Plants
our $ARABIDOPSIS_THALIANA_CORE = "arabidopsis_thaliana_core_24_77_10";
our $ARABIDOPSIS_LYRATA_CORE = "arabidopsis_lyrata_core_24_77_10";
our $BRACHYPODIUM_DISTACHYON_CORE = "brachypodium_distachyon_core_24_77_12";
our $ORYZA_SATIVA_CORE = "oryza_sativa_core_24_77_7";
our $GLYCINE_MAX_CORE = "glycine_max_core_24_77_1";
our $VITIS_VINIFERA_CORE = "vitis_vinifera_core_24_77_3";
our $ZEA_MAYS_CORE = "zea_mays_core_24_77_6";
our $SOLANUM_LYCOPERSICUM_CORE = "solanum_lycopersicum_core_24_77_240";

our $CAVIA_PORCELLUS_CORE = "cavia_porcellus_core_56_3a";
our $PONGO_PYGMAEUS_CORE = "pongo_pygmaeus_core_56_1c";
our $ORNITHORHYNCHUS_ANATINUS_CORE = "ornithorhynchus_anatinus_core_56_1k";

our $COMPARA = "ensembl_compara_78";
our $GO = "ensembl_ontology_78";


# adding biocarta (http://cgap.nci.nih.gov/Info/CGAPDownload) ==> awk -F':' '// {if($1 == "UNIGENE") uni= $2;if($1 == "BIOCARTA") print uni" "$2}' Hs_GeneData.dat > unigene2biocarta.txt

#our $GENE_QUERY = "select g.gene_id,gs.stable_id, g.biotype, sq.name, g.seq_region_start,g.seq_region_end,g.seq_region_strand,g.source, g.status, g.description
#from gene g, gene_stable_id gs,seq_region sq where g.gene_id=gs.gene_id and g.seq_region_id=sq.seq_region_id";
#our $TRANS_QUERY = "select t.transcript_id, t.gene_id,ts.stable_id, t.biotype, sq.name, t.seq_region_start,t.seq_region_end,t.seq_region_strand,t.status, t.description  from trans
#cript t, transcript_stable_id ts, seq_region sq where t.transcript_id=ts.transcript_id and t.seq_region_id=sq.seq_region_id";
#our $EXON_QUERY = "select e.exon_id,es.stable_id, sq.name, e.seq_region_start,e.seq_region_end,e.seq_region_strand from exon e, exon_stable_id es,seq_region sq where e.exon_id=es.
#exon_id and e.seq_region_id=sq.seq_region_id";
#our $EXON_TRANS_QUERY = "select * from exon_transcript";
#our $KARYOTYPE_QUERY = "select sq.name, k.band, k.seq_region_start,k.seq_region_end from karyotype k, seq_region sq where k.seq_region_id = sq.seq_region_id order by sq.name,k.seq
#_region_start;";
#our $SNP_QUERY = "select variation_name,r.name,seq_region_start,seq_region_end,seq_region_strand,allele_string,map_weight,validation_status,consequence_type from $HSA_VARIATION.va
#riation_feature v, $HSA_CORE.seq_region r where r.seq_region_id=v.seq_region_id";
#our $SNP_TRANSCRIPT_QUERY = "select v.variation_name, te.stable_id, t.consequence_type,t.peptide_allele_string,t.translation_start,t.translation_end from $HSA_VARIATION.transcript
#_variation t , $HSA_VARIATION.variation_feature v, $HSA_CORE.transcript_stable_id te where  t.variation_feature_id=v.variation_feature_id and t.transcript_id=te.transcript_id";
#our $GO_INFO_QUERY = "select distinct(t.acc),t.name,t.term_type, td.term_definition,gp.distance,0,0 from term t, term_definition td, graph_path gp where t.id=td.term_id and gp.ter
#m2_id=t.id and gp.distance = (select max(gp2.distance) from graph_path gp2 where t.id = gp2.term2_id)";
#our $GO_GRAPH_PATH_QUERY = "select t1.acc,t2.acc,gp.distance from graph_path gp, term t1, term t2 where gp.term1_id = t1.id and gp.term2_id=t2.id";

our sub file_to_hash {
	my ($filename, $key_index, $value_index) = @_;
	open(TRANS_FILE, $filename) || die "Error opening $filename \n";
	my %trans_ids = ();
	my @data = ();
	while (<TRANS_FILE>) {
		chomp($_);
		@data = split("\t", $_);
		$trans_ids{$data[$key_index]} = $data[$value_index];
	}
	close(TRANS_FILE);
	return %trans_ids;
}

our sub sort {
	my $list= shift;
	my @sorted = sort { $a <=> $b } @$list;
	return \@sorted;
}

our sub sort_chromosomes {
	my $list= shift;
#	my %chrom_indexes = {};
	my @chr_numbers = ();
	my @chr_strings = ();
	my %visited = {};
	foreach my $chr_id(@{$list}) {
		if(not defined $visited{$chr_id}) {
			if($chr_id =~ m/\d/) {
				push(@chr_numbers, $chr_id);
			}else {
				push(@chr_strings, $chr_id);
			}
			$visited{$chr_id} = 1;
		}
	}
	
	my @sorted_numbers = sort { $a <=> $b } @chr_numbers;
	my @sorted_strings = sort { $a <=> $b } @chr_strings;
	foreach my $chr_id(@sorted_strings) {
		push(@sorted_numbers, $chr_id);
	}
	
#	for(my $i=0; $i<@sorted_numbers; $i++) {
#		$chrom_indexes{$sorted_numbers[$i]} = $i;
#	}
#	print Dumper \%chrom_indexes;
	return \@sorted_numbers;
}

our sub get_sorted_chromosomes_indexes {
	my $list= shift;
	my %chrom_indexes = {};
	my @chr_numbers = ();
	my @chr_strings = ();
	my %visited = {};
	foreach my $chr_id(@{$list}) {
		if(not defined $visited{$chr_id}) {
			if($chr_id =~ m/\d/) {
				push(@chr_numbers, $chr_id);
			}else {
				push(@chr_strings, $chr_id);
			}
			$visited{$chr_id} = 1;
		}
	}
	
	my @sorted_numbers = sort { $a <=> $b } @chr_numbers;
	my @sorted_strings = sort { $a <=> $b } @chr_strings;
	foreach my $chr_id(@sorted_strings) {
		push(@sorted_numbers, $chr_id);
	}
	
	for(my $i=0; $i<@sorted_numbers; $i++) {
		$chrom_indexes{$sorted_numbers[$i]} = $i;
	}
#	print Dumper \%chrom_indexes;
	return \%chrom_indexes;
}

sub comp_chr {
	
}

our sub unique {
	my @list = shift(@_);
	my %seen = ();
	my @uniqu = grep { ! $seen{$_} ++ } @list;
	return @uniqu;
}

1;
