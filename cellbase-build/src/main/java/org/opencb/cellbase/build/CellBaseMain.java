package org.opencb.cellbase.build;

import org.apache.commons.cli.*;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.cellbase.build.transform.*;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.serializer.DefaultJsonSerializer;
import org.opencb.cellbase.lib.mongodb.serializer.MongoDBSerializer;
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

    private static Logger logger;

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
        options.addOption(OptionFactory.createOption("clinvar-file", "Input Clinvar XML file", false));

        // gwas
        options.addOption(OptionFactory.createOption("gwas-file", "Input gwas file", false));
        options.addOption(OptionFactory.createOption("dbsnp-file", "Input .gz dbsnp file, used in gwas parsing", false));

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

            Path outputPath = Paths.get(commandLine.getOptionValue("output"));
            createCellBaseSerializer(outputPath);

            buildOption = commandLine.getOptionValue("build");
            switch (buildOption) {
                case "genome-sequence":
                    buildGenomeSequence(outputPath);
                    break;
                case "gene":
                    buildGene();
                    break;
                case "regulation":
                    buildRegulation(outputPath);
                    break;
                case "variation":
                    buildVariation(outputPath);
                    break;
                case "variation-phen-annot":
                    buildVariationPhenotypeAnnotation(outputPath);
                    break;
                case "vep":
                    buildVep();
                    break;
                case "protein":
                    buildProtein(outputPath);
                    break;
                case "conservation":
                    buildConservation();
                    break;
                case "ppi":
                    buildPpi();
                    break;
                case "drug":
                    buildDrug();
                    break;
                case "clinvar":
                    buildClinvar(outputPath);
                    break;
                case "cosmic":
                    buildCosmic(outputPath);
                    break;
                case "gwas":
                    buildGwas(outputPath);
                    break;
                case "all":
                    buildAll();
                    break;
                default:
                    break;
            }
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

    private static void buildAll() throws NoSuchMethodException, FileFormatException, IOException, InterruptedException, SQLException, ClassNotFoundException {
        logger.info("Processing all...");
        Path speciesInDir = Paths.get(commandLine.getOptionValue("indir"));
//                    String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
        Path genomeFastaPath = null;
        for (String fileName : speciesInDir.resolve("sequence").toFile().list()) {
            if (fileName.endsWith(".fa") || fileName.endsWith(".fa.gz")) {
                genomeFastaPath = speciesInDir.resolve("sequence").resolve(fileName);
                break;
            }
        }
        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(genomeFastaPath, null);
        genomeSequenceFastaParser.parse();
        genomeSequenceFastaParser.disconnect();

        GeneParser geneParser = new GeneParser(speciesInDir.resolve("gene"), genomeFastaPath, serializer);
        geneParser.parse();
        geneParser.disconnect();

        Path variationPath = speciesInDir.resolve("variation");
        if (variationPath.toFile().list().length > 2) {
            VariationParser vp = new VariationParser(variationPath, null);
            vp.parse();
            vp.disconnect();
        }

        Path regulationPath = speciesInDir.resolve("regulation");
        if (variationPath.toFile().list().length > 2) {
            RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(regulationPath, null);
            regulatoryParser.parse();
        }
    }

    private static void buildDrug() throws JAXBException, IOException {
        // TODO: DrugParser should extend CellbaseParser
        logger.info("Processing drug...");
        String drugFile = commandLine.getOptionValue("drug-file");
        if (drugFile != null) {
            DrugParser drugParser = new DrugParser(serializer);
            drugParser.parse(Paths.get(drugFile));
        }
    }

    private static void buildPpi() throws IOException {
        logger.info("Processing PPI...");
        String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
        if (psimiTabFile != null) {
            InteractionParser interactionParser = new InteractionParser(Paths.get(psimiTabFile), commandLine.getOptionValue("species"), serializer);
            interactionParser.parse();
            interactionParser.disconnect();
        }
    }

    private static void buildConservation() throws IOException {
        logger.info("Processing conservation...");
        String conservationFilesDir = commandLine.getOptionValue("indir");
        int conservationChunkSize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
        if (conservationFilesDir != null) {
            ConservedRegionParser conservedRegionParser = new ConservedRegionParser(Paths.get(conservationFilesDir), conservationChunkSize, serializer);
            conservedRegionParser.parse();
            conservedRegionParser.disconnect();
        }
    }

    private static void buildProtein(Path outputPath) throws IOException {
        logger.info("Processing protein...");
        String uniprotSplitFilesDir = commandLine.getOptionValue("indir");
        String species = commandLine.getOptionValue("species");
        if (uniprotSplitFilesDir != null && Files.exists(Paths.get(uniprotSplitFilesDir))) {
            DefaultJsonSerializer pSerializer = new DefaultJsonSerializer(outputPath, Paths.get("protein.json"));
            ProteinParser proteinParser = new ProteinParser(Paths.get(uniprotSplitFilesDir), species, pSerializer);
            proteinParser.parse();
            proteinParser.disconnect();
        }
    }

    private static void buildVep() throws IOException {
        logger.info("Processing VEP parser...");
        String effectFile = commandLine.getOptionValue("vep-file");
        VariantEffectParser effectParser = new VariantEffectParser(Paths.get(effectFile), serializer);
        effectParser.parse();
        effectParser.disconnect();
    }

    private static void buildVariationPhenotypeAnnotation(Path outputPath) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        logger.info("Processing variation phenotype annotation...");
        String variationFilesDir = commandLine.getOptionValue("indir");
        if (variationFilesDir != null) {
            DefaultJsonSerializer vSerializer = new DefaultJsonSerializer(outputPath);

            VariationPhenotypeAnnotationParser variationPhenotypeAnnotationParser = new VariationPhenotypeAnnotationParser(Paths.get(variationFilesDir), vSerializer);
            variationPhenotypeAnnotationParser.parse();
            variationPhenotypeAnnotationParser.disconnect();
        }
    }

    private static void buildVariation(Path outputPath) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        logger.info("Processing variation...");
        String variationFilesDir = commandLine.getOptionValue("indir");
        if (variationFilesDir != null) {
            DefaultJsonSerializer vSerializer = new DefaultJsonSerializer(outputPath, Paths.get("variation.json"));
            VariationParser vp = new VariationParser(Paths.get(variationFilesDir), vSerializer);
            vp.parse();
            vp.disconnect();
        }
    }

    private static void buildRegulation(Path outputPath) throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException {
        logger.info("Processing regulation...");
        String regulatoryRegionFilesDir = commandLine.getOptionValue("indir");
        if (regulatoryRegionFilesDir != null) {
            DefaultJsonSerializer serializer = new DefaultJsonSerializer(outputPath);

            RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(Paths.get(regulatoryRegionFilesDir), serializer);
            regulatoryParser.parse();
            regulatoryParser.disconnect();
        }
    }

    private static void buildGene() throws IOException, NoSuchMethodException, FileFormatException, InterruptedException {
        logger.info("Processing gene...");
        String geneFilesDir = commandLine.getOptionValue("indir");
        String gtfFile = commandLine.getOptionValue("gtf-file");
        String genomeFastaFile = commandLine.getOptionValue("fasta-file", "");
        String xrefFile = commandLine.getOptionValue("xref-file", "");
        String uniprotIdMapping = commandLine.getOptionValue("uniprot-id-mapping-file", "");
        String geneDescriptionFile = commandLine.getOptionValue("description-file", "");
        String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
        String mirnaFile = commandLine.getOptionValue("mirna-file", "");

        GeneParser geneParser;
        if (geneFilesDir != null && !geneFilesDir.equals("")) {
            geneParser = new GeneParser(Paths.get(geneFilesDir), Paths.get(genomeFastaFile), serializer);
        } else {
            geneParser = new GeneParser(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(uniprotIdMapping), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(genomeFastaFile), serializer);
        }
        geneParser.parse();
        geneParser.disconnect();
    }

    private static void buildGenomeSequence(Path outputPath) throws Exception {
        logger.info("Processing genome-sequence...");
        String fastaFile = commandLine.getOptionValue("fasta-file");
        if (fastaFile != null && Files.exists(Paths.get(fastaFile))) {
            DefaultJsonSerializer gsfpSerializer = new DefaultJsonSerializer(outputPath, Paths.get("genome_sequence.json"));
            GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(Paths.get(fastaFile), gsfpSerializer);
            genomeSequenceFastaParser.parse();
            genomeSequenceFastaParser.disconnect();
        }
    }

    private static void buildGwas(Path outputPath) throws IOException {
        logger.info("Processing gwas...");
        String gwasFile = commandLine.getOptionValue("gwas-file");
        if (gwasFile != null) {
            String dbSnpFile = commandLine.getOptionValue("dbsnp-file");
            if (dbSnpFile != null) {
                DefaultJsonSerializer gwasJsonSerializer = new DefaultJsonSerializer(outputPath, Paths.get("gwas"), false);
                GwasParser gwasParser = new GwasParser(Paths.get(gwasFile), Paths.get(dbSnpFile), gwasJsonSerializer);
                gwasParser.parse();
            } else {
                logger.error("'dbsnp-file' option is mandatory for 'gwas' builder");
            }
        } else {
            logger.error("'gwas-file' option is mandatory for 'gwas' builder");
        }
    }

    private static void buildCosmic(Path outputPath) throws IOException {
        logger.info("Processing Cosmic ...");
        String cosmicFilePath = commandLine.getOptionValue("cosmic-file");
        if (cosmicFilePath != null) {
            DefaultJsonSerializer cosmicSerializer = new DefaultJsonSerializer(outputPath, Paths.get("cosmic"), false);
            //MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
            //vp.parse();
            // this parser works with cosmic file: CosmicCompleteExport_vXX.tsv (XX >= 70)
            CosmicParser cosmicParser = new CosmicParser(Paths.get(cosmicFilePath), cosmicSerializer);
            cosmicParser.parse();
            cosmicParser.disconnect();
        } else {
            logger.error("'cosmic-file' option is mandatory for 'cosmic' builder");
        }
    }

    private static void buildClinvar(Path outputPath) throws IOException {
        logger.info("Processing ClinVar...");
        String clinvarFile = commandLine.getOptionValue("clinvar-file");
        if (clinvarFile != null) {
            DefaultJsonSerializer clinvarJsonSerializer = new DefaultJsonSerializer(outputPath, Paths.get("clinvar"), false);
            ClinVarParser clinVarParser = new ClinVarParser(Paths.get(clinvarFile), clinvarJsonSerializer);
            clinVarParser.parse();
            clinVarParser.disconnect();
        } else {
            logger.error("'clinvar-file' option is mandatory for 'clinvar' builder");
        }
    }

    private static void parse(String[] args, boolean stopAtNoOption) throws ParseException, IOException {
        parser = new PosixParser();
        commandLine = parser.parse(options, args, stopAtNoOption);
    }

    private static void createCellBaseSerializer(Path outPath) throws IOException  {
        String serializerClass = commandLine.getOptionValue("serializer", "json");
        if (serializerClass != null) {
            // A default implementation for JSON is provided
            if (serializerClass.equalsIgnoreCase("json")) {
                logger.debug("JSON serializer chosen");
                serializer = new DefaultJsonSerializer(outPath);
            } else {
                logger.debug("MongoDB serializer chosen");
                serializer = new MongoDBSerializer(outPath);
            }
        }
    }
}
