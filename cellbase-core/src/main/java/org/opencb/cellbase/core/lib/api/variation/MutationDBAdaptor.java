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

package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface MutationDBAdaptor extends FeatureDBAdaptor {

    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    public QueryResult getAllDiseases(QueryOptions options);

    public QueryResult getAllByDisease(String geneName, QueryOptions options);

    public List<QueryResult> getAllByDiseaseList(List<String> geneName, QueryOptions options);


    public QueryResult getByGeneName(String geneName, QueryOptions options);

    public List<QueryResult> getAllByGeneNameList(List<String> geneNameList, QueryOptions options);


    public QueryResult getByProteinId(String proteinId, QueryOptions options);

    public List<QueryResult> getAllByProteinIdList(List<String> proteinIdList, QueryOptions options);

    public QueryResult getByProteinRegion(String proteinId, int start, int end, QueryOptions options);


    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

    public QueryResult getAllByPosition(Position position, QueryOptions options);

    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);


    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);


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
//	public List<List<MutationPhenotypeAnnotation>> getAllByRegionList(List<Region> regionList);
//
//
//	public List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);
	
}
