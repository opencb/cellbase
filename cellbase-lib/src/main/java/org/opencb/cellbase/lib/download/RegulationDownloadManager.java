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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gff.io.Gff2Reader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.RegulatoryPfm;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.cellbase.lib.EtlCommons.*;


public class RegulationDownloadManager extends AbstractDownloadManager {

    private Path regulationFolder;

    public RegulationDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, NoSuchMethodException, FileFormatException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "regulation")) {
            return Collections.emptyList();
        }
        this.regulationFolder = downloadFolder.resolve("regulation");
        Files.createDirectories(regulationFolder);

        logger.info("Downloading regulation information ...");

        List<DownloadFile> downloadFiles = new ArrayList<>();

        downloadFiles.addAll(downloadRegulatoryaAndMotifFeatures());
        downloadFiles.add(downloadMiRTarBase());
        downloadFiles.add(downloadMirna());

        return downloadFiles;
    }

    /**
     * Downloads Ensembl regulatory buid and motif feature files.
     * @throws IOException Any issue when writing files
     * @throws InterruptedException Any issue downloading files
     */
    private List<DownloadFile> downloadRegulatoryaAndMotifFeatures()
            throws IOException, InterruptedException, NoSuchMethodException, FileFormatException {
        String regulationUrl = ensemblHostUrl + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
            regulationUrl = ensemblHostUrl + "/" + ensemblRelease + "/" + getPhylo(speciesConfiguration);
        }
        regulationUrl += "/regulation/" + speciesShortName;

        List<DownloadFile> downloadFiles = new ArrayList<>();

        Path outputFile = regulationFolder.resolve(EtlCommons.REGULATORY_FEATURES_FILE);
        String regulatoryBuildUrl = regulationUrl + "/*Regulatory_Build.regulatory_features*.gff.gz";
        downloadFiles.add(downloadFile(regulatoryBuildUrl, outputFile.toString()));

        outputFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE);
        String motifUrl = regulationUrl + "/MotifFeatures/*" + assemblyConfiguration.getName() + ".motif_features.gff.gz";
        downloadFiles.add(downloadFile(motifUrl, outputFile.toString()));

        String motifTbiUrl = regulationUrl + "/MotifFeatures/*" + assemblyConfiguration.getName() + ".motif_features.gff.gz.tbi";
        outputFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE + ".tbi");
        downloadFiles.add(downloadFile(motifTbiUrl, outputFile.toString()));

        loadPfmMatrices();

        return downloadFiles;
    }

    private void loadPfmMatrices() throws IOException, NoSuchMethodException, FileFormatException, InterruptedException {
        logger.info("Downloading and building pfm matrices...");
        if (Files.exists(buildFolder.resolve("regulatory_pfm.json.gz"))) {
            logger.info("regulatory_pfm.json.gz is already built");
            return;
        }
        Set<String> motifIds = new HashSet<>();
        Path motifGffFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE);
        try (Gff2Reader motifsFeatureReader = new Gff2Reader(motifGffFile)) {
            Gff2 tfbsMotifFeature;
            Pattern filePattern = Pattern.compile("ENSPFM(\\d+)");
            while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
                String pfmId = getMatrixId(filePattern, tfbsMotifFeature);
                if (StringUtils.isNotEmpty(pfmId)) {
                    motifIds.add(pfmId);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "regulatory_pfm", true);
        if (logger.isInfoEnabled()) {
            logger.info("Looking up {} pfms", motifIds.size());
        }
        for (String pfmId : motifIds) {
            String urlString = "https://rest.ensembl.org/species/homo_sapiens/binding_matrix/" + pfmId
                    + "?unit=frequencies;content-type=application/json";
            URL url = new URL(urlString);
            RegulatoryPfm regulatoryPfm = mapper.readValue(url, RegulatoryPfm.class);
            serializer.serialize(regulatoryPfm);
            // https://github.com/Ensembl/ensembl-rest/wiki/Rate-Limits
            TimeUnit.MILLISECONDS.sleep(250);
        }
        serializer.close();
    }

    private String getMatrixId(Pattern pattern, Gff2 tfbsMotifFeature) {
        Matcher matcher = pattern.matcher(tfbsMotifFeature.getAttribute());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    private DownloadFile downloadMirna() throws IOException, InterruptedException {
        logger.info("Downloading {} ...", MIRBASE_NAME);
        String url = configuration.getDownload().getMirbase().getHost();

        saveVersionData(EtlCommons.REGULATION_DATA, MIRBASE_NAME, configuration.getDownload().getMirbase().getVersion(), getTimeStamp(),
                Collections.singletonList(url), regulationFolder.resolve(MIRBASE_VERSION_FILENAME));
        Path outputPath = regulationFolder.resolve(Paths.get(url).getFileName());
        logger.info("Downloading from {} to {} ...", url, outputPath);
        return downloadFile(url, outputPath.toString());
    }

    private DownloadFile downloadMiRTarBase() throws IOException, InterruptedException {
        logger.info("Downloading {} ...", MIRTARBASE_NAME);
        String url = configuration.getDownload().getMiRTarBase().getHost();

        saveVersionData(EtlCommons.REGULATION_DATA, MIRTARBASE_NAME, configuration.getDownload().getMiRTarBase().getVersion(),
                getTimeStamp(), Collections.singletonList(url), regulationFolder.resolve(MIRTARBASE_VERSION_FILENAME));
        Path outputPath = regulationFolder.resolve(Paths.get(url).getFileName());
        logger.info("Downloading from {} to {} ...", url, outputPath);
        return downloadFile(url, outputPath.toString());
    }
}
