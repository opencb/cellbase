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
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProteinDownloadManager extends DownloadManager {

    private static final String UNIPROT_NAME = "UniProt";

    public ProteinDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    public ProteinDownloadManager(CellBaseConfiguration configuration, Path targetDirectory, SpeciesConfiguration speciesConfiguration,
                                  SpeciesConfiguration.Assembly assembly) throws IOException, CellbaseException {
        super(configuration, targetDirectory, speciesConfiguration, assembly);
    }

    /**
     * This method downloads UniProt, IntAct and Interpro data from EMBL-EBI.
     *
     * @return list of files downloaded
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files     *
     */
    public List<DownloadFile> downloadProtein() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "protein")) {
            return null;
        }
        logger.info("Downloading protein information ...");
        Path proteinFolder = downloadFolder.resolve("protein");
        Files.createDirectories(proteinFolder);
        List<DownloadFile> downloadFiles = new ArrayList<>();

        String url = configuration.getDownload().getUniprot().getHost();
        downloadFiles.add(downloadFile(url, proteinFolder.resolve("uniprot_sprot.xml.gz").toString()));
        Files.createDirectories(proteinFolder.resolve("uniprot_chunks"));
        splitUniprot(proteinFolder.resolve("uniprot_sprot.xml.gz"), proteinFolder.resolve("uniprot_chunks"));

        String relNotesUrl = configuration.getDownload().getUniprotRelNotes().getHost();
        downloadFiles.add(downloadFile(relNotesUrl, proteinFolder.resolve("uniprotRelnotes.txt").toString()));

        saveVersionData(EtlCommons.PROTEIN_DATA, UNIPROT_NAME, getLine(proteinFolder.resolve("uniprotRelnotes.txt"), 1),
                getTimeStamp(), Collections.singletonList(url), proteinFolder.resolve("uniprotVersion.json"));

        return downloadFiles;

//        url = configuration.getDownload().getIntact().getHost();
//        downloadFile(url, proteinFolder.resolve("intact.txt").toString());
//        saveVersionData(EtlCommons.PROTEIN_DATA, INTACT_NAME, null, getTimeStamp(), Collections.singletonList(url),
//                proteinFolder.resolve("intactVersion.json"));
//
//        url = configuration.getDownload().getInterpro().getHost();
//        downloadFile(url, proteinFolder.resolve("protein2ipr.dat.gz").toString());
//        relNotesUrl = configuration.getDownload().getInterproRelNotes().getHost();
//        downloadFile(relNotesUrl, proteinFolder.resolve("interproRelnotes.txt").toString());
//        saveVersionData(EtlCommons.PROTEIN_DATA, INTERPRO_NAME, getLine(proteinFolder.resolve("interproRelnotes.txt"), 5),
//                getTimeStamp(), Collections.singletonList(url), proteinFolder.resolve("interproVersion.json"));
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
