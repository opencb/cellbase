package org.opencb.cellbase.app.transform;

import org.opencb.cellbase.app.serializers.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by imedina on 30/08/14.
 */
public abstract class CellBaseParser {

    protected CellBaseSerializer serializer;

    protected Logger logger;

    public CellBaseParser(CellBaseSerializer serializer) {
        logger = LoggerFactory.getLogger(this.getClass());

        this.serializer = serializer;
        //this.serializer.open();
    }

    public abstract void parse() throws Exception;

    public void disconnect() {
        try {
            serializer.close();
        } catch (Exception e) {
            logger.error("Disconnecting serializer: " + e.getMessage());
        }
    }

}