package org.opencb.cellbase.core.variant.annotation;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.annotation.exceptions.SOTermNotAvailableException;
import org.opencb.biodata.models.variant.avro.*;

import java.util.*;

import static org.apache.tools.ant.taskdefs.Antlib.TAG;

/**
 * Created by fjlopez on 22/06/15.
 */
public class VariantAnnotationUtils {

    public static final char SEPARATOR_CHAR = ':';

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
    public static final String TWOKB_UPSTREAM_VARIANT = "2KB_upstream_variant";
    public static final String DOWNSTREAM_GENE_VARIANT = "downstream_gene_variant";
    public static final String TWOKB_DOWNSTREAM_VARIANT = "2KB_downstream_variant";
    public static final String SPLICE_DONOR_VARIANT = "splice_donor_variant";
    public static final String SPLICE_ACCEPTOR_VARIANT = "splice_acceptor_variant";
    public static final String INTRON_VARIANT = "intron_variant";
    public static final String SPLICE_REGION_VARIANT = "splice_region_variant";
    public static final String FIVE_PRIME_UTR_VARIANT = "5_prime_UTR_variant";
    public static final String THREE_PRIME_UTR_VARIANT = "3_prime_UTR_variant";
    public static final String INCOMPLETE_TERMINAL_CODON_VARIANT = "incomplete_terminal_codon_variant";
    public static final String STOP_RETAINED_VARIANT = "stop_retained_variant";
    public static final String START_RETAINED_VARIANT = "start_retained_variant";
    public static final String SYNONYMOUS_VARIANT = "synonymous_variant";
    public static final String INITIATOR_CODON_VARIANT = "initiator_codon_variant";
    public static final String START_LOST = "start_lost";
    public static final String STOP_GAINED = "stop_gained";
    public static final String STOP_LOST = "stop_lost";
    public static final String MISSENSE_VARIANT = "missense_variant";
    public static final String MATURE_MIRNA_VARIANT = "mature_miRNA_variant";
    public static final String NON_CODING_TRANSCRIPT_EXON_VARIANT = "non_coding_transcript_exon_variant";
    public static final String NON_CODING_TRANSCRIPT_VARIANT = "non_coding_transcript_variant";
    public static final String INFRAME_INSERTION = "inframe_insertion";
    public static final String INFRAME_VARIANT = "inframe_variant";
    public static final String FRAMESHIFT_VARIANT = "frameshift_variant";
    public static final String CODING_SEQUENCE_VARIANT = "coding_sequence_variant";
    public static final String TRANSCRIPT_ABLATION = "transcript_ablation";
    public static final String TRANSCRIPT_AMPLIFICATION = "transcript_amplification";
    public static final String COPY_NUMBER_CHANGE = "copy_number_change";
    public static final String TERMINATOR_CODON_VARIANT = "terminator_codon_variant";
    public static final String FEATURE_TRUNCATION = "feature_truncation";
    public static final String FEATURE_VARIANT = "feature_variant";
    public static final String STRUCTURAL_VARIANT = "structural_variant";
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
    public static final Map<String, AlleleOrigin> ORIGIN_STRING_TO_ALLELE_ORIGIN = new HashMap<>();
    public static final Set<String> CODING_SO_NAMES = new HashSet<>();
    public static final Map<String, ClinicalSignificance> CLINVAR_CLINSIG_TO_ACMG = new HashMap<>();
    public static final Map<String, TraitAssociation> CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION = new HashMap<>();
    public static final HashMap<String, ConsistencyStatus> CLINVAR_REVIEW_TO_CONSISTENCY_STATUS = new HashMap<>();
    // Currently left empty since the only item within DrugResponseClassification that seemed to match any clinvar
    // tag ("responsive") was removed at some point from the model
    public static final Map<String, DrugResponseClassification> CLINVAR_CLINSIG_TO_DRUG_RESPONSE = new HashMap<>();

    public static final HashMap<Object, ModeOfInheritance> MODEOFINHERITANCE_MAP = new HashMap<>();
    public static final HashMap<String, AlleleOrigin> COSMIC_SOMATICSTATUS_TO_ALLELE_ORIGIN = new HashMap<>();
    public static final HashMap<String, String> TO_ABBREVIATED_AA = new HashMap<>(22); // 22 AA
    public static final HashMap<String, String> TO_LONG_AA = new HashMap<>(22); // 22 AA
    private static final String ATG = "ATG";
    private static final String ATA = "ATA";
    private static final String TAA = "TAA";
    private static final String TAG = "TAG";
    private static final String AGA = "AGA";
    private static final String AGG = "AGG";
    private static final String TGA = "TGA";

    static {

        MODEOFINHERITANCE_MAP.put("autosomal dominant inheritance", ModeOfInheritance.monoallelic);
        MODEOFINHERITANCE_MAP.put("autosomal dominant inheritance with maternal imprinting",
                ModeOfInheritance.monoallelic_maternally_imprinted);
        MODEOFINHERITANCE_MAP.put("autosomal dominant inheritance with paternal imprinting",
                ModeOfInheritance.monoallelic_paternally_imprinted);
        MODEOFINHERITANCE_MAP.put("autosomal recessive inheritance",
                ModeOfInheritance.biallelic);
        MODEOFINHERITANCE_MAP.put("mitochondrial inheritance",
                ModeOfInheritance.mitochondrial);
        MODEOFINHERITANCE_MAP.put("sex-limited autosomal dominant",
                ModeOfInheritance.monoallelic);
        MODEOFINHERITANCE_MAP.put("x-linked dominant inheritance",
                ModeOfInheritance.xlinked_monoallelic);
        MODEOFINHERITANCE_MAP.put("x-linked recessive inheritance",
                ModeOfInheritance.xlinked_biallelic);

        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("criteria_provided_conflicting_interpretations", ConsistencyStatus.conflict);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("criteria_provided_multiple_submitters_no_conflicts", ConsistencyStatus.congruent);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("criteria_provided_single_submitter", ConsistencyStatus.congruent);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("reviewed_by_expert_panel", ConsistencyStatus.congruent);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("conflicting interpretations", ConsistencyStatus.conflict);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("no conflicts", ConsistencyStatus.congruent);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("single submitter", ConsistencyStatus.congruent);
        CLINVAR_REVIEW_TO_CONSISTENCY_STATUS.put("reviewed by expert panel", ConsistencyStatus.congruent);


        CLINVAR_CLINSIG_TO_ACMG.put("benign", ClinicalSignificance.benign);
        CLINVAR_CLINSIG_TO_ACMG.put("likely benign", ClinicalSignificance.likely_benign);
        CLINVAR_CLINSIG_TO_ACMG.put("conflicting interpretations of pathogenicity", ClinicalSignificance.uncertain_significance);
        CLINVAR_CLINSIG_TO_ACMG.put("likely pathogenic", ClinicalSignificance.likely_pathogenic);
        CLINVAR_CLINSIG_TO_ACMG.put("pathogenic", ClinicalSignificance.pathogenic);
        CLINVAR_CLINSIG_TO_ACMG.put("uncertain significance", ClinicalSignificance.uncertain_significance);
        CLINVAR_CLINSIG_TO_ACMG.put("conflicting data from submitters", ClinicalSignificance.uncertain_significance);

        CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION.put("risk factor", TraitAssociation.established_risk_allele);
        CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION.put("protective", TraitAssociation.protective);

        ///////////////////////////////////////////////////////////////////////
        /////   ClinVar and Cosmic allele origins to SO terms   ///////////////
        ///////////////////////////////////////////////////////////////////////
        ORIGIN_STRING_TO_ALLELE_ORIGIN.put("germline", AlleleOrigin.germline_variant);
        ORIGIN_STRING_TO_ALLELE_ORIGIN.put("maternal", AlleleOrigin.maternal_variant);
        ORIGIN_STRING_TO_ALLELE_ORIGIN.put("de novo", AlleleOrigin.de_novo_variant);
        ORIGIN_STRING_TO_ALLELE_ORIGIN.put("paternal", AlleleOrigin.paternal_variant);
        ORIGIN_STRING_TO_ALLELE_ORIGIN.put("somatic", AlleleOrigin.somatic_variant);

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

        /*
        Aminoacid abbreviation map
         */
        TO_ABBREVIATED_AA.put("ALA", "A");
        TO_ABBREVIATED_AA.put("ARG", "R");
        TO_ABBREVIATED_AA.put("ASN", "N");
        TO_ABBREVIATED_AA.put("ASP", "D");
        TO_ABBREVIATED_AA.put("ASX", "B");
        TO_ABBREVIATED_AA.put("CYS", "C");
        TO_ABBREVIATED_AA.put("GLU", "E");
        TO_ABBREVIATED_AA.put("GLN", "Q");
        TO_ABBREVIATED_AA.put("GLX", "Z");
        TO_ABBREVIATED_AA.put("GLY", "G");
        TO_ABBREVIATED_AA.put("HIS", "H");
        TO_ABBREVIATED_AA.put("ILE", "I");
        TO_ABBREVIATED_AA.put("LEU", "L");
        TO_ABBREVIATED_AA.put("LYS", "K");
        TO_ABBREVIATED_AA.put("MET", "M");
        TO_ABBREVIATED_AA.put("PHE", "F");
        TO_ABBREVIATED_AA.put("PRO", "P");
        TO_ABBREVIATED_AA.put("SER", "S");
        TO_ABBREVIATED_AA.put("THR", "T");
        TO_ABBREVIATED_AA.put("TRP", "W");
        TO_ABBREVIATED_AA.put("TYR", "Y");
        TO_ABBREVIATED_AA.put("VAL", "V");
        TO_ABBREVIATED_AA.put("STOP", "O");

        for (String aa : TO_ABBREVIATED_AA.keySet()) {
            TO_LONG_AA.put(TO_ABBREVIATED_AA.get(aa), buildUpperLowerCaseString(aa));
        }

        COMPLEMENTARY_NT.put('A', 'T');
        COMPLEMENTARY_NT.put('C', 'G');
        COMPLEMENTARY_NT.put('G', 'C');
        COMPLEMENTARY_NT.put('T', 'A');
        COMPLEMENTARY_NT.put('N', 'N');

        POLYPHEN_DESCRIPTIONS.put(0, "probably damaging");
        POLYPHEN_DESCRIPTIONS.put(1, "possibly damaging");
        POLYPHEN_DESCRIPTIONS.put(2, "benign");
        POLYPHEN_DESCRIPTIONS.put(3, "unknown");

        SIFT_DESCRIPTIONS.put(0, "tolerated");
        SIFT_DESCRIPTIONS.put(1, "deleterious");

        SO_SEVERITY.put("copy_number_change", 42);
        SO_SEVERITY.put("transcript_ablation", 41);
        SO_SEVERITY.put("structural_variant", 40);
        SO_SEVERITY.put("splice_acceptor_variant", 39);
        SO_SEVERITY.put("splice_donor_variant", 38);
        SO_SEVERITY.put("stop_gained", 37);
        SO_SEVERITY.put("frameshift_variant", 36);
        SO_SEVERITY.put("stop_lost", 35);
        SO_SEVERITY.put("terminator_codon_variant", 34);
        SO_SEVERITY.put("start_lost", 34);
        SO_SEVERITY.put("initiator_codon_variant", 33);
        SO_SEVERITY.put("transcript_amplification", 32);
        SO_SEVERITY.put("inframe_insertion", 31);
        SO_SEVERITY.put("inframe_deletion", 30);
        SO_SEVERITY.put("inframe_variant", 29);
        SO_SEVERITY.put("missense_variant", 28);
        SO_SEVERITY.put("splice_region_variant", 27);
        SO_SEVERITY.put("incomplete_terminal_codon_variant", 26);
        SO_SEVERITY.put("stop_retained_variant", 25);
        SO_SEVERITY.put("start_retained_variant", 24);
        SO_SEVERITY.put("synonymous_variant", 23);
        SO_SEVERITY.put("coding_sequence_variant", 22);
        SO_SEVERITY.put("mature_miRNA_variant", 21);
        SO_SEVERITY.put("5_prime_UTR_variant", 20);
        SO_SEVERITY.put("3_prime_UTR_variant", 19);
        SO_SEVERITY.put("non_coding_transcript_exon_variant", 18);
        SO_SEVERITY.put("intron_variant", 17);
        SO_SEVERITY.put("NMD_transcript_variant", 16);
        SO_SEVERITY.put("non_coding_transcript_variant", 15);
        SO_SEVERITY.put("2KB_upstream_variant", 14);
        SO_SEVERITY.put("upstream_gene_variant", 13);
        SO_SEVERITY.put("2KB_downstream_variant", 12);
        SO_SEVERITY.put("downstream_gene_variant", 11);
        SO_SEVERITY.put("TFBS_ablation", 10);
        SO_SEVERITY.put("TFBS_amplification", 9);
        SO_SEVERITY.put("TF_binding_site_variant", 8);
        SO_SEVERITY.put("regulatory_region_ablation", 7);
        SO_SEVERITY.put("regulatory_region_amplification", 6);
        SO_SEVERITY.put("regulatory_region_variant", 5);
        SO_SEVERITY.put("feature_elongation", 4);
        SO_SEVERITY.put("feature_truncation", 3);
        SO_SEVERITY.put("feature_variant", 2);
        SO_SEVERITY.put("intergenic_variant", 1);

        CODING_SO_NAMES.add(STOP_RETAINED_VARIANT);
        CODING_SO_NAMES.add(START_RETAINED_VARIANT);
        CODING_SO_NAMES.add(SYNONYMOUS_VARIANT);
        CODING_SO_NAMES.add(STOP_GAINED);
        CODING_SO_NAMES.add(INITIATOR_CODON_VARIANT);
        CODING_SO_NAMES.add(START_LOST);
        CODING_SO_NAMES.add(STOP_LOST);
        CODING_SO_NAMES.add(MISSENSE_VARIANT);

        SO_NAMES_CORRECTIONS.put("nc_transcript_variant", "non_coding_transcript_variant");
        SO_NAMES_CORRECTIONS.put("non_coding_exon_variant", "non_coding_transcript_exon_variant");
    }

    public static String buildUpperLowerCaseString(String aa) {
        StringBuilder stringBuilder = new StringBuilder(aa);

        for (int i = 1; i < stringBuilder.length(); i++) {
            stringBuilder.setCharAt(i, String.valueOf(stringBuilder.charAt(i)).toLowerCase().charAt(0));
        }

        return stringBuilder.toString();
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
            if (codon.equals(TAA) || codon.equals(TAG) || codon.equals(AGA) || codon.equals(AGG)) {
                return true;
            }
        } else {
            if (codon.equals(TAA) || codon.equals(TGA) || codon.equals(TAG)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStartCodon(boolean mitochondrialCode, String codon) {
        if (mitochondrialCode) {
            if (codon.equals(ATG) || codon.equals(ATA)) {
                return true;
            }
        } else {
            if (codon.equals(ATG)) {
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

    public static String buildVariantId(String chromosome, int start, String reference, String alternate) {
        StringBuilder stringBuilder = new StringBuilder();

        appendChromosome(chromosome, stringBuilder)
                .append(SEPARATOR_CHAR)
                .append(StringUtils.leftPad(Integer.toString(start), 10, " "))
                .append(SEPARATOR_CHAR);

//        if (reference.length() > Variant.SV_THRESHOLD) {
//            stringBuilder.append(new String(CryptoUtils.encryptSha1(reference)));
//        } else if (!(reference == null || reference.isEmpty() || reference.equals("-"))) {
        if (!(reference == null || reference.isEmpty() || reference.equals("-"))) {
            stringBuilder.append(reference);
        }
        stringBuilder.append(SEPARATOR_CHAR);
//        if (alternate.length() > Variant.SV_THRESHOLD) {
//            stringBuilder.append(new String(CryptoUtils.encryptSha1(alternate)));
//        } else if (!(alternate == null  || alternate.isEmpty() || alternate.equals("-"))) {
        if (!(alternate == null  || alternate.isEmpty() || alternate.equals("-"))) {
            stringBuilder.append(alternate);
        }
        return stringBuilder.toString();
    }

    protected static StringBuilder appendChromosome(String chromosome, StringBuilder stringBuilder) {
        if (chromosome.length() == 1 && Character.isDigit(chromosome.charAt(0))) {
            stringBuilder.append(' ');
        }
        return stringBuilder.append(chromosome);
    }

    public static VariantType getVariantType(Variant variant) throws UnsupportedURLVariantFormat {
        if (variant.getType() == null) {
            variant.setType(Variant.inferType(variant.getReference(), variant.getAlternate()));
        }
        // FIXME: remove the if block below as soon as the Variant.inferType method is able to differentiate between
        // FIXME: insertions and deletions
//        if (variant.getType().equals(VariantType.INDEL) || variant.getType().equals(VariantType.SV)) {
        if (variant.getType().equals(VariantType.INDEL)) {
            if (variant.getReference().isEmpty()) {
//                variant.setType(VariantType.INSERTION);
                return VariantType.INSERTION;
            } else if (variant.getAlternate().isEmpty()) {
//                variant.setType(VariantType.DELETION);
                return VariantType.DELETION;
            } else {
                return VariantType.MNV;
            }
        }
        return variant.getType();
//        return getVariantType(variant.getReferenceStart(), variant.getAlternate());
    }
}
