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
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENE_DATA));

        // Create gene folder
        Path geneDownloadPath = downloadFolder.resolve(GENE_DATA);

        // Create Ensembl folder
        Path ensemblDownloadPath = geneDownloadPath.resolve(ENSEMBL_DATA);
        Files.createDirectories(ensemblDownloadPath);

        // Create RefSeq folder
        Path refSeqDownloadPath = geneDownloadPath.resolve(REFSEQ_DATA);
        Files.createDirectories(refSeqDownloadPath);

        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Ensembl
        downloadFiles.addAll(downloadEnsemblData(ensemblDownloadPath));

        // RefSeq
        downloadFiles.addAll(downloadRefSeq(refSeqDownloadPath));

        // Gene annotation
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENE_ANNOTATION_DATA));
        downloadFiles.add(downloadMane(geneDownloadPath));
        downloadFiles.add(downloadLrg(geneDownloadPath));
        downloadFiles.add(downloadHgnc(geneDownloadPath));
        downloadFiles.add(downloadCancerHotspot(geneDownloadPath));
        downloadFiles.add(downloadDrugData(geneDownloadPath));
        downloadFiles.add(downloadGeneUniprotXref(geneDownloadPath));
        downloadFiles.add(downloadGeneExpressionAtlas(geneDownloadPath));
        downloadFiles.add(downloadGeneDiseaseAnnotation(geneDownloadPath));
        downloadFiles.add(downloadGnomadConstraints(geneDownloadPath));
        downloadFiles.add(downloadGO(geneDownloadPath));
        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_ANNOTATION_DATA));

        // Save data sources manually downloaded
        // HPO
        saveDataSource(HPO_DISEASE_DATA, configuration.getDownload().getHpo().getVersion(), getTimeStamp(),
                Collections.singletonList(getManualUrl(configuration.getDownload().getHpo(), HPO_FILE_ID)),
                geneDownloadPath.resolve(getDataVersionFilename(HPO_DISEASE_DATA)));
        logger.warn("{} must be downloaded manually; the version file {} was created at {}", getDataName(HPO_DISEASE_DATA),
                getDataVersionFilename(HPO_DISEASE_DATA), geneDownloadPath);
        // Cancer gene census
        saveDataSource(CANCER_GENE_CENSUS_DATA, configuration.getDownload().getCancerGeneCensus().getVersion(), getTimeStamp(),
                Collections.singletonList(getManualUrl(configuration.getDownload().getCancerGeneCensus(), CANCER_GENE_CENSUS_FILE_ID)),
                geneDownloadPath.resolve(getDataVersionFilename(CANCER_GENE_CENSUS_DATA)));
        logger.warn("{} must be downloaded manually; the version file {} was created at {}", getDataName(CANCER_GENE_CENSUS_DATA),
                getDataVersionFilename(CANCER_GENE_CENSUS_DATA), geneDownloadPath);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_DATA));

        return downloadFiles;
    }

    private List<DownloadFile> downloadEnsemblData(Path ensemblDownloadPath) throws IOException, InterruptedException, CellBaseException {
        logger.info(CATEGORY_DOWNLOADING_LOG_MESSAGE, getDataName(ENSEMBL_DATA), getDataCategory(ENSEMBL_DATA));

        List<DownloadFile> downloadFiles = new ArrayList<>();
        DownloadProperties.EnsemblProperties ensemblProps = configuration.getDownload().getEnsembl();

        // GTF
        downloadFiles.add(downloadEnsemblDataSource(ensemblProps, ENSEMBL_GTF_FILE_ID, ensemblDownloadPath));
        // PEP
        downloadFiles.add(downloadEnsemblDataSource(ensemblProps, ENSEMBL_PEP_FA_FILE_ID, ensemblDownloadPath));
        // CDNA
        downloadFiles.add(downloadEnsemblDataSource(ensemblProps, ENSEMBL_CDNA_FA_FILE_ID, ensemblDownloadPath));

        // Save data source (i.e., metadata)
        List<String> urls = getUrls(downloadFiles);
        // Add manually downloaded files
        urls.addAll(getManualUrls(ensemblProps.getUrl()));
        saveDataSource(ENSEMBL_DATA, ensemblVersion, getTimeStamp(), urls,
                ensemblDownloadPath.resolve(getDataVersionFilename(ENSEMBL_DATA)));

        logger.info(CATEGORY_DOWNLOADING_DONE_LOG_MESSAGE, getDataName(ENSEMBL_DATA), getDataCategory(ENSEMBL_DATA));
        return downloadFiles;
    }

    private List<DownloadFile> downloadRefSeq(Path refSeqDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(CATEGORY_DOWNLOADING_LOG_MESSAGE, getDataName(REFSEQ_DATA), getDataCategory(REFSEQ_DATA));

            List<DownloadFile> downloadFiles = new ArrayList<>();
            DownloadProperties.URLProperties refSeqProps = configuration.getDownload().getRefSeq();

            // GTF
            downloadFiles.add(downloadDataSource(refSeqProps, REFSEQ_GENOMIC_GTF_FILE_ID, refSeqDownloadPath));
            // Genomic FASTA
            downloadFiles.add(downloadDataSource(refSeqProps, REFSEQ_GENOMIC_FNA_FILE_ID, refSeqDownloadPath));
            // Protein FASTA
            downloadFiles.add(downloadDataSource(refSeqProps, REFSEQ_PROTEIN_FAA_FILE_ID, refSeqDownloadPath));
            // cDNA
            downloadFiles.add(downloadDataSource(refSeqProps, REFSEQ_RNA_FNA_FILE_ID, refSeqDownloadPath));

            // Save data source (i.e., metadata)
            saveDataSource(REFSEQ_DATA, refSeqProps.getVersion(), getTimeStamp(), getUrls(downloadFiles),
                    refSeqDownloadPath.resolve(getDataVersionFilename(REFSEQ_DATA)));

            logger.info(CATEGORY_DOWNLOADING_DONE_LOG_MESSAGE, getDataName(REFSEQ_DATA), getDataCategory(REFSEQ_DATA));
            return downloadFiles;
        }
        return Collections.emptyList();
    }

    private DownloadFile downloadMane(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MANE_SELECT_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getManeSelect(), MANE_SELECT_FILE_ID,
                    MANE_SELECT_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(MANE_SELECT_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadLrg(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(LRG_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getLrg(), LRG_FILE_ID, LRG_DATA,
                    geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(LRG_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadHgnc(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(HGNC_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getHgnc(), HGNC_FILE_ID, HGNC_DATA,
                    geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(HGNC_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadCancerHotspot(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(CANCER_HOTSPOT_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getCancerHotspot(), CANCER_HOTSPOT_FILE_ID,
                    CANCER_HOTSPOT_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(CANCER_HOTSPOT_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadDrugData(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(DGIDB_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getDgidb(), DGIDB_FILE_ID, DGIDB_DATA,
                    geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(DGIDB_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadGeneUniprotXref(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (GENE_UNIPROT_XREF_FILES.containsKey(speciesConfiguration.getScientificName())) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(UNIPROT_XREF_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGeneUniprotXref(), UNIPROT_XREF_FILE_ID,
                    UNIPROT_XREF_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(UNIPROT_XREF_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadGeneExpressionAtlas(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENE_EXPRESSION_ATLAS_DATA));

        DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGeneExpressionAtlas(),
                GENE_EXPRESSION_ATLAS_FILE_ID, GENE_EXPRESSION_ATLAS_DATA, geneDownloadPath);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_EXPRESSION_ATLAS_DATA));
        return downloadFile;
    }

    private DownloadFile downloadGeneDiseaseAnnotation(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENE_DISEASE_ANNOTATION_DATA));

        // DisGeNet
        DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getDisgenet(), DISGENET_FILE_ID, DISGENET_DATA,
                geneDownloadPath);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_DISEASE_ANNOTATION_DATA));
        return downloadFile;
    }

    private DownloadFile downloadGnomadConstraints(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GNOMAD_CONSTRAINTS_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGnomadConstraints(),
                    GNOMAD_CONSTRAINTS_FILE_ID, GNOMAD_CONSTRAINTS_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GNOMAD_CONSTRAINTS_DATA));
            return downloadFile;
        }
        return null;
    }

    private DownloadFile downloadGO(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GO_ANNOTATION_DATA));

            DownloadFile downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGoAnnotation(), GO_ANNOTATION_FILE_ID,
                    GO_ANNOTATION_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GO_ANNOTATION_DATA));
            return downloadFile;
        }
        return null;
    }
}
