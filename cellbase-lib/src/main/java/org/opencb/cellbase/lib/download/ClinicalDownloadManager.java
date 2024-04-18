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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ClinicalDownloadManager extends AbstractDownloadManager {

    public ClinicalDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        List<DownloadFile> downloadFiles = new ArrayList<>();
        downloadFiles.addAll(downloadClinical());
        return downloadFiles;
    }

    public List<DownloadFile> downloadClinical() throws IOException, InterruptedException, CellBaseException {
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            Path clinicalFolder = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANTS_SUBDIRECTORY).toAbsolutePath();
            Files.createDirectories(clinicalFolder);
            logger.info("Downloading clinical information at {} ...", clinicalFolder);

            String url;
            List<String> urls;
            Path outPath;
            DownloadProperties.URLProperties props;

            DownloadFile downloadFile;
            List<DownloadFile> downloadFiles = new ArrayList<>();

            // COSMIC
            logger.warn("{} files must be downloaded manually !", COSMIC_NAME);
            props = configuration.getDownload().getCosmic();
            urls = Collections.singletonList(props.getHost() + props.getFiles().get(COSMIC_FILE_ID));
            // Save data source
            saveDataSource(EtlCommons.CLINICAL_VARIANTS_DATA, COSMIC_NAME, props.getVersion(), getTimeStamp(), urls,
                    clinicalFolder.resolve(COSMIC_VERSION_FILENAME));

            // HGMD
            logger.warn("{} files must be downloaded manually !", HGMD_NAME);
            props = configuration.getDownload().getHgmd();
            urls = Collections.singletonList(props.getHost() + props.getFiles().get(HGMD_FILE_ID));
            // Save data source
            saveDataSource(EtlCommons.CLINICAL_VARIANTS_DATA, HGMD_NAME, props.getVersion(), getTimeStamp(), urls,
                    clinicalFolder.resolve(HGMD_VERSION_FILENAME));

            // GWAS catalog
            downloadFile = downloadAndSaveDataSource(configuration.getDownload().getGwasCatalog(), GWAS_FILE_ID, GWAS_NAME,
                    CLINICAL_VARIANTS_DATA, GWAS_VERSION_FILENAME, clinicalFolder);
            downloadFiles.add(downloadFile);

            // ClinVar
            logger.info("Downloading {}} files ...", CLINVAR_NAME);
            props = configuration.getDownload().getClinvar();
            urls = new ArrayList<>();
            for (String fileId : Arrays.asList(CLINVAR_FULL_RELEASE_FILE_ID, CLINVAR_SUMMARY_FILE_ID, CLINVAR_ALLELE_FILE_ID,
                    CLINVAR_EFO_TERMS_FILE_ID)) {
                url = props.getHost() + props.getFiles().get(fileId);
                outPath = clinicalFolder.resolve(getFilenameFromUrl(url));
                logger.info(DOWNLOADING_LOG_MESSAGE, url, outPath);
                downloadFiles.add(downloadFile(url, outPath.toString()));
                urls.add(url);
            }
            // Save data source
            saveDataSource(EtlCommons.CLINICAL_VARIANTS_DATA, CLINVAR_NAME, props.getVersion(), getTimeStamp(), urls,
                    clinicalFolder.resolve(CLINVAR_VERSION_FILENAME));

            // Prepare CliVar chunk files
            Path chunksPath = clinicalFolder.resolve(CLINVAR_CHUNKS_SUBDIRECTORY);
            if (Files.notExists(chunksPath)) {
                Files.createDirectories(chunksPath);
                Path clinvarPath = clinicalFolder.resolve(getFilenameFromUrl(
                        props.getHost() + props.getFiles().get(CLINVAR_FULL_RELEASE_FILE_ID)));
                logger.info("Splitting {} in {} ...", clinvarPath, chunksPath);
                splitClinvar(clinvarPath, chunksPath);
            }

            return downloadFiles;
        }
        return Collections.emptyList();
    }

    private void splitClinvar(Path clinvarXmlFilePath, Path splitOutdirPath) throws IOException {
        PrintWriter pw = null;
        try (BufferedReader br = FileUtils.newBufferedReader(clinvarXmlFilePath)) {
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
}
