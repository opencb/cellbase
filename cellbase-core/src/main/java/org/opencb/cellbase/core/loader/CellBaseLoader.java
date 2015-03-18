package org.opencb.cellbase.core.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by parce on 18/02/15.
 */
public abstract class CellBaseLoader implements Callable<Integer> {

    public static final String CELLBASE_HOST = "host";
    public static final String CELLBASE_PORT = "port";
    public static final String CELLBASE_DATABASE_NAME_PROPERTY = "database";
    public static final String CELLBASE_DEFAULT_DATABASE_NAME = "cellbase";
    public static final String CELLBASE_USER = "user";
    public static final String CELLBASE_PASSWORD = "password";

    protected final BlockingQueue<List<String>> queue;
    protected final Logger logger;
    public String data;
    protected Map<String, String> params;

    public CellBaseLoader (BlockingQueue<List<String>> queue, String data, Map<String, String> params) {
        this.queue = queue;
        this.data = data;
        this.params = params;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void init() throws LoaderException;

    public abstract void disconnect();

    @Override
    public abstract Integer call();
}
