
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

var CHUNK_SIZE = 5000;

function insertChunkIds(doc) {
   var chunkStart = Math.floor((doc.start-5000)/CHUNK_SIZE);
   var chunkEnd = Math.floor((doc.end+5000)/CHUNK_SIZE);

   var chunkIds = [];
   for(var i = chunkStart; chunkStart <= chunkEnd; chunkStart++) {
        chunkIds.push(doc.chromosome + "_" + chunkStart + "_5k")
   }
   print (chunkIds);
   db.gene.update({ "_id": doc._id}, {$set: {"chunkIds": chunkIds }})
}

db.gene.find().forEach(insertChunkIds);

