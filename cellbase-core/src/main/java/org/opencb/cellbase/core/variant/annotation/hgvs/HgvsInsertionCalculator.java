package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsInsertionCalculator extends HgvsCalculator {

    private static final int OFFSET_CONFIRMS_EQUAL_SEQUENCES = 10;

    enum SequenceComparisonState {
        RIGHT_ALIGNING,
        SPANNING_INSERTION,
        FAILED,
        FINISHED
    }

    private static final String INS = "ins";
    private static final String DUP = "dup";
//    private static final String MITOCHONDRIAL_CHROMOSOME_STRING = "MT";
//    private static final char UNKNOWN_AA = 'X';
    private static final String STOP_STRING = "STOP";
    private static final String FRAMESHIFT_SUFFIX = "fs";
    private static final String EMPTY_STRING = "";
    private static final char STOP_CODON_INDICATOR = '*';
    private static final String STOP_GAIN = "stopGain";
    private static final String EXTENSION = "extension";
    private static final String EXTENSION_TAG = "ext";
    private static final String UNKOWN_STOP_CODON_POSITION = "*?";
    private static final String TERMINATION_SUFFIX = "Ter";
    private static final String UNIPROT_LABEL = "uniprotkb/swissprot";

    public HgvsInsertionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    @Override
    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        buildingComponents = new BuildingComponents();
        Variant normalizedVariant = normalize(variant, normalize);
        String transcriptHgvs = calculateTranscriptHgvs(normalizedVariant, transcript, geneId);

        List<String> results = new ArrayList<>();
        if (transcriptHgvs != null) {
            results.add(transcriptHgvs);
            String proteinHgvs = calculateProteinHgvs(normalizedVariant, transcript);
            if (proteinHgvs != null) {
                results.add(proteinHgvs);
                String uniprotHgvs = getUniprotHgvs(transcript, proteinHgvs);
                if (StringUtils.isNotEmpty(uniprotHgvs)) {
                    results.add(uniprotHgvs);
                }
            }
        }
        return results;
    }

    /**
     * Method to create HGVS string with UniProt accession instead of Ensembl.
     *
     * @param transcript Transcript for this variant
     * @param proteinHgvs HGVS string already calculated for Ensembl protein id
     * @return the same HGVS string but with UniProt accession instead of Ensembl
     */
    private String getUniprotHgvs(Transcript transcript, String proteinHgvs) {
        String uniprotAccession = getUniprotAccession(transcript);
        if (uniprotAccession != null) {
            String[] array = proteinHgvs.split(":p.");
            if (array.length != 2) {
                return null;
            }
            return uniprotAccession + ":p." + array[1];
        }
        return null;
    }

    private String getUniprotAccession(Transcript transcript) {
        List<Xref> xrefs = transcript.getXrefs();
        if (xrefs != null && !xrefs.isEmpty()) {
            for (Xref xref : xrefs) {
                if (UNIPROT_LABEL.equals(xref.getDbName())) {
                    return xref.getId();
                }
            }
        }
        return null;
    }

    private String calculateProteinHgvs(Variant variant, Transcript transcript) {
        // Check if protein HGVS can be calculated
        if (isCoding(transcript) && onlySpansCodingSequence(variant, transcript)) {
            // Additional normalization required for insertions
            Variant normalizedVariant = new Variant();
            genomicHgvsNormalize(variant, transcript, normalizedVariant);
            buildingComponents.setProteinId(transcript.getProteinID());
            // We are storing aa position, ref aa and alt aa within a Variant object. This is just a technical issue to
            // be able to re-use methods and available objects

            HgvsProteinVariant proteinVariant = createProteinVariant(normalizedVariant, transcript);
            if (proteinVariant != null && transcript.getProteinSequence() != null) {
                String referenceStartShortSymbol = String.valueOf(transcript.getProteinSequence()
                        .charAt(proteinVariant.getEnd() - 1));

                // Might get out of sequence boundaries after right aligning resulting in a protein extension. Will then
                // set referenceEndShortSymbol to null to enable following if to pass
                String referenceEndShortSymbol
                        = proteinVariant.getStart() == (transcript.getProteinSequence().length() + 1)
                        ? null
                        : String.valueOf(transcript.getProteinSequence().charAt(proteinVariant.getStart() - 1));

                // Do not generate protein HGVS if insertion affects an unconfirmed start, i.e. overlaps with an "X"
                // symbol in the protein sequence
                if (VariantAnnotationUtils.TO_LONG_AA.containsKey(referenceStartShortSymbol)
                        && (referenceEndShortSymbol == null
                        || VariantAnnotationUtils.TO_LONG_AA.containsKey(referenceEndShortSymbol))) {
                    String mutationType = getMutationType(proteinVariant, transcript.getProteinSequence());
                    buildingComponents.setStart(proteinVariant.getStart());
                    buildingComponents.setEnd(proteinVariant.getEnd());
                    buildingComponents.setTerminator(proteinVariant.getTerminator());
                    buildingComponents.setReferenceStart(VariantAnnotationUtils
                            .buildUpperLowerCaseString(VariantAnnotationUtils
                                    .TO_LONG_AA.get(referenceStartShortSymbol)));

                    // null if the insertion is an extension, i.e. start is the next position after the last aa in the
                    // protein sequence
                    buildingComponents.setReferenceEnd(referenceEndShortSymbol == null
                            ? null
                            : VariantAnnotationUtils.buildUpperLowerCaseString(VariantAnnotationUtils
                            .TO_LONG_AA.get(referenceEndShortSymbol)));

                    buildingComponents.setAlternate(proteinVariant.getAlternate());
                    //buildingComponents.setMutationType(mutationType);
                    buildingComponents.setKind(isFrameshift(normalizedVariant)
                            ? BuildingComponents.Kind.FRAMESHIFT
                            : BuildingComponents.Kind.INFRAME);

                    return formatProteinString(buildingComponents);

                }
            }
        }
        return null;
    }

    /**
     * Generate a protein HGVS string.
     * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
     * @return String containing an HGVS formatted variant representation
     */
    @Deprecated
    protected String formatProteinString(BuildingComponents buildingComponents) {
        StringBuilder stringBuilder = (new StringBuilder(buildingComponents.getProteinId()))
                .append(PROTEIN_CHAR);

        if (DUP.equals(buildingComponents.getMutationType())) {
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
                        .append(UNDERSCORE)
                        .append(VariantAnnotationUtils
                                .buildUpperLowerCaseString(VariantAnnotationUtils
                                        .TO_LONG_AA.get(String.valueOf(buildingComponents
                                                .getAlternate()
                                                .charAt(buildingComponents.getAlternate().length() - 1)))))
                        .append(buildingComponents.getEnd());
            }
            stringBuilder.append(DUP);
//            .append(buildingComponents.getAlternate())


        } else if (EXTENSION.equals(buildingComponents.getMutationType())) {
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
                    .append(VariantAnnotationUtils
                            .buildUpperLowerCaseString(VariantAnnotationUtils
                                    .TO_LONG_AA.get(String.valueOf(buildingComponents.getAlternate().charAt(0)))))
                    .append(FRAMESHIFT_SUFFIX)
                    .append(TERMINATION_SUFFIX)
                    .append(buildingComponents.getTerminator());
        } else if (STOP_GAIN.equals(buildingComponents.getMutationType())) {
            stringBuilder.append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getStart())
                    .append(TERMINATION_SUFFIX);
        } else {
            // assuming end = start - 1
            stringBuilder.append(buildingComponents.getReferenceStart())
                    .append(buildingComponents.getEnd())
                    .append(UNDERSCORE)
                    .append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getStart())
                    .append(INS)
                    .append(formatAaSequence(buildingComponents.getAlternate()));
        }

        return stringBuilder.toString();

    }

    @Deprecated
    private String formatAaSequence(String alternate) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < alternate.length(); i++) {
            stringBuilder.append(VariantAnnotationUtils
                    .buildUpperLowerCaseString(VariantAnnotationUtils.TO_LONG_AA
                            .get(String.valueOf(alternate.charAt(i)))));
        }
        return stringBuilder.toString();
    }

    @Deprecated
    private String getMutationType(Variant proteinVariant, String proteinSequence) {

        // Insertion at stop codon - extension
        // Recall proteinVariant.getStart() is base 1
        if (proteinVariant.getStart() == (proteinSequence.length() + 1)) {
            return EXTENSION;
        }

        // If stop gained then skip any normalisation; If there's a stop gain the stop indicator will always be the last
        // element of the predicted sequence, as prediction stops as soon as a STOP codon is found
        if (STOP_CODON_INDICATOR == proteinVariant.getAlternate().charAt(proteinVariant.getAlternate().length() - 1)) {
            return STOP_GAIN;
        }

        // Check duplication
        // TODO: Assuming end = start - 1; might need adjusting
        String previousSequence = proteinSequence.substring(Math.max(0, proteinVariant.getStart()
                - proteinVariant.getAlternate().length() - 1), proteinVariant.getStart() - 1);
        // normalized one in order to take into
        // account potential
        // normalization/lef-right alignment
        // differences
        if (previousSequence.equals(proteinVariant.getAlternate())) {
            return DUP;
        }

        return INS;

    }

    @Deprecated
    private HgvsProteinVariant createProteinVariant(Variant variant, Transcript transcript) {

        int start = getAminoAcidPosition(getCdsStart(transcript, variant.getStart()), transcript);
        int end = start - 1;
        int positionTerminationSite = 0;
        // We expect buildingComponents.getStart() and buildingComponents.getEnd() to be within the sequence boundaries.
        // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
        // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
        if (end > 0 && transcript.getProteinSequence() != null && start <= transcript.getProteinSequence().length()) {
            // base 0
            int referencePosition = start - 1;
            int alternatePosition = referencePosition;
            int currentPosition = referencePosition;
            // after we have the alternate position, we keep predicting the rest of the protein until we get the stop codon indicator

            String reference = transcript.getProteinSequence();
            HgvsProteinCalculator proteinSequencePredictor = new HgvsProteinCalculator(variant, transcript);
            SequenceComparisonState sequenceComparisonState = SequenceComparisonState.RIGHT_ALIGNING;

            boolean insertionProcessed = false;

            char predictedAA = 0;
            while (!sequenceComparisonState.equals(SequenceComparisonState.FINISHED)
                    && !sequenceComparisonState.equals(SequenceComparisonState.FAILED)) {
                switch (sequenceComparisonState) {
                    case RIGHT_ALIGNING:
                        // Right align
                        predictedAA = proteinSequencePredictor.aaAt(currentPosition);
                        // No prediction could be made
                        if (predictedAA == 0) {
                            sequenceComparisonState = SequenceComparisonState.FAILED;
                        } else if (predictedAA == STOP_CODON_INDICATOR) {
                            sequenceComparisonState = SequenceComparisonState.FINISHED;
                            String alternate = proteinSequencePredictor.getSequence(start - 1, alternatePosition + 1);
                            positionTerminationSite = currentPosition - start + alternate.length();
                        } else if (insertionProcessed) {
                            // insertion has been processed. keep going until we hit the stop codon
                            currentPosition++;
                        // Reached end of reference protein sequence - comparison finished successfully
                        } else if (referencePosition == reference.length()) {
                            alternatePosition++;
                            currentPosition++;
                            // transform to base 1
                            start = referencePosition + 1;
                            end = start - 1;
                            sequenceComparisonState = SequenceComparisonState.FINISHED;
                        } else if (reference.charAt(currentPosition) == predictedAA) {
                            referencePosition++;
                            alternatePosition++;
                            currentPosition++;
                        // Actual insertion starts
                        } else {
                            alternatePosition++;
                            currentPosition++;

                            // transform to base 1
                            start = referencePosition + 1;
                            end = start - 1;

                            // For frameshift variants we just need the actual position within the protein sequence in
                            // which the protein sequence varies and therefore, there is no point in continuing with the
                            // -potentially large- process of prediction
                            if (isFrameshift(variant)) {
                                // continue predicting until we have reached the stop codon
                                insertionProcessed = true;
                                //sequenceComparisonState = SequenceComparisonState.FINISHED;
                            } else {
                                sequenceComparisonState = SequenceComparisonState.SPANNING_INSERTION;
                            }
                        }
                        break;
                    case SPANNING_INSERTION:
                        predictedAA = proteinSequencePredictor.aaAt(alternatePosition);
                        // No prediction could be made
                        if (predictedAA == 0) {
                            sequenceComparisonState = SequenceComparisonState.FAILED;
                        } else if (predictedAA == STOP_CODON_INDICATOR) {
                            sequenceComparisonState = SequenceComparisonState.FINISHED;
                            // Potentially end of insertion
                        } else if (reference.charAt(referencePosition) == predictedAA) {
                            // Must have been a single equal aa just by chance, need to confirm by checking a bunch of
                            // the following AAs and confirming they still follow the same sequence
                            if (confirmedEqualSequences(reference,
                                    referencePosition,
                                    proteinSequencePredictor,
                                    alternatePosition)) {
                                sequenceComparisonState = SequenceComparisonState.FINISHED;
                                // Span insertion
                            } else {
                                alternatePosition++;
                            }
                            // Span insertion
                        } else {
                            alternatePosition++;
                        }
                        break;
                    default:
                        break;
                }
            }

            if (sequenceComparisonState.equals(SequenceComparisonState.FINISHED)) {
                HgvsProteinVariant proteinVariant = new HgvsProteinVariant();
                proteinVariant.setReference(EMPTY_STRING);
                // base 0, open right end
                // If prediction terminated because of a STOP codon, that'll be the last char of the predicted sequence
                // and we want to include it so that the code downstream realises that it's a stop gain. Otherwise
                // the last aa in the predicted sequence will overlap with the original sequence an we must exclude it
                if (predictedAA == STOP_CODON_INDICATOR) {
                    proteinVariant.setAlternate(proteinSequencePredictor.getSequence(start - 1, alternatePosition + 1));
                } else {
                    proteinVariant.setAlternate(proteinSequencePredictor.getSequence(start - 1, alternatePosition));
                }
                proteinVariant.setStart(start);
                proteinVariant.setEnd(end);
                proteinVariant.setTerminator(positionTerminationSite);
                return proteinVariant;
            } else {
                logger.warn("Could not predict protein sequence. This should, in principle, not happen and protein HGVS "
                                + "will not be returned. Please, check variant {}, transcript {}, protein {}",
                        variant.toString(), transcript.getId(), transcript.getProteinID());
            }

        } else {
            int sequenceLength = 0;
            if (transcript.getProteinSequence() != null) {
                sequenceLength = transcript.getProteinSequence().length();
            }
            logger.warn("Protein start/end out of protein seq boundaries: {}, {}-{}, prot length: {}. This should, in principle,"
                            + " not happen and protein HGVS will not be returned. Could be expected for "
                            + "unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                    buildingComponents.getProteinId(),
                    start,
                    end,
                    sequenceLength);
        }

        return null;
    }

    @Deprecated
    private boolean isFrameshift(Variant variant) {
        return !(variant.getAlternate().length() % 3 == 0);
    }

    @Deprecated
    private boolean confirmedEqualSequences(String reference,
                                            int referenceStartingPosition,
                                            HgvsProteinCalculator proteinSequencePredictor,
                                            int alternateStartingPosition) {

        int referencePosition = referenceStartingPosition;
        int alternatePosition = alternateStartingPosition;
        char predictedAA = proteinSequencePredictor.aaAt(alternateStartingPosition);

        while (referencePosition < reference.length()
                && predictedAA != 0
                && (referencePosition - referenceStartingPosition) < OFFSET_CONFIRMS_EQUAL_SEQUENCES
                && reference.charAt(referencePosition) == predictedAA) {
            referencePosition++;
            alternatePosition++;
            predictedAA = proteinSequencePredictor.aaAt(alternatePosition);
        }

        // Only false if the two AAs are different. However, it could happen that the end of the reference sequence is
        // reached before the number of AAs in OFFSET_CONFIRMS_EQUALS_SEQUENCES is reached - that would still mean that
        // the sequences are equal and that we cannot continue checking. The end of the reference sequence will always
        // be reached before the end of the predicted sequence, as this is exclusively run for insertions. Therefore,
        // if any issue occurs during the prediction we'll consider the two sequences are not he same
        return !(referencePosition < reference.length()
                && predictedAA != reference.charAt(referencePosition));

    }




    private String calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = genomicHgvsNormalize(variant, transcript, normalizedVariant);

        // Use cDNA coordinates.
        buildingComponents.setKind(isCoding(transcript) ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate.
        if (INS.equals(mutationType)) {
            setRangeCoordsAndAlleles(normalizedVariant.getStart() - 1, normalizedVariant.getStart(),
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);

            // dup of just one nt use only one coordinate
        } else if (normalizedVariant.getLength() == 1) {
            setRangeCoordsAndAlleles(normalizedVariant.getStart() - 1, normalizedVariant.getStart() - 1,
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);
            // dup of more than 1nt
        } else {
            // DUPLICATION: in order for the setRangeCoordsAndAlleles to adequately set the range that is duplicated in
            // positive strands we need to substract the size of the insertion, e.g. :
            //             12345678 9 10 11 12
            //             GATGGTGG A C  A  T  GA
            //                     ^
            //                    TGG
            //   The insertion occurs at position 9; the actual duplicated sequence is that in the range [6, 8]
            int genomicStart = POSITIVE.equals(transcript.getStrand())
                    ? normalizedVariant.getStart() - normalizedVariant.getLength(): normalizedVariant.getStart();
            int genomicEnd = genomicStart + normalizedVariant.getLength() - 1;

            setRangeCoordsAndAlleles(genomicStart,
                    genomicEnd,
                    normalizedVariant.getReference(),
                    normalizedVariant.getAlternate(),
                    transcript,
                    buildingComponents);
        }

        // Check alternate & reference alleles could be properly calculated - e.g. allele strings with unexpected
        // characters would be rejected
        if (buildingComponents.getAlternate() != null && buildingComponents.getReferenceStart() != null) {
            //buildingComponents.setMutationType(mutationType);
            buildingComponents.setTranscriptId(transcript.getId());
            buildingComponents.setGeneId(geneId);

            return formatTranscriptString(buildingComponents);
        } else {
            return null;
        }
    }

    /**
     * Generate HGVS cDNA coordinates string.
     */
    @Override
    protected String formatCdnaCoords(BuildingComponents buildingComponents) {
        if (buildingComponents.getCdnaStart().equals(buildingComponents.getCdnaEnd())) {
            return buildingComponents.getCdnaStart().toString();
        } else {
            return buildingComponents.getCdnaStart().toString() + "_"
                    + buildingComponents.getCdnaEnd().toString();
        }
    }

    /**
     * Generate HGVS DNA allele.
     * @return
     */
    @Override
    protected String formatDnaAllele(BuildingComponents buildingComponents) {
        // Insertion or Insertion normalized as duplication
        // example:
        // "ENST00000382869.3:c.1735+32dupA", 1000_1001 insATG
        return buildingComponents.getMutationType() + buildingComponents.getAlternate();
    }

    private String genomicHgvsNormalize(Variant variant, Transcript transcript, Variant normalizedVariant) {
        // Get genomic sequence around the lesion.
        int start = Math.max(variant.getStart() - NEIGHBOURING_SEQUENCE_SIZE, 1);  // TODO: might need to adjust +-1 nt
        int end = variant.getStart() + NEIGHBOURING_SEQUENCE_SIZE;                 // TODO: might need to adjust +-1 nt
        Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                + ":" + start + "-" + end);
        String genomicSequence = genomeDBAdaptor.getGenomicSequence(query, new QueryOptions()).getResult().get(0).getSequence();

        // Create normalizedVariant and justify sequence to the right/left as appropriate
        normalizedVariant.setChromosome(variant.getChromosome());
        normalizedVariant.setStart(variant.getStart());
        normalizedVariant.setEnd(variant.getEnd());
        normalizedVariant.setReference(variant.getReference());
        normalizedVariant.setAlternate(variant.getAlternate());
        normalizedVariant.resetType();
        normalizedVariant.resetLength();
        // WARNING: it's tricky to understand startOffset and endOffset for insertions. For - strand, the position to
        // compare with the allele is the previous one, that is variant.getStart - 1. For + strand, the position to
        // compare with the allele is CURRENT position and NOT the next one, since the insertion takes place between
        // positions variant.getStart and (variant.getStart - 1). Pointing endOffset to variant.getStart-1 ensures
        // correct behaviour of the "justify" method, since it will be comparing the allele against (endOffset+1) for
        // + strand transcripts.
        justify(normalizedVariant, variant.getStart() - start, variant.getStart() - start - 1,
                normalizedVariant.getAlternate(), genomicSequence, transcript.getStrand());

        // Check duplication
        String previousSequence = genomicSequence.substring(Math.max(0,
                NEIGHBOURING_SEQUENCE_SIZE - variant.getAlternate().length()  // TODO: might need to adjust +-1 nt
                        + (normalizedVariant.getStart() - variant.getStart())), // Needs to sum the difference with the
                // normalized one in order to take into
                // account potential
                // normalization/lef-right alignment
                // differences
                NEIGHBOURING_SEQUENCE_SIZE + (normalizedVariant.getStart() - variant.getStart())); // Needs to sum the difference with the
        // normalized one in order to take into
        // account potential
        // normalization/lef-right alignment
        // differences
        if (previousSequence.equals(normalizedVariant.getAlternate())) {
            return DUP;
        } else {
            String nextSequence = genomicSequence.substring(NEIGHBOURING_SEQUENCE_SIZE // TODO: might need to adjust +-1 nt
                            + (normalizedVariant.getStart() - variant.getStart()), // Needs to sum the difference with the
                    // normalized one in order to take into
                    // account potential
                    // normalization/lef-right alignment
                    // differences
                    NEIGHBOURING_SEQUENCE_SIZE + variant.getAlternate().length()
                            + (normalizedVariant.getStart() - variant.getStart())); // Needs to sum the difference with the
            // normalized one in order to take into
            // account potential
            // normalization/lef-right alignment
            // differences
            if (nextSequence.equals(normalizedVariant.getAlternate())) {
                return DUP;
            }
        }
        return INS;
    }

//    class ProteinSequencePredictor {
//        private final Variant genomicInsertion;
//        private final Transcript transcript;
//        private final StringBuilder sequence = new StringBuilder();
//        // Always points to the position of the next AA to predict
//        private int aaCounter;
//        // To indicate the protein position where prediction starts
//        private final int shift;
//
//        private final int modifiedCodonStart;
//        private int transcriptSequencePosition;
//        private int modifiedCodonPosition;
//        private int alternatePosition;
//        private final String transcriptSequence;
//        private final String alternate;
//        private char[] modifiedCodonArray = null;
//
//        ProteinSequencePredictor(Variant genomicInsertion, Transcript transcript) {
//            this.genomicInsertion = genomicInsertion;
//            this.transcript = transcript;
//            int cdsPosition = getCdsStart(transcript, genomicInsertion.getStart());
//            // -1 to move to base 0
//            this.aaCounter = getAminoAcidPosition(cdsPosition, transcript) - 1;
//            this.shift = aaCounter;
//
//            int variantPhaseShift = getPhaseShift(cdsPosition, transcript);
//            int cdnaVariantStart = getCdnaCodingStart(transcript) + cdsPosition - 1;
//            // First modified position within the codon corresponds to the phase shift
//            modifiedCodonPosition = variantPhaseShift;
//
//            modifiedCodonStart =  cdnaVariantStart - variantPhaseShift;
//            transcriptSequence = transcript.getcDnaSequence();
//            transcriptSequencePosition = cdnaVariantStart - 1;  // 0-based and therefore - 1
//
//            // alternate provided in genomic representation
//            alternate = POSITIVE.equals(transcript.getStrand())
//                    ? genomicInsertion.getAlternate()
//                    : reverseComplementary(genomicInsertion.getAlternate());
//
//            // Out of boundaries might happen for insertions affecting start of unconfirmed-start transcripts.
//            // Prediction will only take place if start codon is well known, i.e. the whole predicted codon is within
//            // transcriptSequence boundaries
//            if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
//                // -1 and +2 because of base 0 String indexing
//                modifiedCodonArray = transcriptSequence.substring(modifiedCodonStart - 1,
//                        modifiedCodonStart + 2).toCharArray();
//            }
//
//            alternatePosition = 0;
//        }
//
//        /**
//         *
//         * @param position 0-based and always assumed to be greater or equal than the position where the insertion
//         *                 starts as projected from the genomic coordinates in genomicInsertion
//         * @return a char with predicted aminoacid at "position" (0-based). Returns "*" to indicate a stop gain in
//         * that position. Will return 0 if prediction could not be made.
//         */
//        public char aaAt(int position) {
//            if (position < shift) {
//                throw new RuntimeException("Position (" + position + ") must be greater than the the "
//                        + "position where the insertion starts (" + shift + ") as projected from the "
//                        + "genomic coordinates (" + genomicInsertion.toString() + "). Transcript: "
//                        + transcript.getId());
//            }
//
//            // If position had been predicted already, simply return it
//            if (position < aaCounter) {
//                return sequence.charAt(position - shift);
//            }
//
//            // Predict all aa until the requested one
//            for (; aaCounter <= position; aaCounter++) {
//                sequence.append(next());
//            }
//
//            return sequence.charAt(sequence.length() - 1);
//
//        }
//
//        private char next() {
//            if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
//                while (alternatePosition < alternate.length()) {
//                    modifiedCodonArray[modifiedCodonPosition] = alternate.toCharArray()[alternatePosition];
//                    if (modifiedCodonPosition == 2) {
//                        modifiedCodonPosition = (modifiedCodonPosition + 1) % 3;
//                        alternatePosition++;
//                        // 0 might be returned, meaning that no AA could be predicted; this can only happen if a
//                        // non-translatable codon is found, e.g. unexpected nt characters -non A,C,G,T- found in the
//                        // codon
//                        return getAA();
//                    }
//                    modifiedCodonPosition = (modifiedCodonPosition + 1) % 3;
//                    alternatePosition++;
//                }
//
//                // Last predicted codon from the alternate needs to be completed with transcript nts;
//                // indexing over transcriptSequence is 0 based
//                for (; modifiedCodonPosition < 3 && transcriptSequencePosition < transcriptSequence.length();
//                     modifiedCodonPosition++) {
//                    modifiedCodonArray[modifiedCodonPosition]
//                            = transcriptSequence.charAt(transcriptSequencePosition);
//                    transcriptSequencePosition++;
//                }
//
//                // Codon completed with transcript sequence nts
//                if (modifiedCodonPosition == 3) {
//                    // Reset modified codon position as it will always be 3 after the loop
//                    modifiedCodonPosition = 0;
//                    return getAA();
//                // Else end of transcript sequence reached before completing the codon, prediction cannot be made, return 0
//                }
//            }
//
//            return 0;
//
//        }
//
//        private char getAA() {
//            String aa = VariantAnnotationUtils.getAminoacid(MT.equals(genomicInsertion.getChromosome()),
//                    String.valueOf(modifiedCodonArray));
//            if (aa != null) {
//                // If STOP codon is gained prediction is interrupted and returned sequence is only predicted until
//                // aa position before the STOP codon
//                if (STOP_STRING.equals(aa)) {
//                    return STOP_CODON_INDICATOR;
//                } else {
//                    return VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aa).charAt(0);
//                }
//            } else {
//                logger.warn("Unknown AA translation. Protein HGVS will not be returned as protein sequence cannot be "
//                                + "predicted. Variant {}, Codon {}, transcriptSequence {} ",
//                        genomicInsertion.toString(),
//                        String.valueOf(modifiedCodonArray),
//                        transcriptSequence);
//                // Prediction could not be done
//                return 0;
//            }
//        }
//
//        public String subsequence(int start, int end) {
//            StringBuilder subsequence = new StringBuilder();
//            for (int i = start; i < end; i++) {
//                subsequence.append(aaAt(i));
//            }
//
//            return subsequence.toString();
//        }
//    }

    /**
     * Holder object for protein variant information.
     */
    private class HgvsProteinVariant extends Variant {
        /**
         *  Position new termination site. Location of the stop codon amino acid.
         */
        private int terminator;

        public int getTerminator() {
            return terminator;
        }

        public HgvsProteinVariant setTerminator(int terminator) {
            this.terminator = terminator;
            return this;
        }
    }
}
