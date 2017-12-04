package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsSNVCalculator extends HgvsCalculator {
    private static final String METIONINE = "M";
    private static final String STOP = "Stop";
    private static final String STOP_GAINED_TAG = "Ter";

    public HgvsSNVCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

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
            buildingComponents.setProteinId(transcript.getProteinID());
            // We are storing aa position, ref aa and alt aa within a Variant object. This is just a technical issue to
            // be able to re-use methods and available objects
            Variant proteinVariant = createProteinVariant(variant, transcript);
            if (proteinVariant != null) {
                buildingComponents.setStart(proteinVariant.getStart());
                buildingComponents.setEnd(proteinVariant.getEnd());
                buildingComponents.setReferenceStart(VariantAnnotationUtils.TO_LONG_AA.get(proteinVariant.getReference()));
                buildingComponents.setAlternate(VariantAnnotationUtils.TO_LONG_AA.get(proteinVariant.getAlternate()));

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
        StringBuilder stringBuilder = (new StringBuilder(buildingComponents.getTranscriptId()))
                .append(PROTEIN_CHAR)
                .append(buildingComponents.getReferenceStart())
                .append(buildingComponents.getStart());
        // Stop gained variant
        if (STOP.equals(buildingComponents.getAlternate())) {
            stringBuilder.append(STOP_GAINED_TAG);
        // missense_variant
        } else {
            stringBuilder.append(buildingComponents.getAlternate());
        }
        return stringBuilder.toString();
    }

    private Variant createProteinVariant(Variant variant, Transcript transcript) {
        Variant proteinVariant = new Variant();

//        int cdnaCodingStart = getCdnaCodingStart(transcript);
        proteinVariant.setStart(getAminoAcidPosition(buildingComponents.getCdnaStart().getOffset()));
        proteinVariant.setEnd(proteinVariant.getStart());

        // Only non-synonymous variants shall have protein hgvs
        if (!proteinVariant.getReference().equals(proteinVariant.getAlternate())) {
            // We expect buildingComponents.getStart() to be within the sequence boundaries.
            // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
            // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
            if (proteinVariant.getStart() > 0 && proteinVariant.getStart() < transcript.getProteinSequence().length()) {
                proteinVariant.setReference(String.valueOf(transcript.getProteinSequence().charAt(proteinVariant.getStart() - 1)));
                String predictedAa = getPredictedAa(variant, transcript);
                if (StringUtils.isNotBlank(predictedAa)) {
                    // Start codon is not translated and there shall be no protein hgvs
                    if (!METIONINE.equals(predictedAa)) {
                        proteinVariant.setAlternate(predictedAa);
                        return proteinVariant;
                    }
                } else {
                    logger.warn("Could not predict new Aa. This should, in principle, not happen and protein HGVS "
                                    + "will not be returned. Please, check variant {}, transcript {}, protein {}",
                            variant.toString(), transcript.getId(), transcript.getProteinID());
                }
            }
            logger.warn("Protein start/end out of protein seq boundaries: {}, {}, prot length: {}. This should, in principle,"
                            + " not happen and protein HGVS will not be returned. Could be expected for "
                            + "unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                    buildingComponents.getProteinId(), proteinVariant.getStart(), transcript.getProteinSequence().length());
        }

        return null;

    }

    private String getPredictedAa(Variant variant, Transcript transcript) {
        int cdsPosition = buildingComponents.getCdnaStart().getOffset();
        int cdnaCodingStart = getCdnaCodingStart(transcript);
        String transcriptSequence = transcript.getcDnaSequence();

        // What buildingComponents.cdnaStart.offset really stores is the cdsStart
        int cdnaVariantStart = cdsPosition + cdnaCodingStart - 1; // TODO: might need adjusting +-1

        // Sum 1 to cdnaVariantStart because of the peculiarities of insertion coordinates:
        // cdnaVariantStart coincides with the vcf position, the actual substituted nt is the one on the right
        Integer variantPhaseShift = (cdnaVariantStart + 1 - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaVariantStart + 1 - variantPhaseShift;
        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            // -1 and +2 because of base 0 String indexing
            String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
            char[] modifiedCodonArray = referenceCodon.toCharArray();
            // First modified position within the codon corresponds to the phase shift
            int modifiedCodonPosition = variantPhaseShift;
            modifiedCodonArray[modifiedCodonPosition] = variant.getAlternate().charAt(0);
            String aa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), String.valueOf(modifiedCodonArray));
            if (StringUtils.isNotBlank(aa)) {
                return VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aa);
            }
        }

        return null;
    }

    /**
     * Generates cdna HGVS names from an SNV.
     * @param transcript Transcript object that will be used as a reference
     */
    private String calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {

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

        return formatTranscriptString(buildingComponents);
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
