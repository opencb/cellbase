package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.UnsupportedURLVariantFormat;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils.PROTEIN_CODING;

/**
 * Created by fjlopez on 26/01/17.
 */
public class HgvsCalculator {

    private GenomeDBAdaptor genomeDBAdaptor;

    public HgvsCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        this.genomeDBAdaptor = genomeDBAdaptor;
    }

    // If allele is greater than this use allele length.
    private static final int MAX_ALLELE_LENGTH = 4;

    private static final VariantNormalizer normalizer = new VariantNormalizer(false, false, true);


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
        if (variant.getStart() <= transcript.getEnd() && variant.getEnd() >= transcript.getStart()) {
            Variant normalizedVariant;
            // Convert VCF-style variant to HGVS-style.
            if (normalize) {
                List<Variant> normalizedVariantList = normalizer.apply(Collections.singletonList(variant));
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
            switch (VariantAnnotationUtils.getVariantType(variant)) {
                case INSERTION:
                    return calculateInsertionHgvs(variant, transcript, geneId);
                case DELETION:
                    return calculateDeletionHhgvs(variant, transcript, geneId, normalize);
                case SNV:
                    return calculateSNVHgvs(variant, transcript, geneId);
                default:
                    throw new UnsupportedURLVariantFormat();
            }
        }

        return Collections.emptyList();
    }

    private List<String> calculateInsertionHgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = hgvsNormalizeInsertion(variant, transcript, normalizedVariant);

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Use cDNA coordinates.
        hgvsStringBuilder.setKind("c");
        boolean isSingleBaseIndel = (("ins".equals(mutationType) && variant.getAlternate().length() == 1)
                || ("dup".equals(mutationType) && variant.getReference().length() == 1));


//        if mutation_type == '>' or (use_counsyl and is_single_base_indel):
//            # Use a single coordinate.
//        hgvs.cdna_start = genomic_to_cdna_coord(transcript, offset)
//        hgvs.cdna_end = hgvs.cdna_start
//        else:
        int start;
        int end;
        // Use a range of coordinates.
        if ("ins".equals(mutationType)) {
            // Insert uses coordinates around the insert point.
            start = normalizedVariant.getStart() - 1;
            end = normalizedVariant.getEnd();
        } else {
            start = normalizedVariant.getStart();
            end = normalizedVariant.getStart() + variant.getReference().length() - 1; // TODO: probably needs +-1 bp adjust
        }
        if ("-".equals(transcript.getStrand())) {
            // swap start and end
            int aux = start;
            start = end;
            end = aux;
        }

        genomicToCdnaCoord(transcript, offset_start)
        hgvs.cdna_end = genomic_to_cdna_coord(transcript, offset_end)


        return null;
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

        hgvsStringBuilder.setCdnaStart(genomicToCdnaCoord(transcript, variant));
        hgvsStringBuilder.setCdnaEnd(hgvsStringBuilder.getCdnaStart());

        // Populate prefix.
        hgvsStringBuilder.setTranscriptId(transcript.getId());
        hgvsStringBuilder.setGeneId(geneId);

        String reference;
        String alternate;
        // Convert alleles to transcript strand.
        if (transcript.getStrand().equals("-")) {
            reference = String.valueOf(VariantAnnotationUtils.COMPLEMENTARY_NT.get(variant.getReference()));
            alternate = String.valueOf(VariantAnnotationUtils.COMPLEMENTARY_NT.get(variant.getAlternate()));
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

    private CdnaCoord genomicToCdnaCoord(Transcript transcript, Variant variant) {
        CdnaCoord cdnaCoord = new CdnaCoord();

        ConsequenceType consequenceType = null;
        if (variant.getAnnotation() != null && variant.getAnnotation().getConsequenceTypes() != null
                && !variant.getAnnotation().getConsequenceTypes().isEmpty()) {
            consequenceType = getTranscriptConsequencetype(variant.getAnnotation().getConsequenceTypes(),
                    transcript.getId());
        }

        // It is not intronic variant - cdna and cds are ready within the ConsequenceType object
        if (consequenceType != null && consequenceType.getCdnaPosition() != null) {
            // Check transcript is protein coding, i.e. start/stop codon present
            if (PROTEIN_CODING.equals(transcript.getBiotype())) {
                // cds coordinate present - it's a coding variant, nothing else to do
                if (consequenceType.getCdsPosition() != null) {
                    cdnaCoord.setCdsPosition(consequenceType.getCdsPosition());
                    // it's UTR variant, get offset from start/stop codon as appropriate
                } else {
                    int startCodonOffset = transcript.getCdnaCodingStart() - consequenceType.getCdnaPosition();  // TODO: probably needs +-1 bp adjust
                    // variant located after the start codon - it's 3' UTR variant, get stop codon offset
                    if (startCodonOffset < 0) {
                        cdnaCoord.setStartStopCodonOffset(consequenceType.getCdnaPosition() - transcript.getCdnaCodingEnd());  // TODO: probably needs +-1 bp adjust
                        cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_STOP_CODON);
                    } else {
                        cdnaCoord.setStartStopCodonOffset(startCodonOffset);  // TODO: probably needs +-1 bp adjust
                        cdnaCoord.setLandmark(CdnaCoord.Landmark.CDNA_START_CODON);
                    }
                }
            // Otherwise just let the position be the cdna position
            } else {
                cdnaCoord.setCdsPosition(consequenceType.getCdnaPosition());
            }
        // Intronic variant - need to get a reference exon and corresponding offset
        } else {
            int genomicPosition = variant.getStart();
            calculateNearestExonRelativeCoords(transcript.getExons(), genomicPosition, cdnaCoord);
        }

        return cdnaCoord;
    }

    private void calculateNearestExonRelativeCoords(List<Exon> exonList, int genomicPosition, CdnaCoord cdnaCoord) {
        // Get the closest exon to the position, measured as the exon that presents the closest start OR end coordinate
        // to the position
        // Careful using GENOMIC coordinates
        Exon nearestExon = exonList.stream().min(Comparator.comparing(exon ->
                Math.min(Math.abs(genomicPosition - exon.getStart()),
                Math.abs(genomicPosition - exon.getEnd())))).get();

        int offset;
        int referenceCdsPosition;
        // Must now check which the closest edge of the exon is to the position: start or end to know which of them
        // to use as a reference
        // Careful using GENOMIC coordinates
        // ------p------S||||||E------------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
        if (genomicPosition - nearestExon.getStart() < 0) {
            offset =  genomicPosition - nearestExon.getStart(); // TODO: probably needs +-1 bp adjust
            referenceCdsPosition = nearestExon.getCdsStart();
        // -------------S||||||E-----p------; p = genomicPosition, S = nearestExon.getStart, E = nearestExon.getEnd
        } else {
            offset =  genomicPosition - nearestExon.getEnd(); // TODO: probably needs +-1 bp adjust
            referenceCdsPosition = nearestExon.getCdsEnd();
        }
        // Offset sign depends on transcript orientation - recall that offset is calculated above using genomic
        // coordinates, i.e. positive strand direction
        if (nearestExon.getStrand().equals("+")) {
            cdnaCoord.setCdsPosition(referenceCdsPosition);
            cdnaCoord.setStartStopCodonOffset(offset);
        } else {
            cdnaCoord.setCdsPosition(referenceCdsPosition);
            cdnaCoord.setStartStopCodonOffset(-offset);
        }
    }

    private ConsequenceType getTranscriptConsequencetype(List<ConsequenceType> consequenceTypeList, String transcriptId) {
        for (ConsequenceType consequenceType : consequenceTypeList) {
            if (consequenceType.getEnsemblTranscriptId().equals(transcriptId)) {
                return consequenceType;
            }
        }
        return null;
    }

}
