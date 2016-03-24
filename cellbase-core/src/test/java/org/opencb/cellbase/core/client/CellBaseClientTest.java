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

package org.opencb.cellbase.core.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.*;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

public class CellBaseClientTest extends TestCase {

    private CellBaseClient cellBaseClient;

    @BeforeClass
    public static void beforeClass() {

    }

    @After
    public void tearDown() {

    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cellBaseClient = new CellBaseClient("bioinfodev.hpc.cam.ac.uk", 80, "/cellbase-dev-v4.0/webservices/rest", "v4", "hsapiens");
    }

    @Test
    public void testGetGene() throws URISyntaxException, IOException {
        //http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/region/3:1000-200000/gene?of=json
        QueryResponse<QueryResult<Gene>> gene =
                cellBaseClient.getGene(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.region, Arrays.asList(new Region("3", 1000, 200000)), null);
        assertEquals(gene.getResponse().get(0).getResult().get(0).getName(), "AY269186.1");
    }

    // TODO: to be reimplemented. CellBaseClient will soon be refactored
//    @Test
//    public void testGetProtein() throws URISyntaxException, IOException {
//        QueryResponse<QueryResult<Entry>> response =
//              cellBaseClient.getInfo(CellBaseClient.Category.feature, CellBaseClient.SubCategory.protein,"ZUFSP_HUMAN", null);
//        assertEquals(response.getResponse().get(0).getResult().get(0).getName(), "ZUFSP_HUMAN");
//    }



    @Test
    public void testGetSequence() throws URISyntaxException, IOException {
        //http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/region/3:1000-200000/gene?of=json
        QueryResponse<QueryResult<GenomeSequenceFeature>> sequence =
                cellBaseClient.getSequence(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.region, Collections.singletonList(new Region("20", 60522, 60622)), null);
        assertEquals(sequence.getResponse().get(0).getResult().get(0).getSequence(), "TCCCCCCTGGCACAAATGGTGCTGGACCACGAGGGGCCAGAGAACAAAGCCTTGGGCGTGGTCCCAACTCCCAAATGTTTGAACACACAAGTTGGAATATT");
    }

    @Test
    public void testGetChromosome() throws URISyntaxException, IOException {
        QueryResponse<QueryResult<InfoStats>> response =
                cellBaseClient.getInfo(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.chromosome, "2", null);
        assertEquals(cellBaseClient.getLastQuery().toString(), "2", response.getResponse().get(0).getResult().get(0).getChromosomes().get(0).getName());
    }

    @Test
    public void testPost() throws URISyntaxException, IOException {
        //http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/22:10000000:A:T/gene?of=json
        QueryResponse<QueryResult<VariantAnnotation>> fullAnnotationPost =
                cellBaseClient.getAnnotation(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.variant, Arrays.asList(new Variant("22", 10000000, "A", "T")),
                        new QueryOptions("post", true));
        QueryResponse<QueryResult<VariantAnnotation>> fullAnnotationGet =
                cellBaseClient.getAnnotation(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.variant, Arrays.asList(new Variant("22", 10000000, "A", "T")),
                        new QueryOptions("post", false));
        Assert.assertEquals(fullAnnotationGet.getResponse().iterator().next().first().toString(),
                fullAnnotationPost.getResponse().iterator().next().first().toString());
    }

}