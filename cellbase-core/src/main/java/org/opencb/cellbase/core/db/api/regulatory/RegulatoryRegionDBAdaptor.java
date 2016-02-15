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

package org.opencb.cellbase.core.db.api.regulatory;

import org.opencb.biodata.models.core.Position;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert
 * Date: 7/18/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */

public interface RegulatoryRegionDBAdaptor extends FeatureDBAdaptor {

    QueryResult next(String id, QueryOptions options);

    QueryResult getAllById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    QueryResult getAllByPosition(Position position, QueryOptions options);

    List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


    QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    QueryResult getAllByRegion(Region region, QueryOptions options);

    List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);


// public List<Tfbs> getAllByTfGeneName(String tfGeneName, String celltype, int start, int end);
//
// public List<List<Tfbs>> getAllByTfGeneNameList(List<String> tfGeneNameList, String celltype, int start, int end);

//    public QueryResponse getAllByTargetGeneId(String targetGeneId, QueryOptions options);

//    public QueryResponse getAllByTargetGeneIdList(List<String> targetGeneIdList, QueryOptions options);


//    public QueryResponse getAllByJasparId(String jasparId, QueryOptions options);

//    public QueryResponse getAllByJasparIdList(List<String> jasparIdList, QueryOptions options);


// public List<Protein> getTfInfoByTfGeneName(String tfGeneName);
//
// public List<List<Protein>> getTfInfoByTfGeneNameList(List<String> tfGeneNameList);

// public List<Pwm> getAllPwmByTfGeneName(String tfName);
//
// public List<List<Pwm>> getAllPwmByTfGeneNameList(List<String> tfNameList);


// public List<Tfbs> getAllByRegion(String chromosome);
//
// public List<Tfbs> getAllByRegion(String chromosome, int start);
//
// public List<Tfbs> getAllByRegion(String chromosome, int start, int end);
//
// public List<Tfbs> getAllByRegion(Region region);
//
// public List<List<Tfbs>> getAllSequencesByRegionList(List<Region> regionList);

// public QueryResponse getAllByPosition(String chromosome, int position, QueryOptions options);


// public List<Tfbs> getAllByInternalIdList(List<String> idList);
//
// public List<Tfbs> getAllByInternalId(String id);


//    public List<Object> getAllAnnotation();

//    public List<Object> getAllAnnotationByCellTypeList(List<String> cellTypes);


//    public List<IntervalFeatureFrequency> getAllTfIntervalFrequencies(Region region, int interval);

}
