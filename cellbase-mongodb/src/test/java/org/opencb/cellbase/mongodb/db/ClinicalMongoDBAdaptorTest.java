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

package org.opencb.cellbase.mongodb.db;

import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.DBAdaptorFactory;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.mongodb.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.util.List;

public class ClinicalMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    // TODO: to be finished - properly implemented
    @Ignore
    @Test
    public void testGetAllByRegionList() throws Exception {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions("include", "clinvarList");

    }

    // TODO: to be finished - properly implemented
    @Ignore
    @Test
    public void testGetAll() {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();

        try {
            cellBaseConfiguration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions();
//        queryOptions.add("source", "gwas");
//        queryOptions.add("phenotype", "ALZHEIMER DISEASE 2, DUE TO APOE4 ISOFORM");
//        queryOptions.addToListOption("phenotype", "ALZHEIMER");
        queryOptions.addToListOption("phenotype", "alzheimer");
//        queryOptions.addToListOption("phenotype", "retinitis");
//        queryOptions.addToListOption("phenotype", "diabetes");
//        queryOptions.addToListOption("region", new Region("3", 550000, 1166666));
//        queryOptions.add("region", "5:13759611-13799611");
//        queryOptions.addToListOption("region", new Region("1", 550000, 1166666));
//        queryOptions.addToListOption("gene", "APOE");
//        queryOptions.addToListOption("significance", "Likely_pathogenic");
//        queryOptions.addToListOption("review", "REVIEWED_BY_PROFESSIONAL_SOCIETY");
//        queryOptions.addToListOption("type", "Indel");
//        queryOptions.addToListOption("so", "missense_variant");
//        queryOptions.addToListOption("rs", "rs429358");
//        queryOptions.addToListOption("rcv", "RCV000019455");
        queryOptions.add("limit", 30);

        QueryResult queryResult = clinicalDBAdaptor.getAll(queryOptions);

    }

    // TODO: to be finished - properly implemented
    @Ignore
    @Test
    public void testGetPhenotypeGeneRelations() throws Exception {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();

        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions();
//        queryOptions.addToListOption("include", "clinvar");
//        queryOptions.addToListOption("include", "gwas");
        List<QueryResult> queryResultList = clinicalDBAdaptor.getPhenotypeGeneRelations(queryOptions);

    }
}