/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.core.variant.annotation.hgvs;

import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.core.GenomeDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.Collections;
import java.util.List;

/**
 * Created by fjlopez on 15/06/17.
 */
public class HgvsInsertionCalculator extends HgvsCalculator {

    private static final String INS = "ins";
    private static final String DUP = "dup";

    public HgvsInsertionCalculator(GenomeDBAdaptor genomeDBAdaptor) {
        super(genomeDBAdaptor);
    }

    @Override
    protected List<String> run(Variant variant, Transcript transcript, String geneId, boolean normalize) {
        Variant normalizedVariant = normalize(variant, normalize);
        return calculateTranscriptHgvs(normalizedVariant, transcript, geneId);
    }

    private List<String> calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {
        // Additional normalization required for insertions
        Variant normalizedVariant = new Variant();
        String mutationType = hgvsNormalize(variant, transcript, normalizedVariant);

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Use cDNA coordinates.
        hgvsStringBuilder.setKind(isCoding(transcript) ? HgvsStringBuilder.Kind.CODING : HgvsStringBuilder.Kind.NON_CODING);

        // Use a range of coordinates. - Calculate start/end, reference/alternate alleles as appropriate.
        if (INS.equals(mutationType)) {
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

    private String hgvsNormalize(Variant variant, Transcript transcript, Variant normalizedVariant) {
        // Get genomic sequence around the lesion.
        int start = Math.max(variant.getStart() - NEIGHBOURING_SEQUENCE_SIZE, 1);  // TODO: might need to adjust +-1 nt
        int end = variant.getStart() + NEIGHBOURING_SEQUENCE_SIZE;                 // TODO: might need to adjust +-1 nt
        Query query = new Query(GenomeDBAdaptor.QueryParams.REGION.key(), variant.getChromosome()
                + ":" + start + "-" + end);
        String genomicSequence = genomeDBAdaptor.getGenomicSequence(query, new QueryOptions()).getResults().get(0).getSequence();

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
