MongoDB installation
======================

Assuming you already have built or downloaded the JSON data files the installation consist of using just mongoimport utility from MongoDB. Collections are installed in *localhost* which is the default, if you need to install in any other host just add *--host hostname* to command lines.

CellBase MongoDB database consist of the following collections:
```
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
```

To have the basic functionality such as genome and gene annotations for a genome browser only three collections are needed. They can be installed by executing:
```
  mongoimport -u root -p XXX --directoryperdb -d hsapiens_cdb_v3_3710 -c info_stats --file info_stats.json
  mongoimport -u root -p XXX --directoryperdb -d hsapiens_cdb_v3_3710 -c genome_sequence --file genome_sequence.json
  mongoimport -u root -p XXX --directoryperdb -d hsapiens_cdb_v3_3710 -c core --file gene.json
```

Where *hsapiens_cdb_v3_3710* is the name of database for human, this can be changed as long as it is also changed in the Java web service package. The rest of collections can be installed in the same way.

Indexes must be created in order to get an acceptable speed, indexes can be found at https://github.com/opencb/cellbase/tree/develop/cellbase-build/installation-dir/mongodb-scripts
```
mongo -u root -p XXX hsapiens_cdb_v3_3710 gene-indexes.js
mongo -u root -p XXX hsapiens_cdb_v3_3710 genome_sequence-indexes.js

```
Notice that *info_stats* does not need indexes as it's a one-only document collection.


Web Server installation
=======================
A Java 7, Tomcat 7 and Maven 3.x are needed. You can build the _war_ file by executing:
```
mvn clean install -DskipTests
```

in the root of the CellBase project and copy the _war_ file int Tomcat _webapps_ folder.

