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
import org.apache.commons.collections4.MapUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantFileMetadata;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.biodata.models.variant.metadata.VariantStudyMetadata;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.cellbase.lib.EtlCommons;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by jtarraga on 23/02/22.
 */
public class HGMDIndexer extends ClinicalIndexer {
    private final Path hgmdFile;
    private final String assembly;

    public HGMDIndexer(Path hgmdFile, boolean normalize, Path genomeSequenceFilePath, String assembly, RocksDB rdb)
            throws IOException {
        super(genomeSequenceFilePath);
        this.rdb = rdb;
        this.assembly = assembly;
        this.hgmdFile = hgmdFile;
        this.normalize = normalize;
    }

    public void index() throws RocksDBException, IOException {
        logger.info("Parsing HGMD file ...");

        try {
            VariantStudyMetadata metadata = new VariantFileMetadata(null, hgmdFile.toString()).toVariantStudyMetadata("study");
            VariantVcfHtsjdkReader reader = new VariantVcfHtsjdkReader(hgmdFile.toAbsolutePath(), metadata);
            for (Variant variant : reader) {
                if (variant != null) {
                    // Parse VCF INFO field containing the HGMD data, and create trait association (i.e., evidence entries)
                    parseHgmdInfo(variant);

                    boolean success = updateRocksDB(variant);
                    // updateRocksDB may fail (false) if normalisation process fails
                    if (success) {
                        numberIndexedRecords++;
                    }
                }
                totalNumberRecords++;
                if (totalNumberRecords % 1000 == 0) {
                    logger.info("{} records parsed", totalNumberRecords);
                }
            }
        } catch (RocksDBException | IOException  e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing HGMD");
            throw e;
        } finally {
            logger.info("Done");

//            this.printSummary();
        }
    }

    private void parseHgmdInfo(Variant variant) {
        if (CollectionUtils.isNotEmpty(variant.getStudies())
                && CollectionUtils.isNotEmpty(variant.getStudies().get(0).getFiles())
                && MapUtils.isNotEmpty(variant.getStudies().get(0).getFiles().get(0).getData())) {
            Map<String, String> info = variant.getStudies().get(0).getFiles().get(0).getData();

            EvidenceEntry entry = new EvidenceEntry();

            // ID
            if (CollectionUtils.isNotEmpty(variant.getNames())) {
                entry.setId(variant.getNames().get(0));
            }

            // Source
            entry.setSource(new EvidenceSource(EtlCommons.HGMD_NAME, "2020.3", "2020"));

            // Assembly
            entry.setAssembly(assembly);

            // Genomic features
            if (info.containsKey("DNA")) {
                Map<String, String> map = new HashMap<>();
                map.put("RefSeq mRNA", cleanString(info.get("DNA")));

                if (info.containsKey("GENE")) {
                    map.put("Gene name", info.get("GENE"));
                }
                if (info.containsKey("PROT")) {
                    map.put("RefSeq protein", cleanString(info.get("PROT")));
                }
                if (info.containsKey("DB")) {
                    map.put("dbSNP", info.get("DB"));
                }

                entry.setGenomicFeatures(Collections.singletonList(new GenomicFeature(FeatureTypes.transcript, null, map)));
            }

            // Heritable traits
            if (info.containsKey("PHEN")) {
                entry.setHeritableTraits(Collections.singletonList(new HeritableTrait(cleanString(info.get("PHEN")), null)));
            }

            // Ethinicity
            entry.setEthnicity(EthnicCategory.Z);

            // Additional properties
            entry.setAdditionalProperties(new ArrayList<>());
            addAdditionalProperty("CLASS", info, entry.getAdditionalProperties());
            addAdditionalProperty("MUT", info, entry.getAdditionalProperties());
            addAdditionalProperty("STRAND", info, entry.getAdditionalProperties());
            addAdditionalProperty("RANKSCORE", info, entry.getAdditionalProperties());
            addAdditionalProperty("SVTYPE", info, entry.getAdditionalProperties());
            addAdditionalProperty("END", info, entry.getAdditionalProperties());
            addAdditionalProperty("SVLEN", info, entry.getAdditionalProperties());

            if (variant.getAnnotation() == null) {
                variant.setAnnotation(new VariantAnnotation());
            }
            variant.getAnnotation().setTraitAssociation(Collections.singletonList(entry));
        }
    }

    private String cleanString(String input) {
        String output = input.replaceAll("%2C", ",");
        output = output.replaceAll("%3A", ":");
        output = output.replaceAll("%3B", ";");
        output = output.replaceAll("%3D", "=");
        output = output.replaceAll("\"", "");
        return output;
    }

    private void addAdditionalProperty(String key, Map<String, String> info, List<Property> additionalProperties) {
        if (info.containsKey(key)) {
            additionalProperties.add(new Property(null, key, info.get(key)));
        }
    }

    private boolean updateRocksDB(Variant variant) throws RocksDBException, IOException {
        // More than one variant being returned from the normalisation process would mean it's and MNV which has been
        // decomposed
        List<String> normalisedVariantStringList = getNormalisedVariantString(variant.getChromosome(),
                variant.getStart(),
                variant.getEnd(),
                variant.getReference(),
                variant.getAlternate());

        if (normalisedVariantStringList != null) {
            for (String normalisedVariantString : normalisedVariantStringList) {
                VariantAnnotation variantAnnotation = getVariantAnnotation(normalisedVariantString.getBytes());

                // Add haplotype property to all EvidenceEntry objects in variant if there are more than 1 variants in
                // normalisedVariantStringList, i.e. if this variant is part of an MNV (haplotype)
                addHaplotypeProperty(variant.getAnnotation().getTraitAssociation(), normalisedVariantStringList);

                // Add EvidenceEntry objects
                variantAnnotation.getTraitAssociation().addAll(variant.getAnnotation().getTraitAssociation());

                rdb.put(normalisedVariantString.getBytes(), jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            }

            return true;
        }
        return false;
    }
}
