package org.opencb.cellbase.build;

import org.apache.commons.cli.*;
import org.opencb.cellbase.build.transform.*;
import org.bioinfo.formats.exception.FileFormatException;
import org.opencb.cellbase.build.transform.serializers.JsonSerializer;

import java.io.File;
import java.io.IOException;
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
		options.addOption(OptionFactory.createOption("build", "Build values: core, genome_sequence, variation, protein"));
		options.addOption(OptionFactory.createOption("indir", "i",  "Input directory with data files", false));
		options.addOption(OptionFactory.createOption("outdir", "o",  "Output directory to save the JSON result", false));
		options.addOption(OptionFactory.createOption("outfile", "Output directory to save the JSON result", false));

		// Core options
		options.addOption(OptionFactory.createOption("gtf-file", "Output directory to save the JSON result", false));
		options.addOption(OptionFactory.createOption("gene-description", "Output directory to save the JSON result", false));
		options.addOption(OptionFactory.createOption("xref-file", "Output directory to save the JSON result", false));
		options.addOption(OptionFactory.createOption("tfbs-file", "Output directory to save the JSON result", false));
		options.addOption(OptionFactory.createOption("mirna-file", "Output directory to save the JSON result", false));
		options.addOption(OptionFactory.createOption("genome-sequence-dir", "Output directory to save the JSON result", false));

		options.addOption(OptionFactory.createOption("chunksize", "Output directory to save the JSON result", false));

		options.addOption(OptionFactory.createOption("species", "s",  "Sapecies...", false, true));

		options.addOption(OptionFactory.createOption("log-level", "DEBUG -1, INFO -2, WARNING - 3, ERROR - 4, FATAL - 5", false));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initOptions();
		try {
			parse(args, false);

            String comm = args [1];

			String buildOption = null;

			// no needed to check as 'build' arg is required in Options
			if(!commandLine.hasOption("build") || commandLine.getOptionValue("build").equals("")) {

			}

			buildOption = commandLine.getOptionValue("build");

			if(buildOption.equals("genome-sequence")) {
				System.out.println("In genome-sequence");
				String indir = commandLine.getOptionValue("indir");
				String outfile = commandLine.getOptionValue("outfile", "/tmp/genome_sequence.json");
				if(indir != null) {
					GenomeSequenceFastaParser genomeSequenceFastaParser = new GenomeSequenceFastaParser(new JsonSerializer(new File(outfile)));
//					genomeSequenceFastaParser.parseFastaGzipFilesToJson(new File(indir), new File(outfile));
					genomeSequenceFastaParser.parse(new File(indir));
				}
			}

			if(buildOption.equals("core")) {
				System.out.println("In core");
				String gtfFile = commandLine.getOptionValue("gtf-file");
				String geneDescriptionFile = commandLine.getOptionValue("gene-description", "");
				String xrefFile = commandLine.getOptionValue("xref-file", "");
				String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
				String mirnaFile = commandLine.getOptionValue("mirna-file", "");
				String genomeSequenceDir = commandLine.getOptionValue("genome-sequence-dir", "");
				String outfile = commandLine.getOptionValue("outfile", "/tmp/gene.json");
				if(gtfFile != null) {
					try {
						GeneParser geneParser = new GeneParser(new JsonSerializer(new File(outfile)));
						geneParser.parse(new File(gtfFile), new File(geneDescriptionFile), new File(xrefFile), new File(tfbsFile), new File(mirnaFile), new File(genomeSequenceDir), new File(outfile));
					} catch (SecurityException | NoSuchMethodException | FileFormatException e) {
						e.printStackTrace();
					}
				}
			}

			if(buildOption.equals("variation")) {
				System.out.println("In variation");
				String indir = commandLine.getOptionValue("indir");
				int chunksize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
				System.out.println("chunksize: "+chunksize);
				String outfile = commandLine.getOptionValue("outfile", "/tmp/variation.json");
				if(indir != null) {
					VariationParser vp = new VariationParser();
					vp.createVariationDatabase(Paths.get(indir));
					
					vp.connect(Paths.get(indir));
//					List<String> res = vp.queryByVariationId(13, "variation_synonym", Paths.get(indir));
//					System.out.println("a");
//					 res = vp.queryByVariationId(4, "variation_synonym", Paths.get(indir));
//					System.out.println("b");
//					res = vp.queryByVariationId(8, "variation_synonym", Paths.get(indir));
//					System.out.println("c");
					vp.parseVariationToJson("", "", "", "", Paths.get(indir), Paths.get(outfile));
					vp.disconnect();
				}
			}
			
			if(buildOption.equals("regulation")) {
				System.out.println("In regulation");
				String indir = commandLine.getOptionValue("indir");
				int chunksize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
				System.out.println("chunksize: "+chunksize);
				String outfile = commandLine.getOptionValue("outfile", "/tmp/regulations.json");
				if(indir != null) {
					try {
						RegulatoryParser.parseRegulatoryGzipFilesToJson(Paths.get(indir), chunksize, Paths.get(outfile));
					} catch (ClassNotFoundException | NoSuchMethodException	| SQLException e) {
						e.printStackTrace();
					}
				}
			}

			if(buildOption.equals("conservation")) {
				System.out.println("In conservation");
				String indir = commandLine.getOptionValue("indir");
				int chunksize = Integer.parseInt(commandLine.getOptionValue("chunksize", "0"));
				String outfile = commandLine.getOptionValue("outfile", "/tmp/conservation.json");
				if(indir != null) {
					ConservedRegionParser.parseConservedRegionFilesToJson(Paths.get(indir), chunksize,  Paths.get(outfile));
				}
			}

		} catch (ParseException | IOException | SQLException | ClassNotFoundException e) {
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

}
