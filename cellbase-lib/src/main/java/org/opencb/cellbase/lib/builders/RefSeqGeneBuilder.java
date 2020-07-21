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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.formats.feature.gtf.io.GtfReader;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.tools.sequence.FastaIndex;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.nio.file.Path;
import java.util.*;

public class RefSeqGeneBuilder extends CellBaseBuilder {

    private Map<String, Integer> transcriptDict;
    private Map<String, Exon> exonDict;
    private Path gtfFile;
    private Path fastaFile;
    private SpeciesConfiguration speciesConfiguration;
    private static final Map<String, String> REFSEQ_CHROMOSOMES = new HashMap<>();
    private final String status = "KNOWN";
    private final String source = "RefSeq";
    private Gene gene = null;
    private Transcript transcript = null;
    private Exon exon = null;
    private Integer cdna = 1;
    private Integer cds = 1;
    private Set<Xref> dbxrefs = new HashSet<>();
    private Set<Xref> geneDbxrefs = new HashSet<>();

    public RefSeqGeneBuilder(Path refSeqDirectoryPath, SpeciesConfiguration speciesConfiguration, CellBaseSerializer serializer) {
        super(serializer);
        this.speciesConfiguration = speciesConfiguration;

        getGtfFileFromDirectoryPath(refSeqDirectoryPath);
        getFastaFileFromDirectoryPath(refSeqDirectoryPath);

        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);
    }

    private void getGtfFileFromDirectoryPath(Path geneDirectoryPath) {
        for (String fileName : geneDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".gtf") || fileName.endsWith(".gtf.gz")) {
                gtfFile = geneDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    private void getFastaFileFromDirectoryPath(Path geneDirectoryPath) {
        for (String fileName : geneDirectoryPath.toFile().list()) {
            if (fileName.endsWith(".fa") || fileName.endsWith(".fa.gz")) {
                fastaFile = geneDirectoryPath.resolve(fileName);
                break;
            }
        }
    }

    public void parse() throws Exception {
        int geneVersion = 1; // FIXME where is the gene version?

        // Preparing the fasta file for fast accessing
        //FastaIndex fastaIndex = new FastaIndex(fastaFile);
        FastaIndex fastaIndex = null; // FIXME which fasta to use?

        logger.info("Parsing RefSeq gtf...");
        GtfReader gtfReader = new GtfReader(gtfFile);

        Gtf gtf;
        while ((gtf = gtfReader.read()) != null) {

            String chromosome = getSequenceName(gtf.getSequenceName());
            switch (gtf.getFeature()) {
                case "gene":
                    parseGene(gtf, chromosome, geneVersion);
                    break;
                case "exon":
                    parseExon(gtf, chromosome, fastaIndex);
                    break;
                case "CDS":
                    parseCDS(gtf);
                    break;
                case "start_codon":
                    // should I be doing something here?
                    break;
                case "stop_codon":
                    parseStopCodon(gtf, transcript, exon, cdna, cds);
                    break;
                default:
                    throw new RuntimeException("Unexpected feature type: " + gtf.getFeature());
            }
        }

        // add xrefs to last transcript
        dbxrefs.addAll(geneDbxrefs);
        transcript.setXrefs(new ArrayList<>(dbxrefs));

        // last gene must be serialized
        store();

        // cleaning
        gtfReader.close();
        serializer.close();
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
        dbxrefs = new HashSet<>();
        geneDbxrefs = new HashSet<>();
        gene = null;
        transcript = null;
        exon = null;
        cdna = 1;
        cds = 1;
    }

    private void parseStopCodon(Gtf gtf, Transcript transcript, Exon exon, Integer cdna, Integer cds) {
        if (exon.getStrand().equals("+")) {
            updateStopCodingDataPositiveExon(exon, cdna, cds, gtf);

            cds += gtf.getEnd() - gtf.getStart();
            // If stop_codon appears, overwrite values
            transcript.setGenomicCodingEnd(gtf.getEnd());
            transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
            transcript.setCdsLength(cds - 1);

        } else {
            updateNegativeExonCodingData(exon, cdna, cds, gtf);

            cds += gtf.getEnd() - gtf.getStart();
            // If stop_codon appears, overwrite values
            transcript.setGenomicCodingStart(gtf.getStart());
            // cdnaCodingEnd points to the same base position than genomicCodingStart
            transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
            transcript.setCdsLength(cds - 1);
        }
    }

    private void parseCDS(Gtf gtf) {
        exon.setGenomicCodingStart(gtf.getStart());
        exon.setGenomicCodingEnd(gtf.getEnd());

        exon.setCdsStart(cds);
        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

        transcript.setProteinId(gtf.getAttributes().get("protein_id"));

        exon.setPhase(Integer.parseInt(gtf.getFrame()));

        dbxrefs.addAll(parseXrefs(gtf));

        if (gtf.getStrand().equals("+")) {

            // cDNA coordinates
            exon.setCdnaCodingStart(gtf.getStart() - exon.getStart() + cdna);
            exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);

            // Set cdnaCodingEnd to prevent those cases without stop_codon
            transcript.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);

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
        } else {

            // cDNA coordinates
            // cdnaCodingStart points to the same base position than genomicCodingEnd
            exon.setCdnaCodingStart(exon.getEnd() - gtf.getEnd() + cdna);
            // cdnaCodingEnd points to the same base position than genomicCodingStart
            exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);

            // Set cdnaCodingEnd to prevent those cases without stop_codon
            transcript.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);

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

        // increment in the coding length
        cds += gtf.getEnd() - gtf.getStart() + 1;
        transcript.setCdsLength(cds - 1);  // Set cdnaCodingEnd to prevent those cases without stop_codon

    }

    private void updateStopCodingDataPositiveExon(Exon exon, int cdna, int cds, Gtf gtf) {
        // we need to increment 3 nts, the stop_codon length.
        exon.setGenomicCodingEnd(gtf.getEnd());
        exon.setCdnaCodingEnd(gtf.getEnd() - exon.getStart() + cdna);
        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

        // If the STOP codon corresponds to the first three nts of the exon then no CDS will be defined
        // in the gtf -as technically the STOP codon is non-coding- and we must manually set coding
        // starts
        if (exon.getGenomicCodingStart() == 0) {
            exon.setGenomicCodingStart(exon.getGenomicCodingEnd() - 2);
        }
        if (exon.getCdnaCodingStart() == 0) {
            exon.setCdnaCodingStart(exon.getCdnaCodingEnd() - 2);
        }
        if (exon.getCdsStart() == 0) {
            exon.setCdsStart(exon.getCdsEnd() - 2);
        }
    }

    private void updateNegativeExonCodingData(Exon exon, int cdna, int cds, Gtf gtf) {
        // we need to increment 3 nts, the stop_codon length.
        exon.setGenomicCodingStart(gtf.getStart());
        // cdnaCodingEnd points to the same base position than genomicCodingStart
        exon.setCdnaCodingEnd(exon.getEnd() - gtf.getStart() + cdna);
        exon.setCdsEnd(gtf.getEnd() - gtf.getStart() + cds);

        // If the STOP codon corresponds to the first three nts of the exon then no CDS will be defined
        // in the gtf -as technically the STOP codon is non-coding- and we must manually set coding
        // starts
        if (exon.getGenomicCodingEnd() == 0) {
            exon.setGenomicCodingEnd(exon.getGenomicCodingStart() + 2);
        }
        if (exon.getCdnaCodingStart() == 0) {
            exon.setCdnaCodingStart(exon.getCdnaCodingEnd() - 2);
        }
        if (exon.getCdsStart() == 0) {
            exon.setCdsStart(exon.getCdsEnd() - 2);
        }
    }

    private void parseGene(Gtf gtf, String chromosome, int geneVersion) throws CellbaseException {
        // If new geneId is different from the current then we must serialize before data new gene
        if (gene != null) {
            store();
        }

        String geneId = getGeneId(gtf);
        String geneName = gtf.getAttributes().get("gene_id");
        String geneDescription = gtf.getAttributes().get("description");
        String geneBiotype = gtf.getAttributes().get("gene_biotype");
        gene = new Gene(geneId, geneName, chromosome, gtf.getStart(), gtf.getEnd(), gtf.getStrand(), geneVersion, geneBiotype,
                status, source, geneDescription, new ArrayList<>(), null, null);
        geneDbxrefs = parseXrefs(gtf);
    }

    private Set<Xref> parseXrefs(Gtf gtf) {
        String xrefs = gtf.getAttributes().get("db_xref");
        Set<Xref> xrefSet = new HashSet();
        if (StringUtils.isNotEmpty(xrefs)) {
            for (String xrefString : xrefs.split(",")) {
                String[] dbxrefParts = xrefString.split(":", 2);
                if (dbxrefParts.length != 2) {
                    throw new RuntimeException(" bad xref " + dbxrefParts[0] + " + " + xrefString + " + " + dbxrefParts.length);
                }
                String dbName = dbxrefParts[0];
                String id = dbxrefParts[1];
                Xref xref = new Xref();
                xref.setId(id);
                xref.setDbDisplayName(dbName);
                xref.setDbName(dbName);
                xrefSet.add(xref);
            }
        }
        return xrefSet;
    }

    private void parseExon(Gtf gtf, String chromosome, FastaIndex fastaIndex) {
        String transcriptId = gtf.getAttributes().get("transcript_id");
        String transcriptVersion = "1";
        if (transcriptId.contains(".")) {
            transcriptVersion = transcriptId.split("\\.")[1];
        }

        // new transcript.
        if (!transcriptDict.containsKey(transcriptId)) {
            // previous transcript is done being parsed, we have all the xrefs.
            if (transcript != null) {
                dbxrefs.addAll(geneDbxrefs);
                transcript.setXrefs(new ArrayList<>(dbxrefs));
            }

            Integer version = -1; // FIXME
            // create new transcript
            if (NumberUtils.isParsable(transcriptVersion)) {
                version = Integer.valueOf(transcriptVersion);
            }
            transcript = getTranscript(gtf, chromosome, transcriptId, version);
        } else {
            transcript = gene.getTranscripts().get(transcriptDict.get(transcriptId));
        }

//        String exonSequence = fastaIndex.query(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());
        String exonSequence = "FIXME-NEED-FASTA"; // FIXME need fasta
        String exonNumber = gtf.getAttributes().get("exon_number");
        String exonId = transcriptId + "_" + exonNumber;

        // default value. can be overwritten in the CDS entry
        int phase = -1;

        exon = new Exon(exonId, chromosome, gtf.getStart(), gtf.getEnd(), gtf.getStrand(), 0, 0, 0, 0, 0, 0, phase,
                Integer.parseInt(exonNumber), exonSequence);
        transcript.getExons().add(exon);
        exonDict.put(transcript.getId() + "_" + exon.getExonNumber(), exon);

        dbxrefs.addAll(parseXrefs(gtf));

        if (gtf.getAttributes().get("exon_number").equals("1")) {
            cdna = 1;
            cds = 1;
        } else {
            // with every exon we update cDNA length with the previous exon length
            cdna += exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)).getEnd()
                    - exonDict.get(transcript.getId() + "_" + (exon.getExonNumber() - 1)).getStart() + 1;
        }
    }

    private Transcript getTranscript(Gtf gtf, String chromosome, String transcriptId, int version) {
        Map<String, String> gtfAttributes = gtf.getAttributes();
        String biotype = gtfAttributes.get("gbkey");
        String transcriptName = gene.getName();

        transcript = new Transcript(transcriptId, transcriptName, biotype, status, source, chromosome, gtf.getStart(), gtf.getEnd(),
                gtf.getStrand(), version, null, 0, 0, 0, 0,
                0, "", "", null, new ArrayList<Exon>(),
                null, null);

        gene.getTranscripts().add(transcript);

        // Do not change order!! size()-1 is the index of the transcript ID
        transcriptDict.put(transcriptId, gene.getTranscripts().size() - 1);
        return transcript;
    }

    private String getGeneId(Gtf gtf) throws CellbaseException {
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
        throw new CellbaseException("Didn't find geneId for db_xref:" + xrefString);
    }

    private String getSequenceName(String fullSequenceName) {
        String[] sequenceNameParts = fullSequenceName.split("\\.");

        if (sequenceNameParts.length != 2) {
            throw new RuntimeException("bad chromosome: " + fullSequenceName);
        }

        // just get the first part, e.g. NC_000024.11
        String sequenceName = sequenceNameParts[0];
        return REFSEQ_CHROMOSOMES.get(sequenceName);
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
