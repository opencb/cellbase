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


        @Parameter(names = {"-s", "--species"}, description = "The name of the species to be downloaded, valid format include 'Homo sapiens' or 'hsapiens' [Homo sapiens]", required = false, arity = 1)
        public String species = "Homo sapiens";

        @Parameter(names = {"-a", "--assembly"}, description = "The name of the assembly, if empty the first assembly in configuration.json will be read", required = false, arity = 1)
        public String assembly;

        @Parameter(names = {"-o", "--output"}, description = "The output directory, species folder will be created [/tmp]", required = false, arity = 1)
        public String output = "/tmp";

        @Parameter(names = {"--all"}, description = "Downloads all data in configuration.json for the species", required = false)
        public boolean all = false;

        @Parameter(names = {"--genome"}, description = "Downloads Ensembl Reference genome sequence from EMBL-EBI", required = false)
        public boolean genome = false;

        @Parameter(names = {"--gene"}, description = "Downloads Ensembl and NCBI RefSeq gene sets", required = false)
        public boolean gene = false;

        @Parameter(names = {"--variation"}, description = "Downloads Ensembl Variation data from EMBL-EBI", required = false)
        public boolean variation = false;

        @Parameter(names = {"--regulation"}, description = "Downloads Ensembl Regulatory and miRNA and targets", required = false)
        public boolean regulation = false;

        @Parameter(names = {"--protein"}, description = "Downloads UniProt, IntAct and InterPro if 'protein' is present in 'data' of configuration.json", required = false)
        public boolean protein = false;

        @Parameter(names = {"--conservation"}, description = "Downloads PhastCons and PhyloP from UCSC, only for human and mouse", required = false)
        public boolean conservation = false;

        @Parameter(names = {"--clinical"}, description = "Downloads ClinVar, Cosmic and GWAS data for Human only", required = false)
        public boolean clinical = false;

    }


    @Parameters(commandNames = {"build"}, commandDescription = "Description")
    public class BuildCommandOptions {

        @ParametersDelegate
        public CommonCommandOptions commonOptions = commonCommandOptions;


        @Parameter(names = {"-b", "--build"}, description = "", required = true, arity = 1)
        public String build;

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


        @Parameter(names = {"-i", "--input"}, description = "Input file or directory with the data to be loaded", required = true, arity = 1)
        public String input;

        @Parameter(names = {"-d", "--data"}, description = "Data type to be loaded, i.e. genome_sequence, gene, ...", required = true, arity = 1)
        public String load;

        @Parameter(names = {"-l", "--loader"}, description = "", required = false, arity = 1)
        public String loader = "org.opencb.cellbase.mongodb.loader.MongoDBCellBaseLoader";

        @DynamicParameter(names = "-D", description = "Dynamic parameters go here", hidden = true)
        public Map<String, String> loaderParams = new HashMap<>();

        @Parameter(names = {"--host"}, description = "Database host name [localhost]", required = false, arity = 1)
        public String host = "localhost";

        @Parameter(names = {"--port"}, description = "", required = false)
        public int port;

        @Parameter(names = {"--user"}, description = "Database user with write access []", required = false, arity = 1)
        public String user = "";

        @Parameter(names = {"--password"}, description = "Database user's password []", required = false, arity = 1)
        public String password = "";

        @Parameter(names = {"--indexFile"}, description = "", required = false, arity = 1)
        public String indexFile;

        @Parameter(names = {"--num-threads"}, description = "Number of threads used for loading data into the database [2]", required = false, arity = 1)
        public int threads = 2;

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
