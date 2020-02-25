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
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

    public CellBaseDataResult<Gene> search(GeneQuery geneQuery) throws QueryException {
        geneQuery.setDefaults();
        geneQuery.validate();
        // TODO throw execption if facets populated
//        return geneDBAdaptor.nativeGet(geneQuery);
        return null;
    }

    public CellBaseDataResult<Gene> groupBy(Query geneQuery, String fields) {
        return geneDBAdaptor.groupBy(geneQuery, Arrays.asList(fields.split(",")), null);
    }

    public CellBaseDataResult<Gene> aggregationStats(Query geneQuery, String fields) {
//        geneQuery.setCount(Boolean.TRUE);
        return geneDBAdaptor.groupBy(geneQuery, Arrays.asList(fields.split(",")), null);
    }

    public List<CellBaseDataResult> info(Query geneQuery, String genes) {
        List<Query> queries = createXrefQueries(geneQuery, genes);
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries, null);
//        for (int i = 0; i < queries.size(); i++) {
//            geneQueryResults.get(i).setId((queries.get(i).getTranscriptsXrefs().get(0)));
//        }
        return geneQueryResults;
    }

    private List<Query> createXrefQueries(Query geneQuery, String xrefs) {
        String[] ids = xrefs.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
        for (String id : ids) {
            // TODO need to clone query properly
            Query geneXrefQuery = new Query();
//            geneXrefQuery.setTranscriptsXrefs(id);
            queries.add(geneXrefQuery);
        }
        return queries;
    }

    public CellBaseDataResult<Gene> distinct(Query geneQuery, String field) {
        return geneDBAdaptor.distinct(geneQuery, field);
    }

    public Iterator<CellBaseDataResult<Gene> > iterator(Query geneQuery, String field) {
        return geneDBAdaptor.iterator(geneQuery, null);
    }

    public List<CellBaseDataResult> getRegulatoryElements(Query geneQuery, String genes) {
        String[] geneArray = genes.split(",");
        List<CellBaseDataResult> geneQueryResults = new ArrayList<>(geneArray.length);
//        for (String gene : geneArray) {
//            geneQuery.setId(gene);
//            CellBaseDataResult geneQueryResult = geneDBAdaptor.getRegulatoryElements(geneQuery);
//            geneQueryResults.add(geneQueryResult);
//        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getTfbs(Query geneQuery, String genes) {
        String[] geneArray = genes.split(",");
        List<CellBaseDataResult> geneQueryResults = new ArrayList<>(geneArray.length);
//        for (String gene : geneArray) {
//            geneQuery.setTranscriptsXrefs(gene);
//            CellBaseDataResult geneQueryResult = geneDBAdaptor.getTfbs(geneQuery);
//            geneQueryResult.setId(gene);
//            geneQueryResults.add(geneQueryResult);
//        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getByTranscript(Query geneQuery, String transcriptId) {
        List<Query> queries = createQueries(geneQuery, transcriptId, GeneDBAdaptor.QueryParams.TRANSCRIPT_ID.key());
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries, null);
//        for (int i = 0; i < queries.size(); i++) {
//            geneQueryResults.get(i).setId(queries.get(i).getTranscriptsId().get(0));
//        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getByRegion(Query geneQuery, String region) {
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
        return null;
    }

    public List<CellBaseDataResult> getByTf(Query geneQuery, String tf) {
        List<Query> queries = createQueries(geneQuery, tf, GeneDBAdaptor.QueryParams.NAME.key());
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries, null);
//        for (int i = 0; i < queries.size(); i++) {
//            geneQueryResults.get(i).setId(queries.get(i).getNames().get(0));
//        }
        return geneQueryResults;
    }

    public List<CellBaseDataResult> getGeneByEnsemblId(String id) {
        String[] ids = id.split(",");
        List<Query> queries = new ArrayList<>(ids.length);
//        for (String s : ids) {
//            queries.add(new Query().setTranscriptsXrefs(s));
//        }
        List<CellBaseDataResult> geneQueryResults = geneDBAdaptor.nativeGet(queries, null);
        for (int i = 0; i < ids.length; i++) {
            geneQueryResults.get(i).setId(ids[i]);
        }
        return geneQueryResults;
    }
}
