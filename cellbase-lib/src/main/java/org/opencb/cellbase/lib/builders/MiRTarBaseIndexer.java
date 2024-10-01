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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opencb.biodata.models.core.MirnaTarget;
import org.opencb.biodata.models.core.TargetGene;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.lib.EtlCommons.MIRTARBASE_DATA;
import static org.opencb.cellbase.lib.builders.AbstractBuilder.PARSING_DONE_LOG_MESSAGE;
import static org.opencb.cellbase.lib.builders.AbstractBuilder.PARSING_LOG_MESSAGE;

public class MiRTarBaseIndexer {

    protected Logger logger;

    public MiRTarBaseIndexer() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Map<String, List<MirnaTarget>> index(Path miRTarBaseFile) throws IOException {
        FileUtils.checkFile(miRTarBaseFile);

        logger.info(PARSING_LOG_MESSAGE, miRTarBaseFile);

        Map<String, List<MirnaTarget>> geneToMirna = new HashMap<>();

        try (InputStream fis = new FileInputStream(miRTarBaseFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            String currentMiRTarBaseId = null;
            String currentMiRNA = null;
            String currentGene = null;
            List<TargetGene> targetGenes = new ArrayList<>();

            for (int rowNum = sheet.getFirstRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);

                // Sanity check
                if (row.getPhysicalNumberOfCells() != 9) {
                    logger.warn("Error parsing line {}: invalid number of columns {} (expected 9 columns). Line {}.",
                            rowNum + 1, row.getPhysicalNumberOfCells());
                    continue;
                }

                if (row.getCell(0).getCellType() != CellType.STRING || row.getCell(0).getStringCellValue() == null
                        || row.getCell(1).getCellType() != CellType.STRING || row.getCell(1).getStringCellValue() == null
                        || row.getCell(3).getCellType() != CellType.STRING || row.getCell(3).getStringCellValue() == null) {
                    logger.warn("Error parsing line {}: mandatory fields(miRTarBase ID, miRNA, Target Gene) are empty or wrong cell type.",
                            rowNum + 1);
                    continue;
                }

                // #0: miRTarBase ID
                Cell cell = row.getCell(0);
                String miRTarBaseId = cell.getStringCellValue();
                if (currentMiRTarBaseId == null) {
                    currentMiRTarBaseId = miRTarBaseId;
                }

                // #1: miRNA
                cell = row.getCell(1);
                String miRNA = cell.getStringCellValue();
                if (currentMiRNA == null) {
                    currentMiRNA = miRNA;
                }

                // #2: Species (miRNA)

                // #3: Target Gene
                cell = row.getCell(3);
                String geneName = cell.getStringCellValue();
                if (currentGene == null) {
                    currentGene = geneName;
                }

                // #4: Target Gene (Entrez ID)
                // #5: Species (Target Gene)

                if (!miRTarBaseId.equals(currentMiRTarBaseId) || !geneName.equals(currentGene)) {
                    // new entry, store current one
                    MirnaTarget miRnaTarget = new MirnaTarget(currentMiRTarBaseId, MIRTARBASE_DATA, currentMiRNA, targetGenes);
                    GeneBuilderIndexer.addValueToMapElement(geneToMirna, currentGene, miRnaTarget);
                    targetGenes = new ArrayList<>();
                    currentGene = geneName;
                    currentMiRTarBaseId = miRTarBaseId;
                    currentMiRNA = miRNA;
                }

                // #6: Experiments
                cell = row.getCell(6);
                String experiment = (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : null);

                // #7: Support Type
                cell = row.getCell(7);
                String supportType = (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : null);

                // #8: pubmed
                cell = row.getCell(8);
                String pubmed = new BigDecimal(cell.getNumericCellValue()).toString();

                if (StringUtils.isNotEmpty(experiment) || StringUtils.isNotEmpty(supportType) || StringUtils.isNotEmpty(pubmed)) {
                    targetGenes.add(new TargetGene(experiment, supportType, pubmed));
                }
            }

            // parse last entry
            MirnaTarget miRnaTarget = new MirnaTarget(currentMiRTarBaseId, MIRTARBASE_DATA, currentMiRNA, targetGenes);
            GeneBuilderIndexer.addValueToMapElement(geneToMirna, currentGene, miRnaTarget);

        }
        logger.info(PARSING_DONE_LOG_MESSAGE, miRTarBaseFile);

        return geneToMirna;
    }
}
