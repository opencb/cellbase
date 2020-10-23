/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.app.cli.main;

import com.beust.jcommander.*;
import org.opencb.cellbase.app.cli.CliOptionsParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 03/02/15.
 */
public class CellBaseCliOptionsParser extends CliOptionsParser {

    private final CommonCommandOptions commonCommandOptions;

    private VariantAnnotationCommandOptions variantAnnotationCommandOptions;

    public CellBaseCliOptionsParser() {
        jCommander.setProgramName("cellbase.sh");
        commonCommandOptions = new CommonCommandOptions();

        //queryCommandOptions = new QueryCommandOptions();
        variantAnnotationCommandOptions = new VariantAnnotationCommandOptions();

        //jCommander.addCommand("query", queryCommandOptions);
        jCommander.addCommand("variant-annotation", variantAnnotationCommandOptions);
    }

    public void parse(String[] args) throws ParameterException {
        jCommander.parse(args);
    }

    @Parameters(commandNames = {"variant-annotation"}, commandDescription = "Annotate variants from VCF files using CellBase and other custom files")
    public class VariantAnnotationCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-i", "--input-file"}, description = "Input file with the data file to be annotated", required = false, arity = 1)
        public String input;

        @Parameter(names = {"--variant"}, description = "A comma separated variant list in the format chr:pos:ref:alt, ie. 1:451941:A:T,19:45411941:T:C", required = false, arity = 1)
        public String variant;

        @Parameter(names = {"-o", "--output"}, description = "Output file/directory where annotations will be saved. "
                + "Set here a directory if flag \"--input-variation-collection\" is activated (see below). Set a file "
                + "name otherwise.", required = true, arity = 1)
        public String output;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", required = true, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be read", required = false, arity = 1)
        public String assembly = null;

        @Parameter(names = {"-l", "--local"}, description = "Database credentials for local annotation are read from configuration.json file", required = false, arity = 0)
        public boolean local;

        @Parameter(names = {"--remote-url"}, description = "The URL of CellBase REST web services, this has no effect if --local is present", required = false, arity = 1)
        public String url = "https://bioinfo.hpc.cam.ac.uk:80/cellbase";

        @Parameter(names = {"--include"}, description = "Comma separated list of annotation types to be included. Available options are "
                + "{variation, populationFrequencies, conservation, functionalScore, traitAssociation, consequenceType, expression, "
                + "geneDisease, drugInteraction, cytoband, repeats, hgvs, geneConstraints, mirnaTargets}", required = false)
        public String include;

        @Parameter(names = {"--exclude"}, description = "Comma separated list of annotation types to be excluded. Available options are {variation, populationFrequencies, conservation, functionalScore, traitAssociation, consequenceType, expression, geneDisease, drugInteraction, cytoband, repeats, hgvs, geneConstraints, mirnaTargets}", required = false)
        public String exclude;

        @Parameter(names = {"-t", "--num-threads"}, description = "Number of threads to be used for loading", required = false, arity = 1)
        public int numThreads = 4;

        @Parameter(names = {"--batch-size"}, description = "Number of variants per batch", required = false, arity = 1)
        public int batchSize = 200;

        @Parameter(names = {"--resume"}, description = "Whether we resume annotation or overwrite the annotation in the output file", required = false, arity = 0)
        public boolean resume;

        @Parameter(names = {"--custom-file"}, description = "String with a comma separated list (no spaces in between) of files with custom annotation to be included during the annotation process. File format must be VCF. For example: file1.vcf,file2.vcf", required = false)
        public String customFiles;

        @Parameter(names = {"--custom-file-id"}, description = "String with a comma separated list (no spaces in between) of short identifiers for each custom file. For example: fileId1,fileId2", required = false)
        public String customFileIds;

        @Parameter(names = {"--custom-file-fields"}, description = "String containing a colon separated list (no spaces in between) of field lists which indicate the info fields to be taken from each VCF file. For example: field1File1,field2File1:field1File2,field3File2", required = false, arity = 1)
        public String customFileFields;

        @Parameter(names = {"--max-open-files"}, description = "Integer containing the maximum number of files that can remain open at a certain time point. This option is just used when providing custom annotation files. Custom annotation indexation may create and keep hundreds of files open at the same time for efficiency purposes. This parameter limits that number of open files: -1 indicates no limit and may be OK in most cases.", required = false, arity = 1)
        public int maxOpenFiles = -1;

        @Parameter(names = {"--output-format"}, description = "Variant annotation output format. Values: JSON, Avro, VEP", required = false, arity = 1)
        public String outputFormat = "JSON";

        @Parameter(names = {"--gzip"}, description = "Whether the output file is gzipped", required = false, arity = 0)
        public boolean gzip;

        @Parameter(names = {"--input-variation-collection"}, description = "Input will be a local installation of the"
                + "CellBase variation collection. Connection details must be properly specified at a configuration.json file",
                required = false, arity = 0)
        public boolean cellBaseAnnotation;

        @Parameter(names = {"--chromosomes"}, description = "Comma separated list (no empty spaces in between) of"
                + " chromosomes to annotate. One may use this parameter only when the --input-variation-collection"
                + " flag is activated. Variants from all chromosomes will be annotated by default. E.g.: 1,22,X,Y",
                required = false, arity = 1)
        public String chromosomeList;

        @Parameter(names = {"--benchmark"}, description = "Run variant annotation benchmark. If this flag is enabled,"
                + "a directory containing a list of Variant Effect Predictor (VEP) files is expected at the -i parameter."
                + " All .vep files within the directory will be processed - the directory must contain only .vep files that "
                + "should be tested",
                required = false, arity = 0)
        public boolean benchmark;

        @Parameter(names = {"--reference-fasta"}, description = "Required for left aligning when annotating in remote"
                + " mode, i.e. --local NOT present. It's strongly discouraged to use --reference-fasta together with "
                + " the --local flag. IF however --reference-fasta is set together with --local, then the genome sequence "
                + " in the fasta file will override CellBase database reference genome sequence. This --reference-fasta"
                + " parameter will be ignored if --skip-normalize is present. Finally, this parameter is required when"
                + " the --benchmark flag is enabled.",
                required = false, arity = 1)
        public String referenceFasta;

        @Parameter(names = {"--skip-normalize"}, description = "Skip normalization of input variants. Should not be used"
                + " when the input (-i, --input-file) is a VCF file. Normalization includes splitting multi-allele positions "
                + "read from a VCF, allele trimming and decomposing MNVs. Has"
                + " no effect if reading variants from a CellBase variation collection "
                + "(\"--input-variation-collection\") or running a variant annotation benchmark (\"--benchmark\"): in"
                + " these two cases variant normalization is never carried out.",
                required = false, arity = 0)
        public boolean skipNormalize = false;

        @Parameter(names = {"--skip-decompose"}, description = "Use this flag to avoid decomposition of "
                + "multi-nucleotide-variants (MNVs) / block substitutions as part of the normalization process. If this"
                + " flag is NOT activated, as a step during the normalization process reference and alternate alleles"
                + " from MNVs/Block substitutions will be aligned and decomposed into their forming simple variants. "
                + " This flag has no effect if --skip-normalize is present.",
                required = false, arity = 0)
        public boolean skipDecompose = false;

        @Parameter(names = {"--skip-left-align"}, description = "Use this flag to avoid left alignment as part of the"
                + " normalization process. If this"
                + " flag is NOT activated, as a step during the normalization process will left align the variant with"
                + " respect to the reference genome."
                + " This flag has no effect if --skip-normalize is present.",
                required = false, arity = 0)
        public boolean skipLeftAlign = false;

        // TODO: remove "phased" CLI parameter in next release. Default behavior from here onwards should be
        //  ignorePhase = false
        @Parameter(names = {"--phased"}, description = "This parameter is now deprecated and will be removed in next" +
                " release. Please, use --ignorePhase instead. Flag to indicate whether phased annotation shall be " +
                " activated.", required = false, arity = 0)
        @Deprecated
        public Boolean phased;

        @Parameter(names = {"--ignorePhase"}, description = "Flag to indicate whether phase should be ignored during" +
                " annotation. By default phased annotation is enabled, i.e. ignorePhase=false.", required = false,
                arity = 0)
        public Boolean ignorePhase;

        @Parameter(names = {"--no-imprecision"}, description = "Flag to indicate whether imprecision borders (CIPOS, CIEND)"
                + " should be taken into account when annotating structural variants or CNVs."
                + " By default imprecision annotation is enabled.", required = false, arity = 0)
        public boolean noImprecision;

        @Parameter(names = {"--check-aminoacid-change"}, description = "true/false to specify whether variant match in " +
                "the clinical variant collection should also be performed at the aminoacid change level",
                required = false,
                arity = 0)
        public boolean checkAminoAcidChange;

        @DynamicParameter(names = "-D", description = "Dynamic parameters. Available parameters: "
                + "{population-frequencies=for internal purposes mainly. Full path to a json file containing Variant "
                + "documents that include lists of population frequencies objects. Will allow annotating the input file "
                + "(-i) with the population frequencies present in this json file; complete-input-population=for internal "
                + "purposes only. To be used together with population-frequencies. Boolean to indicate whether variants "
                + "in the population-frequencies file but not in the input file (-i) should be included in the output. "
                + "sv-extra-padding=Integer to optionally "
                + "provide the size of the extra padding to be used when annotating noImprecision (or not) "
                + "structural variants}; cnv-extra-padding=Integer to optionally provide the size of the extra padding "
                + "to be used when annotating noImprecision (or not) structural variants}")
        public Map<String, String> buildParams;

        public VariantAnnotationCommandOptions() {
            buildParams = new HashMap<>();
            buildParams.put("population-frequencies", null);
            buildParams.put("complete-input-population", "true");
            buildParams.put("sv-extra-padding", "0");
            buildParams.put("cnv-extra-padding", "0");
        }

    }

    @Override
    public boolean isHelp() {
        String parsedCommand = jCommander.getParsedCommand();
        if (parsedCommand != null) {
            JCommander jCommander = this.jCommander.getCommands().get(parsedCommand);
            List<Object> objects = jCommander.getObjects();
            if (!objects.isEmpty() && objects.get(0) instanceof CommonCommandOptions) {
                return ((CommonCommandOptions) objects.get(0)).help;
            }
        }
        return getCommonCommandOptions().help;
    }

    public CommonCommandOptions getCommonCommandOptions() {
        return commonCommandOptions;
    }

    public VariantAnnotationCommandOptions getVariantAnnotationCommandOptions() { return variantAnnotationCommandOptions; }

}
