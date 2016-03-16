
/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

db.getCollection('clinical').createIndex({'id': 1})
db.getCollection('clinical').createIndex({'chromosome': 1, 'start': 1, 'end': 1})
db.getCollection('clinical').createIndex({'clinvarSet.referenceClinVarAssertion.clinVarAccession.acc': 1})
//db.getCollection('clinical').createIndex({'clinvarSet.referenceClinVarAssertion.traitSet.trait.name.elementValue.value': 'text'})
db.getCollection('clinical').createIndex({'clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id': 1})
db.getCollection('clinical').createIndex({'clinvarSet.referenceClinVarAssertion.measureSet.measure.type': 1})
db.getCollection('clinical').createIndex({'_geneIds': 1})
db.getCollection('clinical').createIndex({'_phenotypes': 'text'})
db.getCollection('clinical').createIndex({'source': 1})
//db.getCollection('clinical').createIndex({'annot.consequenceTypes.soTerms.soName': 1})
db.getCollection('clinical').createIndex({'clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus': 1})
db.getCollection('clinical').createIndex({'clinvarSet.referenceClinVarAssertion.clinicalSignificance.description': 1})
db.getCollection('clinical').createIndex({'primarySite': 'text'})
db.getCollection('clinical').createIndex({'siteSubtype': 'text'})
db.getCollection('clinical').createIndex({'primaryHistology': 'text'})
db.getCollection('clinical').createIndex({'histologySubtype': 'text'})
db.getCollection('clinical').createIndex({'studies.traits.diseaseTrait': 'text'})
