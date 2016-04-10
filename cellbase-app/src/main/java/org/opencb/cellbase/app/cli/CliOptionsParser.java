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
import org.opencb.commons.utils.CommandLineUtils;

import java.util.*;

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
    private QueryGrpcCommandOptions queryGrpcCommandOptions;
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
        queryGrpcCommandOptions = new QueryGrpcCommandOptions();
        variantAnnotationCommandOptions = new VariantAnnotationCommandOptions();
        postLoadCommandOptions = new PostLoadCommandOptions();

        jCommander.addCommand("download", downloadCommandOptions);
        jCommander.addCommand("build", buildCommandOptions);
        jCommander.addCommand("load", loadCommandOptions);
        jCommander.addCommand("query", queryCommandOptions);
        jCommander.addCommand("query-grpc", queryGrpcCommandOptions);
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

        @Parameter(names = {"-C", "--config"}, description = "CellBase configuration.json file. Have a look at cellbase/cellbase-core/src/main/resources/configuration.json for an example", required = false, arity = 1)
        public String conf;

    }


    @Parameters(commandNames = {"download"}, commandDescription = "Download all different data sources provided in the configuration.json file")
    public class DownloadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to download: genome, gene, gene_disease_association, variation, variation_functional_score, regulation, protein, conservation, clinical and . 'all' to download everything", required = true, arity = 1)
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


        @Parameter(names = {"-d", "--data"}, description = "Comma separated list of data to build: genome, gene, disgenet, hpo, variation, cadd, regulation, protein, conservation, drug, clinvar, cosmic and GWAS CAatalog. 'all' build everything.", required = true, arity = 1)
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

        @Parameter(names = {"-i", "--input"}, description = "Input directory with the JSON data models to be loaded. Can also be used to specify a" +
                "custom json file to be loaded (look at the --field parameter).", required = true, arity = 1)
        public String input;

        @Parameter(names = {"--database"}, description = "Data model type to be loaded, i.e. genome, gene, ...", required = true, arity = 1)
        public String database;

        @Parameter(names = {"--field"}, description = "Use this parameter when an custom update of the database documents is required. Indicate here" +
                "the full path to the document field that must be updated, e.g. annotation.populationFrequencies. This parameter must be used together" +
                "with a custom file provided at --input and the data to update indicated at --data.", required = false, arity = 1)
        public String field;

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


        @Parameter(names = {"-s", "--species"}, description = "Name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens'", arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "Name of the assembly, if empty the first assembly in configuration.json will be used", required = false, arity = 1)
        public String assembly = "GRCh37";

        @Parameter(names = {"-o", "--output"}, description = "Write result into the file path", required = false, arity = 1)
        public String output;

        @Parameter(names = {"-t", "--type"}, description = "", required = true, arity = 1)
        public String category;

        @Parameter(names = {"--id"}, description = "", required = false, arity = 1)
        public String id;

        @Parameter(names = {"-r", "--resource"}, description = "", required = false, arity = 1)
        public String resource;

        @Parameter(names = {"--region"}, description = "", required = false, arity = 1)
        public String region;

        @Parameter(names = {"--group-by"}, description = "", required = false, arity = 1)
        public String groupBy;

        @Parameter(names = {"--rank"}, description = "", required = false, arity = 1)
        public String rank;

        @Parameter(names = {"--distinct"}, description = "", required = false, arity = 1)
        public String distinct;

        @Parameter(names = {"--histogram"}, description = "", required = false, arity = 0)
        public boolean histogram;

        @Parameter(names = {"--interval"}, description = "", required = false, arity = 1)
        public int interval = 100000;

        @DynamicParameter(names = {"-O", "--options"}, description = "Filter options in the form of -Oa=b, eg. -Obiotype=protein_coding,pseudogene -Oregion=3:44444-55555", required = false)
        public Map<String, String> options = new HashMap<>();

        // QueryOptions parameters
        @Parameter(names = {"-i", "--include"}, description = "Comma separated list of fields to be included, eg. chromsome,start,end", required = false)
        public String include;

        @Parameter(names = {"-e", "--exclude"}, description = "Comma separated list of fields to be excluded, eg. chromsome,start,end", required = false)
        public String exclude;

        @Parameter(names = {"--skip"}, description = "Skip the number of records specified", required = false)
        public int skip;

        @Parameter(names = {"--limit"}, description = "Return the number of records specified", required = false)
        public int limit;

        @Parameter(names = {"-c", "--count"}, description = "Comma separated list of annotators to be excluded", required = false, arity = 0)
        public boolean count;

    }

    @Parameters(commandNames = {"query-grpc"}, commandDescription = "Query and fetch data from CellBase database using gRPC server")
    public class QueryGrpcCommandOptions extends QueryCommandOptions {

        @Parameter(names = {"--host"}, description = "", required = false, arity = 1)
        public String host = "localhost";

        @Parameter(names = {"--port"}, description = "", required = false, arity = 1)
        public int port = 9090;
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
        public String assembly = "GRCh37";

        @Parameter(names = {"-l", "--local"}, description = "Database credentials for local annotation are read from configuration.json file", required = false, arity = 0)
        public boolean local;

        @Parameter(names = {"--remote-url"}, description = "The URL of CellBase REST web services, this has no effect if --local is present", required = false, arity = 1)
        public String url = "bioinfodev.hpc.cam.ac.uk:80/cellbase/webservices/rest";

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
                + "a directory containing a list of Variant Effect Predictor (VEP) files is expected at the -i parameter." +
                "All .txt files within the directory will be processed - the directory must contain only .txt files that " +
                "should be tested",
                required = false, arity = 0)
        public boolean benchmark;
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

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public void printUsage(){
        if(getCommand().isEmpty()) {
            System.err.println("");
            System.err.println("Program:     " + ANSI_WHITE + "CellBase (OpenCB)" + ANSI_RESET);
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

    public QueryGrpcCommandOptions getQueryGrpcCommandOptions() {
        return queryGrpcCommandOptions;
    }

    public VariantAnnotationCommandOptions getVariantAnnotationCommandOptions() { return variantAnnotationCommandOptions; }

    public PostLoadCommandOptions getPostLoadCommandOptions() { return postLoadCommandOptions; }

}
