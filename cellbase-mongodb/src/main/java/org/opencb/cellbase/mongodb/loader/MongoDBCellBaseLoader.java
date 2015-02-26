package org.opencb.cellbase.mongodb.loader;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.opencb.datastore.mongodb.MongoDataStoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by parce on 18/02/15.
 */
public class MongoDBCellBaseLoader extends CellBaseLoader {

    private int[] chunkSizes;
    private String collectionName;
    private MongoDataStore dataStore;
    private MongoDBCollection collection;

//    public MongoDBLoader(BlockingQueue<List<String>> queue, String collectionName, MongoDataStore dataStore) {
//        super(queue);
//        this.collectionName = collectionName;
//        this.dataStore = dataStore;
//        getChunkSizes();
//    }

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, Map<String, String> params) {
        super(queue, params);
        // TODO calculate the chunkIds
        getChunkSizes();
//        this.chunkSizes = chunkSizes;
    }

    private void getChunkSizes() {
        int[] chunkSizes = null;
        switch (collectionName) {
            case "gene":
                // TODO: use real chunk sizes here
                chunkSizes = new int[]{1000, 10000};
                break;
            case "variation":
                // TODO: use real chunk sizes here
                chunkSizes = new int[]{1000};
                break;
            // TODO: add all chunk sizes cases
        }
    }


    @Override
    public void init() {
        collection = dataStore.getCollection(collectionName);
    }

    public void load(List<DBObject> batch) {
        // TODO: queryOptions?
        collection.insert(batch, new QueryOptions());
    }

    @Override
    public void disconnect() {
        // TODO: the conection just can be closed in the DataStoreManager
    }

    @Override
    public Integer call() {
        Integer loadedObjects = 0;
        boolean finished = false;
        while (!finished) {
            try {
                List<String> batch = queue.take();
                if (batch == LoadRunner.POISON_PILL) {
                    finished = true;
                } else {
                    List<DBObject> dbObjectsBatch = new ArrayList<>(batch.size());
                    for (String jsonLine : batch) {
                        DBObject dbObject = getDbObject(jsonLine);
                        dbObjectsBatch.add(dbObject);
                        loadedObjects++;
                    }
                    this.load(dbObjectsBatch);
                }
            } catch (InterruptedException e) {
                logger.error("Loader thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error Loading batch: " + e.getMessage());
            }
        }
        logger.debug("'load' finished. " + loadedObjects + " records serialized");
        return loadedObjects;
    }

    private DBObject getDbObject(String jsonLine) {
        try {
            DBObject dbObject = (DBObject) JSON.parse(jsonLine);
            if (chunkSizes != null && chunkSizes.length > 0) {
                // TODO: get chunkid
                // TODO: add chunkid to dbobject
            }
            return dbObject;
        } catch (Exception e) {
            throw e;
        }
    }
}
