package org.opencb.cellbase.build.transform;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;
import org.opencb.cellbase.lib.mongodb.serializer.MongoDBSerializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by antonior on 10/16/14.
 */
public class DisGeNetParserTest {

    private static Path filedisgenet;
    private static Path fileEntrezIdToEnsemblId;
    private static CellBaseSerializer serializer;

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, IOException {
        URL resource_disnet_file = DisGeNetParserTest.class.getResource("/all_gene_disease_association_sample2.txt");
        System.out.println(resource_disnet_file);
        filedisgenet = Paths.get(resource_disnet_file.toURI());

        URL resource_entrezIdToEnsemblId_file = DisGeNetParserTest.class.getResource("/entrezIdToEnsemblId_sample.txt");
        System.out.println(resource_entrezIdToEnsemblId_file);
        fileEntrezIdToEnsemblId = Paths.get(resource_entrezIdToEnsemblId_file.toURI());

        //serializer = new MongoDBSerializer(Paths.get("/tmp/"));
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        DisGeNetParser instance = new DisGeNetParser(serializer, filedisgenet, fileEntrezIdToEnsemblId);
        instance.parse();
    }
}
