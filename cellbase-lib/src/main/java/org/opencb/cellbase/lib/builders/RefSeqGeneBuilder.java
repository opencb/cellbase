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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.formats.feature.gtf.io.GtfReader;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.tools.sequence.FastaIndex;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class RefSeqGeneBuilder extends CellBaseBuilder {

    private Map<String, Transcript> transcriptDict;
    private Map<String, Exon> exonDict;
    private Path gtfFile;
    private Path fastaFile;
    private Path proteinFastaFile, cdnaFastaFile;
    private Path maneFile, lrgFile, disgenetFile, hpoFile, geneDrugFile, miRTarBaseFile, cancerGeneCensus, cancerHotspot;
    private Path tso500File, eglhHaemOncFile;
    private SpeciesConfiguration speciesConfiguration;
    private static final Map<String, String> REFSEQ_CHROMOSOMES = new HashMap<>();
    private final String status = "KNOWN";
    private static final String SOURCE = ParamConstants.QueryParams.REFSEQ.key();
    private Gene gene = null;
    private Transcript transcript = null;
    private Set<Xref> exonDbxrefs = new HashSet<>();
    private Set<Xref> geneDbxrefs = new HashSet<>();
    // sometimes there are two stop codons (eg NM_018159.4). Only parse the first one, skip the second
    private boolean seenStopCodon = false;


    public RefSeqGeneBuilder(Path refSeqDirectoryPath, SpeciesConfiguration speciesConfiguration, CellBaseSerializer serializer) {
        super(serializer);

        this.speciesConfiguration = speciesConfiguration;

        getGtfFileFromDirectoryPath(refSeqDirectoryPath);
        getFastaFileFromDirectoryPath(refSeqDirectoryPath);
        getProteinFastaFileFromDirectoryPath(refSeqDirectoryPath);
        getCdnaFastaFileFromDirectoryPath(refSeqDirectoryPath);
        setAnnotationFiles(refSeqDirectoryPath);

        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);
    }

    private void setAnnotationFiles(Path refSeqDirectoryPath) {
        Path geneDirectoryPath = refSeqDirectoryPath.getParent().resolve("gene");
        maneFile = geneDirectoryPath.resolve("MANE.GRCh38.v0.91.summary.txt.gz");
        lrgFile = geneDirectoryPath.resolve("list_LRGs_transcripts_xrefs.txt");
        geneDrugFile = geneDirectoryPath.resolve("dgidb.tsv");
        disgenetFile = geneDirectoryPath.resolve("all_gene_disease_associations.tsv.gz");
        hpoFile = geneDirectoryPath.resolve("phenotype_to_genes.txt");
        cancerGeneCensus = geneDirectoryPath.resolve("cancer-gene-census.tsv");
        cancerHotspot = geneDirectoryPath.resolve("hotspots_v2.xls");
        tso500File = geneDirectoryPath.resolve("TSO500_transcripts.txt");
        eglhHaemOncFile = geneDirectoryPath.resolve("EGLH_HaemOnc_transcripts.txt");
        miRTarBaseFile = refSeqDirectoryPath.getParent().resolve("regulation/hsa_MTI.xlsx");
    }

    private void getGtfFileFromDirectoryPath(Path refSeqDirectoryPath) {
        for (String fileName : refSeqDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".gtf") || fileName.endsWith(".gtf.gz")) {
                gtfFile = refSeqDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    private void getFastaFileFromDirectoryPath(Path refSeqDirectoryPath) {
        for (String fileName : refSeqDirectoryPath.toFile().list()) {
            if (fileName.endsWith("genomic.fna") || fileName.endsWith("genomic.fna.gz")) {
                fastaFile = refSeqDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    private void getProteinFastaFileFromDirectoryPath(Path refSeqDirectoryPath) {
        for (String fileName : refSeqDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".faa") || fileName.endsWith(".faa.gz")) {
                proteinFastaFile = refSeqDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    private void getCdnaFastaFileFromDirectoryPath(Path refSeqDirectoryPath) {
        for (String fileName : refSeqDirectoryPath.toFile().list()) {
            if (fileName.endsWith("cdna.fna") || fileName.endsWith("cdna.fna.gz")) {
                cdnaFastaFile = refSeqDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    public void parse() throws Exception {
        // Preparing the fasta file for fast accessing
        FastaIndex fastaIndex = null;
//        if (fastaFile != null) {
//            fastaIndex = new FastaIndex(fastaFile);
//        }

        // index protein sequences for later
        RefSeqGeneBuilderIndexer indexer = new RefSeqGeneBuilderIndexer(gtfFile.getParent());
        indexer.index(maneFile, lrgFile, proteinFastaFile, cdnaFastaFile, geneDrugFile, hpoFile, disgenetFile, miRTarBaseFile,
                cancerGeneCensus, cancerHotspot, tso500File, eglhHaemOncFile);

        logger.info("Parsing RefSeq gtf...");
        GtfReader gtfReader = new GtfReader(gtfFile);

        Gtf gtf;
        while ((gtf = gtfReader.read()) != null) {
            String chromosome = getSequenceName(gtf.getSequenceName());
            switch (gtf.getFeature()) {
                case "gene":
                    // we've finished the previous transcript, store xrefs
                    addXrefs(transcript, geneDbxrefs, exonDbxrefs);
                    parseGene(gtf, chromosome, indexer);
                    break;
                case "transcript":
                    break;
                case "exon":
                    parseExon(gtf, chromosome, fastaIndex, indexer);
                    break;
                case "CDS":
                    parseCDS(gtf, indexer);
                    break;
                case "start_codon":
                    seenStopCodon = false;
                    break;
                case "stop_codon":
                    if (!seenStopCodon) {
                        parseStopCodon(gtf);
                        seenStopCodon = true;
                    }
                    break;
                default:
                    throw new RuntimeException("Unexpected feature type: " + gtf.getFeature());
            }
        }

        // add xrefs to last transcript
        addXrefs(transcript, geneDbxrefs, exonDbxrefs);

        // last gene must be serialized
        store();

        // cleaning
        gtfReader.close();
        serializer.close();
        if (fastaIndex != null) {
            fastaIndex.close();
        }
        indexer.close();
    }

    // store right before parsing the previous gene, or the very last gene.
    private void store() {
        serializer.serialize(gene);
        reset();
    }

    // Empty transcript and exon dictionaries
    private void reset() {
        transcriptDict.clear();
        exonDict.clear();
        exonDbxrefs = new HashSet<>();
        geneDbxrefs = new HashSet<>();
        gene = null;
        transcript = null;
    }

    private void addXrefs(Transcript transcript, Set<Xref> geneDbxrefs, Set<Xref> exonDbxrefs) {
        if (transcript == null) {
            return;
        }
        exonDbxrefs.addAll(geneDbxrefs);
//        transcript.setXrefs(new ArrayList<>(exonDbxrefs));
        transcript.getXrefs().addAll(exonDbxrefs);
        transcript.getXrefs().add(new Xref(transcript.getName(), "HGNC", "HGNC Symbol"));

        // transcript has version, e.g. XR_002957988.1. put both XR_002957988 AND XR_002957988.1 in xrefs
        String transcriptId = transcript.getId();
        Xref transcriptWithVersion = new Xref(transcriptId, "RefSeq", "RefSeq");
        transcript.getXrefs().add(transcriptWithVersion);
        String[] transcriptAndVersion = transcriptId.split("\\.");
        if (transcriptAndVersion.length == 2) {
            Xref transcriptWithoutVersion = new Xref(transcriptAndVersion[0], "RefSeq", "RefSeq");
            transcript.getXrefs().add(transcriptWithoutVersion);
        }
    }

    private void parseGene(Gtf gtf, String chromosome, RefSeqGeneBuilderIndexer indexer)
            throws CellBaseException, IOException, RocksDBException {
        // If new geneId is different from the current then we must serialize before data new gene
        if (gene != null) {
            store();
        }

        String geneId = getGeneId(gtf);
        String geneName = gtf.getAttributes().get("gene_id");
        String geneDescription = gtf.getAttributes().get("description");
        String geneBiotype = gtf.getAttributes().get("gene_biotype");

        GeneAnnotation geneAnnotation = new GeneAnnotation(null, indexer.getDiseases(geneName), indexer.getDrugs(geneName),
                null, indexer.getMirnaTargets(geneName), indexer.getCancerGeneCensus(geneName), indexer.getCancerHotspot(geneName));

        gene = new Gene(geneId, geneName, chromosome, gtf.getStart(), gtf.getEnd(), gtf.getStrand(), "1", geneBiotype,
                status, SOURCE, geneDescription, new ArrayList<>(), null, geneAnnotation);
        geneDbxrefs = parseXrefs(gtf);
    }

    private void parseExon(Gtf gtf, String chromosome, FastaIndex fastaIndex, RefSeqGeneBuilderIndexer indexer) throws RocksDBException {
        String transcriptId = gtf.getAttributes().get("transcript_id");

        // new transcript
        if (!transcriptDict.containsKey(transcriptId)) {
            // previous transcript is done being parsed, we have all the xrefs now.
            if (transcript != null) {
                addXrefs(transcript, geneDbxrefs, exonDbxrefs);
            }

            // microRNAs for example do not have a version. default to 1.
            String transcriptVersion = "1";
            if (transcriptId.contains(".")) {
                transcriptVersion = transcriptId.split("\\.")[1];
            }

            transcript = getTranscript(gtf, chromosome, transcriptId, transcriptVersion, indexer);
        } else {
            transcript = transcriptDict.get(transcriptId);
        }

        String exonSequence = null;
        if (fastaIndex != null) {
            exonSequence = fastaIndex.query(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());
        }
        String exonNumber = gtf.getAttributes().get("exon_number");
        // RefSeq does not provode Exon IDs, we are using transcript ID and exon numbers
        String exonId = transcriptId + "_" + exonNumber;

        // default value. can be overwritten in the CDS entry
        int phase = -1;

        Exon exon = new Exon(exonId, chromosome, gtf.getStart(), gtf.getEnd(), gtf.getStrand(), 0, 0,
                0, 0, 0, 0, phase, Integer.parseInt(exonNumber), exonSequence);
        transcript.getExons().add(exon);
        exonDict.put(transcript.getId() + "_" + exon.getExonNumber(), exon);

        exonDbxrefs.addAll(parseXrefs(gtf));

        if (transcript.getStart() == 0 || transcript.getStart() > exon.getStart()) {
            transcript.setStart(exon.getStart());
        }
        if (transcript.getEnd() == 0 || transcript.getEnd() < exon.getEnd()) {
            transcript.setEnd(exon.getEnd());
        }
    }

    private void parseCDS(Gtf gtf, RefSeqGeneBuilderIndexer indexer) throws RocksDBException {
        String exonNumber = gtf.getAttributes().get("exon_number");
        if (StringUtils.isEmpty(exonNumber)) {
            // this CDS doesn't know which exon it belongs to. skip
            return;
        }

        transcript = transcriptDict.get(gtf.getAttributes().get("transcript_id"));
        String exonId = transcript.getId() + "_" + exonNumber;
        Exon exon = exonDict.get(exonId);

        // doesn't matter which strand
        String proteinId = gtf.getAttributes().get("protein_id");
        transcript.setProteinId(proteinId);
        transcript.setProteinSequence(indexer.getProteinFasta(proteinId));
        exon.setPhase(Integer.parseInt(gtf.getFrame()));
        exonDbxrefs.addAll(parseXrefs(gtf));

        if (gtf.getStrand().equals("+")) {

            if (exon.getExonNumber() == 1) {
                exon.setGenomicCodingStart(gtf.getStart());
                exon.setGenomicCodingEnd(gtf.getEnd());

                int cdnaCodingStart = exon.getGenomicCodingStart() - exon.getStart() + 1;
                exon.setCdnaCodingStart(cdnaCodingStart);
                exon.setCdnaCodingEnd((exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + cdnaCodingStart);

                exon.setCdsStart(1);
                exon.setCdsEnd((exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + exon.getCdsStart());
            } else {
                // Fetch prev exon
                String prevExonId = transcript.getId() + "_" + (exon.getExonNumber() - 1);
                Exon prevExon = exonDict.get(prevExonId);

                exon.setGenomicCodingStart(gtf.getStart());
                exon.setGenomicCodingEnd(gtf.getEnd());

                int cdnaCodingStart = 0;
                int cdnaCodingEnd;
                // Prev exon is a UTR
                if (prevExon.getCdnaCodingStart() == 0) {
                    // previous exon was a UTR. check that the exon BEFORE that one also. could be two in a row!
                    for (int i = 1; i <= prevExon.getExonNumber(); i++) {
                        Exon beforePreviousExon = exonDict.get(transcript.getId() + "_" + i);
                        cdnaCodingStart += beforePreviousExon.getEnd() - beforePreviousExon.getStart() + 1;
                    }
                    cdnaCodingStart += exon.getGenomicCodingStart() - exon.getStart() + 1;
                    cdnaCodingEnd = (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + cdnaCodingStart;
                } else {
                    cdnaCodingStart = prevExon.getCdnaCodingEnd() + 1;
                    cdnaCodingEnd = (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + cdnaCodingStart;
                }

                exon.setCdnaCodingStart(cdnaCodingStart);
                exon.setCdnaCodingEnd(cdnaCodingEnd);

                // Set CDS
                int cdsStart = prevExon.getCdsEnd() + 1;
                int cdsEnd = cdsStart + (cdnaCodingEnd - cdnaCodingStart);
                exon.setCdsStart(cdsStart);
                exon.setCdsEnd(cdsEnd);
            }
        } else {
            // negative strand

            if (exon.getExonNumber() == 1) {
                exon.setGenomicCodingStart(gtf.getStart());
                exon.setGenomicCodingEnd(gtf.getEnd());

                // add 1 for 0-base
                int cdnaCodingStart = exon.getEnd() - exon.getGenomicCodingEnd() + 1;
                exon.setCdnaCodingStart(cdnaCodingStart);
                exon.setCdnaCodingEnd((exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + cdnaCodingStart);

                exon.setCdsStart(1);
                exon.setCdsEnd(exon.getGenomicCodingEnd() - exon.getGenomicCodingStart() + 1);
            } else {
                // Fetch prev exon
                String prevExonId = transcript.getId() + "_" + (exon.getExonNumber() - 1);
                Exon prevExon = exonDict.get(prevExonId);

                exon.setGenomicCodingStart(gtf.getStart());
                exon.setGenomicCodingEnd(gtf.getEnd());

                int cdnaCodingStart = 0;
                int cdnaCodingEnd;
                // Prev exon is a UTR
                if (prevExon.getCdnaCodingStart() == 0) {
                    // previous exon was a UTR. check that the exon BEFORE that one also. could be two in a row!
                    for (int i = 1; i <= prevExon.getExonNumber(); i++) {
                        Exon beforePreviousExon = exonDict.get(transcript.getId() + "_" + i);
                        cdnaCodingStart += beforePreviousExon.getEnd() - beforePreviousExon.getStart() + 1;
                    }
                    // +1 0-base
                    cdnaCodingStart += exon.getEnd() - exon.getGenomicCodingEnd() + 1;
                    cdnaCodingEnd = (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + cdnaCodingStart;
                } else {
                    cdnaCodingStart = prevExon.getCdnaCodingEnd() + 1;
                    cdnaCodingEnd = (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()) + cdnaCodingStart;
                }

                exon.setCdnaCodingStart(cdnaCodingStart);
                exon.setCdnaCodingEnd(cdnaCodingEnd);

                // Set CDS
                int cdsStart = prevExon.getCdsEnd() + 1;
                int cdsEnd = cdsStart + (cdnaCodingEnd - cdnaCodingStart);
                exon.setCdsStart(cdsStart);
                exon.setCdsEnd(cdsEnd);
            }
        }

        // strand doesn't matter
        if (transcript.getGenomicCodingStart() == 0 || transcript.getGenomicCodingStart() > exon.getGenomicCodingStart()) {
            transcript.setGenomicCodingStart(exon.getGenomicCodingStart());
        }
        if (transcript.getGenomicCodingEnd() == 0 || transcript.getGenomicCodingEnd() < exon.getGenomicCodingEnd()) {
            transcript.setGenomicCodingEnd(exon.getGenomicCodingEnd());
        }

        // only first time
        if (transcript.getCdnaCodingStart() == 0 || transcript.getCdnaCodingStart() > exon.getCdnaCodingStart()) {
            transcript.setCdnaCodingStart(exon.getCdnaCodingStart());
        }
        // Set cdnaCodingEnd to prevent those cases without stop_codon
        if (transcript.getCdnaCodingEnd() == 0 || transcript.getCdnaCodingEnd() < exon.getCdnaCodingEnd()) {
            transcript.setCdnaCodingEnd(exon.getCdnaCodingEnd());
        }
        transcript.setCdsLength(transcript.getCdnaCodingEnd() - transcript.getCdnaCodingStart() + 1);
    }

    private void parseStopCodon(Gtf gtf) {
        String exonNumber = gtf.getAttributes().get("exon_number");
        if (StringUtils.isEmpty(exonNumber)) {
            // some codons don't have an exon number, discard
            return;
        }
        Transcript transcript = transcriptDict.get(gtf.getAttributes().get("transcript_id"));
        String exonId = transcript.getId() + "_" + exonNumber;
        Exon exon = exonDict.get(exonId);

        if (gtf.getStrand().equals("+")) {
            if (exon.getGenomicCodingStart() == 0) {
                // this exon has no CDS, set all values

                String prevExonId = transcript.getId() + "_" + (exon.getExonNumber() - 1);
                Exon prevExon = exonDict.get(prevExonId);

                exon.setGenomicCodingStart(gtf.getStart());
                exon.setGenomicCodingEnd(gtf.getEnd());

                exon.setCdnaCodingStart(prevExon.getCdnaCodingEnd() + 1);
                exon.setCdnaCodingEnd(prevExon.getCdnaCodingEnd() + 3);

                // Set CDS
                int cdsStart = prevExon.getCdsEnd() + 1;
                int cdsEnd = cdsStart + 2;
                exon.setCdsStart(cdsStart);
                exon.setCdsEnd(cdsEnd);
            } else {
                // In the positive strand, genomicCodingEnd for the last exon should be the "STOP CODON end"
                exon.setGenomicCodingEnd(gtf.getEnd());
                exon.setCdnaCodingEnd(exon.getCdnaCodingStart() + (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()));
                exon.setCdsEnd(exon.getCdsStart() + (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()));
            }

            // overwrite transcript values
            transcript.setGenomicCodingEnd(exon.getGenomicCodingEnd());
            transcript.setCdnaCodingEnd(exon.getCdnaCodingEnd());
            transcript.setCdsLength(transcript.getCdnaCodingEnd() - transcript.getCdnaCodingStart());

            // For NM_212554.4, the stop codon is split across the last intron with the first two bases of the codon in exon six
            // and the third base of the stop codon in exon seven
            if (gtf.getEnd() - gtf.getStart() == 1) {
                String nextExonId = transcript.getId() + "_" + (exon.getExonNumber() + 1);
                Exon nextExon = exonDict.get(nextExonId);

                nextExon.setGenomicCodingStart(nextExon.getStart());
                nextExon.setGenomicCodingEnd(nextExon.getStart());
                nextExon.setCdnaCodingStart(exon.getCdnaCodingEnd() + 1);
                nextExon.setCdnaCodingEnd(exon.getCdnaCodingEnd() + 1);
                nextExon.setCdsStart(exon.getCdsEnd() + 1);
                nextExon.setCdsEnd(exon.getCdsEnd() + 1);

                transcript.setGenomicCodingEnd(nextExon.getStart());
                transcript.setCdnaCodingEnd(transcript.getCdnaCodingEnd() + 1);
                transcript.setCdsLength(transcript.getCdnaCodingEnd() - transcript.getCdnaCodingStart());
            }

        } else {
            if (exon.getGenomicCodingStart() == 0) {
                // this exon has no CDS, set all values

                String prevExonId = transcript.getId() + "_" + (exon.getExonNumber() - 1);
                Exon prevExon = exonDict.get(prevExonId);

                exon.setGenomicCodingStart(gtf.getStart());
                exon.setGenomicCodingEnd(gtf.getEnd());

                exon.setCdnaCodingStart(prevExon.getCdnaCodingEnd() + 1);
                exon.setCdnaCodingEnd(prevExon.getCdnaCodingEnd() + 3);

                // Set CDS
                int cdsStart = prevExon.getCdsEnd() + 1;
                int cdsEnd = cdsStart + 2;
                exon.setCdsStart(cdsStart);
                exon.setCdsEnd(cdsEnd);
            } else {
                // In the negative strand, genomicCodingStart for the first exon should be the "STOP CODON start".
                exon.setGenomicCodingStart(gtf.getStart());
                exon.setCdnaCodingEnd(exon.getCdnaCodingStart() + (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()));
                exon.setCdsEnd(exon.getCdsStart() + (exon.getGenomicCodingEnd() - exon.getGenomicCodingStart()));
            }

            transcript.setGenomicCodingStart(exon.getGenomicCodingStart());
            transcript.setCdnaCodingEnd(exon.getCdnaCodingEnd());
            transcript.setCdsLength(transcript.getCdnaCodingEnd() - transcript.getCdnaCodingStart());

            // For NM_212554.4, the stop codon is split across the last intron with the first two bases of the codon in exon six
            // and the third base of the stop codon in exon seven
            if (gtf.getEnd() - gtf.getStart() == 1) {
                String nextExonId = transcript.getId() + "_" + (exon.getExonNumber() + 1);
                Exon nextExon = exonDict.get(nextExonId);

                nextExon.setGenomicCodingStart(nextExon.getStart());
                nextExon.setGenomicCodingEnd(nextExon.getStart());
                nextExon.setCdnaCodingStart(exon.getCdnaCodingEnd() + 1);
                nextExon.setCdnaCodingEnd(exon.getCdnaCodingEnd() + 1);
                nextExon.setCdsStart(exon.getCdsEnd() + 1);
                nextExon.setCdsEnd(exon.getCdsEnd() + 1);

                transcript.setGenomicCodingStart(nextExon.getEnd());
                transcript.setCdnaCodingEnd(transcript.getCdnaCodingEnd() + 1);
                transcript.setCdsLength(transcript.getCdnaCodingEnd() - transcript.getCdnaCodingStart());
            }
        }
    }

    private Set<Xref> parseXrefs(Gtf gtf) {
        Set<Xref> xrefSet = new HashSet<>();
        String xrefs = gtf.getAttributes().get("db_xref");
        if (StringUtils.isNotEmpty(xrefs)) {
            for (String xrefString : xrefs.split(",")) {
                String[] dbxrefParts = xrefString.split(":", 2);
                if (dbxrefParts.length != 2) {
                    throw new RuntimeException("Bad xref, expected colon: " + xrefString);
                }
                String id = dbxrefParts[1];
                String dbName = dbxrefParts[0];
                String dbDisplayName = dbName;
                if ("HGNC".equalsIgnoreCase(dbName)) {
                    dbDisplayName = "HGNC ID";
                }
                Xref xref = new Xref(id, dbName, dbDisplayName);
                xrefSet.add(xref);
            }
        }
        return xrefSet;
    }

    private Transcript getTranscript(Gtf gtf, String chromosome, String transcriptId, String version, RefSeqGeneBuilderIndexer indexer)
            throws RocksDBException {
        Map<String, String> gtfAttributes = gtf.getAttributes();

        String name = gene.getName();
//        String biotype = gtfAttributes.get("gbkey");
        String biotype = gtfAttributes.get("transcript_biotype");
        if ("mRNA".equals(biotype)) {
            biotype = "protein_coding";
        }
        transcript = new Transcript(transcriptId, name, chromosome, gtf.getStart(), gtf.getEnd(), gtf.getStrand(), biotype, status,
                0, 0, 0, 0, 0,
                indexer.getCdnaFasta(transcriptId), "", "", "", version, SOURCE,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashSet<>(), new TranscriptAnnotation());

        // Add MANE Select mappings, with this we can know which Ensembl and Refseq transcripts match according to MANE
        for (String suffix: Arrays.asList("ensembl", "ensembl_protein")) {
            String maneRefSeq = indexer.getMane(transcriptId, suffix);
            if (StringUtils.isNotEmpty(maneRefSeq)) {
                transcript.getXrefs().add(new Xref(maneRefSeq, "mane_select_" + suffix,
                        "MANE Select Ensembl" + (suffix.contains("_") ? " Protein" : "")));
            }
        }

        // Add LRG mappings, with this we can know which Ensembl and Refseq transcripts match according to LRG
        String lrgRefSeq = indexer.getLrg(transcriptId, "ensembl");
        if (StringUtils.isNotEmpty(lrgRefSeq)) {
            transcript.getXrefs().add(new Xref(lrgRefSeq, "lrg_ensembl", "LRG Ensembl"));
        }

        // Add Flags
        // 1. MANE Flag
        String maneFlag = indexer.getMane(transcriptId, "flag");
        if (StringUtils.isNotEmpty(maneFlag)) {
            transcript.getFlags().add(maneFlag);
        }
        // 2. LRG Flag
        String lrg = indexer.getLrg(transcriptId, "refseq");
        if (StringUtils.isNotEmpty(lrg)) {
            transcript.getFlags().add("LRG");
        }
        // 3. TSO500 and EGLH HaemOnc
        String tso500Flag = indexer.getTSO500(transcriptId.split("\\.")[0]);
        if (StringUtils.isNotEmpty(tso500Flag)) {
            System.out.println("tso500Flag = " + tso500Flag);
            transcript.getFlags().add(tso500Flag);
        }
        String eglhHaemOncFlag = indexer.getEGLHHaemOnc(transcriptId.split("\\.")[0]);
        if (StringUtils.isNotEmpty(eglhHaemOncFlag)) {
            System.out.println("eglhHaemOncFlag = " + eglhHaemOncFlag);
            transcript.getFlags().add(eglhHaemOncFlag);
        }

        gene.getTranscripts().add(transcript);

        transcriptDict.put(transcriptId, transcript);
        return transcript;
    }

    private String getGeneId(Gtf gtf) throws CellBaseException {
        // db_xref "GeneID:100287102";
        String xrefString = gtf.getAttributes().get("db_xref");
        String[] xrefs = xrefString.split(",");
        for (String xref : xrefs) {
            String[] xrefParts = xref.split(":");
            if ("GeneID".equals(xrefParts[0])) {
                return xrefParts[1];
            }
        }
        // didn't find geneId!
        throw new CellBaseException("Didn't find geneId for db_xref:" + xrefString);
    }

    private String getSequenceName(String fullSequenceName) {
        String[] sequenceNameParts = fullSequenceName.split("\\.");

        if (sequenceNameParts.length != 2) {
            throw new RuntimeException("bad chromosome: " + fullSequenceName);
        }

        // just get the first part, e.g. NC_000024.11
        String sequenceName = sequenceNameParts[0];
        if (REFSEQ_CHROMOSOMES.containsKey(sequenceName)) {
            return REFSEQ_CHROMOSOMES.get(sequenceName);
        }
        // scaffold
        return fullSequenceName;
    }

    static {
        REFSEQ_CHROMOSOMES.put("NC_000001", "1");
        REFSEQ_CHROMOSOMES.put("NC_000002", "2");
        REFSEQ_CHROMOSOMES.put("NC_000003", "3");
        REFSEQ_CHROMOSOMES.put("NC_000004", "4");
        REFSEQ_CHROMOSOMES.put("NC_000005", "5");
        REFSEQ_CHROMOSOMES.put("NC_000006", "6");
        REFSEQ_CHROMOSOMES.put("NC_000007", "7");
        REFSEQ_CHROMOSOMES.put("NC_000008", "8");
        REFSEQ_CHROMOSOMES.put("NC_000009", "9");
        REFSEQ_CHROMOSOMES.put("NC_000010", "10");
        REFSEQ_CHROMOSOMES.put("NC_000011", "11");
        REFSEQ_CHROMOSOMES.put("NC_000012", "12");
        REFSEQ_CHROMOSOMES.put("NC_000013", "13");
        REFSEQ_CHROMOSOMES.put("NC_000014", "14");
        REFSEQ_CHROMOSOMES.put("NC_000015", "15");
        REFSEQ_CHROMOSOMES.put("NC_000016", "16");
        REFSEQ_CHROMOSOMES.put("NC_000017", "17");
        REFSEQ_CHROMOSOMES.put("NC_000018", "18");
        REFSEQ_CHROMOSOMES.put("NC_000019", "19");
        REFSEQ_CHROMOSOMES.put("NC_000020", "20");
        REFSEQ_CHROMOSOMES.put("NC_000021", "21");
        REFSEQ_CHROMOSOMES.put("NC_000022", "22");
        REFSEQ_CHROMOSOMES.put("NC_000023", "X");
        REFSEQ_CHROMOSOMES.put("NC_000024", "Y");
    }
}
