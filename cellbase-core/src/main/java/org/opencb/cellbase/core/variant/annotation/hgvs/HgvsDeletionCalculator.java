package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsDeletionCalculator extends HgvsCalculator {

    private static final String DEL = "del";

    public HgvsDeletionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    @Override
    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        Variant normalizedVariant = normalize(variant, normalize);
        String transcriptHgvs = calculateTranscriptHhgvs(normalizedVariant, transcript, geneId);
        String proteinHgvs = calculateProteinHgvs(normalizedVariant, transcript);

        if (proteinHgvs == null) {
            return Collections.singletonList(transcriptHgvs);
        } else {
            return Arrays.asList(transcriptHgvs, proteinHgvs);
        }
    }

    /**
     * Calculates protein HGVS. Must always be called after calling calculateTranscriptHhgvs, since the latter will
     * normalize and calculate cdna coords that will be used to calculate the protein HGVS.
     * @param variant Variant object containing genomic coordinates and variation for which protein HGVS wants to be
     *                calculated
     * @param transcript Transcript object containing the info for the transcript codifying the protein for which the
     *                  HGVS will be calculated
     * @return
     */
    private String calculateProteinHgvs(Variant variant, Transcript transcript) {
        // Check if protein HGVS can be calculated
        if (isCoding(transcript) && onlySpansCodingSequence(variant)) {
            ProteinHgvsStringBuilder proteinHgvsStringBuilder = new ProteinHgvsStringBuilder();
            proteinHgvsStringBuilder.setId(transcript.getProteinID());
            setProteinCoordinates(proteinHgvsStringBuilder);
            setAffectedAminoAcids(proteinHgvsStringBuilder);
        }

        return null;
    }

    private String calculateTranscriptHhgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = hgvsNormalize(variant, transcript, normalizedVariant);

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Use cDNA coordinates.
        hgvsStringBuilder.setKind(isCoding(transcript) ? HgvsStringBuilder.Kind.CODING : HgvsStringBuilder.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate
//        setRangeCoordsAndAlleles(normalizedVariant, transcript, hgvsStringBuilder);
        setRangeCoordsAndAlleles(normalizedVariant.getStart(), normalizedVariant.getEnd(),
                normalizedVariant.getReference(), normalizedVariant.getAlternate(), transcript, hgvsStringBuilder);

        hgvsStringBuilder.setMutationType(mutationType);
        hgvsStringBuilder.setTranscriptId(transcript.getId());
        hgvsStringBuilder.setGeneId(geneId);

        return hgvsStringBuilder.format();

    }

    private String hgvsNormalize(Variant variant, Transcript transcript, Variant normalizedVariant) {
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
