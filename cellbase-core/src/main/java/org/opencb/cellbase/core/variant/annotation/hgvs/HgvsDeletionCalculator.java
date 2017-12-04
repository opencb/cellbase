package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        String proteinHgvs = calculateProteinHgvs(normalizedVariant, transcript);

        if (proteinHgvs == null) {
            return Collections.singletonList(transcriptHgvs);
        } else {
            return Arrays.asList(transcriptHgvs, proteinHgvs);
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
            buildingComponents.setMutationType(DEL);
            buildingComponents.setProteinId(transcript.getProteinID());
            // We are storing aa position, ref aa and alt aa within a Variant object. This is just a technical issue to
            // be able to re-use methods and available objects
            Variant proteinVariant = createProteinVariant(variant, transcript);
            if (proteinVariant != null) {
                // startOffset must point to the position right before the actual variant start, since that's the position that
                // will be looked at for coincidences within the variant reference sequence. Likewise, endOffset must point tho
                // the position right after the actual variant end.
//                justify(proteinVariant, proteinVariant.getStart() - 1 - 1, // -1 in order to convert to base 0
//                        proteinVariant.getEnd() - 1 - 1, proteinVariant.getReference(),
//                        transcript.getProteinSequence(), POSITIVE);
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
        return null;
        
    }

    private void proteinHgvsNormalize(Variant proteinVariant, String proteinSequence) {
        // It's not worth calling the justify method of the super class, too complicated and the code is relatively
        // simple
        // Justify
        // TODO: assuming this is justificaxtion sense; might need adjusting
        StringBuilder stringBuilder = new StringBuilder(proteinVariant.getReference());
        int end = proteinVariant.getEnd() - 1;
        while ((end + 1) < proteinSequence.length() && proteinSequence.charAt(end + 1) == stringBuilder.charAt(0)) {
            stringBuilder.deleteCharAt(0);
            stringBuilder.append(proteinSequence.charAt(end + 1));
            proteinVariant.setStart(proteinVariant.getStart() + 1);
            proteinVariant.setEnd(proteinVariant.getEnd() + 1);
            end = proteinVariant.getEnd();
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
                    .append(buildingComponents.getStart())
                    .append(UNDERSCORE)
                    .append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getEnd())
                    .append(buildingComponents.getMutationType());
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

        proteinVariant.setStart(getAminoAcidPosition(buildingComponents.getCdnaStart().getReferencePosition()));
        proteinVariant.setEnd(getAminoAcidPosition(buildingComponents.getCdnaEnd().getReferencePosition()));

        // We expect buildingComponents.getStart() and buildingComponents.getEnd() to be within the sequence boundaries.
        // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
        // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
        if (proteinVariant.getStart() > 0 && proteinVariant.getEnd() < transcript.getProteinSequence().length()) {
            proteinVariant.setAlternate(EMPTY_STRING);
            proteinVariant.setReference(transcript.getProteinSequence().substring(proteinVariant.getStart() - 1,
                    proteinVariant.getEnd())); // don't rest -1 since it's base 0 and substring does not include this nt

            return proteinVariant;
        }
        logger.warn("Protein start/end out of protein seq boundaries: {}, {}-{}, prot length: {}. This should, in principle,"
                        + " not happen and protein HGVS will not be returned. Could be expected for "
                        +"unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                buildingComponents.getProteinId(), proteinVariant.getStart(), proteinVariant.getEnd(),
                transcript.getProteinSequence().length());

        return null;
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

        buildingComponents.setMutationType(mutationType);
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
        return buildingComponents.getCdnaStart().toString() + "_"
                + buildingComponents.getCdnaEnd().toString();
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
