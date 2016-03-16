/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.transform;

import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.lang.math.NumberUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.common.clinical.gwas.Gwas;
import org.opencb.cellbase.core.common.clinical.gwas.GwasStudy;
import org.opencb.cellbase.core.common.clinical.gwas.GwasTest;
import org.opencb.cellbase.core.common.clinical.gwas.GwasTrait;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Luis Miguel Cruz
 * @version 1.2.3
 * @since October 08, 2014
 */
public class GwasParser extends CellBaseParser {

    private static final int REF = 0;
    private static final int ALT = 1;

    private final Path gwasFile;
    private final Path dbSnpTabixFilePath;

    private int invalidStartRecords;
    private int invalidChromosome;
    private int gwasLinesNotFoundInDbsnp;

    public GwasParser(Path gwasFile, Path dbSnpTabixFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.gwasFile = gwasFile;
        this.dbSnpTabixFilePath = dbSnpTabixFilePath;
        this.invalidStartRecords = 0;
        this.invalidChromosome = 0;
        this.gwasLinesNotFoundInDbsnp = 0;
    }

    public void parse() {
        if (Files.exists(gwasFile) && Files.exists(dbSnpTabixFilePath)) {
            try {
                logger.info("Opening gwas file " + gwasFile + " ...");
                BufferedReader inputReader = new BufferedReader(new FileReader(gwasFile.toFile()));

                logger.info("Ignoring gwas file header line ...");
                inputReader.readLine();

                Map<Variant, Gwas> variantMap = new HashMap<>();
                logger.info("Opening dbSNP tabix file " + dbSnpTabixFilePath + " ...");
                TabixReader dbsnpTabixReader = new TabixReader(dbSnpTabixFilePath.toString());

                long processedGwasLines = 0;

                logger.info("Parsing gwas file ...");
                String line;
                while ((line = inputReader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        processedGwasLines++;
                        Gwas gwasRecord = buildGwasObject(line.split("\t"), dbsnpTabixReader);
                        if (gwasRecord != null) {
                            addGwasRecordToVariantMap(variantMap, gwasRecord);
                        }
                    }
                }
                dbsnpTabixReader.close();

                logger.info("Serializing parsed variants ...");
                for (Gwas gwasOutputRecord : variantMap.values()) {
                    serializer.serialize(gwasOutputRecord);
                }
                logger.info("Done");
                this.disconnect();
                this.printSummary(processedGwasLines, variantMap);


            } catch (IOException e) {
                logger.error("Unable to parse " + gwasFile + " using dbSNP file " + dbSnpTabixFilePath + ": " + e.getMessage());
            }
        }
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
                    gwasLinesNotFoundInDbsnp++;
                }
            } else {
                invalidChromosome++;
            }
        } else {
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
