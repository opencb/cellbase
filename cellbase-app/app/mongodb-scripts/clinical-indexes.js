
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
db.getCollection('clinical_variants').createIndex({'chromosome': 1, 'start': 1, 'end': 1})
db.getCollection('clinical_variants').createIndex({'annotation.consequenceTypes.sequenceOntologyTerms.name': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Germline.accession': 1, 'annotation.VariantTraitAssociation.Somatic.accession': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Germline.geneNames': 1, 'annotation.VariantTraitAssociation.Somatic.geneNames': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Germline.source': 1,'annotation.VariantTraitAssociation.Somatic.source': 1})
db.getCollection('clinical_variants').createIndex({'_phenotypesDiseases': 'text'})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Germline.reviewStatus': 1,'annotation.VariantTraitAssociation.Somatic.reviewStatus': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Germline.clinicalSignificance': 1,'annotation.VariantTraitAssociation.Somatic.clinicalSignificance': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Somatic.primarySite': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Somatic.siteSubtype': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Somatic.primaryHistology': 1})
db.getCollection('clinical_variants').createIndex({'annotation.VariantTraitAssociation.Somatic.histologySubtype': 1})
