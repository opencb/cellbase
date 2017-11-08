package org.opencb.cellbase.core.variant.annotation.hgvs;

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
                proteinHgvsNormalize(proteinVariant, transcript.getProteinSequence());
                setProteinLocationAndAminoacid(variant, transcript);
                // Additional normalization required for insertions
                Variant normalizedVariant = new Variant();

                return formatProteinString(buildingComponents);
            }

        }
        return null;
    }

    private Variant createProteinVariant(Variant variant, Transcript transcript) {
        Variant proteinVariant = new Variant();

        int cdnaCodingStart = transcript.getCdnaCodingStart();
        if (transcript.unconfirmedStart()) {
            cdnaCodingStart -= ((3 - getFirstCdsPhase(transcript)) % 3);
        }
        proteinVariant.setStart(getAminoAcidPosition(cdnaCodingStart, buildingComponents.getCdnaStart().getOffset()));
        proteinVariant.setEnd(getAminoAcidPosition(cdnaCodingStart, buildingComponents.getCdnaEnd().getOffset()));

        // We expect buildingComponents.getStart() and buildingComponents.getEnd() to be within the sequence boundaries.
        // However, there are pretty weird cases such as unconfirmedStart/unconfirmedEnd transcript which could be
        // potentially dangerous in this sense. Just double-checking with this if to avoid potential exceptions
        if (proteinVariant.getStart() > 0 && proteinVariant.getEnd() < transcript.getProteinSequence().length()) {
            proteinVariant.setAlternate(EMPTY_STRING);
            proteinVariant.setReference(transcript.getProteinSequence().substring(proteinVariant.getStart() - 1,
                    proteinVariant.getEnd() - 1));

            return proteinVariant;
        }
        logger.warn("Protein start/end out of protein seq boundaries: {}, {}-{}, prot length: {}. This should, in principle,"
                        + " not happen and protein HGVS will not be returned. Could be expected for "
                        +"unconfirmedStart/unconfirmedEnd transcripts. Please, check.",
                buildingComponents.getProteinId(), proteinVariant.getStart(), proteinVariant.getEnd(),
                transcript.getProteinSequence().length());

        return null;
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
