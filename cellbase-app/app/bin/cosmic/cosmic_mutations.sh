#!/bin/bash

##################################################################
## COSMIC MUTATIONS TO CELLBASE TABLE
## 27/03/2012 - Marta Bleda Latorre
## Usage:
##      ./cosmic_mutations.sh CosmicCompleteExport_v58_140312.tsv
##################################################################

MINPARAMS=1

## Check file
if [ $# -lt "$MINPARAMS" ]
then
  echo "ERROR:"
  echo "This script needs the COSMIC mutations file!"
  echo "Mutations file can be fount at ftp://ftp.sanger.ac.uk/pub/CGP/cosmic/data_export/"
  echo "Usage:"
  echo -e "   ./cosmic_mutations.sh CosmicCompleteExport_v58_140312.tsv"
fi 

## Not all entries contain coordinates of the mutation so, we need to filter
awk -F'\t' '{if ($19 != "" && $0 !~ /^Gene/) print $19"\t"$1"\t\t"$2"\t"$7"\t"$8"\t"$9"\t"$13"\t"$14"\t"$15"\t"$16"\t"$22"\t"$25"\tCOSMIC"}' $1 | sed 's/:/\t/' | sed 's/-/\t/' | awk -F"\t" 'BEGIN{count=1} {print count"\t"$0; count=count+1}' > cosmic_mutations.txt

if [ -f cosmic_mutations.txt ];
	then 
	fields=`awk -F'\t' '{print NF}' cosmic_mutations.txt | sort | uniq`
	echo "File succesfully created!"
	echo "Number of fields: $fields"
else
	echo "ERROR: file not created."
fi
exit 0
