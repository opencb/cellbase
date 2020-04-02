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

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.builders.*;
import org.opencb.cellbase.lib.builders.clinical.variant.ClinicalVariantBuilder;
import org.opencb.cellbase.lib.builders.variation.VariationBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by imedina on 03/02/15.
 */
public class BuildCommandExecutor extends CommandExecutor {
    private AdminCliOptionsParser.BuildCommandOptions buildCommandOptions;

    private Path output;
    private Path buildFolder = null; // <output>/<species>_<assembly>/generated-json
    private Path downloadFolder = null; // <output>/<species>_<assembly>/download
    private boolean normalize = true;

    private File ensemblScriptsFolder;

    private boolean flexibleGTFParsing;
    private SpeciesConfiguration speciesConfiguration;

    public BuildCommandExecutor(AdminCliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.verbose,
                buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
        this.output = Paths.get(buildCommandOptions.targetDirectory);
        normalize = !buildCommandOptions.skipNormalize;

        this.ensemblScriptsFolder = new File(System.getProperty("basedir") + "/bin/ensembl-scripts/");
        this.flexibleGTFParsing = buildCommandOptions.flexibleGTFParsing;
    }


    /**
     * Parse specific 'build' command options.
     */
    public void execute() {
        try {
            // Output directory need to be created if it doesn't exist
            if (!Files.exists(output)) {
                Files.createDirectories(output);
            }

            speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, buildCommandOptions.species);
            if (speciesConfiguration == null) {
                throw new CellbaseException("Invalid species: '" + buildCommandOptions.species + "'");
            }
            SpeciesConfiguration.Assembly assembly = null;
            if (!StringUtils.isEmpty(buildCommandOptions.assembly)) {
                assembly = SpeciesUtils.getAssembly(speciesConfiguration, buildCommandOptions.assembly);
                if (assembly == null) {
                    throw new CellbaseException("Invalid assembly: '" + buildCommandOptions.assembly + "'");
                }
            } else {
                assembly = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
            }

            String spShortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
            String spAssembly = assembly.getName().toLowerCase();
            Path spFolder = output.resolve(spShortName + "_" + spAssembly);
            // <output>/<species>_<assembly>/download
            downloadFolder = output.resolve(spFolder + "/download");
            if (!Files.exists(downloadFolder)) {
                throw new CellbaseException("Download folder not found '" + spShortName + "_" + spAssembly + "/download'");
            }
            // <output>/<species>_<assembly>/generated_json
            buildFolder = output.resolve(spFolder + "/generated_json");
            makeDir(buildFolder);

            if (buildCommandOptions.data != null) {
                String[] buildOptions;
                if (buildCommandOptions.data.equals("all")) {
                    buildOptions = speciesConfiguration.getData().toArray(new String[0]);
                } else {
                    buildOptions = buildCommandOptions.data.split(",");
                }

                for (int i = 0; i < buildOptions.length; i++) {
                    String buildOption = buildOptions[i];

                    logger.info("Building '{}' data", buildOption);
                    CellBaseBuilder parser = null;
                    switch (buildOption) {
                        case EtlCommons.GENOME_INFO_DATA:
                            buildGenomeInfo();
                            break;
                        case EtlCommons.GENOME_DATA:
                            parser = buildGenomeSequence();
                            break;
                        case EtlCommons.GENE_DATA:
                            if (!buildOption.contains(EtlCommons.GENOME_DATA)) {
                                // user didn't specify, load it anyway because required to load gene
                                buildGenomeSequence();
                            }
                            parser = buildGene();
                            break;
                        case EtlCommons.VARIATION_DATA:
                            parser = buildVariation();
                            break;
                        case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                            parser = buildCadd();
                            break;
                        case EtlCommons.REGULATION_DATA:
                            parser = buildRegulation();
                            break;
                        case EtlCommons.PROTEIN_DATA:
                            parser = buildProtein();
                            break;
                        case EtlCommons.PPI_DATA:
                            parser = getInteractionParser();
                            break;
                        case EtlCommons.CONSERVATION_DATA:
                            parser = buildConservation();
                            break;
                        case EtlCommons.CLINICAL_VARIANTS_DATA:
                            parser = buildClinicalVariants();
                            break;
                        case EtlCommons.STRUCTURAL_VARIANTS_DATA:
                            parser = buildStructuralVariants();
                            break;
                        case EtlCommons.REPEATS_DATA:
                            parser = buildRepeats();
                            break;
                        case EtlCommons.OBO_DATA:
                            parser = buildObo();
                            break;
                        default:
                            logger.error("Build option '" + buildCommandOptions.data + "' is not valid");
                            break;
                    }

                    if (parser != null) {
                        try {
                            parser.parse();
                        } catch (Exception e) {
                            logger.error("Error executing 'build' command " + buildCommandOptions.data + ": " + e.getMessage(), e);
                        }
                        parser.disconnect();
                    }
                }
            }
        } catch (ParameterException e) {
            logger.error("Error parsing build command line parameters: " + e.getMessage(), e);
        } catch (IOException | CellbaseException e) {
            logger.error(e.getMessage());
        }
    }

    private CellBaseBuilder buildStructuralVariants() {
        Path structuralVariantsFolder = downloadFolder.resolve(EtlCommons.STRUCTURAL_VARIANTS_FOLDER);
        copyVersionFiles(Arrays.asList(structuralVariantsFolder.resolve(EtlCommons.DGV_VERSION_FILE)));
        Path structuralVariantsFile = structuralVariantsFolder.resolve(EtlCommons.DGV_FILE);

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, EtlCommons.STRUCTURAL_VARIANTS_JSON,
                true);
        return new DgvBuilder(structuralVariantsFile, serializer);
    }

    private CellBaseBuilder buildRepeats() {
        Path repeatsFilesDir = downloadFolder.resolve(EtlCommons.REPEATS_FOLDER);
        copyVersionFiles(Arrays.asList(repeatsFilesDir.resolve(EtlCommons.TRF_VERSION_FILE)));
        copyVersionFiles(Arrays.asList(repeatsFilesDir.resolve(EtlCommons.GSD_VERSION_FILE)));
        copyVersionFiles(Arrays.asList(repeatsFilesDir.resolve(EtlCommons.WM_VERSION_FILE)));
        // TODO: chunk size is not really used in ConvervedRegionParser, remove?
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, EtlCommons.REPEATS_JSON);
        return new RepeatsBuilder(repeatsFilesDir, serializer);
    }

    private CellBaseBuilder buildObo() {
        Path oboDir = downloadFolder.resolve(EtlCommons.OBO_DATA);
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, EtlCommons.OBO_JSON);
        return new OboBuilder(oboDir, serializer);
    }

    private void copyVersionFiles(List<Path> pathList) {
        for (Path path : pathList) {
            try {
                Files.copy(path, downloadFolder.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.warn("Version file {} not found - skipping", path.toString());
            }
        }
    }

    private void buildGenomeInfo() {
        /**
         * To get some extra info about the genome such as chromosome length or cytobands
         * we execute the following script.
         */
        try {
            String outputFileName = downloadFolder.resolve("genome_info.json").toAbsolutePath().toString();
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList("--species", speciesConfiguration.getScientificName(),
                    "--assembly", buildCommandOptions.assembly == null ? getDefaultHumanAssembly() : buildCommandOptions.assembly,
                    "-o", outputFileName,
                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()));
            if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)
                    && !speciesConfiguration.getScientificName().equals("Drosophila melanogaster")) {
                args.add("--phylo");
                args.add("no-vertebrate");
            }

            String geneInfoLogFileName = downloadFolder.resolve("genome_info.log").toAbsolutePath().toString();

            boolean downloadedGenomeInfo;
            downloadedGenomeInfo = EtlCommons.runCommandLineProcess(ensemblScriptsFolder, "./genome_info.pl", args, geneInfoLogFileName);

            if (downloadedGenomeInfo) {
                logger.info(outputFileName + " created OK");
            } else {
                logger.error("Genome info for " + speciesConfiguration.getScientificName() + " cannot be downloaded");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private CellBaseBuilder buildGenomeSequence() {
        copyVersionFiles(Collections.singletonList(downloadFolder.resolve("genome/genomeVersion.json")));
        Path fastaFile = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "genome_sequence");
        return new GenomeSequenceFastaBuilder(fastaFile, serializer);
    }

    private CellBaseBuilder buildGene() {
        Path geneFolderPath = downloadFolder.resolve("gene");
        copyVersionFiles(Arrays.asList(geneFolderPath.resolve("dgidbVersion.json"),
                geneFolderPath.resolve("ensemblCoreVersion.json"), geneFolderPath.resolve("uniprotXrefVersion.json"),
                geneFolderPath.resolve("geneExpressionAtlasVersion.json"),
                geneFolderPath.resolve("hpoVersion.json"), geneFolderPath.resolve("disgenetVersion.json"),
                geneFolderPath.resolve("gnomadVersion.json")));
        Path genomeFastaFilePath = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "gene");
        return new GeneBuilder(geneFolderPath, genomeFastaFilePath, speciesConfiguration, flexibleGTFParsing, serializer);
    }


    private CellBaseBuilder buildVariation() {
        Path variationFolderPath = downloadFolder.resolve("variation");
        copyVersionFiles(Arrays.asList(variationFolderPath.resolve("ensemblVariationVersion.json")));
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, null, true, true, true);
        return new VariationBuilder(variationFolderPath, serializer);
    }

    private CellBaseBuilder buildCadd() {
        Path variationFunctionalScorePath = downloadFolder.resolve("variation_functional_score");
        copyVersionFiles(Arrays.asList(variationFunctionalScorePath.resolve("caddVersion.json")));
        Path caddFilePath = variationFunctionalScorePath.resolve("whole_genome_SNVs.tsv.gz");
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "cadd");
        return new CaddScoreBuilder(caddFilePath, serializer);
    }

    private CellBaseBuilder buildRegulation() {
        Path regulatoryRegionFilesDir = downloadFolder.resolve("regulation");
        copyVersionFiles(Arrays.asList(regulatoryRegionFilesDir.resolve("ensemblRegulationVersion.json"),
                downloadFolder.resolve("mirbase/mirbaseVersion.json"),
                regulatoryRegionFilesDir.resolve("targetScanVersion.json"),
                regulatoryRegionFilesDir.resolve("miRTarBaseVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "regulatory_region");
        return new RegulatoryRegionBuilder(regulatoryRegionFilesDir, serializer);

    }

    private CellBaseBuilder buildProtein() {
        Path proteinFolder = downloadFolder.resolve("protein");
        copyVersionFiles(Arrays.asList(proteinFolder.resolve("uniprotVersion.json"),
                proteinFolder.resolve("interproVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "protein");
        return new ProteinBuilder(proteinFolder.resolve("uniprot_chunks"),
                downloadFolder.resolve("protein").resolve("protein2ipr.dat.gz"), speciesConfiguration.getScientificName(), serializer);
    }

    private void getProteinFunctionPredictionMatrices(SpeciesConfiguration sp, Path geneFolder)
            throws IOException, InterruptedException {
        logger.info("Downloading protein function prediction matrices ...");

        // run protein_function_prediction_matrices.pl
        String proteinFunctionProcessLogFile = geneFolder.resolve("protein_function_prediction_matrices.log").toString();
        List<String> args = Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toString(),
                "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs());

        boolean proteinFunctionPredictionMatricesObtaines = EtlCommons.runCommandLineProcess(ensemblScriptsFolder,
                "./protein_function_prediction_matrices.pl",
                args,
                proteinFunctionProcessLogFile);

        // check output
        if (proteinFunctionPredictionMatricesObtaines) {
            logger.info("Protein function prediction matrices created OK");
        } else {
            logger.error("Protein function prediction matrices for " + sp.getScientificName() + " cannot be downloaded");
        }
    }

    private CellBaseBuilder getInteractionParser() {
        Path proteinFolder = downloadFolder.resolve("protein");
        Path psimiTabFile = proteinFolder.resolve("intact.txt");
        copyVersionFiles(Arrays.asList(proteinFolder.resolve("intactVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "protein_protein_interaction");
        return new InteractionBuilder(psimiTabFile, speciesConfiguration.getScientificName(), serializer);
    }

    private CellBaseBuilder buildConservation() {
        Path conservationFilesDir = downloadFolder.resolve("conservation");
        copyVersionFiles(Arrays.asList(conservationFilesDir.resolve("gerpVersion.json"),
                conservationFilesDir.resolve("phastConsVersion.json"),
                conservationFilesDir.resolve("phyloPVersion.json")));
        // TODO: chunk size is not really used in ConvervedRegionParser, remove?
        int conservationChunkSize = MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder);
        return new ConservationBuilder(conservationFilesDir, conservationChunkSize, serializer);
    }

    private CellBaseBuilder buildClinicalVariants() {
        Path clinicalVariantFolder = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANTS_FOLDER);
        copyVersionFiles(Arrays.asList(clinicalVariantFolder.resolve("clinvarVersion.json")));
        copyVersionFiles(Arrays.asList(clinicalVariantFolder.resolve("gwasVersion.json")));

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder,
                EtlCommons.CLINICAL_VARIANTS_JSON_FILE.replace(".json.gz", ""), true);
        return new ClinicalVariantBuilder(clinicalVariantFolder, normalize, getFastaReferenceGenome(),
                buildCommandOptions.assembly == null ? getDefaultHumanAssembly() : buildCommandOptions.assembly,
                serializer);
    }

    private String getDefaultHumanAssembly() {
        for (SpeciesConfiguration species : configuration.getSpecies().getVertebrates()) {
            if (species.getId().equals("hsapiens")) {
                return species.getAssemblies().get(0).getName();
            }
        }

        throw new ParameterException("Clinical data can only be built if an hsapiens entry is defined within the "
                + "configuration file. No hsapiens data found within the configuration.json file");
    }

    private Path getFastaReferenceGenome() {
        Path fastaFile = null;
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(downloadFolder.resolve("genome"), entry -> {
                return entry.toString().endsWith(".fa.gz");
            });
            for (Path entry : stream) {
                fastaFile = entry;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fastaFile;
    }
}
