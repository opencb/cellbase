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

import java.util.List;

import static org.opencb.commons.datastore.core.QueryParam.Type.*;

/**
 * Created by imedina on 30/11/15.
 */
public interface ClinicalDBAdaptor<T> extends FeatureDBAdaptor<T> {

    enum QueryParams implements QueryParam {
        REGION("region", TEXT_ARRAY, ""),
        CHROMOSOME("chromosome", STRING, ""),
        START("start", INTEGER, ""),
        END("end", INTEGER, ""),
        CI_START_LEFT("ciStartLeft", INTEGER, ""),
        CI_START_RIGHT("ciStartRight", INTEGER, ""),
        CI_END_LEFT("ciEndLeft", INTEGER, ""),
        CI_END_RIGHT("ciEndRight", INTEGER, ""),
        REFERENCE("reference", STRING, ""),
        ALTERNATE("alternate", STRING, ""),
        PHENOTYPEDISEASE("phenotypeDisease", TEXT_ARRAY, ""),
        FEATURE("feature", TEXT_ARRAY, ""),
        SO("so", TEXT_ARRAY, ""),
        SOURCE("source", TEXT_ARRAY, ""),
        ID("id", TEXT_ARRAY, ""),
        TYPE("type", TEXT_ARRAY, ""),
        CONSISTENCY_STATUS("consistencyStatus", TEXT_ARRAY, ""),
        CLINICALSIGNIFICANCE("clinicalSignificance", TEXT_ARRAY, ""),
        MODE_INHERITANCE("modeInheritance", TEXT_ARRAY, ""),
        ALLELE_ORIGIN("alleleOrigin", TEXT_ARRAY, ""),
        ACCESSION("accession", TEXT_ARRAY, ""),

        @Deprecated
        GENE("gene", TEXT_ARRAY, ""),
        @Deprecated
        CLINVARRCV("clinvarId", TEXT_ARRAY, ""),
        @Deprecated
        CLINVARCLINSIG("clinvar-significance", TEXT_ARRAY, ""),
        @Deprecated
        CLINVARREVIEW("review", TEXT_ARRAY, ""),
        @Deprecated
        CLINVARTYPE("type", TEXT_ARRAY, ""),
        @Deprecated
        CLINVARRS("rs", TEXT_ARRAY, ""),
        @Deprecated
        COSMICID("cosmicId", TEXT_ARRAY, "");



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

    QueryResult<String> getAlleleOriginLabels();

    QueryResult<String> getModeInheritanceLabels();

    QueryResult<String> getClinsigLabels();

    QueryResult<String> getConsistencyLabels();

    QueryResult<String> getVariantTypes();

//    List<QueryResult> getAllByGenomicVariantList(List<Variant> variantList, QueryOptions options);

}
