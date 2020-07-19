
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

db.getCollection('drugbank').createIndex({'type': 1})
db.getCollection('drugbank').createIndex({'action': 1})
db.getCollection('drugbank').createIndex({'drug.xrefs.id': 1})
db.getCollection('drugbank').createIndex({'drug.type': 1})
db.getCollection('drugbank').createIndex({'drug.group': 1})
db.getCollection('drugbank').createIndex({'drug.category': 1})
db.getCollection('drugbank').createIndex({'partner.xrefs.id': 1})
db.getCollection('drugbank').createIndex({'partner.essentiality': 1})
