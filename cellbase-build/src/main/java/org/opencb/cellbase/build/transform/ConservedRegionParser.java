package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.core.common.ConservedRegionChunk;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ConservedRegionParser extends CellBaseParser {

	private static int CHUNKSIZE = 2000;

    private Logger logger;
    private Path conservedRegionPath;
    private int chunksize;

    // Download data:
    // for i in `seq 1 22`; do wget ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/phastCons46way/primates/chr$i.phastCons46way.primates.wigFix.gz; done
    // ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/phyloP46way/primates/

    public ConservedRegionParser(Path conservedRegionPath, int chunksize, CellBaseSerializer serializer) {
        super(serializer);
        this.conservedRegionPath = conservedRegionPath;
        this.chunksize = chunksize;
        logger = LoggerFactory.getLogger(ConservedRegionParser.class);
    }

    @Override
    public void parse() throws IOException {
        Map<String, Path> files = new HashMap<>();
        String chromosome;
        Set<String> chromosomes = new HashSet<>();

        // Reading all files in phastCons folder
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(conservedRegionPath.resolve("phastCons"));
        for(Path path: directoryStream) {
            chromosome = path.getFileName().toString().split("\\.")[0].replace("chr", "");
            chromosomes.add(chromosome);
            files.put(chromosome+"phastCons", path);
        }

        // Reading all files in phylop folder
        directoryStream = Files.newDirectoryStream(conservedRegionPath.resolve("phylop"));
        for(Path path: directoryStream) {
            chromosome = path.getFileName().toString().split("\\.")[0].replace("chr", "");
            chromosomes.add(chromosome);
            files.put(chromosome+"phylop", path);
        }

        /**
         * Now we can iterate over all the chromosomes found and process the files
         */
        logger.debug("Chromosomes found {}", chromosomes.toString());
        for(String chr : chromosomes){
            logger.debug("Processing chromosome {}, file {}", chr, files.get(chr+"phastCons"));
            processFile(files.get(chr+"phastCons"), "phastCons");

            logger.debug("Processing chromosome {}, file {}", chr, files.get(chr+"phylop"));
            processFile(files.get(chr+"phylop"), "phylop");
        }
    }


    private void processFile(Path inGzPath, String conservedType) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(inGzPath))));

        String line = null;
        String chromosome = "";
        int start = 0, end=0;
        float value;
        Map<String, String> attributes = new HashMap<>();
//        ConservedRegion conservedRegion =  null;
        List<Float> values = new ArrayList<>();

        ConservedRegionChunk conservedRegion =  null;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("fixedStep")) {
                //new group, save last
                if(conservedRegion != null){
                    conservedRegion.setEnd(end);
                    conservedRegion = new ConservedRegionChunk(chromosome, start, end, conservedType, start/CHUNKSIZE, values);
                    serializer.serialize(conservedRegion);
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
                int startChunk = start/CHUNKSIZE;
                end++;
                int endChunk = end/CHUNKSIZE;

                if(startChunk != endChunk) {
                    conservedRegion = new ConservedRegionChunk(chromosome, start, end-1, conservedType, startChunk, values);
                    serializer.serialize(conservedRegion);
                    values.clear();
                    start = end;
                }

                value = Float.parseFloat(line.trim());
                values.add(value);
            }
        }
        //write last
        conservedRegion = new ConservedRegionChunk(chromosome, start, end, conservedType, start/CHUNKSIZE, values);
        serializer.serialize(conservedRegion);
        br.close();
    }
}
