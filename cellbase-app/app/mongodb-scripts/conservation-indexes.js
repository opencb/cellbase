
db.getCollection('conservation').ensureIndex({'_chunkIds': 1})
db.getCollection('conservation').ensureIndex({'chromosome': 1, 'start': 1, 'end': 1})
