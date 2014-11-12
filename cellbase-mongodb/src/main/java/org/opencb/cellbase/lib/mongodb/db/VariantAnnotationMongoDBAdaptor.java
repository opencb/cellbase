package org.opencb.cellbase.lib.mongodb.db;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.QueryBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.*;
//import java.util.logging.Logger;

/**
 * Created by imedina on 11/07/14.
 */
public class VariantAnnotationMongoDBAdaptor extends MongoDBAdaptor implements VariantAnnotationDBAdaptor {

//    private DBCollection mongoVariationPhenotypeDBCollection;
    private int coreChunkSize = 5000;
    private static Map<String, Map<String,Boolean>> isSynonymousCodon = new HashMap<>();
    private static Map<String, List<String>> geneticCode = new HashMap<>(20);

    static {

        ///////////////////////////////////////////////////////////////////////
        /////   GENETIC CODE   ////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        geneticCode.put("ALA",new ArrayList<String>());
        geneticCode.get("ALA").add("GCT"); geneticCode.get("ALA").add("GCC"); geneticCode.get("ALA").add("GCA"); geneticCode.get("ALA").add("GCG");
        geneticCode.put("ARG",new ArrayList<String>());
        geneticCode.get("ARG").add("CGT"); geneticCode.get("ARG").add("CGC"); geneticCode.get("ARG").add("CGA"); geneticCode.get("ARG").add("CGG");
        geneticCode.get("ARG").add("AGA"); geneticCode.get("ARG").add("AGG");
        geneticCode.put("ASN", new ArrayList<String>());
        geneticCode.get("ASN").add("AAT"); geneticCode.get("ASN").add("AAC");
        geneticCode.put("ASP", new ArrayList<String>());
        geneticCode.get("ASP").add("GAT"); geneticCode.get("ASP").add("GAC");
        geneticCode.put("CYS", new ArrayList<String>());
        geneticCode.get("CYS").add("TGT"); geneticCode.get("CYS").add("TGC");
        geneticCode.put("GLN", new ArrayList<String>());
        geneticCode.get("GLN").add("CAA"); geneticCode.get("GLN").add("CAG");
        geneticCode.put("GLU", new ArrayList<String>());
        geneticCode.get("GLU").add("GAA"); geneticCode.get("GLU").add("GAG");
        geneticCode.put("GLY",new ArrayList<String>());
        geneticCode.get("GLY").add("GGT"); geneticCode.get("GLY").add("GGC"); geneticCode.get("GLY").add("GGA"); geneticCode.get("GLY").add("GGG");
        geneticCode.put("HIS",new ArrayList<String>());
        geneticCode.get("HIS").add("CAT"); geneticCode.get("HIS").add("CAC");
        geneticCode.put("ILE",new ArrayList<String>());
        geneticCode.get("ILE").add("ATT"); geneticCode.get("ILE").add("ATC"); geneticCode.get("ILE").add("ATA");
        geneticCode.put("LEU",new ArrayList<String>());
        geneticCode.get("LEU").add("TTA"); geneticCode.get("LEU").add("TTG"); geneticCode.get("LEU").add("CTT"); geneticCode.get("LEU").add("CTC");
        geneticCode.get("LEU").add("CTA"); geneticCode.get("LEU").add("CTG");
        geneticCode.put("LYS", new ArrayList<String>());
        geneticCode.get("LYS").add("AAA"); geneticCode.get("LYS").add("AAG");
        geneticCode.put("MET", new ArrayList<String>());
        geneticCode.get("MET").add("ATG");
        geneticCode.put("PHE",new ArrayList<String>());
        geneticCode.get("PHE").add("TTT"); geneticCode.get("PHE").add("TTC");
        geneticCode.put("PRO",new ArrayList<String>());
        geneticCode.get("PRO").add("CCT"); geneticCode.get("PRO").add("CCC"); geneticCode.get("PRO").add("CCA"); geneticCode.get("PRO").add("CCG");
        geneticCode.put("SER",new ArrayList<String>());
        geneticCode.get("SER").add("TCT"); geneticCode.get("SER").add("TCC"); geneticCode.get("SER").add("TCA"); geneticCode.get("SER").add("TCG");
        geneticCode.get("SER").add("AGT"); geneticCode.get("SER").add("AGC");
        geneticCode.put("THR",new ArrayList<String>());
        geneticCode.get("THR").add("ACT"); geneticCode.get("THR").add("ACC"); geneticCode.get("THR").add("ACA"); geneticCode.get("THR").add("ACG");
        geneticCode.put("TRP",new ArrayList<String>());
        geneticCode.get("TRP").add("TGG");
        geneticCode.put("TYR",new ArrayList<String>());
        geneticCode.get("TYR").add("TAT"); geneticCode.get("TYR").add("TAC");
        geneticCode.put("VAL",new ArrayList<String>());
        geneticCode.get("VAL").add("GTT"); geneticCode.get("VAL").add("GTC"); geneticCode.get("VAL").add("GTA"); geneticCode.get("VAL").add("GTG");
        geneticCode.put("STOP",new ArrayList<String>());
        geneticCode.get("STOP").add("TAA"); geneticCode.get("STOP").add("TGA"); geneticCode.get("STOP").add("TAG");

        for(String aa : geneticCode.keySet()) {
            for(String codon : geneticCode.get(aa)) {
                isSynonymousCodon.put(codon, new HashMap<String, Boolean>());
            }
        }
        for(String codon1 : isSynonymousCodon.keySet()) {
            Map<String,Boolean> codonEntry = isSynonymousCodon.get(codon1);
            for(String codon2 : isSynonymousCodon.keySet()) {
                codonEntry.put(codon2,false);
            }
        }
        for(String aa : geneticCode.keySet()) {
            for(String codon1 : geneticCode.get(aa)) {
                for(String codon2 : geneticCode.get(aa)) {
                    isSynonymousCodon.get(codon1).put(codon2,true);
                }
            }
        }

    }

    public VariantAnnotationMongoDBAdaptor(DB db, String species, String assembly) {
        super(db, species, assembly);
    }

    public VariantAnnotationMongoDBAdaptor(DB db, String species, String assembly, int coreChunkSize) {
        super(db, species, assembly);
        this.coreChunkSize = coreChunkSize;
    }

    private Boolean regionsOverlap(Integer region1Start, Integer region1End, Integer region2Start, Integer region2End) {

//        return ((region2Start>=region1Start && region2Start<=region1End) || (region2End>=region1Start && region2End<=region1End) || (region1Start>=region2Start && region1End<=region2End));
        return ((region2Start >= region1Start || region2End >= region1Start) && (region2Start <= region1End || region2End <= region1End));

    }

    private Boolean isStopCodon(String codon) {
        if(codon.equals("TAA") || codon.equals("TGA") || codon.equals("TAG")) {
            return true;
        }
        return false;
    }

    private Boolean gainsStopCodon(String sequence) {
        int i = 0;
        Boolean stop = false;
        do {
            String codon = sequence.substring(i, i + 3);
            stop = (codon.equals("TAA") || codon.equals("TGA") || codon.equals("TAG"));
            i++;
        } while((i<sequence.length()) && !stop);

        return stop;
    }

    private void solveCodingExonEffect(String previousCodonNucleotides, String exonSequence, Integer exonStart, Integer exonEnd, Integer variantStart, Integer variantEnd,
                                       String variantRef, String variantAlt, List<String> consequenceTypeList) {
        Integer variantPhaseShift = (variantStart-(exonStart-previousCodonNucleotides.length())) % 3;
        Integer modifiedCodonRelativeStart = variantStart-variantPhaseShift-exonStart;
        String modifiedCodonPrefix;
        String newCodon;
        if(variantAlt.equals("-")) {  // Deletion
            consequenceTypeList.add("feature_truncation");
            if(variantStart >= exonStart && variantEnd <= exonEnd) {  // Deletion does not go beyond exon limits
                if(modifiedCodonRelativeStart < 0) {
                    modifiedCodonPrefix = previousCodonNucleotides+exonSequence.substring(0,variantStart-exonStart);
                } else {
                    modifiedCodonPrefix = exonSequence.substring(modifiedCodonRelativeStart, modifiedCodonRelativeStart + variantPhaseShift);
                }

                if(variantRef.length()%3 == 0) {
                    if (variantPhaseShift == 0) {  // Check deletion starts at the first position of a codon
                        consequenceTypeList.add("inframe_deletion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                    }
                } else {
                    consequenceTypeList.add("frameshift_variant");
                    newCodon = modifiedCodonPrefix+exonSequence.substring(variantEnd-exonStart+1,variantEnd-exonStart+1+(3-modifiedCodonPrefix.length()));
                    if(isStopCodon(newCodon)) {
                        consequenceTypeList.add("stop_gained");
                    }
                }
            }
        } else {
            if(variantRef.equals("-")) {  // Insertion  TODO: I've seen insertions within Cellbase-mongo with a ref != -
                consequenceTypeList.add("feature_elongation");
                if(variantAlt.length()%3 == 0) {
                    if (variantPhaseShift == 0) {  // Check insertion starts at the first position of a codon
                        consequenceTypeList.add("inframe_insertion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                        if (gainsStopCodon(variantAlt)) {
                            consequenceTypeList.add("stop_gained");
                        }
                    } else {
                        if (modifiedCodonRelativeStart < 0) {
                            modifiedCodonPrefix = previousCodonNucleotides + exonSequence.substring(0, variantStart - exonStart);
                        } else {
                            modifiedCodonPrefix = exonSequence.substring(modifiedCodonRelativeStart, modifiedCodonRelativeStart + variantPhaseShift);
                        }
                        if (gainsStopCodon(modifiedCodonPrefix + variantAlt)) {
                            consequenceTypeList.add("stop_gained");
                        }
                    }
                } else {
                    consequenceTypeList.add("frameshift_variant");
                    if (modifiedCodonRelativeStart < 0) {
                        modifiedCodonPrefix = previousCodonNucleotides + exonSequence.substring(0, variantStart - exonStart);
                    } else {
                        modifiedCodonPrefix = exonSequence.substring(modifiedCodonRelativeStart, modifiedCodonRelativeStart + variantPhaseShift);
                    }
                    if (gainsStopCodon(modifiedCodonPrefix + variantAlt)) {
                        consequenceTypeList.add("stop_gained");
                    }
                }
            } else {  // SNV
                String referenceCodon = exonSequence.substring(modifiedCodonRelativeStart, modifiedCodonRelativeStart + 3);
                char[] modifiedCodonArray = referenceCodon.toCharArray();
                modifiedCodonArray[variantPhaseShift] = variantAlt.toCharArray()[0];
                if(isSynonymousCodon.get(referenceCodon).get(String.valueOf(modifiedCodonArray))){
                    consequenceTypeList.add("synonymous_variant");
                } else {
                    consequenceTypeList.add("missense_variant");
                }
            }
        }
    }

//    private void solveExon(String exonSequence, Integer exonStart, Integer exonEnd, Integer genomicCodingStart, Integer genomicCodingEnd, Integer variantStart, Integer variantEnd,
//                             String variantRef, String variantAlt, List<String> consequenceTypeList, String previousCodonNucleotides) {
//        if(genomicCodingStart <= exonStart) {
//            if(genomicCodingEnd >= exonEnd) {  // The whole exon is coding  -----CCCCCCCCCCCCC------
//                consequenceTypeList.add("coding_sequence_variant");
//                solveCodingExonEffect(previousCodonNucleotides, exonSequence, exonStart, exonEnd, variantStart, variantEnd, variantRef, variantAlt, consequenceTypeList);
//            } else { // Exon contains UTR
//                if(genomicCodingEnd < exonStart) {  // Whole exon is 3'UTR   -----UUUUUUUUUUUUU------
//                    consequenceTypeList.add("3_prime_UTR_variant");
//                } else {  // Right part of the exon is UTR, left is coding  -----CCCCCCCUUUUUU------
//                    if (regionsOverlap(genomicCodingEnd, exonEnd, variantStart, variantEnd)) {  // Variant overlaps UTR
//                        consequenceTypeList.add("3_prime_UTR_variant");
//                        if (regionsOverlap(exonStart, genomicCodingEnd, variantStart, variantEnd)) {  // Variant also overlaps coding region
//                            consequenceTypeList.add("coding_sequence_variant");
//                        } else {  // Variant does not overlap coding region
//                            solveStopCodonGain(prevNts, exonSequence, exonStart, exonEnd, variantStart, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                        }
//                    } else {  // Variant just overlaps coding region
//                        consequenceTypeList.add("coding_sequence_variant");
//                        solveCodingEffect(prevNts, exonSequence, exonStart, genomicCodingEnd, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                    }
//                }
//            }
//        } else {
//            if (genomicCodingEnd >= exonEnd) {
//                if (genomicCodingStart > exonEnd) {  // Whole exon is 5'UTR  -----UUUUUUUUUUUUU------
//                    consequenceTypeList.add("5_prime_UTR_variant");
//                    solveStopCodonGain(prevNts, exonSequence, exonStart, exonEnd, variantStart, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                } else {  // Left part of the exon is 5'UTR, right part of the exon is coding  -----UUUUUUUCCCCCC------
//                    if (regionsOverlap(exonStart,genomicCodingStart,variantStart,variantEnd)) {  // Variant overlaps UTR
//                        consequenceTypeList.add("5_prime_UTR_variant");
//                        if (regionsOverlap(genomicCodingStart,exonEnd,variantStart,variantEnd)) {  // Variant also overlaps coding region
//                            consequenceTypeList.add("coding_sequence_variant");
//                        } else {  // Variant does not overlap coding region
//                            solveStopCodonGain(prevNts, exonSequence, exonStart, exonEnd, variantStart, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                        }
//                    } else {  // Variant just overlaps coding region
//                        consequenceTypeList.add("coding_sequence_variant");
//                        solveCodingEffect(prevNts, exonSequence, exonStart, genomicCodingEnd, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                    }
//                }
//            } else {  //  Exon contains 5'UTR-coding-3'UTR  -----UUUUCCCCCUUUU------
//                if (regionsOverlap(exonStart,genomicCodingStart,variantStart,variantEnd)) {  // Variant overlaps 5'UTR
//                    consequenceTypeList.add("5_prime_UTR_variant");
//                    if(regionsOverlap(genomicCodingStart,genomicCodingEnd,variantStart,variantEnd)) {  // Variant also overlaps codon region
//                        consequenceTypeList.add("coding_sequence_variant");
//                    } else {  // Variant just overlaps 5'UTR
//                        solveStopCodonGain(prevNts, exonSequence, exonStart, exonEnd, variantStart, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                    }
//                } else {
//                    if (regionsOverlap(genomicCodingEnd,exonEnd,variantStart,variantEnd)) {  // Variant overlaps 3'UTR
//                        consequenceTypeList.add("5_prime_UTR_variant");
//                        if (regionsOverlap(genomicCodingStart,genomicCodingEnd,variantStart,variantEnd)) {
//                            consequenceTypeList.add("coding_sequence_variant");
//                        } else {  // Variant just overlaps 3'UTR
//                            solveStopCodonGain(prevNts, exonSequence, exonStart, exonEnd, variantStart, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                        }
//                    } else {  // Variant just overlaps coding region
//                        consequenceTypeList.add("coding_sequence_variant");
//                        solveCodingEffect(prevNts, exonSequence, exonStart, genomicCodingEnd, variantEnd, variantRef, variantAlt, consequenceTypeList);
//                    }
//                }
//            }
//        }
//    }

    private void solveCodingEffect(Boolean splicing, String transcriptSequence, Integer cdnaCodingStart, Integer cdnaCodingEnd,
                                   Integer cdnaVariantStart, Integer cdnaVariantEnd, String variantRef, String variantAlt,
                                   HashSet<String> consequenceTypeList) {
        // TODO: lo q hay dentro de esta funcion es copia pega de solveCodingExonEffect. Arreglarlo. Es basicamente igual,
        // TODO: una vez aqui dentro ya se q la variante esta entre cdnaVariantStart y cdnaVariantEnd. Hay que comprobar
        // TODO: los codones incio/fin. El resto es igual, solo q antes de ponerse a identificar el codon que modifica la variante
        // TODO: hay que comprobar si es un splicing o no. En caso de ser un splicing q no se haga nada, no hay prediccion posible
        Integer variantPhaseShift = (cdnaVariantStart-cdnaCodingStart) % 3;
        String modifiedCodonPrefix,altSuffix;
        String newCodon;

        if(cdnaVariantStart != null && cdnaVariantStart<(cdnaCodingStart+3)) {
            consequenceTypeList.add("initiator_codon_variant");
        }

        if(cdnaVariantEnd != null && cdnaVariantEnd>(cdnaCodingEnd-3)) {
            if(cdnaVariantStart==cdnaVariantEnd) {
                int modifiedCodonStart = cdnaVariantStart - variantPhaseShift;
                String referenceCodon = transcriptSequence.substring(modifiedCodonStart, modifiedCodonStart + 3);
                char[] modifiedCodonArray = referenceCodon.toCharArray();
                modifiedCodonArray[variantPhaseShift] = variantAlt.toCharArray()[0];
                if (isSynonymousCodon.get(referenceCodon).get(String.valueOf(modifiedCodonArray))) {
                    consequenceTypeList.add("stop_retained_variant");
                } else {
                    consequenceTypeList.add("stop_lost");
                }
            }  else {
                consequenceTypeList.add("stop_lost");
            }
        }

        if(variantAlt.equals("-")) {  // Deletion
            consequenceTypeList.add("feature_truncation");
            if(!splicing) {
                if (variantRef.length() % 3 == 0) {
                        consequenceTypeList.add("inframe_deletion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                } else {
                    consequenceTypeList.add("frameshift_variant");
//                    modifiedCodonPrefix = transcriptSequence.substring(cdnaVariantStart-variantPhaseShift, cdnaVariantStart);
//                    if (gainsStopCodon(modifiedCodonPrefix+transcriptSequence.substring(cdnaVariantEnd+1,cdnaCodingEnd-2))) {
//                        consequenceTypeList.add("stop_gained");
//                    }
                }
            }
        } else {
            if(variantRef.equals("-")) {  // Insertion  TODO: I've seen insertions within Cellbase-mongo with a ref != -
                consequenceTypeList.add("feature_elongation");
                if(!splicing) {
                    if(variantAlt.length()%3 == 0) {
                        consequenceTypeList.add("inframe_insertion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                    } else {
                        consequenceTypeList.add("frameshift_variant");
                    }
                }
            } else {  // SNV
                if(!splicing) {
                    int modifiedCodonStart = cdnaVariantStart - variantPhaseShift;
                    String referenceCodon = transcriptSequence.substring(modifiedCodonStart-1, modifiedCodonStart + 2);  // -1 and +2 because of base 0 String indexing
                    char[] modifiedCodonArray = referenceCodon.toCharArray();
                    modifiedCodonArray[variantPhaseShift] = variantAlt.toCharArray()[0];
                    if (isSynonymousCodon.get(referenceCodon).get(String.valueOf(modifiedCodonArray))) {
                        consequenceTypeList.add("synonymous_variant");
                    } else {
                        consequenceTypeList.add("missense_variant");
                    }
                }
            }
        }
    }

    private void solveCodingTranscriptEffect(Boolean splicing, String transcriptSequence, Integer genomicCodingStart,
                                             Integer genomicCodingEnd, Integer variantStart, Integer variantEnd,
                                             Integer cdnaCodingStart, Integer cdnaCodingEnd, Integer cdnaVariantStart,
                                             Integer cdnaVariantEnd, String variantRef, String variantAlt,
                                             HashSet<String> consequenceTypeList) {
        if(variantStart < genomicCodingStart) {
            consequenceTypeList.add("5_prime_UTR_variant");
            if(variantEnd >= genomicCodingStart) {  // Deletion that removes initiator codon
                consequenceTypeList.add("initiator_codon_variant");
                consequenceTypeList.add("coding_sequence_variant");
            }
        } else {
            if(variantStart <= genomicCodingEnd) {
                if(variantEnd <= genomicCodingEnd) {
                    consequenceTypeList.add("coding_sequence_variant");
                    solveCodingEffect(splicing, transcriptSequence, cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart,
                                      cdnaVariantEnd, variantRef, variantAlt, consequenceTypeList);
                } else {
                    consequenceTypeList.add("stop_lost");
                }
            } else {
                consequenceTypeList.add("3_prime_UTR_variant");
            }
        }
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, Integer variantStart, Integer variantEnd, HashSet<String> consequenceTypeList,
                                                Boolean[] junctionSolution) {
        Boolean splicing = false;
        Boolean intron = false;
        Boolean notdonor = true;
        Boolean notacceptor = true;

        if(regionsOverlap(spliceSite1,spliceSite2,variantStart,variantEnd)) {
            consequenceTypeList.add("intron_variant");
            intron = true;
        }
        if(regionsOverlap(spliceSite1-3,spliceSite1+7,variantStart,variantEnd)) {
            consequenceTypeList.add("splice_region_variant");
            splicing = true;
            if(regionsOverlap(spliceSite1,spliceSite1+1,variantStart,variantEnd)) {
                consequenceTypeList.add("splice_donor_variant");
                notdonor = false;
            }
        }
        if(regionsOverlap(spliceSite2-7,spliceSite2+3,variantStart,variantEnd)) {
            consequenceTypeList.add("splice_region_variant");
            splicing = true;
            if(regionsOverlap(spliceSite2-1,spliceSite2,variantStart,variantEnd)) {
                consequenceTypeList.add("splice_acceptor_variant");
                notacceptor = false;
            }
        }

        junctionSolution[0] = splicing;
        junctionSolution[1] = (intron && notdonor && notacceptor);
    }

    @Override
    public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options) {

        Logger logger = LoggerFactory.getLogger(this.getClass());

        HashSet<String> consequenceTypeList = new HashSet<>();
        QueryResult queryResult = null;
        QueryBuilder builder = null;
        BasicDBList transcriptInfoList, exonInfoList;
        BasicDBObject transcriptInfo, exonInfo;
        BasicDBObject geneInfo;
        Integer geneStart, geneEnd, transcriptStart, transcriptEnd, exonStart, exonEnd, genomicCodingStart, genomicCodingEnd;
        Integer cdnaCodingStart, cdnaCodingEnd, cdnaExonStart, cdnaExonEnd, cdnaVariantStart, cdnaVariantEnd, prevSpliceSite;
        Integer variantStart = variant.getPosition();
        Integer variantEnd = variant.getPosition()+variant.getReference().length()-1;  //TODO: Check deletion input format to ensure that variantEnd is correctly calculated
        String geneStrand, transcriptStrand, exonSequence, transcriptSequence;
        String nextCodonNucleotides = "";
        long dbTimeStart, dbTimeEnd;
        Boolean splicing, coding, exonsRemain, variantAhead;
        Boolean[] junctionSolution = {false, false};

        int exonCounter;

        mongoDBCollection = db.getCollection("gene");
        dbTimeStart = System.currentTimeMillis();

        // Get all genes surrounding the variant +-5kb
        builder = QueryBuilder.start("chromosome").is(variant.getChromosome()).and("end")
                .greaterThanEquals(variant.getPosition()-5000).and("start").lessThanEquals(variantEnd+5000); // variantEnd is used rather than variant.getPosition() to account for deletions which end falls within the 5kb left area of the gene
                                                                                                             // variantEnd equals variant.getPosition() for non-deletion variants

        QueryResult geneQueryResult = executeQuery(variant.toString(), builder.get(), options);
        BasicDBList geneInfoList = (BasicDBList) geneQueryResult.getResult();


        for(Object geneInfoObject: geneInfoList) {
            geneInfo = (BasicDBObject) geneInfoObject;
            geneStart = (Integer) geneInfo.get("start");
            geneEnd = (Integer) geneInfo.get("end");
            geneStrand = (String) geneInfo.get("strand");

            transcriptInfoList = (BasicDBList) geneInfo.get("transcripts");
            for(Object transcriptInfoObject: transcriptInfoList) {
                transcriptInfo = (BasicDBObject) transcriptInfoObject;
                transcriptStart = (Integer) transcriptInfo.get("start");
                transcriptEnd = (Integer) transcriptInfo.get("end");
                transcriptStrand = (String) transcriptInfo.get("strand");

                if(transcriptStrand.equals("+")) {
                    // Variant overlaps with -5kb region
                    if(regionsOverlap(transcriptStart-5000, transcriptStart-1, variantStart, variantEnd)) {
                        consequenceTypeList.add("upstream_gene_variant_5kb");
                        // Variant overlaps with -2kb region
                        if(regionsOverlap(transcriptStart-2000, transcriptStart-1, variantStart, variantEnd)) {
                            consequenceTypeList.add("upstream_gene_variant_2kb");
                        }
                    }
                    // Variant overlaps with +5kb region
                    if(regionsOverlap(transcriptEnd+1, transcriptEnd+5000, variantStart, variantEnd)) {
                        consequenceTypeList.add("downstream_gene_variant_5kb");
                        // Variant overlaps with +2kb region
                        if(regionsOverlap(transcriptEnd+1, transcriptEnd+2000, variantStart, variantEnd)) {
                            consequenceTypeList.add("downstream_gene_variant_2kb");
                        }
                    }

                    // Check variant falls within transcript start/end coordinates
                    if(regionsOverlap(transcriptStart,transcriptEnd,variantStart,variantEnd)) {
                        genomicCodingStart = (Integer) transcriptInfo.get("genomicCodingStart");
                        genomicCodingEnd = (Integer) transcriptInfo.get("genomicCodingEnd");
                        cdnaCodingStart = (Integer) transcriptInfo.get("cdnaCodingStart");
                        cdnaCodingEnd = (Integer) transcriptInfo.get("cdnaCodingEnd");
                        exonInfoList = (BasicDBList) transcriptInfo.get("exons");
                        exonInfo = (BasicDBObject) exonInfoList.get(0);
                        exonStart = (Integer) exonInfo.get("start");
                        exonEnd = (Integer) exonInfo.get("end");
                        transcriptSequence = (String) exonInfo.get("sequence");
                        variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
                        cdnaExonEnd = (exonEnd - exonStart + 1);
                        cdnaVariantStart = null;
                        cdnaVariantEnd = null;
                        junctionSolution[0] = false;
                        junctionSolution[1] = false;
                        splicing = false;

                        if(variantStart >= exonStart) {
                            if(variantStart <= exonEnd) {  // Variant start within the exon
                                cdnaVariantStart = cdnaExonEnd - (exonEnd - variantStart);
                                if(variantEnd <= exonEnd) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                                    cdnaVariantEnd = cdnaExonEnd - (exonEnd - variantEnd);
                                }
                            }
                        } else {
                            if(variantEnd <= exonEnd) {
//                                if(variantEnd >= exonStart) {  // Only variant end within the exon  ----||||||||||E||||----
                                // We do not contemplate that variant end can be located before this exon since this is the first exon
                                cdnaVariantEnd = cdnaExonEnd - (exonEnd - variantEnd);
//                                }
                            } // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                        }

                        exonCounter = 1;
                        while(exonCounter<exonInfoList.size() && !splicing && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
                            exonInfo = (BasicDBObject) exonInfoList.get(exonCounter);          // next exon has been loaded
                            exonStart = (Integer) exonInfo.get("start");
                            prevSpliceSite = exonEnd+1;
                            exonEnd = (Integer) exonInfo.get("end");
                            transcriptSequence = transcriptSequence + ((String) exonInfo.get("sequence"));
                            solveJunction(prevSpliceSite, exonStart-1, variantStart, variantEnd, consequenceTypeList, junctionSolution);
                            splicing = (splicing || junctionSolution[0]);

                            if(variantStart >= exonStart) {
                                cdnaExonEnd += (exonEnd - exonStart + 1);
                                if(variantStart <= exonEnd) {  // Variant start within the exon
                                    cdnaVariantStart = cdnaExonEnd - (exonEnd - variantStart);
                                    if(variantEnd <= exonEnd) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                                        cdnaVariantEnd = cdnaExonEnd - (exonEnd - variantEnd);
                                    }
                                }
                            } else {
                                if(variantEnd <= exonEnd) {
                                    if(variantEnd >= exonStart) {  // Only variant end within the exon  ----||||||||||E||||----
                                        cdnaVariantEnd = cdnaExonEnd - (exonEnd - variantEnd);
                                    } else {  // Variant does not include this exon, variant is located before this exon
                                        variantAhead = false;
                                    }
                                } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                                    cdnaExonEnd += (exonEnd - exonStart + 1);
                                }
                            }
                            exonCounter++;
                        }
                        if(!junctionSolution[1]) {
                            solveCodingTranscriptEffect(splicing, transcriptSequence, genomicCodingStart, genomicCodingEnd,
                                    variantStart, variantEnd, cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart, cdnaVariantEnd,
                                    variant.getReference(), variant.getAlternative(), consequenceTypeList);
                        }
                    }
                }
            }
        }


        return queryResult;







//        List<QueryResult> queryResults = new ArrayList<>(variants.size());
//        TabixReader currentTabix = null;
//        String line = "";
//        long dbTimeStart, dbTimeEnd;
//        String document = "";
//        try {
//            currentTabix = new TabixReader(applicationProperties.getProperty("VARIANT_ANNOTATION.FILENAME"));
//            for(GenomicVariant genomicVariant: variants) {
//                System.out.println(">>>"+genomicVariant);
//                TabixReader.Iterator it = currentTabix.query(genomicVariant.getChromosome() + ":" + genomicVariant.getPosition() + "-" + genomicVariant.getPosition());
//                String[] fields = null;
//                dbTimeStart = System.currentTimeMillis();
//                while (it != null && (line = it.next()) != null) {
//                    fields = line.split("\t");
//                    document = fields[2];
////                System.out.println(fields[2]);
////                listRecords = factory.create(source, line);
//
////                if(listRecords.size() > 0){
////
////                    tabixRecord = listRecords.get(0);
////
////                    if (tabixRecord.getReference().equals(record.getReference()) && tabixRecord.getAlternate().equals(record.getAlternate())) {
////                        controlBatch.add(tabixRecord);
////                        map.put(record, cont++);
////                    }
////                }
//                    break;
//                }
//
////            List<GenomicVariantEffect> a = genomicVariantEffectPredictor.getAllEffectsByVariant(variants.get(0), genes, null);
//                dbTimeEnd = System.currentTimeMillis();
//
//                QueryResult queryResult = new QueryResult();
//                queryResult.setDBTime((dbTimeEnd - dbTimeStart));
//                queryResult.setNumResults(1);
//                queryResult.setResult(document);
//
//                queryResults.add(queryResult);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

    @Override
    public List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options) {

        List<QueryResult> queryResults = new ArrayList<>(variants.size());

//        try {
            for (GenomicVariant genomicVariant : variants) {
                queryResults.add(getAllConsequenceTypesByVariant(genomicVariant, options));
            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return queryResults;

    }





    @Override
    public QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options) {
        List<QueryResult> queryResults = new ArrayList<>(variants.size());
        TabixReader currentTabix = null;
        String line = "";
        long dbTimeStart, dbTimeEnd;
        String document = "";
        try {
            currentTabix = new TabixReader(applicationProperties.getProperty("VARIANT_ANNOTATION.FILENAME"));
            for(GenomicVariant genomicVariant: variants) {
                System.out.println(">>>"+genomicVariant);
                TabixReader.Iterator it = currentTabix.query(genomicVariant.getChromosome() + ":" + genomicVariant.getPosition() + "-" + genomicVariant.getPosition());
                String[] fields = null;
                dbTimeStart = System.currentTimeMillis();
                while (it != null && (line = it.next()) != null) {
                    fields = line.split("\t");
                    document = fields[2];
//                System.out.println(fields[2]);
//                listRecords = factory.create(source, line);

//                if(listRecords.size() > 0){
//
//                    tabixRecord = listRecords.get(0);
//
//                    if (tabixRecord.getReference().equals(record.getReference()) && tabixRecord.getAlternate().equals(record.getAlternate())) {
//                        controlBatch.add(tabixRecord);
//                        map.put(record, cont++);
//                    }
//                }
                    break;
                }

//            List<GenomicVariantEffect> a = genomicVariantEffectPredictor.getAllEffectsByVariant(variants.get(0), genes, null);
                dbTimeEnd = System.currentTimeMillis();

                QueryResult queryResult = new QueryResult();
                queryResult.setDBTime((dbTimeEnd - dbTimeStart));
                queryResult.setNumResults(1);
                queryResult.setResult(document);

                queryResults.add(queryResult);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryResults;
    }
}
