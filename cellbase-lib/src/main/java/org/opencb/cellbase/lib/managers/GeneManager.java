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
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;

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

    public CellBaseDataResult<Gene> search(GeneQuery geneQuery) throws CellbaseException {
        validateQueryOptions(geneQuery);
        geneQuery.addLimitIfAbsent();
        geneQuery.addSkipIfAbsent();
        return geneDBAdaptor.nativeGet(geneQuery);
    }

    public CellBaseDataResult<Gene> groupBy(GeneQuery geneQuery, String fields) {
        return geneDBAdaptor.groupBy(geneQuery, Arrays.asList(fields.split(",")));
    }

    public CellBaseDataResult<Gene> aggregationStats(GeneQuery geneQuery, String fields) {
        geneQuery.setCount(Boolean.TRUE);
        return geneDBAdaptor.groupBy(geneQuery, Arrays.asList(fields.split(",")));
    }

    public List<CellBaseDataResult> info(GeneQuery geneQuery, String genes) {
        List<GeneQuery> queries = createXrefQueries(geneQuery, genes);
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries);
        for (int i = 0; i < queries.size(); i++) {
            geneQueryResults.get(i).setId((queries.get(i).getTranscriptsXrefs().get(0)));
        }
        return geneQueryResults;
    }

    private List<GeneQuery> createXrefQueries(GeneQuery geneQuery, String xrefs) {
        String[] ids = xrefs.split(",");
        List<GeneQuery> queries = new ArrayList<>(ids.length);
        for (String id : ids) {
            // TODO need to clone query properly
            GeneQuery geneXrefQuery = new GeneQuery(geneQuery);
            geneXrefQuery.setTranscriptsXrefs(id);
            queries.add(geneXrefQuery);
        }
        return queries;
    }

    public CellBaseDataResult<Gene> distinct(GeneQuery geneQuery, String field) {
        return geneDBAdaptor.distinct(geneQuery, field);
    }

    public List<CellBaseDataResult> getRegulatoryElements(GeneQuery geneQuery, String genes) {
        String[] geneArray = genes.split(",");
        List<CellBaseDataResult> geneQueryResults = new ArrayList<>(geneArray.length);
        for (String gene : geneArray) {
            geneQuery.setId(gene);
            CellBaseDataResult geneQueryResult = geneDBAdaptor.getRegulatoryElements(geneQuery);
            geneQueryResults.add(geneQueryResult);
        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getTfbs(GeneQuery geneQuery, String genes) {
        String[] geneArray = genes.split(",");
        List<CellBaseDataResult> geneQueryResults = new ArrayList<>(geneArray.length);
        for (String gene : geneArray) {
            geneQuery.setTranscriptsXrefs(gene);
            CellBaseDataResult geneQueryResult = getTfbs(geneQuery, gene);
            geneQueryResult.setId(gene);
            geneQueryResults.add(geneQueryResult);
        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getByTranscript(GeneQuery geneQuery, String transcriptId) {
        List<GeneQuery> queries = createQueries(geneQuery, transcriptId, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key());
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries);
        for (int i = 0; i < queries.size(); i++) {
            geneQueryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key()));
        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getByRegion(GeneQuery geneQuery, String region) {
        if (geneQuery.getHistogram()) {
            List<GeneQuery> queries = createQueries(geneQuery, region, GeneDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.getIntervalFrequencies(queries, geneQuery.getInterval());
            for (int i = 0; i < queries.size(); i++) {
                geneQueryResults.get(i).setId((String) geneQuery.get(GeneDBAdaptor.QueryParams.REGION.key()));
            }
            return geneQueryResults;
        } else {
            List<GeneQuery> queries = createQueries(geneQuery, region, GeneDBAdaptor.QueryParams.REGION.key());
            List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries);
            for (int i = 0; i < queries.size(); i++) {
                geneQueryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.REGION.key()));
            }
            return geneQueryResults;
        }
    }

    public List<CellBaseDataResult> getByTf(GeneQuery geneQuery, String tf) {
        List<GeneQuery> queries = createQueries(geneQuery, tf, GeneDBAdaptor.QueryParams.NAME.key());
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries);
        for (int i = 0; i < queries.size(); i++) {
            geneQueryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.NAME.key()));
        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getGeneByEnsemblId(String id) {
        String[] ids = id.split(",");
        List<GeneQuery> queries = new ArrayList<>(ids.length);
        for (String s : ids) {
            queries.add(new GeneQuery().setTranscriptsXrefs(s));
        }
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries);
        for (int i = 0; i < ids.length; i++) {
            geneQueryResults.get(i).setId(ids[i]);
        }
        return geneQueryResults;
    }




}
