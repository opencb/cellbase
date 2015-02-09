package org.opencb.cellbase.app.serializers;

import java.io.IOException;

/**
 * Created by parce on 9/02/15.
 */
public interface CellBaseSerializer {

    public void serialize(Object object);

    public void close() throws IOException;
}
