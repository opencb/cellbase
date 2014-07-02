package org.opencb.cellbase.core.serializer;

/**
 * Created by imedina on 17/06/14.
 */
public interface CellBaseTypeConverter<DataModel, StorageSchema> {

    public StorageSchema convertToStorageSchema(DataModel dataModel);

    public DataModel convertToDataModel(StorageSchema storageSchema);

}
