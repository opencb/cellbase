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

import org.opencb.biodata.models.core.GenomeSequenceChunk;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

public class GenomeSequenceFastaParser extends CellBaseParser {

    private Path genomeReferenceFastaFile;

    private static final int CHUNK_SIZE = 2000;

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
                        if (!sequenceName.contains("PATCH") && !sequenceName.contains("HSCHR") && !sequenceName.contains("contig")) {
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
            if (!sequenceName.contains("PATCH") && !sequenceName.contains("HSCHR") && !sequenceName.contains("contig")) {
                serializeGenomeSequence(sequenceName, sequenceType, sequenceAssembly, sequenceStringBuilder.toString());
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serializeGenomeSequence(String chromosome, String sequenceType, String sequenceAssembly, String sequence)
            throws IOException {
        int chunk = 0;
        int start = 1;
        int end = CHUNK_SIZE - 1;
        String chunkSequence;

        String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";
        GenomeSequenceChunk genomeSequenceChunk;

        if (sequence.length() < CHUNK_SIZE) { //chromosome sequence length can be less than CHUNK_SIZE
            chunkSequence = sequence;
            genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + 0 + "_" + chunkIdSuffix, start,
                    sequence.length() - 1, sequenceType, sequenceAssembly, chunkSequence);
            serializer.serialize(genomeSequenceChunk);
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
                    genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + chunk + "_" + chunkIdSuffix, start,
                            end, sequenceType, sequenceAssembly, chunkSequence);
                    serializer.serialize(genomeSequenceChunk);
                    start += CHUNK_SIZE - 1;

                } else {
                    // Regular chunk
                    if ((start + CHUNK_SIZE) < sequence.length()) {
                        chunkSequence = sequence.substring(start - 1, start + CHUNK_SIZE - 1);
                        genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + chunk + "_" + chunkIdSuffix, start,
                                end, sequenceType, sequenceAssembly, chunkSequence);
                        serializer.serialize(genomeSequenceChunk);
                        start += CHUNK_SIZE;
                    } else {
                        // Last chunk of the chromosome
                        chunkSequence = sequence.substring(start - 1, sequence.length());
                        genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome + "_" + chunk + "_" + chunkIdSuffix, start,
                                sequence.length(), sequenceType, sequenceAssembly, chunkSequence);
                        serializer.serialize(genomeSequenceChunk);
                        start = sequence.length();
                    }
                }
                end = start + CHUNK_SIZE - 1;
                chunk++;
            }
        }
    }
}
