package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.CellBaseConfiguration.SpeciesProperties.Species;

import java.io.File;
import java.io.IOException;
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

            Set<Species> speciesToDownload = getSpecies();

            for (Species sp : speciesToDownload) {
                try {
                    processSpecies(sp, outputDir);
                } catch (IOException | InterruptedException e) {
                    logger.error("Error downloading '" + sp.getScientificName() + "' files: " + e.getMessage());
                }
            }
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        }

    }

    private void checkParameters() {
        if (!downloadCommandOptions.sequence && !downloadCommandOptions.gene && !downloadCommandOptions.variation && !downloadCommandOptions.regulation) {
            throw new ParameterException("At least one download parameter must be selected: sequence, gene, variation, regulation, protein");
        }
    }

    private Set<Species> getSpecies() {
        Set<String> speciesToDownloadNames = new HashSet<>(downloadCommandOptions.species);
        List<Species> allSpecies = configuration.getAllSpecies();
        Set<Species> speciesToDownload = new HashSet<>();

        if (speciesToDownloadNames.size() == 1 && speciesToDownloadNames.contains("all")) {
            speciesToDownload.addAll(allSpecies);
        } else {
            HashMap<String, Species> nameToSpeciesMap = new HashMap<>();
            for (Species sp : allSpecies) {
                nameToSpeciesMap.put(sp.getScientificName().toLowerCase().replaceAll("\\s+", "_"), sp);
            }

            // add all species to download
            for (String spName : speciesToDownloadNames) {
                Species sp = nameToSpeciesMap.get(spName.toLowerCase().replaceAll("\\s+", "_"));
                if (sp == null) {
                    throw new ParameterException("Specie " + spName + " not found in cellbase configuration file");
                } else {
                    speciesToDownload.add(sp);
                }
            }
        }

        return speciesToDownload;
    }

    private void processSpecies(Species sp, Path outputDir) throws IOException, InterruptedException {
        logger.info("Processing species " + sp.getScientificName());

        // output folder
        String spShortName = sp.getScientificName().toLowerCase().replaceAll("\\.", "").replaceAll("\\)", "").replaceAll("[-(/]", " ").replaceAll("\\s+", "_");
        Path spFolder = outputDir.resolve(spShortName);
        makeDir(spFolder);

        String host = getHost(sp);

        // download sequence, gene, variation and regulation
        if (downloadCommandOptions.sequence && specieHasInfoToDownload(sp, "genome_sequence")) {
            downloadSequence(sp, spShortName, spFolder, host);
        }
        if (downloadCommandOptions.gene && specieHasInfoToDownload(sp, "gene")) {
            downloadGene(sp, spFolder);
        }
        if (downloadCommandOptions.variation && specieHasInfoToDownload(sp, "variation")) {
            downloadVariation(sp, spShortName, spFolder, host);
        }
        if (downloadCommandOptions.regulation && specieHasInfoToDownload(sp, "regulation")) {
            downloadRegulation(sp, spShortName, spFolder, host);
        }
    }

    private String getHost(Species sp) {
        String host;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            host = configuration.getDownload().getEnsembl().getUrl().getHost();
        } else {
            host = configuration.getDownload().getEnsemblGenomes().getUrl().getHost();
        }
        return host;
    }

    private boolean specieHasInfoToDownload(Species sp, String info) {
        boolean hasInfo = true;
        if (sp.getData() == null || !sp.getData().contains(info)) {
            logger.warn("Specie " + sp.getScientificName() + " has no " + info + " information available to download");
            hasInfo = false;
        }
        return hasInfo;
    }

    private void downloadSequence(Species sp, String shortName, Path spFolder, String host) throws IOException, InterruptedException {
        logger.info("Downloading genome-sequence information ...");
        Path sequenceFolder = spFolder.resolve("sequence");
        makeDir(sequenceFolder);
        String assembly = getAssembly(sp, downloadCommandOptions.assembly);
        String url = getSequenceUrl(sp, shortName, assembly, host);
        String outputFileName = StringUtils.capitalize(shortName) + "." + assembly + ".fa.gz";
        Path outputPath = sequenceFolder.resolve(outputFileName);
        downloadFile(url, outputPath.toString());
        getGenomeInfo(sp, sequenceFolder);
    }

    private String getAssembly(Species sp, String assembly) {
        if (assembly == null) {
            String defaultAssemblyName = sp.getAssemblies().get(0).getName();
            this.logger.info("No assembly selected, default assembly will be downloaded: " + defaultAssemblyName);
            return defaultAssemblyName;
        } else {
            for (Species.Assembly spAssembly : sp.getAssemblies()) {
                String assemblyName = spAssembly.getName();
                if (assemblyName.equalsIgnoreCase(assembly)) {
                    return assemblyName;
                }
            }
            // assembly not found
            String errorMessage = "Assembly " + assembly + " not found in species " + sp.getScientificName();
            errorMessage +=  "\n\tAvailable assemblies for this specie: " + getAvailableAssemblies(sp);
            throw new ParameterException(errorMessage);
        }
    }

    private String getSequenceUrl(Species sp, String shortName, String assembly, String host) {
        String seqUrl;

        String ensemblRelease = "/release-" + getEnsemblVersion(sp, assembly).split("_")[0];
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            seqUrl = host + ensemblRelease;
        } else {
            seqUrl = host + ensemblRelease + "/" + getPhylo(sp);
        }

        seqUrl = seqUrl + "/fasta/" + shortName + "/dna/*.dna.primary_assembly.fa.gz";

        return seqUrl;
    }

    private String getEnsemblVersion(Species sp, String assembly) {
        for (Species.Assembly spAssembly : sp.getAssemblies()) {
            if (spAssembly.getName().equalsIgnoreCase(assembly)) {
                return spAssembly.getEnsemblVersion();
            }
        }
        return null;
    }

    private String getAvailableAssemblies(Species sp) {
        List<String> assemblies = new ArrayList<>();
        for (Species.Assembly assembly : sp.getAssemblies()) {
            assemblies.add(assembly.getName());
        }
        return StringUtils.join(assemblies, ", ");
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
            throw new ParameterException ("Species " + sp.getScientificName() + " not associated to any phylo in cellbase configuration file");
        }
    }

    private void getGenomeInfo(Species sp, Path genomeSequenceFolder) throws IOException, InterruptedException {
        // run genome_info.pl
        String outputFileName = genomeSequenceFolder + "/genome_info.json";
        List<String> args = Arrays.asList("--species", sp.getScientificName(), "-o", outputFileName);
        String geneInfoLogFileName = genomeSequenceFolder + "/genome_info.log";

        boolean downloadedGenomeInfo = runCommandLineProcess(ensemblScriptsFolder, "./genome_info.pl", args, geneInfoLogFileName);

        // check output
        if (downloadedGenomeInfo) {
            logger.info(outputFileName + "  created OK");
        } else {
            logger.error("Genome info for " + sp.getScientificName() + " cannot be downloaded");
        }
    }

    private void downloadGene(Species sp, Path spFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene information ...");
        Path geneFolder = spFolder.resolve("gene");
        makeDir(geneFolder);
        getGeneExtraInfo(sp, geneFolder);
        if (sp.getScientificName().equalsIgnoreCase("homo sapiens")) {
            // TODO: output folder is gene or regulation?
            getProteinFunctionPredictionMatrices(sp, geneFolder);
        }
    }

    private void getGeneExtraInfo(Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene extra info ...");

        String geneExtraInfoLogFile = geneFolder.resolve("gene_extra_info_cellbase.log").toString();
        List<String> args = Arrays.asList( "--species", sp.getScientificName(), "--outdir", geneFolder.toString());

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

    private void getProteinFunctionPredictionMatrices(Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading protein function prediction matrices ...");

        // run protein_function_prediction_matrices.pl
        String proteinFunctionProcessLogFile = geneFolder.resolve("protein_function_prediction_matrices.log").toString();
        List<String> args = Arrays.asList( "--species", sp.getScientificName(), "--outdir", geneFolder.toString());

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

    private void downloadVariation(Species sp, String shortName, Path spFolder, String host) throws IOException, InterruptedException {
        logger.info("Downloading variation information ...");
        Path variationFolder = spFolder.resolve("variation");
        makeDir(variationFolder);

        String variationUrl = this.getVariationUrl(sp, shortName, host);
        for (String variationFile : variationFiles) {
            Path outputFile = variationFolder.resolve(variationFile);
            downloadFile(variationUrl + "/" + variationFile, outputFile.toString());
        }
    }

    private String getVariationUrl(Species sp, String shortName, String host) {
        String variationUrl;

        String ensemblVersion = getEnsemblVersion(sp, downloadCommandOptions.assembly);
        String ensemblRelease = "/release-" + ensemblVersion.split("_")[0];
        variationUrl = host + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(sp)) {
            variationUrl = host + ensemblRelease + "/" + getPhylo(sp);
        }

        variationUrl = variationUrl + "/mysql/" + shortName + "_variation_" + ensemblVersion;

        return variationUrl;
    }

    private void downloadRegulation(Species sp, String shortName, Path spFolder, String host) throws IOException, InterruptedException {
        logger.info("Downloading regulation information ...");
        Path regulationFolder = spFolder.resolve("regulation");
        makeDir(regulationFolder);

        String regulationUrl = getRegulationUrl(sp, shortName, host);

        for (String regulationFile : regulationFiles) {
            Path outputFile = regulationFolder.resolve(regulationFile);
            downloadFile(regulationUrl + "/" + regulationFile, outputFile.toString());
        }
    }

    private String getRegulationUrl(Species sp, String shortName, String host) {
        String regulationUrl;

        String ensemblVersion = getEnsemblVersion(sp, downloadCommandOptions.assembly);
        String ensemblRelease = "/release-" + ensemblVersion.split("_")[0];
        regulationUrl = host + ensemblRelease + "/regulation/" + shortName;

        return regulationUrl;       
    }

    private void makeDir(Path folderPath) {
        File folder = folderPath.toFile();
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new ParameterException(folderPath.getFileName() + " cannot be created");
            }
        } else if (!folder.isDirectory()) {
            throw new ParameterException(folderPath.getFileName() + " exists and it is a file");
        }
    }

    private void downloadFile(String url, String outputFileName) throws IOException, InterruptedException {
        List<String> wgetArgs = Arrays.asList("--tries=10", url, "-O", outputFileName, "-o", outputFileName + ".log");
        boolean downloaded = runCommandLineProcess(null, "wget", wgetArgs, null);

        if (downloaded) {
            logger.info(outputFileName + "  created OK");
        } else {
            logger.warn(url + " cannot be downloaded");
        }
    }

    private boolean runCommandLineProcess(File workingDirectory,  String binPath, List<String> args, String logFilePath) throws IOException, InterruptedException {
        ProcessBuilder builder = getProcessBuilder(binPath, args, workingDirectory, logFilePath);

        logger.debug("Executing command: " + StringUtils.join(builder.command(), " "));
        Process process = builder.start();
        process.waitFor();

        return checkProcessOutput(process, binPath, logFilePath);
    }

    private ProcessBuilder getProcessBuilder(String binPath, List<String> args, File workingDirectory, String logFilePath) {
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

    private boolean checkProcessOutput(Process process, String binPath, String logFileName) {
        boolean executedWithouErrors;
        // check output
        int genomeInfoExitValue = process.exitValue();
        if (genomeInfoExitValue == 0) {
            executedWithouErrors = true;
        } else {
            logger.warn("Error executing " + binPath + ". For more info read the log file: " + logFileName);
            executedWithouErrors = false;
        }
        return executedWithouErrors;
    }
}
