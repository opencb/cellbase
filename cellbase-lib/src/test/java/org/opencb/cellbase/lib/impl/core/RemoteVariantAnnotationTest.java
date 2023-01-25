package org.opencb.cellbase.lib.impl.core;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class RemoteVariantAnnotationTest {

    //------------------------------------------------------------------------
    // IMPORTANT
    // To run these tests check and update remote.congiguration.test.yaml
    // and create the corresponding tunnel to the CellBase MongoDB
    //------------------------------------------------------------------------

    private int dataRelease = 3;
    private String species = "hsapiens";
    private String assembly = "grch38";

    private CellBaseConfiguration cellBaseConfiguration;
    private CellBaseManagerFactory cellBaseManagerFactory;
    private VariantAnnotationCalculator variantAnnotationCalculator;

    public RemoteVariantAnnotationTest() throws IOException, CellBaseException {
        this.cellBaseConfiguration = CellBaseConfiguration.load(
                GenericMongoDBAdaptorTest.class.getClassLoader().getResourceAsStream("remote.configuration.test.yaml"),
                CellBaseConfiguration.ConfigurationFileFormat.YAML);

        this.cellBaseManagerFactory = new CellBaseManagerFactory(cellBaseConfiguration);

        this.variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly, dataRelease, cellBaseManagerFactory);
    }

    @Test
    public void testEmptyConsequenceTypeListDEL() throws QueryException, ExecutionException, InterruptedException, CellBaseException,
            IllegalAccessException {

        List<Pair<Variant, Integer>> variants = new ArrayList<>();
        variants.add(new ImmutablePair<>(new Variant("10:101008524:G:-"), 29));
        variants.add(new ImmutablePair<>(new Variant("10:101018284:T:-"), 20));
        variants.add(new ImmutablePair<>(new Variant("10:101694601:CCT:-"), 10));
        variants.add(new ImmutablePair<>(new Variant("10:102834048:A:-"), 13));
        variants.add(new ImmutablePair<>(new Variant("10:102834061:A:-"), 13));
        variants.add(new ImmutablePair<>(new Variant("10:103089679:TCC:-"), 73));
        variants.add(new ImmutablePair<>(new Variant("10:103089679:TCCTCC:-"), 73));
        variants.add(new ImmutablePair<>(new Variant("10:103089691:TCCTCT:-"), 73));
        variants.add(new ImmutablePair<>(new Variant("10:103089694:TCT:-"), 73));
        variants.add(new ImmutablePair<>(new Variant("10:104059681:T:-"), 9));
        variants.add(new ImmutablePair<>(new Variant("10:104145538:T:-"), 6));
        variants.add(new ImmutablePair<>(new Variant("10:118691379:AG:-"), 8));

        checkAnnotation(variants);
    }

    @Test
    public void testEmptyConsequenceTypeListSNV() throws QueryException, ExecutionException, InterruptedException, CellBaseException,
            IllegalAccessException {

        List<Pair<Variant, Integer>> variants = new ArrayList<>();
        variants.add(new ImmutablePair<>(new Variant("10:89332624:T:C"), 30));
        variants.add(new ImmutablePair<>(new Variant("12:56117206:G:T"), 25));
        variants.add(new ImmutablePair<>(new Variant("1:39282328:T:A"), 16));
        variants.add(new ImmutablePair<>(new Variant("1:39254314:G:T"), 14));
        variants.add(new ImmutablePair<>(new Variant("1:39254319:C:T"), 14));
        variants.add(new ImmutablePair<>(new Variant("1:39282366:C:A"), 16));
        variants.add(new ImmutablePair<>(new Variant("1:39254320:G:A"), 14));
        variants.add(new ImmutablePair<>(new Variant("1:39254345:G:T"), 14));
        variants.add(new ImmutablePair<>(new Variant("1:39282374:G:A"), 16));
        variants.add(new ImmutablePair<>(new Variant("1:39254347:T:C"), 14));
        variants.add(new ImmutablePair<>(new Variant("1:39254353:T:A"), 14));
        variants.add(new ImmutablePair<>(new Variant("6:56900543:C:T"), 26));
        variants.add(new ImmutablePair<>(new Variant("6:56900614:C:T"), 26));
        variants.add(new ImmutablePair<>(new Variant("6:56954455:C:T"), 38));
        variants.add(new ImmutablePair<>(new Variant("6:56954502:A:C"), 38));
        variants.add(new ImmutablePair<>(new Variant("6:56954528:G:C"), 38));
        variants.add(new ImmutablePair<>(new Variant("8:98883755:A:G"), 15));

        checkAnnotation(variants);
    }

    private void checkAnnotation(List<Pair<Variant, Integer>> variants) throws QueryException, ExecutionException, InterruptedException,
            CellBaseException, IllegalAccessException {

        for (Pair<Variant, Integer> pair : variants) {
            Variant variant = pair.getKey();
            System.out.println(variant.toStringSimple());
            CellBaseDataResult<VariantAnnotation> result = variantAnnotationCalculator.getAnnotationByVariant(variant, QueryOptions.empty());
            VariantAnnotation variantAnnotation = result.first();
            assertEquals(variant.getChromosome(), variantAnnotation.getChromosome());
            assertEquals(variant.getStart(), variantAnnotation.getStart());
            assertEquals(variant.getReference(), variantAnnotation.getReference());
//            System.out.println(variantAnnotation.getConsequenceTypes().size());
            assertEquals(0l + pair.getValue(), variantAnnotation.getConsequenceTypes().size());
        }
    }

}
