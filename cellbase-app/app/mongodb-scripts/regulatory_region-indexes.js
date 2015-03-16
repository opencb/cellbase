
db.getCollection('regulatory_region').ensureIndex({'chunkIds': 1})
db.getCollection('regulatory_region').ensureIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('regulatory_region').ensureIndex({'name': 1})
db.getCollection('regulatory_region').ensureIndex({'featureType': 1})
db.getCollection('regulatory_region').ensureIndex({'featureClass': 1})