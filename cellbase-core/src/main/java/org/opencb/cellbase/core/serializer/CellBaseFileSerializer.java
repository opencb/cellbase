package org.opencb.cellbase.core.serializer;

/**
 * Created by parce on 9/02/15.
 */
public interface CellBaseFileSerializer extends CellBaseSerializer {
    public void serialize(Object object, String fileName);
}
