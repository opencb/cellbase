/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.core.api.core;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.commons.datastore.core.QueryParam.Type.*;

/**
 * Created by imedina on 26/11/15.
 */
public interface VariantDBAdaptor {

    enum QueryParams implements QueryParam {
        ID("id", TEXT_ARRAY, ""),
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
        GENE("gene", TEXT_ARRAY, ""),
        CONSEQUENCE_TYPE("consequenceType", TEXT_ARRAY, ""),
        TRANSCRIPT_CONSEQUENCE_TYPE("transcriptVariations.consequenceTypes", TEXT_ARRAY, ""),
        XREFS("xrefs", TEXT_ARRAY, ""),
        IMPRECISE("imprecise", BOOLEAN, ""),
        SV_TYPE("svType", STRING, ""),
        PHASE("phased", TEXT_ARRAY, ""),
        TYPE("type", STRING, ""),
        REFSEQ("refseq", STRING, ""),
        ENSEMBL("ensembl", STRING, "");

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

//    CellBaseDataResult startsWith(String id, QueryOptions options);

    CellBaseDataResult<Long> update(List objectList, String field, String[] innerFields);

//    default CellBaseDataResult<T> getByVariant(Variant variant, QueryOptions options) {
//        Query query;
////        if (VariantType.CNV.equals(variant.getType())) {
//
//        // Queries for CNVs,SVs are different from simple short variants queries
//        if (variant.getSv() != null
//                && variant.getSv().getCiStartLeft() != null
//                && variant.getSv().getCiStartRight() != null
//                && variant.getSv().getCiEndLeft() != null
//                && variant.getSv().getCiEndRight() != null) {
//            query = new Query(QueryParams.CHROMOSOME.key(), variant.getChromosome());
//            // Imprecise queries can just be enabled for structural variants providing CIPOS positions. Imprecise queries
//            // can be disabled by using the imprecise=false query option
//            if (options.get(QueryParams.IMPRECISE.key()) == null || (Boolean) options.get(QueryParams.IMPRECISE.key())) {
//                int ciStartLeft = variant.getSv().getCiStartLeft();
//                int ciStartRight = variant.getSv().getCiStartRight();
//                int ciEndLeft = variant.getSv().getCiEndLeft();
//                int ciEndRight = variant.getSv().getCiEndRight();
//                query.append(QueryParams.CI_START_LEFT.key(), ciStartLeft)
//                        .append(QueryParams.CI_START_RIGHT.key(), ciStartRight)
//                        .append(QueryParams.CI_END_LEFT.key(), ciEndLeft)
//                        .append(QueryParams.CI_END_RIGHT.key(), ciEndRight);
//            // Exact query for start/end
//            } else {
//                query.append(QueryParams.START.key(), variant.getStart());
//                query.append(QueryParams.END.key(), variant.getStart());
//            }
//            // CNVs must always be matched against COPY_NUMBER_GAIN/COPY_NUMBER_LOSS when searching - if provided
//            if (VariantType.CNV.equals(variant.getType()) && variant.getSv().getType() != null) {
//                query.append(QueryParams.SV_TYPE.key(), variant.getSv().getType().toString());
//            }
//            query.append(QueryParams.TYPE.key(), variant.getType().toString());
//        // simple short variant query; This will be the query run in more than 99% of the cases
//        } else {
//            query = new Query(QueryParams.CHROMOSOME.key(), variant.getChromosome())
//                    .append(QueryParams.START.key(), variant.getStart())
//                    .append(QueryParams.REFERENCE.key(), variant.getReference())
//                    .append(QueryParams.ALTERNATE.key(), variant.getAlternate());
//        }
//        return get(query, options);
//    }

//    default List<CellBaseDataResult<T>> getByVariant(List<Variant> variants, QueryOptions options) {
//        List<CellBaseDataResult<T>> results = new ArrayList<>(variants.size());
//        for (Variant variant: variants) {
//            results.add(getByVariant(variant, options));
//        }
//        return results;
//    }

//    CellBaseDataResult<String> getConsequenceTypes(Query query);

    CellBaseDataResult<Score> getFunctionalScoreVariant(Variant variant, QueryOptions options);

    default List<CellBaseDataResult<Score>> getFunctionalScoreVariant(List<Variant> variants, QueryOptions options) {
        List<CellBaseDataResult<Score>> cellBaseDataResults = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            if (variant.getType() == VariantType.SNV) {
                cellBaseDataResults.add(getFunctionalScoreVariant(variant, options));
            } else {
                cellBaseDataResults.add(new CellBaseDataResult<>(variant.toString(), 0, Collections.emptyList(), 0));
            }
        }
        return cellBaseDataResults;
    }

    List<CellBaseDataResult<Variant>> getPopulationFrequencyByVariant(List<Variant> variants, QueryOptions queryOptions);
}
