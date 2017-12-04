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
public class HgvsInsertionCalculator extends HgvsCalculator {

    private static final String INS = "ins";
    private static final String DUP = "dup";
    private static final String MITOCHONDRIAL_CHROMOSOME_STRING = "MT";
    private static final char UNKNOWN_AA = 'X';
    private static final String STOP_STRING = "STOP";
    private static final String FRAMESHIFT_SUFFIX = "fs";

    public HgvsInsertionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
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

    private String calculateProteinHgvs(Variant variant, Transcript transcript) {
        // Check if protein HGVS can be calculated
        if (isCoding(transcript) && onlySpansCodingSequence(variant, transcript)) {
            buildingComponents.setProteinId(transcript.getProteinID());
            // We are storing aa position, ref aa and alt aa within a Variant object. This is just a technical issue to
            // be able to re-use methods and available objects
            Variant proteinVariant = createProteinVariant(variant, transcript);
            if (proteinVariant != null) {
                String mutationType = proteinHgvsNormalize(proteinVariant, transcript.getProteinSequence());
                buildingComponents.setStart(proteinVariant.getStart());
                buildingComponents.setEnd(proteinVariant.getEnd());
                buildingComponents.setReferenceStart(String.valueOf(transcript.getProteinSequence()
                        .charAt(proteinVariant.getStart() - 1)));
                buildingComponents.setReferenceEnd(String.valueOf(transcript.getProteinSequence()
                        .charAt(proteinVariant.getStart())));
                buildingComponents.setAlternate(proteinVariant.getAlternate());
                buildingComponents.setMutationType(mutationType);
                buildingComponents.setKind(variant.getAlternate().length() % 3 == 0 ? BuildingComponents.Kind.INFRAME
                        : BuildingComponents.Kind.FRAMESHIFT);

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
                .append(PROTEIN_CHAR);

        if (DUP.equals(buildingComponents.getMutationType())) {
            if (buildingComponents.getAlternate().length() == 1) {
                // assuming end = start - 1
                stringBuilder.append(buildingComponents.getEnd());
            } else {
                // assuming end = start - 1
                stringBuilder.append(buildingComponents.getStart() - buildingComponents.getAlternate().length())
                        .append(UNDERSCORE)
                        .append(buildingComponents.getEnd());
            }
            stringBuilder.append(buildingComponents.getAlternate())
                    .append(DUP);

        } else if (BuildingComponents.Kind.FRAMESHIFT.equals(buildingComponents.getKind())){
            // Appends aa name properly formated; first letter uppercase, two last letters lowercase e.g. Arg
            stringBuilder.append(VariantAnnotationUtils.TO_LONG_AA.get(buildingComponents.getAlternate().charAt(0)))
                    .append(buildingComponents.getStart())
                    .append(FRAMESHIFT_SUFFIX);
        } else {
            // assuming end = start - 1
            stringBuilder.append(buildingComponents.getReferenceStart())
                    .append(buildingComponents.getEnd())
                    .append(UNDERSCORE)
                    .append(buildingComponents.getReferenceEnd())
                    .append(buildingComponents.getStart())
                    .append(INS)
                    .append(formatAaSequence(buildingComponents.getAlternate()));
        }

        return stringBuilder.toString();

    }

    private String formatAaSequence(String alternate) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < alternate.length(); i++) {
            stringBuilder.append(VariantAnnotationUtils.buildUpperLowerCaseString(VariantAnnotationUtils.TO_LONG_AA.get(alternate.charAt(i))));
        }
        return stringBuilder.toString();
    }

    private String proteinHgvsNormalize(Variant proteinVariant, String proteinSequence) {
        // It's not worth calling the justify method of the super class, too complicated and the code is relatively
        // simple
        // Justify
        // TODO: assuming this is justificaxtion sense; might need adjusting
        StringBuilder stringBuilder = new StringBuilder(proteinVariant.getAlternate());
        int end = proteinVariant.getEnd();
        while ((end + 1) < proteinSequence.length() && proteinSequence.charAt(end + 1) == stringBuilder.charAt(0)) {
            stringBuilder.deleteCharAt(0);
            stringBuilder.append(proteinSequence.charAt(end + 1));
            proteinVariant.setStart(proteinVariant.getStart() + 1);
            proteinVariant.setEnd(proteinVariant.getEnd() + 1);
            end = proteinVariant.getEnd();
        }
        proteinVariant.setAlternate(stringBuilder.toString());

        // Check duplication
        // TODO: Assuming end = start - 1; might need adjusting
        String previousSequence = proteinSequence.substring(Math.max(0, proteinVariant.getStart()
                - proteinVariant.getAlternate().length()), proteinVariant.getStart());
        // normalized one in order to take into
        // account potential
        // normalization/lef-right alignment
        // differences
        if (previousSequence.equals(proteinVariant.getAlternate())) {
            return DUP;
        } else {
            // TODO: Assuming end = start - 1; might need adjusting
            String nextSequence = proteinSequence.substring(proteinVariant.getStart(),
                    Math.min(proteinSequence.length(), proteinVariant.getEnd() + proteinSequence.length()));
            // normalized one in order to take into
            // account potential
            // normalization/lef-right alignment
            // differences
            if (nextSequence.equals(proteinVariant.getAlternate())) {
                return DUP;
            }
        }
        return INS;

    }

    private Variant createProteinVariant(Variant variant, Transcript transcript) {
        Variant proteinVariant = new Variant();

//        int cdnaCodingStart = getCdnaCodingStart(transcript);
        proteinVariant.setStart(getAminoAcidPosition(buildingComponents.getCdnaStart().getOffset()));
        proteinVariant.setEnd(getAminoAcidPosition(buildingComponents.getCdnaEnd().getOffset()));

        // We expect buildingComponents.getStart() and buildingComponents.getEnd() to be within the sequence boundaries.
        // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
        // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
        if (proteinVariant.getStart() > 0 && proteinVariant.getEnd() < transcript.getProteinSequence().length()) {
            String predictedProteinSequence = getPredictedProteinSequence(variant, transcript);
            if (StringUtils.isNotBlank(predictedProteinSequence)) {
                proteinVariant.setAlternate(predictedProteinSequence);
                // If insertion affects two different aa, paste both of them in the reference
                if (!proteinVariant.getStart().equals(proteinVariant.getEnd())) {
                    proteinVariant.setReference((new StringBuilder(transcript.getProteinSequence().charAt(proteinVariant.getStart())))
                            .append(transcript.getProteinSequence().charAt(proteinVariant.getEnd())).toString());
                    // If it just affects one aa, paste just that aa in the reference
                } else {
                    proteinVariant.setReference(String.valueOf(transcript.getProteinSequence().charAt(proteinVariant.getStart())));
                }

                return proteinVariant;
            } else {
                logger.warn("Could not predict protein sequence. This should, in principle, not happen and protein HGVS "
                                + "will not be returned. Please, check variant {}, transcript {}, protein {}",
                        variant.toString(), transcript.getId(), transcript.getProteinID());
            }
        }
        logger.warn("Protein start/end out of protein seq boundaries: {}, {}-{}, prot length: {}. This should, in principle,"
                        + " not happen and protein HGVS will not be returned. Could be expected for "
                        +"unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                buildingComponents.getProteinId(), proteinVariant.getStart(), proteinVariant.getEnd(),
                transcript.getProteinSequence().length());

        return null;
    }

    private String getPredictedProteinSequence(Variant variant, Transcript transcript) {
        int cdsPosition = buildingComponents.getCdnaStart().getReferencePosition();
        int cdnaCodingStart = getCdnaCodingStart(transcript);
        String transcriptSequence = transcript.getcDnaSequence();

        // What buildingComponents.cdnaStart.offset really stores is the cdsStart
        int cdnaVariantStart = cdsPosition + cdnaCodingStart - 1; // TODO: might need adjusting +-1

        // Sum 1 to cdnaVariantStart because of the peculiarities of insertion coordinates:
        // cdnaVariantStart coincides with the vcf position, the actual substituted nt is the one on the right
        Integer variantPhaseShift = (cdnaVariantStart + 1 - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaVariantStart + 1 - variantPhaseShift;
        if (modifiedCodonStart > 0 && (modifiedCodonStart + 2) <= transcriptSequence.length()) {
            String alternate = variant.getAlternate();
            // -1 and +2 because of base 0 String indexing
            String referenceCodon = transcriptSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
            char[] modifiedCodonArray = referenceCodon.toCharArray();

            // First modified position within the codon corresponds to the phase shift
            int modifiedCodonPosition = variantPhaseShift;
            StringBuilder predictedProteinSequence = new StringBuilder();
            for (int alternatePosition = 0; alternatePosition < alternate.length(); alternatePosition++) {
                modifiedCodonArray[modifiedCodonPosition] = alternate.toCharArray()[alternatePosition];
                if (modifiedCodonPosition == 2) {
                    // False means that no AA was added; this can only happen if a STOP codon is found
                    if (!addNewAa(variant, transcriptSequence, modifiedCodonArray, predictedProteinSequence)) {
                        return predictedProteinSequence.toString();
                    }
                }
                modifiedCodonPosition = (modifiedCodonPosition + 1) % 3;
            }

            // Last predicted codon from the alternate needs to be completed with transcript nts
            if (modifiedCodonPosition < 2) {
                // indexing over transcriptSequence is 0 based
                int transcriptSequencePosition = cdnaVariantStart - 1;
                for (; modifiedCodonPosition < 3; modifiedCodonPosition++) {
                    modifiedCodonArray[modifiedCodonPosition] = alternate.toCharArray()[transcriptSequencePosition];
                    transcriptSequencePosition++;
                }
                addNewAa(variant, transcriptSequence, modifiedCodonArray, predictedProteinSequence);
            }
            return predictedProteinSequence.toString();
        }
        return null;
    }

    private boolean addNewAa(Variant variant, String transcriptSequence, char[] modifiedCodonArray, StringBuilder predictedProteinSequence) {
        String aa = VariantAnnotationUtils.getAminoacid(MT.equals(variant.getChromosome()), String.valueOf(modifiedCodonArray));
        if (aa != null) {
            // If STOP codon is gained prediction is interrupted and returned sequence is only predicted until
            // aa position before the STOP codon
            if (STOP_STRING.equals(aa)) {
                return false;
            } else {
                predictedProteinSequence.append(VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aa));
            }
        } else {
            predictedProteinSequence.append(UNKNOWN_AA);
            logger.warn("Unknown AA translation, setting an {}. Variant {}, Codon {}, cdnaVariantStart {}, "
                            + "transcriptSequence {} ", UNKNOWN_AA, variant.toString(),
                    String.valueOf(modifiedCodonArray), transcriptSequence);
        }
        return true;
    }

    private String getReferenceCodon(String chromosome, String cdnaSequence, int cdnaCodingStart, int cdsPosition) {
        // What buildingComponents.cdnaStart.offset really stores is the cdsStart
        int cdnaPosition = cdsPosition + cdnaCodingStart - 1; // TODO: might need adjusting +-1
        int variantPhaseShift = (cdnaPosition - cdnaCodingStart) % 3;
        int modifiedCodonStart = cdnaPosition - variantPhaseShift;

        // -1 and +2 because of base 0 String indexing
        String referenceCodon = cdnaSequence.substring(modifiedCodonStart - 1, modifiedCodonStart + 2);
        buildingComponents.setReferenceStart(VariantAnnotationUtils.getAminoacid(chromosome
                .equals(MITOCHONDRIAL_CHROMOSOME_STRING), referenceCodon));

        return null;
    }

    private String calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = hgvsNormalize(variant, transcript, normalizedVariant);

        // Use cDNA coordinates.
        buildingComponents.setKind(isCoding(transcript) ? BuildingComponents.Kind.CODING : BuildingComponents.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate.
        if (INS.equals(mutationType)) {
            setRangeCoordsAndAlleles(normalizedVariant.getStart() - 1, normalizedVariant.getStart(),
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);

            // dup of just one nt use only one coordinate
        } else if (normalizedVariant.getLength() == 1) {
            setRangeCoordsAndAlleles(normalizedVariant.getStart() - 1, normalizedVariant.getStart() - 1,
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);
            // dup of more than 1nt
        } else {
            // WARNING: -1 to fit the HGVS specification so that setRangeCoordsAndAlleles appropriately calculates
            // the offset to the nearest exon limit. This normalizedVariant object is not used after this line
            // and therefore has no other effect. Be careful
//            normalizedVariant.setStart(normalizedVariant.getStart() - 1);
//            setRangeCoordsAndAlleles(normalizedVariant, transcript, buildingComponents);
            setRangeCoordsAndAlleles(normalizedVariant.getStart(),
                    normalizedVariant.getStart() + normalizedVariant.getLength() - 1,
                    normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, buildingComponents);
        }

        buildingComponents.setMutationType(mutationType);
        buildingComponents.setTranscriptId(transcript.getId());
        buildingComponents.setGeneId(geneId);

        return formatTranscriptString(buildingComponents);
//        return Collections.singletonList(buildingComponents.format());
    }

    /**
     * Generate HGVS cDNA coordinates string.
     */
    @Override
    protected String formatCdnaCoords(BuildingComponents buildingComponents) {
        if (buildingComponents.getCdnaStart().equals(buildingComponents.getCdnaEnd())) {
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
        // Insertion or Insertion normalized as duplication
        // example:
        // "ENST00000382869.3:c.1735+32dupA", 1000_1001 insATG
        return buildingComponents.getMutationType() + buildingComponents.getAlternate();
    }

    private String hgvsNormalize(Variant variant, Transcript transcript, Variant normalizedVariant) {
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

}
