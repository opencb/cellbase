package org.opencb.cellbase.mongodb.loader;

import com.mongodb.BulkWriteResult;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.core.loader.LoaderException;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDBConfiguration;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.opencb.datastore.mongodb.MongoDataStoreManager;

import java.io.IOException;
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
    private String databaseName;

    public MongoDBCellBaseLoader(BlockingQueue<List<String>> queue, String data, Map<String, String> params) {
        super(queue, data, params);
    }

    @Override
    public void init() throws LoaderException {
        String collectionName = this.getCollectionName(data);
        createConnection();
        collection = dataStore.getCollection(collectionName);
        getChunkSizes(collectionName);
    }


    private String getCollectionName(String data) throws LoaderException {
        String collectionName;
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
            default:
                throw new LoaderException("Unknown collection to store " + data);
        }

        return collectionName;
    }

    private void createConnection() throws LoaderException {
        try {
            CellBaseConfiguration configuration = CellBaseConfiguration.load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            dataStoreManager = new MongoDataStoreManager(getHost(configuration), getPort(configuration));
            MongoDBConfiguration credentials = MongoDBConfiguration.builder().add("username", getUser(configuration)).add("password", getPassword(configuration)).build();
            if (params.containsKey(CELLBASE_DATABASE_NAME_PROPERTY)) {
                databaseName = params.get(CELLBASE_DATABASE_NAME_PROPERTY);
            } else {
                databaseName = CELLBASE_DEFAULT_DATABASE_NAME;
            }
            dataStore = dataStoreManager.get(databaseName, credentials);
        } catch (IOException e) {
            throw new LoaderException(e);
        }
    }

    private String getHost(CellBaseConfiguration configuration) {
        return getParam(CellBaseLoader.CELLBASE_HOST, configuration.getDatabase().getHost());
    }

    private int getPort(CellBaseConfiguration configuration) {
        return Integer.parseInt(getParam(CellBaseLoader.CELLBASE_PORT, configuration.getDatabase().getPort()));
    }

    private String getUser(CellBaseConfiguration configuration) {
        return getParam(CellBaseLoader.CELLBASE_USER, configuration.getDatabase().getUser());
    }

    private String getPassword(CellBaseConfiguration configuration) {
        return getParam(CellBaseLoader.CELLBASE_PASSWORD, configuration.getDatabase().getPassword());
    }

    private String getParam(String paramName, String defaultValue) {
        if (params.containsKey(paramName)) {
            return params.get(paramName);
        } else {
            return defaultValue;
        }
    }

    private void getChunkSizes(String collectionName) {
        if (collectionName != null) {
            switch (collectionName) {
                case "gene":
                    chunkSizes = new int[]{5000};
                    break;
                case "variation":
                    chunkSizes = new int[]{5000};
                    break;
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
        dataStoreManager.close(databaseName);
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
        DBObject dbObject = (DBObject) JSON.parse(jsonLine);
        addChunkId(dbObject);
        return dbObject;
    }

    private void addChunkId(DBObject dbObject) {
        if (chunkSizes != null && chunkSizes.length > 0) {
            List<String> chunkIds = new ArrayList<>();
            for (int chunkSize : chunkSizes) {
                int chunkStart = (Integer) dbObject.get("start") / chunkSize;
                int chunkEnd = (Integer) dbObject.get("end") / chunkSize;
                String chunkIdSuffix = chunkSize / 1000 + "k";
                for (int i = chunkStart; i <= chunkEnd; i++) {
                    chunkIds.add(dbObject.get("chromosome") + "_" + i + "_" + chunkIdSuffix);
                }
            }
            dbObject.put("chunkIds", chunkIds);
        }
    }
}
