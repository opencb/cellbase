#!/bin/bash

# The original MirTarBase hsa_MTI.xlsx contains invalid Gene Symbols in 793 lines.
# To fix it, that file has to be converted to a CSV file, i.e.: hsa_MTI.csv
#
# After converting to CSV file, we can see the errors from the original file for the Gene Symbols (column 4),
# e.g.: 06-mar:
# MIRT050267,hsa-miR-25-3p,Homo sapiens,06-mar,10299,Homo sapiens,CLASH,Functional MTI (Weak),23622248
# MIRT051174,hsa-miR-16-5p,Homo sapiens,06-mar,10299,Homo sapiens,CLASH,Functional MTI (Weak),23622248
#
# This script fix those lines and convert the column 4 for a vaild Gene Symbol:
#
# MIRT050267,hsa-miR-25-3p,Homo sapiens,MARCHF6,10299,Homo sapiens,CLASH,Functional MTI (Weak),23622248
# MIRT051174,hsa-miR-16-5p,Homo sapiens,MARCHF6,10299,Homo sapiens,CLASH,Functional MTI (Weak),23622248

# Check the parameters number
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <csv_file>"
    exit 1
fi

# Check CSV file
csv_file="$1"
if [ ! -f "$csv_file" ]; then
    echo "CSV file '$csv_file' does not exist."
    exit 1
fi

# Fix gene-symbol
while IFS=$'\t' read -r c1 c2 c3 c4 c5 c6 c7 c8 c9 || [[ -n "$c1" ]]; do
    # Aplica las condiciones
    if [ "$c5" = "10299" ]; then
        c4="MARCHF6"
    elif [ "$c5" = "51257" ]; then
        c4="MARCHF2"
    elif [ "$c5" = "54708" ]; then
        c4="MARCHF5"
    elif [ "$c5" = "54996" ]; then
        c4="MTARC2"
    elif [ "$c5" = "55016" ]; then
        c4="MARCHF1"
    elif [ "$c5" = "57574" ]; then
        c4="MARCHF4"
    elif [ "$c5" = "64757" ]; then
        c4="MTARC1"
    elif [ "$c5" = "64844" ]; then
        c4="MARCHF7"
    elif [ "$c5" = "92979" ]; then
        c4="MARCHF9"
    elif [ "$c5" = "115123" ]; then
        c4="MARCHF3"
    elif [ "$c5" = "220972" ]; then
        c4="MARCHF8"
    elif [ "$c5" = "441061" ]; then
        c4="MARCHF11"
    fi

    # Print line
    echo -e "$c1\t$c2\t$c3\t$c4\t$c5\t$c6\t$c7\t$c8\t$c9"
done < "$csv_file"
