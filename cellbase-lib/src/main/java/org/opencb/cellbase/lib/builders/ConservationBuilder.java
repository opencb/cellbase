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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ConservationBuilder extends AbstractBuilder {

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
        outputFileNames = new HashMap<>();
    }

    @Override
    public void parse() throws IOException, CellBaseException {
        if (conservedRegionPath == null || !Files.exists(conservedRegionPath) || !Files.isDirectory(conservedRegionPath)) {
            throw new IOException("Conservation directory " + conservedRegionPath + " does not exist or it is not a directory or it cannot"
                    + " be read");
        }

        // Check GERP folder and files
        Path gerpPath = conservedRegionPath.resolve(GERP_DATA);
        DataSource dataSource = dataSourceReader.readValue(gerpPath.resolve(getDataVersionFilename(GERP_DATA)).toFile());
        List<File> gerpFiles = checkFiles(dataSource, gerpPath, getDataName(GERP_DATA));

        // Check PhastCons folder and files
        Path phastConsPath = conservedRegionPath.resolve(PHASTCONS_DATA);
        dataSource = dataSourceReader.readValue(phastConsPath.resolve(getDataVersionFilename(PHASTCONS_DATA)).toFile());
        List<File> phastConsFiles = checkFiles(dataSource, phastConsPath, getDataName(PHASTCONS_DATA));

        // Check PhyloP folder and files
        Path phylopPath = conservedRegionPath.resolve(PHYLOP_DATA);
        dataSource = dataSourceReader.readValue(phylopPath.resolve(getDataVersionFilename(PHYLOP_DATA)).toFile());
        List<File> phylopFiles = checkFiles(dataSource, phylopPath, getDataName(PHYLOP_DATA));

        // GERP is downloaded from Ensembl as a bigwig file. The library we have doesn't seem to parse
        // this file correctly, so we transform the file into a bedGraph format which is human-readable.
        if (gerpFiles.size() != 1) {
            throw new CellBaseException("Only one " + getDataName(GERP_DATA) + " file is expected, but currently there are "
                    + gerpFiles.size() + " files");
        }
        File bigwigFile = gerpFiles.get(0);
        File bedgraphFile = Paths.get(gerpFiles.get(0).getAbsolutePath() + ".bedgraph").toFile();
        String exec = "bigWigToBedGraph";
        if (!bedgraphFile.exists()) {
            try {
                if (isExecutableAvailable(exec)) {
                    EtlCommons.runCommandLineProcess(null, exec, Arrays.asList(bigwigFile.toString(), bedgraphFile.toString()), null);
                } else {
                    throw new CellBaseException(exec + " not found in your system, install it to build " + getDataName(GERP_DATA)
                            + ". It is available at http://hgdownload.cse.ucsc.edu/admin/exe/linux.x86_64/");
                }
            } catch (IOException e) {
                throw new CellBaseException("Error executing " + exec + " in BIGWIG file " + bigwigFile, e);
            } catch (InterruptedException e) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                throw new CellBaseException("" + e.getMessage(), e);
            }
            if (!bedgraphFile.exists()) {
                throw new CellBaseException("Something happened when executing " + exec + " in BIGWIG file " + bigwigFile + "; the BED"
                        + " graph file was not generated. Please, check " + exec);
            }
        }
        gerpParser(bedgraphFile.toPath());

        // UCSC phastCons and phylop are stored in the same format. They are processed together.
        Map<String, Path> files = new HashMap<>();
        String chromosome;
        Set<String> chromosomes = new HashSet<>();

        // Process PhastCons filenames
        for (File file : phastConsFiles) {
            chromosome = file.getName().split("\\.")[0].replace("chr", "");
            chromosomes.add(chromosome);
            files.put(chromosome + PHASTCONS_DATA, file.toPath());
        }

        // Process PhyloP filenames
        for (File file : phylopFiles) {
            chromosome = file.getName().split("\\.")[0].replace("chr", "");
            chromosomes.add(chromosome);
            files.put(chromosome + PHYLOP_DATA, file.toPath());
        }

        // Now we can iterate over all the chromosomes found and process the files
        logger.debug("Chromosomes found '{}'", chromosomes);
        for (String chr : chromosomes) {
            logger.debug("Processing chromosome '{}', file '{}'", chr, files.get(chr + PHASTCONS_DATA));
            processWigFixFile(files.get(chr + PHASTCONS_DATA), PHASTCONS_DATA);

            logger.debug("Processing chromosome '{}', file '{}'", chr, files.get(chr + PHYLOP_DATA));
            processWigFixFile(files.get(chr + PHYLOP_DATA), PHYLOP_DATA);
        }
    }

    private void gerpParser(Path gerpProcessFilePath) throws IOException, CellBaseException {
        logger.info(PARSING_LOG_MESSAGE, gerpProcessFilePath);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(gerpProcessFilePath)) {
            String line;
            int startOfBatch = 0;
            int previousEndValue = 0;
            String chromosome = null;
            String previousChromosomeValue = null;

            List<Float> conservationScores = new ArrayList<>(chunkSize);
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t");

                // Checking line
                if (fields.length != 4) {
                    throw new CellBaseException("Invalid " + getDataName(GERP_DATA) + " line (expecting 4 columns): " + fields.length
                            + " items: " + line);
                }

                chromosome = fields[0];

                // New chromosome, store batch
                if (previousChromosomeValue != null && !previousChromosomeValue.equals(chromosome)) {
                    storeScores(startOfBatch, previousChromosomeValue, conservationScores);

                    // Reset values for current batch
                    startOfBatch = 0;
                }

                // Reset chromosome for next entry
                previousChromosomeValue = chromosome;

                // File is american! starts at zero, add one
                int start = Integer.parseInt(fields[1]) + 1;
                // Inclusive
                int end = Integer.parseInt(fields[2]) + 1;

                // sSart coordinate for this batch of 2,000
                if (startOfBatch == 0) {
                    startOfBatch = start;
                    previousEndValue = 0;
                }

                // If there is a gap between the last entry and this one
                if (previousEndValue != 0 && (start - previousEndValue) != 0) {
                    // Gap is too big! store what we already have before processing more
                    if (start - previousEndValue >= chunkSize) {
                        // We have a full batch, store
                        storeScores(startOfBatch, chromosome, conservationScores);

                        // Reset batch to start at this record
                        startOfBatch = start;
                    } else {
                        // Fill in the gap with zeroes, don't overfill the batch
                        while (previousEndValue < start && conservationScores.size() < chunkSize) {
                            conservationScores.add((float) 0);
                            previousEndValue++;
                        }

                        // We have a full batch, store
                        if (conservationScores.size() == chunkSize) {
                            storeScores(startOfBatch, chromosome, conservationScores);

                            // Reset: start a new batch
                            startOfBatch = start;
                        }
                    }
                }

                // Reset value
                previousEndValue = end;

                // Score for these coordinates
                String score = fields[3];

                // Add the score for each coordinate included in the range start-end
                while (start < end) {
                    // We have a full batch: store
                    if (conservationScores.size() == chunkSize) {
                        storeScores(startOfBatch, chromosome, conservationScores);

                        // Reset: start a new batch
                        startOfBatch = start;
                    }

                    // Add score to batch
                    conservationScores.add(Float.valueOf(score));

                    // Increment coordinate
                    start++;
                }

                // We have a full batch: store
                if (conservationScores.size() == chunkSize) {
                    storeScores(startOfBatch, chromosome, conservationScores);

                    // Reset: start a new batch
                    startOfBatch = 0;
                }
            }
            // We need to serialize the last chunk that might be incomplete
            if (!conservationScores.isEmpty()) {
                storeScores(startOfBatch, chromosome, conservationScores);
            }
        }

        logger.info(PARSING_DONE_LOG_MESSAGE, gerpProcessFilePath);
    }

    private void storeScores(int startOfBatch, String chromosome, List<Float> conservationScores)
            throws CellBaseException {

        // If this is a small batch, fill in the missing coordinates with 0
        while (conservationScores.size() < chunkSize) {
            conservationScores.add((float) 0);
        }

        if (conservationScores.size() != chunkSize) {
            throw new CellBaseException("Invalid chunk size " + conservationScores.size() + " for " + chromosome + ":" + startOfBatch);
        }

        GenomicScoreRegion<Float> conservationScoreRegion = new GenomicScoreRegion<>(chromosome, startOfBatch,
                startOfBatch + conservationScores.size() - 1, GERP_DATA, conservationScores);
        fileSerializer.serialize(conservationScoreRegion, getOutputFileName(chromosome));

        // Reset
        conservationScores.clear();
    }

    private void processWigFixFile(Path inGzPath, String conservationSource) {
        logger.info(PARSING_LOG_MESSAGE, inGzPath);
        String line = null;
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(inGzPath)) {
            String chromosome = "";
            int start = 0;
            float value;
            Map<String, String> attributes = new HashMap<>();
            List<Float> values = new ArrayList<>();
            GenomicScoreRegion<Float> conservedRegion = null;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("fixedStep")) {
                    // New group, save last
                    if (conservedRegion != null) {
                        conservedRegion = new GenomicScoreRegion<>(chromosome, start, start + values.size() - 1,
                                conservationSource, values);
                        fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
                    }

                    attributes.clear();
                    String[] attrFields = line.split(" ");
                    String[] attrKeyValue;
                    for (String attrField : attrFields) {
                        if (!attrField.equalsIgnoreCase("fixedStep")) {
                            attrKeyValue = attrField.split("=");
                            attributes.put(attrKeyValue[0].toLowerCase(), attrKeyValue[1]);
                        }
                    }

                    chromosome = formatChromosome(attributes);
                    start = Integer.parseInt(attributes.get("start"));

                    values = new ArrayList<>(2000);
                } else {
                    int startChunk = start / MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
                    int endChunk = (start + values.size()) / MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
                    // This is the endChunk if current read score is appended to the array (otherwise it would be start + values.size()
                    // - 1). If this endChunk is different from the startChunk means that current conserved region must be dumped and
                    // current score must be associated to next chunk. Main difference to what there was before is that if the fixedStep
                    // starts on the last position of a chunk e.g. 1999, the chunk must be created with just that score - the chunk was
                    // left empty with the old code
                    if (startChunk != endChunk) {
                        conservedRegion = new GenomicScoreRegion<>(chromosome, start, start + values.size() - 1, conservationSource,
                                values);
                        fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
                        start = start + values.size();
                        values.clear();
                    }

                    try {
                        value = Float.parseFloat(line.trim());
                    } catch (NumberFormatException e) {
                        value = 0;
                        logger.warn("Invalid value: {}. Stack trace: {}", line, e.getStackTrace());
                    }
                    values.add(value);
                }
            }

            // Write last
            conservedRegion = new GenomicScoreRegion<>(chromosome, start, start + values.size() - 1, conservationSource, values);
            fileSerializer.serialize(conservedRegion, getOutputFileName(chromosome));
        } catch (Exception e) {
            logger.error("ERROR parsing {}. Line: {}. Stack trace: {}", inGzPath, line, e.getStackTrace());
        }
        logger.info(PARSING_DONE_LOG_MESSAGE, inGzPath);
    }

    private String getOutputFileName(String chromosome) {
        // phylop and phastcons list the chromosome as M instead of the standard MT. replace.
        if (chromosome.equals("M")) {
            chromosome = "MT";
        }

        String outputFileName;
        if (outputFileNames.containsKey(chromosome)) {
            outputFileName = outputFileNames.get(chromosome);
        } else {
            outputFileName = getFilename(CONSERVATION_DATA, chromosome);
            outputFileNames.put(chromosome, outputFileName);
        }
        return outputFileName;
    }

    /**
     * Remove chr from the chromosome name; and phylop and phastcons list the chromosome as M instead of the standard MT, replace it.
     *
     * @param attributes Attributes map with the chromosome name
     * @return The new chromosome name
     */
    private String formatChromosome(Map<String, String> attributes) {
        String chromosome = attributes.get("chrom").replace("chr", "");

        if (chromosome.equals("M")) {
            chromosome = "MT";
        }
        return chromosome;
    }
}
