package org.opencb.cellbase.mongodb.loader;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by parce on 18/02/15.
 */
public class MongoDBCellBaseLoader extends CellBaseLoader {

    private int[] chunkSizes;
    private String collection;

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, Map<String, String> params) {
        super(queue, params);
        // TODO calculate the chunkIds
//        this.chunkSizes = chunkSizes;
        this.collection = collection;
    }

    @Deprecated
    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, int[] chunkSizes, String collection) {
        super(queue, null);
        this.chunkSizes = chunkSizes;
        this.collection = collection;
    }

    @Override
    public void init() {
        // TODO: establish connection using datastore
    }

    public void load(List<DBObject> batch) {
        // TODO: use datastore to load batch into collection 'collection'
    }

    @Override
    public void disconnect() {
        // TODO: close connection using datastore
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
