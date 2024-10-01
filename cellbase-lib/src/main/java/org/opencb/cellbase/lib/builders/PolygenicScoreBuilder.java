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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.pgs.CommonPolygenicScore;
import org.opencb.biodata.models.core.pgs.PgsCohort;
import org.opencb.biodata.models.core.pgs.PolygenicScore;
import org.opencb.biodata.models.core.pgs.VariantPolygenicScore;
import org.opencb.biodata.models.variant.avro.OntologyTermAnnotation;
import org.opencb.biodata.models.variant.avro.PubmedReference;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.*;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class PolygenicScoreBuilder extends AbstractBuilder {

    private Path downloadPath;
    private Path integrationPath;
    private DataSource dataSource;

    private Set<String> pgsIdSet;
    private Object[] varRDBConn;
    private Object[] varPgsRDBConn;
    private int varBatchCounter = 0;
    private int varPgsBatchCounter = 0;
    private WriteBatch varBatch;
    private WriteBatch varPgsBatch;

    private long duplicatedKeys = 0;

    private static ObjectMapper mapper;
    private static ObjectReader varPgsReader;
    private static ObjectWriter jsonObjectWriter;

    private static final int MAX_BATCH_SIZE = 100;

    private static final String RSID_COL = "rsID";
    private static final String CHR_NAME_COL = "chr_name";
    private static final String EFFECT_ALLELE_COL = "effect_allele";
    private static final String OTHER_ALLELE_COL = "other_allele";
    private static final String EFFECT_WEIGHT_COL = "effect_weight";
    private static final String ALLELEFREQUENCY_EFFECT_COL = "allelefrequency_effect";
    private static final String ODDS_RATIO_COL = "OR";
    private static final String HAZARD_RATIO_COL = "HR";
    private static final String LOCUS_NAME_COL = "locus_name";
    private static final String IS_HAPLOTYPE_COL = "is_haplotype";
    private static final String IS_DIPLOTYPE_COL = "is_diplotype";
    private static final String IMPUTATION_METHOD_COL = "imputation_method";
    private static final String VARIANT_DESCRIPTION_COL = "variant_description";
    private static final String INCLUSION_CRITERIA_COL = "inclusion_criteria";
    private static final String IS_INTERACTION_COL = "is_interaction";
    private static final String IS_DOMINANT_COL = "is_dominant";
    private static final String IS_RECESSIVE_COL = "is_recessive";
    private static final String DOSAGE_0_WEIGHT_COL = "dosage_0_weight";
    private static final String DOSAGE_1_WEIGHT_COL = "dosage_1_weight";
    private static final String DOSAGE_2_WEIGHT_COL = "dosage_2_weight";
    private static final String HM_RSID_COL = "hm_rsID";
    private static final String HM_CHR_COL = "hm_chr";
    private static final String HM_POS_COL = "hm_pos";
    private static final String HM_INFEROTHERALLELE_COL = "hm_inferOtherAllele";

    public static final String SAMPLE_SET_KEY = "Sample Set";
    public static final String ODDS_RATIO_KEY = "Odds ratio";
    public static final String HAZARD_RATIO_KEY = "Hazard ratio";
    public static final String BETA_KEY = "Beta";
    public static final String AUROC_KEY = "AUROC"; // Area Under the Receiver-Operating Characteristic Curve (AUROC)
    public static final String CINDEX_KEY = "C-index"; // Concordance Statistic (C-index)
    public static final String OTHER_KEY = "Other metric";
    private static final String EFFECT_WEIGHT_KEY = "Effect weight";
    private static final String ALLELE_FREQUENCY_EFFECT_KEY = "Allele frequency effect";
    private static final String LOCUS_NAME_KEY = "Locus name";
    private static final String IS_HAPLOTYPE_KEY = "Haplotype";
    private static final String IS_DIPLOTYPE_KEY = "Diplotype";
    private static final String IMPUTATION_METHOD_KEY = "Imputation method";
    private static final String VARIANT_DESCRIPTION_KEY = "Variant description";
    private static final String INCLUSION_CRITERIA_KEY = "Score inclusion criteria";
    private static final String IS_INTERACTION_KEY = "Interaction";
    private static final String IS_DOMINANT_KEY = "Dominant inheritance model";
    private static final String IS_RECESSIVE_KEY = "Recessive inheritance model";
    private static final String DOSAGE_0_WEIGHT_KEY = "Effect weight with 0 copy of the effect allele";
    private static final String DOSAGE_1_WEIGHT_KEY = "Effect weight with 1 copy of the effect allele";
    private static final String DOSAGE_2_WEIGHT_KEY = "Effect weight with 1 copy of the effect allele";

    public static final String PGS_COMMON_OUTPUT_FILENAME = PGS_COMMON_COLLECTION + JSON_GZ_EXTENSION;
    public static final String PGS_VARIANT_OUTPUT_FILENAME = PGS_VARIANT_COLLECTION + JSON_GZ_EXTENSION;

    private static final Set<String> VALID_CHROMOSOMES = new HashSet<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT", "M"));

    private static final byte[] ONE = "1".getBytes();

    static {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        varPgsReader = mapper.readerFor(VariantPolygenicScore.class);
        jsonObjectWriter = mapper.writer();
    }

    public PolygenicScoreBuilder(Path downloadPath, CellBaseFileSerializer serializer) {
        super(serializer);

        this.downloadPath = downloadPath;

        logger = LoggerFactory.getLogger(PolygenicScoreBuilder.class);
    }

    public void check() throws CellBaseException, IOException {
        if (checked) {
            return;
        }

        logger.info(CHECKING_BEFORE_BUILDING_LOG_MESSAGE, getDataName(PGS_DATA));

        // Sanity check
        checkDirectory(downloadPath, getDataName(PGS_DATA));
        integrationPath = serializer.getOutdir().resolve("integration");
        Files.createDirectories(integrationPath);
        if (!Files.exists(integrationPath)) {
            throw new CellBaseException("Could not create the folder " + integrationPath);
        }
        // Prepare RocksDB for variant IDs
        this.varRDBConn = getDBConnection(integrationPath.resolve("rdb-var.idx").toString(), true);
        this.varBatch = new WriteBatch();
        // Prepare RocksDB for PGS/variants
        this.varPgsRDBConn = getDBConnection(integrationPath.resolve("rdb-var-pgs.idx").toString(), true);
        this.varPgsBatch = new WriteBatch();
        // PGS set
        this.pgsIdSet = new HashSet<>();

        // Check downloaded files
        this.dataSource = dataSourceReader.readValue(downloadPath.resolve(getDataVersionFilename(PGS_CATALOG_DATA)).toFile());
        checkFiles(dataSource, downloadPath, getDataName(PGS_CATALOG_DATA));

        logger.info(CHECKING_DONE_BEFORE_BUILDING_LOG_MESSAGE, getDataName(PGS_DATA));
        checked = true;
    }

    @Override
    public void parse() throws Exception {
        check();

        logger.info(BUILDING_LOG_MESSAGE, getDataName(PGS_DATA));

        int numFiles;
        int counter;
        String endsWith;
        File[] files = downloadPath.toFile().listFiles();

        // First, process metadata files
        try (BufferedWriter bw = FileUtils.newBufferedWriter(serializer.getOutdir().resolve(PGS_COMMON_COLLECTION + JSON_GZ_EXTENSION))) {
            counter = 0;
            endsWith = "_metadata" + TAR_GZ_EXTENSION;
            numFiles = getNumFiles(files, endsWith);
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(endsWith)) {
                    // E.g.: PGS004905_metadata.tar.gz: it contains a set of files about metadata
                    logger.info(PARSING_LOG_MESSAGE, file.getName());
                    processPgsMetadataFile(file, bw);
                    logger.info(PARSING_DONE_LOG_MESSAGE, file.getName());
                    logger.info("Progress: {} of {} meta files", ++counter, numFiles);
                }
            }
        }

        // Second, process variant files
        counter = 0;
        endsWith = TXT_GZ_EXTENSION;
        numFiles = getNumFiles(files, endsWith);
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(endsWith)) {
                // E.g.: PGS004905_hmPOS_GRCh38.txt.gz: it contains the variants
                logger.info(PARSING_LOG_MESSAGE, file.getName());

                String pgsId = null;
                Map<String, Integer> columnPos = new HashMap<>();

                try (BufferedReader br = FileUtils.newBufferedReader(file.toPath())) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("#")) {
                            if (line.startsWith("#pgs_id=")) {
                                pgsId = line.split("=")[1].trim();
                                // Sanity check
                                if (!file.getName().startsWith(pgsId)) {
                                    throw new CellBaseException("Error parsing file " + file.getName() + ": pgs_id mismatch");
                                }
                                // Add PGS ID to the set
                                pgsIdSet.add(pgsId);
                            }
                        } else if (line.startsWith(RSID_COL) || line.startsWith(CHR_NAME_COL)) {
                            String[] fields = line.split("\t");
                            for (int i = 0; i < fields.length; i++) {
                                columnPos.put(fields[i], i);
                            }
                        } else {
                            // Sanity check
                            if (pgsId == null) {
                                throw new CellBaseException("Error parsing file " + file.getName() + ": pgs_id is null");
                            }
                            saveVariantPolygenicScore(line, columnPos, pgsId);
                        }
                    }
                }
                logger.info(PARSING_DONE_LOG_MESSAGE, file.getName());
                logger.info("Progress: {} of {} variant files", ++counter, numFiles);
            }
        }

        RocksDB rdb;
        // Write remaining variant ID batch
        if (varBatchCounter > 0) {
            rdb = (RocksDB) varRDBConn[0];
            rdb.write(new WriteOptions(), varBatch);
            varBatch.clear();
        }
        // Write remaining PGS/variant batch
        if (varPgsBatchCounter > 0) {
            rdb = (RocksDB) varPgsRDBConn[0];
            rdb.write(new WriteOptions(), varPgsBatch);
            varPgsBatch.clear();
        }

        // Serialize/write the saved variant polygenic scores in the RocksDB
        serializeRDB();
        serializer.close();

        logger.info(BUILDING_DONE_LOG_MESSAGE, getDataName(PGS_DATA));
    }

    private void processPgsMetadataFile(File metadataFile, BufferedWriter bw) throws CellBaseException {
        String pgsId = metadataFile.getName().split("_")[0];

        Path tmp = serializer.getOutdir().resolve("tmp");
        if (!tmp.toFile().exists()) {
            tmp.toFile().mkdirs();
        }

        String command = "tar -xzf " + metadataFile.getAbsolutePath() + " -C " + tmp.toAbsolutePath();
        try {
            logger.info("Executing: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new CellBaseException("Exception raised when executing: " + command, e);
        }

        // Create PGS object, with the common fields
        String filename;
        CommonPolygenicScore pgs = new CommonPolygenicScore();
        pgs.setId(pgsId);
        pgs.setSource(PGS_CATALOG_DATA);
        pgs.setVersion(dataSource.getVersion());

        String line;
        String[] field;

        // PGSxxxxx_metadata_publications.csv
        filename = pgsId + "_metadata_publications.csv";
        try (BufferedReader br = FileUtils.newBufferedReader(tmp.resolve(filename))) {
            // Skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                // 0                                1              2       3              4                  5              6
                // PGS Publication/Study (PGP) ID   First Author   Title   Journal Name   Publication Date   Release Date   Authors
                // 7                                 8
                // digital object identifier (doi)   PubMed ID (PMID)
                StringReader stringReader = new StringReader(line);
                CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
                CSVRecord strings = csvParser.getRecords().get(0);
                pgs.getPubmedRefs().add(new PubmedReference(strings.get(8), strings.get(2), strings.get(3), strings.get(4), null));
            }
        } catch (IOException e) {
            throw new CellBaseException("Parsing file " + filename, e);
        }

        // PGSxxxxx_metadata_efo_traits.csv
        filename = pgsId + "_metadata_efo_traits.csv";
        try (BufferedReader br = FileUtils.newBufferedReader(tmp.resolve(filename))) {
            // Skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                // 0                   1                      2                            3
                // Ontology Trait ID   Ontology Trait Label   Ontology Trait Description   Ontology URL
                StringReader stringReader = new StringReader(line);
                CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
                CSVRecord strings = csvParser.getRecords().get(0);
                pgs.getTraits().add(new OntologyTermAnnotation(strings.get(0), strings.get(1), strings.get(2), "EFO", strings.get(3),
                        new HashMap<>()));
            }
        } catch (IOException e) {
            throw new CellBaseException("Parsing file " + filename, e);
        }

        // PGSxxxxx_metadata_scores.csv
        filename = pgsId + "_metadata_scores.csv";
        try (BufferedReader br = FileUtils.newBufferedReader(tmp.resolve(filename))) {
            // Skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                // 0                          1          2                3                             4
                // Polygenic Score (PGS) ID   PGS Name   Reported Trait   Mapped Trait(s) (EFO label)   Mapped Trait(s) (EFO ID)
                // 5                        6                                             7                       8
                // PGS Development Method   PGS Development Details/Relevant Parameters   Original Genome Build   Number of Variants
                // 9                             10                       11                         12                   13
                // Number of Interaction Terms   Type of Variant Weight   PGS Publication (PGP) ID   Publication (PMID)   Publication (doi)
                // 14                                                 15
                // Score and results match the original publication   Ancestry Distribution (%) - Source of Variant Associations (GWAS)
                // 16                                                       17                                           18
                // Ancestry Distribution (%) - Score Development/Training   Ancestry Distribution (%) - PGS Evaluation   FTP link
                // 19               20
                // Release Date     License/Terms of Use
                StringReader stringReader = new StringReader(line);
                CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
                CSVRecord strings = csvParser.getRecords().get(0);
                // Sanity check
                if (!pgsId.equals(strings.get(0))) {
                    throw new CellBaseException("Mismatch PGS ID when parsing file " + pgsId + "_metadata_scores.csv");
                }
                if (StringUtils.isNotEmpty(pgs.getName())) {
                    throw new CellBaseException("More than one PGS in file " + pgsId + "_metadata_scores.csv");
                }
                pgs.setName(strings.get(1));
            }
        } catch (IOException e) {
            throw new CellBaseException("Parsing file " + filename, e);
        }

        // TODO: PGSxxxxx_metadata_score_development_samples.csv
        // 0                          1                          2                       3                 4
        // Polygenic Score (PGS) ID   Stage of PGS Development   Number of Individuals   Number of Cases   Number of Controls
        // 5                                      6            7                         8
        // Percent of Participants Who are Male   Sample Age   Broad Ancestry Category   "Ancestry (e.g. French, Chinese)"
        // 9                        10                                11                                  12
        // Country of Recruitment   Additional Ancestry Description   Phenotype Definitions and Methods   Followup Time
        // 13                                13                        14           15          16
        // GWAS Catalog Study ID (GCST...)   Source PubMed ID (PMID)   Source DOI   Cohort(s)   Additional Sample/Cohort Information

        // PGSxxxxx_metadata_performance_metrics.csv
        filename = pgsId + "_metadata_performance_metrics.csv";
        try (BufferedReader br = FileUtils.newBufferedReader(tmp.resolve(filename))) {
            // Skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                // 0                                 1                 2                      3                          4
                // PGS Performance Metric (PPM) ID   Evaluated Score   PGS Sample Set (PSS)   PGS Publication (PGP) ID   Reported Trait
                // 5                                  6                                             7                    8
                // Covariates Included in the Model   PGS Performance: Other Relevant Information   Publication (PMID)   Publication (doi)
                // 9                   10                11     12
                // Hazard Ratio (HR)   Odds Ratio (OR)   Beta   Area Under the Receiver-Operating Characteristic Curve (AUROC)
                // 13                                14
                // Concordance Statistic (C-index)   Other Metric(s)

                StringReader stringReader = new StringReader(line);
                CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
                CSVRecord strings = csvParser.getRecords().get(0);

                // Sanity check
                if (!pgsId.equals(strings.get(1))) {
                    continue;
                }

                Map<String, String> values = new HashMap<>();
                if (StringUtils.isNotEmpty(strings.get(2))) {
                    values.put(SAMPLE_SET_KEY, strings.get(2));
                }
                if (StringUtils.isNotEmpty(strings.get(9))) {
                    values.put(HAZARD_RATIO_KEY, strings.get(9));
                }
                if (StringUtils.isNotEmpty(strings.get(10))) {
                    values.put(ODDS_RATIO_KEY, strings.get(10));
                }
                if (StringUtils.isNotEmpty(strings.get(11))) {
                    values.put(BETA_KEY, strings.get(11));
                }
                if (StringUtils.isNotEmpty(strings.get(12))) {
                    values.put(AUROC_KEY, strings.get(12));
                }
                if (StringUtils.isNotEmpty(strings.get(13))) {
                    values.put(CINDEX_KEY, strings.get(13));
                }
                if (StringUtils.isNotEmpty(strings.get(14))) {
                    values.put(OTHER_KEY, strings.get(14));
                }
                pgs.getValues().add(values);
            }
        } catch (IOException e) {
            throw new CellBaseException("Parsing file " + filename, e);
        }

        // TODO: PGSxxxxx_metadata_evaluation_sample_sets.csv
        // 0                      1                          2                       3                 4
        // PGS Sample Set (PSS)   Polygenic Score (PGS) ID   Number of Individuals   Number of Cases   Number of Controls
        // 5                                      6                                    7
        // Percent of Participants Who are Male   Sample Age,Broad Ancestry Category   "Ancestry (e.g.French, Chinese)"
        // 8                        9                                 10                                  11
        // Country of Recruitment   Additional Ancestry Description   Phenotype Definitions and Methods   Followup Time
        // 12                                13                        14           15          16
        // GWAS Catalog Study ID (GCST...)   Source PubMed ID (PMID)   Source DOI   Cohort(s)   Additional Sample/Cohort Information

        // PGSxxxxx_metadata_cohorts.csv
        filename = pgsId + "_metadata_cohorts.csv";
        try (BufferedReader br = FileUtils.newBufferedReader(tmp.resolve(filename))) {
            // Skip first line
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                // 0           1             2
                // Cohort ID   Cohort Name   Previous/other/additional names
                StringReader stringReader = new StringReader(line);
                CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
                CSVRecord strings = csvParser.getRecords().get(0);
                pgs.getCohorts().add(new PgsCohort(strings.get(0), strings.get(1), strings.get(2)));
            }
        } catch (IOException e) {
            throw new CellBaseException("Parsing file " + filename, e);
        }

        // Create PGS object, with the common fields
        try {
            bw.write(jsonObjectWriter.writeValueAsString(pgs));
            bw.write("\n");
        } catch (IOException e) {
            throw new CellBaseException("Writing CommonPolygenicScore data model", e);
        }

        // Clean tmp folder
        for (File tmpFile : tmp.toFile().listFiles()) {
            tmpFile.delete();
        }
    }

    private void saveVariantPolygenicScore(String line, Map<String, Integer> columnPos, String pgsId)
            throws RocksDBException, IOException, CellBaseException {
        String chrom;
        int position;
        String effectAllele;
        String otherAllele;

        String[] field = line.split("\t", -1);

        if (columnPos.containsKey(HM_CHR_COL)) {
            chrom = field[columnPos.get(HM_CHR_COL)];
            if (!VALID_CHROMOSOMES.contains(chrom)) {
                // Only chromosomes are processed; no contigs, e.g.: 8_KI270821v1_alt, 11_KI270927v1_alt, 12_GL877875v1_alt,...
                return;
            }
        } else {
//            logger.warn("Missing field '{}', skipping line: {}", HM_CHR_COL, line);
            return;
        }
        if (columnPos.containsKey(HM_POS_COL)) {
            try {
                position = Integer.parseInt(field[columnPos.get(HM_POS_COL)]);
            } catch (NumberFormatException e) {
//                logger.warn("Invalid field '{}' (value = {}), skipping line: {}", HM_POS_COL, field[columnPos.get(HM_POS_COL)], line);
                return;
            }
        } else {
//            logger.warn("Missing field '{}', skipping line: {}", HM_POS_COL, line);
            return;
        }
        if (columnPos.containsKey(EFFECT_ALLELE_COL)) {
            effectAllele = field[columnPos.get(EFFECT_ALLELE_COL)];
        } else {
//            logger.warn("Missing field '{}', skipping line: {}", EFFECT_ALLELE_COL, line);
            return;
        }
        if (columnPos.containsKey(HM_INFEROTHERALLELE_COL) && StringUtils.isNotEmpty(field[columnPos.get(HM_INFEROTHERALLELE_COL)])) {
            otherAllele = field[columnPos.get(HM_INFEROTHERALLELE_COL)];
        } else if (columnPos.containsKey(OTHER_ALLELE_COL)) {
            otherAllele = field[columnPos.get(OTHER_ALLELE_COL)];
        } else {
//            logger.warn("Missing fields '{}' and '{}' (at least one is mandatory), skipping line: {}", HM_INFEROTHERALLELE_COL,
//                    OTHER_ALLELE_COL, line);
            return;
        }

        // Create polygenic score
        Map<String, String> values = new HashMap<>();
        if (columnPos.containsKey(EFFECT_WEIGHT_COL)) {
            values.put(EFFECT_WEIGHT_KEY, field[columnPos.get(EFFECT_WEIGHT_COL)]);
        }
        if (columnPos.containsKey(ALLELEFREQUENCY_EFFECT_COL)) {
            values.put(ALLELE_FREQUENCY_EFFECT_KEY, field[columnPos.get(ALLELEFREQUENCY_EFFECT_COL)]);
        }
        if (columnPos.containsKey(ODDS_RATIO_COL)) {
            values.put(ODDS_RATIO_KEY, field[columnPos.get(ODDS_RATIO_COL)]);
        }
        if (columnPos.containsKey(HAZARD_RATIO_COL)) {
            values.put(HAZARD_RATIO_KEY, field[columnPos.get(HAZARD_RATIO_COL)]);
        }
        if (columnPos.containsKey(LOCUS_NAME_COL)) {
            values.put(LOCUS_NAME_KEY, field[columnPos.get(LOCUS_NAME_COL)]);
        }
        if (columnPos.containsKey(IS_HAPLOTYPE_COL)) {
            values.put(IS_HAPLOTYPE_KEY, field[columnPos.get(IS_HAPLOTYPE_COL)]);
        }
        if (columnPos.containsKey(IS_DIPLOTYPE_COL)) {
            values.put(IS_DIPLOTYPE_KEY, field[columnPos.get(IS_DIPLOTYPE_COL)]);
        }
        if (columnPos.containsKey(IMPUTATION_METHOD_COL)) {
            values.put(IMPUTATION_METHOD_KEY, field[columnPos.get(IMPUTATION_METHOD_COL)]);
        }
        if (columnPos.containsKey(VARIANT_DESCRIPTION_COL)) {
            values.put(VARIANT_DESCRIPTION_KEY, field[columnPos.get(VARIANT_DESCRIPTION_COL)]);
        }
        if (columnPos.containsKey(INCLUSION_CRITERIA_COL)) {
            values.put(INCLUSION_CRITERIA_KEY, field[columnPos.get(INCLUSION_CRITERIA_COL)]);
        }
        if (columnPos.containsKey(IS_INTERACTION_COL)) {
            values.put(IS_INTERACTION_KEY, field[columnPos.get(IS_INTERACTION_COL)]);
        }
        if (columnPos.containsKey(IS_DOMINANT_COL)) {
            values.put(IS_DOMINANT_KEY, field[columnPos.get(IS_DOMINANT_COL)]);
        }
        if (columnPos.containsKey(IS_RECESSIVE_COL)) {
            values.put(IS_RECESSIVE_KEY, field[columnPos.get(IS_RECESSIVE_COL)]);
        }
        if (columnPos.containsKey(DOSAGE_0_WEIGHT_COL)) {
            values.put(DOSAGE_0_WEIGHT_KEY, field[columnPos.get(DOSAGE_0_WEIGHT_COL)]);
        }
        if (columnPos.containsKey(DOSAGE_1_WEIGHT_COL)) {
            values.put(DOSAGE_1_WEIGHT_KEY, field[columnPos.get(DOSAGE_1_WEIGHT_COL)]);
        }
        if (columnPos.containsKey(DOSAGE_2_WEIGHT_COL)) {
            values.put(DOSAGE_2_WEIGHT_KEY, field[columnPos.get(DOSAGE_2_WEIGHT_COL)]);
        }

        // Creating and/or updating variant polygenic score

        // First, we store the variant
        RocksDB rdb = (RocksDB) varRDBConn[0];
        String key = chrom + ":" + position + ":" + otherAllele + ":" + effectAllele;
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            // Add data to batch
            varBatch.put(key.getBytes(), ONE);
            varBatchCounter++;
            if (varBatchCounter >= MAX_BATCH_SIZE) {
                // Write the batch to the database
//                logger.info("Writing variant ID batch with {} items, {} KB", varBatch.count(), varBatchSize / 1024);
                rdb.write(new WriteOptions(), varBatch);
                // Reset batch
                varBatch.clear();
                varBatchCounter = 0;
           }
//            rdb.put(key.getBytes(), ONE);
        }

        // Second, we store the polygenic scores
        rdb = (RocksDB) varPgsRDBConn[0];
        key = chrom + ":" + position + ":" + otherAllele + ":" + effectAllele + ":" + pgsId;
        dbContent = rdb.get(key.getBytes());
        if (dbContent != null) {
//            throw new CellBaseException("Error indexing PGS key " + key + ": it must be unique");
            duplicatedKeys++;
            logger.warn("Warning: the indexing PGS key " + key + ": it should be unique");
        } else {
            VariantPolygenicScore varPgs = new VariantPolygenicScore(chrom, position, otherAllele, effectAllele,
                    Collections.singletonList(new PolygenicScore(pgsId, values)));
            // Add data to batch
            byte[] rdbKey = key.getBytes();
            byte[] rdbValue = jsonObjectWriter.writeValueAsBytes(varPgs);
            varPgsBatch.put(rdbKey, rdbValue);
            varPgsBatchCounter++;
            if (varPgsBatchCounter >= MAX_BATCH_SIZE) {
                // Write the batch to the database
//                logger.info("Writing PGS batch with {} items, {} KB", varPgsBatch.count(), varPgsBatchSize / 1024);
                rdb.write(new WriteOptions(), varPgsBatch);
                // Reset batch
                varPgsBatch.clear();
                varPgsBatchCounter = 0;
            }
//            rdb.put(key.getBytes(), jsonObjectWriter.writeValueAsBytes(varPgs));
        }
    }

    private void serializeRDB() throws IOException, RocksDBException {
        long counter = 0;

        RocksDB varRDB = (RocksDB) varRDBConn[0];
        RocksDB varPgsRDB = (RocksDB) varPgsRDBConn[0];

        // DO NOT change the name of the rocksIterator variable - for some unexplainable reason Java VM crashes if it's
        // named "iterator"
        logger.info("Writing variants ...");
        RocksIterator rocksIterator = varRDB.newIterator();
        for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
            String varKey = new String(rocksIterator.key());
            VariantPolygenicScore varPgs = null;
            for (String pgsId : pgsIdSet) {
                String varPgsKey = varKey + ":" + pgsId;
                byte[] dbContent = varPgsRDB.get(varPgsKey.getBytes());
                if (dbContent != null) {
                    VariantPolygenicScore newVarPgs = varPgsReader.readValue(dbContent);
                    if (varPgs == null) {
                        varPgs = newVarPgs;
                    } else {
                        varPgs.getPolygenicScores().addAll(newVarPgs.getPolygenicScores());
                    }
                }
            }
            if (varPgs != null) {
                serializer.serialize(varPgs);
            }
            if (++counter % 500000 == 0) {
                logger.info("Writing {} variants...", counter);
            }
        }
        logger.info("Writing done.");
        logger.info("Num. duplicated keys (PGS/Variant) = {}", duplicatedKeys);

        // Close RocksDB
        closeIndex((RocksDB) varRDBConn[0], (Options) varRDBConn[1], (String) varRDBConn[2]);
        closeIndex((RocksDB) varPgsRDBConn[0], (Options) varPgsRDBConn[1], (String) varPgsRDBConn[2]);
    }

    private void closeIndex(RocksDB rdb, Options dbOption, String dbLocation) throws IOException {
        if (rdb != null) {
            rdb.close();
        }
        if (dbOption != null) {
            dbOption.dispose();
        }
        if (dbLocation != null && Files.exists(Paths.get(dbLocation))) {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(dbLocation));
        }
    }

    private Object[] getDBConnection(String dbLocation, boolean forceCreate) {
        boolean indexingNeeded = forceCreate || !Files.exists(Paths.get(dbLocation));
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setBlockCacheSize(4 * 1024 * 1024 * 1024L); // 16 GB block cache

        Options options = new Options()
                .setCreateIfMissing(true)
                .setWriteBufferSize(256 * 1024 * 1024) // 256 MB
                .setMaxWriteBufferNumber(4)
                .setMinWriteBufferNumberToMerge(2)
                .setIncreaseParallelism(4)
                .setMaxBackgroundCompactions(4)
                .setMaxBackgroundFlushes(2)
                .setLevelCompactionDynamicLevelBytes(true)
                .setTargetFileSizeBase(64 * 1024 * 1024) // 64 MB
                .setMaxBytesForLevelBase(512 * 1024 * 1024) // 512 MB
                .setTableFormatConfig(tableConfig)
                .setCompressionType(CompressionType.LZ4_COMPRESSION);

//        options.setMaxBackgroundCompactions(4);
//        options.setMaxBackgroundFlushes(1);
//        options.setCompressionType(CompressionType.NO_COMPRESSION);
//        options.setMaxOpenFiles(-1);
//        options.setIncreaseParallelism(4);
//        options.setCompactionStyle(CompactionStyle.LEVEL);
//        options.setLevelCompactionDynamicLevelBytes(true);

        RocksDB db = null;
        try {
            // a factory method that returns a RocksDB instance
            if (indexingNeeded) {
                db = RocksDB.open(options, dbLocation);
            } else {
                db = RocksDB.openReadOnly(options, dbLocation);
            }
            // do something
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
            System.exit(1);
        }

        return new Object[]{db, options, dbLocation, indexingNeeded};
    }

    private int getNumFiles(File[] files, String endsWith) {
        int numFiles = 0;
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(endsWith)) {
                ++numFiles;
            }
        }
        return numFiles;
    }
}
