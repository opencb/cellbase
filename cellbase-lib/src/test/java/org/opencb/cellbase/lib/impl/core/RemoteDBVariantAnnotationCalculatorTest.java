package org.opencb.cellbase.lib.impl.core;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RemoteDBVariantAnnotationCalculatorTest {

    //------------------------------------------------------------------------
    // IMPORTANT
    // To run these tests you need to hava the following config file
    // /opt/cellbase/remote.configuration.test.yaml
    // (useful if you have a tunnel to the CellBase MongoDB)
    //------------------------------------------------------------------------

    private int dataRelease = 3;
    private String species = "hsapiens";
    private String assembly = "grch38";

    private CellBaseConfiguration cellBaseConfiguration;
    private CellBaseManagerFactory cellBaseManagerFactory;
    private VariantAnnotationCalculator variantAnnotationCalculator;

    private String SPLICEAI_HGMD_COSMIC_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VzIjp7ImNvc21pYyI6OTIyMzM3MjAzNjg1NDc3NTgwNywic3BsaWNlYWkiOjkyMjMzNzIwMzY4NTQ3NzU4MDcsImhnbWQiOjkyMjMzNzIwMzY4NTQ3NzU4MDd9LCJ2ZXJzaW9uIjoiMS4wIiwic3ViIjoiVEVTVCIsImlhdCI6MTY3Nzc3NTQzMn0.Z-F2WSRkRMyl_uFkf1lHg4WXr49fZvTLLcEc9LCOapU";

    public RemoteDBVariantAnnotationCalculatorTest() {
    }

    @Before
    public void before() throws IOException, CellBaseException {
        File configFile = Paths.get("/opt/cellbase/remote.configuration.test.yaml").toFile();
        Assume.assumeTrue(configFile.exists());

        this.cellBaseConfiguration = CellBaseConfiguration.load(new FileInputStream(configFile.getAbsoluteFile().toString()),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);

        this.cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);
    }

    @Test
    public void testWithoutToken() throws QueryException, ExecutionException, InterruptedException, CellBaseException, IllegalAccessException {
        variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly, dataRelease, "", cellBaseManagerFactory);

        Variant variant = new Variant("1:1000012:C:A");
        CellBaseDataResult<VariantAnnotation> result = variantAnnotationCalculator.getAnnotationByVariant(variant, QueryOptions.empty());
        VariantAnnotation variantAnnotation = result.first();


        for (ConsequenceType ct : variantAnnotation.getConsequenceTypes()) {
            if ("ENST00000304952.11".equals(ct.getEnsemblTranscriptId())) {
                assertEquals(1, ct.getSpliceScores().size());
                assertEquals("mmsplice", ct.getSpliceScores().get(0).getSource().toLowerCase());
                return;
            }
        }
        fail();
    }

    @Test
    public void testSpliceToken() throws QueryException, ExecutionException, InterruptedException, CellBaseException, IllegalAccessException {
        this.variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly, dataRelease, SPLICEAI_HGMD_COSMIC_TOKEN, cellBaseManagerFactory);

        Variant variant = new Variant("1:1000012:C:A");
        CellBaseDataResult<VariantAnnotation> result = variantAnnotationCalculator.getAnnotationByVariant(variant, QueryOptions.empty());
        VariantAnnotation variantAnnotation = result.first();

        for (ConsequenceType ct : variantAnnotation.getConsequenceTypes()) {
            if ("ENST00000304952.11".equals(ct.getEnsemblTranscriptId())) {
                assertEquals(2, ct.getSpliceScores().size());
                assertEquals("mmsplice", ct.getSpliceScores().get(0).getSource().toLowerCase());
                assertEquals("spliceai", ct.getSpliceScores().get(1).getSource().toLowerCase());
                return;
            }
        }
        fail();
    }
}
