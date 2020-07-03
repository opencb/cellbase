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

import org.bson.Document;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.models.variant.avro.SampleEntry;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Created by imedina on 12/02/16.
 */
public class VariantMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    public VariantMongoDBAdaptorTest() throws Exception {
        super();
        setUp();
    }

    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/variation_chr22.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variation_chr17.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variation_chr10.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
        path = Paths.get(getClass()
                .getResource("/variation_chr1.full.test.json.gz").toURI());
        loadRunner.load(path, "variation");
    }

    // TODO: to be finished - properly implemented
    @Disabled
    @Test
    public void testGetFunctionalScoreVariant() throws Exception {
        VariantMongoDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        CellBaseDataResult functionalScoreVariant = variationDBAdaptor.getFunctionalScoreVariant(Variant.parseVariant("10:130862563:A:G"),
                new QueryOptions());
    }

    @Test
    public void getPhasedPopulationFrequencyByVariant() {
        VariantBuilder variantBuilder = new VariantBuilder("1",
                62165739,
                62165739,
                "A",
                "T");
        variantBuilder.setSampleDataKeys(Arrays.asList("PS", "GT"));
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("62165739", "0|1"))));
        Variant variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("1",
                62165740,
                62165740,
                "T",
                "G");
        variantBuilder.setSampleDataKeys("PS", "GT");
        variantBuilder.setSamples(Collections.singletonList(new SampleEntry(null, null, Arrays.asList("62165739", "0|1"))));
        Variant variant1 = variantBuilder.build();

        VariantMongoDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens",
                "GRCh37");
        List<CellBaseDataResult<Variant>> variantCellBaseDataResultList
                = variationDBAdaptor.getPopulationFrequencyByVariant(Arrays.asList(variant, variant1),
                new QueryOptions(VariantDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantCellBaseDataResultList.size());
        CellBaseDataResult<Variant> variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList,
                new Variant("1:62165739:A:T"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(-1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(3, variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies().size());
        List<PopulationFrequency> populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "AT",
                        "1:62165739:A:T,1:62165740:T:G",
                        (float) 0.9849,
                        (float) 0.0151,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "AT",
                        "1:62165739:A:T,1:62165740:T:G",
                        (float) 0.9957,
                        (float) 0.0043,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "AT",
                        "1:62165739:A:T,1:62165740:T:G",
                        (float) 0.999,
                        (float) 0.001,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());

        variantCellBaseDataResult = getByVariant(variantCellBaseDataResultList, new Variant("1:62165740:T:G"));
        assertNotNull(variantCellBaseDataResult);
        assertEquals(1, variantCellBaseDataResult.getNumResults());
        assertEquals(-1, variantCellBaseDataResult.getNumMatches());
        assertEquals(1, variantCellBaseDataResult.getResults().size());
        assertEquals(4, variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies().size());
        populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("GNOMAD_GENOMES",
                        "AMR",
                        "T",
                        "G",
                        (float) 0.98062956,
                        (float) 0.01937046,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AFR",
                        "AT",
                        "1:62165739:A:T,1:62165740:T:G",
                        (float) 0.9849,
                        (float) 0.0151,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "AMR",
                        "AT",
                        "1:62165739:A:T,1:62165740:T:G",
                        (float) 0.9957,
                        (float) 0.0043,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());
        populationFrequencyList
                = getPopulationFrequency(variantCellBaseDataResult.getResults().get(0).getAnnotation().getPopulationFrequencies(),
                new PopulationFrequency("1kG_phase3",
                        "EUR",
                        "AT",
                        "1:62165739:A:T,1:62165740:T:G",
                        (float) 0.999,
                        (float) 0.001,
                        null,
                        null,
                        null));
        assertNotNull(populationFrequencyList);
        assertEquals(1, populationFrequencyList.size());

    }

    private List<PopulationFrequency> getPopulationFrequency(List<PopulationFrequency> populationFrequencyList,
                                                             PopulationFrequency populationFrequency) {
        List<PopulationFrequency> populationFrequencyList1 = new ArrayList<>(1);
        for (PopulationFrequency populationFrequency1 : populationFrequencyList) {
            if (populationFrequency.getStudy().equals(populationFrequency1.getStudy())
                    && populationFrequency.getPopulation().equals(populationFrequency1.getPopulation())
                    && populationFrequency.getRefAllele().equals(populationFrequency1.getRefAllele())
                    && populationFrequency.getAltAllele().equals(populationFrequency1.getAltAllele())
                    && populationFrequency.getAltAlleleFreq().equals(populationFrequency1.getAltAlleleFreq())
                    && populationFrequency.getRefAlleleFreq().equals(populationFrequency1.getRefAlleleFreq())) {
                populationFrequencyList1.add(populationFrequency);
            }
        }

        return populationFrequencyList1;
    }

    @Test
    public void testGet() {
        VariantMongoDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions("include", "id");
//        queryOptions.put("limit", 3);
        CellBaseDataResult<Variant> result = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "CTA-445C9.14"), queryOptions);
        assertEquals(21, result.getNumResults());
        assertThat(result.getResults().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("rs191188630", "rs191113747", "rs191348407", "rs191952842",
                        "rs192035553", "rs192722941", "rs192695313", "rs199730247", "rs199753073", "rs199826190",
                        "rs199934473", "rs200591220", "rs200883222", "rs200830209", "rs200830209", "rs200915243",
                        "rs200994757", "rs200942224", "rs201498625", "rs201498625"));

        CellBaseDataResult<Variant> resultENSEMBLGene = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "ENSG00000261188"), queryOptions);
        assertEquals(result.getResults(), resultENSEMBLGene.getResults());

        // ENSEMBL transcript ids are also allowed for the GENE query parameter - this was done on purpose
        CellBaseDataResult<Variant> resultENSEMBLTranscript = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "ENST00000565764"), queryOptions);
        assertEquals(20, resultENSEMBLTranscript.getNumResults());
        assertThat(resultENSEMBLTranscript.getResults().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("rs191188630", "rs191113747", "rs191348407", "rs191952842", "rs192035553",
                        "rs192722941", "rs192695313", "rs199730247", "rs199753073", "rs199934473", "rs200591220",
                        "rs200883222", "rs200830209", "rs200830209", "rs200915243", "rs200994757", "rs200942224",
                        "rs201498625", "rs201498625", "rs201498625"));

        CellBaseDataResult<Variant> geneCellBaseDataResult = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "CERK"), queryOptions);
        assertThat(geneCellBaseDataResult.getResults().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("rs192195512", "rs193091997", "rs200609865"));

    }

    @Test
    public void testNativeGet() {
        VariantMongoDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        CellBaseDataResult variantCellBaseDataResult = variationDBAdaptor.nativeGet(new Query(VariantDBAdaptor.QueryParams.ID.key(), "rs666"),
                new QueryOptions());
        assertEquals(variantCellBaseDataResult.getNumResults(), 1);
        assertEquals(((Document) variantCellBaseDataResult.getResults().get(0)).get("chromosome"), "17");
        assertEquals(((Document) variantCellBaseDataResult.getResults().get(0)).get("start"), new Integer(64224271));
        assertEquals(((Document) variantCellBaseDataResult.getResults().get(0)).get("reference"), "C");
        assertEquals(((Document) variantCellBaseDataResult.getResults().get(0)).get("alternate"), "T");
    }

    @Test
    public void testGetByVariant() {
        VariantMongoDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        CellBaseDataResult<Variant> variantCellBaseDataResult
                = variationDBAdaptor.getByVariant(Variant.parseVariant("10:118187036:T:C"), new QueryOptions());
        assertEquals(variantCellBaseDataResult.getNumResults(), 1);
        assertEquals(variantCellBaseDataResult.getResults().get(0).getChromosome(), "10");
        assertEquals(variantCellBaseDataResult.getResults().get(0).getStart(), new Integer(118187036));
        assertEquals(variantCellBaseDataResult.getResults().get(0).getReference(), "T");
        assertEquals(variantCellBaseDataResult.getResults().get(0).getAlternate(), "C");
        assertEquals(variantCellBaseDataResult.getResults().get(0).getId(), "rs191078597");

        variantCellBaseDataResult
                = variationDBAdaptor.getByVariant(Variant.parseVariant("22:17438072:G:-"), new QueryOptions());
        assertEquals(variantCellBaseDataResult.getNumResults(), 1);
        assertEquals(variantCellBaseDataResult.getResults().get(0).getChromosome(), "22");
        assertEquals(variantCellBaseDataResult.getResults().get(0).getStart(), new Integer(17438072));
        assertEquals(variantCellBaseDataResult.getResults().get(0).getReference(), "G");
        assertEquals(variantCellBaseDataResult.getResults().get(0).getAlternate(), "");
        assertEquals(variantCellBaseDataResult.getResults().get(0).getId(), "rs76677441");
        assertEquals(VariantType.INDEL, variantCellBaseDataResult.getResults().get(0).getType());

    }
}