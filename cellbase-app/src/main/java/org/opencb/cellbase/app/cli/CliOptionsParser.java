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
import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.utils.CommandLineUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by imedina on 03/02/15.
 */
public class CliOptionsParser {

    private final JCommander jCommander;

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

        jCommander = new JCommander(generalOptions);
        jCommander.setProgramName("cellbase.sh");

        commonCommandOptions = new CommonCommandOptions();

        downloadCommandOptions = new DownloadCommandOptions();
        buildCommandOptions = new BuildCommandOptions();
        loadCommandOptions = new LoadCommandOptions();
        queryCommandOptions = new QueryCommandOptions();
        variantAnnotationCommandOptions = new VariantAnnotationCommandOptions();
        postLoadCommandOptions = new PostLoadCommandOptions();

        jCommander.addCommand("download", downloadCommandOptions);
        jCommander.addCommand("build", buildCommandOptions);
        jCommander.addCommand("load", loadCommandOptions);
        jCommander.addCommand("query", queryCommandOptions);
        jCommander.addCommand("variant-annotation", variantAnnotationCommandOptions);
        jCommander.addCommand("post-load", postLoadCommandOptions);
    }

    public void parse(String[] args) throws ParameterException {
        jCommander.parse(args);
    }

    public String getCommand() {
        return (jCommander.getParsedCommand() != null) ? jCommander.getParsedCommand(): "";
    }

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

    public class GeneralOptions {

        @Parameter(names = {"-h", "--help"}, description = "Display this help and exit", help = true)
        public boolean help;

        @Parameter(names = {"--version"}, description = "Display the version and exit")
        public boolean version;

    }

    public class CommonCommandOptions {

        @Parameter(names = {"-h", "--help"}, description = "Display this help and exit", help = true)
        public boolean help;

        @Parameter(names = {"-L", "--log-level"}, description = "Set the logging level, accepted values are: debug, info, warn, error and fatal", required = false, arity = 1)
        public String logLevel = "info";

        @Deprecated
        @Parameter(names = {"-v", "--verbose"}, description = "[Deprecated] Set the level of the logging", required = false, arity = 1)
        public boolean verbose;

        @Parameter(names = {"-C", "--conf"}, description = "CellBase configuration.json file. Have a look at cellbase/cellbase-core/src/main/resources/configuration.json for an example", required = false, arity = 1)
        public String conf;

    }


    @Parameters(commandNames = {"download"}, commandDescription = "Download all different data sources provided in the configuration.json file")
    public class DownloadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to download: genome, gene, variation, regulation, protein, conservation, clinical and gene2disease. 'all' to download everything", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly = "GRCh37";

        @Parameter(names = {"-o", "--output"}, description = "The output directory, species folder will be created", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--common"}, description = "Directory where common multi-species data will be downloaded, this is mainly protein and expression data [<OUTPUT>/common]", required = false, arity = 1)
        public String common;

    }


    @Parameters(commandNames = {"build"}, commandDescription = "Build CellBase data models from all data sources downloaded")
    public class BuildCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to build: genome, gene, variation, regulation, protein, conservation, drug, clinvar, cosmic and GWAS CAatalog. 'all' build everything.", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be built, valid format include 'Homo sapiens' or 'hsapiens'", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly;

        @Parameter(names = {"-i", "--input"}, description = "Input directory with the downloaded data sources to be loaded", required = true, arity = 1)
        public String input;

        @Parameter(names = {"-o", "--output"}, description = "Output directory where the JSON data models are saved", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--common"}, description = "Directory where common multi-species data will be downloaded, this is mainly protein and expression data [<OUTPUT>/common]", required = false, arity = 1)
        public String common;

    }


    @Parameters(commandNames = {"load"}, commandDescription = "Load the built data models into the database")
    public class LoadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "Data model type to be loaded, i.e. genome, gene, ...", required = true, arity = 1)
        public String data;

        @Parameter(names = {"-i", "--input"}, description = "Input directory with the JSON data models to be loaded", required = true, arity = 1)
        public String input;

        @Parameter(names = {"--database"}, description = "Data model type to be loaded, i.e. genome, gene, ...", required = true, arity = 1)
        public String database;

        @Parameter(names = {"-l", "--loader"}, description = "Database specific data loader to be used", required = false, arity = 1)
        public String loader = "org.opencb.cellbase.mongodb.loader.MongoDBCellBaseLoader";

        @Parameter(names = {"--num-threads"}, description = "Number of threads used for loading data into the database", required = false, arity = 1)
        public int numThreads = 2;

        @DynamicParameter(names = "-D", description = "Dynamic parameters go here", hidden = true)
        public Map<String, String> loaderParams = new HashMap<>();

    }


    @Parameters(commandNames = {"query"}, commandDescription = "Query and fetch data from CellBase database using this command line")
    public class QueryCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", required = true, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly = "GRCh37";

        @Parameter(names = {"--type"}, description = "", required = false, arity = 1)
        public String category;

        @Parameter(names = {"--id"}, description = "", required = false, arity = 1)
        public String id;

        @Parameter(names = {"--resource"}, description = "", required = false, arity = 1)
        public String resource;

        @Parameter(names = {"-o", "--output-file"}, description = "", required = false, arity = 1)
        public String outputFile;

        @Deprecated
        @Parameter(names = {"--variant-annot"}, description = "[DEPRECATED]", required = false)
        public boolean annotate;

        @Deprecated
        @Parameter(names = {"-i", "--input-file"}, description = "[DEPRECATED]", required = false, arity = 1)
        public String inputFile;

        @Deprecated
        @Parameter(names = {"--host-url"}, description = "[DEPRECATED]", required = false, arity = 1)
        public String url;

    }


    @Parameters(commandNames = {"variant-annotation"}, commandDescription = "Annotate variants from VCF files using CellBase and other custom files")
    public class VariantAnnotationCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-i", "--input-file"}, description = "Input file with the data file to be annotated", required = false, arity = 1)
        public String input;

        @Parameter(names = {"--variant"}, description = "A comma separated variant list in the format chr:pos:ref:alt, ie. 1:451941:A:T,19:45411941:T:C", required = false, arity = 1)
        public String variant;

        @Parameter(names = {"-o", "--output-file"}, description = "Output file with the annotations", required = false, arity = 1)
        public String output;

        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", required = true, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be read", required = false, arity = 1)
        public String assembly = "GRCh37";

        @Parameter(names = {"-l", "--local"}, description = "Database credentials for local annotation are read from configuration.json file", required = false, arity = 0)
        public boolean local;

        @Parameter(names = {"--remote-url"}, description = "The URL of CellBase REST web services, this has no effect if --local is present", required = false, arity = 1)
        public String url = "bioinfodev.hpc.cam.ac.uk";

        @Parameter(names = {"--remote-port"}, description = "The port where REST web services are listening", required = false, arity = 1)
        public int port = 80;

        @Parameter(names = {"--include"}, description = "Comma separated list of annotators to be included", required = false)
        public String include;

        @Parameter(names = {"--exclude"}, description = "Comma separated list of annotators to be excluded", required = false)
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

        @Parameter(names = {"--output-format"}, description = "Variant annotation output format. Values: JSON, PB, VEP", required = false, arity = 1)
        public String outputFormat = "JSON";

        @Parameter(names = {"--gzip"}, description = "Whether the output file is gzipped", required = false, arity = 0)
        public boolean gzip;

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

    public void printUsage(){
        if(getCommand().isEmpty()) {
            System.err.println("");
            System.err.println("Program:     CellBase (OpenCB)");
            System.err.println("Version:     3.2.0");
            System.err.println("Description: High-Performance NoSQL database and RESTful web services to access the most relevant biological data");
            System.err.println("");
            System.err.println("Usage:       cellbase.sh [-h|--help] [--version] <command> [options]");
            System.err.println("");
            System.err.println("Commands:");
            printMainUsage();
            System.err.println("");
        } else {
            String parsedCommand = getCommand();
            System.err.println("");
            System.err.println("Usage:   cellbase.sh " + parsedCommand + " [options]");
            System.err.println("");
            System.err.println("Options:");
            CommandLineUtils.printCommandUsage(jCommander.getCommands().get(parsedCommand));
            System.err.println("");
        }
    }

    private void printMainUsage() {
        for (String s : jCommander.getCommands().keySet()) {
            System.err.printf("%20s  %s\n", s, jCommander.getCommandDescription(s));
        }
    }

    public GeneralOptions getGeneralOptions() {
        return generalOptions;
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

    public QueryCommandOptions getQueryCommandOptions() {
        return queryCommandOptions;
    }

    public VariantAnnotationCommandOptions getVariantAnnotationCommandOptions() { return variantAnnotationCommandOptions; }

    public PostLoadCommandOptions getPostLoadCommandOptions() { return postLoadCommandOptions; }

}
