package org.opencb.cellbase.app.serializers;

/**
 * Created by parce on 9/02/15.
 */
public interface CellBaseFileSerializer extends CellBaseSerializer {
    public void serialize(Object object, String fileName);
}
