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

package org.opencb.cellbase.lib.builders;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.MirnaTarget;
import org.opencb.biodata.models.core.TargetGene;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.biodata.models.variant.avro.GeneTraitAssociation;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RefSeqGeneBuilderIndexer extends GeneBuilderIndexer{

    public RefSeqGeneBuilderIndexer(Path refSeqDirectoryPath) {
        super(refSeqDirectoryPath);
    }

    public void index(Path maneFile, Path proteinFastaFile, Path cDnaFastaFile, Path geneDrugFile, Path hpoFilePath, Path disgenetFile,
                      Path miRTarBaseFile) throws IOException, RocksDBException, FileFormatException {
        indexManeMapping(maneFile, "refseq");
        indexProteinSequences(proteinFastaFile);
        indexCdnaSequences(cDnaFastaFile);
        indexDrugs(geneDrugFile);
        indexDiseases(hpoFilePath, disgenetFile);
        indexMiRTarBase(miRTarBaseFile);
    }

    private void indexDrugs(Path geneDrugFile) throws IOException, RocksDBException {
        if (geneDrugFile != null && Files.exists(geneDrugFile) && Files.size(geneDrugFile) > 0) {
            logger.info("Loading gene-drug interaction data from '{}'", geneDrugFile);
            BufferedReader br = FileUtils.newBufferedReader(geneDrugFile);

            // Skip header
            br.readLine();

            int lineCounter = 1;
            String line;
            String currentGene = "";
            List<GeneDrugInteraction> drugs = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String geneName = parts[0];
                if (currentGene.equals("")) {
                    currentGene = geneName;
                } else if (!currentGene.equals(geneName)) {
                    rocksDbManager.update(rocksdb, currentGene + DRUGS_SUFFIX, drugs);
                    drugs = new ArrayList<>();
                    currentGene = geneName;
                }

                String source = null;
                if (parts.length >= 4) {
                    source = parts[3];
                }

                String interactionType = null;
                if (parts.length >= 5) {
                    interactionType = parts[4];
                }

                String drugName = null;
                if (parts.length >= 8) {
                    // if drug name column is empty, use drug claim name instead
                    drugName = StringUtils.isEmpty(parts[7]) ? parts[6] : parts[7];
                }
                if (StringUtils.isEmpty(drugName)) {
                    // no drug name
                    continue;
                }

                String chemblId = null;
                if (parts.length >= 9) {
                    chemblId = parts[8];
                }

                List<String> publications = new ArrayList<>();
                if (parts.length >= 10 && parts[9] != null) {
                    publications = Arrays.asList(parts[9].split(","));
                }

                GeneDrugInteraction drug = new GeneDrugInteraction(
                        geneName, drugName, source, null, null, interactionType, chemblId, publications);
                drugs.add(drug);
                lineCounter++;
            }
            br.close();
            // update last gene
            rocksDbManager.update(rocksdb, currentGene + DRUGS_SUFFIX, drugs);
        } else {
            logger.warn("Gene drug file " + geneDrugFile + " not found");
            logger.warn("Ignoring " + geneDrugFile);
        }
    }

    public List<GeneDrugInteraction> getDrugs(String id) throws RocksDBException, IOException {
        String key = id + DRUGS_SUFFIX;
        return rocksDbManager.getDrugs(rocksdb, key);
    }

    private void indexDiseases(Path hpoFilePath, Path disgenetFilePath) throws IOException, RocksDBException {
        Map<String, List<GeneTraitAssociation>> geneDiseaseAssociationMap = new HashMap<>(50000);

        String line;
        if (hpoFilePath != null && hpoFilePath.toFile().exists() && Files.size(hpoFilePath) > 0) {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(hpoFilePath);
            // skip first header line
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t");
                String omimId = fields[6];
                String geneSymbol = fields[3];
                String hpoId = fields[0];
                String diseaseName = fields[1];
                GeneTraitAssociation disease =
                        new GeneTraitAssociation(omimId, diseaseName, hpoId, 0f, 0, new ArrayList<>(), new ArrayList<>(), "hpo");
                addValueToMapElement(geneDiseaseAssociationMap, geneSymbol, disease);
            }
            bufferedReader.close();
        }

        if (disgenetFilePath != null && disgenetFilePath.toFile().exists() && Files.size(disgenetFilePath) > 0) {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(disgenetFilePath);
            // skip first header line
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t");
                String diseaseId = fields[4];
                String diseaseName = fields[5];
                String score = fields[9];
                String numberOfPubmeds = fields[13].trim();
                String numberOfSNPs = fields[14];
                String source = fields[15];
                GeneTraitAssociation disease = new GeneTraitAssociation(diseaseId, diseaseName, "", Float.parseFloat(score),
                        Integer.parseInt(numberOfPubmeds), Arrays.asList(numberOfSNPs), Arrays.asList(source), "disgenet");
                addValueToMapElement(geneDiseaseAssociationMap, fields[1], disease);
            }
            bufferedReader.close();
        }

        for (Map.Entry<String, List<GeneTraitAssociation>> entry : geneDiseaseAssociationMap.entrySet()) {
            rocksDbManager.update(rocksdb, entry.getKey() + DISEASE_SUFFIX, entry.getValue());
        }
    }

    public List<GeneTraitAssociation> getDiseases(String id) throws RocksDBException, IOException {
        String key = id + DISEASE_SUFFIX;
        return rocksDbManager.getDiseases(rocksdb, key);
    }

    private void indexMiRTarBase(Path miRTarBaseFile) throws IOException, RocksDBException {
        if (miRTarBaseFile != null && Files.exists(miRTarBaseFile) && Files.size(miRTarBaseFile) > 0) {
            logger.info("Loading mirna targets from '{}'", miRTarBaseFile);
            FileInputStream file = new FileInputStream(miRTarBaseFile.toFile());
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            String currentMiRTarBaseId = null;
            String currentMiRNA = null;
            String currentGene = null;
            List<TargetGene> targetGenes = new ArrayList();
            Map<String, List<MirnaTarget>> geneToMirna = new HashMap();
            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();

                Cell cell = cellIterator.next();
                String miRTarBaseId = cell.getStringCellValue();

                // skip header
                if (miRTarBaseId.startsWith("miRTarBase")) {
                    continue;
                }

                if (currentMiRTarBaseId == null) {
                    currentMiRTarBaseId = miRTarBaseId;
                }

                cell = cellIterator.next();
                String miRNA = cell.getStringCellValue();
                if (currentMiRNA == null) {
                    currentMiRNA = miRNA;
                }

                // species
                cellIterator.next();

                cell = cellIterator.next();
                String geneName = cell.getStringCellValue();
                if (currentGene == null) {
                    currentGene = geneName;
                }

                // entrez
                cellIterator.next();
                // species
                cellIterator.next();

                if (!miRTarBaseId.equals(currentMiRTarBaseId) || !geneName.equals(currentGene)) {
                    // new entry, store current one
                    MirnaTarget miRnaTarget = new MirnaTarget(currentMiRTarBaseId, "miRTarBase", currentMiRNA,
                            targetGenes);
                    addValueToMapElement(geneToMirna, currentGene, miRnaTarget);
                    targetGenes = new ArrayList();
                    currentGene = geneName;
                    currentMiRTarBaseId = miRTarBaseId;
                    currentMiRNA = miRNA;
                }

                // experiment
                cell = cellIterator.next();
                String experiment = cell.getStringCellValue();

                // support type
                cell = cellIterator.next();
                String supportType = cell.getStringCellValue();

                // pubmeds
                cell = cellIterator.next();
                String pubmed = null;
                // seems to vary, so check both
                if (cell.getCellType().equals(CellType.NUMERIC)) {
                    pubmed = String.valueOf(cell.getNumericCellValue());
                } else {
                    pubmed = cell.getStringCellValue();
                }

                targetGenes.add(new TargetGene(experiment, supportType, pubmed));
            }

            // parse last entry
            MirnaTarget miRnaTarget = new MirnaTarget(currentMiRTarBaseId, "miRTarBase", currentMiRNA,
                    targetGenes);
            addValueToMapElement(geneToMirna, currentGene, miRnaTarget);

            for (Map.Entry<String, List<MirnaTarget>> entry : geneToMirna.entrySet()) {
                rocksDbManager.update(rocksdb, entry.getKey() + MIRTARBASE_SUFFIX, entry.getValue());
            }
        } else {
            logger.error("mirtarbase file not found");
        }
    }

    public List<MirnaTarget> getMirnaTargets(String geneName) throws RocksDBException, IOException {
        String key = geneName + MIRTARBASE_SUFFIX;
        return rocksDbManager.getMirnaTargets(rocksdb, key);
    }

    private static <T> void addValueToMapElement(Map<String, List<T>> map, String key, T value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            List<T> valueList = new ArrayList<>();
            valueList.add(value);
            map.put(key, valueList);
        }
    }

}
