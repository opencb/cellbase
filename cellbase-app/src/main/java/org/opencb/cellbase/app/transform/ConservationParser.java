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

import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConservationParser extends CellBaseParser {

    private static final int CHUNK_SIZE = 2000;

    private Logger logger;
    private Path conservedRegionPath;
    private int chunkSize;

    private CellBaseFileSerializer fileSerializer;
    private Map<String, String> outputFileNames;
    // Download data:
    // for i in `seq 1 22`;
    // do wget ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/phastCons46way/primates/chr$i.phastCons46way.primates.wigFix.gz;
    // done
    // ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/phyloP46way/primates/

    public ConservationParser(Path conservedRegionPath, CellBaseFileSerializer serializer) {
        this(conservedRegionPath, CHUNK_SIZE, serializer);
    }

    public ConservationParser(Path conservedRegionPath, int chunkSize, CellBaseFileSerializer serializer) {
        super(serializer);
        fileSerializer = serializer;
        this.conservedRegionPath = conservedRegionPath;
        this.chunkSize = chunkSize;
        logger = LoggerFactory.getLogger(ConservationParser.class);
        outputFileNames = new HashMap<>();
    }

    @Override
    public void parse() throws IOException {
        System.out.println("conservedRegionPath = " + conservedRegionPath.toString());
        if (conservedRegionPath == null || !Files.exists(conservedRegionPath) || !Files.isDirectory(conservedRegionPath)) {
            throw new IOException("Conservation directory whether does not exist, is not a directory or cannot be read");
        }

        /*
         * GERP is stored in a particular format
         */
        Path gerpFolderPath = conservedRegionPath.resolve("gerp");
        if (gerpFolderPath.toFile().exists()) {
            logger.debug("Parsing GERP data ...");
            gerpParser(gerpFolderPath);
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
        DirectoryStream<Path> pathDirectoryStream = Files.newDirectoryStream(gerpFolderPath, "*.rates");
        for (Path path : pathDirectoryStream) {
            logger.debug("Processing file '{}'", path.getFileName().toString());
            String[] chromosome = path.getFileName().toString().replaceFirst("chr", "").split("\\.");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(path))));
            String line;
            int start = 1;
            int end = 1999;
            int counter = 1;
            String[] fields;
            List<Float> val = new ArrayList<>(chunkSize);
            while ((line = bufferedReader.readLine()) != null) {
                fields = line.split("\t");
                val.add(Float.valueOf(fields[1]));
                counter++;
                if (counter == chunkSize) {
//                    ConservationScoreRegion conservationScoreRegion = new ConservationScoreRegion(chromosome[0], start, end, "gerp", val);
                    GenomicScoreRegion<Float> conservationScoreRegion =
                            new GenomicScoreRegion<>(chromosome[0], start, end, "gerp", val);
                    fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome[0]));

                    start = end + 1;
                    end += chunkSize;

                    counter = 0;
                    val.clear();
                }
            }

            // we need to serialize the last chunk that might be incomplete
//            ConservationScoreRegion conservationScoreRegion =
//                    new ConservationScoreRegion(chromosome[0], start, start + val.size() - 1, "gerp", val);
            GenomicScoreRegion<Float> conservationScoreRegion =
                    new GenomicScoreRegion<>(chromosome[0], start, start + val.size() - 1, "gerp", val);
            fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome[0]));

            bufferedReader.close();
        }
    }

    private void processWigFixFile(Path inGzPath, String conservationSource) throws IOException {
        BufferedReader bufferedReader = FileUtils.newBufferedReader(inGzPath);

        String line;
        String chromosome = "";
        int start = 0, end = 0;
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
                    conservedRegion.setEnd(end);
//                    conservedRegion = new ConservationScoreRegion(chromosome, start, end, conservationSource, values);
                    conservedRegion = new GenomicScoreRegion<>(chromosome, start, end, conservationSource, values);
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
                end = Integer.parseInt(attributes.get("start"));

                values = new ArrayList<>(2000);
            } else {
                int startChunk = start / CHUNK_SIZE;
                end++;
                int endChunk = end / CHUNK_SIZE;

                if (startChunk != endChunk) {
//                    conservedRegion = new ConservationScoreRegion(chromosome, start, end - 1, conservationSource, values);
                    conservedRegion = new GenomicScoreRegion<>(chromosome, start, end - 1, conservationSource, values);
                    fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
                    values.clear();
                    start = end;
                }

                value = Float.parseFloat(line.trim());
                values.add(value);
            }
        }
        //write last
//        conservedRegion = new ConservationScoreRegion(chromosome, start, end, conservationSource, values);
        conservedRegion = new GenomicScoreRegion<>(chromosome, start, end, conservationSource, values);
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
