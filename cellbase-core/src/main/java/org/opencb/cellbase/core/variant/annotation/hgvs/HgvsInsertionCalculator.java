package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsInsertionCalculator extends HgvsCalculator {

    private static final String INS = "ins";
    private static final String DUP = "dup";
    private static final String MITOCHONDRIAL_CHROMOSOME_STRING = "MT";
    private static final char UNKNOWN_AA = 'X';
    private static final String STOP_STRING = "STOP";
    private static final String FRAMESHIFT_SUFFIX = "fs";
    private static final String EMPTY_STRING = "";
    private static final char STOP_CODON_INDICATOR = '*';
    private static final String STOP_GAIN = "stopGain";
    private static final String TERMINATION_SUFFIX = "Ter";

    public HgvsInsertionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    @Override
    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        buildingComponents = new BuildingComponents();
        Variant normalizedVariant = normalize(variant, normalize);
        String transcriptHgvs = calculateTranscriptHgvs(normalizedVariant, transcript, geneId);
        String proteinHgvs = calculateProteinHgvs(normalizedVariant, transcript);

        if (proteinHgvs == null) {
            return Collections.singletonList(transcriptHgvs);
        } else {
            return Arrays.asList(transcriptHgvs, proteinHgvs);
        }

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
            Variant proteinVariant = createProteinVariant(normalizedVariant, transcript);
            if (proteinVariant != null) {
                String mutationType = proteinHgvsNormalize(proteinVariant, transcript.getProteinSequence());
                buildingComponents.setStart(proteinVariant.getStart());
                buildingComponents.setEnd(proteinVariant.getEnd());
                buildingComponents.setReferenceStart(VariantAnnotationUtils
                        .buildUpperLowerCaseString(VariantAnnotationUtils
                                .TO_LONG_AA.get(String.valueOf(transcript.getProteinSequence()
                        .charAt(proteinVariant.getStart() - 1)))));
                buildingComponents.setReferenceEnd(VariantAnnotationUtils
                        .buildUpperLowerCaseString(VariantAnnotationUtils
                                .TO_LONG_AA.get(String.valueOf(transcript.getProteinSequence()
                        .charAt(proteinVariant.getStart())))));
                buildingComponents.setAlternate(proteinVariant.getAlternate());
                buildingComponents.setMutationType(mutationType);
                buildingComponents.setKind(normalizedVariant.getAlternate().length() % 3 == 0
                        ? BuildingComponents.Kind.INFRAME
                        : BuildingComponents.Kind.FRAMESHIFT);

                return formatProteinString(buildingComponents);
            }

        }
        return null;
    }

    /**
     * Generate a protein HGVS string.
     * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
     * @return String containing an HGVS formatted variant representation
     */
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


        } else if (BuildingComponents.Kind.FRAMESHIFT.equals(buildingComponents.getKind())) {
            // Appends aa name properly formated; first letter uppercase, two last letters lowercase e.g. Arg
            stringBuilder.append(VariantAnnotationUtils.TO_LONG_AA.get(String.valueOf(buildingComponents
                    .getReferenceStart()
                    .charAt(0))))
                    .append(buildingComponents.getStart())
                    .append(FRAMESHIFT_SUFFIX);
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

    private String formatAaSequence(String alternate) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < alternate.length(); i++) {
            stringBuilder.append(VariantAnnotationUtils
                    .buildUpperLowerCaseString(VariantAnnotationUtils.TO_LONG_AA
                            .get(String.valueOf(alternate.charAt(i)))));
        }
        return stringBuilder.toString();
    }

    private String proteinHgvsNormalize(Variant proteinVariant, String proteinSequence) {

        // If stop gained then skip any normalisation; If there's a stop gain the stop indicator will always be the last
        // element of the predicted sequence, as prediction stops as soon as a STOP codon is found
        if (STOP_CODON_INDICATOR == proteinVariant.getAlternate().charAt(proteinVariant.getAlternate().length() - 1)) {
            return STOP_GAIN;
        }

        proteinVariant = trimProteinSequences(proteinVariant);

        // It's not worth calling the justify method of the super class, too complicated and the code is relatively
        // simple
        // Justify
        // TODO: assuming this is justificaxtion sense; might need adjusting
        StringBuilder stringBuilder = new StringBuilder(proteinVariant.getAlternate());
        int end = proteinVariant.getEnd() - 1; // base 0 for string indexing
        while ((end + 1) < proteinSequence.length() && proteinSequence.charAt(end + 1) == stringBuilder.charAt(0)) {
            stringBuilder.deleteCharAt(0);
            stringBuilder.append(proteinSequence.charAt(end + 1));
            proteinVariant.setStart(proteinVariant.getStart() + 1);
            proteinVariant.setEnd(proteinVariant.getEnd() + 1);
            end = proteinVariant.getEnd() - 1; // base 0 for string indexing
        }
        proteinVariant.setAlternate(stringBuilder.toString());

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

        // I don't think this can ever happen as the alternate has been fully justified to the right in the loop above;
        // if there was a duplication on the right it'd no longer be true once it gets here as it'd have been shifted
        // to the right
//        else {
//            // TODO: Assuming end = start - 1; might need adjusting
//            String nextSequence = proteinSequence.substring(proteinVariant.getStart() - 1,
//                    Math.min(proteinSequence.length() - 1, proteinVariant.getEnd() + proteinSequence.length() - 1));
//            // normalized one in order to take into
//            // account potential
//            // normalization/lef-right alignment
//            // differences
//            if (nextSequence.equals(proteinVariant.getAlternate())) {
//                return DUP;
//            }
//        }

        return INS;

    }

    private Variant trimProteinSequences(Variant proteinVariant) {
        String reference = proteinVariant.getReference();
        String alternate = proteinVariant.getAlternate();

        if (StringUtils.isNotBlank(reference)
                && StringUtils.isNotBlank(alternate)) {
            // Shift backwards until a different aa is found
            int referencePosition = reference.length() - 1;
            int alternatePosition = alternate.length() - 1;
            while (referencePosition >= 0
                    && alternatePosition >= 0
                    && reference.charAt(referencePosition) == alternate.charAt(alternatePosition)) {
                referencePosition--;
                alternatePosition--;
            }
            // Update proteinVariant.reference and alternate strings after duplicated subsequence has been identified
            // above no need to update coordinates as it's an insertion
            proteinVariant.setReference(referencePosition < 0
                    ? EMPTY_STRING
                    : reference.substring(0, referencePosition + 1));
            proteinVariant.setAlternate(alternatePosition < 0
                    ? EMPTY_STRING
                    : alternate.substring(0, alternatePosition + 1));
        }

        return proteinVariant;
    }

    private Variant createProteinVariant(Variant variant, Transcript transcript) {
        Variant proteinVariant = new Variant();

//        int cdnaCodingStart = getCdnaCodingStart(transcript);
        proteinVariant.setStart(getAminoAcidPosition(getCdsStart(transcript, variant.getStart()), transcript));
        proteinVariant.setEnd(proteinVariant.getStart() - 1);
//        proteinVariant.setEnd(getAminoAcidPosition(genomicToCdnaCoord(transcript, variant.getEnd()).getReferencePosition()));

        // We expect buildingComponents.getStart() and buildingComponents.getEnd() to be within the sequence boundaries.
        // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
        // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
        if (proteinVariant.getEnd() > 0 && proteinVariant.getStart() <= transcript.getProteinSequence().length()) {
            String predictedProteinSequence = getPredictedProteinSequence(variant, transcript);
            if (StringUtils.isNotBlank(predictedProteinSequence)) {
                proteinVariant.setAlternate(predictedProteinSequence);
                // If insertion affects two different aa, paste both of them in the reference
//                if (!proteinVariant.getStart().equals(proteinVariant.getEnd())) {
//                    proteinVariant
//                            .setReference((new StringBuilder(transcript
//                                    .getProteinSequence()
//                                    .charAt(proteinVariant.getStart() - 1)))
//                            .append(transcript.getProteinSequence().charAt(proteinVariant.getEnd() - 1)).toString());
//                    // If it just affects one aa, paste just that aa in the reference
//                } else {
                    proteinVariant.setReference(String.valueOf(transcript
                            .getProteinSequence()
                            .charAt(proteinVariant.getStart() - 1)));
//                }

                return proteinVariant;
            } else {
                logger.warn("Could not predict protein sequence. This should, in principle, not happen and protein HGVS "
                                + "will not be returned. Please, check variant {}, transcript {}, protein {}",
                        variant.toString(), transcript.getId(), transcript.getProteinID());
            }
        } else {
            logger.warn("Protein start/end out of protein seq boundaries: {}, {}-{}, prot length: {}. This should, in principle,"
                            + " not happen and protein HGVS will not be returned. Could be expected for "
                            + "unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                    buildingComponents.getProteinId(), proteinVariant.getStart(), proteinVariant.getEnd(),
                    transcript.getProteinSequence().length());
        }

        return null;
    }


    /**
     * Required due to the peculiarities of insertion coordinates.
     * Translation to transcript cds position slightly varies for positive and negative strands because of the way
     * the insertion coordinates are interpreted in the genomic context; imagine following GENOMIC variant:
     * 7:-:GTATCCA
     * and following GENOMIC sequence
     * COORDINATES                             123456789 10 11 12 13 14 15 16 17 18 19 20
     * GENOMIC SEQUENCE                        AAGACTGTA T  C  C  A  G  G  T  G  G  G  C
     * ORF(bars indicate last nt of the codon)   |  |  |       |        |        |
     * VARIANT                                       ^
     * VARIANT                                       GTATCCA
     *
     * In a positive transcript shifted codon is GTA (genomic positions [7,9])
     * In a negative transcript shifted codon is AGT (reverse complementary of ACT, genomic positions [4,6]) and
     * therefore the cds start coordinate of the insertion must be +1
     * @param transcript  affected transcript data
     * @param genomicStart start genomic coordinate of the variant
     * @return corresponding cds start coordinate appropriately adjusted according to the transcript strand
     */
    protected int getCdsStart(Transcript transcript, int genomicStart) {
        return POSITIVE.equals(transcript.getStrand())
                ? genomicToCdnaCoord(transcript, genomicStart).getReferencePosition()
                : genomicToCdnaCoord(transcript, genomicStart).getReferencePosition() + 1;
    }

    private String getPredictedProteinSequence(Variant variant, Transcript transcript) {
        int cdsPosition = getCdsStart(transcript, variant.getStart());
//        int variantPhaseShift = (cdsPosition - 1) % 3;
        int variantPhaseShift = getPhaseShift(cdsPosition, transcript);
        int cdnaVariantStart = getCdnaCodingStart(transcript) + cdsPosition - 1;
//        // Sum 1 to cdnaVariantStart because of the peculiarities of insertion coordinates:
//        // cdnaVariantStart coincides with the vcf position, the actual substituted nt is the one on the right
//        Integer variantPhaseShift = (cdnaVariantStart + 1 - cdnaCodingStart) % 3;
//        int modifiedCodonStart = cdnaVariantStart + 1 - variantPhaseShift;

        int modifiedCodonStart =  cdnaVariantStart - variantPhaseShift;
        String transcriptSequence = transcript.getcDnaSequence();

        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            // alternate provided in genomic representation
            String alternate = POSITIVE.equals(transcript.getStrand())
                    ? variant.getAlternate()
                    : reverseComplementary(variant.getAlternate());
            // -1 and +2 because of base 0 String indexing
            String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
            char[] modifiedCodonArray = referenceCodon.toCharArray();

            // First modified position within the codon corresponds to the phase shift
            int modifiedCodonPosition = variantPhaseShift;
            StringBuilder predictedProteinSequence = new StringBuilder();
            for (int alternatePosition = 0; alternatePosition < alternate.length(); alternatePosition++) {
                modifiedCodonArray[modifiedCodonPosition] = alternate.toCharArray()[alternatePosition];
                if (modifiedCodonPosition == 2) {
                    // False means that no AA was added; this can only happen if a STOP codon is found
                    if (!addNewAa(variant, transcriptSequence, modifiedCodonArray, predictedProteinSequence)) {
                        return predictedProteinSequence.toString();
                    }
                }
                modifiedCodonPosition = (modifiedCodonPosition + 1) % 3;
            }

            // Last predicted codon from the alternate needs to be completed with transcript nts;
            // modifiedCodonPosition == 0 if is inframe insertion and it occurrs between two codons without disrupting
            // any of them
            if (modifiedCodonPosition > 0) {
                // indexing over transcriptSequence is 0 based
                int transcriptSequencePosition = cdnaVariantStart - 1;
                for (; modifiedCodonPosition < 3; modifiedCodonPosition++) {
                    modifiedCodonArray[modifiedCodonPosition]
                            = transcriptSequence.toCharArray()[transcriptSequencePosition];
                    transcriptSequencePosition++;
                }
                addNewAa(variant, transcriptSequence, modifiedCodonArray, predictedProteinSequence);
            }
            return predictedProteinSequence.toString();
        }
        return null;
    }

    private boolean addNewAa(Variant variant, String transcriptSequence, char[] modifiedCodonArray,
                             StringBuilder predictedProteinSequence) {
        String aa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), String.valueOf(modifiedCodonArray));
        if (aa != null) {
            // If STOP codon is gained prediction is interrupted and returned sequence is only predicted until
            // aa position before the STOP codon
            if (STOP_STRING.equals(aa)) {
                predictedProteinSequence.append(STOP_CODON_INDICATOR);
                return false;
            } else {
                predictedProteinSequence.append(VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aa));
            }
        } else {
            predictedProteinSequence.append(UNKNOWN_AA);
            logger.warn("Unknown AA translation. Protein HGVS will not be returned as protein sequence cannot be "
                            + "predicted. Variant {}, Codon {}, transcriptSequence {} ",
                    variant.toString(),
                    String.valueOf(modifiedCodonArray),
                    transcriptSequence);
            // Empty predicted protein sequence
            predictedProteinSequence.delete(0, predictedProteinSequence.length());
            return false;
        }
        return true;
    }

    private String getReferenceCodon(String chromosome, String cdnaSequence, int cdnaCodingStart, int cdsPosition) {
        // What buildingComponents.cdnaStart.offset really stores is the cdsStart
        int cdnaPosition = cdsPosition + cdnaCodingStart - 1; // TODO: might need adjusting +-1
        int variantPhaseShift = (cdnaPosition - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaPosition - variantPhaseShift;

        // -1 and +2 because of base 0 String indexing
        String referenceCodon = cdnaSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
        buildingComponents.setReferenceStart(VariantAnnotationUtils.getAminoacid(chromosome
                .equals(MITOCHONDRIAL_CHROMOSOME_STRING), referenceCodon));

        return null;
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

        buildingComponents.setMutationType(mutationType);
        buildingComponents.setTranscriptId(transcript.getId());
        buildingComponents.setGeneId(geneId);

        return formatTranscriptString(buildingComponents);
//        return Collections.singletonList(buildingComponents.format());
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

}
