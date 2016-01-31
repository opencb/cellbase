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

import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by imedina on 25/11/15.
 */
public interface GeneDBAdaptor<T> extends FeatureDBAdaptor<T> {

    enum QueryParams implements QueryParam {
        ID("id", TEXT_ARRAY, ""),
        NAME("name", TEXT_ARRAY, ""),
        REGION("region", TEXT_ARRAY, ""),
        BIOTYPE("biotype", TEXT_ARRAY, ""),
        XREFS("transcripts.xrefs", TEXT_ARRAY, ""),
        TRANSCRIPT_ID("transcripts.id", TEXT_ARRAY, ""),
        TRANSCRIPT_NAME("transcripts.name", TEXT_ARRAY, ""),
        TRANSCRIPT_BIOTYPE("transcripts.biotype", TEXT_ARRAY, ""),
        TFBS_NAME("transcripts.tfbs.name", TEXT_ARRAY, ""),
        ANNOTATION_DISEASE_ID("annotation.diseases.id", TEXT_ARRAY, ""),
        ANNOTATION_DISEASE_NAME("annotation.diseases.name", TEXT_ARRAY, ""),
        ANNOTATION_EXPRESSION_GENE("annotation.expression.gene", TEXT_ARRAY, ""),
        ANNOTATION_EXPRESSION_TISSUE("annotation.expression.tissue", TEXT_ARRAY, ""),
        ANNOTATION_DRUGS_NAME("annotation.drugs.name", TEXT_ARRAY, ""),
        ANNOTATION_DRUGS_GENE("annotation.drugs.gene", TEXT_ARRAY, ""),
        CLINICAL_SO("so", TEXT_ARRAY, ""),
        CLINICAL_GENE("gene", TEXT_ARRAY, ""),
        CLINICAL_PHENOTYPE("phenotype", TEXT_ARRAY, "");

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

    QueryResult getRegulatoryElements(Query query, QueryOptions queryOptions);

    QueryResult getTfbs(Query query, QueryOptions queryOptions);

}
