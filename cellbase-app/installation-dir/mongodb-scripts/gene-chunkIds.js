
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

