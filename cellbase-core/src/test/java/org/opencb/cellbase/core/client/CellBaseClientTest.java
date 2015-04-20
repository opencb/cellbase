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
import org.junit.Assert;
import org.junit.Test;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class CellBaseClientTest extends TestCase {

    @Test
    public void testGetGene() throws URISyntaxException, IOException {
        //http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/region/3:1000-200000/gene?of=json
        CellBaseClient cellBaseClient = new CellBaseClient("wwwdev.ebi.ac.uk", 80, "/cellbase/webservices/rest", "v3", "hsapiens");
        QueryResponse<QueryResult<Gene>> gene =
                cellBaseClient.getGene(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.region, Arrays.asList(new Region("3", 1000, 200000)), null);
    }

    @Test
    public void testPost() throws URISyntaxException, IOException {
        //http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/22:10000000:A:T/gene?of=json
        CellBaseClient cellBaseClient = new CellBaseClient("wwwdev.ebi.ac.uk", 80, "/cellbase/webservices/rest", "v3", "hsapiens");
        QueryResponse<QueryResult<VariantAnnotation>> fullAnnotationPost =
                cellBaseClient.getFullAnnotation(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.variant, Arrays.asList(new GenomicVariant("22", 10000000, "A", "T")),
                        new QueryOptions("post", true));
        QueryResponse<QueryResult<VariantAnnotation>> fullAnnotationGet =
                cellBaseClient.getFullAnnotation(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.variant, Arrays.asList(new GenomicVariant("22", 10000000, "A", "T")),
                        new QueryOptions("post", false));
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        Assert.assertEquals(mapper.writeValueAsString(fullAnnotationGet.getResponse().iterator().next().first()),
                mapper.writeValueAsString(fullAnnotationPost.getResponse().iterator().next().first()));
    }

}