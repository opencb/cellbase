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

package org.opencb.cellbase.client.rest;

import org.junit.jupiter.api.Test;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.core.CellBaseDataResponse;
import org.opencb.commons.datastore.core.QueryOptions;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by swaathi on 25/05/16.
 */
public class ProteinClientTest {
    private CellBaseClient cellBaseClient;

    public ProteinClientTest() {
        try {
            cellBaseClient = new CellBaseClient(ClientConfiguration.load(getClass().getResourceAsStream("/client-configuration-test.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void getSubstitutionScores() throws Exception {
//        CellBaseDataResponse<List> queryResponse = cellBaseClient.getProteinClient().getSubstitutionScores("Q9UL59", null);
//        assertNotNull("Substitution Scores for the given protein is not null", queryResponse.firstResult());
//    }

    @Test
    public void getSequence() throws Exception {
        CellBaseDataResponse<String> queryResponse = cellBaseClient.getProteinClient().getSequence("Q9UL59", new QueryOptions());
        assertEquals("Sequence is not the same", "MAVTFEDVTIIFTWEEWKFLDSSQKRLYREVMWENYTNVMSVENWNESYKSQEEKFRYLEYENF" +
                "SYWQGWWNAGAQMYENQNYGETVQGTDSKDLTQQDRSQCQEWLILSTQVPGYGNYELTFESKSLRNLKYKNFMPWQSLETKTTQDYGREIYMSGSHGFQG" +
                "GRYRLGISRKNLSMEKEQKLIVQHSYIPVEEALPQYVGVICQEDLLRDSMEEKYCGCNKCKGIYYWNSRCVFHKRNQPGENLCQCSICKACFSQRSDLYR" +
                "HPRNHIGKKLYGCDEVDGNFHQSSGVHFHQRVHIGEVPYSCNACGKSFSQISSLHNHQRVHTEEKFYKIECDKDLSRNSLLHIHQRLHIGEKPFKCNQCGKS" +
                "FNRSSVLHVHQRVHTGEKPYKCDECGKGFSQSSNLRIHQLVHTGEKSYKCEDCGKGFTQRSNLQIHQRVHTGEKPYKCDDCGKDFSHSSDLRIHQRVHTGEK" +
                "PYTCPECGKGFSKSSKLHTHQRVHTGEKPYKCEECGKGFSQRSHLLIHQRVHTGEKPYKCHDCGKGFSHSSNLHIHQRVHTGEKPYQCAKCGKGFSHSSAL" +
                "RIHQRVHAGEKPYKCREYYKGFDHNSHLHNNHRRGNL", queryResponse.firstResult());
    }

}