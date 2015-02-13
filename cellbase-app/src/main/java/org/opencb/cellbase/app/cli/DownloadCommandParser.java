package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.CellBaseConfiguration;
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

    public static final String DATABASE_HOST = "databaseHost";
    public static final String DATABASE_PORT = "databasePort";
    private CliOptionsParser.DownloadCommandOptions downloadCommandOptions;

    public DownloadCommandParser(CliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.verbose,
                downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
    }


    /**
     * Parse specific 'download' command options
     */
    public void parse() {
        try {
            Set<Species> speciesToDownload = getSpecies();
            Path outputDir = Paths.get(downloadCommandOptions.outputDir);
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
            downloadRegulation(sp, spFolder);
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
        Path sequenceFolder = spFolder.resolve("sequence");
        makeDir(sequenceFolder);
        String url = getSequenceUrl(sp, shortName, host);
        String outputFileName = StringUtils.capitalize(shortName) + "." + downloadCommandOptions.assembly + ".fa.gz";
        Path outputPath = sequenceFolder.resolve(outputFileName);
        downloadFile(url, outputPath.toString());
        // TODO: genome info.pl!!
    }

    private String getSequenceUrl(Species sp, String shortName, String host) {
        String seqUrl;

        String ensemblRelease = "/release-" + getEnsemblVersion(sp, downloadCommandOptions.assembly).split("_")[0];
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            seqUrl = host + ensemblRelease;
        } else {
            seqUrl = host + ensemblRelease + "/" + getPhylo(sp);
        }

        seqUrl = seqUrl + "/fasta/" + shortName + "/dna/*.dna.primary_assembly.fa.gz";

        return seqUrl;
    }

    private String getEnsemblVersion(Species sp, String assembly) {
        if (assembly == null) {
            if (sp.getAssemblies().size() > 1) {
                // TODO: enumerate available assemblies in error message
                throw new ParameterException("Specie " + sp.getScientificName() + " has several assemblies: should choose one using --assembly option");
                // TODO: should ask the user what assembly use?
            } else {
                // TODO: full ensembl version (78_38) or just 78??
                return sp.getAssemblies().get(0).getEnsemblVersion();
            }
        }
        for (Species.Assembly spAssembly : sp.getAssemblies()) {
            if (spAssembly.getName().equalsIgnoreCase(assembly)) {
                // TODO: full ensembl version (78_38) or just 78??
                return spAssembly.getEnsemblVersion();
            }
        }
        // TODO: enumerate available assemblies in error message??
        throw new ParameterException("Assembly " + assembly + " not found in species " + sp.getScientificName());
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


    private void downloadGene(Species sp, Path spFolder) {
        Path geneFolder = spFolder.resolve("gene");
        makeDir(geneFolder);

    }

    private void downloadVariation(Species sp, String shortName, Path spFolder, String host) throws IOException, InterruptedException {
        Path variationFolder = spFolder.resolve("variation");
        makeDir(variationFolder);
        String variationUrl = this.getVariationUrl(sp, shortName, host);
        String[] variationFiles = {"variation.txt.gz", "variation_feature.txt.gz", "transcript_variation.txt.gz",
                "variation_synonym.txt.gz", "seq_region.txt.gz", "source.txt.gz", "attrib.txt.gz",
                "attrib_type.txt.gz", "seq_region.txt.gz", "structural_variation_feature.txt.gz",
                "study.txt.gz", "phenotype.txt.gz", "phenotype_feature.txt.gz",
                "phenotype_feature_attrib.txt.gz", "motif_feature_variation.txt.gz", "genotype_code.txt.gz",
                "allele_code.txt.gz", "population_genotype.txt.gz", "population.txt.gz", "allele.txt.gz"};
        for (String variationFile : variationFiles) {
            Path outputFile = variationFolder.resolve(variationFile);
            downloadFile(variationUrl + "/" + variationFile, outputFile.toString());
        }
    }

    private String getVariationUrl(Species sp, String shortName, String host) {
        String seqUrl;

        String ensemblVersion = getEnsemblVersion(sp, downloadCommandOptions.assembly);
        String ensemblRelease = "/release-" + ensemblVersion.split("_")[0];
        seqUrl = host + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(sp)) {
            seqUrl = host + ensemblRelease + "/" + getPhylo(sp);
        }

        seqUrl = seqUrl + "/mysql/" + shortName + "_variation_" + ensemblVersion;

        return seqUrl;
    }

    private void downloadRegulation(Species sp, Path spFolder) {
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

    private void downloadFile(String url, String outputFileName) throws IOException, InterruptedException {
        String downloadCommandLine = "wget --tries=10 " + url + " -O '" + outputFileName + "' -o " + outputFileName + ".log";
//        Process process = Runtime.getRuntime().exec(downloadCommandLine);
//        process.waitFor();
        System.out.println(downloadCommandLine);
        // TODO: output value? standard output messages?

    }
}
