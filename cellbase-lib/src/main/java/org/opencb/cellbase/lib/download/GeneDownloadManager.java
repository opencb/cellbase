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

package org.opencb.cellbase.lib.download;

import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class GeneDownloadManager extends AbstractDownloadManager {

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String REFSEQ_NAME = "RefSeq";
    private static final String UNIPROT_NAME = "UniProt";
    private static final String GENE_EXPRESSION_ATLAS_NAME = "Gene Expression Atlas";
    private static final String HPO_NAME = "HPO";
    private static final String DISGENET_NAME = "DisGeNET";
    private static final String MANE_SELECT_NAME = "MANE Select";
    private static final String LRG_NAME = "LRG";
    private static final String HGNC_GENE_NAME = "HGNC Gene";
    private static final String CANCER_HOTSPOT_NAME = "Cancer HotSpot";
    private static final String GO_ANNOTATION_NAME = "EBI Gene Ontology Annotation";
    private static final String DGIDB_NAME = "DGIdb";
    private static final String GNOMAD_NAME = "gnomAD";

    private static final Map<String, String> GENE_UNIPROT_XREF_FILES;

    static {
        GENE_UNIPROT_XREF_FILES = new HashMap<>();
        GENE_UNIPROT_XREF_FILES.put(HOMO_SAPIENS_NAME, "HUMAN_9606_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Mus musculus", "MOUSE_10090_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Rattus norvegicus", "RAT_10116_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Danio rerio", "DANRE_7955_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Drosophila melanogaster", "DROME_7227_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Saccharomyces cerevisiae", "YEAST_559292_idmapping_selected.tab.gz");
    }

    public GeneDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading gene information ...");
        Path geneFolder = downloadFolder.resolve("gene");
        Files.createDirectories(geneFolder);

        Path refseqFolder = downloadFolder.resolve("refseq");
        Files.createDirectories(refseqFolder);

        List<DownloadFile> downloadFiles = new ArrayList<>();

        downloadFiles.addAll(downloadEnsemblData(geneFolder));
        downloadFiles.addAll(downloadRefSeq(refseqFolder));
        downloadFiles.add(downloadMane(geneFolder));
        downloadFiles.add(downloadLrg(geneFolder));
        downloadFiles.add(downloadHgnc(geneFolder));
        downloadFiles.add(downloadCancerHotspot(geneFolder));
        downloadFiles.add(downloadDrugData(geneFolder));
        downloadFiles.add(downloadGeneUniprotXref(geneFolder));
        downloadFiles.add(downloadGeneExpressionAtlas(geneFolder));
        downloadFiles.add(downloadGeneDiseaseAnnotation(geneFolder));
        downloadFiles.add(downloadGnomadConstraints(geneFolder));
        downloadFiles.add(downloadGO(geneFolder));

        return downloadFiles;
    }

    private List<DownloadFile> downloadEnsemblData(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene Ensembl data (gtf, pep, cdna, motifs) ...");
        List<String> downloadedUrls = new ArrayList<>(4);
        List<DownloadFile> downloadFiles = new ArrayList<>();

        String ensemblHost = ensemblHostUrl + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
            ensemblHost = ensemblHostUrl + "/" + ensemblRelease + "/" + getPhylo(speciesConfiguration);
        }

        String ensemblCollection = "";
        if (configuration.getSpecies().getBacteria().contains(speciesConfiguration)) {
            // WARN: assuming there's just one assembly
            ensemblCollection =  speciesConfiguration.getAssemblies().get(0).getEnsemblCollection() + "/";
        }

        // Ensembl leaves now several GTF files in the FTP folder, we need to build a more accurate URL
        // to download the correct GTF file.
        String version = ensemblRelease.split("-")[1];
        String url = ensemblHost + "/gtf/" + ensemblCollection + speciesShortName + "/*" + version + ".gtf.gz";
        String fileName = geneFolder.resolve(speciesShortName + ".gtf.gz").toString();
        logger.info(DOWNLOADING_LOG_MESSAGE, url, fileName);
        downloadFiles.add(downloadFile(url, fileName));
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + ensemblCollection + speciesShortName + "/pep/*.pep.all.fa.gz";
        fileName = geneFolder.resolve(speciesShortName + ".pep.all.fa.gz").toString();
        logger.info(DOWNLOADING_LOG_MESSAGE, url, fileName);
        downloadFiles.add(downloadFile(url, fileName));
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + ensemblCollection + speciesShortName + "/cdna/*.cdna.all.fa.gz";
        fileName = geneFolder.resolve(speciesShortName + ".cdna.all.fa.gz").toString();
        logger.info(DOWNLOADING_LOG_MESSAGE, url, fileName);
        downloadFiles.add(downloadFile(url, fileName));
        downloadedUrls.add(url);

        saveDataSource(EtlCommons.GENE_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                geneFolder.resolve(ENSEMBL_CORE_VERSION_FILENAME));

        return downloadFiles;
    }

    private List<DownloadFile> downloadRefSeq(Path refSeqFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {

            logger.info("Downloading RefSeq data ...");

            List<DownloadFile> downloadFiles = new ArrayList<>();

            String timeStamp = getTimeStamp();

            // gtf
            DownloadFile downloadFile = downloadRefSeqFile(REFSEQ_NAME, configuration.getDownload().getRefSeq(), timeStamp,
                    REFSEQ_VERSION_FILENAME, refSeqFolder);
            downloadFiles.add(downloadFile);

            // genomic fasta
            downloadFile = downloadRefSeqFile(REFSEQ_NAME + " Fasta", configuration.getDownload().getRefSeqFasta(), timeStamp,
                    REFSEQ_ASTA_VERSION_FILENAME, refSeqFolder);
            downloadFiles.add(downloadFile);
            if (StringUtils.isNotEmpty(downloadFile.getOutputFile()) && Paths.get(downloadFile.getOutputFile()).toFile().exists()) {
                logger.info("Unzipping file: {}", downloadFile.getOutputFile());
                EtlCommons.runCommandLineProcess(null, "gunzip", Collections.singletonList(downloadFile.getOutputFile()), null);
            } else {
                logger.warn("Coud not find the file {} to unzip", downloadFile.getOutputFile());
            }

            // protein fasta
            downloadFile = downloadRefSeqFile(REFSEQ_NAME + " Protein Fasta", configuration.getDownload().getRefSeqProteinFasta(),
                    timeStamp, REFSEQ_PROTEIN_FASTA_VERSION_FILENAME, refSeqFolder);
            downloadFiles.add(downloadFile);

            // cDNA
            downloadFile = downloadRefSeqFile(REFSEQ_NAME + " cDNA", configuration.getDownload().getRefSeqCdna(), timeStamp,
                    REFSEQ_CDNA_FASTA_VERSION_FILENAME, refSeqFolder);
            downloadFiles.add(downloadFile);

            return downloadFiles;
        }
        return Collections.emptyList();
    }

    private DownloadFile downloadRefSeqFile(String name, DownloadProperties.URLProperties urlProperties, String timeStamp,
                                            String versionFilename, Path refSeqFolder) throws IOException, InterruptedException {
        String url = urlProperties.getHost();
        String version = urlProperties.getVersion();
        String filename = getUrlFilename(url);
        Path outputPath = refSeqFolder.resolve(filename);
        saveDataSource(EtlCommons.REFSEQ_DATA, name, version, timeStamp, Collections.singletonList(url),
                refSeqFolder.resolve(versionFilename));

        logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
        return downloadFile(url, outputPath.toString());
    }

    private DownloadFile downloadMane(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading MANE Select ...");
            String url = configuration.getDownload().getManeSelect().getHost();
            saveDataSource(EtlCommons.GENE_DATA, MANE_SELECT_NAME, configuration.getDownload().getManeSelect().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(MANE_SELECT_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadLrg(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading LRG data ...");
            String url = configuration.getDownload().getLrg().getHost();
            saveDataSource(EtlCommons.GENE_DATA, LRG_NAME, configuration.getDownload().getLrg().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(LRG_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadHgnc(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading HGNC data ...");
            String url = configuration.getDownload().getHgnc().getHost();
            saveDataSource(GENE_DATA, HGNC_GENE_NAME, configuration.getDownload().getHgnc().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(HGNC_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadCancerHotspot(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading Cancer Hotspot ...");
            String url = configuration.getDownload().getCancerHotspot().getHost();
            saveDataSource(EtlCommons.GENE_DATA, CANCER_HOTSPOT_NAME, configuration.getDownload().getHgnc().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(CANCER_HOTSPOT_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadGO(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading GO annotation...");
            String url = configuration.getDownload().getGoAnnotation().getHost();
            saveDataSource(EtlCommons.GENE_DATA, GO_ANNOTATION_NAME, configuration.getDownload().getGoAnnotation().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(GO_ANNOTATION_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadGnomadConstraints(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading gnomAD constraints data...");
            String url = configuration.getDownload().getGnomadConstraints().getHost();
            saveDataSource(EtlCommons.GENE_DATA, GNOMAD_NAME, configuration.getDownload().getGnomadConstraints().getVersion(),
                    getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(GNOMAD_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadDrugData(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading drug-gene data...");
            String url = configuration.getDownload().getDgidb().getHost();
            saveDataSource(EtlCommons.GENE_DATA, DGIDB_NAME, configuration.getDownload().getDgidb().getVersion(), getTimeStamp(),
                    Collections.singletonList(url), geneFolder.resolve(DGIDB_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(getUrlFilename(url));
            logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
            return downloadFile(url, outputPath.toString());
        }
        return null;
    }

    private DownloadFile downloadGeneUniprotXref(Path geneFolder) throws IOException, InterruptedException {
        if (GENE_UNIPROT_XREF_FILES.containsKey(speciesConfiguration.getScientificName())) {
            logger.info("Downloading UniProt ID mapping ...");

            String filename = GENE_UNIPROT_XREF_FILES.get(speciesConfiguration.getScientificName());
            String geneGtfUrl = configuration.getDownload().getGeneUniprotXref().getHost() + "/" + filename;

            saveDataSource(EtlCommons.GENE_DATA, UNIPROT_NAME,
                    configuration.getDownload().getGeneUniprotXref().getVersion(), getTimeStamp(),
                    Collections.singletonList(geneGtfUrl), geneFolder.resolve(UNIPROT_XREF_VERSION_FILENAME));

            Path outputPath = geneFolder.resolve(filename);
            logger.info(DOWNLOADING_LOG_MESSAGE, geneGtfUrl, outputPath);
            return downloadFile(geneGtfUrl, outputPath.toString());
        }

        return null;
    }

    private DownloadFile downloadGeneExpressionAtlas(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene expression atlas ...");
        String geneGtfUrl = configuration.getDownload().getGeneExpressionAtlas().getHost();
        saveDataSource(EtlCommons.GENE_DATA, GENE_EXPRESSION_ATLAS_NAME, configuration.getDownload().getGeneExpressionAtlas().getVersion(),
                getTimeStamp(), Collections.singletonList(geneGtfUrl), geneFolder.resolve(GENE_EXPRESSION_ATLAS_VERSION_FILENAME));

        Path outputPath = geneFolder.resolve(getUrlFilename(geneGtfUrl));
        logger.info(DOWNLOADING_LOG_MESSAGE, geneGtfUrl, outputPath);
        return downloadFile(geneGtfUrl, outputPath.toString());
    }

    private DownloadFile downloadGeneDiseaseAnnotation(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene disease annotation ...");

        // IMPORTANT !!!
        logger.warn("HPO must be downloaded manually from {} and then create the file {} with data ({}), name ({}) and the version",
                configuration.getDownload().getHpo().getHost(), HPO_VERSION_FILENAME, GENE_DATA, HPO_NAME);

        String url = configuration.getDownload().getDisgenet().getHost();
        saveDataSource(EtlCommons.GENE_DISEASE_ASSOCIATION_DATA, DISGENET_NAME, configuration.getDownload().getDisgenet().getVersion(),
                getTimeStamp(), Collections.singletonList(url), geneFolder.resolve(DISGINET_VERSION_FILENAME));

        Path outputPath = geneFolder.resolve(getUrlFilename(url));
        logger.info(DOWNLOADING_LOG_MESSAGE, url, outputPath);
        return downloadFile(url, outputPath.toString());
    }
}
