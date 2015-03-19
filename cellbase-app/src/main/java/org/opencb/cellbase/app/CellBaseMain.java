package org.opencb.cellbase.app;

import org.opencb.cellbase.app.cli.*;

import java.io.IOException;
import java.net.URISyntaxException;

public class CellBaseMain {

//    public static final String SPECIES_OPTION = "species";
//    public static final String INDIR_OPTION = "indir";
//    public static final String CLINVAR_FILE_OPTION = "clinvar-file";
//    public static final String COSMIC_FILE_OPTION = "cosmic-file";
//    public static final String GWAS_FILE_OPTION = "gwas-file";
//    public static final String FASTA_FILE_OPTION = "fasta-file";
//    public static final String VEP_FILE_OPTION = "vep-file";
//    public static final String PSIMI_TAB_FILE = "psimi-tab-file";
//    public static final String DBSNP_FILE_OPTION = "dbsnp-file";
//    public static final String CHUNK_SIZE_OPTION = "chunksize";
//    public static final String DRUG_FILE_OPTION = "drug-file";
//    public static final String ASSEMBLY_OPTION = "assembly";
//
//    private static Options options;
//    private static CommandLine commandLine;
//
//    private static CellBaseSerializer serializer;
//
//    private static Logger logger;
//
//    private static void initOptions() {
//        options = new Options();
//
//        // Mandatory options
//        options.addOption(OptionFactory.createOption("build", "Build values: core, genome_sequence, variation, protein"));
//        options.addOption(OptionFactory.createOption("output", "o", "Output file or directory (depending on the 'build') to save the result to"));
//
//        // Optional parameter for some builds that accept a folder and not only one or few files
//        options.addOption(OptionFactory.createOption(INDIR_OPTION, "i", "Input directory with data files", false));
//
//        // Sequence and gene options
//        options.addOption(OptionFactory.createOption(FASTA_FILE_OPTION, "Input FASTA file", false));
//
//        // Gene options
//        options.addOption(OptionFactory.createOption("gtf-file", "Input GTF file", false));
//        options.addOption(OptionFactory.createOption("xref-file", "Input xrefs file", false));
//        options.addOption(OptionFactory.createOption("description-file", "Input gene description file", false));
//        options.addOption(OptionFactory.createOption("tfbs-file", "Input TFBS file", false));
//        options.addOption(OptionFactory.createOption("mirna-file", "Input miRNA file", false));
//
//        // Mutation options
//        options.addOption(OptionFactory.createOption(COSMIC_FILE_OPTION, "Input COSMIC file", false));
//
//        // Drug options
//        options.addOption(OptionFactory.createOption(DRUG_FILE_OPTION, "Output directory to save the JSON result", false));
//
//        // ClinVar
//        options.addOption(OptionFactory.createOption(CLINVAR_FILE_OPTION, "Input Clinvar XML file", false));
//        options.addOption(OptionFactory.createOption(ASSEMBLY_OPTION, "Human Genome Assembly. Possible values: " + ClinVarParser.GRCH37_ASSEMBLY + ", " + ClinVarParser.GRCH38_ASSEMBLY, false));
//
//        // gwas
//        options.addOption(OptionFactory.createOption(GWAS_FILE_OPTION, "Input gwas file", false));
//        options.addOption(OptionFactory.createOption(DBSNP_FILE_OPTION, "Input .gz dbsnp file, used in gwas parsing", false));
//
//        // Protein options
//        options.addOption(OptionFactory.createOption(SPECIES_OPTION, "s", "Species", false, true));
//        options.addOption(OptionFactory.createOption(PSIMI_TAB_FILE, "Input PsiMi tab file", false));
//
//        // Variant effect options
//        options.addOption(OptionFactory.createOption(VEP_FILE_OPTION, "Input variant effect file", false));
//
////        options.addOption(OptionFactory.createOption("genome-sequence-dir", "Output directory to save the JSON result", false));
////        options.addOption(OptionFactory.createOption("chunksize", "Output directory to save the JSON result", false));
//        options.addOption(OptionFactory.createOption("serializer", "Serializer that will write the transformed input: "
//                + "Mongo (default) or JSON (only for variant effect)", false));
//        options.addOption(OptionFactory.createOption("log-level", "DEBUG -1, INFO -2, WARNING - 3, ERROR - 4, FATAL - 5", false));
//        options.addOption(OptionFactory.createOption("config", "Path to serializer configuration file (if applies)", false));
//    }

    public static void main(String[] args) {
        CliOptionsParser cliOptionsParser = new CliOptionsParser();
        cliOptionsParser.parse(args);

        String parsedCommand = cliOptionsParser.getCommand();
        if(parsedCommand == null || parsedCommand.isEmpty()) {
            if(cliOptionsParser.getGeneralOptions().help) {
                cliOptionsParser.printUsage();
            }
            if(cliOptionsParser.getGeneralOptions().version) {
                System.out.println("version = 3.1.0");
            }
        }else {
            CommandExecutor commandExecutor = null;
            switch (parsedCommand) {
                case "download":
                    if (cliOptionsParser.getDownloadCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new DownloadCommandExecutor(cliOptionsParser.getDownloadCommandOptions());
                    }
                    break;
                case "build":
                    if (cliOptionsParser.getBuildCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new BuildCommandExecutor(cliOptionsParser.getBuildCommandOptions());
                    }
                    break;
                case "load":
                    if (cliOptionsParser.getLoadCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new LoadCommandExecutor(cliOptionsParser.getLoadCommandOptions());
                    }
                    break;
                case "query":
                    if (cliOptionsParser.getQueryCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new QueryCommandExecutor(cliOptionsParser.getQueryCommandOptions());
                    }
                    break;
                case "variant-annotation":
                    if (cliOptionsParser.getQueryCommandOptions().commonOptions.help) {
                        cliOptionsParser.printUsage();
                    } else {
                        commandExecutor = new VariantAnnotationCommandExecutor(cliOptionsParser.getVariantAnnotationCommandOptions());
                    }
                    break;
                default:
                    break;
            }

            if (commandExecutor != null) {
                try {
                    commandExecutor.loadCellBaseConfiguration();
                    commandExecutor.execute();
                } catch (IOException|URISyntaxException ex) {
                    commandExecutor.getLogger().error("Error reading cellbase configuration: " + ex.getMessage());
                }
            }
        }

//        String buildOption;
//
//        initOptions();
//
//        try {
//            // Help option is checked manually, otherwise the parser will complain about obligatory options
//            if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
//                HelpFormatter formatter = new HelpFormatter();
//                formatter.printHelp("cellbase-build.jar", "Some options are mandatory for all possible 'builds', " +
//                        "while others are only mandatory for some specific 'builds':", options,
//                        "\nFor more information or reporting bugs contact me: imedina@cipf.es", true);
//                return;
//            }
//
//            parseCommandLineArgs(args, false);
//
//            // This small hack allow to configure the appropriate Logger level from the command line, this is done
//            // by setting the DEFAULT_LOG_LEVEL_KEY before the logger object is created.
//            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, commandLine.getOptionValue("log-level", "info"));
//            logger = LoggerFactory.getLogger("org.opencb.cellbase.build.CellBaseMain");
//
//            Path outputPath = Paths.get(commandLine.getOptionValue("output"));
//            createCellBaseSerializer(outputPath);
//
//            buildOption = commandLine.getOptionValue("build");
//            if (buildOption.equals("all")) {
//                buildAll();
//            } else {
//                CellBaseParser parser = CellBaseParserFactory.createParser(buildOption, outputPath, serializer, commandLine);
//                parser.parse();
//                parser.disconnect();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }


//    private static void buildAll() throws NoSuchMethodException, FileFormatException, IOException, InterruptedException, SQLException, ClassNotFoundException {
//        logger.info("Processing all...");
//        Path speciesInDir = Paths.get(commandLine.getOptionValue(INDIR_OPTION));
//        Path genomeFastaPath = getGenomeFastaPath(speciesInDir);
//
//        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(genomeFastaPath, null);
//        genomeSequenceFastaParser.parse();
//        genomeSequenceFastaParser.disconnect();
//
//        GeneParser geneParser = new GeneParser(speciesInDir.resolve("gene"), genomeFastaPath, serializer);
//        geneParser.parse();
//        geneParser.disconnect();
//
//        Path variationPath = speciesInDir.resolve("variation");
//        if (variationPath.toFile().list().length > 2) {
//            VariationParser vp = new VariationParser(variationPath, null);
//            vp.parse();
//            vp.disconnect();
//        }
//
//        Path regulationPath = speciesInDir.resolve("regulation");
//        if (variationPath.toFile().list().length > 2) {
//            RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(regulationPath, null);
//            regulatoryParser.parse();
//        }
//    }
//
//    private static Path getGenomeFastaPath(Path speciesInDir) {
//        Path genomeFastaPath = null;
//        for (String fileName : speciesInDir.resolve("sequence").toFile().list()) {
//            if (fileName.endsWith(".fa") || fileName.endsWith(".fa.gz")) {
//                genomeFastaPath = speciesInDir.resolve("sequence").resolve(fileName);
//                break;
//            }
//        }
//        return genomeFastaPath;
//    }
//
//    private static void parseCommandLineArgs(String[] args, boolean stopAtNoOption) throws ParseException, IOException {
//        CommandLineParser parser = new PosixParser();
//        commandLine = parser.parse(options, args, stopAtNoOption);
//    }
//
//    private static void createCellBaseSerializer(Path outPath) throws IOException  {
//        String serializerClass = commandLine.getOptionValue("serializer", "json");
//        if (serializerClass != null) {
//            // A default implementation for JSON is provided
//            if (serializerClass.equalsIgnoreCase("json")) {
//                logger.debug("JSON serializer chosen");
//                serializer = new DefaultJsonSerializer(outPath);
//            } else {
//                logger.debug("MongoDB serializer chosen");
//                serializer = new MongoDBSerializer(outPath);
//            }
//        }
//    }
}
