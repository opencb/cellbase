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

import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConservationBuilder extends CellBaseBuilder {

    private Logger logger;
    private Path conservedRegionPath;
    private int chunkSize;

    private CellBaseFileSerializer fileSerializer;
    private Map<String, String> outputFileNames;

    public ConservationBuilder(Path conservedRegionPath, CellBaseFileSerializer serializer) {
        this(conservedRegionPath, MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE, serializer);
    }

    public ConservationBuilder(Path conservedRegionPath, int chunkSize, CellBaseFileSerializer serializer) {
        super(serializer);
        fileSerializer = serializer;
        this.conservedRegionPath = conservedRegionPath;
        this.chunkSize = chunkSize;
        logger = LoggerFactory.getLogger(ConservationBuilder.class);
        outputFileNames = new HashMap<>();
    }

    @Override
    public void parse() throws IOException {
        System.out.println("conservedRegionPath = " + conservedRegionPath.toString());
        if (conservedRegionPath == null || !Files.exists(conservedRegionPath) || !Files.isDirectory(conservedRegionPath)) {
            throw new IOException("Conservation directory does not exist, is not a directory or cannot be read");
        }

        /*
         * GERP is downloaded from Ensembl as a bigwig file. The library we have doesn't seem to parse
         * this file correctly, so we transform the file into a bedGraph format which is human readable.
         */
        Path gerpFolderPath = conservedRegionPath.resolve(EtlCommons.GERP_SUBDIRECTORY);
        if (gerpFolderPath.toFile().exists()) {
            logger.debug("Parsing GERP data ...");
            gerpParser(gerpFolderPath);
        } else {
            logger.debug("GERP data not found: " + gerpFolderPath.toString());
        }

        /*
         * UCSC phastCons and phylop are stored in the same format. They are processed together.
         */
        Map<String, Path> files = new HashMap<>();
        String chromosome;
        Set<String> chromosomes = new HashSet<>();

        // Reading all files in phastCons folder
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(conservedRegionPath.resolve("phastCons"), "*.wigFix.gz");
        for (Path path : directoryStream) {
            chromosome = path.getFileName().toString().split("\\.")[0].replace("chr", "");
            chromosomes.add(chromosome);
            files.put(chromosome + "phastCons", path);
        }

        // Reading all files in phylop folder
        directoryStream = Files.newDirectoryStream(conservedRegionPath.resolve("phylop"), "*.wigFix.gz");
        for (Path path : directoryStream) {
            chromosome = path.getFileName().toString().split("\\.")[0].replace("chr", "");
            chromosomes.add(chromosome);
            files.put(chromosome + "phylop", path);
        }

        /*
         * Now we can iterate over all the chromosomes found and process the files
         */
        logger.debug("Chromosomes found '{}'", chromosomes.toString());
        for (String chr : chromosomes) {
            logger.debug("Processing chromosome '{}', file '{}'", chr, files.get(chr + "phastCons"));
            processWigFixFile(files.get(chr + "phastCons"), "phastCons");

            logger.debug("Processing chromosome '{}', file '{}'", chr, files.get(chr + "phylop"));
            processWigFixFile(files.get(chr + "phylop"), "phylop");
        }
    }

    private void gerpParser(Path gerpFolderPath) throws IOException {
        Path gerpProcessFilePath = gerpFolderPath.resolve(EtlCommons.GERP_PROCESSED_FILE);
        logger.info("parsing {}", gerpProcessFilePath);
        BufferedReader bufferedReader = FileUtils.newBufferedReader(gerpProcessFilePath);
        final String dataSource = "gerp";
        String line;
        int counter = 1;
        int startOfBatch = -1;
        String chromosome = "-1";
        List<Float> conservationScores = new ArrayList<>(chunkSize);
        while ((line = bufferedReader.readLine()) != null) {
            String[] fields = line.split("\t");

            // file is wrong. throw an exception instead?
            if (fields.length != 4) {
                logger.error("skipping invalid line: " + line);
                continue;
            }

            chromosome = fields[0];

            // start coordinate for this batch
            if (startOfBatch == -1) {
                // file is american! starts at zero
                startOfBatch = Integer.parseInt(fields[1]) + 1;
            }

            // add score to our collection for this batch
            String score = fields[3];
            conservationScores.add(Float.valueOf(score));

            // keep track of how many in batch
            counter++;

            if (counter == chunkSize) {
                GenomicScoreRegion<Float> conservationScoreRegion = new GenomicScoreRegion(chromosome, startOfBatch,
                        startOfBatch + conservationScores.size(), dataSource, conservationScores);
                fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome));

                // reset everything
                counter = 0;
                startOfBatch = -1;
                conservationScores.clear();
            }
        }
        // we need to serialize the last chunk that might be incomplete
        GenomicScoreRegion<Float> conservationScoreRegion = new GenomicScoreRegion(chromosome, startOfBatch,
                startOfBatch + conservationScores.size() - 1, dataSource, conservationScores);
        fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome));

        bufferedReader.close();
    }

//    @Deprecated
//    private void gerpParser(Path gerpFolderPath) throws IOException, InterruptedException {
//        logger.info("Uncompressing {}", gerpFolderPath.resolve(EtlCommons.GERP_FILE));
//        List<String> tarArgs = Arrays.asList("-xvzf", gerpFolderPath.resolve(EtlCommons.GERP_FILE).toString(),
//                "--overwrite", "-C", gerpFolderPath.toString());
//        EtlCommons.runCommandLineProcess(null, "tar", tarArgs, null);
//
//        DirectoryStream<Path> pathDirectoryStream = Files.newDirectoryStream(gerpFolderPath, "*.rates");
//        boolean filesFound = false;
//        for (Path path : pathDirectoryStream) {
//            filesFound = true;
//            logger.info("Processing file '{}'", path.getFileName().toString());
//            String[] chromosome = path.getFileName().toString().replaceFirst("chr", "").split("\\.");
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(path))));
//            String line;
//            int start = 1;
//            int end = 1999;
//            int counter = 1;
//            String[] fields;
//            List<Float> val = new ArrayList<>(chunkSize);
//            while ((line = bufferedReader.readLine()) != null) {
//                fields = line.split("\t");
//                val.add(Float.valueOf(fields[1]));
//                counter++;
//                if (counter == chunkSize) {
////                    ConservationScoreRegion conservationScoreRegion = new ConservationScoreRegion(chromosome[0], start, end, "gerp",
// val);
//                    GenomicScoreRegion<Float> conservationScoreRegion =
//                            new GenomicScoreRegion<>(chromosome[0], start, end, "gerp", val);
//                    fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome[0]));
//
//                    start = end + 1;
//                    end += chunkSize;
//
//                    counter = 0;
//                    val.clear();
//                }
//            }
//
//            // we need to serialize the last chunk that might be incomplete
////            ConservationScoreRegion conservationScoreRegion =
////                    new ConservationScoreRegion(chromosome[0], start, start + val.size() - 1, "gerp", val);
//            GenomicScoreRegion<Float> conservationScoreRegion =
//                    new GenomicScoreRegion<>(chromosome[0], start, start + val.size() - 1, "gerp", val);
//            fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome[0]));
//
//            bufferedReader.close();
//        }
//
//        if (!filesFound) {
//            logger.warn("No GERP++ files were found. Please check that the original file {} is there, that it was"
//                    + " properly decompressed and that the *.rates files are present",
//                    gerpFolderPath.resolve(EtlCommons.GERP_FILE));
//        }
//    }

    private void processWigFixFile(Path inGzPath, String conservationSource) throws IOException {
        BufferedReader bufferedReader = FileUtils.newBufferedReader(inGzPath);

        String line;
        String chromosome = "";
//        int start = 0, end = 0;
        int start = 0;
        float value;
        Map<String, String> attributes = new HashMap<>();
//        ConservedRegion conservedRegion =  null;
        List<Float> values = new ArrayList<>();
//        ConservationScoreRegion conservedRegion = null;
        GenomicScoreRegion<Float> conservedRegion = null;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("fixedStep")) {
                //new group, save last
                if (conservedRegion != null) {
//                    conservedRegion.setEnd(end);
//                    conservedRegion = new ConservationScoreRegion(chromosome, start, end, conservationSource, values);
                    conservedRegion = new GenomicScoreRegion<>(chromosome, start, start + values.size() - 1,
                            conservationSource, values);
                    fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
                }

//                offset = 0;
                attributes.clear();
                String[] attrFields = line.split(" ");
                String[] attrKeyValue;
                for (String attrField : attrFields) {
                    if (!attrField.equalsIgnoreCase("fixedStep")) {
                        attrKeyValue = attrField.split("=");
                        attributes.put(attrKeyValue[0].toLowerCase(), attrKeyValue[1]);
                    }
                }
                chromosome = attributes.get("chrom").replace("chr", "");
                start = Integer.parseInt(attributes.get("start"));
//                end = Integer.parseInt(attributes.get("start"));

                values = new ArrayList<>(2000);
            } else {
                int startChunk = start / MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
//                end++;
                int endChunk = (start + values.size()) / MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
                // This is the endChunk if current read score is
                // appended to the array (otherwise it would be
                // start + values.size() - 1). If this endChunk is
                // different from the startChunk means that current
                // conserved region must be dumped and current
                // score must be associated to next chunk. Main
                // difference to what there was before is that if
                // the fixedStep starts on the last position of a
                // chunk e.g. 1999, the chunk must be created with
                // just that score - the chunk was left empty with
                // the old code
                if (startChunk != endChunk) {
//                    conservedRegion = new ConservationScoreRegion(chromosome, start, end - 1, conservationSource, values);
                    conservedRegion = new GenomicScoreRegion<>(chromosome, start, start + values.size() - 1,
                            conservationSource, values);
                    fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
                    start = start + values.size();
                    values.clear();
                }

                value = Float.parseFloat(line.trim());
                values.add(value);
            }
        }
        //write last
//        conservedRegion = new ConservationScoreRegion(chromosome, start, end, conservationSource, values);
        conservedRegion = new GenomicScoreRegion<>(chromosome, start, start + values.size() - 1, conservationSource,
                values);
        fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
        bufferedReader.close();
    }

    private String getOutputFileName(String chromosome) {
        String outputFileName = outputFileNames.get(chromosome);
        if (outputFileName == null) {
            outputFileName = "conservation_" + chromosome;
            outputFileNames.put(chromosome, outputFileName);
        }
        return outputFileName;
    }
}
