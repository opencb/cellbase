package org.opencb.cellbase.build;

import org.apache.commons.cli.*;
import org.opencb.cellbase.build.transform.*;
import org.opencb.cellbase.build.transform.serializers.CellbaseSerializer;
import org.opencb.cellbase.build.transform.serializers.mongodb.MongoDBSerializer;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.Logger;

public class CellBaseMain {

    private static Options options;
    private static CommandLine commandLine;
    private static CommandLineParser parser;

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
            CellbaseSerializer serializer = null;


            if(commandLine.hasOption("serializer") && !commandLine.getOptionValue("serializer").equals("")) {
                serializationOutput = commandLine.getOptionValue("serializer");
            }else {
                serializationOutput = "json";
            }

            buildOption = commandLine.getOptionValue("build");

            if(buildOption.equals("genome-sequence")) {
                System.out.println("In genome-sequence...");

                String fastaFile = commandLine.getOptionValue("fasta-file");
                if(fastaFile != null && Files.exists(Paths.get(fastaFile))) {
                    serializer = getSerializer(serializationOutput, commandLine);
                    GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(serializer);
                    genomeSequenceFastaParser.parse(new File(fastaFile));
                    serializer.close();
                }
            }

            if(buildOption.equals("gene")) {
                System.out.println("In gene...");

                String gtfFile = commandLine.getOptionValue("gtf-file");
                String fastaFile = commandLine.getOptionValue("fasta-file", "");
                String xrefFile = commandLine.getOptionValue("xref-file", "");
                String geneDescriptionFile = commandLine.getOptionValue("description-file", "");
                String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
                String mirnaFile = commandLine.getOptionValue("mirna-file", "");

                if(gtfFile != null && Files.exists(Paths.get(gtfFile))) {
                    serializer = getSerializer(serializationOutput, commandLine);
                    GeneParser geneParser = new GeneParser(serializer);
                    geneParser.parse(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(fastaFile));
                    serializer.close();
                }
            }

            if(buildOption.equals("protein")) {
                System.out.println("In protein...");

                String indir = commandLine.getOptionValue("indir");
                String species = commandLine.getOptionValue("species");
                if(indir != null && Files.exists(Paths.get(indir))) {
                    serializer = getSerializer(serializationOutput, commandLine);
                    ProteinParser proteinParser = new ProteinParser(serializer);
                    proteinParser.parse(Paths.get(indir), species);
                    serializer.close();
                }
            }

            if(buildOption.equals("variation")) {
                System.out.println("In variation...");

                String indir = commandLine.getOptionValue("indir");
                int chunksize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
                if(indir != null) {
                    serializer = getSerializer(serializationOutput, commandLine);
                    VariationParser vp = new VariationParser(serializer);
//                    vp.createVariationDatabase(Paths.get(indir));
//                    vp.connect(Paths.get(indir));

//					List<String> res = vp.queryByVariationId(13, "variation_synonym", Paths.get(indir));
//					System.out.println("a");
//					 res = vp.queryByVariationId(4, "variation_synonym", Paths.get(indir));
//					System.out.println("b");
//					res = vp.queryByVariationId(8, "variation_synonym", Paths.get(indir));
//					System.out.println("c");
//                    vp.parse("", "", "", "", Paths.get(indir)); //, Paths.get(outfile)
                    vp.parse(Paths.get(indir)); //, Paths.get(outfile)
                    vp.disconnect();
                }
            }

            if(buildOption.equals("mutation")) {
                System.out.println("In mutation");

                /**
                 * File from Cosmic: CosmicCompleteExport_XXX.tsv
                 */
                String cosmicFilePath = commandLine.getOptionValue("cosmic-file");
                if(cosmicFilePath != null) {
                    serializer = getSerializer(serializationOutput, commandLine);
                    MutationParser vp = new MutationParser(serializer);
//                    vp.parse(Paths.get(cosmicFilePath));
                    vp.parseEnsembl(Paths.get(cosmicFilePath));
                    serializer.close();
                }
            }

            if(buildOption.equals("regulation")) {
                System.out.println("In regulation");
                String indir = commandLine.getOptionValue("indir");
                int chunksize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
                String outfile = commandLine.getOptionValue("output", "/tmp/regulations.json");
                if(indir != null) {
                    try {
                        serializer = getSerializer(serializationOutput, commandLine);
                        RegulatoryRegionParser regulatoryParser = new RegulatoryRegionParser(serializer);
                        regulatoryParser.parse(Paths.get(indir));
                    } catch (ClassNotFoundException | NoSuchMethodException	| SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(buildOption.equals("conservation")) {
                System.out.println("In conservation");
                String indir = commandLine.getOptionValue("indir");
                int chunksize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
                String outfile = commandLine.getOptionValue("output", "/tmp/conservation.json");
                if(indir != null) {
                    ConservedRegionParser.parseConservedRegionFilesToJson(Paths.get(indir), chunksize,  Paths.get(outfile));
                }
            }

            if(buildOption.equals("protein")) {
                System.out.println("In PPI");
                String psimiTabFile = commandLine.getOptionValue("psimi-tab-file");
                String outfile = commandLine.getOptionValue("output", "/tmp/protein_protein_interaction.json");
                if(psimiTabFile != null) {
                    serializer = getSerializer(serializationOutput, commandLine);
                    InteractionParser interactionParser = new InteractionParser(serializer);
                    interactionParser.parse(Paths.get(psimiTabFile), commandLine.getOptionValue("species").toString());
                }
            }

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

    private static CellbaseSerializer getSerializer(String serializationOutput, CommandLine commandLine) throws IOException {
        CellbaseSerializer serializer = null;
        switch(serializationOutput) {
            case "json":
                serializer = new MongoDBSerializer(new File(commandLine.getOptionValue("output")));
                break;
        }

        return serializer;
    }

}
