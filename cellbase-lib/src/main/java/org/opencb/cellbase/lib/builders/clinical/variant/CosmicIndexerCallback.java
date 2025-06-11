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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.variant.cosmic.CosmicParserCallback;
import org.opencb.biodata.models.sequence.SequenceLocation;
import org.opencb.biodata.models.variant.avro.*;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CosmicIndexerCallback implements CosmicParserCallback {

    private RocksDB rdb;
    private ClinicalIndexer clinicalIndexer;

    private static final int GENE_NAMES_COLUMN = 0;
    private static final int HGNC_COLUMN = 3;
    private static final int PRIMARY_SITE_COLUMN = 7;
    private static final int SITE_SUBTYPE_COLUMN = 8;
    private static final int PRIMARY_HISTOLOGY_COLUMN = 11;
    private static final int HISTOLOGY_SUBTYPE_COLUMN = 12;
    private static final int ID_COLUMN = 16;
    private static final int COSM_ID_COLUMN = 17;
    private static final int HGVS_COLUMN = 19;
    private static final int MUTATION_DESCRIPTION_COLUMN = 21;
    private static final int MUTATION_ZYGOSITY_COLUMN = 22;
    private static final int FATHMM_PREDICTION_COLUMN = 29;
    private static final int FATHMM_SCORE_COLUMN = 30;
    private static final int MUTATION_SOMATIC_STATUS_COLUMN = 31;
    private static final int PUBMED_PMID_COLUMN = 32;
    private static final int SAMPLE_SOURCE_COLUMN = 34;
    private static final int TUMOUR_ORIGIN_COLUMN = 35;

    private static final String HGVS_INSERTION_TAG = "ins";
    private static final String HGVS_SNV_CHANGE_SYMBOL = ">";
    private static final String HGVS_DELETION_TAG = "del";
    private static final String HGVS_DUPLICATION_TAG = "dup";
    private static final String CHROMOSOME = "CHR";
    private static final String START = "START";
    private static final String END = "END";
    private static final String REF = "REF";
    private static final String ALT = "ALT";
    private int invalidPositionLines = 0;
    private int invalidSubstitutionLines = 0;
    private int invalidDeletionLines = 0;
    private int invalidInsertionLines = 0;
    private int invalidDuplicationLines = 0;
    private int invalidMutationCDSOtherReason = 0;

    private Pattern mutationGRCh37GenomePositionPattern  = Pattern.compile("(?<" + CHROMOSOME + ">\\S+):(?<" + START + ">\\d+)-(?<"
            + END + ">\\d+)");
    private Pattern snvPattern = Pattern.compile("c\\.\\d+((\\+|\\-|_)\\d+)?(?<" + REF + ">(A|C|T|G)+)>(?<" + ALT + ">(A|C|T|G)+)");

    private static Logger logger = LoggerFactory.getLogger(CosmicIndexerCallback.class);

    public CosmicIndexerCallback(RocksDB rdb, ClinicalIndexer clinicalIndexer) {
        this.rdb = rdb;
        this.clinicalIndexer = clinicalIndexer;
    }

    @Override
    public boolean processEvidenceEntries(SequenceLocation sequenceLocation, List<EvidenceEntry> evidenceEntries) {
        try {
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
        if (normalisedVariantStringList != null) {
            for (String normalisedVariantString : normalisedVariantStringList) {
                VariantAnnotation variantAnnotation = clinicalIndexer.getVariantAnnotation(normalisedVariantString.getBytes());
                List<EvidenceEntry> mergedEvidenceEntries = mergeEvidenceEntries(evidenceEntries);
                clinicalIndexer.addHaplotypeProperty(mergedEvidenceEntries, normalisedVariantStringList);
                // IMPORTANT: COSMIC must be indexed first because of the next line !!!
                variantAnnotation.setTraitAssociation(mergedEvidenceEntries);
                rdb.put(normalisedVariantString.getBytes(), clinicalIndexer.jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            }
            return true;
        }
        return false;
    }

    private List<EvidenceEntry> mergeEvidenceEntries(List<EvidenceEntry> evidenceEntries) {
        List<EvidenceEntry> mergedEvidenceEntries = new ArrayList<>();
        if (evidenceEntries.size() > 0) {
            mergedEvidenceEntries.add(evidenceEntries.get(0));
            // For each evidence entry ...
            for (int i = 1; i < evidenceEntries.size(); i++) {
                boolean merged = false;
                // ... check if it matches a existing evidence entry
                for (EvidenceEntry mergedEvidenceEntry : mergedEvidenceEntries) {
                    if (sameSomaticDocument(evidenceEntries.get(i), mergedEvidenceEntry)) {
                        // Merge Transcripts
                        if (mergedEvidenceEntry.getGenomicFeatures() != null) {
                            if (evidenceEntries.get(i).getGenomicFeatures() != null) {
                                for (GenomicFeature newGenomicFeature : evidenceEntries.get(i).getGenomicFeatures()) {
                                    if (newGenomicFeature.getFeatureType().equals(FeatureTypes.transcript)) {
                                        boolean found = false;
                                        for (GenomicFeature feature : mergedEvidenceEntry.getGenomicFeatures()) {
                                            if (feature.getXrefs().get(clinicalIndexer.SYMBOL)
                                                    .equals(newGenomicFeature.getXrefs().get(clinicalIndexer.SYMBOL))) {
                                                found = true;
                                            }
                                        }
                                        if (!found) {
                                            mergedEvidenceEntry.getGenomicFeatures().add(newGenomicFeature);
                                        }
                                    }
                                }
                            }
                        } else {
                            mergedEvidenceEntry.setGenomicFeatures(evidenceEntries.get(i).getGenomicFeatures());
                        }

                        // Merge Bibliography
                        // There are cosmic records which share all the fields but the bibliography. In some occassions (COSM12600)
                        // the redundancy is such that the document becomes much bigger than 16MB and cannot be loaded into MongoDB.
                        // This merge reduces redundancy.
                        if (mergedEvidenceEntry.getBibliography() != null) {
                            if (evidenceEntries.get(i).getBibliography() != null) {
                                Set<String> bibliographySet = new HashSet<>(mergedEvidenceEntry.getBibliography());
                                bibliographySet.addAll(new HashSet<>(evidenceEntries.get(i).getBibliography()));
                                mergedEvidenceEntry.setBibliography(new ArrayList<>(bibliographySet));
                            }
                        } else {
                            mergedEvidenceEntry.setBibliography(evidenceEntries.get(i).getBibliography());
                        }

                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    mergedEvidenceEntries.add(evidenceEntries.get(i));
                }
            }
        }

        return mergedEvidenceEntries;
    }

    public boolean sameSomaticDocument(EvidenceEntry evidenceEntry1, EvidenceEntry evidenceEntry2) {
        if (evidenceEntry1 == evidenceEntry2) {
            return true;
        }
        if (evidenceEntry2 == null || evidenceEntry1.getClass() != evidenceEntry2.getClass()) {
            return false;
        }

        if (evidenceEntry1.getSource() != null ? !evidenceEntry1.getSource().equals(evidenceEntry2.getSource())
                : evidenceEntry2.getSource() != null) {
            return false;
        }
        if (evidenceEntry1.getSomaticInformation() != null
                ? !evidenceEntry1.getSomaticInformation().equals(evidenceEntry2.getSomaticInformation())
                : evidenceEntry2.getSomaticInformation() != null) {
            return false;
        }
        if (evidenceEntry1.getId() != null
                ? !evidenceEntry1.getId().equals(evidenceEntry2.getId()) : evidenceEntry2.getId() != null) {
            return false;
        }
        if (evidenceEntry1.getAlleleOrigin() != null
                ? !evidenceEntry1.getAlleleOrigin().equals(evidenceEntry2.getAlleleOrigin())
                : evidenceEntry2.getAlleleOrigin() != null) {
            return false;
        }
        if (evidenceEntry1.getGenomicFeatures() != null
                ? !evidenceEntry1.getGenomicFeatures().equals(evidenceEntry2.getGenomicFeatures())
                : evidenceEntry2.getGenomicFeatures() != null) {
            return false;
        }
        if (evidenceEntry1.getAdditionalProperties() != null
                ? !evidenceEntry1.getAdditionalProperties().equals(evidenceEntry2.getAdditionalProperties())
                : evidenceEntry2.getAdditionalProperties() != null) {
            return false;
        }

        return true;
    }

    private VariantType getVariantType(String mutationCds) {
        if (mutationCds.contains(HGVS_SNV_CHANGE_SYMBOL)) {
            return VariantType.SNV;
        } else if (mutationCds.contains(HGVS_DELETION_TAG)) {
            return VariantType.DELETION;
        } else if (mutationCds.contains(HGVS_INSERTION_TAG)) {
            return VariantType.INSERTION;
        } else if (mutationCds.contains(HGVS_DUPLICATION_TAG)) {
            return VariantType.DUPLICATION;
        } else {
            return null;
        }
    }

    public SequenceLocation parseLocation(String[] fields) {
        SequenceLocation sequenceLocation = null;
        String locationString = fields[25];
        if (StringUtils.isNotEmpty(locationString)) {
            Matcher matcher = mutationGRCh37GenomePositionPattern.matcher(locationString);
            if (matcher.matches()) {
                sequenceLocation = new SequenceLocation();
                sequenceLocation.setChromosome(getCosmicChromosome(matcher.group(CHROMOSOME)));
                sequenceLocation.setStrand(fields[26]);

                String mutationCds = fields[HGVS_COLUMN];
                VariantType variantType = getVariantType(mutationCds);
                if (VariantType.INSERTION.equals(variantType)) {
                    sequenceLocation.setEnd(Integer.parseInt(matcher.group(START)));
                    sequenceLocation.setStart(Integer.parseInt(matcher.group(END)));
                } else {
                    sequenceLocation.setStart(Integer.parseInt(matcher.group(START)));
                    sequenceLocation.setEnd(Integer.parseInt(matcher.group(END)));
                }
            }
        }
        if (sequenceLocation == null) {
            this.invalidPositionLines++;
        }
        return sequenceLocation;
    }

    private String getCosmicChromosome(String chromosome) {
        switch (chromosome) {
            case "23":
                return "X";
            case "24":
                return "Y";
            case "25":
                return "MT";
            default:
                return chromosome;
        }
    }
}
