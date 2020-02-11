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
        Variant normalizedVariant = normalize(variant, normalize);
        return calculateTranscriptHgvs(normalizedVariant, transcript, geneId);
    }

    /**
     * Generates cdna HGVS names from an SNV.
     * @param transcript Transcript object that will be used as a reference
     */
    private List<String> calculateTranscriptHgvs(Variant variant, Transcript transcript, String geneId) {

        String mutationType = ">";

        // Populate HGVSName parse tree.
        HgvsStringBuilder hgvsStringBuilder = new HgvsStringBuilder();

        // Populate coordinates.
        // Use cDNA coordinates.
        hgvsStringBuilder.setKind(isCoding(transcript) ? HgvsStringBuilder.Kind.CODING : HgvsStringBuilder.Kind.NON_CODING);

        hgvsStringBuilder.setCdnaStart(genomicToCdnaCoord(transcript, variant.getStart()));
        hgvsStringBuilder.setCdnaEnd(hgvsStringBuilder.getCdnaStart());

        // Populate prefix.
        hgvsStringBuilder.setTranscriptId(transcript.getId());
        hgvsStringBuilder.setGeneId(geneId);

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
        hgvsStringBuilder.setMutationType(mutationType);
        hgvsStringBuilder.setReference(reference);
        hgvsStringBuilder.setAlternate(alternate);

        return Collections.singletonList(hgvsStringBuilder.format());
    }

}
