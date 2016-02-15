
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

db.getCollection('protein').createIndex({'accession': 1})
db.getCollection('protein').createIndex({'name': 1})
db.getCollection('protein').createIndex({'protein.recommendedName.fullName.value': 1})
db.getCollection('protein').createIndex({'gene.name.value': 1})
db.getCollection('protein').createIndex({'dbReference.id': 1})
db.getCollection('protein').createIndex({'dbReference.type': 1})
db.getCollection('protein').createIndex({'keyword.id': 1})
db.getCollection('protein').createIndex({'keyword.value': 1})
db.getCollection('protein').createIndex({'feature.id': 1})
db.getCollection('protein').createIndex({'feature.type': 1})
db.getCollection('protein').createIndex({'feature.location.position.position': 1})
db.getCollection('protein').createIndex({'sequence.checksum': 1})
