
db.getCollection('variation_phenotype_annotation').ensureIndex({'id': 1})
db.getCollection('variation_phenotype_annotation').ensureIndex({'chunkIds': 1})
db.getCollection('variation_phenotype_annotation').ensureIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('variation_phenotype_annotation').ensureIndex({'phenotype': 1})
db.getCollection('variation_phenotype_annotation').ensureIndex({'source': 1})
db.getCollection('variation_phenotype_annotation').ensureIndex({'associatedGenes': 1})

