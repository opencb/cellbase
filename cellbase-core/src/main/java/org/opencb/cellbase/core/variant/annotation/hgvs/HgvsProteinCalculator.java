package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Calculates HGVS protein string based on variant and transcript.
 */
public class HgvsProteinCalculator {

    protected static Logger logger = LoggerFactory.getLogger(HgvsProteinCalculator.class);

    private final Variant variant;
    private final Transcript transcript;
    private TranscriptUtils transcriptUtils;
    // protein sequence updated with variation
    private StringBuilder alternateProteinSequence;

    private static final String MT = "MT";
    private static final String STOP_STRING = "STOP";
//    private static final String FRAMESHIFT_SUFFIX = "fs";
//    private static final String EXTENSION_TAG = "ext";
//    private static final String UNKOWN_STOP_CODON_POSITION = "*?";
//    private static final String TERMINATION_SUFFIX = "Ter";
//    private static final String INS_SUFFIX = "ins";
//    private static final String DUP_SUFFIX = "dup";
//    private static final char STOP_CODON_INDICATOR = '*';
    private static final String UNIPROT_LABEL = "uniprotkb/swissprot";
    protected BuildingComponents buildingComponents = null;

    public static final int MAX_NUMBER_AMINOACIDS_DISPLAYED = 10;

    /**
     * Constructor.
     *
     * @param variant variant of interest. Can be SNV, DEL or INS
     * @param transcript transcript containing variant
     */
    public HgvsProteinCalculator(Variant variant, Transcript transcript) {
        this.variant = variant;
        this.transcript = transcript;
        this.transcriptUtils =  new TranscriptUtils(transcript);
    }

    /**
     * For the given variant and transcript, return the correctly formatted HGVSp string.
     *
     * @return HGVSp string for variant and transcript
     */
    public HgvsProtein calculate() {
        // FIXME  restore !onlySpansCodingSequence(variant, transcript) check
        if (!transcriptUtils.isCoding() || StringUtils.isEmpty(transcript.getProteinSequence())) {
            return null;
        }

        buildingComponents = new BuildingComponents();

        switch (this.variant.getType()) {
            case SNV:
                return calculateSnvHgvs();
            case INDEL:
                if (StringUtils.isBlank(variant.getReference())) {
                    // insertion
                    return calculateInsertionHgvs();
                } else {
                    // deletion
                    if (StringUtils.isBlank(variant.getAlternate())) {
                        return calculateDeletionHgvs();
                    } else {
                        logger.debug("No HGVS implementation available for variant MNV. Returning empty list of HGVS identifiers.");
                        return null;
                    }
                }
            case INSERTION:
                return calculateInsertionHgvs();
            case DELETION:
                return calculateDeletionHgvs();
            default:
                // TODO throw an error?
                logger.error("Don't know how to handle this variant of type {}", variant.getType());
                return null;
        }
    }

    private HgvsProtein calculateSnvHgvs() {
        if (variant.getEnd() < transcript.getGenomicCodingStart() || variant.getStart() > transcript.getGenomicCodingEnd()) {
            return null;
        }

        if (!transcriptUtils.isExonic(variant.getStart())) {
            return null;
        }

        String alternate = transcript.getStrand().equals("+")
                ? variant.getAlternate()
                : VariantAnnotationUtils.reverseComplementary(variant.getAlternate());

        int cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());
        // FIXME we need to change this method
        if (transcript.getStrand().equals("-")) {
            cdsVariantStartPosition--;
        }
        int codonPosition = transcriptUtils.getCodonPosition(cdsVariantStartPosition);
        String referenceCodon = transcriptUtils.getCodon(codonPosition);

        // Check the SNV is not in the first or last incomplete codons
        if (transcriptUtils.hasUnconfirmedStart() && codonPosition == 1
                && (referenceCodon.length() < 3 || referenceCodon.contains("N"))) {
            return new HgvsProtein(getProteinIds(), "p.Ter" + codonPosition + "=", transcript.getcDnaSequence());
        }
        if (transcriptUtils.hasUnconfirmedSEnd() && codonPosition >= transcript.getProteinSequence().length()
                && (referenceCodon.length() < 3 || referenceCodon.contains("N"))) {
            return new HgvsProtein(getProteinIds(), "p.Ter" + codonPosition + "=", transcript.getcDnaSequence());
        }

        // TODO check unconfirmed end, example: 1:201359245:G:T
        String referenceAAUppercase = VariantAnnotationUtils
                .getAminoacid(VariantAnnotationUtils.MT.equals(variant.getChromosome()), referenceCodon);
        if (StringUtils.isEmpty(referenceAAUppercase)) {
            logger.warn("Invalid reference nucleotide symbol {} found for variant {}. Skipping protein HGVS "
                    + "calculation.", referenceAAUppercase, variant.toString());
            return null;
        }
        // three letter abbreviation
        String referenceAminoacid = VariantAnnotationUtils.buildUpperLowerCaseString(referenceAAUppercase);

        // FIXME - forcing to be > 0
        int positionAtCodon = Math.max(transcriptUtils.getPositionAtCodon(cdsVariantStartPosition), 1);
        char[] chars = referenceCodon.toCharArray();
        chars[positionAtCodon - 1] = alternate.charAt(0);
        String alternateAAUpperCase =
                VariantAnnotationUtils.getAminoacid(VariantAnnotationUtils.MT.equals(variant.getChromosome()), new String(chars));
        if (StringUtils.isEmpty(alternateAAUpperCase)) {
            logger.warn("Invalid alternate nucleotide symbol {} found for variant {}. Skipping protein HGVS "
                    + "calculation.", alternateAAUpperCase, variant.toString());
            return null;
        }
        // three letter abbreviation
        String alternateAminoacid = VariantAnnotationUtils.buildUpperLowerCaseString(alternateAAUpperCase);

        String hgvsString;
        if (referenceAminoacid.equals(alternateAminoacid)) {
            // silent (no change)
            //  NP_003997.1:p.Cys188=
            //  amino acid Cys188 is not changed (DNA level change ..TGC.. to ..TGT..)
            //  NOTE: the description p.= means the entire protein coding region was analysed and no variant was found that changes
            //  (or is predicted to change) the protein sequence.
            // This includes one STOP codon to another STOP codon. We need to chech the STOP codon is not a Sec amino acid
            if (referenceAminoacid.equalsIgnoreCase(STOP_STRING) && codonPosition < transcript.getProteinSequence().length()
                    && transcript.getProteinSequence().charAt(codonPosition - 1) != 'U') {
                hgvsString = "p.Ter" + codonPosition + "=";
            } else {
                hgvsString = "p." + referenceAminoacid + codonPosition + "=";
            }
        } else {
            // Different amino acid, several scenarios
            if (referenceAminoacid.equalsIgnoreCase("STOP")) { // && codonPosition < transcript.getProteinSequence().length()
//                    && transcript.getProteinSequence().charAt(codonPosition - 1) != 'U'
                // translation termination codon (stop codon, no-stop change)
                return calculateFrameshiftHgvs();
            } else {
                if (alternateAminoacid.equalsIgnoreCase("STOP")) {
                    // nonsense
                    //  LRG_199p1:p.Trp24Ter (p.Trp24*)
                    //  amino acid Trp24 is changed to a stop codon (Ter, *)
                    //  NOTE: this change is not described as a deletion of the C-terminal end of the protein (i.e. p.Trp24_Met36853del)
                    hgvsString = "p." + referenceAminoacid + codonPosition + "Ter";
                } else {
                    if ("Met".equals(referenceAminoacid) && codonPosition == 1) {
                        // translation initiation codon
                        // unknown
                        //  LRG_199p1:p.Met1?
                        //  the consequence, at the protein level, of a variant affecting the translation initiation codon #
                        //  can not be predicted (i.e. is unknown)
                        hgvsString = "p.Met1?";
                    } else {
                        // missense
                        //  LRG_199p1:p.Trp24Cys
                        //  amino acid Trp24 is changed to a Cys
                        hgvsString = "p." + referenceAminoacid + codonPosition + alternateAminoacid;
                    }
                }
            }
        }

        alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
//        alternateProteinSequence.setCharAt(codonPosition, alternateAminoacid.charAt(0));
        return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
    }

    /**
     * This method can produce 3 different HGVS mutations:
     *
     * HGVS Insertion: a sequence change between the translation initiation (start) and termination (stop) codon where,
     * compared to the reference sequence, one or more amino acids are inserted,
     * which is not a frame shift and where the insertion is not a copy of a sequence immediately N-terminal (5')
     *
     * HGVS Duplication: a sequence change between the translation initiation (start) and termination (stop) codon where, compared to
     * a reference sequence, a copy of one or more amino acids are inserted directly C-terminal of the original copy of that sequence.
     *
     * HGVS Frame shift: a sequence change between the translation initiation (start) and termination (stop) codon where,
     * compared to a reference sequence, translation shifts to another reading frame.
     *
     * @return
     */
    private HgvsProtein calculateInsertionHgvs() {
        if (variant.getEnd() < transcript.getGenomicCodingStart() || variant.getStart() > transcript.getGenomicCodingEnd()) {
            return null;
        }

        // Get CDS position
        int cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());

        // Get variant alternate in the same strand than the transcript
        String alternate = transcript.getStrand().equals("+")
                ? variant.getAlternate()
                : VariantAnnotationUtils.reverseComplementary(variant.getAlternate());

        int codonPosition = transcriptUtils.getCodonPosition(cdsVariantStartPosition);
        int positionAtCodon = transcriptUtils.getPositionAtCodon(cdsVariantStartPosition);

        // Check if this is an in an insertion, duplication or frameshift.
        // Alternate length for Insertions and Duplications must be multiple of 3, otherwise it is a frameshift.
        if (variant.getAlternate().length() % 3 == 0) {
            if (positionAtCodon == 1) {
                // An in-frame HGVS Insertion or Duplication, insert is happening between two codons.
                return this.hgvsInsertionFormatter(codonPosition, alternate);
            } else {
                // Position at codon is not 1 and therefore the codon is split, but there is still the possibility of having
                // an in-frame HGVS Insertion or Duplication.
                //
                // Example: position at codon is 2 and 9 nucleotides are inserted, but GLY is not changed, only three amino acid are
                // inserted Variant at:  231 -/AGGGCGAGG  resulting in:  p.Glu75_Glu77dup
                // 218    221    224    227    230    233    236
                // 74     75     76     77     78     79     80
                // GAG    GAG    GGC    GAG    G GC   GGG    GTG
                // -----------------------------*-----------------
                // 73     74     75     76     77     78     79
                // GLU    GLU    GLY    GLU    GLY    GLY    VAL
                // E      E      G      E      G      G      V
                //
                // The inserted codons are: G_AG - GGC - GAG - G_GC
                //                           GLU   GLY   GLU    GLY
                // Note: if it is an insertion or duplication the original reference codon must remain intact after being split. Then,
                // the insertion can happen on the left or right of the reference codon. Left Insertion: codon formed with the end of
                // the insertion is the same as reference, insertion happens in the left.

                // Get the reference codon and the new sequence inserted
                String refCodon = transcriptUtils.getCodon(codonPosition);
                String refAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), refCodon);

                // Build the new inserted sequence = split codon + alternate allele
                String insSequence = refCodon.substring(0, positionAtCodon - 1) + alternate + refCodon.substring(positionAtCodon - 1);

                // Insertion or Duplication need the reference codon to be the same.
                // Check if the reference codon is at any end of the new inserted sequence.
                String insertLeftAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), insSequence.substring(0, 3));
                String insertRightAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()),
                        insSequence.substring(insSequence.length() - 3));

                // Check special case when a new STOP codon is inserted on the left
                if (insertLeftAa.equalsIgnoreCase("STOP")) {
                    // HGVS Substitution, if the codon affected becomes a STOP codon with on insertion on the left then this is just
                    // an amino acid substitution
                    String hgvsString = "p." + StringUtils.capitalize(refAa.toLowerCase()) + codonPosition + "Ter";
                    StringBuilder alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
                    alternateProteinSequence.insert(codonPosition - 1, StringUtils.join(codonPosition, ""));
                    return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
                }

                // Check special case when a new STOP codon is inserted on the right
                // TODO Think about this case a bit more, not clear if this is a delins or just an insertion
                if (insertRightAa.equalsIgnoreCase("STOP")) {
                    // HGVS DELETION-INSERTION
                    // p.(Pro578_Lys579delinsLeuTer)
                    //  the predicted change at the protein level resulting from DNA variant NM_080877.2c.1733_1735delinsTTT is a
                    //  deletion of amino acids Pro578 and Lys579 replaced with LeuTer
                    // One single amino acid at codonPosition is deleted
                    return this.hgvsDeletionInsertionFormatter(codonPosition, codonPosition, alternate);
                }

                // Check if the the original amino acid is kept
                if (refAa.equalsIgnoreCase(insertLeftAa) || refAa.equalsIgnoreCase(insertRightAa)) {
                    // Check if the new sequence is inserted left or right to the original reference codon.
                    // Remove the reference codon and update codonPosition for the insertion
                    if (refAa.equalsIgnoreCase(insertLeftAa)) {
                        insSequence = insSequence.substring(3);
                        codonPosition++;
                    } else {
                        insSequence = insSequence.substring(0, insSequence.length() - 3);
                    }
                    return this.hgvsInsertionFormatter(codonPosition, insSequence);
                } else {
                    // HGVS DELETION-INSERTION
                    // The insertion happens between codonPosition - 1 and codonPosition
                    return this.hgvsDeletionInsertionFormatter(codonPosition - 1, codonPosition, alternate);
                }
            }
        } else {
            // call to frameshift
            return calculateFrameshiftHgvs();
        }
    }

    private HgvsProtein hgvsInsertionFormatter(int aminoacidPosition, String alternate) {
        String hgvsString;

        // Get amino acid sequence of the insertion
        List<String> aminoacids =  new ArrayList<>(alternate.length() / 3);
        List<String> codedAminoacids =  new ArrayList<>(alternate.length() / 3);
        for (int i = 0; i < alternate.length(); i += 3) {
            String alternateCodon = alternate.substring(i, i + 3);
            String alternateAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), alternateCodon);
            aminoacids.add(VariantAnnotationUtils.buildUpperLowerCaseString(alternateAa));
            codedAminoacids.add(VariantAnnotationUtils.TO_ABBREVIATED_AA.get(alternateAa));
        }

        // Create HGVS string
        // IMPORTANT: Check if this is a HGVS Duplication instead of a HGVS Insertion
        // keep moving to the right (3' Rule) while the first amino acid inserted equals the first one after insertion
        // New algorithm to move to the right the insertion ONLY if a duplication exists.
        // Example:
        // Original Sequence: A B C A B C A B C D   and   Insertion Sequence: c a b
        // Result:  A B C A B c a b C A B C D
        // We iterate over all possible splits (left and right) and move to the right. Iterations:
        // 1. cab == CAB
        // 2. Bca == bCA
        // 3. ABc == abC  Yes! move to the right 2 positions and start again!
        boolean isDuplication = false;
        boolean keepMovingRight = true;
        String insertSeq = StringUtils.join(codedAminoacids, "");
        while (keepMovingRight) {
            keepMovingRight = false;
            // Start iterating of all possible insert sequence splits
            for (int i = 0; i <= codedAminoacids.size(); i++) {
                // Check we are i the right ranges
                if (aminoacidPosition - i - 1 < 0
                        || aminoacidPosition + insertSeq.length() - i - 1 > transcript.getProteinSequence().length()) {
                    continue;
                }

                // Calculate left and right seq for this iteration
                String leftSeq = transcript.getProteinSequence().substring(aminoacidPosition - i - 1, aminoacidPosition - 1)
                        + insertSeq.substring(0, insertSeq.length() - i);
                String rightSeq = insertSeq.substring(insertSeq.length() - i)
                        + transcript.getProteinSequence().substring(aminoacidPosition - 1, aminoacidPosition + insertSeq.length() - i - 1);
                if (leftSeq.equals(rightSeq)) {
                    // A Duplication has been found! This variable is used below.
                    isDuplication = true;
                    // Update amino acid position (move right) ONLY if a the duplication is on the right!
                    // Last iteration checks if the Duplication is on the left of insertion amino acid.
                    if (i != insertSeq.length()) {
                        aminoacidPosition += insertSeq.length() - i;
                        keepMovingRight = true;
                    }
                    break;
                }
            }
        }

        if (isDuplication) {
            // HGVS Duplication: a sequence change between the translation initiation (start) and termination (stop) codon where,
            // compared to a reference sequence, a copy of one or more amino acids are inserted directly C-terminal
            // of the original copy of that sequence.
            // Examples:
            // p.Ala3dup (one amino acid)
            //  a duplication of amino acid Ala3 in the sequence MetGlyAlaArgSerSerHis to MetGlyAlaAlaArgSerSerHis
            // p.Ala3_Ser5dup (several amino acids)
            //  a duplication of amino acids Ala3 to Ser5 in the sequence MetGlyAlaArgSerSerHis to MetGlyAlaArgSerAlaArgSerSerHis

            String leftDupAa =  String.valueOf(transcript.getProteinSequence().charAt(aminoacidPosition - aminoacids.size() - 1));
            if (aminoacids.size() == 1) {
                hgvsString = "p." + StringUtils.capitalize(VariantAnnotationUtils.TO_LONG_AA.get(leftDupAa).toLowerCase())
                        + (aminoacidPosition - aminoacids.size()) + "dup";
            } else {
                String rightDupAa =  String.valueOf(transcript.getProteinSequence().charAt(aminoacidPosition - 2));
                hgvsString = "p." + StringUtils.capitalize(VariantAnnotationUtils.TO_LONG_AA.get(leftDupAa).toLowerCase())
                        + (aminoacidPosition - aminoacids.size()) + "_"
                        + StringUtils.capitalize(VariantAnnotationUtils.TO_LONG_AA.get(rightDupAa).toLowerCase())
                        + (aminoacidPosition - 1) + "dup";
            }
        } else {
            // HGVS Insertion: a sequence change between the translation initiation (start) and termination (stop) codon where,
            // compared to the reference sequence, one or more amino acids are inserted, which is not a frame shift and
            // where the insertion is not a copy of a sequence immediately N-terminal (5')

            // keep moving to the right (3' Rule) while the first amino acid deleted equals the first one after deletion,
            // Example: check 11:6390701:-:CTGGCGCTGGCG
            String aaAfterInsertion = transcript.getProteinSequence().substring(aminoacidPosition - 1, aminoacidPosition);
            while (codedAminoacids.get(0).equals(aaAfterInsertion)) {
                aminoacidPosition++;
                aminoacids.remove(0);
                codedAminoacids.remove(0);
                aminoacids.add(VariantAnnotationUtils.TO_LONG_AA.get(aaAfterInsertion));
                codedAminoacids.add(aaAfterInsertion);
                aaAfterInsertion = transcript.getProteinSequence().substring(aminoacidPosition - 1, aminoacidPosition);
            }

            // Get position and flanking amino acids
            int codonIndex = aminoacidPosition - 1;
            String leftCodedAa = transcript.getProteinSequence().substring(codonIndex - 1, codonIndex);
            String leftAa = VariantAnnotationUtils.TO_LONG_AA.get(leftCodedAa);

            String rightCodedAa;
            String rightAa;
            if (codonIndex < transcript.getProteinSequence().length()) {
                rightCodedAa = transcript.getProteinSequence().substring(codonIndex, codonIndex + 1);
                rightAa = VariantAnnotationUtils.TO_LONG_AA.get(rightCodedAa);
            } else {
                // Means the insertion is right after the last codon.
                rightAa = "Ter";
            }

            // Check if a STOP codon is being inserted
            int stopIndex = -1;
            for (int i = 0; i < aminoacids.size(); i++) {
                if (aminoacids.get(i).equalsIgnoreCase("STOP")) {
                    stopIndex = i;
                    break;
                }
            }

            // At least one amino acid is inserted
            if (aminoacids.size() <= MAX_NUMBER_AMINOACIDS_DISPLAYED) {
                if (stopIndex < 0) {
                    // p.Lys2_Gly3insGlnSerLys
                    //  the insertion of amino acids GlnSerLys between amino acids Lys2 and Gly3
                    //  changing MetLysGlyHisGlnGlnCys to MetLysGlnSerLysGlyHisGlnGlnCys
                    hgvsString = "p." + leftAa + (aminoacidPosition - 1) + "_"
                            + rightAa + aminoacidPosition + "ins" + StringUtils.join(aminoacids, "");
                } else {
                    // p.(Met3_His4insGlyTer)
                    //  the predicted consequence at the protein level of an insertion at the DNA level (c.9_10insGGGTAG) is the
                    //  insertion of GlyTer (alternatively Gly*)
                    //  NOTE: this is not described as p.(Met3_Ile3418delinsGly), a deletion-insertion replacing the entire C-terminal
                    //  protein coding sequence downstream of Met3 with a Gly)
                    List<String> nonStopCodonAminoAcids = aminoacids.subList(0, stopIndex);
                    hgvsString = "p." + leftAa + (aminoacidPosition - 1) + "_"
                            + rightAa + aminoacidPosition + "ins" + StringUtils.join(nonStopCodonAminoAcids, "") + "Ter";
                }
            } else {
                // Check if last amino acid inserted is a STOP codon:
                if (stopIndex < 0) {
                    // p.Arg78_Gly79ins23
                    //  the in-frame insertion of a 23 amino acid sequence between amino acids Arg78 and Gly79
                    //  NOTE: it must be possible to deduce the 23 inserted amino acids from the description at DNA or RNA level
                    hgvsString = "p." + leftAa + (aminoacidPosition - 1) + "_"
                            + rightAa + aminoacidPosition + "ins" + aminoacids.size();
                } else {
                    // NP_060250.2:p.Gln746_Lys747ins*63
                    //  the in-frame insertion of a 62 amino acid sequence ending at a stop codon at position *63 between
                    //  amino acids Gln746 and Lys747.
                    //  NOTE: it must be possible to deduce the inserted amino acid sequence from the description given
                    //  at DNA or RNA level
                    hgvsString = "p." + leftAa + (aminoacidPosition - 1) + "_"
                            + rightAa + aminoacidPosition + "ins" + "*" + (stopIndex + 1);
                }
            }
        }

        StringBuilder alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
        alternateProteinSequence.insert(aminoacidPosition - 1, StringUtils.join(codedAminoacids, ""));
        return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
    }

    /**
     * This method can produce 2 different HGVS mutations:
     *
     * HGVS Deletion: a sequence change between the translation initiation (start) and termination (stop) codon where,
     * compared to a reference sequence, one or more amino acids are not present (deleted)
     *
     * HGVS Frame shift: a sequence change between the translation initiation (start) and termination (stop) codon where,
     * compared to a reference sequence, translation shifts to another reading frame.
     *
     * @return
     */
    private HgvsProtein calculateDeletionHgvs() {
        if (variant.getEnd() < transcript.getGenomicCodingStart() || variant.getStart() > transcript.getGenomicCodingEnd()) {
            return null;
        }

        int cdsVariantStartPosition;
        String referenceAllele = variant.getReference();
        if (transcript.getStrand().equals("+")) {
            if (variant.getStart() < transcript.getGenomicCodingStart() && variant.getEnd() >= transcript.getGenomicCodingStart()) {
                referenceAllele = variant.getReference().substring(transcript.getGenomicCodingStart() - variant.getStart());
                cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, transcript.getGenomicCodingStart());
            } else {
                cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());
            }
        } else {
            if (variant.getStart() <= transcript.getGenomicCodingEnd() && variant.getEnd() > transcript.getGenomicCodingEnd()) {
                referenceAllele = variant.getReference().substring(0, transcript.getGenomicCodingEnd() - variant.getStart() + 1);
                cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, transcript.getGenomicCodingEnd());
            } else {
                cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, variant.getEnd());
            }
            // FIXME Method HgvsCalculator.getCdsStart wringly returns +1 for reverse strand
            cdsVariantStartPosition--;
        }

        // Prepare variables
        String hgvsString;
        int deletionAaLength = referenceAllele.length() / 3;
        int codonPosition = transcriptUtils.getCodonPosition(cdsVariantStartPosition);
        int positionAtCodon = transcriptUtils.getPositionAtCodon(cdsVariantStartPosition);

        // We copy codonPosition to aminoacidPosition to be free of changing aminoacidPosition when needed
        int aminoacidPosition = codonPosition;

        // Check if this is an in frame deletion
        if (referenceAllele.length() % 3 == 0) {
            if (positionAtCodon == 1) {
                // TODO Check is the aminoposition is the STOP codon, this is a HGVS extension, is this a frameshift?
                if (aminoacidPosition + deletionAaLength > transcript.getProteinSequence().length()) {
                    // Deletion includes STOP codon
                    return null;
                }

                String nextAminoAcidAfterDeletion = transcript.getProteinSequence()
                        .substring(aminoacidPosition + deletionAaLength, aminoacidPosition + deletionAaLength + 1);
                if (nextAminoAcidAfterDeletion.equalsIgnoreCase("STOP")) {
                    // HGVS Substitution: in theory, a nonsense variant can be considered as a deletion removing the C-terminal end of
                    // the protein (e.g. p.Trp26_Arg1623del). However, in HGVS nomenclature, nonsense variants are described as an
                    // amino acid substitution (p.Trp26Ter or p.Trp26* see Substitution) replacing the first amino acid affected by a
                    // translation termination (stop) codon.

                    String leftCodedAa = transcript.getProteinSequence().substring(aminoacidPosition - 1, aminoacidPosition);
                    String leftAa = VariantAnnotationUtils.TO_LONG_AA.get(leftCodedAa);
                    hgvsString = "p." + leftAa + aminoacidPosition + "Ter";
                } else {
                    // Check for a very special case when the first Met (Met1) is deleted, we need to find another Met after the deletion
                    if (aminoacidPosition == 1) {
                        // p.Gly2_Met46del
                        //  a deletion of amino acids Gly2 to Met46 as a consequence of a variant silencing translation initiation ate Met1
                        //  but activating a new downstream translation initiation site (at Met46)
                        //  NOTE: the 3’ rule has been applied.

                        // We look for the first 'M' after the deletion end:  int i = deletionAaLength;
                        int nextStartCodonIndex = -1;
                        for (int i = deletionAaLength; i < transcript.getProteinSequence().length(); i++) {
                            if (transcript.getProteinSequence().charAt(i) == 'M') {
                                nextStartCodonIndex = i;
                                break;
                            }
                        }

                        if (nextStartCodonIndex >= 0) {
                            // The left amino acid is the second one, the first Met is reported as not deleted, see explanation.
                            String leftCodedAa = transcript.getProteinSequence().substring(aminoacidPosition, aminoacidPosition + 1);
                            String leftAa = VariantAnnotationUtils.TO_LONG_AA.get(leftCodedAa);
                            // The right amino acid MUST be always a Met
                            String rightCodedAa = transcript.getProteinSequence().substring(nextStartCodonIndex, nextStartCodonIndex + 1);
                            String rightAa = VariantAnnotationUtils.TO_LONG_AA.get(rightCodedAa);

                            hgvsString = "p." + leftAa + aminoacidPosition + "_" + rightAa + (nextStartCodonIndex + 1) + "del";
                        } else {
                            // no protein - This can only happen in the very unlikely case there is not other Met after the deletion
                            //  LRG_199p1:p.0
                            //  as a consequence of a variant in the translation initiation codon no protein is produced
                            //  NOTE: LRG_199p1:p.0? can be used when you predict that no protein is produced. Do not use descriptions
                            //  like “p.Met1Thr”, this is for sure not the consequence of the effect on protein translation
                            hgvsString = "p.0";
                        }
                    } else {
                        // HGVS Deletion: a sequence change between the translation initiation (start) and termination (stop) codon where,
                        // compared to a reference sequence, one or more amino acids are not present (deleted)
                        return this.hgvsDeletionFormatter(aminoacidPosition, referenceAllele);
                    }
                }

                StringBuilder alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
                alternateProteinSequence.replace(aminoacidPosition, aminoacidPosition + deletionAaLength, "");
                return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
            } else {
                // Position at codon is not 1 but is multiple of 3, but there is still the possibility to be a deletion
                // Example: position at codon is 3 and 6 nucleotides are deleted, but ASP is not changed, only LYS and ARG deleted
                //   CAAGCG/-
                //
                //   721    724    727    730    733
                //   241    242    243    244    245
                //   CGC    GAC    AAG    CGC    AGC
                //   ---------**************-----------
                //   164    165    166    167    168
                //   ARG    ASP    LYS    ARG    SER
                //   R      D      K      R      S

                // Check is deletion include STOP codon
                // TODO check this implementation
                if (aminoacidPosition + deletionAaLength > transcript.getProteinSequence().length()) {
//                    return calculateFrameshiftHgvs();
                    return null;
                }

                // Get the new codon created after the deletion
                String firstAffectedCodon = transcriptUtils.getCodon(aminoacidPosition);    // GAC
                String lastAffectedCodon = transcriptUtils.getCodon(aminoacidPosition + deletionAaLength);  // CGC
                String newAlternateCodon = firstAffectedCodon.substring(0, positionAtCodon - 1)
                        + lastAffectedCodon.substring(positionAtCodon - 1); // GA + C = GAC

                String firstAffectedAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), firstAffectedCodon);
                String lastAffectedAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), lastAffectedCodon);
                String newAlternateAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), newAlternateCodon);

                // Check special case when a new STOP codon is inserted
                if (newAlternateAa.equalsIgnoreCase("STOP")) {
                    // HGVS Substitution, if the codon affected becomes a STOP codon with on inserteion on the left then this is just
                    // an amino acid substitution
                    hgvsString = "p." + StringUtils.capitalize(newAlternateAa.toLowerCase()) + aminoacidPosition + "Ter";
                    StringBuilder alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
//                    alternateProteinSequence.insert(codonPosition - 1, StringUtils.join(codonPosition, ""));
                    return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
                }

                if (firstAffectedAa.equals(newAlternateAa) || lastAffectedAa.equals(newAlternateAa)) {
                    // Check if the new sequence is inserted left or right to the original reference codon.
                    // Remove the reference codon and update codonPosition for the insertion
                    StringBuilder allele = new StringBuilder();
                    if (firstAffectedAa.equalsIgnoreCase(newAlternateAa)) {
                        aminoacidPosition++;
                        for (int i = 0; i < deletionAaLength; i++) {
                            allele.append(transcriptUtils.getCodon(i + aminoacidPosition));
                        }
                    } else {
                        for (int i = 0; i < deletionAaLength; i++) {
                            allele.append(transcriptUtils.getCodon(i + aminoacidPosition));
                        }
                    }
                    return this.hgvsDeletionFormatter(aminoacidPosition, allele.toString());
                } else {
                    // HGVS DELETION-INSERTION
                    // The insertion happens between codonPosition - 1 and codonPosition
                    return this.hgvsDeletionInsertionFormatter(aminoacidPosition, aminoacidPosition + deletionAaLength, newAlternateCodon);
                }
            }
        } else {
            return calculateFrameshiftHgvs();
        }
    }

    private HgvsProtein hgvsDeletionFormatter(int aminoacidPosition, String referenceAllele) {
        // HGVS Deletion: a sequence change between the translation initiation (start) and termination (stop) codon where,
        // compared to a reference sequence, one or more amino acids are not present (deleted)

        String hgvsString;
        int deletionAaLength = referenceAllele.length() / 3;

        // Get deleted sequence
        List<String> aminoacids =  new ArrayList<>(deletionAaLength);
        for (int i = 0; i < referenceAllele.length(); i += 3) {
            String alternateCodon = referenceAllele.substring(i, i + 3);
            String alternateAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), alternateCodon);
            aminoacids.add(alternateAa);
        }

        // Move to the C-terminal (3' Rule) as much as possible in both single and multiple amino acid deletion:
        // LRG_199p1:p.Trp4del  (1 amino acid)
        //  a deletion of amino acid Trp4 in the sequence MetLeuTrpTrpGlu to MetLeuTrp_Glu
        //  NOTE: for deletions in single amino acid stretches or tandem repeats, the most C-terminal residue is arbitrarily
        //  assigned to have been deleted
        // LRG_199p1:p.Trp4del  (more than 1 amino acid)
        //  a deletion of amino acid Trp4 in the sequence MetLeuTrpTrpGlu to MetLeuTrp_Glu
        //  NOTE: for deletions in single amino acid stretches or tandem repeats, the most C-terminal residue is arbitrarily
        //  assigned to have been deleted
        //
        // We keep moving to the right while the first amino acid deleted equals the first one after deletion.
        // Check if we are at the end of the protein.
        if (aminoacidPosition + deletionAaLength <= transcript.getProteinSequence().length()) {
            String aaAfterDeletion = transcript.getProteinSequence()
                    .substring(aminoacidPosition + deletionAaLength - 1, aminoacidPosition + deletionAaLength);
            while (transcript.getProteinSequence().substring(aminoacidPosition - 1, aminoacidPosition).equals(aaAfterDeletion)) {
                aminoacidPosition++;
                // Check of there is enough protein sequence, we might reached the end
                if (aminoacidPosition + deletionAaLength <= transcript.getProteinSequence().length()) {
                    aaAfterDeletion = transcript.getProteinSequence()
                            .substring(aminoacidPosition + deletionAaLength - 1, aminoacidPosition + deletionAaLength);
                } else {
                    // we have reached the end of the protein
                    break;
                }
            }
        }

        // Get first amino acid DELETED
        String leftCodedAa = transcript.getProteinSequence().substring(aminoacidPosition - 1, aminoacidPosition);
        String leftAa = VariantAnnotationUtils.TO_LONG_AA.get(leftCodedAa);

        // Only 1 amino acid deleted
        if (aminoacids.size() == 1) {
            // LRG_199p1:p.Val7del
            //  a deletion of amino acid Val7 in the reference sequence LRG_199p1
            if (aminoacidPosition == 1 && leftAa.equals("Met")) {
                hgvsString = "p." + leftAa + aminoacidPosition + "?";
            } else {
                hgvsString = "p." + leftAa + aminoacidPosition + "del";
            }
        } else {
            // NP_003997.1:p.Lys23_Val25del
            //  a deletion of amino acids Lys23 to Val25 in reference sequence NP_003997.1
            // Get last amino acid DELETED
            String rightCodedAa = transcript.getProteinSequence()
                    .substring(aminoacidPosition + aminoacids.size() - 2, aminoacidPosition + aminoacids.size() - 1);
            String rightAa = VariantAnnotationUtils.TO_LONG_AA.get(rightCodedAa);
            hgvsString = "p." + leftAa + aminoacidPosition + "_" + rightAa + (aminoacidPosition + aminoacids.size() - 1) + "del";
        }

        StringBuilder alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
        alternateProteinSequence.replace(aminoacidPosition, aminoacidPosition + deletionAaLength, "");
        return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
    }

    private HgvsProtein hgvsDeletionInsertionFormatter(int leftAminoAcidPosition, int rightAminoAcidPosition, String alternate) {
        String hgvsString = "";

        // Get amino acid sequence of the insertion
        List<String> aminoacids =  new ArrayList<>(alternate.length() / 3);
        for (int i = 0; i < alternate.length(); i += 3) {
            String alternateCodon = alternate.substring(i, i + 3);
            String alternateAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), alternateCodon);
            aminoacids.add(VariantAnnotationUtils.buildUpperLowerCaseString(alternateAa));
        }

        // Check if a STOP codon is being inserted
        int stopIndex = -1;
        for (int i = 0; i < aminoacids.size(); i++) {
            if (aminoacids.get(i).equalsIgnoreCase(STOP_STRING)) {
                stopIndex = i;
                break;
            }
        }

        // Get position and flanking amino acids
        String leftCodedAa = transcript.getProteinSequence().substring(leftAminoAcidPosition - 1, leftAminoAcidPosition);
        String leftAa = VariantAnnotationUtils.TO_LONG_AA.get(leftCodedAa);
        String rightCodedAa = transcript.getProteinSequence().substring(rightAminoAcidPosition - 1, rightAminoAcidPosition);
        String rightAa = VariantAnnotationUtils.TO_LONG_AA.get(rightCodedAa);

        // One single amino acid is deleted
        if (leftAminoAcidPosition == rightAminoAcidPosition) {
            // At least one amino acid is inserted
            if (aminoacids.size() <= MAX_NUMBER_AMINOACIDS_DISPLAYED) {
                if (stopIndex < 0) {
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "delins" + StringUtils.join(aminoacids, "");
                } else {
                    List<String> nonStopCodonAminoAcids = aminoacids.subList(0, stopIndex);
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "delins" + StringUtils.join(nonStopCodonAminoAcids, "") + "Ter";
                }
            } else {
                // Check if last amino acid inserted is a STOP codon:
                if (stopIndex < 0) {
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "delins" + "(" + aminoacids.size() + ")";
                } else {
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "delins" + "(" + (stopIndex + 1) + ")";
                }
            }
        } else {
            if (aminoacids.size() <= MAX_NUMBER_AMINOACIDS_DISPLAYED) {
                if (stopIndex < 0) {
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "_"
                            + rightAa + rightAminoAcidPosition + "delins" +  StringUtils.join(aminoacids, "");
                } else {
                    List<String> nonStopCodonAminoAcids = aminoacids.subList(0, stopIndex);
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "_"
                            + rightAa + rightAminoAcidPosition + "delins" + StringUtils.join(nonStopCodonAminoAcids, "") + "Ter";
                }
            } else {
                // Check if last amino acid inserted is a STOP codon:
                if (stopIndex < 0) {
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "_"
                            + rightAa + rightAminoAcidPosition + "delins" + "(" + aminoacids.size() + ")";
                } else {
                    hgvsString = "p." + leftAa + leftAminoAcidPosition + "_"
                            + rightAa + rightAminoAcidPosition + "delins" + "(" + (stopIndex + 1) + ")";
                }
            }
        }

        StringBuilder alternateProteinSequence = new StringBuilder(transcript.getProteinSequence());
//        alternateProteinSequence.replace(aminoacidPosition, aminoacidPosition + deletionAaLength - 1, "");
        return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSequence.toString());
    }

    /**
     * This method can prodce 2 HGVS mutations
     *
     * HGVS Frame shift: a sequence change between the translation initiation (start) and termination (stop) codon where,
     * compared to a reference sequence, translation shifts to another reading frame.
     *
     * HGVS Extension: a sequence change extending the reference amino acid sequence at the N- or C-terminal end
     * with one or more amino acids.
     *
     * @return
     */
    private HgvsProtein calculateFrameshiftHgvs() {
        if (variant.getEnd() < transcript.getGenomicCodingStart() || variant.getStart() > transcript.getGenomicCodingEnd()) {
            return null;
        }

        // Get CDS position
        int cdsVariantStartPosition = HgvsCalculator.getCdsStart(transcript, variant.getEnd());
        if (cdsVariantStartPosition == 0) {
//            return new HgvsProtein(getProteinIds(), "", transcript.getProteinSequence());
            return null;
        }

        String hgvsString;

        int phaseOffset = 0;
        int currentAaIndex = 0;
        StringBuilder alternateProteinSeq = new StringBuilder();
        if (transcriptUtils.hasUnconfirmedStart()) {
            phaseOffset = transcriptUtils.getFirstCodonPhase();

            // if reference protein sequence start with X, prepend X to our new alternate sequence also
            if (transcript.getProteinSequence().startsWith(HgvsCalculator.UNKNOWN_AMINOACID)) {
                alternateProteinSeq.append("X");
                currentAaIndex++;
            }
        }

        int codonIndex = transcript.getCdnaCodingStart() + phaseOffset  - 1;
        int firstDiffIndex = -1;
        String firstReferencedAa = "";
        String firstAlternateAa = "";
        int stopIndex = -1;
        String stopAlternateAa = "";
        int originalStopIndex = -1;
        String alternateCdnaSeq = transcriptUtils.getAlternateCdnaSequence(variant);

        // We ned to include the STOP codon in the loop to check if there is a variant braking the STOP codon
        while (codonIndex + 3 <= alternateCdnaSeq.length()) {
            // Build the new amino acid sequence
            String alternateCodon = alternateCdnaSeq.substring(codonIndex, codonIndex + 3);
            String alternateAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), alternateCodon);
            String alternateCodedAa = VariantAnnotationUtils.TO_ABBREVIATED_AA.get(alternateAa);
            alternateProteinSeq.append(alternateCodedAa);

            // Alternate protein can miss a STOP codon and be longer than the reference protein
            if (currentAaIndex < transcript.getProteinSequence().length()) {
                String referenceCodedAa = String.valueOf(transcript.getProteinSequence().charAt(currentAaIndex));
//                System.out.println((currentAaIndex + 1) + ": " + referenceCodedAa + " - " + alternateCodedAa + " - " + alternateCodon);

                // STOP codons cannot exist inside the protein, and if a new STOP codon is generated the while loop os broken below.
                // The only explanation for this STOP codpn is to be the SEC amino acid.
                if (alternateAa .equalsIgnoreCase(STOP_STRING) && transcript.getProteinSequence().charAt(currentAaIndex) == 'U') {
                    alternateAa = "SEC";
                    alternateCodedAa = "U";
                    alternateProteinSeq.replace(alternateProteinSeq.length() - 1, alternateProteinSeq.length(), alternateCodedAa);
                }

                // Compare reference and new alternate amino acid.
                // Note if a premature STOP codon comes up this is also false
                if (!alternateCodedAa.equals(referenceCodedAa)) {
                    // Keep the first amino acid changed, including a premature STOP codon
                    if (firstDiffIndex == -1) {
                        firstReferencedAa = StringUtils.capitalize(VariantAnnotationUtils.TO_LONG_AA.get(referenceCodedAa).toLowerCase());
                        firstAlternateAa = StringUtils.capitalize(alternateAa.toLowerCase());
                        firstDiffIndex = currentAaIndex;

                    }

                    if (alternateAa.equalsIgnoreCase(STOP_STRING)) {
                        stopIndex = currentAaIndex;
                        break;
                    }
                }
            } else {
                // We have passed the protein sequence, the first time we get here is the STOP codon.
                // Incomplete 3' proteins do not reach this point, while finish before because last codon is not complete.'
//                System.out.println((currentAaIndex + 1) + ": null - " + alternateCodedAa + " - " + alternateCodon);
                if (currentAaIndex == transcript.getProteinSequence().length()) {
                    if (alternateAa.equalsIgnoreCase(STOP_STRING)) {
                        // STOP codon remains the same
//                        stopReferenceAa = StringUtils.capitalize(alternateAa.toLowerCase());
                        stopIndex = currentAaIndex;
                        break;
                    } else {
                        // STOP codon has changed
                        stopAlternateAa = StringUtils.capitalize(alternateAa.toLowerCase());
                        originalStopIndex = currentAaIndex;
                    }
                } else {
                    if (alternateAa.equalsIgnoreCase(STOP_STRING)) {
                        // A new STOP codon found after the original STOP codon, there are two possibilities:
                        //  1. there is an amino acid change
                        //  2. the original STOP codon is lost
                        // The position is adjusted below.
                        stopIndex = currentAaIndex;
                        break;
                    }
                }
            }

            // move to next codon and amino acid
            codonIndex += 3;
            currentAaIndex++;
        }

        // Create the HGVS string
        if (firstDiffIndex >= 0) {
            if (stopIndex >= 0) {
                if (firstAlternateAa.equalsIgnoreCase(STOP_STRING)) {
                    // p.(Tyr4*)
                    //  the predicted consequence at the protein level of the variant ATGGATGCATACGTCACG.. to ATGGATGCATA\_GTCACG (c.12delC)
                    //  is a Tyr to translation termination codon.
                    //  NOTE: the variant is described as a substitution, not as a frame shift (p.Tyr4TerfsTer1)
                    //
                    // We have found a STOP codon but not amino acid change
                    hgvsString = "p." + firstReferencedAa + (firstDiffIndex + 1) + "Ter";
                } else {
                    // p.Arg97ProfsTer23 (short p.Arg97fs)
                    //  a variant with Arg97 as the first amino acid changed, shifting the reading frame, replacing it for a Pro and
                    //  terminating at position Ter23.
                    hgvsString = "p." + firstReferencedAa + (firstDiffIndex + 1) + firstAlternateAa + "fsTer"
                            + (stopIndex - firstDiffIndex + 1);
                }
            } else {
                // p.Ile327Argfs*? (short p.Ile327fs)   * == Ter
                //  the predicted consequence of a frame shifting variant changes Ile327 to an Arg but the new reading frame
                //  does not encounter a new translation termination (stop) codon
                hgvsString = "p." + firstReferencedAa + (firstDiffIndex + 1) + firstAlternateAa + "fsTer?";
            }
        } else {
            // No amino acid difference found
            if (stopIndex >= 0) {
                if (StringUtils.isNotEmpty(stopAlternateAa)) {
                    // A new STOP codon hs been found, but not amino amino acid has changed so the original STOP codon changed
                    hgvsString = "p.Ter" + (originalStopIndex + 1) + stopAlternateAa + "extTer"
                            + (stopIndex - transcript.getProteinSequence().length());
                } else {
                    // Silent mutation in the STOP codon
                    hgvsString = "p.Ter" + (stopIndex + 1) + "=";
                }
            } else {
                if (StringUtils.isNotEmpty(stopAlternateAa)) {
                    // Original STOP codon changed and no new STOP codon found
                    hgvsString = "p.Ter" + (originalStopIndex + 1) + stopAlternateAa + "extTer?";
                } else {
                    // Not found firstDiffIndex and stopIndex, this means no AA changed and no STOP codon found. This typically happens
                    // in incomplete 3' ends
                    hgvsString = "p.Ter" + (currentAaIndex + 1) + "=";
                }
            }
        }

        return new HgvsProtein(getProteinIds(), hgvsString, alternateProteinSeq.toString());
    }

    protected char aaAt(int position) {
        return alternateProteinSequence.subSequence(position, position).charAt(0);
    }

    /**
     *
     * @return the AA sequence for the alternate
     */
    protected String getSequence() {
        return alternateProteinSequence.toString();
    }

    protected String getSequence(int start, int end) {
        return alternateProteinSequence.subSequence(start, end).toString();
    }

    // FIXME need to do this check early
    protected boolean onlySpansCodingSequence(Variant variant, Transcript transcript) {
        if (buildingComponents.getCdnaStart().getOffset() == 0  // Start falls within coding exon
                && buildingComponents.getCdnaEnd().getOffset() == 0) { // End falls within coding exon

            List<Exon> exonList = transcript.getExons();
            // Get the closest exon to the variant start, measured as the exon that presents the closest start OR end
            // coordinate to the position
            Exon nearestExon = exonList.stream().min(Comparator.comparing(exon ->
                    Math.min(Math.abs(variant.getStart() - exon.getStart()),
                            Math.abs(variant.getStart() - exon.getEnd())))).get();

            // Check if the same exon contains the variant end
            return variant.getEnd() >= nearestExon.getStart() && variant.getEnd() <= nearestExon.getEnd();

        }
        return false;
    }

    private List<String> getProteinIds() {
        List<String> proteinIds = new ArrayList<>();
        proteinIds.add(transcript.getProteinID());
        String uniprotAccession = transcriptUtils.getXrefId(UNIPROT_LABEL);
        if (StringUtils.isNotEmpty(uniprotAccession)) {
            proteinIds.add(uniprotAccession);
        }
        return proteinIds;
    }
}
