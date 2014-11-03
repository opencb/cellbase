package org.opencb.cellbase.lib.mongodb.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by parce on 10/31/14.
 */
public abstract class MongoDBLoader {
    protected final Logger logger;
    protected String host;
    protected int port;
    protected String user;
    protected String password;

    public MongoDBLoader(String user, int port, String password, String host) {
        this.user = user;
        this.port = port;
        this.password = password;
        this.host = host;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void load();
}
