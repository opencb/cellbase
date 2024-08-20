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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClinicalDownloadManager extends AbstractDownloadManager {

    private static final String CLINVAR_NAME = "ClinVar";
    private static final String GWAS_NAME = "GWAS catalog";
    private static final String IARCTP53_NAME = "IARC TP53 Database";


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
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading clinical variant information ...");

            String url;
            List<DownloadFile> downloadFiles = new ArrayList<>();

            Path clinicalFolder = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANTS_FOLDER);
            Files.createDirectories(clinicalFolder);
            logger.info("\t\tDownloading ClinVar files ...");

            List<String> clinvarUrls = new ArrayList<>(3);
            url = configuration.getDownload().getClinvar().getHost();

            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_XML_FILE).toString()));
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarEfoTerms().getHost();
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_EFO_FILE).toString()));
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarSummary().getHost();
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_SUMMARY_FILE).toString()));
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarVariationAllele().getHost();
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_VARIATION_ALLELE_FILE).toString()));
            clinvarUrls.add(url);
            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, CLINVAR_NAME, configuration.getDownload().getClinvar()
                            .getVersion(), getTimeStamp(), clinvarUrls, clinicalFolder.resolve("clinvarVersion.json"));

            logger.info("\t\tDone");

            // Gwas catalog
            logger.info("\t\tDownloading GWAS catalog file ...");
            DownloadProperties.URLProperties gwasCatalog = configuration.getDownload().getGwasCatalog();
            url = gwasCatalog.getHost();
            downloadFiles.add(downloadFile(url, clinicalFolder.resolve(EtlCommons.GWAS_FILE).toString()));
            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, GWAS_NAME, gwasCatalog.getVersion(), getTimeStamp(),
                    Collections.singletonList(url), clinicalFolder.resolve("gwasVersion.json"));
            logger.info("\t\tDone");

//            List<String> hgvsList = getDocmHgvsList();
//            if (!hgvsList.isEmpty()) {
//                downloadDocm(hgvsList, clinicalFolder.resolve(EtlCommons.DOCM_FILE));
//                downloadFiles.add(downloadFile(configuration.getDownload().getDocmVersion().getHost(),
//                        clinicalFolder.resolve("docmIndex.html").toString()));
//                saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, EtlCommons.DOCM_NAME,
//                        getDocmVersion(clinicalFolder.resolve("docmIndex.html")), getTimeStamp(),
//                        Arrays.asList(configuration.getDownload().getDocm().getHost() + "v1/variants.json",
//                                configuration.getDownload().getDocm().getHost() + "v1/variants/{hgvs}.json"),
//                        clinicalFolder.resolve("docmVersion.json"));
//            } else {
//                logger.warn("No DOCM variants found for assembly {}. Please double-check that this is the correct "
//                        + "assembly", assemblyConfiguration.getName());
//            }

            // I am only able to download these files manually
//            if (assemblyConfiguration.getName().equalsIgnoreCase("grch38")) {
//                url = configuration.getDownload().getIarctp53().getHost();
//                downloadFiles.add(downloadFile(url, clinicalFolder.resolve(EtlCommons.IARCTP53_FILE).toString()));
//
//                ZipFile zipFile = new ZipFile(clinicalFolder.resolve(EtlCommons.IARCTP53_FILE).toString());
//                Enumeration<? extends ZipEntry> entries = zipFile.entries();
//                while (entries.hasMoreElements()) {
//                    ZipEntry entry = entries.nextElement();
//                    File entryDestination = new File(clinicalFolder.toFile(), entry.getName());
//                    if (entry.isDirectory()) {
//                        entryDestination.mkdirs();
//                    } else {
//                        entryDestination.getParentFile().mkdirs();
//                        InputStream in = zipFile.getInputStream(entry);
//                        OutputStream out = new FileOutputStream(entryDestination);
//                        IOUtils.copy(in, out);
//                        IOUtils.closeQuietly(in);
//                        out.close();
//                    }
//                }
//                saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, IARCTP53_NAME,
//                        getVersionFromVersionLine(clinicalFolder.resolve("Disclaimer.txt"),
//                                "The version of the database should be identified"), getTimeStamp(),
//                        Collections.singletonList(url), clinicalFolder.resolve("iarctp53Version.json"));
//            }

            if (Files.notExists(clinicalFolder.resolve("clinvar_chunks"))) {
                Files.createDirectories(clinicalFolder.resolve("clinvar_chunks"));
                splitClinvar(clinicalFolder.resolve(EtlCommons.CLINVAR_XML_FILE), clinicalFolder.resolve("clinvar_chunks"));
            }

            return downloadFiles;
        }
        return null;
    }

    private void splitClinvar(Path clinvarXmlFilePath, Path splitOutdirPath) throws IOException {
        BufferedReader br = FileUtils.newBufferedReader(clinvarXmlFilePath);
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
                    pw.print("</ReleaseSet>");
                    pw.close();
                    chunk++;
                }
            }
        }
        pw.print("</ReleaseSet>");
        pw.close();
        br.close();
    }

    private String getDocmVersion(Path docmIndexHtml) {
        return getVersionFromVersionLine(docmIndexHtml, "<select name=\"version\" id=\"version\"");
    }

    private void downloadDocm(List<String> hgvsList, Path path) throws IOException, InterruptedException {
        BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
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
        logger.info("Finished. {} DOCM variants saved at {}", counter, path.toString());
        bufferedWriter.close();
    }

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
