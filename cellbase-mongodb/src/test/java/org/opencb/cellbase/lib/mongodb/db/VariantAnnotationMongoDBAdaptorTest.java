package org.opencb.cellbase.lib.mongodb.db;


import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VariantVcfReader;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class VariantAnnotationMongoDBAdaptorTest {


    private int countLines(String fileName) throws IOException {
        System.out.println("Counting lines...");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();

        return lines;
    }

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

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getGenomicVariantAnnotationDBAdaptor("hsapiens", "GRCh37");
//        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getGenomicVariantAnnotationDBAdaptor("agambiae", "GRCh37");

        String line = null;

        String INPUTFILE = "/home/fjlopez/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf";

        QueryResult queryResult = null;
        BufferedWriter bw = Files.newBufferedWriter(Paths.get("/home/fjlopez/tmp/22.uva.vcf"), Charset.defaultCharset());

        // Use ebi cellbase to test these
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

        bw.write("#CHR\tPOS\tALT\tENSG\tFEA_TYPE\tBIOTYPE\tSTRAND\tCT\tCDNA\tCDS\tA_POS\tA_CHANGE\tCODON\n");

        VcfRawReader vcfReader = new VcfRawReader(INPUTFILE);
        if(vcfReader.open()) {

            List<VcfRecord> vcfRecordList= vcfReader.read(1000);
            int nLines = countLines(INPUTFILE);
            int lineCounter = 0;
            String ref;
            String alt;
            System.out.println("Processing vcf lines...");
            while(vcfRecordList.size()>0) {
                for(VcfRecord vcfRecord : vcfRecordList) {
                    if(vcfRecord.getReference().length()>1) {
                        ref = vcfRecord.getReference().substring(1);
                        alt = "-";
                    } else if(vcfRecord.getAlternate().length()>1) {
                        ref = "-";
                        alt = vcfRecord.getAlternate().substring(1);
                    } else {
                        ref = vcfRecord.getReference();
                        alt = vcfRecord.getAlternate();
                    }
//                    System.out.println("new GenomicVariant = " + new GenomicVariant(vcfRecord.getChromosome(), vcfRecord.getPosition(),ref, alt));
                    queryResult = variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant(vcfRecord.getChromosome(), vcfRecord.getPosition(),
                            ref, alt), new QueryOptions());

                    for(ConsequenceType consequenceType : (List<ConsequenceType>) queryResult.getResult()) {
                        String pos;
                        if(vcfRecord.getReference().length()>1) {
                            if(consequenceType.getStrand()!=null && consequenceType.getStrand().equals("+")) {
                                pos = vcfRecord.getPosition() + "-" + (vcfRecord.getPosition() + vcfRecord.getReference().length() - 1);
                            } else {
                                pos = (vcfRecord.getPosition() - vcfRecord.getReference().length() + 1)+"-"+vcfRecord.getPosition();
                            }
                        } else {
                            pos = Integer.toString(vcfRecord.getPosition());
                        }

                        String feaType;
                        String strand;
                        String cDnaPosition;
                        String cdsPosition;
                        String aPosition;
                        String aChange;
                        String codon;
                        switch (consequenceType.getSOName()) {
                            case "TF_binding_site_variant":
                                feaType = "MotifFeature";
                                strand = "-";
                                cDnaPosition = "-";
                                cdsPosition = "-";
                                aPosition = "-";
                                aChange = "-";
                                codon = "-";
                                break;
                            case "regulatory_region_variant":
                                feaType = "RegulatoryFeature";
                                strand = "-";
                                cDnaPosition = "-";
                                cdsPosition = "-";
                                aPosition = "-";
                                aChange = "-";
                                codon = "-";
                                break;
                            case "intergenic_variant":
                                feaType = "-";
                                strand = "-";
                                cDnaPosition = "-";
                                cdsPosition = "-";
                                aPosition = "-";
                                aChange = "-";
                                codon = "-";
                                break;
                            default:
                                feaType = "Transcript";
                                if(consequenceType.getStrand().equals("+")) {
                                    strand = "1";
                                } else {
                                    strand = "-1";
                                }
                                if(consequenceType.getcDnaPosition() == null) {
                                    cDnaPosition = "-";
                                } else {
                                    cDnaPosition = Integer.toString(consequenceType.getcDnaPosition());
                                }
                                if(consequenceType.getCdsPosition() == null) {
                                    cdsPosition = "-";
                                } else {
                                    cdsPosition = Integer.toString(consequenceType.getCdsPosition());
                                }
                                if(consequenceType.getaPosition() == null) {
                                    aPosition = "-";
                                } else {
                                    aPosition = Integer.toString(consequenceType.getaPosition());
                                }
                                if(consequenceType.getaChange() == null) {
                                    aChange = "-";
                                } else {
                                    aChange = consequenceType.getaChange();
                                }
                                if(consequenceType.getCodon() == null) {
                                    codon = "-";
                                } else {
                                    codon = consequenceType.getCodon();
                                }

                        }
                        bw.write(vcfRecord.getChromosome()+"\t"+pos+"\t"+vcfRecord.getAlternate()+"\t"+
                                consequenceType.getEnsemblGeneId("-")+"\t"+feaType+"\t"+consequenceType.getBiotype("-")+"\t"+
                                strand+"\t"+consequenceType.getSOName()+"\t"+cDnaPosition+"\t"+
                                cdsPosition+"\t"+aPosition+"\t"+aChange+"\t"+codon+"\n");
                    }
                }
                vcfRecordList = vcfReader.read(1000);
                lineCounter += 1000;
                System.out.print(lineCounter+"/"+nLines+"\r");
            }
        }
    }
}