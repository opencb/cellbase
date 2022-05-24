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

import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.common.clinical.gwas.Gwas;
import org.opencb.cellbase.core.common.clinical.gwas.GwasStudy;
import org.opencb.cellbase.core.common.clinical.gwas.GwasTest;
import org.opencb.cellbase.core.common.clinical.gwas.GwasTrait;
import org.opencb.cellbase.lib.EtlCommons;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;

public class GwasIndexer extends ClinicalIndexer {

    private static final int REF = 0;
    private static final int ALT = 1;

    private final Path gwasFile;
    private final Path dbSnpTabixFile;
    private final String assembly;

    private int invalidStartRecords;
    private int invalidChromosome;
    private int gwasLinesNotFoundInDbsnp;

    public GwasIndexer(Path gwasFile, Path dbSnpTabixFile, Path genomeSequenceFilePath, String assembly, RocksDB rdb) throws IOException {
        super(genomeSequenceFilePath);

        this.gwasFile = gwasFile;
        this.dbSnpTabixFile = dbSnpTabixFile;
        this.assembly = assembly;
        this.rdb = rdb;
    }

    public void index() throws RocksDBException, IOException {
        logger.info("Parsing GWAS catalog file ...");

        try {
            logger.info("Opening GWAS catalog file " + gwasFile + " ...");
            BufferedReader inputReader = new BufferedReader(new FileReader(gwasFile.toFile()));

            logger.info("Ignoring GWAS catalog file header line ...");
            inputReader.readLine();

            Map<Variant, Gwas> variantMap = new HashMap<>();
            logger.info("Opening dbSNP tabix file " + dbSnpTabixFile + " ...");
            TabixReader dbsnpTabixReader = new TabixReader(dbSnpTabixFile.toString());

            long processedGwasLines = 0;

            logger.info("Parsing gwas file ...");
            String line;
            while ((line = inputReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    processedGwasLines++;
                    if (processedGwasLines % 10000 == 0) {
                        logger.info("{} lines parsed", processedGwasLines);
                    }
                    Gwas gwasRecord = buildGwasObject(line.split("\t"), dbsnpTabixReader);
                    if (gwasRecord != null) {
                        addGwasRecordToVariantMap(variantMap, gwasRecord);
                    }
                }
            }
            dbsnpTabixReader.close();

            logger.info("Updating clinical variant annotation...");
            for (Gwas gwas : variantMap.values()) {
                // First of all, check if there are traits for that GWAS catalog entry
                List<HeritableTrait> traits = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(gwas.getStudies())) {
                    for (GwasStudy gwasStudy : gwas.getStudies()) {
                        if (CollectionUtils.isNotEmpty(gwasStudy.getTraits())) {
                            for (GwasTrait gwasTrait : gwasStudy.getTraits()) {
                                traits.add(new HeritableTrait(gwasTrait.getDiseaseTrait(), null));
                            }
                        }
                    }
                }
                if (CollectionUtils.isEmpty(traits)) {
                    // No traits, nothing to do
                    continue;
                }
                if (traits.size() > 1) {
                    System.out.println("======================================>>>> size = " + traits.size());
                    System.out.println(gwas.toJson());
                }

                Variant variant = new Variant(gwas.getChromosome(), gwas.getStart(), gwas.getEnd(), gwas.getReference(),
                        gwas.getAlternate());
                variant.setNames(Collections.singletonList(gwas.getSnpId()));

                EvidenceEntry entry = new EvidenceEntry();

                // ID
                if (CollectionUtils.isNotEmpty(variant.getNames())) {
                    entry.setId(variant.getNames().get(0));
                }

                // Source
                entry.setSource(new EvidenceSource(EtlCommons.GWAS_DATA, "1.0.2", ""));

                // Assembly
                entry.setAssembly(assembly);

                // Heritable traits
                entry.setHeritableTraits(traits);

                // Ethinicity
                entry.setEthnicity(EthnicCategory.Z);

                // Additional properties
                entry.setAdditionalProperties(Collections.singletonList(new Property(null, "GWAS", gwas.toJson())));

                // Set variant annotation
                if (variant.getAnnotation() == null) {
                    variant.setAnnotation(new VariantAnnotation());
                }
                variant.getAnnotation().setTraitAssociation(Collections.singletonList(entry));

                // updateRocksDB may fail (false) if normalisation process fails
                boolean success = updateRocksDB(variant);
                if (success) {
                    numberIndexedRecords++;
                    if (processedGwasLines % 10000 == 0) {
                        logger.info("{} records updated", numberIndexedRecords);
                    }
                }
            }
            logger.info("Done");

            this.printSummary(processedGwasLines, variantMap);
        } catch (RocksDBException | IOException  e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing HGMD");
            throw e;
        } finally {
            logger.info("Done");

//            this.printSummary();
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
                VariantAnnotation variantAnnotation = getVariantAnnotation(variant.toString().getBytes());

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

    private Gwas buildGwasObject(String[] values, TabixReader dbsnpTabixReader) {
        Gwas gwas = null;
        Integer start = parseStart(values);
        if (start != null) {
            Integer end = start;

            String chromosome = parseChromosome(values[11]);
            if (chromosome != null) {
                String snpId = values[21].trim();
                String[] refAndAlt = getRefAndAltFromDbsnp(chromosome, start, snpId, dbsnpTabixReader);
                if (refAndAlt != null) {
                    gwas = new Gwas(chromosome, start, end, refAndAlt[REF], refAndAlt[ALT], values[10], values[13], values[14],
                            values[15], values[16], values[17], values[18], values[19], values[20], snpId, values[22], values[23],
                            values[24], values[25], parseFloat(values[26]), values[33]);
                    addGwasStudy(values, gwas);

                } else {
                    System.out.println("variant not found in dbSNP = " + snpId);
                    System.out.println(StringUtils.join(values, "\t\t\t"));
                    gwasLinesNotFoundInDbsnp++;
                }
            } else {
                System.out.println("invalid chromosome = " + chromosome);
                System.out.println(StringUtils.join(values, "\t\t\t"));
                invalidChromosome++;
            }
        } else {
            System.out.println("invalid start = " + start);
            System.out.println(StringUtils.join(values, "\t\t\t"));
            invalidStartRecords++;
        }

        return gwas;
    }

    private Integer parseStart(String[] values) {
        Integer start = null;
        if (NumberUtils.isDigits(values[12])) {
            start = Integer.parseInt(values[12]);
        }
        return start;
    }

    private String parseChromosome(String chromosome) {
        String transformedChromosome = null;
        if (!chromosome.isEmpty()) {
            switch (chromosome) {
                case "23":
                    transformedChromosome = "X";
                    break;
                case "24":
                    transformedChromosome = "Y";
                    break;
                case "25":
                    transformedChromosome = "MT";
                    break;
                default:
                    transformedChromosome = chromosome;
            }
        }
        return transformedChromosome;
    }

    private Float parseFloat(String value) {
        Float riskAlleleFrequency = null;
        if (NumberUtils.isNumber(value)) {
            riskAlleleFrequency = Float.parseFloat(value);
        }
        return riskAlleleFrequency;
    }

    private String[] getRefAndAltFromDbsnp(String chromosome, Integer start, String snpId, TabixReader dbsnpTabixReader) {
        String[] refAndAlt = null;

        TabixReader.Iterator dbsnpIterator = dbsnpTabixReader.query(chromosome + ":" + start + "-" + start);
        try {
            String dbSnpRecord = dbsnpIterator.next();
            boolean found = false;
            while (dbSnpRecord != null && !found) {
                String[] dbsnpFields = dbSnpRecord.split("\t");

                if (snpId.equalsIgnoreCase(dbsnpFields[2])) {
                    refAndAlt = new String[2];
                    refAndAlt[REF] = dbsnpFields[3];
                    refAndAlt[ALT] = dbsnpFields[4];
                    found = true;
                }

                dbSnpRecord = dbsnpIterator.next();
            }
        } catch (IOException e) {
            logger.warn("Error reading position '" + chromosome + ":" + start + "' in dbSNP: " + e.getMessage());
        }

        return refAndAlt;
    }

    private void addGwasStudy(String[] values, Gwas gwas) {
        // Add the study values
        GwasStudy study = new GwasStudy(values[1], values[2], values[3], values[4], values[5], values[6], values[8], values[9], values[32]);
        addGwasTraitToStudy(values, study);
        gwas.addStudy(study);
    }

    private void addGwasTraitToStudy(String[] values, GwasStudy study) {
        // Add the trait values
        GwasTrait trait = new GwasTrait(values[7], values[0]);
        addGwasTestToTrait(values, trait);
        study.addTrait(trait);
    }

    private void addGwasTestToTrait(String[] values, GwasTrait trait) {
        // Add the test values
        Float pValue = parseFloat(values[27]);
        Float pValueMlog = parseFloat(values[28]);
        GwasTest test = new GwasTest(pValue, pValueMlog, values[29], values[30], values[31]);
        trait.addTest(test);
    }

    private void printSummary(long processedGwasLines, Map<Variant, Gwas> variantMap) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(processedGwasLines) + " gwas lines");
        logger.info("Serialized " + formatter.format(variantMap.size()) + " variants");
        logger.info(formatter.format(gwasLinesNotFoundInDbsnp) + " gwas lines ignored because variant not found in dbsnp");
        if (invalidStartRecords != 0) {
            logger.info(formatter.format(invalidStartRecords) + " gwas lines ignored because have no valid 'start' value");
        }
        if (invalidChromosome != 0) {
            logger.info(formatter.format(invalidChromosome) + " gwas lines ignored because have no valid chromosome");
        }
    }

    private void addGwasRecordToVariantMap(Map<Variant, Gwas> variantMap, Gwas gwasRecord) {
        String[] alternates = gwasRecord.getAlternate().split(",");
        for (int i = 0; i < alternates.length; i++) {
            String alternate = alternates[i];
            Variant variantKey = new Variant(
                    gwasRecord.getChromosome(), gwasRecord.getStart(), gwasRecord.getEnd(), gwasRecord.getReference(), alternate);
            if (variantMap.containsKey(variantKey)) {
                updateGwasEntry(variantMap, gwasRecord, variantKey);
            } else {
                // if a gwas record has several alternatives, it has to be cloned to avoid side effects (set gwasRecord
                // alternative would update the previous instance of gwas record saved in the 'variantMap')
                gwasRecord = cloneGwasRecordIfNecessary(gwasRecord, i);
                gwasRecord.setAlternate(alternate);
                variantMap.put(variantKey, gwasRecord);
            }
        }
    }

    private Gwas cloneGwasRecordIfNecessary(Gwas gwasRecord, int i) {
        if (i > 0) {
            gwasRecord = new Gwas(gwasRecord);
        }
        return gwasRecord;
    }

    private void updateGwasEntry(Map<Variant, Gwas> variantMap, Gwas gwasVO, Variant gwasKey) {
        Gwas gwas = variantMap.get(gwasKey);
        gwas.addStudies(gwasVO.getStudies());
        variantMap.put(gwasKey, gwas);
    }
}
