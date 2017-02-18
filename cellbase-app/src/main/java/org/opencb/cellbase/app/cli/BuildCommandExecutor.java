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

package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.app.transform.*;
import org.opencb.cellbase.app.transform.variation.VariationParser;
import org.opencb.cellbase.core.config.Species;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.utils.FileUtils;

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

    // TODO: these constants should be defined in the 'download' module
    public static final String GWAS_INPUT_FILE_NAME = "gwas_catalog.tsv";
    public static final String CADD_INPUT_FILE_NAME = "whole_genome_SNVs.tsv.gz";
    public static final String DISGENET_INPUT_FILE_NAME = "all_gene_disease_associations.txt.gz";
    public static final String HPO_INPUT_FILE_NAME = "ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt";
    public static final String DBSNP_INPUT_FILE_NAME = "All.vcf.gz";

    private CliOptionsParser.BuildCommandOptions buildCommandOptions;

    private Path input = null;
    private Path output = null;
    private Path common = null;

    private File ensemblScriptsFolder;
    private File proteinScriptsFolder;

    private boolean flexibleGTFParsing;
    private Species species;

    public BuildCommandExecutor(CliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.verbose,
                buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;

        if (buildCommandOptions.input != null) {
            input = Paths.get(buildCommandOptions.input);
        }
        if (buildCommandOptions.output != null) {
            output = Paths.get(buildCommandOptions.output);
        }
        if (buildCommandOptions.common != null) {
            common = Paths.get(buildCommandOptions.common);
        } else {
            common = input.getParent().getParent().resolve("common");
        }

        this.ensemblScriptsFolder = new File(System.getProperty("basedir") + "/bin/ensembl-scripts/");
        this.proteinScriptsFolder = new File(System.getProperty("basedir") + "/bin/protein/");
        this.flexibleGTFParsing = buildCommandOptions.flexibleGTFParsing;
    }


    /**
     * Parse specific 'build' command options.
     */
    public void execute() {
        try {
            checkParameters();

            // Output directory need to be created if it not exists
            if (!Files.exists(output)) {
                Files.createDirectories(output);
            }

            // We need to get the Species object from the CLI name
            // This can be the scientific or common name, or the ID
            for (Species sp : configuration.getAllSpecies()) {
                if (buildCommandOptions.species.equalsIgnoreCase(sp.getScientificName())
                        || buildCommandOptions.species.equalsIgnoreCase(sp.getCommonName())
                        || buildCommandOptions.species.equalsIgnoreCase(sp.getId())) {
                    species = sp;
                    break;
                }
            }

            if (species == null) {
                logger.error("Species '{}' not valid", buildCommandOptions.species);
            }

            if (buildCommandOptions.data != null) {

                String[] buildOptions;
                if (buildCommandOptions.data.equals("all")) {
//                    buildOptions = new String[]{EtlCommons.GENOME_INFO_DATA, EtlCommons.GENOME_DATA, EtlCommons.GENE_DATA,
//                            EtlCommons.DISGENET_DATA, EtlCommons.HPO_DATA, EtlCommons.CONSERVATION_DATA,
//                            EtlCommons.REGULATION_DATA, EtlCommons.PROTEIN_DATA, EtlCommons.PPI_DATA,
//                            EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA, EtlCommons.VARIATION_DATA,
//                            EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, EtlCommons.CLINVAR_DATA, EtlCommons.COSMIC_DATA,
//                            EtlCommons.GWAS_DATA, };
                    buildOptions = species.getData().toArray(new String[0]);
                } else {
                    buildOptions = buildCommandOptions.data.split(",");
                }

                for (int i = 0; i < buildOptions.length; i++) {
                    String buildOption = buildOptions[i];

                    logger.info("Building '{}' data", buildOption);
                    CellBaseParser parser = null;
                    switch (buildOption) {
                        case EtlCommons.GENOME_INFO_DATA:
                            buildGenomeInfo();
                            break;
                        case EtlCommons.GENOME_DATA:
                            parser = buildGenomeSequence();
                            break;
                        case EtlCommons.GENE_DATA:
                            parser = buildGene();
                            break;
//                        case EtlCommons.DISGENET_DATA:
//                            parser = buildDisgenet();
//                            break;
//                        case EtlCommons.HPO_DATA:
//                            parser = buildHpo();
//                            break;
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
                        case EtlCommons.DRUG_DATA:
                            parser = buildDrugParser();
                            break;
                        case EtlCommons.CLINVAR_DATA:
                            parser = buildClinvar();
                            break;
                        case EtlCommons.COSMIC_DATA:
                            parser = buildCosmic();
                            break;
                        case EtlCommons.GWAS_DATA:
                            parser = buildGwas();
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
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void copyVersionFiles(List<Path> pathList) {
        for (Path path : pathList) {
            try {
                Files.copy(path, output.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.warn("Version file {} not found - skipping", path.toString());
            }
        }
    }

    private void checkParameters() throws IOException {
        if (!Files.exists(input) || !Files.isDirectory(input)) {
            throw new IOException("Input parameter '" + input.toString() + "' does not exist or is not a directory");
        }

        if (!Files.exists(common) || !Files.isDirectory(common)) {
            throw new IOException("Common parameter '" + common.toString() + "' does not exist or is not a directory");
        }

    }

    private void buildGenomeInfo() {
        /**
         * To get some extra info about the genome such as chromosome length or cytobands
         * we execute the following script.
         */
        try {
            String outputFileName = output.resolve("genome_info.json").toAbsolutePath().toString();
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList("--species", species.getScientificName(), "-o", outputFileName,
                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()));
            if (!configuration.getSpecies().getVertebrates().contains(species)
                    && !species.getScientificName().equals("Drosophila melanogaster")) {
                args.add("--phylo");
                args.add("no-vertebrate");
            }

            String geneInfoLogFileName = output.resolve("genome_info.log").toAbsolutePath().toString();

            boolean downloadedGenomeInfo;
            downloadedGenomeInfo = EtlCommons.runCommandLineProcess(ensemblScriptsFolder, "./genome_info.pl", args, geneInfoLogFileName);

            if (downloadedGenomeInfo) {
                logger.info(outputFileName + " created OK");
            } else {
                logger.error("Genome info for " + species.getScientificName() + " cannot be downloaded");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private CellBaseParser buildGenomeSequence() {
        copyVersionFiles(Collections.singletonList(input.resolve("genome/genomeVersion.json")));
        Path fastaFile = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "genome_sequence");
        return new GenomeSequenceFastaParser(fastaFile, serializer);
    }

    private CellBaseParser buildGene() {
        Path geneFolderPath = input.resolve("gene");
        copyVersionFiles(Arrays.asList(geneFolderPath.resolve("geneDrug/dgidbVersion.json"),
                geneFolderPath.resolve("ensemblCoreVersion.json"), geneFolderPath.resolve("uniprotXrefVersion.json"),
                geneFolderPath.resolve(common.resolve("expression/geneExpressionAtlasVersion.json")),
                geneFolderPath.resolve("hpoVersion.json"), geneFolderPath.resolve("disgenetVersion.json")));
        Path genomeFastaFilePath = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "gene");
        return new GeneParser(geneFolderPath, genomeFastaFilePath, species, flexibleGTFParsing, serializer);
    }


    private CellBaseParser buildVariation() {
        Path variationFolderPath = input.resolve("variation");
        copyVersionFiles(Arrays.asList(variationFolderPath.resolve("ensemblVariationVersion.json")));
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output, null, true, true, true);
        return new VariationParser(variationFolderPath, serializer);
    }

    private CellBaseParser buildCadd() {
        Path variationFunctionalScorePath = input.resolve("variation_functional_score");
        copyVersionFiles(Arrays.asList(variationFunctionalScorePath.resolve("caddVersion.json")));
        Path caddFilePath = variationFunctionalScorePath.resolve(CADD_INPUT_FILE_NAME);
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output, "cadd");
        return new CaddScoreParser(caddFilePath, serializer);
    }

    private CellBaseParser buildRegulation() {
        Path regulatoryRegionFilesDir = input.resolve("regulation");
        copyVersionFiles(Arrays.asList(regulatoryRegionFilesDir.resolve("ensemblRegulationVersion.json"),
                common.resolve("mirbase/mirbaseVersion.json"),
                regulatoryRegionFilesDir.resolve("targetScanVersion.json"),
                regulatoryRegionFilesDir.resolve("miRTarBaseVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "regulatory_region");
        return new RegulatoryRegionParser(regulatoryRegionFilesDir, serializer);

    }

    private CellBaseParser buildProtein() {
        Path proteinFolder = common.resolve("protein");
        copyVersionFiles(Arrays.asList(proteinFolder.resolve("uniprotVersion.json"),
                proteinFolder.resolve("interproVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "protein");
        return new ProteinParser(proteinFolder.resolve("uniprot_chunks"),
                common.resolve("protein").resolve("protein2ipr.dat.gz"), species.getScientificName(), serializer);
    }

    private void getProteinFunctionPredictionMatrices(Species sp, Path geneFolder)
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

    private CellBaseParser getInteractionParser() {
        Path proteinFolder = common.resolve("protein");
        Path psimiTabFile = proteinFolder.resolve("intact.txt");
        copyVersionFiles(Arrays.asList(proteinFolder.resolve("intactVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "protein_protein_interaction");
        return new InteractionParser(psimiTabFile, species.getScientificName(), serializer);
    }

    private CellBaseParser buildDrugParser() {
        throw new ParameterException("'drug' builder is not implemented yet");
//        Path drugFile = getInputFileFromCommandLine();
//        CellBaseSerializer serializer = new JsonParser(output, "drug");
//        return new DrugParser(drugFile, serializer);
    }


    private CellBaseParser buildConservation() {
        Path conservationFilesDir = input.resolve("conservation");
        copyVersionFiles(Arrays.asList(conservationFilesDir.resolve("gerpVersion.json"),
                conservationFilesDir.resolve("phastConsVersion.json"),
                conservationFilesDir.resolve("phyloPVersion.json")));
        // TODO: chunk size is not really used in ConvervedRegionParser, remove?
        int conservationChunkSize = MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
        return new ConservationParser(conservationFilesDir, conservationChunkSize, serializer);
    }


    private CellBaseParser buildClinvar() {
        Path clinvarFolder = input.resolve("clinical");
        copyVersionFiles(Arrays.asList(clinvarFolder.resolve("clinvarVersion.json")));
        Path clinvarFile = clinvarFolder.resolve("ClinVar.xml.gz");
        Path clinvarSummaryFile = clinvarFolder.resolve("variant_summary.txt.gz");
        Path efosFilePath = clinvarFolder.resolve("ClinVar_Traits_EFO_Names.csv");
        if (!efosFilePath.toFile().exists()) {
            efosFilePath = null;
        }

        String assembly = buildCommandOptions.assembly;
        checkMandatoryOption("assembly", assembly);
        if (!assembly.equals(ClinVarParser.GRCH37_ASSEMBLY) && !assembly.equals(ClinVarParser.GRCH38_ASSEMBLY)) {
            throw new ParameterException("Assembly '" + assembly + "' is not valid. Possible values: " + ClinVarParser.GRCH37_ASSEMBLY
                    + ", " + ClinVarParser.GRCH38_ASSEMBLY);
        }

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "clinvar", true);
        return new ClinVarParser(clinvarFile, clinvarSummaryFile, efosFilePath, assembly, serializer);
    }

    private CellBaseParser buildCosmic() {
        Path cosmicFilePath = input.resolve("CosmicMutantExport.tsv");
        //MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
        // this parser works with cosmic file: CosmicCompleteExport_vXX.tsv (XX >= 70)
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "cosmic", true);
        String assembly = buildCommandOptions.assembly;
        return new CosmicParser(cosmicFilePath, serializer, assembly);
    }

    private CellBaseParser buildGwas() throws IOException {
        Path inputDir = getInputDirFromCommandLine().resolve("clinical");
        copyVersionFiles(Arrays.asList(inputDir.resolve("gwasVersion.json")));
        Path gwasFile = inputDir.resolve(GWAS_INPUT_FILE_NAME);
        FileUtils.checkPath(gwasFile);
        Path dbsnpFile = inputDir.resolve(DBSNP_INPUT_FILE_NAME);
        FileUtils.checkPath(dbsnpFile);
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "gwas");
        return new GwasParser(gwasFile, dbsnpFile, serializer);
    }

    @Deprecated
    private CellBaseParser buildDisgenet() throws IOException {
        Path inputDir = getInputDirFromCommandLine().resolve("gene_disease_association");
        copyVersionFiles(Collections.singletonList(inputDir.resolve("disgenetVersion.json")));
        Path disgenetFile = inputDir.resolve(DISGENET_INPUT_FILE_NAME);
        FileUtils.checkPath(disgenetFile);
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "disgenet");
        return new DisgenetParser(disgenetFile, serializer);
    }

    @Deprecated
    private CellBaseParser buildHpo() throws IOException {
        Path inputDir = getInputDirFromCommandLine().resolve("gene_disease_association");
        copyVersionFiles(Collections.singletonList(inputDir.resolve("hpoVersion.json")));
        Path hpoFilePath = inputDir.resolve(HPO_INPUT_FILE_NAME);
        FileUtils.checkPath(hpoFilePath);
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "hpo");
        return new DisgenetParser(hpoFilePath, serializer);
    }

    private Path getInputDirFromCommandLine() {
        File inputDirectory = new File(input.toString());
        if (inputDirectory.exists()) {
            if (inputDirectory.isDirectory()) {
                return input;
            } else {
                throw new ParameterException("'" + input + "' is not a directory");
            }
        } else {
            throw new ParameterException("Folder '" + input + "' doesn't exist");
        }
    }

    private Path getFastaReferenceGenome() {
        Path fastaFile = null;
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(input.resolve("genome"), entry -> {
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

    private void checkMandatoryOption(String option, String value) {
        if (value == null) {
            throw new ParameterException("'" + option + "' option is mandatory for '" + buildCommandOptions.data + "' builder");
        }
    }

}
