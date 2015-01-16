package org.opencb.cellbase.core.client;

import junit.framework.TestCase;
import org.junit.Test;
import org.opencb.biodata.models.feature.Region;
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
    public void test() throws URISyntaxException, IOException {
        //http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/region/3:1000-200000/gene?of=json
        CellBaseClient cellBaseClient = new CellBaseClient("wwwdev.ebi.ac.uk", 80, "/cellbase/webservices/rest", "v3", "hsapiens");
        QueryResponse<QueryResult<Gene>> gene =
                cellBaseClient.getGene(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.region, Arrays.asList(new Region("3", 1000, 200000)), null);
    }

}