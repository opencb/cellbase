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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.variant.civic.CivicParserCallback;
import org.opencb.biodata.models.core.civic.CivicAssertion;
import org.opencb.biodata.models.core.civic.CivicClinicalEvidence;
import org.opencb.biodata.models.core.civic.CivicFeature;
import org.opencb.biodata.models.core.civic.CivicVariant;
import org.opencb.biodata.models.sequence.SequenceLocation;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.lib.EtlCommons;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class CivicIndexerCallback implements CivicParserCallback {

    private RocksDB rdb;
    private ClinicalIndexer clinicalIndexer;

    private EvidenceSource evidenceSource;

    private int numInvalidPositionLines = 0;
    private int numInvalidBaseLines = 0;
    private int numPassedVariants = 0;

    private static Logger logger = LoggerFactory.getLogger(CivicIndexerCallback.class);

    public CivicIndexerCallback(RocksDB rdb, ClinicalIndexer clinicalIndexer) {
        this.rdb = rdb;
        this.clinicalIndexer = clinicalIndexer;

        this.evidenceSource = new EvidenceSource(EtlCommons.CIVIC_DATA, clinicalIndexer.version, null);
    }

    @Override
    public boolean processCivicVariant(CivicVariant civicVariant) {
        try {
            // Get sequence location
            SequenceLocation sequenceLocation = getLocation(civicVariant);

            // Get evidence entries
            List<EvidenceEntry> evidenceEntries = getEvidences(civicVariant);

            // Update RocksDB
            return updateRocksDB(sequenceLocation, evidenceEntries);
        } catch (Exception e) {
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

                rdb.put(normalisedVariantString.getBytes(), clinicalIndexer.jsonObjectWriter.writeValueAsBytes(variantAnnotation));
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

        if (StringUtils.isNotEmpty(chromosome) && StringUtils.isNotEmpty(start)) {
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
        EvidenceEntry evidenceEntry = new EvidenceEntry();

        // Set source, ID and URL
        evidenceEntry.setSource(evidenceSource);
        evidenceEntry.setId(civicVariant.getNcitId());
        evidenceEntry.setUrl(civicVariant.getVariantCivicUrl());

        // Genomic feature
        if (civicVariant.getFeature() != null && StringUtils.isNotEmpty(civicVariant.getFeature().getName())
                && StringUtils.isNotEmpty(civicVariant.getFeature().getFeatureType())) {
            CivicFeature civicFeature = civicVariant.getFeature();

            // feature_type: Gene (e.g.: FGFR1), Factor (e.g.: Kataegis) and Fusion (e.g.:LANCL2::EGFR)
            Map<String, String> xrefs = new HashMap<>();
            GenomicFeature genomicFeature = new GenomicFeature();
            if ("Gene".equals(civicFeature.getFeatureType())) {
                genomicFeature.setFeatureType(FeatureTypes.gene);
                xrefs.put("", civicFeature.getName());
            } else {
                xrefs.put(civicFeature.getFeatureType(), civicFeature.getName());
            }
            if (StringUtils.isNotEmpty(civicFeature.getEntrezId())) {
                xrefs.put("entrez", civicFeature.getEntrezId());
            }
            if (StringUtils.isNotEmpty(civicFeature.getNcitId())) {
                xrefs.put("ncit", civicFeature.getNcitId());
            }
            genomicFeature.setXrefs(xrefs);

            // Add genomic feature to evidence entry
            evidenceEntry.setGenomicFeatures(Collections.singletonList(genomicFeature));
        }

        // Biographical info
        List<String> bibliography = getBibliography(civicVariant);
        if (CollectionUtils.isNotEmpty(bibliography)) {
            evidenceEntry.setBibliography(bibliography);
        }

        return Collections.singletonList(evidenceEntry);
    }

    private List<String> getBibliography(CivicVariant civicVariant) {
        Set<String> bibliography = new HashSet<>();
        if (civicVariant.getMolecularProfile() != null) {
            // Get bibliography from evidences of the molecular profile
            if (CollectionUtils.isNotEmpty(civicVariant.getMolecularProfile().getEvidences())) {
                bibliography.addAll(getBibliography(civicVariant.getMolecularProfile().getEvidences()));
            }

            // Get bibliography from evidences of the assertions
            if (CollectionUtils.isNotEmpty(civicVariant.getMolecularProfile().getAssertions())) {
                for (CivicAssertion assertion : civicVariant.getMolecularProfile().getAssertions()) {
                    if (CollectionUtils.isNotEmpty(assertion.getEvidences())) {
                        bibliography.addAll(getBibliography(assertion.getEvidences()));
                    }
                }
            }
        }

        return new ArrayList<>(bibliography);
    }

    private Set<String> getBibliography(List<CivicClinicalEvidence> civicEvidences) {
        Set<String> bibliography = new HashSet<>();
        for (CivicClinicalEvidence evidence : civicEvidences) {
            if (StringUtils.isNotEmpty(evidence.getCitation()) && StringUtils.isNotEmpty(evidence.getSourceType())) {
                bibliography.add(evidence.getSourceType() + ":" + evidence.getCitation());
            }
        }
        return bibliography;
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
