
/*
 * Copyright 2015-2019 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core.api;

import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.List;

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
        TFBS_NAME("tfbs.name", TEXT_ARRAY, ""),
        XREFS("xrefs", TEXT_ARRAY, ""),
        // TODO: add miRNA to query params
//        MIRNA("mirna", TEXT_ARRAY, ""),
        ANNOTATION_FLAGS("annotationFlags", TEXT_ARRAY, "");


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

    QueryResult<String> getCdna(String id);

    default List<QueryResult<String>> getCdna(List<String> idList) {
        List<QueryResult<String>> queryResults = new ArrayList<>();
        for (String id : idList) {
            queryResults.add(getCdna(id));
        }
        return queryResults;
    }

}
