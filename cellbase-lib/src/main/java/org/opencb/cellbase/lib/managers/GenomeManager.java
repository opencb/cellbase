/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.managers;

import com.google.common.base.Splitter;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

public class GenomeManager extends AbstractManager {

    public GenomeManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public CellBaseDataResult info(QueryOptions queryOptions, String species, String assembly) {
        GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        CellBaseDataResult queryResult = genomeDBAdaptor.getGenomeInfo(queryOptions);
        queryResult.setId(species);
        return queryResult;
    }

    public List<CellBaseDataResult> getChromosomes(QueryOptions queryOptions, String species, String assembly, String chromosomeId) {
        GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        List<String> chromosomeList = Splitter.on(",").splitToList(chromosomeId);
        List<CellBaseDataResult> queryResults = new ArrayList<>(chromosomeList.size());
        for (String chromosome : chromosomeList) {
            CellBaseDataResult queryResult = dbAdaptor.getChromosomeInfo(chromosome, queryOptions);
            queryResult.setId(chromosome);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    public List<CellBaseDataResult<GenomeSequenceFeature>> getByRegions(QueryOptions queryOptions, String species, String assembly,
                                                                        String regions) {
        GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        List<Region> regionList = Region.parseRegions(regions);
        List<CellBaseDataResult<GenomeSequenceFeature>> queryResults =
                genomeDBAdaptor.getSequence(Region.parseRegions(regions), queryOptions);
        for (int i = 0; i < regionList.size(); i++) {
            queryResults.get(i).setId(regionList.get(i).toString());
        }
        return queryResults;

    }

    public CellBaseDataResult<GenomeSequenceFeature> getByRegion(Query query, QueryOptions queryOptions, String species, String assembly,
                                                                 String regions, String strand) {
        GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        query.put(GenomeDBAdaptor.QueryParams.REGION.key(), regions);
        query.put("strand", strand);
        CellBaseDataResult queryResult = genomeDBAdaptor.getGenomicSequence(query, queryOptions);
        queryResult.setId(regions);
        return queryResult;
    }

    public List<CellBaseDataResult<GenomicScoreRegion<Float>>> getConservation(Query query, QueryOptions queryOptions, String species,
                                                                               String assembly, String regions) {
        GenomeDBAdaptor conservationDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        List<Region> regionList = Region.parseRegions(regions);
        List<CellBaseDataResult<GenomicScoreRegion<Float>>> queryResultList
                = conservationDBAdaptor.getConservation(regionList, queryOptions);
        for (int i = 0; i < regionList.size(); i++) {
            queryResultList.get(i).setId(regions);
        }
        return queryResultList;
    }
}
