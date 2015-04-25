/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.cli;

import com.beust.jcommander.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 03/02/15.
 */
public class CliOptionsParser {

    private final JCommander jcommander;

    private final GeneralOptions generalOptions;
    private final CommonCommandOptions commonCommandOptions;

    private DownloadCommandOptions downloadCommandOptions;
    private BuildCommandOptions buildCommandOptions;
    private LoadCommandOptions loadCommandOptions;
    private QueryCommandOptions queryCommandOptions;
    private VariantAnnotationCommandOptions variantAnnotationCommandOptions;
    private PostLoadCommandOptions postLoadCommandOptions;


    public CliOptionsParser() {
        generalOptions = new GeneralOptions();

        jcommander = new JCommander(generalOptions);
        jcommander.setProgramName("cellbase.sh");

        commonCommandOptions = new CommonCommandOptions();

        downloadCommandOptions = new DownloadCommandOptions();
        buildCommandOptions = new BuildCommandOptions();
        loadCommandOptions = new LoadCommandOptions();
        queryCommandOptions = new QueryCommandOptions();
        variantAnnotationCommandOptions = new VariantAnnotationCommandOptions();
        postLoadCommandOptions = new PostLoadCommandOptions();

        jcommander.addCommand("download", downloadCommandOptions);
        jcommander.addCommand("build", buildCommandOptions);
        jcommander.addCommand("load", loadCommandOptions);
        jcommander.addCommand("query", queryCommandOptions);
        jcommander.addCommand("variant-annotation", variantAnnotationCommandOptions);
        jcommander.addCommand("post-load", postLoadCommandOptions);

    }

    public void parse(String[] args) throws ParameterException {
        jcommander.parse(args);
    }

    public String getCommand() {
        return (jcommander.getParsedCommand() != null) ? jcommander.getParsedCommand(): "";
    }

    public void printUsage(){
        if(getCommand().isEmpty()) {
            jcommander.usage();
        } else {
            jcommander.usage(getCommand());
        }
    }

    public class GeneralOptions {

        @Parameter(names = {"-h", "--help"}, help = true)
        public boolean help;
        @Parameter(names = {"--version"})
        public boolean version;

    }

    public class CommonCommandOptions {

        @Parameter(names = {"-h", "--help"}, help = true)
        public boolean help;

        @Parameter(names = {"-L", "--log-level"}, description = "This parameter set the level of the logging", required = false, arity = 1)
        public String logLevel = "info";

        @Parameter(names = {"-v", "--verbose"}, description = "This parameter set the level of the logging", required = false, arity = 1)
        public boolean verbose;

        @Parameter(names = {"-C", "--conf"}, description = "CellBase configuration json file. Have a look at cellbase/cellbase-core/src/main/resources/configuration.json for an example", required = false, arity = 1)
        public String conf;

    }


    @Parameters(commandNames = {"download"}, commandDescription = "Description")
    public class DownloadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to download: genome, gene, variation, regulation, protein, conservation and clinical. 'all' download everything.", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "The name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens' [Homo sapiens]", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "The name of the assembly, if empty the first assembly in configuration.json will be read", required = false, arity = 1)
        public String assembly;

        @Parameter(names = {"-o", "--output"}, description = "The output directory, species folder will be created [/tmp]", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--common"}, description = "Directory where common multi-species data will be downloaded, this is mainly protein and expression data [<OUTPUT>/common]", required = false, arity = 1)
        public String common;

    }


    @Parameters(commandNames = {"build"}, commandDescription = "Description")
    public class BuildCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "", required = false)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "", required = false)
        public String assembly;

        @Parameter(names = {"-i", "--input"}, description = "", required = true, arity = 1)
        public String input;

        @Parameter(names = {"-o", "--output"}, description = "", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--common"}, description = "", required = false, arity = 1)
        public String common;

    }


    @Parameters(commandNames = {"load"}, commandDescription = "Description")
    public class LoadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "Data type to be loaded, i.e. genome, gene, ...", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-i", "--input"}, description = "Input file or directory with the data to be loaded", required = true, arity = 1)
        public String input;

        @Parameter(names = {"--database"}, description = "Data type to be loaded, i.e. genome, gene, ...", required = true, arity = 1)
        public String database;

        @Parameter(names = {"-l", "--loader"}, description = "", required = false, arity = 1)
        public String loader = "org.opencb.cellbase.mongodb.loader.MongoDBCellBaseLoader";

        @Parameter(names = {"--num-threads"}, description = "Number of threads used for loading data into the database [2]", required = false, arity = 1)
        public int numThreads = 2;

        @DynamicParameter(names = "-D", description = "Dynamic parameters go here", hidden = true)
        public Map<String, String> loaderParams = new HashMap<>();

    }


    @Parameters(commandNames = {"query"}, commandDescription = "Description")
    public class QueryCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"--species"}, description = "", required = true)
        public String species;

        @Parameter(names = {"--assembly"}, description = "", required = false)
        public String assembly;

        @Parameter(names = {"--type"}, description = "", required = false, arity = 1)
        public String category;

        @Parameter(names = {"--id"}, description = "", required = false, variableArity = true)
        public List<String> ids;

        @Parameter(names = {"--resource"}, description = "", required = false, arity = 1)
        public String resource;

        @Deprecated
        @Parameter(names = {"--variant-annot"}, description = "", required = false)
        public boolean annotate;

        @Parameter(names = {"-i", "--input-file"}, description = "", required = false, arity = 1)
        public String inputFile;

        @Parameter(names = {"-o", "--output-file"}, description = "", required = false, arity = 1)
        public String outputFile;

        @Parameter(names = {"--host-url"}, description = "", required = false, arity = 1)
        public String url;

        @Parameter(names = {"--num-threads"}, description = "", required = false, arity = 1)
        public int threads = 2;

    }


    @Parameters(commandNames = {"variant-annotation"}, commandDescription = "Description")
    public class VariantAnnotationCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-s", "--species"}, description = "The name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens' [Homo sapiens]", required = true)
        public String species;

        @Parameter(names = {"-a", "--assembly"}, description = "The name of the assembly, if empty the first assembly in configuration.json will be read", required = false)
        public String assembly;

        @Parameter(names = {"-i", "--input-file"}, description = "Input file with the data file to be annotated", required = true, arity = 1)
        public String input;

        @Parameter(names = {"-o", "--output-file"}, description = "Output file with the annotations", required = true, arity = 1)
        public String output;

        @Parameter(names = {"-u", "--host-url"}, description = "The URL of CellBase REST web services [bioinfo.hpc.cam.ac.uk]", required = false, arity = 1)
        public String url = "bioinfo.hpc.cam.ac.uk";

        @Parameter(names = {"--port"}, description = "The port where REST web services are listening[80]", required = false, arity = 1)
        public int port = 80;

        @Parameter(names = {"-t", "--num-threads"}, description = "Number of threads to be used [4]", required = false, arity = 1)
        public int numThreads = 4;

        @Parameter(names = {"--batch-size"}, description = "Number of variants per thread [200]", required = false, arity = 1)
        public int batchSize = 200;

    }

    @Parameters(commandNames = {"post-load"}, commandDescription = "Description: complements data already loaded in CellBase")
    public class PostLoadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-a", "--assembly"}, description = "The name of the assembly", required = false, arity = 1)
        public String assembly = null;

        @Parameter(names = {"--clinical-annotation-file"}, description = "Specify a file containing variant annotations for CellBase clinical data. Accepted file formats: VEP's file format", required = false)
        public String clinicalAnnotationFilename = null;

    }


    public GeneralOptions getGeneralOptions() {
        return generalOptions;
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

    public QueryCommandOptions getQueryCommandOptions() {
        return queryCommandOptions;
    }

    public VariantAnnotationCommandOptions getVariantAnnotationCommandOptions() { return variantAnnotationCommandOptions; }

    public PostLoadCommandOptions getPostLoadCommandOptions() { return postLoadCommandOptions; }

}
