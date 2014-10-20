package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.build.serializers.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by imedina on 30/08/14.
 */
public abstract class CellBaseParser {

    private CellBaseSerializer serializer;

    protected Logger logger;

    public CellBaseParser(CellBaseSerializer serializer) {
        logger = LoggerFactory.getLogger(this.getClass());

        this.serializer = serializer;
        this.serializer.open();

    }

    public abstract void parse() throws Exception;

    protected boolean serialize(Object data) {
        return this.serializer.serialize(data);
    }

    public boolean disconnect() throws Exception {
        return this.serializer.close();
    }

}