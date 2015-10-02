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

import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface VariationDBAdaptor extends FeatureDBAdaptor {


    QueryResult next(String id, QueryOptions options);

    QueryResult getById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    QueryResult getAllConsequenceTypes(QueryOptions options);

    QueryResult getAllConsequenceTypesById(String id, QueryOptions options);


    QueryResult getByGeneId(String id, QueryOptions options);

    List<QueryResult> getAllByGeneIdList(List<String> idList, QueryOptions options);

    QueryResult getByTranscriptId(String id, QueryOptions options);

    List<QueryResult> getAllByTranscriptIdList(List<String> idList, QueryOptions options);



    QueryResult getAllPhenotypes(QueryOptions options);

    List<QueryResult> getAllPhenotypeByRegion(List<Region> regions, QueryOptions options);

    QueryResult getAllByPhenotype(String phenotype, QueryOptions options);

    List<QueryResult> getAllByPhenotypeList(List<String> phenotypeList, QueryOptions options);

    QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options);

    List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options);

    ////	public List<SnpPopulationFrequency> getAllSnpPopulationFrequency(String name);
////
////	public List<List<SnpPopulationFrequency>> getAllSnpPopulationFrequencyList(List<String> nameList);
////


    QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);

    List<QueryResult> getIdByVariantList(List<GenomicVariant> variations, QueryOptions options);

    List<QueryResult> getAllByVariantList(List<GenomicVariant> variations, QueryOptions options);

}
