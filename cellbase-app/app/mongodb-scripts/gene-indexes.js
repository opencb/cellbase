
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

db.getCollection('gene').createIndex({'id': 1})
db.getCollection('gene').createIndex({'name': 1})
db.getCollection('gene').createIndex({'biotype': 1})
db.getCollection('gene').createIndex({'_chunkIds': 1})
db.getCollection('gene').createIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('gene').createIndex({'transcripts.id': 1})
db.getCollection('gene').createIndex({'transcripts.name': 1})
db.getCollection('gene').createIndex({'transcripts.biotype': 1})
db.getCollection('gene').createIndex({'transcripts.chromosome': 1, 'transcripts.start': 1, 'transcripts.end': 1})
db.getCollection('gene').createIndex({'transcripts.xrefs.id': 1})
db.getCollection('gene').createIndex({'transcripts.xrefs.dbName': 1})
db.getCollection('gene').createIndex({'transcripts.xrefs.dbDisplayName': 1})
db.getCollection('gene').createIndex({'transcripts.exons.id': 1})
db.getCollection('gene').createIndex({'transcripts.exons.chromosome': 1, 'transcripts.exons.start': 1, 'transcripts.exons.end': 1})