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

package org.opencb.cellbase.core.db.api.variation;

import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

@Deprecated
public interface MutationDBAdaptor extends FeatureDBAdaptor {

    QueryResult getAllById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    QueryResult getAllDiseases(QueryOptions options);

    QueryResult getAllByDisease(String geneName, QueryOptions options);

    List<QueryResult> getAllByDiseaseList(List<String> geneName, QueryOptions options);


    QueryResult getByGeneName(String geneName, QueryOptions options);

    List<QueryResult> getAllByGeneNameList(List<String> geneNameList, QueryOptions options);


    QueryResult getByProteinId(String proteinId, QueryOptions options);

    List<QueryResult> getAllByProteinIdList(List<String> proteinIdList, QueryOptions options);

    QueryResult getByProteinRegion(String proteinId, int start, int end, QueryOptions options);


    QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

    QueryResult getAllByPosition(Position position, QueryOptions options);

    List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


    QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    QueryResult getAllByRegion(Region region, QueryOptions options);

    List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);


    QueryResult getIntervalFrequencies(Region region, QueryOptions options);

    List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);


//	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByGeneName(String geneName);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByGeneNameList(List<String> geneNameList);
//
//	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByEnsemblTranscript(String ensemblTranscript);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByEnsemblTranscriptList(List<String> ensemblTranscriptList);
//
//
//	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByPosition(Position position);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByPositionList(List<Position> position);
//
//
//
//	public List<MutationPhenotypeAnnotation> getAllByRegion(Region region);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllSequencesByRegionList(List<Region> regionList);
//
//
//	public List<IntervalFeatureFrequency> getIntervalFrequencies(Region region, int interval);
	
}
