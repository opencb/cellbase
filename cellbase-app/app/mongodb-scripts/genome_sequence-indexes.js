
db.getCollection('genome_sequence').ensureIndex({'chunkId': 1})
db.getCollection('genome_sequence').ensureIndex({'sequenceType': 1})