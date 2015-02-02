package org.opencb.cellbase.build.transform;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.opencb.cellbase.build.CellBaseMain;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Created by parce on 26/11/14.
 */
public class CellBaseParserFactory {

    private static CommandLine commandLine;
    private static String buildOption;
    private static CellBaseSerializer serializer;

    public static CellBaseParser createParser(String buildOption, Path outputPath, CellBaseSerializer serializer, CommandLine commandLine) throws ParseException {
        CellBaseParserFactory.commandLine = commandLine;
        CellBaseParserFactory.buildOption = buildOption;
        CellBaseParserFactory.serializer = serializer;
        switch (buildOption) {
            case "genome-sequence":
                return buildGenomeSequence();
            case "gene":
                return buildGene();
            case "regulation":
                return buildRegulation();
            case "variation":
                return buildVariation();
            case "variation-phen-annot":
                return buildVariationPhenotypeAnnotation();
            case "vep":
                return buildVep();
            case "protein":
                return buildProtein();
            case "conservation":
                return buildConservation();
            case "ppi":
                return buildPpi();
            case "drug":
                return buildDrugParser();
            case "clinvar":
                return buildClinvar();
            case "cosmic":
                return buildCosmic();
            case "gwas":
                return buildGwas();
            default:
                throw new ParseException("Build option '" + buildOption + "' is not valid");
        }
    }

    private static CellBaseParser buildPpi() throws ParseException {
        Path psimiTabFile = getInputFileFromCommandLine(CellBaseMain.PSIMI_TAB_FILE);
        return new InteractionParser(psimiTabFile, getMandatoryOptionValue(CellBaseMain.SPECIES_OPTION), serializer);
    }

    private static CellBaseParser buildConservation() throws ParseException {
        String conservationFilesDir = getInputDirFromCommandLine();
        int conservationChunkSize = Integer.parseInt(commandLine.getOptionValue(CellBaseMain.CHUNK_SIZE_OPTION, "0"));
        return new ConservedRegionParser(Paths.get(conservationFilesDir), conservationChunkSize, serializer);
    }

    private static CellBaseParser buildProtein() throws  ParseException {
        String uniprotSplitFilesDir = getInputDirFromCommandLine();
        String species = getMandatoryOptionValue(CellBaseMain.SPECIES_OPTION);
        return new ProteinParser(Paths.get(uniprotSplitFilesDir), species, serializer);

    }

    private static CellBaseParser buildVep() throws ParseException {
        Path effectFile = getInputFileFromCommandLine(CellBaseMain.VEP_FILE_OPTION);
        return new VariantEffectParser(effectFile, serializer);
    }

    private static CellBaseParser buildVariationPhenotypeAnnotation() throws ParseException {
        String variationFilesDir = getInputDirFromCommandLine();
        return new VariationPhenotypeAnnotationParser(Paths.get(variationFilesDir), serializer);
    }

    private static CellBaseParser buildVariation() throws ParseException {
        String variationFilesDir = getInputDirFromCommandLine();
        return new VariationParser(Paths.get(variationFilesDir), serializer);

    }

    private static CellBaseParser buildRegulation() throws ParseException {
        Path regulatoryRegionFilesDir = getInputFileFromCommandLine(CellBaseMain.INDIR_OPTION);
        return new RegulatoryRegionParser(regulatoryRegionFilesDir, serializer);

    }

    private static CellBaseParser buildGene() {
        String geneFilesDir = commandLine.getOptionValue(CellBaseMain.INDIR_OPTION);
        String genomeFastaFile = commandLine.getOptionValue(CellBaseMain.FASTA_FILE_OPTION, "");

        GeneParser geneParser;
        if (geneFilesDir != null && !geneFilesDir.equals("")) {
            geneParser = new GeneParser(Paths.get(geneFilesDir), Paths.get(genomeFastaFile), serializer);
        } else {
            String gtfFile = commandLine.getOptionValue("gtf-file");
            String xrefFile = commandLine.getOptionValue("xref-file", "");
            String uniprotIdMapping = commandLine.getOptionValue("uniprot-id-mapping-file", "");
            String geneDescriptionFile = commandLine.getOptionValue("description-file", "");
            String tfbsFile = commandLine.getOptionValue("tfbs-file", "");
            String mirnaFile = commandLine.getOptionValue("mirna-file", "");
            geneParser = new GeneParser(Paths.get(gtfFile), Paths.get(geneDescriptionFile), Paths.get(xrefFile), Paths.get(uniprotIdMapping), Paths.get(tfbsFile), Paths.get(mirnaFile), Paths.get(genomeFastaFile), serializer);
        }
        return geneParser;
    }

    private static CellBaseParser buildGenomeSequence() throws ParseException {
        Path fastaFile = getInputFileFromCommandLine(CellBaseMain.FASTA_FILE_OPTION);
        return new GenomeSequenceFastaParser(fastaFile, serializer);

    }

    private static CellBaseParser buildDrugParser() throws ParseException {
        Path drugFile = getInputFileFromCommandLine(CellBaseMain.DRUG_FILE_OPTION);
        return new DrugParser(drugFile, serializer);
    }


    private static CellBaseParser buildGwas() throws ParseException {
        Path gwasFile = getInputFileFromCommandLine(CellBaseMain.GWAS_FILE_OPTION);
        Path dbsnpFile = getInputFileFromCommandLine(CellBaseMain.DBSNP_FILE_OPTION);
        serializer.setOutputFileName(Paths.get("gwas.json"));
        serializer.setSerializeEmptyValues(false);
        return new GwasParser(gwasFile, dbsnpFile, serializer);

    }

    private static CellBaseParser buildCosmic() throws ParseException {
        Path cosmicFilePath = getInputFileFromCommandLine(CellBaseMain.COSMIC_FILE_OPTION);
        serializer.setOutputFileName(Paths.get("cosmic.json"));
        serializer.setSerializeEmptyValues(false);
        //MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
        // this parser works with cosmic file: CosmicCompleteExport_vXX.tsv (XX >= 70)
        return new CosmicParser(cosmicFilePath, serializer);
    }

    private static CellBaseParser buildClinvar() throws ParseException {
        Path clinvarFile = getInputFileFromCommandLine(CellBaseMain.CLINVAR_FILE_OPTION);
        serializer.setOutputFileName(Paths.get("clinvar.json"));
        serializer.setSerializeEmptyValues(false);
        String assembly = getMandatoryOptionValue(CellBaseMain.ASSEMBLY_OPTION);
        if (!assembly.equals(ClinVarParser.GRCH37_ASSEMBLY) && !assembly.equals(ClinVarParser.GRCH38_ASSEMBLY)) {
            throw new ParseException("Assembly '" + assembly + "' is not valid. Possible values: " + ClinVarParser.GRCH37_ASSEMBLY + ", " + ClinVarParser.GRCH38_ASSEMBLY);
        }
        return new ClinVarParser(clinvarFile, assembly, serializer);
    }

    private static Path getInputFileFromCommandLine(String option) throws ParseException {
        String fileName = getMandatoryOptionValue(option);
        if (new File(fileName).exists()) {
            return Paths.get(fileName);
        } else {
            throw new ParseException("File '" + fileName + "' doesn't exist");
        }
    }

    private static String getInputDirFromCommandLine() throws ParseException {
        String inputDirName = getMandatoryOptionValue(CellBaseMain.INDIR_OPTION);
        File inputDirectory = new File(inputDirName);
        if (inputDirectory.exists()) {
            if (inputDirectory.isDirectory()) {
                return inputDirName;
            } else {
                throw new ParseException("'" + inputDirName + "' is not a directory");
            }
        } else {
            throw new ParseException("Folder '" + inputDirName + "' doesn't exist");
        }
    }

    private static String getMandatoryOptionValue(String option) throws ParseException {
        if (commandLine.hasOption(option)) {
            return commandLine.getOptionValue(option);
        } else {
            throw new ParseException("'" + option + "' option is mandatory for '" + buildOption + "' builder");
        }
    }

}
