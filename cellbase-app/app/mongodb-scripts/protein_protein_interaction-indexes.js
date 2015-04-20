
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

db.getCollection('protein_protein_interaction').ensureIndex({'interactorA.id': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'interactorA.dbName': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'interactorA.xrefs.id': 1})
//db.getCollection('protein_protein_interaction').ensureIndex({'interactorA.xrefs.dbName': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'interactorB.id': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'interactorB.dbName': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'interactorB.xrefs.id': 1})
//db.getCollection('protein_protein_interaction').ensureIndex({'interactorB.xrefs.dbName': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'type.psimi': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'type.name': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'detectionMethod.psimi': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'detectionMethod.name': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'status': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'xrefs.id': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'xrefs.dbName': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'source.psimi': 1})
db.getCollection('protein_protein_interaction').ensureIndex({'source.name': 1})


