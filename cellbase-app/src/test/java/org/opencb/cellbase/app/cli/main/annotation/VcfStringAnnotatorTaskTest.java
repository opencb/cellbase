package org.opencb.cellbase.app.cli.main.annotation;

import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.FullVcfCodec;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.StringDataReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;

public class VcfStringAnnotatorTaskTest {

    private static final String DP_TAG = "DP";
    private static final String AU_TAG = "AU";
    private static final String FDP_TAG = "FDP";
    private final Path resourcesFolder = Paths.get(getClass().getResource("/variant/annotation/").toURI());

    public VcfStringAnnotatorTaskTest() throws URISyntaxException {
    }

    @Test
    public void testSampleDataKeepsOriginalOrder() throws Exception {
        Path input = Paths.get(resourcesFolder.resolve("sample1_sample2.vcf.gz").toString());
        FullVcfCodec codec = new FullVcfCodec();
        VcfStringAnnotatorTask vcfStringAnnotatorTask;
        try (InputStream fileInputStream = input.toString().endsWith("gz")
                ? new GZIPInputStream(new FileInputStream(input.toFile()))
                : new FileInputStream(input.toFile())) {
            LineIterator lineIterator = codec.makeSourceFromStream(fileInputStream);
            VCFHeader header = (VCFHeader) codec.readActualHeader(lineIterator);
            VCFHeaderVersion headerVersion = codec.getVCFHeaderVersion();
            vcfStringAnnotatorTask = new VcfStringAnnotatorTask(header, headerVersion,
                    Collections.emptyList(), null, false, null);
        } catch (IOException e) {
            throw new IOException("Unable to read VCFHeader");
        }

        DataReader<String> dataReader = new StringDataReader(input);
        dataReader.open();
        dataReader.pre();

        List<String> line = dataReader.read();
        while (line != null && !line.isEmpty() && line.get(0).startsWith("#")) {
            line = dataReader.read();
        }
        dataReader.post();
        dataReader.close();
        List<Variant> variantList = vcfStringAnnotatorTask.apply(line);

        assertEquals(1, variantList.size());
        Variant variant = variantList.get(0);
        assertEquals(1, variant.getStudies().size());
        List<String> format = variant.getStudies().get(0).getFormat();
        List<List<String>> samplesData = variant.getStudies().get(0).getSamplesData();

        // Check samples data is exactly in the same order as in the VCF (not alphabetical!)
        assertEquals(2, samplesData.size());
        int dpPosition = format.indexOf(DP_TAG);
        assertEquals("54", samplesData.get(0).get(dpPosition));
        assertEquals("152", samplesData.get(1).get(dpPosition));
        int auPosition = format.indexOf(AU_TAG);
        assertEquals("0,5", samplesData.get(0).get(auPosition));
        assertEquals("3,26", samplesData.get(1).get(auPosition));
        int fdpPosition = format.indexOf(FDP_TAG);
        assertEquals("14", samplesData.get(0).get(fdpPosition));
        assertEquals("42", samplesData.get(1).get(fdpPosition));

    }
}
