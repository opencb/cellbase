package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.UnsupportedURLVariantFormat;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fjlopez on 26/01/17.
 */
public class HgvsCalculator {

    protected static final char COLON = ':';
    private static final String CODING_TRANSCRIPT_CHAR = "c.";
    private static final String NON_CODING_TRANSCRIPT_CHAR = "n.";
    protected static final String PROTEIN_CHAR = "p.";
    protected static final char UNDERSCORE = '_';
    protected static final String POSITIVE = "+";
    protected static Logger logger = LoggerFactory.getLogger(HgvsCalculator.class);
    protected static final int NEIGHBOURING_SEQUENCE_SIZE = 100;
    protected GenomeDBAdaptor genomeDBAdaptor;
    protected BuildingComponents buildingComponents;

    public HgvsCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    // If allele is greater than this use allele length.
    private static final int MAX_ALLELE_LENGTH = 4;

    private static final VariantNormalizer NORMALIZER = new VariantNormalizer(false, false,
            false);


    public List<String> run(Variant variant, List<Gene> geneList) {
        return this.run(variant, geneList, true);
    }

    public List<String> run(Variant variant, List<Gene> geneList, boolean normalize) {
        List<String> hgvsList = new ArrayList<>();
        for (Gene gene : geneList) {
            hgvsList.addAll(this.run(variant, gene, normalize));
        }

        return hgvsList;
    }

    public List<String> run(Variant variant, Gene gene) {
        return run(variant, gene, true);
    }

    public List<String> run(Variant variant, Gene gene, boolean normalize) {
        List<String> hgvsList = new ArrayList<>(gene.getTranscripts().size());
        for (Transcript transcript : gene.getTranscripts()) {
            hgvsList.addAll(this.run(variant, transcript, gene.getId(), normalize));
        }

        return hgvsList;
    }

    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        // Check variant falls within transcript coords
        if (variant.getStart() <= transcript.getEnd() && variant.getEnd() >= transcript.getStart()) {
            // We cannot know the type of variant before normalization has been carried out
            Variant normalizedVariant = normalize(variant, normalize);
            HgvsCalculator hgvsCalculator = getHgvsCalculator(normalizedVariant);
            // Can be null if there's no hgvs implementation for the variant type
            if (hgvsCalculator != null) {
                // Normalization set to false - if needed, it would have been done already two lines above
                return hgvsCalculator.run(normalizedVariant, transcript, geneId, false);
            }
        }
        return Collections.emptyList();
    }

    private HgvsCalculator getHgvsCalculator(Variant normalizedVariant) {
//        switch (VariantAnnotationUtils.getVariantType(normalizedVariant)) {
        switch (normalizedVariant.getType()) {
            case SNV:
                return new HgvsSNVCalculator(genomeDBAdaptor);
            case INDEL:
                if (StringUtils.isBlank(normalizedVariant.getReference())) {
                    return new HgvsInsertionCalculator(genomeDBAdaptor);
                } else if (StringUtils.isBlank(normalizedVariant.getAlternate())) {
                    return new HgvsDeletionCalculator(genomeDBAdaptor);
                } else {
                    logger.debug("No HGVS implementation available for variant MNV. Returning empty list of HGVS "
                            + "identifiers.");
                    return null;
                }
            default:
                 logger.debug("No HGVS implementation available for structural variants. Found {}. Returning empty list"
                        + "  of HGVS identifiers.", normalizedVariant.getType());
                return null;
        }
    }

    protected Variant normalize(Variant variant, boolean normalize) {
        Variant normalizedVariant;
        // Convert VCF-style variant to HGVS-style.
        if (normalize) {
            List<Variant> normalizedVariantList = NORMALIZER.apply(Collections.singletonList(variant));
            if (normalizedVariantList != null && !normalizedVariantList.isEmpty()) {
                normalizedVariant = normalizedVariantList.get(0);
            } else {
                throw new UnsupportedURLVariantFormat("Variant " + variant.toString() + " cannot be properly normalized. "
                        + " Please check.");
            }
        } else {
            normalizedVariant = variant;
        }
        return normalizedVariant;
    }

    protected boolean isCoding(Transcript transcript) {
        // 0 in the cdnaCodingEnd means that the transcript doesn't
        // have a coding end <==> is non coding. Just annotating
        // coding transcripts in a first approach
        return transcript.getCdnaCodingEnd() != 0;
    }

    protected void setRangeCoordsAndAlleles(int genomicStart, int genomicEnd, String genomicReference,
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
//            start = variant.getStart() + variant.getReferenceStart().length() - 1;
            reference = genomicReference.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicReference.length())
                    : reverseComplementary(genomicReference);
            alternate = genomicAlternate.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicAlternate.length())
                    : reverseComplementary(genomicAlternate);
        }
        buildingComponents.setReferenceStart(reference);
        buildingComponents.setAlternate(alternate);
        buildingComponents.setCdnaStart(genomicToCdnaCoord(transcript, start));
        buildingComponents.setCdnaEnd(genomicToCdnaCoord(transcript, end));
    }

    private String reverseComplementary(String string) {
        StringBuilder stringBuilder = new StringBuilder(string).reverse();
        for (int i = 0; i < stringBuilder.length(); i++) {
            stringBuilder.setCharAt(i, VariantAnnotationUtils.COMPLEMENTARY_NT.get(stringBuilder.charAt(i)));
        }
        return stringBuilder.toString();
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

    protected CdnaCoord genomicToCdnaCoord(Transcript transcript, int genomicPosition) {
        if (isCoding(transcript)) {
            return genomicToCdnaCoordInCodingTranscript(transcript, genomicPosition);
        } else {
            return genomicToCdnaCoordInNonCodingTranscript(transcript, genomicPosition);
        }

    }

    private CdnaCoord genomicToCdnaCoordInNonCodingTranscript(Transcript transcript, int genomicPosition) {
        CdnaCoord cdnaCoord = new CdnaCoord();
        List<Exon> exonList = transcript.getExons();

        // Get the closest exon to the position, measured as the exon that presents the closest start OR end coordinate
        // to the position
        // Careful using GENOMIC coordinates
        Exon nearestExon = exonList.stream().min(Comparator.comparing(exon ->
                Math.min(Math.abs(genomicPosition - exon.getStart()),
                        Math.abs(genomicPosition - exon.getEnd())))).get();

        if (transcript.getStrand().equals("+")) {
            // Must now check which the closest edge of the exon is to the position: start or end to know which of them
            // to use as a reference
            // Careful using GENOMIC coordinates
            // Non-exonic variant: intronic
            // ------p------S||||||E------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            if (genomicPosition - nearestExon.getStart() < 0) {
                // offset must be negative
                cdnaCoord.setOffset(genomicPosition - nearestExon.getStart()); // TODO: probably needs +-1 bp adjust
                cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getStart()));
                cdnaCoord.setLandmark(CdnaCoord.Landmark.TRANSCRIPT_START);
            // Exonic variant
            // -------------S|||p||E------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else if (genomicPosition - nearestExon.getEnd() < 0) {
                // no offset
                cdnaCoord.setReferencePosition(getCdnaPosition(transcript, genomicPosition));
                cdnaCoord.setLandmark(CdnaCoord.Landmark.TRANSCRIPT_START);
            // Non-exonic variant: intronic, intergenic
            // -------------S||||||E-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else {
                // offset must be positive
                cdnaCoord.setOffset(genomicPosition - nearestExon.getEnd()); // TODO: probably needs +-1 bp adjust
                cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getEnd()));
                cdnaCoord.setLandmark(CdnaCoord.Landmark.TRANSCRIPT_START);
            }
        } else {
            // Must now check which the closest edge of the exon is to the position: start or end to know which of them
            // to use as a reference
            // Careful using GENOMIC coordinates
            // Non-exonic variant: intronic, intergenic
            // ------p------E||||||S------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            if (genomicPosition - nearestExon.getStart() < 0) {
                // offset must be positive
                cdnaCoord.setOffset(nearestExon.getStart() - genomicPosition); // TODO: probably needs +-1 bp adjust
                cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getStart()));
                cdnaCoord.setLandmark(CdnaCoord.Landmark.TRANSCRIPT_START);
            // Exonic variant
            // -------------E|||p||S------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else if (genomicPosition - nearestExon.getEnd() < 0) {
                // no offset
                cdnaCoord.setReferencePosition(getCdnaPosition(transcript, genomicPosition));
                cdnaCoord.setLandmark(CdnaCoord.Landmark.TRANSCRIPT_START);
            // Non-exonic variant: intronic, intergenic
            // -------------E||||||S-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else {
                // offset must be negative
                cdnaCoord.setOffset(nearestExon.getEnd() - genomicPosition); // TODO: probably needs +-1 bp adjust
                cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getEnd()));
                cdnaCoord.setLandmark(CdnaCoord.Landmark.TRANSCRIPT_START);
            }
        }

        return cdnaCoord;

    }

    private CdnaCoord genomicToCdnaCoordInCodingTranscript(Transcript transcript, int genomicPosition) {
        CdnaCoord cdnaCoord = new CdnaCoord();
        List<Exon> exonList = transcript.getExons();

        // Get the closest exon to the position, measured as the exon that presents the closest start OR end coordinate
        // to the position
        // Careful using GENOMIC coordinates
        Exon nearestExon = exonList.stream().min(Comparator.comparing(exon ->
                Math.min(Math.abs(genomicPosition - exon.getStart()),
                        Math.abs(genomicPosition - exon.getEnd())))).get();

        if (transcript.getStrand().equals("+")) {
            // Must now check which the closest edge of the exon is to the position: start or end to know which of them
            // to use as a reference
            // Careful using GENOMIC coordinates
            // Non-exonic variant: intronic
            // ------p------S||||||E------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            if (genomicPosition - nearestExon.getStart() < 0) {
                // Before coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setOffset(genomicPosition - nearestExon.getStart());
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getStart()) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // After coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setOffset(genomicPosition - nearestExon.getStart());
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getStart()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // Within coding start and end
                } else {
                    // offset must be negative
                    cdnaCoord.setOffset(genomicPosition - nearestExon.getStart()); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setReferencePosition(nearestExon.getCdsStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Exonic variant
            // -------------S|||p||E------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else if (genomicPosition - nearestExon.getEnd() < 0) {
                // Before coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // After coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // Within coding start and end
                } else {
                    // no offset
                    cdnaCoord.setReferencePosition(nearestExon.getCdsStart()
                            + genomicPosition - nearestExon.getGenomicCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Non-exonic variant: intronic, intergenic
            // -------------S||||||E-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else {
                // Before coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setOffset(genomicPosition - nearestExon.getEnd());
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // After coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setOffset(genomicPosition - nearestExon.getEnd());
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // Within coding start and end
                } else {
                    // offset must be positive
                    cdnaCoord.setOffset(genomicPosition - nearestExon.getEnd()); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setReferencePosition(nearestExon.getCdsEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            }
        } else {
            // Must now check which the closest edge of the exon is to the position: start or end to know which of them
            // to use as a reference
            // Careful using GENOMIC coordinates
            // Non-exonic variant: intronic, intergenic
            // ------p------E||||||S------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            if (genomicPosition - nearestExon.getStart() < 0) {
                // Before (genomic) coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setOffset(nearestExon.getStart() - genomicPosition);
                    cdnaCoord.setReferencePosition(transcript.getCdnaCodingEnd() - getCdnaPosition(transcript, nearestExon.getStart()));
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // After (genomic) coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setOffset(nearestExon.getStart() - genomicPosition);
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getStart()) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // Within coding start and end
                } else {
                    // offset must be positive
                    cdnaCoord.setOffset(nearestExon.getStart() - genomicPosition); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setReferencePosition(nearestExon.getCdsEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Exonic variant
            // -------------E|||p||S------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else if (genomicPosition - nearestExon.getEnd() < 0) {
                // Before (genomic) coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // After (genomic) coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // Within coding start and end
                } else {
                    // no offset
                    cdnaCoord.setReferencePosition(nearestExon.getCdsStart() + nearestExon.getGenomicCodingEnd() - genomicPosition);
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Non-exonic variant: intronic, intergenic
            // -------------E||||||S-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else {
                // Before (genomic) coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setOffset(nearestExon.getEnd() - genomicPosition);
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // After (genomic) coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setOffset(nearestExon.getEnd() - genomicPosition);
                    cdnaCoord.setReferencePosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // Within coding start and end
                } else {
                    // offset must be negative
                    cdnaCoord.setOffset(nearestExon.getEnd() - genomicPosition); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setReferencePosition(nearestExon.getCdsStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            }
        }

        return cdnaCoord;
    }

    private int getCdnaPosition(Transcript transcript, int genomicPosition) {

        int i = 0;
        int cdnaPosition = 0;
        List<Exon> exonList = transcript.getExons();

        // Sum the part that corresponds to the exon where genomicPosition is located
        if ("+".equals(transcript.getStrand())) {
            while (i < exonList.size() && genomicPosition > exonList.get(i).getEnd()) {
                cdnaPosition += (exonList.get(i).getEnd() - exonList.get(i).getStart() + 1);
                i++;
            }
            return cdnaPosition + genomicPosition - exonList.get(i).getStart() + 1;
        } else {
            while (i < exonList.size() && genomicPosition < exonList.get(i).getStart()) {
                cdnaPosition += (exonList.get(i).getEnd() - exonList.get(i).getStart() + 1);
                i++;
            }
            return cdnaPosition + exonList.get(i).getEnd() - genomicPosition + 1;
        }

    }

    /**
     * Generate a protein HGVS string.
     * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
     * @return String containing an HGVS formatted variant representation
     */
    protected String formatProteinString(BuildingComponents buildingComponents) {
        return null;
    }

    /**
     * Generate a transcript HGVS string.
     * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
     * @return String containing an HGVS formatted variant representation
     */
    protected String formatTranscriptString(BuildingComponents buildingComponents) {

        StringBuilder allele = new StringBuilder();
        allele.append(formatPrefix(buildingComponents));  // if use_prefix else ''
        allele.append(COLON);

        if (buildingComponents.getKind().equals(BuildingComponents.Kind.CODING)) {
            allele.append(CODING_TRANSCRIPT_CHAR).append(formatCdnaCoords(buildingComponents)
                    + formatDnaAllele(buildingComponents));
        } else if (buildingComponents.getKind().equals(BuildingComponents.Kind.NON_CODING)) {
            allele.append(NON_CODING_TRANSCRIPT_CHAR).append(formatCdnaCoords(buildingComponents)
                    + formatDnaAllele(buildingComponents));
        } else {
            throw new NotImplementedException("HGVS calculation not implemented for variant "
                    + buildingComponents.getChromosome() + ":"
                    + buildingComponents.getStart() + ":" + buildingComponents.getReferenceStart() + ":"
                    + buildingComponents.getAlternate() + "; kind: " + buildingComponents.getKind());
        }

        return allele.toString();

    }

    protected String formatDnaAllele(BuildingComponents buildingComponents) {
        return null;
    }

    protected boolean onlySpansCodingSequence(Variant variant, Transcript transcript) {
        if (buildingComponents.getCdnaStart().getReferencePosition() == 0  // Start falls within an exon
                && buildingComponents.getCdnaEnd().getReferencePosition() == 0) { // End falls within an exon

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

    protected int getFirstCdsPhase(Transcript transcript) {
        if (transcript.getStrand().equals(POSITIVE)) {
            return transcript.getExons().get(0).getPhase();
        } else {
            return transcript.getExons().get(transcript.getExons().size() - 1).getPhase();
        }
    }

    protected int getAminoAcidPosition(int cdnaCodingStart, int cdsPosition) {
        int cdnaPosition = cdsPosition + cdnaCodingStart - 1; // TODO: might need adjusting +-1
        int cdsVariantStart = cdnaPosition - cdnaCodingStart + 1;
        return ((cdsVariantStart - 1) / 3) + 1;
    }

    protected int getCdnaCodingStart(Transcript transcript) {
        int cdnaCodingStart = transcript.getCdnaCodingStart();
        if (transcript.unconfirmedStart()) {
            cdnaCodingStart -= ((3 - getFirstCdsPhase(transcript)) % 3);
        }
        return cdnaCodingStart;
    }

    protected String formatCdnaCoords(BuildingComponents buildingComponents) {
        return null;
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



}
