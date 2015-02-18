package org.opencb.cellbase.core.loader;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by parce on 18/02/15.
 */
public abstract class CellBaseLoader implements Runnable {

    protected final BlockingQueue<List<String>> queue;

    public CellBaseLoader (BlockingQueue<List<String>> queue) {
        this.queue = queue;
    }

    public abstract void init();

    public abstract void disconnect();
}
