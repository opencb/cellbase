
db.getCollection('drugbank').ensureIndex({'type': 1})
db.getCollection('drugbank').ensureIndex({'action': 1})
db.getCollection('drugbank').ensureIndex({'drug.xrefs.id': 1})
db.getCollection('drugbank').ensureIndex({'drug.xrefs.type': 1})
db.getCollection('drugbank').ensureIndex({'drug.xrefs.group': 1})
db.getCollection('drugbank').ensureIndex({'drug.xrefs.category': 1})
db.getCollection('drugbank').ensureIndex({'partner.xrefs.id': 1})
db.getCollection('drugbank').ensureIndex({'partner.essentiality': 1})
