package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.common.ConservedRegionChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ConservedRegionParser extends CellBaseParser {
	private static int CHUNKSIZE = 2000;

	private final Path conservedRegionPath;
	private int chunksize;
    private ObjectMapper gson;
    private Logger logger;

    // Download data:
    // for i in `seq 1 22`; do wget ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/phastCons46way/primates/chr$i.phastCons46way.primates.wigFix.gz; done
    // ftp://hgdownload.cse.ucsc.edu/goldenPath/hg19/phyloP46way/primates/

    public ConservedRegionParser(CellBaseSerializer serializer, Path conservedRegionPath, int chunksize) {
    	super(serializer);
        logger = LoggerFactory.getLogger(ConservedRegionParser.class);
        gson = new ObjectMapper();
        this.conservedRegionPath = conservedRegionPath;
        this.chunksize = chunksize;
    }

    public void parse() throws IOException {
        Path outJsonPath;

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
        boolean isFirst = true;
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
//                    bw.write(gson.toJson(conservedRegion)+"\n");
                    conservedRegion = new ConservedRegionChunk(chromosome, start, end, conservedType, start/CHUNKSIZE, values);
                    //bw.write(gson.writeValueAsString(conservedRegion)+"\n");
                    serialize(conservedRegion);
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
//                step = Integer.parseInt(attributes.get("step"));

                values = new ArrayList<>(2000);

//                conservedRegion = new ConservedRegion(chromosome, start, 0, conservedType, values);
//                conservedRegion = new ConservedRegionChunk(chromosome, start, 0, conservedType, start/CHUNKSIZE, values);
//                System.out.println(start);

            } else {
                int startChunk = start/CHUNKSIZE;
                end++;
                int endChunk = end/CHUNKSIZE;

                if(startChunk != endChunk) {
//                    System.out.println("coords: "+start+", "+(end-1));
//                    System.out.println("values length: "+values.size());
                    conservedRegion = new ConservedRegionChunk(chromosome, start, end-1, conservedType, startChunk, values);
                    //bw.write(gson.writeValueAsString(conservedRegion)+"\n");
                    serialize(conservedRegion);
                    values.clear();
                    start = end;
                }

//                offset += step;
                value = Float.parseFloat(line.trim());
                values.add(value);
            }
        }
        //write last
        conservedRegion = new ConservedRegionChunk(chromosome, start, end, conservedType, start/CHUNKSIZE, values);
        //bw.write(gson.writeValueAsString(conservedRegion)+"\n");
        serialize(conservedRegion);
//        conservedRegion.setEnd(end);
        br.close();
    }

//    @Deprecated
//    public static Path getConservedRegionPath(Path conservedRegionFolderPath, String chrFile) {
//        String file = "";
//        String conservedRegion = conservedRegionFolderPath.getFileName().toString();
//        switch (conservedRegion.toLowerCase()) {
//            case "phastcons":
//                file = "chr" + chrFile + ".phastCons46way.primates.wigFix.gz";
//                break;
//            case "phylop":
//                file = "chr" + chrFile + ".phyloP46way.primate.wigFix.gz";
//                break;
//        }
//        return conservedRegionFolderPath.resolve(file);
//    }

//    private static void processFileOld(Path inGzPath, Map<Integer, ConservedRegionChunk> conservedRegionChunks, String conservedType, int chunksize) throws IOException {
//        if(chunksize <= 0) {
//            chunksize = CHUNKSIZE;
//        }
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(inGzPath))));
//
//        String line = null;
//        int start = 0, offset = 0, step = 1, chunkId, position;
//        float value;
//        String chromosome = "";
//        Map<String, String> attributes = new HashMap<>();
//
//
//        while ((line = br.readLine()) != null) {
//            if (!line.startsWith("fixedStep")) {
//                position = start + offset;
//                offset += step;
//                chunkId = position/chunksize;
//                value = Float.parseFloat(line.trim());
//
//                if(conservedRegionChunks.get(chunkId)==null){
//                    int chunkStart = getChunkStart(chunkId, chunksize);
//                    int chunkEnd = getChunkEnd(chunkId, chunksize);
//                    conservedRegionChunks.put(chunkId, new ConservedRegionChunk(chromosome, chunkId, chunkStart, chunkEnd));
//                }
//
//                Float[] values;
//                if(conservedType.toLowerCase().equals("phastcons")){
//                    values = conservedRegionChunks.get(chunkId).getPhastCons();
//                }else{
//                    values = conservedRegionChunks.get(chunkId).getPhylop();
//                }
//
//                if(chunkId==0){
//                    values[((position%chunksize)-1)] = value;
//                }else{
//                    values[(position%chunksize)] = value;
//                }
//            } else {
//                offset = 0;
//                attributes.clear();
//                String[] atrrFields = line.split(" ");
//                String[] attrKeyValue;
//                for (String attrField : atrrFields) {
//                    if (!attrField.equalsIgnoreCase("fixedStep")) {
//                        attrKeyValue = attrField.split("=");
//                        attributes.put(attrKeyValue[0].toLowerCase(), attrKeyValue[1]);
//                    }
//                }
//                start = Integer.parseInt(attributes.get("start"));
//                step = Integer.parseInt(attributes.get("step"));
//                chromosome = attributes.get("chrom").replace("chr", "");
//                System.out.println(line);
//            }
//        }
//        br.close();
//
//    }
//
//
//    private static int getChunkId(int position, int chunksize){
//        if(chunksize <= 0) {
//            return position/CHUNKSIZE;
//        }else {
//            return position/chunksize;
//        }
//    }
//    private static int getChunkStart(int id, int chunksize){
//        if(chunksize <= 0) {
//            return (id == 0) ? 1 : id*CHUNKSIZE;
//        }else {
//            return (id==0) ? 1 : id*chunksize;
//        }
//    }
//    private static int getChunkEnd(int id, int chunksize) {
//        if(chunksize <= 0) {
//            return (id * CHUNKSIZE) + CHUNKSIZE - 1;
//        }else {
//            return (id*chunksize)+chunksize-1;
//        }
//    }


}
