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

    private DownloadCommandOptions downloadCommandOptions;
    private BuildCommandOptions buildCommandOptions;
    private LoadCommandOptions loadCommandOptions;
    private PostLoadCommandOptions postLoadCommandOptions;

    public AdminCliOptionsParser() {
        jCommander.setProgramName("cellbase-admin.sh");
        commonCommandOptions = new CommonCommandOptions();

        downloadCommandOptions = new DownloadCommandOptions();
        buildCommandOptions = new BuildCommandOptions();
        loadCommandOptions = new LoadCommandOptions();
        postLoadCommandOptions = new PostLoadCommandOptions();

        jCommander.addCommand("download", downloadCommandOptions);
        jCommander.addCommand("build", buildCommandOptions);
        jCommander.addCommand("load", loadCommandOptions);
        jCommander.addCommand("post-load", postLoadCommandOptions);
    }

    public void parse(String[] args) throws ParameterException {
        jCommander.parse(args);
    }

    @Parameters(commandNames = {"download"}, commandDescription = "Download all different data sources provided in the configuration.json file")
    public class DownloadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to download: genome, gene, "
                + "variation, variation_functional_score, regulation, protein, conservation, "
                + "clinical_variants, repeats, svs and 'all' to download everything", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly = null;

        @Parameter(names = {"-o", "--output"}, description = "The output directory, species folder will be created", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--common"}, description = "Directory where common multi-species data will be downloaded, this is mainly protein and expression data [<OUTPUT>/common]", required = false, arity = 1)
        public String common;

    }


    @Parameters(commandNames = {"build"}, commandDescription = "Build CellBase data models from all data sources downloaded")
    public class BuildCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to build: genome, genome_info, "
                + "gene, variation, variation_functional_score, regulation, protein, ppi, conservation, drug, "
                + "clinical_variants, repeats, svs. 'all' builds everything.", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be built, valid format include 'Homo sapiens' or 'hsapiens'", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly = null;

        @Parameter(names = {"-i", "--input"}, description = "Input directory with the downloaded data sources to be loaded", required = true, arity = 1)
        public String input;

        @Parameter(names = {"-o", "--output"}, description = "Output directory where the JSON data models are saved", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--common"}, description = "Directory where common multi-species data will be downloaded, this is mainly protein and expression data [<OUTPUT>/common]", required = false, arity = 1)
        public String common;

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
                + "variation_functional_score, conservation, regulation, protein, ppi, protein_functional_prediction, "
                + "clinical_variants, repeats, svs. 'all' loads everything", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-i", "--input"}, description = "Input directory with the JSON data models to be loaded. Can also be used to specify a" +
                "custom json file to be loaded (look at the --field parameter).", required = true, arity = 1)
        public String input;

        @Parameter(names = {"--database"}, description = "Data model type to be loaded, i.e. genome, gene, ...", required = true, arity = 1)
        public String database;

        @Parameter(names = {"--fields"}, description = "Use this parameter when an custom update of the database documents is required. Indicate here" +
                " the full path to the document field that must be updated, e.g. annotation.populationFrequencies. This parameter must be used together" +
                "with a custom file provided at --input and the data to update indicated at --data.", required = false, arity = 1)
        public String field;

        @Parameter(names = {"--overwrite-inner-fields"}, description = "Use this parameter together with --fields to specify"
                + " which inner attributes shall be overwritten for updated objects, "
                + " e.g. --fields annotation --overwrite-inner-fields consequenceTypes,displayConsequenceType,conservation"
                + " List of inner fields must be specified as a comma-separated list (no spaces in between).",
                required = false, arity = 1)
        public String innerFields;

        @Parameter(names = {"-l", "--loader"}, description = "Database specific data loader to be used", required = false, arity = 1)
        public String loader = "org.opencb.cellbase.lib.loader.MongoDBCellBaseLoader";

        @Parameter(names = {"--num-threads"}, description = "Number of threads used for loading data into the database", required = false, arity = 1)
        public int numThreads = 2;

        @DynamicParameter(names = "-D", description = "Dynamic parameters go here", hidden = true)
        public Map<String, String> loaderParams = new HashMap<>();

    }

    @Parameters(commandNames = {"post-load"}, commandDescription = "Complements data already loaded in CellBase")
    public class PostLoadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-a", "--assembly"}, description = "The name of the assembly", required = false, arity = 1)
        public String assembly = null;

        @Parameter(names = {"--clinical-annotation-file"}, description = "Specify a file containing variant annotations for CellBase clinical data. Accepted file formats: VEP's file format", required = false)
        public String clinicalAnnotationFilename = null;

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

    public PostLoadCommandOptions getPostLoadCommandOptions() { return postLoadCommandOptions; }

}
