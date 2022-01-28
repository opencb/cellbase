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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.core.SpliceScoreAlternate;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SpliceBuilder extends CellBaseBuilder {

    private Path spliceDir;
    private CellBaseFileSerializer fileSerializer;

    public SpliceBuilder(Path spliceDir, CellBaseFileSerializer serializer) {
        super(serializer);

        this.fileSerializer = serializer;
        this.spliceDir = spliceDir;

        logger = LoggerFactory.getLogger(SpliceBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        // Check input folder
        FileUtils.checkPath(spliceDir);

        logger.info("Parsing splice files...");

        Path splicePath = spliceDir.resolve(EtlCommons.MMSPLICE_SUBDIRECTORY);
        if (splicePath.toFile().exists()) {
            logger.info("Parsing MMSplice data...");
            mmspliceParser(splicePath);
        } else {
            logger.debug("MMSplice data not found: " + splicePath);
        }
        splicePath = spliceDir.resolve(EtlCommons.SPLICEAI_SUBDIRECTORY);
        if (splicePath.toFile().exists()) {
            logger.info("Parsing SpliceAI data...");
            spliceaiParser(splicePath);
        } else {
            logger.debug("SpliceAI data not found: " + splicePath);
        }

        logger.info("Parsing splice scores finished.");
    }

    /**
     * Parse MMSplice files containing the raw splice scores in order to generate a JSON file for each chromosome where each line is
     * spllice score object (SpliceScore data model).
     *
     * @param mmsplicePath  Path where MMSplice files are located
     * @throws IOException  Exception
     */
    private void mmspliceParser(Path mmsplicePath) throws IOException {
        // Check output folder: MMSplice
        Path mmspliceOutFolder = fileSerializer.getOutdir().resolve(EtlCommons.MMSPLICE_SUBDIRECTORY);
        if (!mmspliceOutFolder.toFile().exists()) {
            mmspliceOutFolder.toFile().mkdirs();
        }

        // RocksDB to avoid duplicated data
        File rocksDBFile = new File("/tmp/mmsplice.rocksdb");
        if (rocksDBFile.exists()) {
            org.apache.commons.io.FileUtils.deleteDirectory(rocksDBFile);
        }
        RocksDbManager rocksDbManager = new RocksDbManager();
        RocksDB rocksDB = rocksDbManager.getDBConnection(rocksDBFile.getAbsolutePath());

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader objectReader = mapper.readerFor(SpliceScore.class);
        ObjectWriter objectWriter = mapper.writerFor(SpliceScore.class);

        long count = 0;
        for (File file : mmsplicePath.toFile().listFiles()) {
            logger.info("Parsing MMSplice file {} ...", file.getName());
            try (BufferedReader in = FileUtils.newBufferedReader(file.toPath())) {
                String line = in.readLine();
                if (line != null) {
                    String[] labels = line.split(",");

                    SpliceScore spliceScore = null;

                    // Main loop
                    while ((line = in.readLine()) != null) {
                        String[] fields = line.split(",");
                        String[] idFields = fields[0].split(":");

                        String chrom = idFields[0];
                        int position = Integer.parseInt(idFields[1]);
                        String ref = idFields[2];
                        String alt = idFields[3].replace("'", "").replace("[", "").replace("]", "");

                        // Normalize variant
                        Variant variant = new Variant(chrom, position, ref, alt);
                        VariantNormalizer normalizer = new VariantNormalizer();
                        Variant normVariant = normalizer.apply(variant).get(0);
//                    System.out.println(variant.toStringSimple() + " -> norm: " + normVariant.toStringSimple());
                        ref = normVariant.getReference().isEmpty() ? "-" : normVariant.getReference();
                        alt = normVariant.getAlternate().isEmpty() ? "-" : normVariant.getAlternate();

                        // Transcript
                        String transcript = fields[5];

                        // Check for duplicated lines
                        String uid = normVariant.getChromosome() + ":" + normVariant.getStart() + ":" + ref + ":" + transcript;
                        if (rocksDB.get(uid.getBytes()) != null) {
                            spliceScore = objectReader.readValue(rocksDB.get(uid.getBytes()));
                        } else {
                            spliceScore = new SpliceScore();
                            spliceScore.setChromosome(normVariant.getChromosome());
                            spliceScore.setPosition(normVariant.getStart());
                            spliceScore.setRefAllele(ref);
                            spliceScore.setGeneId(fields[3]);
                            spliceScore.setGeneName(fields[4]);
                            spliceScore.setTranscriptId(transcript);
                            spliceScore.setExonId(fields[2]);
                            spliceScore.setSource("MMSplice");
                            spliceScore.setAlternates(new ArrayList<>());
                        }

                        // Ignore duplications
                        if (!existsSpliceScoreAlternate(alt, spliceScore)) {
                            // Create splice score alternate
                            SpliceScoreAlternate scoreAlt = new SpliceScoreAlternate(alt, new HashMap<>());
                            for (int i = 6; i < labels.length; i++) {
                                scoreAlt.getScores().put(labels[i], Double.parseDouble(fields[i]));
                            }

                            // Add splice score alternate to the splice score and add it to the rocksDB
                            spliceScore.getAlternates().add(scoreAlt);
                            rocksDB.put(uid.getBytes(), objectWriter.writeValueAsBytes(spliceScore));

                            // Progress
                            if (++count % 1000000 == 0) {
                                logger.info("Processing " + count + " scores from file " + file.getName() + "; "
                                        + normVariant.getChromosome() + ":" + normVariant.getStart());
                            }
                        }
                    }
                }
            } catch (IOException | RocksDBException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }

        // Dump rocksDB to JSON file
        dumpRocksDB(EtlCommons.MMSPLICE_SUBDIRECTORY + "/splice_score_mmsplice_chr", rocksDB);

        // Clean up
        rocksDB.close();
        if (rocksDBFile.exists()) {
            org.apache.commons.io.FileUtils.deleteDirectory(rocksDBFile);
        }
    }

    /**
     * Parse SpliceAI VCF files containing the raw splice scores in order to generate a JSON file for each chromosome where each line is
     * spllice score object (SpliceScore data model).
     *
     * @param spliceaiPath  Path where SpliceAI VCF files are located
     * @throws IOException  Exception
     */
    private void spliceaiParser(Path spliceaiPath) throws IOException {
        // Check output folder: MMSplice
        Path spliceaiOutFolder = fileSerializer.getOutdir().resolve(EtlCommons.SPLICEAI_SUBDIRECTORY);
        if (!spliceaiOutFolder.toFile().exists()) {
            spliceaiOutFolder.toFile().mkdirs();
        }

        // RocksDB to avoid duplicated data
        File rocksDBFile = new File("/tmp/spliceai.rocksdb");
        if (rocksDBFile.exists()) {
            org.apache.commons.io.FileUtils.deleteDirectory(rocksDBFile);
        }
        RocksDbManager rocksDbManager = new RocksDbManager();
        RocksDB rocksDB = rocksDbManager.getDBConnection(rocksDBFile.getAbsolutePath());

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader objectReader = mapper.readerFor(SpliceScore.class);
        ObjectWriter objectWriter = mapper.writerFor(SpliceScore.class);

        long count = 0;
        for (File file : spliceaiPath.toFile().listFiles()) {
            logger.info("Parsing SpliceAI VCF file {} ...", file.getName());
            try (BufferedReader in = FileUtils.newBufferedReader(file.toPath())) {
                String line;
                SpliceScore spliceScore = null;

                // Main loop
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }

                    String[] fields = line.split("\t");
                    // Sanity check
                    if (fields.length < 8) {
                        logger.error("Skipping line because of missing values: " + line);
                        continue;
                    }

                    Variant variant = new Variant(fields[0], Integer.parseInt(fields[1]), fields[3], fields[4]);
                    VariantNormalizer normalizer = new VariantNormalizer();
                    Variant normVariant = normalizer.apply(variant).get(0);
//                    System.out.println(variant.toStringSimple() + " -> norm: " + normVariant.toStringSimple());
                    String ref = normVariant.getReference().isEmpty() ? "-" : normVariant.getReference();
                    String alt = normVariant.getAlternate().isEmpty() ? "-" : normVariant.getAlternate();


                    // Create alternate splice score to be added further
                    String info = fields[7];
                    fields = info.split("\\|");

                    // Gene
                    String geneName = fields[1];

                    // Check for duplicated lines
                    String uid = normVariant.getChromosome() + ":" + normVariant.getStart() + ":" + ref + ":" + geneName;
                    if (rocksDB.get(uid.getBytes()) != null) {
                        spliceScore = objectReader.readValue(rocksDB.get(uid.getBytes()));
                    } else {
                        spliceScore = new SpliceScore();
                        spliceScore.setChromosome(normVariant.getChromosome());
                        spliceScore.setPosition(normVariant.getStart());
                        spliceScore.setRefAllele(ref);
                        spliceScore.setGeneName(geneName);
                        spliceScore.setSource("SpliceAI");
                        spliceScore.setAlternates(new ArrayList<>());
                    }

                    // Ignore duplications
                    if (!existsSpliceScoreAlternate(alt, spliceScore)) {
                        // Create splice score alternate
                        SpliceScoreAlternate scoreAlt = new SpliceScoreAlternate(alt, new HashMap<>());
                        scoreAlt.getScores().put("DS_AG", Double.parseDouble(fields[2]));
                        scoreAlt.getScores().put("DS_AL", Double.parseDouble(fields[3]));
                        scoreAlt.getScores().put("DS_DG", Double.parseDouble(fields[4]));
                        scoreAlt.getScores().put("DS_DL", Double.parseDouble(fields[5]));
                        scoreAlt.getScores().put("DP_AG", Double.parseDouble(fields[6]));
                        scoreAlt.getScores().put("DP_AL", Double.parseDouble(fields[7]));
                        scoreAlt.getScores().put("DP_DG", Double.parseDouble(fields[8]));
                        scoreAlt.getScores().put("DP_DL", Double.parseDouble(fields[9]));

                        // Add splice score alternate to the splice score and add it to the rocksDB
                        spliceScore.getAlternates().add(scoreAlt);
                        rocksDB.put(uid.getBytes(), objectWriter.writeValueAsBytes(spliceScore));

                        // Progress
                        if (++count % 1000000 == 0) {
                            logger.info("Processing " + count + " scores from file " + file.getName() + "; " + normVariant.getChromosome()
                                    + ":" + normVariant.getStart());
                        }
                    }
                }
            } catch (IOException | RocksDBException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }

        // Dump rocksDB to JSON file
        dumpRocksDB(EtlCommons.SPLICEAI_SUBDIRECTORY + "/splice_score_spliceai_chr", rocksDB);

        // Clean up
        rocksDB.close();
        if (rocksDBFile.exists()) {
            org.apache.commons.io.FileUtils.deleteDirectory(rocksDBFile);
        }
    }

    private boolean existsSpliceScoreAlternate(String alt, SpliceScore spliceScore) {
        for (SpliceScoreAlternate spliceScoreAlt : spliceScore.getAlternates()) {
            if (alt.equals(spliceScoreAlt.getAltAllele())) {
                return true;
            }
        }
        return false;
    }

    private void dumpRocksDB(String baseFilename, RocksDB rocksDB) throws IOException {
        logger.info("Writing output files (JSON format)");

        ObjectMapper mapper = new ObjectMapper();
        ObjectReader objectReader = mapper.readerFor(SpliceScore.class);

        RocksIterator rocksIterator = rocksDB.newIterator();
        rocksIterator.seekToFirst();
        while (rocksIterator.isValid()) {
            SpliceScore spliceScore = objectReader.readValue(rocksIterator.value());
            fileSerializer.serialize(spliceScore, baseFilename + spliceScore.getChromosome());
            rocksIterator.next();
        }
    }
}
