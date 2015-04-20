
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

db.getCollection('variation').ensureIndex({'id': 1})
db.getCollection('variation').ensureIndex({'_chunkIds': 1})
db.getCollection('variation').ensureIndex({'chromosome': 1, "start": 1, "end": 1})
db.getCollection('variation').ensureIndex({'type': 1})
db.getCollection('variation').ensureIndex({'consequenceTypes': 1})
db.getCollection('variation').ensureIndex({'transcriptVariations.transcriptId': 1})
db.getCollection('variation').ensureIndex({'transcriptVariations.consequenceTypes': 1})
db.getCollection('variation').ensureIndex({'xrefs.crossReference': 1})