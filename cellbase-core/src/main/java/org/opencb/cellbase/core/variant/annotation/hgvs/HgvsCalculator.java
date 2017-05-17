package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.UnsupportedURLVariantFormat;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fjlopez on 26/01/17.
 */
public class HgvsCalculator {

    private static final String DUP = "dup";
    private static final String INS = "ins";
    private static final String DEL = "del";
    private static final int NEIGHBOURING_SEQUENCE_SIZE = 100;
    private GenomeDBAdaptor genomeDBAdaptor;

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

    private List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        // Check variant falls within transcript coords
        if (variant.getStart() <= transcript.getEnd() && variant.getEnd() >= transcript.getStart()
                && transcript.getCdnaCodingEnd() != 0) { // 0 in the cdnaCodingEnd means that the transcript doesn't
                                                         // have a coding end <==> is non coding. Just annotating
                                                         // coding transcripts in a first approach
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

            // We cannot know the type of variant before normalization has been carried out
            switch (VariantAnnotationUtils.getVariantType(normalizedVariant)) {
                case INSERTION:
                    return calculateInsertionHgvs(normalizedVariant, transcript, geneId);
                case DELETION:
                    return calculateDeletionHhgvs(normalizedVariant, transcript, geneId);
                case SNV:
                    return calculateSNVHgvs(normalizedVariant, transcript, geneId);
                default:
                    throw new UnsupportedURLVariantFormat();
            }
        }

        return Collections.emptyList();
    }

    private List<String> calculateDeletionHhgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = hgvsNormalizeDeletion(variant, transcript, normalizedVariant);

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Use cDNA coordinates.
        hgvsStringBuilder.setKind("c");

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate
//        setRangeCoordsAndAlleles(normalizedVariant, transcript, hgvsStringBuilder);
        setRangeCoordsAndAlleles(normalizedVariant.getStart(), normalizedVariant.getEnd(),
                normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, hgvsStringBuilder);

        hgvsStringBuilder.setMutationType(mutationType);
        hgvsStringBuilder.setTranscriptId(transcript.getId());
        hgvsStringBuilder.setGeneId(geneId);

        return Collections.singletonList(hgvsStringBuilder.format());

    }

    private String hgvsNormalizeDeletion(Variant variant, Transcript transcript, Variant normalizedVariant) {
        // Get genomic sequence around the lesion.
        int start = Math.max(variant.getStart() - NEIGHBOURING_SEQUENCE_SIZE, 1);  // TODO: might need to adjust +-1 nt
        int end = variant.getStart() + NEIGHBOURING_SEQUENCE_SIZE;                 // TODO: might need to adjust +-1 nt
        Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                + ":" + start + "-" + end);
        String genomicSequence
                = genomeDBAdaptor.getGenomicSequence(query, new QueryOptions()).getResult().get(0).getSequence();

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
//        justify(normalizedVariant, variant.getStart() - start - 1,
//                variant.getStart() - start + normalizedVariant.getReference().length(),
//                normalizedVariant.getReference(), genomicSequence, transcript.getStrand());

        return DEL;
    }

    private List<String> calculateInsertionHgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = hgvsNormalizeInsertion(variant, transcript, normalizedVariant);

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Use cDNA coordinates.
        hgvsStringBuilder.setKind("c");
//        boolean isSingleBaseIndel = (("ins".equals(mutationType) && variant.getAlternate().length() == 1)
//                || ("dup".equals(mutationType) && variant.getReference().length() == 1));


//        if mutation_type == '>' or (use_counsyl and is_single_base_indel):
//            # Use a single coordinate.
//        hgvs.cdna_start = genomic_to_cdna_coord(transcript, offset)
//        hgvs.cdna_end = hgvs.cdna_start
//        else:
        int start;
        int end;
        String reference;
        String alternate;
        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate.
        if (INS.equals(mutationType)) {
//            reference = "";
//            if ("+".equals(transcript.getStrand())) {
//                // Insert uses coordinates around the insert point.
//                start = normalizedVariant.getStart() - 1;
//                end = start + 1;
//                alternate = normalizedVariant.getAlternate().length() > MAX_ALLELE_LENGTH
//                        ? String.valueOf(normalizedVariant.getAlternate().length()) : normalizedVariant.getAlternate();
//            } else {
//                // Insert uses coordinates around the insert point.
//                end = normalizedVariant.getStart() - 1;
//                start = end + 1;
//                alternate = normalizedVariant.getAlternate().length() > MAX_ALLELE_LENGTH
//                        ? String.valueOf(normalizedVariant.getAlternate().length())
//                        : reverseComplementary(normalizedVariant.getAlternate());
//            }
//            hgvsStringBuilder.setReference(reference);
//            hgvsStringBuilder.setAlternate(alternate);
//            hgvsStringBuilder.setCdnaStart(genomicToCdnaCoord(transcript, start));
//            hgvsStringBuilder.setCdnaEnd(genomicToCdnaCoord(transcript, end));
            setRangeCoordsAndAlleles(normalizedVariant.getStart() - 1, normalizedVariant.getStart(),
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, hgvsStringBuilder);

        // dup of just one nt use only one coordinate
        } else if (normalizedVariant.getLength() == 1) {
            setRangeCoordsAndAlleles(normalizedVariant.getStart() - 1, normalizedVariant.getStart() - 1,
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, hgvsStringBuilder);
        // dup of more than 1nt
        } else {
            // WARNING: -1 to fit the HGVS specification so that setRangeCoordsAndAlleles appropriately calculates
            // the offset to the nearest exon limit. This normalizedVariant object is not used after this line
            // and therefore has no other effect. Be careful
//            normalizedVariant.setStart(normalizedVariant.getStart() - 1);
//            setRangeCoordsAndAlleles(normalizedVariant, transcript, hgvsStringBuilder);
            setRangeCoordsAndAlleles(normalizedVariant.getStart(),
                    normalizedVariant.getStart() + normalizedVariant.getLength() - 1,
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, hgvsStringBuilder);
        }

        hgvsStringBuilder.setMutationType(mutationType);
        hgvsStringBuilder.setTranscriptId(transcript.getId());
        hgvsStringBuilder.setGeneId(geneId);

        return Collections.singletonList(hgvsStringBuilder.format());
    }

    private void setRangeCoordsAndAlleles(int genomicStart, int genomicEnd, String genomicReference,
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


//        int start;
//        int end;
//        String reference;
//        String alternate;
//        if ("+".equals(transcript.getStrand())) {
//            start = variant.getStart();
//            // TODO: probably needs +-1 bp adjust
////            end = variant.getStart() + variant.getReference().length() - 1;
//            end = variant.getStart() + variant.getLength() - 1;
//            reference = variant.getReference().length() > MAX_ALLELE_LENGTH
//                    ? String.valueOf(variant.getReference().length()) : variant.getReference();
//            alternate = variant.getAlternate().length() > MAX_ALLELE_LENGTH
//                    ? String.valueOf(variant.getAlternate().length()) : variant.getAlternate();
//        } else {
//            end = variant.getStart();
//            // TODO: probably needs +-1 bp adjust
//            start = variant.getStart() + variant.getLength() - 1;
////            start = variant.getStart() + variant.getReference().length() - 1;
//            reference = variant.getReference().length() > MAX_ALLELE_LENGTH
//                    ? String.valueOf(variant.getReference().length())
//                    : reverseComplementary(variant.getReference());
//            alternate = variant.getAlternate().length() > MAX_ALLELE_LENGTH
//                    ? String.valueOf(variant.getAlternate().length())
//                    : reverseComplementary(variant.getAlternate());
//        }
//        hgvsStringBuilder.setReference(reference);
//        hgvsStringBuilder.setAlternate(alternate);
//        hgvsStringBuilder.setCdnaStart(genomicToCdnaCoord(transcript, start));
//        hgvsStringBuilder.setCdnaEnd(genomicToCdnaCoord(transcript, end));

    }

    private String reverseComplementary(String string) {
        StringBuilder stringBuilder = new StringBuilder(string).reverse();
        for (int i = 0; i < stringBuilder.length(); i++) {
            stringBuilder.setCharAt(i, VariantAnnotationUtils.COMPLEMENTARY_NT.get(stringBuilder.charAt(i)));
        }
        return stringBuilder.toString();
    }

    private String hgvsNormalizeInsertion(Variant variant, Transcript transcript, Variant normalizedVariant) {
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

    /**
     * Justify an indel to the left or right along a sequence 'seq'.
     * @param variant: Variant object that needs to be justified. It will get modified accordingly.
     * @param startOffset: relative start position of the variant within genomicSequence (0-based)
     * @param endOffset: relative end position of the variant within genomicSequence (0-based, startOffset=endOffset
     *                 for insertions)
     * @param allele: String containing the allele that needs to be justified
     * @param genomicSequence: String containing the genomic sequence around the variant.getStart() position
     *                       (+-NEIGHBOURING_SEQUENCE_SIZE)
     * @param strand: String {"+", "-"}
     */
    private void justify(Variant variant, int startOffset, int endOffset, String allele, String genomicSequence,
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

    /**
     * Generates cdna HGVS names from an SNV.
     * @param transcript Transcript object that will be used as a reference
     */
    private List<String> calculateSNVHgvs(Variant variant, Transcript transcript, String geneId) {



        // Don't consider that reference = alternate -> there would be no variant then
//        // Convert VCF-style variant to HGVS-style
//        if (variant.getReference().equals(variant.getAlternate())) {
//            mutation_type = "=";
//        }

        String mutationType = ">";

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Populate coordinates.
        // Use cDNA coordinates.
        hgvsStringBuilder.setKind("c");

        hgvsStringBuilder.setCdnaStart(genomicToCdnaCoord(transcript, variant.getStart()));
        hgvsStringBuilder.setCdnaEnd(hgvsStringBuilder.getCdnaStart());

        // Populate prefix.
        hgvsStringBuilder.setTranscriptId(transcript.getId());
        hgvsStringBuilder.setGeneId(geneId);

        String reference;
        String alternate;
        // Convert alleles to transcript strand.
        if (transcript.getStrand().equals("-")) {
            reference = String.valueOf(VariantAnnotationUtils.COMPLEMENTARY_NT.get(variant.getReference().charAt(0)));
            alternate = String.valueOf(VariantAnnotationUtils.COMPLEMENTARY_NT.get(variant.getAlternate().charAt(0)));
        } else {
            reference = variant.getReference();
            alternate = variant.getAlternate();
        }

        // Populate alleles.
        hgvsStringBuilder.setMutationType(mutationType);
        hgvsStringBuilder.setReference(reference);
        hgvsStringBuilder.setAlternate(alternate);

        return Collections.singletonList(hgvsStringBuilder.format());
    }

    private CdnaCoord genomicToCdnaCoord(Transcript transcript, int genomicPosition) {
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
                    cdnaCoord.setStartStopCodonOffset(genomicPosition - nearestExon.getStart());
                    cdnaCoord.setCdsPosition(getCdnaPosition(transcript, nearestExon.getStart()) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // After coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setStartStopCodonOffset(genomicPosition - nearestExon.getStart());
                    cdnaCoord.setCdsPosition(getCdnaPosition(transcript, nearestExon.getStart()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // Within coding start and end
                } else {
                    // offset must be negative
                    cdnaCoord.setStartStopCodonOffset(genomicPosition - nearestExon.getStart()); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setCdsPosition(nearestExon.getCdsStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Exonic variant
            // -------------S|||p||E------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else if (genomicPosition - nearestExon.getEnd() < 0) {
                // Before coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setStartStopCodonOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // After coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setStartStopCodonOffset(getCdnaPosition(transcript, nearestExon.getStart()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // Within coding start and end
                } else {
                    // no offset
                    cdnaCoord.setCdsPosition(nearestExon.getCdsStart() + genomicPosition - nearestExon.getStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Non-exonic variant: intronic, intergenic
            // -------------S||||||E-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else {
                // Before coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setStartStopCodonOffset(genomicPosition - nearestExon.getEnd());
                    cdnaCoord.setCdsPosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // After coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setStartStopCodonOffset(genomicPosition - nearestExon.getEnd());
                    cdnaCoord.setCdsPosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // Within coding start and end
                } else {
                    // offset must be positive
                    cdnaCoord.setStartStopCodonOffset(genomicPosition - nearestExon.getEnd()); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setCdsPosition(nearestExon.getCdsEnd());
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
                    cdnaCoord.setStartStopCodonOffset(nearestExon.getStart() - genomicPosition);
                    cdnaCoord.setCdsPosition(transcript.getCdnaCodingStart() - getCdnaPosition(transcript, nearestExon.getStart()));
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // After (genomic) coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setStartStopCodonOffset(nearestExon.getStart() - genomicPosition);
                    cdnaCoord.setCdsPosition(transcript.getCdnaCodingStart() - getCdnaPosition(transcript, nearestExon.getStart()));
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // Within coding start and end
                } else {
                    // offset must be positive
                    cdnaCoord.setStartStopCodonOffset(nearestExon.getStart() - genomicPosition); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setCdsPosition(nearestExon.getCdsEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Exonic variant
            // -------------E|||p||S------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else if (genomicPosition - nearestExon.getEnd() < 0) {
                // Before (genomic) coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setStartStopCodonOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // After (genomic) coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setStartStopCodonOffset(getCdnaPosition(transcript, genomicPosition) - transcript.getCdnaCodingStart());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // Within coding start and end
                } else {
                    // no offset
                    cdnaCoord.setCdsPosition(nearestExon.getCdsStart() + nearestExon.getEnd() - genomicPosition);
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                }
            // Non-exonic variant: intronic, intergenic
            // -------------E||||||S-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
            } else {
                // Before (genomic) coding start
                if (genomicPosition < transcript.getGenomicCodingStart())  {
                    cdnaCoord.setStartStopCodonOffset(nearestExon.getEnd() - genomicPosition);
                    cdnaCoord.setCdsPosition(getCdnaPosition(transcript, nearestExon.getEnd()) - transcript.getCdnaCodingEnd());
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                // After (genomic) coding end
                } else if (genomicPosition > transcript.getGenomicCodingEnd()) {
                    cdnaCoord.setStartStopCodonOffset(nearestExon.getEnd() - genomicPosition);
                    cdnaCoord.setCdsPosition(transcript.getCdnaCodingEnd() - getCdnaPosition(transcript, nearestExon.getEnd()));
                    cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                // Within coding start and end
                } else {
                    // offset must be negative
                    cdnaCoord.setStartStopCodonOffset(nearestExon.getEnd() - genomicPosition); // TODO: probably needs +-1 bp adjust
                    cdnaCoord.setCdsPosition(nearestExon.getCdsStart());
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
