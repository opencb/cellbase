
db.getCollection('variation_phenotype').ensureIndex({'phenotype': 1})
db.getCollection('variation_phenotype').ensureIndex({'associatedGenes': 1})
