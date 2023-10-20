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

package org.opencb.cellbase.lib.builders.clinical.variant;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
import org.opencb.commons.utils.PrintUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by fjlopez on 04/10/16.
 */
public abstract class ClinicalIndexer {

    protected static final char HAPLOTYPE_STRING_SEPARATOR = ',';
    protected static Logger logger
            = LoggerFactory.getLogger("org.opencb.cellbase.app.transform.clinical.variant.ClinicalIndexer");
    private static final String VARIANT_STRING_PATTERN = "([ACGTN]*)|(<CNV[0-9]+>)|(<DUP>)|(<DEL>)|(<INS>)|(<INV>)";

    protected int numberNewVariants = 0;
    protected int numberVariantUpdates = 0;
    protected int totalNumberRecords = 0;
    protected int numberIndexedRecords = 0;
    protected RocksDB rdb;


    protected static final String SYMBOL = "symbol";

    protected static ObjectMapper mapper;
    protected static ObjectReader objectReader;
    protected static ObjectWriter jsonObjectWriter;

    static {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectReader = mapper.readerFor(VariantAnnotation.class);
        jsonObjectWriter = mapper.writer();

        PrintUtils.printSpace();
//        jsonObjectWriter = mapper.writerFor(VariantAnnotation.class);
    }

    protected Path genomeSequenceFilePath;
    protected boolean normalize = true;
    protected VariantNormalizer normalizer;

    public ClinicalIndexer(Path genomeSequenceFilePath) throws IOException {
        // Forcing decomposition here in all cases - assuming the way CellBase stores clinical variants from here
        // onwards will be decomposed and Adaptors will deal with phased/no-phased queries
        VariantNormalizer.VariantNormalizerConfig variantNormalizerConfig
                = (new VariantNormalizer.VariantNormalizerConfig())
                .setReuseVariants(true)
                .setNormalizeAlleles(false)
                .setDecomposeMNVs(true);

        if (genomeSequenceFilePath != null) {
            logger.info("Enabling left aligning by using sequence at {}", genomeSequenceFilePath.toString());
            variantNormalizerConfig.enableLeftAlign(genomeSequenceFilePath.toString());
        } else {
            logger.info("Left alignment is NOT enabled.");
        }
        normalizer = new VariantNormalizer(variantNormalizerConfig);
    }


    protected VariantAnnotation getVariantAnnotation(byte[] key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key);
//        List<EvidenceEntry> evidenceEntryList;
        VariantAnnotation variantAnnotation;
        if (dbContent == null) {
            variantAnnotation = new VariantAnnotation();
            List<EvidenceEntry> evidenceEntryList = new ArrayList<>();
            variantAnnotation.setTraitAssociation(evidenceEntryList);
            numberNewVariants++;
        } else {
            variantAnnotation = objectReader.readValue(dbContent);
//            List<EvidenceEntry> evidenceEntryList = mapper.readValue(dbContent, mapper.getTypeFactory()
// .constructParametrizedType(List.class, List.class, EvidenceEntry.class));
            numberVariantUpdates++;
        }
        return variantAnnotation;
    }

    protected GenomicFeature createGeneGenomicFeature(String gene) {
        Map<String, String> map = new HashMap<>(1);
        map.put(SYMBOL, gene);

        return new GenomicFeature(FeatureTypes.gene, null, map);
    }

    protected GenomicFeature createGeneGenomicFeature(String featureId, FeatureTypes featureTypes) {
        Map<String, String> map = new HashMap<>(1);
        map.put(SYMBOL, featureId);
        return new GenomicFeature(featureTypes, null, map);
    }

    protected List<AlleleOrigin> getAlleleOriginList(List<String> sourceOriginList) {
        List<AlleleOrigin> alleleOrigin;
        alleleOrigin = new ArrayList<>(sourceOriginList.size());
        for (String originString : sourceOriginList) {
            if (VariantAnnotationUtils.ORIGIN_STRING_TO_ALLELE_ORIGIN.containsKey(originString)) {
                alleleOrigin.add(VariantAnnotationUtils.ORIGIN_STRING_TO_ALLELE_ORIGIN.get(originString));
            } else {
                logger.debug("No SO term found for allele origin {}. Skipping.", originString);
            }
        }
        return alleleOrigin;
    }

    protected VariantClassification getVariantClassification(List<String> classificationStringList) {
        VariantClassification variantClassification = new VariantClassification();
        for (String value : classificationStringList) {
            value = value.toLowerCase().trim();
            if (VariantAnnotationUtils.CLINVAR_CLINSIG_TO_ACMG.containsKey(value)) {
                // No value set
                if (variantClassification.getClinicalSignificance() == null) {
                    variantClassification.setClinicalSignificance(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_ACMG.get(value));
                    // Seen cases like Benign;Pathogenic;association;not provided;risk factor for the same record
                } else if (isBenign(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_ACMG.get(value))
                        && isPathogenic(variantClassification.getClinicalSignificance())) {
                    logger.warn("Benign and Pathogenic clinical significances found for the same record");
                    logger.warn("Will set uncertain_significance instead");
                    variantClassification.setClinicalSignificance(ClinicalSignificance.uncertain_significance);
                }
            } else if (VariantAnnotationUtils.CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION.containsKey(value)) {
                variantClassification.setTraitAssociation(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_TRAIT_ASSOCIATION.get(value));
            } else if (VariantAnnotationUtils.CLINVAR_CLINSIG_TO_DRUG_RESPONSE.containsKey(value)) {
                variantClassification.setDrugResponseClassification(VariantAnnotationUtils.CLINVAR_CLINSIG_TO_DRUG_RESPONSE.get(value));
            } else {
                logger.debug("No mapping found for referenceClinVarAssertion.clinicalSignificance {}", value);
                logger.debug("No value will be set at EvidenceEntry.variantClassification for this term");
            }
        }
        return variantClassification;
    }

    private boolean isPathogenic(ClinicalSignificance clinicalSignificance) {
        return ClinicalSignificance.pathogenic.equals(clinicalSignificance)
                || ClinicalSignificance.likely_pathogenic.equals(clinicalSignificance);
    }

    private boolean isBenign(ClinicalSignificance clinicalSignificance) {
        return ClinicalSignificance.benign.equals(clinicalSignificance)
                || ClinicalSignificance.likely_benign.equals(clinicalSignificance);
    }

    protected List<String> getNormalisedVariantString(String chromosome, int start, String reference, String alternate) {
        Variant variant = new Variant(chromosome, start, reference, alternate);
        return getNormalisedVariantString(variant);
    }

    protected List<String> getNormalisedVariantString(String chromosome, int start, int end, String reference, String alternate) {
        Variant variant = new Variant(chromosome, start, end, reference, alternate);
        return getNormalisedVariantString(variant);
    }

    protected List<String> getNormalisedVariantString(Variant variant) {

        // Checks no weird characters are part of the reference & alternate alleles
        if (isValid(variant)) {
            List<Variant> normalizedVariantList;
            if (normalize) {
                try {
                    // No decomposition allowed at the moment therefore only one variant in returned list of variants.
                    normalizedVariantList = normalizer.apply(Collections.singletonList(variant));
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    logger.warn("Error found during variant normalization. Skipping variant: {}", variant.toString());
                    return null;
                }
            } else {
                normalizedVariantList = Collections.singletonList(variant);
            }

            return normalizedVariantList.stream().map((variant1) -> variant1.toString()).collect(Collectors.toList());
        }

        return null;
    }

    protected boolean isValid(Variant variant) {
        return (variant.getReference().matches(VARIANT_STRING_PATTERN)
                && (variant.getAlternate().matches(VARIANT_STRING_PATTERN)
                && !variant.getAlternate().equals(variant.getReference())));
    }

    protected void addHaplotypeProperty(EvidenceEntry evidenceEntry, List<String> normalisedVariantStringList) {
        // If more than one variant in the MNV (haplotype), create haplotype property in additionalProperties
        if (normalisedVariantStringList.size() > 1) {
            // This variant is part of an MNV (haplotype). Leave a flag of all variants that form the MNV
            // Assuming additionalProperties has already been created as per the upstream code
            evidenceEntry.getAdditionalProperties().add(new Property("HAPLOTYPE", "Haplotype",
                    StringUtils.join(normalisedVariantStringList, HAPLOTYPE_STRING_SEPARATOR)));
        }
    }

    protected void addHaplotypeProperty(List<EvidenceEntry> evidenceEntryList, List<String> normalisedVariantStringList) {
        // If more than one variant in the MNV (haplotype), create haplotype property in additionalProperties
        if (evidenceEntryList != null && normalisedVariantStringList.size() > 1) {
            for (EvidenceEntry evidenceEntry : evidenceEntryList) {
                addHaplotypeProperty(evidenceEntry, normalisedVariantStringList);
            }
        }
    }

    class SequenceLocation {
        private String chromosome;
        private int start;
        private int end;
        private String reference;
        private String alternate;
        private String strand;

        SequenceLocation() {
        }

        SequenceLocation(String chromosome, int start, int end, String reference, String alternate) {
            this(chromosome, start, end, reference, alternate, "+");
        }

        SequenceLocation(String chromosome, int start, int end, String reference, String alternate, String strand) {
            this.chromosome = chromosome;
            this.start = start;
            this.end = end;
            this.reference = reference;
            this.alternate = alternate;
            this.strand = strand;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SequenceLocation{");
            sb.append("chromosome='").append(chromosome).append('\'');
            sb.append(", start=").append(start);
            sb.append(", end=").append(end);
            sb.append(", reference='").append(reference).append('\'');
            sb.append(", alternate='").append(alternate).append('\'');
            sb.append(", strand='").append(strand).append('\'');
            sb.append('}');
            return sb.toString();
        }

        public String getChromosome() {
            return chromosome;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getReference() {
            return reference;
        }

        public String getAlternate() {
            return alternate;
        }

        public String getStrand() {
            return strand;
        }

        public void setChromosome(String chromosome) {
            this.chromosome = chromosome;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public void setAlternate(String alternate) {
            this.alternate = alternate;
        }

        public void setStrand(String strand) {
            this.strand = strand;
        }
    }


}
