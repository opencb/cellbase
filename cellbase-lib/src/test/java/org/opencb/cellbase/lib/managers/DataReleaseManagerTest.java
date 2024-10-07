package org.opencb.cellbase.lib.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.loader.LoaderException;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataReleaseManagerTest extends GenericMongoDBAdaptorTest {

    protected DataReleaseManager dataReleaseManager;

    public DataReleaseManagerTest() throws CellBaseException {
        super();
        dataReleaseManager = cellBaseManagerFactory.getDataReleaseManager(SPECIES, ASSEMBLY);
    }

    @Test
    @Disabled
    public void testCreate() throws JsonProcessingException {
        CellBaseDataResult<DataRelease> result = dataReleaseManager.getReleases();
        DataRelease dr = dataReleaseManager.createRelease();
        assertEquals(result.getNumResults() + 1, dr.getRelease());
    }

    @Test
    @Disabled
    public void testAddActiveByDefaultIn() throws CellBaseException, JsonProcessingException {
        DataRelease dr = dataReleaseManager.createRelease();
        dataReleaseManager.update(dr.getRelease(), Arrays.asList("v5.1", "v5.2"));

        DataRelease auxDr = dataReleaseManager.get(dr.getRelease());
        assertEquals(dr.getRelease(), auxDr.getRelease());
        assertEquals(2, auxDr.getActiveByDefaultIn().size());
    }

    @Test
    @Disabled
    public void testChangeActiveByDefaultIn() throws JsonProcessingException, CellBaseException {
        String version3 = "v5.3";
        String version4 = "v5.4";

        int rA = dataReleaseManager.createRelease().getRelease();
        dataReleaseManager.update(rA, Arrays.asList(version3, version4));

        DataRelease auxDr1 = dataReleaseManager.get(rA);
        assertEquals(2, auxDr1.getActiveByDefaultIn().size());
        assertTrue(auxDr1.getActiveByDefaultIn().contains(version3));
        assertTrue(auxDr1.getActiveByDefaultIn().contains(version4));

        int rB = dataReleaseManager.createRelease().getRelease();
        dataReleaseManager.update(rB, Arrays.asList(version4));

        DataRelease auxDr2 = dataReleaseManager.get(rA);
        assertEquals(1, auxDr2.getActiveByDefaultIn().size(), 1);
        assertEquals(version3, auxDr2.getActiveByDefaultIn().get(0));

        DataRelease auxDr3 = dataReleaseManager.get(rB);
        assertEquals(1, auxDr3.getActiveByDefaultIn().size());
        assertEquals(version4, auxDr3.getActiveByDefaultIn().get(0));
    }

    @Test
    @Disabled
    public void failLoading() throws IOException, ExecutionException, ClassNotFoundException, InterruptedException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, URISyntaxException,
            CellBaseException, LoaderException {
        String version5 = "v5.5";

        int release = dataReleaseManager.createRelease().getRelease();
        dataReleaseManager.update(release, Arrays.asList(version5));

        Path path = Paths.get(getClass()
                .getResource("/variant-annotation/gene.test.json.gz").toURI());

        CellBaseException thrown = Assertions.assertThrows(CellBaseException.class, () -> { loadRunner.load(path, "gene", release); });
        Assertions.assertTrue(thrown.getMessage().contains("since it has already assigned CellBase versions: " + version5));
    }

    @Test
    @Disabled
    public void testMultipleAddActiveByDefaultIn() throws JsonProcessingException, CellBaseException {
        String version6 = "v5.6";
        String version7 = "v5.7";

        int rA = dataReleaseManager.createRelease().getRelease();
        dataReleaseManager.update(rA, Arrays.asList(version6));

        DataRelease auxDr1 = dataReleaseManager.get(rA);
        assertEquals(auxDr1.getActiveByDefaultIn().size(), 1);
        assertTrue(auxDr1.getActiveByDefaultIn().contains(version6));

        dataReleaseManager.update(rA, Arrays.asList(version7));

        DataRelease auxDr2 = dataReleaseManager.get(rA);
        assertEquals(2, auxDr2.getActiveByDefaultIn().size());
        assertTrue(auxDr2.getActiveByDefaultIn().contains(version6));
        assertTrue(auxDr2.getActiveByDefaultIn().contains(version7));
    }

    @Test
    @Disabled
    public void testRemoveMultipleAddActiveByDefaultIn() throws JsonProcessingException, CellBaseException {
        String version8 = "v5.8";
        String version9 = "v5.9";

        int rA = dataReleaseManager.createRelease().getRelease();
        dataReleaseManager.update(rA, Arrays.asList(version8, version9));

        DataRelease auxDr1 = dataReleaseManager.get(rA);
        assertEquals(2, auxDr1.getActiveByDefaultIn().size());
        assertTrue(auxDr1.getActiveByDefaultIn().contains(version8));
        assertTrue(auxDr1.getActiveByDefaultIn().contains(version9));

        int rB = dataReleaseManager.createRelease().getRelease();
        dataReleaseManager.update(rB, Arrays.asList(version8, version9));

        auxDr1 = dataReleaseManager.get(rA);
        assertEquals(0, auxDr1.getActiveByDefaultIn().size());

        DataRelease auxDr2 = dataReleaseManager.get(rB);
        assertEquals(2, auxDr2.getActiveByDefaultIn().size());
        assertTrue(auxDr2.getActiveByDefaultIn().contains(version8));
        assertTrue(auxDr2.getActiveByDefaultIn().contains(version9));
    }

    @Test
    @Disabled
    public void testAnnotation() throws CellBaseException, QueryException, ExecutionException, InterruptedException,
            IllegalAccessException {
        dataReleaseManager.update(1, Arrays.asList("v5.5"));

        DataRelease dataRelease = dataReleaseManager.get(1);
        VariantAnnotationCalculator annotator = new VariantAnnotationCalculator(SPECIES, ASSEMBLY, dataRelease, apiKey,
                cellBaseManagerFactory, cellBaseConfiguration);

        Variant variant = new Variant("10", 113588287, "G", "A");
        CellBaseDataResult<VariantAnnotation> cellBaseDataResult = annotator.getAnnotationByVariant(variant, QueryOptions.empty());
        VariantAnnotation variantAnnotation = cellBaseDataResult.first();
        System.out.println(variantAnnotation);
        assertEquals(variant.getChromosome(), variantAnnotation.getChromosome());
        assertEquals(variant.getStart(), variantAnnotation.getStart());
        assertEquals(variant.getReference(), variantAnnotation.getReference());
        assertEquals(variant.getAlternate(), variantAnnotation.getAlternate());
    }
}