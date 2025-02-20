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

import java.io.BufferedReader;
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

//        String currentMiRTarBaseId = null;
//        String currentMiRNA = null;
//        String currentGene = null;
        List<TargetGene> targetGenes = new ArrayList<>();

        try (BufferedReader br = FileUtils.newBufferedReader(miRTarBaseFile)) {
            // Skip first line, i.e. the header line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);

                // #0: miRTarBase ID
                String miRTarBaseId = fields[0];

                // #1: miRNA
                String miRNA = fields[1];

                // #2: Species (miRNA)

                // #3: Target Gene
                String geneName = fields[3];

                // #4: Target Gene (Entrez ID)
                // #5: Species (Target Gene)

                // #6: Experiments
                String experiment = fields[6];

                // #7: Support Type
                String supportType = fields[7];

                // #8: pubmed
                String pubmed = fields[8];
                if (StringUtils.isNotEmpty(pubmed) && pubmed.contains(".")) {
                    pubmed = pubmed.split("\\.")[0];
                }

                // Add to map
                if (!geneToMirna.containsKey(geneName)) {
                    geneToMirna.put(geneName, new ArrayList<>());
                }
                List<MirnaTarget> mirnaTargets = geneToMirna.get(geneName);
                MirnaTarget target = null;
                for (MirnaTarget mirnaTarget : mirnaTargets) {
                   if (mirnaTarget.getId().equals(miRTarBaseId) && mirnaTarget.getSourceId().equals(miRNA)) {
                       target = mirnaTarget;
                       break;
                   }
                }
                if (target == null) {
                    target = new MirnaTarget(miRTarBaseId, MIRTARBASE_DATA, miRNA, new ArrayList<>());
                    geneToMirna.get(geneName).add(target);
                }
                if (StringUtils.isNotEmpty(experiment) || StringUtils.isNotEmpty(supportType) || StringUtils.isNotEmpty(pubmed)) {
                    boolean found = false;
                    for (TargetGene targetGene : target.getTargets()) {
                        if (targetGene.getExperiment().equals(experiment) && targetGene.getEvidence().equals(supportType) && targetGene.getPubmed().equals(pubmed)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        target.getTargets().add(new TargetGene(experiment, supportType, pubmed));
                    }
                }
            }
        }

        logger.info(PARSING_DONE_LOG_MESSAGE, miRTarBaseFile);

        return geneToMirna;
    }
}
