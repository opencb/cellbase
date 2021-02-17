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

package org.opencb.cellbase.app.cli.admin;

import com.beust.jcommander.*;
import org.opencb.cellbase.app.cli.CliOptionsParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 03/02/15.
 */
public class AdminCliOptionsParser extends CliOptionsParser {

    private final CommonCommandOptions commonCommandOptions;
    private final SpeciesAndAssemblyCommandOptions speciesAndAssemblyCommandOptions;

    private DownloadCommandOptions downloadCommandOptions;
    private BuildCommandOptions buildCommandOptions;
    private LoadCommandOptions loadCommandOptions;
    private IndexCommandOptions indexCommandOptions;
    private InstallCommandOptions installCommandOptions;
    private ServerCommandOptions serverCommandOptions;
    private ValidationCommandOptions validationCommandOptions;

    public AdminCliOptionsParser() {
        jCommander.setProgramName("cellbase-admin.sh");
        commonCommandOptions = new CommonCommandOptions();
        speciesAndAssemblyCommandOptions = new SpeciesAndAssemblyCommandOptions();

        downloadCommandOptions = new DownloadCommandOptions();
        buildCommandOptions = new BuildCommandOptions();
        loadCommandOptions = new LoadCommandOptions();
        indexCommandOptions = new IndexCommandOptions();
        installCommandOptions = new InstallCommandOptions();
        serverCommandOptions = new ServerCommandOptions();
        validationCommandOptions = new ValidationCommandOptions();

        jCommander.addCommand("download", downloadCommandOptions);
        jCommander.addCommand("build", buildCommandOptions);
        jCommander.addCommand("load", loadCommandOptions);
        jCommander.addCommand("index", indexCommandOptions);
        jCommander.addCommand("install", installCommandOptions);
        jCommander.addCommand("server", serverCommandOptions);
        jCommander.addCommand("validate", validationCommandOptions);
    }

    public void parse(String[] args) throws ParameterException {
        jCommander.parse(args);
    }

    @Parameters(commandNames = {"download"}, commandDescription = "Download all different data sources provided in the configuration.yml file")
    public class DownloadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @ParametersDelegate
        public SpeciesAndAssemblyCommandOptions speciesAndAssemblyOptions = speciesAndAssemblyCommandOptions;

        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to download: genome, gene, "
                + "variation, variation_functional_score, regulation, protein, conservation, "
                + "clinical_variants, repeats, svs and 'all' to download everything", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-o", "--outdir"}, description = "Downloaded files will be saved in this directory.", required = true, arity = 1)
        public String outputDirectory;
    }

    @Parameters(commandNames = {"build"}, commandDescription = "Build CellBase data models from all data sources downloaded")
    public class BuildCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to build: genome, genome_info, "
                + "gene, variation, variation_functional_score, regulation, protein, ppi, conservation, drug, "
                + "clinical_variants, repeats, svs. 'all' builds everything.", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be built, valid formats include 'Homo sapiens' or 'hsapiens'", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.yml will be used", required = false, arity = 1)
        public String assembly;

        @Parameter(names = {"-o", "--outdir"}, description = "Downloaded files will be saved in this directory.", required = true, arity = 1)
        public String outputDirectory;

        @Parameter(names = {"--skip-normalize"}, description = "Skip normalization of clinical variants. Normalization"
                + " includes allele trimming and left alignment. **NOTE** this parameter will only be used when building"
                + " the clinical_variants dataset.",
                required = false, arity = 0)
        public boolean skipNormalize = false;

        @Parameter(names = {"--flexible-gtf-parsing"}, description = "By default, ENSEMBL GTF format is expected. "
                + " Nevertheless, GTF specification is quite loose and other GTFs may be provided in which the order "
                + "of the features is not as systematic as within the ENSEMBL's GTFs. Use this option to enable a more "
                + "flexible parsing of the GTF if it does not strictly follow ENSEMBL's GTFs format. Flexible GTF "
                + "requires more memory and is less efficient.", required = false, arity = 0)
        public boolean flexibleGTFParsing = false;

    }

    @Parameters(commandNames = {"load"}, commandDescription = "Load the built data models into the database")
    public class LoadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-d", "--data"}, description = "Data model type to be loaded: genome, gene, variation, "
                + "conservation, regulation, protein, clinical_variants, repeats, regulatory_pfm. 'all' loads everything",
                required = true, arity = 1)
        public String data;

        @Parameter(names = {"-i", "--input"}, description = "Input directory with the JSON data models to be loaded, e.g. "
                + "'/data/hsapiens_grch38/generated-json'. Can also be used to specify a custom json file to be loaded (look at the "
                + "--fields parameter).", required = true, arity = 1)
        public String input;

        @Parameter(names = {"--database"}, description = "Database name", required = true, arity = 1)
        public String database;

        @Parameter(names = {"--fields"}, description = "Use this parameter when an custom update of the database documents is required. "
                + "Indicate here the full path to the document field that must be updated, e.g. annotation.populationFrequencies. This "
                + "parameter must be used together with a custom file provided at --input and the data to update indicated at --data.",
                arity = 1)
        public String field;

        @Parameter(names = {"--overwrite-inner-fields"}, description = "Use this parameter together with --fields to specify"
                + " which inner attributes shall be overwritten for updated objects, "
                + " e.g. --fields annotation --overwrite-inner-fields consequenceTypes,displayConsequenceType,conservation"
                + " List of inner fields must be specified as a comma-separated list (no spaces in between).",
                arity = 1)
        public String innerFields;

        @Parameter(names = {"-l", "--loader"}, description = "Database specific data loader to be used", required = false, arity = 1)
        public String loader = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";

        @Parameter(names = {"--num-threads"}, description = "Number of threads used for loading data into the database", arity = 1)
        public int numThreads = 2;

        @Parameter(names = {"--skip-index"}, description = "After loading, add index to the database", arity = 0)
        public boolean skipIndex;

        @DynamicParameter(names = "-D", description = "Dynamic parameters go here", hidden = true)
        public Map<String, String> loaderParams = new HashMap<>();

    }

    @Parameters(commandNames = {"index"}, commandDescription = "Create indexes in mongodb")
    public class IndexCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-d", "--data"}, description = "Data model type to be indexed: genome, gene, variation, "
                + "regulation, protein, ontology, clinical_variants, repeats, refseq and missense_variation_functional_score. 'all' "
                + "indexes everything", required = true,
                arity = 1)
        public String data;

        @Parameter(names = {"--database"}, description = "Database name.", required = true, arity = 1)
        public String database;

        @Parameter(names = {"--drop-indexes-first"}, description = "Use this flag to drop the indexes before creating new ones.", arity = 0)
        public boolean dropIndexesFirst;

        @Parameter(names = {"--validate"}, description = "Compare the existing indexes in specified database with the index JSON file",
                arity = 0)
        public boolean validate;
    }

    @Parameters(commandNames = {"install"}, commandDescription = "Set up sharding for CellBase")
    public class InstallCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @ParametersDelegate
        public SpeciesAndAssemblyCommandOptions speciesAndAssemblyOptions = speciesAndAssemblyCommandOptions;
    }

    @Parameters(commandNames = {"server"}, commandDescription = "Manage REST server")
    public class ServerCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"--start"}, description = "Start the REST server", arity = 0)
        public boolean start;

        @Parameter(names = {"--stop"}, description = "Stop the REST server", arity = 0)
        public boolean stop;

        @Parameter(names = {"--port"}, description = "REST port to be used", arity = 1)
        public int port;
    }

    @Parameters(commandNames = {"validate"}, commandDescription = "Compare CellBase HGVS strings to Ensembl VEP. Only valid for GRCh38.")
    public class ValidationCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly = "GRCh38";

        @Parameter(names = {"-i", "--input-file"}, description = "Full path to VCF", required = true, arity = 1)
        public String inputFile;

        @Parameter(names = {"-V", "--vep-file"}, description = "Full path to VEP annotation JSON file", required = true, arity = 1)
        public String vepFile;

        @Parameter(names = {"-o", "--output-dir"}, description = "Output directory where the comparison report is saved", required = false,
                arity = 1)
        public String outputDirectory = "/tmp";

        @Parameter(names = {"-t", "--type"}, description = "Which type to analyse: 'Protein', 'Transcript' or 'Both'", required =
                false, arity = 1)
        public String category = "protein";

        @Parameter(names = {"-m", "--mutation-type"}, description = "Which variant type to analyse: 'SNV', 'INSERTION', 'DELETION'. Leave "
                + "enpty to analyse all types",
                required = false, arity = 1)
        public String mutationType;
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

    public DownloadCommandOptions getDownloadCommandOptions() {
        return downloadCommandOptions;
    }

    public BuildCommandOptions getBuildCommandOptions() {
        return buildCommandOptions;
    }

    public LoadCommandOptions getLoadCommandOptions() {
        return loadCommandOptions;
    }

    public IndexCommandOptions getIndexCommandOptions() {
        return indexCommandOptions;
    }

    public InstallCommandOptions getInstallCommandOptions() { return installCommandOptions; }

    public ServerCommandOptions getServerCommandOptions() { return serverCommandOptions; }

    public ValidationCommandOptions getValidationCommandOptions() { return validationCommandOptions; }

}
