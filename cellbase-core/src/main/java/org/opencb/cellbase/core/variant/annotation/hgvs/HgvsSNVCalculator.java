package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsSNVCalculator extends HgvsCalculator {
    public HgvsSNVCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    @Override
    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        buildingComponents = new BuildingComponents();

        Variant normalizedVariant = normalize(variant, normalize);
        return calculateTranscriptHgvs(normalizedVariant, transcript, geneId);
    }

    /**
     * Generates cdna HGVS names from an SNV.
     * @param transcript Transcript object that will be used as a reference
     */
    private List<String> calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {

        String mutationType = ">";

        // Populate coordinates.
        // Use cDNA coordinates.
        buildingComponents.setKind(isCoding(transcript) ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        buildingComponents.setCdnaStart(genomicToCdnaCoord(transcript, variant.getStart()));
        buildingComponents.setCdnaEnd(buildingComponents.getCdnaStart());

        // Populate prefix.
        buildingComponents.setTranscriptId(transcript.getId());
        buildingComponents.setGeneId(geneId);

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
        buildingComponents.setMutationType(mutationType);
        buildingComponents.setReferenceStart(reference);
        buildingComponents.setAlternate(alternate);

        return Collections.singletonList(formatTranscriptString(buildingComponents));
//        return Collections.singletonList(buildingComponents.format());
    }

    /**
     * Generate HGVS cDNA coordinates string.
     */
    @Override
    protected String formatCdnaCoords(BuildingComponents buildingComponents) {
        return buildingComponents.getCdnaStart().toString();
    }

    /**
     * Generate HGVS DNA allele.
     * @return
     */
    @Override
    protected String formatDnaAllele(BuildingComponents buildingComponents) {
        return buildingComponents.getReferenceStart() + '>' + buildingComponents.getAlternate();
    }

}
