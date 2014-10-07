
db.getCollection('regulatory_region').ensureIndex({'chunkIds': 1})
db.getCollection('regulatory_region').ensureIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('regulatory_region').ensureIndex({'name': 1})
db.getCollection('regulatory_region').ensureIndex({'featuresType': 1})
db.getCollection('regulatory_region').ensureIndex({'featuresClass': 1})