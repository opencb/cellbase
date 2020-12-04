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
public class HgvsDeletionCalculator extends HgvsCalculator {

    private static final String DEL = "del";
    private static final String FRAMESHIFT_TAG = "fs";
    private static final String EMPTY_STRING = "";


    public HgvsDeletionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    @Override
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

    /**
     * Calculates protein HGVS. Must always be called after calling calculateTranscriptHgvs, since the latter will
     * normalize and calculate cdna coords that will be used to calculate the protein HGVS.
     * @param variant Variant object containing genomic coordinates and variation for which protein HGVS wants to be
     *                calculated
     * @param transcript Transcript object containing the info for the transcript codifying the protein for which the
     *                  HGVS will be calculated
     * @return
     */
    private String calculateProteinHgvs(Variant variant, Transcript transcript) {
        // Check if protein HGVS can be calculated
        if (isCoding(transcript) && onlySpansCodingSequence(variant, transcript)) {
            Variant normalizedVariant = new Variant();
            transcriptHgvsNormalize(variant, transcript, normalizedVariant);
            buildingComponents.setMutationType(BuildingComponents.MutationType.DELETION);
            buildingComponents.setProteinId(transcript.getProteinID());
            // We are storing aa position, ref aa and alt aa within a Variant object. This is just a technical issue to
            // be able to re-use methods and available objects
            Variant proteinVariant = createProteinVariant(normalizedVariant, transcript);
            if (proteinVariant != null && transcript.getProteinSequence() != null) {
                String referenceStartShortSymbol = String.valueOf(transcript.getProteinSequence()
                        .charAt(proteinVariant.getEnd() - 1));
                String referenceEndShortSymbol = String.valueOf(transcript
                        .getProteinSequence()
                        .charAt(proteinVariant.getStart() - 1));

                // Do not generate protein HGVS if insertion affects an unconfirmed start, i.e. overlaps with an "X"
                // symbol in the protein sequence
                if (VariantAnnotationUtils.TO_LONG_AA.containsKey(referenceStartShortSymbol)
                        && VariantAnnotationUtils.TO_LONG_AA.containsKey(referenceEndShortSymbol)) {
                    proteinHgvsNormalize(proteinVariant, transcript.getProteinSequence());
                    buildingComponents.setStart(proteinVariant.getStart());
                    buildingComponents.setEnd(proteinVariant.getEnd());
                    buildingComponents.setReferenceStart(VariantAnnotationUtils
                            .buildUpperLowerCaseString(VariantAnnotationUtils
                                    .TO_LONG_AA.get(String.valueOf(transcript.getProteinSequence()
                                            .charAt(proteinVariant.getStart() - 1)))));
                    buildingComponents.setReferenceEnd(VariantAnnotationUtils
                            .buildUpperLowerCaseString(VariantAnnotationUtils
                                    .TO_LONG_AA.get(String.valueOf(transcript.getProteinSequence()
                                            .charAt(proteinVariant.getEnd() - 1)))));
                    // Check frameshift/inframe
                    if (variant.getReference().length() % 3 == 0) {
                        buildingComponents.setKind(BuildingComponents.Kind.INFRAME);
                    } else {
                        buildingComponents.setKind(BuildingComponents.Kind.FRAMESHIFT);
                    }

                    return formatProteinString(buildingComponents);
                }
            }
        }
        return null;
    }

    private void proteinHgvsNormalize(Variant proteinVariant, String proteinSequence) {
        // It's not worth calling the justify method of the super class, too complicated and the code is relatively
        // simple
        // Justify
        // TODO: assuming this is justificaxtion sense; might need adjusting
        StringBuilder stringBuilder = new StringBuilder(proteinVariant.getReference());
        int end = proteinVariant.getEnd() - 1; // base 0 for string indexing
        while ((end + 1) < proteinSequence.length() && proteinSequence.charAt(end + 1) == stringBuilder.charAt(0)) {
            stringBuilder.deleteCharAt(0);
            stringBuilder.append(proteinSequence.charAt(end + 1));
            proteinVariant.setStart(proteinVariant.getStart() + 1);
            proteinVariant.setEnd(proteinVariant.getEnd() + 1);
            end = proteinVariant.getEnd() - 1; // base 0 for string indexing
        }
        proteinVariant.setReference(stringBuilder.toString());

    }

        /**
         * Generate a protein HGVS string.
         * @param buildingComponents BuildingComponents object containing all elements needed to build the hgvs string
         * @return String containing an HGVS formatted variant representation
         */
    protected String formatProteinString(BuildingComponents buildingComponents) {

        StringBuilder allele = new StringBuilder();
        allele.append(buildingComponents.getProteinId());  // if use_prefix else ''

        if (buildingComponents.getKind().equals(BuildingComponents.Kind.INFRAME)) {
            allele.append(PROTEIN_CHAR)
                    .append(buildingComponents.getReferenceStart())
                    .append(buildingComponents.getStart());
            if (buildingComponents.getStart() != buildingComponents.getEnd()) {
                allele.append(UNDERSCORE)
                    .append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getEnd());
            }
            allele.append(buildingComponents.getMutationType());
        } else if (buildingComponents.getKind().equals(BuildingComponents.Kind.FRAMESHIFT)) {
            allele.append(PROTEIN_CHAR)
                    .append(buildingComponents.getReferenceStart())
                    .append(buildingComponents.getStart())
                    .append(FRAMESHIFT_TAG);
        }

        return allele.toString();

    }


    private Variant createProteinVariant(Variant variant, Transcript transcript) {
        Variant proteinVariant = new Variant();

        //int cdnaCodingStart = getCdnaCodingStart(transcript);

//        proteinVariant.setStart(getAminoAcidPosition(buildingComponents.getCdnaStart().getReferencePosition()));
//        proteinVariant.setEnd(getAminoAcidPosition(buildingComponents.getCdnaEnd().getReferencePosition()));

        int cdsPosition1 = genomicToCdnaCoord(transcript, variant.getStart()).getReferencePosition();
        int cdsPosition2 = genomicToCdnaCoord(transcript, variant.getEnd()).getReferencePosition();
        boolean checkNewShiftedPosition;
        do {
            checkNewShiftedPosition = false;
            // NOTE: protein coordinates set at the protein variant object are NOT the ones from buildingComponent, i.e.
            // before hgvs normalization. HOWEVER, within getGeneratedAa the buildingComponents ones will be used which
            // already reflect hgvs normalization
            int aminoAcidPosition1 = getAminoAcidPosition(cdsPosition1, transcript);
            int aminoAcidPosition2 = getAminoAcidPosition(cdsPosition2, transcript);
            int cdsStart;
            int cdsEnd;
            if (POSITIVE.equals(transcript.getStrand())) {
                cdsStart = cdsPosition1;
                cdsEnd = cdsPosition2;
                proteinVariant.setStart(aminoAcidPosition1);
                proteinVariant.setEnd(aminoAcidPosition2);
            } else {
                cdsStart = cdsPosition2;
                cdsEnd = cdsPosition1;
                proteinVariant.setStart(aminoAcidPosition2);
                proteinVariant.setEnd(aminoAcidPosition1);
            }

            String generatedAa = null;
            // We expect buildingComponents.getStart() and buildingComponents.getEnd() to be within the sequence boundaries.
            // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
            // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
            if (proteinVariant.getStart() > 0 && transcript.getProteinSequence() != null
                    && proteinVariant.getEnd() <= transcript.getProteinSequence().length()) {
                proteinVariant.setAlternate(EMPTY_STRING);
                // start and end fall within the same codon - affect the same aminoacid
                if (!proteinVariant.getStart().equals(proteinVariant.getEnd())) {
                    generatedAa = getGeneratedAa(variant.getChromosome(), cdsStart, cdsEnd, transcript);

                    // Might be null for deletions affecting start of transcript in unconfirmed start transcripts
                    if (generatedAa != null) {
                        // generatedAa == 0 if whole codons (3 nts) are removed from the start and end of the variant
                        if (!generatedAa.isEmpty()) {
                            // Generated aa after pasting the two ends of remaining sequence coincides with the aa at start position
                            // on the original seq
                            if (generatedAa.charAt(0)
                                    == transcript.getProteinSequence().charAt(proteinVariant.getStart() - 1)) {
                                // Skip the first aa since coincides with the generated one
                                //proteinVariant.setStart(proteinVariant.getStart() + 1);
                                cdsPosition1++;
                                cdsPosition2++;
                                checkNewShiftedPosition = true;
                            } else {
                                proteinVariant.setReference(transcript.getProteinSequence().substring(proteinVariant.getStart() - 1,
                                        proteinVariant.getEnd())); // don't rest -1 since it's base 0 and substring does not include this nt
                            }
                        } else {
                            proteinVariant.setReference(transcript.getProteinSequence().substring(proteinVariant.getStart() - 1,
                                    proteinVariant.getEnd())); // don't rest -1 since it's base 0 and substring does not
                            // include this nt
                        }
                    // Avoiding logging a warning since it's been observed it gets too verbose with normal samples
                    } else {
                        return null;
                    }
                } else {
                    proteinVariant.setReference(String.valueOf(transcript
                            .getProteinSequence()
                            .charAt(proteinVariant.getStart() - 1))); // don't rest -1 since it's base 0 and substring
                                                                      // does not include this nt
                }
            } else {
                logger.warn("Protein start/end out of protein seq boundaries: {}, {}-{}, prot length: {}. This should, in principle,"
                                + " not happen and protein HGVS will not be returned. Could be expected for "
                                + "unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                        buildingComponents.getProteinId(), proteinVariant.getStart(), proteinVariant.getEnd(),
                        transcript.getProteinSequence().length());

                return null;
            }
        } while (checkNewShiftedPosition);

        return proteinVariant;

    }

    private String getGeneratedAa(String chromosome, int cdsStart, int cdsEnd, Transcript transcript) {

        // There's no need to differentiate between + and - strands since the Transcript object contains the transcript
        // sequence already complementary-reversed if necessary.

        // Only use the "unconfirmedStart" data to determine the variant phase and the codon it belongs to.
        // getPhaseShift adjusts the phase taking into account the "unconfirmedStart" status.
        int variantPhaseShift = getPhaseShift(cdsStart, transcript);

        // NOTE: unconfirmedStart status not taken into account to calculate the cdnaVariantStart/End. This was decided
        // as otherwise cdnaVariantStart/End would not correlate (would be shifted) regarding the corresponding position
        // within the corresponding transcript.getSequence String, as this String does not include the "unknown" nts at
        // the beginning of the transcript for unconfirmedStart transcripts.
        int cdnaVariantStart = transcript.getCdnaCodingStart() + cdsStart - 1;
        int cdnaVariantEnd = transcript.getCdnaCodingStart() + cdsEnd - 1;

        // use the variantPhaseShift (calculated taking into account unconfirmedStart status) to determine the start
        // coordinate of the codon containing the variant
        int modifiedCodonStart = cdnaVariantStart - variantPhaseShift;

        String transcriptSequence = transcript.getcDnaSequence();
        String aa = null;
        // Out of boundaries can happen for unconfirmed start transcripts, for example
        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            char[] referenceCodonArray = transcriptSequence
                    .substring(modifiedCodonStart - 1, modifiedCodonStart + 2)
                    .toCharArray();
            int i = cdnaVariantEnd;  // Position (0 based index) in transcriptSequence of the first nt after the deletion
            int codonPosition;
            // If we get here, cdnaVariantStart and cdnaVariantEnd != -1; this is an assumption that was made just before
            // calling this method
            for (codonPosition = variantPhaseShift; codonPosition < 3; codonPosition++) {
                char substitutingNt;
                // Means we've reached the beginning of the transcript, i.e. transcript.start
                if (i >= transcriptSequence.length()) {
                    // Adding +1 to i since it's originally 0-based and want to make it 1-based for the function call
                    substitutingNt = getNextNt(chromosome, transcript, i + 1);
                } else {
                    // Paste reference nts after deletion in the corresponding codon position
                    substitutingNt = transcriptSequence.charAt(i);
                }
                referenceCodonArray[codonPosition] = substitutingNt;
                i++;
            }

            aa = VariantAnnotationUtils.TO_ABBREVIATED_AA.get(VariantAnnotationUtils
                    .getAminoacid(MT.equals(chromosome), String.valueOf(referenceCodonArray)));

            if (StringUtils.isNotBlank(aa)) {
                return aa;
            } else {
                return EMPTY_STRING;
            }
        } else {
            return null;
        }

    }

    /**
     *
     * @param chromosome
     * @param transcript
     * @param virtualCdnaPosition: named "virtual" since it is expected to be beyond the transcript sequence end limit.
     * @return
     */
    private char getNextNt(String chromosome, Transcript transcript, int virtualCdnaPosition) {
        char substitutingNt;

        int genomicCoordinate;
        // Need to differentiate between + and - since need to calculate the next genomic position from the cdna
        // position
        if (POSITIVE.equals(transcript.getStrand())) {
            genomicCoordinate = transcript.getEnd() + (virtualCdnaPosition - transcript.getcDnaSequence().length());
        } else {
            genomicCoordinate = transcript.getStart() - (virtualCdnaPosition - transcript.getcDnaSequence().length());
        }

        Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), chromosome
                + ":" + genomicCoordinate
                + "-" + (genomicCoordinate + 1));
        substitutingNt = genomeDBAdaptor
                .getGenomicSequence(query, new QueryOptions()).getResult().get(0).getSequence().charAt(0);

        if (POSITIVE.equals(transcript.getStrand())) {
            return substitutingNt;
        } else {
            return VariantAnnotationUtils.COMPLEMENTARY_NT.get(substitutingNt);
        }
    }

    private String calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = transcriptHgvsNormalize(variant, transcript, normalizedVariant);

        // Use cDNA coordinates.
        buildingComponents.setKind(isCoding(transcript) ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate
//        setRangeCoordsAndAlleles(normalizedVariant, transcript, hgvsStringBuilder);
        setRangeCoordsAndAlleles(normalizedVariant.getStart(), normalizedVariant.getEnd(),
                normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);

        buildingComponents.setMutationType(BuildingComponents.MutationType.DELETION);
        buildingComponents.setTranscriptId(transcript.getId());
        buildingComponents.setGeneId(geneId);

        return formatTranscriptString(buildingComponents);
//        return hgvsStringBuildingComponents.format();

    }

    /**
     * Generate HGVS cDNA coordinates string.
     */
    @Override
    protected String formatCdnaCoords(BuildingComponents buildingComponents) {
        if (buildingComponents.getCdnaStart() != null
                && buildingComponents.getCdnaStart().equals(buildingComponents.getCdnaEnd())) {
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
        // Delete
        // example:
        // 1000_1003d elATG
        return buildingComponents.getMutationType() + buildingComponents.getReferenceStart();
    }

    private String transcriptHgvsNormalize(Variant variant, Transcript transcript, Variant normalizedVariant) {
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

        return DEL;
    }



}
