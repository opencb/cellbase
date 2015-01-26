
var CHUNK_SIZE = 1000;

function insertChunkIds(doc) {
   var chunkStart = Math.floor(doc.start/CHUNK_SIZE);
   var chunkEnd = Math.floor(doc.end/CHUNK_SIZE);

   var chunkIds = [];
   for(var i = chunkStart; chunkStart <= chunkEnd; chunkStart++) {
        chunkIds.push(doc.chromosome + "_" + chunkStart + "_1k")
   }
   print (chunkIds);
   db.variation.update({ "_id": doc._id}, {$set: {"chunkIds": chunkIds }})
}

db.variation.find().forEach(insertChunkIds);

