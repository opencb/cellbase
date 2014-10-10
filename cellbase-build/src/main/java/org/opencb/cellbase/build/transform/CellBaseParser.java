package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencb.cellbase.build.serializers.json.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Created by imedina on 30/08/14.
 */
public abstract class CellBaseParser {

    private JsonSerializer writer;

    protected Logger logger;

    public CellBaseParser(Path outFile) {
        logger = LoggerFactory.getLogger(this.getClass());

        this.writer = new JsonSerializer(outFile);
        this.writer.open();
        this.writer.pre();

    }

    public abstract void parse() throws Exception;

    protected boolean write(Object data) throws JsonProcessingException {
        return this.writer.write(data);
    }

    public boolean disconnect() throws Exception {
        return this.writer.post() && this.writer.close();
    }

}