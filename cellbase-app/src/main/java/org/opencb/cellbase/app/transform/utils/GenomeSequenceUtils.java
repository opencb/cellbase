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

package org.opencb.cellbase.app.transform.utils;

import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;


/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/25/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenomeSequenceUtils {

    public static Map<String, String> getGenomeSequence(Path genomeSequencePath) throws IOException {
        FileUtils.checkPath(genomeSequencePath);
        BufferedReader br = FileUtils.newBufferedReader(genomeSequencePath);

        Map<String, String> genomeSequenceMap = new HashMap<>();
        String line = "";
        String chromosome = "";
        StringBuilder sequenceStringBuilder = new StringBuilder(100000);

        while ((line = br.readLine()) != null) {
            if (!line.startsWith(">")) {
                sequenceStringBuilder.append(line);
            } else {
                // new chromosome found
                if (sequenceStringBuilder.length() > 0) {
                    // we have read whole chromosome sequence, it's time to store it
                    if (!chromosome.contains("PATCH") && !chromosome.contains("HSCHR")) {
                        genomeSequenceMap.put(chromosome, sequenceStringBuilder.toString());
                    }
                }
                // initialize data structures and empty previous sequence
                chromosome = line.replace(">", "").split(" ")[0];
                sequenceStringBuilder.delete(0, sequenceStringBuilder.length());
                System.out.println(chromosome);
            }
        }

        br.close();
        return genomeSequenceMap;
    }

    public static Map<String, byte[]> getGenomeSequenceGZipped(Path genomeSequencePath) throws IOException {
        FileUtils.checkPath(genomeSequencePath);
        BufferedReader br = FileUtils.newBufferedReader(genomeSequencePath);

        Map<String, byte[]> genomeSequenceMap = new HashMap<>();
        String line = "";
        String chromosome = "";
        StringBuilder sequenceStringBuilder = new StringBuilder();

        while ((line = br.readLine()) != null) {
            if (!line.startsWith(">")) {
                sequenceStringBuilder.append(line);
            } else {
                // new chromosome found
                if (sequenceStringBuilder.length() > 0) {
                    // we have read whole chromosome sequence, it's time to store it
                    if (!chromosome.contains("PATCH") && !chromosome.contains("HSCHR")) {
                        System.out.println("Loading chrom: " + chromosome);
                        genomeSequenceMap.put(chromosome, StringUtils.gzip(sequenceStringBuilder.toString()));
                    }
                }
                // initialize data structures and empty previous sequence
                chromosome = line.replace(">", "").split(" ")[0];
                sequenceStringBuilder.delete(0, sequenceStringBuilder.length());
            }
        }

        br.close();
        return genomeSequenceMap;
    }

    public static String getChromosomeSequence(String chromosome, Path genomeSequencePath) throws IOException {
        FileUtils.checkPath(genomeSequencePath);
        BufferedReader br = FileUtils.newBufferedReader(genomeSequencePath);

        StringBuilder stringBuilder = new StringBuilder(100000);
        String line = "";
        boolean found = false;
        while ((line = br.readLine()) != null) {
            if (found) {
                if (!line.startsWith(">")) {
                    stringBuilder.append(line);
                } else {
                    break;
                }
            }
            // chromosomes names look like: >
            if (line.startsWith(">" + chromosome + " ")) {
                found = true;
            }
        }
        br.close();
        return stringBuilder.toString();
    }

    public static String getChromosomeSequenceFromFiles(String chrom, Path genomeSequenceDir) throws IOException {
        FileUtils.checkPath(genomeSequenceDir);

        File[] files = genomeSequenceDir.toFile().listFiles();
        File file = null;
        for (File f : files) {
            if (f.getName().endsWith("_" + chrom + ".fa.gz") || f.getName().endsWith("." + chrom + ".fa.gz")) {
                System.out.println(f.getAbsolutePath());
                file = f;
                break;
            }
        }
        StringBuilder sb = new StringBuilder(100000);
        if (file != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                if (found) {
                    if (!line.startsWith(">")) {
                        sb.append(line);
                    } else {
                        break;
                    }
                }
                if (line.startsWith(">")) {
                    found = true;
                }
            }
            br.close();
        }
        return sb.toString();
    }

}
