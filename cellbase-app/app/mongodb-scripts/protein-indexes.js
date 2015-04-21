
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

db.getCollection('protein').ensureIndex({'accession': 1})
db.getCollection('protein').ensureIndex({'name': 1})
db.getCollection('protein').ensureIndex({'protein.recommendedName.fullName.value': 1})
db.getCollection('protein').ensureIndex({'gene.name.value': 1})
db.getCollection('protein').ensureIndex({'dbReference.id': 1})
db.getCollection('protein').ensureIndex({'dbReference.type': 1})
db.getCollection('protein').ensureIndex({'keyword.id': 1})
db.getCollection('protein').ensureIndex({'keyword.value': 1})
db.getCollection('protein').ensureIndex({'feature.id': 1})
db.getCollection('protein').ensureIndex({'feature.type': 1})
db.getCollection('protein').ensureIndex({'feature.location.position.position': 1})
db.getCollection('protein').ensureIndex({'sequence.checksum': 1})
