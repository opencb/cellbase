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

import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Downloader {

    private String species;
    private String assembly;
    private Path outputDirectory;
    private CellBaseConfiguration configuration;

    public Downloader(String species, String assembly, Path outputDirectory, CellBaseConfiguration configuration) {
        this.species = species;
        this.assembly = assembly;
        this.outputDirectory = outputDirectory;
        this.configuration = configuration;
    }

    public List<DownloadFile> downloadGenome() throws IOException, CellbaseException, InterruptedException {
        GenomeDownloadManager manager = new GenomeDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadGene() throws IOException, CellbaseException, InterruptedException {
        GeneDownloadManager manager = new GeneDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadRegulation() throws IOException, CellbaseException, InterruptedException,
            NoSuchMethodException, FileFormatException {
        RegulationDownloadManager manager = new RegulationDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadProtein() throws IOException, CellbaseException, InterruptedException {
        ProteinDownloadManager manager = new ProteinDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadConservation() throws IOException, CellbaseException, InterruptedException {
        GenomeDownloadManager manager = new GenomeDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.downloadConservation();
    }

    public List<DownloadFile> downloadClinicalVariants() throws IOException, CellbaseException, InterruptedException {
        ClinicalDownloadManager manager = new ClinicalDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadRepeats() throws IOException, CellbaseException, InterruptedException {
        GenomeDownloadManager manager = new GenomeDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.downloadRepeats();
    }

    public List<DownloadFile> downloadOntologies() throws IOException, CellbaseException, InterruptedException {
        OntologyDownloadManager manager = new OntologyDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadCaddScores() throws IOException, CellbaseException, InterruptedException {
        CaddDownloadManager manager = new CaddDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }

    public List<DownloadFile> downloadPredictionScores() throws IOException, CellbaseException, InterruptedException {
        PredictionScoresDownloadManager manager = new PredictionScoresDownloadManager(species, assembly, outputDirectory, configuration);
        return manager.download();
    }
}
