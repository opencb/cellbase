package org.opencb.cellbase.build;

import org.apache.commons.cli.*;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.build.loaders.mongodb.VariantEffectMongoDBLoader;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.serializers.json.JsonSerializer;
import org.opencb.cellbase.build.transform.*;
import org.opencb.commons.io.DataWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class CellBaseMain {

    private static Options options;
    private static CommandLine commandLine;
    private static CommandLineParser parser;

    private static CellBaseSerializer serializer = null;
    private static DataWriter newSerializer = null;

    private Logger logger;

    private static String MONGODB_SERIALIZER = "org.opencb.cellbase.lib.mongodb.serializer.MongoDBSerializer";
    private static String JSON_SERIALIZER = "org.opencb.cellbase.lib.mongodb.serializer.MongoDBSerializer";

    static {
        parser = new PosixParser();
    }

    public CellBaseMain() {
        initOptions();
    }

    private static void initOptions() {
        options = new Options();

        // Mandatory options
        options.addOption(OptionFactory.createOption("build", "Build values: core, genome_sequence, variation, protein"));
        options.addOption(OptionFactory.createOption("output", "o", "Output file or directory (depending on the 'build') to save the result to"));

        // Optional parameter for some builds that accept a folder and not only one or few files
        options.addOption(OptionFactory.createOption("indir", "i", "Input directory with data files", false));

        // Sequence and gene options
        options.addOption(OptionFactory.createOption("fasta-file", "Input FASTA file", false));

        // Gene options
        options.addOption(OptionFactory.createOption("gtf-file", "Input GTF file", false));
        options.addOption(OptionFactory.createOption("xref-file", "Input xrefs file", false));
        options.addOption(OptionFactory.createOption("description-file", "Input gene description file", false));
        options.addOption(OptionFactory.createOption("tfbs-file", "Input TFBS file", false));
        options.addOption(OptionFactory.createOption("mirna-file", "Input miRNA file", false));

        // Mutation options
        options.addOption(OptionFactory.createOption("cosmic-file", "Input COSMIC file", false));

        // Protein options
        options.addOption(OptionFactory.createOption("species", "s", "Species", false, true));
        options.addOption(OptionFactory.createOption("psimi-tab-file", "Input PsiMi tab file", false));

        // Variant effect options
        options.addOption(OptionFactory.createOption("vep-file", "Input variant effect file", false));

//        options.addOption(OptionFactory.createOption("genome-sequence-dir", "Output directory to save the JSON result", false));
//        options.addOption(OptionFactory.createOption("chunksize", "Output directory to save the JSON result", false));
        options.addOption(OptionFactory.createOption("serializer", "Serializer that will write the transformed input: "
                + "Mongo (default) or JSON (only for variant effect)", false));
        options.addOption(OptionFactory.createOption("log-level", "DEBUG -1, INFO -2, WARNING - 3, ERROR - 4, FATAL - 5", false));
        options.addOption(OptionFactory.createOption("config", "Path to serializer configuration file (if applies)", false));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        initOptions();
        try {

            if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("cellbase-build.jar", "Some options are mandatory for all possible 'builds', while others are only mandatory for some specific 'builds':", options, "\nFor more information or reporting bugs contact me: imedina@cipf.es", true);
                return;
            }

            parse(args, false);

            String buildOption = null;

            /**
             * This code create a serializer for a specific database, only
             * MongoDB has been implemented so far, DI pattern could be applied
             * to get other database outputs.
             */
//            if (commandLine.hasOption("serializer") && !commandLine.getOptionValue("serializer").equals("")) {
//                serializationOutput = commandLine.getOptionValue("serializer");
//            } else {
//                serializationOutput = "mongodb";
//            }
            String serializarClass = commandLine.getOptionValue("serializer", "json");
            Path outputPath = Paths.get(commandLine.getOptionValue("output"));
            serializer = createCellBaseSerializer(serializarClass, outputPath);


//            try {
//                serializer = (CellBaseSerializer) Class.forName(MONGODB_SERIALIZER).newInstance();
//                System.out.println(serializer);
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }


            buildOption = commandLine.getOptionValue("build");
            switch (buildOption) {
                case "genome-sequence":
                    System.out.println("In genome-sequence...");
                    String fastaFile = commandLine.getOptionValue("fasta-file");
                    if (fastaFile != null && Files.exists(Paths.get(fastaFile))) {
                        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(serializer);
                        genomeSequenceFastaParser.parse(Paths.get(fastaFile));
                    }
                    break;
                case "gene":
                    System.out.println("In gene...");
                    String geneFilesDir = commandLine.getOptionValue("indir");
                    String gtfFile = commandLine.getOptionValue("gtf-file");
                    String genomeFastaFile = commandLine.getOptionValue("fasta-file", "");
                    String xrefFile = commandLine.getOptionValue("xref-file", "");
                    String geneDescriptionFile = commandLine.getOptionValue("description-file", "");
                    String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
                    String mirnaFile = commandLine.getOptionValue("mirna-file", "");

//                    if(gtfFile != null && Files.exists(Paths.get(gtfFile))) {
                    GeneParser geneParser = new GeneParser(serializer);
                    if (geneFilesDir != null && !geneFilesDir.equals("")) {
                        geneParser.parse(Paths.get(geneFilesDir), Paths.get(genomeFastaFile));
                    } else {
                        geneParser.parse(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(genomeFastaFile));
                    }
//                    }
                    break;
                case "regulation":
                    System.out.println("In regulation");
                    String regulatoryRegionFilesDir = commandLine.getOptionValue("indir");
                    if (regulatoryRegionFilesDir != null) {
                        RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(serializer);
                        regulatoryParser.parse(Paths.get(regulatoryRegionFilesDir));
                    }
                    break;
                case "variation":
                    System.out.println("In variation...");
                    String variationFilesDir = commandLine.getOptionValue("indir");
                    if (variationFilesDir != null) {
                        VariationParser vp = new VariationParser(serializer);
                        vp.parse(Paths.get(variationFilesDir)); //, Paths.get(outfile)
                    }
                    break;
                case "variation-phen-annot":
                    System.out.println("In variation phenotype annotation...");
                    variationFilesDir = commandLine.getOptionValue("indir");
                    if (variationFilesDir != null) {
                        VariationPhenotypeAnnotationParser variationPhenotypeAnnotationParser = new VariationPhenotypeAnnotationParser(serializer);
//                    vp.parseCosmic(Paths.get(cosmicFilePath));
                        variationPhenotypeAnnotationParser.parseEnsembl(Paths.get(variationFilesDir));
                    }
                    break;
                case "vep":
                    System.out.println("In VEP parser...");
                    newSerializer = getSerializerNew(serializarClass, Paths.get(commandLine.getOptionValue("output")), VariantEffect.class);
                    String effectFile = commandLine.getOptionValue("vep-file");
                    VariantEffectParser effectParser = new VariantEffectParser(serializer);
                    effectParser.parse(Paths.get(effectFile));

//                    if (effectFile != null && Files.exists(Paths.get(effectFile))) {
//                        if (newSerializer instanceof JsonSerializer) {
//                            VariantEffectParser effectParser = new VariantEffectParser(newSerializer);
//                            effectParser.parse(Paths.get(effectFile));
//                        } else if (newSerializer instanceof VariantEffectMongoDBLoader) {
//                            JsonReader<VariantEffect> effectParser = new JsonReader<>(Paths.get(effectFile), VariantEffect.class, newSerializer);
//                            effectParser.open();
//                            effectParser.pre();
//                            effectParser.parse();
//                            effectParser.post();
//                            effectParser.close();
//                        }
//                    }
                    break;
                case "protein":
                    System.out.println("In protein...");
                    String uniprotSplitFilesDir = commandLine.getOptionValue("indir");
                    String species = commandLine.getOptionValue("species");
                    if (uniprotSplitFilesDir != null && Files.exists(Paths.get(uniprotSplitFilesDir))) {
                        ProteinParser proteinParser = new ProteinParser(serializer);
                        proteinParser.parse(Paths.get(uniprotSplitFilesDir), species);
                    }
                    break;
                case "mutation":
                    System.out.println("In mutation");
                    /**
                     * File from Cosmic: CosmicCompleteExport_XXX.tsv
                     */
                    String cosmicFilePath = commandLine.getOptionValue("cosmic-file");
                    if (cosmicFilePath != null) {
                        MutationParser vp = new MutationParser(serializer);
//                    vp.parseCosmic(Paths.get(cosmicFilePath));
                        vp.parseCosmic(Paths.get(cosmicFilePath));
                    }
                    break;

                case "conservation":
                    System.out.println("In conservation");
                    String conservationFilesDir = commandLine.getOptionValue("indir");
                    int conservationChunkSize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
                    String conservationOutputFile = commandLine.getOptionValue("output", "/tmp/conservation.json");
                    if (conservationFilesDir != null) {
                        ConservedRegionParser.parseConservedRegionFilesToJson(Paths.get(conservationFilesDir), conservationChunkSize, Paths.get(conservationOutputFile));
                    }
                    break;
                case "ppi":
                    System.out.println("In PPI...");
                    String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
                    if (psimiTabFile != null) {
                        InteractionParser interactionParser = new InteractionParser(serializer);
                        interactionParser.parse(Paths.get(psimiTabFile), commandLine.getOptionValue("species").toString());
                    }
                    break;
                case "all":
                    System.out.println("In all...");
                    String speciesDataDir = commandLine.getOptionValue("indir");
//                    String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
                    parseAll(Paths.get(speciesDataDir));

            }
            serializer.close();
        } catch (ParseException | IOException | SQLException | ClassNotFoundException | NoSuchMethodException | FileFormatException | InterruptedException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static void parse(String[] args, boolean stopAtNoOption) throws ParseException, IOException {
        parser = new PosixParser();
        commandLine = parser.parse(options, args, stopAtNoOption);

        //		if(commandLine.hasOption("outdir")) {
        //			this.outdir = commandLine.getOptionValue("outdir");
        //		}
        //		if(commandLine.hasOption("log-level")) {
        //			logger.setLevel(Integer.parseInt(commandLine.getOptionValue("log-level")));
        //		}
        if (args.length > 0 && "variation".equals(args[1])) {
            System.out.println("variation SQL test");
        }
    }

    private static CellBaseSerializer createCellBaseSerializer(String serializerClass, Path outPath) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if(serializerClass != null) {
            // A default implementation for JSON is provided
            if(serializerClass.equalsIgnoreCase("json")) {
                System.out.println("JSON serializer");
                return (CellBaseSerializer) Class.forName(JSON_SERIALIZER).getConstructor(Path.class).newInstance(outPath);
//                return new MongoDBSerializerOld(outPath);
            }else {
                System.out.println(MONGODB_SERIALIZER+" serializer");
                return (CellBaseSerializer) Class.forName(MONGODB_SERIALIZER).getConstructor(Path.class).newInstance(outPath);
            }
        }
        return serializer;

//        switch (serializarClass) {
//            case "json":
//                return new MongoDBSerializer(outPath);
//            case "mongodb":
//                return new MongoDBSerializer(outPath);
//            default:
//                return null;
//        }

    }

    private static DataWriter getSerializerNew(String serializationOutput, Path outPath, Class clazz) throws IOException {
        switch (serializationOutput) {
            case "json":
                return new JsonSerializer(outPath);
            case "mongodb":
                if (clazz.equals(VariantEffect.class)) {
                    Properties properties = new Properties();
                    properties.load(CellBaseMain.class.getResource("/application.properties").openStream());
                    return new VariantEffectMongoDBLoader(properties);
                }
            default:
                return null;
        }
    }


    private static void parseAll(Path speciesInDir) throws NoSuchMethodException, FileFormatException, IOException, InterruptedException, SQLException, ClassNotFoundException {
        Path genomeFastaPath = null;
        for (String fileName : speciesInDir.resolve("sequence").toFile().list()) {
            if (fileName.endsWith(".fa") || fileName.endsWith(".fa.gz")) {
                genomeFastaPath = speciesInDir.resolve("sequence").resolve(fileName);
                break;
            }
        }
        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(serializer);
        genomeSequenceFastaParser.parse(genomeFastaPath);

        GeneParser geneParser = new GeneParser(serializer);
        geneParser.parse(speciesInDir.resolve("gene"), genomeFastaPath);

        Path variationPath = speciesInDir.resolve("variation");
        if (variationPath.toFile().list().length > 2) {
            VariationParser vp = new VariationParser(serializer);
            vp.parse(variationPath);
        }

        Path regulationPath = speciesInDir.resolve("regulation");
        if (variationPath.toFile().list().length > 2) {
            RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(serializer);
            regulatoryParser.parse(regulationPath);
        }
    }

}
