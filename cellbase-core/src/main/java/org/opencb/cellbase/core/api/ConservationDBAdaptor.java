package org.opencb.cellbase.core.api;

import org.opencb.commons.datastore.core.QueryParam;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by swaathi on 26/11/15.
 */
public interface ConservationDBAdaptor extends CellBaseDBAdaptor {
<<<<<<< HEAD:cellbase-core/src/main/java/org/opencb/cellbase/core/api/ConservationDBAdaptor.java
=======

>>>>>>> c0e10ac80e8d8eab76d06141a8316a71ab50c560:cellbase-core/src/main/java/org/opencb/cellbase/core/api/ConservationDBAdaptor.java
    enum QueryParams implements QueryParam {
        REGION("region", TEXT_ARRAY, "");

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
