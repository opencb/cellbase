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


package org.opencb.cellbase.app.cli.admin.executors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.app.cli.admin.executors.validation.VEPVariant;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.CellBaseManagerFactory;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.lib.impl.core.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ValidationCommandExecutor extends CommandExecutor {

    private MongoDBAdaptorFactory dbAdaptorFactory;
    private AdminCliOptionsParser.ValidationCommandOptions validationCommandOptions;
    private ObjectMapper objectMapper;
    private String resultsFile;
    private String mismatchesFile, matchesFile;
    private static final String RESULTS_FILE_NAME = "cellbase_vep_comparison_results.txt";
    private static final String MISMATCHES_FILE_NAME = "cellbase_vep_comparison_mismatches.txt";
    private static final String MATCHES_FILE_NAME = "cellbase_vep_comparison_matches.txt";
    private String defaultOutputDirectory = "/tmp";
    private int vepEmpty, cbEmpty, bothEmpty, other, matches;

    public ValidationCommandExecutor(AdminCliOptionsParser.ValidationCommandOptions validationCommandOptions) {
        super(validationCommandOptions.commonOptions.logLevel, validationCommandOptions.commonOptions.conf);

        this.validationCommandOptions = validationCommandOptions;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void execute() {
        checkFilesExist();

        CellBaseManagerFactory cellbaseManagerFactory = new CellBaseManagerFactory(configuration);
        dbAdaptorFactory = new MongoDBAdaptorFactory(configuration);
        VariantAnnotationCalculator variantAnnotationCalculator = null;
        try {
            variantAnnotationCalculator = new VariantAnnotationCalculator(validationCommandOptions.species,
                    validationCommandOptions.assembly, cellbaseManagerFactory);
        } catch (CellbaseException e) {
            e.printStackTrace();
            return;
        }

        // default path
        resultsFile = defaultOutputDirectory + "/" + RESULTS_FILE_NAME;
        mismatchesFile = defaultOutputDirectory + "/" + MISMATCHES_FILE_NAME;
        matchesFile = defaultOutputDirectory + "/" + MATCHES_FILE_NAME;

        // if output is specified and valid
        if (validationCommandOptions.outputDirectory != null && !validationCommandOptions.outputDirectory.isEmpty()) {
            resultsFile = validationCommandOptions.outputDirectory + "/" + RESULTS_FILE_NAME;
            mismatchesFile = validationCommandOptions.outputDirectory + "/" + MISMATCHES_FILE_NAME;
            matchesFile = validationCommandOptions.outputDirectory + "/" + MATCHES_FILE_NAME;
        }

        try {
            BufferedWriter resultsWriter = FileUtils.newBufferedWriter(Paths.get(resultsFile));
            BufferedWriter mismatchWriter = FileUtils.newBufferedWriter(Paths.get(mismatchesFile));
            BufferedWriter matchWriter = FileUtils.newBufferedWriter(Paths.get(matchesFile));

            // header
            mismatchWriter.write("var_id\tentity_id\tvep_hgvs\tcb_hgvs\tmismatch_category\n");
            matchWriter.write("var_id\tentity_id\tvep_hgvs\tcb_hgvs\n");

            // read in all ensembl variants
            Map<String, VEPVariant> vepVariantMap = getVepVariantMap();

            // read through variant file
            BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(validationCommandOptions.inputFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] cols = line.split("\t");
                String variantId = cols[0] + ":" + cols[1] + ":" + cols[3].replace(".", "-") + ":"
                        + cols[4].replace(".", "-");
                Variant variant = Variant.parseVariant(variantId);

                // skip variant based on user input. if no input, process all
                String mutationType = validationCommandOptions.mutationType;
                if (StringUtils.isNotEmpty(mutationType)) {
                    switch (validationCommandOptions.mutationType) {
                        case "SNV":
                            if (StringUtils.isBlank(variant.getReference()) || StringUtils.isBlank(variant.getAlternate())) {
                                continue;
                            }
                            break;
                        case "INSERTION":
                        case "DELETION":
                        case "INDEL":
                            if (StringUtils.isBlank(variant.getReference())) {
                                if (!"INSERTION".equals(mutationType) && !"INDEL".equals(mutationType)) {
                                    continue;
                                }
                            } else if (StringUtils.isBlank(variant.getAlternate())) {
                                if (!"DELETION".equals(mutationType) && !"INDEL".equals(mutationType)) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            break;
                        default:
                            continue;
                    }
                }
                // query for cellbase annotation
                CellBaseDataResult<VariantAnnotation> variantAnnotationQueryResult =
                        variantAnnotationCalculator.getAnnotationByVariant(variant, getQueryOptions());
                // vep
                VEPVariant vepVariant = vepVariantMap.get(variantId);

                // compare the two annotations
                if (!variantAnnotationQueryResult.getResults().isEmpty()) {
                    VariantAnnotation variantAnnotation = variantAnnotationQueryResult.getResults().get(0);
                    compare(mismatchWriter, matchWriter, getCategory(), variantId, variantAnnotation, vepVariant);
                } else {
                    compare(mismatchWriter, matchWriter, getCategory(), variantId, null, vepVariant);
                }
            }
            bufferedReader.close();
            mismatchWriter.close();
            matchWriter.close();

            writeResultsSummary(resultsWriter);
        } catch (IOException | InterruptedException | ExecutionException | IllegalAccessException | QueryException e) {
            e.printStackTrace();
        }
    }

    private void writeResultsSummary(BufferedWriter writer) throws IOException {
        writer.write("mismatch_category\tcount\n");
        writer.write("-----------------\t-----\n");
        writer.write("vep_empty\t" + vepEmpty + "\n");
        writer.write("cb_empty\t" + cbEmpty + "\n");
        writer.write("both_empty\t" + bothEmpty + "\n");
        writer.write("other\t" + other + "\n");
        writer.write("matches\t" + matches + "\n");
        writer.close();
    }

    private void compare(BufferedWriter mismatchWriter, BufferedWriter matchWriter, String category, String variantId,
                         VariantAnnotation cbVariant,
                         VEPVariant vepVariant) throws IOException {
        if (cbVariant == null && vepVariant == null) {
            bothEmpty++;
            mismatchWriter.write(variantId + "\t\t\t\t" + "both_empty");
        }
        Map<String, String> cbHgvsMap = formatHgvs(cbVariant.getHgvs());
        if (vepVariant != null) {
            Map<String, String> vepHgvsPMap = formatHgvs(vepVariant.getProteinHgvs());
            Map<String, String> vepHgvsCMap = formatHgvs(vepVariant.getTranscriptHgvs());

            if (!"transcript".equalsIgnoreCase(category)) {
                // protein - vep
                for (Map.Entry<String, String> entry : vepHgvsPMap.entrySet()) {
                    String proteinAccession = entry.getKey();
                    String vepHgvs = entry.getValue();

                    String cbHgvs = cbHgvsMap.get(proteinAccession);
                    cbHgvsMap.remove(proteinAccession);
                    if (StringUtils.isEmpty(cbHgvs)) {
                        cbEmpty++;
                        mismatchWriter.write(variantId + "\t" + proteinAccession + "\t" + vepHgvs + "\t-\tcb_empty\n");
                    } else if (!cbHgvs.equals(vepHgvs)) {
                        other++;
                        mismatchWriter.write(variantId + "\t" + proteinAccession + "\t" + vepHgvs + "\t" + cbHgvs + "\tother\n");
                    } else {
                        matches++;
                        matchWriter.write(variantId + "\t" + proteinAccession + "\t" + vepHgvs + "\t" + cbHgvs + "\n");
                    }
                }
            }

            if (!"protein".equalsIgnoreCase(category)) {
                // transcript - vep
                for (Map.Entry<String, String> entry : vepHgvsCMap.entrySet()) {
                    String transcriptId = entry.getKey();
                    String vepHgvs = entry.getValue();

                    String cbHgvs = cbHgvsMap.get(transcriptId);
                    cbHgvsMap.remove(transcriptId);
                    if (StringUtils.isEmpty(cbHgvs)) {
                        cbEmpty++;
                        mismatchWriter.write(variantId + "\t" + transcriptId + "\t" + vepHgvs + "\t-\tcb_empty\n");
                    } else if (!cbHgvs.equals(vepHgvs)) {
                        other++;
                        mismatchWriter.write(variantId + "\t" + transcriptId + "\t" + vepHgvs + "\t" + cbHgvs + "\tother\n");
                    } else {
                        matches++;
                        matchWriter.write(variantId + "\t" + transcriptId + "\t" + vepHgvs + "\t" + cbHgvs + "\n");
                    }
                }
            }
        }

        // cb -- anything not in vep
        for (Map.Entry<String, String> entry : cbHgvsMap.entrySet()) {
            String entityId = entry.getKey();
            String cbHgvs = entry.getValue();

            // skip proteins if we are only processing transcripts
            // skip transcripts if we are only processing proteins
            if (("transcript".equalsIgnoreCase(category) && entityId.startsWith("ENSP"))
                || ("protein".equalsIgnoreCase(category) && entityId.startsWith("ENST"))) {
                continue;
            }

            vepEmpty++;
            mismatchWriter.write(variantId + "\t" + entityId + "\t-\t" + cbHgvs + "\tvep_empty\n");
        }
    }

    private Map<String, String> formatHgvs(List<String> hgvsList) {
        Map<String, String> idToHgvs = new HashMap<>();
        for (String hgvsPhrase : hgvsList) {
            if (StringUtils.isEmpty(hgvsPhrase)) {
                continue;
            }
            // e.g. ENSP00000225964.5:p.Arg882Ter
            // ENST00000262304(ENSG00000008710):c.7915C>T
            String[] parts = hgvsPhrase.split(":");
            if (parts.length != 2) {
                throw new RuntimeException("Can't parse " + hgvsPhrase);
            }
            String entityId = parts[0];
            if (entityId.contains("(")) {
                entityId = entityId.split("\\(")[0];
            }
            if (entityId.contains(".")) {
                entityId = entityId.split("\\.")[0];
            }
            String hgvsString = parts[1];
            idToHgvs.put(entityId, hgvsString);
        }
        return idToHgvs;
    }

    private QueryOptions getQueryOptions() {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.put("include", "hgvs");
        return queryOptions;
    }

    private Map<String, VEPVariant> getVepVariantMap() throws IOException {
        Map<String, VEPVariant> variants = new HashMap<>();
        BufferedReader bufferedReader = FileUtils.newBufferedReader(Paths.get(validationCommandOptions.vepFile));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            VEPVariant vepVariant = objectMapper.readValue(line, VEPVariant.class);
            variants.put(vepVariant.getVariantId(), vepVariant);
        }
        return variants;
    }

    private void checkFilesExist() {
        Path vepFile = Paths.get(validationCommandOptions.vepFile);
        if (!Files.exists(vepFile)) {
            throw new RuntimeException("Invalid VEP file: " + validationCommandOptions.vepFile);
        }
        Path vcfFile = Paths.get(validationCommandOptions.inputFile);
        if (!Files.exists(vcfFile)) {
            throw new RuntimeException("Invalid input file: " + validationCommandOptions.inputFile);
        }
    }

    private String getCategory() {
        if (validationCommandOptions.category.equalsIgnoreCase("protein")) {
            return "protein";
        } else if (validationCommandOptions.category.equalsIgnoreCase("transcript")) {
            return "transcript";
        }
        // process both types
        return null;
    }
}
