package org.opencb.cellbase.build.transform.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by imedina on 11/12/13.
 */
public class VariationUtils {

    public static Map<String, String> parseSeqRegionToMap(Path variationDirectoryPath) {
        Map<String, String> seqRegion = new HashMap<>();
        try {
            File seqRegionFile = variationDirectoryPath.resolve("seq_region.txt.gz").toFile();
            if(seqRegionFile.exists()) {
                BufferedReader br = FileUtils.newBufferedReader(seqRegionFile.toPath());
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    String[] readLineFields = readLine.split("\t");
                    seqRegion.put(readLineFields[0], readLineFields[1]);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seqRegion;
    }

    public static Map<String, String> parsePhenotypeToMap(Path variationDirectoryPath) {
        Map<String, String> seqRegion = new HashMap<>();
        try {
            File seqRegionFile = variationDirectoryPath.resolve("phenotype.txt.gz").toFile();
            if(seqRegionFile.exists()) {
                BufferedReader br = FileUtils.newBufferedReader(seqRegionFile.toPath());
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    String[] readLineFields = readLine.split("\t");
                    seqRegion.put(readLineFields[0], readLineFields[3]);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seqRegion;
    }

    public static Map<String, String> parseSourceToMap(Path variationDirectoryPath) {
        Map<String, String> sourceMap = new HashMap<>();
        try {
            File sourceFile = variationDirectoryPath.resolve("source.txt.gz").toFile();
            if(sourceFile.exists()) {
                BufferedReader br = FileUtils.newBufferedReader(sourceFile.toPath(), Charset.defaultCharset());
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    String[] readLineFields = readLine.split("\t");
                    if (readLineFields.length == 7) {
                        sourceMap.put(readLineFields[0], readLineFields[1] + "," + readLineFields[2]);
                    } else {
                        sourceMap.put(readLineFields[0], readLineFields[1] + "," + readLineFields[2]);
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sourceMap;
    }

    public static Map<String, String> parseStudyToMap(Path variationDirectoryPath) {
        Map<String, String> seqRegion = new HashMap<>();
        try {
            File seqRegionFile = variationDirectoryPath.resolve("study.txt.gz").toFile();
            if(seqRegionFile.exists()) {
                BufferedReader br = FileUtils.newBufferedReader(seqRegionFile.toPath());
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    String[] readLineFields = readLine.split("\t");
                    seqRegion.put(readLineFields[0], readLineFields[5]);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seqRegion;
    }

    public static Map<String, String> parseAttribTypeToMap(Path variationDirectoryPath) {
        Map<String, String> seqRegion = new HashMap<>();
        try {
            File seqRegionFile = variationDirectoryPath.resolve("attrib_type.txt.gz").toFile();
            if(seqRegionFile.exists()) {
                BufferedReader br = FileUtils.newBufferedReader(seqRegionFile.toPath());
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    String[] readLineFields = readLine.split("\t");
                    seqRegion.put(readLineFields[0], readLineFields[1]);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seqRegion;
    }

}
