package org.opencb.cellbase.lib.mongodb.db;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.QueryBuilder;
import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
//import java.util.logging.Logger;

/**
 * Created by imedina on 11/07/14.
 */
public class VariantAnnotationMongoDBAdaptor extends MongoDBAdaptor implements VariantAnnotationDBAdaptor {


//    private DBCollection mongoVariationPhenotypeDBCollection;
    private int coreChunkSize = 5000;
    private int regulatoryChunkSize = 2000;  //TODO: load this value from properties
    private static Map<String, Map<String,Boolean>> isSynonymousCodon = new HashMap<>();
    private static Map<String, List<String>> aToCodon = new HashMap<>(20);
    private static Map<String, String> codonToA = new HashMap<>();
    private static Map<String, Integer> biotypes = new HashMap<>(30);
    private static Map<Character, Character> complementaryNt = new HashMap<>();

    static {

        ///////////////////////////////////////////////////////////////////////
        /////   GENETIC CODE   ////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        aToCodon.put("ALA",new ArrayList<String>());
        aToCodon.get("ALA").add("GCT"); aToCodon.get("ALA").add("GCC"); aToCodon.get("ALA").add("GCA"); aToCodon.get("ALA").add("GCG");
        aToCodon.put("ARG",new ArrayList<String>());
        aToCodon.get("ARG").add("CGT"); aToCodon.get("ARG").add("CGC"); aToCodon.get("ARG").add("CGA"); aToCodon.get("ARG").add("CGG");
        aToCodon.get("ARG").add("AGA"); aToCodon.get("ARG").add("AGG");
        aToCodon.put("ASN", new ArrayList<String>());
        aToCodon.get("ASN").add("AAT"); aToCodon.get("ASN").add("AAC");
        aToCodon.put("ASP", new ArrayList<String>());
        aToCodon.get("ASP").add("GAT"); aToCodon.get("ASP").add("GAC");
        aToCodon.put("CYS", new ArrayList<String>());
        aToCodon.get("CYS").add("TGT"); aToCodon.get("CYS").add("TGC");
        aToCodon.put("GLN", new ArrayList<String>());
        aToCodon.get("GLN").add("CAA"); aToCodon.get("GLN").add("CAG");
        aToCodon.put("GLU", new ArrayList<String>());
        aToCodon.get("GLU").add("GAA"); aToCodon.get("GLU").add("GAG");
        aToCodon.put("GLY",new ArrayList<String>());
        aToCodon.get("GLY").add("GGT"); aToCodon.get("GLY").add("GGC"); aToCodon.get("GLY").add("GGA"); aToCodon.get("GLY").add("GGG");
        aToCodon.put("HIS",new ArrayList<String>());
        aToCodon.get("HIS").add("CAT"); aToCodon.get("HIS").add("CAC");
        aToCodon.put("ILE",new ArrayList<String>());
        aToCodon.get("ILE").add("ATT"); aToCodon.get("ILE").add("ATC"); aToCodon.get("ILE").add("ATA");
        aToCodon.put("LEU",new ArrayList<String>());
        aToCodon.get("LEU").add("TTA"); aToCodon.get("LEU").add("TTG"); aToCodon.get("LEU").add("CTT"); aToCodon.get("LEU").add("CTC");
        aToCodon.get("LEU").add("CTA"); aToCodon.get("LEU").add("CTG");
        aToCodon.put("LYS", new ArrayList<String>());
        aToCodon.get("LYS").add("AAA"); aToCodon.get("LYS").add("AAG");
        aToCodon.put("MET", new ArrayList<String>());
        aToCodon.get("MET").add("ATG");
        aToCodon.put("PHE",new ArrayList<String>());
        aToCodon.get("PHE").add("TTT"); aToCodon.get("PHE").add("TTC");
        aToCodon.put("PRO",new ArrayList<String>());
        aToCodon.get("PRO").add("CCT"); aToCodon.get("PRO").add("CCC"); aToCodon.get("PRO").add("CCA"); aToCodon.get("PRO").add("CCG");
        aToCodon.put("SER",new ArrayList<String>());
        aToCodon.get("SER").add("TCT"); aToCodon.get("SER").add("TCC"); aToCodon.get("SER").add("TCA"); aToCodon.get("SER").add("TCG");
        aToCodon.get("SER").add("AGT"); aToCodon.get("SER").add("AGC");
        aToCodon.put("THR",new ArrayList<String>());
        aToCodon.get("THR").add("ACT"); aToCodon.get("THR").add("ACC"); aToCodon.get("THR").add("ACA"); aToCodon.get("THR").add("ACG");
        aToCodon.put("TRP",new ArrayList<String>());
        aToCodon.get("TRP").add("TGG");
        aToCodon.put("TYR",new ArrayList<String>());
        aToCodon.get("TYR").add("TAT"); aToCodon.get("TYR").add("TAC");
        aToCodon.put("VAL",new ArrayList<String>());
        aToCodon.get("VAL").add("GTT"); aToCodon.get("VAL").add("GTC"); aToCodon.get("VAL").add("GTA"); aToCodon.get("VAL").add("GTG");
        aToCodon.put("STOP",new ArrayList<String>());
        aToCodon.get("STOP").add("TAA"); aToCodon.get("STOP").add("TGA"); aToCodon.get("STOP").add("TAG");

        for(String aa : aToCodon.keySet()) {
            for(String codon : aToCodon.get(aa)) {
                isSynonymousCodon.put(codon, new HashMap<String, Boolean>());
                codonToA.put(codon, aa);
            }
        }
        for(String codon1 : isSynonymousCodon.keySet()) {
            Map<String,Boolean> codonEntry = isSynonymousCodon.get(codon1);
            for(String codon2 : isSynonymousCodon.keySet()) {
                codonEntry.put(codon2,false);
            }
        }
        for(String aa : aToCodon.keySet()) {
            for(String codon1 : aToCodon.get(aa)) {
                for(String codon2 : aToCodon.get(aa)) {
                    isSynonymousCodon.get(codon1).put(codon2,true);
                }
            }
        }

	    biotypes.put("3prime_overlapping_ncrna",0);
        biotypes.put("IG_C_gene",1);
        biotypes.put("IG_C_pseudogene",2);
        biotypes.put("IG_D_gene",3);
        biotypes.put("IG_J_gene",4);
        biotypes.put("IG_J_pseudogene",5);
        biotypes.put("IG_V_gene",6);
        biotypes.put("IG_V_pseudogene",7);
        biotypes.put("Mt_rRNA",8);
        biotypes.put("Mt_tRNA",9);
        biotypes.put("TR_C_gene",10);
        biotypes.put("TR_D_gene",11);
        biotypes.put("TR_J_gene",12);
        biotypes.put("TR_J_pseudogene",13);
        biotypes.put("TR_V_gene",14);
        biotypes.put("TR_V_pseudogene",15);
        biotypes.put("antisense",16);
        biotypes.put("lincRNA",17);
        biotypes.put("miRNA",18);
        biotypes.put("misc_RNA",19);
        biotypes.put("polymorphic_pseudogene",20);
        biotypes.put("processed_pseudogene",21);
        biotypes.put("processed_transcript",22);
        biotypes.put("protein_coding",23);
        biotypes.put("pseudogene",24);
        biotypes.put("rRNA",25);
        biotypes.put("sense_intronic",26);
        biotypes.put("sense_overlapping",27);
        biotypes.put("snRNA",28);
        biotypes.put("snoRNA",29);
        biotypes.put("nonsense_mediated_decay",30);
        biotypes.put("unprocessed_pseudogene",31);
        biotypes.put("transcribed_unprocessed_pseudogene",32);
        biotypes.put("retained_intron",33);
        biotypes.put("non_stop_decay",34);
        biotypes.put("unitary_pseudogene",35);
        biotypes.put("translated_processed_pseudogene",36);
        biotypes.put("transcribed_processed_pseudogene",37);
        biotypes.put("tRNA_pseudogene",38);
        biotypes.put("snoRNA_pseudogene",39);
        biotypes.put("snRNA_pseudogene",40);
        biotypes.put("scRNA_pseudogene",41);
        biotypes.put("rRNA_pseudogene",42);
        biotypes.put("misc_RNA_pseudogene",43);
        biotypes.put("miRNA_pseudogene",44);
        biotypes.put("non_coding",45);
        biotypes.put("ambiguous_orf",46);
        biotypes.put("known_ncrna",47);
        biotypes.put("retrotransposed",48);
        biotypes.put("transcribed_unitary_pseudogene",49);
        biotypes.put("translated_unprocessed_pseudogene",50);
        biotypes.put("LRG_gene",51);


        complementaryNt.put('A','T');
        complementaryNt.put('C','G');
        complementaryNt.put('G','C');
        complementaryNt.put('T','A');

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

    private void solvePositiveCodingEffect(Boolean splicing, String transcriptSequence, Integer cdnaCodingStart, Integer cdnaCodingEnd,
                                           Integer cdnaVariantStart, Integer cdnaVariantEnd, String variantRef, String variantAlt,
                                           HashSet<String> SoNames, ConsequenceType consequenceTypeTemplate) {
        // TODO: lo q hay dentro de esta funcion es copia pega de solveCodingExonEffect. Arreglarlo. Es basicamente igual,
        // TODO: una vez aqui dentro ya se q la variante esta entre cdnaVariantStart y cdnaVariantEnd. Hay que comprobar
        // TODO: los codones incio/fin. El resto es igual, solo q antes de ponerse a identificar el codon que modifica la variante
        // TODO: hay que comprobar si es un splicing o no. En caso de ser un splicing q no se haga nada, no hay prediccion posible

        Boolean codingAnnotationAdded = false;  // This will indicate wether it is needed to add the "coding_sequence_variant" annotation or not

        if(variantAlt.equals("-")) {  // Deletion
//            SoNames.add("feature_truncation");
            if(cdnaVariantStart != null && cdnaVariantStart<(cdnaCodingStart+3)) {  // cdnaVariantStart=null if variant is intronic
                SoNames.add("initiator_codon_variant");
                codingAnnotationAdded = true;
            }
            if(cdnaCodingEnd!=0) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaCodingEnd to 0
                if (cdnaVariantEnd != null && cdnaVariantEnd > (cdnaCodingEnd - 3)) {
                    SoNames.add("stop_lost");
                    codingAnnotationAdded = true;
                }
            } else {
                if(cdnaVariantEnd != null && cdnaVariantEnd>(transcriptSequence.length()-((transcriptSequence.length()%3)==0?3:(transcriptSequence.length()%3)))) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                    SoNames.add("incomplete_terminal_codon_variant");
                    codingAnnotationAdded = true;
                }
            }
            if(!splicing && cdnaVariantStart != null) {  // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
                if (variantRef.length() % 3 == 0) {
                    SoNames.add("inframe_deletion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                    codingAnnotationAdded = true;
                } else {
                    SoNames.add("frameshift_variant");
                    codingAnnotationAdded = true;
                }
            }
        } else {
            if(variantRef.equals("-")) {  // Insertion  TODO: I've seen insertions within Cellbase-mongo with a ref != -
                if(cdnaVariantStart != null && cdnaVariantStart<(cdnaCodingStart+3)) {  // cdnaVariantStart=null if variant is intronic
                    SoNames.add("initiator_codon_variant");
                    codingAnnotationAdded = true;
                }
                if(cdnaCodingEnd!=0) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                    if (cdnaVariantEnd != null && cdnaVariantEnd > (cdnaCodingEnd - 3)) {
                        SoNames.add("stop_lost");
                        codingAnnotationAdded = true;
                    }
                } else {
                    if(cdnaVariantEnd != null && cdnaVariantEnd>(transcriptSequence.length()-((transcriptSequence.length()%3)==0?3:(transcriptSequence.length()%3)))) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                        SoNames.add("incomplete_terminal_codon_variant");
                        codingAnnotationAdded = true;
                    }
                }
                SoNames.add("feature_elongation");
                if(!splicing && cdnaVariantStart != null) {
                    codingAnnotationAdded = true;
                    if(variantAlt.length()%3 == 0) {
                        SoNames.add("inframe_insertion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                    } else {
                        SoNames.add("frameshift_variant");
                    }
                }
            } else {  // SNV
                if(cdnaVariantStart != null) {
                    if (cdnaVariantStart < (cdnaCodingStart + 3)) {  // cdnaVariantStart=null if variant start is intronic
                        SoNames.add("initiator_codon_variant");
                        codingAnnotationAdded = true;
                    }
                    int finalNtPhase = (transcriptSequence.length()-cdnaCodingStart) % 3;
                    if (cdnaCodingEnd == 0 && (cdnaVariantEnd >= (transcriptSequence.length() - finalNtPhase))) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                        SoNames.add("incomplete_terminal_codon_variant");                                       // If that is the case and variant ocurs in the last complete/incomplete codon, no coding prediction is needed
                        codingAnnotationAdded = true;
                    } else if (!splicing) {
                        Integer variantPhaseShift = (cdnaVariantStart-cdnaCodingStart) % 3;
                        int modifiedCodonStart = cdnaVariantStart-variantPhaseShift;
                        String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);  // -1 and +2 because of base 0 String indexing
                        char[] modifiedCodonArray = referenceCodon.toCharArray();
                        modifiedCodonArray[variantPhaseShift] = variantAlt.toCharArray()[0];
                        codingAnnotationAdded = true;
                        if (isSynonymousCodon.get(referenceCodon).get(String.valueOf(modifiedCodonArray))) {
                            SoNames.add((cdnaVariantEnd < (cdnaCodingEnd - 2)) ? "synonymous_variant" : "stop_retained_variant");
                        } else {
                            if(cdnaVariantEnd < (cdnaCodingEnd - 2)) {
                                SoNames.add(isStopCodon(String.valueOf(modifiedCodonArray)) ? "stop_gained" : "missense_variant");
                            } else {
                                SoNames.add("stop_lost");
                            }
                        }
                        // Set consequenceTypeTemplate.aChange
                        consequenceTypeTemplate.setaChange(codonToA.get(referenceCodon) + "/" + codonToA.get(String.valueOf(modifiedCodonArray)));
                        // Set consequenceTypeTemplate.codon leaving only the nt that changes in uppercase. Careful with upper/lower case letters
                        char[] referenceCodonArray = referenceCodon.toLowerCase().toCharArray();
                        referenceCodonArray[variantPhaseShift] = Character.toUpperCase(referenceCodonArray[variantPhaseShift]);
                        modifiedCodonArray = String.valueOf(modifiedCodonArray).toLowerCase().toCharArray();
                        modifiedCodonArray[variantPhaseShift] = Character.toUpperCase(modifiedCodonArray[variantPhaseShift]);
                        consequenceTypeTemplate.setCodon(String.valueOf(referenceCodonArray) + "/" + String.valueOf(modifiedCodonArray));
                    }
                }
            }
        }
        if(!codingAnnotationAdded) {
            SoNames.add("coding_sequence_variant");
        }
    }

    private void solveNegativeCodingEffect(Boolean splicing, String transcriptSequence, Integer cdnaCodingStart, Integer cdnaCodingEnd,
                                           Integer cdnaVariantStart, Integer cdnaVariantEnd, String variantRef, String variantAlt,
                                           HashSet<String> SoNames, ConsequenceType consequenceTypeTemplate) {
        // TODO: lo q hay dentro de esta funcion es copia pega de solveCodingExonEffect. Arreglarlo. Es basicamente igual,
        // TODO: una vez aqui dentro ya se q la variante esta entre cdnaVariantStart y cdnaVariantEnd. Hay que comprobar
        // TODO: los codones incio/fin. El resto es igual, solo q antes de ponerse a identificar el codon que modifica la variante
        // TODO: hay que comprobar si es un splicing o no. En caso de ser un splicing q no se haga nada, no hay prediccion posible

        Boolean codingAnnotationAdded = false;

        if(variantAlt.equals("-")) {  // Deletion
            if(cdnaVariantStart != null && cdnaVariantStart<(cdnaCodingStart+3)) {  // cdnaVariantStart=null if variant is intronic
                SoNames.add("initiator_codon_variant");
                codingAnnotationAdded = true;
            }
            if(cdnaCodingEnd!=0) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                if (cdnaVariantEnd != null && cdnaVariantEnd > (cdnaCodingEnd - 3)) {
                    SoNames.add("stop_lost");
                    codingAnnotationAdded = true;
                }
            } else {
                if(cdnaVariantEnd != null && cdnaVariantEnd>(transcriptSequence.length()-((transcriptSequence.length()%3)==0?3:(transcriptSequence.length()%3)))) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                    SoNames.add("incomplete_terminal_codon_variant");
                    codingAnnotationAdded = true;
                }
            }
//            SoNames.add("feature_truncation");
            if(!splicing && cdnaVariantStart != null) {  // just checks cdnaVariantStart!=null because no splicing means cdnaVariantEnd is also != null
                if (variantRef.length() % 3 == 0) {
                    SoNames.add("inframe_deletion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                    codingAnnotationAdded = true;
                } else {
                    SoNames.add("frameshift_variant");
                    codingAnnotationAdded = true;
                }
            }
        } else {
            if(variantRef.equals("-")) {  // Insertion  TODO: I've seen insertions within Cellbase-mongo with a ref != -
                if(cdnaVariantStart != null && cdnaVariantStart<(cdnaCodingStart+3)) {  // cdnaVariantStart=null if variant is intronic
                    SoNames.add("initiator_codon_variant");
                    codingAnnotationAdded = true;
                }
                if(cdnaCodingEnd!=0) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                    if (cdnaVariantEnd != null && cdnaVariantEnd > (cdnaCodingEnd - 3)) {
                        SoNames.add("stop_lost");
                        codingAnnotationAdded = true;
                    }
                } else {
                    if(cdnaVariantEnd != null && cdnaVariantEnd>(transcriptSequence.length()-((transcriptSequence.length()%3)==0?3:(transcriptSequence.length()%3)))) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                        SoNames.add("incomplete_terminal_codon_variant");
                        codingAnnotationAdded = true;
                    }
                }
                SoNames.add("feature_elongation");
                if(!splicing && cdnaVariantStart != null) {
                    codingAnnotationAdded = true;
                    if(variantAlt.length()%3 == 0) {
                        SoNames.add("inframe_insertion");  // TODO: check that I correctly interpreted the meaning of this consequence type
                    } else {
                        SoNames.add("frameshift_variant");
                    }
                }
            } else {  // SNV
                if(cdnaVariantStart != null) {
                    if (cdnaVariantStart < (cdnaCodingStart + 3)) {  // cdnaVariantStart=null if variant is intronic
                        SoNames.add("initiator_codon_variant");
                        codingAnnotationAdded = true;
                    }
                    int finalNtPhase = (transcriptSequence.length()-cdnaCodingStart) % 3;
                    if (cdnaCodingEnd == 0 && (cdnaVariantEnd >= (transcriptSequence.length() - finalNtPhase))) { // Some transcripts do not have a STOP codon annotated in the ENSEMBL gtf. This causes CellbaseBuilder to leave cdnaVariantEnd to 0
                        SoNames.add("incomplete_terminal_codon_variant");                                       // If that is the case and variant ocurs in the last complete/incomplete codon, no coding prediction is needed
                        codingAnnotationAdded = true;
                    } else if (!splicing) {
                        Integer variantPhaseShift = (cdnaVariantStart-cdnaCodingStart) % 3;
                        int modifiedCodonStart = cdnaVariantStart - variantPhaseShift;
                        String reverseCodon = new StringBuilder(transcriptSequence.substring(transcriptSequence.length() - modifiedCodonStart - 2,
                                transcriptSequence.length() - modifiedCodonStart + 1)).reverse().toString(); // Rigth limit of the substring sums +1 because substring does not include that position
                        char[] referenceCodon = reverseCodon.toCharArray();
                        referenceCodon[0] = complementaryNt.get(referenceCodon[0]);
                        referenceCodon[1] = complementaryNt.get(referenceCodon[1]);
                        referenceCodon[2] = complementaryNt.get(referenceCodon[2]);
                        char[] modifiedCodonArray = referenceCodon.clone();
                        modifiedCodonArray[variantPhaseShift] = complementaryNt.get(variantAlt.toCharArray()[0]);
                        codingAnnotationAdded = true;
                        if (isSynonymousCodon.get(String.valueOf(referenceCodon)).get(String.valueOf(modifiedCodonArray))) {
                            SoNames.add("synonymous_variant");
                        } else {
                            if(cdnaVariantEnd < (cdnaCodingEnd - 2)) {
                                SoNames.add(isStopCodon(String.valueOf(modifiedCodonArray))?"stop_gained":"missense_variant");
                            } else {
                                SoNames.add("stop_lost");
                            }
                        }
                        // Set consequenceTypeTemplate.aChange
                        consequenceTypeTemplate.setaChange(codonToA.get(String.valueOf(referenceCodon)) + "/" + codonToA.get(String.valueOf(modifiedCodonArray)));
                        // Fill consequenceTypeTemplate.codon leaving only the nt that changes in uppercase. Careful with upper/lower case letters
                        char[] referenceCodonArray = String.valueOf(referenceCodon).toLowerCase().toCharArray();
                        referenceCodonArray[variantPhaseShift] = Character.toUpperCase(referenceCodonArray[variantPhaseShift]);
                        modifiedCodonArray = String.valueOf(modifiedCodonArray).toLowerCase().toCharArray();
                        modifiedCodonArray[variantPhaseShift] = Character.toUpperCase(modifiedCodonArray[variantPhaseShift]);
                        consequenceTypeTemplate.setCodon(String.valueOf(referenceCodonArray) + "/" + String.valueOf(modifiedCodonArray));
                    }
                }
            }
        }
        if(!codingAnnotationAdded) {
            SoNames.add("coding_sequence_variant");
        }
    }

    private void solveCodingPositiveTranscriptEffect(Boolean splicing, String transcriptSequence, Integer transcriptStart, Integer transcriptEnd, Integer genomicCodingStart,
                                                     Integer genomicCodingEnd, Integer variantStart, Integer variantEnd,
                                                     Integer cdnaCodingStart, Integer cdnaCodingEnd, Integer cdnaVariantStart,
                                                     Integer cdnaVariantEnd, String variantRef, String variantAlt,
                                                     HashSet<String> SoNames, ConsequenceType consequenceTypeTemplate) {
        if(variantStart < genomicCodingStart) {
            if(transcriptStart<genomicCodingStart) { // Check transcript has 5 UTR
                SoNames.add("5_prime_UTR_variant");
            }
            if(variantEnd >= genomicCodingStart) {  // Deletion that removes initiator codon
                SoNames.add("initiator_codon_variant");
//                SoNames.add("coding_sequence_variant");
            }
        } else {
            if(variantStart <= genomicCodingEnd) {  // Variant start within coding region
                if(cdnaVariantStart!=null) {  // cdnaVariantStart may be null if variantStart falls in an intron
                    int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                    consequenceTypeTemplate.setCdsPosition(cdsVariantStart);
                    consequenceTypeTemplate.setaPosition((cdsVariantStart - 1) / 3);
                }
//                SoNames.add("coding_sequence_variant");
                if(variantEnd <= genomicCodingEnd) {  // Variant end also within coding region
                    solvePositiveCodingEffect(splicing, transcriptSequence, cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart,
                            cdnaVariantEnd, variantRef, variantAlt, SoNames, consequenceTypeTemplate);
                } else {
                    if(transcriptEnd>genomicCodingEnd) {// Check transcript has 3 UTR)
                        SoNames.add("3_prime_UTR_variant");
                    }
                    SoNames.add("stop_lost");
                }
            } else {
                if(transcriptEnd>genomicCodingEnd) {// Check transcript has 3 UTR)
                    SoNames.add("3_prime_UTR_variant");
                }
            }
        }
    }

    private void solveCodingNegativeTranscriptEffect(Boolean splicing, String transcriptSequence, Integer transcriptStart, Integer transcriptEnd, Integer genomicCodingStart,
                                                     Integer genomicCodingEnd, Integer variantStart, Integer variantEnd,
                                                     Integer cdnaCodingStart, Integer cdnaCodingEnd, Integer cdnaVariantStart,
                                                     Integer cdnaVariantEnd, String variantRef, String variantAlt,
                                                     HashSet<String> SoNames, ConsequenceType consequenceTypeTemplate) {
        if(variantEnd > genomicCodingEnd) {
            if(transcriptEnd>genomicCodingEnd) { // Check transcript has 5 UTR
                SoNames.add("5_prime_UTR_variant");
            }
            if(variantStart <= genomicCodingEnd) {  // Deletion that removes initiator codon
                SoNames.add("initiator_codon_variant");
//                SoNames.add("coding_sequence_variant");
            }
        } else {
            if(variantEnd >= genomicCodingStart) {  // Variant end within coding region
                if(cdnaVariantStart!=null) {  // cdnaVariantStart may be null if variantEnd falls in an intron
                    int cdsVariantStart = cdnaVariantStart - cdnaCodingStart + 1;
                    consequenceTypeTemplate.setCdsPosition(cdsVariantStart);
                    consequenceTypeTemplate.setaPosition((cdsVariantStart - 1) / 3);
                }
//                SoNames.add("coding_sequence_variant");
                if(variantStart >= genomicCodingStart) {  // Variant start also within coding region
                    solveNegativeCodingEffect(splicing, transcriptSequence, cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart,
                            cdnaVariantEnd, variantRef, variantAlt, SoNames, consequenceTypeTemplate);
                } else {
                    if(transcriptStart<genomicCodingStart) {// Check transcript has 3 UTR)
                        SoNames.add("3_prime_UTR_variant");
                    }
                    SoNames.add("stop_lost");
                }
            } else {
                if(transcriptStart>genomicCodingStart) {// Check transcript has 3 UTR)
                    SoNames.add("3_prime_UTR_variant");
                }
            }
        }
    }

    private void solveJunction(Integer spliceSite1, Integer spliceSite2, Integer variantStart, Integer variantEnd, HashSet<String> SoNames,
                                                String leftSpliceSiteTag, String rightSpliceSiteTag, Boolean[] junctionSolution) {
//        Boolean splicing = false;
//        Boolean intron = false;
//        Boolean notdonor = true;
//        Boolean notacceptor = true;

        junctionSolution[0] = false;
        junctionSolution[1] = false;
        if(regionsOverlap(spliceSite1,spliceSite2,variantStart,variantEnd)) {
            SoNames.add("intron_variant");
//            intron = true;
            if(variantStart>=spliceSite1 && variantEnd<=spliceSite2) {
                junctionSolution[1] = true;
            }
        }
        if(regionsOverlap(spliceSite1-3,spliceSite1+7,variantStart,variantEnd)) {
            SoNames.add("splice_region_variant");
            junctionSolution[0] = true;
            if(regionsOverlap(spliceSite1,spliceSite1+1,variantStart,variantEnd)) {
                SoNames.add(leftSpliceSiteTag);  // donor/acceptor depending on transcript strand
//                notdonor = false;
            }
        }
        if(regionsOverlap(spliceSite2-7,spliceSite2+3,variantStart,variantEnd)) {
            SoNames.add("splice_region_variant");
            junctionSolution[0] = true;
            if(regionsOverlap(spliceSite2-1,spliceSite2,variantStart,variantEnd)) {
                SoNames.add(rightSpliceSiteTag);  // donor/acceptor depending on transcript strand
//                notacceptor = false;
            }
        }
    }

    @Override
    public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options) {

        Logger logger = LoggerFactory.getLogger(this.getClass());

        HashSet<String> SoNames = new HashSet<>();
        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        QueryResult queryResult = new QueryResult();
        QueryBuilder builderGene = null;
        QueryBuilder builderRegulatory = null;
        BasicDBList transcriptInfoList, exonInfoList;
        BasicDBObject transcriptInfo, exonInfo;
        BasicDBObject geneInfo;
        BasicDBObject regulatoryInfo;
        Integer geneStart, geneEnd, transcriptStart, transcriptEnd, exonStart, exonEnd, genomicCodingStart, genomicCodingEnd;
        Integer cdnaCodingStart, cdnaCodingEnd, cdnaExonStart, cdnaExonEnd, cdnaVariantStart, cdnaVariantEnd, prevSpliceSite;
        Integer regulatoryStart, regulatoryEnd;
        Integer variantStart = variant.getPosition();
        Integer variantEnd = variant.getPosition()+variant.getReference().length()-1;  //TODO: Check deletion input format to ensure that variantEnd is correctly calculated
        String geneStrand, transcriptStrand, exonSequence, transcriptSequence;
        String regulatoryChromosome, regulatoryType;
        String nextCodonNucleotides = "";
        String ensemblTranscriptId;
        String geneName;
        String ensemblGeneId;
        int transcriptBiotype;
        long dbTimeStart, dbTimeEnd;
        Boolean splicing, coding, exonsRemain, variantAhead, exonVariant, TFBSFound;
        int exonCounter,i;
        ConsequenceType consequenceTypeTemplate = new ConsequenceType();

        // Get all genes surrounding the variant +-5kb
        builderGene = QueryBuilder.start("chromosome").is(variant.getChromosome()).and("end")
                .greaterThanEquals(variant.getPosition()-5000).and("start").lessThanEquals(variantEnd+5000); // variantEnd is used rather than variant.getPosition() to account for deletions which end falls within the 5kb left area of the gene

        // Get all regulatory regions surrounding the variant
        String chunkId = getChunkPrefix(variant.getChromosome(), variant.getPosition(), regulatoryChunkSize);
        BasicDBList chunksId = new BasicDBList();
        chunksId.add(chunkId);
        builderRegulatory = QueryBuilder.start("chunkIds").in(chunksId).and("start").lessThanEquals(variantEnd).and("end")
                .greaterThanEquals(variant.getPosition()); // variantEnd is used rather than variant.getPosition() to account for deletions which end falls within the 5kb left area of the gene

        // Execute query and calculate time
        mongoDBCollection = db.getCollection("gene");
        dbTimeStart = System.currentTimeMillis();
        QueryResult geneQueryResult = executeQuery(variant.toString(), builderGene.get(), options);
        mongoDBCollection = db.getCollection("regulatory_region");
        QueryResult regulatoryQueryResult = executeQuery(variant.toString(), builderRegulatory.get(), options);
        dbTimeEnd = System.currentTimeMillis();
        BasicDBList geneInfoList = (BasicDBList) geneQueryResult.getResult();


        for(Object geneInfoObject: geneInfoList) {
            geneInfo = (BasicDBObject) geneInfoObject;
            geneStart = (Integer) geneInfo.get("start");
            geneEnd = (Integer) geneInfo.get("end");
            geneStrand = (String) geneInfo.get("strand");
            geneName = (String) geneInfo.get("name");
            ensemblGeneId = (String) geneInfo.get("id");
            consequenceTypeTemplate.setGeneName((String) geneInfo.get("name"));
            consequenceTypeTemplate.setEnsemblGeneId((String) geneInfo.get("id"));


            transcriptInfoList = (BasicDBList) geneInfo.get("transcripts");
            for(Object transcriptInfoObject: transcriptInfoList) {
                transcriptInfo = (BasicDBObject) transcriptInfoObject;
                ensemblTranscriptId = (String) transcriptInfo.get("id");
                transcriptStart = (Integer) transcriptInfo.get("start");
                transcriptEnd = (Integer) transcriptInfo.get("end");
                transcriptStrand = (String) transcriptInfo.get("strand");
                try {
                    transcriptBiotype = biotypes.get((String) transcriptInfo.get("biotype"));
                } catch (NullPointerException e) {
//                    logger.info("WARNING: biotype not found within the list of hardcoded biotypes - "+transcriptInfo.get("biotype"));
//                    logger.info("WARNING: transcript: "+ensemblTranscriptId);
//                    logger.info("WARNING: setting transcript biotype to non_coding ");
                    transcriptBiotype = 45;
                }
                SoNames.clear();
                consequenceTypeTemplate.setEnsemblTranscriptId(ensemblTranscriptId);
                consequenceTypeTemplate.setcDnaPosition(null);
                consequenceTypeTemplate.setCdsPosition(null);
                consequenceTypeTemplate.setaPosition(null);
                consequenceTypeTemplate.setaChange(null);
                consequenceTypeTemplate.setCodon(null);
                consequenceTypeTemplate.setStrand((String) geneInfo.get("strand"));
                consequenceTypeTemplate.setBiotype((String) transcriptInfo.get("biotype"));

                if(transcriptStrand.equals("+")) {
                    solveTranscriptFlankingRegions(SoNames, transcriptStart, transcriptEnd, variantStart, variantEnd,
                            "upstream_gene_variant", "downstream_gene_variant");

                    // Check variant falls within transcript start/end coordinates
                    if(regionsOverlap(transcriptStart,transcriptEnd,variantStart,variantEnd)) {
                        if(variant.getAlternative().equals("-")){  // Deletion
                            SoNames.add("feature_truncation");
                        } else if (variant.getReference().equals("-")) { // Insertion
                            SoNames.add("feature_elongation");
                        }
                        switch (transcriptBiotype) {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 20:
                            case 23:
                            case 24:
                            case 35:
                            case 36:
                            case 51:    // LRG_gene
                                solveCodingPositiveTranscript(variant, SoNames, transcriptInfo, transcriptStart,
                                        transcriptEnd, variantStart, variantEnd, consequenceTypeTemplate);
                                for(String SoName : SoNames) {
                                    consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                            consequenceTypeTemplate.getEnsemblGeneId(),
                                            consequenceTypeTemplate.getEnsemblTranscriptId(),
                                            consequenceTypeTemplate.getStrand(),
                                            consequenceTypeTemplate.getBiotype(),
                                            consequenceTypeTemplate.getcDnaPosition(),
                                            consequenceTypeTemplate.getCdsPosition(),
                                            consequenceTypeTemplate.getaPosition(),
                                            consequenceTypeTemplate.getaChange(),
                                            consequenceTypeTemplate.getCodon(), SoName));
                                }
                                break;
                            case 30:
                                SoNames.add("NMD_transcript_variant");
                            case 0:
                            case 16:    // antisense
                            case 17:
                            case 18:
                            case 19:
                            case 21:  // processed_pseudogene
                            case 22:  // processed_transcript
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 31:  // unprocessed_pseudogene
                            case 32:  // transcribed_unprocessed_pseudogene
                            case 37:  // transcribed_processed_pseudogene
                            case 33:
                            case 34:
                            case 38:
                            case 39:
                            case 40:
                            case 41:
                            case 42:
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                            case 50:
                                exonVariant = solveNonCodingPositiveTranscript(variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, variantStart, variantEnd, consequenceTypeTemplate);
                                if(exonVariant) {
                                    if (transcriptBiotype == 18) {
                                        SoNames.add("mature_miRNA_variant");
                                    } else {
                                        SoNames.add("non_coding_transcript_exon_variant");
                                    }
                                } else {
                                    SoNames.add("non_coding_transcript_variant");
                                }
                                for(String SoName : SoNames) {
                                    consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                            consequenceTypeTemplate.getEnsemblGeneId(),
                                            consequenceTypeTemplate.getEnsemblTranscriptId(),
                                            consequenceTypeTemplate.getStrand(),
                                            consequenceTypeTemplate.getBiotype(),
                                            consequenceTypeTemplate.getcDnaPosition(), SoName));
                                }
                                break;
                        }
                    } else { // Variant does not overlap gene region, just has upstream/downstream annotations
                        for(String SoName : SoNames) {
                            consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                    consequenceTypeTemplate.getEnsemblGeneId(),
                                    consequenceTypeTemplate.getEnsemblTranscriptId(),
                                    consequenceTypeTemplate.getStrand(),
                                    consequenceTypeTemplate.getBiotype(), SoName));
                        }
                    }
                } else {
                    solveTranscriptFlankingRegions(SoNames, transcriptStart, transcriptEnd, variantStart,
                            variantEnd, "downstream_gene_variant",
                            "upstream_gene_variant");
                    // Check variant falls within transcript start/end coordinates
                    if(regionsOverlap(transcriptStart,transcriptEnd,variantStart,variantEnd)) {
                        if(variant.getAlternative().equals("-")){  // Deletion
                            SoNames.add("feature_truncation");
                        } else if (variant.getReference().equals("-")) { // Insertion
                            SoNames.add("feature_elongation");
                        }
                        switch (transcriptBiotype) {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 20:
                            case 23:
                            case 24:
                            case 35:
                            case 36:
                            case 51:    // LRG_gene
                                solveCodingNegativeTranscript(variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, variantStart, variantEnd, consequenceTypeTemplate);
                                for(String SoName : SoNames) {
                                    consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                            consequenceTypeTemplate.getEnsemblGeneId(),
                                            consequenceTypeTemplate.getEnsemblTranscriptId(),
                                            consequenceTypeTemplate.getStrand(),
                                            consequenceTypeTemplate.getBiotype(),
                                            consequenceTypeTemplate.getcDnaPosition(),
                                            consequenceTypeTemplate.getCdsPosition(),
                                            consequenceTypeTemplate.getaPosition(),
                                            consequenceTypeTemplate.getaChange(),
                                            consequenceTypeTemplate.getCodon(), SoName));
                                }
                                break;
                            case 30:
                                SoNames.add("NMD_transcript_variant");
                            case 0:
                            case 16:  // antisense
                            case 17:
                            case 18:
                            case 19:
                            case 21:  // processed_pseudogene
                            case 22:  // processed_transcript
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 31:  // unprocessed_pseudogene
                            case 32:  // transcribed_unprocessed_pseudogene
                            case 37:  // transcribed_processed_pseudogene
                            case 33:
                            case 34:
                            case 38:
                            case 39:
                            case 40:
                            case 41:
                            case 42:
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                            case 50:
                                exonVariant = solveNonCodingNegativeTranscript(variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, variantStart, variantEnd, consequenceTypeTemplate);
                                if(exonVariant) {
                                    if (transcriptBiotype == 18) {
                                        SoNames.add("mature_miRNA_variant");
                                    } else {
                                        SoNames.add("non_coding_transcript_exon_variant");
                                    }
                                } else {
                                    SoNames.add("non_coding_transcript_variant");
                                }
                                for(String SoName : SoNames) {
                                    consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                            consequenceTypeTemplate.getEnsemblGeneId(),
                                            consequenceTypeTemplate.getEnsemblTranscriptId(),
                                            consequenceTypeTemplate.getStrand(),
                                            consequenceTypeTemplate.getBiotype(),
                                            consequenceTypeTemplate.getcDnaPosition(), SoName));
                                }
                                break;
                        }
                    } else { // Variant does not overlap gene region, just has upstream/downstream annotations
                        for(String SoName : SoNames) {
                            consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                    consequenceTypeTemplate.getEnsemblGeneId(),
                                    consequenceTypeTemplate.getEnsemblTranscriptId(),
                                    consequenceTypeTemplate.getStrand(),
                                    consequenceTypeTemplate.getBiotype(), SoName));
                        }
                    }

                }
            }
        }

        BasicDBList regulatoryInfoList = (BasicDBList) regulatoryQueryResult.getResult();
        if(!regulatoryInfoList.isEmpty()) {
            consequenceTypeList.add(new ConsequenceType("regulatory_region_variant"));
            i = 0;
            do {
                regulatoryInfo = (BasicDBObject) regulatoryInfoList.get(i);
                regulatoryType = (String) regulatoryInfo.get("featureType");
                TFBSFound = regulatoryType.equals("TF_binding_site") || regulatoryType.equals("TF_binding_site_motif");
                i++;
            } while(i<regulatoryInfoList.size() && !TFBSFound);
            if(TFBSFound) {
                consequenceTypeList.add(new ConsequenceType("TF_binding_site_variant"));
            }
        } else {
            int b;
            b = 1;
        }

        if(consequenceTypeList.size()==0) {
            consequenceTypeList.add(new ConsequenceType("intergenic_variant"));
        }

        // setting queryResult fields
        queryResult.setId(variant.toString());
        queryResult.setDBTime((dbTimeEnd - dbTimeStart));
        queryResult.setNumResults(consequenceTypeList.size());
        queryResult.setResult(consequenceTypeList);

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

    private void solveTranscriptFlankingRegions(HashSet<String> SoNames, Integer transcriptStart,
                                                Integer transcriptEnd, Integer variantStart, Integer variantEnd,
                                                String leftRegionTag, String rightRegionTag) {
        // Variant overlaps with -5kb region
        if(regionsOverlap(transcriptStart-5000, transcriptStart-1, variantStart, variantEnd)) {
            // Variant overlaps with -2kb region
            if(regionsOverlap(transcriptStart-2000, transcriptStart-1, variantStart, variantEnd)) {
                SoNames.add("2KB_" + leftRegionTag);
            } else {
                SoNames.add(leftRegionTag);
            }
        }
        // Variant overlaps with +5kb region
        if(regionsOverlap(transcriptEnd+1, transcriptEnd+5000, variantStart, variantEnd)) {
            // Variant overlaps with +2kb region
            if(regionsOverlap(transcriptEnd+1, transcriptEnd+2000, variantStart, variantEnd)) {
                SoNames.add("2KB_" + rightRegionTag);
            } else {
                SoNames.add(rightRegionTag);
            }
        }
    }

    private void solveCodingPositiveTranscript(GenomicVariant variant, HashSet<String> SoNames,
                                               BasicDBObject transcriptInfo, Integer transcriptStart,
                                               Integer transcriptEnd, Integer variantStart, Integer variantEnd,
                                               ConsequenceType consequenceTypeTemplate) {
        Integer genomicCodingStart;
        Integer genomicCodingEnd;
        Integer cdnaCodingStart;
        Integer cdnaCodingEnd;
        BasicDBList exonInfoList;
        BasicDBObject exonInfo;
        Integer exonStart;
        Integer exonEnd;
        String transcriptSequence;
        Boolean variantAhead;
        Integer cdnaExonEnd;
        Integer cdnaVariantStart;
        Integer cdnaVariantEnd;
        Boolean splicing;
        int exonCounter;
        Integer prevSpliceSite;
        Boolean[] junctionSolution = {false, false};

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
                consequenceTypeTemplate.setcDnaPosition(cdnaVariantStart);
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
        while(exonCounter<exonInfoList.size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
//        while(exonCounter<exonInfoList.size() && !splicing && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            exonInfo = (BasicDBObject) exonInfoList.get(exonCounter);          // next exon has been loaded
            exonStart = (Integer) exonInfo.get("start");
            prevSpliceSite = exonEnd+1;
            exonEnd = (Integer) exonInfo.get("end");
            transcriptSequence = transcriptSequence + ((String) exonInfo.get("sequence"));
            solveJunction(prevSpliceSite, exonStart-1, variantStart, variantEnd, SoNames,
                    "splice_donor_variant", "splice_acceptor_variant", junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variantStart >= exonStart) {
                cdnaExonEnd += (exonEnd - exonStart + 1);
                if(variantStart <= exonEnd) {  // Variant start within the exon
                    cdnaVariantStart = cdnaExonEnd - (exonEnd - variantStart);
                    consequenceTypeTemplate.setcDnaPosition(cdnaVariantStart);
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
            solveCodingPositiveTranscriptEffect(splicing, transcriptSequence, transcriptStart, transcriptEnd, genomicCodingStart, genomicCodingEnd,
                    variantStart, variantEnd, cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart, cdnaVariantEnd,
                    variant.getReference(), variant.getAlternative(), SoNames, consequenceTypeTemplate);
        }
    }

    private void solveCodingNegativeTranscript(GenomicVariant variant, HashSet<String> SoNames,
                                               BasicDBObject transcriptInfo, Integer transcriptStart,
                                               Integer transcriptEnd, Integer variantStart, Integer variantEnd,
                                               ConsequenceType consequenceTypeTemplate) {
        Integer genomicCodingStart;
        Integer genomicCodingEnd;
        Integer cdnaCodingStart;
        Integer cdnaCodingEnd;
        BasicDBList exonInfoList;
        BasicDBObject exonInfo;
        Integer exonStart;
        Integer exonEnd;
        String transcriptSequence;
        Boolean variantAhead;
        Integer cdnaExonEnd;
        Integer cdnaVariantStart;
        Integer cdnaVariantEnd;
        Boolean splicing;
        int exonCounter;
        Integer prevSpliceSite;
        Boolean[] junctionSolution = {false, false};

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
        cdnaExonEnd = (exonEnd-exonStart+1);  // cdnaExonEnd poinst to the same base than exonStart
        cdnaVariantStart = null;  // cdnaVariantStart points to the same base than variantEnd
        cdnaVariantEnd = null;    // cdnaVariantEnd points to the same base than variantStart
        junctionSolution[0] = false;
        junctionSolution[1] = false;
        splicing = false;

        if(variantEnd <= exonEnd) {
            if(variantEnd >= exonStart) {  // Variant end within the exon
                cdnaVariantStart = cdnaExonEnd - (variantEnd - exonStart);
                consequenceTypeTemplate.setcDnaPosition(cdnaVariantStart);
                if(variantStart >= exonStart) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                    cdnaVariantEnd = cdnaExonEnd - (variantStart - exonStart);
                }
            }
        } else {
            if(variantStart >= exonStart) {
//                                if(variantEnd >= exonStart) {  // Only variant end within the exon  ----||||||||||E||||----
                // We do not contemplate that variant end can be located before this exon since this is the first exon
                cdnaVariantEnd = cdnaExonEnd - (variantEnd - exonStart);
//                                }
            } // Variant includes the whole exon. Variant end is located before the exon, variant start is located after the exon
        }

        exonCounter = 1;
        while(exonCounter<exonInfoList.size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
//        while(exonCounter<exonInfoList.size() && !splicing && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            exonInfo = (BasicDBObject) exonInfoList.get(exonCounter);          // next exon has been loaded
            prevSpliceSite = exonStart-1;
            exonStart = (Integer) exonInfo.get("start");
            exonEnd = (Integer) exonInfo.get("end");
            transcriptSequence = ((String) exonInfo.get("sequence"))+transcriptSequence;
            solveJunction(exonEnd+1, prevSpliceSite, variantStart, variantEnd, SoNames,
                    "splice_acceptor_variant", "splice_donor_variant", junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variantEnd <= exonEnd) {
                cdnaExonEnd += (exonEnd - exonStart + 1);
                if(variantEnd >= exonStart) {  // Variant end within the exon
                    cdnaVariantStart = cdnaExonEnd - (variantEnd - exonStart);
                    consequenceTypeTemplate.setcDnaPosition(cdnaVariantStart);
                    if(variantStart >= exonStart) {  // Both variant start and variant end within the exon  ----||||S|||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exonStart);
                    }
                }
            } else {
                if(variantStart >= exonStart) {
                    if(variantStart <= exonEnd) {  // Only variant start within the exon  ----||||||||||E||||----
                        cdnaVariantEnd = cdnaExonEnd - (variantStart - exonStart);
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
            solveCodingNegativeTranscriptEffect(splicing, transcriptSequence, transcriptStart, transcriptEnd, genomicCodingStart, genomicCodingEnd,
                    variantStart, variantEnd, cdnaCodingStart, cdnaCodingEnd, cdnaVariantStart, cdnaVariantEnd,
                    variant.getReference(), variant.getAlternative(), SoNames, consequenceTypeTemplate);
        }
    }

    private Boolean solveNonCodingPositiveTranscript(GenomicVariant variant, HashSet<String> SoNames,
                                                     BasicDBObject transcriptInfo, Integer transcriptStart,
                                                     Integer transcriptEnd, Integer variantStart, Integer variantEnd,
                                                     ConsequenceType consequenceTypeTemplate) {
        BasicDBList exonInfoList;
        BasicDBObject exonInfo;
        Integer exonStart;
        Integer exonEnd;
        String transcriptSequence;
        Boolean variantAhead;
        Integer cdnaExonEnd;
        Boolean splicing;
        int exonCounter;
        Integer prevSpliceSite;
        Boolean[] junctionSolution = {false, false};

        exonInfoList = (BasicDBList) transcriptInfo.get("exons");
        exonInfo = (BasicDBObject) exonInfoList.get(0);
        exonStart = (Integer) exonInfo.get("start");
        exonEnd = (Integer) exonInfo.get("end");
        transcriptSequence = (String) exonInfo.get("sequence");
        variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        cdnaExonEnd = (exonEnd - exonStart + 1);
        junctionSolution[0] = false;
        junctionSolution[1] = false;
        splicing = false;

        if(variantStart >= exonStart) {
            if(variantStart <= exonEnd) {  // Variant start within the exon. Set cdnaPosition in consequenceTypeTemplate
                consequenceTypeTemplate.setcDnaPosition(cdnaExonEnd - (exonEnd - variantStart));
            }
        }

        exonCounter = 1;
        while(exonCounter<exonInfoList.size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
//        while(exonCounter<exonInfoList.size() && !splicing && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            exonInfo = (BasicDBObject) exonInfoList.get(exonCounter);          // next exon has been loaded
            exonStart = (Integer) exonInfo.get("start");
            prevSpliceSite = exonEnd+1;
            exonEnd = (Integer) exonInfo.get("end");
            transcriptSequence = transcriptSequence + ((String) exonInfo.get("sequence"));
            solveJunction(prevSpliceSite, exonStart-1, variantStart, variantEnd, SoNames,
                    "splice_donor_variant", "splice_acceptor_variant", junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variantStart >= exonStart) {
                cdnaExonEnd += (exonEnd - exonStart + 1);
                if(variantStart <= exonEnd) {  // Variant start within the exon. Set cdnaPosition in consequenceTypeTemplate
                    consequenceTypeTemplate.setcDnaPosition(cdnaExonEnd - (exonEnd - variantStart));
                }
            } else {
                if(variantEnd <= exonEnd) {
                    if(variantEnd < exonStart) {  // Variant does not include this exon, variant is located before this exon
                        variantAhead = false;
                    }
                } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                    cdnaExonEnd += (exonEnd - exonStart + 1);
                }
            }
            exonCounter++;
        }

        return !junctionSolution[1];

    }

    private Boolean solveNonCodingNegativeTranscript(GenomicVariant variant, HashSet<String> SoNames,
                                                     BasicDBObject transcriptInfo, Integer transcriptStart,
                                                     Integer transcriptEnd, Integer variantStart, Integer variantEnd,
                                                     ConsequenceType consequenceTypeTemplate) {
        BasicDBList exonInfoList;
        BasicDBObject exonInfo;
        Integer exonStart;
        Integer exonEnd;
        String transcriptSequence;
        Boolean variantAhead;
        Integer cdnaExonEnd;
        Boolean splicing;
        int exonCounter;
        Integer prevSpliceSite;
        Boolean[] junctionSolution = {false, false};

        exonInfoList = (BasicDBList) transcriptInfo.get("exons");
        exonInfo = (BasicDBObject) exonInfoList.get(0);
        exonStart = (Integer) exonInfo.get("start");
        exonEnd = (Integer) exonInfo.get("end");
        transcriptSequence = (String) exonInfo.get("sequence");
        variantAhead = true; // we need a first iteration within the while to ensure junction is solved in case needed
        cdnaExonEnd = (exonEnd-exonStart+1);  // cdnaExonEnd poinst to the same base than exonStart
        junctionSolution[0] = false;
        junctionSolution[1] = false;
        splicing = false;

        if(variantEnd <= exonEnd) {
            if(variantEnd >= exonStart) {  // Variant end within the exon
                consequenceTypeTemplate.setcDnaPosition(cdnaExonEnd - (variantEnd - exonStart));
            }
        }

        exonCounter = 1;
        while(exonCounter<exonInfoList.size() && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
//        while(exonCounter<exonInfoList.size() && !splicing && variantAhead) {  // This is not a do-while since we cannot call solveJunction  until
            exonInfo = (BasicDBObject) exonInfoList.get(exonCounter);          // next exon has been loaded
            prevSpliceSite = exonStart-1;
            exonStart = (Integer) exonInfo.get("start");
            exonEnd = (Integer) exonInfo.get("end");
            transcriptSequence = ((String) exonInfo.get("sequence"))+transcriptSequence;
            solveJunction(exonEnd+1, prevSpliceSite, variantStart, variantEnd, SoNames,
                    "splice_acceptor_variant", "splice_donor_variant", junctionSolution);
            splicing = (splicing || junctionSolution[0]);

            if(variantEnd <= exonEnd) {
                cdnaExonEnd += (exonEnd - exonStart + 1);
                if(variantEnd >= exonStart) {  // Variant end within the exon
                    consequenceTypeTemplate.setcDnaPosition(cdnaExonEnd - (variantEnd - exonStart));
                }
            } else {
                if(variantStart >= exonStart) {
                    if(variantStart > exonEnd) {  // Variant does not include this exon, variant is located before this exon
                        variantAhead = false;
                    }
                } else {  // Variant includes the whole exon. Variant start is located before the exon, variant end is located after the exon
                    cdnaExonEnd += (exonEnd - exonStart + 1);
                }
            }
            exonCounter++;
        }

        return !junctionSolution[1];

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
