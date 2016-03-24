package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.variant.annotation.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.annotation.exceptions.SOTermNotAvailableException;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;

import java.util.*;

/**
 * Created by fjlopez on 22/06/15.
 */
public class VariantAnnotationUtils {

    public static final String THREEPRIME_OVERLAPPING_NCRNA = "3prime_overlapping_ncrna";
    public static final String IG_C_GENE = "IG_C_gene";
    public static final String IG_C_PSEUDOGENE = "IG_C_pseudogene";
    public static final String IG_D_GENE = "IG_D_gene";
    public static final String IG_J_GENE = "IG_J_gene";
    public static final String IG_J_PSEUDOGENE = "IG_J_pseudogene";
    public static final String IG_V_GENE = "IG_V_gene";
    public static final String IG_V_PSEUDOGENE = "IG_V_pseudogene";
    public static final String MT_RRNA = "Mt_rRNA";
    public static final String MT_TRNA = "Mt_tRNA";
    public static final String TR_C_GENE = "TR_C_gene";
    public static final String TR_D_GENE = "TR_D_gene";
    public static final String TR_J_GENE = "TR_J_gene";
    public static final String TR_J_PSEUDOGENE = "TR_J_pseudogene";
    public static final String TR_V_GENE = "TR_V_gene";
    public static final String TR_V_PSEUDOGENE = "TR_V_pseudogene";
    public static final String ANTISENSE = "antisense";
    public static final String LINCRNA = "lincRNA";
    public static final String MIRNA = "miRNA";
    public static final String MISC_RNA = "misc_RNA";
    public static final String POLYMORPHIC_PSEUDOGENE = "polymorphic_pseudogene";
    public static final String PROCESSED_PSEUDOGENE = "processed_pseudogene";
    public static final String PROCESSED_TRANSCRIPT = "processed_transcript";
    public static final String PROTEIN_CODING = "protein_coding";
    public static final String PSEUDOGENE = "pseudogene";
    public static final String RRNA = "rRNA";
    public static final String SENSE_INTRONIC = "sense_intronic";
    public static final String SENSE_OVERLAPPING = "sense_overlapping";
    public static final String SNRNA = "snRNA";
    public static final String SNORNA = "snoRNA";
    public static final String NONSENSE_MEDIATED_DECAY = "nonsense_mediated_decay";
    public static final String NMD_TRANSCRIPT_VARIANT = "NMD_transcript_variant";
    public static final String UNPROCESSED_PSEUDOGENE = "unprocessed_pseudogene";
    public static final String TRANSCRIBED_UNPROCESSED_PSEUDGENE = "transcribed_unprocessed_pseudogene";
    public static final String RETAINED_INTRON = "retained_intron";
    public static final String NON_STOP_DECAY = "non_stop_decay";
    public static final String UNITARY_PSEUDOGENE = "unitary_pseudogene";
    public static final String TRANSLATED_PROCESSED_PSEUDOGENE = "translated_processed_pseudogene";
    public static final String TRANSCRIBED_PROCESSED_PSEUDOGENE = "transcribed_processed_pseudogene";
    public static final String TRNA_PSEUDOGENE = "tRNA_pseudogene";
    public static final String SNORNA_PSEUDOGENE = "snoRNA_pseudogene";
    public static final String SNRNA_PSEUDOGENE = "snRNA_pseudogene";
    public static final String SCRNA_PSEUDOGENE = "scRNA_pseudogene";
    public static final String RRNA_PSEUDOGENE = "rRNA_pseudogene";
    public static final String MISC_RNA_PSEUDOGENE = "misc_RNA_pseudogene";
    public static final String MIRNA_PSEUDOGENE = "miRNA_pseudogene";
    public static final String NON_CODING = "non_coding";
    public static final String AMBIGUOUS_ORF = "ambiguous_orf";
    public static final String KNOWN_NCRNA = "known_ncrna";
    public static final String RETROTRANSPOSED = "retrotransposed";
    public static final String TRANSCRIBED_UNITARY_PSEUDOGENE = "transcribed_unitary_pseudogene";
    public static final String TRANSLATED_UNPROCESSED_PSEUDOGENE = "translated_unprocessed_pseudogene";
    public static final String LRG_GENE = "LRG_gene";

    public static final String INTERGENIC_VARIANT = "intergenic_variant";
    public static final String REGULATORY_REGION_VARIANT = "regulatory_region_variant";
    public static final String TF_BINDING_SITE_VARIANT = "TF_binding_site_variant";
    public static final String UPSTREAM_GENE_VARIANT = "upstream_gene_variant";
    public static final String DOWNSTREAM_GENE_VARIANT = "downstream_gene_variant";
    public static final String SPLICE_DONOR_VARIANT = "splice_donor_variant";
    public static final String SPLICE_ACCEPTOR_VARIANT = "splice_acceptor_variant";
    public static final String INTRON_VARIANT = "intron_variant";
    public static final String SPLICE_REGION_VARIANT = "splice_region_variant";
    public static final String FIVE_PRIME_UTR_VARIANT = "5_prime_UTR_variant";
    public static final String THREE_PRIME_UTR_VARIANT = "3_prime_UTR_variant";
    public static final String INCOMPLETE_TERMINAL_CODON_VARIANT = "incomplete_terminal_codon_variant";
    public static final String STOP_RETAINED_VARIANT = "stop_retained_variant";
    public static final String SYNONYMOUS_VARIANT = "synonymous_variant";
    public static final String INITIATOR_CODON_VARIANT = "initiator_codon_variant";
    public static final String STOP_GAINED = "stop_gained";
    public static final String STOP_LOST = "stop_lost";
    public static final String MISSENSE_VARIANT = "missense_variant";
    public static final String MATURE_MIRNA_VARIANT = "mature_miRNA_variant";
    public static final String NON_CODING_TRANSCRIPT_EXON_VARIANT = "non_coding_transcript_exon_variant";
    public static final String NON_CODING_TRANSCRIPT_VARIANT = "non_coding_transcript_variant";
    public static final String INFRAME_INSERTION = "inframe_insertion";
    public static final String FRAMESHIFT_VARIANT = "frameshift_variant";
    public static final String CODING_SEQUENCE_VARIANT = "coding_sequence_variant";
    public static final String TRANSCRIPT_ABLATION = "transcript_ablation";
    public static final String FEATURE_TRUNCATION = "feature_truncation";
    public static final String INFRAME_DELETION = "inframe_deletion";

    public static final String CDS_START_NF = "cds_start_NF";
    public static final String CDS_END_NF = "cds_end_NF";

    public static final Map<String, Map<String, Boolean>> IS_SYNONYMOUS_CODON = new HashMap<>();
    public static final Map<String, Map<String, Boolean>> MT_IS_SYNONYMOUS_CODON = new HashMap<>();
    public static final Map<String, String> SO_NAMES_CORRECTIONS = new HashMap<>();
    public static final Map<String, List<String>> A_TO_CODON = new HashMap<>();
    public static final Map<String, List<String>> MT_A_TO_CODON = new HashMap<>();
    public static final Map<String, String> CODON_TO_A = new HashMap<>();
    public static final Map<String, String> MT_CODON_TO_A = new HashMap<>();
    public static final Map<Character, Character> COMPLEMENTARY_NT = new HashMap<>();
    public static final Map<Integer, String> SIFT_DESCRIPTIONS = new HashMap<>();
    public static final Map<Integer, String> POLYPHEN_DESCRIPTIONS = new HashMap<>();
    public static final Map<String, Integer> SO_SEVERITY = new HashMap<>();
    public static final Set<String> CODING_SO_NAMES = new HashSet<>();

    static {

        ///////////////////////////////////////////////////////////////////////
        /////   GENETIC CODE   ////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        A_TO_CODON.put("ALA", new ArrayList<String>());
        A_TO_CODON.get("ALA").add("GCT");
        A_TO_CODON.get("ALA").add("GCC");
        A_TO_CODON.get("ALA").add("GCA");
        A_TO_CODON.get("ALA").add("GCG");
        A_TO_CODON.put("ARG", new ArrayList<String>());
        A_TO_CODON.get("ARG").add("CGT");
        A_TO_CODON.get("ARG").add("CGC");
        A_TO_CODON.get("ARG").add("CGA");
        A_TO_CODON.get("ARG").add("CGG");
        A_TO_CODON.get("ARG").add("AGA");
        A_TO_CODON.get("ARG").add("AGG");
        A_TO_CODON.put("ASN", new ArrayList<String>());
        A_TO_CODON.get("ASN").add("AAT");
        A_TO_CODON.get("ASN").add("AAC");
        A_TO_CODON.put("ASP", new ArrayList<String>());
        A_TO_CODON.get("ASP").add("GAT");
        A_TO_CODON.get("ASP").add("GAC");
        A_TO_CODON.put("CYS", new ArrayList<String>());
        A_TO_CODON.get("CYS").add("TGT");
        A_TO_CODON.get("CYS").add("TGC");
        A_TO_CODON.put("GLN", new ArrayList<String>());
        A_TO_CODON.get("GLN").add("CAA");
        A_TO_CODON.get("GLN").add("CAG");
        A_TO_CODON.put("GLU", new ArrayList<String>());
        A_TO_CODON.get("GLU").add("GAA");
        A_TO_CODON.get("GLU").add("GAG");
        A_TO_CODON.put("GLY", new ArrayList<String>());
        A_TO_CODON.get("GLY").add("GGT");
        A_TO_CODON.get("GLY").add("GGC");
        A_TO_CODON.get("GLY").add("GGA");
        A_TO_CODON.get("GLY").add("GGG");
        A_TO_CODON.put("HIS", new ArrayList<String>());
        A_TO_CODON.get("HIS").add("CAT");
        A_TO_CODON.get("HIS").add("CAC");
        A_TO_CODON.put("ILE", new ArrayList<String>());
        A_TO_CODON.get("ILE").add("ATT");
        A_TO_CODON.get("ILE").add("ATC");
        A_TO_CODON.get("ILE").add("ATA");
        A_TO_CODON.put("LEU", new ArrayList<String>());
        A_TO_CODON.get("LEU").add("TTA");
        A_TO_CODON.get("LEU").add("TTG");
        A_TO_CODON.get("LEU").add("CTT");
        A_TO_CODON.get("LEU").add("CTC");
        A_TO_CODON.get("LEU").add("CTA");
        A_TO_CODON.get("LEU").add("CTG");
        A_TO_CODON.put("LYS", new ArrayList<String>());
        A_TO_CODON.get("LYS").add("AAA");
        A_TO_CODON.get("LYS").add("AAG");
        A_TO_CODON.put("MET", new ArrayList<String>());
        A_TO_CODON.get("MET").add("ATG");
        A_TO_CODON.put("PHE", new ArrayList<String>());
        A_TO_CODON.get("PHE").add("TTT");
        A_TO_CODON.get("PHE").add("TTC");
        A_TO_CODON.put("PRO", new ArrayList<String>());
        A_TO_CODON.get("PRO").add("CCT");
        A_TO_CODON.get("PRO").add("CCC");
        A_TO_CODON.get("PRO").add("CCA");
        A_TO_CODON.get("PRO").add("CCG");
        A_TO_CODON.put("SER", new ArrayList<String>());
        A_TO_CODON.get("SER").add("TCT");
        A_TO_CODON.get("SER").add("TCC");
        A_TO_CODON.get("SER").add("TCA");
        A_TO_CODON.get("SER").add("TCG");
        A_TO_CODON.get("SER").add("AGT");
        A_TO_CODON.get("SER").add("AGC");
        A_TO_CODON.put("THR", new ArrayList<String>());
        A_TO_CODON.get("THR").add("ACT");
        A_TO_CODON.get("THR").add("ACC");
        A_TO_CODON.get("THR").add("ACA");
        A_TO_CODON.get("THR").add("ACG");
        A_TO_CODON.put("TRP", new ArrayList<String>());
        A_TO_CODON.get("TRP").add("TGG");
        A_TO_CODON.put("TYR", new ArrayList<String>());
        A_TO_CODON.get("TYR").add("TAT");
        A_TO_CODON.get("TYR").add("TAC");
        A_TO_CODON.put("VAL", new ArrayList<String>());
        A_TO_CODON.get("VAL").add("GTT");
        A_TO_CODON.get("VAL").add("GTC");
        A_TO_CODON.get("VAL").add("GTA");
        A_TO_CODON.get("VAL").add("GTG");
        A_TO_CODON.put("STOP", new ArrayList<String>());
        A_TO_CODON.get("STOP").add("TAA");
        A_TO_CODON.get("STOP").add("TGA");
        A_TO_CODON.get("STOP").add("TAG");

        for (String aa : A_TO_CODON.keySet()) {
            for (String codon : A_TO_CODON.get(aa)) {
                IS_SYNONYMOUS_CODON.put(codon, new HashMap<String, Boolean>());
                CODON_TO_A.put(codon, aa);
            }
        }
        for (String codon1 : IS_SYNONYMOUS_CODON.keySet()) {
            Map<String, Boolean> codonEntry = IS_SYNONYMOUS_CODON.get(codon1);
            for (String codon2 : IS_SYNONYMOUS_CODON.keySet()) {
                codonEntry.put(codon2, false);
            }
        }
        for (String aa : A_TO_CODON.keySet()) {
            for (String codon1 : A_TO_CODON.get(aa)) {
                for (String codon2 : A_TO_CODON.get(aa)) {
                    IS_SYNONYMOUS_CODON.get(codon1).put(codon2, true);
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////
        /////   MITOCHONDRIAL GENETIC CODE   //////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        MT_A_TO_CODON.put("ALA", new ArrayList<String>());
        MT_A_TO_CODON.get("ALA").add("GCT");
        MT_A_TO_CODON.get("ALA").add("GCC");
        MT_A_TO_CODON.get("ALA").add("GCA");
        MT_A_TO_CODON.get("ALA").add("GCG");
        MT_A_TO_CODON.put("ARG", new ArrayList<String>());
        MT_A_TO_CODON.get("ARG").add("CGT");
        MT_A_TO_CODON.get("ARG").add("CGC");
        MT_A_TO_CODON.get("ARG").add("CGA");
        MT_A_TO_CODON.get("ARG").add("CGG");
        MT_A_TO_CODON.put("ASN", new ArrayList<String>());
        MT_A_TO_CODON.get("ASN").add("AAT");
        MT_A_TO_CODON.get("ASN").add("AAC");
        MT_A_TO_CODON.put("ASP", new ArrayList<String>());
        MT_A_TO_CODON.get("ASP").add("GAT");
        MT_A_TO_CODON.get("ASP").add("GAC");
        MT_A_TO_CODON.put("CYS", new ArrayList<String>());
        MT_A_TO_CODON.get("CYS").add("TGT");
        MT_A_TO_CODON.get("CYS").add("TGC");
        MT_A_TO_CODON.put("GLN", new ArrayList<String>());
        MT_A_TO_CODON.get("GLN").add("CAA");
        MT_A_TO_CODON.get("GLN").add("CAG");
        MT_A_TO_CODON.put("GLU", new ArrayList<String>());
        MT_A_TO_CODON.get("GLU").add("GAA");
        MT_A_TO_CODON.get("GLU").add("GAG");
        MT_A_TO_CODON.put("GLY", new ArrayList<String>());
        MT_A_TO_CODON.get("GLY").add("GGT");
        MT_A_TO_CODON.get("GLY").add("GGC");
        MT_A_TO_CODON.get("GLY").add("GGA");
        MT_A_TO_CODON.get("GLY").add("GGG");
        MT_A_TO_CODON.put("HIS", new ArrayList<String>());
        MT_A_TO_CODON.get("HIS").add("CAT");
        MT_A_TO_CODON.get("HIS").add("CAC");
        MT_A_TO_CODON.put("ILE", new ArrayList<String>());
        MT_A_TO_CODON.get("ILE").add("ATT");
        MT_A_TO_CODON.get("ILE").add("ATC");
        MT_A_TO_CODON.put("LEU", new ArrayList<String>());
        MT_A_TO_CODON.get("LEU").add("TTA");
        MT_A_TO_CODON.get("LEU").add("TTG");
        MT_A_TO_CODON.get("LEU").add("CTT");
        MT_A_TO_CODON.get("LEU").add("CTC");
        MT_A_TO_CODON.get("LEU").add("CTA");
        MT_A_TO_CODON.get("LEU").add("CTG");
        MT_A_TO_CODON.put("LYS", new ArrayList<String>());
        MT_A_TO_CODON.get("LYS").add("AAA");
        MT_A_TO_CODON.get("LYS").add("AAG");
        MT_A_TO_CODON.put("MET", new ArrayList<String>());
        MT_A_TO_CODON.get("MET").add("ATG");
        MT_A_TO_CODON.get("MET").add("ATA");
        MT_A_TO_CODON.put("PHE", new ArrayList<String>());
        MT_A_TO_CODON.get("PHE").add("TTT");
        MT_A_TO_CODON.get("PHE").add("TTC");
        MT_A_TO_CODON.put("PRO", new ArrayList<String>());
        MT_A_TO_CODON.get("PRO").add("CCT");
        MT_A_TO_CODON.get("PRO").add("CCC");
        MT_A_TO_CODON.get("PRO").add("CCA");
        MT_A_TO_CODON.get("PRO").add("CCG");
        MT_A_TO_CODON.put("SER", new ArrayList<String>());
        MT_A_TO_CODON.get("SER").add("TCT");
        MT_A_TO_CODON.get("SER").add("TCC");
        MT_A_TO_CODON.get("SER").add("TCA");
        MT_A_TO_CODON.get("SER").add("TCG");
        MT_A_TO_CODON.get("SER").add("AGT");
        MT_A_TO_CODON.get("SER").add("AGC");
        MT_A_TO_CODON.put("THR", new ArrayList<String>());
        MT_A_TO_CODON.get("THR").add("ACT");
        MT_A_TO_CODON.get("THR").add("ACC");
        MT_A_TO_CODON.get("THR").add("ACA");
        MT_A_TO_CODON.get("THR").add("ACG");
        MT_A_TO_CODON.put("TRP", new ArrayList<String>());
        MT_A_TO_CODON.get("TRP").add("TGG");
        MT_A_TO_CODON.get("TRP").add("TGA");
        MT_A_TO_CODON.put("TYR", new ArrayList<String>());
        MT_A_TO_CODON.get("TYR").add("TAT");
        MT_A_TO_CODON.get("TYR").add("TAC");
        MT_A_TO_CODON.put("VAL", new ArrayList<String>());
        MT_A_TO_CODON.get("VAL").add("GTT");
        MT_A_TO_CODON.get("VAL").add("GTC");
        MT_A_TO_CODON.get("VAL").add("GTA");
        MT_A_TO_CODON.get("VAL").add("GTG");
        MT_A_TO_CODON.put("STOP", new ArrayList<String>());
        MT_A_TO_CODON.get("STOP").add("TAA");
        MT_A_TO_CODON.get("STOP").add("TAG");
        MT_A_TO_CODON.get("STOP").add("AGA");
        MT_A_TO_CODON.get("STOP").add("AGG");

        for (String aa : MT_A_TO_CODON.keySet()) {
            for (String codon : MT_A_TO_CODON.get(aa)) {
                MT_IS_SYNONYMOUS_CODON.put(codon, new HashMap<String, Boolean>());
                MT_CODON_TO_A.put(codon, aa);
            }
        }
        for (String codon1 : MT_IS_SYNONYMOUS_CODON.keySet()) {
            Map<String, Boolean> codonEntry = MT_IS_SYNONYMOUS_CODON.get(codon1);
            for (String codon2 : MT_IS_SYNONYMOUS_CODON.keySet()) {
                codonEntry.put(codon2, false);
            }
        }
        for (String aa : MT_A_TO_CODON.keySet()) {
            for (String codon1 : MT_A_TO_CODON.get(aa)) {
                for (String codon2 : MT_A_TO_CODON.get(aa)) {
                    MT_IS_SYNONYMOUS_CODON.get(codon1).put(codon2, true);
                }
            }
        }

        COMPLEMENTARY_NT.put('A', 'T');
        COMPLEMENTARY_NT.put('C', 'G');
        COMPLEMENTARY_NT.put('G', 'C');
        COMPLEMENTARY_NT.put('T', 'A');

        POLYPHEN_DESCRIPTIONS.put(0, "probably damaging");
        POLYPHEN_DESCRIPTIONS.put(1, "possibly damaging");
        POLYPHEN_DESCRIPTIONS.put(2, "benign");
        POLYPHEN_DESCRIPTIONS.put(3, "unknown");

        SIFT_DESCRIPTIONS.put(0, "tolerated");
        SIFT_DESCRIPTIONS.put(1, "deleterious");

        SO_SEVERITY.put("transcript_ablation", 36);
        SO_SEVERITY.put("splice_acceptor_variant", 35);
        SO_SEVERITY.put("splice_donor_variant", 34);
        SO_SEVERITY.put("stop_gained", 33);
        SO_SEVERITY.put("frameshift_variant", 32);
        SO_SEVERITY.put("stop_lost", 31);
        SO_SEVERITY.put("initiator_codon_variant", 30);
        SO_SEVERITY.put("transcript_amplification", 29);
        SO_SEVERITY.put("inframe_insertion", 28);
        SO_SEVERITY.put("inframe_deletion", 27);
        SO_SEVERITY.put("missense_variant", 26);
        SO_SEVERITY.put("splice_region_variant", 25);
        SO_SEVERITY.put("incomplete_terminal_codon_variant", 24);
        SO_SEVERITY.put("stop_retained_variant", 23);
        SO_SEVERITY.put("synonymous_variant", 22);
        SO_SEVERITY.put("coding_sequence_variant", 21);
        SO_SEVERITY.put("mature_miRNA_variant", 20);
        SO_SEVERITY.put("5_prime_UTR_variant", 19);
        SO_SEVERITY.put("3_prime_UTR_variant", 18);
        SO_SEVERITY.put("non_coding_transcript_exon_variant", 17);
        SO_SEVERITY.put("intron_variant", 16);
        SO_SEVERITY.put("NMD_transcript_variant", 15);
        SO_SEVERITY.put("non_coding_transcript_variant", 14);
        SO_SEVERITY.put("2KB_upstream_gene_variant", 13);
        SO_SEVERITY.put("upstream_gene_variant", 12);
        SO_SEVERITY.put("2KB_downstream_gene_variant", 11);
        SO_SEVERITY.put("downstream_gene_variant", 10);
        SO_SEVERITY.put("TFBS_ablation", 9);
        SO_SEVERITY.put("TFBS_amplification", 8);
        SO_SEVERITY.put("TF_binding_site_variant", 7);
        SO_SEVERITY.put("regulatory_region_ablation", 6);
        SO_SEVERITY.put("regulatory_region_amplification", 5);
        SO_SEVERITY.put("regulatory_region_variant", 4);
        SO_SEVERITY.put("feature_elongation", 3);
        SO_SEVERITY.put("feature_truncation", 2);
        SO_SEVERITY.put("intergenic_variant", 1);

        CODING_SO_NAMES.add(STOP_RETAINED_VARIANT);
        CODING_SO_NAMES.add(SYNONYMOUS_VARIANT);
        CODING_SO_NAMES.add(STOP_GAINED);
        CODING_SO_NAMES.add(INITIATOR_CODON_VARIANT);
        CODING_SO_NAMES.add(STOP_LOST);
        CODING_SO_NAMES.add(MISSENSE_VARIANT);

        SO_NAMES_CORRECTIONS.put("nc_transcript_variant", "non_coding_transcript_variant");
        SO_NAMES_CORRECTIONS.put("non_coding_exon_variant", "non_coding_transcript_exon_variant");
    }

    public static Boolean isSynonymousCodon(String codon1, String codon2) {
        return isSynonymousCodon(false, codon1, codon2);
    }

    public static Boolean isSynonymousCodon(Boolean mitochondrialCode, String codon1, String codon2) {
        Map<String, String> geneticCode = null;
        if (mitochondrialCode) {
            return MT_IS_SYNONYMOUS_CODON.get(codon1).get(codon2);
        } else {
            return IS_SYNONYMOUS_CODON.get(codon1).get(codon2);
        }
    }

    public static Boolean isStopCodon(String codon) {
        return isStopCodon(false, codon);
    }

    public static Boolean isStopCodon(boolean mitochondrialCode, String codon) {
        if (mitochondrialCode) {
            if (codon.equals("TAA") || codon.equals("TAG") || codon.equals("AGA") || codon.equals("AGG")) {
                return true;
            }
        } else {
            if (codon.equals("TAA") || codon.equals("TGA") || codon.equals("TAG")) {
                return true;
            }
        }
        return false;
    }

    public static String getAminoacid(boolean mitochondrialCode, String codon) {
        if (mitochondrialCode) {
            return MT_CODON_TO_A.get(codon);
        } else {
            return CODON_TO_A.get(codon);
        }
    }

    public static List<SequenceOntologyTerm> getSequenceOntologyTerms(Iterable<String> soNames) throws SOTermNotAvailableException {
        List<SequenceOntologyTerm> sequenceOntologyTerms = new ArrayList<>();
        for (String name : soNames) {
            name = fixSONameIfNeeded(name);
            sequenceOntologyTerms.add(newSequenceOntologyTerm(name));
        }
        return sequenceOntologyTerms;
    }

    private static String fixSONameIfNeeded(String name) {
        String fixedName = SO_NAMES_CORRECTIONS.get(name);
        return fixedName == null ? name : fixedName;
    }

    public static SequenceOntologyTerm newSequenceOntologyTerm(String name) throws SOTermNotAvailableException {
        return new SequenceOntologyTerm(ConsequenceTypeMappings.getSoAccessionString(name), name);
    }

}
