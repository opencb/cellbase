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
import org.opencb.biodata.models.core.pgs.*;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PolygenicScoreBuilder extends CellBaseBuilder {

    private String source;
    private String version;

    private Path pgsDir;
    private CellBaseFileSerializer fileSerializer;

    protected RocksDB rdb;

    protected static ObjectMapper mapper;
    protected static ObjectReader varPgsReader;
    protected static ObjectWriter jsonObjectWriter;

    public static final String COMMON_POLYGENIC_SCORE_FILENAME =  "common_polygenic_score.json.gz";
    public static final String VARIANT_POLYGENIC_SCORE_FILENAME =  "variant_polygenic_score.json.gz";

    private static final String RSID = "rsID";
    private static final String CHR_NAME = "chr_name";
    private static final String EFFECT_ALLELE = "effect_allele";
    private static final String OTHER_ALLELE = "other_allele";
    private static final String EFFECT_WEIGHT = "effect_weight";
    private static final String ALLELEFREQUENCY_EFFECT = "allelefrequency_effect";
    private static final String LOCUS_NAME = "locus_name";
    private static final String OR = "OR";
    private static final String HM_SOURCE = "hm_source";
    private static final String HM_RSID = "hm_rsID";
    private static final String HM_CHR = "hm_chr";
    private static final String HM_POS = "hm_pos";
    private static final String HM_INFEROTHERALLELE = "hm_inferOtherAllele";

    static {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        varPgsReader = mapper.readerFor(VariantPolygenicScore.class);
        jsonObjectWriter = mapper.writer();
    }

    public PolygenicScoreBuilder(String source, String version, Path pgsDir, CellBaseFileSerializer serializer) {
        super(serializer);

        this.source = source;
        this.version = version;

        this.fileSerializer = serializer;
        this.pgsDir = pgsDir;

        logger = LoggerFactory.getLogger(PolygenicScoreBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        // Check input folder
        FileUtils.checkPath(pgsDir);

        logger.info("Parsing polygenic score (PGS) files...");

        Object[] dbConnection = getDBConnection(pgsDir.resolve("rdb.idx").toString(), true);
        rdb = (RocksDB) dbConnection[0];
        Options dbOption = (Options) dbConnection[1];
        String dbLocation = (String) dbConnection[2];

        BufferedWriter bw = FileUtils.newBufferedWriter(serializer.getOutdir().resolve(COMMON_POLYGENIC_SCORE_FILENAME));

        for (File file : pgsDir.toFile().listFiles()) {
            if (file.isFile()) {
                if (file.getName().endsWith(".txt.gz")) {
                    logger.info("Processing PGS file: {}", file.getName());

                    String pgsId = null;
                    Map<String, Integer> labelPos = new HashMap<>();

                    BufferedReader br = FileUtils.newBufferedReader(file.toPath());
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("#")) {
                            if (line.startsWith("#pgs_id=")) {
                                pgsId = line.split("=")[1].trim();
                                // Sanity check
                                if (!file.getName().startsWith(pgsId)) {
                                    throw new CellBaseException("Error parsing file " + file.getName() + ": pgs_id mismatch");
                                }
                            }
                        } else if (line.startsWith(RSID) || line.startsWith(CHR_NAME)) {
                            String[] fields = line.split("\t");
                            for (int i = 0; i < fields.length; i++) {
                                labelPos.put(fields[i], i);
                            }
                        } else {
                            // Sanity check
                            if (pgsId == null) {
                                throw new CellBaseException("Error parsing file " + file.getName() + ": pgs_id is null");
                            }
                            saveVariantPolygenicScore(line, labelPos, pgsId);
                        }
                    }
                    br.close();
                } else if (file.getName().endsWith("_metadata.tar.gz")) {
                    processPgsMetadataFile(file, bw);
                }
            }
        }

        // Serialize/write the saved variant polygenic scores in the RocksDB
        serializeRDB(rdb);
        closeIndex(rdb, dbOption, dbLocation);
        serializer.close();

        // Close PGS file (with common attributes)
        bw.close();

        logger.info("Parsing PGS files finished.");
    }

    private void processPgsMetadataFile(File metadataFile, BufferedWriter bw) throws IOException, CellBaseException {
        String pgsId = metadataFile.getName().split("_")[0];

        Path tmp = pgsDir.resolve("tmp");
        if (!tmp.toFile().exists()) {
            tmp.toFile().mkdirs();
        }

        String command = "tar -xzf " + metadataFile.getAbsolutePath() + " -C " + tmp.toAbsolutePath();
        Process process = Runtime.getRuntime().exec(command);

        // Wait for the process to complete
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Error waiting for the process to complete.", e);
        }

        // Check the exit code
        if (exitCode != 0) {
            throw new IOException("Error executing the command. Exit code: " + exitCode);
        }

        // Create PGS object, with the common fields
        CommonPolygenicScore pgs = new CommonPolygenicScore();
        pgs.setId(pgsId);
        pgs.setSource(source);
        pgs.setVersion(version);

        String line;
        String[] field;
        BufferedReader br;
        // PGSxxxxx_metadata_publications.csv
        br = FileUtils.newBufferedReader(tmp.resolve(pgsId + "_metadata_publications.csv"));
        // Skip first line
        line = br.readLine();
        while ((line = br.readLine()) != null) {
            // 0                                1              2       3              4                  5              6
            // PGS Publication/Study (PGP) ID   First Author   Title   Journal Name   Publication Date   Release Date   Authors
            // 7                                 8
            // digital object identifier (doi)   PubMed ID (PMID)
            StringReader stringReader = new StringReader(line);
            CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
            pgs.getPubmedIds().add(csvParser.getRecords().get(0).get(8));
        }

        // PGSxxxxx_metadata_efo_traits.csv
        br = FileUtils.newBufferedReader(tmp.resolve(pgsId + "_metadata_efo_traits.csv"));
        // Skip first line
        line = br.readLine();
        while ((line = br.readLine()) != null) {
            // 0                   1                      2                            3
            // Ontology Trait ID   Ontology Trait Label   Ontology Trait Description   Ontology URL
            StringReader stringReader = new StringReader(line);
            CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
            CSVRecord strings = csvParser.getRecords().get(0);
            pgs.getEfoTraits().add(new EfoTrait(strings.get(0), strings.get(1), strings.get(2), strings.get(3)));
        }

        // PGSxxxxx_metadata_scores.csv
        br = FileUtils.newBufferedReader(tmp.resolve(pgsId + "_metadata_scores.csv"));
        // Skip first line
        line = br.readLine();
        while ((line = br.readLine()) != null) {
            // 0                          1          2                3                             4
            // Polygenic Score (PGS) ID   PGS Name   Reported Trait   Mapped Trait(s) (EFO label)   Mapped Trait(s) (EFO ID)
            // 5                        6                                             7                       8
            // PGS Development Method   PGS Development Details/Relevant Parameters   Original Genome Build   Number of Variants
            // 9                             10                       11                         12                   13
            // Number of Interaction Terms   Type of Variant Weight   PGS Publication (PGP) ID   Publication (PMID)   Publication (doi)
            // 14                                                 15
            // Score and results match the original publication   Ancestry Distribution (%) - Source of Variant Associations (GWAS)
            // 16                                                       17                                           18         19
            // Ancestry Distribution (%) - Score Development/Training   Ancestry Distribution (%) - PGS Evaluation   FTP link   Release Date
            // 19
            // License/Terms of Use
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

        // PGSxxxxx_metadata_score_development_samples.csv
        // 0                          1                          2                       3                 4
        // Polygenic Score (PGS) ID   Stage of PGS Development   Number of Individuals   Number of Cases   Number of Controls
        // 5                                      6            7                         8
        // Percent of Participants Who are Male   Sample Age   Broad Ancestry Category   "Ancestry (e.g. French, Chinese)"
        // 9                        10                                11                                  12
        // Country of Recruitment   Additional Ancestry Description   Phenotype Definitions and Methods   Followup Time
        // 13                                13                        14           15          16
        // GWAS Catalog Study ID (GCST...)   Source PubMed ID (PMID)   Source DOI   Cohort(s)   Additional Sample/Cohort Information

        // PGSxxxxx_metadata_performance_metrics.csv
        br = FileUtils.newBufferedReader(tmp.resolve(pgsId + "_metadata_performance_metrics.csv"));
        // Skip first line
        line = br.readLine();
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

            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setId(strings.get(0));
            if (StringUtils.isNotEmpty(strings.get(9))) {
                metrics.setHazardRatio(strings.get(9));
            }
            if (StringUtils.isNotEmpty(strings.get(10))) {
                metrics.setOddsRatio(strings.get(10));
            }
            if (StringUtils.isNotEmpty(strings.get(11))) {
                metrics.setBeta(strings.get(11));
            }
            if (StringUtils.isNotEmpty(strings.get(12))) {
                metrics.setAuroc(strings.get(12));
            }
            if (StringUtils.isNotEmpty(strings.get(13))) {
                metrics.setcIndex(strings.get(13));
            }
            if (StringUtils.isNotEmpty(strings.get(14))) {
                metrics.setOtherMetrics(strings.get(14));
            }
            pgs.getPerformanceMetrics().add(metrics);
        }

        // PGSxxxxx_metadata_evaluation_sample_sets.csv
        // 0                      1                          2                       3                 4
        // PGS Sample Set (PSS)   Polygenic Score (PGS) ID   Number of Individuals   Number of Cases   Number of Controls
        // 5                                      6                                    7
        // Percent of Participants Who are Male   Sample Age,Broad Ancestry Category   "Ancestry (e.g.French, Chinese)"
        // 8                        9                                 10                                  11
        // Country of Recruitment   Additional Ancestry Description   Phenotype Definitions and Methods   Followup Time
        // 12                                13                        14           15          16
        // GWAS Catalog Study ID (GCST...)   Source PubMed ID (PMID)   Source DOI   Cohort(s)   Additional Sample/Cohort Information

        // PGSxxxxx_metadata_cohorts.csv
        br = FileUtils.newBufferedReader(tmp.resolve(pgsId + "_metadata_cohorts.csv"));
        // Skip first line
        line = br.readLine();
        while ((line = br.readLine()) != null) {
            // 0           1             2
            // Cohort ID   Cohort Name   Previous/other/additional names
            StringReader stringReader = new StringReader(line);
            CSVParser csvParser = CSVFormat.DEFAULT.parse(stringReader);
            CSVRecord strings = csvParser.getRecords().get(0);
            pgs.getCohorts().add(new Cohort(strings.get(0), strings.get(1)));
        }


        // Create PGS object, with the common fields
        bw.write(jsonObjectWriter.writeValueAsString(pgs));
        bw.write("\n");

        // Clean tmp folder
        for (File tmpFile : tmp.toFile().listFiles()) {
            tmpFile.delete();
        }
    }

    private void saveVariantPolygenicScore(String line, Map<String, Integer> labelPos, String pgsId)
            throws RocksDBException, IOException {
        String chrom;
        int position;
        String effectAllele;
        String otherAllele;

        String[] field = line.split("\t", -1);

        if (labelPos.containsKey(HM_CHR)) {
            chrom = field[labelPos.get(HM_CHR)];
        } else {
            logger.warn("Missing field '{}', skipping line: {}", HM_CHR, line);
            return;
        }
        if (labelPos.containsKey(HM_POS)) {
            position = Integer.parseInt(field[labelPos.get(HM_POS)]);
        } else {
            logger.warn("Missing field '{}', skipping line: {}", HM_POS, line);
            return;
        }
        if (labelPos.containsKey(EFFECT_ALLELE)) {
            effectAllele = field[labelPos.get(EFFECT_ALLELE)];
        } else {
            logger.warn("Missing field '{}', skipping line: {}", EFFECT_ALLELE, line);
            return;
        }
        if (labelPos.containsKey(HM_INFEROTHERALLELE) && StringUtils.isNotEmpty(field[labelPos.get(HM_INFEROTHERALLELE)])) {
            otherAllele = field[labelPos.get(HM_INFEROTHERALLELE)];
        } else if (labelPos.containsKey(OTHER_ALLELE)) {
            otherAllele = field[labelPos.get(OTHER_ALLELE)];
        } else {
            logger.warn("Missing fields '{}' and '{}' (at least one is mandatory), skipping line: {}", HM_INFEROTHERALLELE, OTHER_ALLELE,
                    line);
            return;
        }

        // Create polygenic score
        PolygenicScore pgs = new PolygenicScore();
        pgs.setId(pgsId);
        if (labelPos.containsKey(EFFECT_WEIGHT)) {
            pgs.setEffectWeight(Double.parseDouble(field[labelPos.get(EFFECT_WEIGHT)]));
        }
        if (labelPos.containsKey(ALLELEFREQUENCY_EFFECT)) {
            pgs.setAlleleFrequencyEffect(Double.parseDouble(field[labelPos.get(ALLELEFREQUENCY_EFFECT)]));
        }
        if (labelPos.containsKey(OR)) {
            pgs.setOr(Double.parseDouble(field[labelPos.get(OR)]));
        }
        if (labelPos.containsKey(LOCUS_NAME)) {
            pgs.setLocusName(field[labelPos.get(LOCUS_NAME)]);
        }

        // Creating and/or updating variant polygenic score
        VariantPolygenicScore varPgs;
        String key = chrom + ":" + position + ":" + otherAllele + ":" + effectAllele;
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            varPgs = new VariantPolygenicScore(chrom, position, otherAllele, effectAllele,
                    Collections.singletonList(pgs));
        } else {
            varPgs = varPgsReader.readValue(dbContent);
            varPgs.getPolygenicScores().add(pgs);
        }
        rdb.put(key.getBytes(), jsonObjectWriter.writeValueAsBytes(varPgs));
    }

    private void serializeRDB(RocksDB rdb) throws IOException {
        // DO NOT change the name of the rocksIterator variable - for some unexplainable reason Java VM crashes if it's
        // named "iterator"
        RocksIterator rocksIterator = rdb.newIterator();

        logger.info("Reading from RocksDB index and serializing to {}.json.gz", serializer.getOutdir().resolve(serializer.getFileName()));
        int counter = 0;
        for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
            logger.info("variant = {}", new String(rocksIterator.key()));
            VariantPolygenicScore varPgs = varPgsReader.readValue(rocksIterator.value());
            logger.info("variant PGS: {}", varPgs.toString());
            serializer.serialize(varPgs);
            counter++;
            if (counter % 10000 == 0) {
                logger.info("{} written", counter);
            }
        }
        serializer.close();
        logger.info("Done.");
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
        Options options = new Options().setCreateIfMissing(true);

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
}
