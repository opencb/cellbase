/*
 * Copyright 2015 OpenCB
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.lib.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


public class VariantAnnotationCalculatorTest {

    ObjectMapper jsonObjectMapper;
    VariantAnnotationCalculator variantAnnotationCalculator;

    @Before
    public void setUp() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        MongoDBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        variantAnnotationCalculator = new VariantAnnotationCalculator("hsapiens", "GRCh37", dbAdaptorFactory);
    }

    @Test
    public void testExonAnnotation() throws Exception {
        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "consequenceType");

        /**
         * Non coding positive transcript
         */
        Variant variant = new Variant("22:18512237:-:AGTT");
        QueryResult<ConsequenceType> consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000443243").getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("1/2", -1f))));

        variant = new Variant("22:18512237-18521035:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000443243").getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("1/2", 49.411764705882355f),
                        new ExonOverlap("2/2", 100.0f))));

        variant = new Variant("22:18512237:A:T");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000443243").getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("1/2", 0.5882352941176471f))));

        /**
         * Non coding negative transcript
         */
        variant = new Variant("22:18673994:-:AGTT");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("5/9", -1.0f))));

        variant = new Variant("22:18673994-18682094:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("5/9", 33.333333333333336f),
                        new ExonOverlap("4/9", 100.0f),
                        new ExonOverlap("3/9", 100.0f))));

        variant = new Variant("22:18673994:A:T");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("5/9", 1.5151515151515151f))));

        /**
         * Coding positive transcript
         */
        variant = new Variant("22:18732054:-:AGTT");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("5/9", -1.0f))));

        variant = new Variant("22:18732054-18736388:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("5/9", 53.03030303030303f),
                        new ExonOverlap("6/9", 100.0f),
                        new ExonOverlap("7/9", 100.0f))));

        variant = new Variant("22:18732054:T:A");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("5/9", 1.5151515151515151f))));

        /**
         * Coding negative transcript
         */
        variant = new Variant("22:17288712:-:AGTT");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("2/4", -1.0f))));

        variant = new Variant("22:17280612-17288712:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("2/4", 24.347826086956523f),
                        new ExonOverlap("3/4", 100.0f))));

        variant = new Variant("22:17288712:G:A");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(new HashSet(consequenceTypeResult.getResult().get(0).getExonOverlap()),
                new HashSet<>(Arrays.asList(new ExonOverlap("2/4", 0.2898550724637681f))));



    }

    @Test
    public void testSVsCNVsConsequenceTypeAnnotation() throws Exception {
        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "consequenceType");

        /**
         * BND
         */
        Variant variant = new Variant("1", 16877367, "", "[chr4:17481913[T");
        QueryResult<ConsequenceType> consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(4, consequenceTypeResult.getNumResults());
        assertEquals(2, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000438396").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000438396").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                        "non_coding_transcript_exon_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant")));
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000577341").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000577341").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0002083",
                        "2KB_downstream_variant")));
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000513615").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000513615").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001627",
                        "intron_variant")));

        /**
         * Whole affected non coding transcript
         */
        variant = new Variant("22:16328960-16344095:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001537",
                        "structural_variant")));

        variant = new Variant("22:16328960-16344095:<DEL>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001893",
                        "transcript_ablation")));

        variant = new Variant("22:16328960-16344095:<DUP>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001889",
                        "transcript_amplification")));

        variant = new Variant("22:16328960-16344095:<INV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001537",
                        "structural_variant")));

        /**
         * Partially affected non coding transcript
         */
        variant = new Variant("22:16328960-16339130:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(2, consequenceTypeResult.getNumResults());
        assertEquals(3, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                        "non_coding_transcript_exon_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        variant = new Variant("22:16328960-16339130:<DEL>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(2, consequenceTypeResult.getNumResults());
        assertEquals(4, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                        "non_coding_transcript_exon_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant"),
                        new SequenceOntologyTerm("SO:0001906",
                                "feature_truncation"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        variant = new Variant("22:16328960-16339130:<DUP>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(2, consequenceTypeResult.getNumResults());
        assertEquals(3, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                                "non_coding_transcript_exon_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        variant = new Variant("22:16328960-16339130:<INV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(2, consequenceTypeResult.getNumResults());
        assertEquals(3, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000435410").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                                "non_coding_transcript_exon_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        /**
         * Whole affected coding transcript
         */
        variant = new Variant("22:17254012-17306871:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(5, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001537",
                        "structural_variant")));

        variant = new Variant("22:17254012-17306871:<DEL>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(5, consequenceTypeResult.getNumResults());
        assertEquals(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size(), 1);
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001893",
                        "transcript_ablation")));

        variant = new Variant("22:17254012-17306871:<DUP>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(5, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001889",
                        "transcript_amplification")));

        variant = new Variant("22:17254012-17306871:<INV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(5, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001537",
                        "structural_variant")));

        /**
         * Partially affected coding transcript
         */
        variant = new Variant("22:17254012-17281248:<CNV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(4, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001590",
                                "terminator_codon_variant"),
                        new SequenceOntologyTerm("SO:0001580",
                                "coding_sequence_variant"),
                        new SequenceOntologyTerm("SO:0001624",
                                "3_prime_UTR_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        variant = new Variant("22:17254012-17281248:<DEL>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(5, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001578",
                                "stop_lost"),
                        new SequenceOntologyTerm("SO:0001580",
                                "coding_sequence_variant"),
                        new SequenceOntologyTerm("SO:0001624",
                                "3_prime_UTR_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant"),
                        new SequenceOntologyTerm("SO:0001906",
                                "feature_truncation")));

        variant = new Variant("22:17254012-17281248:<DUP>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(4, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001590",
                                "terminator_codon_variant"),
                        new SequenceOntologyTerm("SO:0001580",
                                "coding_sequence_variant"),
                        new SequenceOntologyTerm("SO:0001624",
                                "3_prime_UTR_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        variant = new Variant("22:17254012-17281248:<INV>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(4, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001590",
                                "terminator_codon_variant"),
                        new SequenceOntologyTerm("SO:0001580",
                                "coding_sequence_variant"),
                        new SequenceOntologyTerm("SO:0001624",
                                "3_prime_UTR_variant"),
                        new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

        /**
         * Insertions affecting non coding transcript
         */
        variant = new Variant("22:17308607:<INS>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(2, consequenceTypeResult.getNumResults());
        assertEquals(2, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000423928").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000423928").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                                "non_coding_transcript_exon_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant")));

        variant = new Variant("22:17309113:<INS>");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(2, consequenceTypeResult.getNumResults());
        assertEquals(2, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000423928").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000423928").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001627",
                                "intron_variant"),
                        new SequenceOntologyTerm("SO:0001619",
                                "non_coding_transcript_variant")));

        /**
         * Insertions affecting coding transcript
         */
        variant = new Variant("22:17264399:<INS>");
        variant.getSv().setLeftSvInsSeq("AGAACCTTAATACCCTAGTCTCGATGGTCTTTACATTTTGGCATGATTTTGCAGCGGCTGGTACCGG");
        variant.getSv().setRightSvInsSeq("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(1, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001624",
                                "3_prime_UTR_variant")));

        variant = new Variant("22:17265044:<INS>");
        variant.getSv().setLeftSvInsSeq("AGAACCTTAATACCCTAGTCTCGATGGTCTTTACATTTTGGCATGATTTTGCAGCGGCTGGTACCGG");
        variant.getSv().setRightSvInsSeq("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(3, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001580",
                                "coding_sequence_variant")));

        variant = new Variant("22:17266127:<INS>");
        variant.getSv().setLeftSvInsSeq("AGAACCTTAATACCCTAGTCTCGATGGTCTTTACATTTTGGCATGATTTTGCAGCGGCTGGTACCGG");
        variant.getSv().setRightSvInsSeq("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(1, consequenceTypeResult.getNumResults());
        assertEquals(1, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000331428").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001627",
                                "intron_variant")));

    }

    @Test
    public void testImpreciseConsequenceTypeAnnotation() throws Exception {
        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "consequenceType");

        Variant variant = new Variant("1:33322-35865:<CN4>");
        queryOptions.put("cnvExtraPadding", 500);
        StructuralVariation structuralVariation = new StructuralVariation(33322, 33322,
                35865, 35865, 4, null, null, null);
        variant.setSv(structuralVariation);
        QueryResult<ConsequenceType> consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(11, consequenceTypeResult.getNumResults());
        assertThat(consequenceTypeResult.getResult(),
                CoreMatchers.hasItems(new ConsequenceType("FAM138A", "ENSG00000237613",
                        "ENST00000417324", "-", "lincRNA", null,
                        Arrays.asList("basic"), null, null, null,
                        null, Arrays.asList(new SequenceOntologyTerm("SO:0001889",
                        "transcript_amplification"))),
                        new ConsequenceType("FAM138A", "ENSG00000237613",
                                "ENST00000461467", "-", "lincRNA", null,
                                null, null, null, null,
                                null, Arrays.asList(new SequenceOntologyTerm("SO:0001889",
                                "transcript_amplification")))));

        variant = new Variant("1:33322-35865:<CN4>");
        queryOptions.remove("cnvExtraPadding");
        structuralVariation = new StructuralVariation(33322, 33322,
                35835, 35965, 4, null, null, null);
        variant.setSv(structuralVariation);
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(11, consequenceTypeResult.getNumResults());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000417324").getSequenceOntologyTerms(),
                CoreMatchers.not(CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001889",
                        "transcript_amplification"))));
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000461467").getSequenceOntologyTerms(),
                CoreMatchers.not(CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001889",
                        "transcript_amplification"))));
        assertEquals(3, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000417324").getSequenceOntologyTerms().size());
        assertEquals(3, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000461467").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000417324").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                        "non_coding_transcript_exon_variant")));
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000461467").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001792",
                        "non_coding_transcript_exon_variant")));


        variant = new Variant("1:33322-35865:<DEL>");
        structuralVariation = new StructuralVariation(33322, 33322,
                36035, 36136, 0, null, null, null);
        variant.setSv(structuralVariation);
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(11, consequenceTypeResult.getNumResults());
        assertThat(consequenceTypeResult.getResult(),
                CoreMatchers.hasItems(new ConsequenceType("FAM138A", "ENSG00000237613",
                        "ENST00000417324", "-", "lincRNA", null,
                        Arrays.asList("basic"), null, null, null,
                        null, Arrays.asList(new SequenceOntologyTerm("SO:0001893",
                        "transcript_ablation"))),
                        new ConsequenceType("FAM138A", "ENSG00000237613",
                                "ENST00000461467", "-", "lincRNA", null,
                                null, null, null, null,
                                null, Arrays.asList(new SequenceOntologyTerm("SO:0001893",
                                "transcript_ablation")))));

        queryOptions.put("imprecise", false);
        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(variant, queryOptions);
        assertEquals(11, consequenceTypeResult.getNumResults());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000417324").getSequenceOntologyTerms(),
                CoreMatchers.not(CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001893",
                        "transcript_ablation"))));
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000461467").getSequenceOntologyTerms(),
                CoreMatchers.not(CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001893",
                        "transcript_ablation"))));
        assertEquals(4, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000417324").getSequenceOntologyTerms().size());
        assertEquals(4, getConsequenceType(consequenceTypeResult.getResult(), "ENST00000461467").getSequenceOntologyTerms().size());
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000417324").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001906",
                        "feature_truncation")));
        assertThat(getConsequenceType(consequenceTypeResult.getResult(), "ENST00000461467").getSequenceOntologyTerms(),
                CoreMatchers.hasItems(new SequenceOntologyTerm("SO:0001906",
                        "feature_truncation")));
    }

    private ConsequenceType getConsequenceType(List<ConsequenceType> consequenceTypeList, String transcriptId) {
        for (ConsequenceType consequenceType : consequenceTypeList) {
            if (transcriptId.equals(consequenceType.getEnsemblTranscriptId())) {
                return consequenceType;
            }
        }
        return null;
    }

//    @Test
//    public void testGetAnnotationByVariantList() throws Exception {
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getClass().getResource("/variant-annotation-test.json.gz").getFile()))));
//        String[] variantArray = {"2:114210741:TGATGCT:AGATGGC", "1:40768842:C:G", "2:114340663:GCTGGGCATCC:ACTGGGCATCC",
//                "19:45411941:T:C", "1:819287-820859:<CN12>"};
//        String line = reader.readLine();
//        QueryOptions queryOptions = new QueryOptions("normalize", true);
//        queryOptions.put("phased", true);
//        queryOptions.put("useCache", false);
//        int i = 0;
//        while (line !=null ) {
//            assertVariantAnnotationQueryResultEquals(variantAnnotationCalculator
//                            .getAnnotationByVariantList((Variant.parseVariants(variantArray[i])), queryOptions),
//                    jsonObjectMapper.convertValue(JSON.parse(line),
//                            List.class));
//            line = reader.readLine();
//            i++;
//        }



////        http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/2:114340663:GCTGGGCATCCT:ACTGGGCATCCT/full_annotation

//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("22:16275272:C:T")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("22:16050654:A:<CN0>")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("MT:7443:A:G")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotatio<n) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("2:48025849:CGCCAAATAAA:CCACAAATAAA")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("2:48025849:CGCCAAATAAA:CTGTTTATAAA")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("2:114340663:GCTGGGTATT:ACTGGGCATCCT")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:61482087:G:C")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:56370672:A:C")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:102672886:C:A")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:108338984:C:T")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:144854598:C:T")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:69100:G:T")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:14981854:G:A")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:249240621:G:T")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("6:160990451:C:G")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("22:16051722:TA:T")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("1", 948813, "G", "C"))  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("1", 167385325, "A", "-"))  // Should not return null
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("1", 220603289, "-", "GTGT"))  // Should not return null
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("19", 45411941, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 16050612, "C", "G"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("13", 45411941, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("21", 18992155, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("2", 130498751, "A", "G"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("19", 45411941, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 21982892, "C", "T"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 21982892, "C", "G"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("10", 78444456, "G", "T"))  // Should include population frequencies
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 22022872, "T", "C"))  // Should not raise java.lang.NullPointerException
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 16123409, "-", "A"))
//                , new QueryOptions());

//        VepFormatWriter vepFormatWriter = new VepFormatWriter("/tmp/test.vep");
//        vepFormatWriter.open();
//        vepFormatWriter.pre();
//        vepFormatWriter.write(variantAnnotationList);
//        vepFormatWriter.post();
//        vepFormatWriter.close();



//    }

    @Test
    public void testPopulationFrequencies() throws Exception {

        QueryOptions queryOptions = new QueryOptions("normalize", true);
        queryOptions.put("useCache", false);
        queryOptions.put("include", "populationFrequencies");

        // BE ADVISED: rs is on mitochondrial genome have significantly changed since e82 - same rs assigned different
        // genomic positions, e.g. rs28358574, rs193303007 are assigned 1736 in e82 while appear at 1738 in e89 GRCh37
        // web. I've checked source files and everything is correct in CellBase - source e82 files assign these rs
        // to position 1736. This is not unusual for MT variants, can be easily checked using HGVA.
        // Also, ENSEMBL does not provide 1kG freqs for MT variants, just HAPMAP ones. I've manually checked that
        // approximately match 1kG freqs CellBase is returning and that's why it just tests some of the populations
        // below
        QueryResult<VariantAnnotation> queryResult =
                variantAnnotationCalculator.getAnnotationByVariant(new Variant("MT", 1438,
                                "A", "G"), queryOptions);
        assertThat(queryResult.getResult().get(0).getPopulationFrequencies(),
                CoreMatchers.hasItems(new PopulationFrequency("1kG_phase3_chrMT", "ASW", "A",
                        "G", 0.16666667f, 0.8333333f, 0.16666667f,
                        0f, 0.8333333f),
                        new PopulationFrequency("1kG_phase3_chrMT", "CHB", "A", "G",
                                0.067961164f, 0.93203884f, 0.067961164f,
                                0f, 0.93203884f),
                        new PopulationFrequency("1kG_phase3_chrMT", "GIH", "A", "G",
                                0.018867925f, 0.9811321f, 0.018867925f,
                                0f, 0.9811321f),
                        new PopulationFrequency("1kG_phase3_chrMT", "LWK", "A", "G",
                                0.03960396f, 0.96039605f, 0.03960396f,
                                0f, 0.96039605f),
                        new PopulationFrequency("1kG_phase3_chrMT", "MXL", "A", "G",
                                0.014925373f, 0.98507464f, 0.014925373f,
                                0f, 0.98507464f),
                        new PopulationFrequency("1kG_phase3_chrMT", "TSI", "A", "G",
                                0.018518519f, 0.9814815f, 0.018518519f,
                                0f, 0.9814815f),
                        new PopulationFrequency("1kG_phase3_chrMT", "CEU", "A", "G",
                                0.030303031f, 0.969697f, 0.030303031f,
                                0f, 0.969697f),
                        new PopulationFrequency("1kG_phase3_chrMT", "JPT", "A", "G",
                                0.01923077f, 0.9807692f, 0.01923077f,
                                0f, 0.9807692f),
                        new PopulationFrequency("1kG_phase3_chrMT", "YRI", "A", "G",
                                0.14814815f, 0.8518519f, 0.14814815f,
                                0f, 0.8518519f)));

        queryResult =
                variantAnnotationCalculator.getAnnotationByVariant(new Variant("1", 55505283,
                                "GGAGGAGTGA", "G"), queryOptions);
        assertObjectListEquals("[{\"study\":\"GNOMAD_GENOMES\",\"population\":\"ALL\",\"refAllele\":\"GAGGAGTGA\",\"altAllele\":\"\",\"refAlleleFreq\":0.9999677,\"altAlleleFreq\":0.00003229974,\"refHomGenotypeFreq\":0.9999354,\"hetGenotypeFreq\":0.00006459948,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"NFE\",\"refAllele\":\"GAGGAGTGA\",\"altAllele\":\"\",\"refAlleleFreq\":0.99993336,\"altAlleleFreq\":0.000066657776,\"refHomGenotypeFreq\":0.99986666,\"hetGenotypeFreq\":0.00013331555,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"MALE\",\"refAllele\":\"GAGGAGTGA\",\"altAllele\":\"\",\"refAlleleFreq\":0.9999415,\"altAlleleFreq\":0.00005845902,\"refHomGenotypeFreq\":0.99988306,\"hetGenotypeFreq\":0.00011691804,\"altHomGenotypeFreq\":0}]",
                queryResult.getResult().get(0).getPopulationFrequencies(), PopulationFrequency.class);

        queryResult =
                variantAnnotationCalculator.getAnnotationByVariant(new Variant("1", 55516888,
                                "G", "GA"), queryOptions);
        assertObjectListEquals("[{\"study\":\"GNOMAD_EXOMES\",\"population\":\"ALL\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99901116,\"altAlleleFreq\":0.00092401967,\"refHomGenotypeFreq\":0.9980385,\"hetGenotypeFreq\":0.0019453046,\"altHomGenotypeFreq\":0.000016210872},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"OTH\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9994266,\"altAlleleFreq\":0.00057339447,\"refHomGenotypeFreq\":0.9988532,\"hetGenotypeFreq\":0.0011467889,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"AMR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9992799,\"altAlleleFreq\":0.0007200949,\"refHomGenotypeFreq\":0.99855983,\"hetGenotypeFreq\":0.0014401898,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"NFE\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9999111,\"altAlleleFreq\":0.00008888099,\"refHomGenotypeFreq\":0.99982226,\"hetGenotypeFreq\":0.00017776198,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"AFR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.984375,\"altAlleleFreq\":0.015625,\"refHomGenotypeFreq\":0.9690934,\"hetGenotypeFreq\":0.030563187,\"altHomGenotypeFreq\":0.0003434066},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"MALE\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9993554,\"altAlleleFreq\":0.0005713616,\"refHomGenotypeFreq\":0.9987401,\"hetGenotypeFreq\":0.001230625,\"altHomGenotypeFreq\":0.000029300594},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"FEMALE\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9985848,\"altAlleleFreq\":0.0013607664,\"refHomGenotypeFreq\":0.9971696,\"hetGenotypeFreq\":0.002830394,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"ALL\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99486005,\"altAlleleFreq\":0.0051399753,\"refHomGenotypeFreq\":0.98972005,\"hetGenotypeFreq\":0.010279951,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"OTH\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99796337,\"altAlleleFreq\":0.00203666,\"refHomGenotypeFreq\":0.9959267,\"hetGenotypeFreq\":0.00407332,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"NFE\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99993324,\"altAlleleFreq\":0.000066755674,\"refHomGenotypeFreq\":0.9998665,\"hetGenotypeFreq\":0.00013351135,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"AFR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.98210186,\"altAlleleFreq\":0.017898118,\"refHomGenotypeFreq\":0.9642038,\"hetGenotypeFreq\":0.035796236,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"MALE\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99467963,\"altAlleleFreq\":0.005320393,\"refHomGenotypeFreq\":0.9893592,\"hetGenotypeFreq\":0.010640786,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"FEMALE\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99508315,\"altAlleleFreq\":0.004916847,\"refHomGenotypeFreq\":0.9901663,\"hetGenotypeFreq\":0.009833694,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"ALL\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9998099,\"altAlleleFreq\":0.000095038966,\"refHomGenotypeFreq\":0.99961984,\"hetGenotypeFreq\":0.00038015586,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"AFR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9947917,\"altAlleleFreq\":0.0052083335,\"refHomGenotypeFreq\":0.9895833,\"hetGenotypeFreq\":0.010416667,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"ALL\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99680513,\"altAlleleFreq\":0.0031948881,\"refHomGenotypeFreq\":0.9936102,\"hetGenotypeFreq\":0.0063897762,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"AFR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.98865354,\"altAlleleFreq\":0.0113464445,\"refHomGenotypeFreq\":0.9773071,\"hetGenotypeFreq\":0.022692889,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"YRI\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9907407,\"altAlleleFreq\":0.009259259,\"refHomGenotypeFreq\":0.9814815,\"hetGenotypeFreq\":0.018518519,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"GWD\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99115044,\"altAlleleFreq\":0.0088495575,\"refHomGenotypeFreq\":0.9823009,\"hetGenotypeFreq\":0.017699115,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"ACB\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.984375,\"altAlleleFreq\":0.015625,\"refHomGenotypeFreq\":0.96875,\"hetGenotypeFreq\":0.03125,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"ESN\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9949495,\"altAlleleFreq\":0.005050505,\"refHomGenotypeFreq\":0.989899,\"hetGenotypeFreq\":0.01010101,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"LWK\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.989899,\"altAlleleFreq\":0.01010101,\"refHomGenotypeFreq\":0.97979796,\"hetGenotypeFreq\":0.02020202,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"ASW\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9918033,\"altAlleleFreq\":0.008196721,\"refHomGenotypeFreq\":0.9836066,\"hetGenotypeFreq\":0.016393442,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"AMR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99855906,\"altAlleleFreq\":0.0014409221,\"refHomGenotypeFreq\":0.9971182,\"hetGenotypeFreq\":0.0028818443,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"MSL\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9764706,\"altAlleleFreq\":0.023529412,\"refHomGenotypeFreq\":0.9529412,\"hetGenotypeFreq\":0.047058824,\"altHomGenotypeFreq\":0},{\"study\":\"1kG_phase3\",\"population\":\"PUR\",\"refAllele\":\"\",\"altAllele\":\"A\",\"refAlleleFreq\":0.9951923,\"altAlleleFreq\":0.0048076925,\"refHomGenotypeFreq\":0.99038464,\"hetGenotypeFreq\":0.009615385,\"altHomGenotypeFreq\":0}]",
                queryResult.getResult().get(0).getPopulationFrequencies(), PopulationFrequency.class);

        queryResult =
                variantAnnotationCalculator.getAnnotationByVariant(new Variant("19", 45411941,
                                "T", "C"), queryOptions);
        assertObjectListEquals("[{\"study\":\"GNOMAD_EXOMES\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.86164606,\"altAlleleFreq\":0.13835394,\"refHomGenotypeFreq\":0.7428665,\"hetGenotypeFreq\":0.23755904,\"altHomGenotypeFreq\":0.019574426},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"OTH\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8729389,\"altAlleleFreq\":0.1270611,\"refHomGenotypeFreq\":0.7594568,\"hetGenotypeFreq\":0.22696412,\"altHomGenotypeFreq\":0.013579049},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"EAS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9130086,\"altAlleleFreq\":0.08699144,\"refHomGenotypeFreq\":0.83297646,\"hetGenotypeFreq\":0.16006424,\"altHomGenotypeFreq\":0.006959315},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"AMR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.89662445,\"altAlleleFreq\":0.103375524,\"refHomGenotypeFreq\":0.8039965,\"hetGenotypeFreq\":0.18525594,\"altHomGenotypeFreq\":0.010747552},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"ASJ\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8845232,\"altAlleleFreq\":0.11547676,\"refHomGenotypeFreq\":0.7800671,\"hetGenotypeFreq\":0.20891231,\"altHomGenotypeFreq\":0.011020604},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"FIN\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.79147565,\"altAlleleFreq\":0.20852435,\"refHomGenotypeFreq\":0.6279271,\"hetGenotypeFreq\":0.32709703,\"altHomGenotypeFreq\":0.04497584},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"NFE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.84970874,\"altAlleleFreq\":0.15029128,\"refHomGenotypeFreq\":0.71927917,\"hetGenotypeFreq\":0.26085907,\"altHomGenotypeFreq\":0.019861752},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"AFR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7777113,\"altAlleleFreq\":0.22228873,\"refHomGenotypeFreq\":0.60090977,\"hetGenotypeFreq\":0.35360307,\"altHomGenotypeFreq\":0.04548719},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"MALE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8640703,\"altAlleleFreq\":0.13592973,\"refHomGenotypeFreq\":0.7469009,\"hetGenotypeFreq\":0.23433875,\"altHomGenotypeFreq\":0.018760357},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"FEMALE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8586724,\"altAlleleFreq\":0.14132762,\"refHomGenotypeFreq\":0.7379178,\"hetGenotypeFreq\":0.24150923,\"altHomGenotypeFreq\":0.020573009},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.83512056,\"altAlleleFreq\":0.16487944,\"refHomGenotypeFreq\":0.6976669,\"hetGenotypeFreq\":0.27490738,\"altHomGenotypeFreq\":0.02742575},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"OTH\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.838809,\"altAlleleFreq\":0.16119097,\"refHomGenotypeFreq\":0.69609857,\"hetGenotypeFreq\":0.28542095,\"altHomGenotypeFreq\":0.018480493},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"EAS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.900625,\"altAlleleFreq\":0.099375,\"refHomGenotypeFreq\":0.80875,\"hetGenotypeFreq\":0.18375,\"altHomGenotypeFreq\":0.0075},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"AMR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.875,\"altAlleleFreq\":0.125,\"refHomGenotypeFreq\":0.7644231,\"hetGenotypeFreq\":0.22115384,\"altHomGenotypeFreq\":0.014423077},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"ASJ\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8833333,\"altAlleleFreq\":0.11666667,\"refHomGenotypeFreq\":0.7733333,\"hetGenotypeFreq\":0.22,\"altHomGenotypeFreq\":0.006666667},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"FIN\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.81095815,\"altAlleleFreq\":0.18904188,\"refHomGenotypeFreq\":0.65404475,\"hetGenotypeFreq\":0.31382674,\"altHomGenotypeFreq\":0.032128513},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"NFE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.85714287,\"altAlleleFreq\":0.14285715,\"refHomGenotypeFreq\":0.7344064,\"hetGenotypeFreq\":0.24547283,\"altHomGenotypeFreq\":0.020120725},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"AFR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.788976,\"altAlleleFreq\":0.21102399,\"refHomGenotypeFreq\":0.6226937,\"hetGenotypeFreq\":0.33256456,\"altHomGenotypeFreq\":0.044741698},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"MALE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.83254886,\"altAlleleFreq\":0.16745116,\"refHomGenotypeFreq\":0.69404566,\"hetGenotypeFreq\":0.27700636,\"altHomGenotypeFreq\":0.028947989},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"FEMALE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.83829296,\"altAlleleFreq\":0.16170707,\"refHomGenotypeFreq\":0.70213383,\"hetGenotypeFreq\":0.27231818,\"altHomGenotypeFreq\":0.025547976},{\"study\":\"UK10K_TWINSUK\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.86030203,\"altAlleleFreq\":0.13969795},{\"study\":\"GONL\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8416834,\"altAlleleFreq\":0.15831663},{\"study\":\"UK10K_ALSPAC\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8461339,\"altAlleleFreq\":0.15386611},{\"study\":\"EXAC\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.81566757,\"altAlleleFreq\":0.18433243,\"refHomGenotypeFreq\":0.65346056,\"hetGenotypeFreq\":0.324414,\"altHomGenotypeFreq\":0.022125423},{\"study\":\"EXAC\",\"population\":\"OTH\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8293651,\"altAlleleFreq\":0.17063493,\"refHomGenotypeFreq\":0.6666667,\"hetGenotypeFreq\":0.32539684,\"altHomGenotypeFreq\":0.007936508},{\"study\":\"EXAC\",\"population\":\"SAS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8898131,\"altAlleleFreq\":0.110186875,\"refHomGenotypeFreq\":0.7932999,\"hetGenotypeFreq\":0.19302644,\"altHomGenotypeFreq\":0.013673656},{\"study\":\"EXAC\",\"population\":\"EAS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8496169,\"altAlleleFreq\":0.15038314,\"refHomGenotypeFreq\":0.710728,\"hetGenotypeFreq\":0.2777778,\"altHomGenotypeFreq\":0.011494253},{\"study\":\"EXAC\",\"population\":\"AMR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7854512,\"altAlleleFreq\":0.2145488,\"refHomGenotypeFreq\":0.5782689,\"hetGenotypeFreq\":0.41436464,\"altHomGenotypeFreq\":0.0073664826},{\"study\":\"EXAC\",\"population\":\"FIN\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.6733746,\"altAlleleFreq\":0.32662538,\"refHomGenotypeFreq\":0.4148607,\"hetGenotypeFreq\":0.51702785,\"altHomGenotypeFreq\":0.06811146},{\"study\":\"EXAC\",\"population\":\"NFE\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.79221934,\"altAlleleFreq\":0.20778066,\"refHomGenotypeFreq\":0.60645425,\"hetGenotypeFreq\":0.37153015,\"altHomGenotypeFreq\":0.022015588},{\"study\":\"EXAC\",\"population\":\"AFR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.72676283,\"altAlleleFreq\":0.27323717,\"refHomGenotypeFreq\":0.50641024,\"hetGenotypeFreq\":0.44070512,\"altHomGenotypeFreq\":0.052884616},{\"study\":\"ESP6500\",\"population\":\"AA\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.81068414,\"altAlleleFreq\":0.18931584,\"refHomGenotypeFreq\":0.66166824,\"hetGenotypeFreq\":0.29803187,\"altHomGenotypeFreq\":0.040299907},{\"study\":\"ESP6500\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.85836667,\"altAlleleFreq\":0.1416333,\"refHomGenotypeFreq\":0.74107283,\"hetGenotypeFreq\":0.23458767,\"altHomGenotypeFreq\":0.024339471},{\"study\":\"ESP6500\",\"population\":\"EA\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.88311845,\"altAlleleFreq\":0.116881534,\"refHomGenotypeFreq\":0.7822914,\"hetGenotypeFreq\":0.20165409,\"altHomGenotypeFreq\":0.016054489},{\"study\":\"1kG_phase3\",\"population\":\"MXL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9140625,\"altAlleleFreq\":0.0859375,\"refHomGenotypeFreq\":0.828125,\"hetGenotypeFreq\":0.171875,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"ALL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8494409,\"altAlleleFreq\":0.15055911,\"refHomGenotypeFreq\":0.72723645,\"hetGenotypeFreq\":0.24440894,\"altHomGenotypeFreq\":0.028354632},{\"study\":\"1kG_phase3\",\"population\":\"SAS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.91308796,\"altAlleleFreq\":0.086912066,\"refHomGenotypeFreq\":0.8364008,\"hetGenotypeFreq\":0.15337422,\"altHomGenotypeFreq\":0.010224949},{\"study\":\"1kG_phase3\",\"population\":\"CLM\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.84574467,\"altAlleleFreq\":0.15425532,\"refHomGenotypeFreq\":0.71276593,\"hetGenotypeFreq\":0.26595744,\"altHomGenotypeFreq\":0.021276595},{\"study\":\"1kG_phase3\",\"population\":\"ITU\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9166667,\"altAlleleFreq\":0.083333336,\"refHomGenotypeFreq\":0.8333333,\"hetGenotypeFreq\":0.16666667,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"AFR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7322239,\"altAlleleFreq\":0.2677761,\"refHomGenotypeFreq\":0.5385779,\"hetGenotypeFreq\":0.38729197,\"altHomGenotypeFreq\":0.0741301},{\"study\":\"1kG_phase3\",\"population\":\"CHS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.94285715,\"altAlleleFreq\":0.057142857,\"refHomGenotypeFreq\":0.8857143,\"hetGenotypeFreq\":0.11428572,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"JPT\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9182692,\"altAlleleFreq\":0.08173077,\"refHomGenotypeFreq\":0.8557692,\"hetGenotypeFreq\":0.125,\"altHomGenotypeFreq\":0.01923077},{\"study\":\"1kG_phase3\",\"population\":\"YRI\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7638889,\"altAlleleFreq\":0.2361111,\"refHomGenotypeFreq\":0.5740741,\"hetGenotypeFreq\":0.3796296,\"altHomGenotypeFreq\":0.046296295},{\"study\":\"1kG_phase3\",\"population\":\"PJL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9166667,\"altAlleleFreq\":0.083333336,\"refHomGenotypeFreq\":0.84375,\"hetGenotypeFreq\":0.14583334,\"altHomGenotypeFreq\":0.010416667},{\"study\":\"1kG_phase3\",\"population\":\"GWD\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7256637,\"altAlleleFreq\":0.27433628,\"refHomGenotypeFreq\":0.53097343,\"hetGenotypeFreq\":0.38938054,\"altHomGenotypeFreq\":0.07964602},{\"study\":\"1kG_phase3\",\"population\":\"STU\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.86764705,\"altAlleleFreq\":0.13235295,\"refHomGenotypeFreq\":0.75490195,\"hetGenotypeFreq\":0.22549021,\"altHomGenotypeFreq\":0.019607844},{\"study\":\"1kG_phase3\",\"population\":\"GBR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.82417583,\"altAlleleFreq\":0.17582418,\"refHomGenotypeFreq\":0.6923077,\"hetGenotypeFreq\":0.26373628,\"altHomGenotypeFreq\":0.043956045},{\"study\":\"1kG_phase3\",\"population\":\"CDX\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.89784944,\"altAlleleFreq\":0.10215054,\"refHomGenotypeFreq\":0.79569894,\"hetGenotypeFreq\":0.20430107,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"KHV\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.90909094,\"altAlleleFreq\":0.09090909,\"refHomGenotypeFreq\":0.83838385,\"hetGenotypeFreq\":0.14141414,\"altHomGenotypeFreq\":0.02020202},{\"study\":\"1kG_phase3\",\"population\":\"IBS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8598131,\"altAlleleFreq\":0.14018692,\"refHomGenotypeFreq\":0.72897196,\"hetGenotypeFreq\":0.26168224,\"altHomGenotypeFreq\":0.009345794},{\"study\":\"1kG_phase3\",\"population\":\"BEB\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9127907,\"altAlleleFreq\":0.0872093,\"refHomGenotypeFreq\":0.8372093,\"hetGenotypeFreq\":0.15116279,\"altHomGenotypeFreq\":0.011627907},{\"study\":\"1kG_phase3\",\"population\":\"ACB\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7447917,\"altAlleleFreq\":0.25520834,\"refHomGenotypeFreq\":0.5625,\"hetGenotypeFreq\":0.36458334,\"altHomGenotypeFreq\":0.072916664},{\"study\":\"1kG_phase3\",\"population\":\"ESN\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.75757575,\"altAlleleFreq\":0.24242425,\"refHomGenotypeFreq\":0.57575756,\"hetGenotypeFreq\":0.36363637,\"altHomGenotypeFreq\":0.060606062},{\"study\":\"1kG_phase3\",\"population\":\"LWK\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.6212121,\"altAlleleFreq\":0.37878788,\"refHomGenotypeFreq\":0.3939394,\"hetGenotypeFreq\":0.45454544,\"altHomGenotypeFreq\":0.15151516},{\"study\":\"1kG_phase3\",\"population\":\"EUR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8449304,\"altAlleleFreq\":0.15506959,\"refHomGenotypeFreq\":0.71172965,\"hetGenotypeFreq\":0.2664016,\"altHomGenotypeFreq\":0.021868788},{\"study\":\"1kG_phase3\",\"population\":\"ASW\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.795082,\"altAlleleFreq\":0.20491803,\"refHomGenotypeFreq\":0.6229508,\"hetGenotypeFreq\":0.3442623,\"altHomGenotypeFreq\":0.032786883},{\"study\":\"1kG_phase3\",\"population\":\"AMR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8962536,\"altAlleleFreq\":0.1037464,\"refHomGenotypeFreq\":0.7982709,\"hetGenotypeFreq\":0.19596541,\"altHomGenotypeFreq\":0.0057636886},{\"study\":\"1kG_phase3\",\"population\":\"MSL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.7411765,\"altAlleleFreq\":0.25882354,\"refHomGenotypeFreq\":0.5411765,\"hetGenotypeFreq\":0.4,\"altHomGenotypeFreq\":0.05882353},{\"study\":\"1kG_phase3\",\"population\":\"GIH\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9514563,\"altAlleleFreq\":0.048543688,\"refHomGenotypeFreq\":0.9126214,\"hetGenotypeFreq\":0.0776699,\"altHomGenotypeFreq\":0.009708738},{\"study\":\"1kG_phase3\",\"population\":\"FIN\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.81313133,\"altAlleleFreq\":0.18686868,\"refHomGenotypeFreq\":0.6666667,\"hetGenotypeFreq\":0.2929293,\"altHomGenotypeFreq\":0.04040404},{\"study\":\"1kG_phase3\",\"population\":\"TSI\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.89719623,\"altAlleleFreq\":0.10280374,\"refHomGenotypeFreq\":0.7943925,\"hetGenotypeFreq\":0.20560747,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"PUR\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.8942308,\"altAlleleFreq\":0.10576923,\"refHomGenotypeFreq\":0.78846157,\"hetGenotypeFreq\":0.21153846,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"CEU\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.82323235,\"altAlleleFreq\":0.17676768,\"refHomGenotypeFreq\":0.6666667,\"hetGenotypeFreq\":0.3131313,\"altHomGenotypeFreq\":0.02020202},{\"study\":\"1kG_phase3\",\"population\":\"EAS\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.91369045,\"altAlleleFreq\":0.08630952,\"refHomGenotypeFreq\":0.83531743,\"hetGenotypeFreq\":0.15674603,\"altHomGenotypeFreq\":0.007936508},{\"study\":\"1kG_phase3\",\"population\":\"PEL\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.9411765,\"altAlleleFreq\":0.05882353,\"refHomGenotypeFreq\":0.88235295,\"hetGenotypeFreq\":0.11764707,\"altHomGenotypeFreq\":0.0},{\"study\":\"1kG_phase3\",\"population\":\"CHB\",\"refAllele\":\"T\",\"altAllele\":\"C\",\"refAlleleFreq\":0.89805824,\"altAlleleFreq\":0.10194175,\"refHomGenotypeFreq\":0.79611653,\"hetGenotypeFreq\":0.2038835,\"altHomGenotypeFreq\":0.0}]",
                queryResult.getResult().get(0).getPopulationFrequencies(), PopulationFrequency.class);

        queryResult =
                variantAnnotationCalculator.getAnnotationByVariant(new Variant("2", 114340663,
                                "GCTGGGCATCC", "ACTGGGCATCC"), queryOptions);
        assertObjectListEquals("[{\"study\":\"MGP\",\"population\":\"ALL\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.99250937,\"altAlleleFreq\":0.007490637,\"refHomGenotypeFreq\":0.98501873,\"hetGenotypeFreq\":0.014981274,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"ALL\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.68756205,\"altAlleleFreq\":0.31243795,\"refHomGenotypeFreq\":0.3751358,\"hetGenotypeFreq\":0.62485254,\"altHomGenotypeFreq\":0.000011682107},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"OTH\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.64637905,\"altAlleleFreq\":0.35362095,\"refHomGenotypeFreq\":0.29275808,\"hetGenotypeFreq\":0.7072419,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"EAS\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.6414259,\"altAlleleFreq\":0.3585741,\"refHomGenotypeFreq\":0.28285182,\"hetGenotypeFreq\":0.7171482,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"AMR\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.6448952,\"altAlleleFreq\":0.35510483,\"refHomGenotypeFreq\":0.28979036,\"hetGenotypeFreq\":0.71020967,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"ASJ\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.6128126,\"altAlleleFreq\":0.38718742,\"refHomGenotypeFreq\":0.22562517,\"hetGenotypeFreq\":0.77437484,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"FIN\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.6261249,\"altAlleleFreq\":0.37387505,\"refHomGenotypeFreq\":0.25224987,\"hetGenotypeFreq\":0.7477501,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"NFE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.751171,\"altAlleleFreq\":0.248829,\"refHomGenotypeFreq\":0.502342,\"hetGenotypeFreq\":0.497658,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"AFR\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.65996754,\"altAlleleFreq\":0.3400325,\"refHomGenotypeFreq\":0.31993502,\"hetGenotypeFreq\":0.680065,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"MALE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.69635016,\"altAlleleFreq\":0.30364984,\"refHomGenotypeFreq\":0.39272162,\"hetGenotypeFreq\":0.60725707,\"altHomGenotypeFreq\":0.000021319234},{\"study\":\"GNOMAD_EXOMES\",\"population\":\"FEMALE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.67690915,\"altAlleleFreq\":0.32309085,\"refHomGenotypeFreq\":0.35381833,\"hetGenotypeFreq\":0.6461817,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"ALL\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.50730366,\"altAlleleFreq\":0.49269632,\"refHomGenotypeFreq\":0.014607344,\"hetGenotypeFreq\":0.98539263,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"OTH\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.51265824,\"altAlleleFreq\":0.48734176,\"refHomGenotypeFreq\":0.025316456,\"hetGenotypeFreq\":0.9746835,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"EAS\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.50253487,\"altAlleleFreq\":0.49746513,\"refHomGenotypeFreq\":0.0050697085,\"hetGenotypeFreq\":0.99493027,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"AMR\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.50493824,\"altAlleleFreq\":0.49506173,\"refHomGenotypeFreq\":0.009876544,\"hetGenotypeFreq\":0.99012345,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"ASJ\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.51459855,\"altAlleleFreq\":0.48540145,\"refHomGenotypeFreq\":0.02919708,\"hetGenotypeFreq\":0.9708029,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"FIN\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.5056514,\"altAlleleFreq\":0.49434862,\"refHomGenotypeFreq\":0.011302796,\"hetGenotypeFreq\":0.98869723,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"NFE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.5101583,\"altAlleleFreq\":0.48984167,\"refHomGenotypeFreq\":0.02031666,\"hetGenotypeFreq\":0.97968334,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"AFR\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.5034876,\"altAlleleFreq\":0.49651244,\"refHomGenotypeFreq\":0.0069751223,\"hetGenotypeFreq\":0.9930249,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"MALE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.5076308,\"altAlleleFreq\":0.49236917,\"refHomGenotypeFreq\":0.015261628,\"hetGenotypeFreq\":0.98473835,\"altHomGenotypeFreq\":0},{\"study\":\"GNOMAD_GENOMES\",\"population\":\"FEMALE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.50689864,\"altAlleleFreq\":0.4931014,\"refHomGenotypeFreq\":0.01379724,\"hetGenotypeFreq\":0.9862028,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"ALL\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.7961549,\"altAlleleFreq\":0.20384505,\"refHomGenotypeFreq\":0.5923099,\"hetGenotypeFreq\":0.4076901,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"OTH\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.76941746,\"altAlleleFreq\":0.23058252,\"refHomGenotypeFreq\":0.5388349,\"hetGenotypeFreq\":0.46116504,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"SAS\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.6825376,\"altAlleleFreq\":0.31746238,\"refHomGenotypeFreq\":0.3650752,\"hetGenotypeFreq\":0.63492477,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"EAS\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.78699017,\"altAlleleFreq\":0.21300986,\"refHomGenotypeFreq\":0.5739803,\"hetGenotypeFreq\":0.42601973,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"AMR\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.89922637,\"altAlleleFreq\":0.100773655,\"refHomGenotypeFreq\":0.7984527,\"hetGenotypeFreq\":0.20154731,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"FIN\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.76068676,\"altAlleleFreq\":0.23931324,\"refHomGenotypeFreq\":0.5213735,\"hetGenotypeFreq\":0.4786265,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"NFE\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.82728344,\"altAlleleFreq\":0.17271654,\"refHomGenotypeFreq\":0.6545669,\"hetGenotypeFreq\":0.3454331,\"altHomGenotypeFreq\":0},{\"study\":\"EXAC\",\"population\":\"AFR\",\"refAllele\":\"G\",\"altAllele\":\"A\",\"refAlleleFreq\":0.69621515,\"altAlleleFreq\":0.30378485,\"refHomGenotypeFreq\":0.39243028,\"hetGenotypeFreq\":0.6075697,\"altHomGenotypeFreq\":0}]",
                queryResult.getResult().get(0).getPopulationFrequencies(), PopulationFrequency.class);

    }

    @Test
    public void testHgvsAnnotation() throws Exception {
        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "hgvs");
        QueryResult<VariantAnnotation> queryResult = variantAnnotationCalculator
                .getAnnotationByVariant(new Variant("19:45411941:T:C"), queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(4, queryResult.getResult().get(0).getHgvs().size());
        assertEquals(new HashSet<>(Arrays.asList("ENST00000252486(ENSG00000130203):c.388T>C",
                "ENST00000446996(ENSG00000130203):c.388T>C", "ENST00000434152(ENSG00000130203):c.466T>C",
                "ENST00000425718(ENSG00000130203):c.388T>C")), new HashSet<String>(queryResult.getResult().get(0).getHgvs()));
    }

    @Test
    public void testCytobandAnnotation() throws Exception {

        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "cytoband");
        queryOptions.put("cnvExtraPadding", 500);
        QueryResult<VariantAnnotation> queryResult = variantAnnotationCalculator
                .getAnnotationByVariant(new Variant("19:37800050-37801000:<CN3>"), queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(2, queryResult.getResult().get(0).getCytoband().size());
        assertEquals(queryResult.getResult().get(0).getCytoband().stream().collect(Collectors.toSet()),
                new HashSet<Cytoband>(Arrays.asList(
                        new Cytoband("gpos25", "q13.12", 35100001,37800000),
                        new Cytoband("gneg", "q13.13", 37800001,38200000))));

        List<QueryResult<VariantAnnotation>> queryResultList = variantAnnotationCalculator
                .getAnnotationByVariantList(Arrays.asList(new Variant("19:37800050-42910001:<CN3>"),
                        new Variant("18:63902001:T:A"),
                        new Variant("6:148500101-148500201:<DEL>")), queryOptions);
        assertEquals(3, queryResultList.size());
        assertEquals(1, queryResultList.get(0).getNumTotalResults());
        assertEquals(3, queryResultList.get(0).getResult().get(0).getCytoband().size());
        assertEquals(queryResultList.get(0).getResult().get(0).getCytoband().stream().collect(Collectors.toSet()),
                new HashSet<Cytoband>(Arrays.asList(
                        new Cytoband("gpos25", "q13.12", 35100001,37800000),
                        new Cytoband("gneg", "q13.13", 37800001,38200000),
                        new Cytoband("gneg", "q13.31", 42900001,44700000))));

        assertEquals(1, queryResultList.get(1).getNumTotalResults());
        assertEquals(1, queryResultList.get(1).getResult().get(0).getCytoband().size());
        assertEquals(queryResultList.get(1).getResult().get(0).getCytoband().get(0),
                        new Cytoband("gpos100", "q22.1", 63900001,69100000));

        assertEquals(1, queryResultList.get(2).getNumTotalResults());
        assertEquals(1, queryResultList.get(2).getResult().get(0).getCytoband().size());
        assertEquals(queryResultList.get(2).getResult().get(0).getCytoband().get(0),
                new Cytoband("gneg", "q25.1", 148500001,152100000));

    }

    @Test
    public void testDGVAnnotation() throws Exception {
        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "variation");
        Variant variant = new Variant("1:10161-10291:<DEL>");
        StructuralVariation structuralVariation = new StructuralVariation(10161 - 10, 10161 + 50,
                10291 - 100, 10291 + 10, 0, null, null,
                null);
        variant.setSv(structuralVariation);
        QueryResult<VariantAnnotation> queryResult = variantAnnotationCalculator
                .getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals("nsv958854", queryResult.getResult().get(0).getId());

        variant = new Variant("1:10401-127130:<CN10>");
        structuralVariation = new StructuralVariation(9000, 10401, 127130,
                127630, 10, null, null,
                StructuralVariantType.COPY_NUMBER_GAIN);
        variant.setSv(structuralVariation);
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals("nsv7879", queryResult.getResult().get(0).getId());

        queryOptions.put("imprecise", false);
        variant = new Variant("1:10401-127130:<CN10>");
        structuralVariation = new StructuralVariation(9000, 10401, 127130,
                127630, 10, null, null,
                StructuralVariantType.COPY_NUMBER_GAIN);
        variant.setSv(structuralVariation);
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertNull(queryResult.getResult().get(0).getId());
    }

    @Test
    public void testRepeatAnnotation() throws Exception {
        QueryOptions queryOptions = new QueryOptions("useCache", false);
        queryOptions.put("include", "repeats");

        Variant variant = new Variant("19", 172450, "", "A]2:10000]");
        StructuralVariation structuralVariation = new StructuralVariation(172450 - 10, 172450 + 30,
                10000 - 100, 10000 + 7, null, null, null,
                null);
        variant.setSv(structuralVariation);
        QueryResult<VariantAnnotation> queryResult = variantAnnotationCalculator
                .getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(22, queryResult.getResult().get(0).getRepeat().size());
        assertThat(queryResult.getResult().get(0).getRepeat(),
                CoreMatchers.hasItems(new Repeat("10420", "19", 60001, 172445, null,
                        2f, 0.991464f, null, null, "genomicSuperDup"),
                        new Repeat(null, "2", 10006, 10174, 13,
                                12.7f, 0.82f, 213f, "CCCACACACCACA", "trf")));

        variant = new Variant("19:82354-82444:<CN4>");
        structuralVariation = new StructuralVariation(82354, 82354,
                82444, 82444, 0, null, null, null);
        queryOptions.put("cnvExtraPadding", 150);
        variant.setSv(structuralVariation);
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(6, queryResult.getResult().get(0).getRepeat().size());
        assertThat(queryResult.getResult().get(0).getRepeat(),
                CoreMatchers.hasItems(new Repeat("15610", "19", 60001, 82344, null,
                        2f, 0.997228f, null, null, "genomicSuperDup")));

        queryOptions.remove("cnvExtraPadding");
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(2, queryResult.getResult().get(0).getRepeat().size());
        assertThat(queryResult.getResult().get(0).getRepeat(),
                CoreMatchers.not(CoreMatchers.hasItems(new Repeat("15610", "19", 60001,
                        82344, null, 2f, 0.997228f, null, null,
                        "genomicSuperDup"))));

        variant = new Variant("19:82354-82444:<DEL>");
        structuralVariation = new StructuralVariation(82354, 82354,
                82444, 82444, 0, null, null, null);
        queryOptions.put("svExtraPadding", 150);
        variant.setSv(structuralVariation);
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(6, queryResult.getResult().get(0).getRepeat().size());
        assertThat(queryResult.getResult().get(0).getRepeat(),
                CoreMatchers.hasItems(new Repeat("15610", "19", 60001, 82344, null,
                        2f, 0.997228f, null, null, "genomicSuperDup")));

        queryOptions.remove("svExtraPadding");
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(2, queryResult.getResult().get(0).getRepeat().size());
        assertThat(queryResult.getResult().get(0).getRepeat(),
                CoreMatchers.not(CoreMatchers.hasItems(new Repeat("15610", "19", 60001,
                        82344, null, 2f, 0.997228f, null, null,
                        "genomicSuperDup"))));

        variant = new Variant("1:1822100-1823770:<DEL>");
        structuralVariation = new StructuralVariation(1822100 - 10, 1822100 + 50,
                1823770 - 20, 1823770 + 10, 0, null, null,
                null);
        variant.setSv(structuralVariation);
        queryOptions.put("imprecise", false);
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertNull(queryResult.getResult().get(0).getRepeat());

        variant = new Variant("1:1822100-1823770:<DEL>");
        structuralVariation = new StructuralVariation(1822100 - 10, 1822100 + 50,
                1823770 - 20, 1823770 + 10, 0, null, null,
                null);
        variant.setSv(structuralVariation);
        queryOptions.remove("imprecise");
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(variant, queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(1, queryResult.getResult().get(0).getRepeat().size());
        assertEquals(queryResult.getResult().get(0).getRepeat().stream().collect(Collectors.toSet()),
                new HashSet<Repeat>(Arrays.asList(
                        new Repeat(null, "1", 1822050, 1822099, 13, 3.8f,
                                0.8f, 50f, "CCCTCGACCCCGA", "trf"))));

        queryResult = variantAnnotationCalculator.getAnnotationByVariant(new Variant("1:1822050:T:C"), queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(1, queryResult.getResult().get(0).getRepeat().size());
        assertEquals(queryResult.getResult().get(0).getRepeat().stream().collect(Collectors.toSet()),
                new HashSet<Repeat>(Arrays.asList(
                        new Repeat(null, "1", 1822050, 1822099, 13, 3.8f,
                                0.8f, 50f, "CCCTCGACCCCGA", "trf"))));

        queryOptions.put("imprecise", false);
        queryOptions.put("cnvExtraPadding", 150);
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(new Variant("1:8000-9990:<CN3>"), queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertNull(queryResult.getResult().get(0).getRepeat());

        queryOptions.remove("imprecise");
        queryResult = variantAnnotationCalculator.getAnnotationByVariant(new Variant("1:8000-9990:<CN3>"), queryOptions);
        assertEquals(1, queryResult.getNumTotalResults());
        assertEquals(5, queryResult.getResult().get(0).getRepeat().size());
        assertEquals(queryResult.getResult().get(0).getRepeat().stream().collect(Collectors.toSet()),
                new HashSet<Repeat>(Arrays.asList(
                        new Repeat(null, "1", 10001, 10468, 6, 77.2f,
                                0.95f, 789f, "TAACCC", "trf"),
                        new Repeat("9119", "1", 10001, 87112, null, 2f,
                                0.992904f, null, null, "genomicSuperDup"),
                        new Repeat("6001", "1", 10001, 20818, null, 2f,
                                0.981582f, null, null, "genomicSuperDup"),
                        new Repeat("2698", "1", 10001, 19844, null, 2f,
                                0.982898f, null, null, "genomicSuperDup"),
                        new Repeat("2595", "1", 10001, 19844, null, 2f,
                                0.982898f, null, null, "genomicSuperDup"))));
//        assertEquals(queryResult.getResult().get(0).getRepeat().stream().collect(Collectors.toSet()),
//                new HashSet<Repeat>(Arrays.asList(
//                        new Repeat(null, "1", 1823242, 1823267, 1, Float.valueOf(25),
//                                Float.valueOf(1), Float.valueOf(50), "A", "trf"),
//                        new Repeat(null, "1", 1800743, 1800796, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1800810, 1800854, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1801025, 1801354, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1801634, 1801700, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1801704, 1801750, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1822767, 1822794, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1822871, 1822904, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1822940, 1823278, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1823332, 1823339, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1823351, 1823397, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1823460, 1823512, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1823577, 1823603, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1823664, 1823686, null, null,
//                                null, null, null, "windowMasker"),
//                        new Repeat(null, "1", 1823699, 1823720, null, null,
//                                null, null, null, "windowMasker"))));

    }

    private void assertVariantAnnotationQueryResultEquals(List<QueryResult<VariantAnnotation>> actualQueryResultList,
                                                          List expectedObjectList) {
        assertEquals(actualQueryResultList.size(), expectedObjectList.size());
        for (int i = 0; i < actualQueryResultList.size(); i++) {
            VariantAnnotation expected = jsonObjectMapper.convertValue(((List) ((Map) expectedObjectList.get(i)).get("result")).get(0),
                    VariantAnnotation.class);
            VariantAnnotation actual = actualQueryResultList.get(i).getResult().get(0);
            assertEquals(expected, actual);
        }
    }

    private int countLines(String fileName) throws IOException {
        System.out.println("Counting lines...");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();

        return lines;
    }

    public class ConsequenceTypeComparator implements Comparator<ConsequenceType> {
        public int compare(ConsequenceType consequenceType1, ConsequenceType consequenceType2) {
            String geneId1 = consequenceType1.getEnsemblGeneId()==null?"":consequenceType1.getEnsemblGeneId();
            String geneId2 = consequenceType2.getEnsemblGeneId()==null?"":consequenceType2.getEnsemblGeneId();

            int geneComparison = geneId1.compareTo(geneId2);
            if(geneComparison == 0 && !geneId1.equals("")) {
                return consequenceType1.getEnsemblTranscriptId().compareTo(consequenceType2.getEnsemblTranscriptId());
            } else {
                return geneComparison;
            }
        }
    }

    private class AnnotationComparisonObject {
        String chr;
        String pos;
        String alt;
        String ensemblGeneId;
        String ensemblTranscriptId;
        String biotype;
        String SOname;

        public AnnotationComparisonObject(String chr, String pos, String alt, String ensemblGeneId,
                                          String ensemblTranscriptId, String SOname) {
            this(chr, pos, alt, ensemblGeneId, ensemblTranscriptId, "-", SOname);
        }

        public AnnotationComparisonObject(String chr, String pos, String alt, String ensemblGeneId,
                                          String ensemblTranscriptId, String biotype, String SOname) {
            this.chr = chr;
            this.pos = pos;
            this.alt = alt;
            this.ensemblGeneId = ensemblGeneId;
            this.ensemblTranscriptId = ensemblTranscriptId;
            this.biotype = biotype;
            this.SOname = SOname;
        }

        public String getChr() {
            return chr;
        }

        public String getPos() {
            return pos;
        }

        public String getAlt() {
            return alt;
        }

        public String getEnsemblGeneId() {
            return ensemblGeneId;
        }

        public String getSOname() {
            return SOname;
        }

        public String getEnsemblTranscriptId() {
            return ensemblTranscriptId;
        }

        public void setEnsemblTranscriptId(String ensemblTranscriptId) {
            this.ensemblTranscriptId = ensemblTranscriptId;
        }

        public String getBiotype() {
            return biotype;
        }

        public void setBiotype(String biotype) {
            this.biotype = biotype;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AnnotationComparisonObject)) return false;

            AnnotationComparisonObject that = (AnnotationComparisonObject) o;

            if (SOname != null ? !SOname.equals(that.SOname) : that.SOname != null) return false;
            if (alt != null ? !alt.equals(that.alt) : that.alt != null) return false;
            if (chr != null ? !chr.equals(that.chr) : that.chr != null) return false;
            if (ensemblGeneId != null ? !ensemblGeneId.equals(that.ensemblGeneId) : that.ensemblGeneId != null)
                return false;
            if (ensemblTranscriptId != null ? !ensemblTranscriptId.equals(that.ensemblTranscriptId) : that.ensemblTranscriptId != null)
                return false;
            if (pos != null ? !pos.equals(that.pos) : that.pos != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = chr != null ? chr.hashCode() : 0;
            result = 31 * result + (pos != null ? pos.hashCode() : 0);
            result = 31 * result + (alt != null ? alt.hashCode() : 0);
            result = 31 * result + (ensemblGeneId != null ? ensemblGeneId.hashCode() : 0);
            result = 31 * result + (ensemblTranscriptId != null ? ensemblTranscriptId.hashCode() : 0);
            result = 31 * result + (SOname != null ? SOname.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return chr+"\t"+pos+"\t"+alt+"\t"+ensemblGeneId+"\t"+ensemblTranscriptId+"\t"+biotype+"\t"+SOname+"\n";
        }
    }

    public class AnnotationComparisonObjectComparator implements Comparator<AnnotationComparisonObject> {
        public int compare(AnnotationComparisonObject annotationComparisonObject1, AnnotationComparisonObject annotationComparisonObject2) {

            int chrComparison = annotationComparisonObject1.getChr().compareTo(annotationComparisonObject2.getChr());
            if(chrComparison == 0) {
                return annotationComparisonObject1.getPos().compareTo(annotationComparisonObject2.getPos());
            } else {
                return chrComparison;
            }
        }

    }

    @Test
    public void testGetAllConsequenceTypesByVariant() throws IOException, URISyntaxException {

        QueryResult<ConsequenceType> consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 33167329, "AC", "TT"),
                    new QueryOptions("normalize", false));
        assertObjectListEquals("[{\"geneName\":\"ANKRD27\",\"ensemblGeneId\":\"ENSG00000105186\",\"ensemblTranscriptId\":\"ENST00000306065\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ANKRD27\",\"ensemblGeneId\":\"ENSG00000105186\",\"ensemblTranscriptId\":\"ENST00000587352\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ANKRD27\",\"ensemblGeneId\":\"ENSG00000105186\",\"ensemblTranscriptId\":\"ENST00000586463\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ANKRD27\",\"ensemblGeneId\":\"ENSG00000105186\",\"ensemblTranscriptId\":\"ENST00000588700\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ANKRD27\",\"ensemblGeneId\":\"ENSG00000105186\",\"ensemblTranscriptId\":\"ENST00000586693\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ANKRD27\",\"ensemblGeneId\":\"ENSG00000105186\",\"ensemblTranscriptId\":\"ENST00000590519\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.24630541871921183,\"number\":\"1/4\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":174,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001623\",\"name\":\"5_prime_UTR_variant\"}]},{\"geneName\":\"CTC-379B2.4\",\"ensemblGeneId\":\"ENSG00000267557\",\"ensemblTranscriptId\":\"ENST00000589127\",\"strand\":\"+\",\"biotype\":\"antisense\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"RGS9BP\",\"ensemblGeneId\":\"ENSG00000186326\",\"ensemblTranscriptId\":\"ENST00000334176\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.0691085003455425,\"number\":\"1/1\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1017,\"cdsPosition\":160,\"codon\":\"ACC/TTC\",\"proteinVariantAnnotation\":{\"position\":54,\"reference\":\"THR\",\"alternate\":\"PHE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001650\",\"name\":\"inframe_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 33321526, "TTAC", "CC"),
                    new QueryOptions("normalize", false));
        assertObjectListEquals("[{\"geneName\":\"TDRD12\",\"ensemblGeneId\":\"ENSG00000173809\",\"ensemblTranscriptId\":\"ENST00000564769\",\"strand\":\"+\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"SLC7A9\",\"ensemblGeneId\":\"ENSG00000021488\",\"ensemblTranscriptId\":\"ENST00000590341\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":2.272727272727273,\"number\":\"13/13\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1638,\"cdsPosition\":1461,\"codon\":\"gaG/gaG\",\"proteinVariantAnnotation\":{\"position\":487,\"reference\":\"GLU\",\"alternate\":\"GLU\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001578\",\"name\":\"stop_lost\"}]},{\"geneName\":\"SLC7A9\",\"ensemblGeneId\":\"ENSG00000021488\",\"ensemblTranscriptId\":\"ENST00000590465\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":2.3529411764705883,\"number\":\"9/9\"}],\"cdnaPosition\":1890,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001624\",\"name\":\"3_prime_UTR_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"SLC7A9\",\"ensemblGeneId\":\"ENSG00000021488\",\"ensemblTranscriptId\":\"ENST00000023064\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":2.3529411764705883,\"number\":\"13/13\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1653,\"cdsPosition\":1461,\"codon\":\"gaG/gaG\",\"proteinVariantAnnotation\":{\"position\":487,\"reference\":\"GLU\",\"alternate\":\"GLU\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001578\",\"name\":\"stop_lost\"}]},{\"geneName\":\"SLC7A9\",\"ensemblGeneId\":\"ENSG00000021488\",\"ensemblTranscriptId\":\"ENST00000592232\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":4.123711340206185,\"number\":\"8/8\"}],\"cdnaPosition\":995,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001624\",\"name\":\"3_prime_UTR_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"SLC7A9\",\"ensemblGeneId\":\"ENSG00000021488\",\"ensemblTranscriptId\":\"ENST00000587772\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":6.153846153846154,\"number\":\"13/13\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1565,\"cdsPosition\":1461,\"codon\":\"gaG/gaG\",\"proteinVariantAnnotation\":{\"position\":487,\"reference\":\"GLU\",\"alternate\":\"GLU\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001578\",\"name\":\"stop_lost\"}]},{\"geneName\":\"CTD-2085J24.3\",\"ensemblGeneId\":\"ENSG00000267555\",\"ensemblTranscriptId\":\"ENST00000590069\",\"strand\":\"+\",\"biotype\":\"antisense\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 32884828, "CAAT", "CTT"),
                    new QueryOptions("normalize", false));
        assertObjectListEquals("[{\"geneName\":\"ZAR1L\",\"ensemblGeneId\":\"ENSG00000189167\",\"ensemblTranscriptId\":\"ENST00000533490\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":4.301075268817204,\"number\":\"4/6\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1111,\"cdsPosition\":692,\"codon\":\"gAT/gAA\",\"proteinVariantAnnotation\":{\"position\":231,\"reference\":\"ASP\",\"alternate\":\"GLU\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"ZAR1L\",\"ensemblGeneId\":\"ENSG00000189167\",\"ensemblTranscriptId\":\"ENST00000345108\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":4.301075268817204,\"number\":\"2/4\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":721,\"cdsPosition\":692,\"codon\":\"gAT/gAA\",\"proteinVariantAnnotation\":{\"position\":231,\"reference\":\"ASP\",\"alternate\":\"GLU\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000380152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000544455\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000530893\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 32316471, "GA", "AG"),
                    new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"RXFP2\",\"ensemblGeneId\":\"ENSG00000133105\",\"ensemblTranscriptId\":\"ENST00000380314\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"RXFP2\",\"ensemblGeneId\":\"ENSG00000133105\",\"ensemblTranscriptId\":\"ENST00000298386\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 32316469, "TG", "AT"),
                    new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"RXFP2\",\"ensemblGeneId\":\"ENSG00000133105\",\"ensemblTranscriptId\":\"ENST00000380314\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"RXFP2\",\"ensemblGeneId\":\"ENSG00000133105\",\"ensemblTranscriptId\":\"ENST00000298386\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("17", 43124094, "AT", "TC"),
                    new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"DCAKD\",\"ensemblGeneId\":\"ENSG00000172992\",\"ensemblTranscriptId\":\"ENST00000342350\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"DCAKD\",\"ensemblGeneId\":\"ENSG00000172992\",\"ensemblTranscriptId\":\"ENST00000588499\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"DCAKD\",\"ensemblGeneId\":\"ENSG00000172992\",\"ensemblTranscriptId\":\"ENST00000593094\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"DCAKD\",\"ensemblGeneId\":\"ENSG00000172992\",\"ensemblTranscriptId\":\"ENST00000588295\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"DCAKD\",\"ensemblGeneId\":\"ENSG00000172992\",\"ensemblTranscriptId\":\"ENST00000310604\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"NMT1\",\"ensemblGeneId\":\"ENSG00000136448\",\"ensemblTranscriptId\":\"ENST00000592782\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
            variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("MT", 12906, "C", "A"),
                    new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"MT-CO2\",\"ensemblGeneId\":\"ENSG00000198712\",\"ensemblTranscriptId\":\"ENST00000361739\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-TK\",\"ensemblGeneId\":\"ENSG00000210156\",\"ensemblTranscriptId\":\"ENST00000387421\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-ATP8\",\"ensemblGeneId\":\"ENSG00000228253\",\"ensemblTranscriptId\":\"ENST00000361851\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-ATP6\",\"ensemblGeneId\":\"ENSG00000198899\",\"ensemblTranscriptId\":\"ENST00000361899\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-CO3\",\"ensemblGeneId\":\"ENSG00000198938\",\"ensemblTranscriptId\":\"ENST00000362079\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-TG\",\"ensemblGeneId\":\"ENSG00000210164\",\"ensemblTranscriptId\":\"ENST00000387429\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-ND3\",\"ensemblGeneId\":\"ENSG00000198840\",\"ensemblTranscriptId\":\"ENST00000361227\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-TR\",\"ensemblGeneId\":\"ENSG00000210174\",\"ensemblTranscriptId\":\"ENST00000387439\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-ND4L\",\"ensemblGeneId\":\"ENSG00000212907\",\"ensemblTranscriptId\":\"ENST00000361335\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"MT-ND4\",\"ensemblGeneId\":\"ENSG00000198886\",\"ensemblTranscriptId\":\"ENST00000361381\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"MT-TH\",\"ensemblGeneId\":\"ENSG00000210176\",\"ensemblTranscriptId\":\"ENST00000387441\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"MT-TS2\",\"ensemblGeneId\":\"ENSG00000210184\",\"ensemblTranscriptId\":\"ENST00000387449\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"MT-TL2\",\"ensemblGeneId\":\"ENSG00000210191\",\"ensemblTranscriptId\":\"ENST00000387456\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"MT-ND5\",\"ensemblGeneId\":\"ENSG00000198786\",\"ensemblTranscriptId\":\"ENST00000361567\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.05518763796909492,\"number\":\"1/1\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":570,\"cdsPosition\":570,\"codon\":\"atC/atA\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P03915\",\"position\":190,\"reference\":\"ILE\",\"alternate\":\"MET\",\"substitutionScores\":[{\"score\":0.31,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.052,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"Complete proteome\",\"Disease mutation\",\"Electron transport\",\"Leber hereditary optic neuropathy\",\"Leigh syndrome\",\"MELAS syndrome\",\"Membrane\",\"Mitochondrion\",\"Mitochondrion inner membrane\",\"NAD\",\"Oxidoreductase\",\"Polymorphism\",\"Reference proteome\",\"Respiratory chain\",\"Transmembrane\",\"Transmembrane helix\",\"Transport\",\"Ubiquinone\"],\"features\":[{\"id\":\"IPR003945\",\"start\":8,\"end\":501,\"description\":\"NADH-plastoquinone oxidoreductase, chain 5\"},{\"id\":\"IPR001750\",\"start\":134,\"end\":418,\"description\":\"NADH:quinone oxidoreductase/Mrp antiporter, membrane subunit\"},{\"start\":171,\"end\":191,\"type\":\"transmembrane region\",\"description\":\"Helical\"},{\"id\":\"PRO_0000118101\",\"start\":1,\"end\":603,\"type\":\"chain\",\"description\":\"NADH-ubiquinone oxidoreductase chain 5\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"MT-ND6\",\"ensemblGeneId\":\"ENSG00000198695\",\"ensemblTranscriptId\":\"ENST00000361681\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"MT-TE\",\"ensemblGeneId\":\"ENSG00000210194\",\"ensemblTranscriptId\":\"ENST00000387459\",\"strand\":\"-\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"MT-CYB\",\"ensemblGeneId\":\"ENSG00000198727\",\"ensemblTranscriptId\":\"ENST00000361789\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"MT-TT\",\"ensemblGeneId\":\"ENSG00000210195\",\"ensemblTranscriptId\":\"ENST00000387460\",\"strand\":\"+\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"MT-TP\",\"ensemblGeneId\":\"ENSG00000210196\",\"ensemblTranscriptId\":\"ENST00000387461\",\"strand\":\"-\",\"biotype\":\"Mt_tRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1:818401-819973:<CN10>"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"AL645608.2\",\"ensemblGeneId\":\"ENSG00000269308\",\"ensemblTranscriptId\":\"ENST00000594233\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"2/3\"},{\"percentage\":56.52173913043478,\"number\":\"3/3\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1:819287-820859:<CN3>"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"AL645608.2\",\"ensemblGeneId\":\"ENSG00000269308\",\"ensemblTranscriptId\":\"ENST00000594233\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"2/3\"},{\"percentage\":100,\"number\":\"3/3\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001590\",\"name\":\"terminator_codon_variant\"},{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1:816505-825225:<CN4>"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"FAM41C\",\"ensemblGeneId\":\"ENSG00000230368\",\"ensemblTranscriptId\":\"ENST00000446136\",\"strand\":\"-\",\"biotype\":\"lincRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"FAM41C\",\"ensemblGeneId\":\"ENSG00000230368\",\"ensemblTranscriptId\":\"ENST00000427857\",\"strand\":\"-\",\"biotype\":\"lincRNA\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"AL645608.2\",\"ensemblGeneId\":\"ENSG00000269308\",\"ensemblTranscriptId\":\"ENST00000594233\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001889\",\"name\":\"transcript_amplification\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("17", 4542753, "N", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000570836\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.49504950495049505,\"number\":\"3/15\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":406,\"cdsPosition\":309,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":103,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000293761\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.49504950495049505,\"number\":\"2/14\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":323,\"cdsPosition\":309,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":103,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000574640\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":1.1764705882352942,\"number\":\"2/14\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":476,\"cdsPosition\":192,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":64,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000545513\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.49504950495049505,\"number\":\"3/15\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":406,\"cdsPosition\":375,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":125,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000576572\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000572265\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.22075055187637968,\"number\":\"1/3\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":425,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001623\",\"name\":\"5_prime_UTR_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000573740\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"exonOverlap\":[{\"percentage\":0.49504950495049505,\"number\":\"2/3\"}],\"cdnaPosition\":336,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000576394\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.5154639175257731,\"number\":\"2/2\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":533,\"cdsPosition\":309,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":103,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("9", 133936571, "N", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"LAMC3\",\"ensemblGeneId\":\"ENSG00000050555\",\"ensemblTranscriptId\":\"ENST00000361069\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.5291005291005291,\"number\":\"13/28\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":2441,\"cdsPosition\":2308,\"codon\":\"Cgg/Agg\",\"proteinVariantAnnotation\":{\"position\":770,\"reference\":\"ARG\",\"alternate\":\"ARG\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"LAMC3\",\"ensemblGeneId\":\"ENSG00000050555\",\"ensemblTranscriptId\":\"ENST00000480883\",\"strand\":\"+\",\"biotype\":\"processed_transcript\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287261, "G", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"POTEH\",\"ensemblGeneId\":\"ENSG00000198062\",\"ensemblTranscriptId\":\"ENST00000452800\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":0.21551724137931033,\"number\":\"1/12\"}],\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":457,\"cdsPosition\":457,\"codon\":\"Caa/Taa\",\"proteinVariantAnnotation\":{\"position\":153,\"reference\":\"GLN\",\"alternate\":\"STOP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"POTEH\",\"ensemblGeneId\":\"ENSG00000198062\",\"ensemblTranscriptId\":\"ENST00000343518\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.14619883040935672,\"number\":\"1/11\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":677,\"cdsPosition\":625,\"codon\":\"Caa/Taa\",\"proteinVariantAnnotation\":{\"position\":209,\"reference\":\"GLN\",\"alternate\":\"STOP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16057210, "C", "T"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"LA16c-4G1.3\",\"ensemblGeneId\":\"ENSG00000233866\",\"ensemblTranscriptId\":\"ENST00000424770\",\"strand\":\"+\",\"biotype\":\"lincRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 163395, "T", "G"),
                        new QueryOptions());
        assertObjectListEquals("[{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001628\",\"name\":\"intergenic_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("18", 163395, "C", "G"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000582707\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.684931506849315,\"number\":\"2/15\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":420,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.967,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000400266\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.684931506849315,\"number\":\"2/15\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":267,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.881,\"source\":\"polyphen\",\"description\":\"possibly damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000580410\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.8064516129032258,\"number\":\"3/3\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":438,\"cdsPosition\":26,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":9,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000578942\",\"strand\":\"+\",\"biotype\":\"retained_intron\",\"exonOverlap\":[{\"percentage\":0.684931506849315,\"number\":\"2/14\"}],\"cdnaPosition\":242,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000383589\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.684931506849315,\"number\":\"2/14\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":212,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.985,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000261601\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.684931506849315,\"number\":\"2/16\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":195,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000581983\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.6802721088435374,\"number\":\"3/3\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":444,\"cdsPosition\":26,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":9,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000583119\",\"strand\":\"+\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":1.639344262295082,\"number\":\"1/6\"}],\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":3,\"cdsPosition\":5,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":2,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17054103, "G", "A"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"KB-67B5.12\",\"ensemblGeneId\":\"ENSG00000233995\",\"ensemblTranscriptId\":\"ENST00000454360\",\"strand\":\"+\",\"biotype\":\"unprocessed_pseudogene\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001575\",\"name\":\"splice_donor_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 69585, "TGAGGTCGATAGTTTTTA", "-"),
                        new QueryOptions());  // should not return NPE
        assertObjectListEquals("[{\"geneName\":\"OR4F5\",\"ensemblGeneId\":\"ENSG00000186092\",\"ensemblTranscriptId\":\"ENST00000335137\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":1.9607843137254901,\"number\":\"1/1\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":495,\"cdsPosition\":495,\"codon\":\"aaT/AAT\",\"proteinVariantAnnotation\":{\"position\":165,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001822\",\"name\":\"inframe_deletion\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17668822, "TCTCTACTAAAAATACAAAAAATTAGCCAGGCGTGGTGGCAGGTGCCTGTAGTACCAGCTACTTGGAAGGCTGAGGCAGGAGACTCTCTTGAACCTGGGAAGCCGAGGTTGCAGTGAGCTGGGCGACAGAGGGAGACTCCGTAAAAAAAAGAAAAAAAAAGAAGAAGAAGAAAAGAAAACAGGAAGGAAAGAAGAAAGAGAAACTAGAAATAATACATGTAAAGTGGCTGATTCTATTATCCTTGTTATTCCTTCTCCATGGGGCTGTTGTCAGGATTAAGTGAGATAGAGCACAGGAAAGGGCTCTGGAAACGCCTGTAGGCTCTAACCCTGAGGCATGGGCCTGTGGCCAGGAGCTCTCCCATTGACCACCTCCGCTGCCTCTGCTCGCATCCCGCAGGCTCACCTGTTTCTCCGGCGTGGAAGAAGTAAGGCAGCTTAACGCCATCCTTGGCGGGGATCATCAGAGCTTCCTTGTAGTCATGCAAGGAGTGGCCAGTGTCCTCATGCCCCACCTGCAGGACAGAGAGGGACAGGGAGGTGTCTGCAGGGCGCATGCCTCACTTGCTGATGGCGCGCCCTGGAGCCTGTGCACACCCTTCCTTGTACCCTGCCACCACTGCCGGGACCTTTGTCACACAGCCTTTTAAGAATGACCAGGAGCAGGCCAGGCGTGGTGGCTCACACCTGTAATCCCAGCACTTTGGGAGGCCGAGGCAGGCAGATCACGAAGTCAGGAGATCGAGACCATCCTGGCTAACACAGTGAAACCCCA", "-"),
                        new QueryOptions());  // should not return NPE
        assertObjectListEquals("[{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000399839\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"7/10\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001906\",\"name\":\"feature_truncation\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000330232\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"4/7\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001906\",\"name\":\"feature_truncation\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000262607\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"6/9\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001906\",\"name\":\"feature_truncation\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000449907\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"7/10\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001906\",\"name\":\"feature_truncation\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000399837\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":100,\"number\":\"7/10\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001580\",\"name\":\"coding_sequence_variant\"},{\"accession\":\"SO:0001906\",\"name\":\"feature_truncation\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000469063\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"exonOverlap\":[{\"percentage\":94.60154241645245,\"number\":\"1/2\"}],\"cdnaPosition\":22,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001906\",\"name\":\"feature_truncation\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CECR1\",\"ensemblGeneId\":\"ENSG00000093072\",\"ensemblTranscriptId\":\"ENST00000480276\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"AC005300.5\",\"ensemblGeneId\":\"ENSG00000236325\",\"ensemblTranscriptId\":\"ENST00000428401\",\"strand\":\"+\",\"biotype\":\"processed_pseudogene\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 16555369, "T", "-"),
                        new QueryOptions());  // should not return NPE
        assertObjectListEquals("[{\"geneName\":\"ANO7P1\",\"ensemblGeneId\":\"ENSG00000237276\",\"ensemblTranscriptId\":\"ENST00000602586\",\"strand\":\"-\",\"biotype\":\"transcribed_unprocessed_pseudogene\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"C1orf134\",\"ensemblGeneId\":\"ENSG00000204377\",\"ensemblTranscriptId\":\"ENST00000375605\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.5235602094240838,\"number\":\"2/2\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":251,\"cdsPosition\":251,\"codon\":\"tAG/TGT\",\"proteinVariantAnnotation\":{\"position\":84,\"reference\":\"STOP\",\"alternate\":\"CYS\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001578\",\"name\":\"stop_lost\"}]},{\"geneName\":\"RSG1\",\"ensemblGeneId\":\"ENSG00000132881\",\"ensemblTranscriptId\":\"ENST00000375599\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"RSG1\",\"ensemblGeneId\":\"ENSG00000132881\",\"ensemblTranscriptId\":\"ENST00000434014\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\",\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("18", 30913143, "T", ""),
                        new QueryOptions());  // should not return String Index Out of Bounds
        assertObjectListEquals("[{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000403303\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"9/22\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1016,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000383096\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"10/23\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1057,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000300227\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"10/22\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1085,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000579916\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000583930\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"9/23\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":953,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000406524\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"8/22\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":898,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000402325\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"8/20\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":874,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000579947\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"10/23\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1057,\"cdsPosition\":874,\"codon\":\"ATG/TGG\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"MET\",\"alternate\":\"TRP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000577268\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"exonOverlap\":[{\"percentage\":0.45248868778280543,\"number\":\"2/3\"}],\"cdnaPosition\":228,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000399177\",\"strand\":\"-\",\"biotype\":\"non_stop_decay\",\"exonOverlap\":[{\"percentage\":0.46296296296296297,\"number\":\"9/9\"}],\"cdnaPosition\":1017,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001626\",\"name\":\"incomplete_terminal_codon_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("14", 38679764, "-", "GATCTGAGAAGNGGAANANAAGGG"),
                        new QueryOptions());  // should not return NPE
        assertObjectListEquals("[{\"geneName\":\"SSTR1\",\"ensemblGeneId\":\"ENSG00000139874\",\"ensemblTranscriptId\":\"ENST00000267377\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"3/3\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1786,\"cdsPosition\":1169,\"codon\":\"acG/acG\",\"proteinVariantAnnotation\":{\"position\":390,\"reference\":\"THR\",\"alternate\":\"THR\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"},{\"accession\":\"SO:0001821\",\"name\":\"inframe_insertion\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("20", 44485953, "-", "ATCT"),
                        new QueryOptions());  // should return ENSG00000101473 ENST00000217455 -       initiator_codon_variant
        assertObjectListEquals("[{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000217455\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/6\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":93,\"cdsPosition\":2,\"codon\":\"atG/atA\",\"proteinVariantAnnotation\":{\"position\":1,\"reference\":\"MET\",\"alternate\":\"ILE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0002012\",\"name\":\"start_lost\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000461272\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/6\"}],\"cdnaPosition\":76,\"cdsPosition\":2,\"codon\":\"atG/atA\",\"proteinVariantAnnotation\":{\"position\":1,\"reference\":\"MET\",\"alternate\":\"ILE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0002012\",\"name\":\"start_lost\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000488679\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/5\"}],\"cdnaPosition\":89,\"cdsPosition\":2,\"codon\":\"atG/atA\",\"proteinVariantAnnotation\":{\"position\":1,\"reference\":\"MET\",\"alternate\":\"ILE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0002012\",\"name\":\"start_lost\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000487205\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000493118\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/3\"}],\"cdnaPosition\":58,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000483141\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/4\"}],\"cdnaPosition\":56,\"cdsPosition\":2,\"codon\":\"atG/atA\",\"proteinVariantAnnotation\":{\"position\":1,\"reference\":\"MET\",\"alternate\":\"ILE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0002012\",\"name\":\"start_lost\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000484783\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/4\"}],\"cdnaPosition\":84,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000486165\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/5\"}],\"cdnaPosition\":58,\"cdsPosition\":2,\"codon\":\"atG/atA\",\"proteinVariantAnnotation\":{\"position\":1,\"reference\":\"MET\",\"alternate\":\"ILE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0002012\",\"name\":\"start_lost\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000481938\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/4\"}],\"cdnaPosition\":92,\"cdsPosition\":2,\"codon\":\"atG/atA\",\"proteinVariantAnnotation\":{\"position\":1,\"reference\":\"MET\",\"alternate\":\"ILE\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0002012\",\"name\":\"start_lost\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000457981\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000426915\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ZSWIM3\",\"ensemblGeneId\":\"ENSG00000132801\",\"ensemblTranscriptId\":\"ENST00000255152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"geneName\":\"ZSWIM3\",\"ensemblGeneId\":\"ENSG00000132801\",\"ensemblTranscriptId\":\"ENST00000454862\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001636\",\"name\":\"2KB_upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]},{\"geneName\":null,\"ensemblGeneId\":null,\"ensemblTranscriptId\":null,\"strand\":null,\"biotype\":null,\"exonOverlap\":null,\"transcriptAnnotationFlags\":null,\"cdnaPosition\":null,\"cdsPosition\":null,\"codon\":null,\"proteinVariantAnnotation\":null,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001782\",\"name\":\"TF_binding_site_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("15", 78224189, "-", "C"),
                        new QueryOptions());  // should return ENSG00000101473 ENST00000217455 -       initiator_codon_variant
        assertObjectListEquals("[{\"geneName\":\"RP11-114H24.2\",\"ensemblGeneId\":\"ENSG00000260776\",\"ensemblTranscriptId\":\"ENST00000567226\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 52718051, "-", "T"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000258597\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"10/16\"}],\"cdnaPosition\":951,\"cdsPosition\":876,\"codon\":\"CAA/ACA\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"GLN\",\"alternate\":\"THR\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000548127\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"10/16\"}],\"cdnaPosition\":1252,\"cdsPosition\":876,\"codon\":\"CAA/ACA\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"GLN\",\"alternate\":\"THR\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000339406\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"10/16\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1252,\"cdsPosition\":876,\"codon\":\"GCA/AGC\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"ALA\",\"alternate\":\"SER\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000378101\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"10/16\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1111,\"cdsPosition\":876,\"codon\":\"GCA/AGC\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"ALA\",\"alternate\":\"SER\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000400357\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"9/14\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":2170,\"cdsPosition\":876,\"codon\":\"CAA/ACA\",\"proteinVariantAnnotation\":{\"position\":292,\"reference\":\"GLN\",\"alternate\":\"THR\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000452082\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"9/14\"}],\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":2170,\"cdsPosition\":939,\"codon\":\"CAA/ACA\",\"proteinVariantAnnotation\":{\"position\":313,\"reference\":\"GLN\",\"alternate\":\"THR\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000547820\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"exonOverlap\":[{\"percentage\":-1,\"number\":\"1/6\"}],\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":65,\"cdsPosition\":66,\"codon\":\"CAA/ACA\",\"proteinVariantAnnotation\":{\"position\":22,\"reference\":\"GLN\",\"alternate\":\"THR\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000551355\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000552973\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 32893271, "A", "G"),
                        new QueryOptions());  // should set functional description "In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046" for ENST00000380152
        assertObjectListEquals("[{\"geneName\":\"ZAR1L\",\"ensemblGeneId\":\"ENSG00000189167\",\"ensemblTranscriptId\":\"ENST00000533490\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000380152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.40160642570281124,\"number\":\"3/27\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":358,\"cdsPosition\":125,\"codon\":\"tAt/tGt\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P51587\",\"position\":42,\"reference\":\"TYR\",\"alternate\":\"CYS\",\"uniprotVariantId\":\"VAR_020705\",\"functionalDescription\":\"In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046.\",\"substitutionScores\":[{\"score\":0.11,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.032,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Cell cycle\",\"Complete proteome\",\"Cytoplasm\",\"Cytoskeleton\",\"Disease mutation\",\"DNA damage\",\"DNA recombination\",\"DNA repair\",\"DNA-binding\",\"Fanconi anemia\",\"Nucleus\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Tumor suppressor\",\"Ubl conjugation\"],\"features\":[{\"id\":\"IPR015525\",\"start\":2,\"end\":1709,\"description\":\"Breast cancer type 2 susceptibility protein\"},{\"id\":\"IPR015525\",\"start\":1,\"end\":3418,\"type\":\"chain\",\"description\":\"Breast cancer type 2 susceptibility protein\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000544455\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.40160642570281124,\"number\":\"3/28\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":352,\"cdsPosition\":125,\"codon\":\"tAt/tGt\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P51587\",\"position\":42,\"reference\":\"TYR\",\"alternate\":\"CYS\",\"uniprotVariantId\":\"VAR_020705\",\"functionalDescription\":\"In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046.\",\"substitutionScores\":[{\"score\":0.11,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.032,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Cell cycle\",\"Complete proteome\",\"Cytoplasm\",\"Cytoskeleton\",\"Disease mutation\",\"DNA damage\",\"DNA recombination\",\"DNA repair\",\"DNA-binding\",\"Fanconi anemia\",\"Nucleus\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Tumor suppressor\",\"Ubl conjugation\"],\"features\":[{\"id\":\"IPR015525\",\"start\":2,\"end\":1709,\"description\":\"Breast cancer type 2 susceptibility protein\"},{\"id\":\"IPR015525\",\"start\":1,\"end\":3418,\"type\":\"chain\",\"description\":\"Breast cancer type 2 susceptibility protein\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000530893\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.40160642570281124,\"number\":\"3/10\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":323,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001623\",\"name\":\"5_prime_UTR_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"TOMM40\",\"ensemblGeneId\":\"ENSG00000130204\",\"ensemblTranscriptId\":\"ENST00000252487\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"TOMM40\",\"ensemblGeneId\":\"ENSG00000130204\",\"ensemblTranscriptId\":\"ENST00000592434\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000252486\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.11614401858304298,\"number\":\"4/4\"}],\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":499,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P02649\",\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"uniprotVariantId\":\"VAR_000652\",\"functionalDescription\":\"In HLPP3; form E3**, form E4, form E4/3 and some forms E5-type; only form E3** is disease-linked; dbSNP:rs429358.\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Alzheimer disease\",\"Amyloidosis\",\"Cholesterol metabolism\",\"Chylomicron\",\"Complete proteome\",\"Direct protein sequencing\",\"Disease mutation\",\"Glycation\",\"Glycoprotein\",\"HDL\",\"Heparin-binding\",\"Hyperlipidemia\",\"Lipid metabolism\",\"Lipid transport\",\"Neurodegeneration\",\"Oxidation\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Secreted\",\"Signal\",\"Steroid metabolism\",\"Sterol metabolism\",\"Transport\",\"VLDL\"],\"features\":[{\"start\":106,\"end\":141,\"type\":\"helix\"},{\"start\":80,\"end\":255,\"type\":\"region of interest\",\"description\":\"8 X 22 AA approximate tandem repeats\"},{\"start\":124,\"end\":145,\"type\":\"repeat\",\"description\":\"3\"},{\"id\":\"IPR000074\",\"start\":81,\"end\":292,\"description\":\"Apolipoprotein A/E\"},{\"id\":\"PRO_0000001987\",\"start\":19,\"end\":317,\"type\":\"chain\",\"description\":\"Apolipoprotein E\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000446996\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.24271844660194175,\"number\":\"4/4\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":477,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000485628\",\"strand\":\"+\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0002083\",\"name\":\"2KB_downstream_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000434152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.2028397565922921,\"number\":\"4/4\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":523,\"cdsPosition\":466,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":156,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":0.91,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000425718\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"exonOverlap\":[{\"percentage\":0.23696682464454977,\"number\":\"3/3\"}],\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":653,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("17", 52, "C", "A"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"AC108004.5\",\"ensemblGeneId\":\"ENSG00000273288\",\"ensemblTranscriptId\":\"ENST00000583926\",\"strand\":\"-\",\"biotype\":\"miRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

////        QueryResult queryResult = variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 6638139, "A", "T"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 108309064, StringUtils.repeat("N",2252), "-"), new QueryOptions());  // should return ENSG00000215002 ENST00000399415 -       transcript_ablation
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 124814698, StringUtils.repeat("N",1048), "-"), new QueryOptions());  // should return ENSG00000215002 ENST00000399415 -       transcript_ablation
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 327947, "A", "-"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 327947, "A", "-"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 327947, "A", "-"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 101786, "A", "-"), new QueryOptions());  // should return ENSG00000173876 ENST00000413237 -       intron_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 10103931, "A", "-"), new QueryOptions());  // should return ENSG00000224788 ENST00000429539 -       intron_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 10005684, "-", "AT"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 696869, "C", "-"), new QueryOptions());  // should not return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 52365874, "G", "A"), new QueryOptions());  // should not return 10      133761141       A       ENSG00000175470 ENST00000422256 -       missense_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 133761141, "G", "A"), new QueryOptions());  // should not return 10      133761141       A       ENSG00000175470 ENST00000422256 -       missense_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 12172775, "G", "A"), new QueryOptions());  // should return 10      12172775        A       ENSG00000265653 ENST00000584402 -       non_coding_transcript_exon_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 10993859, "G", "C"), new QueryOptions());  // should return 10      10993859        C       ENSG00000229240 ENST00000598573 -       splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 323246, "T", "C"), new QueryOptions());  // should not return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 323246, "T", "C"), new QueryOptions());  // should not return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 295047, "T", "G"), new QueryOptions());  // should return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 295047, "T", "G"), new QueryOptions());  // should return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 172663, "G", "A"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 114340663, "GCTGGGCATCCT", "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 114340663, "GCTGGGCATCCT", "ACTGGGCATCCT"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 220603289, "-", "GTGT"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 220603347, "CCTAGTA", "ACTACTA"), new QueryOptions());  // last triplet of the transcript (- strand) and last codifying codon of the transcript, should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 16555369, "-", "TG"), new QueryOptions());  // last triplet of the transcript (- strand) and last codifying codon of the transcript, should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 16555369, "T", "-"), new QueryOptions());  // last triplet of the transcript (- strand) and last codifying codon of the transcript, should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 70008, "-", "TG"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 167385325, "A", "-"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287365, "C", "T"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("5", 150407694, "G", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 20047783, "AAAAAA", "-"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 28942717, "NNN", "-"), new QueryOptions());  // should return ENST00000541932 stop_retained
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 45411941, "T", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("9", 107366952, StringUtils.repeat("N",12577), "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("7", 23775220, "T", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("5", 150407694, "G", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("5", 150407693, "T", "G"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("4", 48896023, "G", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 12837706, "-", "CC"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 20047783, "-", "AAAAAA"), new QueryOptions());  // should return stop_gained
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 115828861, "C", "G"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("16", 32859177, "C", "T"), new QueryOptions());  // should return stop_lost
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 13481174, "NN", "-"), new QueryOptions());  // should return stop_lost
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 153600596, "-", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 10041199, "A", "T"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 102269845, "C", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("7", 158384306, "TGTG", "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("11", 118898436, "N", "-"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 6638139, "-", "T"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 70612070, StringUtils.repeat("N",11725), "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587846, "-", "CT"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 52718051, "-", "T"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 115412783, "-", "C"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 27793991, StringUtils.repeat("N",1907), "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 27436462, StringUtils.repeat("N",2), "-"), new QueryOptions());  // should not return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 6638139, "A", "T"), new QueryOptions());  // should not return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 3745870, "C", "T"), new QueryOptions());  // should not return null
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 35656173, "C", "A"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 28071285, "C", "G"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 35656173, "C", "A"), new QueryOptions());  // should return synonymous_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22274249, "-", "AGGAG"), new QueryOptions());  // should return downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 51042514, "-", "G"), new QueryOptions());  // should return downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587846, "-", "CT"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 42537628, "T", "C"), new QueryOptions());  // should return downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 27283340, "-", "C"), new QueryOptions());  // should return splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 31478142, "-", "G"), new QueryOptions());  // should return downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 29684676, "G", "A"), new QueryOptions());  // should return downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 40806293, "-", "TGTG"), new QueryOptions());  // should return downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 39426437, StringUtils.repeat("N",20092), "-"), new QueryOptions());  // ¿should return 3_prime_UTR_variant? No if ENSEMBLs gtf was used
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 38069602, StringUtils.repeat("N",5799), "-"), new QueryOptions());  // should return 3_prime_UTR_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17054103, "A", "G"), new QueryOptions());  // should NOT return non_coding_transcript_exon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 35661560, "A", "G"), new QueryOptions());  // should return synonymous_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 31368158, StringUtils.repeat("N",4), "-"), new QueryOptions());  // should return donor_variant, intron_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587124, "-", "TA"), new QueryOptions());  // should return stop_retained_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 30824659, "-", "A"), new QueryOptions());  // should return stop_retained_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 26951215, "T", "C"), new QueryOptions());  // should NOT return null pointer exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17602839, "G", "A"), new QueryOptions());  // should NOT return null pointer exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20891503, "-", "CCTC"), new QueryOptions());  // should return missense_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 21991357, "T", "C"), new QueryOptions());  // should return missense_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 24717655, "C", "T"), new QueryOptions());  // should return missense_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 24314402, StringUtils.repeat("N",19399), "-"), new QueryOptions());  // should return 3prime_UTR_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22007661, "G", "A"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 23476261, "G", "A"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22517056, StringUtils.repeat("N",82585), "-"), new QueryOptions());  // should return 3prime_UTR_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 21388510, "A", "T"), new QueryOptions());  // should NOT return mature_miRNA_variant but non_coding_transcript_variant,non_coding_transcript_exon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22007634, "G", "A"), new QueryOptions());  // should NOT return mature_miRNA_variant but non_coding_transcript_variant,non_coding_transcript_exon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20891502, "-", "CCTC"), new QueryOptions());  // should return splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18387495, "G", "A"), new QueryOptions());  // should NOT return incomplete_teminator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 19258045, StringUtils.repeat("N",27376), "-"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18293502, "T", "C"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18620375, StringUtils.repeat("N",9436), "-"), new QueryOptions());  // should return transcript_ablation
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18997219, StringUtils.repeat("N",12521), "-"), new QueryOptions());  // should return transcript_ablation
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17449263, "G", "A"), new QueryOptions());  // should return
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 21982892, "C", "T"), new QueryOptions());  // should return a result
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16676212, "C", "T"), new QueryOptions());  // should include downstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22022872, "T", "C"), new QueryOptions());  // should not raise an error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 179633644, "G", "C"), new QueryOptions());  // should include
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16123409, "-", "A"), new QueryOptions());  // should include
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 51234118, "C", "G"), new QueryOptions());  // should include upstream_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 155159745, "G", "A"), new QueryOptions());  // should not raise error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 179621477, "C", "T"), new QueryOptions());  // should not raise error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20918922, "C", "T"), new QueryOptions());  // should not raise java.lang.StringIndexOutOfBoundsException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18628756, "A", "T"), new QueryOptions());  // should not raise java.lang.NumberFormatException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17488995, "G", "A"), new QueryOptions());  // should not raise java.lang.NumberFormatException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17280889, "G", "A"), new QueryOptions());  // should not raise java.lang.NumberFormatException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16449075, "G", "A"), new QueryOptions());  // should not raise null exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287784, "C", "T"), new QueryOptions());  // should not raise null exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287365, "C", "T"), new QueryOptions());  // should not raise null exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17468875, "C", "A"), new QueryOptions());  // missense_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17451081, "C", "T"), new QueryOptions());  // should not include stop_reained_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17468875, "C", "T"), new QueryOptions());  // synonymous_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17449263, "G", "A"), new QueryOptions());  // should not include stop_reained_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17449238, "T", "C"), new QueryOptions());  // should not include stop_codon
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17071673, "A", "G"), new QueryOptions());  // 3_prime_UTR_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16151191, "G", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16340551, "A", "G"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17039749, "C", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287365, "C", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16101010, "TTA", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16062270, "G", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20918922, "C", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17668822, "TCTCTACTAAAAATACAAAAAATTAGCCAGGCGTGGTGGCAGGTGCCTGTAGTACCAGCTACTTGGAAGGCTGAGGCAGGAGACTCTCTTGAACCTGGGAAGCCGAGGTTGCAGTGAGCTGGGCGACAGAGGGAGACTCCGTAAAAAAAAGAAAAAAAAAGAAGAAGAAGAAAAGAAAACAGGAAGGAAAGAAGAAAGAGAAACTAGAAATAATACATGTAAAGTGGCTGATTCTATTATCCTTGTTATTCCTTCTCCATGGGGCTGTTGTCAGGATTAAGTGAGATAGAGCACAGGAAAGGGCTCTGGAAACGCCTGTAGGCTCTAACCCTGAGGCATGGGCCTGTGGCCAGGAGCTCTCCCATTGACCACCTCCGCTGCCTCTGCTCGCATCCCGCAGGCTCACCTGTTTCTCCGGCGTGGAAGAAGTAAGGCAGCTTAACGCCATCCTTGGCGGGGATCATCAGAGCTTCCTTGTAGTCATGCAAGGAGTGGCCAGTGTCCTCATGCCCCACCTGCAGGACAGAGAGGGACAGGGAGGTGTCTGCAGGGCGCATGCCTCACTTGCTGATGGCGCGCCCTGGAGCCTGTGCACACCCTTCCTTGTACCCTGCCACCACTGCCGGGACCTTTGTCACACAGCCTTTTAAGAATGACCAGGAGCAGGCCAGGCGTGGTGGCTCACACCTGTAATCCCAGCACTTTGGGAGGCCGAGGCAGGCAGATCACGAAGTCAGGAGATCGAGACCATCCTGGCTAACACAGTGAAACCCCA", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17668818, "C", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("8", 408515, "GAA", ""), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("3", 367747, "C", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("9", 214512, "C", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("14", 19108198, "-", "GGTCTAGCATG"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("3L", 22024723, "G", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2L", 37541199, "G", "A"), new QueryOptions());
//
////
////        // Use local gene collection to test these
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 5, "GGTCTAGCATG", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 1, "G", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 5, "GGTCTAGCATGTTACATGAAG", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 15, "GTTACATGAAG", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 21, "T", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 34, "-", "AAAT"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 42, "G", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 75, "T", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 75, "TCTAAGGCCTC", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 25, "GATAGTTCCTA", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 45, "GATAGGGTAC", "-"), new QueryOptions());
//
//
////        try {
////            br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf"))));
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
        /**
         * Calculates annotation for vcf file variants, loads vep annotations, compares batches and writes results
         */
//        String DIROUT = "/tmp/";
////        String DIROUT = "/homes/fjlopez/tmp/";
//        List<String> VCFS = new ArrayList<>();
//////        VCFS.add("/tmp/test.vcf");
//        VCFS.add("/media/shared/tmp/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr10.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr11.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr12.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr13.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr14.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr15.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr16.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr17.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr18.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr19.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr1.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr20.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr21.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr2.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr3.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr4.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr5.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr6.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr7.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr8.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr9.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chrX.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
////
//        List<String> VEPFILENAMES = new ArrayList<>();
//////        VEPFILENAMES.add("/tmp/test.txt");
//        VEPFILENAMES.add("/media/shared/tmp/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr10.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr11.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr12.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr13.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr14.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr15.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr16.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr17.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr18.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr19.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr1.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr20.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr21.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr2.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr3.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr4.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr5.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr6.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr7.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr8.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr9.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chrX.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
////
////
////
//        Set<AnnotationComparisonObject> uvaAnnotationSet = new HashSet<>();
//        Set<AnnotationComparisonObject> vepAnnotationSet = new HashSet<>();
//        int vepFileIndex = 0;
//        int nNonRegulatoryAnnotations = 0;
//        int nVariants = 0;
//        for (String vcfFilename : VCFS) {
//            System.out.println("Processing "+vcfFilename+" lines...");
//            VcfRawReader vcfReader = new VcfRawReader(vcfFilename);
//            File file = new File(VEPFILENAMES.get(vepFileIndex));
//            RandomAccessFile raf = new RandomAccessFile(file, "r");
//            if (vcfReader.open()) {
//                vcfReader.pre();
//                skipVepFileHeader(raf);
//                int nLines = countLines(vcfFilename);
//                int nReadVariants;
//                int lineCounter=0;
//                do {
//                    nReadVariants = getVcfAnnotationBatch(vcfReader, variantAnnotationCalculator, uvaAnnotationSet);
//                    nNonRegulatoryAnnotations += getVepAnnotationBatch(raf, nReadVariants, vepAnnotationSet);
//                    nVariants += nReadVariants;
//                    compareAndWrite(uvaAnnotationSet, vepAnnotationSet, lineCounter, nLines, nNonRegulatoryAnnotations,
//                            nVariants, DIROUT);
//                    lineCounter += nReadVariants;
//                    System.out.print(lineCounter+"/"+nLines+" - non-regulatory annotations: "+nNonRegulatoryAnnotations+"\r");
//                } while (nReadVariants > 0);
//                vcfReader.post();
//                vcfReader.close();
//                raf.close();
//            }
//            vepFileIndex++;
//        }
    }

    private <T> void assertObjectListEquals(String expectedConsequenceTypeJson, List<T> actualList,
                                            Class<T> clazz) {
        List expectedObjectList = jsonObjectMapper.convertValue(JSON.parse(expectedConsequenceTypeJson), List.class);
        assertEquals(expectedObjectList.size(), actualList.size());
        Set<T> actual = new HashSet<>(actualList);
        Set<T> expected = (Set) expectedObjectList.stream()
                .map(result -> (jsonObjectMapper.convertValue(result, clazz))).collect(Collectors.toSet());

        assertEquals(expected, actual);
//        for (int i = 0; i < list.size(); i++) {
//            assertEquals(list.get(i), jsonObjectMapper.convertValue(goldenObjectList.get(i), clazz));
//        }
    }

    private void skipVepFileHeader(RandomAccessFile raf) throws IOException {
        String line;
        long pos;
        do {
            pos = raf.getFilePointer();
            line = raf.readLine();
        }while(line.startsWith("#"));
        raf.seek(pos);
    }

    private int getVcfAnnotationBatch(VcfRawReader vcfReader, VariantAnnotationCalculator variantAnnotationDBAdaptor,
                                      Set<AnnotationComparisonObject> uvaAnnotationSet) {
        QueryResult queryResult = null;
        String pos;
        String ref;
        String cellbaseRef;
        String alt;
        String cellbaseAlt;
        String SoNameToTest;

        List<VcfRecord> vcfRecordList = vcfReader.read(1000);
        int ensemblPos;
        int processedVariants = 0;

        for (VcfRecord vcfRecord : vcfRecordList) {
//            boolean isSnv = false;
            // Short deletion
            if (vcfRecord.getReference().length() > 1) {
                ref = vcfRecord.getReference().substring(1);
                cellbaseRef = vcfRecord.getReference().substring(1);
                alt = "-";
                cellbaseAlt = "";
                ensemblPos = vcfRecord.getPosition() + 1;
                int end = getEndFromInfoField(vcfRecord);
                if(end==-1) {
                    if (ref.length() > 1) {
                        pos = (vcfRecord.getPosition() + 1) + "-" + (vcfRecord.getPosition() + ref.length());
                    } else {
                        pos = Integer.toString(vcfRecord.getPosition() + 1);
                    }
                } else {
                    pos = (vcfRecord.getPosition() + 1) + "-" + end;
                }
                // Alternate length may be > 1 if it contains <DEL>
            } else if (vcfRecord.getAlternate().length() > 1) {
                // Large deletion
                if (vcfRecord.getAlternate().equals("<DEL>")) {
                    ensemblPos = vcfRecord.getPosition() + 1;
                    int end = getEndFromInfoField(vcfRecord);
                    pos = (vcfRecord.getPosition() + 1) + "-" + end;
                    ref = StringUtils.repeat("N", end - vcfRecord.getPosition());
                    cellbaseRef = StringUtils.repeat("N", end - vcfRecord.getPosition());
                    alt = "-";
                    cellbaseAlt = "";
                    // Short insertion
                } else {
                    ensemblPos = vcfRecord.getPosition() + 1;
                    ref = "-";
                    cellbaseRef = "";
                    alt = vcfRecord.getAlternate().substring(1);
                    cellbaseAlt = vcfRecord.getAlternate().substring(1);
                    pos = vcfRecord.getPosition() + "-" + (vcfRecord.getPosition() + 1);
                }
                // SNV
            } else {
                ref = vcfRecord.getReference();
                cellbaseRef = vcfRecord.getReference();
                alt = vcfRecord.getAlternate();
                cellbaseAlt = vcfRecord.getAlternate();
                ensemblPos = vcfRecord.getPosition();
                pos = Integer.toString(ensemblPos);
//                isSnv = true;
            }
            // TODO: Remove this if as refactoring implements consequence types for other variant types
//            if(isSnv) {
                processedVariants++;
                try {
                    queryResult = variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new Variant(vcfRecord.getChromosome(), ensemblPos, cellbaseRef, cellbaseAlt), new QueryOptions());
                } catch (Exception e) {
                    System.out.println("new Variant = " + new Variant(vcfRecord.getChromosome(), ensemblPos, cellbaseRef, cellbaseAlt));
                    e.printStackTrace();
                    System.exit(1);
                }

                int i;
                List<ConsequenceType> consequenceTypeList = (List<ConsequenceType>) queryResult.getResult();
                for (i = 0; i < consequenceTypeList.size(); i++) {
                    for (SequenceOntologyTerm soTerm : consequenceTypeList.get(i).getSequenceOntologyTerms()) {
                        if (soTerm.getName().equals("2KB_upstream_variant")) {
                            SoNameToTest = "upstream_variant";
                        } else if (soTerm.getName().equals("2KB_downstream_variant")) {
                            SoNameToTest = "downstream_variant";
                        } else {
                            SoNameToTest = soTerm.getName();
                        }
                        uvaAnnotationSet.add(new AnnotationComparisonObject(vcfRecord.getChromosome(), pos, alt,
                                consequenceTypeList.get(i).getEnsemblGeneId() == null ? "-" : consequenceTypeList.get(i).getEnsemblGeneId(),
                                consequenceTypeList.get(i).getEnsemblTranscriptId() == null ? "-" : consequenceTypeList.get(i).getEnsemblTranscriptId(),
                                consequenceTypeList.get(i).getBiotype() == null ? "-" : consequenceTypeList.get(i).getBiotype(),
                                SoNameToTest));
                    }
                }
            }
//        }
        return processedVariants;
//        return vcfRecordList.size();
    }

    private int getEndFromInfoField(VcfRecord vcfRecord) {
        String[] infoFields = vcfRecord.getInfo().split(";");
        int i = 0;
        while (i < infoFields.length && !infoFields[i].startsWith("END=")) {
            i++;
        }

        if(i<infoFields.length) {
            return Integer.parseInt(infoFields[i].split("=")[1]);
        } else {
            return -1;
        }
    }

    private int getVepAnnotationBatch(RandomAccessFile raf, int nVariantsToRead,
                                       Set<AnnotationComparisonObject> vepAnnotationSet) throws IOException {
        /**
         * Loads VEP annotation
         */
        String newLine;
        int nNonRegulatoryAnnotations = 0;
        int nReadVariants = 0;
        String previousChr = "";
        String previousPosition = "";
        String previousAlt = "";
        String alt;
        long filePointer=0;

        if(nVariantsToRead>0) {
            while (((newLine = raf.readLine()) != null) && nReadVariants <= nVariantsToRead) {
                String[] lineFields = newLine.split("\t");
                String[] coordinatesParts = lineFields[1].split(":");
                if (lineFields[2].equals("deletion")) {
                    alt = "-";
                } else {
                    alt = lineFields[2];
                }
                // TODO: Remove this if as refactoring implements consequence types for other variant types
//                if(!alt.equals("-") && coordinatesParts[1].split("-").length==1) {
                    if (!previousChr.equals(coordinatesParts[0]) || !previousPosition.equals(coordinatesParts[1]) ||
                            !previousAlt.equals(alt)) {
                        nReadVariants++;
                    }
                    if (nReadVariants <= nVariantsToRead) {
                        for (String SOname : lineFields[6].split(",")) {
                            if (SOname.equals("nc_transcript_variant")) {
                                SOname = "non_coding_transcript_variant";
                            }
                            if (!SOname.equals("regulatory_region_variant")) {
                                nNonRegulatoryAnnotations++;
                            }
                            vepAnnotationSet.add(new AnnotationComparisonObject(coordinatesParts[0], coordinatesParts[1], alt, lineFields[3],
                                    lineFields[4], SOname));
                        }
                        previousChr = coordinatesParts[0];
                        previousPosition = coordinatesParts[1];
                        previousAlt = alt;
                        filePointer = raf.getFilePointer();
                    }
//                }
            }

            raf.seek(filePointer);
        }

        return nNonRegulatoryAnnotations;
    }

    private void compareAndWrite(Set<AnnotationComparisonObject> uvaAnnotationSet,
                                 Set<AnnotationComparisonObject> vepAnnotationSet, int lineCounter, int nLines,
                                 int nNonRegulatoryAnnotations, int nVariants, String dirout) throws IOException {

        /**
         * Compare both annotation sets and get UVA specific annotations
         */
        BufferedWriter bw = Files.newBufferedWriter(Paths.get(dirout+"/uva.specific.txt"), Charset.defaultCharset());
        bw.write("#CHR\tPOS\tALT\tENSG\tENST\tBIOTYPE\tCT\n");
        Set<AnnotationComparisonObject> uvaAnnotationSetBck = new HashSet<>(uvaAnnotationSet);
        uvaAnnotationSet.removeAll(vepAnnotationSet);
        List<AnnotationComparisonObject> uvaSpecificAnnotationList = new ArrayList(uvaAnnotationSet);
        Collections.sort(uvaSpecificAnnotationList, new AnnotationComparisonObjectComparator());
        for(AnnotationComparisonObject comparisonObject : uvaSpecificAnnotationList) {
            if(!comparisonObject.getSOname().equals("regulatory_region_variant")) {
                bw.write(comparisonObject.toString());
            }
        }
        bw.close();

        /**
         * Compare both annotation sets and get VEP specific annotations
         */
        bw = Files.newBufferedWriter(Paths.get(dirout+"vep.specific.txt"), Charset.defaultCharset());
        bw.write("#CHR\tPOS\tALT\tENSG\tENST\tBIOTYPE\tCT\n");
        vepAnnotationSet.removeAll(uvaAnnotationSetBck);
        List<AnnotationComparisonObject> vepSpecificAnnotationList = new ArrayList<>(vepAnnotationSet);
        Collections.sort(vepSpecificAnnotationList, new AnnotationComparisonObjectComparator());
        for(AnnotationComparisonObject comparisonObject : vepSpecificAnnotationList) {
            bw.write(comparisonObject.toString());
        }
        bw.write("\n\n\n");
        bw.write(lineCounter+"/"+nLines+"\n");
        bw.write("# processed variants: "+nVariants+"\n");
        bw.write("# non-regulatory annotations: "+nNonRegulatoryAnnotations+"\n");

        bw.close();
    }

}
