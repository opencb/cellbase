#!/bin/sh

set -e 

PERL5LIB=${PERL5LIB}:/opt/bioperl-live
PERL5LIB=${PERL5LIB}:/opt/ensembl/modules
PERL5LIB=${PERL5LIB}:/opt/ensembl-compara/modules
PERL5LIB=${PERL5LIB}:/opt/ensembl-variation/modules
PERL5LIB=${PERL5LIB}:/opt/ensembl-funcgen/modules
PERL5LIB=${PERL5LIB}:/opt/lib/perl/5.18.2/
PERL5LIB=${PERL5LIB}:/opt/
export PERL5LIB

#PATH=${PATH}:/opt/tabix/
#export PATH

#sed -i.backup "s/  host = ensembldb.ensembl.org/  host = ensembl-db.bridge/" /opt/ensembl-rest/ensembl_rest.conf
#sed -i.backup "s/  port = 5306/  host = 3306/" /opt/ensembl-rest/ensembl_rest.conf

#perl /opt/gene_extra_info.pl
#perl /opt/protein_function_prediction_matrices.pl
perl /opt/genome_info.pl

"$@"
