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
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.List;

import static org.opencb.commons.datastore.core.QueryParam.Type.INTEGER;
import static org.opencb.commons.datastore.core.QueryParam.Type.STRING;
import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by imedina on 26/11/15.
 */
public interface VariantDBAdaptor<T> extends FeatureDBAdaptor<T> {

    enum QueryParams implements QueryParam {
        ID("id", TEXT_ARRAY, ""),
        REGION("region", TEXT_ARRAY, ""),
        CHROMOSOME("chromosome", STRING, ""),
        START("start", INTEGER, ""),
        REFERENCE("reference", STRING, ""),
        ALTERNATE("alternate", STRING, ""),
        GENE("gene", TEXT_ARRAY, ""),
        CONSEQUENCE_TYPE("consequenceType", TEXT_ARRAY, ""),
        TRANSCRIPT_CONSEQUENCE_TYPE("transcriptVariations.consequenceTypes", TEXT_ARRAY, ""),
        XREFS("xrefs", TEXT_ARRAY, "");

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

    QueryResult startsWith(String id, QueryOptions options);

    default QueryResult<T> getByVariant(Variant variant, QueryOptions options) {
        Query query = new Query(QueryParams.REGION.key(), variant.getChromosome() + ":" + variant.getStart() + "-" + variant.getStart())
                .append(QueryParams.REFERENCE.key(), variant.getReference())
                .append(QueryParams.ALTERNATE.key(), variant.getAlternate());
        return get(query, options);
    }

    default List<QueryResult<T>> getByVariant(List<Variant> variants, QueryOptions options) {
        List<QueryResult<T>> results = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            results.add(getByVariant(variant, options));
        }
        return results;
    }

    QueryResult<Score> getFunctionalScoreVariant(Variant variant, QueryOptions options);

    default List<QueryResult<Score>> getFunctionalScoreVariant(List<Variant> variants, QueryOptions options) {
        List<QueryResult<Score>> queryResults = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            queryResults.add(getFunctionalScoreVariant(variant, options));
        }
        return queryResults;
    }

}
