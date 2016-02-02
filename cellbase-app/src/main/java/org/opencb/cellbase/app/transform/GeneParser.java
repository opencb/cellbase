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

import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.formats.feature.gtf.io.GtfReader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.biodata.models.variant.avro.GeneTraitAssociation;
import org.opencb.biodata.tools.sequence.fasta.FastaIndexManager;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class GeneParser extends CellBaseParser {

    private Map<String, Integer> transcriptDict;
    private Map<String, Exon> exonDict;


    private Path gtfFile;
    private Path proteinFastaFile;
    private Path cDnaFastaFile;
    private Path geneDescriptionFile;
    private Path xrefsFile;
    private Path uniprotIdMappingFile;
    private Path tfbsFile;
    private Path mirnaFile;
    private Path geneExpressionFile;
    private Path geneDrugFile;
    private Path hpoFile;
    private Path disgenetFile;
    private Path genomeSequenceFilePath;

    private CellBaseConfiguration.SpeciesProperties.Species species;

    private Connection sqlConn;
    private PreparedStatement sqlQuery;

    private int CHUNK_SIZE = 2000;
    private String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";
    private Set<String> indexedSequences;


    public GeneParser(Path geneDirectoryPath, Path genomeSequenceFastaFile, CellBaseConfiguration.SpeciesProperties.Species species,
                      CellBaseSerializer serializer) {
        this(null, geneDirectoryPath.resolve("description.txt"), geneDirectoryPath.resolve("xrefs.txt"),
                geneDirectoryPath.resolve("idmapping_selected.tab.gz"), geneDirectoryPath.resolve("MotifFeatures.gff.gz"),
                geneDirectoryPath.resolve("mirna.txt"),
                geneDirectoryPath.getParent().getParent().resolve("common/expression/allgenes_updown_in_organism_part.tab.gz"),
                geneDirectoryPath.resolve("geneDrug/dgidb.tsv"),
                geneDirectoryPath.resolve("ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt"),
                geneDirectoryPath.resolve("all_gene_disease_associations.txt.gz"),
                genomeSequenceFastaFile, species, serializer);
        getGtfFileFromGeneDirectoryPath(geneDirectoryPath);
        getProteinFastaFileFromGeneDirectoryPath(geneDirectoryPath);
        getCDnaFastaFileFromGeneDirectoryPath(geneDirectoryPath);
    }

    public GeneParser(Path gtfFile, Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile, Path tfbsFile, Path mirnaFile,
                      Path geneExpressionFile, Path geneDrugFile, Path hpoFile, Path disgenetFile, Path genomeSequenceFilePath,
                      CellBaseConfiguration.SpeciesProperties.Species species, CellBaseSerializer serializer) {
        super(serializer);
        this.gtfFile = gtfFile;
        this.geneDescriptionFile = geneDescriptionFile;
        this.xrefsFile = xrefsFile;
        this.uniprotIdMappingFile = uniprotIdMappingFile;
        this.tfbsFile = tfbsFile;
        this.mirnaFile = mirnaFile;
        this.geneExpressionFile = geneExpressionFile;
        this.geneDrugFile = geneDrugFile;
        this.hpoFile = hpoFile;
        this.disgenetFile = disgenetFile;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.species = species;

        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);
    }

    public void parse() throws IOException, SecurityException, NoSuchMethodException, FileFormatException, InterruptedException {
        Gene gene = null;
        Transcript transcript;
        Exon exon = null;

        int cdna = 1;
        int cds = 1;

        Map<String, String> geneDescriptionMap = getGeneDescriptionMap();
        Map<String, ArrayList<Xref>> xrefMap = GeneParserUtils.getXrefMap(xrefsFile, uniprotIdMappingFile);
        Map<String, Fasta> proteinSequencesMap = getProteinSequencesMap();
        Map<String, Fasta> cDnaSequencesMap = getCDnaSequencesMap();
        Map<String, SortedSet<Gff2>> tfbsMap = GeneParserUtils.getTfbsMap(tfbsFile);
        Map<String, MiRNAGene> mirnaGeneMap = GeneParserUtils.getmiRNAGeneMap(mirnaFile);

        // Gene annotation data
        Map<String, List<Expression>> geneExpressionMap = GeneParserUtils
                .getGeneExpressionMap(species.getScientificName(), geneExpressionFile);
        Map<String, List<GeneDrugInteraction>> geneDrugMap = GeneParserUtils.getGeneDrugMap(geneDrugFile);
        Map<String, List<GeneTraitAssociation>> diseaseAssociationMap = GeneParserUtils.getGeneDiseaseAssociationMap(hpoFile, disgenetFile);

        // Preparing the fasta file for fast accessing
        FastaIndexManager fastaIndexManager = null;
        try {
            fastaIndexManager = new FastaIndexManager(genomeSequenceFilePath, true);
            if (!fastaIndexManager.isConnected()) {
                fastaIndexManager.index();
            }
        } catch (Exception e) {
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

            if (gtf.getFeature().equals("gene") || gtf.getFeature().equals("transcript")
                    || gtf.getFeature().equals("UTR") || gtf.getFeature().equals("Selenocysteine")) {
                continue;
            }

            String geneId = gtf.getAttributes().get("gene_id");
            String transcriptId = gtf.getAttributes().get("transcript_id");

            if (newGene(gene, geneId)) {
                // If new geneId is different from the current then we must serialize before data new gene
                if (gene != null) {
                    serializer.serialize(gene);
                }

                GeneAnnotation geneAnnotation = new GeneAnnotation(geneExpressionMap.get(geneId),
                        diseaseAssociationMap.get(gtf.getAttributes().get("gene_name")),
                        geneDrugMap.get(gtf.getAttributes().get("gene_name")));

                gene = new Gene(geneId, gtf.getAttributes().get("gene_name"), gtf.getAttributes().get("gene_biotype"),
                        "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
                        gtf.getStrand(), "Ensembl", geneDescriptionMap.get(geneId), new ArrayList<>(),
                        mirnaGeneMap.get(geneId), geneAnnotation);
                // Do not change order!! size()-1 is the index of the gene ID
            }

            // Check if Transcript exist in the Gene Set of transcripts
            if (!transcriptDict.containsKey(transcriptId)) {
                // TODO: transcript tfbs should be a list and not an array list
                String transcriptChrosome = gtf.getSequenceName().replaceFirst("chr", "");
                ArrayList<TranscriptTfbs> transcriptTfbses = getTranscriptTfbses(gtf, transcriptChrosome, tfbsMap);
                Map<String, String> gtfAttributes = gtf.getAttributes();
                transcript = new Transcript(transcriptId, gtfAttributes.get("transcript_name"),
                        (gtfAttributes.get("transcript_biotype") != null) ? gtfAttributes.get("transcript_biotype") : gtf.getSource(),
                        "KNOWN", transcriptChrosome, gtf.getStart(), gtf.getEnd(),
                        gtf.getStrand(), 0, 0, 0, 0, 0, "", "", xrefMap.get(transcriptId), new ArrayList<Exon>(), transcriptTfbses);

                String tags = gtf.getAttributes().get("tag");
                if (tags != null) {
                    transcript.setAnnotationFlags(new HashSet<String>(Arrays.asList(tags.split(","))));
                }

                Fasta proteinFasta = proteinSequencesMap.get(transcriptId);
                if (proteinFasta != null) {
                    transcript.setProteinSequence(proteinFasta.getSeq());
                }

                Fasta cDnaFasta = cDnaSequencesMap.get(transcriptId);
                if (cDnaFasta != null) {
                    transcript.setcDnaSequence(cDnaFasta.getSeq());
                }
                gene.getTranscripts().add(transcript);
                // TODO: could use a transcriptId -> transcript map?
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
                //String exonSequence = getExonSequence(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());
                String exonSequence = null;
                try {
                    exonSequence = fastaIndexManager.query(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());
                } catch (RocksDBException e) {
                    e.printStackTrace();
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
                        // Set cdnaCodingEnd to prevent those cases without stop_codon

                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        exon.setCdsStart(cds);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;
                        transcript.setCdsLength(cds - 1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

                        exon.setPhase(Integer.valueOf(gtf.getFrame()));

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
                        // cdnaCodingStart points to the same base position than genomicCodingEnd
                        exon.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        // Set cdnaCodingEnd to prevent those cases without stop_codon
                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);

                        exon.setCdsStart(cds);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;
                        transcript.setCdsLength(cds - 1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

                        exon.setPhase(Integer.valueOf(gtf.getFrame()));

                        if (transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                            transcript.setGenomicCodingStart(gtf.getStart());
                        }
                        if (transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
                            transcript.setGenomicCodingEnd(gtf.getEnd());
                        }
                        // only first time
                        if (transcript.getCdnaCodingStart() == 0) {
                            // cdnaCodingStart points to the same base position than genomicCodingEnd
                            transcript.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);
                        }
                    }
                    // no strand dependent
                    transcript.setProteinID(gtf.getAttributes().get("protein_id"));
                }

                if (gtf.getFeature().equalsIgnoreCase("start_codon")) {
                    // nothing to do
                    System.out.println("Empty block, this should be redesigned");
                }

                if (gtf.getFeature().equalsIgnoreCase("stop_codon")) {
//                      setCdnaCodingEnd = false; // stop_codon found, cdnaCodingEnd will be set here,
//                      no need to set it at the beginning of next feature
                    if (exon.getStrand().equals("+")) {
                        // we need to increment 3 nts, the stop_codon length.
                        exon.setGenomicCodingEnd(gtf.getEnd());
                        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        // If stop_codon appears, overwrite values
                        transcript.setGenomicCodingEnd(gtf.getEnd());
                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        transcript.setCdsLength(cds - 1);
                    } else {
                        // we need to increment 3 nts, the stop_codon length.
                        exon.setGenomicCodingStart(gtf.getStart());
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        // If stop_codon appears, overwrite values
                        transcript.setGenomicCodingStart(gtf.getStart());
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        transcript.setCdsLength(cds - 1);
                    }
                }
            }
        }

        // last gene must be serialized
        serializer.serialize(gene);

        // cleaning
        gtfReader.close();
        serializer.close();
        fastaIndexManager.close();
    }

    private ArrayList<TranscriptTfbs> getTranscriptTfbses(Gtf transcript, String chromosome, Map<String, SortedSet<Gff2>> tfbsMap) {
        ArrayList<TranscriptTfbs> transcriptTfbses = null;
        if (tfbsMap.containsKey(chromosome)) {
            for (Gff2 tfbs : tfbsMap.get(chromosome)) {
                if (transcript.getStrand().equals("+")) {
                    if (tfbs.getStart() > transcript.getStart() + 500) {
                        break;
                    } else if (tfbs.getEnd() > transcript.getStart() - 2500) {
                        transcriptTfbses = addTranscriptTfbstoList(tfbs, transcript, chromosome, transcriptTfbses);
                    }
                } else {
                    // transcript in negative strand
                    if (tfbs.getStart() > transcript.getEnd() + 2500) {
                        break;
                    } else if (tfbs.getStart() > transcript.getEnd() - 500) {
                        transcriptTfbses = addTranscriptTfbstoList(tfbs, transcript, chromosome, transcriptTfbses);
                    }
                }
            }
        }
        return transcriptTfbses;
    }

    private ArrayList<TranscriptTfbs> addTranscriptTfbstoList(Gff2 tfbs, Gtf transcript, String chromosome,
                                                              ArrayList<TranscriptTfbs> transcriptTfbses) {
        if (transcriptTfbses == null) {
            transcriptTfbses = new ArrayList<>();
        }
        String[] tfbsNameFields = tfbs.getAttribute().split("=")[1].split(":");
        transcriptTfbses.add(new TranscriptTfbs(tfbsNameFields[0], tfbsNameFields[1], chromosome, tfbs.getStart(), tfbs.getEnd(),
                tfbs.getStrand(), getRelativeTranscriptTfbsStart(tfbs, transcript), getRelativeTranscriptTfbsEnd(tfbs, transcript),
                Float.parseFloat(tfbs.getScore())));
        return transcriptTfbses;
    }

    private Integer getRelativeTranscriptTfbsStart(Gff2 tfbs, Gtf transcript) {
        Integer relativeStart;
        if (transcript.getStrand().equals("+")) {
            if (tfbs.getStart() < transcript.getStart()) {
                relativeStart = tfbs.getStart() - transcript.getStart();
            } else {
                relativeStart = tfbs.getStart() - transcript.getStart() + 1;
            }
        } else {
            // negative strand transcript
            if (tfbs.getEnd() > transcript.getEnd()) {
                relativeStart = transcript.getEnd() - tfbs.getEnd();
            } else {
                relativeStart = transcript.getEnd() - tfbs.getEnd() + 1;
            }
        }
        return relativeStart;
    }

    private Integer getRelativeTranscriptTfbsEnd(Gff2 tfbs, Gtf transcript) {
        Integer relativeEnd;
        if (transcript.getStrand().equals("+")) {
            if (tfbs.getEnd() < transcript.getStart()) {
                relativeEnd = tfbs.getEnd() - transcript.getStart();
            } else {
                relativeEnd = tfbs.getEnd() - transcript.getStart() + 1;
            }
        } else {
            if (tfbs.getStart() > transcript.getEnd()) {
                relativeEnd = transcript.getEnd() - tfbs.getStart();
            } else {
                relativeEnd = transcript.getEnd() - tfbs.getStart() + 1;
            }
        }
        return relativeEnd;
    }


    private Map<String, Fasta> getCDnaSequencesMap() throws IOException, FileFormatException {
        logger.info("Loading ENSEMBL's cDNA sequences...");
        Map<String, Fasta> cDnaSequencesMap = new HashMap<>();
        if (cDnaFastaFile != null && Files.exists(cDnaFastaFile) && !Files.isDirectory(cDnaFastaFile)) {
            FastaReader fastaReader = new FastaReader(cDnaFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for (Fasta fasta : fastaList) {
                cDnaSequencesMap.put(fasta.getId(), fasta);
            }
        } else {
            logger.warn("cDNA fasta file " + cDnaFastaFile + " not found");
            logger.warn("ENSEMBL's cDNA sequences not loaded");
        }
        return cDnaSequencesMap;
    }

    private Map<String, Fasta> getProteinSequencesMap() throws IOException, FileFormatException {
        logger.info("Loading ENSEMBL's protein sequences...");
        Map<String, Fasta> proteinSequencesMap = new HashMap<>();
        if (proteinFastaFile != null && Files.exists(proteinFastaFile) && !Files.isDirectory(proteinFastaFile)) {
            FastaReader fastaReader = new FastaReader(proteinFastaFile);
            List<Fasta> fastaList = fastaReader.readAll();
            fastaReader.close();
            for (Fasta fasta : fastaList) {
                proteinSequencesMap.put(fasta.getDescription().split("transcript:")[1].split("\\s")[0], fasta);
            }
        } else {
            logger.warn("Protein fasta file " + proteinFastaFile + " not found");
            logger.warn("ENSEMBL's protein sequences not loaded");
        }
        return proteinSequencesMap;
    }


    private Map<String, String> getGeneDescriptionMap() throws IOException {
        logger.info("Loading gene description data...");
        Map<String, String> geneDescriptionMap = new HashMap<>();
        String[] fields;
        if (geneDescriptionFile != null && Files.exists(geneDescriptionFile)) {
            List<String> lines = Files.readAllLines(geneDescriptionFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                geneDescriptionMap.put(fields[0], fields[1]);
            }
        } else {
            logger.warn("Gene description file " + geneDescriptionFile + " not found");
            logger.warn("Gene description data not loaded");
        }
        return geneDescriptionMap;
    }


    private boolean newGene(Gene previousGene, String newGeneId) {
        return previousGene == null || !newGeneId.equals(previousGene.getId());
    }

    @Deprecated
    private void connect(Path genomeSequenceFilePath) throws ClassNotFoundException, SQLException, IOException {
        logger.info("Connecting to reference genome sequence database ...");
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + genomeSequenceFilePath.getParent().toString() + "/reference_genome.db");
        if (!Files.exists(Paths.get(genomeSequenceFilePath.getParent().toString(), "reference_genome.db"))
                || Files.size(genomeSequenceFilePath.getParent().resolve("reference_genome.db")) == 0) {
            logger.info("Genome sequence database doesn't exists and will be created");
            Statement createTable = sqlConn.createStatement();
            createTable.executeUpdate("CREATE TABLE if not exists  "
                    + "genome_sequence (sequenceName VARCHAR(50), chunkId VARCHAR(30), start INT, end INT, sequence VARCHAR(2000))");
            indexReferenceGenomeFasta(genomeSequenceFilePath);
        }
        indexedSequences = getIndexedSequences();
        sqlQuery = sqlConn.prepareStatement("SELECT sequence from genome_sequence WHERE chunkId = ? "); //AND start <= ? AND end >= ?
        logger.info("Genome sequence database connected");
    }

    @Deprecated
    private Set<String> getIndexedSequences() throws SQLException {
        Set<String> indexedSeq = new HashSet<>();

        PreparedStatement seqNameQuery = sqlConn.prepareStatement("SELECT DISTINCT sequenceName from genome_sequence");
        ResultSet rs = seqNameQuery.executeQuery();
        while (rs.next()) {
            indexedSeq.add(rs.getString(1));
        }
        rs.close();

        return indexedSeq;
    }

    @Deprecated
    private void disconnectSqlite() throws SQLException {
        sqlConn.close();
    }

    @Deprecated
    private void indexReferenceGenomeFasta(Path genomeSequenceFilePath) throws IOException, ClassNotFoundException, SQLException {
        BufferedReader bufferedReader = FileUtils.newBufferedReader(genomeSequenceFilePath);

        // Some parameters initialization
        String sequenceName = "";
        boolean haplotypeSequenceType = false;

        PreparedStatement sqlInsert = sqlConn
                .prepareStatement("INSERT INTO genome_sequence (chunkID, start, end, sequence, sequenceName) values (?, ?, ?, ?, ?)");
        StringBuilder sequenceStringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            // We accumulate the complete sequence in a StringBuilder
            if (!line.startsWith(">")) {
                sequenceStringBuilder.append(line);
            } else {
                // New sequence has been found and we must insert it into SQLite.
                // Note that the first time there is no sequence. Only not HAP sequences are stored.
                if (sequenceStringBuilder.length() > 0) {
                    insertGenomeSequence(sequenceName, haplotypeSequenceType, sqlInsert, sequenceStringBuilder);
                }

                // initialize data structures
                sequenceName = line.replace(">", "").split(" ")[0];
                haplotypeSequenceType = line.endsWith("HAP");
                sequenceStringBuilder.delete(0, sequenceStringBuilder.length());
            }
        }
        insertGenomeSequence(sequenceName, haplotypeSequenceType, sqlInsert, sequenceStringBuilder);

        bufferedReader.close();

        Statement stm = sqlConn.createStatement();
        stm.executeUpdate("CREATE INDEX chunkId_idx on genome_sequence(chunkId)");
    }

    @Deprecated
    private void insertGenomeSequence(String sequenceName, boolean haplotypeSequenceType, PreparedStatement sqlInsert,
                                      StringBuilder sequenceStringBuilder) throws SQLException {
        int chunk = 0;
        int start = 1;
        int end = CHUNK_SIZE - 1;
        // if the sequence read is not HAP then we stored in sqlite
        if (!haplotypeSequenceType && !sequenceName.contains("PATCH")) {
            logger.info("Indexing genome sequence {} ...", sequenceName);
            sqlInsert.setString(5, sequenceName);
            //chromosome sequence length could shorter than CHUNK_SIZE
            if (sequenceStringBuilder.length() < CHUNK_SIZE) {
                sqlInsert.setString(1, sequenceName + "_" + chunk + "_" + chunkIdSuffix);
                sqlInsert.setInt(2, start);
                sqlInsert.setInt(3, sequenceStringBuilder.length() - 1);
                sqlInsert.setString(4, sequenceStringBuilder.toString());

                // Sequence to store is larger than CHUNK_SIZE
            } else {
                int sequenceLength = sequenceStringBuilder.length();

                sqlConn.setAutoCommit(false);
                while (start < sequenceLength) {
                    if (chunk % 10000 == 0 && chunk != 0) {
                        sqlInsert.executeBatch();
                        sqlConn.commit();
                    }

                    // chunkId is common for all the options
                    sqlInsert.setString(1, sequenceName + "_" + chunk + "_" + chunkIdSuffix);
                    if (start == 1) {   // First chunk of the chromosome
                        // First chunk contains CHUNK_SIZE-1 nucleotides as index start at position 1 but must end at 1999
//                                        chunkSequence = sequenceStringBuilder.substring(start - 1, CHUNK_SIZE - 1);
//                                        genomeSequenceChunk = new GenomeSequenceChunk(chromosome, chromosome+"_"+chunk+"_"
// +chunkIdSuffix, start, end, sequenceType, sequenceAssembly, chunkSequence);
                        sqlInsert.setInt(2, start);
                        sqlInsert.setInt(3, end);
                        sqlInsert.setString(4, sequenceStringBuilder.substring(start - 1, CHUNK_SIZE - 1));

                        start += CHUNK_SIZE - 1;
                    } else {    // Regular chunk
                        if ((start + CHUNK_SIZE) < sequenceLength) {
                            sqlInsert.setInt(2, start);
                            sqlInsert.setInt(3, end);
                            sqlInsert.setString(4, sequenceStringBuilder.substring(start - 1, start + CHUNK_SIZE - 1));
                            start += CHUNK_SIZE;
                        } else {    // Last chunk of the chromosome
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
    @Deprecated
    private String getExonSequence(String sequenceName, int start, int end) {
        String subStr = "";
        if (indexedSequences.contains(sequenceName)) {
            try {
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
                if (regionChunkStart > 0) {
                    if (stringBuilder.toString().length() > 0 && stringBuilder.toString().length() >= endStr) {
                        subStr = stringBuilder.toString().substring(startStr, endStr);
                    }
                } else {
                    if (stringBuilder.toString().length() > 0 && stringBuilder.toString().length() + 1 >= endStr) {
                        subStr = stringBuilder.toString().substring(startStr - 1, endStr - 1);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error obtaining exon sequence ({}:{}-{})", sequenceName, start, end);
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
