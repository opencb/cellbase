package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.UnsupportedURLVariantFormat;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fjlopez on 26/01/17.
 */
public class HgvsCalculator {

    protected static final int NEIGHBOURING_SEQUENCE_SIZE = 100;
    protected GenomeDBAdaptor genomeDBAdaptor;

    public HgvsCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    // If allele is greater than this use allele length.
    private static final int MAX_ALLELE_LENGTH = 4;

    private static final VariantNormalizer NORMALIZER = new VariantNormalizer(false, false, true);


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
            // Normalization set to false - if needed, it would have been done already two lines above
            return hgvsCalculator.run(normalizedVariant, transcript, geneId, false);
        }
        return Collections.emptyList();
    }

    private HgvsCalculator getHgvsCalculator(Variant normalizedVariant) {
        switch (VariantAnnotationUtils.getVariantType(normalizedVariant)) {
            case SNV:
                return new HgvsSNVCalculator(genomeDBAdaptor);
            case INSERTION:
                return new HgvsInsertionCalculator(genomeDBAdaptor);
            case DELETION:
                return new HgvsDeletionCalculator(genomeDBAdaptor);
            default:
                throw new UnsupportedURLVariantFormat();
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
                                          HgvsStringBuilder hgvsStringBuilder) {
        int start;
        int end;
        String reference;
        String alternate;
        if ("+".equals(transcript.getStrand())) {
            start = genomicStart;
            // TODO: probably needs +-1 bp adjust
//            end = variant.getStart() + variant.getReference().length() - 1;
            end = genomicEnd;
            reference = genomicReference.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicReference.length()) : genomicReference;
            alternate = genomicAlternate.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicAlternate.length()) : genomicAlternate;
        } else {
            end = genomicStart;
            // TODO: probably needs +-1 bp adjust
            start = genomicEnd;
//            start = variant.getStart() + variant.getReference().length() - 1;
            reference = genomicReference.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicReference.length())
                    : reverseComplementary(genomicReference);
            alternate = genomicAlternate.length() > MAX_ALLELE_LENGTH
                    ? String.valueOf(genomicAlternate.length())
                    : reverseComplementary(genomicAlternate);
        }
        hgvsStringBuilder.setReference(reference);
        hgvsStringBuilder.setAlternate(alternate);
        hgvsStringBuilder.setCdnaStart(genomicToCdnaCoord(transcript, start));
        hgvsStringBuilder.setCdnaEnd(genomicToCdnaCoord(transcript, end));
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
                    cdnaCoord.setReferencePosition(nearestExon.getCdsStart() + genomicPosition - nearestExon.getStart());
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
                    cdnaCoord.setReferencePosition(nearestExon.getCdsStart() + nearestExon.getEnd() - genomicPosition);
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

}
