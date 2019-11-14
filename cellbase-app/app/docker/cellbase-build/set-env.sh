#!/bin/sh

PERL5LIB=${PERL5LIB}:/opt/bioperl-live
PERL5LIB=${PERL5LIB}:/opt/ensembl/modules
PERL5LIB=${PERL5LIB}:/opt/ensembl-compara/modules
PERL5LIB=${PERL5LIB}:/opt/ensembl-variation/modules
PERL5LIB=${PERL5LIB}:/opt/ensembl-funcgen/modules
PERL5LIB=${PERL5LIB}:/opt/lib/perl/5.18.2/
PERL5LIB=${PERL5LIB}:/opt/
export PERL5LIB
