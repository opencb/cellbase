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

package org.opencb.cellbase.lib.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by imedina on 12/11/15.
 */
@Deprecated
public class GeneBuilderUtils {

    private static Logger logger = LoggerFactory.getLogger(GeneBuilderUtils.class);

//    @Deprecated
//    public static Map<String, SortedSet<Gff2>> getTfbsMap(Path tfbsFile) throws IOException, NoSuchMethodException, FileFormatException {
//        Map<String, SortedSet<Gff2>> tfbsMap = new HashMap<>();
//        if (tfbsFile != null && Files.exists(tfbsFile) && !Files.isDirectory(tfbsFile) && Files.size(tfbsFile) > 0) {
//            Gff2Reader motifsFeatureReader = new Gff2Reader(tfbsFile);
//            Gff2 tfbsMotifFeature;
//            while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
//                // we only want high quality data. See issue 466
//                if (!tfbsMotifFeature.getAttribute().contains("experimental_evidence")) {
//                    continue;
//                }
//                String chromosome = tfbsMotifFeature.getSequenceName().replaceFirst("chr", "");
//                SortedSet<Gff2> chromosomeTfbsSet = tfbsMap.get(chromosome);
//                if (chromosomeTfbsSet == null) {
//                    chromosomeTfbsSet = new TreeSet<>((Comparator<Gff2>) (feature1, feature2) -> {
//                        // TODO: maybe this should be in TranscriptTfbs class, and equals method should be overriden too
//                        if (feature1.getStart() != feature2.getStart()) {
//                            return feature1.getStart() - feature2.getStart();
//                        } else {
//                            return feature1.getAttribute().compareTo(feature2.getAttribute());
//                        }
//                    });
//                    tfbsMap.put(chromosome, chromosomeTfbsSet);
//                }
//                chromosomeTfbsSet.add(tfbsMotifFeature);
//            }
//            motifsFeatureReader.close();
//        }
//        return tfbsMap;
//    }

//    public static Map<String, ArrayList<Xref>> getXrefMap(Path xrefsFile, Path uniprotIdMappingFile) throws IOException {
//        Map<String, ArrayList<Xref>> xrefMap = new HashMap<>();
//        logger.info("Loading xref data...");
//        String[] fields;
//        if (xrefsFile != null && Files.exists(xrefsFile) && Files.size(xrefsFile) > 0) {
//            List<String> lines = Files.readAllLines(xrefsFile, Charset.forName("ISO-8859-1"));
//            for (String line : lines) {
//                fields = line.split("\t", -1);
//                if (fields.length >= 4) {
//                    if (!xrefMap.containsKey(fields[0])) {
//                        xrefMap.put(fields[0], new ArrayList<>());
//                    }
//                    xrefMap.get(fields[0]).add(new Xref(fields[1], fields[2], fields[3]));
//                }
//            }
//        } else {
//            logger.warn("Xrefs file " + xrefsFile + " not found");
//            logger.warn("Xref data not loaded");
//        }
//
//        logger.info("Loading protein mapping into xref data...");
//        if (uniprotIdMappingFile != null && Files.exists(uniprotIdMappingFile) && Files.size(uniprotIdMappingFile) > 0) {
//            BufferedReader br = FileUtils.newBufferedReader(uniprotIdMappingFile);
//            String line;
//            while ((line = br.readLine()) != null) {
//                fields = line.split("\t", -1);
//                if (fields.length >= 19 && fields[19].startsWith("ENST")) {
//                    String[] transcripts = fields[19].split("; ");
//                    for (String transcript : transcripts) {
//                        if (!xrefMap.containsKey(transcript)) {
//                            xrefMap.put(transcript, new ArrayList<Xref>());
//                        }
//                        xrefMap.get(transcript).add(new Xref(fields[0], "uniprotkb_acc", "UniProtKB ACC"));
//                        xrefMap.get(transcript).add(new Xref(fields[1], "uniprotkb_id", "UniProtKB ID"));
//                    }
//                }
//            }
//            br.close();
//        } else {
//            logger.warn("Uniprot if mapping file " + uniprotIdMappingFile + " not found");
//            logger.warn("Protein mapping into xref data not loaded");
//        }
//
//        return xrefMap;
//    }

//    public static Map<String, List<GeneDrugInteraction>> getGeneDrugMap(Path geneDrugFile) throws IOException {
//        Map<String, List<GeneDrugInteraction>> geneDrugMap = new HashMap<>();
//        if (geneDrugFile != null && Files.exists(geneDrugFile) && Files.size(geneDrugFile) > 0) {
//            logger.info("Loading gene-drug interaction data from '{}'", geneDrugFile);
//            BufferedReader br = FileUtils.newBufferedReader(geneDrugFile);
//
//            // Skip header
//            br.readLine();
//
//            int lineCounter = 1;
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split("\t");
//                String geneName = parts[0];
//
//                String source = null;
//                if (parts.length >= 4) {
//                    source = parts[3];
//                }
//
//                String interactionType = null;
//                if (parts.length >= 5) {
//                    interactionType = parts[4];
//                }
//
//                String drugName = null;
//                if (parts.length >= 8) {
//                    // if drug name column is empty, use drug claim name instead
//                    drugName = StringUtils.isEmpty(parts[7]) ? parts[6] : parts[7];
//                }
//                if (StringUtils.isEmpty(drugName)) {
//                    // no drug name
//                    continue;
//                }
//
//                String chemblId = null;
//                if (parts.length >= 9) {
//                    chemblId = parts[8];
//                }
//
//                List<String> publications = new ArrayList<>();
//                if (parts.length >= 10 && parts[9] != null) {
//                    publications = Arrays.asList(parts[9].split(","));
//                }
//
//                //addValueToMapElement(geneDrugMap, geneName, new GeneDrugInteraction(geneName, drugName, source, null, interactionType));
//                // TODO update model to add new attributes
//                addValueToMapElement(geneDrugMap, geneName, new GeneDrugInteraction(geneName, drugName, source, null, null,
//                        interactionType, chemblId, publications));
//                lineCounter++;
//            }
//
//            br.close();
//        } else {
//            logger.warn("Gene drug file " + geneDrugFile + " not found");
//            logger.warn("Ignoring " + geneDrugFile);
//        }
//
//        return geneDrugMap;
//    }


//
//    public static Map<String, List<GeneTraitAssociation>> getGeneDiseaseAssociationMap(Path hpoFilePath, Path disgenetFilePath)
//            throws IOException {
//        Map<String, List<GeneTraitAssociation>> geneDiseaseAssociationMap = new HashMap<>(50000);
//
//        String line;
//        if (hpoFilePath != null && hpoFilePath.toFile().exists() && Files.size(hpoFilePath) > 0) {
//            BufferedReader bufferedReader = FileUtils.newBufferedReader(hpoFilePath);
//            // skip first header line
//            bufferedReader.readLine();
//            while ((line = bufferedReader.readLine()) != null) {
//                String[] fields = line.split("\t");
//                String omimId = fields[6];
//                String geneSymbol = fields[3];
//                String hpoId = fields[0];
//                String diseaseName = fields[1];
//                GeneTraitAssociation disease =
//                        new GeneTraitAssociation(omimId, diseaseName, hpoId, 0f, 0, new ArrayList<>(), new ArrayList<>(), "hpo");
//                addValueToMapElement(geneDiseaseAssociationMap, geneSymbol, disease);
//            }
//            bufferedReader.close();
//        }
//
//        if (disgenetFilePath != null && disgenetFilePath.toFile().exists() && Files.size(disgenetFilePath) > 0) {
//            BufferedReader bufferedReader = FileUtils.newBufferedReader(disgenetFilePath);
//            // skip first header line
//            bufferedReader.readLine();
//            while ((line = bufferedReader.readLine()) != null) {
//                String[] fields = line.split("\t");
//                String diseaseId = fields[4];
//                String diseaseName = fields[5];
//                String score = fields[9];
//                String numberOfPubmeds = fields[13].trim();
//                String numberOfSNPs = fields[14];
//                String source = fields[15];
//                GeneTraitAssociation disease = new GeneTraitAssociation(diseaseId, diseaseName, "", Float.parseFloat(score),
//                        Integer.parseInt(numberOfPubmeds), Arrays.asList(numberOfSNPs), Arrays.asList(source), "disgenet");
//                addValueToMapElement(geneDiseaseAssociationMap, fields[1], disease);
//            }
//            bufferedReader.close();
//        }
//
//        return geneDiseaseAssociationMap;
//    }
//
//    /**
//     * For a gnomad file, parse and return a map of transcript to constraints.
//     *
//     * @param gnomadFile gene annotation file path
//     * @return map of transcript to constraints
//     * @throws IOException if goa file can't be read
//     */
//    public static Map<String, List<Constraint>> getConstraints(Path gnomadFile) throws IOException {
//        Map<String, List<Constraint>> transcriptConstraints = new HashMap<>();
//
//        if (gnomadFile != null && Files.exists(gnomadFile) && Files.size(gnomadFile) > 0) {
//            logger.info("Loading OE scores from '{}'", gnomadFile);
////            BufferedReader br = FileUtils.newBufferedReader(gnomadFile);
//            InputStream inputStream = Files.newInputStream(gnomadFile);
//            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)));
//            // Skip header.
//            br.readLine();
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split("\t");
//                String transcriptIdentifier = parts[1];
//                String canonical = parts[2];
//                String oeMis = parts[5];
//                String oeSyn = parts[14];
//                String oeLof = parts[24];
//                String exacPLI = parts[70];
//                String exacLof = parts[73];
//                String geneIdentifier = parts[64];
//
//                List<Constraint> constraints = new ArrayList<>();
//                addConstraint(constraints, "oe_mis", oeMis);
//                addConstraint(constraints, "oe_syn", oeSyn);
//                addConstraint(constraints, "oe_lof", oeLof);
//                addConstraint(constraints, "exac_pLI", exacPLI);
//                addConstraint(constraints, "exac_oe_lof", exacLof);
//                transcriptConstraints.put(transcriptIdentifier, constraints);
//
//                if ("TRUE".equalsIgnoreCase(canonical)) {
//                    transcriptConstraints.put(geneIdentifier, constraints);
//                }
//            }
//            br.close();
//        }
//        return transcriptConstraints;
//    }
//
//    private static void addConstraint(List<Constraint> constraints, String name, String value) {
//        Constraint constraint = new Constraint();
//        constraint.setMethod("pLoF");
//        constraint.setSource("gnomAD");
//        constraint.setName(name);
//        try {
//            constraint.setValue(Double.parseDouble(value));
//        } catch (NumberFormatException e) {
//            // invalid number (e.g. NA), discard.
//            return;
//        }
//        constraints.add(constraint);
//    }
//
//    /**
//     * For a gene annotation file, parse and return a map of proteins to ontology annotation objects.
//     *
//     * @param goaFile gene annotation file path
//     * @return map of proteins to ontology annotation objects.
//     * @throws IOException if goa file can't be read
//     */
//    public static Map<String, List<FeatureOntologyTermAnnotation>> getOntologyAnnotations(Path goaFile) throws IOException {
//        Map<String, List<FeatureOntologyTermAnnotation>> annotations = new HashMap<>();
//        if (goaFile != null && Files.exists(goaFile) && Files.size(goaFile) > 0) {
//            logger.info("Loading GO annotation from '{}'", goaFile);
//            BufferedReader br = FileUtils.newBufferedReader(goaFile);
//            GafParser parser = new GafParser();
//            annotations = parser.parseGaf(br);
//        }
//        return annotations;
//    }
}
