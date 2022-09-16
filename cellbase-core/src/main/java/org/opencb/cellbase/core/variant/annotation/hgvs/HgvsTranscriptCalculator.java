package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils.COMPLEMENTARY_NT;

/**
 * Calculates HGVS string based on variant, transcript and gene.
 *
 */
public class HgvsTranscriptCalculator {

    private BuildingComponents buildingComponents;
    private static final Logger LOGGER = LoggerFactory.getLogger(HgvsProteinCalculator.class);
    private final Variant variant;
    private final Transcript transcript;
    private final TranscriptUtils transcriptUtils;
    private final String geneId;
    private static final String CODING_TRANSCRIPT_CHAR = "c.";
    private static final String NON_CODING_TRANSCRIPT_CHAR = "n.";
    private static final String POSITIVE = "+";
    // If allele is greater than this use allele length.
    private static final int MAX_ALLELE_LENGTH = 4;
    private static final String INS = "ins";
    private static final String DEL = "del";
    private static final String DUP = "dup";
    private static final int MINIMUM_NEIGHBOURING_SEQUENCE_SIZE = 100;
    private final GenomeDBAdaptor genomeDBAdaptor;

    /**
     * Constructor.
     *
     * @param variant variant of interest. Can be SNV, DEL or INS
     * @param transcript transcript containing variant
     * @param genomeDBAdaptor database connection used for querying sequences
     * @param geneId ensembl gene id
     */
    public HgvsTranscriptCalculator(GenomeDBAdaptor genomeDBAdaptor, Variant variant, Transcript transcript, String geneId) {
        this.variant = variant;
        this.transcript = transcript;
        this.transcriptUtils =  new TranscriptUtils(transcript);
        this.geneId = geneId;
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    /**
     * Generates cdna HGVS names from an SNV.
     * @return HGVS string for this transcript
     */
    public String calculate() {
        // Check reference and alternate alleles do not contain unexpected characters
        if (!HgvsCalculator.isValid(this.variant)) {
            return null;
        }

        switch (variant.getType()) {
            case SNV:
                return calculateSNVHgvsString();
            case INSERTION:
            case DELETION:
            case INDEL:
                if (StringUtils.isBlank(variant.getReference())) {
                    return calculateInsertionHgvsString();
                } else if (StringUtils.isBlank(variant.getAlternate())) {
                    return calculateDeletionHgvsString();
                } else {
                    LOGGER.debug("No HGVS implementation available for variant MNV. Returning empty list of HGVS "
                            + "identifiers.");
                    return null;
                }
            default:
                LOGGER.debug("No HGVS implementation available for structural variants. Found {}. Returning empty list"
                        + "  of HGVS identifiers.", variant.getType());
                return null;
        }
    }

    private String calculateSNVHgvsString() {
        buildingComponents = new BuildingComponents();

//        String mutationType = ">";

        // Populate coordinates.
        // Use cDNA coordinates.
        buildingComponents.setKind(transcriptUtils.isCoding() ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        buildingComponents.setCdnaStart(HgvsCalculator.genomicToCdnaCoord(transcript, variant.getStart()));
        buildingComponents.setCdnaEnd(buildingComponents.getCdnaStart());

        // Populate prefix.
        buildingComponents.setTranscriptId(transcript.getId());
        buildingComponents.setGeneId(geneId);

        String reference;
        String alternate;
        // Convert alleles to transcript strand.
        if (transcript.getStrand().equals("-")) {
            reference = String.valueOf(COMPLEMENTARY_NT.get(variant.getReference().charAt(0)));
            alternate = String.valueOf(COMPLEMENTARY_NT.get(variant.getAlternate().charAt(0)));
        } else {
            reference = variant.getReference();
            alternate = variant.getAlternate();
        }

        // Populate alleles.
        buildingComponents.setMutationType(BuildingComponents.MutationType.SUBSTITUTION);
        buildingComponents.setReferenceStart(reference);
        buildingComponents.setAlternate(alternate);

        StringBuilder allele = new StringBuilder();
        allele.append(formatPrefix(buildingComponents));  // if use_prefix else ''
        allele.append(":");

        String cdnaCoordinates = buildingComponents.getCdnaStart().toString();
        String dnaAllele = buildingComponents.getReferenceStart() + '>' + buildingComponents.getAlternate();

        String transcriptChar = null;
        if (buildingComponents.getKind().equals(BuildingComponents.Kind.CODING)) {
            transcriptChar = CODING_TRANSCRIPT_CHAR;
        } else if (buildingComponents.getKind().equals(BuildingComponents.Kind.NON_CODING)) {
            transcriptChar = NON_CODING_TRANSCRIPT_CHAR;
        } else {
            throw new NotImplementedException("HGVS calculation not implemented for variant "
                    + buildingComponents.getChromosome() + ":"
                    + buildingComponents.getStart() + ":" + buildingComponents.getReferenceStart() + ":"
                    + buildingComponents.getAlternate() + "; kind: " + buildingComponents.getKind());
        }

        allele.append(transcriptChar).append(cdnaCoordinates + dnaAllele);

        return allele.toString();
    }

    private String calculateInsertionHgvsString() {
        buildingComponents = new BuildingComponents();

        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        BuildingComponents.MutationType mutationType = genomicInsertionHgvsNormalize(variant, transcript, normalizedVariant);

        // Use cDNA coordinates.
        buildingComponents.setKind(transcriptUtils.isCoding() ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate.
        if (BuildingComponents.MutationType.INSERTION.equals(mutationType)) {
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
            buildingComponents.setMutationType(mutationType);
            buildingComponents.setTranscriptId(transcript.getId());
            buildingComponents.setGeneId(geneId);

            // return formatTranscriptString(buildingComponents);
            StringBuilder allele = new StringBuilder();
            allele.append(formatPrefix(buildingComponents));  // if use_prefix else ''
            allele.append(":");

            String cdnaCoordinates = buildingComponents.getCdnaStart().toString();
            if (buildingComponents.getCdnaStart() != null && !buildingComponents.getCdnaStart().equals(buildingComponents.getCdnaEnd())) {
                cdnaCoordinates = cdnaCoordinates + "_" + buildingComponents.getCdnaEnd().toString();
            }
            String dnaAllele = formatMutationType(buildingComponents.getMutationType()) + buildingComponents.getAlternate();

            String transcriptChar = null;
            if (buildingComponents.getKind().equals(BuildingComponents.Kind.CODING)) {
                transcriptChar = CODING_TRANSCRIPT_CHAR;
            } else if (buildingComponents.getKind().equals(BuildingComponents.Kind.NON_CODING)) {
                transcriptChar = NON_CODING_TRANSCRIPT_CHAR;
            } else {
                throw new NotImplementedException("HGVS calculation not implemented for variant "
                        + buildingComponents.getChromosome() + ":"
                        + buildingComponents.getStart() + ":" + buildingComponents.getReferenceStart() + ":"
                        + buildingComponents.getAlternate() + "; kind: " + buildingComponents.getKind());
            }

            allele.append(transcriptChar).append(cdnaCoordinates + dnaAllele);

            return allele.toString();
        } else {
            return null;
        }
    }

    private String calculateDeletionHgvsString() {
        buildingComponents = new BuildingComponents();

        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = transcriptDeletionHgvsNormalize(variant, transcript, normalizedVariant);

        // Use cDNA coordinates.
        buildingComponents.setKind(transcriptUtils.isCoding() ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate
//        setRangeCoordsAndAlleles(normalizedVariant, transcript, hgvsStringBuilder);
        setRangeCoordsAndAlleles(normalizedVariant.getStart(), normalizedVariant.getEnd(),
                normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);

        buildingComponents.setMutationType(BuildingComponents.MutationType.DELETION);
        buildingComponents.setTranscriptId(transcript.getId());
        buildingComponents.setGeneId(geneId);

      //  return formatTranscriptString(buildingComponents);
//        return hgvsStringBuildingComponents.format();

        StringBuilder allele = new StringBuilder();
        allele.append(formatPrefix(buildingComponents));  // if use_prefix else ''
        allele.append(":");

        String dnaAllele = formatMutationType(buildingComponents.getMutationType()) + buildingComponents.getReferenceStart();

        String cdnaCoordinates = buildingComponents.getCdnaStart().toString();
        if (buildingComponents.getCdnaStart() != null && !buildingComponents.getCdnaStart().equals(buildingComponents.getCdnaEnd())) {
            cdnaCoordinates = cdnaCoordinates + "_" + buildingComponents.getCdnaEnd().toString();
        }

        String transcriptChar = null;
        if (buildingComponents.getKind().equals(BuildingComponents.Kind.CODING)) {
            transcriptChar = CODING_TRANSCRIPT_CHAR;
        } else if (buildingComponents.getKind().equals(BuildingComponents.Kind.NON_CODING)) {
            transcriptChar = NON_CODING_TRANSCRIPT_CHAR;
        } else {
            throw new NotImplementedException("HGVS calculation not implemented for variant "
                    + buildingComponents.getChromosome() + ":"
                    + buildingComponents.getStart() + ":" + buildingComponents.getReferenceStart() + ":"
                    + buildingComponents.getAlternate() + "; kind: " + buildingComponents.getKind());
        }

        allele.append(transcriptChar).append(cdnaCoordinates + dnaAllele);

        return allele.toString();
    }

//    /**
//     * Generate a transcript HGVS string.
//     * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
//     * @return String containing an HGVS formatted variant representation
//     */
//    private String formatTranscriptString(BuildingComponents buildingComponents) {
//
//        StringBuilder allele = new StringBuilder();
//        allele.append(formatPrefix(buildingComponents));  // if use_prefix else ''
//        allele.append(":");
//
//        if (buildingComponents.getKind().equals(BuildingComponents.Kind.CODING)) {
//            allele.append(CODING_TRANSCRIPT_CHAR).append(formatCdnaCoords(buildingComponents)
//                    + formatDnaAllele(buildingComponents));
//        } else if (buildingComponents.getKind().equals(BuildingComponents.Kind.NON_CODING)) {
//            allele.append(NON_CODING_TRANSCRIPT_CHAR).append(formatCdnaCoords(buildingComponents)
//                    + formatDnaAllele(buildingComponents));
//        } else {
//            throw new NotImplementedException("HGVS calculation not implemented for variant "
//                    + buildingComponents.getChromosome() + ":"
//                    + buildingComponents.getStart() + ":" + buildingComponents.getReferenceStart() + ":"
//                    + buildingComponents.getAlternate() + "; kind: " + buildingComponents.getKind());
//        }
//
//        return allele.toString();
//
//    }

//    private String formatDnaAllele(BuildingComponents buildingComponents) {
//        switch (variant.getType()) {
//            case SNV:
//                return buildingComponents.getReferenceStart() + '>' + buildingComponents.getAlternate();
//            case INSERTION:
//            case DELETION:
//            case INDEL:
//                String mutationType = formatMutationType(buildingComponents.getMutationType());
//
//                if (StringUtils.isBlank(variant.getReference())) {
//                    // Insertion or Insertion normalized as duplication
//                    // example:
//                    // "ENST00000382869.3:c.1735+32dupA", 1000_1001 insATG
//                    if ("ins".equals(mutationType)) {
//                        return mutationType + buildingComponents.getAlternate();
//                    } else {
//                        return mutationType;
//                    }
//                } else if (StringUtils.isBlank(variant.getAlternate())) {
//                    // Delete
//                    // example:
//                    // 1000_1003d elATG
//                    //return mutationType + buildingComponents.getReferenceStart();
//                    return mutationType;
//                } else {
//                    LOGGER.debug("No HGVS implementation available for variant MNV. Returning empty list of HGVS "
//                            + "identifiers.");
//                    return null;
//                }
//            default:
//                LOGGER.debug("No HGVS implementation available for structural variants. Found {}. Returning empty list"
//                        + "  of HGVS identifiers.", variant.getType());
//                return null;
//        }
//    }

    private String formatMutationType(BuildingComponents.MutationType mutationType) {
        switch (mutationType) {
            case DELETION:
                return "del";
            case INSERTION:
                return "ins";
            case DUPLICATION:
                return "dup";
            default:
                throw new RuntimeException("Can't find mutation type " + mutationType);
        }
    }

    /**
     * Generate HGVS trancript/geneId prefix.
     * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
     * Some examples of full hgvs names with transcriptId include:
     * NM_007294.3:c.2207A>C
     * NM_007294.3(BRCA1):c.2207A>C
     */
    private String formatPrefix(BuildingComponents buildingComponents) {
        StringBuilder stringBuilder = new StringBuilder(buildingComponents.getTranscriptId());
        stringBuilder.append("(").append(buildingComponents.getGeneId()).append(")");

        return stringBuilder.toString();
    }

//    private String formatCdnaCoords(BuildingComponents buildingComponents) {
//        switch (variant.getType()) {
//            case SNV:
//                return buildingComponents.getCdnaStart().toString();
//            case INSERTION:
//            case DELETION:
//            case INDEL:
//                if (buildingComponents.getCdnaStart() != null
//                        && buildingComponents.getCdnaStart().equals(buildingComponents.getCdnaEnd())) {
//                    return buildingComponents.getCdnaStart().toString();
//                } else {
//                    return buildingComponents.getCdnaStart().toString() + "_" + buildingComponents.getCdnaEnd().toString();
//                }
//            default:
//                LOGGER.debug("No HGVS implementation available for structural variants. Found {}. Returning empty list"
//                        + "  of HGVS identifiers.", variant.getType());
//                return null;
//        }
//    }

    private void setRangeCoordsAndAlleles(int genomicStart, int genomicEnd, String genomicReference,
                                            String genomicAlternate, Transcript transcript,
                                            BuildingComponents buildingComponents) {
        int start;
        int end;
        String reference;
        String alternate;
        if ("+".equals(transcript.getStrand())) {
            start = genomicStart;
            // TODO: probably needs +-1 bp adjust
//            end = variant.getStart() + variant.getReferenceStart().length() - 1;
            end = genomicEnd;
            reference = genomicReference.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicReference.length()) : genomicReference;
            alternate = genomicAlternate.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicAlternate.length()) : genomicAlternate;
        } else {
            end = genomicStart;
            // TODO: probably needs +-1 bp adjust
            start = genomicEnd;
            reference = genomicReference.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicReference.length())
                    : VariantAnnotationUtils.reverseComplementary(genomicReference);
            alternate = genomicAlternate.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicAlternate.length())
                    : VariantAnnotationUtils.reverseComplementary(genomicAlternate);
        }
        buildingComponents.setReferenceStart(reference);
        buildingComponents.setAlternate(alternate);
        buildingComponents.setCdnaStart(HgvsCalculator.genomicToCdnaCoord(transcript, start));
        buildingComponents.setCdnaEnd(HgvsCalculator.genomicToCdnaCoord(transcript, end));
    }

    private BuildingComponents.MutationType genomicInsertionHgvsNormalize(Variant variant, Transcript transcript,
                                                                          Variant normalizedVariant) {
        // Get genomic sequence around the lesion.
        int neighbouringSequenceSize = Math.max(MINIMUM_NEIGHBOURING_SEQUENCE_SIZE, variant.getAlternate().length());
        int start = Math.max(variant.getStart() - neighbouringSequenceSize, 1);  // TODO: might need to adjust +-1 nt
        int end = variant.getStart() + neighbouringSequenceSize + variant.getAlternate().length(); // TODO: might need to adjust +-1 nt
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
                neighbouringSequenceSize - variant.getAlternate().length()  // TODO: might need to adjust +-1 nt
                        + (normalizedVariant.getStart() - variant.getStart())), // Needs to sum the difference with the
                // normalized one in order to take into
                // account potential
                // normalization/lef-right alignment
                // differences
                neighbouringSequenceSize + (normalizedVariant.getStart() - variant.getStart())); // Needs to sum the difference with the
        // normalized one in order to take into
        // account potential
        // normalization/lef-right alignment
        // differences
        if (previousSequence.equals(normalizedVariant.getAlternate())) {
            return BuildingComponents.MutationType.DUPLICATION;
        } else {
            String nextSequence = genomicSequence.substring(neighbouringSequenceSize // TODO: might need to adjust +-1 nt
                            + (normalizedVariant.getStart() - variant.getStart()), // Needs to sum the difference with the
                    // normalized one in order to take into
                    // account potential
                    // normalization/lef-right alignment
                    // differences
                    neighbouringSequenceSize + variant.getAlternate().length()
                            + (normalizedVariant.getStart() - variant.getStart())); // Needs to sum the difference with the
            // normalized one in order to take into
            // account potential
            // normalization/lef-right alignment
            // differences
            if (nextSequence.equals(normalizedVariant.getAlternate())) {
                return BuildingComponents.MutationType.DUPLICATION;
            }
        }
        return BuildingComponents.MutationType.INSERTION;
    }

    private String transcriptDeletionHgvsNormalize(Variant variant, Transcript transcript, Variant normalizedVariant) {
        // Get genomic sequence around the lesion.
        int start = Math.max(variant.getStart() - MINIMUM_NEIGHBOURING_SEQUENCE_SIZE, 1);  // TODO: might need to adjust +-1 nt
        int end = variant.getStart() + MINIMUM_NEIGHBOURING_SEQUENCE_SIZE;                 // TODO: might need to adjust +-1 nt
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
        // startOffset must point to the position right before the actual variant start, since that's the position that
        // will be looked at for coincidences within the variant reference sequence. Likewise, endOffset must point tho
        // the position right after the actual variant end.
        justify(normalizedVariant, variant.getStart() - start,
                variant.getStart() - start + normalizedVariant.getReference().length() - 1,
                normalizedVariant.getReference(), genomicSequence, transcript.getStrand());

        return DEL;
    }

    /**
     * Justify an indel to the left or right along a sequence 'seq'.
     * @param variant Variant object that needs to be justified. It will get modified accordingly.
     * @param startOffset relative start position of the variant within genomicSequence (0-based).
     * @param endOffset relative end position of the variant within genomicSequence (0-based, startOffset=endOffset
     *                 for insertions).
     * @param allele String containing the allele that needs to be justified.
     * @param genomicSequence String containing the genomic sequence around the variant.getStart() position
     *                       (+-NEIGHBOURING_SEQUENCE_SIZE).
     * @param strand String {"+", "-"}.
     */
    protected void justify(Variant variant, int startOffset, int endOffset, String allele, String genomicSequence,
                           String strand) {
        StringBuilder stringBuilder = new StringBuilder(allele);
        // Justify to the left
        if ("-".equals(strand)) {
            while (startOffset > 0 && genomicSequence.charAt(startOffset - 1) == stringBuilder.charAt(stringBuilder.length() - 1)) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.insert(0, genomicSequence.charAt(startOffset - 1));
                startOffset--;
                endOffset--;
                variant.setStart(variant.getStart() - 1);
                variant.setEnd(variant.getEnd() - 1);
            }
            // Justify to the right
        } else {
            while ((endOffset + 1) < genomicSequence.length() && genomicSequence.charAt(endOffset + 1) == stringBuilder.charAt(0)) {
                stringBuilder.deleteCharAt(0);
                stringBuilder.append(genomicSequence.charAt(endOffset + 1));
                startOffset++;
                endOffset++;
                variant.setStart(variant.getStart() + 1);
                variant.setEnd(variant.getEnd() + 1);
            }
        }
        // Insertion
        if (variant.getReference().isEmpty()) {
            variant.setAlternate(stringBuilder.toString());
            // Deletion
        } else {
            variant.setReference(stringBuilder.toString());
        }
    }
}
