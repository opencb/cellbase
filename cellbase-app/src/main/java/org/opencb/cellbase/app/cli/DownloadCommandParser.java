package org.opencb.cellbase.app.cli;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandParser extends CommandParser {

    private CliOptionsParser.DownloadCommandOptions downloadCommandOptions;

    public DownloadCommandParser(CliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.verbose,
                downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
    }


    /**
     * Parse specific 'download' command options
     */
    public void parse() {

        if(downloadCommandOptions.sequence) {
            System.out.println("sequence");
        }

    }
}
