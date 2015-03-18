package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.CellBaseConfiguration.SpeciesProperties.Species;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandParser extends CommandParser {

    private File ensemblScriptsFolder;
    private CliOptionsParser.DownloadCommandOptions downloadCommandOptions;

    private static final String[] variationFiles = {"variation.txt.gz", "variation_feature.txt.gz",
            "transcript_variation.txt.gz", "variation_synonym.txt.gz", "seq_region.txt.gz", "source.txt.gz",
            "attrib.txt.gz", "attrib_type.txt.gz", "seq_region.txt.gz", "structural_variation_feature.txt.gz",
            "study.txt.gz", "phenotype.txt.gz", "phenotype_feature.txt.gz", "phenotype_feature_attrib.txt.gz",
            "motif_feature_variation.txt.gz", "genotype_code.txt.gz", "allele_code.txt.gz",
            "population_genotype.txt.gz", "population.txt.gz", "allele.txt.gz"};

    private static final String[] regulationFiles = {"AnnotatedFeatures.gff.gz", "MotifFeatures.gff.gz", "RegulatoryFeatures_MultiCell.gff.gz"};

    private String ensemblVersion;
    private String ensemblRelease;

    public DownloadCommandParser(CliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.verbose,
                downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
        this.ensemblScriptsFolder = new File(System.getProperty("basedir") + "/bin/ensembl-scripts/");
    }


    /**
     * Parse specific 'download' command options
     */
    public void parse() {
        try {
            checkParameters();
            Path outputDir = Paths.get(downloadCommandOptions.outputDir);
            makeDir(outputDir);

            // We need to get the Species object from the CLI name
            // This can be the scientific or common name, or the ID
            Species speciesToDownload = null;
            for (Species species: configuration.getAllSpecies()) {
                if (downloadCommandOptions.species.equalsIgnoreCase(species.getScientificName())
                        || downloadCommandOptions.species.equalsIgnoreCase(species.getCommonName())
                        || downloadCommandOptions.species.equalsIgnoreCase(species.getId())) {
                    speciesToDownload = species;
                    break;
                }
            }

            // If everything is right we launch the download
            if(speciesToDownload != null) {
                processSpecies(speciesToDownload, outputDir);
            }else {
                logger.error("Species '{}' not valid", downloadCommandOptions.species);
            }
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.error("Error downloading '" + downloadCommandOptions.species + "' files: " + e.getMessage());
        }

    }

    private void checkParameters() {
        if (!downloadCommandOptions.sequence && !downloadCommandOptions.gene && !downloadCommandOptions.variation
                && !downloadCommandOptions.regulation && !downloadCommandOptions.protein) {
            throw new ParameterException("At least one 'download' option must be selected: sequence, gene, variation, regulation, protein");
        }
    }

    private void processSpecies(Species sp, Path outputDir) throws IOException, InterruptedException {
        logger.info("Processing species " + sp.getScientificName());

        // output folder
        String spShortName = sp.getScientificName().toLowerCase().replaceAll("\\.", "").replaceAll("\\)", "").replaceAll("[-(/]", " ").replaceAll("\\s+", "_");
        Path spFolder = outputDir.resolve(spShortName);
        makeDir(spFolder);

        // We need to find which is the Ensembl host URL.
        // This can different depending on if is a vertebrate species.
        String ensemblHostUrl;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            ensemblHostUrl = configuration.getDownload().getEnsembl().getUrl().getHost();
        } else {
            ensemblHostUrl = configuration.getDownload().getEnsemblGenomes().getUrl().getHost();
        }

        // Getting the assembly. By default the first assembly in the configuration.json
        Species.Assembly assembly = null;
        if(downloadCommandOptions.assembly == null || downloadCommandOptions.assembly.isEmpty()) {
            assembly = sp.getAssemblies().get(0);
        }else {
            for (Species.Assembly assembly1 : sp.getAssemblies()) {
                if(downloadCommandOptions.assembly.equalsIgnoreCase(assembly1.getName())) {
                    assembly = assembly1;
                    break;
                }
            }
        }

        // Checking that the species and assembly are correct
        if(ensemblHostUrl == null || assembly == null) {
            logger.error("Something is not correct, check the species '{}' or the assembly '{}'",
                    downloadCommandOptions.species, downloadCommandOptions.assembly);
            return;
        }

        ensemblVersion = assembly.getEnsemblVersion();
        ensemblRelease = "release-" + ensemblVersion.split("_")[0];

        // download sequence, gene, variation, regulation and protein
        if (downloadCommandOptions.sequence && speciesHasInfoToDownload(sp, "genome_sequence")) {
            downloadSequence(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
        }
        if (downloadCommandOptions.gene && speciesHasInfoToDownload(sp, "gene")) {
            downloadGene(sp, spShortName, spFolder, ensemblHostUrl);
        }
        if (downloadCommandOptions.variation && speciesHasInfoToDownload(sp, "variation")) {
            downloadVariation(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
        }
        if (downloadCommandOptions.regulation && speciesHasInfoToDownload(sp, "regulation")) {
            downloadRegulation(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
        }
        if (downloadCommandOptions.protein && speciesHasInfoToDownload(sp, "protein")) {
            downloadProtein(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
        }
    }


    private boolean speciesHasInfoToDownload(Species sp, String info) {
        boolean hasInfo = true;
        if (sp.getData() == null || !sp.getData().contains(info)) {
            logger.warn("Specie " + sp.getScientificName() + " has no " + info + " information available to download");
            hasInfo = false;
        }
        return hasInfo;
    }

    private void downloadSequence(Species sp, String shortName, String assembly, Path spFolder, String host) throws IOException, InterruptedException {
        logger.info("Downloading genome-sequence information ...");
        Path sequenceFolder = spFolder.resolve("sequence");
        makeDir(sequenceFolder);

        String url;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            url = host + "/" + ensemblRelease;
        } else {
            url = host + "/" + ensemblRelease + "/" + getPhylo(sp);
        }
        url = url + "/fasta/" + shortName + "/dna/*.dna.primary_assembly.fa.gz";

        String outputFileName = StringUtils.capitalize(shortName) + "." + assembly + ".fa.gz";
        Path outputPath = sequenceFolder.resolve(outputFileName);
        downloadFile(url, outputPath.toString());
        getGenomeInfo(sp, sequenceFolder);
    }

    private void getGenomeInfo(Species sp, Path genomeSequenceFolder) throws IOException, InterruptedException {
        // run genome_info.pl
        String outputFileName = genomeSequenceFolder + "/genome_info.json";
        List<String> args = Arrays.asList("--species", sp.getScientificName(), "-o", outputFileName,
                "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs());
        String geneInfoLogFileName = genomeSequenceFolder + "/genome_info.log";

        boolean downloadedGenomeInfo = runCommandLineProcess(ensemblScriptsFolder, "./genome_info.pl", args, geneInfoLogFileName);

        // check output
        if (downloadedGenomeInfo) {
            logger.info(outputFileName + "  created OK");
        } else {
            logger.error("Genome info for " + sp.getScientificName() + " cannot be downloaded");
        }
    }

    private String getPhylo(Species sp) {
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            return "vertebrates";
        } else if (configuration.getSpecies().getMetazoa().contains(sp)) {
            return "metazoa";
        } else if (configuration.getSpecies().getFungi().contains(sp)) {
            return "fungi";
        } else if (configuration.getSpecies().getProtist().contains(sp)) {
            return "protists";
        } else if (configuration.getSpecies().getPlants().contains(sp)) {
            return "plants";
        } else {
            throw new ParameterException ("Species " + sp.getScientificName() + " not associated to any phylo in the configuration file");
        }
    }

    private void downloadGene(Species sp, String spShortName, Path spFolder, String host) throws IOException, InterruptedException {
        logger.info("Downloading gene information ...");
        Path geneFolder = spFolder.resolve("gene");
        makeDir(geneFolder);
        downloadGeneGtf(sp, spShortName, geneFolder, host);
        getGeneExtraInfo(sp, geneFolder);
        if (sp.getScientificName().equalsIgnoreCase("homo sapiens")) {
            // TODO: output folder is gene or regulation?
            getProteinFunctionPredictionMatrices(sp, geneFolder);
        }
    }

    private void downloadGeneGtf(Species sp, String spShortName, Path geneFolder, String host) throws IOException, InterruptedException {
        logger.info("Downloading gene gtf ...");
        String geneGtfUrl;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            geneGtfUrl = host + "/" + ensemblRelease;
        } else {
            geneGtfUrl = host + "/" + ensemblRelease + "/" + getPhylo(sp);
        }
        geneGtfUrl = geneGtfUrl + "/gtf/" + spShortName + "/*.gtf.gz";

        String geneGtfOutputFileName = geneFolder.resolve(spShortName + ".gtf.gz").toString();

        downloadFile(geneGtfUrl, geneGtfOutputFileName);
    }

    private void getGeneExtraInfo(Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene extra info ...");

        String geneExtraInfoLogFile = geneFolder.resolve("gene_extra_info_cellbase.log").toString();
        List<String> args = Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toString(),
                "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs());

        // run gene_extra_info_cellbase.pl
        boolean geneExtraInfoDownloaded = runCommandLineProcess(ensemblScriptsFolder,
                "./gene_extra_info_cellbase.pl",
                args,
                geneExtraInfoLogFile);

        // check output
        if (geneExtraInfoDownloaded) {
            logger.info("Gene extra files created OK");
        } else {
            logger.error("Gene extra info for " + sp.getScientificName() + " cannot be downloaded");
        }
    }

    private void downloadVariation(Species sp, String shortName, String assembly, Path spFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading variation information ...");
        Path variationFolder = spFolder.resolve("variation");
        makeDir(variationFolder);

        String variationUrl = host + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(sp)) {
            variationUrl = host + "/" + ensemblRelease + "/" + getPhylo(sp);
        }
        variationUrl = variationUrl + "/mysql/" + shortName + "_variation_" + ensemblVersion;

        for (String variationFile : variationFiles) {
            Path outputFile = variationFolder.resolve(variationFile);
            downloadFile(variationUrl + "/" + variationFile, outputFile.toString());
        }
    }

    private void downloadRegulation(Species sp, String shortName, String assembly, Path spFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading regulation information ...");
        Path regulationFolder = spFolder.resolve("regulation");
        makeDir(regulationFolder);

        String regulationUrl = host + "/" + ensemblRelease + "/regulation/" + shortName;

        for (String regulationFile : regulationFiles) {
            Path outputFile = regulationFolder.resolve(regulationFile);
            downloadFile(regulationUrl + "/" + regulationFile, outputFile.toString());
        }
    }

    /*
     * PROTEIN METHODS
     */

    private void downloadProtein(Species sp, String shortName, String assembly, Path spFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading protein information ...");
        Path proteinFolder = spFolder.resolve("common").resolve("protein");
        if(!Files.exists(proteinFolder.resolve("uniprot_sprot.xml.gz"))) {
            makeDir(proteinFolder);

            String proteinUrl = configuration.getDownload().getUniprot().getHost();
            downloadFile(proteinUrl, proteinFolder.resolve("uniprot_sprot.xml.gz").toString());
        }else {
            logger.info("File '{}' already exists", proteinFolder.resolve("uniprot_sprot.xml.gz"));
        }
    }

    private void getProteinFunctionPredictionMatrices(Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading protein function prediction matrices ...");

        // run protein_function_prediction_matrices.pl
        String proteinFunctionProcessLogFile = geneFolder.resolve("protein_function_prediction_matrices.log").toString();
        List<String> args = Arrays.asList( "--species", sp.getScientificName(), "--outdir", geneFolder.toString(),
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

    private void makeDir(Path folderPath) throws IOException {
        if(!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

    private void downloadFile(String url, String outputFileName) throws IOException, InterruptedException {
        List<String> wgetArgs = Arrays.asList("--tries=10", url, "-O", outputFileName, "-o", outputFileName + ".log");
        boolean downloaded = runCommandLineProcess(null, "wget", wgetArgs, null);

        if (downloaded) {
            logger.info(outputFileName + " created OK");
        } else {
            logger.warn(url + " cannot be downloaded");
        }
    }

    private boolean runCommandLineProcess(File workingDirectory, String binPath, List<String> args, String logFilePath) throws IOException, InterruptedException {
        ProcessBuilder builder = getProcessBuilder(workingDirectory, binPath, args, logFilePath);

        logger.debug("Executing command: " + StringUtils.join(builder.command(), " "));
        Process process = builder.start();
        process.waitFor();

        // Check process output
        boolean executedWithoutErrors = true;
        int genomeInfoExitValue = process.exitValue();
        if (genomeInfoExitValue != 0) {
            logger.warn("Error executing {}, error code: {}. More info in log file: {}", binPath, genomeInfoExitValue, logFilePath);
            executedWithoutErrors = false;
        }
        return executedWithoutErrors;
    }

    private ProcessBuilder getProcessBuilder(File workingDirectory, String binPath, List<String> args, String logFilePath) {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(binPath);
        commandArgs.addAll(args);
        ProcessBuilder builder = new ProcessBuilder(commandArgs);

        // working directoy and error and output log outputs
        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }
        builder.redirectErrorStream(true);
        if (logFilePath != null) {
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilePath)));
        }

        return builder;
    }
}
