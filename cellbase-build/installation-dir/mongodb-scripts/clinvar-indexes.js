
db.getCollection('clinvar').ensureIndex({'id': 1})
db.getCollection('clinvar').ensureIndex({'referenceClinVarAssertion.clinVarAccession.acc': 1})
db.getCollection('clinvar').ensureIndex({'referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.chr': 1, 'referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.start': 1, 'referenceClinVarAssertion.measureSet.measure.measureRelationship.sequenceLocation.stop': 1}, {name: "location_1"})