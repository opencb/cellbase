package org.opencb.cellbase.client.grpc;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by swaathi on 27/05/16.
 */
public class GeneGrpcClientTest {
    private CellbaseGrpcClient cellBaseGrpcClient;

    public GeneGrpcClientTest() {
        cellBaseGrpcClient = new CellbaseGrpcClient("localhost", 9090);
    }

    @Test
    public void count() throws Exception {
        Long count = cellBaseGrpcClient.getGeneClient().count(new HashMap<>());
        assertEquals("Number of genes does not match", 57905, count.longValue());
        System.out.println(count);
    }

}