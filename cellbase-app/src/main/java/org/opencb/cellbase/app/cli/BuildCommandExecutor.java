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
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.commons.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

    private CellBaseConfiguration.SpeciesProperties.Species species;

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
            for (CellBaseConfiguration.SpeciesProperties.Species sp : configuration.getAllSpecies()) {
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
                    buildOptions = new String[]{"genome_info", "genome", "gene", "disgenet", "hpo", "variation", "cadd", "regulation",
                            "protein", "ppi", "conservation", "drug", "clinvar", "cosmic", "gwas", };
                } else {
                    buildOptions = buildCommandOptions.data.split(",");
                }

                for (int i = 0; i < buildOptions.length; i++) {
                    String buildOption = buildOptions[i];

                    logger.info("Building '{}' data", buildOption);
                    CellBaseParser parser = null;
                    switch (buildOption) {
                        case "genome_info":
                            buildGenomeInfo();
                            break;
                        case "genome":
                            parser = buildGenomeSequence();
                            break;
                        case "gene":
                            parser = buildGene();
                            break;
                        case "disgenet":
                            parser = buildDisgenet();
                            break;
                        case "hpo":
                            parser = buildHpo();
                            break;
                        case "variation":
                            parser = buildVariation();
                            break;
                        case "cadd":
                            parser = buildCadd();
                            break;
                        case "regulation":
                            parser = buildRegulation();
                            break;
                        case "protein":
                            parser = buildProtein();
                            break;
                        case "ppi":
                            parser = getInteractionParser();
                            break;
                        case "conservation":
                            parser = buildConservation();
                            break;
                        case "drug":
                            parser = buildDrugParser();
                            break;
                        case "clinvar":
                            parser = buildClinvar();
                            break;
                        case "cosmic":
                            parser = buildCosmic();
                            break;
                        case "gwas":
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
            downloadedGenomeInfo = runCommandLineProcess(ensemblScriptsFolder, "./genome_info.pl", args, geneInfoLogFileName);

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
        Path fastaFile = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "genome_sequence");
        return new GenomeSequenceFastaParser(fastaFile, serializer);
    }


    private CellBaseParser buildGene() {
        Path geneFolderPath = input.resolve("gene");
        Path genomeFastaFilePath = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "gene");
        return new GeneParser(geneFolderPath, genomeFastaFilePath, species, serializer);
//        return new GeneParserProto(geneFolderPath, genomeFastaFilePath, species, serializer);
    }


    private CellBaseParser buildVariation() {
        Path variationFolderPath = input.resolve("variation");
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
        return new VariationParser(variationFolderPath, serializer);
    }

    private CellBaseParser buildCadd() {
        Path caddFilePath = input.resolve("variation_functional_score").resolve(CADD_INPUT_FILE_NAME);
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output, "cadd");
        return new CaddScoreParser(caddFilePath, serializer);
    }

    private CellBaseParser buildRegulation() {
        Path regulatoryRegionFilesDir = input.resolve("regulation");
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "regulatory_region");
        return new RegulatoryRegionParser(regulatoryRegionFilesDir, serializer);

    }

    private CellBaseParser buildProtein() {
        Path proteinFolder = common.resolve("protein");
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "protein");
        return new ProteinParser(proteinFolder.resolve("uniprot_chunks"), common.resolve("protein").resolve("protein2ipr.dat.gz"),
                species.getScientificName(), serializer);

    }

    private void getProteinFunctionPredictionMatrices(CellBaseConfiguration.SpeciesProperties.Species sp, Path geneFolder)
            throws IOException, InterruptedException {
        logger.info("Downloading protein function prediction matrices ...");

        // run protein_function_prediction_matrices.pl
        String proteinFunctionProcessLogFile = geneFolder.resolve("protein_function_prediction_matrices.log").toString();
        List<String> args = Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toString(),
                "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs());

        boolean proteinFunctionPredictionMatricesObtaines = runCommandLineProcess(ensemblScriptsFolder,
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
        Path psimiTabFile = common.resolve("protein").resolve("intact.txt");
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
        // TODO: chunk size is not really used in ConvervedRegionParser, remove?
        int conservationChunkSize = MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
        return new ConservationParser(conservationFilesDir, conservationChunkSize, serializer);
    }


    private CellBaseParser buildClinvar() {
        Path clinvarFile = input.resolve("ClinVar.xml.gz");
        Path clinvarSummaryFile = input.resolve("variant_summary.txt.gz");
        Path efosFilePath = input.resolve("ClinVar_Traits_EFO_Names.csv");
        if (!efosFilePath.toFile().exists()) {
            efosFilePath = null;
        }

        String assembly = buildCommandOptions.assembly;
        checkMandatoryOption("assembly", assembly);
        if (!assembly.equals(ClinVarParser.GRCH37_ASSEMBLY) && !assembly.equals(ClinVarParser.GRCH38_ASSEMBLY)) {
            throw new ParameterException("Assembly '" + assembly + "' is not valid. Possible values: " + ClinVarParser.GRCH37_ASSEMBLY
                    + ", " + ClinVarParser.GRCH38_ASSEMBLY);
        }

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "clinvar");
        return new ClinVarParser(clinvarFile, clinvarSummaryFile, efosFilePath, assembly, serializer);
    }

    private CellBaseParser buildCosmic() {
        Path cosmicFilePath = input.resolve("CosmicMutantExport.tsv");
        //MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
        // this parser works with cosmic file: CosmicCompleteExport_vXX.tsv (XX >= 70)
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "cosmic");
        return new CosmicParser(cosmicFilePath, serializer);
    }

    private CellBaseParser buildGwas() throws IOException {
        Path inputDir = getInputDirFromCommandLine();
        Path gwasFile = inputDir.resolve(GWAS_INPUT_FILE_NAME);
        FileUtils.checkPath(gwasFile);
        Path dbsnpFile = inputDir.resolve(DBSNP_INPUT_FILE_NAME);
        FileUtils.checkPath(dbsnpFile);
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "gwas");
        return new GwasParser(gwasFile, dbsnpFile, serializer);
    }

    private CellBaseParser buildDisgenet() throws IOException {
        Path inputDir = getInputDirFromCommandLine();
        Path disgenetFile = inputDir.resolve("gene_disease_association").resolve(DISGENET_INPUT_FILE_NAME);
        FileUtils.checkPath(disgenetFile);
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(output, "disgenet");
        return new DisgenetParser(disgenetFile, serializer);
    }

    private CellBaseParser buildHpo() throws IOException {
        Path inputDir = getInputDirFromCommandLine();
        Path hpoFilePath = inputDir.resolve("gene_disease_association").resolve(HPO_INPUT_FILE_NAME);
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
