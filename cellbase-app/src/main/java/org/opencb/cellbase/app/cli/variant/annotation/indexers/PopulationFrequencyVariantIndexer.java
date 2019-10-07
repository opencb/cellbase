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

package org.opencb.cellbase.app.cli.variant.annotation.indexers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.List;

public class PopulationFrequencyVariantIndexer extends VariantIndexer {
    private static final String EMPTY_ALLELE_STRING = "";
    private static final char SHIFTED_POSITION_CHARACTER = '-';
    private static final char UKNOWN_NUCLEOTIDE = 'n';

    public PopulationFrequencyVariantIndexer(VariantReader variantReader, int maxOpenFiles, boolean forceCreate) {
        super(variantReader, maxOpenFiles, forceCreate);
    }

    @Override
    protected void updateIndex(List<Variant> variantList) throws IOException, RocksDBException {
        for (Variant variant : variantList) {
            // If MNV then edit alternate allele to include a string tha represents all variants forming the MNV
            String haplotypeString = AnnotationBasedPhasedQueryManager.getSampleAttribute(variant,
                    AnnotationBasedPhasedQueryManager.PHASE_SET_TAG);
            if (StringUtils.isNotBlank(haplotypeString)) {
                Pair<String, String> alleleAlignment = getAlleleAlignment(haplotypeString, variant);
                for (PopulationFrequency populationFrequency : variant.getAnnotation().getPopulationFrequencies()) {
                    populationFrequency.setRefAllele(alleleAlignment.getLeft());
                    populationFrequency.setAltAllele(alleleAlignment.getRight());
                }
            }

            byte[] dbContent = dbIndex.get(variant.toString().getBytes());
            Variant variantToIndex;

            if (dbContent == null) {
                variantToIndex = variant;
            } else {
                variantToIndex = jsonObjectMapper.readValue(dbContent, Variant.class);

                // Add all pop frequencies from current variant
                variantToIndex
                        .getAnnotation()
                        .getPopulationFrequencies().addAll(variant.getAnnotation().getPopulationFrequencies());
            }

            dbIndex.put(variantToIndex.toString().getBytes(), jsonObjectWriter.writeValueAsBytes(variantToIndex));
        }
    }

    private Pair<String, String> getAlleleAlignment(String haplotypeString, Variant variant) {
        List<Variant> variantList = Variant.parseVariants(haplotypeString);
        StringBuilder referenceBuilder = new StringBuilder();
        StringBuilder alternateBuilder = new StringBuilder();

        // Assumming variantList has at least 1 element because it's checked in the code upstream
        int currentGenomicPosition = variantList.get(0).getStart();
        for (Variant variant1 : variantList) {

            // Fill with 'n's positions that correspond to matches, e.g in alignment AATT--
            //                                                                       --TTGG
            // since we are just iterating variants the T>T T>T changes would not be taken into consideration otherwise
            int matchSpaceSize = variant1.getStart() - currentGenomicPosition;
            for (int i = 0; i < (matchSpaceSize); i++) {
                referenceBuilder.append(UKNOWN_NUCLEOTIDE);
                alternateBuilder.append(UKNOWN_NUCLEOTIDE);
                currentGenomicPosition++;
            }

            // Assuming only simple variants can (and will) make it to this point
            boolean sameVariant = isSameVariant(variant, variant1);
            // Insertions
            if (EMPTY_ALLELE_STRING.equals(variant1.getReference())) {
                for (char nucleotide : variant1.getAlternate().toCharArray()) {
                    referenceBuilder.append(SHIFTED_POSITION_CHARACTER);
                    alternateBuilder.append(sameVariant ? nucleotide : Character.toLowerCase(nucleotide));
                    currentGenomicPosition++;
                }
            // Deletions
            } else if (EMPTY_ALLELE_STRING.equals(variant1.getAlternate())) {
                for (char nucleotide : variant1.getReference().toCharArray()) {
                    alternateBuilder.append(SHIFTED_POSITION_CHARACTER);
                    referenceBuilder.append(sameVariant ? nucleotide : Character.toLowerCase(nucleotide));
                    currentGenomicPosition++;
                }
            // SNVs
            } else if (variant1.getReference().length() == variant1.getAlternate().length()) {
                referenceBuilder.append(sameVariant ? variant1.getReference() : variant1.getReference().toLowerCase());
                alternateBuilder.append(sameVariant ? variant1.getAlternate() : variant1.getAlternate().toLowerCase());
                currentGenomicPosition++;
            } else {
                throw new IllegalArgumentException("Unexpected variant type when processing haplotype "
                        + haplotypeString + ". Trying to parse variant " + variant1.toString() + " into alignment.");
            }

        }

        return new ImmutablePair<>(referenceBuilder.toString(), alternateBuilder.toString());
    }

    private boolean isSameVariant(Variant variant, Variant variant1) {
        // In the context of this class can safely assume chromosome is always the same
        return variant.getStart().equals(variant1.getStart())
                && variant.getEnd().equals(variant1.getEnd())
                && variant.getReference().equals(variant1.getReference())
                && variant.getAlternate().equals(variant1.getAlternate());
    }
}
