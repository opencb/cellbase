
db.getCollection('genome_sequence').ensureIndex({'_chunkIds': 1})
db.getCollection('genome_sequence').ensureIndex({'sequenceType': 1})