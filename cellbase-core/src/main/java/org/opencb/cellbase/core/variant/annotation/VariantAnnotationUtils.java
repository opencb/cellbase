package org.opencb.cellbase.core.variant.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 22/06/15.
 */
public class VariantAnnotationUtils {
    
    public static final String THREEPRIME_OVERLAPPING_NCRNA="3prime_overlapping_ncrna";
    public static final String IG_C_GENE="IG_C_gene";
    public static final String IG_C_PSEUDOGENE="IG_C_pseudogene";
    public static final String IG_D_GENE="IG_D_gene";
    public static final String IG_J_GENE="IG_J_gene";
    public static final String IG_J_PSEUDOGENE="IG_J_pseudogene";
    public static final String IG_V_GENE="IG_V_gene";
    public static final String IG_V_PSEUDOGENE="IG_V_pseudogene";
    public static final String MT_RRNA="Mt_rRNA";
    public static final String MT_TRNA="Mt_tRNA";
    public static final String TR_C_GENE="TR_C_gene";
    public static final String TR_D_GENE="TR_D_gene";
    public static final String TR_J_GENE="TR_J_gene";
    public static final String TR_J_PSEUDOGENE="TR_J_pseudogene";
    public static final String TR_V_GENE="TR_V_gene";
    public static final String TR_V_PSEUDOGENE="TR_V_pseudogene";
    public static final String ANTISENSE="antisense";
    public static final String LINCRNA="lincRNA";
    public static final String MIRNA="miRNA";
    public static final String MISC_RNA="misc_RNA";
    public static final String POLYMORPHIC_PSEUDOGENE="polymorphic_pseudogene";
    public static final String PROCESSED_PSEUDOGENE="processed_pseudogene";
    public static final String PROCESSED_TRANSCRIPT="processed_transcript";
    public static final String PROTEIN_CODING="protein_coding";
    public static final String PSEUDOGENE="pseudogene";
    public static final String RRNA="rRNA";
    public static final String SENSE_INTRONIC="sense_intronic";
    public static final String SENSE_OVERLAPPING="sense_overlapping";
    public static final String SNRNA="snRNA";
    public static final String SNORNA="snoRNA";
    public static final String NONSENSE_MEDIATED_DECAY="nonsense_mediated_decay";
    public static final String NMD_TRANSCRIPT_VARIANT="NMD_transcript_variant";
    public static final String UNPROCESSED_PSEUDOGENE="unprocessed_pseudogene";
    public static final String TRANSCRIBED_UNPROCESSED_PSEUDGENE="transcribed_unprocessed_pseudogene";
    public static final String RETAINED_INTRON="retained_intron";
    public static final String NON_STOP_DECAY="non_stop_decay";
    public static final String UNITARY_PSEUDOGENE="unitary_pseudogene";
    public static final String TRANSLATED_PROCESSED_PSEUDOGENE="translated_processed_pseudogene";
    public static final String TRANSCRIBED_PROCESSED_PSEUDOGENE="transcribed_processed_pseudogene";
    public static final String TRNA_PSEUDOGENE="tRNA_pseudogene";
    public static final String SNORNA_PSEUDOGENE="snoRNA_pseudogene";
    public static final String SNRNA_PSEUDOGENE="snRNA_pseudogene";
    public static final String SCRNA_PSEUDOGENE="scRNA_pseudogene";
    public static final String RRNA_PSEUDOGENE="rRNA_pseudogene";
    public static final String MISC_RNA_PSEUDOGENE="misc_RNA_pseudogene";
    public static final String MIRNA_PSEUDOGENE="miRNA_pseudogene";
    public static final String NON_CODING="non_coding";
    public static final String AMBIGUOUS_ORF="ambiguous_orf";
    public static final String KNOWN_NCRNA="known_ncrna";
    public static final String RETROTRANSPOSED="retrotransposed";
    public static final String TRANSCRIBED_UNITARY_PSEUDOGENE="transcribed_unitary_pseudogene";
    public static final String TRANSLATED_UNPROCESSED_PSEUDOGENE="translated_unprocessed_pseudogene";
    public static final String LRG_GENE="LRG_gene";

    public static final String INTERGENIC_VARIANT="intergenic_variant";
    public static final String REGULATORY_REGION_VARIANT="regulatory_region_variant";
    public static final String TF_BINDING_SITE_VARIANT="TF_binding_site_variant";
    public static final String UPSTREAM_GENE_VARIANT="upstream_gene_variant";
    public static final String DOWNSTREAM_GENE_VARIANT="downstream_gene_variant";
    public static final String SPLICE_DONOR_VARIANT="splice_donor_variant";
    public static final String SPLICE_ACCEPTOR_VARIANT="splice_acceptor_variant";
    public static final String INTRON_VARIANT="intron_variant";
    public static final String SPLICE_REGION_VARIANT="splice_region_variant";
    public static final String FIVE_PRIME_UTR_VARIANT="5_prime_UTR_variant";
    public static final String THREE_PRIME_UTR_VARIANT="3_prime_UTR_variant";
    public static final String INCOMPLETE_TERMINAL_CODON_VARIANT="incomplete_terminal_codon_variant";
    public static final String STOP_RETAINED_VARIANT="stop_retained_variant";
    public static final String SYNONYMOUS_VARIANT="synonymous_variant";
    public static final String INITIATOR_CODON_VARIANT="initiator_codon_variant";
    public static final String STOP_GAINED="stop_gained";
    public static final String STOP_LOST="stop_lost";
    public static final String MISSENSE_VARIANT="missense_variant";
    public static final String MATURE_MIRNA_VARIANT="mature_miRNA_variant";
    public static final String NON_CODING_TRANSCRIPT_EXON_VARIANT="non_coding_transcript_exon_variant";
    public static final String NON_CODING_TRANSCRIPT_VARIANT="non_coding_transcript_variant";
    public static final String INFRAME_INSERTION="inframe_insertion";
    public static final String FRAMESHIFT_VARIANT="frameshift_variant";
    public static final String CODING_SEQUENCE_VARIANT="coding_sequence_variant";
    public static final String TRANSCRIPT_ABLATION="transcript_ablation";
    public static final String FEATURE_TRUNCATION="feature_truncation";
    public static final String INFRAME_DELETION="inframe_deletion";

    public static final String CDS_START_NF="cds_start_NF";
    public static final String CDS_END_NF="cds_end_NF";

    public static Map<String, Map<String,Boolean>> isSynonymousCodon = new HashMap<>();
    public static Map<String, List<String>> aToCodon = new HashMap<>(20);
    public static Map<String, String> codonToA = new HashMap<>();
    public static Map<Character, Character> complementaryNt = new HashMap<>();
    public static Map<Integer, String> siftDescriptions = new HashMap<>();
    public static Map<Integer, String> polyphenDescriptions = new HashMap<>();
    public static Map<String, Integer> soSeverity = new HashMap<>();

    static {

        ///////////////////////////////////////////////////////////////////////
        /////   GENETIC CODE   ////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////
        aToCodon.put("ALA", new ArrayList<String>());
        aToCodon.get("ALA").add("GCT");
        aToCodon.get("ALA").add("GCC");
        aToCodon.get("ALA").add("GCA");
        aToCodon.get("ALA").add("GCG");
        aToCodon.put("ARG", new ArrayList<String>());
        aToCodon.get("ARG").add("CGT");
        aToCodon.get("ARG").add("CGC");
        aToCodon.get("ARG").add("CGA");
        aToCodon.get("ARG").add("CGG");
        aToCodon.get("ARG").add("AGA");
        aToCodon.get("ARG").add("AGG");
        aToCodon.put("ASN", new ArrayList<String>());
        aToCodon.get("ASN").add("AAT");
        aToCodon.get("ASN").add("AAC");
        aToCodon.put("ASP", new ArrayList<String>());
        aToCodon.get("ASP").add("GAT");
        aToCodon.get("ASP").add("GAC");
        aToCodon.put("CYS", new ArrayList<String>());
        aToCodon.get("CYS").add("TGT");
        aToCodon.get("CYS").add("TGC");
        aToCodon.put("GLN", new ArrayList<String>());
        aToCodon.get("GLN").add("CAA");
        aToCodon.get("GLN").add("CAG");
        aToCodon.put("GLU", new ArrayList<String>());
        aToCodon.get("GLU").add("GAA");
        aToCodon.get("GLU").add("GAG");
        aToCodon.put("GLY", new ArrayList<String>());
        aToCodon.get("GLY").add("GGT");
        aToCodon.get("GLY").add("GGC");
        aToCodon.get("GLY").add("GGA");
        aToCodon.get("GLY").add("GGG");
        aToCodon.put("HIS", new ArrayList<String>());
        aToCodon.get("HIS").add("CAT");
        aToCodon.get("HIS").add("CAC");
        aToCodon.put("ILE", new ArrayList<String>());
        aToCodon.get("ILE").add("ATT");
        aToCodon.get("ILE").add("ATC");
        aToCodon.get("ILE").add("ATA");
        aToCodon.put("LEU", new ArrayList<String>());
        aToCodon.get("LEU").add("TTA");
        aToCodon.get("LEU").add("TTG");
        aToCodon.get("LEU").add("CTT");
        aToCodon.get("LEU").add("CTC");
        aToCodon.get("LEU").add("CTA");
        aToCodon.get("LEU").add("CTG");
        aToCodon.put("LYS", new ArrayList<String>());
        aToCodon.get("LYS").add("AAA");
        aToCodon.get("LYS").add("AAG");
        aToCodon.put("MET", new ArrayList<String>());
        aToCodon.get("MET").add("ATG");
        aToCodon.put("PHE", new ArrayList<String>());
        aToCodon.get("PHE").add("TTT");
        aToCodon.get("PHE").add("TTC");
        aToCodon.put("PRO", new ArrayList<String>());
        aToCodon.get("PRO").add("CCT");
        aToCodon.get("PRO").add("CCC");
        aToCodon.get("PRO").add("CCA");
        aToCodon.get("PRO").add("CCG");
        aToCodon.put("SER", new ArrayList<String>());
        aToCodon.get("SER").add("TCT");
        aToCodon.get("SER").add("TCC");
        aToCodon.get("SER").add("TCA");
        aToCodon.get("SER").add("TCG");
        aToCodon.get("SER").add("AGT");
        aToCodon.get("SER").add("AGC");
        aToCodon.put("THR", new ArrayList<String>());
        aToCodon.get("THR").add("ACT");
        aToCodon.get("THR").add("ACC");
        aToCodon.get("THR").add("ACA");
        aToCodon.get("THR").add("ACG");
        aToCodon.put("TRP", new ArrayList<String>());
        aToCodon.get("TRP").add("TGG");
        aToCodon.put("TYR", new ArrayList<String>());
        aToCodon.get("TYR").add("TAT");
        aToCodon.get("TYR").add("TAC");
        aToCodon.put("VAL", new ArrayList<String>());
        aToCodon.get("VAL").add("GTT");
        aToCodon.get("VAL").add("GTC");
        aToCodon.get("VAL").add("GTA");
        aToCodon.get("VAL").add("GTG");
        aToCodon.put("STOP", new ArrayList<String>());
        aToCodon.get("STOP").add("TAA");
        aToCodon.get("STOP").add("TGA");
        aToCodon.get("STOP").add("TAG");

        for (String aa : aToCodon.keySet()) {
            for (String codon : aToCodon.get(aa)) {
                isSynonymousCodon.put(codon, new HashMap<String, Boolean>());
                codonToA.put(codon, aa);
            }
        }
        for (String codon1 : isSynonymousCodon.keySet()) {
            Map<String, Boolean> codonEntry = isSynonymousCodon.get(codon1);
            for (String codon2 : isSynonymousCodon.keySet()) {
                codonEntry.put(codon2, false);
            }
        }
        for (String aa : aToCodon.keySet()) {
            for (String codon1 : aToCodon.get(aa)) {
                for (String codon2 : aToCodon.get(aa)) {
                    isSynonymousCodon.get(codon1).put(codon2, true);
                }
            }
        }

        complementaryNt.put('A','T');
        complementaryNt.put('C','G');
        complementaryNt.put('G','C');
        complementaryNt.put('T','A');

        polyphenDescriptions.put(0,"probably damaging");
        polyphenDescriptions.put(1,"possibly damaging");
        polyphenDescriptions.put(2,"benign");
        polyphenDescriptions.put(3,"unknown");

        siftDescriptions.put(0,"tolerated");
        siftDescriptions.put(1,"deleterious");

        soSeverity.put("transcript_ablation", 36);
        soSeverity.put("splice_acceptor_variant", 35);
        soSeverity.put("splice_donor_variant", 34);
        soSeverity.put("stop_gained", 33);
        soSeverity.put("frameshift_variant", 32);
        soSeverity.put("stop_lost", 31);
        soSeverity.put("initiator_codon_variant", 30);
        soSeverity.put("transcript_amplification", 29);
        soSeverity.put("inframe_insertion", 28);
        soSeverity.put("inframe_deletion", 27);
        soSeverity.put("missense_variant", 26);
        soSeverity.put("splice_region_variant", 25);
        soSeverity.put("incomplete_terminal_codon_variant", 24);
        soSeverity.put("stop_retained_variant", 23);
        soSeverity.put("synonymous_variant", 22);
        soSeverity.put("coding_sequence_variant", 21);
        soSeverity.put("mature_miRNA_variant", 20);
        soSeverity.put("5_prime_UTR_variant", 19);
        soSeverity.put("3_prime_UTR_variant", 18);
        soSeverity.put("non_coding_transcript_exon_variant", 17);
        soSeverity.put("intron_variant", 16);
        soSeverity.put("NMD_transcript_variant", 15);
        soSeverity.put("non_coding_transcript_variant", 14);
        soSeverity.put("2KB_upstream_gene_variant", 13);
        soSeverity.put("upstream_gene_variant", 12);
        soSeverity.put("2KB_downstream_gene_variant", 11);
        soSeverity.put("downstream_gene_variant", 10);
        soSeverity.put("TFBS_ablation", 9);
        soSeverity.put("TFBS_amplification", 8);
        soSeverity.put("TF_binding_site_variant", 7);
        soSeverity.put("regulatory_region_ablation", 6);
        soSeverity.put("regulatory_region_amplification", 5);
        soSeverity.put("regulatory_region_variant", 4);
        soSeverity.put("feature_elongation", 3);
        soSeverity.put("feature_truncation", 2);
        soSeverity.put("intergenic_variant", 1);

    }

    public static Boolean isStopCodon(String codon) {
        if(codon.equals("TAA") || codon.equals("TGA") || codon.equals("TAG")) {
            return true;
        }
        return false;
    }

}
