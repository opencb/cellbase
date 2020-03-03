/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.app.cli.main.annotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private BufferedWriter nonRegulatorydiff1Bw;
    private BufferedWriter regulatorydiff1Bw;
    private BufferedWriter nonRegulatorydiff2Bw;
    private BufferedWriter regulatorydiff2Bw;
    private BufferedWriter annotation1Bw;
    private BufferedWriter annotation2Bw;
    private ObjectWriter jsonObjectWriter;

    private Map<String, Integer> overallSummaryStats;
    private Map<String, Integer> annotator1SummaryStats;
    private Map<String, Integer> annotator2SummaryStats;
    private static final String TOTAL_VARIANTS = "totalVariants";
    private static final String TOTAL_NON_REGULATORY_ANNOTATIONS = "totalNonRegulatoryAnnotations";
    private static final String TOTAL_REGULATORY_ANNOTATIONS = "totalRegulatoryAnnotations";
    private static final String TOTAL_ANNOTATIONS = "totalAnnotations";
    private static final String TOTAL_NON_REGULATORY_DIFF_VARIANTS = "totalNonRegulatoryDiffVariants";
    private static final String TOTAL_REGULATORY_DIFF_VARIANTS = "totalRegulatoryDiffVariants";
    private static final String TOTAL_DIFF_VARIANTS = "totalDiffVariants";
    private static final String TOTAL_DIFF_NON_REGULATORY_SO_TERMS = "totalDiffNonRegulatorySOTerms";
    private static final String TOTAL_DIFF_REGULATORY_SO_TERMS = "totalDiffRegulatorySOTerms";
    private static final String TOTAL_DIFF_SO_TERMS = "totalDiffSOTerms";

    private Map<String, Integer> countsBySOTerm1;
    private Map<String, Integer> countsBySOTerm2;

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

        // To store overall stats
        overallSummaryStats = new HashMap<>();
        overallSummaryStats.put(TOTAL_VARIANTS, 0);
        overallSummaryStats.put(TOTAL_NON_REGULATORY_DIFF_VARIANTS, 0);
        overallSummaryStats.put(TOTAL_REGULATORY_DIFF_VARIANTS, 0);
        overallSummaryStats.put(TOTAL_DIFF_VARIANTS, 0);

        // To store annotator 1 stats
        annotator1SummaryStats = new HashMap<>();
        annotator1SummaryStats.put(TOTAL_NON_REGULATORY_ANNOTATIONS, 0);
        annotator1SummaryStats.put(TOTAL_REGULATORY_ANNOTATIONS, 0);
        annotator1SummaryStats.put(TOTAL_ANNOTATIONS, 0);
        annotator1SummaryStats.put(TOTAL_DIFF_NON_REGULATORY_SO_TERMS, 0);
        annotator1SummaryStats.put(TOTAL_DIFF_REGULATORY_SO_TERMS, 0);
        annotator1SummaryStats.put(TOTAL_DIFF_SO_TERMS, 0);

        // To store annotator 2 stats
        annotator2SummaryStats = new HashMap<>();
        annotator2SummaryStats.put(TOTAL_NON_REGULATORY_ANNOTATIONS, 0);
        annotator2SummaryStats.put(TOTAL_REGULATORY_ANNOTATIONS, 0);
        annotator2SummaryStats.put(TOTAL_ANNOTATIONS, 0);
        annotator2SummaryStats.put(TOTAL_DIFF_NON_REGULATORY_SO_TERMS, 0);
        annotator2SummaryStats.put(TOTAL_DIFF_REGULATORY_SO_TERMS, 0);
        annotator2SummaryStats.put(TOTAL_DIFF_SO_TERMS, 0);

        // Counts breakdown by SO term
        countsBySOTerm1 = new HashMap<>();
        countsBySOTerm2 = new HashMap<>();
    }

    @Override
    public boolean open() {
        try {
            // Files that will contain differences on SO terms (annotator 1)
            OutputStream os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_non_regulatory_"
                    + annotator1Name + ".tsv.gz"), CREATE, APPEND));
            nonRegulatorydiff1Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_regulatory_" + annotator1Name
                    + ".tsv.gz"), CREATE, APPEND));
            regulatorydiff1Bw = new BufferedWriter(new OutputStreamWriter(os));

            // Files that will contain differences on SO terms (annotator 2)
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_non_regulatory_" + annotator2Name
                    + ".tsv.gz"), CREATE, APPEND));
            nonRegulatorydiff2Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("diff_regulatory_" + annotator2Name
                    + ".tsv.gz"), CREATE, APPEND));
            regulatorydiff2Bw = new BufferedWriter(new OutputStreamWriter(os));

            // Files that will contain whole variant objects for those variants with conflicting annotation
            os = new GZIPOutputStream(Files.newOutputStream(outdir.resolve("annotation_" + annotator1Name + ".json.gz"),
                    CREATE, APPEND));
            annotation1Bw = new BufferedWriter(new OutputStreamWriter(os));
            os = Files.newOutputStream(outdir.resolve("annotation_" + annotator2Name + ".json"), CREATE, APPEND);
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
            nonRegulatorydiff1Bw.close();
            regulatorydiff1Bw.close();
            nonRegulatorydiff2Bw.close();
            regulatorydiff2Bw.close();
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
           bw.write("Total number of checked variants\t" + overallSummaryStats.get(TOTAL_VARIANTS) + "\n");
           bw.write("Total number of annotations provided by " + annotator1Name + "\t"
                   + annotator1SummaryStats.get(TOTAL_ANNOTATIONS) + "\n");
           bw.write("Total number of annotations provided by " + annotator2Name + "\t"
                   + annotator2SummaryStats.get(TOTAL_ANNOTATIONS) + "\n");
           bw.write("Total number of non-regulatory annotations provided by " + annotator1Name + "\t"
                   + annotator1SummaryStats.get(TOTAL_NON_REGULATORY_ANNOTATIONS) + "\n");
           bw.write("Total number of non-regulatory annotations provided by " + annotator2Name + "\t"
                   + annotator2SummaryStats.get(TOTAL_NON_REGULATORY_ANNOTATIONS) + "\n");
           bw.write("Total number of regulatory annotations provided by " + annotator1Name + "\t"
                   + annotator1SummaryStats.get(TOTAL_REGULATORY_ANNOTATIONS) + "\n");
           bw.write("Total number of regulatory annotations provided by " + annotator2Name + "\t"
                   + annotator2SummaryStats.get(TOTAL_REGULATORY_ANNOTATIONS) + "\n\n");

           // Coincidence variant level
           bw.write("################# Coincidence at variant level #################\t\n");
           bw.write("Total number of variants with conflicting annotation\t"
                   + overallSummaryStats.get(TOTAL_DIFF_VARIANTS) + "/" + overallSummaryStats.get(TOTAL_VARIANTS) + "\t"
                   + (100 - overallSummaryStats.get(TOTAL_DIFF_VARIANTS) * 100.0 / overallSummaryStats.get(TOTAL_VARIANTS))
                   + "% coincidence\n");
           bw.write("Total number of variants with conflicting non-regulatory annotation\t"
                   + overallSummaryStats.get(TOTAL_NON_REGULATORY_DIFF_VARIANTS) + "/" + overallSummaryStats.get(TOTAL_VARIANTS) + "\t"
                   + (100 - overallSummaryStats.get(TOTAL_NON_REGULATORY_DIFF_VARIANTS) * 100.0 / overallSummaryStats.get(TOTAL_VARIANTS))
                   + "% coincidence\n");
           bw.write("Total number of variants with conflicting regulatory annotation\t"
                   + overallSummaryStats.get(TOTAL_REGULATORY_DIFF_VARIANTS) + "/" + overallSummaryStats.get(TOTAL_VARIANTS) + "\t"
                   + (100 - overallSummaryStats.get(TOTAL_REGULATORY_DIFF_VARIANTS) * 100.0 / overallSummaryStats.get(TOTAL_VARIANTS))
                   + "% coincidence\n\n");

           // Coincidence annotation level (annotator 1)
           bw.write("################# Coincidence at SO term level - " + annotator1Name + " annotations #################\t\n");
           bw.write("Total annotations provided by " + annotator1Name + " and not provided by "
                   + annotator2Name + "\t" + annotator1SummaryStats.get(TOTAL_DIFF_SO_TERMS) + "/"
                   + annotator1SummaryStats.get(TOTAL_ANNOTATIONS) + "\t"
                   + (100 - annotator1SummaryStats.get(TOTAL_DIFF_SO_TERMS) * 100.0
                   / annotator1SummaryStats.get(TOTAL_ANNOTATIONS)) + "% coincidence\n");
           bw.write("Total non-regulatory annotations provided by " + annotator1Name + " and not provided by "
                   + annotator2Name + "\t" + annotator1SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS) + "/"
                   + annotator1SummaryStats.get(TOTAL_NON_REGULATORY_ANNOTATIONS) + "\t"
                   + (100 - annotator1SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS) * 100.0
                   / annotator1SummaryStats.get(TOTAL_NON_REGULATORY_ANNOTATIONS)) + "% coincidence\n");
           bw.write("Total regulatory annotations provided by " + annotator1Name + " and not provided by "
                   + annotator2Name + "\t" + annotator1SummaryStats.get(TOTAL_DIFF_REGULATORY_SO_TERMS) + "/"
                   + annotator1SummaryStats.get(TOTAL_REGULATORY_ANNOTATIONS) + "\t"
                   + (100 - annotator1SummaryStats.get(TOTAL_DIFF_REGULATORY_SO_TERMS) * 100.0
                   / annotator1SummaryStats.get(TOTAL_REGULATORY_ANNOTATIONS)) + "% coincidence\n\n");

           // Coincidence annotation level (annotator 2)
           bw.write("################# Coincidence at SO term level - " + annotator2Name + " annotations #################\t\n");
           bw.write("Total annotations provided by " + annotator2Name + " and not provided by "
                   + annotator1Name + "\t" + annotator2SummaryStats.get(TOTAL_DIFF_SO_TERMS) + "/"
                   + annotator2SummaryStats.get(TOTAL_ANNOTATIONS) + "\t"
                   + (100 - annotator2SummaryStats.get(TOTAL_DIFF_SO_TERMS) * 100.0
                   / annotator2SummaryStats.get(TOTAL_ANNOTATIONS)) + "% coincidence\n");
           bw.write("Total non-regulatory annotations provided by " + annotator2Name + " and not provided by "
                   + annotator1Name + "\t" + annotator2SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS) + "/"
                   + annotator2SummaryStats.get(TOTAL_NON_REGULATORY_ANNOTATIONS) + "\t"
                   + (100 - annotator2SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS) * 100.0
                   / annotator2SummaryStats.get(TOTAL_NON_REGULATORY_ANNOTATIONS)) + "% coincidence\n");
           bw.write("Total regulatory annotations provided by " + annotator2Name + " and not provided by "
                   + annotator1Name + "\t" + annotator2SummaryStats.get(TOTAL_DIFF_REGULATORY_SO_TERMS) + "/"
                   + annotator2SummaryStats.get(TOTAL_REGULATORY_ANNOTATIONS) + "\t"
                   + (100 - annotator2SummaryStats.get(TOTAL_DIFF_REGULATORY_SO_TERMS) * 100.0
                   / annotator2SummaryStats.get(TOTAL_REGULATORY_ANNOTATIONS)) + "% coincidence\n");
           bw.close();

           writeCountsBySOTerm(countsBySOTerm1, "diff_counts_by_SO_term_" + annotator1Name + ".tsv");
           writeCountsBySOTerm(countsBySOTerm2, "diff_counts_by_SO_term_" + annotator2Name + ".tsv");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private void writeCountsBySOTerm(Map<String, Integer> countsBySOTerm, String fileName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outdir.resolve(fileName))));
        List<String> sortedSOTerms = countsBySOTerm.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // custom Comparator
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        for (String soTerm : sortedSOTerms) {
            bw.write(soTerm + "\t" + countsBySOTerm.get(soTerm) + "\n");
        }
        bw.close();
    }

    @Override
    public boolean write(Pair<VariantAnnotationDiff, VariantAnnotationDiff> variantAnnotationDiffPair) {
        // Write differing sequence ontology terms
        writeSequenceOntologyTerms(nonRegulatorydiff1Bw, regulatorydiff1Bw, variantAnnotationDiffPair.getLeft());
        writeSequenceOntologyTerms(nonRegulatorydiff2Bw, regulatorydiff2Bw, variantAnnotationDiffPair.getRight());
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
        }

        updateSummaryStats(variantAnnotationDiffPair);

        return true;
    }

    private void updateSummaryStats(Pair<VariantAnnotationDiff, VariantAnnotationDiff> variantAnnotationDiffPair) {

        // Count total number of conflicting variants
        if (!(variantAnnotationDiffPair.getLeft().isEmpty() && variantAnnotationDiffPair.getRight().isEmpty())) {
            increment(overallSummaryStats, TOTAL_DIFF_VARIANTS, 1);
        }

        // Update annotator-dependent counts - some overall stats will be updated as well
        updateAnnotatorSummaryStats(variantAnnotationDiffPair.getLeft(), annotator1SummaryStats, countsBySOTerm1);
        updateAnnotatorSummaryStats(variantAnnotationDiffPair.getRight(), annotator2SummaryStats, countsBySOTerm2);

        // Count total number of processed variants
        increment(overallSummaryStats, TOTAL_VARIANTS, 1);

        int sum = 0;
        for (String soName : countsBySOTerm2.keySet()) {
            if (!isRegulatory(soName)) {
                sum += countsBySOTerm2.get(soName);
            }
        }
        if (!annotator2SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS).equals(sum)) {
            int a = 1;
        }
    }

    private void updateAnnotatorSummaryStats(VariantAnnotationDiff variantAnnotationDiff,
                                             Map<String, Integer> summaryStats, Map<String, Integer> countsBySOTerm) {

        boolean regulatoryDifferenceFound = false;
        boolean nonRegulatoryDifferenceFound = false;

        // Count number of conflicting annotations provided by each annotator
        if (variantAnnotationDiff.getSequenceOntology() != null) {
            increment(summaryStats, TOTAL_DIFF_SO_TERMS, variantAnnotationDiff.getSequenceOntology().size());
            for (SequenceOntologyTermComparisonObject comparisonObject : variantAnnotationDiff.getSequenceOntology()) {
                if (isRegulatory(comparisonObject.getName())) {
                    increment(summaryStats, TOTAL_DIFF_REGULATORY_SO_TERMS, 1);
                    regulatoryDifferenceFound = true;
                } else {
                    increment(summaryStats, TOTAL_DIFF_NON_REGULATORY_SO_TERMS, 1);
                    nonRegulatoryDifferenceFound = true;
                }
                if (countsBySOTerm.containsKey(comparisonObject.getName())) {
                    increment(countsBySOTerm, comparisonObject.getName(), 1);
                } else {
                    countsBySOTerm.put(comparisonObject.getName(), 1);
                }
            }
        }

        // Go through all annotations and update the counts
        for (ConsequenceType consequenceType : variantAnnotationDiff.getVariantAnnotation().getConsequenceTypes()) {
            for (SequenceOntologyTerm sequenceOntologyTerm : consequenceType.getSequenceOntologyTerms()) {
                if (isRegulatory(sequenceOntologyTerm.getName())) {
                    increment(summaryStats, TOTAL_REGULATORY_ANNOTATIONS, 1);
                } else {
                    increment(summaryStats, TOTAL_NON_REGULATORY_ANNOTATIONS, 1);
                }
            }
            increment(summaryStats, TOTAL_ANNOTATIONS, consequenceType.getSequenceOntologyTerms().size());
        }

        if (regulatoryDifferenceFound) {
            increment(overallSummaryStats, TOTAL_REGULATORY_DIFF_VARIANTS, 1);
        }
        if (nonRegulatoryDifferenceFound) {
            increment(overallSummaryStats, TOTAL_NON_REGULATORY_DIFF_VARIANTS, 1);
        }
    }

    private void increment(Map<String, Integer> map, String key, Integer incrementValue) {
        map.put(key, map.get(key) + incrementValue);
    }

    private boolean isRegulatory(String soName) {
        return (soName.equals(VariantAnnotationUtils.REGULATORY_REGION_VARIANT)
                || soName.equals(VariantAnnotationUtils.TF_BINDING_SITE_VARIANT));
    }

    private void writeSequenceOntologyTerms(BufferedWriter nonRegulatoryBw, BufferedWriter regulatoryBw,
                                            VariantAnnotationDiff variantAnnotationDiff) {
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

                    // Expected many differences depending on the regulatory source databases used by the annotators.
                    // Better separate regulatory_region_variant annotations
                    if (isRegulatory(comparisonObject.getName())) {
                        regulatoryBw.write(stringBuilder.toString());
                    } else {
                        nonRegulatoryBw.write(stringBuilder.toString());
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            try {
                logger.error("Variant failing: {}\n", jsonObjectWriter
                        .writeValueAsString(variantAnnotationDiff.getVariantAnnotation()));
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @Override
    public boolean write(List<Pair<VariantAnnotationDiff, VariantAnnotationDiff>> list) {
        if (list != null) {
            for (Pair<VariantAnnotationDiff, VariantAnnotationDiff> variantAnnotationDiffPair : list) {
                write(variantAnnotationDiffPair);

                if ((overallSummaryStats.get(TOTAL_VARIANTS) % 3000) == 0) {
                    logger.info("{} variants checked", overallSummaryStats.get(TOTAL_VARIANTS));
                }
                if ((overallSummaryStats.get(TOTAL_VARIANTS) % 10000) == 0) {
                    logger.info("Total number (%) of variants with conflicting annotation: {}/{} ({}% coincidence)",
                            overallSummaryStats.get(TOTAL_DIFF_VARIANTS),
                            overallSummaryStats.get(TOTAL_VARIANTS),
                            100 - overallSummaryStats.get(TOTAL_DIFF_VARIANTS) * 100.0 / overallSummaryStats.get(TOTAL_VARIANTS));
                    logger.info("Total number (%) of variants with conflicting non-regulatory annotation: {}/{} ({}% coincidence)",
                            overallSummaryStats.get(TOTAL_NON_REGULATORY_DIFF_VARIANTS),
                            overallSummaryStats.get(TOTAL_VARIANTS),
                            100 - overallSummaryStats.get(TOTAL_NON_REGULATORY_DIFF_VARIANTS) * 100.0
                                    / overallSummaryStats.get(TOTAL_VARIANTS));
                    logger.info("Total number (%) of variants with conflicting regulatory annotation: {}/{} ({}% coincidence)",
                            overallSummaryStats.get(TOTAL_REGULATORY_DIFF_VARIANTS),
                            overallSummaryStats.get(TOTAL_VARIANTS),
                            100 - overallSummaryStats.get(TOTAL_REGULATORY_DIFF_VARIANTS) * 100.0
                                    / overallSummaryStats.get(TOTAL_VARIANTS));
                    logger.info("Total non-regulatory annotations provided by {} and not provided by {}: {}/{} ({}% coincidence)",
                            annotator1Name,
                            annotator2Name,
                            annotator1SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS),
                            annotator1SummaryStats.get(TOTAL_ANNOTATIONS),
                            100 - annotator1SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS) * 100.0
                                    / annotator1SummaryStats.get(TOTAL_ANNOTATIONS));
                    logger.info("Total non-regulatory annotations provided by {} and not provided by {}: {}/{} ({}% coincidence)",
                            annotator2Name,
                            annotator1Name,
                            annotator2SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS),
                            annotator2SummaryStats.get(TOTAL_ANNOTATIONS),
                            100 - annotator2SummaryStats.get(TOTAL_DIFF_NON_REGULATORY_SO_TERMS) * 100.0
                                    / annotator2SummaryStats.get(TOTAL_ANNOTATIONS));
                }
            }
            return true;
        } else {
            logger.warn("list of VariantAnnotationDiff is null");
        }
        return false;

    }



}
