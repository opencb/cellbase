package org.opencb.cellbase.lib.builders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.eclipse.jetty.util.ajax.JSON;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.tools.sequence.FastaIndex;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.lib.builders.formats.Genome;
import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpliceBuilderTest {


    @Test
    @Disabled
    public void createGtfFile() throws IOException {
        Path genePath = Paths.get("/home/jtarraga/bioinfo/cellbase/MANE.v0.93/gene.json.gz");
        Path inputAnnotPath = Paths.get("/home/jtarraga/bioinfo/cellbase/MANE.v0.93/grch38.gtf");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        PrintWriter pw = new PrintWriter(inputAnnotPath.toFile());

        StringBuilder sb = new StringBuilder();

        BufferedReader bufferedReader = FileUtils.newBufferedReader(genePath);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            Gene gene = mapper.readValue(line, Gene.class);

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
                    // Transcript
                    sb.setLength(0);
                    sb.append(transcript.getChromosome()).append("\t");
                    sb.append(transcript.getBiotype()).append("\t");
                    sb.append("transcript\t");
                    sb.append(transcript.getStart()).append("\t");
                    sb.append(transcript.getEnd()).append("\t");
                    sb.append(".\t");
                    sb.append(transcript.getStrand()).append("\t");
                    sb.append(".\t");
                    sb.append("gene_id \"").append(gene.getId()).append("\"; ");
                    sb.append("gene_name \"").append(gene.getName()).append("\"; ");
                    sb.append("transcript_id \"").append(transcript.getId()).append("\"; ");
                    sb.append("transcript_name \"").append(transcript.getName()).append("\"; ");
                    pw.println(sb.toString());

                    for (Exon exon : transcript.getExons()) {
                        // Exon
                        sb.setLength(0);
                        sb.append(exon.getChromosome()).append("\t");
                        sb.append(transcript.getBiotype()).append("\t");
                        sb.append("exon\t");
                        sb.append(exon.getStart()).append("\t");
                        sb.append(exon.getEnd()).append("\t");
                        sb.append(".\t");
                        sb.append(exon.getStrand()).append("\t");
                        sb.append(".\t");
                        sb.append("gene_id \"").append(gene.getId()).append("\"; ");
                        sb.append("gene_name \"").append(gene.getName()).append("\"; ");
                        sb.append("transcript_id \"").append(transcript.getId()).append("\"; ");
                        sb.append("transcript_name \"").append(transcript.getName()).append("\"; ");
                        sb.append("exon_number \"").append(exon.getExonNumber()).append("\"; ");
                        sb.append("exon_id \"").append(exon.getId()).append("\"; ");
                        pw.println(sb.toString());
                    }
                }
            }
        }
        pw.close();
    }

    @Test
    @Disabled
    public void createInputVcfFiles() throws IOException {
        Path fastaPath = Paths.get("/home/jtarraga/bioinfo/cellbase/fasta/Homo_sapiens.GRCh38.fa");
        Path genePath = Paths.get("/home/jtarraga/bioinfo/cellbase/MANE.v0.93/gene.json.gz");
        Path genomeInfoPath = Paths.get("/home/jtarraga/bioinfo/cellbase/MANE.v0.93/genome_info.json");
        Path tempPath = Paths.get("/home/jtarraga/bioinfo/cellbase/MMSplice/vcf");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Genome genome = mapper.readValue(genomeInfoPath.toFile(), Genome.class);

        int chunkSize = 5000000;
        int distance = 50;
        int counter = 0;
        int fileIndex = 0;

        String basePrefix = tempPath + "/";
        FileWriter fw = getVcfFileWriter(basePrefix, fileIndex, genome);

        List<String> bases = new ArrayList<>(Arrays.asList("A", "C", "G", "T"));

        FastaIndex fastaIndex = new FastaIndex(fastaPath);

        BufferedReader bufferedReader = FileUtils.newBufferedReader(genePath);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            Gene gene = mapper.readValue(line, Gene.class);

//            if (!gene.getChromosome().equals("17")) {
//                continue;
//            }
//            System.out.println(gene.getId() + ", " + gene.getName() + ", counter = " + counter);

//            Variant variant;
//            VariantNormalizer normalizer = new VariantNormalizer();

            Set<String> uniqPos = new HashSet<>();
            for (Transcript transcript : gene.getTranscripts()) {
                if (CollectionUtils.isNotEmpty(transcript.getFlags()) && transcript.getFlags().contains("MANE Select")) {
                    // Accessing to the context sequence and write it into the context index file
                    String ref, alt;
                    String sequence = fastaIndex.query(gene.getChromosome(), gene.getStart() - distance, gene.getEnd() + distance);

                    int start = gene.getStart() - distance;


                    System.out.println(transcript.getChromosome() + ":" + transcript.getStart() + "-" + transcript.getEnd() + "; gene = "
                            + gene.getId() + ", " + gene.getName());

                    for (Exon exon : transcript.getExons()) {
//                        System.out.println(exon.getId() + " (" + exon.getStart() + ") > " + exon.getSequence());
                        StringBuilder sb = new StringBuilder();
                        for (int i = exon.getStart() - distance; i <= exon.getEnd() + distance; i++) {
                            ref = sequence.substring(i - start, i - start + 1).toUpperCase();
                            if (!bases.contains(ref)) {
                                continue;
                            }

                            String pos = gene.getChromosome() + "." + i;
                            if (!uniqPos.contains(pos)) {
                                uniqPos.add(pos);

                                sb.append(gene.getChromosome()).append("\t");
                                sb.append(i).append("\t");
                                sb.append(".\t");

                                // SNV
                                for (String base : bases) {
                                    if (!base.equals(ref)) {
                                        fw.write(sb.toString() + ref + "\t" + base + "\t.\t.\t\n");
                                        ++counter;
                                    }
                                }

                                // 1-nt INS
                                for (String base : bases) {
                                    fw.write(sb.toString() + ref + "\t" + ref + base + "\t.\t.\t\n");
                                    ++counter;
                                }

                                // 2-nt INS
                                for (String base1 : bases) {
                                    for (String base2 : bases) {
                                        fw.write(sb.toString() + ref + "\t" + ref + base1 + base2 + "\t.\t.\t\n");
                                        ++counter;
                                    }
                                }

                                // 1-nt DEL
                                try {
                                    ref = sequence.substring(i - start, i - start + 2).toUpperCase();
                                    alt = ref.substring(0, 1);
                                    fw.write(sb.toString() + ref + "\t" + alt + "\t.\t.\t\n");
                                    ++counter;
                                } catch (Exception e) {
                                    // Do nothing
                                }

                                // 2-nt DEL
                                try {
                                    ref = sequence.substring(i - start, i - start + 3).toUpperCase();
                                    alt = ref.substring(0, 1);
                                    fw.write(sb.toString() + ref + "\t" + alt + "\t.\t.\t\n");
                                    ++counter;
                                } catch (Exception e) {
                                    // Do nothing
                                }

                                sb.setLength(0);

                                if (counter >= chunkSize) {
                                    counter = 0;
                                    fileIndex++;

                                    // Close and get the new file writer
                                    fw.close();
                                    fw = getVcfFileWriter(basePrefix, fileIndex, genome);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (fw != null) {
            fw.close();
        }
//        System.out.println("counter = " + counter);
    }

    private FileWriter getVcfFileWriter(String basePrefix, int fileIndex, Genome genome) throws IOException {
        FileWriter fw;
        File vcfFile = new File(basePrefix + "input.chunk" + fileIndex + ".vcf");
        if (!vcfFile.exists()) {
            fw = new FileWriter(vcfFile, true);
            writeVcfHeader(fw, genome);
        } else {
            fw = new FileWriter(vcfFile, true);
        }
        return fw;
    }

    private void writeVcfHeader(FileWriter fw, Genome genome) throws IOException {

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

//        // INFO
//        fw.write("##INFO=<ID=GN,Number=1,Type=String,Description=\"Ensembl gene ID\">\n");
//        fw.write("##INFO=<ID=TR,Number=1,Type=String,Description=\"Ensembl transcript ID\">\n");
//        fw.write("##INFO=<ID=EX,Number=1,Type=String,Description=\"Ensembl exon ID\">\n");
//        fw.write("##INFO=<ID=EXN,Number=1,Type=String,Description=\"Exon number\">\n");

        // Last header line
        fw.write("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n");
    }

    @Test
    @Disabled
    public void parseMMSpliceFile() throws Exception {
        Path spliceDir = Paths.get(SpliceBuilderTest.class.getResource("/splice").toURI());
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/tmp/"));
        SpliceBuilder spliceBuilder = new SpliceBuilder(spliceDir, serializer);
        spliceBuilder.parse();
        serializer.close();

        List<SpliceScore> actual = loadSpliceScores(Paths.get("/tmp/splice_1.json.gz"));
        List<SpliceScore> expected = loadSpliceScores(Paths.get(SpliceBuilderTest.class.getResource("/splice/build/splice_1.json.gz")
                .getFile()));

        assertEquals(expected.size(), actual.size());
    }

    private List<SpliceScore> loadSpliceScores(Path path) throws IOException {
        List<SpliceScore> spliceScores = new ArrayList<>(10);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(path)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                spliceScores.add(jsonObjectMapper.convertValue(JSON.parse(line), SpliceScore.class));
                line = bufferedReader.readLine();
            }
        }

        return spliceScores;
    }

    @Test
    @Disabled
    public void buildMMSpliceFile() throws Exception {
        Path spliceDir = Paths.get("/home/jtarraga/data150/cellbase/homo_sapiens_grch38/download/splice");
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(Paths.get("/home/jtarraga/data150/cellbase/homo_sapiens_grch38/generated_json/splice"));
        SpliceBuilder spliceBuilder = new SpliceBuilder(spliceDir, serializer);
        spliceBuilder.parse();
        serializer.close();
    }
}