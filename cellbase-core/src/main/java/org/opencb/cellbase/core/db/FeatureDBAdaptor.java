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

package org.opencb.cellbase.core.db;

import org.opencb.biodata.models.core.Position;
import org.opencb.biodata.models.core.Region;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public interface FeatureDBAdaptor extends DBAdaptor {


    QueryResult getAll(QueryOptions options);

    QueryResult next(String chromosome, int position, QueryOptions options);


    default QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return getAllByRegion(new Region(chromosome, position, position), options);
    }

    default QueryResult getAllByPosition(Position position, QueryOptions options) {
        return getAllByRegion(new Region(position.getChromosome(), position.getPosition(), position.getPosition()), options);
    }

    default List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
//        List<Region> regions = new ArrayList<>(positionList.size());
//        for (Position position : positionList) {
//            regions.add(new Region(position.getChromosomeInfo(), position.getPosition(), position.getPosition()));
//        }
        List<Region> regions = positionList.stream()
                .map(position -> new Region(position.getChromosome(), position.getPosition(), position.getPosition()))
                .collect(Collectors.toList());
        return getAllByRegionList(regions, options);
    }


    default QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options) {
        return getAllByRegion(new Region(chromosome, start, end), options);
    }

    default QueryResult getAllByRegion(Region region, QueryOptions options) {
        return getAllByRegionList(Collections.singletonList(region), options).get(0);
    }

    List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);


    QueryResult getIntervalFrequencies(Region region, QueryOptions options);

}
