package org.opencb.cellbase.core.api;

import org.opencb.commons.datastore.core.QueryParam;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by swaathi on 08/12/15.
 */
public interface RegulationDBAdaptor<RegulatoryRegion> extends CellBaseDBAdaptor<RegulatoryRegion> {

    enum QueryParams implements QueryParam {
        NAME("name", TEXT_ARRAY, ""),
        REGION("region", TEXT_ARRAY, ""),
        FEATURE_TYPE("featureType", TEXT_ARRAY, ""),
        FEATURE_CLASS("featureClass", TEXT_ARRAY, ""),
        CELL_TYPES("cellTypes", TEXT_ARRAY, "");

        QueryParams(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        private final String key;
        private Type type;
        private String description;

        @Override
        public String key() {
            return null;
        }

        @Override
        public String description() {
            return null;
        }

        @Override
        public Type type() {
            return null;
        }
    }
}
