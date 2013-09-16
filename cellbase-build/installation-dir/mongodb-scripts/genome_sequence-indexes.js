
db.getCollection('genome_sequence').ensureIndex({'chromosome': 1, 'chunk': 1})
db.getCollection('genome_sequence').ensureIndex({'sequenceType': 1})