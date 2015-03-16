package org.opencb.cellbase.app.query;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.formats.annotation.io.VepFormatWriter;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by parce on 25/02/15.
 */
public class VcfAnnotator {

    private Path inputFile;
    private Path outputFile;
    private CellBaseClient cellBaseClient;

    public static final int BATCH_SIZE = 100;

    public VcfAnnotator(Path inputFile, Path outputFile, CellBaseClient cellBaseClient) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.cellBaseClient = cellBaseClient;
    }

    public void annotateVcfFile() throws URISyntaxException, IOException {
        VcfRawReader vcfReader = new VcfRawReader(inputFile.toString());
        VepFormatWriter vepWriter = new VepFormatWriter(outputFile.toString());
        if (vcfReader.open() && vepWriter.open()) {
            vcfReader.pre();
            vepWriter.pre();

            List<VcfRecord> vcfBatch = vcfReader.read(BATCH_SIZE);
            List<GenomicVariant> variantBatch;
            while (!vcfBatch.isEmpty()) {
                variantBatch = convertVcfRecordsToGenomicVariants(vcfBatch);
                QueryResponse<QueryResult<VariantAnnotation>> response =
                        cellBaseClient.getFullAnnotation(CellBaseClient.Category.genomic, CellBaseClient.SubCategory.variant, variantBatch, new QueryOptions());
                writeResponse(response, vepWriter);
                vcfBatch = vcfReader.read(BATCH_SIZE);
            }
            close(vcfReader, vepWriter);
        }
    }

    private void close(VcfRawReader vcfReader, VepFormatWriter vepWriter) {
        vcfReader.post();
        vcfReader.close();
        vepWriter.post();
        vepWriter.close();
    }

    private List<GenomicVariant> convertVcfRecordsToGenomicVariants(List<VcfRecord> vcfBatch) {
        List<GenomicVariant> genomicVariants = new ArrayList<>(vcfBatch.size());
        for (VcfRecord vcfRecord : vcfBatch) {
            genomicVariants.add(getGenomicVariant(vcfRecord));
        }
        return genomicVariants;
    }

    // TODO: use a external class for this (this method could be added to GenomicVariant class)
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
