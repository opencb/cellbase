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

import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.commons.utils.DockerUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class GeneDownloadManager extends AbstractDownloadManager {

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

        // Ensembl canonical
        downloadEnsemblCanonical(geneDownloadPath);

        // Gene extra info
        downloadGeneExtraInfo(geneDownloadPath);

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
        downloadFiles.add(downloadGnomadConstraints(geneDownloadPath));
        downloadFiles.add(downloadGO(geneDownloadPath));
        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_ANNOTATION_DATA));

        // Save data sources manually downloaded
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            // HPO
            if (Files.exists(geneDownloadPath.resolve(getDataVersionFilename(HPO_DISEASE_DATA)))) {
                logger.warn("The version file {} already exists", getDataVersionFilename(HPO_DISEASE_DATA));
            } else {
                saveDataSource(HPO_DISEASE_DATA, configuration.getDownload().getHpo().getVersion(), getTimeStamp(),
                        Collections.singletonList(getManualUrl(configuration.getDownload().getHpo(), HPO_FILE_ID)),
                        geneDownloadPath.resolve(getDataVersionFilename(HPO_DISEASE_DATA)));
                logger.warn("{} must be downloaded manually; the version file {} was created at {}", getDataName(HPO_DISEASE_DATA),
                        getDataVersionFilename(HPO_DISEASE_DATA), geneDownloadPath);
            }

            // Cancer gene census
            if (Files.exists(geneDownloadPath.resolve(getDataVersionFilename(CANCER_GENE_CENSUS_DATA)))) {
                logger.warn("The version file {} already exists", getDataVersionFilename(CANCER_GENE_CENSUS_DATA));
            } else {
                saveDataSource(CANCER_GENE_CENSUS_DATA, configuration.getDownload().getCancerGeneCensus().getVersion(), getTimeStamp(),
                        Collections.singletonList(getManualUrl(configuration.getDownload().getCancerGeneCensus(),
                                CANCER_GENE_CENSUS_FILE_ID)), geneDownloadPath.resolve(getDataVersionFilename(CANCER_GENE_CENSUS_DATA)));
                logger.warn("{} must be downloaded manually; the version file {} was created at {}", getDataName(CANCER_GENE_CENSUS_DATA),
                        getDataVersionFilename(CANCER_GENE_CENSUS_DATA), geneDownloadPath);
            }
        }

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_DATA));
        return downloadFiles;
    }

    private List<DownloadFile> downloadEnsemblData(Path ensemblDownloadPath) throws IOException, InterruptedException, CellBaseException {
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Check if the species is supported
        if (SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), GENE_DATA)) {
            // Already downloaded ?
            if (isAlreadyDownloaded(ensemblDownloadPath.resolve(getDataVersionFilename(ENSEMBL_DATA)), getDataName(ENSEMBL_DATA))) {
                return downloadFiles;
            }

            logger.info(CATEGORY_DOWNLOADING_LOG_MESSAGE, getDataName(ENSEMBL_DATA), getDataCategory(ENSEMBL_DATA));
            DownloadProperties.EnsemblProperties ensemblConfig = configuration.getDownload().getEnsembl();

            // GTF, DNA, RNA
            downloadFiles.add(downloadEnsemblDataSource(ensemblConfig, ENSEMBL_GTF_FILE_ID, ensemblDownloadPath));
            downloadFiles.add(downloadEnsemblDataSource(ensemblConfig, ENSEMBL_PEP_FA_FILE_ID, ensemblDownloadPath));
            downloadFiles.add(downloadEnsemblDataSource(ensemblConfig, ENSEMBL_CDNA_FA_FILE_ID, ensemblDownloadPath));

            // Save data source (i.e., metadata)
            List<String> urls = getUrls(downloadFiles);

            // Add files created by scripts
            urls.add(getManualUrl(configuration.getDownload().getEnsembl().getUrl(), ENSEMBL_DESCRIPTION_FILE_ID));
            urls.add(getManualUrl(configuration.getDownload().getEnsembl().getUrl(), ENSEMBL_XREFS_FILE_ID));
            urls.add(getManualUrl(configuration.getDownload().getEnsembl().getUrl(), ENSEMBL_CANONICAL_FILE_ID));

            saveDataSource(ENSEMBL_DATA, ensemblVersion, getTimeStamp(), urls,
                    ensemblDownloadPath.resolve(getDataVersionFilename(ENSEMBL_DATA)));

            logger.info(CATEGORY_DOWNLOADING_DONE_LOG_MESSAGE, getDataName(ENSEMBL_DATA), getDataCategory(ENSEMBL_DATA));
        }
        return downloadFiles;
    }

    private List<DownloadFile> downloadRefSeq(Path refSeqDownloadPath) throws IOException, InterruptedException, CellBaseException {
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Check if the species is supported
        if (SpeciesUtils.hasData(configuration, speciesConfiguration.getScientificName(), GENE_DATA)) {
            // GTF, DNA, RNA, Protein
            String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());
            if (configuration.getDownload().getRefSeq().getFiles().containsKey(prefixId + REFSEQ_GENOMIC_GTF_FILE_ID)
                    && !isAlreadyDownloaded(refSeqDownloadPath.resolve(getDataVersionFilename(REFSEQ_DATA)), getDataName(REFSEQ_DATA))) {
                logger.info(CATEGORY_DOWNLOADING_LOG_MESSAGE, getDataName(REFSEQ_DATA), getDataCategory(REFSEQ_DATA));

                DownloadProperties.URLProperties refSeqConfig = configuration.getDownload().getRefSeq();
                downloadFiles.add(downloadDataSource(refSeqConfig, prefixId + REFSEQ_GENOMIC_GTF_FILE_ID, refSeqDownloadPath));
                downloadFiles.add(downloadDataSource(refSeqConfig, prefixId + REFSEQ_GENOMIC_FNA_FILE_ID, refSeqDownloadPath));
                downloadFiles.add(downloadDataSource(refSeqConfig, prefixId + REFSEQ_RNA_FNA_FILE_ID, refSeqDownloadPath));
                downloadFiles.add(downloadDataSource(refSeqConfig, prefixId + REFSEQ_PROTEIN_FAA_FILE_ID, refSeqDownloadPath));

                // Save data source (i.e., metadata)
                saveDataSource(REFSEQ_DATA, refSeqConfig.getVersion(), getTimeStamp(), getUrls(downloadFiles),
                        refSeqDownloadPath.resolve(getDataVersionFilename(REFSEQ_DATA)));

                logger.info(CATEGORY_DOWNLOADING_DONE_LOG_MESSAGE, getDataName(REFSEQ_DATA), getDataCategory(REFSEQ_DATA));
            }
        }
        return downloadFiles;
    }

    public void downloadEnsemblCanonical(Path geneDownloadPath) throws IOException, CellBaseException {
        String ensemblCanonicalScript = "ensembl_canonical.pl";
        String ensemblCanonicalFilename = "ensembl_canonical.txt";

        if (Files.exists(geneDownloadPath.resolve(ensemblCanonicalFilename))) {
            logger.warn("File {} already exists, skipping running the Perl script {}", ensemblCanonicalFilename, ensemblCanonicalScript);
            return;
        }

        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(ENSEMBL_CANONICAL_DATA));

        String dockerImage = "opencb/cellbase-builder:" + GitRepositoryState.get().getBuildVersion();

        // Build command line to run Perl script via docker image
        // Output binding
        AbstractMap.SimpleEntry<String, String> outputBinding = new AbstractMap.SimpleEntry<>(
                geneDownloadPath.toAbsolutePath().toString(), "/tmp");

        // Params
        String params = "/opt/cellbase/scripts/ensembl-scripts/" + ensemblCanonicalScript
                + " --species \"" + speciesConfiguration.getId() + "\""
                + " --outdir \"" + outputBinding.getValue() + "\"";

        try {
            // Execute perl script in docker
            DockerUtils.run(dockerImage, null, outputBinding, params, null);
        } catch (Exception e) {
            logger.error("Error executing script {}: {}", params, e.getStackTrace());
        }

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(ENSEMBL_CANONICAL_DATA));
    }

    public void downloadGeneExtraInfo(Path geneDownloadPath) throws IOException, CellBaseException {
        String geneExtraInfoScript = "gene_extra_info.pl";
        String descriptionFilename = "description.txt";
        String xrefsFilename = "xrefs.txt";

        if (Files.exists(geneDownloadPath.resolve(descriptionFilename)) && Files.exists(geneDownloadPath.resolve(xrefsFilename))) {
            logger.warn("Files {} and {} already exist, skipping running the Perl script {}", descriptionFilename, xrefsFilename,
                    geneExtraInfoScript);
            return;
        }

        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENE_EXTRA_INFO_DATA));

        String dockerImage = "opencb/cellbase-builder:" + GitRepositoryState.get().getBuildVersion();

        // Build command line to run Perl script via docker image
        // Output binding
        AbstractMap.SimpleEntry<String, String> outputBinding = new AbstractMap.SimpleEntry<>(
                geneDownloadPath.toAbsolutePath().toString(), "/tmp");

        // Params
        String params = "/opt/cellbase/scripts/ensembl-scripts/" + geneExtraInfoScript
                + " --species \"" + speciesConfiguration.getScientificName() + "\""
                + " --assembly \"" + assemblyConfiguration.getName() + "\""
                + " --outdir \"" + outputBinding.getValue() + "\"";

        try {
            // Execute perl script in docker
            DockerUtils.run(dockerImage, null, outputBinding, params, null);
        } catch (Exception e) {
            logger.error("Error executing script {}: {}", params, e.getStackTrace());
        }

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_EXTRA_INFO_DATA));
    }

    private DownloadFile downloadMane(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(MANE_SELECT_DATA)),
                getDataName(MANE_SELECT_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(MANE_SELECT_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getManeSelect(), MANE_SELECT_FILE_ID,
                    MANE_SELECT_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(MANE_SELECT_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadLrg(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(LRG_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getLrg(), LRG_FILE_ID, LRG_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(LRG_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadHgnc(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(HGNC_DATA)), getDataName(HGNC_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(HGNC_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getHgnc(), HGNC_FILE_ID, HGNC_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(HGNC_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadCancerHotspot(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(CANCER_HOTSPOT_DATA)),
                getDataName(CANCER_HOTSPOT_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(CANCER_HOTSPOT_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getCancerHotspot(), CANCER_HOTSPOT_FILE_ID,
                    CANCER_HOTSPOT_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(CANCER_HOTSPOT_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadDrugData(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(DGIDB_DATA)), getDataName(DGIDB_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(DGIDB_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getDgidb(), DGIDB_FILE_ID, DGIDB_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(DGIDB_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadGeneUniprotXref(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());
        if (configuration.getDownload().getGeneUniprotXref().getFiles().containsKey(prefixId + UNIPROT_XREF_FILE_ID)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(UNIPROT_XREF_DATA)),
                getDataName(UNIPROT_XREF_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(UNIPROT_XREF_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGeneUniprotXref(),
                    prefixId + UNIPROT_XREF_FILE_ID, UNIPROT_XREF_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(UNIPROT_XREF_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadGeneExpressionAtlas(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(GENE_EXPRESSION_ATLAS_DATA)),
                getDataName(GENE_EXPRESSION_ATLAS_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GENE_EXPRESSION_ATLAS_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGeneExpressionAtlas(),
                    GENE_EXPRESSION_ATLAS_FILE_ID, GENE_EXPRESSION_ATLAS_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(GENE_EXPRESSION_ATLAS_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadGnomadConstraints(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(GNOMAD_CONSTRAINTS_DATA)),
                getDataName(GNOMAD_CONSTRAINTS_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GNOMAD_CONSTRAINTS_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGnomadConstraints(),
                    GNOMAD_CONSTRAINTS_FILE_ID, GNOMAD_CONSTRAINTS_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GNOMAD_CONSTRAINTS_DATA));
        }
        return downloadFile;
    }

    private DownloadFile downloadGO(Path geneDownloadPath) throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFile = null;

        // Check if the species is supported
        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());
        if (configuration.getDownload().getGoAnnotation().getFiles().containsKey(prefixId + GO_ANNOTATION_FILE_ID)
                && !isAlreadyDownloaded(geneDownloadPath.resolve(getDataVersionFilename(GO_ANNOTATION_DATA)),
                getDataName(GO_ANNOTATION_DATA))) {
            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GO_ANNOTATION_DATA));

            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGoAnnotation(),
                    prefixId + GO_ANNOTATION_FILE_ID, GO_ANNOTATION_DATA, geneDownloadPath);

            logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(GO_ANNOTATION_DATA));
        }
        return downloadFile;
    }
}
