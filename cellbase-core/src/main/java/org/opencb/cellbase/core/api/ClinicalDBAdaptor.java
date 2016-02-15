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

import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by imedina on 30/11/15.
 */
public interface ClinicalDBAdaptor<T> extends FeatureDBAdaptor<T> {

    enum QueryParams implements QueryParam {
        REGION("region", TEXT_ARRAY, ""),
        GENE("gene", TEXT_ARRAY, ""),
        PHENOTYPE("phenotype", TEXT_ARRAY, ""),
        SO("so", TEXT_ARRAY, ""),
        SOURCE("source", TEXT_ARRAY, ""),
        CLINVARRCV("rcv", TEXT_ARRAY, ""),
        CLINVARCLINSIG("significance", TEXT_ARRAY, ""),
        CLINVARREVIEW("review", TEXT_ARRAY, ""),
        CLINVARTYPE("type", TEXT_ARRAY, ""),
        CLINVARRS("rs", TEXT_ARRAY, "");

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

    List<QueryResult> getPhenotypeGeneRelations(Query query, QueryOptions queryOptions);

    List<QueryResult> getAllByGenomicVariantList(List<Variant> variantList, QueryOptions options);

}
