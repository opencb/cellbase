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

import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.queries.RegulationQuery;
import org.opencb.cellbase.core.common.regulatory.RegulatoryRegion;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.lib.impl.core.RegulationMongoDBAdaptor;

public class RegulatoryManager extends AbstractManager implements AggregationApi<RegulationQuery, RegulatoryRegion>  {

    private RegulationMongoDBAdaptor regulationDBAdaptor;

    public RegulatoryManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(species, assembly);
    }


    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return regulationDBAdaptor;
    }


//    public CellBaseDataResult getFeatureTypes(Query query) {
//        return regulationDBAdaptor.distinct(query, "featureType");
//    }
//
//    public CellBaseDataResult getFeatureClasses(Query query) {
//        return regulationDBAdaptor.distinct(query, "featureClass");
//    }

//    public CellBaseDataResult<RegulatoryFeature> search(Query query, QueryOptions queryOptions) {
//        return regulationDBAdaptor.query(new GeneQuery());
//    }

//    public List<CellBaseDataResult<RegulatoryFeature>> getByRegions(Query query, QueryOptions queryOptions, String regions) {
//        List<Query> queries = createQueries(query, regions, RegulationDBAdaptor.QueryParams.REGION.key());
//        List<CellBaseDataResult<RegulatoryFeature>> queryResults = regulationDBAdaptor.query(queries);
//        for (int i = 0; i < queries.size(); i++) {
//            queryResults.get(i).setId((String) queries.get(i).get(RegulationDBAdaptor.QueryParams.REGION.key()));
//        }
//        return queryResults;
//    }

//    public List<CellBaseDataResult<RegulatoryFeature>> getAllByTfbs(Query query, QueryOptions queryOptions, String tf) {
//        List<Query> queries = createQueries(query, tf, RegulationDBAdaptor.QueryParams.NAME.key(),
//                RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), RegulationDBAdaptor.FeatureType.TF_binding_site
//                        + "," + RegulationDBAdaptor.FeatureType.TF_binding_site_motif);
//        List<CellBaseDataResult<RegulatoryFeature>> queryResults = regulationDBAdaptor.query(queries);
//        for (int i = 0; i < queries.size(); i++) {
//            queryResults.get(i).setId((String) queries.get(i).get(RegulationDBAdaptor.QueryParams.NAME.key()));
//        }
//        return queryResults;
//    }

//    public List<CellBaseDataResult<RegulatoryFeature>> getTfByRegions(Query query, QueryOptions queryOptions, String regions) {
//        List<Query> queries = createQueries(query, regions, RegulationDBAdaptor.QueryParams.REGION.key(),
//                RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(),
//                RegulationDBAdaptor.FeatureType.TF_binding_site + ","
//                        + RegulationDBAdaptor.FeatureType.TF_binding_site_motif);
//        List<CellBaseDataResult<RegulatoryFeature>> queryResults = regulationDBAdaptor.query(queries);
//        for (int i = 0; i < queries.size(); i++) {
//            queryResults.get(i).setId((String) queries.get(i).get(RegulationDBAdaptor.QueryParams.REGION.key()));
//        }
//        return queryResults;
//
//    }

}
