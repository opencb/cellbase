package org.opencb.cellbase.client.rest;

import org.junit.Test;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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
//        QueryResponse<List> queryResponse = cellBaseClient.getProteinClient().getSubstitutionScores("Q9UL59", null);
//        assertNotNull("Substitution Scores for the given protein is not null", queryResponse.firstResult());
//    }

    @Test
    public void getSequence() throws Exception {
        QueryResponse<String> queryResponse = cellBaseClient.getProteinClient().getSequence("Q9UL59", new QueryOptions());
        assertEquals("Sequence is not the same", "MAVTFEDVTIIFTWEEWKFLDSSQKRLYREVMWENYTNVMSVENWNESYKSQEEKFRYLEYENF" +
                "SYWQGWWNAGAQMYENQNYGETVQGTDSKDLTQQDRSQCQEWLILSTQVPGYGNYELTFESKSLRNLKYKNFMPWQSLETKTTQDYGREIYMSGSHGFQG" +
                "GRYRLGISRKNLSMEKEQKLIVQHSYIPVEEALPQYVGVICQEDLLRDSMEEKYCGCNKCKGIYYWNSRCVFHKRNQPGENLCQCSICKACFSQRSDLYR" +
                "HPRNHIGKKLYGCDEVDGNFHQSSGVHFHQRVHIGEVPYSCNACGKSFSQISSLHNHQRVHTEEKFYKIECDKDLSRNSLLHIHQRLHIGEKPFKCNQCGKS" +
                "FNRSSVLHVHQRVHTGEKPYKCDECGKGFSQSSNLRIHQLVHTGEKSYKCEDCGKGFTQRSNLQIHQRVHTGEKPYKCDDCGKDFSHSSDLRIHQRVHTGEK" +
                "PYTCPECGKGFSKSSKLHTHQRVHTGEKPYKCEECGKGFSQRSHLLIHQRVHTGEKPYKCHDCGKGFSHSSNLHIHQRVHTGEKPYQCAKCGKGFSHSSAL" +
                "RIHQRVHAGEKPYKCREYYKGFDHNSHLHNNHRRGNL", queryResponse.firstResult());
    }

}