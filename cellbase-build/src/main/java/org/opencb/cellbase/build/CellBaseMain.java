package org.opencb.cellbase.build;

import org.apache.commons.cli.*;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.build.serializers.json.CellBaseJsonSerializer;
import org.opencb.cellbase.build.transform.*;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class CellBaseMain {

    private static Options options;
    private static CommandLine commandLine;
    private static CommandLineParser parser;

    private static CellBaseSerializer serializer = null;
    private static org.opencb.cellbase.build.serializers.CellBaseSerializer newSerializer = null;

    private static Logger logger;

    private static String JSON_SERIALIZER = "org.opencb.cellbase.core.serializer.DefaultJsonSerializer";
    private static String MONGODB_SERIALIZER = "org.opencb.cellbase.lib.mongodb.serializer.MongoDBSerializer";

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

        // Drug options
        options.addOption(OptionFactory.createOption("drug-file", "Output directory to save the JSON result", false));

        // ClinVar
        options.addOption(OptionFactory.createOption("clinvar-file", "Output directory to save the JSON result", false));

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
        String buildOption = null;
        String serializationOutput = null;

        initOptions();

        try {
            // Help option is checked manually, otherwise the parser will complain about obligatory options
            if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("cellbase-build.jar", "Some options are mandatory for all possible 'builds', while others are only mandatory for some specific 'builds':", options, "\nFor more information or reporting bugs contact me: imedina@cipf.es", true);
                return;
            }

            // Now we can parse the command line
            parse(args, false);

            // This small hack allow to configure the appropriate Logger level from the command line, this is done
            // by setting the DEFAULT_LOG_LEVEL_KEY before the logger object is created.
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, commandLine.getOptionValue("log-level", "info"));
            logger = LoggerFactory.getLogger("org.opencb.cellbase.build.CellBaseMain");


            /**
             * This code use Java reflection to create a data serializer for a specific database engine,
             * only a default JSON and MongoDB serializers have been implemented so far, this DI pattern
             * may be applied to get other database outputs.
             * This is in charge of creating the specific data model for the database backend.
             */
            String serializerClass = commandLine.getOptionValue("serializer", "json");
            Path outputPath = Paths.get(commandLine.getOptionValue("output"));
            serializer = createCellBaseSerializer(serializerClass, outputPath);


            buildOption = commandLine.getOptionValue("build");
            switch (buildOption) {
                case "genome-sequence":
                    logger.info("Processing genome-sequence...");
                    String fastaFile = commandLine.getOptionValue("fasta-file");
                    if (fastaFile != null && Files.exists(Paths.get(fastaFile))) {

                        CellBaseJsonSerializer gsfpSerializer = new CellBaseJsonSerializer(outputPath.resolve("genome_sequence.json"));
                        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(Paths.get(fastaFile), gsfpSerializer);
                        genomeSequenceFastaParser.parse();
                        genomeSequenceFastaParser.disconnect();
                    }
                    break;
                case "gene": // TODO aaleman:
//                    logger.info("Processing gene...");
                    String geneFilesDir = commandLine.getOptionValue("indir");
                    String gtfFile = commandLine.getOptionValue("gtf-file");
                    String genomeFastaFile = commandLine.getOptionValue("fasta-file", "");
                    String xrefFile = commandLine.getOptionValue("xref-file", "");
                    String uniprotIdMapping = commandLine.getOptionValue("uniprot-id-mapping-file", "");
                    String geneDescriptionFile = commandLine.getOptionValue("description-file", "");
                    String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
                    String mirnaFile = commandLine.getOptionValue("mirna-file", "");

//                    if(gtfFile != null && Files.exists(Paths.get(gtfFile))) {
                    GeneParser geneParser = new GeneParser(Paths.get(geneFilesDir), Paths.get(genomeFastaFile));
                    if (geneFilesDir != null && !geneFilesDir.equals("")) {
                        geneParser.parse();
                    } else {
                        geneParser.parse(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(uniprotIdMapping), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(genomeFastaFile));
                    }
//                    }
                    break;
                case "regulation":
                    logger.info("Processing regulation");
                    String regulatoryRegionFilesDir = commandLine.getOptionValue("indir");
                    if (regulatoryRegionFilesDir != null) {
                        CellBaseJsonSerializer rSerializer = new CellBaseJsonSerializer(outputPath.resolve("regulatory_region.json"));

                        RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(Paths.get(regulatoryRegionFilesDir), rSerializer);
                        regulatoryParser.parse();
                    }
                    break;
                case "variation":
                    logger.info("Processing variation...");
                    String variationFilesDir = commandLine.getOptionValue("indir");
                    if (variationFilesDir != null) {
                        CellBaseJsonSerializer vSerializer = new CellBaseJsonSerializer(outputPath.resolve("variation.json"));
                        VariationParser vp = new VariationParser(Paths.get(variationFilesDir), vSerializer);
                        vp.parse();
                    }
                    break;
                case "variation-phen-annot":
                    logger.info("Processing variation phenotype annotation...");
                    variationFilesDir = commandLine.getOptionValue("indir");
                    if (variationFilesDir != null) {

                        CellBaseJsonSerializer vSerializer = new CellBaseJsonSerializer(outputPath.resolve("variation_phenotype_annotation.json"));


                        VariationPhenotypeAnnotationParser variationPhenotypeAnnotationParser = new VariationPhenotypeAnnotationParser(Paths.get(variationFilesDir), vSerializer);
//                    vp.parseCosmic(Paths.get(cosmicFilePath));
                        variationPhenotypeAnnotationParser.parse();
                    }
                    break;
                case "vep": // TODO aaleman:
                    logger.info("Processing VEP parser...");
                    newSerializer = getSerializerNew(serializerClass, Paths.get(commandLine.getOptionValue("output")), VariantEffect.class);
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
                    logger.info("Processing protein...");
                    String uniprotSplitFilesDir = commandLine.getOptionValue("indir");
                    String species = commandLine.getOptionValue("species");
                    if (uniprotSplitFilesDir != null && Files.exists(Paths.get(uniprotSplitFilesDir))) {
                        CellBaseJsonSerializer pSerializer = new CellBaseJsonSerializer(outputPath.resolve("protein.json"));
                        ProteinParser proteinParser = new ProteinParser(Paths.get(uniprotSplitFilesDir), species, pSerializer);
                        proteinParser.parse();
                    }
                    break;
                case "mutation": // TODO aaleman: fix parser
                    logger.info("Processing mutation");
                    /**
                     * File from Cosmic: CosmicCompleteExport_XXX.tsv
                     */
                    String cosmicFilePath = commandLine.getOptionValue("cosmic-file");
                    if (cosmicFilePath != null) {

                        CellBaseJsonSerializer mSerializer = new CellBaseJsonSerializer(outputPath.resolve("mutation.json"));
                        MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
                        vp.parse();
                    }
                    break;

                case "conservation":
                    logger.info("Processing conservation");
                    String conservationFilesDir = commandLine.getOptionValue("indir");
                    int conservationChunkSize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
                    String conservationOutputFile = commandLine.getOptionValue("output", "/tmp/conservation.json");
                    if (conservationFilesDir != null) {
                        ConservedRegionParser conservedRegionParser = new ConservedRegionParser();
                        conservedRegionParser.parse(Paths.get(conservationFilesDir), conservationChunkSize, Paths.get(conservationOutputFile));
                    }
                    break;
                case "ppi":
                    logger.info("Processing PPI...");
                    String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
                    if (psimiTabFile != null) {
                        InteractionParser interactionParser = new InteractionParser(serializer);
                        interactionParser.parse(Paths.get(psimiTabFile), commandLine.getOptionValue("species").toString());
                    }
                    break;
                case "drug":
                    logger.info("Processing drug...");
                    String drugFile = commandLine.getOptionValue("drug-file");
                    if (drugFile != null) {
                        DrugParser drugParser = new DrugParser(serializer);
                        drugParser.parse(Paths.get(drugFile));
                    }
                    break;
                case "clinvar":
                    logger.info("Processing ClinVar...");
                    String clinvarFile = commandLine.getOptionValue("clinvar-file");
                    if (clinvarFile != null) {
                        ClinVarParser clinVarParser = new ClinVarParser(serializer);
                        clinVarParser.parse(Paths.get(clinvarFile));
                    }
                    break;
                case "all":
                    logger.info("Processing all...");
                    String speciesDataDir = commandLine.getOptionValue("indir");
//                    String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
                    parseAll(Paths.get(speciesDataDir));
                    break;
                default:
                    break;

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
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
        if (serializerClass != null) {
            // A default implementation for JSON is provided
            if (serializerClass.equalsIgnoreCase("json")) {
                logger.debug("JSON serializer chosen");
                return (CellBaseSerializer) Class.forName(JSON_SERIALIZER).getConstructor(Path.class).newInstance(outPath);
            } else {
                logger.debug("MongoDB serializer chosen");
                return (CellBaseSerializer) Class.forName(MONGODB_SERIALIZER).getConstructor(Path.class).newInstance(outPath);
            }
        }
        return serializer;
    }

    private static org.opencb.cellbase.build.serializers.CellBaseSerializer getSerializerNew(String serializationOutput, Path outPath, Class clazz) throws IOException {
        switch (serializationOutput) {
            case "json":
                return new CellBaseJsonSerializer(outPath);
            case "mongodb":
//                if (clazz.equals(VariantEffect.class)) {
//                    Properties properties = new Properties();
//                    properties.load(CellBaseMain.class.getResource("/application.properties").openStream());
//                    return new VariantEffectMongoDBLoader(properties);
//                }
                return null;
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
        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(genomeFastaPath, null);
        genomeSequenceFastaParser.parse();

        GeneParser geneParser = new GeneParser(speciesInDir.resolve("gene"), genomeFastaPath);
        geneParser.parse();

        Path variationPath = speciesInDir.resolve("variation");
        if (variationPath.toFile().list().length > 2) {
            VariationParser vp = new VariationParser(variationPath, null);
            vp.parse();
        }

        Path regulationPath = speciesInDir.resolve("regulation");
        if (variationPath.toFile().list().length > 2) {
            RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(regulationPath, null);
            regulatoryParser.parse();
        }
    }

}
