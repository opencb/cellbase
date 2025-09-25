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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.variant.civic.CivicParserCallback;
import org.opencb.biodata.models.core.civic.*;
import org.opencb.biodata.models.sequence.SequenceLocation;
import org.opencb.biodata.models.variant.avro.*;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.CIVIC_DATA;
import static org.opencb.cellbase.lib.builders.clinical.variant.ClinicalIndexer.ORIGINAL_ADDITIONAL_PROPERTY_ID;

public class CivicIndexerCallback implements CivicParserCallback {

    private RocksDB rdb;
    private ClinicalIndexer clinicalIndexer;

    private EvidenceSource evidenceSource;
    private ObjectWriter civicVariantObjectWriter;

    private int numInvalidPositionLines = 0;
    private int numInvalidBaseLines = 0;
    private int numPassedVariants = 0;

    private static Logger logger = LoggerFactory.getLogger(CivicIndexerCallback.class);

    public CivicIndexerCallback(RocksDB rdb, ClinicalIndexer clinicalIndexer) {
        this.rdb = rdb;
        this.clinicalIndexer = clinicalIndexer;

        this.evidenceSource = new EvidenceSource(CIVIC_DATA, clinicalIndexer.version, null);
        this.civicVariantObjectWriter = new ObjectMapper().writerFor(CivicVariant.class);
    }

    @Override
    public boolean processCivicVariant(CivicVariant civicVariant) {
        try {
            // Get sequence location
            SequenceLocation sequenceLocation = getLocation(civicVariant);
            if (sequenceLocation != null) {
                // Get evidence entries
                List<EvidenceEntry> evidenceEntries = getEvidences(civicVariant);
                if (CollectionUtils.isNotEmpty(evidenceEntries)) {
                    // Update RocksDB
                    return updateRocksDB(sequenceLocation, evidenceEntries);
                } else {
                    logger.warn("Skipping CIViC variant ID {}: no evidences found", civicVariant.getVariantId());
                    return false;
                }
            } else {
                logger.warn("Skipping CIViC variant ID {}: invalid position", civicVariant.getVariantId());
                return false;
            }
        } catch (Exception e) {
            // Log message
            logger.warn("Skipping CIViC variant ID {}; exception message: {}", civicVariant.getVariantId(), e.getMessage());
            return false;
        }
    }

    private boolean updateRocksDB(SequenceLocation sequenceLocation, List<EvidenceEntry> evidenceEntries)
            throws RocksDBException, IOException {
        // More than one variant being returned from the normalisation process would mean it's and MNV which has been decomposed
        List<String> normalisedVariantStringList = clinicalIndexer.getNormalisedVariantString(sequenceLocation.getChromosome(),
                sequenceLocation.getStart(), sequenceLocation.getReference(), sequenceLocation.getAlternate());

        if (CollectionUtils.isNotEmpty(normalisedVariantStringList)) {
            for (String normalisedVariantString : normalisedVariantStringList) {
                VariantAnnotation variantAnnotation = clinicalIndexer.getVariantAnnotation(normalisedVariantString.getBytes());

                // Add haplotype property to all EvidenceEntry objects in variant if there are more than 1 variants in
                // normalisedVariantStringList, i.e. if this variant is part of an MNV (haplotype)
                clinicalIndexer.addHaplotypeProperty(evidenceEntries, normalisedVariantStringList);

                // Add EvidenceEntry objects
                variantAnnotation.getTraitAssociation().addAll(evidenceEntries);

                rdb.put(normalisedVariantString.getBytes(), ClinicalIndexer.jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            }

            numPassedVariants++;
        }

        // Always return true to continue processing (exceptions are caught in processCivicVariant, then false is returned)
        return true;
    }

    private SequenceLocation getLocation(CivicVariant civicVariant) {
        String chromosome = civicVariant.getChromosome();
        String start = civicVariant.getStart();
        String ref = StringUtils.isEmpty(civicVariant.getReferenceBases()) ? "-" : civicVariant.getReferenceBases();
        String alt = StringUtils.isEmpty(civicVariant.getVariantBases()) ? "-" : civicVariant.getVariantBases();

        if (StringUtils.isEmpty(chromosome) || StringUtils.isEmpty(start)) {
            logger.warn("Invalid position: {}:{} (CIViC ID: {})", chromosome, start, civicVariant.getVariantId());
            numInvalidPositionLines++;
            return null;
        }

        if ("-".equals(ref) && "-".equals(alt)) {
            logger.warn("Invalid alleles: {}:{} {}>{} (CIViC ID: {})", chromosome, start, ref, alt, civicVariant.getVariantId());
            numInvalidBaseLines++;
            return null;
        }

        return new SequenceLocation(chromosome, Integer.parseInt(start), Integer.parseInt(start), ref, alt);
    }

    private List<EvidenceEntry> getEvidences(CivicVariant civicVariant) {
        List<EvidenceEntry> evidenceEntries = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(civicVariant.getMolecularProfiles())) {
            for (CivicMolecularProfile molecularProfile : civicVariant.getMolecularProfiles()) {
                // Process evidences from molecular profile
                if (CollectionUtils.isNotEmpty(molecularProfile.getEvidences())) {
                    addEvidenceEntries(civicVariant, molecularProfile.getEvidences(), evidenceEntries);
                }

                // Process evidences from assertions
                if (CollectionUtils.isNotEmpty(molecularProfile.getAssertions())) {
                    for (CivicAssertion assertion : molecularProfile.getAssertions()) {
                        if (CollectionUtils.isNotEmpty(assertion.getEvidences())) {
                            addEvidenceEntries(civicVariant, assertion.getEvidences(), evidenceEntries);
                        }
                    }
                }
            }
        }

        return evidenceEntries;
    }

    private void addEvidenceEntries(CivicVariant civicVariant, List<CivicClinicalEvidence> civicEvidences,
                                    List<EvidenceEntry> evidenceEntries) {
        for (CivicClinicalEvidence civicEvidence : civicEvidences) {
            try {
                EvidenceEntry evidenceEntry = createEvidenceEntry(civicVariant, civicEvidence);
                evidenceEntries.add(evidenceEntry);
            } catch (JsonProcessingException e) {
                logger.warn("Error creating evidence entry for CIViC evidence ID {}: {}", civicEvidence.getEvidenceId(), e.getMessage());
            }
        }
    }

    private EvidenceEntry createEvidenceEntry(CivicVariant civicVariant, CivicClinicalEvidence civicEvidence)
            throws JsonProcessingException {
        EvidenceEntry evidenceEntry = new EvidenceEntry();

        // Set source, ID and URL
        evidenceEntry.setSource(evidenceSource);
        evidenceEntry.setId(civicEvidence.getEvidenceId());
        evidenceEntry.setUrl(civicEvidence.getEvidenceCivicUrl());

        // Assembly
        evidenceEntry.setAssembly(clinicalIndexer.assembly);

        // Set genomic feature from variant's feature
        if (civicVariant.getFeature() != null) {
            List<GenomicFeature> genomicFeatures = createGenomicFeatures(civicVariant.getFeature());
            evidenceEntry.setGenomicFeatures(genomicFeatures);
        }

        // Impact
        evidenceEntry.setImpact(getEvidenceImpact(civicEvidence.getEvidenceLevel()));

        // Confidence
        evidenceEntry.setConfidence(getConfidence(civicEvidence.getRating()));

        // Description
        evidenceEntry.setDescription(civicEvidence.getEvidenceStatement());

        // Bibliography
        List<String> bibliography = new ArrayList<>();
        if (StringUtils.isNotEmpty(civicEvidence.getCitation()) && StringUtils.isNotEmpty(civicEvidence.getSourceType())) {
            bibliography.add(civicEvidence.getSourceType() + ":" + civicEvidence.getCitation());
        }
        evidenceEntry.setBibliography(bibliography);

        // In additional properties, we put all the CIViC variant related to that evidence and other properties from evidence
        List<Property> additionalProperties = new ArrayList<>();
        String jsonCivicVariant = civicVariantObjectWriter.writeValueAsString(civicVariant);
        additionalProperties.add(new Property(ORIGINAL_ADDITIONAL_PROPERTY_ID, CIVIC_DATA, jsonCivicVariant));
        addAdditionalProperties(civicEvidence, additionalProperties);
        evidenceEntry.setAdditionalProperties(additionalProperties);

        return evidenceEntry;
    }

    private List<GenomicFeature> createGenomicFeatures(CivicFeature civicFeature) {
        if (civicFeature == null || StringUtils.isEmpty(civicFeature.getName()) || StringUtils.isEmpty(civicFeature.getFeatureType())) {
            return Collections.emptyList();
        }

        Map<String, String> xrefs = new HashMap<>();
        GenomicFeature genomicFeature = new GenomicFeature();

        // Set feature type based on CIViC feature type
        if ("Gene".equals(civicFeature.getFeatureType())) {
            genomicFeature.setFeatureType(FeatureTypes.gene);
            xrefs.put("symbol", civicFeature.getName());
        } else {
            xrefs.put(civicFeature.getFeatureType(), civicFeature.getName());
        }

        // Add additional cross-references
        if (StringUtils.isNotEmpty(civicFeature.getEntrezId())) {
            xrefs.put("entrez", civicFeature.getEntrezId());
        }
        if (StringUtils.isNotEmpty(civicFeature.getNcitId())) {
            xrefs.put("ncit", civicFeature.getNcitId());
        }

        genomicFeature.setXrefs(xrefs);
        return Collections.singletonList(genomicFeature);
    }

    private EvidenceImpact getEvidenceImpact(String civicEvidenceLevel) {
        if (civicEvidenceLevel == null) {
            return null;
        }

        switch (civicEvidenceLevel.toUpperCase()) {
            case "A":
                // Validated, well-powered studies
                return EvidenceImpact.very_strong;
            case "B":
                // Multiple clinical studies
                return EvidenceImpact.strong;
            case "C":
                // Case studies/series
                return EvidenceImpact.moderate;
            case "D":
                // Preclinical evidence
                return EvidenceImpact.supporting;
            case "E":
                // Inferential evidence (also supporting level)
                return EvidenceImpact.supporting;
            default:
                return null;
        }
    }

    private Confidence getConfidence(String rating) {
        if (StringUtils.isEmpty(rating)) {
            return null;
        }

        switch (rating) {
            case "5":
            case "4":
                return Confidence.high_confidence_level;
            case "3":
            case "2":
                return Confidence.medium_confidence_level;
            case "1":
                return Confidence.low_confidence_level;
            default:
                return null;
        }
    }

    private void addAdditionalProperties(CivicClinicalEvidence civicEvidence, List<Property> additionalProperties) {
        addAdditionalProperty("rating", civicEvidence.getRating(), additionalProperties);
        addAdditionalProperty("evidence_level", civicEvidence.getEvidenceLevel(), additionalProperties);
        addAdditionalProperty("disease", civicEvidence.getDisease(), additionalProperties);
        addAdditionalProperty("doid", civicEvidence.getDoid(), additionalProperties);
        addAdditionalProperty("phenotypes", civicEvidence.getPhenotypes(), additionalProperties);
        addAdditionalProperty("therapies", civicEvidence.getTherapies(), additionalProperties);
        addAdditionalProperty("therapy_interaction_type", civicEvidence.getTherapyInteractionType(), additionalProperties);
        addAdditionalProperty("evidence_type", civicEvidence.getEvidenceType(), additionalProperties);
        addAdditionalProperty("evidence_direction", civicEvidence.getEvidenceDirection(), additionalProperties);
        addAdditionalProperty("significance", civicEvidence.getSignificance(), additionalProperties);
        addAdditionalProperty("evidence_status", civicEvidence.getEvidenceStatus(), additionalProperties);
        addAdditionalProperty("variant_origin", civicEvidence.getVariantOrigin(), additionalProperties);
        addAdditionalProperty("last_review_date", civicEvidence.getLastReviewDate(), additionalProperties);
        addAdditionalProperty("is_flagged", civicEvidence.getFlagged(), additionalProperties);
    }

    private void addAdditionalProperty(String name, String value, List<Property> additionalProperties) {
        if (StringUtils.isNotEmpty(value)) {
            additionalProperties.add(new Property(null, name, value));
        }
    }

    private void addAdditionalProperty(String name, List<String> values, List<Property> additionalProperties) {
        if (CollectionUtils.isNotEmpty(values)) {
            additionalProperties.add(new Property(null, name, StringUtils.join(values, "; ")));
        }
    }

    private void addAdditionalProperty(String name, Boolean value, List<Property> additionalProperties) {
        if (value != null) {
            additionalProperties.add(new Property(null, name, Boolean.toString(value)));
        }
    }

    public int getNumInvalidPositionLines() {
        return numInvalidPositionLines;
    }

    public CivicIndexerCallback setNumInvalidPositionLines(int numInvalidPositionLines) {
        this.numInvalidPositionLines = numInvalidPositionLines;
        return this;
    }

    public int getNumInvalidBaseLines() {
        return numInvalidBaseLines;
    }

    public CivicIndexerCallback setNumInvalidBaseLines(int numInvalidBaseLines) {
        this.numInvalidBaseLines = numInvalidBaseLines;
        return this;
    }

    public int getNumPassedVariants() {
        return numPassedVariants;
    }

    public CivicIndexerCallback setNumPassedVariants(int numPassedVariants) {
        this.numPassedVariants = numPassedVariants;
        return this;
    }
}
