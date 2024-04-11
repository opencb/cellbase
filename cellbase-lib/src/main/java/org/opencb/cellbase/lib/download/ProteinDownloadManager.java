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
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ProteinDownloadManager extends AbstractDownloadManager {

    public ProteinDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    /**
     * This method downloads UniProt, IntAct and Interpro data from EMBL-EBI.
     *
     * @return list of files downloaded
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files     *
     */
    public List<DownloadFile> download() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, PROTEIN_DATA)) {
            return null;
        }
        Path proteinFolder = downloadFolder.resolve(PROTEIN_SUBDIRECTORY);
        Files.createDirectories(proteinFolder);
        logger.info("Downloading protein information at {} ...");

        DownloadFile downloadFile;
        List<DownloadFile> downloadFiles = new ArrayList<>();

        // Uniprot
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getUniprot(), UNIPROT_NAME, PROTEIN_DATA, UNIPROT_FILE_ID,
                UNIPROT_VERSION_FILENAME, proteinFolder);
        Path chunksPath = proteinFolder.resolve(UNIPROT_CHUNKS_SUBDIRECTORY);
        String uniprotFilename = getUrlFilename(configuration.getDownload().getUniprot().getFiles().get(UNIPROT_FILE_ID));
        logger.info("Split UniProt file {} into chunks at {}", uniprotFilename, chunksPath);
        Files.createDirectories(chunksPath);
        splitUniprot(proteinFolder.resolve(uniprotFilename), chunksPath);
        downloadFiles.add(downloadFile);

        // Interpro
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getInterpro(), INTERPRO_NAME, PROTEIN_DATA, INTERPRO_FILE_ID,
                INTERPRO_VERSION_FILENAME, proteinFolder);
        downloadFiles.add(downloadFile);

        // Intact
        downloadFile = downloadAndSaveDataSource(configuration.getDownload().getIntact(), INTACT_NAME, PROTEIN_DATA, INTACT_FILE_ID,
                INTACT_VERSION_FILENAME, proteinFolder);
        downloadFiles.add(downloadFile);

        return downloadFiles;
    }

    private void splitUniprot(Path uniprotFilePath, Path splitOutdirPath) throws IOException {
        BufferedReader br = FileUtils.newBufferedReader(uniprotFilePath);
        PrintWriter pw = null;
        StringBuilder header = new StringBuilder();
        boolean beforeEntry = true;
        boolean inEntry = false;
        int count = 0;
        int chunk = 0;
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("<entry ")) {
                inEntry = true;
                beforeEntry = false;
                if (count % 10000 == 0) {
                    pw = new PrintWriter(Files.newOutputStream(splitOutdirPath.resolve("chunk_" + chunk + ".xml").toFile().toPath()));
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

            if (line.trim().startsWith("</entry>")) {
                inEntry = false;
                if (count % 10000 == 0) {
                    pw.print("</uniprot>");
                    pw.close();
                    chunk++;
                }
            }
        }
        pw.print("</uniprot>");
        pw.close();
        br.close();
    }
}
