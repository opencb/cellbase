
db.getCollection('protein_functional_prediction').ensureIndex({'checksum': 1})
db.getCollection('protein_functional_prediction').ensureIndex({'uniprotId': 1})
db.getCollection('protein_functional_prediction').ensureIndex({'transcriptId': 1})
