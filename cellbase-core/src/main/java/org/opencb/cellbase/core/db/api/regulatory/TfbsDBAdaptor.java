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

import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

public interface TfbsDBAdaptor extends RegulatoryRegionDBAdaptor {

// public List<Tfbs> getAllByTfGeneName(String tfGeneName, String celltype, int start, int end);
// public List<List<Tfbs>> getAllByTfGeneNameList(List<String> tfGeneNameList, String celltype, int start, int end);

    QueryResult getAllByTargetGeneId(String targetGeneId, QueryOptions options);

    List<QueryResult> getAllByTargetGeneIdList(List<String> targetGeneIdList, QueryOptions options);


    QueryResult getAllByJasparId(String jasparId, QueryOptions options);

    List<QueryResult> getAllByJasparIdList(List<String> jasparIdList, QueryOptions options);


    List<Object> getAllAnnotation();

    List<Object> getAllAnnotationByCellTypeList(List<String> cellTypes);


    List<IntervalFeatureFrequency> getAllTfIntervalFrequencies(Region region, int interval);

}
