package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandParser extends CommandParser {

    public static final String DATABASE_HOST = "databaseHost";
    public static final String DATABASE_PORT = "databasePort";
    private CliOptionsParser.DownloadCommandOptions downloadCommandOptions;

    private static final String ENSEMBL_HOST = "ftp://ftp.ensembl.org/pub/";

    private Properties properties;

    public DownloadCommandParser(CliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.verbose,
                downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
    }


    /**
     * Parse specific 'download' command options
     */
    public void parse() {
        this.loadProperties();
        try {
            Set<String> species = getSpecies();
            String host = getHost();
            Path outputDir = Paths.get(downloadCommandOptions.outputDir);
            for (String sp : species) {
                processSpecies(sp, outputDir);
            }
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        }

    }

    private void processSpecies(String sp, Path outputDir) {
        logger.info("Processing species " + sp);

        // output folder
        // TODO: replace('.','') is not necessary because species cannot contain '.' -> CHECK
        //       replace(' ', '') is not necessary because species cannot contain ' ' -> CHECK
        String spShortName = sp.toLowerCase().replaceAll("\\)", "").replaceAll("[-(/]", " ").replaceAll("\\s+", "_");
        Path spFolder = outputDir.resolve(spShortName);
        makeDir(spFolder);

        // download sequence, gene, variation and regulation
        if (downloadCommandOptions.sequence && specieHasInfoToDownload(sp, "sequence")) {
            downloadSequence(sp, spFolder);
        }
        if (downloadCommandOptions.gene && specieHasInfoToDownload(sp, "gene")) {
            downloadGene(sp, spFolder);
        }
        if (downloadCommandOptions.variation && specieHasInfoToDownload(sp, "variation")) {
            downloadVariation(spFolder);
        }
        if (downloadCommandOptions.regulation && specieHasInfoToDownload(sp, "regulation")) {
            downloadRegulation(spFolder);
        }
    }

    private boolean specieHasInfoToDownload(String specie, String info) {
        // TODO: implement (read from application properties json)
        logger.warn("Specie " + specie + " has no " + info + " information available to download");
        return false;
    }

    private void downloadSequence(String specie, Path spFolder) {
        Path sequenceFolder = spFolder.resolve("sequence");
        makeDir(sequenceFolder);
        S
    }


    private void downloadGene(String specie, Path spFolder) {
        Path geneFolder = spFolder.resolve("gene");
        makeDir(geneFolder);
        String phylo = getPhyloOfSpecie(specie);
    }

    private void downloadVariation(Path spFolder) {
        Path variationFolder = spFolder.resolve("variation");
        makeDir(variationFolder);
    }

    private void downloadRegulation(Path spFolder) {
        Path regulationFolder = spFolder.resolve("regulation");
        makeDir(regulationFolder);
    }

    private void makeDir(Path folderPath) {
        File folder = folderPath.toFile();
        if (!folder.exists()) {
            folder.mkdir();
        } else if (!folder.isDirectory()) {
            throw new ParameterException(folderPath.getFileName() + " exists and it is a file");
        }
    }

    private String getPhyloOfSpecie(String sp) {
        String phylo = null;
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String propertyName = (String) e.nextElement();
            // TODO: regular expression for dot (.) char
            String[] propertyElements = propertyName.split(".");
            if (sp.equals(propertyElements[1])) {
                phylo = propertyElements[0];
                break;
            }
        }
        if (phylo == null) {
            throw new ParameterException("Phylo not found for specie " + sp);
        }
        return phylo;
    }

    private Set<String> getAllPhylosInPropertiesFile() {
        Set<String> phylos = new HashSet<>();
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String propertyName = (String) e.nextElement();
            phylos.add(propertyName.split(".")[0]);
        }
        return phylos;
    }

    private void loadProperties() {
        // TODO: create properties file and implement this method
//        properties = new Properties();
//        try {
//            properties.load(new InputStreamReader(new FileInputStream(credentialsPath.toString())));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private Set<String> getSpecies() {
        Set<String> species = new HashSet<>(downloadCommandOptions.species);
        Set<String> allSpecies = getAllSpeciesFromJsonPropertiesFile();

        // check if all species exist in properties file
        for (String specie : species) {
            if (!allSpecies.contains(specie)) {
                throw new ParameterException("Specie " + specie + " not found in cellbase properties");
            }
        }

        if (species.contains("all")) {
            species = allSpecies;
        }

        return species;
    }

    private Set<String> getAllSpeciesFromJsonPropertiesFile() {
        // TODO: implement
        return null;
    }

    @Deprecated
    private Set<String> getAllSpeciesInPropertiesFile() {
        Set<String> species = new HashSet<>();
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String propertyName = (String) e.nextElement();
            String element = propertyName.split(".")[1];
            if (!element.equals(DATABASE_HOST) && !element.equals(DATABASE_PORT)) {
                species.add(element);
            }
        }
        return species;
    }

    public String getHost() {
        // TODO: in genomeFetcher.py there is an host input parameter, add it to CliOptionsParser?
        return ENSEMBL_HOST;
    }


}
