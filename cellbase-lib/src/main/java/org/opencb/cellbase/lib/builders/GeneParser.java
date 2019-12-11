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

import org.apache.commons.collections.map.HashedMap;
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
import org.opencb.biodata.tools.sequence.FastaIndexManager;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class GeneParser extends CellBaseParser {

    private static final String ENSEMBL_GTF_DBNAME = "ensembl_gtf";
    private static final java.lang.String ENSEMBL_GTF_DISPLAY = "Ensembl GTF";
    private Map<String, Integer> transcriptDict;
    private Map<String, Exon> exonDict;

    private Path gtfFile;
    private Path proteinFastaFile;
    private Path cDnaFastaFile;
    private Path geneDescriptionFile;
    private Path xrefsFile;
    private Path uniprotIdMappingFile;
    private Path tfbsFile;
    private Path geneExpressionFile;
    private Path geneDrugFile;
    private Path hpoFile;
    private Path disgenetFile;
    private Path gnomadFile;
    private Path genomeSequenceFilePath;
    private boolean flexibleGTFParsing;

    private SpeciesConfiguration speciesConfiguration;

    private Connection sqlConn;
    private PreparedStatement sqlQuery;

    private int CHUNK_SIZE = 2000;
    private String chunkIdSuffix = CHUNK_SIZE / 1000 + "k";
    private Set<String> indexedSequences;

    private int featureCounter = -1; // initialize to -1 so that first +1 sets it to 0
    private String[] featureTypes = {"exon", "cds", "start_codon", "stop_codon"};
    private String currentFeature = "";
    private Map<String, Object> currentTranscriptMap;

    private int geneCounter;
    private ArrayList<String> geneList;
    private String geneName;
    private int transcriptCounter;
    private ArrayList<String> transcriptList;
    private String transcriptName;
    private int exonCounter;
    private String feature;
    private Gtf nextGtfToReturn;

    public GeneParser(Path geneDirectoryPath, Path genomeSequenceFastaFile,
                      SpeciesConfiguration speciesConfiguration,
                      CellBaseSerializer serializer) {
        this(geneDirectoryPath, genomeSequenceFastaFile, speciesConfiguration, false, serializer);
    }

    public GeneParser(Path geneDirectoryPath, Path genomeSequenceFastaFile,
                      SpeciesConfiguration speciesConfiguration, boolean flexibleGTFParsing,
                      CellBaseSerializer serializer) {
        this(null, geneDirectoryPath.resolve("description.txt"), geneDirectoryPath.resolve("xrefs.txt"),
                geneDirectoryPath.resolve("idmapping_selected.tab.gz"), geneDirectoryPath.resolve("MotifFeatures.gff.gz"),
                geneDirectoryPath.getParent().getParent().resolve("common/expression/allgenes_updown_in_organism_part.tab.gz"),
                geneDirectoryPath.resolve("dgidb.tsv"),
                geneDirectoryPath.resolve("ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt"),
                geneDirectoryPath.resolve("all_gene_disease_associations.txt.gz"),
                geneDirectoryPath.resolve("gnomad.v2.1.1.lof_metrics.by_transcript.txt.bgz"),
                genomeSequenceFastaFile, speciesConfiguration, flexibleGTFParsing, serializer);
        getGtfFileFromGeneDirectoryPath(geneDirectoryPath);
        getProteinFastaFileFromGeneDirectoryPath(geneDirectoryPath);
        getCDnaFastaFileFromGeneDirectoryPath(geneDirectoryPath);

        this.genomeSequenceFilePath = genomeSequenceFastaFile;
    }

    public GeneParser(Path gtfFile, Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile, Path tfbsFile,
                      Path geneExpressionFile, Path geneDrugFile, Path hpoFile, Path disgenetFile, Path gnomadFile,
                      Path genomeSequenceFilePath, SpeciesConfiguration speciesConfiguration, boolean flexibleGTFParsing,
                      CellBaseSerializer serializer) {
        super(serializer);
        this.gtfFile = gtfFile;
        this.geneDescriptionFile = geneDescriptionFile;
        this.xrefsFile = xrefsFile;
        this.uniprotIdMappingFile = uniprotIdMappingFile;
        this.tfbsFile = tfbsFile;
        this.geneExpressionFile = geneExpressionFile;
        this.geneDrugFile = geneDrugFile;
        this.hpoFile = hpoFile;
        this.disgenetFile = disgenetFile;
        this.gnomadFile = gnomadFile;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.speciesConfiguration = speciesConfiguration;
        this.flexibleGTFParsing = flexibleGTFParsing;

        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);
    }

    public void parse() throws Exception {
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

        // Gene annotation data
        Map<String, List<Expression>> geneExpressionMap = GeneParserUtils
                .getGeneExpressionMap(speciesConfiguration.getScientificName(), geneExpressionFile);
        Map<String, List<GeneDrugInteraction>> geneDrugMap = GeneParserUtils.getGeneDrugMap(geneDrugFile);
        Map<String, List<GeneTraitAssociation>> diseaseAssociationMap = GeneParserUtils.getGeneDiseaseAssociationMap(hpoFile, disgenetFile);

        // Transcript and Gene constraint scores annotation
        Map<String, List<Constraint>> constraints = GeneParserUtils.getConstraints(gnomadFile);

        // Preparing the fasta file for fast accessing
        FastaIndexManager fastaIndexManager = getFastaIndexManager();

        // Empty transcript and exon dictionaries
        transcriptDict.clear();
        exonDict.clear();
        logger.info("Parsing gtf...");
        GtfReader gtfReader = new GtfReader(gtfFile);

        // Gene->Transcript->Feature->GTF line
        Map<String, Map<String, Map<String, Object>>> gtfMap = null;
        if (flexibleGTFParsing) {
            gtfMap = loadGTFMap(gtfReader);
            initializePointers(gtfMap);
        }

        Gtf gtf;
        while ((gtf = getGTFEntry(gtfReader, gtfMap)) != null) {

            if (gtf.getFeature().equals("gene") || gtf.getFeature().equals("transcript")
                    || gtf.getFeature().equals("UTR") || gtf.getFeature().equals("Selenocysteine")) {
                continue;
            }

            String geneId = gtf.getAttributes().get("gene_id");
            String transcriptId = gtf.getAttributes().get("transcript_id");
            if (newGene(gene, geneId)) {
                gene = getGene(gene, geneDescriptionMap, geneExpressionMap, geneDrugMap, diseaseAssociationMap, constraints, gtf, geneId);
            }

            // Check if Transcript exist in the Gene Set of transcripts
            if (!transcriptDict.containsKey(transcriptId)) {
                transcript = getTranscript(gene, xrefMap, proteinSequencesMap, cDnaSequencesMap, tfbsMap, constraints, gtf, transcriptId);
            } else {
                transcript = gene.getTranscripts().get(transcriptDict.get(transcriptId));
            }

            // At this point gene and transcript objects are set up
            // Update gene and transcript genomic coordinates, start must be the
            // lower, and end the higher
            updateTranscriptAndGeneCoords(transcript, gene, gtf);

            if (gtf.getFeature().equalsIgnoreCase("exon")) {
                // Obtaining the exon sequence
                String exonSequence = null;
                try {
                    exonSequence = fastaIndexManager.query(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());
                } catch (RocksDBException e) {
                    e.printStackTrace();
                }

                exon = new Exon(gtf.getAttributes().get("exon_id"), gtf.getSequenceName().replaceFirst("chr", ""),
                        gtf.getStart(), gtf.getEnd(), gtf.getStrand(), 0, 0,
                        0, 0, 0, 0, -1, Integer.parseInt(gtf
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
                    cds = getCds(transcript, exon, cdna, cds, gtf);
                    // no strand dependent
                    transcript.setProteinID(gtf.getAttributes().get("protein_id"));
                }
                if (gtf.getFeature().equalsIgnoreCase("stop_codon")) {
                    //                      setCdnaCodingEnd = false; // stop_codon found, cdnaCodingEnd will be set here,
                    //                      no need to set it at the beginning of next feature
                    cds = processStopCodons(transcript, exon, cdna, cds, gtf);
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

    private int getCds(Transcript transcript, Exon exon, int cdna, int cds, Gtf gtf) {
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
        return cds;
    }

    private Transcript getTranscript(Gene gene, Map<String, ArrayList<Xref>> xrefMap, Map<String, Fasta> proteinSequencesMap,
                                     Map<String, Fasta> cDnaSequencesMap, Map<String, SortedSet<Gff2>> tfbsMap,
                                     Map<String, List<Constraint>> constraints, Gtf gtf, String transcriptId) {
        Transcript transcript; // TODO: transcript tfbs should be a list and not an array list
        String transcriptChrosome = gtf.getSequenceName().replaceFirst("chr", "");
        ArrayList<TranscriptTfbs> transcriptTfbses = getTranscriptTfbses(gtf, transcriptChrosome, tfbsMap);
        Map<String, String> gtfAttributes = gtf.getAttributes();

        TranscriptAnnotation transcriptAnnotation = new TranscriptAnnotation(constraints.get(transcriptId));

        transcript = new Transcript(transcriptId, gtfAttributes.get("transcript_name"),
                (gtfAttributes.get("transcript_biotype") != null) ? gtfAttributes.get("transcript_biotype") : gtf.getSource(),
                "KNOWN", transcriptChrosome, gtf.getStart(), gtf.getEnd(),
                gtf.getStrand(), 0, 0, 0, 0,
                0, "", "", xrefMap.get(transcriptId), new ArrayList<Exon>(),
                transcriptTfbses, transcriptAnnotation);

        // Adding Ids appearing in the GTF to the xrefs is required, since for some unknown reason the ENSEMBL
        // Perl API often doesn't return all genes resulting in an incomplete xrefs.txt file. We must ensure
        // that the xrefs array contains all ids present in the GTF file
        addGtfXrefs(transcript, gene);

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
        return transcript;
    }

    private Gene getGene(Gene gene, Map<String, String> geneDescriptionMap, Map<String, List<Expression>> geneExpressionMap,
                         Map<String, List<GeneDrugInteraction>> geneDrugMap, Map<String, List<GeneTraitAssociation>> diseaseAssociationMap,
                         Map<String, List<Constraint>> constraints, Gtf gtf, String geneId) {
        // If new geneId is different from the current then we must serialize before data new gene
        if (gene != null) {
            serializer.serialize(gene);
        }

        GeneAnnotation geneAnnotation = new GeneAnnotation(geneExpressionMap.get(geneId),
                diseaseAssociationMap.get(gtf.getAttributes().get("gene_name")),
                geneDrugMap.get(gtf.getAttributes().get("gene_name")), constraints.get(geneId));

        gene = new Gene(geneId, gtf.getAttributes().get("gene_name"), gtf.getAttributes().get("gene_biotype"),
                "KNOWN", gtf.getSequenceName().replaceFirst("chr", ""), gtf.getStart(), gtf.getEnd(),
                gtf.getStrand(), "Ensembl", geneDescriptionMap.get(geneId), new ArrayList<>(),
                null, geneAnnotation);
        // Do not change order!! size()-1 is the index of the gene ID
        return gene;
    }

    private int processStopCodons(Transcript transcript, Exon exon, int cdna, int cds, Gtf gtf) {
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
        return cds;
    }

    private FastaIndexManager getFastaIndexManager() throws Exception {
        FastaIndexManager fastaIndexManager;
        fastaIndexManager = new FastaIndexManager(genomeSequenceFilePath, true);
        if (!fastaIndexManager.isConnected()) {
            fastaIndexManager.index();
        }
        return fastaIndexManager;
    }

    private void addGtfXrefs(Transcript transcript, Gene gene) {
        List<Xref> xrefList = transcript.getXrefs();
        if (xrefList == null) {
            xrefList = new ArrayList<>();
            transcript.setXrefs(xrefList);
        }
        xrefList.add(new Xref(gene.getId(), ENSEMBL_GTF_DBNAME, ENSEMBL_GTF_DISPLAY));
        xrefList.add(new Xref(gene.getName(), ENSEMBL_GTF_DBNAME, ENSEMBL_GTF_DISPLAY));
        xrefList.add(new Xref(transcript.getId(), ENSEMBL_GTF_DBNAME, ENSEMBL_GTF_DISPLAY));
        xrefList.add(new Xref(transcript.getName(), ENSEMBL_GTF_DBNAME, ENSEMBL_GTF_DISPLAY));
    }

    private void initializePointers(Map<String, Map<String, Map<String, Object>>> gtfMap) {
        geneCounter = 0;
        geneList = new ArrayList<>(gtfMap.keySet());
        geneName = geneList.get(geneCounter);
        transcriptCounter = 0;
        transcriptList = new ArrayList<>(gtfMap.get(geneName).keySet());
        transcriptName = transcriptList.get(transcriptCounter);
        exonCounter = 0;
        feature = "exon";
        nextGtfToReturn = (Gtf) ((List) gtfMap.get(geneName).get(transcriptName).get("exon")).get(exonCounter);
    }

    private Gtf getGTFEntry(GtfReader gtfReader, Map<String, Map<String, Map<String, Object>>> gtfMap) throws FileFormatException {
        // Flexible parsing is deactivated, return next line
        if (gtfMap == null) {
            return gtfReader.read();
        // Flexible parsing activated, carefully select next line to return
        } else {
            // No more genes/features to return
            if (nextGtfToReturn == null) {
                return null;
            }
            Gtf gtfToReturn = nextGtfToReturn;
            if (feature.equals("exon")) {
//                gtfToReturn = (Gtf) ((List) gtfMap.get(geneName).get(transcriptName).get("exon")).get(exonCounter);
                if (gtfMap.get(geneName).get(transcriptName).containsKey("cds")) {
                    nextGtfToReturn = getExonCDSLine(((Gtf) ((List) gtfMap.get(geneName)
                                    .get(transcriptName).get("exon")).get(exonCounter)).getStart(),
                            ((Gtf) ((List) gtfMap.get(geneName).get(transcriptName).get("exon")).get(exonCounter)).getEnd(),
                            (List) gtfMap.get(geneName).get(transcriptName).get("cds"));
                    if (nextGtfToReturn != null) {
                        feature = "cds";
                        return gtfToReturn;
                    }
                }
                // if no cds was found for this exon, get next exon
                getFeatureFollowsExon(gtfMap);
                return gtfToReturn;
            }
            if (feature.equals("cds") || feature.equals("stop_codon")) {
                getFeatureFollowsExon(gtfMap);
                return gtfToReturn;
            }
            if (feature.equals("start_codon")) {
                feature = "stop_codon";
                nextGtfToReturn = (Gtf) gtfMap.get(geneName).get(transcriptName).get("stop_codon");
                return gtfToReturn;
            }
            // The only accepted features that should appear in the gtfMap are exon, cds, start_codon and stop_codon
            throw new FileFormatException("Execution cannot reach this point");
        }
    }

    private Gtf getExonCDSLine(Integer exonStart, Integer exonEnd, List cdsList) {
        for (Object cdsObject : cdsList) {
            Integer cdsStart = ((Gtf) cdsObject).getStart();
            Integer cdsEnd = ((Gtf) cdsObject).getEnd();
            if (cdsStart <= exonEnd && cdsEnd >= exonStart) {
                return (Gtf) cdsObject;
            }
        }
        return null;
    }

    private void getFeatureFollowsExon(Map<String, Map<String, Map<String, Object>>> gtfMap) {
        exonCounter++;
        if (exonCounter == ((List) gtfMap.get(geneName).get(transcriptName).get("exon")).size()
                || feature.equals("stop_codon")) {
            // If last returned feature was a stop_codon or no start_codon is provided for this transcript,
            // next transcript must be selected
            if (!feature.equals("stop_codon") && gtfMap.get(geneName).get(transcriptName).containsKey("start_codon")) {
                feature = "start_codon";
                nextGtfToReturn = (Gtf) gtfMap.get(geneName).get(transcriptName).get("start_codon");
            } else {
                transcriptCounter++;
                // No more transcripts in this gene, check if there are more genes
                if (transcriptCounter == gtfMap.get(geneName).size()) {
                    geneCounter++;
                    // No more genes available, end parsing
                    if (geneCounter == gtfMap.size()) {
                        nextGtfToReturn = null;
                        feature = null;
                    // Still more genes to parse, select next one
                    } else {
                        geneName = geneList.get(geneCounter);
                        transcriptCounter = 0;
                        transcriptList = new ArrayList<>(gtfMap.get(geneName).keySet());
                    }
                }
                // Check if a new gene was selected - null would indicate there're no more genes
                if (nextGtfToReturn != null) {
                    transcriptName = transcriptList.get(transcriptCounter);
                    exonCounter = 0;
                    feature = "exon";
                    nextGtfToReturn = (Gtf) ((List) gtfMap.get(geneName).get(transcriptName).get("exon")).get(exonCounter);
                }
            }
        } else {
            feature = "exon";
            nextGtfToReturn = (Gtf) ((List) gtfMap.get(geneName).get(transcriptName).get("exon")).get(exonCounter);
        }
    }

    private Map<String, Map<String, Map<String, Object>>> loadGTFMap(GtfReader gtfReader) throws FileFormatException {
        Map<String, Map<String, Map<String, Object>>> gtfMap = new HashedMap();
        Gtf gtf;
        while ((gtf = gtfReader.read()) != null) {
            if (gtf.getFeature().equals("gene") || gtf.getFeature().equals("transcript")
                    || gtf.getFeature().equals("UTR") || gtf.getFeature().equals("Selenocysteine")) {
                continue;
            }

            // Get GTF lines associated with this gene - create a new Map of GTF entries if it's a new gene
            String geneId = gtf.getAttributes().get("gene_id");
            // Transcript -> feature -> GTF line
            Map<String, Map<String, Object>> gtfMapGeneEntry;
            if (gtfMap.containsKey(geneId)) {
                gtfMapGeneEntry =  gtfMap.get(geneId);
            } else {
                gtfMapGeneEntry = new HashedMap();
                gtfMap.put(geneId, gtfMapGeneEntry);
            }

            // Get GTF lines associated with this transcript - create a new Map of GTF entries if it's a new gene
            String transcriptId = gtf.getAttributes().get("transcript_id");
            Map<String, Object> gtfMapTranscriptEntry;
            if (gtfMapGeneEntry.containsKey(transcriptId)) {
                gtfMapTranscriptEntry =  gtfMapGeneEntry.get(transcriptId);
            } else {
                gtfMapTranscriptEntry = new HashedMap();
                gtfMapGeneEntry.put(transcriptId, gtfMapTranscriptEntry);
            }

            addGTFLineToGTFMap(gtfMapTranscriptEntry, gtf);

        }

        // Exon number is mandatory for the parser to be able to properly generate the gene data model
        if (!exonNumberPresent(gtfMap)) {
            setExonNumber(gtfMap);
        }

        return gtfMap;
    }

    private boolean exonNumberPresent(Map<String, Map<String, Map<String, Object>>> gtfMap) {
        Map<String, Map<String, Object>> geneGtfMap = gtfMap.get(gtfMap.keySet().iterator().next());
        return ((Gtf) ((List) geneGtfMap.get(geneGtfMap.keySet().iterator().next()).get("exon")).get(0))
                .getAttributes().containsKey("exon_number");
    }

    private void setExonNumber(Map<String, Map<String, Map<String, Object>>> gtfMap) {
        for (String gene : gtfMap.keySet()) {
            for (String transcript : gtfMap.get(gene).keySet()) {
                List<Gtf> exonList = (List<Gtf>) gtfMap.get(gene).get(transcript).get("exon");
                Collections.sort(exonList, (e1, e2) -> Integer.valueOf(e1.getStart()).compareTo(e2.getStart()));
                if (exonList.get(0).getStrand().equals("+")) {
                    int exonNumber = 1;
                    for (Gtf gtf : exonList) {
                        gtf.getAttributes().put("exon_number", String.valueOf(exonNumber));
                        exonNumber++;
                    }
                } else {
                    int exonNumber = exonList.size();
                    for (Gtf gtf : exonList) {
                        gtf.getAttributes().put("exon_number", String.valueOf(exonNumber));
                        exonNumber--;
                    }
                }
            }
        }
    }

    private void addGTFLineToGTFMap(Map<String, Object> gtfMapTranscriptEntry, Gtf gtf) {

        String featureType = gtf.getFeature().toLowerCase();

        // Add exon/cds GTF line to the corresponding gene entry in the map
        if (featureType.equals("exon") || featureType.equals("cds")) {
            List gtfList = null;
            // Check if there were exons already stored
            if (gtfMapTranscriptEntry.containsKey(featureType)) {
                gtfList =  (List) gtfMapTranscriptEntry.get(featureType);
            } else {
                gtfList = new ArrayList<>();
                gtfMapTranscriptEntry.put(featureType, gtfList);
            }
            gtfList.add(gtf);
        // Only one start/stop codon can be stored per transcript - no need to check if the "start_codon"/"stop_codon"
        // keys are already there
        } else if (featureType.equals("start_codon") || featureType.equals("stop_codon")) {
            gtfMapTranscriptEntry.put(featureType, gtf);
        }

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
        if (cDnaFastaFile != null && Files.exists(cDnaFastaFile) && !Files.isDirectory(cDnaFastaFile)
                && Files.size(cDnaFastaFile) > 0) {
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
        if (proteinFastaFile != null && Files.exists(proteinFastaFile) && !Files.isDirectory(proteinFastaFile)
                && Files.size(proteinFastaFile) > 0) {
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
        if (geneDescriptionFile != null && Files.exists(geneDescriptionFile) && Files.size(geneDescriptionFile) > 0) {
            List<String> lines = Files.readAllLines(geneDescriptionFile, Charset.forName("ISO-8859-1"));
//            List<String> lines = Files.readAllLines(geneDescriptionFile, Charset.defaultCharset());
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
