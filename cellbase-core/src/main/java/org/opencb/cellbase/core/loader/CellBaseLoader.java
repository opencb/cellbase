package org.opencb.cellbase.core.loader;

import org.opencb.cellbase.core.CellBaseConfiguration;
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

    protected final BlockingQueue<List<String>> blockingQueue;
    protected String data;
    protected String database;
    protected Map<String, String> loaderParams;

    protected CellBaseConfiguration cellBaseConfiguration;

    protected final Logger logger;

    public CellBaseLoader (BlockingQueue<List<String>> blockingQueue, String data, String database,
                           Map<String, String> loaderParams) {
        this(blockingQueue, data, database, loaderParams, null);
    }

    public CellBaseLoader (BlockingQueue<List<String>> blockingQueue, String data, String database,
                           Map<String, String> loaderParams, CellBaseConfiguration cellBaseConfiguration) {
        this.blockingQueue = blockingQueue;
        this.data = data;
        this.database = database;
        this.loaderParams = loaderParams;

        if(cellBaseConfiguration != null) {
            this.cellBaseConfiguration = cellBaseConfiguration;
        }else {
            try {
                this.cellBaseConfiguration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void init() throws LoaderException;

    @Override
    public abstract Integer call();

    public abstract void createIndex(String data) throws LoaderException;

    public abstract void close();

}
