
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

db.getCollection('variation_phenotype_annotation').createIndex({'id': 1})
db.getCollection('variation_phenotype_annotation').createIndex({'chunkIds': 1})
db.getCollection('variation_phenotype_annotation').createIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('variation_phenotype_annotation').createIndex({'phenotype': 1})
db.getCollection('variation_phenotype_annotation').createIndex({'source': 1})
db.getCollection('variation_phenotype_annotation').createIndex({'associatedGenes': 1})

