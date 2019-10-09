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

package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by imedina on 12/02/16.
 */
public class VariantMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    public VariantMongoDBAdaptorTest() throws IOException {
    }

//    private static DBAdaptorFactory dbAdaptorFactory;

//    @Ignore
//    @Test
//    public VariantMongoDBAdaptorTest() {
//        try {
//            Path inputPath = Paths.get(getClass().getResource("/configuration.test.json").toURI());
//            CellBaseConfiguration cellBaseConfiguration = CellBaseConfiguration.load(new FileInputStream(inputPath.toFile()));
//            dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Before
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
    @Ignore
    @Test
    public void testGetFunctionalScoreVariant() throws Exception {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        QueryResult functionalScoreVariant = variationDBAdaptor.getFunctionalScoreVariant(Variant.parseVariant("10:130862563:A:G"),
                new QueryOptions());
    }

    @Test
    public void getPhasedPopulationFrequencyByVariant() {
        VariantBuilder variantBuilder = new VariantBuilder("1",
                62165739,
                62165739,
                "A",
                "T");
        variantBuilder.setFormat(Arrays.asList("PS", "GT"));
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("62165739", "0|1")));
        Variant variant = variantBuilder.build();

        variantBuilder = new VariantBuilder("1",
                62165740,
                62165740,
                "T",
                "G");
        variantBuilder.setFormat("PS", "GT");
        variantBuilder.setSamplesData(Collections.singletonList(Arrays.asList("62165739", "0|1")));
        Variant variant1 = variantBuilder.build();

        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens",
                "GRCh37");
        List<QueryResult<Variant>> variantQueryResultList
                = variationDBAdaptor.getPopulationFrequencyByVariant(Arrays.asList(variant, variant1),
                new QueryOptions(VariantDBAdaptor.QueryParams.PHASE.key(), true));

        assertEquals(2, variantQueryResultList.size());
        QueryResult<Variant> variantQueryResult = getByVariant(variantQueryResultList,
                new Variant("1:62165739:A:T"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(3, variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies().size());
        List<PopulationFrequency> populationFrequencyList
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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

        variantQueryResult = getByVariant(variantQueryResultList, new Variant("1:62165740:T:G"));
        assertNotNull(variantQueryResult);
        assertEquals(1, variantQueryResult.getNumResults());
        assertEquals(1, variantQueryResult.getNumTotalResults());
        assertEquals(1, variantQueryResult.getResult().size());
        assertEquals(4, variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies().size());
        populationFrequencyList
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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
                = getPopulationFrequency(variantQueryResult.getResult().get(0).getAnnotation().getPopulationFrequencies(),
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
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions("include", "id");
//        queryOptions.put("limit", 3);
        QueryResult<Variant> result = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "CTA-445C9.14"), queryOptions);
        assertEquals(21, result.getNumResults());
        assertThat(result.getResult().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("rs191188630", "rs191113747", "rs191348407", "rs191952842",
                        "rs192035553", "rs192722941", "rs192695313", "rs199730247", "rs199753073", "rs199826190",
                        "rs199934473", "rs200591220", "rs200883222", "rs200830209", "rs200830209", "rs200915243",
                        "rs200994757", "rs200942224", "rs201498625", "rs201498625"));

        QueryResult<Variant> resultENSEMBLGene = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "ENSG00000261188"), queryOptions);
        assertEquals(result.getResult(), resultENSEMBLGene.getResult());

        // ENSEMBL transcript ids are also allowed for the GENE query parameter - this was done on purpose
        QueryResult<Variant> resultENSEMBLTranscript = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "ENST00000565764"), queryOptions);
        assertEquals(20, resultENSEMBLTranscript.getNumResults());
        assertThat(resultENSEMBLTranscript.getResult().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("rs191188630", "rs191113747", "rs191348407", "rs191952842", "rs192035553",
                        "rs192722941", "rs192695313", "rs199730247", "rs199753073", "rs199934473", "rs200591220",
                        "rs200883222", "rs200830209", "rs200830209", "rs200915243", "rs200994757", "rs200942224",
                        "rs201498625", "rs201498625", "rs201498625"));

        QueryResult<Variant> geneQueryResult = variationDBAdaptor
                .get(new Query(VariantDBAdaptor.QueryParams.GENE.key(), "CERK"), queryOptions);
        assertThat(geneQueryResult.getResult().stream().map(variant -> variant.getId()).collect(Collectors.toList()),
                CoreMatchers.hasItems("rs192195512", "rs193091997", "rs200609865"));

    }

    @Test
    public void testNativeGet() {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        QueryResult variantQueryResult = variationDBAdaptor.nativeGet(new Query(VariantDBAdaptor.QueryParams.ID.key(), "rs666"),
                new QueryOptions());
        assertEquals(variantQueryResult.getNumResults(), 1);
        assertEquals(((Document) variantQueryResult.getResult().get(0)).get("chromosome"), "17");
        assertEquals(((Document) variantQueryResult.getResult().get(0)).get("start"), new Integer(64224271));
        assertEquals(((Document) variantQueryResult.getResult().get(0)).get("reference"), "C");
        assertEquals(((Document) variantQueryResult.getResult().get(0)).get("alternate"), "T");
    }

    @Test
    public void testGetByVariant() {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor("hsapiens", "GRCh37");
        QueryResult<Variant> variantQueryResult
                = variationDBAdaptor.getByVariant(Variant.parseVariant("10:118187036:T:C"), new QueryOptions());
        assertEquals(variantQueryResult.getNumResults(), 1);
        assertEquals(variantQueryResult.getResult().get(0).getChromosome(), "10");
        assertEquals(variantQueryResult.getResult().get(0).getStart(), new Integer(118187036));
        assertEquals(variantQueryResult.getResult().get(0).getReference(), "T");
        assertEquals(variantQueryResult.getResult().get(0).getAlternate(), "C");
        assertEquals(variantQueryResult.getResult().get(0).getId(), "rs191078597");

        variantQueryResult
                = variationDBAdaptor.getByVariant(Variant.parseVariant("22:17438072:G:-"), new QueryOptions());
        assertEquals(variantQueryResult.getNumResults(), 1);
        assertEquals(variantQueryResult.getResult().get(0).getChromosome(), "22");
        assertEquals(variantQueryResult.getResult().get(0).getStart(), new Integer(17438072));
        assertEquals(variantQueryResult.getResult().get(0).getReference(), "G");
        assertEquals(variantQueryResult.getResult().get(0).getAlternate(), "");
        assertEquals(variantQueryResult.getResult().get(0).getId(), "rs76677441");
        assertEquals(VariantType.INDEL, variantQueryResult.getResult().get(0).getType());

    }
}