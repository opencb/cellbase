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
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ClinicalDownloadManager extends AbstractDownloadManager {

    public ClinicalDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        List<DownloadFile> downloadFiles = new ArrayList<>();
        downloadFiles.addAll(downloadClinical());
        return downloadFiles;
    }

    public List<DownloadFile> downloadClinical() throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {

            logger.info("Downloading clinical information ...");

            String url;
            String filename;
            List<DownloadFile> downloadFiles = new ArrayList<>();


            Path clinicalFolder = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANTS_FOLDER).toAbsolutePath();
            Files.createDirectories(clinicalFolder);

            // COSMIC
            logger.info("\t\tCOSMIC files must be downloaded manually !");
            List<String> cosmicUrls = new ArrayList<>();
            cosmicUrls.add(configuration.getDownload().getCosmic().getHost());
            cosmicUrls.addAll(configuration.getDownload().getCosmic().getFiles());
            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, COSMIC_NAME, configuration.getDownload().getCosmic().getVersion(),
                    getTimeStamp(), cosmicUrls, clinicalFolder.resolve(COSMIC_VERSION_FILENAME));

            // HGMD
            logger.info("\t\tHGMD files must be downloaded manually !");
            List<String> hgmdUrls = new ArrayList<>();
            hgmdUrls.add(configuration.getDownload().getHgmd().getHost());
            hgmdUrls.addAll(configuration.getDownload().getHgmd().getFiles());
            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, HGMD_NAME, configuration.getDownload().getHgmd().getVersion(),
                    getTimeStamp(), hgmdUrls, clinicalFolder.resolve(HGMD_VERSION_FILENAME));

            // ClinVar
            logger.info("\t\tDownloading ClinVar files ...");
            List<String> clinvarUrls = new ArrayList<>(3);
            url = configuration.getDownload().getClinvar().getHost();
            filename = Paths.get(url).getFileName().toString();
            logger.info("\t\tDownloading {} to {} ...", url, clinicalFolder.resolve(filename));
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(filename).toString()));
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarEfoTerms().getHost();
            filename = Paths.get(url).getFileName().toString();
            logger.info("\t\tDownloading {} to {} ...", url, clinicalFolder.resolve(filename));
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(filename).toString()));
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarSummary().getHost();
            filename = Paths.get(url).getFileName().toString();
            logger.info("\t\tDownloading {} to {} ...", url, clinicalFolder.resolve(filename));
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(filename).toString()));
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarVariationAllele().getHost();
            filename = Paths.get(url).getFileName().toString();
            logger.info("\t\tDownloading {} to {} ...", url, clinicalFolder.resolve(filename));
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(filename).toString()));
            clinvarUrls.add(url);

            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, CLINVAR_NAME, configuration.getDownload().getClinvar().getVersion(),
                    getTimeStamp(), clinvarUrls, clinicalFolder.resolve(CLINVAR_VERSION_FILENAME));

            // Gwas catalog
            logger.info("\t\tDownloading GWAS catalog file ...");
            DownloadProperties.URLProperties gwasCatalog = configuration.getDownload().getGwasCatalog();
            url = gwasCatalog.getHost();
            filename = Paths.get(url).getFileName().toString();
            logger.info("\t\tDownloading {} to {} ...", url, clinicalFolder.resolve(filename));
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(filename).toString()));
            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, GWAS_NAME, gwasCatalog.getVersion(), getTimeStamp(),
                    Collections.singletonList(url), clinicalFolder.resolve(GWAS_VERSION_FILENAME));

            final String chunkDir = "clinvar_chunks";
            if (Files.notExists(clinicalFolder.resolve(chunkDir))) {
                Files.createDirectories(clinicalFolder.resolve(chunkDir));
                filename = Paths.get(configuration.getDownload().getClinvar().getHost()).getFileName().toString();
                logger.info("\t\tSplitting {} int {} ...", clinicalFolder.resolve(filename), clinicalFolder.resolve(chunkDir));
                splitClinvar(clinicalFolder.resolve(filename), clinicalFolder.resolve(chunkDir));
            }

            return downloadFiles;
        }
        return Collections.emptyList();
    }

    private void splitClinvar(Path clinvarXmlFilePath, Path splitOutdirPath) throws IOException {
        try (BufferedReader br = FileUtils.newBufferedReader(clinvarXmlFilePath)) {
            PrintWriter pw = null;
            StringBuilder header = new StringBuilder();
            boolean beforeEntry = true;
            boolean inEntry = false;
            int count = 0;
            int chunk = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("<ClinVarSet ")) {
                    inEntry = true;
                    beforeEntry = false;
                    if (count % 10000 == 0) {
                        pw = new PrintWriter(new FileOutputStream(splitOutdirPath.resolve("chunk_" + chunk + ".xml").toFile()));
                        pw.println(header.toString().trim());
                    }
                    count++;
                }

                if (beforeEntry) {
                    header.append(line).append("\n");
                }

                if (inEntry) {
                    pw.println(line);
                }

                if (line.trim().startsWith("</ClinVarSet>")) {
                    inEntry = false;
                    if (count % 10000 == 0) {
                        if (pw != null) {
                            pw.print("</ReleaseSet>");
                            pw.close();
                        }
                        chunk++;
                    }
                }
            }
            if (pw != null) {
                pw.print("</ReleaseSet>");
                pw.close();
            }
        }
    }

    /**
     * @deprecated
     * @param docmIndexHtml
     * @return
     */
    @Deprecated
    private String getDocmVersion(Path docmIndexHtml) {
        return getVersionFromVersionLine(docmIndexHtml, "<select name=\"version\" id=\"version\"");
    }

    /**
     * @deprecated
     * @param hgvsList
     * @param path
     * @throws IOException
     * @throws InterruptedException
     */
    @Deprecated
    private void downloadDocm(List<String> hgvsList, Path path) throws IOException, InterruptedException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            Client client = ClientBuilder.newClient();
            WebTarget restUrlBase = client
                    .target(URI.create(configuration.getDownload().getDocm().getHost() + "v1/variants"));

            logger.info("Querying DOCM REST API to get detailed data for all their variants");
            int counter = 0;
            for (String hgvs : hgvsList) {
                WebTarget callUrl = restUrlBase.path(hgvs + ".json");
                String jsonString = callUrl.request().get(String.class);
                bufferedWriter.write(jsonString + "\n");

                if (counter % 10 == 0) {
                    logger.info("{} DOCM variants saved", counter);
                }
                // Wait 1/3 of a second to avoid saturating their REST server - also avoid getting banned
                Thread.sleep(300);

                counter++;
            }
            logger.info("Finished. {} DOCM variants saved at {}", counter, path);
        }
    }

    /**
     * @deprecated
     * @return
     * @throws IOException
     */
    @Deprecated
    private List<String> getDocmHgvsList() throws IOException {
        Client client = ClientBuilder.newClient();
        WebTarget restUrl = client
                .target(URI.create(configuration.getDownload().getDocm().getHost() + "v1/variants.json"));

        String jsonString;
        logger.info("Getting full list of DOCM hgvs from: {}", restUrl.getUri().toURL());
        jsonString = restUrl.request().get(String.class);

        List<Map<String, String>> responseMap = parseResult(jsonString);
        List<String> hgvsList = new ArrayList<>(responseMap.size());
        for (Map<String, String> document : responseMap) {
            if (document.containsKey("reference_version")
                    && document.get("reference_version").equalsIgnoreCase(assemblyConfiguration.getName())) {
                hgvsList.add(document.get("hgvs"));
            }
        }
        logger.info("{} hgvs found", hgvsList.size());

        return hgvsList;
    }
}
