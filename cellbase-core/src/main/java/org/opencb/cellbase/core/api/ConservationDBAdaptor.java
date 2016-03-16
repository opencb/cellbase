package org.opencb.cellbase.core.api;

import org.opencb.biodata.models.core.Region;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by swaathi on 26/11/15.
 */
@Deprecated
public interface ConservationDBAdaptor<T> extends CellBaseDBAdaptor<T> {

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

    List<QueryResult> getAllByRegionList(List<Region> regionList, QueryOptions options);

    @Deprecated
    List<QueryResult> getAllScoresByRegionList(List<Region> regions, QueryOptions options);

}
