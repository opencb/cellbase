MongoDB installation
======================

Assuming you already have built or downloaded the JSON data files the installation consist of using just mongoimport itulity from MongoDB.

CellBase MongoDB database consist of the following collections:
conserved_region
core
drugbank
genome_sequence
info_stats
mutation
pathway
protein
protein_functional_prediction
protein_protein_interaction
regulatory_region
variation
variation_phenotype_annotation


To have the basic functionality such as genome and gene annotations for a genome browser only three collections are needed. They can be installed by executing:

mongoimport -u root -p XXX --directoryperdb -d hsapiens_cdb_v3_3710 -c info_stats --file info_stats.json
mongoimport -u root -p XXX --directoryperdb -d hsapiens_cdb_v3_3710 -c genome_sequence --file genome_sequence.json
mongoimport -u root -p XXX --directoryperdb -d hsapiens_cdb_v3_3710 -c core --file gene.json

Where hsapiens_cdb_v3_3710 is the name of database for human, this can be changed as long as it is also changed in the Java web service package. The rest of collections can be installed in the same way.
