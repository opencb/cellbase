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
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.impl.core.GeneCoreDBAdaptor;

import java.util.Iterator;

public class GeneManager extends AbstractManager implements AggregationApi  {

    private GeneCoreDBAdaptor geneDBAdaptor;

    public GeneManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
    }

    public Iterator<Gene> iterator(GeneQuery geneQuery) {
        return geneDBAdaptor.iterator(geneQuery);
    }

    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return geneDBAdaptor;
    }

//    public List<CellBaseDataResult> getRegulatoryElements(Query geneQuery, String genes) {
//        String[] geneArray = genes.split(",");
//        List<CellBaseDataResult> geneQueryResults = new ArrayList<>(geneArray.length);
//        for (String gene : geneArray) {
//            geneQuery.setId(gene);
//            CellBaseDataResult geneQueryResult = geneDBAdaptor.getRegulatoryElements(geneQuery);
//            geneQueryResults.add(geneQueryResult);
//        }
//        return geneQueryResults;
//    }
//
//    public CellBaseDataResult getRegulatoryElements(GeneQuery geneQuery) {
//        return geneDBAdaptor.getRegulatoryElements(geneQuery);
//    }
//
//    public List<CellBaseDataResult> getTfbs(Query geneQuery, String genes) {
//        String[] geneArray = genes.split(",");
//        List<CellBaseDataResult> geneQueryResults = new ArrayList<>(geneArray.length);
//        for (String gene : geneArray) {
//            geneQuery.setTranscriptsXrefs(gene);
//            CellBaseDataResult geneQueryResult = geneDBAdaptor.getTfbs(geneQuery);
//            geneQueryResult.setId(gene);
//            geneQueryResults.add(geneQueryResult);
//        }
//        return geneQueryResults;
//    }
//
//    public List<CellBaseDataResult> getByTranscript(Query geneQuery, String transcriptId) {
//        List<Query> queries = createQueries(geneQuery, transcriptId, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key());
//        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries, null);
//        for (int i = 0; i < queries.size(); i++) {
//            geneQueryResults.get(i).setId(queries.get(i).getTranscriptsId().get(0));
//        }
//        return geneQueryResults;
//    }
//
//    public List<CellBaseDataResult> getByRegion(Query geneQuery, String region) {
//        if (geneQuery.getHistogram()) {
//            List<Query> queries = createQueries(geneQuery, region, GeneDBAdaptor.QueryParams.REGION.key());
//            List<CellBaseDataResult> geneQueryResults = new ArrayList<>();
////            List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.getIntervalFrequencies(queries, geneQuery.getInterval());
//            for (int i = 0; i < queries.size(); i++) {
//                geneQueryResults.get(i).setId(geneQuery.getRegions().get(0).toString());
//            }
//            return geneQueryResults;
//        } else {
//            List<Query> queries = createQueries(geneQuery, region, GeneDBAdaptor.QueryParams.REGION.key());
//            List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries);
//            for (int i = 0; i < queries.size(); i++) {
//                geneQueryResults.get(i).setId(queries.get(i).getRegions().get(0).toString());
//            }
//            return geneQueryResults;
//        }
//    }
//
//    public List<CellBaseDataResult> getByTf(Query geneQuery, String tf) {
//        List<Query> queries = createQueries(geneQuery, tf, GeneDBAdaptor.QueryParams.NAME.key());
//        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries, null);
//        for (int i = 0; i < queries.size(); i++) {
//            geneQueryResults.get(i).setId(queries.get(i).getNames().get(0));
//        }
//        return geneQueryResults;
//    }

//    @Deprecated
//    public List<CellBaseDataResult<Gene>> getGeneByEnsemblId(String id) {
//        String[] ids = id.split(",");
//        List<GeneQuery> queries = new ArrayList<>(ids.length);
//        for (String s : ids) {
//            queries.add(new GeneQuery().setTranscriptsXrefs(Arrays.asList(s)));
//        }
//        List<CellBaseDataResult<Gene>> geneQueryResults = geneDBAdaptor.query(queries);
//        for (int i = 0; i < ids.length; i++) {
//            geneQueryResults.get(i).setId(ids[i]);
//        }
//        return geneQueryResults;
//    }
}
