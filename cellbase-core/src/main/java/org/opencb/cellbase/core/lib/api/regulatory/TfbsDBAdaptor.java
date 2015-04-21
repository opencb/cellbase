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

package org.opencb.cellbase.core.lib.api.regulatory;

import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

public interface TfbsDBAdaptor extends RegulatoryRegionDBAdaptor {


//	public List<Tfbs> getAllByTfGeneName(String tfGeneName, String celltype, int start, int end);
//
//	public List<List<Tfbs>> getAllByTfGeneNameList(List<String> tfGeneNameList, String celltype, int start, int end);

	public QueryResult getAllByTargetGeneId(String targetGeneId, QueryOptions options);

	public List<QueryResult> getAllByTargetGeneIdList(List<String> targetGeneIdList, QueryOptions options);
	
	
	public QueryResult getAllByJasparId(String jasparId, QueryOptions options);

	public List<QueryResult> getAllByJasparIdList(List<String> jasparIdList, QueryOptions options);
	
	
//	public List<Protein> getTfInfoByTfGeneName(String tfGeneName);
//	
//	public List<List<Protein>> getTfInfoByTfGeneNameList(List<String> tfGeneNameList);

//	public List<Pwm> getAllPwmByTfGeneName(String tfName);
//	
//	public List<List<Pwm>> getAllPwmByTfGeneNameList(List<String> tfNameList);

	
//	public List<Tfbs> getAllByRegion(String chromosome);
//
//	public List<Tfbs> getAllByRegion(String chromosome, int start);
//
//	public List<Tfbs> getAllByRegion(String chromosome, int start, int end);
//
//	public List<Tfbs> getAllByRegion(Region region);
//
//	public List<List<Tfbs>> getAllByRegionList(List<Region> regionList);

//	public QueryResponse getAllByPosition(String chromosome, int position, QueryOptions options);


//	public List<Tfbs> getAllByInternalIdList(List<String> idList);
//
//	public List<Tfbs> getAllByInternalId(String id);
	

	public List<Object> getAllAnnotation();

	public List<Object> getAllAnnotationByCellTypeList(List<String> cellTypes);

	
	public List<IntervalFeatureFrequency> getAllTfIntervalFrequencies(Region region, int interval);
}
