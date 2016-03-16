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

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencb.commons.datastore.core.QueryParam.Type.STRING;

/**
 * Created by imedina on 30/11/15.
 */
public interface GenomeDBAdaptor extends CellBaseDBAdaptor {

    enum QueryParams implements QueryParam {
        REGION("region", STRING, "");

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

    QueryResult getGenomeInfo(QueryOptions queryOptions);

    QueryResult getChromosomeInfo(String chromosomeId, QueryOptions queryOptions);


    @Deprecated
    QueryResult<GenomeSequenceFeature> getGenomicSequence(Query query, QueryOptions queryOptions);

    @Deprecated
    default List<QueryResult<GenomeSequenceFeature>> getGenomicSequence(List<Query> queries, QueryOptions queryOptions) {
        List<QueryResult<GenomeSequenceFeature>> queryResults = new ArrayList<>(queries.size());
        queryResults.addAll(queries.stream().map(query -> getGenomicSequence(query, queryOptions)).collect(Collectors.toList()));
        return queryResults;
    }


    QueryResult<GenomeSequenceFeature> getSequence(Region region, QueryOptions queryOptions);

    default List<QueryResult<GenomeSequenceFeature>> getSequence(List<Region> regions, QueryOptions queryOptions) {
        List<QueryResult<GenomeSequenceFeature>> queryResults = new ArrayList<>(regions.size());
        queryResults.addAll(regions.stream().map(region -> getSequence(region, queryOptions)).collect(Collectors.toList()));
        return queryResults;
    }


//    default QueryResult<ConservationScoreRegion> getConservation(Region region, QueryOptions queryOptions) {
    default QueryResult<GenomicScoreRegion<Float>> getConservation(Region region, QueryOptions queryOptions) {
        return getConservation(Collections.singletonList(region), queryOptions).get(0);
    }

//    List<QueryResult<ConservationScoreRegion>> getConservation(List<Region> regions, QueryOptions queryOptions);
    List<QueryResult<GenomicScoreRegion<Float>>> getConservation(List<Region> regions, QueryOptions queryOptions);

}
