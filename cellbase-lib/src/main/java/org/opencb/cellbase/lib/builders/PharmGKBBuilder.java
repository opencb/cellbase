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
import org.opencb.biodata.models.core.PharmaChemical;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.exec.Command;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opencb.cellbase.lib.EtlCommons.PHARMGKB_DATA;
import static org.opencb.cellbase.lib.EtlCommons.PHARMGKB_NAME;

public class PharmGKBBuilder extends CellBaseBuilder {

    private Path inputDir;
    private CellBaseFileSerializer fileSerializer;

    private static final String CHEMICALS_FILENAME = "chemicals";

    public PharmGKBBuilder(Path inputDir, CellBaseFileSerializer serializer) {
        super(serializer);

        this.fileSerializer = serializer;
        this.inputDir = inputDir;

        logger = LoggerFactory.getLogger(PharmGKBBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        // Check input folder
        FileUtils.checkPath(inputDir);

        // PharmGKB
        logger.info("Parsing PharmGKB files...");

        Path inPharmGKBDir = inputDir.resolve(PHARMGKB_DATA);
        FileUtils.checkPath(inPharmGKBDir);

        unzip(inPharmGKBDir, CHEMICALS_FILENAME + ".zip", inPharmGKBDir.resolve(CHEMICALS_FILENAME));

        Map<String, PharmaChemical> chemicalsMap = parseChemicals(inPharmGKBDir.resolve(CHEMICALS_FILENAME)
                .resolve(CHEMICALS_FILENAME + ".tsv"));
        for (Map.Entry<String, PharmaChemical> entry : chemicalsMap.entrySet()) {
            logger.info(entry.getKey() + " -> " + entry.getValue());
        }
        logger.info("Chemical mapsize = " + chemicalsMap.size());

        logger.info("Parsing PharmGKB files finished.");
    }

    private Map<String, PharmaChemical> parseChemicals(Path chemicalsFile) throws IOException {
        Map<String, PharmaChemical> chemicalsMap = new HashMap<>();
        try (BufferedReader br = FileUtils.newBufferedReader(chemicalsFile)) {
            // Skip first line, i.e. the header line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                // 0                      1     2              3            4              5    6                7      8
                // PharmGKB Accession Id  Name  Generic Names  Trade Names  Brand Mixtures Type Cross-references SMILES InChI
                // 9                10                  11                        12                       13            14
                // Dosing Guideline External Vocabulary Clinical Annotation Count Variant Annotation Count Pathway Count VIP Count
                // 15                        16                             17                           18
                // Dosing Guideline Sources  Top Clinical Annotation Level  Top FDA Label Testing Level  Top Any Drug Label Testing Level
                // 19                     20                 21                  22               23
                // Label Has Dosing Info  Has Rx Annotation  RxNorm Identifiers  ATC Identifiers  PubChem Compound Identifiers
                PharmaChemical pharmaChemical = new PharmaChemical()
                        .setId(fields[0])
                        .setSource(PHARMGKB_NAME)
                        .setName(fields[1]);

                if (StringUtils.isNotEmpty(fields[5])) {
                    pharmaChemical.setTypes(Arrays.stream(fields[5].split(",")).map(s -> s.trim()).collect(Collectors.toList()));
                }

                chemicalsMap.put(pharmaChemical.getId(), pharmaChemical);
            }
        }
        return chemicalsMap;
    }

    private void unzip(Path inPath, String zipFilename, Path outPath) throws CellBaseException {
        if (!outPath.toFile().exists()) {
            if (!inPath.resolve(zipFilename).toFile().exists()) {
                throw new CellBaseException("PharmGKB file '" + zipFilename + ".zip found");
            }
            logger.info("Uncompressing {} into {}", zipFilename, outPath);
            Command cmd = new Command("unzip -d " + outPath + " " + inPath.resolve(zipFilename));
            cmd.run();
        }
    }
}
