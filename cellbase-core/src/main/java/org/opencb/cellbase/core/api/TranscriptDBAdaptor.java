
package org.opencb.cellbase.core.api;

import org.opencb.commons.datastore.core.QueryParam;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by imedina on 30/11/15.
 */
public interface TranscriptDBAdaptor<Transcript> extends FeatureDBAdaptor<Transcript> {

    enum QueryParams implements QueryParam {
        ID("id", TEXT_ARRAY, ""),
        NAME("name", TEXT_ARRAY, ""),
        REGION("region", TEXT_ARRAY, ""),
        BIOTYPE("biotype", TEXT_ARRAY, ""),
        TFBS_NAME("transcripts.tfbs.name", TEXT_ARRAY, ""),
        XREFS("transcripts.xrefs", TEXT_ARRAY, "");


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
            return key;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
