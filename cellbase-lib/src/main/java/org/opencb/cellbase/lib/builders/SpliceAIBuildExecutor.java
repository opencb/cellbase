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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.tools.sequence.FastaIndex;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.builders.formats.Genome;
import org.opencb.commons.exec.Command;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SpliceAIBuildExecutor {

    private static final String INPUT_VCF_FILENAME_PREFIX = "input_";
    private static final String INPUT_ANNOT_FILENAME = "annotation.txt";
    private static final String OUTPUT_VCF_FILENAME_PREFIX = "output_";

    private Path genePath;
    private Path genomeInfoPath;
    private Path fastaPath;
    private int distance;
    private int chunkSize;
    private CellBaseSerializer serializer;

    private Path tempPath;
    private Path inputAnnotPath;

    private ObjectMapper jsonObjectMapper;
    private Logger logger;

    public SpliceAIBuildExecutor(Path genePath, Path genomeInfoPath, Path fastaPath, int distance, int chunkSize,
                                 CellBaseSerializer serializer) {
        this.genePath = genePath;
        this.genomeInfoPath = genomeInfoPath;
        this.fastaPath = fastaPath;
        this.distance = distance;
        this.chunkSize = chunkSize;
        this.serializer = serializer;

        tempPath = serializer.getOutdir().resolve("tmp.splice");
        inputAnnotPath = tempPath.resolve(INPUT_ANNOT_FILENAME);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        logger = LoggerFactory.getLogger(SpliceAIBuildExecutor.class);
    }

    public void prepareInput() throws IOException {
        // Create annotation file (-A parameter)
        try {
            if (!tempPath.toFile().exists()) {
                Files.createDirectory(tempPath);
            }
            createInputAnnotationFile(inputAnnotPath);
        } catch (IOException e) {
            if (inputAnnotPath.toFile().exists()) {
                inputAnnotPath.toFile().delete();
            }
            throw e;
        }

        // Create input vcf file (-I parameter)
        createInputVcfFiles();
    }

    public void runSpliceAI() {
        // Execute SpliceAI:
        // $ spliceai -I input.vcf -O output.vcf -R ref.fa -A annotation.txt
        //outputVcfPath = tempPath.resolve(OUTPUT_VCF_FILENAME);

        // Populates the array with names of files and directories
        String[] pathnames = tempPath.toFile().list();

        // For each pathname in the pathnames array
        for (String pathname : pathnames) {
            if (pathname.startsWith(INPUT_VCF_FILENAME_PREFIX)) {
                String outPathname = pathname.replace(INPUT_VCF_FILENAME_PREFIX, OUTPUT_VCF_FILENAME_PREFIX);
                String cmdline = "spliceai -I " + tempPath + "/" + pathname
                        + " -O " + tempPath + "/" + outPathname
                        + " -R " + fastaPath
                        + " -A " + inputAnnotPath;
                logger.debug(cmdline);
                System.out.println(cmdline);

                Command command = new Command(cmdline);
//                    .setOutputOutputStream(
//                new DataOutputStream(new FileOutputStream(getOutDir().resolve(STDOUT_FILENAME).toFile())))
//                .setErrorOutputStream(
//                        new DataOutputStream(new FileOutputStream(getOutDir().resolve(STDERR_FILENAME).toFile())))
//                command.run();
            }
        }
    }

    public void parseOutput() throws IOException, CellBaseException {
        // Parse output vcf (-O output)
        // Populates the array with names of files and directories
        String[] pathnames = tempPath.toFile().list();

        // For each pathname in the pathnames array
        for (String pathname : pathnames) {
            if (pathname.startsWith(OUTPUT_VCF_FILENAME_PREFIX)) {
                BufferedReader bufferedReader = FileUtils.newBufferedReader(tempPath.resolve(pathname));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("#") || line.trim().isEmpty()) {
                        continue;
                    }
                    String[] vcfFields = line.split("\t");
                    if (vcfFields.length > 7 && StringUtils.isNotEmpty(vcfFields[7])) {
                        SpliceScore spliceScore = null;

                        String geneId = "";
                        String transcriptId = "";
                        String exonId = "";
                        int exonNumber = -1;

                        String[] infoFields = vcfFields[7].split(";");
                        for (String infoField : infoFields) {
                            if (infoField.startsWith("GN")) {
                                geneId = infoField.split("=")[1];
                            } else if (infoField.startsWith("TR")) {
                                transcriptId = infoField.split("=")[1];
                            } else if (infoField.startsWith("EXN")) {
                                exonNumber = Integer.parseInt(infoField.split("=")[1]);
                            } else if (infoField.startsWith("EX")) {
                                exonId = infoField.split("=")[1];
                            } else if (infoField.startsWith("SpliceAI")) {
                                String[] split = line.split("SpliceAI=");
                                if (split.length > 1) {
                                    String[] aiStrings = split[1].split(",");
                                    System.out.println(line);

                                    spliceScore = new SpliceScore();

                                    spliceScore.setChromosome(vcfFields[0]);
                                    spliceScore.setPosition(Integer.parseInt(vcfFields[1]));
                                    spliceScore.setRefAllele(vcfFields[3]);
                                    spliceScore.setGeneId(geneId);
                                    spliceScore.setTranscritptId(transcriptId);
                                    spliceScore.setExonId(exonId);
                                    spliceScore.setExonNumber(exonNumber);
                                    spliceScore.setSource("SpliceAI");
                                    spliceScore.setAlternates(new ArrayList<>());

                                    for (String aiString : aiStrings) {

                                        String[] aiFields = aiString.split("\\|");

                                        spliceScore.setGeneName(aiFields[1]);
                                        SpliceScore.AlternateSpliceScore altScore = new SpliceScore().new AlternateSpliceScore(aiFields[0],
                                                new HashMap<>());

                                        int[] deltaPositions = new int[4];
                                        double[] deltaScores = new double[4];

                                        try {
                                            deltaScores[0] = Double.parseDouble(aiFields[2]);
                                        } catch (NumberFormatException e) {
                                            deltaScores[0] = Double.NaN;
                                        }

                                        try {
                                            deltaScores[1] = Double.parseDouble(aiFields[3]);
                                        } catch (NumberFormatException e) {
                                            deltaScores[1] = Double.NaN;
                                        }

                                        try {
                                            deltaScores[2] = Double.parseDouble(aiFields[4]);
                                        } catch (NumberFormatException e) {
                                            deltaScores[2] = Double.NaN;
                                        }

                                        try {
                                            deltaScores[3] = Double.parseDouble(aiFields[5]);
                                        } catch (NumberFormatException e) {
                                            deltaScores[3] = Double.NaN;
                                        }

                                        try {
                                            deltaPositions[0] = Integer.parseInt(aiFields[6]);
                                        } catch (NumberFormatException e) {
                                            deltaPositions[0] = 0;
                                        }

                                        try {
                                            deltaPositions[1] = Integer.parseInt(aiFields[7]);
                                        } catch (NumberFormatException e) {
                                            deltaPositions[1] = 0;
                                        }

                                        try {
                                            deltaPositions[2] = Integer.parseInt(aiFields[8]);
                                        } catch (NumberFormatException e) {
                                            deltaPositions[2] = 0;
                                        }

                                        try {
                                            deltaPositions[3] = Integer.parseInt(aiFields[9]);
                                        } catch (NumberFormatException e) {
                                            deltaPositions[3] = 0;
                                        }

                                        // Add scores to the list
                                        altScore.getScores().put("deltaScores", deltaScores);
                                        altScore.getScores().put("deltaPositions", deltaPositions);
                                        spliceScore.getAlternates().add(altScore);
                                    }
                                }
                            }
                        }
                        if (spliceScore != null) {
                            serializer.serialize(spliceScore);
                        }
                    }
                }
            }
        }
        serializer.close();
    }

    public void clean() throws IOException {
        logger.debug("Delete temporary directory " + tempPath.toFile());
        org.apache.commons.io.FileUtils.deleteDirectory(tempPath.toFile());
    }

    private void createInputAnnotationFile(Path inputAnnotPath) throws IOException {
        PrintWriter pw = new PrintWriter(inputAnnotPath.toFile());

        pw.println("#NAME\tCHROM\tSTRAND\tTX_START\tTX_END\tEXON_START\tEXON_END");
        BufferedReader bufferedReader = FileUtils.newBufferedReader(genePath);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            Gene gene = jsonObjectMapper.readValue(line, Gene.class);

            if ("ensembl".equals(gene.getSource())) {
                Transcript maneSelectTranscript = null;
                Transcript canonicalTranscript = null;
                for (Transcript transcript : gene.getTranscripts()) {
                    if (CollectionUtils.isNotEmpty(transcript.getFlags())) {
                        if (transcript.getFlags().contains("MANE Select")) {
                            maneSelectTranscript = transcript;
                            break;
                        }
                    }
                }
                Transcript transcript = (maneSelectTranscript != null) ? maneSelectTranscript : canonicalTranscript;
                if (transcript != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(gene.getName()).append("\t");
                    sb.append(transcript.getChromosome()).append("\t");
                    sb.append(transcript.getStrand()).append("\t");
                    sb.append(transcript.getStart()).append("\t");
                    sb.append(transcript.getEnd()).append("\t");
                    List<Integer> exonStart = new ArrayList<>();
                    List<Integer> exonEnd = new ArrayList<>();
                    for (Exon exon : transcript.getExons()) {
                        exonStart.add(exon.getStart());
                        exonEnd.add(exon.getEnd());
                    }
                    sb.append(StringUtils.join(exonStart, ",")).append("\t");
                    sb.append(StringUtils.join(exonEnd, ","));

                    pw.println(sb.toString());
                    sb.setLength(0);
                }
            }
        }
        pw.close();
    }

    private void createInputVcfFiles() throws IOException {
        int counter = 0;
        int fileIndex = 0;
        String prevChromosome = "";

        String basePrefix = tempPath + "/" + INPUT_VCF_FILENAME_PREFIX;

        List<String> bases = new ArrayList<>(Arrays.asList("A", "C", "G", "T"));

        FastaIndex fastaIndex = new FastaIndex(fastaPath);

        BufferedReader bufferedReader = FileUtils.newBufferedReader(genePath);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            Gene gene = jsonObjectMapper.readValue(line, Gene.class);

            if (!gene.getChromosome().equals(prevChromosome)) {
                prevChromosome = gene.getChromosome();
                fileIndex = 0;
                counter = 0;
            }

            FileWriter fw = getVcfFileWriter(basePrefix, gene.getChromosome(), fileIndex);
            if (counter >= chunkSize) {
                counter = 0;
                fileIndex++;
            }

            Set<String> uniqPos = new HashSet<>();
            for (Transcript transcript : gene.getTranscripts()) {
                if (CollectionUtils.isNotEmpty(transcript.getFlags()) && transcript.getFlags().contains("MANE Select")) {
                    // Accessing to the context sequence and write it into the context index file
                    String sequence = fastaIndex.query(gene.getChromosome(), gene.getStart() - distance, gene.getEnd() + distance);

                    int start = gene.getStart() - distance;


                    for (Exon exon : transcript.getExons()) {
//                                System.out.println(exon.getId() + " (" + exon.getStart() + ") > " + exon.getSequence());
                        StringBuilder sb = new StringBuilder();
                        for (int i = exon.getStart() - distance; i <= exon.getEnd() + distance; i++) {
                            String pos = gene.getChromosome() + "." + i;
                            if (!uniqPos.contains(pos)) {
                                uniqPos.add(pos);

                                sb.append(gene.getChromosome()).append("\t");
                                sb.append(i).append("\t");
                                sb.append(".\t");
                                String ref = sequence.substring(i - start, i - start + 1);
                                sb.append(ref).append("\t");
                                List<String> alt = new ArrayList<>();
                                for (String base : bases) {
                                    if (!base.equals(ref)) {
                                        alt.add(base);
                                    }
                                }
                                sb.append(StringUtils.join(alt, ",")).append("\t.\t.\t");
                                sb.append("GN=" + gene.getId());
                                sb.append(";TR=" + transcript.getId());
                                sb.append(";EX=" + exon.getId());
                                sb.append(";EXN=" + exon.getExonNumber());

                                // Get current input VCF file writer
                                ++counter;
                                fw.write(sb.toString());
                                fw.write("\n");
                                sb.setLength(0);
                            }
                        }
                    }
                    break;
                }
            }
            fw.close();
        }
    }

    private FileWriter getVcfFileWriter(String basePrefix, String chromosome, int fileIndex) throws IOException {
        FileWriter fw;
        File vcfFile = new File(basePrefix + "chr" + chromosome + "_chunk" + fileIndex + ".vcf");
        if (!vcfFile.exists()) {
            fw = new FileWriter(vcfFile, true);
            writeVcfHeader(fw);
        } else {
            fw = new FileWriter(vcfFile, true);
        }
        return fw;
    }

    private void writeVcfHeader(FileWriter fw) throws IOException {
        Genome genome = jsonObjectMapper.readValue(genomeInfoPath.toFile(), Genome.class);

        // Version
        fw.write("##fileformat=VCFv4.2\n");

        // File date
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime now = LocalDateTime.now();
        fw.write("##fileDate=" + dtf.format(now) + "\n");

        // Assembly
        fw.write("##reference=GRCh38/hg19\n");

        // Chromosomes
        for (Chromosome chromosome : genome.getChromosomes()) {
            fw.write("##contig=<ID=" + chromosome.getName() + ",length=" + chromosome.getSize() + ">\n");
        }

        // INFO
        fw.write("##INFO=<ID=GN,Number=1,Type=String,Description=\"Ensembl gene ID\">\n");
        fw.write("##INFO=<ID=TR,Number=1,Type=String,Description=\"Ensembl transcript ID\">\n");
        fw.write("##INFO=<ID=EX,Number=1,Type=String,Description=\"Ensembl exon ID\">\n");
        fw.write("##INFO=<ID=EXN,Number=1,Type=String,Description=\"Exon number\">\n");

        // Last header line
        fw.write("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n");
    }
}
