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

package org.opencb.cellbase.core.variant;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.tools.variant.VariantNormalizer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PopulationFrequencyPhasedQueryManager extends AnnotationBasedPhasedQueryManager<PopulationFrequency> {
    private static final char SHIFTED_POSITION_CHARACTER = '-';

    @Override
    protected List<Variant> getHaplotype(PopulationFrequency populationFrequency, Variant variant) {
        // Assuming only simple variants make it to this point
        // Simple variants - there's no complex haplotype, i.e. there's no set of phased variants.
        // Also detects whether decomposition took place for MNVs, as decomposed MNV allele strings must have at least
        // one lowercase character or one "-" indicating alignment shifting
        if ((populationFrequency.getRefAllele().length() == 1 && populationFrequency.getAltAllele().length() == 1)
                || StringUtils.isEmpty(populationFrequency.getRefAllele())
                || StringUtils.isEmpty(populationFrequency.getAltAllele())
                || !wasDecomposed(populationFrequency)) {
            return Collections.emptyList();
        }

        return alignmentToVariantList(populationFrequency.getRefAllele(),
                populationFrequency.getAltAllele(),
                variant);

    }

    private boolean wasDecomposed(PopulationFrequency populationFrequency) {
        for (char nucleotide : populationFrequency.getRefAllele().toCharArray()) {
            if (SHIFTED_POSITION_CHARACTER == nucleotide || Character.isLowerCase(nucleotide)) {
                return true;
            }
        }

        return false;
    }

    private List<Variant> alignmentToVariantList(String refAllele, String altAllele, Variant matchedVariant) {

        // Need to calculate the start coordinate of the alignment - matchedVariant might not be the first simple
        // variant that forms the MNV
        List<VariantNormalizer.VariantKeyFields> keyFieldsList
                = VariantNormalizer.decomposeAlignmentSingleVariants(refAllele.toUpperCase(),
                altAllele.toUpperCase(),
                getMNVStart(refAllele, altAllele, matchedVariant), null);

        return keyFieldsList.stream().map((keyFields) -> (new Variant(matchedVariant.getChromosome(),
                keyFields.getStart(),
                keyFields.getEnd(),
                keyFields.getReference(),
                keyFields.getAlternate()))).collect(Collectors.toList());

    }

    private int getMNVStart(String refAllele, String altAllele, Variant matchedVariant) {
        int uppercasePosition = getFirstUppercase(refAllele);

        if (uppercasePosition != -1) {
            // Remove the relative position within the MNV
            return matchedVariant.getStart() - uppercasePosition;
        }

        uppercasePosition = getFirstUppercase(altAllele);

        if (uppercasePosition != -1) {
            // Remove the relative position within the MNV
            return matchedVariant.getStart() - uppercasePosition;
        }

        throw new IllegalArgumentException("Malformed MNV alignment " + refAllele + "/" + altAllele + ". At least one"
                + " uppercase character is expected in any of the strings to be able to re-construct genomic "
                + " coordinates of the original MNV");

    }

    private int getFirstUppercase(String alleleString) {
        int i = 0;
        for (char nucleotide : alleleString.toCharArray()) {
            if (SHIFTED_POSITION_CHARACTER != nucleotide && Character.isUpperCase(nucleotide)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    @Override
    protected List<PopulationFrequency> getAnnotationObjectList(Variant variant) {
        return variant.getAnnotation().getPopulationFrequencies();
    }
}
