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

import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.result.CellBaseDataResponse;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on 16/11/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VariantClientTest {


    public VariantClientTest() {
    }

    @ParameterizedTest
    @ArgumentsSource(CellbaseClientProvider.class)
    @Disabled
    public void getAnnotationId(CellBaseClient cellBaseClient) throws Exception {
        // Check assembly = GRCh38 is being correctly sent by the client and appropriately managed by the server
        CellBaseDataResponse<VariantAnnotation> annotationsGet = cellBaseClient.getVariantClient()
                .getAnnotationByVariantIds(Collections.singletonList("13:32316514:C:G"), null);
        assertEquals("13:32316514:C:G", annotationsGet.firstResult().getId(), "SNP Id for the first variant is wrong");
    }

    @ParameterizedTest
    @ArgumentsSource(CellbaseClientProvider.class)
    public void getAnnotations(CellBaseClient cellBaseClient) throws Exception {
        CellBaseDataResponse<VariantAnnotation> annotationsGet;

        List<String> idString = Arrays.asList("1:26644214:T:C,1:26771575:-:CATT,1:26776276:CCCACCTCAGGTAATCCG:-,1:26782700:A:G,1:26791626:A:T,1:26794719:A:-,1:26797508:C:T,1:26808191:A:G,1:26812192:C:G,1:26812298:G:A,1:26822975:C:A,1:26827162:G:C,1:26829071:A:G,1:26829582:A:G,1:26830627:A:G,1:26831260:A:G,1:26831377:T:C,1:26833532:T:A,1:26834580:AA:-,1:26834729:AA:-,1:26837067:C:A,1:26839556:T:C,1:26839576:C:A,1:26839578:C:T,1:26839910:T:G,1:26840069:G:A,1:26840240:C:T,1:26843220:T:A,1:26845586:C:T,1:26849312:G:A,1:26857954:T:A,1:26858475:C:T,1:26858779:C:T,1:26865922:G:A,1:26872554:A:G,1:26872832:G:A,1:26876406:C:A,1:26877708:G:A,1:26879376:T:C,1:26880288:C:T,1:26880989:T:C,1:26881806:C:A,1:26886931:T:C,1:26887150:A:G,1:26887592:T:C,1:26893836:G:A,1:26897511:-:G,1:26898142:A:G,1:26898956:T:C,1:26899125:C:T,1:26899687:C:A,1:26907963:A:T,1:26908414:T:A,1:26917919:C:A,1:26925245:C:T,1:26960581:G:-,1:26997922:T:G,1:27021228:A:G,1:27024080:T:G,1:27024611:G:T,1:27594065:T:C,1:27031942:T:C,1:27595862:C:T,1:27037064:C:T,1:27596402:TTGTTG:-,1:27037997:A:G,1:27596590:A:C,1:27046168:A:G,1:27047002:G:T,1:27605432:G:T,1:27047006:G:T,1:27609630:A:-,1:27047016:C:A,1:27615593:T:C,1:27082379:C:T,1:27639947:A:T,1:27083581:T:A,1:27640565:G:C,1:27091581:C:A,1:27643414:A:G,1:27107448:T:A,1:27643517:C:A,1:27130841:A:T,1:27643757:A:-,1:27142786:G:A,1:27145144:T:G,1:27660340:T:C,1:27145444:C:T,1:27660568:G:A,1:27145471:G:A,1:27661037:T:C,1:27157262:TTGTTG:-,1:27661707:T:C,1:27161599:A:G,1:27661756:-:GTA,1:27173728:G:A,1:27662235:G:T,1:27183044:G:A,1:27662666:A:G,1:27198102:A:G,1:645710:A:<INS:ME:ALU>".split(","));
        annotationsGet = cellBaseClient.getVariantClient()
                .getAnnotationByVariantIds(idString, new QueryOptions("numThreads", 4).append("assembly", "GRCh38"), false);
        assertEquals(101, annotationsGet.getResponses().size());

        CellBaseDataResponse<VariantAnnotation> annotationsPost = cellBaseClient.getVariantClient()
                .getAnnotationByVariantIds(idString, new QueryOptions("numThreads", 4).append("assembly", "GRCh38"), true);

        // Check GET and POST calls return exactly the same result
        assertEquals(annotationsGet.getResponses().size(), annotationsPost.getResponses().size());
        Map<String, VariantAnnotation> getAnnotationSet = annotationsGet.getResponses().stream()
                .map(CellBaseDataResult -> (CellBaseDataResult.getNumResults() > 0 ? CellBaseDataResult.getResults().get(0) : null))
                .collect(Collectors.toMap(v -> v.getChromosome() + ":" + v.getStart() + ":" + v.getReference() + ":" + v.getAlternate(), v -> v));
        Map<String, VariantAnnotation> postAnnotationSet = annotationsPost.getResponses().stream()
                .map(CellBaseDataResult -> (CellBaseDataResult.getNumResults() > 0 ? CellBaseDataResult.getResults().get(0) : null))
                .collect(Collectors.toMap(v -> v.getChromosome() + ":" + v.getStart() + ":" + v.getReference() + ":" + v.getAlternate(), v -> v));


        assertEquals(getAnnotationSet.size(), postAnnotationSet.size());
        assertEquals(getAnnotationSet.keySet(), postAnnotationSet.keySet());
        boolean withTranscriptFlags = false;
        for (String id : getAnnotationSet.keySet()) {
            VariantAnnotation va1 = getAnnotationSet.get(id);
            VariantAnnotation va2 = postAnnotationSet.get(id);
            orderVariantAnnotation(va1);
            orderVariantAnnotation(va2);
            assertEquals(va1, va2, () -> id + " :\n" + va1 + "\n" + va2);

            for (ConsequenceType consequenceType : va1.getConsequenceTypes()) {
                if (consequenceType.getTranscriptFlags() != null) {
                    withTranscriptFlags = true;
                }
            }
        }
        assertTrue(withTranscriptFlags);
        assertEquals(getAnnotationSet, postAnnotationSet);
    }

    private void orderVariantAnnotation(VariantAnnotation va) {
        va.getConsequenceTypes().forEach(c->{
            if (c.getProteinVariantAnnotation() != null && c.getProteinVariantAnnotation().getFeatures() != null) {
                c.getProteinVariantAnnotation().getFeatures().sort(Comparator.comparing(SpecificRecordBase::hashCode));
            }
        });
    }

//    @Test
//    public void count() throws Exception {
//        CellBaseDataResponse<Long> count = cellBaseClient.getVariationClient().count(new Query());
//        assertTrue("Number of returned variants do not match", count.firstResult().longValue() > 329000000 );
//    }

//    @Test
//    public void first() throws Exception {
//        CellBaseDataResponse<Variant> first = cellBaseClient.getVariationClient().first();
//        assertNotNull(first,"First Variation in the collection must be returned");
//    }

    @ParameterizedTest
    @ArgumentsSource(CellbaseClientProvider.class)
    @Disabled
    public void get(CellBaseClient cellBaseClient) throws Exception {
        CellBaseDataResponse<Variant> variation = cellBaseClient.getVariantClient().get(Collections.singletonList("13:32316514:C:G"), null);
        assertNotNull(variation.firstResult(), "This variation should exist");
    }

    @ParameterizedTest
    @ArgumentsSource(CellbaseClientProvider.class)
    public void getAllConsequenceTypes(CellBaseClient cellBaseClient) throws Exception {
        CellBaseDataResponse<String> response = cellBaseClient.getVariantClient().getAllConsequenceTypes(new Query());
        assertNotNull(response.firstResult(), "List of all the consequence types present should be returned");
    }

//    @Test
//    public void getConsequenceTypeById() throws Exception {
//        CellBaseDataResponse<String> stringCellBaseDataResponse = cellBaseClient.getVariantClient().getConsequenceTypeById("22:35490160:G:A", null);
//        assertEquals("Consequence Type of rs6661 is wrong", "3_prime_UTR_variant", stringCellBaseDataResponse.firstResult());
//    }
}
