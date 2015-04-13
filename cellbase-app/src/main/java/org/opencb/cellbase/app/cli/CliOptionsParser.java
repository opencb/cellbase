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

        jcommander.addCommand("download", downloadCommandOptions);
        jcommander.addCommand("build", buildCommandOptions);
        jcommander.addCommand("load", loadCommandOptions);
        jcommander.addCommand("query", queryCommandOptions);
        jcommander.addCommand("variant-annotation", variantAnnotationCommandOptions);

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

        @Parameter(names = {"-C", "--conf"}, description = "This parameter set the level of the logging", required = false, arity = 1)
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


        @Parameter(names = {"--species"}, description = "", required = true)
        public String species;

        @Parameter(names = {"--assembly"}, description = "", required = false)
        public String assembly;

        @Parameter(names = {"-i", "--input-file"}, description = "", required = true, arity = 1)
        public String inputFile;

        @Parameter(names = {"-o", "--output-file"}, description = "", required = true, arity = 1)
        public String outputFile;

        @Parameter(names = {"--host-url"}, description = "", required = false, arity = 1)
        public String url = "wwwdev.ebi.ac.uk";

        @Parameter(names = {"--port"}, description = "", required = false, arity = 1)
        public int port = 80;

        @Parameter(names = {"--num-threads"}, description = "", required = false, arity = 1)
        public int threads = 2;

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

    public VariantAnnotationCommandOptions getVariantAnnotationCommandOptions() {
        return variantAnnotationCommandOptions;
    }

}
