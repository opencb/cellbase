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
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.avro.Cytoband;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.GenomeQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.impl.core.GenomeMongoDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

public class GenomeManager extends AbstractManager implements AggregationApi<GenomeQuery, Chromosome> {

    private GenomeMongoDBAdaptor genomeDBAdaptor;

    public GenomeManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    @Override
    public CellBaseCoreDBAdaptor<GenomeQuery, Chromosome> getDBAdaptor() {
        return genomeDBAdaptor;
    }

    private void init() {
        genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
    }

    public CellBaseDataResult getGenomeInfo(QueryOptions queryOptions) {
        return genomeDBAdaptor.getGenomeInfo(queryOptions);
    }

    public List<CellBaseDataResult> getChromosomes(QueryOptions queryOptions, String chromosomeId) {
        List<String> chromosomeList = Splitter.on(",").splitToList(chromosomeId);
        List<CellBaseDataResult> queryResults = new ArrayList<>(chromosomeList.size());
        for (String chromosome : chromosomeList) {
            CellBaseDataResult queryResult = genomeDBAdaptor.getChromosomeInfo(chromosome, queryOptions);
            queryResult.setId(chromosome);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    /**
     * For a single query, return the sequence for each region provided. Each region will be its own CellBaseDataResult.
     *
     * @param query The genome query. Should have regions populated
     * @return sequence for each region
     */
    public List<CellBaseDataResult<GenomeSequenceFeature>> getByRegions(GenomeQuery query) {
        List<CellBaseDataResult<GenomeSequenceFeature>> queryResults = new ArrayList<>();
        for (Region region : query.getRegions()) {
            queryResults.add(genomeDBAdaptor.getSequence(region, query.toQueryOptions()));
        }

        for (int i = 0; i < query.getRegions().size(); i++) {
            queryResults.get(i).setId(query.getRegions().get(i).toString());
        }
        return queryResults;
    }

    @Deprecated
    public List<CellBaseDataResult<GenomeSequenceFeature>> getByRegions(QueryOptions queryOptions, String regions) {
        List<Region> regionList = Region.parseRegions(regions);
        List<CellBaseDataResult<GenomeSequenceFeature>> queryResults = new ArrayList<>();
        for (Region region : regionList) {
            queryResults.add(genomeDBAdaptor.getSequence(region, queryOptions));
        }

        for (int i = 0; i < regionList.size(); i++) {
            queryResults.get(i).setId(regionList.get(i).toString());
        }
        return queryResults;
    }

    @Deprecated
    public CellBaseDataResult<GenomeSequenceFeature> getByRegion(Query query, QueryOptions queryOptions, String regions, String strand) {
        query.put(ParamConstants.QueryParams.REGION.key(), regions);
        query.put("strand", strand);
        CellBaseDataResult queryResult = genomeDBAdaptor.getGenomicSequence(query, queryOptions);
        queryResult.setId(regions);
        return queryResult;
    }

    public List<CellBaseDataResult<GenomicScoreRegion<Float>>> getConservation(QueryOptions queryOptions, List<Region> regionList) {
        List<CellBaseDataResult<GenomicScoreRegion<Float>>> queryResultList = genomeDBAdaptor.getConservation(regionList, queryOptions);
        return queryResultList;
    }

    public List<CellBaseDataResult<GenomicScoreRegion<Float>>> getConservation(QueryOptions queryOptions, String regions) {
        List<Region> regionList = Region.parseRegions(regions);
        List<CellBaseDataResult<GenomicScoreRegion<Float>>> queryResultList = genomeDBAdaptor.getConservation(regionList, queryOptions);
        for (int i = 0; i < regionList.size(); i++) {
            queryResultList.get(i).setId(regions);
        }
        return queryResultList;
    }

    public List<CellBaseDataResult<Score>> getAllScoresByRegionList(List<Region> regionList, QueryOptions options) {
        return genomeDBAdaptor.getAllScoresByRegionList(regionList, options);
    }

    public CellBaseDataResult<GenomeSequenceFeature> getSequence(Region region, QueryOptions queryOptions) {
        return genomeDBAdaptor.getSequence(region, queryOptions);
    }

    public CellBaseDataResult<GenomeSequenceFeature> getGenomicSequence(Query query, QueryOptions queryOptions) {
        return genomeDBAdaptor.getGenomicSequence(query, queryOptions);
    }

    public CellBaseDataResult<Cytoband> getCytobands(Region region, QueryOptions queryOptions) {
        return genomeDBAdaptor.getCytobands(region, queryOptions);
    }

    public List<CellBaseDataResult<Cytoband>> getCytobands(List<Region> regionList, QueryOptions queryOptions) {
        List<CellBaseDataResult<Cytoband>> cellBaseDataResultList = new ArrayList<>(regionList.size());
        for (Region region : regionList) {
            cellBaseDataResultList.add(getCytobands(region, queryOptions));
        }
        return cellBaseDataResultList;
    }

    public List<CellBaseDataResult<Cytoband>> getCytobands(List<Region> regionList) {
        return getCytobands(regionList, null);
    }
}
