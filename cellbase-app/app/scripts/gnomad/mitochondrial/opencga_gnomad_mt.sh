#!/bin/bash

# Variables
user="user"
host="host_name"
project="population"
project_name="Population"
study="gnomad_mt"
study_name="gnomAD v3.1 Mitocondrial DNA Variants"
study_path="data/"$study
folder_path="/home/gnomad_mt"
mapping_file="mapping_file_gnomad_mt_mod_file.txt"
vcf_file="gnomad.genomes.v3.1.sites.chrM.mod.vcf.gz"
mapping_file_path=$folder_path$mapping_file
vcf_file_path=$folder_path$vcf_file

# Login
/home/opencga-client-2.12.0/bin/opencga.sh login $user --host $host

# Project creation
/home/opencga-client-2.12.0/bin/opencga.sh projects create --id $project --name $project_name --organism-scientific-name hsapiens --organism-assembly grch38 --host $host

# Study creation
/home/opencga-client-2.12.0/bin/opencga.sh studies create --id $study --name $study_name --project $project --host $host

# Folders creation within Catalog
/home/opencga-client-2.12.0/bin/opencga.sh files create --path $study_path --parents --study $study --type DIRECTORY --host $host

# Uploading gnomad mt variants VCF and mapping file for gnomad mt variants
/home/opencga-client-2.12.0/bin/opencga.sh files upload -i $mapping_file_path --path $study_path --study $study --host $host

/home/opencga-client-2.12.0/bin/opencga.sh files upload -i $vcf_file_path --path $study_path --study $study --host $host

# Variant index for gnomad mt variants VCF
/home/opencga-client-2.12.0/bin/opencga.sh operations variant-index --study $study --file $vcf_file --load-archive NO --load-split-data CHROMOSOME --host $host

# Variant stats index for gnomad mt variants. The corresponding cohorts and variant cohort stats will be generated using the information of interest provided in the mapping file and INFO column of the gnomad mt VCF
/home/opencga-client-2.12.0/bin/opencga.sh operations variant-stats-index --study $study --aggregation-mapping-file $mapping_file --aggregated BASIC --host $host

# Variant cohort stats will be converted to population frequencies data model (julie-tool)
/home/opencga-client-2.12.0/bin/opencga.sh operations variant-julie-run --project $project --host $host

# Export of annotation.populationFrequencies in json format
/home/opencga-client-2.12.0/bin/opencga.sh variant export-run --body_include annotation.populationFrequencies --body_project $project --project $project --output-file-format json --host $host
