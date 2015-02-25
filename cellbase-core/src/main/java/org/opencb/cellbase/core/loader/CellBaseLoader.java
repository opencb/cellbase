package org.opencb.cellbase.core.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by parce on 18/02/15.
 */
public abstract class CellBaseLoader implements Callable<Integer> {

    protected final BlockingQueue<List<String>> queue;
    protected final Logger logger;

    public CellBaseLoader (BlockingQueue<List<String>> queue) {
        this.queue = queue;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public abstract void init();

    public abstract void disconnect();

    @Override
    public abstract Integer call();
}
