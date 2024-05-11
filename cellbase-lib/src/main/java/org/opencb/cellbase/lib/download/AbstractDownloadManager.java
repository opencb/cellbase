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

import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.EtlCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.*;

public abstract class AbstractDownloadManager {

    protected static final String DOWNLOADING_LOG_MESSAGE = "Downloading {} ...";
    protected static final String DOWNLOADING_DONE_LOG_MESSAGE = "Ok ({})";
    protected static final String CATEGORY_DOWNLOADING_LOG_MESSAGE = "Downloading {}/{} ...";
    protected static final String CATEGORY_DOWNLOADING_DONE_LOG_MESSAGE = "Ok ({}/{})";
    protected static final String DOWNLOADING_FROM_TO_LOG_MESSAGE = "Downloading {} to {} ...";

    protected String species;
    protected String assembly;
    protected Path outdir;
    protected CellBaseConfiguration configuration;

    protected SpeciesConfiguration speciesConfiguration;
    protected String speciesShortName;
    protected String ensemblHostUrl;
    protected SpeciesConfiguration.Assembly assemblyConfiguration;
    protected String ensemblVersion;
    protected String ensemblRelease;
    protected Path downloadFolder;
    protected Path downloadLogFolder; // /download/log
    protected Path buildFolder; // <output>/<species>_<assembly>/generated-json

    protected ObjectReader dataSourceReader;
    protected ObjectWriter dataSourceWriter;

    protected Logger logger;

    protected AbstractDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        this.species = species;
        this.assembly = assembly;
        this.outdir = outdir;
        this.configuration = configuration;

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        this.dataSourceReader = jsonObjectMapper.readerFor(DataSource.class);
        this.dataSourceWriter = jsonObjectMapper.writerFor(DataSource.class);

        this.init();
    }

    private void init() throws CellBaseException, IOException {
        logger = LoggerFactory.getLogger(this.getClass());

        // Check Species
        this.speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, species);
        if (speciesConfiguration == null) {
            throw new CellBaseException("Invalid species: '" + species + "'");
        }
        this.speciesShortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
        this.ensemblHostUrl = getEnsemblURL(speciesConfiguration);

        // Check assembly and get Ensembl version
        if (StringUtils.isEmpty(assembly)) {
            this.assemblyConfiguration = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
        } else {
            this.assemblyConfiguration = SpeciesUtils.getAssembly(speciesConfiguration, assembly);
        }
        if (assemblyConfiguration == null) {
            throw new CellBaseException("Invalid assembly: '" + assembly + "'");
        }
        this.ensemblVersion = assemblyConfiguration.getEnsemblVersion();
        this.ensemblRelease = "release-" + ensemblVersion.split("_")[0];

        // Prepare outdir
        Path speciesFolder = outdir.resolve(speciesShortName + "_" + assemblyConfiguration.getName().toLowerCase());
        downloadFolder = outdir.resolve(speciesFolder + "/download");
        logger.info("Creating download dir {}", downloadFolder);
        Files.createDirectories(downloadFolder);

        downloadLogFolder = outdir.resolve(speciesFolder + "/download/log");
        logger.info("Creating download log dir {}", downloadLogFolder);
        Files.createDirectories(downloadLogFolder);

        // <output>/<species>_<assembly>/generated_json
        buildFolder = outdir.resolve(speciesFolder + "/generated_json");
        logger.info("Creating build dir {}", buildFolder);
        Files.createDirectories(buildFolder);

        logger.info("Processing species {}", speciesConfiguration.getScientificName());
    }

    public abstract List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException;

    protected boolean speciesHasInfoToDownload(SpeciesConfiguration sp, String info) {
        boolean hasInfo = true;
        if (sp.getData() == null || !sp.getData().contains(info)) {
            logger.warn("Species '{}' has no '{}' information available to download", sp.getScientificName(), info);
            hasInfo = false;
        }
        return hasInfo;
    }

    protected DownloadFile downloadAndSaveDataSource(DownloadProperties.URLProperties props, String fileId, String data, Path outPath)
            throws IOException, InterruptedException, CellBaseException {
        return downloadAndSaveDataSource(props, fileId, data, null, outPath);
    }

    protected DownloadFile downloadAndSaveDataSource(DownloadProperties.URLProperties props, String fileId, String data, String chromosome,
                                                     Path outPath) throws IOException, InterruptedException, CellBaseException {
        String versionFilename = getDataVersionFilename(data);

        // Download file
        DownloadFile downloadFile = downloadDataSource(props, fileId, chromosome, outPath);

        // Save data source
        saveDataSource(data, props.getVersion(), getTimeStamp(), Collections.singletonList(downloadFile.getUrl()),
                outPath.resolve(versionFilename));

        return downloadFile;
    }

    protected DownloadFile downloadAndSaveEnsemblDataSource(DownloadProperties.EnsemblProperties ensemblProps, String fileId, String data,
                                                            Path outPath) throws IOException, InterruptedException, CellBaseException {
        return downloadAndSaveEnsemblDataSource(ensemblProps, fileId, data, null, outPath);
    }

    protected DownloadFile downloadAndSaveEnsemblDataSource(DownloadProperties.EnsemblProperties ensemblProps, String fileId, String data,
                                                            String chromosome, Path outPath)
            throws IOException, InterruptedException, CellBaseException {
        // Download file
        DownloadFile downloadFile = downloadEnsemblDataSource(ensemblProps, fileId, chromosome, outPath);

        // Save data source
        saveDataSource(data, "(" + getDataName(ENSEMBL_DATA) + " " + ensemblVersion + ")", getTimeStamp(),
                Collections.singletonList(downloadFile.getUrl()), outPath.resolve(getDataVersionFilename(data)));

        return downloadFile;
    }

    protected DownloadFile downloadDataSource(DownloadProperties.URLProperties props, String fileId, Path outPath)
            throws IOException, InterruptedException, CellBaseException {
        return downloadDataSource(props, fileId, null, outPath);
    }

    protected DownloadFile downloadDataSource(DownloadProperties.URLProperties props, String fileId,
                                              String chromosome, Path outPath)
            throws IOException, InterruptedException, CellBaseException {
        String url = EtlCommons.getUrl(props, fileId, species, assembly, chromosome);
        File outFile = outPath.resolve(getFilenameFromUrl(url)).toFile();
        logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outFile);
        DownloadFile downloadFile = downloadFile(url, outFile.toString());
        logger.info(OK_LOG_MESSAGE);
        return downloadFile;
    }

    protected DownloadFile downloadEnsemblDataSource(DownloadProperties.EnsemblProperties ensemblProps, String fileId, Path outPath)
            throws IOException, InterruptedException, CellBaseException {
        return downloadEnsemblDataSource(ensemblProps, fileId, null, outPath);
    }

    protected DownloadFile downloadEnsemblDataSource(DownloadProperties.EnsemblProperties ensemblProps, String fileId, String chromosome,
                                                     Path outPath) throws IOException, InterruptedException, CellBaseException {
        String url = EtlCommons.getEnsemblUrl(ensemblProps, ensemblRelease, fileId, speciesShortName, assemblyConfiguration.getName(),
                chromosome);
        File outFile = outPath.resolve(getFilenameFromUrl(url)).toFile();
        logger.info(DOWNLOADING_FROM_TO_LOG_MESSAGE, url, outFile);
        DownloadFile downloadFile = downloadFile(url, outFile.toString());
        logger.info(OK_LOG_MESSAGE);
        return downloadFile;
    }

    protected void saveDataSource(String data, String version, String date, List<String> urls, Path versionFilePath)
            throws IOException, CellBaseException {
        String name = getDataName(data);
        String category = getDataCategory(data);
        DataSource dataSource = new DataSource(name, category, version, date, urls);

        if (StringUtils.isEmpty(version)) {
            logger.warn("Version missing for data source {}/{}, using the date as version: {}", category, name, date);
            dataSource.setVersion(date);
        }

        dataSourceWriter.writeValue(versionFilePath.toFile(), dataSource);
        logger.info("Created the " + getDataName(data) + " version file " + versionFilePath.getFileName() + " at "
                + versionFilePath.getParent());
    }

    protected String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    protected String getLine(Path readmePath, int lineNumber) {
        Files.exists(readmePath);
        try (BufferedReader reader = Files.newBufferedReader(readmePath, Charset.defaultCharset())) {
            String line = null;
            for (int i = 0; i < lineNumber; i++) {
                line = reader.readLine();
            }
            return line;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected List<Map<String, String>> parseResult(String json) throws IOException {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        return reader.readValue(json);
    }

    protected String getPhylo(SpeciesConfiguration sp) {
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            return "vertebrates";
        } else if (configuration.getSpecies().getMetazoa().contains(sp)) {
            return "metazoa";
        } else if (configuration.getSpecies().getFungi().contains(sp)) {
            return "fungi";
        } else if (configuration.getSpecies().getProtist().contains(sp)) {
            return "protists";
        } else if (configuration.getSpecies().getPlants().contains(sp)) {
            return "plants";
        } else if (configuration.getSpecies().getVirus().contains(sp)) {
            return "virus";
        } else if (configuration.getSpecies().getBacteria().contains(sp)) {
            return "bacteria";
        } else {
            throw new ParameterException("Species " + sp.getScientificName() + " not associated to any phylo in the configuration file");
        }
    }

    protected DownloadFile downloadFile(String url, String outputFileName) throws IOException, InterruptedException, CellBaseException {
        return downloadFile(url, outputFileName, null);
    }

    protected DownloadFile downloadFile(String url, String outputFileName, List<String> wgetAdditionalArgs)
            throws IOException, InterruptedException, CellBaseException {
        DownloadFile downloadFileInfo = new DownloadFile(url, outputFileName, Timestamp.valueOf(LocalDateTime.now()).toString());
        Long startTime = System.currentTimeMillis();
        if (Paths.get(outputFileName).toFile().exists()) {
            logger.warn("File '{}' is already downloaded", outputFileName);
            setDownloadStatusAndMessage(outputFileName, downloadFileInfo, "File '" + outputFileName + "' is already downloaded", true);
        } else {
            final String outputLog = downloadLogFolder + "/" + Paths.get(outputFileName).toFile().getName() + ".log";
            List<String> wgetArgs = new ArrayList<>(Arrays.asList("--tries=10", url, "-O", outputFileName, "-o", outputLog));
            if (wgetAdditionalArgs != null && !wgetAdditionalArgs.isEmpty()) {
                wgetArgs.addAll(wgetAdditionalArgs);
            }
            boolean downloaded = EtlCommons.runCommandLineProcess(null, "wget", wgetArgs, outputLog);
            setDownloadStatusAndMessage(outputFileName, downloadFileInfo, outputLog, downloaded);
        }
        downloadFileInfo.setElapsedTime(startTime, System.currentTimeMillis());
        return downloadFileInfo;
    }

    private void setDownloadStatusAndMessage(String outputFileName, DownloadFile downloadFile, String outputLog, boolean downloaded) {
        if (downloaded) {
            boolean validFileSize = validateDownloadFile(downloadFile, outputFileName, outputLog);
            if (validFileSize) {
                downloadFile.setStatus(DownloadFile.Status.OK);
                downloadFile.setMessage("File downloaded successfully");
            } else {
                downloadFile.setStatus(DownloadFile.Status.ERROR);
                downloadFile.setMessage("Expected downloaded file size " + downloadFile.getExpectedFileSize()
                        + ", Actual file size " + downloadFile.getActualFileSize());
            }
        } else {
            downloadFile.setMessage("See full error message in " + outputLog);
            downloadFile.setStatus(DownloadFile.Status.ERROR);
            // because we use the -O flag, a file will be written, even on error. See #467
//            Files.deleteIfExists((new File(outputFileName)).toPath());
        }
    }

    public static void writeDownloadLogFile(Path downloadFolder, List<DownloadFile> downloadFiles) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File(downloadFolder + "/download_log.json"), downloadFiles);
    }

    private boolean validateDownloadFile(DownloadFile downloadFile, String outputFileName, String outputFileLog) {
        long expectedFileSize = getExpectedFileSize(outputFileLog);
        long actualFileSize = FileUtils.sizeOf(new File(outputFileName));
        downloadFile.setActualFileSize(actualFileSize);
        downloadFile.setExpectedFileSize(expectedFileSize);
        return expectedFileSize == actualFileSize;
    }

    private long getExpectedFileSize(String outputFileLog) {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFileLog))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // looking for: Length: 13846591 (13M)
                if (line.startsWith("Length:")) {
                    String[] parts = line.split("\\s");
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (Exception e) {
            logger.info("Error getting expected file size {}", e.getMessage());
        }
        return -1;
    }

    private String getEnsemblURL(SpeciesConfiguration sp) {
        // We need to find which is the correct Ensembl host URL.
        // This can different depending on if is a vertebrate species.
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            return configuration.getDownload().getEnsembl().getUrl().getHost();
        } else {
            return configuration.getDownload().getEnsemblGenomes().getUrl().getHost();
        }
    }
}


