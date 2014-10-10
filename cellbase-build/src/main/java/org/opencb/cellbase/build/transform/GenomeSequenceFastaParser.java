package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.core.Chromosome;
import org.opencb.biodata.models.core.Cytoband;
import org.opencb.biodata.models.core.GenomeSequenceChunk;
import org.opencb.biodata.models.core.InfoStats;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;
import org.opencb.cellbase.build.transform.utils.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class GenomeSequenceFastaParser extends CellBaseParser {

    private Path genomeReferenceFastaFile;


    private int CHUNK_SIZE = 2000;


    public GenomeSequenceFastaParser(Path genomeReferenceFastaFile, CellBaseSerializer serializer) {
        super(serializer);
        this.genomeReferenceFastaFile = genomeReferenceFastaFile;
    }


    @Override
    public void parse() {

        try {
            String sequenceName = "";
            String sequenceType = "";
            String sequenceAssembly = "";
            String line;
            StringBuilder sequenceStringBuilder = new StringBuilder();

            // Preparing input and output files
            BufferedReader br;
            br = FileUtils.newBufferedReader(genomeReferenceFastaFile);

            while ((line = br.readLine()) != null) {

                if (!line.startsWith(">")) {
                    sequenceStringBuilder.append(line);
                } else {
                    // new chromosome, save data
                    if (sequenceStringBuilder.length() > 0) {
                        if (!sequenceName.contains("PATCH") && !sequenceName.contains("HSCHR")) {
                            System.out.println(sequenceName);
                            serializeGenomeSequence(sequenceName, sequenceType, sequenceAssembly, sequenceStringBuilder.toString());
                        }
                    }

                    // initialize data structures
                    sequenceName = line.replace(">", "").split(" ")[0];
                    sequenceType = line.replace(">", "").split(" ")[2].split(":")[0];
                    sequenceAssembly = line.replace(">", "").split(" ")[2].split(":")[1];
                    sequenceStringBuilder.delete(0, sequenceStringBuilder.length());
                }
            }
            // Last chromosome must be processed
            if (!sequenceName.contains("PATCH") && !sequenceName.contains("HSCHR")) {
                serializeGenomeSequence(sequenceName, sequenceType, sequenceAssembly, sequenceStringBuilder.toString());
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serializeGenomeSequence(String chromosome, String sequenceType, String sequenceAssembly, String sequence) throws IOException {
        int chunk = 0;
        int start = 1;
        int end = CHUNK_SIZE - 1;
        String chunkSequence;

        String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";
        GenomeSequenceChunk genomeSequenceChunk;

        if (sequence.length() < CHUNK_SIZE) {//chromosome sequence length can be less than CHUNK_SIZE
            chunkSequence = sequence;
            genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + 0 + "_" + chunkIdSuffix, start, sequence.length() - 1, sequenceType, sequenceAssembly, chunkSequence);
            serialize(genomeSequenceChunk);
            start += CHUNK_SIZE - 1;
        } else {
            while (start < sequence.length()) {
                if (chunk % 10000 == 0) {
                    System.out.println("Chr:" + chromosome + " chunkId:" + chunk);
                }
                // First chunk of the chromosome
                if (start == 1) {
                    // First chunk contains CHUNK_SIZE-1 nucleotides as index start at position 1 but must end at 1999
                    chunkSequence = sequence.substring(start - 1, CHUNK_SIZE - 1);
                    genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + chunk + "_" + chunkIdSuffix, start, end, sequenceType, sequenceAssembly, chunkSequence);
                    serialize(genomeSequenceChunk);
                    start += CHUNK_SIZE - 1;

                } else {
                    // Regular chunk
                    if ((start + CHUNK_SIZE) < sequence.length()) {
                        chunkSequence = sequence.substring(start - 1, start + CHUNK_SIZE - 1);
                        genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + chunk + "_" + chunkIdSuffix, start, end, sequenceType, sequenceAssembly, chunkSequence);
                        serialize(genomeSequenceChunk);
                        start += CHUNK_SIZE;
                    } else {
                        // Last chunk of the chromosome
                        chunkSequence = sequence.substring(start - 1, sequence.length());
                        genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + chunk + "_" + chunkIdSuffix, start, sequence.length(), sequenceType, sequenceAssembly, chunkSequence);
                        serialize(genomeSequenceChunk);
                        start = sequence.length();
                    }
                }
                end = start + CHUNK_SIZE - 1;
                chunk++;
            }
        }
    }

    public void parseFastaGzipFilesToJson(File genomeReferenceFastaDir, File outJsonFile) {
        try {
            StringBuilder sequenceStringBuilder;
            File[] files = genomeReferenceFastaDir.listFiles();
            BufferedWriter bw = Files.newBufferedWriter(Paths.get(outJsonFile.toURI()), Charset.defaultCharset(), StandardOpenOption.CREATE);
            for (File file : files) {
                if (file.getName().endsWith(".fa.gz")) {
                    System.out.println(file.getAbsolutePath());

                    String chromosome = "";
                    String line;
                    sequenceStringBuilder = new StringBuilder();
                    // Java 7 IO code
                    BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith(">")) {
                            sequenceStringBuilder.append(line);
                        } else {
                            // new chromosome
                            // save data
                            if (sequenceStringBuilder.length() > 0) {
                                System.out.println(chromosome);
//								serializeGenomeSequence(chromosome, sequenceStringBuilder.toString(), bw);
                                serializeGenomeSequence(chromosome, "", "", sequenceStringBuilder.toString());
                            }

                            // initialize data structures
                            chromosome = line.replace(">", "").split(" ")[0];
                            sequenceStringBuilder.delete(0, sequenceStringBuilder.length());
                        }
                    }
                    // Last chromosome must be processed
//					serializeGenomeSequence(chromosome, sequenceStringBuilder.toString(), bw);
                    serializeGenomeSequence(chromosome, "", "", sequenceStringBuilder.toString());
                    br.close();
                }
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
