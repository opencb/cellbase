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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.formats.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class VariantAnnotationMongoDBAdaptorTest {

    @Ignore
    @Test
    public void testGetAnnotationByVariantList() throws Exception {

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();

        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("hsapiens", "GRCh37");

        List<VariantAnnotation> variantAnnotationList = new ArrayList<>();

        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("19", 45411941, "T", "C"))  // Should return any result
                , new QueryOptions()).get(0).getResult()).get(0));
        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("22", 16050612, "C", "G"))  // Should return any result
                , new QueryOptions()).get(0).getResult()).get(0));

        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("13", 45411941, "T", "C"))  // Should return any result
                , new QueryOptions()).get(0).getResult()).get(0));
        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("21", 18992155, "T", "C"))  // Should return any result
                , new QueryOptions()).get(0).getResult()).get(0));
        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("2", 130498751, "A", "G"))  // Should return any result
                , new QueryOptions()).get(0).getResult()).get(0));
        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("19", 45411941, "T", "C"))  // Should return any result
                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("22", 21982892, "C", "T"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("22", 21982892, "C", "G"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("10", 78444456, "G", "T"))  // Should include population frequencies
//                , new QueryOptions());
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("22", 22022872, "T", "C"))  // Should not raise java.lang.NullPointerException
//                , new QueryOptions());
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("22", 16123409, "-", "A"))
//                , new QueryOptions());

        VepFormatWriter vepFormatWriter = new VepFormatWriter("/tmp/test.vep");
        vepFormatWriter.open();
        vepFormatWriter.pre();
        vepFormatWriter.write(variantAnnotationList);
        vepFormatWriter.post();
        vepFormatWriter.close();



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

//    @Ignore
    @Test
    public void testGetAllConsequenceTypesByVariant() throws IOException, URISyntaxException {

//        URI uri = new URI("http", null, "172.22.68.41", 8080, "/cellbase/webservices/rest/", null, null);
//        UriBuilder uriBuilder = UriBuilder.fromUri(uri).path("v3").path("hsapiens").path("genomic").path("variant").path("full_annotation");
//        ClientConfig clientConfig = new ClientConfig();
//        Client client = ClientBuilder.newClient(clientConfig);
//        Invocation.Builder request = client.target(uriBuilder).request();
//        Response response = request.post(Entity.entity("19:45411941:T:C", "text/plain"));
//        String responseStr = response.readEntity(String.class);
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectReader objectReader = objectMapper.reader(objectMapper.getTypeFactory().constructParametricType(
//                QueryResponse.class, objectMapper.getTypeFactory().constructParametricType(org.opencb.datastore.core.QueryResult.class, VariantAnnotation.class)));
//        objectReader.readValue(response);
//        String result = objectMapper.writeValueAsString(response);
//        int a = 1;

//        CellbaseConfiguration config = new CellbaseConfiguration();
        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();

        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        config.addSpeciesAlias("agambiae", "agambiae");
//        config.addSpeciesAlias("hsapiens", "hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("hsapiens", "GRCh37");
//        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("agambiae", "GRCh37");

        String line = null;


        // Use ebi cellbase to test these
        // TODO: check differences against Web VEP
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16287365, "C", "T"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("19", 45411941, "T", "C"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("5", 150407694, "G", "A"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("19", 20047783, "AAAAAA", "-"), new QueryOptions());  // should
        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("13", 28942717, "NNN", "-"), new QueryOptions());  // should return ENST00000541932 stop_retained
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("13", 45411941, "T", "C"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("9", 107366952, StringUtils.repeat("N",12577), "A"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("7", 23775220, "T", "A"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("5", 150407694, "G", "A"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("5", 150407693, "T", "G"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("4", 48896023, "G", "C"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 12837706, "-", "CC"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("19", 20047783, "-", "AAAAAA"), new QueryOptions());  // should return stop_gained
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 115828861, "C", "G"), new QueryOptions());  // should return stop_lost
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("16", 32859177, "C", "T"), new QueryOptions());  // should return stop_lost
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 13481174, "NN", "-"), new QueryOptions());  // should return stop_lost
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 153600596, "-", "C"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 10041199, "A", "T"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 102269845, "C", "A"), new QueryOptions());  // should
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("7", 158384306, "TGTG", "-"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("11", 118898436, "N", "-"), new QueryOptions());  // should return intergenic_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 6638139, "-", "T"), new QueryOptions());  // should return intergenic_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 70612070, StringUtils.repeat("N",11725), "-"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 36587846, "-", "CT"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("13", 52718051, "-", "T"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 115412783, "-", "C"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 27793991, StringUtils.repeat("N",1907), "-"), new QueryOptions());  // should not return null
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 27436462, StringUtils.repeat("N",2), "-"), new QueryOptions());  // should not return intergenic_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("10", 6638139, "A", "T"), new QueryOptions());  // should not return intergenic_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 3745870, "C", "T"), new QueryOptions());  // should not return null
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 35656173, "C", "A"), new QueryOptions());  // should return initiator_codon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 28071285, "C", "G"), new QueryOptions());  // should return initiator_codon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 35656173, "C", "A"), new QueryOptions());  // should return synonymous_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 22274249, "-", "AGGAG"), new QueryOptions());  // should return downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 51042514, "-", "G"), new QueryOptions());  // should return downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 36587846, "-", "CT"), new QueryOptions());  // should
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 42537628, "T", "C"), new QueryOptions());  // should return downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 27283340, "-", "C"), new QueryOptions());  // should return splice_region_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 31478142, "-", "G"), new QueryOptions());  // should return downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 29684676, "G", "A"), new QueryOptions());  // should return downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 40806293, "-", "TGTG"), new QueryOptions());  // should return downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 39426437, StringUtils.repeat("N",20092), "-"), new QueryOptions());  // ¿should return 3_prime_UTR_variant? No if ENSEMBLs gtf was used
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 38069602, StringUtils.repeat("N",5799), "-"), new QueryOptions());  // should return 3_prime_UTR_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17054103, "A", "G"), new QueryOptions());  // should NOT return non_coding_transcript_exon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 35661560, "A", "G"), new QueryOptions());  // should return synonymous_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 31368158, StringUtils.repeat("N",4), "-"), new QueryOptions());  // should return donor_variant, intron_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 36587124, "-", "TA"), new QueryOptions());  // should return stop_retained_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 30824659, "-", "A"), new QueryOptions());  // should return stop_retained_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 26951215, "T", "C"), new QueryOptions());  // should NOT return null pointer exception
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17602839, "G", "A"), new QueryOptions());  // should NOT return null pointer exception
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 20891503, "-", "CCTC"), new QueryOptions());  // should return missense_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 21991357, "T", "C"), new QueryOptions());  // should return missense_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 24717655, "C", "T"), new QueryOptions());  // should return missense_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 24314402, StringUtils.repeat("N",19399), "-"), new QueryOptions());  // should return 3prime_UTR_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 22007661, "G", "A"), new QueryOptions());  // should
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 23476261, "G", "A"), new QueryOptions());  // should
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 22517056, StringUtils.repeat("N",82585), "-"), new QueryOptions());  // should return 3prime_UTR_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 21388510, "A", "T"), new QueryOptions());  // should NOT return mature_miRNA_variant but non_coding_transcript_variant,non_coding_transcript_exon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 22007634, "G", "A"), new QueryOptions());  // should NOT return mature_miRNA_variant but non_coding_transcript_variant,non_coding_transcript_exon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 20891502, "-", "CCTC"), new QueryOptions());  // should return splice_region_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 18387495, "G", "A"), new QueryOptions());  // should NOT return incomplete_teminator_codon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 19258045, StringUtils.repeat("N",27376), "-"), new QueryOptions());  // should return initiator_codon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 18293502, "T", "C"), new QueryOptions());  // should return initiator_codon_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 18620375, StringUtils.repeat("N",9436), "-"), new QueryOptions());  // should return transcript_ablation
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 18997219, StringUtils.repeat("N",12521), "-"), new QueryOptions());  // should return transcript_ablation
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17449263, "G", "A"), new QueryOptions());  // should return
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 21982892, "C", "T"), new QueryOptions());  // should return a result
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16676212, "C", "T"), new QueryOptions());  // should include downstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 22022872, "T", "C"), new QueryOptions());  // should not raise an error
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("2", 179633644, "G", "C"), new QueryOptions());  // should include
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16123409, "-", "A"), new QueryOptions());  // should include
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 51234118, "C", "G"), new QueryOptions());  // should include upstream_gene_variant
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 155159745, "G", "A"), new QueryOptions());  // should not raise error
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("2", 179621477, "C", "T"), new QueryOptions());  // should not raise error
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 20918922, "C", "T"), new QueryOptions());  // should not raise java.lang.StringIndexOutOfBoundsException
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 18628756, "A", "T"), new QueryOptions());  // should not raise java.lang.NumberFormatException
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17488995, "G", "A"), new QueryOptions());  // should not raise java.lang.NumberFormatException
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17280889, "G", "A"), new QueryOptions());  // should not raise java.lang.NumberFormatException
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16449075, "G", "A"), new QueryOptions());  // should not raise null exception
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16287784, "C", "T"), new QueryOptions());  // should not raise null exception
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16287365, "C", "T"), new QueryOptions());  // should not raise null exception
//          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17468875, "C", "A"), new QueryOptions());  // missense_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17451081, "C", "T"), new QueryOptions());  // should not include stop_reained_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17468875, "C", "T"), new QueryOptions());  // synonymous_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17449263, "G", "A"), new QueryOptions());  // should not include stop_reained_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17449238, "T", "C"), new QueryOptions());  // should not include stop_codon
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17071673, "A", "G"), new QueryOptions());  // 3_prime_UTR_variant
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16151191, "G", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16340551, "A", "G"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17039749, "C", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16287365, "C", "T"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16101010, "TTA", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 16062270, "G", "T"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 20918922, "C", "T"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17668822, "TCTCTACTAAAAATACAAAAAATTAGCCAGGCGTGGTGGCAGGTGCCTGTAGTACCAGCTACTTGGAAGGCTGAGGCAGGAGACTCTCTTGAACCTGGGAAGCCGAGGTTGCAGTGAGCTGGGCGACAGAGGGAGACTCCGTAAAAAAAAGAAAAAAAAAGAAGAAGAAGAAAAGAAAACAGGAAGGAAAGAAGAAAGAGAAACTAGAAATAATACATGTAAAGTGGCTGATTCTATTATCCTTGTTATTCCTTCTCCATGGGGCTGTTGTCAGGATTAAGTGAGATAGAGCACAGGAAAGGGCTCTGGAAACGCCTGTAGGCTCTAACCCTGAGGCATGGGCCTGTGGCCAGGAGCTCTCCCATTGACCACCTCCGCTGCCTCTGCTCGCATCCCGCAGGCTCACCTGTTTCTCCGGCGTGGAAGAAGTAAGGCAGCTTAACGCCATCCTTGGCGGGGATCATCAGAGCTTCCTTGTAGTCATGCAAGGAGTGGCCAGTGTCCTCATGCCCCACCTGCAGGACAGAGAGGGACAGGGAGGTGTCTGCAGGGCGCATGCCTCACTTGCTGATGGCGCGCCCTGGAGCCTGTGCACACCCTTCCTTGTACCCTGCCACCACTGCCGGGACCTTTGTCACACAGCCTTTTAAGAATGACCAGGAGCAGGCCAGGCGTGGTGGCTCACACCTGTAATCCCAGCACTTTGGGAGGCCGAGGCAGGCAGATCACGAAGTCAGGAGATCGAGACCATCCTGGCTAACACAGTGAAACCCCA", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 17668818, "C", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("8", 408515, "GAA", ""), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("3", 367747, "C", "T"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("9", 214512, "C", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("14", 19108198, "-", "GGTCTAGCATG"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("3L", 22024723, "G", "T"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("2L", 37541199, "G", "A"), new QueryOptions());
//
//        // Use local gene collection to test these
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 5, "GGTCTAGCATG", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 1, "G", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 5, "GGTCTAGCATGTTACATGAAG", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 15, "GTTACATGAAG", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 21, "T", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 34, "-", "AAAT"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 42, "G", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 75, "T", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 75, "TCTAAGGCCTC", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 25, "GATAGTTCCTA", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 45, "GATAGGGTAC", "-"), new QueryOptions());


//        try {
//            br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        /**
         * Calculates annotation for vcf file variants, loads vep annotations, compares batches and writes results
         */
        String DIROUT = "/home/fjlopez/tmp/";
//        String DIROUT = "/homes/fjlopez/tmp/";
        List<String> VCFS = new ArrayList<>();
        VCFS.add("/tmp/test.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr10.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr11.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr12.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr13.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr14.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr15.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr16.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr17.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr18.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr19.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr1.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr20.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr21.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr2.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr3.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr4.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr5.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr6.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr7.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr8.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr9.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chrX.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");

        List<String> VEPFILENAMES = new ArrayList<>();
        VEPFILENAMES.add("/tmp/test.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr10.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr11.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr12.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr13.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr14.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr15.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr16.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr17.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr18.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr19.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr1.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr20.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr21.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr2.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr3.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr4.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr5.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr6.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr7.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr8.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr9.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//        VEPFILENAMES.add("/nfs/production2/eva/VEP/eva_output/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chrX.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");



        Set<AnnotationComparisonObject> uvaAnnotationSet = new HashSet<>();
        Set<AnnotationComparisonObject> vepAnnotationSet = new HashSet<>();
        int vepFileIndex = 0;
        int nNonRegulatoryAnnotations = 0;
        int nVariants = 0;
        for (String vcfFilename : VCFS) {
            System.out.println("Processing "+vcfFilename+" lines...");
            VcfRawReader vcfReader = new VcfRawReader(vcfFilename);
            File file = new File(VEPFILENAMES.get(vepFileIndex));
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            if (vcfReader.open()) {
                vcfReader.pre();
                skipVepFileHeader(raf);
                int nLines = countLines(vcfFilename);
                int nReadVariants;
                int lineCounter=0;
                do {
                    nReadVariants = getVcfAnnotationBatch(vcfReader, variantAnnotationDBAdaptor, uvaAnnotationSet);
                    nNonRegulatoryAnnotations += getVepAnnotationBatch(raf, nReadVariants, vepAnnotationSet);
                    nVariants += nReadVariants;
                    compareAndWrite(uvaAnnotationSet, vepAnnotationSet, lineCounter, nLines, nNonRegulatoryAnnotations,
                            nVariants, DIROUT);
                    lineCounter += nReadVariants;
                    System.out.print(lineCounter+"/"+nLines+" - non-regulatory annotations: "+nNonRegulatoryAnnotations+"\r");
                } while (nReadVariants > 0);
                vcfReader.post();
                vcfReader.close();
                raf.close();
            }
            vepFileIndex++;
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

    private int getVcfAnnotationBatch(VcfRawReader vcfReader, VariantAnnotationDBAdaptor variantAnnotationDBAdaptor,
                                      Set<AnnotationComparisonObject> uvaAnnotationSet) {
        QueryResult queryResult = null;
        String pos;
        String ref;
        String alt;
        String SoNameToTest;

        List<VcfRecord> vcfRecordList = vcfReader.read(1000);
        int ensemblPos;

        for (VcfRecord vcfRecord : vcfRecordList) {
            // Short deletion
            if (vcfRecord.getReference().length() > 1) {
                ref = vcfRecord.getReference().substring(1);
                alt = "-";
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
                    alt = "-";
                    // Short insertion
                } else {
                    ensemblPos = vcfRecord.getPosition() + 1;
                    ref = "-";
                    alt = vcfRecord.getAlternate().substring(1);
                    pos = vcfRecord.getPosition() + "-" + (vcfRecord.getPosition() + 1);
                }
                // SNV
            } else {
                ref = vcfRecord.getReference();
                alt = vcfRecord.getAlternate();
                ensemblPos = vcfRecord.getPosition();
                pos = Integer.toString(ensemblPos);
            }
            try {
                queryResult = variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant(vcfRecord.getChromosome(), ensemblPos,
                        ref, alt), new QueryOptions());
            } catch (Exception e) {
                System.out.println("new GenomicVariant = " + new GenomicVariant(vcfRecord.getChromosome(), ensemblPos, ref, alt));
                System.exit(1);
            }

            int i;
            List<ConsequenceType> consequenceTypeList = (List<ConsequenceType>) queryResult.getResult();
            for (i = 0; i < consequenceTypeList.size(); i++) {
                for (ConsequenceType.ConsequenceTypeEntry soTerm : consequenceTypeList.get(i).getSoTerms()) {
                    if (soTerm.getSoName().equals("2KB_upstream_gene_variant")) {
                        SoNameToTest = "upstream_gene_variant";
                    } else if (soTerm.getSoName().equals("2KB_downstream_gene_variant")) {
                        SoNameToTest = "downstream_gene_variant";
                    } else {
                        SoNameToTest = soTerm.getSoName();
                    }
                    uvaAnnotationSet.add(new AnnotationComparisonObject(vcfRecord.getChromosome(), pos, alt,
                            consequenceTypeList.get(i).getEnsemblGeneId() == null ? "-" : consequenceTypeList.get(i).getEnsemblGeneId(),
                            consequenceTypeList.get(i).getEnsemblTranscriptId() == null ? "-" : consequenceTypeList.get(i).getEnsemblTranscriptId(),
                            consequenceTypeList.get(i).getBiotype() == null ? "-" : consequenceTypeList.get(i).getBiotype(),
                            SoNameToTest));
                }
            }
        }
        return vcfRecordList.size();
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
            bw.write(comparisonObject.toString());
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
