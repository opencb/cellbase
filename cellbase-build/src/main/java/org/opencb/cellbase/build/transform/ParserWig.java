package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.transform.formats.ConservedRegionFeature;

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
public class ParserWig extends CellBaseParser{
    private final Path wigs_folder;
    private int chunksize;
    private String type;
    private Float[] arrayValues;
    private Integer lastInsertedChunk;
    private String regEx;
    Pattern pattern;

    public ParserWig(CellBaseSerializer serializer, Path ConsFolder, int chunksize, String type){
        super(serializer);
        this.wigs_folder = ConsFolder;
        this.chunksize = chunksize;
        this.type = type;

        regEx = ".*chrom=chr(.*) start=(.*) step=(.*)";
        pattern = Pattern.compile(regEx);
    }

    public void parse() throws Exception {
        if (Files.exists(wigs_folder)) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(wigs_folder.resolve(type));

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
                lastInsertedChunk = null;
                arrayValues = new Float[chunksize];
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
            position = SetNewPositionAfterJump(line);
            // set chromosome of the chunks, it will change if there is more than one chromosome in the same file
            chunk_chr = SetChromosomeAfterJump(line);
        } else {
            // set the new position
            position++;
            // set chunk id
            Integer position_chunk = (position / chunksize);
            // if position belong to a new chunk print the position before and initialize a new one
            if (!Objects.equals(previous_chunk, position_chunk)) {
                previous_chunk = position_chunk;
                if (newConservedRegion != null) {
                    serialize(newConservedRegion);
                }

                //Initialize a new chunk
                Integer start = (position / chunksize) * chunksize;
                Integer end = start + chunksize - 1;
                newConservedRegion = new ConservedRegionFeature(chunk_chr, start, end, position_chunk);

                Float[] values = new Float[chunksize];
                newConservedRegion.addSource(type, Arrays.asList(values));

            }
            if (newConservedRegion != null) {
                newConservedRegion.getSources().get(0).getValues().set(position % chunksize, Float.parseFloat(line.trim()));
            }

        }

        serialize(newConservedRegion);
        br.close();
        this.disconnect();
    }

    private String SetChromosomeAfterJump(String line) {
        Matcher matcher = pattern.matcher(line);
        matcher.matches();
        return matcher.group(1);
    }

    private Integer SetNewPositionAfterJump(String line) {
        Matcher matcher = pattern.matcher(line);
        matcher.matches();
        return Integer.parseInt(matcher.group(2))-1;
    }
}