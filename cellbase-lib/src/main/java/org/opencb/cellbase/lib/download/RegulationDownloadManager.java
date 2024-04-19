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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;


public class RegulationDownloadManager extends AbstractDownloadManager {

    private Path regulationFolder;

    public RegulationDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        if (!speciesHasInfoToDownload(speciesConfiguration, REGULATION_DATA)) {
            return Collections.emptyList();
        }
        regulationFolder = downloadFolder.resolve(REGULATION_SUBDIRECTORY);
        Files.createDirectories(regulationFolder);
        logger.info("Downloading {} files at {} ...", REGULATION_DATA, regulationFolder);

        List<DownloadFile> downloadFiles = new ArrayList<>();

        downloadFiles.addAll(downloadRegulatoryaAndMotifFeatures());
        downloadFiles.add(downloadMiRTarBase());
        downloadFiles.add(downloadMirna());

        return downloadFiles;
    }

    /**
     * Downloads Ensembl regulatory build and motif feature files.
     * @throws IOException Any issue when writing files
     * @throws InterruptedException Any issue downloading files
     */
    private List<DownloadFile> downloadRegulatoryaAndMotifFeatures() throws IOException, InterruptedException, CellBaseException {
//        String baseUrl;
//        if (configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
//            baseUrl = ensemblHostUrl + ensemblRelease + "/";
//        } else {
//            baseUrl = ensemblHostUrl + ensemblRelease + "/" + getPhylo(speciesConfiguration) + "/";
//        }

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Regulatory build
        downloadFile = downloadAndSaveEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_REGULATORY_BUILD_FILE_ID,
                REGULATORY_BUILD_NAME, REGULATION_DATA, null, REGULATORY_BUILD_VERSION_FILENAME, regulationFolder);
        downloadFiles.add(downloadFile);

        // Motifs features
        List<String> urls = new ArrayList<>();
        downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_MOTIF_FEATURES_FILE_ID, null,
                regulationFolder);
        downloadFiles.add(downloadFile);
        urls.add(downloadFile.getUrl());
        // And now the index file
        downloadFile = downloadEnsemblDataSource(configuration.getDownload().getEnsembl(), ENSEMBL_MOTIF_FEATURES_INDEX_FILE_ID, null,
                regulationFolder);
        downloadFiles.add(downloadFile);
        urls.add(downloadFile.getUrl());
        // Save data source (name, category, version,...)
        saveDataSource(MOTIF_FEATURES_NAME, REGULATION_DATA, "(Ensembl " + ensemblVersion + ")", getTimeStamp(), urls,
                regulationFolder.resolve(MOTIF_FEATURES_VERSION_FILENAME));

        // TODO: This will be executed in the CellBase build
//        loadPfmMatrices();

        return downloadFiles;
    }

//    private void loadPfmMatrices()
//    throws IOException, NoSuchMethodException, FileFormatException, InterruptedException, CellBaseException {
//        logger.info("Downloading and building pfm matrices...");
//        if (Files.exists(buildFolder.resolve("regulatory_pfm.json.gz"))) {
//            logger.info("regulatory_pfm.json.gz is already built");
//            return;
//        }
//        Set<String> motifIds = new HashSet<>();
//        Path motifGffFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE);
//        try (Gff2Reader motifsFeatureReader = new Gff2Reader(motifGffFile)) {
//            Gff2 tfbsMotifFeature;
//            Pattern filePattern = Pattern.compile("ENSPFM(\\d+)");
//            while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
//                String pfmId = getMatrixId(filePattern, tfbsMotifFeature);
//                if (StringUtils.isNotEmpty(pfmId)) {
//                    motifIds.add(pfmId);
//                }
//            }
//        }
//
//        ObjectMapper mapper = new ObjectMapper();
//        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "regulatory_pfm", true);
//        if (logger.isInfoEnabled()) {
//            logger.info("Looking up {} pfms", motifIds.size());
//        }
//        for (String pfmId : motifIds) {
//            String urlString = "https://rest.ensembl.org/species/homo_sapiens/binding_matrix/" + pfmId
//                    + "?unit=frequencies;content-type=application/json";
//            URL url = new URL(urlString);
//            RegulatoryPfm regulatoryPfm = mapper.readValue(url, RegulatoryPfm.class);
//            serializer.serialize(regulatoryPfm);
//            // https://github.com/Ensembl/ensembl-rest/wiki/Rate-Limits
//            TimeUnit.MILLISECONDS.sleep(250);
//        }
//        serializer.close();
//    }
//
//    private String getMatrixId(Pattern pattern, Gff2 tfbsMotifFeature) {
//        Matcher matcher = pattern.matcher(tfbsMotifFeature.getAttribute());
//        if (matcher.find()) {
//            return matcher.group(0);
//        }
//        return null;
//    }

    private DownloadFile downloadMirna() throws IOException, InterruptedException, CellBaseException {
        logger.info("Downloading {} ...", MIRBASE_NAME);
        return downloadAndSaveDataSource(configuration.getDownload().getMirbase(), MIRBASE_FILE_ID, MIRBASE_NAME, REGULATION_DATA,
                MIRBASE_VERSION_FILENAME, regulationFolder);
    }

    private DownloadFile downloadMiRTarBase() throws IOException, InterruptedException, CellBaseException {
        logger.info("Downloading {} ...", MIRTARBASE_NAME);
        return downloadAndSaveDataSource(configuration.getDownload().getMiRTarBase(), MIRTARBASE_FILE_ID, MIRTARBASE_NAME, REGULATION_DATA,
                MIRTARBASE_VERSION_FILENAME, regulationFolder);
    }
}
