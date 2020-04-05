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
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegulationDownloadManager extends DownloadManager {

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String MIRBASE_NAME = "miRBase";
    private static final String MIRTARBASE_NAME = "miRTarBase";
    private static final String TARGETSCAN_NAME = "TargetScan";


    public RegulationDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, outdir, configuration);
    }

    public void downloadRegulation() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "regulation")) {
            return;
        }
        logger.info("Downloading regulation information ...");

        Path regulationFolder = downloadFolder.resolve("regulation");
        Files.createDirectories(regulationFolder);

        // Downloading Ensembl Regulation
        String regulationUrl = ensemblHostUrl + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
            regulationUrl = ensemblHostUrl + "/" + ensemblRelease + "/" + getPhylo(speciesConfiguration);
        }
        regulationUrl = regulationUrl + "/regulation/" + speciesShortName;

        List<String> downloadedUrls = new ArrayList<>();
        // TODO: REMOVE
        // >>>>>>>>>>>>>>>>>>>>>>>>>>> DEPRECATED
//        for (String regulationFile : DEPRECATED_REGULATION_FILES) {
//            Path outputFile = regulationFolder.resolve(regulationFile);
//            downloadFile(regulationUrl + "/" + regulationFile, outputFile.toString());
//            downloadedUrls.add(regulationUrl + "/" + regulationFile);
//        }
        // <<<<<<<<<<<<<<<<<<<<<<<<<<< DEPRECATED

        Path outputFile = regulationFolder.resolve(EtlCommons.REGULATORY_FEATURES_FILE);
        downloadFile(regulationUrl + "/*Regulatory_Build.regulatory_features*.gff.gz", outputFile.toString());
        downloadedUrls.add(regulationUrl + "/*Regulatory_Build.regulatory_features*.gff.gz");

        outputFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE);
        downloadFile(regulationUrl + "/*motiffeatures*.gff.gz", outputFile.toString());
        downloadedUrls.add(regulationUrl + "/*motiffeatures*.gff.gz");

        saveVersionData(EtlCommons.REGULATION_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                regulationFolder.resolve("ensemblRegulationVersion.json"));

        // Downloading miRNA info
        String url;
        Path mirbaseFolder = regulationFolder.resolve("mirbase");
        if (!Files.exists(mirbaseFolder)) {
            Files.createDirectories(mirbaseFolder);
            downloadedUrls = new ArrayList<>(2);

            url = configuration.getDownload().getMirbase().getHost() + "/miRNA.xls.gz";
            downloadFile(url, mirbaseFolder.resolve("miRNA.xls.gz").toString());
            downloadedUrls.add(url);

            url = configuration.getDownload().getMirbase().getHost() + "/aliases.txt.gz";
            downloadFile(url, mirbaseFolder.resolve("aliases.txt.gz").toString());
            downloadedUrls.add(url);

            String readmeUrl = configuration.getDownload().getMirbaseReadme().getHost();
            downloadFile(readmeUrl, mirbaseFolder.resolve("mirbaseReadme.txt").toString());
            saveVersionData(EtlCommons.REGULATION_DATA, MIRBASE_NAME,
                    getLine(mirbaseFolder.resolve("mirbaseReadme.txt"), 1), getTimeStamp(),
                    Collections.singletonList(url), mirbaseFolder.resolve("mirbaseVersion.json"));
        }

        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            if (assemblyConfiguration.getName().equalsIgnoreCase("GRCh37")) {
                url = configuration.getDownload().getTargetScan().getHost() + "/hg19/database/targetScanS.txt.gz";
                downloadFile(url, regulationFolder.resolve("targetScanS.txt.gz").toString());

                String readmeUrl = configuration.getDownload().getTargetScan().getHost() + "/hg19/database/README.txt";
                saveVersionData(EtlCommons.REGULATION_DATA, TARGETSCAN_NAME, null, getTimeStamp(),
                        Collections.singletonList(url), regulationFolder.resolve("targetScanVersion.json"));

                url = configuration.getDownload().getMiRTarBase().getHost() + "/hsa_MTI.xls";
                downloadFile(url, regulationFolder.resolve("hsa_MTI.xls").toString());
                saveVersionData(EtlCommons.REGULATION_DATA, MIRTARBASE_NAME, url.split("/")[5], getTimeStamp(),
                        Collections.singletonList(url), regulationFolder.resolve("miRTarBaseVersion.json"));
            }
        }
        if (speciesConfiguration.getScientificName().equals("Mus musculus")) {
            url = configuration.getDownload().getTargetScan().getHost() + "/mm9/database/targetScanS.txt.gz";
            downloadFile(url, regulationFolder.resolve("targetScanS.txt.gz").toString());

            String readmeUrl = configuration.getDownload().getTargetScan().getHost() + "/mm9/database/README.txt";
            downloadFile(readmeUrl, regulationFolder.resolve("targetScanReadme.txt").toString());
            saveVersionData(EtlCommons.REGULATION_DATA, TARGETSCAN_NAME, null, getTimeStamp(),
                    Collections.singletonList(url), regulationFolder.resolve("targetScanVersion.json"));

            url = configuration.getDownload().getMiRTarBase().getHost() + "/mmu_MTI.xls";
            downloadFile(url, regulationFolder.resolve("mmu_MTI.xls").toString());
            saveVersionData(EtlCommons.REGULATION_DATA, MIRTARBASE_NAME, url.split("/")[5], getTimeStamp(),
                    Collections.singletonList(url),
                    regulationFolder.resolve("miRTarBaseVersion.json"));
        }
    }
}
