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

package org.opencb.cellbase.core.db.api.core;

import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.DBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;


public interface GenomeDBAdaptor extends DBAdaptor {


    @Deprecated
    QueryResult speciesInfoTmp(String id, QueryOptions options);

    QueryResult getGenomeInfo(QueryOptions options);


    QueryResult getChromosomeById(String id, QueryOptions options);

    List<QueryResult> getAllByChromosomeIdList(List<String> idList, QueryOptions options);


    QueryResult getSequenceByRegion(String chromosome, int start, int end, QueryOptions options);

    List<QueryResult> getAllSequencesByRegionList(List<Region> regions, QueryOptions options);


    QueryResult getAllFeaturesByRegion(Region region, QueryOptions queryOptions);

    List<QueryResult> getAllFeaturesByRegionList(List<Region> positionList, QueryOptions queryOptions);


    QueryResult getAllCytobandsById(String id, QueryOptions options);

    List<QueryResult> getAllCytobandsByIdList(List<String> idList, QueryOptions options);

}
