package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.build.transform.serializers.CellBaseSerializer;
import org.opencb.cellbase.build.transform.utils.GenomeSequenceUtils;
import org.opencb.cellbase.core.common.core.*;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;
import org.opencb.commons.bioformats.feature.gtf.Gtf;
import org.opencb.commons.bioformats.feature.gtf.io.GtfReader;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GeneParser {

    //	private Map<String, Integer> geneDict;
    private Map<String, Integer> transcriptDict;
    private Map<String, Exon> exonDict;
    private RandomAccessFile rafChromosomeSequenceFile;

    private static final int CHUNK_SIZE = 5000;

    private CellBaseSerializer serializer;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public GeneParser(CellBaseSerializer serializer) {
        this.serializer = serializer;
        init();
    }

    private void init() {
        // genes = new ArrayList<Gene>(70000);
        // geneDict = new HashMap<String, Integer>(70000);
        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);
    }

    public void parse(Path geneDirectoryPath, Path genomeSequenceDir)
            throws IOException, SecurityException, NoSuchMethodException, FileFormatException, InterruptedException {
        Path gtfFile = null;
        for(String fileName: geneDirectoryPath.toFile().list()) {
            if(fileName.endsWith(".gtf") || fileName.endsWith(".gtf.gz")) {
                gtfFile = geneDirectoryPath.resolve(fileName);
                break;
            }
        }
        parse(gtfFile, geneDirectoryPath.resolve("description.txt"), geneDirectoryPath.resolve("xrefs.txt"), geneDirectoryPath.resolve("tfbs.txt"), geneDirectoryPath.resolve("mirna.txt"), genomeSequenceDir);
    }

    public void parse(Path gtfFile, Path geneDescriptionFile, Path xrefsFile, Path tfbsFile, Path mirnaFile, Path genomeSequenceFilePath)
            throws IOException, SecurityException, NoSuchMethodException, FileFormatException, InterruptedException {
        Files.exists(gtfFile);
        init();

        String geneId;
        String transcriptId;
        String currentChromosome = "";
        String chromSequence = "";
        String exonSequence = "";

        GeneMongoDB gene = null;
        Transcript transcript;
        Exon exon = null;
        int cdna = 1;
        int cds = 1;
        String[] fields;

//        Map<String, String> gseq = GenomeSequenceUtils.getGenomeSequence(genomeSequenceDir);
//        System.out.println("toma!!");

        Map<String, String> geneDescriptionMap = new HashMap<>();
        if (geneDescriptionFile != null && Files.exists(geneDescriptionFile)) {
            List<String> lines = Files.readAllLines(geneDescriptionFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                geneDescriptionMap.put(fields[0], fields[1]);
            }
        }

        Map<String, ArrayList<Xref>> xrefMap = new HashMap<>();
        if (xrefsFile != null && Files.exists(xrefsFile)) {
            List<String> lines = Files.readAllLines(xrefsFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                if(fields.length >= 4) {
                    if (!xrefMap.containsKey(fields[0])) {
                        xrefMap.put(fields[0], new ArrayList<Xref>());
                    }
                    xrefMap.get(fields[0]).add(new Xref(fields[1], fields[2], fields[3]));
                }
            }
        }

        Map<String, ArrayList<TranscriptTfbs>> tfbsMap = new HashMap<>();
        if(tfbsFile != null && Files.exists(tfbsFile) && !Files.isDirectory(tfbsFile)) {
            List<String> lines = Files.readAllLines(tfbsFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                if (!tfbsMap.containsKey(fields[0])) {
                    tfbsMap.put(fields[0], new ArrayList<TranscriptTfbs>());
                }
                tfbsMap.get(fields[0]).add(new TranscriptTfbs(fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), fields[6], Integer.parseInt(fields[7]), Integer.parseInt(fields[8]), Float.parseFloat(fields[9])));
            }
        }

        // Loading MiRNAGene file
        Map<String, MiRNAGene> mirnaGeneMap = new HashMap<>();
        if(mirnaFile != null && Files.exists(mirnaFile) && !Files.isDirectory(mirnaFile)) {
            mirnaGeneMap = getmiRNAGeneMap(mirnaFile);
        }

        // Preparing the fasta file for fast accessing
        // File must be ungunzipped and offsets stored
        // Commented because it takes too much time to gzip/gunzip
//        Map<String, Long> chromSequenceOffsets = prepareChromosomeSequenceFile(genomeSequenceFilePath);


        // BasicBSONList list = new BasicBSONList();
        String chunkIdSuffix = CHUNK_SIZE/1000 + "k";
        int cont = 0;
        GtfReader gtfReader = new GtfReader(gtfFile);
        Gtf gtf;
        while ((gtf = gtfReader.read()) != null) {
            geneId = gtf.getAttributes().get("gene_id");
            transcriptId = gtf.getAttributes().get("transcript_id");
            /*
			 * If chromosome is changed (or it's the first chromosome)
			 * we load the new chromosome sequence.
			 */
            if(!currentChromosome.equals(gtf.getSequenceName()) && !gtf.getSequenceName().startsWith("GL") && !gtf.getSequenceName().startsWith("HS") && !gtf.getSequenceName().startsWith("HG")) {
                currentChromosome = gtf.getSequenceName();
                chromSequence = getSequenceByChromosome(currentChromosome, genomeSequenceFilePath);
//                chromSequence = getSequenceByChromosome(currentChromosome, chromSequenceOffsets, genomeSequenceFilePath);
//				chromSequence = getSequenceByChromosomeName(currentChromosome, genomeSequenceDir);
            }

            // Gene object can only be null the first time
            // If new geneId is different from the current then we must serialize before load new gene
            if (gene == null || !geneId.equals(gene.getId())) {
                // gene object can only be null the first time
                if (gene != null) { // genes.size()>0
                    // Adding chunksIds
                    int chunkStart = (gene.getStart() - 5000) / CHUNK_SIZE;
                    int chunkEnd = (gene.getEnd() + 5000) / CHUNK_SIZE;
                    for(int i=chunkStart; i<=chunkEnd; i++) {
                        gene.getChunkIds().add(gene.getChromosome()+"_"+i+"_"+chunkIdSuffix);
                    }
                    serializer.serialize(gene);
                }

                gene = new GeneMongoDB(geneId, gtf.getAttributes().get("gene_name"), gtf.getAttributes().get("gene_biotype"),
                        "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
                        gtf.getStrand(), "Ensembl", geneDescriptionMap.get(geneId), new ArrayList<Transcript>(), mirnaGeneMap.get(geneId));
                // Do not change order!! size()-1 is the index of the gene ID
            }

            // Check if Transcript exist in the Gene Set of transcripts
            if (!transcriptDict.containsKey(transcriptId)) {
                transcript = new Transcript(transcriptId, gtf.getAttributes().get("transcript_name"), gtf.getSource(),
                        "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
                        gtf.getStrand(), 0, 0, 0, 0, 0, "", "", xrefMap.get(transcriptId), new ArrayList<Exon>(), tfbsMap.get(transcriptId));
                gene.getTranscripts().add(transcript);
                // Do not change order!! size()-1 is the index of the transcript
                // ID
                transcriptDict.put(transcriptId, gene.getTranscripts().size() - 1);
            } else {
                transcript = gene.getTranscripts().get(transcriptDict.get(transcriptId));
            }

            // At this point gene and transcript objects are set up

            // Update gene and transcript genomic coordinates, start must be the
            // lower, and end the higher
            updateTranscriptAndGeneCoords(transcript, gene, gtf);

            if (gtf.getFeature().equalsIgnoreCase("exon")) {
                // Obtaining the exon sequence
                exonSequence = "";
                if(currentChromosome.equals(gtf.getSequenceName()) && chromSequence.length() > 0) {
                    // as starts is inclusive and position begins in 1 we must -1, end is OK.
                    exonSequence = chromSequence.substring(gtf.getStart()-1, gtf.getEnd());
                }
                exon = new Exon(gtf.getAttributes().get("exon_id"), gtf.getSequenceName().replaceFirst("chr", ""),
                        gtf.getStart(), gtf.getEnd(), gtf.getStrand(), 0, 0, 0, 0, 0, 0, -1, Integer.parseInt(gtf
                        .getAttributes().get("exon_number")), exonSequence);
                transcript.getExons().add(exon);
                exonDict.put(transcript.getId() + "_" + exon.getExonNumber(), exon);
                if (gtf.getAttributes().get("exon_number").equals("1")) {
                    cdna = 1;
                    cds = 1;
                } else {
                    // with every exon we update cDNA length with the previous
                    // exon length
                    cdna += exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)).getEnd()
                            - exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)).getStart() + 1;
                }
            } else {
                exon = exonDict.get(transcript.getId() + "_" + exon.getExonNumber());
                if (gtf.getFeature().equalsIgnoreCase("CDS")) {
                    if (gtf.getStrand().equals("+") || gtf.getStrand().equals("1")) {
                        // CDS states the beginning of coding start
                        exon.setGenomicCodingStart(gtf.getStart());
                        exon.setGenomicCodingEnd(gtf.getEnd());

                        // cDNA coordinates
                        exon.setCdnaCodingStart(gtf.getStart() - exon.getStart() + cdna);
                        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);

                        exon.setCdsStart(cds);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;

                        // phase calculation
                        if (gtf.getStart() == exon.getStart()) {
                            // retrieve previous exon if exists
                            if (exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)) != null) {
                                Exon e = exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1));
                                if (e.getPhase() == -1) {
                                    exon.setPhase((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3); // (prev-phase+1)%3
                                } else {
                                    exon.setPhase(((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3 + e
                                            .getPhase()) % 3); // (prev-phase+current-phase+1)%3
                                }
                            } else {
                                // if it is the first exon then we just take the
                                // frame
                                if (gtf.getFrame().equals("0")) {
                                    exon.setPhase(Integer.parseInt(gtf.getFrame()));
                                } else {
                                    if (gtf.getFrame().equals("1")) {
                                        exon.setPhase(2);
                                    } else {
                                        exon.setPhase(1);
                                    }
                                }
                            }
                        } else {
                            // if coding start and genomic start is different
                            // then there is UTR: -1
                            exon.setPhase(-1);
                        }

                        if(transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                            transcript.setGenomicCodingStart(gtf.getStart());
                        }
                        if(transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
                            transcript.setGenomicCodingEnd(gtf.getEnd());
                        }
                        // only first time
                        if (transcript.getCdnaCodingStart() == 0) {
                            transcript.setCdnaCodingStart(gtf.getStart() - exon.getStart() + cdna);
                        }
                        // strand -
                    } else {
                        // CDS states the beginning of coding start
                        exon.setGenomicCodingStart(gtf.getStart());
                        exon.setGenomicCodingEnd(gtf.getEnd());

                        // cDNA coordinates
                        exon.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);
                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);

                        exon.setCdsStart(cds);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;

                        // phase calculation
                        if (gtf.getEnd() == exon.getEnd()) {
                            // retrieve previous exon if exists
                            if (exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)) != null) {
                                Exon e = exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1));
                                if (e.getPhase() == -1) {
                                    exon.setPhase((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3); // (prev-phase+1)%3
                                } else {
                                    exon.setPhase(((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3 + e
                                            .getPhase()) % 3); // (prev-phase+current-phase+1)%3
                                }
                            } else {
                                // if it is the first exon then we just take the
                                // frame
                                if (gtf.getFrame().equals("0")) {
                                    exon.setPhase(Integer.parseInt(gtf.getFrame()));
                                } else {
                                    if (gtf.getFrame().equals("1")) {
                                        exon.setPhase(2);
                                    } else {
                                        exon.setPhase(1);
                                    }
                                }
                            }
                        } else {
                            // if coding start and genomic start is different
                            // then there is UTR: -1
                            exon.setPhase(-1);
                        }

                        if(transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                            transcript.setGenomicCodingStart(gtf.getStart());
                        }
                        if(transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
                            transcript.setGenomicCodingEnd(gtf.getEnd());
                        }
                        // only first time
                        if (transcript.getCdnaCodingStart() == 0) {
                            transcript.setCdnaCodingStart(gtf.getStart() - exon.getStart() + cdna);
                        }
                    }

                    // no strand dependent
                    transcript.setProteinID(gtf.getAttributes().get("protein_id"));
                }

                if (gtf.getFeature().equalsIgnoreCase("start_codon")) {
                    // nothing to do
                }

                if (gtf.getFeature().equalsIgnoreCase("stop_codon")) {
                    if (exon.getStrand().equals("+")) {
                        // we need to increment 3 nts, the stop_codon length.
                        exon.setGenomicCodingEnd(gtf.getEnd());
                        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        transcript.setGenomicCodingEnd(gtf.getEnd());
                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        transcript.setCdsLength(cds);
                    } else {
                        // we need to increment 3 nts, the stop_codon length.
                        exon.setGenomicCodingStart(gtf.getStart());
                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        transcript.setGenomicCodingStart(gtf.getStart());
                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        transcript.setCdsLength(cds);
                    }
                }
            }
        }

        // Adding chunksIds
        int chunkStart = (gene.getStart() - 5000) / CHUNK_SIZE;
        int chunkEnd = (gene.getEnd() + 5000) / CHUNK_SIZE;
        for(int i=chunkStart; i<=chunkEnd; i++) {
            gene.getChunkIds().add(gene.getChromosome()+"_"+i+"_"+chunkIdSuffix);
        }
        // last gene must be written
        serializer.serialize(gene);

        // cleaning
        gtfReader.close();
        serializer.close();
        // compress fasta file
        // commented becasue it takes too much time to gzip/gunzip the fasta file
//        Path gunzipedSeqFile = Paths.get(genomeSequenceFilePath.toString().replace(".gz", ""));
//        if(Files.exists(gunzipedSeqFile)) {
//            Process process = Runtime.getRuntime().exec("gzip " + gunzipedSeqFile.toAbsolutePath());
//            process.waitFor();
//        }
    }

    private Map<String, Long> prepareChromosomeSequenceFile(Path genomeSequenceFilePath) throws IOException, InterruptedException {
        if(Files.exists(genomeSequenceFilePath)) {
            Process process = Runtime.getRuntime().exec("gunzip " + genomeSequenceFilePath.toAbsolutePath());
            process.waitFor();
        }
        Map<String, Long> chromOffsets = new HashMap<>(200);
        Path gunzipedChromosomeSequenceFile = Paths.get(genomeSequenceFilePath.toString().replace(".gz", ""));
        if(Files.exists(gunzipedChromosomeSequenceFile)) {
            long offset = 0;
            String chrom;
            String line = null;
            BufferedReader br = FileUtils.newBufferedReader(gunzipedChromosomeSequenceFile, Charset.defaultCharset());
            while ((line = br.readLine()) != null) {
                if(line.startsWith(">")) {
                    chrom = line.split(" ")[0].replace(">", "");
                    chromOffsets.put(chrom, offset);
                }
                offset += line.length() + 1;
            }
            br.close();
//            rafChromosomeSequenceFile = new RandomAccessFile(new File(genomeSequenceFilePath.toString().replace(".gz", "")), "r");
//            for(String s: chromOffsets.keySet()) {
//                System.out.println(chromOffsets.get(s));
//                Long l = chromOffsets.get(s);
//                rafChromosomeSequenceFile.seek(l);
//                String li = rafChromosomeSequenceFile.readLine();
//                System.out.println(li);
//            }
//            rafChromosomeSequenceFile.close();
        }
        return chromOffsets;
    }

    private void updateTranscriptAndGeneCoords(Transcript transcript, Gene gene, Gtf gtf) {
        if (transcript.getStart() > gtf.getStart()) {
            transcript.setStart(gtf.getStart());
        }
        if (transcript.getEnd() < gtf.getEnd()) {
            transcript.setEnd(gtf.getEnd());
        }
        if (gene.getStart() > gtf.getStart()) {
            gene.setStart(gtf.getStart());
        }
        if (gene.getEnd() < gtf.getEnd()) {
            gene.setEnd(gtf.getEnd());
        }
    }

    public Map<String, String> readGenomeSequence(File genomeSequence) {
        Map<String, String> genomeSequenceMap = new HashMap<>();

        return genomeSequenceMap;
    }

    public String getSequenceByChromosomeName(String chrom, Path genomeSequenceDir) throws IOException {
//		File[] files = genomeSequenceDir.listFiles();
        File file = null;
        DirectoryStream<Path> ds = Files.newDirectoryStream(genomeSequenceDir);
        for(Path p: ds) {
            if(p.toFile().getName().endsWith("_"+chrom+".fa.gz") || p.toFile().getName().endsWith("."+chrom+".fa.gz")) {
                System.out.println(p.toAbsolutePath());
                file = p.toFile();
                break;
            }
        }

//		File file = null;
//		for(File f: files) {
//			if(f.getName().endsWith("_"+chrom+".fa.gz") || f.getName().endsWith("."+chrom+".fa.gz")) {
//				System.out.println(f.getAbsolutePath());
//				file = f;
//				break;
//			}
//		}

        StringBuilder sb = new StringBuilder(100000);
        if(file != null) {
            //		BufferedReader br = Files.newBufferedReader(files[0].toPath(), Charset.defaultCharset());
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            String line = "";
            boolean found = false;
            while((line = br.readLine()) != null) {
                if(found) {
                    if(!line.startsWith(">")) {
                        sb.append(line);
                    }else {
                        break;
                    }
                }
                if(line.startsWith(">")) {
                    found = true;
                }
            }
            br.close();
        }
        return sb.toString();
    }

    public String getSequenceByChromosome(String chrom, Path genomeSequenceFile) throws IOException {
        BufferedReader br = FileUtils.newBufferedReader(genomeSequenceFile, Charset.defaultCharset());
        StringBuilder sb = new StringBuilder(100000);
        String line = "";
        boolean found = false;
        while((line = br.readLine()) != null) {
            if(found) {
                if(!line.startsWith(">")) {
                    sb.append(line);
                }else {
                    break;
                }
            }
            if(line.startsWith(">"+chrom+" ")) {
                found = true;
            }
        }
        br.close();
        return sb.toString();
    }

    public String getSequenceByChromosome(String chrom, Map<String, Long> chromOffsets, Path genomeSequenceFile) throws IOException {
//        BufferedReader br = FileUtils.newBufferedReader(genomeSequenceFile, Charset.defaultCharset());
        RandomAccessFile rafChromosomeSequenceFile = new RandomAccessFile(new File(genomeSequenceFile.toString().replace(".gz", "")), "r");
        rafChromosomeSequenceFile.seek(chromOffsets.get(chrom));
        StringBuilder sb = new StringBuilder(100000);
        String line = "";

        // first line contains the chromosome info line from fasta file
        // we must consume it
        rafChromosomeSequenceFile.readLine();
        while((line = rafChromosomeSequenceFile.readLine()) != null) {
            if(!line.startsWith(">")) {
                sb.append(line);
            }else {
                break;
            }
        }
        rafChromosomeSequenceFile.close();
        return sb.toString();
    }

    private Map<String, MiRNAGene> getmiRNAGeneMap(Path mirnaGeneFile) throws IOException {
        Map<String, MiRNAGene> mirnaGeneMap = new HashMap<>(3000);
        BufferedReader br = Files.newBufferedReader(mirnaGeneFile, Charset.defaultCharset());
        String line = "";
        String[] fields, mirnaMatures, mirnaMaturesFields;
        List<String> aliases;
        MiRNAGene miRNAGene;
        while((line = br.readLine()) != null) {
            fields = line.split("\t");

            // First, read aliases of miRNA, field #5
            aliases = new ArrayList<>();
            for(String alias: fields[5].split(",")) {
                aliases.add(alias);
            }

            miRNAGene = new MiRNAGene(fields[1], fields[2], fields[3], fields[4], aliases, new ArrayList<MiRNAGene.MiRNAMature>());

            // Second, read the miRNA matures, field #6
            mirnaMatures = fields[6].split(",");
            for(String s: mirnaMatures) {
//				System.out.println(s);
                mirnaMaturesFields = s.split("\\|");
//				System.out.println("\t"+Arrays.toString(mirnaMaturesFields));
                // Save directly into MiRNAGene object.
                miRNAGene.addMiRNAMature(mirnaMaturesFields[0], mirnaMaturesFields[1], mirnaMaturesFields[2]);
            }

            // Add object to Map<EnsemblID, MiRNAGene>
            mirnaGeneMap.put(fields[0], miRNAGene);
        }
        br.close();
        return mirnaGeneMap;
    }

    class GeneMongoDB extends Gene {
        private List<String> chunkIds;

        public GeneMongoDB() {
            chunkIds = new ArrayList<>(50);
        }

        public GeneMongoDB(String id, String name, String biotype,
                           String status, String chromosome, Integer start, Integer end,
                           String strand, String source, String description,
                           List<Transcript> transcripts, MiRNAGene mirna) {
            super(id, name, biotype, status, chromosome, start, end, strand, source,
                    description, transcripts, mirna);
            chunkIds = new ArrayList<>(50);
        }

        public List<String> getChunkIds() {
            return chunkIds;
        }

        public void setChunkIds(List<String> chunkIds) {
            this.chunkIds = chunkIds;
        }

    }

    public void parseGff3ToJson(File getFile, File geneDescriptionFile, File xrefsFile, File outJsonFile)
            throws IOException, SecurityException, NoSuchMethodException, FileFormatException {

        Map<String, Gene> genes = new HashMap<String, Gene>();
        Map<String, Transcript> transcripts = new HashMap<String, Transcript>();

        Map<String, String> attributes = new HashMap<>();

        System.out.println("READ FILE START::::::::::::::::::::::::::");
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(getFile));
        System.out.println(br.readLine());
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#") || !line.contains("\t"))
                continue;

            String fields[] = line.split("\t", -1);
            String group[] = fields[8].split(";");

            String id = group[0].split("=")[1];
            String name = "";
            String parent = "";
            String chromosome = fields[0].replace("_Cc_182", "");
            int start = Integer.parseInt(fields[3]);
            int end = Integer.parseInt(fields[4]);
            String strand = fields[6];
            String feature = fields[2];

            // parsing attributres, column 9
            attributes.clear();
            String[] atrrFields = fields[8].split(";");
            String[] attrKeyValue;
            for (String attrField : atrrFields) {
                attrKeyValue = attrField.split("=");
                attributes.put(attrKeyValue[0].toLowerCase(), attrKeyValue[1]);
            }

            if (feature.equalsIgnoreCase("CDS")) {
                name = "";
                // parent = group[1].split("=")[1];
                parent = attributes.get("parent");
                int phase = Integer.parseInt(fields[7]);
                Transcript t = transcripts.get(parent);

                Exon e = new Exon();
                e.setId(id);
                e.setChromosome(chromosome);
                e.setStart(start);
                e.setEnd(end);
                e.setStrand(strand);
                e.setPhase(phase);

                e.setGenomicCodingStart(start);
                e.setGenomicCodingEnd(end);

                // // just in case...
                // if(t.getExons() == null) {
                // t.setExons(new ArrayList<Exon>());
                // }
                //
                // // before adding
                // if(t.getExons().size() > 0) {
                // if(strand.equals("1") || strand.equals("+")) {
                // Exon lastExon = t.getExons().get(t.getExons().size()-1);
                // if(lastExon.getEnd() == e.getStart()-1) {
                // lastExon.setEnd(e.getEnd());
                // lastExon.setId(e.getId());
                // lastExon.setGenomicCodingStart(e.getStart());
                // lastExon.setGenomicCodingEnd(e.getEnd());
                // }else {
                // t.getExons().add(e);
                // }
                // }else { // negative strand
                //
                // }
                // }else {
                // t.getExons().add(e);
                // }

                t.getExons().add(e);

                // Collections.sort(list, new FeatureComparable());

            }
            if (feature.equalsIgnoreCase("five_prime_UTR") || feature.equalsIgnoreCase("three_prime_UTR")) {

                // name = "";
                // parent = group[1].split("=")[1];
                // FivePrimeUtr fivePrimeUtr = new FivePrimeUtr(id, chromosome,
                // start, end, strand);
                // t.getFivePrimeUtr().add(fivePrimeUtr);
                parent = attributes.get("parent");
                // int phase = Integer.parseInt(fields[7]);
                Transcript t = transcripts.get(parent);

                Exon e = new Exon();
                e.setId(id);
                e.setChromosome(chromosome);
                e.setStart(start);
                e.setEnd(end);
                e.setStrand(strand);
                // e.setPhase(phase);

                e.setGenomicCodingStart(start);
                e.setGenomicCodingEnd(end);
                t.getExons().add(e);

            }
            // if (feature.equalsIgnoreCase("three_prime_UTR")) {
            // name = "";
            // parent = group[1].split("=")[1];

            // Transcript t = transcriptsId.get(parent);
            // ThreePrimeUtr threePrimeUtr = new ThreePrimeUtr(id,
            // chromosome, start, end, strand);
            // t.getThreePrimeUtr().add(threePrimeUtr);
            // }
            if (feature.equalsIgnoreCase("mRNA")) {
                id = group[0].split("=")[1];
                name = group[1].split("=")[1];
                parent = group[4].split("=")[1];

                Transcript tr = new Transcript();
                tr.setExons(new ArrayList<Exon>());
                tr.setXrefs(new ArrayList<Xref>());
                tr.setId(id);
                tr.setName(name);
                tr.setBiotype("");
                tr.setStatus("");
                tr.setChromosome(chromosome);
                tr.setStart(start);
                tr.setEnd(end);
                tr.setGenomicCodingStart(start);
                tr.setGenomicCodingStart(end);
                tr.setStrand(strand);

                transcripts.put(id, tr);
                genes.get(parent).getTranscripts().add(tr);
            }
            if (feature.equalsIgnoreCase("gene")) {

                name = group[1].split("=")[1];
                Gene g = new Gene(id, name, "", "", chromosome, start, end, strand, "JGI", "",
                        new ArrayList<Transcript>(), null);
                // g.setTranscripts(new ArrayList<Transcript>());
                // g.setId(id);
                // g.setBiotype("");
                // g.setStatus("");
                // g.setName(name);
                // g.setChromosome(chromosome);
                // g.setStart(start);
                // g.setEnd(end);
                // g.setStrand(strand);

                genes.put(id, g);
            }

        }
        br.close();

        // Reorder
        for (String geneId : genes.keySet()) {
            Gene gene = genes.get(geneId);
            for (Transcript transcript : gene.getTranscripts()) {
                Collections.sort(transcript.getExons(), new FeatureComparable());

                Exon prevExon = null;
                List<Exon> toRemove = new ArrayList<Exon>();
                for (Exon exon : transcript.getExons()) {
                    if (prevExon != null) {

                        String strand = exon.getStrand();
                        if (strand.equals("1") || strand.equals("+")) {

                            if (prevExon.getEnd() == exon.getStart() - 1) {
                                if (prevExon.getId().contains("five_prime_UTR")) {
                                    exon.setStart(prevExon.getStart());
//									transcript.setGenomicCodingStart(exon.getGenomicCodingStart());
                                    toRemove.add(prevExon);
                                }
                                if (exon.getId().contains("three_prime_UTR")) {
                                    prevExon.setEnd(exon.getEnd());
//									transcript.setGenomicCodingEnd(prevExon.getGenomicCodingEnd());
                                    toRemove.add(exon);
                                }
                            }

                        } else { // negative strand

                            if (prevExon.getEnd() == exon.getStart() - 1) {
                                if (prevExon.getId().contains("three_prime_UTR")) {
                                    exon.setStart(prevExon.getStart());
//									transcript.setGenomicCodingStart(exon.getGenomicCodingStart());
                                    toRemove.add(prevExon);
                                }
                                if (exon.getId().contains("five_prime_UTR")) {
                                    prevExon.setEnd(exon.getEnd());
//									transcript.setGenomicCodingEnd(prevExon.getGenomicCodingEnd());
                                    toRemove.add(exon);
                                }
                            }
                        }
                    }

                    prevExon = exon;
                }
                for (Exon primeUTR : toRemove) {
                    transcript.getExons().remove(primeUTR);
                }

                //Update genomic coding region start and end on transcripts
                int i = 1;
                Exon e = transcript.getExons().get(0);
                while(e.getId().contains("prime_UTR")){
                    e = transcript.getExons().get(i);
                    i++;
                }
                transcript.setGenomicCodingStart(e.getGenomicCodingStart());

                int exonSize = transcript.getExons().size();
                int j = exonSize-2;
                Exon ee = transcript.getExons().get(exonSize-1);
                while(ee.getId().contains("prime_UTR")){
                    ee = transcript.getExons().get(j);
                    j--;
                }
                transcript.setGenomicCodingEnd(ee.getGenomicCodingEnd());

            }
        }

//		Gson gson = new Gson();
//		TextFileWriter tfw = new TextFileWriter(outJsonFile.getAbsolutePath());
        BufferedWriter bw = Files.newBufferedWriter(outJsonFile.toPath(), Charset.defaultCharset());

        System.out.println("");
        System.out.println("START WRITE");
        for (String geneId : genes.keySet()) {
            Gene gene = genes.get(geneId);
//			tfw.writeStringToFile(gson.writeValueAsString(gene));
//			tfw.writeStringToFile("\n");
        }
        bw.close();


//		System.out.println(gson.toJson(genes.get("Ciclev10007224m.g")));
//		System.out.println(gson.writeValueAsString(genes.get("Ciclev10008515m.g")));
//		System.out.println(gson.writeValueAsString(genes.get("Ciclev10007219m.g")));
        System.out.println("END WRITE");
    }

    private class FeatureComparable implements Comparator<Object> {
        @Override
        public int compare(Object exon1, Object exon2) {
            return ((Exon) exon1).getStart() - ((Exon) exon2).getStart();
        }
    }
}
