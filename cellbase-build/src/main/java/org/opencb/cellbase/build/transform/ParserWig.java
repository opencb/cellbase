package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.feature.ConservedRegionFeature;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

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
    private final Path phylopFolder;
    private int chunksize;
    private String type;
    private Float[] arrayValues;
    private Integer lastInsertedChunk;
    private String regEx;
    Pattern pattern;

    public ParserWig(CellBaseSerializer serializer, Path phylopFolder, int chunksize, String type){
        super(serializer);
        this.phylopFolder = phylopFolder;
        this.chunksize = chunksize;
        this.type = type;

        regEx = new String(".*chrom=chr(.*) start=(.*) step=(.*)");
        pattern = Pattern.compile(regEx);
    }

    public void parse() throws Exception {
        if (Files.exists(phylopFolder)) {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(phylopFolder.resolve(type));

            Map<String, Path> files = new HashMap<>();
            String chromosome;
            Set<String> chromosomes = new HashSet<>();

            // Reading all files in phylop folder
            for (Path phylopFile : directoryStream) {
                chromosome = phylopFile.getFileName().toString().split("\\.")[0].replace("chr", "");
                chromosomes.add(chromosome);
                files.put(chromosome + type, phylopFile);
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
        String line = br.readLine();
        Integer positionNumber = null, auxPositionNumber = null;

        // Build the first object
        ConservedRegionFeature currentConservedRegion =  buildConservedRegionFeature(line, positionNumber);
        positionNumber = Integer.parseInt(line.split("start=")[1].split(" ")[0]);
        ConservedRegionFeature newConservedRegion = null;
        boolean printedLastChunk = true;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("fixedStep")) {
                newConservedRegion = buildConservedRegionFeature(line, auxPositionNumber);
                Integer currentChunk = calculateChunk(newConservedRegion.getStart());

                if(lastInsertedChunk.compareTo(currentChunk) != 0){
                    currentConservedRegion = serializeAndChangeCurrentChunk(currentChunk, currentConservedRegion,
                            newConservedRegion);
                    printedLastChunk = true;
                }
            } else {
                printedLastChunk = false;

                if(positionNumber > currentConservedRegion.getEnd()){
                    Integer start = (positionNumber/chunksize)*chunksize;
                    newConservedRegion = new ConservedRegionFeature(currentConservedRegion.getChromosome(),
                           start, start + chunksize - 1, currentConservedRegion.getChunk() + 1);
                    newConservedRegion.addSource(type);

                    if(lastInsertedChunk.compareTo(start/chunksize) != 0){
                        currentConservedRegion = serializeAndChangeCurrentChunk(start/chunksize, currentConservedRegion,
                                newConservedRegion);
                        printedLastChunk = true;
                    }

                    arrayValues[positionNumber%chunksize] = Float.parseFloat(line.trim());
                } else {
                    arrayValues[positionNumber%chunksize] = Float.parseFloat(line.trim());
                    positionNumber++;
                }
            }
        }

        if(!printedLastChunk){
            currentConservedRegion.getSource(type).addValues(Arrays.asList(arrayValues));
            //currentConservedRegion.addValues(Arrays.asList(arrayValues));
            System.out.println(currentConservedRegion);
            serialize(currentConservedRegion);
        }

        br.close();
        this.disconnect();
    }

    private ConservedRegionFeature serializeAndChangeCurrentChunk(Integer currentChunk,
                    ConservedRegionFeature currentConservedRegion, ConservedRegionFeature newConservedRegion) {
        lastInsertedChunk = currentChunk;
        currentConservedRegion.getSource(type).addValues(Arrays.asList(arrayValues));
        //currentConservedRegion.addValues(Arrays.asList(arrayValues));
        serialize(currentConservedRegion);

        currentConservedRegion = newConservedRegion;
        arrayValues = new Float[chunksize];

        return currentConservedRegion;
    }


    private ConservedRegionFeature buildConservedRegionFeature(String line, Integer positionNumber){
        Matcher matcher = pattern.matcher(line);
        matcher.matches();

        String chr = matcher.group(1);
        positionNumber = Integer.parseInt(matcher.group(2));
        int step = Integer.parseInt(matcher.group(3));

        // Calculate the current chunk
        int chunkNumber = calculateChunk(positionNumber);
        int startChunk = chunkNumber * chunksize;
        int endChunk = startChunk + chunksize - 1;
        ConservedRegionFeature conservedRegionFeature = new ConservedRegionFeature(chr, startChunk, endChunk, chunkNumber);
        conservedRegionFeature.addSource(type);

        // Set the last chunk used to insert
        lastInsertedChunk = chunkNumber;

        return conservedRegionFeature;
    }

    private Integer calculateChunk(Integer startPosition){
        return startPosition/chunksize;
    }

    @Override
    public boolean disconnect() {
        boolean disconnected = false;
        try {
            disconnected = super.disconnect();
        } catch (Exception e) {
            logger.error("Disconnecting parser: " + e.getMessage());
        }
        return disconnected;
    }
}