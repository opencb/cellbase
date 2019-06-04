package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.core.loader.LoadRunner;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by fjlopez on 24/03/17.
 */
public class ClinicalMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public ClinicalMongoDBAdaptorTest() throws IOException {
    }

    @Test
    public void phasedQueriesTest() throws Exception {

        // Load test data
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/clinicalMongoDBAdaptor/phasedQueries/clinical_variants.full.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants");

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens",
                "GRCh37");

        // Two variants being queried with PS and genotype. The PS is different in each of them. In the database, these
        // variants form an MNV. Both of them should be returned since the fact of having different PS indicates that
        // it's unknown if alternate alleles are in the same chromosome copy or not, i.e. could potentially  be in the
        // same chromosome copy
        VariantBuilder variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1")));
        Variant variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653363", "0|1")));
        Variant variant1 = variantBuilder.build();

        List<QueryResult<Variant>> variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        QueryResult<Variant> variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Two variants being queried with PS and genotype. Second is hom reference. Both have the same PS (100653362).
        // In the database, these variants form an MNV. None should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1")));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "0|0")));
        variant1 = variantBuilder.build();

         variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = variantQueryResultList.get(0);
        assertEquals(0, variantQueryResult.getNumResults());
        assertEquals(0, variantQueryResult.getNumTotalResults());
        assertTrue(variantQueryResult.getResult().isEmpty());

        variantQueryResult = variantQueryResultList.get(1);
        assertNotNull(variantQueryResult);
        assertEquals(0, variantQueryResult.getNumResults());
        assertEquals(0, variantQueryResult.getNumTotalResults());
        assertTrue(variantQueryResult.getResult().isEmpty());

        // Two X variants being queried with PS and genotype. First is haploid. Second is diploid (heterozygous) AND
        // uses the phased genotype i.e. '|'. Both have the same PS (100653362). In the database, these variants form
        // an MNV. Both of them should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1")));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "0|1")));
        variant1 = variantBuilder.build();

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Two X variants being queried with PS and genotype. First is haploid. Second is diploid (heterozygous) AND
        // uses the phased genotype i.e. '|'. Both have the same PS (100653362). Same as the one above but in this case
        // the '1' of the diploid is placed on the other side of the '|'. In the database, these variants form
        // an MNV. Both of them should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1")));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1|0")));
        variant1 = variantBuilder.build();

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));


        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Two X variants being queried with PS and genotype. First is haploid. Second is diploid (heterozygous) AND
        // uses the non phased genotype - not sure if this is allowed in VCF - i.e. '/'. Both
        // have the same PS (100653362). In the database, these variants form an MNV. Both of them should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1")));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("100653362", "1/0")));
        variant1 = variantBuilder.build();

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));


        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(2, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Two variants being queried; one with PS and PHASED genotype, the other with PS but UN-PHASED genotype:
        // in the database, these two variant form an MNV. Comparison of a phased genotype and un-phased genotype
        // (as long as the alternate allele is present in both) considers that alternate allele could potentially be in
        // the same copy. Therefore both variants should be returned with all their EvidenceEntries
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("115256528", "0|1")));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("1",
                115256529,
                115256529,
                "T",
                "A");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("115256528", "1/0")));
        variant1 = variantBuilder.build();

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1",
                115256528,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(1, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1",
                115256529,
                "T",
                "A"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(1, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Just one variant being queried with PS and genotype: in the database, this variant forms an MNV with
        // another. Since just one of the two is being queried (input list) no results should be returned
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("115256528", "0|1")));
        variant = variantBuilder.build();

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(variant),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantQueryResultList.size());
        variantQueryResult = variantQueryResultList.get(0);
        assertEquals(0, variantQueryResult.getNumResults());
        assertEquals(0, variantQueryResult.getNumTotalResults());
        assertTrue(variantQueryResult.getResult().isEmpty());


        // Two variants being queried; one with PS but NOT genotype, the other with missing phase data: in the database,
        // these two variant forms an MNV. Both of them should be returned with all their EvidenceEntries
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setFormat("PS");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("115256528")));
        variant = variantBuilder.build();
        variant1 = new Variant("1", 115256529, "T", "A");

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1",
                115256528,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(1, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1",
                115256529,
                "T",
                "A"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(1, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Just one variant being queried with PS but NOT genotype: in the database, this variant forms an MNV with
        // another. Since just one of the two is being queried (input list) no results should be returned
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setFormat("PS");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("115256528")));
        variant = variantBuilder.build();

        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(variant),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantQueryResultList.size());
        variantQueryResult = variantQueryResultList.get(0);
        assertEquals(0, variantQueryResult.getNumResults());
        assertEquals(0, variantQueryResult.getNumTotalResults());
        assertTrue(variantQueryResult.getResult().isEmpty());

        // Classic, simple query; one variant queried with missing phase data: in the database, same variant is stored,
        // also without phase data for any of its three EvidenceEntries. That variant with its three EvidenceEntries
        // should be returned
        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(new Variant("14", 55369176, "G", "A")),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("14",
                55369176,
                "G",
                "A"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(3, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Two variants with missing phase data (therefore potentially forming an MNV) being queried: in the database,
        // these two variants also form an MNV. Results should be returned for both
        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(new Variant("1", 115256528, "T", "C"),
                        new Variant("1", 115256529, "T", "A")),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1",
                115256528,
                "T",
                "C"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(1, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1",
                115256529,
                "T",
                "A"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(1, variantQueryResult.getResult().get(0).getAnnotation().getTraitAssociation().size());

        // Just one variant being queried: in the database, this variant forms an MNV with another. Since just one of
        // the two is being queried (input list) no results should be returned
        variantQueryResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(new Variant("1", 115256528, "T", "C")),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantQueryResultList.size());
        variantQueryResult = variantQueryResultList.get(0);
        assertEquals(0, variantQueryResult.getNumResults());
        assertEquals(0, variantQueryResult.getNumTotalResults());
        assertTrue(variantQueryResult.getResult().isEmpty());

        // Just one variant being queried: in the database, this variant is duplicated. An exception must be raised
        // since variants are not expected to be repeated in the database
        try {
            variantQueryResultList = clinicalDBAdaptor.getByVariant(
                    Collections.singletonList(new Variant("1", 1, "T", "A")),
                    new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));
            assert false;
        } catch (RuntimeException runTimeException) {
            assertEquals("Unexpected: more than one result found in the clinical variant "
                    + "collection for variant 1:1:T:A. Please, check.", runTimeException.getMessage());
        }

    }

    @Test
    public void nativeGet() throws Exception {

        // Load test data
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/clinicalMongoDBAdaptor/nativeGet/clinical_variants.full.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants");

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions1 = new QueryOptions();

        Query query1 = new Query();
        query1.put(ClinicalDBAdaptor.QueryParams.TRAIT.key(), "alzheimer");
        queryOptions1.add(QueryOptions.INCLUDE, "annotation.traitAssociation.id");
        QueryResult<Variant> queryResult1 = clinicalDBAdaptor.get(query1, queryOptions1);
        assertEquals(1, queryResult1.getNumResults());
        assertTrue(containsAccession(queryResult1, "RCV000172777"));

        Query query2 = new Query();
        query2.put(ClinicalDBAdaptor.QueryParams.TRAIT.key(), "myelofibrosis");
        QueryOptions queryOptions2 = new QueryOptions();
        queryOptions2.add(QueryOptions.INCLUDE, "annotation.traitAssociation.id");
        QueryResult queryResult2 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        assertEquals(1, queryResult2.getNumResults());

        Query query4 = new Query();
        query4.put(ClinicalDBAdaptor.QueryParams.REGION.key(),
                new Region("2", 170360030, 170362030));
        QueryOptions queryOptions4 = new QueryOptions();
        queryOptions4.add(QueryOptions.INCLUDE, "annotation.traitAssociation.id");
        QueryResult<Variant> queryResult4 = clinicalDBAdaptor.get(query4, queryOptions4);
        assertEquals(2, queryResult4.getNumTotalResults());
        assertTrue(containsAccession(queryResult4, "COSM4624460"));
        assertTrue(containsAccession(queryResult4, "RCV000171500"));

        Query query5 = new Query();
        query5.put(ClinicalDBAdaptor.QueryParams.CLINICALSIGNIFICANCE.key(), "likely_pathogenic");
        QueryOptions queryOptions5 = new QueryOptions();
        QueryResult queryResult5 = clinicalDBAdaptor.nativeGet(query5, queryOptions5);
        assertEquals(2, queryResult5.getNumTotalResults());

        Query query6 = new Query();
        query6.put(ClinicalDBAdaptor.QueryParams.FEATURE.key(), "APOE");
        QueryOptions queryOptions6 = new QueryOptions();
        queryOptions6.put(QueryOptions.SORT, "chromosome,start");
        queryOptions6.put(QueryOptions.INCLUDE, "chromosome,start,annotation.consequenceTypes.geneName,annotation.traitAssociation.genomicFeatures.xrefs.symbol,annotation.consequenceTypes,annotation.traitAssociation.id");
        QueryResult queryResult6 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        // Check sorted output
        int previousStart = -1;
        for (Document document : (List<Document>) queryResult6.getResult()) {
            assertTrue(previousStart < document.getInteger("start"));
        }

        queryOptions6.remove(QueryOptions.SORT);
        query6.put(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "clinvar");
        QueryResult queryResult7 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(1, queryResult7.getNumTotalResults());

        query6.put(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "cosmic");
        QueryResult<Variant> queryResult8 = clinicalDBAdaptor.get(query6, queryOptions6);
        assertEquals(1, queryResult8.getNumTotalResults());
        List<String> geneSymbols = queryResult8.getResult().get(0).getAnnotation().getTraitAssociation().stream()
                .map((evidenceEntry) -> evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"))
                .collect(Collectors.toList());
        geneSymbols.addAll(queryResult8.getResult().get(0).getAnnotation().getConsequenceTypes().stream()
                .map((consequenceType) -> consequenceType.getGeneName())
                .collect(Collectors.toList()));
        assertThat(geneSymbols, CoreMatchers.hasItem("APOE"));

        Query query7 = new Query();
        query7.put(ClinicalDBAdaptor.QueryParams.ACCESSION.key(),"COSM306824");
        query7.put(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "cosmic");
        QueryOptions options = new QueryOptions();
        QueryResult<Variant> queryResult9 = clinicalDBAdaptor.get(query7, options);
        assertNotNull("Should return the queryResult of id=COSM306824", queryResult9.getResult());
        assertThat(queryResult9.getResult().get(0).getAnnotation().getTraitAssociation().stream()
                        .map((evidenceEntry) -> evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"))
                        .collect(Collectors.toList()),
                CoreMatchers.hasItem("FMN2"));

    }

    private boolean containsAccession(QueryResult<Variant> queryResult1, String accession) {
        boolean found = false;
        int i = 0;
        while (i < queryResult1.getNumResults() && !found) {
            int j = 0;
            while (j < queryResult1.getResult().get(i).getAnnotation().getTraitAssociation().size() && !found) {
                found = queryResult1
                        .getResult()
                        .get(i)
                        .getAnnotation()
                        .getTraitAssociation()
                        .get(j)
                        .getId()
                        .equals(accession);
                j++;
            }
            i++;
        }
        return found;
    }

}