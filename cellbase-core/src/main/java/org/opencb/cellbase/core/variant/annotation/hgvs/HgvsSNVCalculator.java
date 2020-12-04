package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils.COMPLEMENTARY_NT;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsSNVCalculator extends HgvsCalculator {

    private static final String STOP = "Stop";
    private static final String STOP_GAINED_TAG = "Ter";
    private static final String SYNONYMOUS_VARIANT_SUFFIX = "=";
    private static final String START_AA = "Met";
    private static final String START_LOSS_HGVS_STRING = "1?";

    public HgvsSNVCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        buildingComponents = new BuildingComponents();
        Variant normalizedVariant = normalize(variant, normalize);
        String transcriptHgvs = calculateTranscriptHgvs(normalizedVariant, transcript, geneId);

        if (transcriptHgvs != null) {
            String proteinHgvs = calculateProteinHgvs(normalizedVariant, transcript);
            if (proteinHgvs == null) {
                return Collections.singletonList(transcriptHgvs);
            } else {
                return Arrays.asList(transcriptHgvs, proteinHgvs);
            }
        } else {
            return Collections.emptyList();
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
                String referenceStart = VariantAnnotationUtils.TO_LONG_AA.get(proteinVariant.getReference());
                String alternate = VariantAnnotationUtils.TO_LONG_AA.get(proteinVariant.getAlternate());
                // Could happen if unexpected characters are found in the protein seq, for example ENST00000525566
                if (referenceStart != null && alternate != null) {
                    buildingComponents.setStart(proteinVariant.getStart());
                    buildingComponents.setEnd(proteinVariant.getEnd());
                    buildingComponents.setReferenceStart(referenceStart);
                    buildingComponents.setAlternate(alternate);
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
    protected String formatProteinString(BuildingComponents buildingComponents) {
        StringBuilder stringBuilder = (new StringBuilder(buildingComponents.getProteinId()))
                .append(PROTEIN_CHAR)
                .append(buildingComponents.getReferenceStart());

        // start loss
        if (START_AA.equals(buildingComponents.getReferenceStart()) && buildingComponents.getStart() == 1) {
            // "Do not use descriptions like p.Met1Thr, this is for sure not the consequence of the effect on protein translation"
            stringBuilder.append(START_LOSS_HGVS_STRING);
        } else {
            stringBuilder.append(buildingComponents.getStart());
            // Synonymous variant
            if (buildingComponents.getReferenceStart().equals(buildingComponents.getAlternate())) {
                stringBuilder.append(SYNONYMOUS_VARIANT_SUFFIX);
                // Stop gained variant
            } else if (STOP.equals(buildingComponents.getAlternate())) {
                stringBuilder.append(STOP_GAINED_TAG);
                // missense_variant
            } else {
                stringBuilder.append(buildingComponents.getAlternate());
            }
        }
        return stringBuilder.toString();
    }

    private Variant createProteinVariant(Variant variant, Transcript transcript) {
        Variant proteinVariant = new Variant();
        proteinVariant.setStart(getAminoAcidPosition(genomicToCdnaCoord(transcript, variant.getStart())
                .getReferencePosition(), transcript));
        proteinVariant.setEnd(proteinVariant.getStart());

        // We expect buildingComponents.getStart() to be within the sequence boundaries.
        // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
        // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
        if (proteinVariant.getStart() > 0 && transcript.getProteinSequence() != null
                && proteinVariant.getStart() <= transcript.getProteinSequence().length()) {
            proteinVariant.setReference(String.valueOf(transcript.getProteinSequence().charAt(proteinVariant.getStart() - 1)));
            String predictedAa = getPredictedAa(variant, transcript);
            if (StringUtils.isNotBlank(predictedAa)) {
                // Start codon is not translated and there shall be no protein hgvs
                // if in UTR, okay if protein sequence. unlikely to get a stop_codon_gain in UTR
                //if (!METIONINE.equals(predictedAa)) {
                proteinVariant.setAlternate(predictedAa);
                return proteinVariant;
                //}
            } else {
                logger.warn("Could not predict new Aa. This should, in principle, not happen and protein HGVS "
                                + "will not be returned. Please, check variant {}, transcript {}, protein {}",
                        variant.toString(), transcript.getId(), transcript.getProteinID());
            }
        } else {
            int sequenceLength = 0;
            if (transcript.getProteinSequence() != null) {
                sequenceLength = transcript.getProteinSequence().length();
            }
            logger.warn("Protein start/end out of protein seq boundaries: {}, {}, prot length: {}. This should, in principle,"
                            + " not happen and protein HGVS will not be returned. Could be expected for "
                            + "unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                    buildingComponents.getProteinId(), proteinVariant.getStart(), sequenceLength);
        }

        return null;

    }

    private String getPredictedAa(Variant variant, Transcript transcript) {
        // There's no need to differentiate between + and - strands since the Transcript object contains the transcript
        // sequence already complementary-reversed if necessary.
        int cdsPosition = genomicToCdnaCoord(transcript, variant.getStart()).getReferencePosition();

        // Only use the "unconfirmedStart" data to determine the variant phase and the codon it belongs to.
        // getPhaseShift adjusts the phase taking into account the "unconfirmedStart" status.
        int variantPhaseShift = getPhaseShift(cdsPosition, transcript);

        // NOTE: unconfirmedStart status not taken into account to calculate the cdnaVariantStart. This was decided as
        // otherwise cdnaVariantStart would not correlate (would be shifted) regarding the corresponding position within
        // the corresponding transcript.getSequence String, as this String does not include the "unknown" nts at the
        // beginning of the transcript for unconfirmedStart transcripts.
        int cdnaVariantStart = transcript.getCdnaCodingStart() + cdsPosition - 1;

        // use the variantPhaseShift (calculated taking into account unconfirmedStart status) to determine the start
        // coordinate of the codon containing the variant
        int modifiedCodonStart = cdnaVariantStart - variantPhaseShift;

        String transcriptSequence = transcript.getcDnaSequence();
        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            // -1 and +2 because of base 0 String indexing
            char[] modifiedCodonArray = transcriptSequence
                    .substring(modifiedCodonStart - 1, modifiedCodonStart + 2)
                    .toCharArray();            // First modified position within the codon corresponds to the phase shift
            int modifiedCodonPosition = variantPhaseShift;

            char substitutingNt = 0;
            if (POSITIVE.equals(transcript.getStrand())) {
                substitutingNt = variant.getAlternate().charAt(0);
            } else if (COMPLEMENTARY_NT.containsKey(variant.getAlternate().charAt(0))) {
                substitutingNt = COMPLEMENTARY_NT.get(variant.getAlternate().charAt(0));
            }

            if (substitutingNt != 0) {
                modifiedCodonArray[modifiedCodonPosition] = substitutingNt;
                String aa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), String.valueOf(modifiedCodonArray));
                if (StringUtils.isNotBlank(aa)) {
                    return VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aa);
                }
            } else {
                logger.warn("Invalid alternate nucleotide symbol found for variant {}. Skipping protein HGVS "
                        + "calculation.", variant.toString());
            }
        }

        return null;
    }

    /**
     * Generates cdna HGVS names from an SNV.
     * @param transcript Transcript object that will be used as a reference
     */
    private String calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {

//        String mutationType = ">";

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
