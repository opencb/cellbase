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
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.core.SpeciesUtils;
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

@Deprecated
public class DownloadManager {


    private static final String CADD_NAME = "CADD";
    private static final String DGV_NAME = "DGV";
//    private static final String GWAS_NAME = "Gwas Catalog";
//    private static final String DBSNP_NAME = "dbSNP";
//    private static final String REACTOME_NAME = "Reactome";

    private static final String GNOMAD_NAME = "gnomAD";

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
    protected Path buildFolder; // <output>/<species>_<assembly>/generated-json
    protected Logger logger;

    public DownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        this.species = species;
        this.assembly = assembly;
        this.outdir = outdir;
        this.configuration = configuration;

        this.init();
    }

    @Deprecated
    public DownloadManager(CellBaseConfiguration configuration, Path targetDirectory, SpeciesConfiguration speciesConfiguration,
                           SpeciesConfiguration.Assembly assembly) throws IOException {
        logger = LoggerFactory.getLogger(this.getClass());

        this.configuration = configuration;
        this.speciesConfiguration = speciesConfiguration;
//        assemblyName = assembly.getName();

        // Output folder creation
        speciesShortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
        // <output>/<species>_<assembly>
        Path speciesFolder = targetDirectory.resolve(speciesShortName + "_" + assembly.getName().toLowerCase());
        // <output>/<species>_<assembly>/download
        downloadFolder = targetDirectory.resolve(speciesFolder + "/download");
        makeDir(downloadFolder);

        ensemblHostUrl = getEnsemblURL(speciesConfiguration);
        ensemblVersion = assembly.getEnsemblVersion();
        ensemblRelease = "release-" + ensemblVersion.split("_")[0];
    }

    private void init() throws CellbaseException, IOException {
        logger = LoggerFactory.getLogger(this.getClass());

        // Check Species
        this.speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, species);
        if (speciesConfiguration == null) {
            throw new CellbaseException("Invalid species: '" + species + "'");
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
            throw new CellbaseException("Invalid assembly: '" + assembly + "'");
        }
        this.ensemblVersion = assemblyConfiguration.getEnsemblVersion();
        this.ensemblRelease = "release-" + ensemblVersion.split("_")[0];

        // Prepare outdir
        Path speciesFolder = outdir.resolve(speciesShortName + "_" + assemblyConfiguration.getName().toLowerCase());
        downloadFolder = outdir.resolve(speciesFolder + "/download");
        Files.createDirectories(downloadFolder);

        // <output>/<species>_<assembly>/generated_json
        buildFolder = outdir.resolve(speciesFolder + "/generated_json");
        Files.createDirectories(buildFolder);

        logger.info("Processing species " + speciesConfiguration.getScientificName());
    }

    @Deprecated
    public DownloadFile downloadStructuralVariants() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "svs")) {
             return null;
        }
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading DGV data ...");

            Path structuralVariantsFolder = downloadFolder.resolve(EtlCommons.STRUCTURAL_VARIANTS_FOLDER);
            Files.createDirectories(structuralVariantsFolder);
            String sourceFilename = (assemblyConfiguration.getName().equalsIgnoreCase("grch37") ? "GRCh37_hg19" : "GRCh38_hg38")
                    + "_variants_2016-05-15.txt";
            String url = configuration.getDownload().getDgv().getHost() + "/" + sourceFilename;
            saveVersionData(EtlCommons.STRUCTURAL_VARIANTS_DATA, DGV_NAME, getDGVVersion(sourceFilename), getTimeStamp(),
                    Collections.singletonList(url), buildFolder.resolve(EtlCommons.DGV_VERSION_FILE));
            return downloadFile(url, structuralVariantsFolder.resolve(EtlCommons.DGV_FILE).toString());
        }
        return null;
    }

    private String getDGVVersion(String sourceFilename) {
        return sourceFilename.split("\\.")[0].split("_")[3];
    }

    protected boolean speciesHasInfoToDownload(SpeciesConfiguration sp, String info) {
        boolean hasInfo = true;
        if (sp.getData() == null || !sp.getData().contains(info)) {
            logger.warn("Species '{}' has no '{}' information available to download", sp.getScientificName(), info);
            hasInfo = false;
        }
        return hasInfo;
    }

    protected String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    protected void saveVersionData(String data, String source, String version, String date, List<String> url, Path outputFilePath)
            throws IOException {
        Map<String, Object> versionDataMap = new HashMap<>();
        versionDataMap.put("data", data);
        versionDataMap.put("source", source);
        versionDataMap.put("version", version);
        versionDataMap.put("downloadDate", date);
        versionDataMap.put("uRL", url);

        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.writeValue(outputFilePath.toFile(), versionDataMap);
    }

    protected String getLine(Path readmePath, int lineNumber) {
        Files.exists(readmePath);
        try {
            BufferedReader reader = Files.newBufferedReader(readmePath, Charset.defaultCharset());
            String line = null;
            for (int i = 0; i < lineNumber; i++) {
                line = reader.readLine();
            }
            reader.close();
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

    public DownloadFile downloadCaddScores() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "variation_functional_score")) {
            return null;
        }
        if (speciesConfiguration.getScientificName().equals("Homo sapiens") && assemblyConfiguration.getName().equalsIgnoreCase("GRCh37")) {
            logger.info("Downloading CADD scores information ...");

            Path variationFunctionalScoreFolder = downloadFolder.resolve("variation_functional_score");
            Files.createDirectories(variationFunctionalScoreFolder);

            // Downloads CADD scores
            String url = configuration.getDownload().getCadd().getHost();

            saveVersionData(EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, CADD_NAME, url.split("/")[5], getTimeStamp(),
                    Collections.singletonList(url), buildFolder.resolve("caddVersion.json"));
            return downloadFile(url, variationFunctionalScoreFolder.resolve("whole_genome_SNVs.tsv.gz").toString());
        }
        return null;
    }

    protected DownloadFile downloadFile(String url, String outputFileName) throws IOException, InterruptedException {
        return downloadFile(url, outputFileName, null);
    }

//    protected void downloadFiles(String host, List<String> fileNames) throws IOException, InterruptedException {
//        downloadFiles(host, fileNames, fileNames);
//    }

//    protected void downloadFiles(String host, List<String> fileNames, List<String> ouputFileNames)
//        throws IOException, InterruptedException {
//        for (int i = 0; i < fileNames.size(); i++) {
//            downloadFile(host + "/" + fileNames.get(i), ouputFileNames.get(i), null);
//        }
//    }

    protected DownloadFile downloadFile(String url, String outputFileName, List<String> wgetAdditionalArgs)
            throws IOException, InterruptedException {
        DownloadFile downloadFileInfo = new DownloadFile(url, outputFileName, Timestamp.valueOf(LocalDateTime.now()).toString());
        Long startTime = System.currentTimeMillis();
        if (Paths.get(outputFileName).toFile().exists()) {
            logger.warn("File '{}' is already downloaded", outputFileName);
            setDownloadStatusAndMessage(outputFileName, downloadFileInfo, "File '" + outputFileName + "' is already downloaded", true);
        } else {
            final String outputLog = outputFileName + ".log";
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

    public void writeDownloadLogFile(List<DownloadFile> downloadFiles) throws IOException {
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

    private int getExpectedFileSize(String outputFileLog) {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFileLog))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // looking for: Length: 13846591 (13M)
                if (line.startsWith("Length:")) {
                    String[] parts = line.split("\\s");
                    return Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return 0;
    }

    protected String getVersionFromVersionLine(Path path, String tag) {
        Files.exists(path);
        try {
            BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());
            String line = reader.readLine();
            // There shall be a line at the README.txt containing the version.
            // e.g. The files in the current directory contain the data corresponding to the latest release
            // (version 4.0, April 2016). ...
            while (line != null) {
                // tag specifies a certain string that must be found within the line supposed to contain the version
                // info
                if (line.contains(tag)) {
                    String version = line.split("\\(")[1].split("\\)")[0];
                    reader.close();
                    return version;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    private void makeDir(Path folderPath) throws IOException {
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

    @Deprecated
    private String getEnsemblURL(SpeciesConfiguration sp) {
        // We need to find which is the correct Ensembl host URL.
        // This can different depending on if is a vertebrate species.
        String ensemblHostUrl;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            ensemblHostUrl = configuration.getDownload().getEnsembl().getUrl().getHost();
        } else {
            ensemblHostUrl = configuration.getDownload().getEnsemblGenomes().getUrl().getHost();
        }
        return ensemblHostUrl;
    }
}


