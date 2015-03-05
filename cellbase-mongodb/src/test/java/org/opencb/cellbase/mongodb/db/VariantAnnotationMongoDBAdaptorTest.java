package org.opencb.cellbase.mongodb.db;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
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

        CellbaseConfiguration config = new CellbaseConfiguration();

        config.addSpeciesConnection("hsapiens", "GRCh37", "mongodb-hxvm-var-001", "cellbase_hsapiens_grch37_v3", 27017, "mongo", "biouser",
                "B10p@ss", 10, 10);

        config.addSpeciesAlias("hsapiens", "hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("hsapiens", "GRCh37");

//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("21", 18992155,"T","C"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("2", 130498751,"A","G"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationDBAdaptor.getAnnotationByVariantList(Collections.singletonList(new GenomicVariant("19", 45411941,"T","C"))  // Should return any result
//                , new QueryOptions());
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

        public AnnotationComparisonObject(String chr, String pos, String alt, String ensemblGeneId, String SOname) {
            this(chr, pos, alt, ensemblGeneId, "-", "-", SOname);
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

//            if (SOname != null) {
//                if(!SOname.equals(that.SOname) && !((SOname.equals("2KB_upstream_gene_variant") && that.SOname.equals("upstream_gene_variant")) ||
//                        (SOname.equals("2KB_downstream_gene_variant") && that.SOname.equals("downstream_gene_variant")) ||
//                        (SOname.equals("upstream_gene_variant") && that.SOname.equals("2KB_upstream_gene_variant")) ||
//                        (SOname.equals("downstream_gene_variant") && that.SOname.equals("2KB_downstream_gene_variant")) ||
//                        (SOname.equals("non_coding_transcript_variant") && that.SOname.equals("nc_transcript_variant")) ||
//                        (SOname.equals("nc_transcript_variant") && that.SOname.equals("non_coding_transcript_variant")))) {
//                    return false;
//                }
//            } else if (that.SOname != null) {
//                return false;
//            }

            if (this == o) return true;
            if (!(o instanceof AnnotationComparisonObject)) return false;

            AnnotationComparisonObject that = (AnnotationComparisonObject) o;

            if (SOname != null ? !SOname.equals(that.SOname) : that.SOname != null) return false;
            if (alt != null ? !alt.equals(that.alt) : that.alt != null) return false;
            if (chr != null ? !chr.equals(that.chr) : that.chr != null) return false;
            if (ensemblGeneId != null ? !ensemblGeneId.equals(that.ensemblGeneId) : that.ensemblGeneId != null)
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

    @Ignore
    @Test
    public void testGetAllConsequenceTypesByVariant() throws IOException {

        CellbaseConfiguration config = new CellbaseConfiguration();

        config.addSpeciesConnection("hsapiens", "GRCh37", "mongodb-hxvm-var-001", "cellbase_hsapiens_grch37_v3", 27017, "mongo", "biouser",
                "B10p@ss", 10, 10);

//        config.addSpeciesConnection("agambiae", "GRCh37", "mongodb-hxvm-var-001", "cellbase_agambiae_agamp4_v3", 27017, "mongo", "biouser",
//                "B10p@ss", 10, 10);

//        config.addSpeciesConnection("hsapiens", "GRCh37", "localhost", "test", 27017, "mongo", "", "", 10, 10);

//        config.addSpeciesAlias("agambiae", "agambiae");
        config.addSpeciesAlias("hsapiens", "hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("hsapiens", "GRCh37");
//        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getVariantAnnotationDBAdaptor("agambiae", "GRCh37");

        String line = null;


        // Use ebi cellbase to test these
        // TODO: check differences against Web VEP
          variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("22", 18997219, StringUtils.repeat("N",12521), "-"), new QueryOptions());  // should return
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
         * Calculates annotation for vcf file variants
         */
        String INPUTFILE = "/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.test.vcf";
//        String INPUTFILE = "/home/fjlopez/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf";
        QueryResult queryResult = null;
        Set<AnnotationComparisonObject> uvaAnnotationSet = new HashSet<>();
        VcfRawReader vcfReader = new VcfRawReader(INPUTFILE);
        String pos;
        String ref;
        String alt;
        String SoNameToTest;

        if(vcfReader.open()) {
            vcfReader.pre();
            List<VcfRecord> vcfRecordList= vcfReader.read(1000);
            int nLines = countLines(INPUTFILE);
            int lineCounter = 0;
            int TESTSIZE = 1000;
            int ensemblPos;
            System.out.println("Processing vcf lines...");
            while(vcfRecordList.size()>0) {
//            while(vcfRecordList.size()>0 && lineCounter<TESTSIZE) {
                for(VcfRecord vcfRecord : vcfRecordList) {
                    // Short deletion
                    if(vcfRecord.getReference().length()>1) {
                        ref = vcfRecord.getReference().substring(1);
                        alt = "-";
                        ensemblPos = vcfRecord.getPosition()+1;
                        if(ref.length()>1) {
                            pos = (vcfRecord.getPosition() + 1) + "-" + (vcfRecord.getPosition() + ref.length());
                        } else {
                            pos = Integer.toString(vcfRecord.getPosition() + 1);
                        }
                    // Alternate length may be > 1 if it contains <DEL>
                    } else if(vcfRecord.getAlternate().length()>1) {
                        ensemblPos = vcfRecord.getPosition() + 1;
                        // Large deletion
                        if(vcfRecord.getAlternate().equals("<DEL>")) {
                            String[] infoFields = vcfRecord.getInfo().split(";");
                            int i = 0;
                            while(i<infoFields.length && !infoFields[i].startsWith("END=")) {
                                i++;
                            }
                            int end = Integer.parseInt(infoFields[i].split("=")[1]);
                            pos = (vcfRecord.getPosition()+1) + "-" + end;
                            ref = StringUtils.repeat("N",end-vcfRecord.getPosition());
                            alt = "-";
                        // Short insertion
                        } else {
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
                        System.out.println("new GenomicVariant = " + new GenomicVariant(vcfRecord.getChromosome(), vcfRecord.getPosition(),ref, alt));
                        System.exit(1);
                    }

                    int i;
                    List<ConsequenceType> consequenceTypeList = (List<ConsequenceType>) queryResult.getResult();
                    for(i=0; i < consequenceTypeList.size(); i++) {
                        if(consequenceTypeList.get(i).getEnsemblGeneId()==null?false:consequenceTypeList.get(i).getEnsemblGeneId().equals("ENSG00000236235")) {
                            int a;
                            a=1;
                        }
                        for(ConsequenceType.ConsequenceTypeEntry soTerm : consequenceTypeList.get(i).getSoTerms()) {
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
                vcfRecordList = vcfReader.read(1000);
                lineCounter += 1000;
                System.out.print(lineCounter+"/"+nLines+"\r");
            }
        }

        vcfReader.post();
        vcfReader.close();

//        System.exit(0);

        /**
         * Loads VEP annotation from VEP parsed annotations
         */
        BufferedReader br = Files.newBufferedReader(Paths.get("/tmp/22.vep.output.parsed.test.txt"), Charset.defaultCharset());
//        BufferedReader br = Files.newBufferedReader(Paths.get("/home/fjlopez/tmp/22.vep.output.parsed.txt"), Charset.defaultCharset());
        Set<AnnotationComparisonObject> vepAnnotationSet = new HashSet<>();
        String newLine;
        br.readLine();
        while((newLine=br.readLine())!=null) {
            String[] lineFields = newLine.split("\t");
            for(String SOname : lineFields[7].split(",")) {
                if(SOname.equals("nc_transcript_variant")) {
                    SOname = "non_coding_transcript_variant";
                }
                if(lineFields[2].equals("deletion")) {
                    alt = "-";
                } else {
                    alt = lineFields[2];
                }
                vepAnnotationSet.add(new AnnotationComparisonObject(lineFields[0], lineFields[1], alt, lineFields[3], SOname));
            }
        }

        /**
         * Compare both annotation sets and get UVA specific annotations
         */
        BufferedWriter bw = Files.newBufferedWriter(Paths.get("/home/fjlopez/tmp/22.uva.specific.txt"), Charset.defaultCharset());
        bw.write("#CHR\tPOS\tALT\tENSG\tENST\tBIOTYPE\tCT\n");
        Set<AnnotationComparisonObject> uvaSpecificAnnotationSet = new HashSet<>(uvaAnnotationSet);
        uvaSpecificAnnotationSet.removeAll(vepAnnotationSet);
        List<AnnotationComparisonObject> uvaSpecificAnnotationList = new ArrayList(uvaSpecificAnnotationSet);
        Collections.sort(uvaSpecificAnnotationList, new AnnotationComparisonObjectComparator());
        for(AnnotationComparisonObject comparisonObject : uvaSpecificAnnotationList) {
            bw.write(comparisonObject.toString());
        }
        bw.close();

        /**
         * Compare both annotation sets and get VEP specific annotations
         */
        bw = Files.newBufferedWriter(Paths.get("/home/fjlopez/tmp/22.vep.specific.txt"), Charset.defaultCharset());
        bw.write("#CHR\tPOS\tALT\tENSG\tENST\tBIOTYPE\tCT\n");
        Set<AnnotationComparisonObject> vepSpecificAnnotationSet = new HashSet<>(vepAnnotationSet);
        vepSpecificAnnotationSet.removeAll(uvaAnnotationSet);
        List<AnnotationComparisonObject> vepSpecificAnnotationList = new ArrayList<>(vepSpecificAnnotationSet);
        Collections.sort(vepSpecificAnnotationList, new AnnotationComparisonObjectComparator());
        for(AnnotationComparisonObject comparisonObject : vepSpecificAnnotationList) {
            bw.write(comparisonObject.toString());
        }
        bw.close();
    }

//    private void writeLine(BufferedWriter bw, String pos, String alt, VcfRecord vcfRecord, ConsequenceType consequenceType, String SOnames) throws IOException {
//        String feaType;
//        String strand;
//        String cDnaPosition;
//        String cdsPosition;
//        String aPosition;
//        String aChange;
//        String codon;
//        switch (consequenceType.getSoName()) {
//            case "TF_binding_site_variant":
//                feaType = "MotifFeature";
//                strand = "-";
//                cDnaPosition = "-";
//                cdsPosition = "-";
//                aPosition = "-";
//                aChange = "-";
//                codon = "-";
//                break;
//            case "regulatory_region_variant":
//                feaType = "RegulatoryFeature";
//                strand = "-";
//                cDnaPosition = "-";
//                cdsPosition = "-";
//                aPosition = "-";
//                aChange = "-";
//                codon = "-";
//                break;
//            case "intergenic_variant":
//                feaType = "-";
//                strand = "-";
//                cDnaPosition = "-";
//                cdsPosition = "-";
//                aPosition = "-";
//                aChange = "-";
//                codon = "-";
//                break;
//            default:
//                feaType = "Transcript";
//                if(consequenceType.getStrand().equals("+")) {
//                    strand = "1";
//                } else {
//                    strand = "-1";
//                }
//                if(consequenceType.getcDnaPosition() == null) {
//                    cDnaPosition = "-";
//                } else {
//                    cDnaPosition = Integer.toString(consequenceType.getcDnaPosition());
//                }
//                if(consequenceType.getCdsPosition() == null) {
//                    cdsPosition = "-";
//                } else {
//                    cdsPosition = Integer.toString(consequenceType.getCdsPosition());
//                }
//                if(consequenceType.getAaPosition() == null) {
//                    aPosition = "-";
//                } else {
//                    aPosition = Integer.toString(consequenceType.getAaPosition());
//                }
//                if(consequenceType.getAaChange() == null) {
//                    aChange = "-";
//                } else {
//                    aChange = consequenceType.getAaChange();
//                }
//                if(consequenceType.getCodon() == null) {
//                    codon = "-";
//                } else {
//                    codon = consequenceType.getCodon();
//                }
//
//        }
//        bw.write(vcfRecord.getChromosome()+"\t"+pos+"\t"+alt+"\t"+
//                consequenceType.getEnsemblGeneId("-")+"\t"+feaType+"\t"+consequenceType.getBiotype("-")+"\t"+
//                strand+"\t"+SOnames+"\t"+cDnaPosition+"\t"+
//                cdsPosition+"\t"+aPosition+"\t"+aChange+"\t"+codon+"\n");
//    }
}