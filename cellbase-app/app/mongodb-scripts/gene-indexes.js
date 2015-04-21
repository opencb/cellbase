
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

db.getCollection('gene').ensureIndex({'id': 1})
db.getCollection('gene').ensureIndex({'name': 1})
db.getCollection('gene').ensureIndex({'biotype': 1})
db.getCollection('gene').ensureIndex({'_chunkIds': 1})
db.getCollection('gene').ensureIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('gene').ensureIndex({'transcripts.id': 1})
db.getCollection('gene').ensureIndex({'transcripts.name': 1})
db.getCollection('gene').ensureIndex({'transcripts.biotype': 1})
db.getCollection('gene').ensureIndex({'transcripts.chromosome': 1, 'transcripts.start': 1, 'transcripts.end': 1})
db.getCollection('gene').ensureIndex({'transcripts.xrefs.id': 1})
db.getCollection('gene').ensureIndex({'transcripts.xrefs.dbName': 1})
db.getCollection('gene').ensureIndex({'transcripts.xrefs.dbDisplayName': 1})
db.getCollection('gene').ensureIndex({'transcripts.exons.id': 1})
db.getCollection('gene').ensureIndex({'transcripts.exons.chromosome': 1, 'transcripts.exons.start': 1, 'transcripts.exons.end': 1})