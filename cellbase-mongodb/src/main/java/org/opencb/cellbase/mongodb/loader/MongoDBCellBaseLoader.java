package org.opencb.cellbase.mongodb.loader;

import com.mongodb.BulkWriteResult;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDBConfiguration;
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
    private MongoDataStore dataStore;
    private MongoDBCollection collection;
    private MongoDataStoreManager dataStoreManager;

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, Map<String, String> params) {
        super(queue, data, params);
    }

    @Override
    public void init() {
        String collectionName = this.getCollectionName(data);
        createConnection();
        collection = dataStore.getCollection(collectionName);
        getChunkSizes(collectionName);
    }


    private String getCollectionName(String data) {
        String collectionName = null;
        switch (data) {
            case "cosmic":
            case "clinvar":
            case "gwas":
                collectionName = "clinical";
                break;
            case "gene":
                collectionName = "gene";
                break;
            case "variation":
                collectionName = "variation";
                break;
        }

        // TODO: throw a exception if data is not known

        return collectionName;
    }

    private void createConnection() {
        dataStoreManager = new MongoDataStoreManager(params.get(CellBaseLoader.CELLBASE_HOST), Integer.parseInt(params.get(CELLBASE_PORT)));
        MongoDBConfiguration credentials = MongoDBConfiguration.builder().add("username", params.get("user")).add("password", params.get("password")).build();
        dataStore = dataStoreManager.get(CELLBASE_DATABASE_NAME, credentials);
    }

    private void getChunkSizes(String collectionName) {
        if (collectionName != null) {
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
    }

    public int load(List<DBObject> batch) {
        // TODO: queryOptions?
        QueryResult<BulkWriteResult> result = collection.insert(batch, new QueryOptions());
        return result.first().getInsertedCount();
    }



    @Override
    public void disconnect() {
        dataStoreManager.close(CELLBASE_DATABASE_NAME);
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

                    }
                    loadedObjects += load(dbObjectsBatch);
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
