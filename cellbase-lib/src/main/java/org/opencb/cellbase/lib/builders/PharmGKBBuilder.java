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
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.biodata.models.pharma.PharmaClinicalAllele;
import org.opencb.biodata.models.pharma.PharmaClinicalAnnotation;
import org.opencb.biodata.models.pharma.PharmaClinicalEvidence;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.exec.Command;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencb.cellbase.lib.EtlCommons.PHARMGKB_DATA;
import static org.opencb.cellbase.lib.EtlCommons.PHARMGKB_NAME;

public class PharmGKBBuilder extends CellBaseBuilder {

    private Path inputDir;
    private CellBaseFileSerializer fileSerializer;

    private static final String CHEMICALS_BASENAME = "chemicals";
    private static final String CHEMICALS_ZIP_FILENAME = "chemicals.zip";
    private static final String CHEMICALS_TSV_FILENAME = "chemicals.tsv";
    private static final String CLINICAL_ANNOTATIONS_BASENAME = "clinicalAnnotations";
    private static final String CLINICAL_ANNOTATIONS_ZIP_FILENAME = "clinicalAnnotations.zip";
    private static final String CLINICAL_ANNOTATIONS_TSV_FILENAME = "clinical_annotations.tsv";
    private static final String CLINICAL_ANN_ALLELES_TSV_FILENAME = "clinical_ann_alleles.tsv";
    private static final String CLINICAL_ANN_EVIDENCE_TSV_FILENAME = "clinical_ann_evidence.tsv";

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

        // Chemicals
        unzip(inPharmGKBDir, CHEMICALS_ZIP_FILENAME, Collections.singletonList(CHEMICALS_TSV_FILENAME),
                inPharmGKBDir.resolve(CHEMICALS_BASENAME));
        Map<String, PharmaChemical> chemicalsMap = parseChemicals(inPharmGKBDir.resolve(CHEMICALS_BASENAME)
                .resolve(CHEMICALS_TSV_FILENAME));
        logger.info("Chemical mapsize = " + chemicalsMap.size());

        // Clinical annotation
        unzip(inPharmGKBDir, CLINICAL_ANNOTATIONS_ZIP_FILENAME, Arrays.asList(CLINICAL_ANNOTATIONS_TSV_FILENAME,
                        CLINICAL_ANN_ALLELES_TSV_FILENAME, CLINICAL_ANN_EVIDENCE_TSV_FILENAME),
                inPharmGKBDir.resolve(CLINICAL_ANNOTATIONS_BASENAME));
        parseClinicalAnnotations(inPharmGKBDir.resolve(CLINICAL_ANNOTATIONS_BASENAME), chemicalsMap);

        for (Map.Entry<String, PharmaChemical> entry : chemicalsMap.entrySet()) {
            logger.info(entry.getKey() + " -> " + entry.getValue());
        }

        logger.info("Parsing PharmGKB files finished.");
    }

    private void parseClinicalAnnotations(Path annPath, Map<String, PharmaChemical> chemicalsMap) throws IOException {
        Map<String, PharmaClinicalAnnotation> annotationMap = new HashMap<>();
        Map<String, List<String>> drugToAnnotationIdMap = new HashMap<>();

        // clinical_annotations.tsv
        try (BufferedReader br = FileUtils.newBufferedReader(annPath.resolve(CLINICAL_ANNOTATIONS_TSV_FILENAME))) {
            // Skip first line, i.e. the header line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                // 0                       1                   2     3                  4               5                6
                // Clinical Annotation ID  Variant/Haplotypes  Gene  Level of Evidence  Level Override  Level Modifiers  Score
                // 7                   8           9               10       11            12                                13
                // Phenotype Category  PMID Count  Evidence Count  Drug(s)  Phenotype(s)  Latest History Date (YYYY-MM-DD)  URL
                // 14
                // Specialty Population
                PharmaClinicalAnnotation clinicalAnnotation = new PharmaClinicalAnnotation()
                        .setVariantId(fields[1])
                        .setGene(fields[2])
                        .setLevelOfEvidence(fields[3])
                        .setLevelOverride(fields[4])
                        .setLevelModifiers(fields[5])
                        .setScore(fields[6])
                        .setPhenotypeCategory(fields[7])
                        .setLatestUpdateDate(fields[12])
                        .setUrl(fields[13])
                        .setSpecialtyPopulation(fields[14]);

                if (StringUtils.isNotEmpty(fields[11])) {
                    clinicalAnnotation.setPhenotypes(stringFieldToList(fields[11]));
                }

                // Sanity check
                if (StringUtils.isEmpty(fields[0])) {
                    logger.warn("Clinical annotation ID is missing in clinical annotations line: {}", line);
                    continue;
                }
                // Add the annotation to the annotationMap by annotation ID
                annotationMap.put(fields[0], clinicalAnnotation);

                // Process the drug names to update the drugToAnnotationId map
                if (StringUtils.isNotEmpty(fields[10])) {
                    // Drugs are separated by ;
                    String[] drugs = fields[10].split(";");
                    for (String drug : drugs) {
                        if (!drugToAnnotationIdMap.containsKey(drug)) {
                            // Add the drug to the map
                            drugToAnnotationIdMap.put(drug, new ArrayList<>());
                        }
                        // Add the annotation ID to that drug
                        drugToAnnotationIdMap.get(drug).add(fields[0]);
                    }
                }
            }
        }

        // clinical_ann_evidence.tsv
        try (BufferedReader br = FileUtils.newBufferedReader(annPath.resolve(CLINICAL_ANN_EVIDENCE_TSV_FILENAME))) {
            // Skip first line, i.e. the header line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                // 0                       1            2              3             4     5        6
                // Clinical Annotation ID  Evidence ID  Evidence Type  Evidence URL  PMID  Summary  Score
                PharmaClinicalEvidence evidence = new PharmaClinicalEvidence()
                        .setType(fields[2])
                        .setUrl(fields[3])
                        .setPmid(fields[4])
                        .setSummary(fields[5])
                        .setScore(fields[6]);

                // Sanity check
                if (StringUtils.isEmpty(fields[0])) {
                    logger.warn("Clinical annotation ID is missing in clinical annotation evidence line: {}", line);
                    continue;
                }
                if (annotationMap.containsKey(fields[0])) {
                    annotationMap.get(fields[0]).getEvidences().add(evidence);
                } else {
                    logger.warn("Clinical annotation ID {} from clinical annotation evidence not found in clinical annotations", fields[0]);
                }
            }
        }

        // clinical_ann_alleles.tsv
        try (BufferedReader br = FileUtils.newBufferedReader(annPath.resolve(CLINICAL_ANN_ALLELES_TSV_FILENAME))) {
            // Skip first line, i.e. the header line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                // 0                       1                2                3
                // Clinical Annotation ID  Genotype/Allele  Annotation Text  Allele Function
                PharmaClinicalAllele clinicalAllele = new PharmaClinicalAllele()
                        .setAllele(fields[1])
                        .setAnnotation(fields[2])
                        .setFunction(fields[3]);

                // Sanity check
                if (StringUtils.isEmpty(fields[0])) {
                    logger.warn("Clinical annotation ID is missing in clinical annotation alleles line: {}", line);
                    continue;
                }
                if (annotationMap.containsKey(fields[0])) {
                    annotationMap.get(fields[0]).getAlleles().add(clinicalAllele);
                } else {
                    logger.warn("Clinical annotation ID {} from clinical annotation allele not found in clinical annotations", fields[0]);
                }
            }
        }

        // Update chemicals map by adding the clinical annotation
        for (String drug : drugToAnnotationIdMap.keySet()) {
            if (chemicalsMap.containsKey(drug)) {
                for (String annotationId : drugToAnnotationIdMap.get(drug)) {
                    chemicalsMap.get(drug).getAnnotations().add(annotationMap.get(annotationId));
                }
            }
        }
    }

    private Map<String, PharmaChemical> parseChemicals(Path chemicalsFile) throws IOException {
        Map<String, PharmaChemical> chemicalsMap = new HashMap<>();
        try (BufferedReader br = FileUtils.newBufferedReader(chemicalsFile)) {
            // Skip first line, i.e. the header line
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t", -1);
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
                        .setName(fields[1])
                        .setSmiles(fields[7])
                        .setInChI(fields[8]);

                // Generic Names
                if (StringUtils.isNotEmpty(fields[2])) {
                    pharmaChemical.setGenericNames(stringFieldToList(fields[2]));
                }

                // Trade Names
                if (StringUtils.isNotEmpty(fields[3])) {
                    pharmaChemical.setTradeNames(stringFieldToList(fields[3]));
                }

                // Brand Mixtures
                if (StringUtils.isNotEmpty(fields[4])) {
                    pharmaChemical.setTradeMixtures(stringFieldToList(fields[4]));
                }

                // Types
                if (StringUtils.isNotEmpty(fields[5])) {
                    pharmaChemical.setTypes(Arrays.stream(fields[5].split(",")).map(s -> s.trim()).collect(Collectors.toList()));
                }

                chemicalsMap.put(pharmaChemical.getName(), pharmaChemical);
            }
        }
        return chemicalsMap;
    }

    private List<String> stringFieldToList(String field) {
        if (field.startsWith("\"")) {
            // FIXME: double double quotes
            return Arrays.stream(field.replace("\", \"", "\",\"").split("\",\"")).map(s -> s.trim()).collect(Collectors.toList());
        } else if (field.contains(", ")) {
            return Arrays.stream(field.replace(", ", ",").split(",")).map(s -> s.trim()).collect(Collectors.toList());
        } else {
            return Collections.singletonList(field);
        }
    }

    private void unzip(Path inPath, String zipFilename, List<String> outFilenames, Path outPath) throws CellBaseException {
        if (!outPath.toFile().exists()) {
            if (!inPath.resolve(zipFilename).toFile().exists()) {
                throw new CellBaseException("PharmGKB file '" + zipFilename + "' not found");
            }
            logger.info("Unzipping {} into {}", zipFilename, outPath);
            Command cmd = new Command("unzip -d " + outPath + " " + inPath.resolve(zipFilename));
            cmd.run();

            for (String outFilename : outFilenames) {
                if (!outPath.resolve(outFilename).toFile().exists()) {
                    throw new CellBaseException("After unzipping the PharmGKB file '" + outFilename + "' not found");
                }
            }
        }
    }
}
