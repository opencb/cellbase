/*
 * Copyright 2015 OpenCB
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

import org.opencb.biodata.models.variant.avro.ProteinVariantAnnotation;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by imedina on 30/11/15.
 */
public interface ProteinDBAdaptor<T> extends CellBaseDBAdaptor<T> {

    enum QueryParams implements QueryParam {
        ACCESSION("accession", TEXT_ARRAY, ""),
        NAME("name", TEXT_ARRAY, ""),
        GENE("gene", TEXT_ARRAY, ""),
        XREFS("xrefs", TEXT_ARRAY, ""),
        KEYWORD("keyword", TEXT_ARRAY, ""),
        FEATURE_ID("feature.id", TEXT_ARRAY, ""),
        FEATURE_TYPE("feature.type", TEXT_ARRAY, "");

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

    QueryResult<Score> getSubstitutionScores(Query query, QueryOptions options);

    QueryResult<ProteinVariantAnnotation> getVariantAnnotation(String enstranscriptId, int position, String aaReference,
                                                               String aaAlternate, QueryOptions options);

}
