
db.getCollection('clinical').ensureIndex({'id': 1})
db.getCollection('clinical').ensureIndex({'chromosome': 1, 'start': 1, 'end': 1}, {name: "location_1"})
db.getCollection('clinical').ensureIndex({'clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value': 'text'})
db.getCollection('clinical').ensureIndex({'clinvarSet.referenceClinVarAssertion.clinVarAccession.acc': 1})
db.getCollection('clinical').ensureIndex({'clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id': 1})
db.getCollection('clinical').ensureIndex({'annot.consequenceTypes.soTerms.soName': 1})