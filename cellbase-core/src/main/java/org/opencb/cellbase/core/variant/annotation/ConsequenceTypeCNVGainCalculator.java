/*
 * Copyright 2015-2019 OpenCB
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

package org.opencb.cellbase.core.variant.annotation;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 31/05/16.
 */
public class ConsequenceTypeCNVGainCalculator extends ConsequenceTypeGenericRegionCalculator {

    public ConsequenceTypeCNVGainCalculator() { }

    public List<ConsequenceType> run(Variant inputVariant, List<Gene> geneList, boolean[] overlapsRegulatoryRegion,
                                     QueryOptions queryOptions) {
        parseQueryParam(queryOptions);
        List<ConsequenceType> consequenceTypeList = new ArrayList<>();
        variant = inputVariant;
        variantEnd = getEnd(cnvExtraPadding);
        variantStart = getStart(cnvExtraPadding);
//        isBigDeletion = ((variantEnd - variantStart) > BIG_VARIANT_SIZE_THRESHOLD);
        boolean isIntergenic = true;
        for (Gene currentGene : geneList) {
            gene = currentGene;
            for (Transcript currentTranscript : gene.getTranscripts()) {
                isIntergenic = isIntergenic && (variantEnd < currentTranscript.getStart() || variantStart > currentTranscript.getEnd());
                transcript = currentTranscript;
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(transcript.getId());
                consequenceType.setStrand(transcript.getStrand());
                consequenceType.setBiotype(transcript.getBiotype());
                consequenceType.setTranscriptAnnotationFlags(transcript.getAnnotationFlags() != null
                        ? new ArrayList<>(transcript.getAnnotationFlags()) : null);
                SoNames.clear();

                if (transcript.getStrand().equals("+")) {
                    // whole transcript affected
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) {
                        SoNames.add(VariantAnnotationUtils.TRANSCRIPT_AMPLIFICATION);
                        consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        solvePositiveTranscript(consequenceTypeList);
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.UPSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just may have upstream/downstream annotations
                            consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                } else {
                    if (variantStart <= transcript.getStart() && variantEnd >= transcript.getEnd()) { // whole trans. affected
                        SoNames.add(VariantAnnotationUtils.TRANSCRIPT_AMPLIFICATION);
                        consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                        consequenceTypeList.add(consequenceType);
                    } else if (regionsOverlap(transcript.getStart(), transcript.getEnd(), variantStart, variantEnd)) {
                        solveNegativeTranscript(consequenceTypeList);
                    } else {
                        solveTranscriptFlankingRegions(VariantAnnotationUtils.DOWNSTREAM_GENE_VARIANT,
                                VariantAnnotationUtils.UPSTREAM_GENE_VARIANT);
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just has upstream/downstream annotations
                            consequenceType.setSequenceOntologyTerms(getSequenceOntologyTerms(SoNames));
                            consequenceTypeList.add(consequenceType);
                        }
                    }
                }
            }
        }

        solveIntergenic(consequenceTypeList, isIntergenic);
        solveRegulatoryRegions(overlapsRegulatoryRegion, consequenceTypeList);
        return consequenceTypeList;
    }
}
