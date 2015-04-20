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
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import javax.management.Query;
import java.util.List;


public interface VariationDBAdaptor {

	
    public QueryResult getById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult getAllConsequenceTypes(QueryOptions options);


    public QueryResult getByTranscriptId(String id, QueryOptions options);

    public List<QueryResult> getAllByTranscriptIdList(List<String> idList, QueryOptions options);



    public QueryResult getAllPhenotypes(QueryOptions options);

    public List<QueryResult> getAllPhenotypeByRegion(List<Region> regions, QueryOptions options);

    public QueryResult getAllByPhenotype(String phenotype, QueryOptions options);

    public List<QueryResult> getAllByPhenotypeList(List<String> phenotypeList, QueryOptions options);

    public QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options);

    public List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options);


    
	public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

	public QueryResult getAllByPosition(Position position, QueryOptions options);
	
	public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);
	

	public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);
    
    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);

    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);

    public List<QueryResult> getIdByVariantList(List<GenomicVariant> variations, QueryOptions options);

    public List<QueryResult> getAllByVariantList(List<GenomicVariant> variations, QueryOptions options);

}
