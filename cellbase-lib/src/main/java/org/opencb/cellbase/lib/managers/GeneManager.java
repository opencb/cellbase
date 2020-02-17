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

import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneManager extends AbstractManager {

    private GeneDBAdaptor geneDBAdaptor;

    public GeneManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
    }

    public CellBaseDataResult<Gene> search(Query query, QueryOptions queryOptions) {
        return geneDBAdaptor.nativeGet(query, queryOptions);
    }

    public CellBaseDataResult<Gene> groupBy(Query query, QueryOptions queryOptions, String fields) {
        return geneDBAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public CellBaseDataResult<Gene> aggregationStats(Query query, QueryOptions queryOptions, String fields) {
        queryOptions.put(QueryOptions.COUNT, true);
        return geneDBAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions);
    }

    public List<CellBaseDataResult> info(Query query, QueryOptions queryOptions, String genes) {
        logger.debug("blahh...");
        List<Query> queries = createQueries(query, genes, GeneDBAdaptor.QueryParams.XREFS.key());
        List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.XREFS.key()));
        }
        return queryResults;
    }

    public CellBaseDataResult<Gene> distinct(Query query, String field) {
        return geneDBAdaptor.distinct(query, field);
    }

    public List<CellBaseDataResult> getRegulatoryElements(Query query, QueryOptions queryOptions, String genes) {
        String[] geneArray = genes.split(",");
        List<CellBaseDataResult> queryResults = new ArrayList<>(geneArray.length);
        for (String gene : geneArray) {
            query.put(GeneDBAdaptor.QueryParams.ID.key(), gene);
            CellBaseDataResult queryResult = geneDBAdaptor.getRegulatoryElements(query, queryOptions);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    public List<CellBaseDataResult> getTfbs(Query query, QueryOptions queryOptions, String genes) {
        String[] geneArray = genes.split(",");
        List<CellBaseDataResult> queryResults = new ArrayList<>(geneArray.length);
        for (String gene : geneArray) {
            query.put(GeneDBAdaptor.QueryParams.XREFS.key(), gene);
            CellBaseDataResult queryResult = geneDBAdaptor.getTfbs(query, queryOptions);
            queryResult.setId(gene);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    public List<CellBaseDataResult> getByTranscript(Query query, QueryOptions queryOptions, String transcriptId) {
        logger.debug("blahh...");
        List<Query> queries = createQueries(query, transcriptId, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key());
        List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key()));
        }
        return queryResults;
    }

    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String region) {
        if (hasHistogramQueryParam(queryOptions)) {
            List<Query> queries = createQueries(query, region, GeneDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> queryResults = geneDBAdaptor.getIntervalFrequencies(queries, getHistogramIntervalSize(queryOptions),
                    queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) query.get(GeneDBAdaptor.QueryParams.REGION.key()));
            }
            return queryResults;
        } else {
            List<Query> queries = createQueries(query, region, GeneDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.REGION.key()));
            }
            return queryResults;
        }
    }

    public List<CellBaseDataResult> getByTf(Query query, QueryOptions queryOptions, String tf) {
        List<Query> queries = createQueries(query, tf, GeneDBAdaptor.QueryParams.NAME.key());
        List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.NAME.key()));
        }
        return queryResults;
    }

    public List<CellBaseDataResult> getGeneByEnsemblId(QueryOptions queryOptions, String id) {
        String[] ids = id.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
        for (String s : ids) {
            queries.add(new Query(GeneDBAdaptor.QueryParams.XREFS.key(), s));
        }
        List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < ids.length; i++) {
            queryResults.get(i).setId(ids[i]);
        }
        return queryResults;
    }




}
