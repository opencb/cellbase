package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.formats.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 20/02/15.
 */
public class QueryCommandParser extends CommandParser {

    private CliOptionsParser.QueryCommandOptions queryCommandOptions;

    private Path inputFile;
    private Path outputFile;

    public QueryCommandParser(CliOptionsParser.QueryCommandOptions queryCommandOptions) {
        super(queryCommandOptions.commonOptions.logLevel, queryCommandOptions.commonOptions.verbose,
                queryCommandOptions.commonOptions.conf);

        this.queryCommandOptions = queryCommandOptions;
    }

    @Override
    public void parse() {
        checkParameters();
        try {
            if (queryCommandOptions.annotate && inputFile != null && inputFile.toString().toLowerCase().endsWith(".vcf")) {
                annotateVcfFile();
            }
        } catch (ParameterException e) {
            logger.error("Error parsing 'query' command line parameters: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void checkParameters() {
        // input file
        if (queryCommandOptions.inputFile != null) {
            inputFile = Paths.get(queryCommandOptions.inputFile);
            if (!inputFile.toFile().exists()) {
                throw new ParameterException("Input file " + inputFile + " doesn't exist");
            } else if (inputFile.toFile().isDirectory()) {
                throw new ParameterException("Input file cannot be a directory: " + inputFile);
            }
        }
        // TODO: check that output file is not a directory
    }

    private void annotateVcfFile() throws URISyntaxException, IOException {
        // TODO: extract this to a new class (AnnotationRunner?)
        VcfRawReader vcfReader = new VcfRawReader(inputFile.toString());
        VepFormatWriter vepWriter = new VepFormatWriter(queryCommandOptions.outputFile);
        if (vcfReader.open() && vepWriter.open()) {
            vcfReader.pre();
            vepWriter.pre();

            CellBaseClient cellBaseClient = getCellBaseClient();

            List<VcfRecord> vcfBatch = vcfReader.read(100);
            List<GenomicVariant> variantBatch;
            while (!vcfBatch.isEmpty()) {
                variantBatch = convertVcfRecordsToGenomicVariants(vcfBatch);
                QueryResponse<QueryResult<VariantAnnotation>> response =
                        cellBaseClient.getFullAnnotation(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.variant, variantBatch, new QueryOptions());
                writeResponse(response, vepWriter);
                vcfBatch = vcfReader.read(100);
            }
            vcfReader.post();
            vcfReader.close();
            vepWriter.post();
            vepWriter.close();
        }
    }

    private CellBaseClient getCellBaseClient() throws URISyntaxException {
        CellBaseConfiguration.DatabaseProperties cellbaseDDBBProperties = configuration.getDatabase();
//        String host = cellbaseDDBBProperties.getHost();
//        int port = Integer.parseInt(cellbaseDDBBProperties.getPort());
        // TODO: read path from configuration file?
        // TODO: hardcoded port???
        String path = "/cellbase/webservices/rest/";
        return new CellBaseClient(queryCommandOptions.url, 80, path, configuration.getVersion(), queryCommandOptions.species);
    }

    private List<GenomicVariant> convertVcfRecordsToGenomicVariants(List<VcfRecord> vcfBatch) {
        List<GenomicVariant> genomicVariants = new ArrayList<>(vcfBatch.size());
        for (VcfRecord vcfRecord : vcfBatch) {
            genomicVariants.add(getGenomicVariant(vcfRecord));
        }
        return genomicVariants;
    }

    private GenomicVariant getGenomicVariant(VcfRecord vcfRecord) {
        int ensemblPos;
        String ref, alt;
        if (vcfRecord.getReference().length() > 1) {
            ref = vcfRecord.getReference().substring(1);
            alt = "-";
            ensemblPos = vcfRecord.getPosition() + 1;
        } else if (vcfRecord.getAlternate().length() > 1) {
            ensemblPos = vcfRecord.getPosition() + 1;
            if (vcfRecord.getAlternate().equals("<DEL>")) {
                // large deletion
                String[] infoFields = vcfRecord.getInfo().split(";");
                int i = 0;
                while(i<infoFields.length && !infoFields[i].startsWith("END=")) {
                    i++;
                }
                int end = Integer.parseInt(infoFields[i].split("=")[1]);
                ref = StringUtils.repeat("N", end - vcfRecord.getPosition());
                alt = "-";
            } else {
                // short insertion
                ref = "-";
                alt = vcfRecord.getAlternate().substring(1);
            }
        } else {
            // SNV
            ref = vcfRecord.getReference();
            alt = vcfRecord.getAlternate();
            ensemblPos = vcfRecord.getPosition();
        }
        return new GenomicVariant(vcfRecord.getChromosome(), ensemblPos, ref, alt);
    }

    private void writeResponse(QueryResponse<QueryResult<VariantAnnotation>> response, VepFormatWriter vepWriter) {
        for (QueryResult<VariantAnnotation> queryResult : response.getResponse()) {
             vepWriter.write(queryResult.getResult());
        }
    }
}
