package org.opencb.cellbase.build;

import org.apache.commons.cli.*;
import org.opencb.cellbase.build.transform.*;
import org.opencb.cellbase.build.transform.serializers.CellBaseSerializer;
import org.opencb.cellbase.build.transform.serializers.mongodb.MongoDBSerializer;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.Logger;

public class CellBaseMain {

    private static Options options;
    private static CommandLine commandLine;
    private static CommandLineParser parser;

    private static CellBaseSerializer serializer = null;

    private Logger logger;

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
        options.addOption(OptionFactory.createOption("output", "o",  "Output file or directory (depending on the 'build') to save the result"));

        // Optional parameter for some builds that accept a folder and not only one or few files
        options.addOption(OptionFactory.createOption("indir", "i",  "Input directory with data files", false));

        // Sequence and gene options
        options.addOption(OptionFactory.createOption("fasta-file", "Output directory to save the JSON result", false));

        // Gene options
        options.addOption(OptionFactory.createOption("gtf-file", "Output directory to save the JSON result", false));
        options.addOption(OptionFactory.createOption("xref-file", "Output directory to save the JSON result", false));
        options.addOption(OptionFactory.createOption("description-file", "Output directory to save the JSON result", false));
        options.addOption(OptionFactory.createOption("tfbs-file", "Output directory to save the JSON result", false));
        options.addOption(OptionFactory.createOption("mirna-file", "Output directory to save the JSON result", false));

        // Mutation options
        options.addOption(OptionFactory.createOption("cosmic-file", "Output directory to save the JSON result", false));

        // Protein options
        options.addOption(OptionFactory.createOption("species", "s",  "Sapecies...", false, true));
        options.addOption(OptionFactory.createOption("psimi-tab-file", "Output directory to save the JSON result", false));

//        options.addOption(OptionFactory.createOption("genome-sequence-dir", "Output directory to save the JSON result", false));
//        options.addOption(OptionFactory.createOption("chunksize", "Output directory to save the JSON result", false));


        options.addOption(OptionFactory.createOption("log-level", "DEBUG -1, INFO -2, WARNING - 3, ERROR - 4, FATAL - 5", false));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        initOptions();
        try {

            if(args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("cellbase-build.jar", "Some options are mandatory for all possible 'builds', while others are only mandatory for some specific 'builds':", options, "\nFor more information or reporting bugs contact me: imedina@cipf.es", true);
                return;
            }


            parse(args, false);

            String buildOption = null;
            String serializationOutput = null;

            /**
             * This code create a serializer for a specific database, only MongoDB has been implemented so far,
             * DI pattern could be applied to get other database outputs.
             */
            if(commandLine.hasOption("serializer") && !commandLine.getOptionValue("serializer").equals("")) {
                serializationOutput = commandLine.getOptionValue("serializer");
            }else {
                serializationOutput = "mongodb";
            }
            serializer = getSerializer(serializationOutput, Paths.get(commandLine.getOptionValue("output")));


            buildOption = commandLine.getOptionValue("build");
            switch(buildOption) {
                case "genome-sequence":
                    System.out.println("In genome-sequence...");
                    String fastaFile = commandLine.getOptionValue("fasta-file");
                    if(fastaFile != null && Files.exists(Paths.get(fastaFile))) {
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
                        if(geneFilesDir != null && !geneFilesDir.equals("")) {
                            geneParser.parse(Paths.get(geneFilesDir), Paths.get(genomeFastaFile));
                        }else {
                            geneParser.parse(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(genomeFastaFile));
                        }
//                    }
                    break;
                case "regulation":
                    System.out.println("In regulation");
                    String regulatoryRegionFilesDir = commandLine.getOptionValue("indir");
                    if(regulatoryRegionFilesDir != null) {
                        RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(serializer);
                        regulatoryParser.parse(Paths.get(regulatoryRegionFilesDir));
                    }
                    break;
                case "variation":
                    System.out.println("In variation...");
                    String variationFilesDir = commandLine.getOptionValue("indir");
                    if(variationFilesDir != null) {
                        VariationParser vp = new VariationParser(serializer);
                        vp.parse(Paths.get(variationFilesDir)); //, Paths.get(outfile)
                    }
                    break;
                case "variation-phen-annot":
                    System.out.println("In variation phenotype annotation...");
                    variationFilesDir = commandLine.getOptionValue("indir");
                    if(variationFilesDir != null) {
                        VariationPhenotypeAnnotationParser variationPhenotypeAnnotationParser = new VariationPhenotypeAnnotationParser(serializer);
//                    vp.parseCosmic(Paths.get(cosmicFilePath));
                        variationPhenotypeAnnotationParser.parseEnsembl(Paths.get(variationFilesDir));
                    }
                    break;
                case "protein":
                    System.out.println("In protein...");
                    String uniprotSplitFilesDir = commandLine.getOptionValue("indir");
                    String species = commandLine.getOptionValue("species");
                    if(uniprotSplitFilesDir != null && Files.exists(Paths.get(uniprotSplitFilesDir))) {
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
                    if(cosmicFilePath != null) {
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
                    if(conservationFilesDir != null) {
                        ConservedRegionParser.parseConservedRegionFilesToJson(Paths.get(conservationFilesDir), conservationChunkSize,  Paths.get(conservationOutputFile));
                    }
                    break;
                case "ppi":
                    System.out.println("In PPI...");
                    String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
                    if(psimiTabFile != null) {
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
        } catch (ParseException | IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
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

        if(args.length > 0 && "variation".equals(args[1])) {
            System.out.println("variation SQL test");
        }
    }

    private static CellBaseSerializer getSerializer(String serializationOutput, Path outPath) throws IOException {
        CellBaseSerializer serializer = null;
        switch(serializationOutput) {
            case "mongodb":
                serializer = new MongoDBSerializer(outPath);
                break;
        }

        return serializer;
    }

    private static void parseAll(Path speciesInDir) throws NoSuchMethodException, FileFormatException, IOException, InterruptedException, SQLException, ClassNotFoundException {
        Path genomeFastaPath = null;
        for(String fileName: speciesInDir.resolve("sequence").toFile().list()) {
            if(fileName.endsWith(".fa") || fileName.endsWith(".fa.gz")) {
                genomeFastaPath = speciesInDir.resolve("sequence").resolve(fileName);
                break;
            }
        }
        GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(serializer);
        genomeSequenceFastaParser.parse(genomeFastaPath);

        GeneParser geneParser = new GeneParser(serializer);
        geneParser.parse(speciesInDir.resolve("gene"), genomeFastaPath);

        Path variationPath = speciesInDir.resolve("variation");
        if(variationPath.toFile().list().length > 2) {
            VariationParser vp = new VariationParser(serializer);
            vp.parse(variationPath);
        }

        Path regulationPath = speciesInDir.resolve("regulation");
        if(variationPath.toFile().list().length > 2) {
        RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(serializer);
        regulatoryParser.parse(regulationPath);
        }
    }

}
