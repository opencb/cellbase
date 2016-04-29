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

package org.opencb.cellbase.mongodb.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.jackson.Log4jJsonObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;


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
    public void testGetAnnotationByVariantList() throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getClass().getResource("/variant-annotation-test.json.gz").getFile()))));

        String[] variantArray = {"1:40768842:C:G", "2:114340663:GCTGGGCATCC:ACTGGGCATCC", "19:45411941:T:C"};
        String line = reader.readLine();
        int i = 0;
        while (line !=null ) {
            assertVariantAnnotationQueryResultEquals(variantAnnotationCalculator.getAnnotationByVariantList((Variant.parseVariants(variantArray[i])),
                    new QueryOptions()),
                    jsonObjectMapper.convertValue(JSON.parse(line),
                            List.class));
            line = reader.readLine();
            i++;
        }

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
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("17", 4542753, "N", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000570836\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":406,\"cdsPosition\":309,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":103,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000293761\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":323,\"cdsPosition\":309,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":103,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000574640\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":476,\"cdsPosition\":192,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":64,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000545513\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":406,\"cdsPosition\":375,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":125,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000576572\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000572265\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":425,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001623\",\"name\":\"5_prime_UTR_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000573740\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":336,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ALOX15\",\"ensemblGeneId\":\"ENSG00000161905\",\"ensemblTranscriptId\":\"ENST00000576394\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":533,\"cdsPosition\":309,\"codon\":\"aaC/aaT\",\"proteinVariantAnnotation\":{\"position\":103,\"reference\":\"ASN\",\"alternate\":\"ASN\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("9", 133936571, "N", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"LAMC3\",\"ensemblGeneId\":\"ENSG00000050555\",\"ensemblTranscriptId\":\"ENST00000361069\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":2441,\"cdsPosition\":2308,\"codon\":\"Cgg/Agg\",\"proteinVariantAnnotation\":{\"position\":770,\"reference\":\"ARG\",\"alternate\":\"ARG\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001819\",\"name\":\"synonymous_variant\"}]},{\"geneName\":\"LAMC3\",\"ensemblGeneId\":\"ENSG00000050555\",\"ensemblTranscriptId\":\"ENST00000480883\",\"strand\":\"+\",\"biotype\":\"processed_transcript\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287261, "G", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"POTEH\",\"ensemblGeneId\":\"ENSG00000198062\",\"ensemblTranscriptId\":\"ENST00000452800\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":457,\"cdsPosition\":457,\"codon\":\"Caa/Taa\",\"proteinVariantAnnotation\":{\"position\":153,\"reference\":\"GLN\",\"alternate\":\"STOP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"POTEH\",\"ensemblGeneId\":\"ENSG00000198062\",\"ensemblTranscriptId\":\"ENST00000343518\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":677,\"cdsPosition\":625,\"codon\":\"Caa/Taa\",\"proteinVariantAnnotation\":{\"position\":209,\"reference\":\"GLN\",\"alternate\":\"STOP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16057210, "C", "T"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"LA16c-4G1.3\",\"ensemblGeneId\":\"ENSG00000233866\",\"ensemblTranscriptId\":\"ENST00000424770\",\"strand\":\"+\",\"biotype\":\"lincRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 163395, "C", "G"),
                        new QueryOptions());
        assertObjectListEquals("[{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001628\",\"name\":\"intergenic_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("18", 163395, "C", "G"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000582707\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":420,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.967,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000400266\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":267,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.881,\"source\":\"polyphen\",\"description\":\"possibly damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000580410\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":438,\"cdsPosition\":26,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":9,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000578942\",\"strand\":\"+\",\"biotype\":\"retained_intron\",\"cdnaPosition\":242,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000383589\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":212,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.985,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000261601\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":195,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000581983\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":444,\"cdsPosition\":26,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":9,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000583119\",\"strand\":\"+\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":3,\"cdsPosition\":5,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":2,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17054103, "G", "A"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"KB-67B5.12\",\"ensemblGeneId\":\"ENSG00000233995\",\"ensemblTranscriptId\":\"ENST00000454360\",\"strand\":\"+\",\"biotype\":\"unprocessed_pseudogene\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001575\",\"name\":\"splice_donor_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("18", 30913143, "T", ""),
                        new QueryOptions());  // should not return String Index Out of Bounds
        assertObjectListEquals("[{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000403303\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1016,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000383096\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1057,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000300227\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1085,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000579916\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000583930\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":953,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000406524\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":898,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000402325\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":874,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000579947\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1057,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000577268\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":228,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000399177\",\"strand\":\"-\",\"biotype\":\"non_stop_decay\",\"cdnaPosition\":1017,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001626\",\"name\":\"incomplete_terminal_codon_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("14", 38679764, "-", "GATCTGAGAAGNGGAANANAAGGG"),
                        new QueryOptions());  // should not return NPE
        assertObjectListEquals("[{\"geneName\":\"SSTR1\",\"ensemblGeneId\":\"ENSG00000139874\",\"ensemblTranscriptId\":\"ENST00000267377\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1786,\"cdsPosition\":1169,\"proteinVariantAnnotation\":{\"position\":390},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"},{\"accession\":\"SO:0001821\",\"name\":\"inframe_insertion\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("20", 44485953, "-", "ATCT"),
                        new QueryOptions());  // should return ENSG00000101473 ENST00000217455 -       initiator_codon_variant
        assertObjectListEquals("[{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000217455\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":93,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000461272\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":76,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000488679\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":89,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000487205\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000493118\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":58,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000483141\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":56,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000484783\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":84,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000486165\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":58,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000481938\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":92,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000457981\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000426915\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ZSWIM3\",\"ensemblGeneId\":\"ENSG00000132801\",\"ensemblTranscriptId\":\"ENST00000255152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ZSWIM3\",\"ensemblGeneId\":\"ENSG00000132801\",\"ensemblTranscriptId\":\"ENST00000454862\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("15", 78224189, "-", "C"),
                        new QueryOptions());  // should return ENSG00000101473 ENST00000217455 -       initiator_codon_variant
        assertObjectListEquals("[{\"geneName\":\"RP11-114H24.2\",\"ensemblGeneId\":\"ENSG00000260776\",\"ensemblTranscriptId\":\"ENST00000567226\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 52718051, "-", "T"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000258597\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":951,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000548127\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":1252,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000339406\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1252,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000378101\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1111,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000400357\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":2170,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000452082\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":2170,\"cdsPosition\":939,\"proteinVariantAnnotation\":{\"position\":313},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000547820\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":65,\"cdsPosition\":66,\"proteinVariantAnnotation\":{\"position\":22},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000551355\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"2KB_downstream_gene_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000552973\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 32893271, "A", "G"),
                        new QueryOptions());  // should set functional description "In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046" for ENST00000380152
        assertObjectListEquals("[{\"geneName\":\"ZAR1L\",\"ensemblGeneId\":\"ENSG00000189167\",\"ensemblTranscriptId\":\"ENST00000533490\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000380152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":358,\"cdsPosition\":125,\"codon\":\"tAt/tGt\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P51587\",\"position\":42,\"reference\":\"TYR\",\"alternate\":\"CYS\",\"uniprotVariantId\":\"VAR_020705\",\"functionalDescription\":\"In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046.\",\"substitutionScores\":[{\"score\":0.11,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.032,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Cell cycle\",\"Complete proteome\",\"Cytoplasm\",\"Cytoskeleton\",\"Disease mutation\",\"DNA damage\",\"DNA recombination\",\"DNA repair\",\"DNA-binding\",\"Fanconi anemia\",\"Nucleus\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Tumor suppressor\",\"Ubl conjugation\"],\"features\":[{\"id\":\"IPR015525\",\"start\":2,\"end\":1709,\"description\":\"Breast cancer type 2 susceptibility protein\"},{\"id\":\"IPR015525\",\"start\":1,\"end\":3418,\"type\":\"chain\",\"description\":\"Breast cancer type 2 susceptibility protein\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000544455\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":352,\"cdsPosition\":125,\"codon\":\"tAt/tGt\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P51587\",\"position\":42,\"reference\":\"TYR\",\"alternate\":\"CYS\",\"uniprotVariantId\":\"VAR_020705\",\"functionalDescription\":\"In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046.\",\"substitutionScores\":[{\"score\":0.11,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.032,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Cell cycle\",\"Complete proteome\",\"Cytoplasm\",\"Cytoskeleton\",\"Disease mutation\",\"DNA damage\",\"DNA recombination\",\"DNA repair\",\"DNA-binding\",\"Fanconi anemia\",\"Nucleus\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Tumor suppressor\",\"Ubl conjugation\"],\"features\":[{\"id\":\"IPR015525\",\"start\":2,\"end\":1709,\"description\":\"Breast cancer type 2 susceptibility protein\"},{\"id\":\"IPR015525\",\"start\":1,\"end\":3418,\"type\":\"chain\",\"description\":\"Breast cancer type 2 susceptibility protein\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000530893\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":323,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001623\",\"name\":\"5_prime_UTR_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"TOMM40\",\"ensemblGeneId\":\"ENSG00000130204\",\"ensemblTranscriptId\":\"ENST00000252487\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"geneName\":\"TOMM40\",\"ensemblGeneId\":\"ENSG00000130204\",\"ensemblTranscriptId\":\"ENST00000592434\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000252486\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":499,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P02649\",\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"uniprotVariantId\":\"VAR_000652\",\"functionalDescription\":\"In HLPP3; form E3**, form E4, form E4/3 and some forms E5-type; only form E3** is disease-linked; dbSNP:rs429358.\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Alzheimer disease\",\"Amyloidosis\",\"Cholesterol metabolism\",\"Chylomicron\",\"Complete proteome\",\"Direct protein sequencing\",\"Disease mutation\",\"Glycation\",\"Glycoprotein\",\"HDL\",\"Heparin-binding\",\"Hyperlipidemia\",\"Lipid metabolism\",\"Lipid transport\",\"Neurodegeneration\",\"Oxidation\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Secreted\",\"Signal\",\"Steroid metabolism\",\"Sterol metabolism\",\"Transport\",\"VLDL\"],\"features\":[{\"start\":106,\"end\":141,\"type\":\"helix\"},{\"start\":80,\"end\":255,\"type\":\"region of interest\",\"description\":\"8 X 22 AA approximate tandem repeats\"},{\"start\":124,\"end\":145,\"type\":\"repeat\",\"description\":\"3\"},{\"id\":\"IPR000074\",\"start\":81,\"end\":292,\"description\":\"Apolipoprotein A/E\"},{\"id\":\"PRO_0000001987\",\"start\":19,\"end\":317,\"type\":\"chain\",\"description\":\"Apolipoprotein E\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000446996\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":477,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000485628\",\"strand\":\"+\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"2KB_downstream_gene_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000434152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":523,\"cdsPosition\":466,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":156,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":0.91,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000425718\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":653,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("17", 52, "C", "A"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"AC108004.5\",\"ensemblGeneId\":\"ENSG00000273288\",\"ensemblTranscriptId\":\"ENST00000583926\",\"strand\":\"-\",\"biotype\":\"miRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
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
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22274249, "-", "AGGAG"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 51042514, "-", "G"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587846, "-", "CT"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 42537628, "T", "C"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 27283340, "-", "C"), new QueryOptions());  // should return splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 31478142, "-", "G"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 29684676, "G", "A"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 40806293, "-", "TGTG"), new QueryOptions());  // should return downstream_gene_variant
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
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16676212, "C", "T"), new QueryOptions());  // should include downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22022872, "T", "C"), new QueryOptions());  // should not raise an error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 179633644, "G", "C"), new QueryOptions());  // should include
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16123409, "-", "A"), new QueryOptions());  // should include
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 51234118, "C", "G"), new QueryOptions());  // should include upstream_gene_variant
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

    private <T> void assertObjectListEquals(String consequenceTypeJson, List<T> list,
                                            Class<T> clazz) {
        List goldenObjectList = jsonObjectMapper.convertValue(JSON.parse(consequenceTypeJson), List.class);
        assertEquals(goldenObjectList.size(), list.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), jsonObjectMapper.convertValue(goldenObjectList.get(i), clazz));
        }
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
                        if (soTerm.getName().equals("2KB_upstream_gene_variant")) {
                            SoNameToTest = "upstream_gene_variant";
                        } else if (soTerm.getName().equals("2KB_downstream_gene_variant")) {
                            SoNameToTest = "downstream_gene_variant";
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
