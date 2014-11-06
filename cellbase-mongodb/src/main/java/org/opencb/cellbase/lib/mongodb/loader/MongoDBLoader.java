package org.opencb.cellbase.lib.mongodb.loader;

import org.opencb.cellbase.lib.mongodb.serializer.CellbaseMongoDBSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by parce on 10/31/14.
 */
public abstract class MongoDBLoader {

    protected final Logger logger;
    protected CellbaseMongoDBSerializer serializer;

    public MongoDBLoader(CellbaseMongoDBSerializer serializer) {
        this.serializer = serializer;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void load();
}
