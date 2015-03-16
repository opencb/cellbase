package org.opencb.cellbase.mongodb.loader;

import org.opencb.cellbase.core.loader.CellBaseLoader;
import org.opencb.cellbase.core.loader.LoadRunner;

import java.nio.file.Path;


/**
 * Created by parce on 18/02/15.
 */
@Deprecated
public class MongoDBLoadRunner extends LoadRunner {

    private final String collection;

    public MongoDBLoadRunner(Path inputJsonFile, String collection, int threadsNumber) {
        super(inputJsonFile, threadsNumber);
        this.collection = collection;
    }

    protected CellBaseLoader createConsumer() {
        int[] chunkSizes = null;
        switch (collection) {
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
        return new MongoDBCellBaseLoader(queue, chunkSizes, collection);
    }
}
