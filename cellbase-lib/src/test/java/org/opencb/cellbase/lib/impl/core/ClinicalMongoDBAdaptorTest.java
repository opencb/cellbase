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

package org.opencb.cellbase.lib.impl.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.mortbay.util.ajax.JSON;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.SampleEntry;
import org.opencb.cellbase.core.api.core.ClinicalDBAdaptor;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        ClinicalMongoDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens",
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
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));

        SampleEntry sampleEntry = new SampleEntry();
        sampleEntry.setData(Arrays.asList("100653362", "1"));
        variantBuilder.setSamples(Collections.singletonList(sampleEntry));

        Variant variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653363", "0|1"))));
        Variant variant1 = variantBuilder.build();

        List<CellBaseDataResult<Variant>> variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        CellBaseDataResult<Variant> variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Two variants being queried with PS and genotype. Second is hom reference. Both have the same PS (100653362).
        // In the database, these variants form an MNV. None should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "1"))));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "0|0"))));
        variant1 = variantBuilder.build();

         variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = variantCellBaseDataResultList.get(0);
        assertEquals(0, variantCellBaseDataResult.getNumResults());
        assertEquals(0, variantCellBaseDataResult.getNumMatches());
        assertTrue(variantCellBaseDataResult.getResults().isEmpty());

        variantCellBaseDataResult = variantCellBaseDataResultList.get(1);
        assertNotNull(variantCellBaseDataResult);
        assertEquals(0, variantCellBaseDataResult.getNumResults());
        assertEquals(0, variantCellBaseDataResult.getNumMatches());
        assertTrue(variantCellBaseDataResult.getResults().isEmpty());

        // Two X variants being queried with PS and genotype. First is haploid. Second is diploid (heterozygous) AND
        // uses the phased genotype i.e. '|'. Both have the same PS (100653362). In the database, these variants form
        // an MNV. Both of them should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "1"))));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "0|1"))));
        variant1 = variantBuilder.build();

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Two X variants being queried with PS and genotype. First is haploid. Second is diploid (heterozygous) AND
        // uses the phased genotype i.e. '|'. Both have the same PS (100653362). Same as the one above but in this case
        // the '1' of the diploid is placed on the other side of the '|'. In the database, these variants form
        // an MNV. Both of them should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "1"))));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "1|0"))));
        variant1 = variantBuilder.build();

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));


        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Two X variants being queried with PS and genotype. First is haploid. Second is diploid (heterozygous) AND
        // uses the non phased genotype - not sure if this is allowed in VCF - i.e. '/'. Both
        // have the same PS (100653362). In the database, these variants form an MNV. Both of them should be returned.
        variantBuilder = new VariantBuilder("X",
                100653362,
                100653362,
                "C",
                "T");
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "1"))));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("X",
                100653363,
                100653363,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("100653362", "1/0"))));
        variant1 = variantBuilder.build();

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));


        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653362,
                "C",
                "T"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("X",
                100653363,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(2, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Two variants being queried; one with PS and PHASED genotype, the other with PS but UN-PHASED genotype:
        // in the database, these two variant form an MNV. Comparison of a phased genotype and un-phased genotype
        // (as long as the alternate allele is present in both) considers that alternate allele could potentially be in
        // the same copy. Therefore both variants should be returned with all their EvidenceEntries
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("115256528", "0|1"))));
        variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("1",
                115256529,
                115256529,
                "T",
                "A");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("115256528", "1/0"))));
        variant1 = variantBuilder.build();

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1",
                115256528,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(1, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1",
                115256529,
                "T",
                "A"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(1, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Just one variant being queried with PS and genotype: in the database, this variant forms an MNV with
        // another. Since just one of the two is being queried (input list) no results should be returned
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("115256528", "0|1"))));
        variant = variantBuilder.build();

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(variant),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = variantCellBaseDataResultList.get(0);
        assertEquals(0, variantCellBaseDataResult.getNumResults());
        assertEquals(0, variantCellBaseDataResult.getNumMatches());
        assertTrue(variantCellBaseDataResult.getResults().isEmpty());


        // Two variants being queried; one with PS but NOT genotype, the other with missing phase data: in the database,
        // these two variant forms an MNV. Both of them should be returned with all their EvidenceEntries
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("115256528"))));
        variant = variantBuilder.build();
        variant1 = new Variant("1", 115256529, "T", "A");

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(variant, variant1),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1",
                115256528,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(1, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1",
                115256529,
                "T",
                "A"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(1, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Just one variant being queried with PS but NOT genotype: in the database, this variant forms an MNV with
        // another. Since just one of the two is being queried (input list) no results should be returned
        variantBuilder = new VariantBuilder("1",
                115256528,
                115256528,
                "T",
                "C");
        variantBuilder.setSampleDataKeys("PS");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("115256528"))));
        variant = variantBuilder.build();

        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(variant),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = variantCellBaseDataResultList.get(0);
        assertEquals(0, variantCellBaseDataResult.getNumResults());
        assertEquals(0, variantCellBaseDataResult.getNumMatches());
        assertTrue(variantCellBaseDataResult.getResults().isEmpty());

        // Classic, simple query; one variant queried with missing phase data: in the database, same variant is stored,
        // also without phase data for any of its three EvidenceEntries. That variant with its three EvidenceEntries
        // should be returned
        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(new Variant("14", 55369176, "G", "A")),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("14",
                55369176,
                "G",
                "A"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(3, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Two variants with missing phase data (therefore potentially forming an MNV) being queried: in the database,
        // these two variants also form an MNV. Results should be returned for both
        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Arrays.asList(new Variant("1", 115256528, "T", "C"),
                        new Variant("1", 115256529, "T", "A")),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1",
                115256528,
                "T",
                "C"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(1, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1",
                115256529,
                "T",
                "A"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(1, variantCellBaseDataResult.getResults().get(0).getAnnotation().getTraitAssociation().size());

        // Just one variant being queried: in the database, this variant forms an MNV with another. Since just one of
        // the two is being queried (input list) no results should be returned
        variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
                Collections.singletonList(new Variant("1", 115256528, "T", "C")),
                new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(1, variantCellBaseDataResultList.size());
        variantCellBaseDataResult = variantCellBaseDataResultList.get(0);
        assertEquals(0, variantCellBaseDataResult.getNumResults());
        assertEquals(0, variantCellBaseDataResult.getNumMatches());
        assertTrue(variantCellBaseDataResult.getResults().isEmpty());

        // Just one variant being queried: in the database, this variant is duplicated. An exception must be raised
        // since variants are not expected to be repeated in the database <--- This is no longer the expected behavior,
        // as it is not controlled taht in the variation collection there is strictly one document per variant object
        // now: the phased query manager is arbitrarily selecting the first one and logging a warning message
//        try {
//            variantCellBaseDataResultList = clinicalDBAdaptor.getByVariant(
//                    Collections.singletonList(new Variant("1", 1, "T", "A")),
//                    new QueryOptions(ClinicalDBAdaptor.QueryParams.PHASE.key(), true));
//            assert false;
//        } catch (RuntimeException runTimeException) {
//            assertEquals("Unexpected: more than one result found in the clinical variant "
//                    + "collection for variant 1:1:T:A. Please, check.", runTimeException.getMessage());
//        }

    }

    @Test
    public void proteinChangeMatchTest() throws Exception {

        // Load test data
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/clinicalMongoDBAdaptor/nativeGet/clinical_variants.full.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants");

        ClinicalMongoDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");

//        List<CellBaseDataResult<Variant>> queryResultList = clinicalDBAdaptor.getByVariant(
//                Collections.singletonList(new Variant("2:170361068:G:C")),
//                loadGeneList(),
//                new QueryOptions(ClinicalDBAdaptor.QueryParams.CHECK_AMINO_ACID_CHANGE.key(), true));

        List<CellBaseDataResult<Variant>> queryResultList = new ArrayList<>();

        assertEquals(1, queryResultList.size());
        CellBaseDataResult<Variant> queryResult = queryResultList.get(0);
        assertEquals(1, queryResult.getNumResults());
        assertTrue(containsAccession(queryResult, "COSM4624460"));

    }

    private List<Gene> loadGeneList() throws URISyntaxException, IOException {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Path path = Paths.get(getClass()
                .getResource("/clinicalMongoDBAdaptor/gene_list.json.gz").toURI());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile()))));

        List<Gene> geneList = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            Gene gene = jsonObjectMapper.convertValue(JSON.parse(line), Gene.class);
            geneList.add(gene);
        }

        return geneList;
    }

    @Test
    public void nativeGet() throws Exception {

        // Load test data
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/clinicalMongoDBAdaptor/nativeGet/clinical_variants.full.test.json.gz").toURI());
        loadRunner.load(path, "clinical_variants");

        ClinicalMongoDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions1 = new QueryOptions();

        Query query1 = new Query();
        query1.put(ClinicalDBAdaptor.QueryParams.TRAIT.key(), "alzheimer");
        queryOptions1.add(QueryOptions.INCLUDE, "annotation.traitAssociation.id");
        CellBaseDataResult<Variant> CellBaseDataResult1 = clinicalDBAdaptor.get(query1, queryOptions1);
        assertEquals(1, CellBaseDataResult1.getNumResults());
        assertTrue(containsAccession(CellBaseDataResult1, "RCV000172777"));

        Query query2 = new Query();
        query2.put(ClinicalDBAdaptor.QueryParams.TRAIT.key(), "myelofibrosis");
        QueryOptions queryOptions2 = new QueryOptions();
        queryOptions2.add(QueryOptions.INCLUDE, "annotation.traitAssociation.id");
        CellBaseDataResult CellBaseDataResult2 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        assertEquals(1, CellBaseDataResult2.getNumResults());

        Query query4 = new Query();
        query4.put(ClinicalDBAdaptor.QueryParams.REGION.key(),
                new Region("2", 170360030, 170362030));
        QueryOptions queryOptions4 = new QueryOptions();
        queryOptions4.add(QueryOptions.INCLUDE, "annotation.traitAssociation.id");
        CellBaseDataResult<Variant> CellBaseDataResult4 = clinicalDBAdaptor.get(query4, queryOptions4);
        assertEquals(2, CellBaseDataResult4.getNumTotalResults());
        assertTrue(containsAccession(CellBaseDataResult4, "COSM4624460"));
        assertTrue(containsAccession(CellBaseDataResult4, "RCV000171500"));

        Query query5 = new Query();
        query5.put(ClinicalDBAdaptor.QueryParams.CLINICALSIGNIFICANCE.key(), "likely_pathogenic");
        QueryOptions queryOptions5 = new QueryOptions();
        CellBaseDataResult CellBaseDataResult5 = clinicalDBAdaptor.nativeGet(query5, queryOptions5);
        assertEquals(2, CellBaseDataResult5.getNumTotalResults());

        Query query6 = new Query();
        query6.put(ClinicalDBAdaptor.QueryParams.FEATURE.key(), "APOE");
        QueryOptions queryOptions6 = new QueryOptions();
        queryOptions6.put(QueryOptions.SORT, "chromosome,start");
        queryOptions6.put(QueryOptions.INCLUDE, "chromosome,start,annotation.consequenceTypes.geneName,annotation.traitAssociation.genomicFeatures.xrefs.symbol,annotation.consequenceTypes,annotation.traitAssociation.id");
        CellBaseDataResult CellBaseDataResult6 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        // Check sorted output
        int previousStart = -1;
        for (Document document : (List<Document>) CellBaseDataResult6.getResults()) {
            assertTrue(previousStart < document.getInteger("start"));
        }

        queryOptions6.remove(QueryOptions.SORT);
        query6.put(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "clinvar");
        CellBaseDataResult CellBaseDataResult7 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(1, CellBaseDataResult7.getNumTotalResults());

        query6.put(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "cosmic");
        CellBaseDataResult<Variant> CellBaseDataResult8 = clinicalDBAdaptor.get(query6, queryOptions6);
        assertEquals(1, CellBaseDataResult8.getNumTotalResults());
        List<String> geneSymbols = CellBaseDataResult8.getResults().get(0).getAnnotation().getTraitAssociation().stream()
                .map((evidenceEntry) -> evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"))
                .collect(Collectors.toList());
        geneSymbols.addAll(CellBaseDataResult8.getResults().get(0).getAnnotation().getConsequenceTypes().stream()
                .map((consequenceType) -> consequenceType.getGeneName())
                .collect(Collectors.toList()));
        assertThat(geneSymbols, CoreMatchers.hasItem("APOE"));

        Query query7 = new Query();
        query7.put(ClinicalDBAdaptor.QueryParams.ACCESSION.key(),"COSM306824");
        query7.put(ClinicalDBAdaptor.QueryParams.SOURCE.key(), "cosmic");
        QueryOptions options = new QueryOptions();
        CellBaseDataResult<Variant> CellBaseDataResult9 = clinicalDBAdaptor.get(query7, options);
        assertNotNull("Should return the CellBaseDataResult of id=COSM306824", CellBaseDataResult9.getResults());
        assertThat(CellBaseDataResult9.getResults().get(0).getAnnotation().getTraitAssociation().stream()
                        .map((evidenceEntry) -> evidenceEntry.getGenomicFeatures().get(0).getXrefs().get("symbol"))
                        .collect(Collectors.toList()),
                CoreMatchers.hasItem("FMN2"));

    }

    private boolean containsAccession(CellBaseDataResult<Variant> CellBaseDataResult1, String accession) {
        boolean found = false;
        int i = 0;
        while (i < CellBaseDataResult1.getNumResults() && !found) {
            int j = 0;
            while (j < CellBaseDataResult1.getResults().get(i).getAnnotation().getTraitAssociation().size() && !found) {
                found = CellBaseDataResult1
                        .getResults()
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