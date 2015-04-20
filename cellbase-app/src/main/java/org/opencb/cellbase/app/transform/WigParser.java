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

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.app.transform.formats.ConservedRegionFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author lcruz
 * @since 03/11/2014
 */
public class WigParser extends CellBaseParser{
    private final Path wigsFolder;
    private int chunkSize;
    private String type;
    private Pattern pattern;

    public WigParser(Path conservationFilesFolder, int chunkSize, String type, CellBaseSerializer serializer){
        super(serializer);
        this.wigsFolder = conservationFilesFolder;
        this.chunkSize = chunkSize;
        this.type = type;

        String regEx = ".*chrom=chr(.*) start=(.*) step=(.*)";
        pattern = Pattern.compile(regEx);
    }

    public void parse() throws Exception {
        if (Files.exists(wigsFolder)) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(wigsFolder.resolve(type));

            Map<String, Path> files = new HashMap<>();
            String chromosome;
            Set<String> chromosomes = new HashSet<>();

            // Reading all files in conservation folder
            for (Path wig_file : directoryStream) {
                chromosome = wig_file.getFileName().toString().split("\\.")[0].replace("chr", "");
                chromosomes.add(chromosome);
                files.put(chromosome + type, wig_file);
            }

            logger.debug("Chromosomes found {}", chromosomes.toString());
            for (String chr : chromosomes) {
                logger.debug("Processing chromosome {}, file {}", chr, files.get(chr + type));
                processFile(files.get(chr + type));
            }
        }
    }

    private void processFile(Path inputFilePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(inputFilePath))));
        String line;
        Integer position = null;
        Integer previous_chunk = -1;
        String chunk_chr = "";

        // Build the first object
        ConservedRegionFeature newConservedRegion = null;

        while ((line = br.readLine()) != null) if (line.startsWith("fixedStep")) {
            // set position to one before of the start position
            position = setNewPositionAfterJump(line);
            // set chromosome of the chunks, it will change if there is more than one chromosome in the same file
            chunk_chr = setChromosomeAfterJump(line);
        } else {
            // set the new position
            position++;
            // set chunk id
            Integer position_chunk = (position / chunkSize);
            // if position belong to a new chunk print the position before and initialize a new one
            if (!Objects.equals(previous_chunk, position_chunk)) {
                previous_chunk = position_chunk;
                if (newConservedRegion != null) {
                    serializer.serialize(newConservedRegion);
                }

                //Initialize a new chunk
                Integer start = (position / chunkSize) * chunkSize;
                Integer end = start + chunkSize - 1;
                newConservedRegion = new ConservedRegionFeature(chunk_chr, start, end, position_chunk);

                Float[] values = new Float[chunkSize];
                newConservedRegion.addSource(type, Arrays.asList(values));

            }
            if (newConservedRegion != null) {
                newConservedRegion.getSources().get(0).getValues().set(position % chunkSize, Float.parseFloat(line.trim()));
            }

        }

        serializer.serialize(newConservedRegion);
        br.close();
    }

    private String setChromosomeAfterJump(String line) {
        Matcher matcher = pattern.matcher(line);
        matcher.matches();
        return matcher.group(1);
    }

    private Integer setNewPositionAfterJump(String line) {
        Matcher matcher = pattern.matcher(line);
        matcher.matches();
        return Integer.parseInt(matcher.group(2))-1;
    }
}