
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

db.getCollection('clinical_variants').createIndex({'id': 1})
db.getCollection('clinical_variants').createIndex({'type': 1})
db.getCollection('clinical_variants').createIndex({'chromosome': 1, 'start': 1, 'end': 1})
db.getCollection('clinical_variants').createIndex({'annotation.consequenceTypes.sequenceOntologyTerms.name': 1})
db.getCollection('clinical_variants').createIndex({'_featureXrefs': 1})
db.getCollection('clinical_variants').createIndex({'annotation.traitAssociation.id': 1})
db.getCollection('clinical_variants').createIndex({'annotation.traitAssociation.consistencyStatus': 1}, {sparse: true})
db.getCollection('clinical_variants').createIndex({'annotation.traitAssociation.variantClassification.clinicalSignificance': 1}, {sparse: true})
db.getCollection('clinical_variants').createIndex({'annotation.traitAssociation.heritableTraits.inheritanceMode': 1}, {sparse: true})
db.getCollection('clinical_variants').createIndex({'annotation.traitAssociation.alleleOrigin': 1}, {sparse: true})
db.getCollection('clinical_variants').createIndex({'annotation.traitAssociation.heritableTraits.trait':'text',
    'annotation.traitAssociation.somaticInformation.primarySite': 'text',
    'annotation.traitAssociation.somaticInformation.siteSubtype': 'text',
    'annotation.traitAssociation.somaticInformation.primaryHistology': 'text',
    'annotation.traitAssociation.somaticInformation.histologySubtype': 'text',
    'annotation.traitAssociation.somaticInformation.sampleSource': 'text',
    'annotation.traitAssociation.somaticInformation.tumourOrigin': 'text'}, {name: "_diseasePhenotype"})
