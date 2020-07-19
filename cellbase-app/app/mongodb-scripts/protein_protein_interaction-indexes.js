
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

db.getCollection('protein_protein_interaction').createIndex({'interactorA.id': 1})
db.getCollection('protein_protein_interaction').createIndex({'interactorA.dbName': 1})
db.getCollection('protein_protein_interaction').createIndex({'interactorA.xrefs.id': 1})
//db.getCollection('protein_protein_interaction').createIndex({'interactorA.xrefs.dbName': 1})
db.getCollection('protein_protein_interaction').createIndex({'interactorB.id': 1})
db.getCollection('protein_protein_interaction').createIndex({'interactorB.dbName': 1})
db.getCollection('protein_protein_interaction').createIndex({'interactorB.xrefs.id': 1})
//db.getCollection('protein_protein_interaction').createIndex({'interactorB.xrefs.dbName': 1})
db.getCollection('protein_protein_interaction').createIndex({'type.psimi': 1})
db.getCollection('protein_protein_interaction').createIndex({'type.name': 1})
db.getCollection('protein_protein_interaction').createIndex({'detectionMethod.psimi': 1})
db.getCollection('protein_protein_interaction').createIndex({'detectionMethod.name': 1})
db.getCollection('protein_protein_interaction').createIndex({'status': 1})
db.getCollection('protein_protein_interaction').createIndex({'xrefs.id': 1})
db.getCollection('protein_protein_interaction').createIndex({'xrefs.dbName': 1})
db.getCollection('protein_protein_interaction').createIndex({'source.psimi': 1})
db.getCollection('protein_protein_interaction').createIndex({'source.name': 1})


