package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicts AA sequence given a transcript and a variant
 */
public class AlternateProteinSequencePredictor {

    protected static Logger logger = LoggerFactory.getLogger(AlternateProteinSequencePredictor.class);

    private final Variant variant;
    private final Transcript transcript;
    // protein sequence updated with variation
    private StringBuilder alternateProteinSequence = new StringBuilder();
    // Ter position
    private int stopCodonPosition;
    private static final String MT = "MT";
    private static final String STOP_STRING = "STOP";

    // what the AA is in the reference sequence
    private String referenceAA;
    // the AA in our alternate sequence
    private String alternateAA;

    /**
     * Constructor.
     *
     * @param variant variant of interest. Can be SNV, DEL or INS
     * @param transcript transcript containing variant
     */
    public AlternateProteinSequencePredictor(Variant variant, Transcript transcript) {
        this.variant = variant;
        this.transcript = transcript;
        init();
    }

    private void init() {

        // do translation
        String alternateDnaSequence = getAlternateDnaSequence();

        // loop through DNA, translating each codon
        for (int i = 0; i < alternateDnaSequence.length(); i = i + 3) {
            String codonArray = alternateDnaSequence.substring(i, i + 3);
            // three letter AA
            String predictedAA = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), codonArray);
            // single AA
            String abbreviatedAA = VariantAnnotationUtils.TO_ABBREVIATED_AA.get(predictedAA);

            // build the new sequence
            alternateProteinSequence.append(abbreviatedAA);

            int aaPosition = ((i - 1) / 3) + 1;

            if (STOP_STRING.equals(predictedAA)) {
                stopCodonPosition = aaPosition;
                // we're done
                return;
            // we found first different amino acid
            } else if (alternateAA == null && !abbreviatedAA.equals(transcript.getProteinSequence().substring(aaPosition))) {
                referenceAA = transcript.getProteinSequence().substring(aaPosition);
                alternateAA = predictedAA;
            }
        }


    }

    /**
     * @return three letter reference amino acid, e.g. ARG
     */
    public String getReferenceAA() {
        return referenceAA;
    }

    /**
     * @return three letter alternate amino acid, e.g. ARG
     */
    public String getAlternateAA() {
        return alternateAA;
    }

    public int getStopCodonPosition() {
        return stopCodonPosition;
    }


    /**
     *
     * @return the DNA sequence updated with the alternate sequence
     */
    protected String getAlternateDnaSequence() {
        StringBuilder alternateDnaSequence = new StringBuilder(transcript.getcDnaSequence());

        String reference = variant.getReference();
        String alternate = variant.getAlternate();

        int cdsPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());
        int cdnaVariantStart = HgvsCalculator.getCdnaCodingStart(transcript) + cdsPosition - 1;

        // manipulating strings which are 0-based with start value which is 1-based
        final int arrayPosition = cdnaVariantStart - 1;

        switch (variant.getType()) {
            case SNV:
                // String array is 0-based, subtract 1
                alternateDnaSequence.setCharAt(arrayPosition, alternate.charAt(0));
                break;
            case INDEL:

                // insertion
                if (StringUtils.isBlank(variant.getReference())) {
                    alternateDnaSequence.insert(arrayPosition, alternate);
                // deletion
                } else if (StringUtils.isBlank(variant.getAlternate())) {
                    alternateDnaSequence.replace(arrayPosition, arrayPosition + reference.length(), "");
                } else {
                    logger.debug("No HGVS implementation available for variant MNV.");
                    return null;
                }
                break;
            default:
                logger.debug("No HGVS implementation available for structural variants. Found {}.", variant.getType());
                return null;
        }
        return alternateDnaSequence.toString();
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

    // codeAt
}
