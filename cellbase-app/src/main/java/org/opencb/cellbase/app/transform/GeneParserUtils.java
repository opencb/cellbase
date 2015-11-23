/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.transform;

import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gff.io.Gff2Reader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.MiRNAGene;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.biodata.models.variant.avro.GeneTraitAssociation;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by imedina on 12/11/15.
 */
public class GeneParserUtils {

    private static Logger logger = LoggerFactory.getLogger(GeneParserUtils.class);

    public static Map<String, SortedSet<Gff2>> getTfbsMap(Path tfbsFile) throws IOException, NoSuchMethodException, FileFormatException {
        Map<String, SortedSet<Gff2>> tfbsMap = new HashMap<>();

        if (tfbsFile != null && Files.exists(tfbsFile) && !Files.isDirectory(tfbsFile)) {
            Gff2Reader motifsFeatureReader = new Gff2Reader(tfbsFile);
            Gff2 tfbsMotifFeature;
            while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
                String chromosome = tfbsMotifFeature.getSequenceName().replaceFirst("chr", "");
                SortedSet<Gff2> chromosomeTfbsSet = tfbsMap.get(chromosome);
                if (chromosomeTfbsSet == null) {
                    chromosomeTfbsSet = new TreeSet<>((Comparator<Gff2>) (feature1, feature2) -> {
                        // TODO: maybe this should be in TranscriptTfbs class, and equals method should be overriden too
                        if (feature1.getStart() != feature2.getStart()) {
                            return feature1.getStart() - feature2.getStart();
                        } else {
                            return feature1.getAttribute().compareTo(feature2.getAttribute());
                        }
                    });
                    tfbsMap.put(chromosome, chromosomeTfbsSet);
                }
                chromosomeTfbsSet.add(tfbsMotifFeature);
            }
            motifsFeatureReader.close();
        }
        return tfbsMap;
    }

    public static Map<String, MiRNAGene> getmiRNAGeneMap(Path mirnaGeneFile) throws IOException {
        Map<String, MiRNAGene> mirnaGeneMap = new HashMap<>();

        if (mirnaGeneFile != null && Files.exists(mirnaGeneFile) && !Files.isDirectory(mirnaGeneFile)) {
            logger.info("Loading miRNA data ...");
            BufferedReader br = Files.newBufferedReader(mirnaGeneFile, Charset.defaultCharset());

            String line;
            String[] fields, mirnaMatures, mirnaMaturesFields;
            List<String> aliases;
            MiRNAGene miRNAGene;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t");

                // First, read aliases of miRNA, field #5
                aliases = new ArrayList<>();
                for (String alias : fields[5].split(",")) {
                    aliases.add(alias);
                }

                miRNAGene = new MiRNAGene(fields[1], fields[2], fields[3], fields[4], aliases, new ArrayList<>());

                // Second, read the miRNA matures, field #6
                mirnaMatures = fields[6].split(",");
                for (String s : mirnaMatures) {
                    mirnaMaturesFields = s.split("\\|");
                    int cdnaStart = fields[4].indexOf(mirnaMaturesFields[2]) + 1;
                    int cdnaEnd = cdnaStart + mirnaMaturesFields[2].length() - 1;
                    // Save directly into MiRNAGene object.
                    miRNAGene.addMiRNAMature(mirnaMaturesFields[0], mirnaMaturesFields[1], mirnaMaturesFields[2], cdnaStart, cdnaEnd);
                }

                // Add object to Map<EnsemblID, MiRNAGene>
                mirnaGeneMap.put(fields[0], miRNAGene);
            }
            br.close();
        } else {
            logger.warn("Mirna file '{}' not found", mirnaGeneFile);
            logger.warn("Mirna data not loaded");
        }
        return mirnaGeneMap;
    }


    public static Map<String, ArrayList<Xref>> getXrefMap(Path xrefsFile, Path uniprotIdMappingFile) throws IOException {
        Map<String, ArrayList<Xref>> xrefMap = new HashMap<>();
        logger.info("Loading xref data...");
        String[] fields;
        if (xrefsFile != null && Files.exists(xrefsFile)) {
            List<String> lines = Files.readAllLines(xrefsFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                if (fields.length >= 4) {
                    if (!xrefMap.containsKey(fields[0])) {
                        xrefMap.put(fields[0], new ArrayList<>());
                    }
                    xrefMap.get(fields[0]).add(new Xref(fields[1], fields[2], fields[3]));
                }
            }
        } else {
            logger.warn("Xrefs file " + xrefsFile + " not found");
            logger.warn("Xref data not loaded");
        }

        logger.info("Loading protein mapping into xref data...");
        if (uniprotIdMappingFile != null && Files.exists(uniprotIdMappingFile)) {
            BufferedReader br = FileUtils.newBufferedReader(uniprotIdMappingFile);
            String line;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t", -1);
                if (fields.length >= 19 && fields[19].startsWith("ENST")) {
                    String[] transcripts = fields[19].split("; ");
                    for (String transcript : transcripts) {
                        if (!xrefMap.containsKey(transcript)) {
                            xrefMap.put(transcript, new ArrayList<Xref>());
                        }
                        xrefMap.get(transcript).add(new Xref(fields[0], "uniprotkb_acc", "UniProtKB ACC"));
                        xrefMap.get(transcript).add(new Xref(fields[1], "uniprotkb_id", "UniProtKB ID"));
                    }
                }
            }
            br.close();
        } else {
            logger.warn("Uniprot if mapping file " + uniprotIdMappingFile + " not found");
            logger.warn("Protein mapping into xref data not loaded");
        }

        return xrefMap;
    }

    public static Map<String, List<GeneDrugInteraction>> getGeneDrugMap(Path geneDrugFile) throws IOException {
        Map<String, List<GeneDrugInteraction>> geneDrugMap = new HashMap<>();
        if (geneDrugFile != null && Files.exists(geneDrugFile)) {
            logger.info("Loading gene-drug interaction data from '{}'", geneDrugFile);
            BufferedReader br = FileUtils.newBufferedReader(geneDrugFile);

            // Skip header
            br.readLine();

            int lineCounter = 1;
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                addValueToMapElement(geneDrugMap, parts[0], new GeneDrugInteraction(parts[0], parts[4], "dgidb", parts[2], parts[3]));
                lineCounter++;
            }

            br.close();
        } else {
            logger.warn("Gene drug file " + geneDrugFile + " not found");
            logger.warn("Ignoring " + geneDrugFile);
        }

        return geneDrugMap;
    }

    public static Map<String, List<Expression>> getGeneExpressionMap(String species, Path geneExpressionFile) throws IOException {
        Map<String, List<Expression>> geneExpressionMap = new HashMap<>();

        if (geneExpressionFile != null && Files.exists(geneExpressionFile) && species != null) {
            logger.info("Loading gene expression data from '{}'", geneExpressionFile);
            BufferedReader br = FileUtils.newBufferedReader(geneExpressionFile);

            // Skip header. Column name line does not start with # so the last line read by this while will be this one
            int lineCounter = 0;
            String line;
            while (((line = br.readLine()) != null)) {  //  && (line.startsWith("#"))
                if (line.startsWith("#")) {
                    lineCounter++;
                } else {
                    break;
                }
            }

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (species.equals(parts[2])) {
                    if (parts[7].equals("UP")) {
                        addValueToMapElement(geneExpressionMap, parts[1], new Expression(parts[1], null, parts[3],
                                parts[4], parts[5], parts[6], ExpressionCall.UP, Float.valueOf(parts[8])));
                    } else if (parts[7].equals("DOWN")) {
                        addValueToMapElement(geneExpressionMap, parts[1], new Expression(parts[1], null, parts[3],
                                parts[4], parts[5], parts[6], ExpressionCall.DOWN, Float.valueOf(parts[8])));
                    } else {
                        logger.warn("Expression tags found different from UP/DOWN at line {}. Entry omitted. ", lineCounter);
                    }
                }
                lineCounter++;
            }

            br.close();
        } else {
            logger.warn("Parameters are not correct");
        }

        return geneExpressionMap;
    }

    private static <T> void addValueToMapElement(Map<String, List<T>> map, String key, T value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            List<T> expressionValueList = new ArrayList<>();
            expressionValueList.add(value);
            map.put(key, expressionValueList);
        }

    }


    public static Map<String, List<GeneTraitAssociation>> getGeneDiseaseAssociationMap(Path hpoFilePath, Path disgenetFilePath)
            throws IOException {
        Map<String, List<GeneTraitAssociation>> geneDiseaseAssociationMap = new HashMap<>(50000);

        String[] fields;
        String line;
        if (hpoFilePath != null && hpoFilePath.toFile().exists()) {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(hpoFilePath);
            // skip first header line
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                fields = line.split("\t");
                GeneTraitAssociation disease =
                        new GeneTraitAssociation(fields[0], fields[4], fields[3], 0f, 0, new ArrayList<>(), new ArrayList<>(), "hpo");
                addValueToMapElement(geneDiseaseAssociationMap, fields[1], disease);
            }
            bufferedReader.close();
        }

        if (disgenetFilePath != null && disgenetFilePath.toFile().exists()) {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(disgenetFilePath);
            // skip first header line
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                fields = line.split("\t");
                GeneTraitAssociation disease = new GeneTraitAssociation(fields[3], fields[4], "", Float.parseFloat(fields[5]),
                        Integer.parseInt(fields[6]), Arrays.asList(fields[7]), Arrays.asList(fields[8].split(", ")), "disgenet");
                addValueToMapElement(geneDiseaseAssociationMap, fields[1], disease);
            }
            bufferedReader.close();
        }

        return geneDiseaseAssociationMap;
    }

}
