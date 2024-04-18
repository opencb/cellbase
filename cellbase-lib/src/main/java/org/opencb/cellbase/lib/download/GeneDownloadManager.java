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

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class GeneDownloadManager extends AbstractDownloadManager {

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
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
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

    private List<DownloadFile> downloadRefSeq(Path refSeqFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} data ...", REFSEQ_NAME);

            List<DownloadFile> downloadFiles = new ArrayList<>();

            // GTF
            downloadFiles.add(downloadDataSource(configuration.getDownload().getRefSeq(), REFSEQ_GENOMIC_GTF_FILE_ID, refSeqFolder));
            // Genomic FASTA
            downloadFiles.add(downloadDataSource(configuration.getDownload().getRefSeq(), REFSEQ_GENOMIC_FNA_FILE_ID, refSeqFolder));
            // Protein FASTA
            downloadFiles.add(downloadDataSource(configuration.getDownload().getRefSeq(), REFSEQ_PROTEIN_FAA_FILE_ID, refSeqFolder));
            // cDNA
            downloadFiles.add(downloadDataSource(configuration.getDownload().getRefSeq(), REFSEQ_RNA_FNA_FILE_ID, refSeqFolder));

            // Save data source (i.e., metadata)
            saveDataSource(REFSEQ_NAME, GENE_DATA, configuration.getDownload().getRefSeq().getVersion(), getTimeStamp(),
                    downloadFiles.stream().map(DownloadFile::getUrl).collect(Collectors.toList()),
                    refSeqFolder.resolve(REFSEQ_VERSION_FILENAME));

            return downloadFiles;
        }
        return Collections.emptyList();
    }

    private DownloadFile downloadMane(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", MANE_SELECT_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getManeSelect(), MANE_SELECT_FILE_ID, MANE_SELECT_NAME, GENE_DATA,
                    MANE_SELECT_VERSION_FILENAME, geneFolder);
        }
        return null;
    }

    private DownloadFile downloadLrg(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", LRG_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getLrg(), LRG_FILE_ID, LRG_NAME, GENE_DATA, LRG_VERSION_FILENAME,
                    geneFolder);
        }
        return null;
    }

    private DownloadFile downloadHgnc(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", HGNC_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getHgnc(), HGNC_FILE_ID, HGNC_NAME, GENE_DATA,
                    HGNC_VERSION_FILENAME, geneFolder);
        }
        return null;
    }

    private DownloadFile downloadCancerHotspot(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", CANCER_HOTSPOT_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getCancerHotspot(), CANCER_HOTSPOT_FILE_ID, CANCER_HOTSPOT_NAME,
                    GENE_DATA, CANCER_HOTSPOT_VERSION_FILENAME, geneFolder);
        }
        return null;
    }

    private DownloadFile downloadDrugData(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", DGIDB_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getDgidb(), DGIDB_FILE_ID, DGIDB_NAME, GENE_DATA,
                    DGIDB_VERSION_FILENAME, geneFolder);
        }
        return null;
    }

    private DownloadFile downloadGeneUniprotXref(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (GENE_UNIPROT_XREF_FILES.containsKey(speciesConfiguration.getScientificName())) {
            logger.info("Downloading {} ...", UNIPROT_XREF_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getGeneUniprotXref(), UNIPROT_XREF_FILE_ID, UNIPROT_XREF_NAME,
                    GENE_DATA, UNIPROT_XREF_VERSION_FILENAME, geneFolder);
        }
        return null;
    }

    private DownloadFile downloadGeneExpressionAtlas(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        logger.info("Downloading {} ...", GENE_EXPRESSION_ATLAS_NAME);
        return downloadAndSaveDataSource(configuration.getDownload().getGeneExpressionAtlas(), GENE_EXPRESSION_ATLAS_FILE_ID,
                GENE_EXPRESSION_ATLAS_NAME, GENE_DATA, GENE_EXPRESSION_ATLAS_VERSION_FILENAME, geneFolder);
    }

    private DownloadFile downloadGeneDiseaseAnnotation(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        logger.info("Downloading {} ...", GENE_DISEASE_ANNOTATION_NAME);

        // IMPORTANT !!!
        logger.warn("{} must be downloaded manually from {} and then create the file {} with data ({}), name ({}) and the version",
                HPO_NAME, configuration.getDownload().getHpo().getHost(), HPO_VERSION_FILENAME, GENE_DATA, HPO_NAME);
        saveDataSource(HPO_NAME, GENE_DISEASE_ANNOTATION_NAME, configuration.getDownload().getHpo().getVersion(), getTimeStamp(),
                Collections.singletonList(configuration.getDownload().getHpo().getHost()), geneFolder.resolve(HPO_VERSION_FILENAME));

        return downloadAndSaveDataSource(configuration.getDownload().getDisgenet(), DISGENET_FILE_ID, DISGENET_NAME,
                GENE_DISEASE_ANNOTATION_NAME, DISGENET_VERSION_FILENAME, geneFolder);
    }

    private DownloadFile downloadGnomadConstraints(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", GNOMAD_CONSTRAINTS_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getGnomadConstraints(), GNOMAD_CONSTRAINTS_FILE_ID,
                    GNOMAD_CONSTRAINTS_NAME, GENE_DATA, GNOMAD_CONSTRAINTS_VERSION_FILENAME, geneFolder);
        }
        return null;
    }

    private DownloadFile downloadGO(Path geneFolder) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("Downloading {} ...", GO_ANNOTATION_NAME);
            return downloadAndSaveDataSource(configuration.getDownload().getGoAnnotation(), GO_ANNOTATION_FILE_ID, GO_ANNOTATION_NAME,
                    GENE_DATA, GO_ANNOTATION_VERSION_FILENAME, geneFolder);
        }
        return null;
    }
}
