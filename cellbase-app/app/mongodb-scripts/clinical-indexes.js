
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
db.getCollection('clinical_variants').createIndex({'_sources': 1})
db.getCollection('clinical_variants').createIndex({'_accessions': 1})
db.getCollection('clinical_variants').createIndex({'_reviewStatus': 1})
db.getCollection('clinical_variants').createIndex({'_clinicalSignificance': 1})
db.getCollection('clinical_variants').createIndex({'annotation.variantTraitAssociation.germline.phenotype':'text',
    'annotation.variantTraitAssociation.germline.disease':'text',
    'annotation.variantTraitAssociation.somatic.primarySite': 'text',
    'annotation.variantTraitAssociation.somatic.siteSubtype': 'text',
    'annotation.variantTraitAssociation.somatic.primaryHistology': 'text',
    'annotation.variantTraitAssociation.somatic.histologySubtype': 'text',
    'annotation.variantTraitAssociation.somatic.sampleSource': 'text',
    'annotation.variantTraitAssociation.somatic.tumourOrigin': 'text'}, {name: "_diseasePhenotype"})
