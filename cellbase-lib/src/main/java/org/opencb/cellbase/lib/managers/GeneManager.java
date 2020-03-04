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
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.GeneMongoDBAdaptor;

import java.util.Iterator;
import java.util.List;

public class GeneManager extends AbstractManager {

    private GeneMongoDBAdaptor geneDBAdaptor;

    public GeneManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
    }

    public CellBaseDataResult<Gene> search(GeneQuery geneQuery) throws QueryException, IllegalAccessException {
        geneQuery.setDefaults();
        geneQuery.validate();
        return geneDBAdaptor.query(geneQuery);
    }

    public CellBaseDataResult<Gene> groupBy(GeneQuery geneQuery) {
        geneQuery.setCount(Boolean.FALSE);
        return geneDBAdaptor.groupBy(geneQuery);
    }

    public CellBaseDataResult<Gene> aggregationStats(GeneQuery geneQuery) {
        geneQuery.setCount(Boolean.TRUE);
        return geneDBAdaptor.groupBy(geneQuery);
    }

    public List<CellBaseDataResult<Gene>> info(List<GeneQuery> geneQueries) {
        List<CellBaseDataResult<Gene>> geneQueryResults = geneDBAdaptor.query(geneQueries);
        return geneQueryResults;
    }

    public CellBaseDataResult<String> distinct(GeneQuery geneQuery) {
        geneQuery.setCount(Boolean.FALSE);
        return geneDBAdaptor.distinct(geneQuery.getFacet(), geneQuery);
    }

//    public CellBaseDataResult<Gene> distinct(GeneQuery geneQuery) {
//        return geneDBAdaptor.distinct(geneQuery);
//    }

    public Iterator<Gene> iterator(GeneQuery geneQuery) {
        return geneDBAdaptor.iterator(geneQuery);
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
