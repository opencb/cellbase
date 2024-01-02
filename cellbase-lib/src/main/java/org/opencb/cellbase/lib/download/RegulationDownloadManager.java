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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegulationDownloadManager extends AbstractDownloadManager {

    private Path regulationFolder;

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String MIRBASE_NAME = "miRBase";
    private static final String MIRTARBASE_NAME = "miRTarBase";

    public RegulationDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, NoSuchMethodException, FileFormatException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "regulation")) {
            return null;
        }
        this.regulationFolder = downloadFolder.resolve("regulation");
        Files.createDirectories(regulationFolder);

        logger.info("Downloading regulation information ...");

        List<DownloadFile> downloadFiles = new ArrayList<>();

        downloadFiles.addAll(downloadRegulatoryaAndMotifFeatures());
        downloadFiles.add(downloadMirna());
        downloadFiles.add(downloadMiRTarBase());

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
        Path motifGffFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE);
        Gff2Reader motifsFeatureReader = new Gff2Reader(motifGffFile);
        Gff2 tfbsMotifFeature;
        Set<String> motifIds = new HashSet<>();
        Pattern filePattern = Pattern.compile("ENSPFM(\\d+)");
        while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
            String pfmId = getMatrixId(filePattern, tfbsMotifFeature);
            if (StringUtils.isNotEmpty(pfmId)) {
                motifIds.add(pfmId);
            }
        }
        motifsFeatureReader.close();

        ObjectMapper mapper = new ObjectMapper();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "regulatory_pfm", true);
        logger.info("Looking up " + motifIds.size() + " pfms");
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
        String url = configuration.getDownload().getMirbase().getHost();
        String readmeUrl = configuration.getDownload().getMirbaseReadme().getHost();
        downloadFile(readmeUrl, regulationFolder.resolve("mirbaseReadme.txt").toString());
        saveVersionData(EtlCommons.REGULATION_DATA, MIRBASE_NAME,
                getLine(regulationFolder.resolve("mirbaseReadme.txt"), 1), getTimeStamp(),
                Collections.singletonList(url), regulationFolder.resolve("mirbaseVersion.json"));
        Path outputPath = regulationFolder.resolve("miRNA.xls.gz");
        DownloadFile downloadFile = downloadFile(url, regulationFolder.resolve("miRNA.xls.gz").toString());
        EtlCommons.runCommandLineProcess(null, "gunzip", Collections.singletonList(outputPath.toString()), null);
        return downloadFile;
    }

    private DownloadFile downloadMiRTarBase() throws IOException, InterruptedException {
        String url = configuration.getDownload().getMiRTarBase().getHost();
        saveVersionData(EtlCommons.REGULATION_DATA, MIRTARBASE_NAME, null, getTimeStamp(), Collections.singletonList(url),
                regulationFolder.resolve("miRTarBaseVersion.json"));
        return downloadFile(url, regulationFolder.resolve("hsa_MTI.xlsx").toString());
    }
}
