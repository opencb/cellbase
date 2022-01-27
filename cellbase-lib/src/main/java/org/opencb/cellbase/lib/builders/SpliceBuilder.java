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

import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.core.SpliceScoreAlternate;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class SpliceBuilder extends CellBaseBuilder {

    private CellBaseFileSerializer fileSerializer;
    private Path spliceDir;

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
//        if (splicePath.toFile().exists()) {
//            logger.info("Parsing MMSplice data...");
//            mmspliceParser(splicePath);
//        } else {
//            logger.debug("MMSplice data not found: " + splicePath.toString());
//        }
        splicePath = spliceDir.resolve(EtlCommons.SPLICEAI_SUBDIRECTORY);
        if (splicePath.toFile().exists()) {
            logger.info("Parsing SpliceAI data...");
            spliceaiParser(splicePath);
        } else {
            logger.debug("SpliceAI data not found: " + splicePath.toString());
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

        for (File file : mmsplicePath.toFile().listFiles()) {
            logger.info("Parsing MMSplice file {} ...", file.getName());
            try (BufferedReader in = FileUtils.newBufferedReader(file.toPath())) {
                String line = in.readLine();
                long count = 0;
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

                        // Transcript
                        String transcript = fields[5];

                        // Check for duplicated lines
                        String uid = chrom + ":" + position + ":" + ref + ":" + alt + ":" + transcript;
                        if (rocksDB.get(uid.getBytes()) != null) {
                            continue;
                        }
                        rocksDB.put(uid.getBytes(), "1".getBytes());

                        // Create alternate splice score to be added further
                        SpliceScoreAlternate scoreAlt = new SpliceScoreAlternate(alt, new HashMap<>());
                        for (int i = 6; i < labels.length; i++) {
                            scoreAlt.getScores().put(labels[i], Double.parseDouble(fields[i]));
                        }

                        // Create splice score
                        if (spliceScore != null
                                && spliceScore.getChromosome().equals(chrom)
                                && spliceScore.getPosition() == position
                                && spliceScore.getRefAllele().equals(ref)
                                && spliceScore.getTranscriptId().equals(transcript)) {
                            spliceScore.getAlternates().add(scoreAlt);
                        } else {
                            ++count;
                            if (count % 500000 == 0) {
                                logger.info("Processing " + count + " positions from file " + file.getName());
                            }

                            if (spliceScore != null) {
                                // Write the currant splice score
                                fileSerializer.serialize(spliceScore, EtlCommons.MMSPLICE_SUBDIRECTORY + "/splice_score_mmsplice_chr"
                                        + spliceScore.getChromosome());
                            }

                            // And prepare the new splice score
                            spliceScore = new SpliceScore();
                            spliceScore.setChromosome(chrom);
                            spliceScore.setPosition(position);
                            spliceScore.setRefAllele(ref);
                            spliceScore.setGeneId(fields[3]);
                            spliceScore.setGeneName(fields[4]);
                            spliceScore.setTranscriptId(transcript);
                            spliceScore.setExonId(fields[2]);
                            spliceScore.setSource("MMSplice");
                            spliceScore.setAlternates(new ArrayList<>());

                            spliceScore.getAlternates().add(scoreAlt);
                        }
                    }

                    if (spliceScore != null) {
                        // Write the last splice score
                        fileSerializer.serialize(spliceScore, EtlCommons.MMSPLICE_SUBDIRECTORY + "/splice_score_mmsplice_chr"
                                + spliceScore.getChromosome());
                    }
                }
            } catch (IOException | RocksDBException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
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
     * @param mmsplicePath  Path where SpliceAI VCF files are located
     * @throws IOException  Exception
     */
    private void spliceaiParser(Path mmsplicePath) throws IOException {
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

        long count = 0;
        for (File file : mmsplicePath.toFile().listFiles()) {
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

                    String chrom = fields[0];
                    int position = Integer.parseInt(fields[1]);
                    String ref = fields[3];
                    String alt = fields[4];

                    // Create alternate splice score to be added further
                    String info = fields[7];
                    fields = info.split("\\|");

                    // Gene
                    String geneName = fields[1];

                    // Check for duplicated lines
                    String uid = chrom + ":" + position + ":" + ref + ":" + alt + ":" + geneName;
                    if (rocksDB.get(uid.getBytes()) != null) {
                        continue;
                    }
                    rocksDB.put(uid.getBytes(), "1".getBytes());

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

                    // Create splice score
                    if (spliceScore != null
                            && spliceScore.getChromosome().equals(chrom)
                            && spliceScore.getPosition() == position
                            && spliceScore.getRefAllele().equals(ref)
                            && spliceScore.getGeneName().equals(geneName)) {
                        spliceScore.getAlternates().add(scoreAlt);
                    } else {
                        ++count;
                        if (count % 500000 == 0) {
                            logger.info("Processing " + count + " positions from file " + file.getName());
                        }

                        if (spliceScore != null) {
                            // Write the currant splice score
                            fileSerializer.serialize(spliceScore, EtlCommons.SPLICEAI_SUBDIRECTORY + "/splice_score_spliceai_chr"
                                    + spliceScore.getChromosome());
                        }

                        // And prepare the new splice score
                        spliceScore = new SpliceScore();
                        spliceScore.setChromosome(chrom);
                        spliceScore.setPosition(position);
                        spliceScore.setRefAllele(ref);
//                        spliceScore.setGeneId(fields[3]);
                        spliceScore.setGeneName(geneName);
//                        spliceScore.setTranscriptId(transcript);
//                        spliceScore.setExonId(fields[2]);
                        spliceScore.setSource("SpliceAI");
                        spliceScore.setAlternates(new ArrayList<>());

                        spliceScore.getAlternates().add(scoreAlt);
                    }
                }

                if (spliceScore != null) {
                    // Write the last splice score
                    fileSerializer.serialize(spliceScore, EtlCommons.MMSPLICE_SUBDIRECTORY + "/splice_score_spliceai_chr"
                            + spliceScore.getChromosome());
                }
            } catch (IOException | RocksDBException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
        // Clean up
        rocksDB.close();
        if (rocksDBFile.exists()) {
            org.apache.commons.io.FileUtils.deleteDirectory(rocksDBFile);
        }
    }
}
