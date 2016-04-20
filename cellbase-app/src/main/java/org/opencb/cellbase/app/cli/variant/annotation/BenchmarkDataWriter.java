package org.opencb.cellbase.app.cli.variant.annotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
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

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

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
    private int totalVariants = 0;
    private int totalAnnotations1 = 0;
    private int totalAnnotations2 = 0;
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
            OutputStream os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_" + annotator1Name + ".tsv.gz"),
                    CREATE, APPEND));
            diff1Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_" + annotator2Name + ".tsv.gz"),
                    CREATE, APPEND));
            diff2Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("annotation_" + annotator1Name + ".json.gz"),
                    CREATE, APPEND));
            annotation1Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("annotation_" + annotator2Name + ".json.gz"),
                    CREATE, APPEND));
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
           bw.write("Total number of checked variants\t" + totalVariants + "\n");
           bw.write("Total number of annotations provided by " + annotator1Name + "\t" + totalAnnotations1 + "\n");
           bw.write("Total number of annotations provided by " + annotator2Name + "\t" + totalAnnotations2 + "\n");
           bw.write("Total number of variants with conflicting annotation\t" + totalDiffVariants + "/" + totalVariants
                   + "\t" + (100 - totalDiffVariants * 100.0 / totalVariants) + "% coincidence\n");
           bw.write("Total annotations provided by " + annotator1Name + " and not provided by " + annotator2Name + "\t"
                   + totalDiff1SequenceOntologyTerms + "/" + totalAnnotations1 + "\t"
                   + (100 - totalDiff1SequenceOntologyTerms * 100.0 / totalAnnotations1) + "% coincidence\n");
           bw.write("Total annotations provided by " + annotator2Name + " and not provided by " + annotator1Name + "\t"
                   + totalDiff2SequenceOntologyTerms + "/" + totalAnnotations2 + "\t"
                   + (100 - totalDiff2SequenceOntologyTerms * 100.0 / totalAnnotations2) + "% coincidence\n");
           bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public boolean write(Pair<VariantAnnotationDiff, VariantAnnotationDiff> variantAnnotationDiffPair) {
        // Write differing sequence ontology terms
        writeSequenceOntologyTerms(diff1Bw, variantAnnotationDiffPair.getLeft());
        writeSequenceOntologyTerms(diff2Bw, variantAnnotationDiffPair.getRight());
        // Write full variant annotations for conflicting variants only - i.e. variants with differing annotations
        if (!(variantAnnotationDiffPair.getLeft().isEmpty() && variantAnnotationDiffPair.getRight().isEmpty())) {
            try {
                annotation1Bw.write(jsonObjectWriter
                        .writeValueAsString(variantAnnotationDiffPair.getLeft().getVariantAnnotation()) + "\n");
                annotation2Bw.write(jsonObjectWriter
                        .writeValueAsString(variantAnnotationDiffPair.getRight().getVariantAnnotation()) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            // Count total number of conflicting variants
            totalDiffVariants++;
        }
        // Count total number of conflicting annotations provided by each annotator
        if (variantAnnotationDiffPair.getLeft().getSequenceOntology() != null) {
            totalDiff1SequenceOntologyTerms += variantAnnotationDiffPair.getLeft().getSequenceOntology().size();
        }
        if (variantAnnotationDiffPair.getRight().getSequenceOntology() != null) {
            totalDiff2SequenceOntologyTerms += variantAnnotationDiffPair.getRight().getSequenceOntology().size();
        }

        // Add number of total annotations provided by each annotator
        for (ConsequenceType consequenceType : variantAnnotationDiffPair.getLeft().getVariantAnnotation().getConsequenceTypes()) {
            totalAnnotations1 += consequenceType.getSequenceOntologyTerms().size();
        }
        for (ConsequenceType consequenceType : variantAnnotationDiffPair.getRight().getVariantAnnotation().getConsequenceTypes()) {
            totalAnnotations2 += consequenceType.getSequenceOntologyTerms().size();
        }
        // Count total number of processed variants
        totalVariants++;

        return true;
    }

    private void writeSequenceOntologyTerms(BufferedWriter bw, VariantAnnotationDiff variantAnnotationDiff) {
        try {
            if (variantAnnotationDiff.getSequenceOntology() != null) {
                for (SequenceOntologyTermComparisonObject comparisonObject : variantAnnotationDiff.getSequenceOntology()) {
                    StringBuilder stringBuilder = new StringBuilder(variantAnnotationDiff.getVariantAnnotation().getChromosome());
                    stringBuilder.append("\t");
                    stringBuilder.append(variantAnnotationDiff.getVariantAnnotation().getStart());
                    stringBuilder.append("\t");
                    stringBuilder.append(variantAnnotationDiff.getVariantAnnotation().getReference());
                    stringBuilder.append("\t");
                    stringBuilder.append(variantAnnotationDiff.getVariantAnnotation().getAlternate());
                    stringBuilder.append("\t");
                    stringBuilder.append(comparisonObject.getTranscriptId() != null ? comparisonObject.getTranscriptId() : "");
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

                if ((totalVariants % 3000) == 0) {
                    logger.info("{} variants checked", totalVariants);
                }
                if ((totalVariants % 10000) == 0) {
                    logger.info("Total number (%) of variants with conflicting annotation: {}/{} ({}% coincidence)",
                            totalDiffVariants,
                            totalVariants,
                            100 - totalDiffVariants * 100.0 / totalVariants);
                    logger.info("Total annotations provided by {} and not provided by {}: {}/{} ({}% coincidence)",
                            annotator1Name,
                            annotator2Name,
                            totalDiff1SequenceOntologyTerms,
                            totalAnnotations1,
                            100 - totalDiff1SequenceOntologyTerms * 100.0 / totalAnnotations1);
                    logger.info("Total annotations provided by {} and not provided by {}: {}/{} ({}% coincidence)",
                            annotator2Name,
                            annotator1Name,
                            totalDiff2SequenceOntologyTerms,
                            totalAnnotations2,
                            100 - totalDiff2SequenceOntologyTerms * 100.0 / totalAnnotations2);
                }
            }
            return true;
        } else {
            logger.warn("list of VariantAnnotationDiff is null");
        }
        return false;

    }



}
