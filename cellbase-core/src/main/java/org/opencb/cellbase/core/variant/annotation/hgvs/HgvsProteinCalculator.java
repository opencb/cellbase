package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * Predicts AA sequence given a transcript and a variant.
 */
public class HgvsProteinCalculator {

    protected static Logger logger = LoggerFactory.getLogger(HgvsProteinCalculator.class);

    private final Variant variant;
    private final Transcript transcript;
    // protein sequence updated with variation
    private StringBuilder alternateProteinSequence;

    private static final String MT = "MT";
    private static final String STOP_STRING = "STOP";
    private static final String FRAMESHIFT_SUFFIX = "fs";
    private static final String EXTENSION_TAG = "ext";
    private static final String UNKOWN_STOP_CODON_POSITION = "*?";
    private static final String TERMINATION_SUFFIX = "Ter";
    private static final String INS_SUFFIX = "ins";
    private static final String DUP_SUFFIX = "dup";
    private static final char STOP_CODON_INDICATOR = '*';
    protected BuildingComponents buildingComponents = null;

    /**
     * Constructor.
     *
     * @param variant variant of interest. Can be SNV, DEL or INS
     * @param transcript transcript containing variant
     */
    public HgvsProteinCalculator(Variant variant, Transcript transcript) {
        this.variant = variant;
        this.transcript = transcript;

        this.init();
    }

    private void init() {

        // FIXME  restore !onlySpansCodingSequence(variant, transcript) check
        if (!HgvsCalculator.isCoding(transcript) || transcript.getProteinSequence() == null) {
            return;
        }

        alternateProteinSequence = new StringBuilder();
        buildingComponents = new BuildingComponents();

        // do translation
//        System.out.println(transcript.getcDnaSequence());
//        System.out.println(transcript.getProteinSequence());

        String alternateDnaSequence = getAlternateCdnaSequence();
//        System.out.println(alternateDnaSequence);

        int phaseOffset = 0;
        // current position in the protein string. JAVIER:  int aaPosition = ((codonPosition - 1) / 3) + 1;
        int currentAaIndex = 0;
        if (transcript.unconfirmedStart() || (transcript.getProteinSequence() != null
                && transcript.getProteinSequence().startsWith(HgvsCalculator.UNKNOWN_AMINOACID))) {
            phaseOffset = HgvsCalculator.getFirstCodingExonPhase(transcript);

            // if reference protein sequence start with X, prepend X to our new alternate sequence also
            if (transcript.getProteinSequence().startsWith(HgvsCalculator.UNKNOWN_AMINOACID)) {
                alternateProteinSequence.append("X");
                currentAaIndex++;
            }
        }

        // 1-based
        int cdnaPosition = transcript.getCdnaCodingStart();

        // initial codon position
        // Index variables are always 0-based for working with strings
        int codonIndex = cdnaPosition + phaseOffset - 1;

//        System.out.println(transcript.getcDnaSequence().length());
//        System.out.println(alternateDnaSequence.length());
//        System.out.println(transcript.getProteinSequence().length());
//        System.out.println(currentAaIndex);
//        System.out.println();

        // loop through DNA, translating each codon
//        while (transcript.getProteinSequence().length() > currentAaIndex) {
        int terPosition = 0;
        while (alternateDnaSequence.length() >= codonIndex) {

            int length = alternateDnaSequence.length();

            String codonArray = alternateDnaSequence.substring(codonIndex, codonIndex + 3);
            // three letter AA, eg PHE
            String alternateAa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), codonArray);
            // single AA, eg. L
            String codedAlternateAa = VariantAnnotationUtils.TO_ABBREVIATED_AA.get(alternateAa);

            // build the new sequence
            alternateProteinSequence.append(codedAlternateAa);

            String codedReferenceAa =  null;
            if (transcript.getProteinSequence().length() > currentAaIndex) {
                codedReferenceAa = String.valueOf(transcript.getProteinSequence().charAt(currentAaIndex));
            }

//            System.out.println(codonIndex);
//            System.out.println(currentAaIndex);
//            System.out.println(VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()),
//            transcript.getcDnaSequence().substring(codonIndex, codonIndex + 3)));
//            System.out.println(alternateAa);
//            System.out.println();
            if (terPosition > 0) {
                terPosition++;
            }
            if (STOP_STRING.equals(alternateAa)) {
//                buildingComponents.setTerminator(currentAaIndex + 1 - buildingComponents.getStart() + 1);
                if (terPosition > 0) {
                    buildingComponents.setTerminator(terPosition);
                } else {
                    buildingComponents.setTerminator(currentAaIndex + 1);
                }
                break;
            // we found first different amino acid
            } else {
                // if terminator position is 0, we haven't found the first different AA yet
                if (terPosition == 0 && !codedAlternateAa.equals(codedReferenceAa)) {
                    buildingComponents.setAlternate(alternateAa);
                    // put back to 1 - base
                    int start = currentAaIndex + 1;
                    buildingComponents.setStart(start);
                    // FIXME valid only for insertions?
                    buildingComponents.setEnd(start - 1);
                    terPosition = 1;
                }
            }

            // move to the next letter
            currentAaIndex++;
            // move to next codon
            codonIndex = codonIndex + 3;

        }

//        System.out.println(transcript.getcDnaSequence());
//        System.out.println(alternateDnaSequence);
//
//        System.out.println(transcript.getProteinSequence());
//        System.out.println(alternateProteinSequence);

        processBuildingComponents();

    }

    private void processBuildingComponents() {

        buildingComponents.setProteinId(transcript.getProteinID());

        int start = buildingComponents.getStart();
        int end = buildingComponents.getEnd();

        String referenceStartShortSymbol = String.valueOf(transcript.getProteinSequence().charAt(end - 1));

        // Might get out of sequence boundaries after right aligning resulting in a protein extension. Will then
        // set referenceEndShortSymbol to null to enable following if to pass
        String referenceEndShortSymbol
                = start == (transcript.getProteinSequence().length() + 1)
                ? null
                : String.valueOf(transcript.getProteinSequence().charAt(start - 1));

        // Do not generate protein HGVS if insertion affects an unconfirmed start, i.e. overlaps with an "X"
        // symbol in the protein sequence
        if (VariantAnnotationUtils.TO_LONG_AA.containsKey(referenceStartShortSymbol)
                && (referenceEndShortSymbol == null
                || VariantAnnotationUtils.TO_LONG_AA.containsKey(referenceEndShortSymbol))) {


            buildingComponents.setReferenceStart(VariantAnnotationUtils.buildUpperLowerCaseString(VariantAnnotationUtils
                    .TO_LONG_AA.get(referenceStartShortSymbol)));

            // null if the insertion is an extension, i.e. start is the next position after the last aa in the
            // protein sequence
            buildingComponents.setReferenceEnd(referenceEndShortSymbol == null
                    ? null
                    : VariantAnnotationUtils.buildUpperLowerCaseString(VariantAnnotationUtils
                    .TO_LONG_AA.get(referenceEndShortSymbol)));

            BuildingComponents.MutationType mutationType = getMutationType();
            buildingComponents.setMutationType(mutationType);
            buildingComponents.setKind(isFrameshift()
                    ? BuildingComponents.Kind.FRAMESHIFT
                    : BuildingComponents.Kind.INFRAME);

        }
    }

    private BuildingComponents.MutationType getMutationType() {

        String proteinSequence = transcript.getProteinSequence();
        int start = buildingComponents.getStart();
        String alternate = buildingComponents.getAlternate();

        // Insertion at stop codon - extension
        // Recall proteinVariant.getStart() is base 1
        if (start == (proteinSequence.length() + 1)) {
            return BuildingComponents.MutationType.EXTENSION;
        }

        // If stop gained then skip any normalisation; If there's a stop gain the stop indicator will always be the last
        // element of the predicted sequence, as prediction stops as soon as a STOP codon is found
        if (STOP_CODON_INDICATOR == alternate.charAt(alternate.length() - 1)) {
            return BuildingComponents.MutationType.STOP_GAIN;
        }

        // Check duplication
        // TODO: Assuming end = start - 1; might need adjusting
        String previousSequence = proteinSequence.substring(Math.max(0, start - alternate.length() - 1), start - 1);
        // normalized one in order to take into
        // account potential
        // normalization/lef-right alignment
        // differences
        if (previousSequence.equals(alternate)) {
            return BuildingComponents.MutationType.DUPLICATION;
        }

        return BuildingComponents.MutationType.INSERTION;
    }

    /**
     * Using the variant and transcript provided, builds the HGVS protein string.
     *
     * @return protein HGVS string
     */
    public String calculate() {

        // wasn't able to process sequence, won't be able to build hgvs string
        if (buildingComponents == null) {
            return null;
        }

        StringBuilder stringBuilder = (new StringBuilder(buildingComponents.getProteinId()))
                .append(HgvsCalculator.PROTEIN_CHAR);

        if (BuildingComponents.MutationType.DUPLICATION.equals(buildingComponents.getMutationType())) {
            if (buildingComponents.getAlternate().length() == 1) {
                // assuming end = start - 1
                stringBuilder.append(VariantAnnotationUtils
                        .buildUpperLowerCaseString(VariantAnnotationUtils
                                .TO_LONG_AA.get(String.valueOf(buildingComponents.getAlternate().charAt(0)))))
                        .append(buildingComponents.getEnd());
            } else {
                // assuming end = start - 1
                stringBuilder.append(VariantAnnotationUtils
                        .buildUpperLowerCaseString(VariantAnnotationUtils
                                .TO_LONG_AA.get(String.valueOf(buildingComponents.getAlternate().charAt(0)))))
                        .append(buildingComponents.getStart() - buildingComponents.getAlternate().length())
                        .append(HgvsCalculator.UNDERSCORE)
                        .append(VariantAnnotationUtils
                                .buildUpperLowerCaseString(VariantAnnotationUtils
                                        .TO_LONG_AA.get(String.valueOf(buildingComponents
                                                .getAlternate()
                                                .charAt(buildingComponents.getAlternate().length() - 1)))))
                        .append(buildingComponents.getEnd());
            }
            stringBuilder.append(DUP_SUFFIX);
//            .append(buildingComponents.getAlternate())


        } else if (BuildingComponents.MutationType.EXTENSION.equals(buildingComponents.getMutationType())) {
            try {
                stringBuilder.append(TERMINATION_SUFFIX)
                        .append(buildingComponents.getStart())
                        .append(VariantAnnotationUtils
                                .buildUpperLowerCaseString(VariantAnnotationUtils
                                        .TO_LONG_AA.get(String.valueOf(buildingComponents.getAlternate().charAt(0)))))
                        .append(EXTENSION_TAG)
                        .append(UNKOWN_STOP_CODON_POSITION);
            } catch (NullPointerException e) {
                int a = 1;
                throw e;
            }

        } else if (BuildingComponents.Kind.FRAMESHIFT.equals(buildingComponents.getKind())) {
            // Appends aa name properly formated; first letter uppercase, two last letters lowercase e.g. Arg
            stringBuilder.append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getStart())
                    .append(VariantAnnotationUtils.buildUpperLowerCaseString(buildingComponents.getAlternate()))
                    .append(FRAMESHIFT_SUFFIX)
                    .append(TERMINATION_SUFFIX)
                    .append(buildingComponents.getTerminator());
        } else if (BuildingComponents.MutationType.STOP_GAIN.equals(buildingComponents.getMutationType())) {
            stringBuilder.append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getStart())
                    .append(TERMINATION_SUFFIX);
        } else {
            // assuming end = start - 1
            stringBuilder.append(buildingComponents.getReferenceStart())
                    .append(buildingComponents.getEnd())
                    .append(HgvsCalculator.UNDERSCORE)
                    .append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getStart())
                    .append(INS_SUFFIX)
                    .append(formatAaSequence(buildingComponents.getAlternate()));
        }

        return stringBuilder.toString();

    }


    /**
     *
     * @return the DNA sequence updated with the alternate sequence
     */
    protected String getAlternateCdnaSequence() {
        StringBuilder alternateDnaSequence = new StringBuilder(transcript.getcDnaSequence());

        String reference = variant.getReference();
        String alternate = variant.getAlternate();

        // adjusted for phase
        int cdnaStartPosition = HgvsCalculator.getCdnaCodingStart(transcript);

        // genomic to cDNA
        int cdsStartPosition = HgvsCalculator.getCdsStart(transcript, variant.getStart());

        // -1 to manipulate strings
        int cdnaVariantIndex = cdsStartPosition + cdnaStartPosition - 1;

        switch (variant.getType()) {
            case SNV:
                alternateDnaSequence.setCharAt(cdnaVariantIndex, alternate.charAt(0));
                break;
            case INDEL:
                // insertion
                if (StringUtils.isBlank(variant.getReference())) {
                    alternateDnaSequence.insert(cdnaVariantIndex, alternate);
                // deletion
                } else if (StringUtils.isBlank(variant.getAlternate())) {
                    alternateDnaSequence.replace(cdnaVariantIndex, cdnaVariantIndex + reference.length(), "");
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

    private String formatAaSequence(String alternate) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < alternate.length(); i++) {
            stringBuilder.append(VariantAnnotationUtils
                    .buildUpperLowerCaseString(VariantAnnotationUtils.TO_LONG_AA
                            .get(String.valueOf(alternate.charAt(i)))));
        }
        return stringBuilder.toString();
    }

    // FIXME should be variant.start - cdnastart % 3 == 0, to be safe
    private boolean isFrameshift() {
        return !(variant.getAlternate().length() % 3 == 0);
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
}
