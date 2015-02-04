package org.opencb.cellbase.app.cli;

import com.beust.jcommander.*;

import java.util.List;

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

    public CliOptionsParser() {
        generalOptions = new GeneralOptions();

        jcommander = new JCommander(generalOptions);
        jcommander.setProgramName("cellbase.sh");

        commonCommandOptions = new CommonCommandOptions();

        downloadCommandOptions = new DownloadCommandOptions();
        buildCommandOptions = new BuildCommandOptions();
        loadCommandOptions = new LoadCommandOptions();

        jcommander.addCommand("download", downloadCommandOptions);
        jcommander.addCommand("build", buildCommandOptions);
        jcommander.addCommand("load", loadCommandOptions);

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
        public String logLevel;

        @Parameter(names = {"-v", "--verbose"}, description = "This parameter set the level of the logging", required = false, arity = 1)
        public boolean verbose;

        @Parameter(names = {"-C", "--conf"}, description = "This parameter set the level of the logging", required = false, arity = 1)
        public String conf;
    }

    @Parameters(commandNames = {"download"}, commandDescription = "Description")
    public class DownloadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-s", "--species"}, description = "", required = true, arity = 1)
        public String species;

        @Parameter(names = {"-a", "--assembly"}, description = "", required = true, arity = 1)
        public String assembly;

        @Parameter(names = {"-o", "--output-dir"}, description = "", required = false, arity = 1)
        public String outputDir = "/tmp";

        @Parameter(names = {"--sequence"}, description = "", required = false)
        public boolean sequence = false;

        @Parameter(names = {"--gene"}, description = "", required = false)
        public boolean gene = false;

        @Parameter(names = {"--variation"}, description = "", required = false)
        public boolean variation = false;

        @Parameter(names = {"--regulation"}, description = "", required = false)
        public boolean regulation = false;

        @Parameter(names = {"--protein"}, description = "", required = false)
        public boolean protein = false;
    }

    @Parameters(commandNames = {"build"}, commandDescription = "Description")
    public class BuildCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;

        @Parameter(names = {"-b", "--build"}, description = "", required = true, variableArity = true)
        public List<String> build;

        @Parameter(names = {"-i", "--input"}, description = "", required = true, arity = 1)
        public String input;

        @Parameter(names = {"-o", "--output"}, description = "", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--reference-genome-file"}, description = "", required = false)
        public String referenceGenomeFile;
    }

    @Parameters(commandNames = {"load"}, commandDescription = "Description")
    public class LoadCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-l", "--load"}, description = "", required = true, arity = 1)
        public String load;

        @Parameter(names = {"-i", "--input-file"}, description = "", required = false, arity = 1)
        public String inputFile;

        @Parameter(names = {"--input-dir"}, description = "", required = false, arity = 1)
        public String inputDir;

        @Parameter(names = {"--host"}, description = "", required = false, arity = 1)
        public String host;

        @Parameter(names = {"--port"}, description = "", required = false)
        public int port;

        @Parameter(names = {"--user"}, description = "", required = false, arity = 1)
        public String user;

        @Parameter(names = {"--password"}, description = "", required = false, arity = 1)
        public String password;

        @Parameter(names = {"--indexFile"}, description = "", required = false, arity = 1)
        public String indexFile;
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

}
