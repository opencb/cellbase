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

import com.google.protobuf.util.JsonFormat;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gff.io.Gff2Reader;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.formats.feature.gtf.io.GtfReader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.sequence.fasta.Fasta;
import org.opencb.biodata.formats.sequence.fasta.io.FastaReader;
import org.opencb.biodata.models.core.protobuf.GeneModel.Gene;
import org.opencb.biodata.models.core.protobuf.GeneModel.MiRNAGene;
import org.opencb.biodata.models.core.protobuf.TranscriptModel.Exon;
import org.opencb.biodata.models.core.protobuf.TranscriptModel.Transcript;
import org.opencb.biodata.models.core.protobuf.TranscriptModel.TranscriptTfbs;
import org.opencb.biodata.models.core.protobuf.TranscriptModel.Xref;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GeneParserProto extends CellBaseParser {

    private Map<String, Integer> transcriptDict;
    private Map<String, Exon.Builder> exonDict;

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
    private Path genomeSequenceFilePath;

    private CellBaseConfiguration.SpeciesProperties.Species species;

    private Connection sqlConn;
    private PreparedStatement sqlQuery;

    private int CHUNK_SIZE = 2000;
    private String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";
    private Set<String> indexedSequences;


    public GeneParserProto(Path geneDirectoryPath, Path genomeSequenceFastaFile,
                           CellBaseConfiguration.SpeciesProperties.Species species, CellBaseSerializer serializer) {
        this(null, geneDirectoryPath.resolve("description.txt"), geneDirectoryPath.resolve("xrefs.txt"),
                geneDirectoryPath.resolve("idmapping_selected.tab.gz"), geneDirectoryPath.resolve("MotifFeatures.gff"),
                geneDirectoryPath.resolve("mirna.txt"),
                geneDirectoryPath.getParent().getParent().resolve("common/expression/allgenes_updown_in_organism_part.tab.gz"),
                geneDirectoryPath.resolve("geneDrug/dgidb.tsv"),
                genomeSequenceFastaFile, species, serializer);
        getGtfFileFromGeneDirectoryPath(geneDirectoryPath);
        getProteinFastaFileFromGeneDirectoryPath(geneDirectoryPath);
        getCDnaFastaFileFromGeneDirectoryPath(geneDirectoryPath);
    }

    public GeneParserProto(Path gtfFile, Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile, Path tfbsFile,
                           Path mirnaFile, Path geneExpressionFile, Path geneDrugFile, Path genomeSequenceFilePath,
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
        Map<String, ArrayList<Xref>> xrefMap = getXrefMap();
        Map<String, Fasta> proteinSequencesMap = getProteinSequencesMap();
        Map<String, Fasta> cDnaSequencesMap = getCDnaSequencesMap();
        Map<String, SortedSet<Gff2>> tfbsMap = getTfbsMap();
        Map<String, MiRNAGene> mirnaGeneMap = getmiRNAGeneMap(mirnaFile);
//        Map<String, List<Expression>> geneExpressionMap = getGeneExpressionMap();
        Map<String, List<GeneDrugInteraction>> geneDrugMap = getGeneDrugMap();


        // Preparing the fasta file for fast accessing
        try {
            connect(genomeSequenceFilePath);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return;
        }

        // TODO remove
        // Empty transcript and exon dictionaries
        transcriptDict.clear();
        exonDict.clear();

        List<Transcript> transcripts = null;
        List<Exon> exons = null;

        Gene.Builder geneBuilder = null;
        Transcript.Builder transcriptBuilder = null;
        Exon.Builder exonBuilder = null;

        String currentGeneId = null;

        logger.info("Parsing gtf...");
        GtfReader gtfReader = new GtfReader(gtfFile);
        Gtf gtf;
        PrintWriter pw = new PrintWriter(new File("/tmp/aaa.json"));
        while ((gtf = gtfReader.read()) != null) {

            if (gtf.getFeature().equals("gene") || gtf.getFeature().equals("transcript") || gtf.getFeature().equals("UTR")
                    || gtf.getFeature().equals("Selenocysteine")) {
                continue;
            }

            String geneId = gtf.getAttributes().get("gene_id");
            String transcriptId = gtf.getAttributes().get("transcript_id");

//            if (newGene(gene, geneId)) {
            if (currentGeneId == null || !currentGeneId.equals(geneId)) {
                currentGeneId = geneId;
                // If new geneId is different from the current then we must serialize before data new gene
//                if (gene != null) { // FIXME fix this!
                if (geneBuilder != null) { // FIXME fix this!
                    geneBuilder.addAllTranscripts(transcripts);
//                    serializer.serialize(gene);
                    gene = geneBuilder.build();

                    JsonFormat.Printer jsonPrinter = JsonFormat.printer();
                    System.out.println(jsonPrinter.print(gene));
//                    jsonPrinter.appendTo(gene, pw);

//                    serializer.serialize(gene);
                    logger.info("Serialized {}", geneBuilder.getId());
//                    System.out.println("gene = " + gene.toString());
                }

//                gene = new Gene(geneId, gtf.getAttributes().get("gene_name"), gtf.getAttributes().get("gene_biotype"),
//                        "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
//                        gtf.getStrand(), "Ensembl", geneDescriptionMap.get(geneId), new ArrayList<Transcript>(),
//                        mirnaGeneMap.get(geneId), geneExpressionMap.get(geneId), geneDrugMap.get(gtf.getAttributes().get("gene_name")));

                transcripts = new ArrayList<>();
                logger.info("New gene '{}' found", geneId);
                geneBuilder = Gene.newBuilder()
                        .setId(geneId)
                        .setName(gtf.getAttributes().get("gene_name"))
                        .setBiotype(gtf.getAttributes().get("gene_biotype"))
                        .setStatus("KNOWN")
                        .setChromosome(gtf.getSequenceName().replaceFirst("chr", ""))
                        .setStart(gtf.getStart())
                        .setEnd(gtf.getEnd())
                        .setStrand(gtf.getStrand())
                        .setSource("Ensembl")
                        .setDescription(geneDescriptionMap.get(geneId) != null ? geneDescriptionMap.get(geneId) : "");
//                        .addAllTranscripts(transcripts);
                if (mirnaGeneMap.get(geneId) != null) {
                    geneBuilder.setMirna(mirnaGeneMap.get(geneId));

                }
//                        .setAnnotation()
//                        .build();
                // Do not change order!! size()-1 is the index of the gene ID
            }

            // Check if Transcript exist in the Gene Set of transcripts
            if (!transcriptDict.containsKey(transcriptId)) {
                logger.info("New transcript '{}' found", transcriptId);
                // TODO: transcript tfbs should be a list and not an array list
                String transcriptChrosome = gtf.getSequenceName().replaceFirst("chr", "");
                List<TranscriptTfbs> transcriptTfbses = getTranscriptTfbses(gtf, transcriptChrosome, tfbsMap);
                Map<String, String> gtfAttributes = gtf.getAttributes();
//                transcript = new Transcript(transcriptId, gtfAttributes.get("transcript_name"),
//                        (gtfAttributes.get("transcript_biotype") != null) ? gtfAttributes.get("transcript_biotype") : gtf.getSource(),
//                        "KNOWN", transcriptChrosome, gtf.getStart(), gtf.getEnd(),
//                        gtf.getStrand(), 0, 0, 0, 0, 0, "", "", xrefMap.get(transcriptId), new ArrayList<Exon>(), transcriptTfbses);
                exons = new ArrayList<>();
                transcriptBuilder = Transcript.newBuilder()
                        .setId(transcriptId)
                        .setName(gtfAttributes.get("transcript_name"))
                        .setBiotype((gtfAttributes.get("transcript_biotype") != null)
                                ? gtfAttributes.get("transcript_biotype")
                                : gtf.getSource())
                        .setStatus("KNOWN")
                        .setChromosome(transcriptChrosome)
                        .setStart(gtf.getStart())
                        .setEnd(gtf.getEnd())
                        .setStrand(gtf.getStrand())
                        .setGenomicCodingStart(0)
                        .setGenomicCodingEnd(0)
                        .setCdnaCodingStart(0)
                        .setCdnaCodingEnd(0)
                        .setCdsLength(0)
                        .setProteinId("")
                        .setDescription("")
//                        .setXrefs(xrefMap.get(transcriptId))
//                        .addAllExons(exons)
                        .addAllTfbs(transcriptTfbses);
//                        .build();
                String tags;
                if ((tags = gtf.getAttributes().get("tag")) != null) {
//                    transcript.setAnnotationFlags(new HashSet<>(Arrays.asList(tags.split(","))));
//                    transcriptBuilder.setAnnotationFlags(new HashSet<>(Arrays.asList(tags.split(","))));
                }

                Fasta proteinFasta;
                if ((proteinFasta = proteinSequencesMap.get(transcriptId)) != null) {
//                    transcript.setProteinSequence(proteinFasta.getSeq());
                    transcriptBuilder.setProteinSequence(proteinFasta.getSeq());
                }
                Fasta cDnaFasta;
                if ((cDnaFasta = cDnaSequencesMap.get(transcriptId)) != null) {
//                    transcript.setcDnaSequence(cDnaFasta.getSeq());
                    transcriptBuilder.setCdnaSequence(cDnaFasta.getSeq());
                }

                // TODO: could use a transcriptId -> transcript map?
                // Do not change order!! size()-1 is the index of the transcript ID
//                transcriptDict.put(transcriptId, gene.getTranscripts().size() - 1);
                transcriptDict.put(transcriptId, transcripts.size());


//                System.out.println("Line:" + gtf.toString());
//                gene.getTranscripts().add(transcript);
//                System.out.println(transcripts.size());
                transcripts.add(transcriptBuilder.build());
//                geneBuilder.setTranscripts(transcripts.size(), transcriptBuilder.build());
//                System.out.println(transcripts.size());
//                geneBuilder.addAllTranscripts(transcripts);


            } else {
//                transcript = gene.getTranscripts().get(transcriptDict.get(transcriptId));
//                System.out.println(">>"+transcripts.size());
//                System.out.println("transcriptDict = " + transcriptDict);
//                transcript = geneBuilder.getTranscriptsList().get(transcriptDict.get(transcriptId));
                transcript = transcripts.get(transcriptDict.get(transcriptId));
            }

            // At this point gene and transcript objects are set up

            // Update gene and transcript genomic coordinates, start must be the
            // lower, and end the higher
//            updateTranscriptAndGeneCoords(transcript, gene, gtf);
            updateTranscriptAndGeneCoords(transcriptBuilder, geneBuilder, gtf);

            if (gtf.getFeature().equalsIgnoreCase("exon")) {
                // Obtaining the exon sequence
                String exonSequence = getExonSequence(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());

//                exon = new Exon(gtf.getAttributes().get("exon_id"), gtf.getSequenceName().replaceFirst("chr", ""),
//                        gtf.getStart(), gtf.getEnd(), gtf.getStrand(), 0, 0, 0, 0, 0, 0, -1, Integer.parseInt(gtf
//                        .getAttributes().get("exon_number")), exonSequence);
                exonBuilder = Exon.newBuilder()
                        .setId(gtf.getAttributes().get("exon_id"))
                        .setChromosome(gtf.getSequenceName().replaceFirst("chr", ""))
                        .setStart(gtf.getStart())
                        .setEnd(gtf.getEnd())
                        .setStrand(gtf.getStrand())
                        .setGenomicCodingStart(0)
                        .setGenomicCodingEnd(0)
                        .setCdnaCodingStart(0)
                        .setCdnaCodingEnd(0)
                        .setCdsStart(0)
                        .setCdsEnd(0)
                        .setPhase(-1)
                        .setExonNumber(Integer.parseInt(gtf.getAttributes().get("exon_number")))
                        .setSequence(exonSequence);
//                        .build();

//                transcript.getExons().add(exon);
                exons.add(exon);

//                exonDict.put(transcript.getId() + "_" + exon.getExonNumber(), exon);
                exonDict.put(transcriptBuilder.getId() + "_" + exonBuilder.getExonNumber(), exonBuilder);
                if (gtf.getAttributes().get("exon_number").equals("1")) {
                    cdna = 1;
                    cds = 1;
                } else {
                    // with every exon we update cDNA length with the previous exon length
//                    cdna += exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)).getEnd()
//                            - exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)).getStart() + 1;
//                    System.out.println("transcriptBuilder = " + transcriptBuilder);
//                    System.out.println("exonDict = " + exonDict);
                    cdna += exonDict.get(transcriptBuilder.getId() + "_" + (exonBuilder.getExonNumber() - 1)).getEnd()
                            - exonDict.get(transcriptBuilder.getId() + "_" + (exonBuilder.getExonNumber() - 1)).getStart() + 1;
                }
            } else {
//                exon = exonDict.get(transcript.getId() + "_" + exon.getExonNumber());
                exonBuilder = exonDict.get(transcriptBuilder.getId() + "_" + exonBuilder.getExonNumber());
                if (gtf.getFeature().equalsIgnoreCase("CDS")) {
                    if (gtf.getStrand().equals("+") || gtf.getStrand().equals("1")) {
                        // CDS states the beginning of coding start
//                        exon.setGenomicCodingStart(gtf.getStart());
//                        exon.setGenomicCodingEnd(gtf.getEnd());
                        exonBuilder.setGenomicCodingStart(gtf.getStart());
                        exonBuilder.setGenomicCodingEnd(gtf.getEnd());

                        // cDNA coordinates
//                        exon.setCdnaCodingStart(gtf.getStart() - exon.getStart() + cdna);
//                        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        exonBuilder.setCdnaCodingStart(gtf.getStart() - exonBuilder.getStart() + cdna);
                        exonBuilder.setCdnaCodingEnd(gtf.getEnd() - exonBuilder.getStart() + cdna);

                        // Set cdnaCodingEnd to prevent those cases without stop_codon
//                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
                        // Set cdnaCodingEnd to prevent those cases without stop_codon
                        transcriptBuilder.setCdnaCodingEnd(gtf.getEnd() - exonBuilder.getStart() + cdna);

//                        exon.setCdsStart(cds);
//                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        exonBuilder.setCdsStart(cds);
                        exonBuilder.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;
//                        transcript.setCdsLength(cds-1);  // Set cdnaCodingEnd to prevent those cases without stop_codon
                        transcriptBuilder.setCdsLength(cds - 1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

//                        exon.setPhase(Integer.valueOf(gtf.getFrame()));
                        exonBuilder.setPhase(Integer.valueOf(gtf.getFrame()));

//                        if (transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                        if (transcriptBuilder.getGenomicCodingStart() == 0 || transcriptBuilder.getGenomicCodingStart() > gtf.getStart()) {
//                            transcript.setGenomicCodingStart(gtf.getStart());
                            transcriptBuilder.setGenomicCodingStart(gtf.getStart());
                        }
//                        if (transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
                        if (transcriptBuilder.getGenomicCodingEnd() == 0 || transcriptBuilder.getGenomicCodingEnd() < gtf.getEnd()) {
//                            transcript.setGenomicCodingEnd(gtf.getEnd());
                            transcriptBuilder.setGenomicCodingEnd(gtf.getEnd());
                        }
                        // only first time
//                        if (transcript.getCdnaCodingStart() == 0) {
                        if (transcriptBuilder.getCdnaCodingStart() == 0) {
//                            transcript.setCdnaCodingStart(gtf.getStart() - exon.getStart() + cdna);
                            transcriptBuilder.setCdnaCodingStart(gtf.getStart() - exonBuilder.getStart() + cdna);
                        }

                        // strand -
                    } else {
                        // CDS states the beginning of coding start
//                        exon.setGenomicCodingStart(gtf.getStart());
//                        exon.setGenomicCodingEnd(gtf.getEnd());
                        exonBuilder.setGenomicCodingStart(gtf.getStart());
                        exonBuilder.setGenomicCodingEnd(gtf.getEnd());

                        // cDNA coordinates
                        // cdnaCodingStart points to the same base position than genomicCodingEnd
//                        exon.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
//                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        // cdnaCodingStart points to the same base position than genomicCodingEnd
                        exonBuilder.setCdnaCodingStart(exonBuilder.getEnd() - gtf.getEnd() + cdna);
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
                        exonBuilder.setCdnaCodingEnd(exonBuilder.getEnd() - gtf.getStart() + cdna);
                        // Set cdnaCodingEnd to prevent those cases without stop_codon
//                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
                        // Set cdnaCodingEnd to prevent those cases without stop_codon
                        transcriptBuilder.setCdnaCodingEnd(exonBuilder.getEnd() - gtf.getStart() + cdna);

//                        exon.setCdsStart(cds);
//                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        exonBuilder.setCdsStart(cds);
                        exonBuilder.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

                        // increment in the coding length
                        cds += gtf.getEnd() - gtf.getStart() + 1;
//                        transcript.setCdsLength(cds-1);  // Set cdnaCodingEnd to prevent those cases without stop_codon
                        transcriptBuilder.setCdsLength(cds - 1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

//                        exon.setPhase(Integer.valueOf(gtf.getFrame()));
                        exonBuilder.setPhase(Integer.valueOf(gtf.getFrame()));

//                        if (transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > gtf.getStart()) {
                        if (transcriptBuilder.getGenomicCodingStart() == 0 || transcriptBuilder.getGenomicCodingStart() > gtf.getStart()) {
//                            transcript.setGenomicCodingStart(gtf.getStart());
                            transcriptBuilder.setGenomicCodingStart(gtf.getStart());
                        }
//                        if (transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < gtf.getEnd()) {
                        if (transcriptBuilder.getGenomicCodingEnd() == 0 || transcriptBuilder.getGenomicCodingEnd() < gtf.getEnd()) {
//                            transcript.setGenomicCodingEnd(gtf.getEnd());
                            transcriptBuilder.setGenomicCodingEnd(gtf.getEnd());
                        }
                        // only first time
//                        if (transcript.getCdnaCodingStart() == 0) {
                        if (transcriptBuilder.getCdnaCodingStart() == 0) {
                            // cdnaCodingStart points to the same base position than genomicCodingEnd
//                            transcript.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);
                            // cdnaCodingStart points to the same base position than genomicCodingEnd
                            transcriptBuilder.setCdnaCodingStart(exonBuilder.getEnd() - gtf.getEnd() + cdna);
                        }
                    }

                    // no strand dependent
//                    transcript.setProteinID(gtf.getAttributes().get("protein_id"));
                    transcriptBuilder.setProteinId(gtf.getAttributes().get("protein_id"));
                }

                if (gtf.getFeature().equalsIgnoreCase("start_codon")) {
                    // nothing to do
                }

                if (gtf.getFeature().equalsIgnoreCase("stop_codon")) {
//                    setCdnaCodingEnd = false; // stop_codon found, cdnaCodingEnd will be set here,
// no need to set it at the beginning of next feature
                    if (exonBuilder.getStrand().equals("+")) {
                        // we need to increment 3 nts, the stop_codon length.
//                        exon.setGenomicCodingEnd(gtf.getEnd());
//                        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
//                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        exonBuilder.setGenomicCodingEnd(gtf.getEnd());
                        exonBuilder.setCdnaCodingEnd(gtf.getEnd() - exonBuilder.getStart() + cdna);
                        exonBuilder.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        // If stop_codon appears, overwrite values
//                        transcript.setGenomicCodingEnd(gtf.getEnd());
//                        transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
//                        transcript.setCdsLength(cds-1);
                        transcriptBuilder.setGenomicCodingEnd(gtf.getEnd());
                        transcriptBuilder.setCdnaCodingEnd(gtf.getEnd() - exonBuilder.getStart() + cdna);
                        transcriptBuilder.setCdsLength(cds - 1);
                    } else {
                        // we need to increment 3 nts, the stop_codon length.
//                        exon.setGenomicCodingStart(gtf.getStart());
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
//                        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
//                        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        exonBuilder.setGenomicCodingStart(gtf.getStart());
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
                        exonBuilder.setCdnaCodingEnd(exonBuilder.getEnd() - gtf.getStart() + cdna);
                        exonBuilder.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);
                        cds += gtf.getEnd() - gtf.getStart();

                        // If stop_codon appears, overwrite values
//                        transcript.setGenomicCodingStart(gtf.getStart());
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
//                        transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
//                        transcript.setCdsLength(cds-1);
                        transcriptBuilder.setGenomicCodingStart(gtf.getStart());
                        // cdnaCodingEnd points to the same base position than genomicCodingStart
                        transcriptBuilder.setCdnaCodingEnd(exonBuilder.getEnd() - gtf.getStart() + cdna);
                        transcriptBuilder.setCdsLength(cds - 1);
                    }
                }
            }
        }

        pw.close();
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
    }

    private Map<String, List<GeneDrugInteraction>> getGeneDrugMap() throws IOException {
        Map<String, List<GeneDrugInteraction>> geneDrugMap = new HashMap<>();
        BufferedReader br;
        if (geneDrugFile.toFile().getName().endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(geneDrugFile.toFile()))));
        } else {
            br = Files.newBufferedReader(geneDrugFile, Charset.defaultCharset());
        }

        logger.info("Loading gene-drug data form {}", geneDrugFile);
        // Skip header
        br.readLine();

        int lineCounter = 1;
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            addValueToMapElement(geneDrugMap, parts[0], new GeneDrugInteraction(parts[0], parts[4], "dgidb", parts[2],
                    parts[3]));
            lineCounter++;
        }

        br.close();
        return geneDrugMap;
    }

    private Map<String, List<Expression>> getGeneExpressionMap() throws IOException {
        Map<String, List<Expression>> geneExpressionMap = new HashMap<>();

        BufferedReader br;
        if (geneExpressionFile.toFile().getName().endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(geneExpressionFile.toFile()))));
        } else {
            br = Files.newBufferedReader(geneExpressionFile, Charset.defaultCharset());
        }

        logger.info("Loading gene expression data form {}", geneExpressionFile);
        // Skip header. Column name line does not start with # so the last line read by this while will be this one
        int lineCounter = 0;
        String line;
        while (((line = br.readLine()) != null) && (line.startsWith("#"))) {
            lineCounter++;
        }

        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            if (species.getScientificName().equals(parts[2])) {
                if (parts[7].equals("UP")) {
                    addValueToMapElement(geneExpressionMap, parts[1], new Expression(parts[1], null, parts[3],
                            parts[4], parts[5], parts[6], ExpressionCall.UP, Float.valueOf(parts[8])));
                } else if (parts[7].equals("DOWN")) {
                    addValueToMapElement(geneExpressionMap, parts[1], new Expression(parts[1], null, parts[3],
                            parts[4], parts[5], parts[6], ExpressionCall.DOWN, Float.valueOf(parts[8])));
                } else {
                    logger.warn("Expression tags found different from UP/DOWN at line {}. Entry omitted. ", lineCounter);
                }
            }
            lineCounter++;
        }
        br.close();
        return geneExpressionMap;
    }

    private <T> void addValueToMapElement(Map<String, List<T>> map, String key, T value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            List<T> expressionValueList = new ArrayList<>();
            expressionValueList.add(value);
            map.put(key, expressionValueList);
        }

    }

    private List<TranscriptTfbs> getTranscriptTfbses(Gtf transcript, String chromosome, Map<String, SortedSet<Gff2>> tfbsMap) {
        List<TranscriptTfbs> transcriptTfbses = new ArrayList<>();
        if (tfbsMap.containsKey(chromosome)) {
            for (Gff2 tfbs : tfbsMap.get(chromosome)) {
                if (transcript.getStrand().equals("+")) {
                    if (tfbs.getStart() > transcript.getStart() + 500) {
                        break;
                    } else if (tfbs.getEnd() > transcript.getStart() - 2500) {
//                        transcriptTfbses = addTranscriptTfbstoList(tfbs, transcript, chromosome, transcriptTfbses);
                        transcriptTfbses.add(addTranscriptTfbstoList(tfbs, transcript, chromosome));
                    }
                } else {
                    // transcript in negative strand
                    if (tfbs.getStart() > transcript.getEnd() + 2500) {
                        break;
                    } else if (tfbs.getStart() > transcript.getEnd() - 500) {
//                        transcriptTfbses = addTranscriptTfbstoList(tfbs, transcript, chromosome, transcriptTfbses);
                        transcriptTfbses.add(addTranscriptTfbstoList(tfbs, transcript, chromosome));
                    }
                }
            }
        }
        return transcriptTfbses;
    }

    private TranscriptTfbs addTranscriptTfbstoList(Gff2 tfbs, Gtf transcript, String chromosome) {
//        if (transcriptTfbses == null) {
//            transcriptTfbses = new ArrayList<>();
//        }
        String[] tfbsNameFields = tfbs.getAttribute().split("=")[1].split(":");
//        transcriptTfbses.add(new TranscriptTfbs(tfbsNameFields[0], tfbsNameFields[1], chromosome, tfbs.getStart(), tfbs.getEnd(),
// tfbs.getStrand(), getRelativeTranscriptTfbsStart(tfbs, transcript),
// getRelativeTranscriptTfbsEnd(tfbs, transcript), Float.parseFloat(tfbs.getScore())));
//        TranscriptTfbs transcriptTfbs = new TranscriptTfbs(tfbsNameFields[0], tfbsNameFields[1], chromosome, tfbs.getStart(),
// tfbs.getEnd(), tfbs.getStrand(), getRelativeTranscriptTfbsStart(tfbs, transcript), getRelativeTranscriptTfbsEnd(tfbs, transcript),
// Float.parseFloat(tfbs.getScore()));
        TranscriptTfbs transcriptTfbs = TranscriptTfbs.newBuilder()
                .setTfName(tfbsNameFields[0])
                .setPwm(tfbsNameFields[1])
                .setChromosome(chromosome)
                .setStart(tfbs.getStart())
                .setEnd(tfbs.getEnd())
                .setStrand(tfbs.getStrand())
                .setRelativeStart(getRelativeTranscriptTfbsStart(tfbs, transcript))
                .setRelativeEnd(getRelativeTranscriptTfbsEnd(tfbs, transcript))
                .setScore(Float.parseFloat(tfbs.getScore()))
                .build();

        return transcriptTfbs;
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

    private Map<String, SortedSet<Gff2>> getTfbsMap() {
        // data MotifFeatures content in a Map
        Map<String, SortedSet<Gff2>> tfbsMap = new HashMap<>();
        try {
            if (tfbsFile != null && Files.exists(tfbsFile) && !Files.isDirectory(tfbsFile)) {
                Gff2Reader motifsFeatureReader = new Gff2Reader(tfbsFile);
                Gff2 tfbsMotifFeature;
                while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
                    addTfbsMotifToMap(tfbsMap, tfbsMotifFeature);
                }
                motifsFeatureReader.close();
            }
        } catch (IOException | NoSuchMethodException | FileFormatException e) {
            logger.error("Error loading TFBS file: " + e.getMessage());
            logger.error("transcript TFBS objects will not be serialized");
            tfbsMap.clear();
        }
        return tfbsMap;
    }

    private void addTfbsMotifToMap(Map<String, SortedSet<Gff2>> tfbsMap, Gff2 tfbsMotifFeature) {
        String chromosome = tfbsMotifFeature.getSequenceName().replaceFirst("chr", "");
        SortedSet<Gff2> chromosomeTfbsSet = tfbsMap.get(chromosome);
        if (chromosomeTfbsSet == null) {
            chromosomeTfbsSet = new TreeSet<Gff2>(new Comparator<Gff2>() {
                @Override
                public int compare(Gff2 feature1, Gff2 feature2) {
                    // TODO: maybe this should be in TranscriptTfbs class, and equals method should be overriden too
                    if (feature1.getStart() != feature2.getStart()) {
                        return feature1.getStart() - feature2.getStart();
                    } else {
                        return feature1.getAttribute().compareTo(feature2.getAttribute());
                    }
                }
            });
            tfbsMap.put(chromosome, chromosomeTfbsSet);
        }
        chromosomeTfbsSet.add(tfbsMotifFeature);
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

    private Map<String, ArrayList<Xref>> getXrefMap() throws IOException {
        String[] fields;
        logger.info("Loading xref data...");
        Map<String, ArrayList<Xref>> xrefMap = new HashMap<>();
        if (xrefsFile != null && Files.exists(xrefsFile)) {
            List<String> lines = Files.readAllLines(xrefsFile, Charset.defaultCharset());
            for (String line : lines) {
                fields = line.split("\t", -1);
                if (fields.length >= 4) {
                    if (!xrefMap.containsKey(fields[0])) {
                        xrefMap.put(fields[0], new ArrayList<>());
                    }
//                    xrefMap.get(fields[0]).add(new Xref(fields[1], fields[2], fields[3]));
                    Xref xref = Xref.newBuilder()
                            .setId(fields[1])
                            .setDbName(fields[2])
                            .setDbDisplayName(fields[3])
                            .build();
                    xrefMap.get(fields[0]).add(xref);
                }
            }
        } else {
            logger.warn("Xrefs file " + xrefsFile + " not found");
            logger.warn("Xref data not loaded");
        }

        logger.info("Loading protein mapping into xref data...");
        if (uniprotIdMappingFile != null && Files.exists(uniprotIdMappingFile)) {
            BufferedReader br = FileUtils.newBufferedReader(uniprotIdMappingFile);
            String line;
            while ((line = br.readLine()) != null) {
                fields = line.split("\t", -1);
                if (fields.length >= 19 && fields[19].startsWith("ENST")) {
//                    System.out.println(Arrays.toString(fields[19].split("; ")));
                    String[] transcripts = fields[19].split("; ");
                    for (String transcript : transcripts) {
                        if (!xrefMap.containsKey(transcript)) {
                            xrefMap.put(transcript, new ArrayList<Xref>());
                        }
//                        xrefMap.get(transcript).add(new Xref(fields[0], "uniprotkb_acc", "UniProtKB ACC"));
                        Xref uniprotkbAcc = Xref.newBuilder()
                                .setId(fields[0])
                                .setDbName("uniprotkb_acc")
                                .setDbDisplayName("UniProtKB ACC")
                                .build();
                        xrefMap.get(transcript).add(uniprotkbAcc);

//                        xrefMap.get(transcript).add(new Xref(fields[1], "uniprotkb_id", "UniProtKB ID"));
                        Xref uniprotkbId = Xref.newBuilder()
                                .setId(fields[0])
                                .setDbName("uniprotkb_id")
                                .setDbDisplayName("UniProtKB ID")
                                .build();
                        xrefMap.get(transcript).add(uniprotkbId);
                    }
                }
            }
            br.close();
        } else {
            logger.warn("Uniprot if mapping file " + uniprotIdMappingFile + " not found");
            logger.warn("Protein mapping into xref data not loaded");
        }

        return xrefMap;
    }

    private Map<String, String> getGeneDescriptionMap() throws IOException {
        String[] fields;
        logger.info("Loading gene description data...");
        Map<String, String> geneDescriptionMap = new HashMap<>();
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

//    private Map<String, ArrayList<TranscriptTfbs>> getTfbsMap() throws IOException {
//        String[] fields;
//        Map<String, ArrayList<TranscriptTfbs>> tfbsMap = new HashMap<>();
//        if (tfbsFile != null && Files.exists(tfbsFile) && !Files.isDirectory(tfbsFile)) {
//            List<String> lines = Files.readAllLines(tfbsFile, Charset.defaultCharset());
//            for (String line : lines) {
//                fields = line.split("\t", -1);
//                if (!tfbsMap.containsKey(fields[0])) {
//                    tfbsMap.put(fields[0], new ArrayList<TranscriptTfbs>());
//                }
//                tfbsMap.get(fields[0]).add(new TranscriptTfbs(fields[1], fields[2], fields[3], Integer.parseInt(fields[4]),
// Integer.parseInt(fields[5]), fields[6], Integer.parseInt(fields[7]), Integer.parseInt(fields[8]), Float.parseFloat(fields[9])));
//            }
//        }
//        return tfbsMap;
//    }

    private boolean newGene(Gene previousGene, String newGeneId) {
        return previousGene == null || !newGeneId.equals(previousGene.getId());
    }

    private void connect(Path genomeSequenceFilePath) throws ClassNotFoundException, SQLException, IOException {
        logger.info("Connecting to reference genome sequence database ...");
        Class.forName("org.sqlite.JDBC");
        sqlConn = DriverManager.getConnection("jdbc:sqlite:" + genomeSequenceFilePath.getParent().toString() + "/reference_genome.db");
        if (!Files.exists(Paths.get(genomeSequenceFilePath.getParent().toString(), "reference_genome.db"))
                || Files.size(genomeSequenceFilePath.getParent().resolve("reference_genome.db")) == 0) {
            logger.info("Genome sequence database doesn't exists and will be created");
            Statement createTable = sqlConn.createStatement();
            createTable.executeUpdate("CREATE TABLE if not exists "
                    + "genome_sequence (sequenceName VARCHAR(50), chunkId VARCHAR(30), start INT, end INT, sequence VARCHAR(2000))");
            indexReferenceGenomeFasta(genomeSequenceFilePath);
        }
        indexedSequences = getIndexedSequences();
        sqlQuery = sqlConn.prepareStatement("SELECT sequence from genome_sequence WHERE chunkId = ? "); //AND start <= ? AND end >= ?
        logger.info("Genome sequence database connected");
    }

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

    private void disconnectSqlite() throws SQLException {
        sqlConn.close();
    }

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
//                                          +chunkIdSuffix, start, end, sequenceType, sequenceAssembly, chunkSequence);
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

    private void updateTranscriptAndGeneCoords(Transcript.Builder transcriptBuilder, Gene.Builder geneBuilder, Gtf gtf) {
//        if (transcript.getStart() > gtf.getStart()) {
        if (transcriptBuilder.getStart() > gtf.getStart()) {
//            transcript.setStart(gtf.getStart());
            transcriptBuilder.setStart(gtf.getStart());
        }
//        if (transcript.getEnd() < gtf.getEnd()) {
        if (transcriptBuilder.getEnd() < gtf.getEnd()) {
//            transcript.setEnd(gtf.getEnd());
            transcriptBuilder.setEnd(gtf.getEnd());
        }
//        if (gene.getStart() > gtf.getStart()) {
        if (geneBuilder.getStart() > gtf.getStart()) {
//            gene.setStart(gtf.getStart());
            geneBuilder.setStart(gtf.getStart());
        }
//        if (gene.getEnd() < gtf.getEnd()) {
        if (geneBuilder.getEnd() < gtf.getEnd()) {
//            gene.setEnd(gtf.getEnd());
            geneBuilder.setEnd(gtf.getEnd());
        }
    }

    private Map<String, MiRNAGene> getmiRNAGeneMap(Path mirnaGeneFile) throws IOException {
        logger.info("Loading miRNA data ...");
        Map<String, MiRNAGene> mirnaGeneMap = new HashMap<>();
        if (mirnaFile != null && Files.exists(mirnaFile) && !Files.isDirectory(mirnaFile)) {
            BufferedReader br = Files.newBufferedReader(mirnaGeneFile, Charset.defaultCharset());
            String line;
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

//                miRNAGene = new MiRNAGene(fields[1], fields[2], fields[3], fields[4], aliases, new ArrayList<MiRNAGene.MiRNAMature>());


                // Second, read the miRNA matures, field #6
                mirnaMatures = fields[6].split(",");
                List<MiRNAGene.MiRNAMature> miRNAMatures = new ArrayList<>();
                for (String s : mirnaMatures) {
                    mirnaMaturesFields = s.split("\\|");
                    int cdnaStart = fields[4].indexOf(mirnaMaturesFields[2]) + 1;
                    int cdnaEnd = cdnaStart + mirnaMaturesFields[2].length() - 1;
                    // Save directly into MiRNAGene object.
//                    miRNAGene.addMiRNAMature(mirnaMaturesFields[0], mirnaMaturesFields[1], mirnaMaturesFields[2], cdnaStart, cdnaEnd);
                    MiRNAGene.MiRNAMature miRNAMature = MiRNAGene.MiRNAMature.newBuilder()
                            .setMiRBaseAccession(mirnaMaturesFields[0])
                            .setMiRBaseId(mirnaMaturesFields[1])
                            .setSequence(mirnaMaturesFields[2])
                            .setCdnaStart(cdnaStart)
                            .setCdnaEnd(cdnaEnd)
                            .build();
                    miRNAMatures.add(miRNAMature);
                }

                miRNAGene = MiRNAGene.newBuilder()
                        .setMiRBaseAccession(fields[1])
                        .setMiRBaseId(fields[2])
                        .setStatus(fields[3])
                        .setSequence(fields[4])
                        .addAllAlias(aliases)
                        .addAllMatures(miRNAMatures)
                        .build();

                // Add object to Map<EnsemblID, MiRNAGene>
                mirnaGeneMap.put(fields[0], miRNAGene);
            }
            br.close();
        } else {
            logger.warn("Mirna file " + mirnaFile + " not found");
            logger.warn("Mirna data not loaded");
        }

        return mirnaGeneMap;
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
