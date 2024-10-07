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

import htsjdk.tribble.readers.TabixReader;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gtf.Gtf;
import org.opencb.biodata.formats.feature.gtf.io.GtfReader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.tools.sequence.FastaIndex;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class EnsemblGeneBuilder extends AbstractBuilder {

    private Path downloadPath;
    private SpeciesConfiguration speciesConfiguration;
    private boolean flexibleGTFParsing;
    private CellBaseConfiguration configuration;

    private final Map<String, Integer> transcriptDict;
    private final Map<String, Exon> exonDict;

    private Path gtfFile = null;
    private Path proteinFastaFile = null;
    private Path cDnaFastaFile = null;
    private Path geneDescriptionFile = null;
    private Path xrefsFile = null;
    private Path hgncFile = null;
    private Path maneFile = null;
    private Path lrgFile = null;
    private Path uniprotIdMappingFile = null;
    private Path tfbsFile = null;
    private Path tabixFile = null;
    private Path geneExpressionFile = null;
    private Path geneDrugFile = null;
    private Path hpoFile = null;
    private Path genomeSequenceFilePath = null;
    private Path gnomadFile = null;
    private Path geneOntologyAnnotationFile = null;
    private Path miRBaseFile = null;
    private Path miRTarBaseFile = null;
    private Path cancerGeneCensusFile = null;
    private Path cancerHostpotFile = null;
    private Path ensemblCanonicalFile = null;

    // source for genes is either ensembl or refseq
    private final String SOURCE = ParamConstants.QueryParams.ENSEMBL.key();

    private int geneCounter;
    private ArrayList<String> geneList;
    private String geneName;
    private int transcriptCounter;
    private ArrayList<String> transcriptList;
    private String transcriptName;
    private int exonCounter;
    private String feature;
    private Gtf nextGtfToReturn;

    private boolean isHSapiens = false;

    public static final String ENSEMBL_GENE_BASENAME = "ensemblGene";
    public static final String ENSEMBL_GENE_OUTPUT_FILENAME = ENSEMBL_GENE_BASENAME + ".json.gz";

    public EnsemblGeneBuilder(Path downloadPath, SpeciesConfiguration speciesConfiguration, boolean flexibleGTFParsing,
                              CellBaseConfiguration configuration, CellBaseSerializer serializer) {
        super(serializer);

        this.downloadPath = downloadPath;
        this.speciesConfiguration = speciesConfiguration;
        this.flexibleGTFParsing = flexibleGTFParsing;
        this.configuration = configuration;

        transcriptDict = new HashMap<>(250000);
        exonDict = new HashMap<>(8000000);

        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS)) {
            isHSapiens = true;
        }
    }

    public void check() throws Exception {
        if (checked) {
            return;
        }

        String ensemblGeneLabel = getDataName(ENSEMBL_DATA) + " " + getDataName(GENE_DATA);
        logger.info(CHECKING_BEFORE_BUILDING_LOG_MESSAGE, ensemblGeneLabel);

        // Sanity check
        checkDirectory(downloadPath, ensemblGeneLabel);
        if (!Files.exists(serializer.getOutdir())) {
            try {
                Files.createDirectories(serializer.getOutdir());
            } catch (IOException e) {
                throw new CellBaseException("Error creating folder " + serializer.getOutdir(), e);
            }
        }

        // Check Ensembl files
        DownloadProperties.URLProperties props = configuration.getDownload().getEnsembl().getUrl();
        gtfFile = checkFile(props, ENSEMBL_GTF_FILE_ID, downloadPath, "Ensembl GTF").toPath();
        proteinFastaFile = checkFile(props, ENSEMBL_PEP_FA_FILE_ID, downloadPath, "Ensembl Protein Fasta").toPath();
        cDnaFastaFile = checkFile(props, ENSEMBL_CDNA_FA_FILE_ID, downloadPath, "Ensembl CDNA Fasta").toPath();

        // Commons
        geneDescriptionFile = checkFile(props, ENSEMBL_DESCRIPTION_FILE_ID, downloadPath.getParent(), "Ensembl Description").toPath();
        xrefsFile = checkFile(props, ENSEMBL_XREFS_FILE_ID, downloadPath.getParent(), "Ensembl Xrefs").toPath();
        ensemblCanonicalFile = checkFile(props, ENSEMBL_CANONICAL_FILE_ID, downloadPath.getParent(), "Ensembl Canonical").toPath();

        // Check common files
        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());
        if (isHSapiens || isDataSupported(configuration.getDownload().getManeSelect(), prefixId)) {
            maneFile = checkFiles(MANE_SELECT_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(MANE_SELECT_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getLrg(), prefixId)) {
            lrgFile = checkFiles(LRG_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(LRG_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getHgnc(), prefixId)) {
            hgncFile = checkFiles(HGNC_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(HGNC_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getCancerHotspot(), prefixId)) {
            cancerHostpotFile = checkFiles(CANCER_HOTSPOT_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(CANCER_HOTSPOT_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getDgidb(), prefixId)) {
            geneDrugFile = checkFiles(DGIDB_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(DGIDB_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getGeneUniprotXref(), prefixId)) {
            uniprotIdMappingFile = checkFiles(UNIPROT_XREF_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(UNIPROT_XREF_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getGeneExpressionAtlas(), prefixId)) {
            geneExpressionFile = checkFiles(GENE_EXPRESSION_ATLAS_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(UNIPROT_XREF_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getHpo(), prefixId)) {
            hpoFile = checkFiles(HPO_DISEASE_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(HPO_DISEASE_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getGnomadConstraints(), prefixId)) {
            gnomadFile = checkFiles(GNOMAD_CONSTRAINTS_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(GNOMAD_CONSTRAINTS_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getGoAnnotation(), prefixId)) {
            geneOntologyAnnotationFile = checkFiles(GO_ANNOTATION_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(GO_ANNOTATION_DATA), speciesConfiguration.getScientificName());
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getCancerHotspot(), prefixId)) {
            cancerGeneCensusFile = checkFiles(CANCER_GENE_CENSUS_DATA, downloadPath.getParent(), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(CANCER_GENE_CENSUS_DATA), speciesConfiguration.getScientificName());
        }

        // Check regulation files
        // Motif features
        List<File> files = checkFiles(ensemblGeneLabel, MOTIF_FEATURES_DATA, downloadPath.getParent().getParent().resolve(REGULATION_DATA)
                        .resolve(MOTIF_FEATURES_DATA), 2);
        if (files.get(0).getName().endsWith("tbi")) {
            tabixFile = files.get(0).toPath();
            tfbsFile = files.get(1).toPath();
        } else {
            tabixFile = files.get(1).toPath();
            tfbsFile = files.get(0).toPath();
        }

        // mirbase
        if (isHSapiens || isDataSupported(configuration.getDownload().getMirbase(), prefixId)) {
            miRBaseFile = checkFiles(MIRBASE_DATA, downloadPath.getParent().getParent().resolve(REGULATION_DATA)
                    .resolve(MIRBASE_DATA), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(MIRTARBASE_DATA), speciesConfiguration.getScientificName());
        }

        // mirtarbase
        if (isHSapiens || isDataSupported(configuration.getDownload().getMiRTarBase(), prefixId)) {
            miRTarBaseFile = checkFiles(MIRTARBASE_DATA, downloadPath.getParent().getParent().resolve(REGULATION_DATA)
                    .resolve(MIRTARBASE_DATA), 1).get(0).toPath();
        } else {
            logger.info(SKIPPING_INDEX_DATA_LOG_MESSAGE, getDataName(MIRTARBASE_DATA), speciesConfiguration.getScientificName());
        }

        // Check genome FASTA file
        Path genomeDownloadPath = downloadPath.getParent().getParent().resolve(GENOME_DATA);
        String genomeGzFilename = Paths.get(((DataSource) dataSourceReader.readValue(genomeDownloadPath
                .resolve(getDataVersionFilename(GENOME_DATA)).toFile())).getUrls().get(0)).getFileName().toString();
        genomeSequenceFilePath = getFastaPath(genomeDownloadPath.resolve(genomeGzFilename));

        logger.info(CHECKING_DONE_BEFORE_BUILDING_LOG_MESSAGE, ensemblGeneLabel);
        checked = true;
    }

    public void parse() throws Exception {
        check();

        Gene gene = null;
        Transcript transcript;
        Exon exon = null;
        int cdna = 1;
        int cds = 1;

        EnsemblGeneBuilderIndexer indexer = new EnsemblGeneBuilderIndexer(serializer.getOutdir());
        try {
            // process files and put values in rocksdb
            indexer.index(geneDescriptionFile, xrefsFile, hgncFile, maneFile, lrgFile, uniprotIdMappingFile, proteinFastaFile,
                    cDnaFastaFile, speciesConfiguration.getScientificName(), geneExpressionFile, geneDrugFile, hpoFile, gnomadFile,
                    geneOntologyAnnotationFile, miRBaseFile, miRTarBaseFile, cancerGeneCensusFile, cancerHostpotFile, ensemblCanonicalFile);

            TabixReader tabixReader = null;
            if (!Files.exists(tfbsFile) || !Files.exists(tabixFile)) {
                logger.error("Tfbs or tabix file not found. Download them and try again.");
            } else {
                tabixReader = new TabixReader(tfbsFile.toAbsolutePath().toString(), tabixFile.toAbsolutePath().toString());
            }

            // Preparing the fasta file for fast accessing
            FastaIndex fastaIndex = new FastaIndex(genomeSequenceFilePath);

            // Empty transcript and exon dictionaries
            transcriptDict.clear();
            exonDict.clear();

            logger.info(PARSING_LOG_MESSAGE, gtfFile);
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
                String geneName = gtf.getAttributes().get("gene_name");
                if (newGene(gene, geneId)) {
                    // If new geneId is different from the current then we must serialize before data new gene
                    if (gene != null) {
                        serializer.serialize(gene);
                    }

                    GeneAnnotation geneAnnotation = new GeneAnnotation(indexer.getExpression(geneId), indexer.getDiseases(geneName),
                            indexer.getDrugs(geneName), indexer.getConstraints(geneId), indexer.getMirnaTargets(geneName),
                            indexer.getCancerGeneCensus(geneName), indexer.getCancerHotspot(geneName));

                    gene = new Gene(geneId, geneName, gtf.getSequenceName().replaceFirst("chr", ""),
                            gtf.getStart(), gtf.getEnd(), gtf.getStrand(), gtf.getAttributes().get("gene_version"),
                            gtf.getAttributes().get("gene_biotype"), "KNOWN", SOURCE, indexer.getDescription(geneId),
                            new ArrayList<>(), indexer.getMirnaGene(transcriptId), geneAnnotation);
                }

                // Check if Transcript exist in the Gene Set of transcripts
                if (!transcriptDict.containsKey(transcriptId)) {
                    transcript = getTranscript(gene, indexer, tabixReader, gtf, transcriptId);
                } else {
                    transcript = gene.getTranscripts().get(transcriptDict.get(transcriptId));
                }

                // At this point gene and transcript objects are set up
                // Update gene and transcript genomic coordinates, start must be the
                // lower, and end the higher
                updateTranscriptAndGeneCoords(transcript, gene, gtf);

                String transcriptIdWithoutVersion = transcript.getId().split("\\.")[0];
                if (gtf.getFeature().equalsIgnoreCase("exon")) {
                    // Obtaining the exon sequence
                    String exonId = gtf.getAttributes().get("exon_id") + "." + gtf.getAttributes().get("exon_version");
                    String exonSequence = fastaIndex.query(gtf.getSequenceName(), gtf.getStart(), gtf.getEnd());

                    exon = new Exon(exonId, gtf.getSequenceName().replaceFirst("chr", ""),
                            gtf.getStart(), gtf.getEnd(), gtf.getStrand(), 0, 0, 0, 0, 0, 0, -1, Integer.parseInt(gtf
                            .getAttributes().get("exon_number")), exonSequence);
                    transcript.getExons().add(exon);

                    exonDict.put(transcriptIdWithoutVersion + "_" + exon.getExonNumber(), exon);
                    if (gtf.getAttributes().get("exon_number").equals("1")) {
                        cdna = 1;
                        cds = 1;
                    } else {
                        // with every exon we update cDNA length with the previous exon length
                        cdna += exonDict.get(transcriptIdWithoutVersion + "_" + (exon.getExonNumber() - 1)).getEnd()
                                - exonDict.get(transcriptIdWithoutVersion + "_" + (exon.getExonNumber() - 1)).getStart() + 1;
                    }
                } else {
                    exon = exonDict.get(transcriptIdWithoutVersion + "_" + exon.getExonNumber());
                    if (gtf.getFeature().equalsIgnoreCase("CDS")) {
                        // Protein ID is only present in CDS lines
                        String proteinId = gtf.getAttributes().get("protein_id") != null
                                ? gtf.getAttributes().get("protein_id") + "." + gtf.getAttributes().get("protein_version")
                                : "";
                        transcript.setProteinId(proteinId);
                        transcript.setProteinSequence(indexer.getProteinFasta(proteinId));

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

                            exon.setPhase(Integer.parseInt(gtf.getFrame()));

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
                            exon.setPhase(Integer.parseInt(gtf.getFrame()));

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

                    }
//                if (gtf.getFeature().equalsIgnoreCase("start_codon")) {
//                    // nothing to do
//                    System.out.println("Empty block, this should be redesigned");
//                }
                    if (gtf.getFeature().equalsIgnoreCase("stop_codon")) {
                        //                      setCdnaCodingEnd = false; // stop_codon found, cdnaCodingEnd will be set here,
                        //                      no need to set it at the beginning of next feature
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
                }
            }

            // last gene must be serialized
            serializer.serialize(gene);

            // Close
            gtfReader.close();
            serializer.close();
            fastaIndex.close();
            indexer.close();

            logger.info(PARSING_DONE_LOG_MESSAGE, gtfFile);
        } catch (Exception e) {
            indexer.close();
            throw e;
        }
    }

    private Transcript getTranscript(Gene gene, EnsemblGeneBuilderIndexer indexer, TabixReader tabixReader, Gtf gtf, String transcriptId)
            throws IOException, RocksDBException {
        Map<String, String> gtfAttributes = gtf.getAttributes();

        // To match Ensembl, we set the ID as transcript+version. This also matches the Ensembl website.
        String transcriptIdWithVersion = transcriptId + "." + gtfAttributes.get("transcript_version");
        String biotype = gtfAttributes.get("transcript_biotype") != null ? gtfAttributes.get("transcript_biotype") : "";
        String transcriptChromosome = gtf.getSequenceName().replaceFirst("chr", "");
        List<TranscriptTfbs> transcriptTfbses = getTranscriptTfbses(gtf, transcriptChromosome, tabixReader);

        List<FeatureOntologyTermAnnotation> ontologyAnnotations = getOntologyAnnotations(indexer.getXrefs(transcriptId), indexer);
        TranscriptAnnotation transcriptAnnotation = new TranscriptAnnotation(ontologyAnnotations, indexer.getConstraints(transcriptId));

        Transcript transcript = new Transcript(transcriptIdWithVersion, gtfAttributes.get("transcript_name"), transcriptChromosome,
                gtf.getStart(), gtf.getEnd(), gtf.getStrand(), biotype, "KNOWN",
                0, 0, 0, 0, 0,
                indexer.getCdnaFasta(transcriptIdWithVersion), "", "", "",
                gtfAttributes.get("transcript_version"), SOURCE, new ArrayList<>(), indexer.getXrefs(transcriptId), transcriptTfbses,
                new HashSet<>(), transcriptAnnotation);

        // Adding Ids appearing in the GTF to the xrefs is required, since for some unknown reason the ENSEMBL
        // Perl API often doesn't return all genes resulting in an incomplete xrefs.txt file. We must ensure
        // that the xrefs array contains all ids present in the GTF file
        addGtfXrefs(transcript, gene, gtfAttributes);

        // Add HGNC ID mappings, with this we can know which Ensembl and Refseq transcripts match to HGNC ID
        String hgncId = indexer.getHgncId(gene.getName());
        if (StringUtils.isNotEmpty(hgncId)) {
            transcript.getXrefs().add(new Xref(hgncId, "hgnc_id", "HGNC ID"));
        }

        // Add MANE Select mappings, with this we can know which Ensembl and Refseq transcripts match according to MANE
        for (String suffix: Arrays.asList("refseq", "refseq_protein")) {
            String maneRefSeq = indexer.getMane(transcriptIdWithVersion, suffix);
            if (StringUtils.isNotEmpty(maneRefSeq)) {
                transcript.getXrefs().add(new Xref(maneRefSeq, "mane_select_" + suffix,
                        "MANE Select RefSeq" + (suffix.contains("_") ? " Protein" : "")));
            }
        }

        // Add LRG mappings, with this we can know which Ensembl and Refseq transcripts match according to LRG
        String lrgRefSeq = indexer.getLrg(transcriptIdWithVersion, "refseq");
        if (StringUtils.isNotEmpty(lrgRefSeq)) {
            transcript.getXrefs().add(new Xref(lrgRefSeq, "lrg_refseq", "LRG RefSeq"));
        }

        // Add Flags
        // 1. GTF tags
        String tags = gtf.getAttributes().get("tag");
        if (StringUtils.isNotEmpty(tags)) {
            transcript.getFlags().addAll(Arrays.asList(tags.split(",")));
        }

        // 2. TSL
        String supportLevel = gtfAttributes.get("transcript_support_level");
        if (StringUtils.isNotEmpty(supportLevel)) {
            // split on space so "5 (assigned to previous version 3)" and "5" both become "TSL:5"
            String truncatedSupportLevel = supportLevel.split(" ")[0];
            transcript.getFlags().add("TSL:" + truncatedSupportLevel);
        }

        // 3. MANE Flag
        String maneFlag = indexer.getMane(transcriptIdWithVersion, "flag");
        if (StringUtils.isNotEmpty(maneFlag)) {
            transcript.getFlags().add(maneFlag);
        }

        // 4. LRG Flag
        String lrg = indexer.getLrg(transcriptIdWithVersion, "ensembl");
        if (StringUtils.isNotEmpty(lrg)) {
            transcript.getFlags().add("LRG");
        } else {
            for (Xref xref : transcript.getXrefs()) {
                if (xref.getId().startsWith("LRG_") && xref.getId().contains("t")) {
                    transcript.getFlags().add("LRG");
                }
            }
        }

        // 5. Ensembl Canonical
        String canonicalFlag = indexer.getCanonical(transcriptIdWithVersion);
        if (StringUtils.isNotEmpty(canonicalFlag)) {
            transcript.getFlags().add(canonicalFlag);
        }

        // 6. TSO500 and EGLH HaemOnc
//        String maneRefSeq = indexer.getMane(transcriptIdWithVersion, "refseq");
//        if (StringUtils.isNotEmpty(maneRefSeq)) {
//            String tso500Flag = indexer.getTSO500(maneRefSeq.split("\\.")[0]);
//            if (StringUtils.isNotEmpty(tso500Flag)) {
//                transcript.getFlags().add(tso500Flag);
//            }
//
//            String eglhHaemOncFlag = indexer.getEGLHHaemOnc(maneRefSeq.split("\\.")[0]);
//            if (StringUtils.isNotEmpty(eglhHaemOncFlag)) {
//                transcript.getFlags().add(eglhHaemOncFlag);
//            }
//        }

        gene.getTranscripts().add(transcript);

        // Do not change order!! size()-1 is the index of the transcript ID
        transcriptDict.put(transcriptId, gene.getTranscripts().size() - 1);
        return transcript;
    }

    private List<FeatureOntologyTermAnnotation> getOntologyAnnotations(List<Xref> xrefs,  EnsemblGeneBuilderIndexer indexer)
            throws IOException, RocksDBException {
        if (xrefs == null || indexer == null) {
            return null;
        }
        List<FeatureOntologyTermAnnotation> annotations = new ArrayList<>();
        for (Xref xref : xrefs) {
            if (xref.getDbName().equals("uniprotkb_acc")) {
                String key = xref.getId();
                if (key != null && indexer.getOntologyAnnotations(key) != null) {
                    annotations.addAll(indexer.getOntologyAnnotations(key));
                }
            }
        }
        return annotations;
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

    private void addGtfXrefs(Transcript transcript, Gene gene, Map<String, String> gtfAttributes) {
        if (transcript.getXrefs() == null) {
            transcript.setXrefs(new ArrayList<>());
        }

        transcript.getXrefs().add(new Xref(gene.getId(), "ensembl_gene", "Ensembl Gene"));
        transcript.getXrefs().add(new Xref(transcript.getId(), "ensembl_transcript", "Ensembl Transcript"));

        // Some non-coding genes do not have Gene names
        if (StringUtils.isNotEmpty(gene.getName())) {
            transcript.getXrefs().add(new Xref(gene.getName(), "hgnc_symbol", "HGNC Symbol"));
            transcript.getXrefs().add(new Xref(transcript.getName(), "ensembl_transcript_name", "Ensembl Transcript Name"));
        }

        if (gtfAttributes.get("ccds_id") != null) {
            transcript.getXrefs().add(new Xref(gtfAttributes.get("ccds_id"), "ccds_id", "CCDS"));
        }
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
            int cdsStart = ((Gtf) cdsObject).getStart();
            int cdsEnd = ((Gtf) cdsObject).getEnd();
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
        Map<String, Map<String, Map<String, Object>>> gtfMap = new HashMap<>();
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
                gtfMapGeneEntry = new HashMap();
                gtfMap.put(geneId, gtfMapGeneEntry);
            }

            // Get GTF lines associated with this transcript - create a new Map of GTF entries if it's a new gene
            String transcriptId = gtf.getAttributes().get("transcript_id");
            Map<String, Object> gtfMapTranscriptEntry;
            if (gtfMapGeneEntry.containsKey(transcriptId)) {
                gtfMapTranscriptEntry =  gtfMapGeneEntry.get(transcriptId);
            } else {
                gtfMapTranscriptEntry = new HashMap();
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
        // Add exon/cds GTF line to the corresponding gene entry in the map
        String featureType = gtf.getFeature().toLowerCase();
        if (featureType.equals("exon") || featureType.equals("cds")) {
            List gtfList;
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

    private List<TranscriptTfbs> getTranscriptTfbses(Gtf transcript, String chromosome, TabixReader tabixReader) throws IOException {
        if (tabixReader == null) {
            return null;
        }
        List<TranscriptTfbs> transcriptTfbses = null;

        int transcriptStart = transcript.getStart();
        int transcriptEnd = transcript.getEnd();


        String line;
        TabixReader.Iterator iter = tabixReader.query(chromosome, transcriptStart, transcriptEnd);
        while ((line = iter.next()) != null) {
            String[] elements = line.split("\t");

            String sequenceName = elements[0];
            String source = elements[1];
            String feature = elements[2];
            int start = Integer.parseInt(elements[3]);
            int end = Integer.parseInt(elements[4]);
            String score = elements[5];
            String strand = elements[6];
            String frame = elements[7];
            String attribute = elements[8];

            if (strand.equals(transcript.getStrand())) {
                continue;
            }

            if (transcript.getStrand().equals("+")) {
                if (start > transcript.getStart() + 500) {
                    break;
                } else if (end > transcript.getStart() - 2500) {
                    Gff2 tfbs = new Gff2(sequenceName, source, feature, start, end, score, strand, frame, attribute);
                    transcriptTfbses = addTranscriptTfbstoList(tfbs, transcript, chromosome, transcriptTfbses);
                }
            } else {
                // transcript in negative strand
                if (start > transcript.getEnd() + 2500) {
                    break;
                } else if (start > transcript.getEnd() - 500) {
                    Gff2 tfbs = new Gff2(sequenceName, source, feature, start, end, score, strand, frame, attribute);
                    transcriptTfbses = addTranscriptTfbstoList(tfbs, transcript, chromosome, transcriptTfbses);
                }
            }
        }

        return transcriptTfbses;
    }

    protected List<TranscriptTfbs> addTranscriptTfbstoList(Gff2 tfbs, Gtf transcript, String chromosome,
                                                           List<TranscriptTfbs> transcriptTfbses) {
        if (transcriptTfbses == null) {
            transcriptTfbses = new ArrayList<>();
        }

        // binding_matrix_stable_id=ENSPFM0542;epigenomes_with_experimental_evidence=SK-N.%2CMCF-7%2CH1-hESC_3%2CHCT116;
        // stable_id=ENSM00208374688;transcription_factor_complex=TEAD4::ESRRB
        String[] attributes = tfbs.getAttribute().split(";");

        String id = null;
        String pfmId = null;
        List<String> transciptionFactors = null;

        for (String attributePair : attributes) {
            String[] attributePairArray = attributePair.split("=");
            switch(attributePairArray[0]) {
                case "binding_matrix_stable_id":
                    pfmId = attributePairArray[1];
                    break;
                case "stable_id":
                    id = attributePairArray[1];
                    break;
                case "transcription_factor_complex":
                    transciptionFactors = Arrays.asList(attributePairArray[1].split("(::)|(%2C)"));
                    break;
                default:
                    break;
            }
        }

        transcriptTfbses.add(new TranscriptTfbs(id, pfmId, tfbs.getFeature(), transciptionFactors, chromosome, tfbs.getStart(),
                tfbs.getEnd(), getRelativeTranscriptTfbsStart(tfbs, transcript), getRelativeTranscriptTfbsEnd(tfbs, transcript),
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



    private boolean newGene(Gene previousGene, String newGeneId) {
        return previousGene == null || !newGeneId.equals(previousGene.getId());
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
