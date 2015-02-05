package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
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
            String outputDir = downloadCommandOptions.outputDir;
            for (String sp : species) {
                processSpecies(sp);
            }
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        }

    }

    private void processSpecies(String sp) {
        logger.info("Processing species " + sp);
        String phylo = getPhyloOfSpecie(sp);
        String databaseHost = properties.getProperty(phylo + "." + DATABASE_HOST);
        Integer databasePort = Integer.parseInt(properties.getProperty(phylo + "." + DATABASE_HOST));
//        ## preparing some variables
//                sp_short = sp.lower().replace('.', '').replace('=', '').replace(')', '').replace('-', ' ').replace('(', ' ').replace('/', ' ').replace('  ', ' ').replace(' ', '_')
//        logging.debug(sp_short)
//        sp_folder = outdir+"/{0}".format(sp_short)
//        sequence_folder = outdir+"/{0}/sequence".format(sp_short)
//        gene_folder = outdir+"/{0}/gene".format(sp_short)
//        variation_folder = outdir+"/{0}/variation".format(sp_short)
//        regulation_folder = outdir+"/{0}/regulation".format(sp_short)
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
        Set<String> allSpecies = getAllSpeciesInPropertiesFile();

        if (species.contains("all")) {
            species = allSpecies;
        } else {
            // check if all species exist in properties file
            for (String specie : species) {
                if (!allSpecies.contains(specie)) {
                    throw new ParameterException("Specie " + specie + " doesn't exist in properties file");
                }
            }
        }
        return species;
    }

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
        // TODO: in genomeFetcher.py there is an host option, add it to CliOptionsParser?
        return ENSEMBL_HOST;
    }


}
