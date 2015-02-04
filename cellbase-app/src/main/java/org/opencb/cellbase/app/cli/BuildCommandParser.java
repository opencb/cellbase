package org.opencb.cellbase.app.cli;

import org.apache.commons.cli.ParseException;
import org.opencb.cellbase.app.transform.*;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.serializer.DefaultJsonSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by imedina on 03/02/15.
 */
public class BuildCommandParser extends CommandParser {

    // TODO: these two constants should be defined in the 'download' module
    public static final String GWAS_INPUT_FILE_NAME = "gwasCatalog.txt";
    public static final String DBSNP_INPUT_FILE_NAME = "dbSnp142-00-All.vcf.gz";

    private String input = null;
    private String output = null;
    private CellBaseSerializer serializer;

    private CliOptionsParser.BuildCommandOptions buildCommandOptions;

    public BuildCommandParser(CliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.verbose,
                buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
        if(buildCommandOptions.input != null) {
            input = buildCommandOptions.input;
        }
        if(buildCommandOptions.output != null) {
            output = buildCommandOptions.output;
        }
    }


    /**
     * Parse specific 'build' command options
     */
    public void parse() {
        createSerializer();

        if(buildCommandOptions.build != null && serializer != null) {
            CellBaseParser parser = null;
            try {
                switch (buildCommandOptions.build) {
                    case "genome-sequence":
                        buildGenomeSequence();
                    case "gene":
                        buildGene();
                    case "regulation":
                        buildRegulation();
                    case "variation":
                        buildVariation();
                    case "variation-phen-annot":
                        buildVariationPhenotypeAnnotation();
                    case "vep":
                        buildVep();
                    case "protein":
                        buildProtein();
                    case "conservation":
                        buildConservation();
                    case "ppi":
                        parser = getInteractionParser();
                    case "drug":
                        buildDrugParser();
                    case "clinvar":
                        buildClinvar();
                    case "cosmic":
                        buildCosmic();
                    case "gwas":
                        buildGwas();
                    default:
                        logger.error("Build option '" + buildCommandOptions.build + "' is not valid");
                }
            } catch (ParseException e) {
                logger.error("Error executing build command: " + e.getMessage());
            }
            if (parser != null) {
                try {
                    parser.parse();
                } catch (Exception e) {
                    logger.error("Error executing 'build' command " + buildCommandOptions.build + ": " + e.getMessage());
                }
                parser.disconnect();
            }
        }

    }

    private void createSerializer() {
        // check output parameter
        try {
            serializer = new DefaultJsonSerializer(Paths.get(output));
        } catch (IOException e) {
            logger.error("Error creating output serializer: " + e.getMessage());
        }
    }

    private CellBaseParser getInteractionParser() throws ParseException {
        Path psimiTabFile = getInputFileFromCommandLine();
        String species = buildCommandOptions.species;
        checkMandatoryOption(species);
        return new InteractionParser(psimiTabFile, species, serializer);
    }

    private CellBaseParser buildConservation() throws ParseException {
        Path conservationFilesDir = getInputDirFromCommandLine();
        // TODO: chunk size is not really used in ConvervedRegionParser, remove?
        //int conservationChunkSize = Integer.parseInt(commandLine.getOptionValue(CellBaseMain.CHUNK_SIZE_OPTION, "0"));
        int conservationChunkSize = 0;
        return new ConservedRegionParser(conservationFilesDir, conservationChunkSize, serializer);
    }

    private CellBaseParser buildProtein() throws  ParseException {
        Path uniprotSplitFilesDir = getInputDirFromCommandLine();
        String species = buildCommandOptions.species;
        checkMandatoryOption(species);
        return new ProteinParser(uniprotSplitFilesDir, species, serializer);

    }

    private CellBaseParser buildVep() throws ParseException {
        Path vepFile = getInputFileFromCommandLine();
        return new VariantEffectParser(vepFile, serializer);
    }

    private CellBaseParser buildVariationPhenotypeAnnotation() throws ParseException {
        Path variationFilesDir = getInputDirFromCommandLine();
        return new VariationPhenotypeAnnotationParser(variationFilesDir, serializer);
    }

    private CellBaseParser buildVariation() throws ParseException {
        Path variationFilesDir = getInputDirFromCommandLine();
        return new VariationParser(variationFilesDir, serializer);

    }

    private CellBaseParser buildRegulation() throws ParseException {
        Path regulatoryRegionFilesDir = getInputDirFromCommandLine();
        return new RegulatoryRegionParser(regulatoryRegionFilesDir, serializer);

    }

    private CellBaseParser buildGene() throws ParseException {
        Path inputDir = getInputDirFromCommandLine();

        String genomeFastaFile = buildCommandOptions.referenceGenomeFile;
        checkMandatoryOption(genomeFastaFile);
        GeneParser geneParser = new GeneParser(inputDir, Paths.get(genomeFastaFile), serializer);

        // TODO: gtf-file?
//        String gtfFile = commandLine.getOptionValue("gtf-file");
//        String xrefFile = commandLine.getOptionValue("xref-file", "");
//        String uniprotIdMapping = commandLine.getOptionValue("uniprot-id-mapping-file", "");
//        String geneDescriptionFile = commandLine.getOptionValue("description-file", "");
//        String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
//        String mirnaFile = commandLine.getOptionValue("mirna-file", "");
//        geneParser = new GeneParser(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(uniprotIdMapping), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(genomeFastaFile), serializer);

        return geneParser;
    }

    private CellBaseParser buildGenomeSequence() throws ParseException {
        Path fastaFile = getInputFileFromCommandLine();
        return new GenomeSequenceFastaParser(fastaFile, serializer);

    }

    private CellBaseParser buildDrugParser() throws ParseException {
        Path drugFile = getInputFileFromCommandLine();
        return new DrugParser(drugFile, serializer);
    }


    private CellBaseParser buildGwas() throws ParseException {
        Path inputDir = getInputDirFromCommandLine();
        Path gwasFile = inputDir.resolve(GWAS_INPUT_FILE_NAME);
        Path dbsnpFile = inputDir.resolve(DBSNP_INPUT_FILE_NAME);
        // TODO: serializer will only receive a directory and not a file
        serializer.setOutputFileName(Paths.get("gwas.json"));
        serializer.setSerializeEmptyValues(false);
        return new GwasParser(gwasFile, dbsnpFile, serializer);
    }

    private CellBaseParser buildCosmic() throws ParseException {
        Path cosmicFilePath = getInputFileFromCommandLine();
        // TODO: serializer will only receive a directory and not a file
        serializer.setOutputFileName(Paths.get("cosmic.json"));
        serializer.setSerializeEmptyValues(false);
        //MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
        // this parser works with cosmic file: CosmicCompleteExport_vXX.tsv (XX >= 70)
        return new CosmicParser(cosmicFilePath, serializer);
    }

    private CellBaseParser buildClinvar() throws ParseException {
        Path clinvarFile = getInputFileFromCommandLine();
        // TODO: serializer will only receive a directory and not a file
        serializer.setOutputFileName(Paths.get("clinvar.json"));
        serializer.setSerializeEmptyValues(false);

        // assembly
        String assembly = buildCommandOptions.assembly;
        checkMandatoryOption(assembly);
        if (!assembly.equals(ClinVarParser.GRCH37_ASSEMBLY) && !assembly.equals(ClinVarParser.GRCH38_ASSEMBLY)) {
            throw new ParseException("Assembly '" + assembly + "' is not valid. Possible values: " + ClinVarParser.GRCH37_ASSEMBLY + ", " + ClinVarParser.GRCH38_ASSEMBLY);
        }

        return new ClinVarParser(clinvarFile, assembly, serializer);
    }

    private Path getInputFileFromCommandLine() throws ParseException {
        File inputFile = new File(input);
        if (inputFile.exists()) {
            if (inputFile.isDirectory()) {
                throw new ParseException(input + " is a directory: it must be a file for " + buildCommandOptions.build + " builder");
            } else {
                return Paths.get(input);
            }
        } else {
            throw new ParseException("File '" + input + "' doesn't exist");
        }
    }

    private Path getInputDirFromCommandLine() throws ParseException {
        File inputDirectory = new File(input);
        if (inputDirectory.exists()) {
            if (inputDirectory.isDirectory()) {
                return Paths.get(input);
            } else {
                throw new ParseException("'" + input + "' is not a directory");
            }
        } else {
            throw new ParseException("Folder '" + input + "' doesn't exist");
        }
    }

    private void checkMandatoryOption(String option) throws ParseException {
        if (option == null) {
            throw new ParseException("'" + option + "' option is mandatory for '" + buildCommandOptions.build + "' builder");
        }
    }

}
