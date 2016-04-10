package org.opencb.cellbase.app.cli.variant.annotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Created by fjlopez on 07/04/16.
 */
public class BenchmarkDataWriter implements DataWriter<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> {

    private String annotator1Name;
    private String annotator2Name;
    private Path outdir;
    private BufferedWriter diff1Bw;
    private BufferedWriter diff2Bw;
    private BufferedWriter annotation1Bw;
    private BufferedWriter annotation2Bw;
    private ObjectWriter jsonObjectWriter;
    private int totalDiffVariants = 0;
    private int totalDiff1SequenceOntologyTerms = 0;
    private int totalDiff2SequenceOntologyTerms = 0;
    private Logger logger;


    public BenchmarkDataWriter(String annotator1Name, String annotator2Name, Path outdir) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.annotator1Name = annotator1Name;
        this.annotator2Name = annotator2Name;
        this.outdir = outdir;
    }

    @Override
    public boolean open() {
        try {
            OutputStream os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_" + annotator1Name + ".tsv.gz")));
            diff1Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_" + annotator2Name + ".tsv.gz")));
            diff2Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("annotation_" + annotator1Name + ".json.gz")));
            annotation1Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("annotation_" + annotator2Name + ".json.gz")));
            annotation2Bw = new BufferedWriter(new OutputStreamWriter(os));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean close() {
        try {
            diff1Bw.close();
            diff2Bw.close();
            annotation1Bw.close();
            annotation2Bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean pre() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();
        return true;
    }

    @Override
    public boolean post() {

       try {
           BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outdir.resolve("summary.tsv"))));
           bw.write("Total variants with at least one different annotation\t" + totalDiffVariants);
           bw.write("Total annotations provided by " + annotator1Name + " and not provided by " + annotator2Name + "\t"
                   + totalDiff1SequenceOntologyTerms);
           bw.write("Total annotations provided by " + annotator2Name + " and not provided by " + annotator1Name + "\t"
                   + totalDiff2SequenceOntologyTerms);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public boolean write(Pair<VariantAnnotationDiff, VariantAnnotationDiff> variantAnnotationDiffPair) {
        writeSequenceOntologyTerms(diff1Bw, variantAnnotationDiffPair.getLeft().getSequenceOntology());
        writeSequenceOntologyTerms(diff2Bw, variantAnnotationDiffPair.getRight().getSequenceOntology());
        try {
            annotation1Bw.write(jsonObjectWriter.writeValueAsString(variantAnnotationDiffPair.getLeft().getVariant()) + "\n");
            annotation2Bw.write(jsonObjectWriter.writeValueAsString(variantAnnotationDiffPair.getRight().getVariant()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        totalDiffVariants++;
        totalDiff1SequenceOntologyTerms +=  variantAnnotationDiffPair.getLeft().getSequenceOntology().size();
        totalDiff2SequenceOntologyTerms +=  variantAnnotationDiffPair.getRight().getSequenceOntology().size();

        return true;
    }

    private void writeSequenceOntologyTerms(BufferedWriter bw, List<SequenceOntologyTermComparisonObject> comparisonObjectList) {
        try {
            if (comparisonObjectList != null) {
                for (SequenceOntologyTermComparisonObject comparisonObject : comparisonObjectList) {
                    StringBuilder stringBuilder = new StringBuilder(comparisonObject.getTranscriptId());
                    stringBuilder.append("\t");
                    stringBuilder.append(comparisonObject.getName());
                    stringBuilder.append("\t");
                    stringBuilder.append(comparisonObject.getAccession());
                    stringBuilder.append("\n");
                    bw.write(stringBuilder.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean write(List<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> list) {
        if (list != null) {
            for (Pair<VariantAnnotationDiff, VariantAnnotationDiff> variantAnnotationDiffPair : list) {
                write(variantAnnotationDiffPair);
            }
            return true;
        } else {
            logger.warn("list of VariantAnnotationDiff is null");
        }
        return false;

    }



}
