package org.opencb.cellbase.client.rest;

import org.junit.Test;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 16/11/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class VariantClientTest {

    private CellBaseClient cellBaseClient;

    public VariantClientTest() {
        ClientConfiguration clientConfiguration;
        try {
            clientConfiguration = ClientConfiguration.load(getClass().getResource("/client-configuration-test.yml").openStream());
            cellBaseClient = new CellBaseClient(clientConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getAnnotations() throws Exception {
        QueryResponse<VariantAnnotation> annotationsGet = cellBaseClient.getVariantClient().getAnnotations("19:45411941:T:C, 14:38679764:-:GATCTG", null);
        assertEquals("SNP Id for the first variant is wrong", "rs429358", annotationsGet.firstResult().getId());
        System.out.println(annotationsGet.getResponse().get(1));
        assertNotNull(annotationsGet.getResponse().get(1));

        String idString = "1:26644214:T:C,1:26771575:-:CATT,1:26776276:CCCACCTCAGGTAATCCG:-,1:26782700:A:G,1:26791626:A:T,1:26794719:A:-,1:26797508:C:T,1:26808191:A:G,1:26812192:C:G,1:26812298:G:A,1:26822975:C:A,1:26827162:G:C,1:26829071:A:G,1:26829582:A:G,1:26830627:A:G,1:26831260:A:G,1:26831377:T:C,1:26833532:T:A,1:26834580:AA:-,1:26834729:AA:-,1:26837067:C:A,1:26839556:T:C,1:26839576:C:A,1:26839578:C:T,1:26839910:T:G,1:26840069:G:A,1:26840240:C:T,1:26843220:T:A,1:26845586:C:T,1:26849312:G:A,1:26857954:T:A,1:26858475:C:T,1:26858779:C:T,1:26865922:G:A,1:26872554:A:G,1:26872832:G:A,1:26876406:C:A,1:26877708:G:A,1:26879376:T:C,1:26880288:C:T,1:26880989:T:C,1:26881806:C:A,1:26886931:T:C,1:26887150:A:G,1:26887592:T:C,1:26893836:G:A,1:26897511:-:G,1:26898142:A:G,1:26898956:T:C,1:26899125:C:T,1:26899687:C:A,1:26907963:A:T,1:26908414:T:A,1:26917919:C:A,1:26925245:C:T,1:26960581:G:-,1:26997922:T:G,1:27021228:A:G,1:27024080:T:G,1:27024611:G:T,1:27594065:T:C,1:27031942:T:C,1:27595862:C:T,1:27037064:C:T,1:27596402:TTGTTG:-,1:27037997:A:G,1:27596590:A:C,1:27046168:A:G,1:27047002:G:T,1:27605432:G:T,1:27047006:G:T,1:27609630:A:-,1:27047016:C:A,1:27615593:T:C,1:27082379:C:T,1:27639947:A:T,1:27083581:T:A,1:27640565:G:C,1:27091581:C:A,1:27643414:A:G,1:27107448:T:A,1:27643517:C:A,1:27130841:A:T,1:27643757:A:-,1:27142786:G:A,1:27145144:T:G,1:27660340:T:C,1:27145444:C:T,1:27660568:G:A,1:27145471:G:A,1:27661037:T:C,1:27157262:TTGTTG:-,1:27661707:T:C,1:27161599:A:G,1:27661756:-:GTA,1:27173728:G:A,1:27662235:G:T,1:27183044:G:A,1:27662666:A:G,1:27198102:A:G";
        annotationsGet = cellBaseClient.getVariantClient()
                .getAnnotations(idString, new QueryOptions("numThreads", 4));
        assertEquals(100, annotationsGet.getResponse().size());

        QueryResponse<VariantAnnotation> annotationsPost = cellBaseClient.getVariantClient()
                .getAnnotations(idString, new QueryOptions("numThreads", 4), true);

        // Check GET and POST calls return exactly the same result
        assertEquals(annotationsGet.getResponse().size(), annotationsPost.getResponse().size());
        Set<VariantAnnotation> getAnnotationSet = new HashSet<>(annotationsGet.getResponse().stream()
                .map(queryResult -> (queryResult.getResult().get(0))).collect(Collectors.toSet()));
        Set<VariantAnnotation> postAnnotationSet = new HashSet<>(annotationsPost.getResponse().stream()
                .map(queryResult -> (queryResult.getResult().get(0))).collect(Collectors.toSet()));

        assertEquals(getAnnotationSet, postAnnotationSet);

    }
}
