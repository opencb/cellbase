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

package org.opencb.cellbase.lib.builders.clinical.variant;

import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;

public class GwasIndexer extends ClinicalIndexer {

    private static final int REF = 0;
    private static final int ALT = 1;

    private final Path gwasFile;
    private final Path dbSnpTabixFile;
    private final String assembly;

    private int invalidStartRecords;
    private int invalidChromosome;
    private int gwasLinesNotFoundInDbsnp;
    private int invalidVariantRecords;

    public GwasIndexer(Path gwasFile, Path dbSnpTabixFile, Path genomeSequenceFilePath, String assembly, RocksDB rdb) throws IOException {
        super(genomeSequenceFilePath);

        this.gwasFile = gwasFile;
        this.dbSnpTabixFile = dbSnpTabixFile;
        this.assembly = assembly;
        this.rdb = rdb;
    }

    public void index() throws RocksDBException, IOException {
        logger.info("Parsing GWAS catalog file ...");

        BufferedReader inputReader = null;
        TabixReader dbsnpTabixReader = null;

        try {
            logger.info("Opening GWAS catalog file " + gwasFile + " ...");
            inputReader = new BufferedReader(new FileReader(gwasFile.toFile()));

            logger.info("Ignoring GWAS catalog file header line ...");
            String line = inputReader.readLine();

            Map<String, GwasAssociation> gwasMap = new HashMap<>();
            logger.info("Opening dbSNP tabix file " + dbSnpTabixFile + " ...");
            dbsnpTabixReader = new TabixReader(dbSnpTabixFile.toString());

            long processedGwasLines = 0;

            logger.info("Parsing GWAS catalog file ...");
            while ((line = inputReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    processedGwasLines++;
                    if (processedGwasLines % 10000 == 0) {
                        logger.info("{} lines parsed", processedGwasLines);
                    }

                    processGwasCatalogLine(line.split("\t"), dbsnpTabixReader, gwasMap);
                }
            }
            dbsnpTabixReader.close();

            logger.info("Updating clinical variant annotation...");
            long counter = 0;
            for (Map.Entry<String, GwasAssociation> entry : gwasMap.entrySet()) {
                VariantAnnotation variantAnnotation = getVariantAnnotation(entry.getKey().getBytes());

                counter++;
                if (counter % 10000 == 0) {
                    logger.info("{} variants updated", counter);
                }

                // Check Xrefs and add dbSNP ID if necessary
                boolean found = false;
                List<Xref> xrefs= new ArrayList<>();
                if (CollectionUtils.isNotEmpty(variantAnnotation.getXrefs())) {
                    xrefs = variantAnnotation.getXrefs();
                    for (Xref xref : xrefs) {
                        if (entry.getValue().getSnpId().equals(xref.getId())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    xrefs.add(new Xref(entry.getValue().getSnpId(), "dbSNP"));
                    variantAnnotation.setXrefs(xrefs);
                }

                variantAnnotation.setGwas(entry.getValue());
                rdb.put(entry.getKey().getBytes(), jsonObjectWriter.writeValueAsBytes(variantAnnotation));
            }
            this.printSummary(processedGwasLines, gwasMap);
        } catch (RocksDBException | IOException  e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing GWAS catalog file");
            throw e;
        } finally {
            if (inputReader != null) {
                inputReader.close();
            }
            if (dbsnpTabixReader != null) {
                dbsnpTabixReader.close();
            }
        }
    }

    /*
    0 DATE ADDED TO CATALOG* +: Date a study is published in the catalog
    1 PUBMEDID* +: PubMed identification number
    2 FIRST AUTHOR* +: Last name and initials of first author
    3 DATE* +: Publication date (online (epub) date if available)
    4 JOURNAL* +: Abbreviated journal name
    5 LINK* +: PubMed URL
    6 STUDY* +: Title of paper
    7 DISEASE/TRAIT* +: Disease or trait examined in study
    8 INITIAL SAMPLE DESCRIPTION* +: Sample size and ancestry description for stage 1 of GWAS (summing across multiple Stage 1 populations,
      if applicable)
    9 REPLICATION SAMPLE DESCRIPTION* +: Sample size and ancestry description for subsequent replication(s) (summing across multiple
      populations, if applicable)
    10 REGION*: Cytogenetic region associated with rs number
    11 CHR_ID*: Chromosome number associated with rs number
    12 CHR_POS*: Chromosomal position associated with rs number
    13 REPORTED GENE(S)*: Gene(s) reported by author
    14 MAPPED GENE(S)*: Gene(s) mapped to the strongest SNP. If the SNP is located within a gene, that gene is listed. If the SNP is located
       within multiple genes, these genes are listed separated by commas. If the SNP is intergenic, the upstream and downstream genes are
       listed, separated by a hyphen.
    15 UPSTREAM_GENE_ID*: Entrez Gene ID for nearest upstream gene to rs number, if not within gene
    16 DOWNSTREAM_GENE_ID*: Entrez Gene ID for nearest downstream gene to rs number, if not within gene
    17 SNP_GENE_IDS*: Entrez Gene ID, if rs number within gene; multiple genes denotes overlapping transcripts
    18 UPSTREAM_GENE_DISTANCE*: distance in kb for nearest upstream gene to rs number, if not within gene
    19 DOWNSTREAM_GENE_DISTANCE*: distance in kb for nearest downstream gene to rs number, if not within gene
    20 STRONGEST SNP-RISK ALLELE*: SNP(s) most strongly associated with trait + risk allele (? for unknown risk allele). May also refer
       to a haplotype.
    21 SNPS*: Strongest SNP; if a haplotype it may include more than one rs number (multiple SNPs comprising the haplotype)
    22 MERGED*: denotes whether the SNP has been merged into a subsequent rs record (0 = no; 1 = yes;)
    23 SNP_ID_CURRENT*: current rs number (will differ from strongest SNP when merged = 1)
    24 CONTEXT*: SNP functional class
    25 INTERGENIC*: denotes whether SNP is in intergenic region (0 = no; 1 = yes)
    26 RISK ALLELE FREQUENCY*: Reported risk/effect allele frequency associated with strongest SNP in controls (if not available among all
       controls, among the control group with the largest sample size). If the associated locus is a haplotype the haplotype frequency will
       be extracted.
    27 P-VALUE*: Reported p-value for strongest SNP risk allele (linked to dbGaP Association Browser). Note that p-values are rounded to 1
       significant digit (for example, a published p-value of 4.8 x 10-7 is rounded to 5 x 10-7).
    28 PVALUE_MLOG*: -log(p-value)
    29 P-VALUE (TEXT)*: Information describing context of p-value (e.g. females, smokers).
    30 OR or BETA*: Reported odds ratio or beta-coefficient associated with strongest SNP risk allele. Note that if an OR <1 is reported
       this is inverted, along with the reported allele, so that all ORs included in the Catalog are >1. Appropriate unit and
       increase/decrease are included for beta coefficients.
    31 95% CI (TEXT)*: Reported 95% confidence interval associated with strongest SNP risk allele, along with unit in the case of
       beta-coefficients. If 95% CIs are not published, we estimate these using the standard error, where available.
    32 PLATFORM (SNPS PASSING QC)*: Genotyping platform manufacturer used in Stage 1; also includes notation of pooled DNA study design or
       imputation of SNPs, where applicable
    33 CNV*: Study of copy number variation (yes/no)
    34 MAPPED_TRAIT* +: Mapped Experimental Factor Ontology trait for this study
    35 MAPPED_TRAIT_URI* +: URI of the EFO trait
    36 STUDY ACCESSION* +: Accession ID allocated to a GWAS Catalog study
    37 GENOTYPING_TECHNOLOGY* +: Genotyping technology/ies used in this study, with additional array information (ex. Immunochip or Exome
       array) in brackets.
*/
    private void processGwasCatalogLine(String[] values, TabixReader dbsnpTabixReader, Map<String, GwasAssociation> gwasMap) {
        Integer start = parseStart(values);
        if (start != null) {
            String chromosome = parseChromosome(values[11]);
            if (StringUtils.isNotEmpty(chromosome)) {
                String snpId = "rs" + values[23].trim();
                String[] refAndAlt = getRefAndAltFromDbsnp(chromosome, start, snpId, dbsnpTabixReader);
                if (refAndAlt != null) {
                    // Create variant
                    Variant variant;
                    try {
                        variant = new Variant(chromosome, start, refAndAlt[0], refAndAlt[1]);
                    } catch (Exception e) {
                        // Do nothing
                        return;
                    }

                    // Create GWAS association
                    GwasAssociation gwas = new GwasAssociation();
                    gwas.setSource("EBI GWAS catalog");
                    gwas.setRegion(values[10]);
                    gwas.setSnpId(snpId);
                    try {
                        if (StringUtils.isNotEmpty(values[20])) {
                            String[] snps = values[20].split(",");
                            if (snps[0].contains("-")) {
                                gwas.setRiskAllele(snps[0].split("-")[1]);
                            }
                        }
                    } catch (Exception e) {
                        // Do nothing
                        invalidVariantRecords++;
                        return;
                    }
                    gwas.setSnpId(snpId);

                    try {
                        gwas.setRiskAlleleFrequency(Double.parseDouble(values[26]));
                    } catch (NumberFormatException e) {
//                        logger.warn(e.getMessage() + ". Parsing risk allele frequnency: " + values[26]);
                    }
                    gwas.setStudies(new ArrayList<>());

                    // Study management
                    GwasAssociationStudy study = new GwasAssociationStudy();
                    study.setPubmedid(values[1]);
                    study.setStudy(values[6]);
                    study.setStudyAccession(values[36]);
                    study.setInitialSampleSizeDescription(values[8]);
//                    study.setInitialSampleSize(values[8]);
                    study.setPlatform(values[32]);
                    study.setGenotypingTechnology(values[37]);
                    study.setTraits(new ArrayList<>());

                    // Trait management
                    GwasAssociationStudyTrait trait = new GwasAssociationStudyTrait();
                    trait.setDiseaseTrait(values[7]);
                    if (StringUtils.isNotEmpty(values[17])) {
                        trait.setStrongestSnpRiskAllele(new ArrayList<>(Arrays.asList(values[17].split(","))));
                    }
                    try {
                        if (StringUtils.isNotEmpty(values[35])) {
                            List<OntologyTermAnnotation> ontologies = new ArrayList<>();
                            String[] names = values[34].split(",");
                            String[] terms = values[35].split(",");
                            int i = 0;
                            for (String term : terms) {
                                if (term.contains("EFO_")) {
                                    OntologyTermAnnotation ontology = new OntologyTermAnnotation();
                                    ontology.setId("EFO:" + term.split("EFO_")[1]);
                                    ontology.setName(names[i]);
                                    ontology.setUrl(term);
                                    ontology.setSource("Experimental Factor Ontology");
                                    ontologies.add(ontology);
                                }
                                i++;
                            }
                            trait.setOntologies(ontologies);
                        }
                    } catch (Exception e) {
                        // Do nothing
                    }
                    trait.setScores(new ArrayList<>());

                    // Scores management
                    GwasAssociationStudyTraitScores scores = new GwasAssociationStudyTraitScores();
                    try {
                        scores.setPValue(Double.parseDouble(values[27]));
                    } catch (NumberFormatException e) {
//                        logger.warn(e.getMessage() + ". Parsing pValue: " + values[27]);
                    }
                    try {
                        scores.setPValueMlog(Double.parseDouble(values[28]));
                    } catch (NumberFormatException e) {
//                        logger.warn(e.getMessage() + ". Parsing pValue mlog: " + values[28]);
                    }
                    scores.setPValueText(values[29]);
                    try {
                        scores.setOrBeta(Double.parseDouble(values[30]));
                    } catch (NumberFormatException e) {
//                        logger.warn(e.getMessage() + ". Parsing Odd or beta: " + values[30]);
                    }
                    scores.setPercentCI(values[31]);

                    // List management
                    trait.getScores().add(scores);
                    study.getTraits().add(trait);
                    gwas.getStudies().add(study);

                    // Check variant map and update if necessary
                    String key = variant.toStringSimple();
                    if (gwasMap.containsKey(key)) {
                        checkAndAddGwasAssociation(key, gwas, gwasMap);
                    } else {
                        gwasMap.put(key, gwas);
                    }
                } else {
//                    logger.warn("Variant not found in dbSNP " + snpId + ". Line: " + StringUtils.join(values, "\t\t\t"));
                    gwasLinesNotFoundInDbsnp++;
                }
            } else {
//                logger.warn("Invalid chromosome " + chromosome + ". Line: " + StringUtils.join(values, "\t\t\t"));
                invalidChromosome++;
            }
        } else {
//            logger.warn("Invalid position " + start + ". Line: " + StringUtils.join(values, "\t\t\t"));
            invalidStartRecords++;
        }
    }

    private Integer parseStart(String[] values) {
        Integer start = null;
        if (NumberUtils.isDigits(values[12])) {
            start = Integer.parseInt(values[12]);
        }
        return start;
    }

    private String parseChromosome(String chromosome) {
        String transformedChromosome = null;
        if (!chromosome.isEmpty()) {
            switch (chromosome) {
                case "23":
                    transformedChromosome = "X";
                    break;
                case "24":
                    transformedChromosome = "Y";
                    break;
                case "25":
                    transformedChromosome = "MT";
                    break;
                default:
                    transformedChromosome = chromosome;
            }
        }
        return transformedChromosome;
    }

    private Float parseFloat(String value) {
        Float riskAlleleFrequency = null;
        if (NumberUtils.isNumber(value)) {
            riskAlleleFrequency = Float.parseFloat(value);
        }
        return riskAlleleFrequency;
    }

    private String[] getRefAndAltFromDbsnp(String chromosome, Integer start, String snpId, TabixReader dbsnpTabixReader) {
        String[] refAndAlt = null;

        TabixReader.Iterator dbsnpIterator = dbsnpTabixReader.query(chromosome + ":" + start + "-" + start);
        try {
            String dbSnpRecord = dbsnpIterator.next();
            boolean found = false;
            while (dbSnpRecord != null && !found) {
                String[] dbsnpFields = dbSnpRecord.split("\t");

                if (snpId.equalsIgnoreCase(dbsnpFields[2])) {
                    refAndAlt = new String[2];
                    refAndAlt[REF] = dbsnpFields[3];
                    refAndAlt[ALT] = dbsnpFields[4];
                    found = true;
                }

                dbSnpRecord = dbsnpIterator.next();
            }
        } catch (IOException e) {
            logger.warn("Error reading position '" + chromosome + ":" + start + "' in dbSNP: " + e.getMessage());
        }

        return refAndAlt;
    }

    private void printSummary(long processedGwasLines, Map<String, GwasAssociation> gwasMap) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(processedGwasLines) + " gwas lines");
        logger.info("Serialized " + formatter.format(gwasMap.size()) + " variants");
        logger.info(formatter.format(gwasLinesNotFoundInDbsnp) + " gwas lines ignored because variant not found in dbsnp");
        if (invalidStartRecords != 0) {
            logger.info(formatter.format(invalidStartRecords) + " gwas lines ignored because have no valid 'position' value");
        }
        if (invalidChromosome != 0) {
            logger.info(formatter.format(invalidChromosome) + " gwas lines ignored because have no valid chromosome");
        }
        if (invalidVariantRecords != 0) {
            logger.info(formatter.format(invalidVariantRecords) + " gwas lines ignored because can not create variant");
        }
    }

    private void checkAndAddGwasAssociation(String key, GwasAssociation newGwas, Map<String, GwasAssociation> gwasMap) {
        GwasAssociation currGwas = gwasMap.get(key);
        GwasAssociationStudy currStudy = null;
        GwasAssociationStudy newStudy = newGwas.getStudies().get(0);
        for (GwasAssociationStudy study : currGwas.getStudies()) {
            if (study.getPubmedid().equals(newStudy.getPubmedid())) {
                currStudy = study;
                break;
            }
        }
        if (currStudy == null) {
            // New study, we must add to the current GWAS (the new GWAS has only one study)
            currGwas.getStudies().add(newStudy);
        } else {
            // The current GWAS has already the same study
            // We must check traits for that study
            GwasAssociationStudyTrait currTrait = null;
            GwasAssociationStudyTrait newTrait = newStudy.getTraits().get(0);
            for (GwasAssociationStudyTrait trait : currStudy.getTraits()) {
                if (trait.getDiseaseTrait().equals(newTrait.getDiseaseTrait())) {
                    currTrait = trait;
                    break;
                }
            }
            if (currTrait == null) {
                // New trait, we must add to the current GWAS (the new GWAS has only one trait)
                currStudy.getTraits().add(newTrait);
            } else {
                // The current study has already the same trait
                // We must check traits for that study
                GwasAssociationStudyTraitScores currScores = null;
                GwasAssociationStudyTraitScores newScores = newTrait.getScores().get(0);
                for (GwasAssociationStudyTraitScores scores : currTrait.getScores()) {
                    if (scores.getOrBeta() == newScores.getOrBeta()
                            && scores.getPValueMlog() == newScores.getPValueMlog()
                            && scores.getPValue() == newScores.getPValue()) {
                        currScores = scores;
                        break;
                    }
                }
                if (currScores != null) {
                    currTrait.getScores().add(newScores);
                }
            }
        }
    }
}
