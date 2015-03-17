package org.opencb.cellbase.app.transform;

import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.formats.feature.gtf.io.GtfReader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.opencb.biodata.models.core.*;
import org.opencb.cellbase.app.serializers.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GeneParser extends CellBaseParser {

    private Map<String, Integer> transcriptDict;
    private Map<String, Exon> exonDict;


    private Path gtfFile;
    private Path proteinFastaFile;
    private Path cDnaFastaFile ;
    private Path geneDescriptionFile;
    private Path xrefsFile;
    private Path uniprotIdMappingFile;
    private Path tfbsFile;
    private Path mirnaFile;
    private Path genomeSequenceFilePath;

    private Connection sqlConn;
    private PreparedStatement sqlInsert, sqlQuery;

    private int CHUNK_SIZE = 2000;
    private String chunkIdSuffix = CHUNK_SIZE/1000 + "k";



    public GeneParser(Path geneDirectoryPath, Path genomeSequenceFastaFile, CellBaseSerializer serializer) {
        this(null, geneDirectoryPath.resolve("description.txt"), geneDirectoryPath.resolve("xrefs.txt"),
                geneDirectoryPath.resolve("idmapping_selected.tab.gz"), geneDirectoryPath.resolve("tfbs.txt"),
                geneDirectoryPath.resolve("mirna.txt"), genomeSequenceFastaFile, serializer);
        getGtfFileFromGeneDirectoryPath(geneDirectoryPath);
        getProteinFastaFileFromGeneDirectoryPath(geneDirectoryPath);
        getCDnaFastaFileFromGeneDirectoryPath(geneDirectoryPath);
    }

    public GeneParser(Path gtfFile, Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile, Path tfbsFile, Path mirnaFile, Path genomeSequenceFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.gtfFile = gtfFile;
        this.geneDescriptionFile = geneDescriptionFile;
        this.xrefsFile = xrefsFile;
        this.uniprotIdMappingFile = uniprotIdMappingFile;
        this.tfbsFile = tfbsFile;
        this.mirnaFile = mirnaFile;
        this.genomeSequenceFilePath = genomeSequenceFilePath;

        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);
    }

    public void parse()
            throws IOException, SecurityException, NoSuchMethodException, FileFormatException, InterruptedException {
        Files.exists(gtfFile);

        String geneId;
        String transcriptId;
        String currentChromosome = "";
        String chromSequence = "";
        String exonSequence = "";

        Gene gene = null;
        Transcript transcript;
        Exon exon = null;

        int cdna = 1;
        int cds = 1;
        String[] fields;


//        Map<String, String> gseq = GenomeSequenceUtils.getGenomeSequence(genomeSequenceDir);

        /**
         Loading Gene Description data
         */
        Map<String, String> geneDescriptionMap = new HashMap<>();
        if (geneDescriptionFile != null && Files.exists(geneDescriptionFile)) {
            List<String> lines = Files.readAllLines(geneDescriptionFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                geneDescriptionMap.put(fields[0], fields[1]);
            }
        }

        /**
         Loading Gene Xref data
         */
        Map<String, ArrayList<Xref>> xrefMap = new HashMap<>();
        if (xrefsFile != null && Files.exists(xrefsFile)) {
            List<String> lines = Files.readAllLines(xrefsFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                if (fields.length >= 4) {
                    if (!xrefMap.containsKey(fields[0])) {
                        xrefMap.put(fields[0], new ArrayList<Xref>());
                    }
                    xrefMap.get(fields[0]).add(new Xref(fields[1], fields[2], fields[3]));
                }
            }
        }

        /**
         Loading Protein mapping into Xref data
         */
        if (uniprotIdMappingFile != null && Files.exists(uniprotIdMappingFile)) {
            BufferedReader br = FileUtils.newBufferedReader(uniprotIdMappingFile);
            String line;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t", -1);
                if (fields.length >= 20 && fields[20].startsWith("ENST")) {
                    if (!xrefMap.containsKey(fields[20])) {
                        xrefMap.put(fields[20], new ArrayList<Xref>());
                    }
                    xrefMap.get(fields[20]).add(new Xref(fields[0], "uniprotkb_acc", "UniProtKB ACC"));
                    xrefMap.get(fields[20]).add(new Xref(fields[1], "uniprotkb_id", "UniProtKB ID"));
                }
            }
            br.close();
        }

        /**
         * Load ENSEMBL's protein sequences
         */
        logger.info("Loading ENSEMBL's protein sequences...");
        Map<String, Fasta> proteinSequencesMap = new HashMap<>();
        if(proteinFastaFile != null && Files.exists(proteinFastaFile) &&
                !Files.isDirectory(proteinFastaFile)) {
            FastaReader fastaReader = new FastaReader(proteinFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for(Fasta fasta : fastaList) {
                proteinSequencesMap.put(fasta.getDescription().split("transcript:")[1].split("\\s")[0], fasta);
            }
        }

        /**
         * Load ENSEMBL's cDNA sequences
         */
        logger.info("Loading ENSEMBL's cDNA sequences...");
        Map<String, Fasta> cDnaSequencesMap = new HashMap<>();
        if(cDnaFastaFile != null && Files.exists(cDnaFastaFile) &&
                !Files.isDirectory(cDnaFastaFile)) {
            FastaReader fastaReader = new FastaReader(cDnaFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for(Fasta fasta : fastaList) {
                cDnaSequencesMap.put(fasta.getId(), fasta);
            }
        }
        logger.info("Done.");

        /*
            Loading Gene Description data
         */
        Map<String, ArrayList<TranscriptTfbs>> tfbsMap = new HashMap<>();
        if (tfbsFile != null && Files.exists(tfbsFile) && !Files.isDirectory(tfbsFile)) {
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
        if (mirnaFile != null && Files.exists(mirnaFile) && !Files.isDirectory(mirnaFile)) {
            mirnaGeneMap = getmiRNAGeneMap(mirnaFile);
        }

        // Preparing the fasta file for fast accessing
        // File must be ungunzipped and offsets stored
        // Commented because it takes too much time to gzip/gunzip
//        Map<String, Long> chromSequenceOffsets = prepareChromosomeSequenceFile(genomeSequenceFilePath);
        try {
            connect(genomeSequenceFilePath);
//            indexReferenceGenomeFasta(genomeSequenceFilePath);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return;
        }

        // Empty transcript and exon dictionaries
        transcriptDict.clear();
        exonDict.clear();

        logger.info("Parsing gtf...");
        GtfReader gtfReader = new GtfReader(gtfFile);
        Gtf gtf;
        while ((gtf = gtfReader.read()) != null) {

            if (gtf.getFeature().equals("gene") || gtf.getFeature().equals("transcript") || gtf.getFeature().equals("UTR") || gtf.getFeature().equals("Selenocysteine")) {
                continue;
            }

            geneId = gtf.getAttributes().get("gene_id");
            transcriptId = gtf.getAttributes().get("transcript_id");

            /**
             * If chromosome is changed (or it's the first chromosome)
             * we load the new chromosome sequence.
             */
            if (!currentChromosome.equals(gtf.getSequenceName()) && !gtf.getSequenceName().startsWith("GL") && !gtf.getSequenceName().startsWith("HS") && !gtf.getSequenceName().startsWith("HG")) {
                currentChromosome = gtf.getSequenceName();
//                chromSequence = getSequenceByChromosome(currentChromosome, genomeSequenceFilePath);
//                chromSequence = getSequenceByChromosome(currentChromosome, chromSequenceOffsets, genomeSequenceFilePath);
//				chromSequence = getSequenceByChromosomeName(currentChromosome, genomeSequenceDir);
            }

            // Gene object can only be null the first time
            // If new geneId is different from the current then we must serialize before load new gene
            if (gene == null || !geneId.equals(gene.getId())) {
                // gene object can only be null the first time
                if (gene != null) { // genes.size()>0
                    logger.debug("Serializing gene {}", geneId);
                    serializer.serialize(gene);
                }

                gene = new Gene(geneId, gtf.getAttributes().get("gene_name"), gtf.getAttributes().get("gene_biotype"),
                        "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
                        gtf.getStrand(), "Ensembl", geneDescriptionMap.get(geneId), new ArrayList<Transcript>(), mirnaGeneMap.get(geneId));
                // Do not change order!! size()-1 is the index of the gene ID
            }

            // Check if Transcript exist in the Gene Set of transcripts
            if (!transcriptDict.containsKey(transcriptId)) {
                transcript = new Transcript(transcriptId, gtf.getAttributes().get("transcript_name"), gtf.getSource(),
                        "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
                        gtf.getStrand(), 0, 0, 0, 0, 0, "", "", xrefMap.get(transcriptId), new ArrayList<Exon>(), tfbsMap.get(transcriptId));
                String tags;
                if((tags = gtf.getAttributes().get("tag"))!=null) {
                    transcript.setAnnotationFlags(new HashSet<String>(Arrays.asList(tags.split(","))));
                }

                Fasta proteinFasta;
                if((proteinFasta=proteinSequencesMap.get(transcriptId))!=null) {
                    transcript.setProteinSequence(proteinFasta.getSeq());
                }
                Fasta cDnaFasta;
                if((cDnaFasta=cDnaSequencesMap.get(transcriptId))!=null) {
                    transcript.setcDnaSequence(cDnaFasta.getSeq());
                }
                gene.getTranscripts().add(transcript);
                // Do not change order!! size()-1 is the index of the transcript ID
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
                if(currentChromosome.equals(gtf.getSequenceName()) ) { //&& chromSequence.length() > 0
                    // as starts is inclusive and position begins in 1 we must -1, end is OK.
//                    exonSequence = chromSequence.substring(gtf.getStart()-1, gtf.getEnd());
//                    System.out.println("exonSequence = " + exonSequence);
                    try {
                        exonSequence = getExonSequence(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());
                    } catch (SQLException e) {
                        System.out.println(gtf.getSequenceName()+", start: "+gtf.getStart()+", end: "+gtf.getEnd());
                        e.printStackTrace();
                    }
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
                    // with every exon we update cDNA length with the previous exon length
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
                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);  // Set cdnaCodingEnd to prevent those cases without stop_codon

                        exon.setCdsStart(cds);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;
                        transcript.setCdsLength(cds-1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

                        exon.setPhase(Integer.valueOf(gtf.getFrame()));
//                        // phase calculation
//                        if (gtf.getStart() == exon.getStart()) {
//                            // retrieve previous exon if exists
//                            if (exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)) != null) {
//                                Exon e = exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1));
//                                if (e.getPhase() == -1) {
//                                    exon.setPhase((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3); // (prev-phase+1)%3
//                                } else {
//                                    exon.setPhase(((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3 + e
//                                            .getPhase()) % 3); // (prev-phase+current-phase+1)%3
//                                }
//                            } else {
//                                // if it is the first exon then we just take the
//                                // frame
//                                if (gtf.getFrame().equals("0")) {
//                                    exon.setPhase(Integer.parseInt(gtf.getFrame()));
//                                } else {
//                                    if (gtf.getFrame().equals("1")) {
//                                        exon.setPhase(2);
//                                    } else {
//                                        exon.setPhase(1);
//                                    }
//                                }
//                            }
//                        } else {
//                            // if coding start and genomic start is different
//                            // then there is UTR: -1
//                            exon.setPhase(-1);
//                        }

                        if (transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                            transcript.setGenomicCodingStart(gtf.getStart());
                        }
                        if (transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
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
                        exon.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);  // cdnaCodingStart points to the same base position than genomicCodingEnd
                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);  // cdnaCodingEnd points to the same base position than genomicCodingStart
                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);  // Set cdnaCodingEnd to prevent those cases without stop_codon

                        exon.setCdsStart(cds);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;
                        transcript.setCdsLength(cds-1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

                        exon.setPhase(Integer.valueOf(gtf.getFrame()));
//                        // phase calculation
//                        if (gtf.getEnd() == exon.getEnd()) {
//                            // retrieve previous exon if exists
//                            if (exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)) != null) {
//                                Exon e = exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1));
//                                if (e.getPhase() == -1) {
//                                    exon.setPhase((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3); // (prev-phase+1)%3
//                                } else {
//                                    exon.setPhase(((e.getCdnaCodingEnd() - e.getCdnaCodingStart() + 1) % 3 + e
//                                            .getPhase()) % 3); // (prev-phase+current-phase+1)%3
//                                }
//                            } else {
//                                // if it is the first exon then we just take the
//                                // frame
//                                if (gtf.getFrame().equals("0")) {
//                                    exon.setPhase(Integer.parseInt(gtf.getFrame()));
//                                } else {
//                                    if (gtf.getFrame().equals("1")) {
//                                        exon.setPhase(2);
//                                    } else {
//                                        exon.setPhase(1);
//                                    }
//                                }
//                            }
//                        } else {
//                            // if coding start and genomic start is different
//                            // then there is UTR: -1
//                            exon.setPhase(-1);
//                        }

                        if (transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                            transcript.setGenomicCodingStart(gtf.getStart());
                        }
                        if (transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
                            transcript.setGenomicCodingEnd(gtf.getEnd());
                        }
                        // only first time
                        if (transcript.getCdnaCodingStart() == 0) {
                            transcript.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);  // cdnaCodingStart points to the same base position than genomicCodingEnd
                        }
                    }

                    // no strand dependent
                    transcript.setProteinID(gtf.getAttributes().get("protein_id"));
                }

                if (gtf.getFeature().equalsIgnoreCase("start_codon")) {
                    // nothing to do
                }

                if (gtf.getFeature().equalsIgnoreCase("stop_codon")) {
//                    setCdnaCodingEnd = false; // stop_codon found, cdnaCodingEnd will be set here, no need to set it at the beginning of next feature
                    if (exon.getStrand().equals("+")) {
                        // we need to increment 3 nts, the stop_codon length.
                        exon.setGenomicCodingEnd(gtf.getEnd());
                        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        // If stop_codon appears, overwrite values
                        transcript.setGenomicCodingEnd(gtf.getEnd());
                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        transcript.setCdsLength(cds-1);
                    } else {
                        // we need to increment 3 nts, the stop_codon length.
                        exon.setGenomicCodingStart(gtf.getStart());
                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);  // cdnaCodingEnd points to the same base position than genomicCodingStart
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        // If stop_codon appears, overwrite values
                        transcript.setGenomicCodingStart(gtf.getStart());
                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);  // cdnaCodingEnd points to the same base position than genomicCodingStart
                        transcript.setCdsLength(cds-1);
                    }
                }
            }
        }

        // last gene must be serialized
        serializer.serialize(gene);

        // cleaning
        gtfReader.close();
        serializer.close();

        try {
            disconnectSqlite();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // compress fasta file
        // commented becasue it takes too much time to gzip/gunzip the fasta file
//        Path gunzipedSeqFile = Paths.get(genomeSequenceFilePath.toString().replace(".gz", ""));
//        if(Files.exists(gunzipedSeqFile)) {
//            Process process = Runtime.getRuntime().exec("gzip " + gunzipedSeqFile.toAbsolutePath());
//            process.waitFor();
//        }
    }

    private void connect(Path genomeSequenceFilePath) throws ClassNotFoundException, SQLException, IOException {
        /**
         * Preparation of the SQLite database
         */
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + genomeSequenceFilePath.getParent().toString() + "/reference_genome.db");
        if(!Files.exists(Paths.get(genomeSequenceFilePath.getParent().toString(), "reference_genome.db")) ||
                Files.size(genomeSequenceFilePath.getParent().resolve("reference_genome.db")) == 0) {
            Statement createTable = sqlConn.createStatement();
            createTable.executeUpdate("CREATE TABLE if not exists  genome_sequence (sequenceName VARCHAR(50), chunkId VARCHAR(30), start INT, end INT, sequence VARCHAR(2000))");
            sqlInsert = sqlConn.prepareStatement("INSERT INTO genome_sequence (chunkID, start, end, sequence) values (?, ?, ?, ?)");
            indexReferenceGenomeFasta(genomeSequenceFilePath);
        }
        sqlQuery = sqlConn.prepareStatement("SELECT sequence from genome_sequence WHERE chunkId = ? "); //AND start <= ? AND end >= ?
    }

    private void disconnectSqlite() throws SQLException {
        sqlConn.close();
    }

    private void indexReferenceGenomeFasta(Path genomeSequenceFilePath) throws IOException, ClassNotFoundException, SQLException {

//        if(!Files.exists(Paths.get(genomeSequenceFilePath.getParent().toString(), "reference_genome.db"))) {
        BufferedReader bufferedReader = FileUtils.newBufferedReader(genomeSequenceFilePath);

//            Statement createTable = sqlConn.createStatement();
//            createTable.executeUpdate("CREATE TABLE if not exists  genome_sequence (sequenceName VARCHAR(50), chunkId VARCHAR(30), start INT, end INT, sequence VARCHAR(2000))");

        // Some parameters initialization
        String sequenceName = "";
//            String sequenceAssembly = "";
        boolean haplotypeSequenceType = true;
        int chunk = 0;
        int start = 1;
        int end = CHUNK_SIZE - 1;
        String sequence;
        String chunkSequence;

        StringBuilder sequenceStringBuilder = new StringBuilder();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            /**
             * We accumulate the complete sequence in a StringBuilder
             */
            if (!line.startsWith(">")) {
                sequenceStringBuilder.append(line);
            } else {
                /**
                 * New sequence has been found and we must insert it into SQLite.
                 * Note that the first time there is no sequence. Only not HAP sequences are stored.
                 */
                chunk = 0;
                start = 1;
                end = CHUNK_SIZE - 1;
                if (sequenceStringBuilder.length() > 0) {
                    // if the sequence read is not HAP then we stored in sqlite
                    if(!haplotypeSequenceType && !sequenceName.contains("PATCH") && !sequenceName.contains("HSCHR")) {
                        System.out.println(sequenceName);

                        //chromosome sequence length could shorter than CHUNK_SIZE
                        if (sequenceStringBuilder.length() < CHUNK_SIZE) {
//                                chunkSequence = sequenceStringBuilder.toString();
//                                genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome+"_"+0+"_"+chunkIdSuffix, start, sequence.length() - 1, sequenceType, sequenceAssembly, chunkSequence);
                            sqlInsert.setString(1, sequenceName+"_"+chunk+"_"+chunkIdSuffix);
                            sqlInsert.setInt(2, start);
                            sqlInsert.setInt(3, sequenceStringBuilder.length() - 1);
                            sqlInsert.setString(4, sequenceStringBuilder.toString());

                            start += CHUNK_SIZE - 1;
                            // Sequence to store is larger than CHUNK_SIZE
                        } else {
                            int sequenceLength = sequenceStringBuilder.length();

                            sqlConn.setAutoCommit(false);
                            while (start < sequenceLength) {
                                if (chunk % 10000 == 0 && chunk != 0) {
                                    System.out.println("Sequence: " + sequenceName + ", chunkId:" + chunk);
                                    sqlInsert.executeBatch();
                                    sqlConn.commit();
                                }

                                // chunkId is common for all the options
                                sqlInsert.setString(1, sequenceName+"_"+chunk+"_"+chunkIdSuffix);
                                if (start == 1) {   // First chunk of the chromosome
                                    // First chunk contains CHUNK_SIZE-1 nucleotides as index start at position 1 but must end at 1999
//                                        chunkSequence = sequenceStringBuilder.substring(start - 1, CHUNK_SIZE - 1);
//                                        genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome+"_"+chunk+"_"+chunkIdSuffix, start, end, sequenceType, sequenceAssembly, chunkSequence);
                                    sqlInsert.setInt(2, start);
                                    sqlInsert.setInt(3, end);
                                    sqlInsert.setString(4, sequenceStringBuilder.substring(start - 1, CHUNK_SIZE - 1));

                                    start += CHUNK_SIZE - 1;
                                } else {    // Regular chunk
                                    if ((start + CHUNK_SIZE) < sequenceLength) {
//                                            chunkSequence = sequenceStringBuilder.substring(start - 1, start + CHUNK_SIZE - 1);
//                                            genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome+"_"+chunk+"_"+chunkIdSuffix, start, end, sequenceType, sequenceAssembly, chunkSequence);
                                        sqlInsert.setInt(2, start);
                                        sqlInsert.setInt(3, end);
                                        sqlInsert.setString(4, sequenceStringBuilder.substring(start - 1, start + CHUNK_SIZE - 1));
                                        start += CHUNK_SIZE;
                                    } else {    // Last chunk of the chromosome
//                                            chunkSequence = sequenceStringBuilder.substring(start - 1, sequenceLength);
//                                            genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome+"_"+chunk+"_"+chunkIdSuffix, start, sequence.length(), sequenceType, sequenceAssembly, chunkSequence);
                                        sqlInsert.setInt(2, start);
                                        sqlInsert.setInt(3, sequenceLength);
                                        sqlInsert.setString(4, sequenceStringBuilder.substring(start - 1, sequenceLength));
                                        start = sequenceLength;
                                    }
                                }
                                // we add the inserts in a batch
                                sqlInsert.addBatch();

                                end = start + CHUNK_SIZE - 1;
                                chunk++;
                            }

                            sqlInsert.executeBatch();
                            sqlConn.commit();

                            sqlConn.setAutoCommit(true);
                        }
                    }
                }

                // initialize data structures
                sequenceName = line.replace(">", "").split(" ")[0];
                haplotypeSequenceType = line.endsWith("HAP");
//                    sequenceAssembly = line.replace(">", "").split(" ")[2].split(":")[1];
                sequenceStringBuilder.delete(0, sequenceStringBuilder.length());
            }
        }

        bufferedReader.close();

        Statement stm = sqlConn.createStatement();
        stm.executeUpdate("CREATE INDEX chunkkId_idx on genome_sequence(chunkId)");
//            sqlConn.commit();

//        }else {
//            System.out.println("File found: " + Paths.get(genomeSequenceFilePath.getParent().toString(), "reference_genome.db"));
//        }

    }

    private String getExonSequence(String sequenceName, int start, int end) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        ResultSet rs;
        int regionChunkStart = getChunk(start);
        int regionChunkEnd = getChunk(end);
        for (int chunkId = regionChunkStart; chunkId <= regionChunkEnd; chunkId++) {
            sqlQuery.setString(1, sequenceName + "_" + chunkId + "_" + chunkIdSuffix);
            rs = sqlQuery.executeQuery();
            stringBuilder.append(rs.getString(1));
        }

        int startStr = getOffset(start);
        int endStr = getOffset(start) + (end - start) + 1;
        String subStr = "";
        if (getChunk(start) > 0) {
            if (stringBuilder.toString().length() > 0 && stringBuilder.toString().length() >= endStr) {
                subStr = stringBuilder.toString().substring(startStr, endStr);
            }
        } else {
            if (stringBuilder.toString().length() > 0 && stringBuilder.toString().length() + 1 >= endStr) {
                subStr = stringBuilder.toString().substring(startStr - 1, endStr - 1);
            }
        }
        return subStr;
    }

    private int getChunk(int position) {
        return (position / CHUNK_SIZE);
    }

    private int getOffset(int position) {
        return (position % CHUNK_SIZE);
    }

    private Map<String, Long> prepareChromosomeSequenceFile(Path genomeSequenceFilePath) throws IOException, InterruptedException {
        if (Files.exists(genomeSequenceFilePath)) {
            Process process = Runtime.getRuntime().exec("gunzip " + genomeSequenceFilePath.toAbsolutePath());
            process.waitFor();
        }
        Map<String, Long> chromOffsets = new HashMap<>(200);
        Path gunzipedChromosomeSequenceFile = Paths.get(genomeSequenceFilePath.toString().replace(".gz", ""));
        if (Files.exists(gunzipedChromosomeSequenceFile)) {
            long offset = 0;
            String chrom;
            String line = null;
            BufferedReader br = FileUtils.newBufferedReader(gunzipedChromosomeSequenceFile, Charset.defaultCharset());
            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
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
        for (Path p : ds) {
            if (p.toFile().getName().endsWith("_" + chrom + ".fa.gz") || p.toFile().getName().endsWith("." + chrom + ".fa.gz")) {
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
        if (file != null) {
            //		BufferedReader br = Files.newBufferedReader(files[0].toPath(), Charset.defaultCharset());
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            String line = "";
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

    public String getSequenceByChromosome(String chrom, Path genomeSequenceFile) throws IOException {
        BufferedReader br = FileUtils.newBufferedReader(genomeSequenceFile, Charset.defaultCharset());
        StringBuilder sb = new StringBuilder(100000);
        String line = "";
        boolean found = false;
        while ((line = br.readLine()) != null) {
            if (found) {
                if (!line.startsWith(">")) {
                    sb.append(line);
                } else {
                    break;
                }
            }
            if (line.startsWith(">" + chrom + " ")) {
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
        while ((line = rafChromosomeSequenceFile.readLine()) != null) {
            if (!line.startsWith(">")) {
                sb.append(line);
            } else {
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
        while ((line = br.readLine()) != null) {
            fields = line.split("\t");

            // First, read aliases of miRNA, field #5
            aliases = new ArrayList<>();
            for (String alias : fields[5].split(",")) {
                aliases.add(alias);
            }

            miRNAGene = new MiRNAGene(fields[1], fields[2], fields[3], fields[4], aliases, new ArrayList<MiRNAGene.MiRNAMature>());

            // Second, read the miRNA matures, field #6
            mirnaMatures = fields[6].split(",");
            for (String s : mirnaMatures) {
                mirnaMaturesFields = s.split("\\|");
                // Save directly into MiRNAGene object.
                miRNAGene.addMiRNAMature(mirnaMaturesFields[0], mirnaMaturesFields[1], mirnaMaturesFields[2]);
            }

            // Add object to Map<EnsemblID, MiRNAGene>
            mirnaGeneMap.put(fields[0], miRNAGene);
        }
        br.close();
        return mirnaGeneMap;
    }


//    public void parseGff3ToJson(File getFile, File geneDescriptionFile, File xrefsFile, File outJsonFile)
//            throws IOException, SecurityException, NoSuchMethodException, FileFormatException {
//
//        Map<String, Gene> genes = new HashMap<String, Gene>();
//        Map<String, Transcript> transcripts = new HashMap<String, Transcript>();
//
//        Map<String, String> attributes = new HashMap<>();
//
//        System.out.println("READ FILE START::::::::::::::::::::::::::");
//        String line = "";
//        BufferedReader br = new BufferedReader(new FileReader(getFile));
//        System.out.println(br.readLine());
//        while ((line = br.readLine()) != null) {
//            if (line.startsWith("#") || !line.contains("\t"))
//                continue;
//
//            String fields[] = line.split("\t", -1);
//            String group[] = fields[8].split(";");
//
//            String id = group[0].split("=")[1];
//            String name = "";
//            String parent = "";
//            String chromosome = fields[0].replace("_Cc_182", "");
//            int start = Integer.parseInt(fields[3]);
//            int end = Integer.parseInt(fields[4]);
//            String strand = fields[6];
//            String feature = fields[2];
//
//            // parsing attributres, column 9
//            attributes.clear();
//            String[] atrrFields = fields[8].split(";");
//            String[] attrKeyValue;
//            for (String attrField : atrrFields) {
//                attrKeyValue = attrField.split("=");
//                attributes.put(attrKeyValue[0].toLowerCase(), attrKeyValue[1]);
//            }
//
//            if (feature.equalsIgnoreCase("CDS")) {
//                name = "";
//                // parent = group[1].split("=")[1];
//                parent = attributes.get("parent");
//                int phase = Integer.parseInt(fields[7]);
//                Transcript t = transcripts.get(parent);
//
//                Exon e = new Exon();
//                e.setId(id);
//                e.setChromosome(chromosome);
//                e.setStart(start);
//                e.setEnd(end);
//                e.setStrand(strand);
//                e.setPhase(phase);
//
//                e.setGenomicCodingStart(start);
//                e.setGenomicCodingEnd(end);
//
//                // // just in case...
//                // if(t.getExons() == null) {
//                // t.setExons(new ArrayList<Exon>());
//                // }
//                //
//                // // before adding
//                // if(t.getExons().size() > 0) {
//                // if(strand.equals("1") || strand.equals("+")) {
//                // Exon lastExon = t.getExons().get(t.getExons().size()-1);
//                // if(lastExon.getEnd() == e.getStart()-1) {
//                // lastExon.setEnd(e.getEnd());
//                // lastExon.setId(e.getId());
//                // lastExon.setGenomicCodingStart(e.getStart());
//                // lastExon.setGenomicCodingEnd(e.getEnd());
//                // }else {
//                // t.getExons().add(e);
//                // }
//                // }else { // negative strand
//                //
//                // }
//                // }else {
//                // t.getExons().add(e);
//                // }
//
//                t.getExons().add(e);
//
//                // Collections.sort(list, new FeatureComparable());
//
//            }
//            if (feature.equalsIgnoreCase("five_prime_UTR") || feature.equalsIgnoreCase("three_prime_UTR")) {
//
//                // name = "";
//                // parent = group[1].split("=")[1];
//                // FivePrimeUtr fivePrimeUtr = new FivePrimeUtr(id, chromosome,
//                // start, end, strand);
//                // t.getFivePrimeUtr().add(fivePrimeUtr);
//                parent = attributes.get("parent");
//                // int phase = Integer.parseInt(fields[7]);
//                Transcript t = transcripts.get(parent);
//
//                Exon e = new Exon();
//                e.setId(id);
//                e.setChromosome(chromosome);
//                e.setStart(start);
//                e.setEnd(end);
//                e.setStrand(strand);
//                // e.setPhase(phase);
//
//                e.setGenomicCodingStart(start);
//                e.setGenomicCodingEnd(end);
//                t.getExons().add(e);
//
//            }
//            // if (feature.equalsIgnoreCase("three_prime_UTR")) {
//            // name = "";
//            // parent = group[1].split("=")[1];
//
//            // Transcript t = transcriptsId.get(parent);
//            // ThreePrimeUtr threePrimeUtr = new ThreePrimeUtr(id,
//            // chromosome, start, end, strand);
//            // t.getThreePrimeUtr().add(threePrimeUtr);
//            // }
//            if (feature.equalsIgnoreCase("mRNA")) {
//                id = group[0].split("=")[1];
//                name = group[1].split("=")[1];
//                parent = group[4].split("=")[1];
//
//                Transcript tr = new Transcript();
//                tr.setExons(new ArrayList<Exon>());
//                tr.setXrefs(new ArrayList<Xref>());
//                tr.setId(id);
//                tr.setName(name);
//                tr.setBiotype("");
//                tr.setStatus("");
//                tr.setChromosome(chromosome);
//                tr.setStart(start);
//                tr.setEnd(end);
//                tr.setGenomicCodingStart(start);
//                tr.setGenomicCodingStart(end);
//                tr.setStrand(strand);
//
//                transcripts.put(id, tr);
//                genes.get(parent).getTranscripts().add(tr);
//            }
//            if (feature.equalsIgnoreCase("gene")) {
//
//                name = group[1].split("=")[1];
//                Gene g = new Gene(id, name, "", "", chromosome, start, end, strand, "JGI", "",
//                        new ArrayList<Transcript>(), null);
//                // g.setTranscripts(new ArrayList<Transcript>());
//                // g.setId(id);
//                // g.setBiotype("");
//                // g.setStatus("");
//                // g.setName(name);
//                // g.setSequenceName(chromosome);
//                // g.setStart(start);
//                // g.setEnd(end);
//                // g.setStrand(strand);
//
//                genes.put(id, g);
//            }
//
//        }
//        br.close();
//
//        // Reorder
//        for (String geneId : genes.keySet()) {
//            Gene gene = genes.get(geneId);
//            for (Transcript transcript : gene.getTranscripts()) {
//                Collections.sort(transcript.getExons(), new FeatureComparable());
//
//                Exon prevExon = null;
//                List<Exon> toRemove = new ArrayList<Exon>();
//                for (Exon exon : transcript.getExons()) {
//                    if (prevExon != null) {
//
//                        String strand = exon.getStrand();
//                        if (strand.equals("1") || strand.equals("+")) {
//
//                            if (prevExon.getEnd() == exon.getStart() - 1) {
//                                if (prevExon.getId().contains("five_prime_UTR")) {
//                                    exon.setStart(prevExon.getStart());
////									transcript.setGenomicCodingStart(exon.getGenomicCodingStart());
//                                    toRemove.add(prevExon);
//                                }
//                                if (exon.getId().contains("three_prime_UTR")) {
//                                    prevExon.setEnd(exon.getEnd());
////									transcript.setGenomicCodingEnd(prevExon.getGenomicCodingEnd());
//                                    toRemove.add(exon);
//                                }
//                            }
//
//                        } else { // negative strand
//
//                            if (prevExon.getEnd() == exon.getStart() - 1) {
//                                if (prevExon.getId().contains("three_prime_UTR")) {
//                                    exon.setStart(prevExon.getStart());
////									transcript.setGenomicCodingStart(exon.getGenomicCodingStart());
//                                    toRemove.add(prevExon);
//                                }
//                                if (exon.getId().contains("five_prime_UTR")) {
//                                    prevExon.setEnd(exon.getEnd());
////									transcript.setGenomicCodingEnd(prevExon.getGenomicCodingEnd());
//                                    toRemove.add(exon);
//                                }
//                            }
//                        }
//                    }
//
//                    prevExon = exon;
//                }
//                for (Exon primeUTR : toRemove) {
//                    transcript.getExons().remove(primeUTR);
//                }
//
//                //Update genomic coding region start and end on transcripts
//                int i = 1;
//                Exon e = transcript.getExons().get(0);
//                while (e.getId().contains("prime_UTR")) {
//                    e = transcript.getExons().get(i);
//                    i++;
//                }
//                transcript.setGenomicCodingStart(e.getGenomicCodingStart());
//
//                int exonSize = transcript.getExons().size();
//                int j = exonSize - 2;
//                Exon ee = transcript.getExons().get(exonSize - 1);
//                while (ee.getId().contains("prime_UTR")) {
//                    ee = transcript.getExons().get(j);
//                    j--;
//                }
//                transcript.setGenomicCodingEnd(ee.getGenomicCodingEnd());
//
//            }
//        }
//
////		Gson gson = new Gson();
////		TextFileWriter tfw = new TextFileWriter(outJsonFile.getAbsolutePath());
//        BufferedWriter bw = Files.newBufferedWriter(outJsonFile.toPath(), Charset.defaultCharset());
//
//        System.out.println("");
//        System.out.println("START WRITE");
//        for (String geneId : genes.keySet()) {
//            Gene gene = genes.get(geneId);
////			tfw.writeStringToFile(gson.writeValueAsString(gene));
////			tfw.writeStringToFile("\n");
//        }
//        bw.close();
//
//
////		System.out.println(gson.toJson(genes.get("Ciclev10007224m.g")));
////		System.out.println(gson.writeValueAsString(genes.get("Ciclev10008515m.g")));
////		System.out.println(gson.writeValueAsString(genes.get("Ciclev10007219m.g")));
//        System.out.println("END WRITE");
//    }

//    private class FeatureComparable implements Comparator<Object> {
//        @Override
//        public int compare(Object exon1, Object exon2) {
//            return ((Exon) exon1).getStart() - ((Exon) exon2).getStart();
//        }
//    }

    private void getGtfFileFromGeneDirectoryPath(Path geneDirectoryPath) {
        for (String fileName : geneDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".gtf") || fileName.endsWith(".gtf.gz")) {
                gtfFile = geneDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    private void getProteinFastaFileFromGeneDirectoryPath(Path geneDirectoryPath) {
        for (String fileName : geneDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".pep.all.fa") || fileName.endsWith(".pep.all.fa.gz")) {
                proteinFastaFile = geneDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    private void getCDnaFastaFileFromGeneDirectoryPath(Path geneDirectoryPath) {
        for (String fileName : geneDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".cdna.all.fa") || fileName.endsWith(".cdna.all.fa.gz")) {
                cDnaFastaFile = geneDirectoryPath.resolve(fileName);
                break;
            }
        }
    }
}
