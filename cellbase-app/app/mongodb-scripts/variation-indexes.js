
db.getCollection('variation').ensureIndex({'id': 1})
db.getCollection('variation').ensureIndex({'chunkIds': 1})
db.getCollection('variation').ensureIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('variation').ensureIndex({'type': 1})
db.getCollection('variation').ensureIndex({'consequenceTypes': 1})
db.getCollection('variation').ensureIndex({'transcriptVariations.transcriptId': 1})
db.getCollection('variation').ensureIndex({'transcriptVariations.consequenceTypes': 1})
db.getCollection('variation').ensureIndex({'xrefs.crossReference': 1})