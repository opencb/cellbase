package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by imedina on 30/08/14.
 */
public abstract class CellBaseParser {

    private CellBaseSerializer serializer;

    protected Logger logger;

    public CellBaseParser(CellBaseSerializer serializer) {
        logger = LoggerFactory.getLogger(this.getClass());

        this.serializer = serializer;
        //this.serializer.open();

    }

    public abstract void parse() throws Exception;

    protected void serialize(Object data) {
        this.serializer.serialize(data);
    }

    public void disconnect() {
        try {
            serializer.close();
        } catch (Exception e) {
            logger.error("Disconnecting serializer: " + e.getMessage());
        }
    }

}