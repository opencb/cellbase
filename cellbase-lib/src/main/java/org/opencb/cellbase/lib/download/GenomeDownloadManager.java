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
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.DockerUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenomeDownloadManager extends AbstractDownloadManager {

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String GERP_NAME = "GERP++";
    private static final String PHASTCONS_NAME = "PhastCons";
    private static final String PHYLOP_NAME = "PhyloP";
    private static final String TRF_NAME = "Tandem repeats finder";
    private static final String GSD_NAME = "Genomic super duplications";
    private static final String WM_NAME = "WindowMasker";

    public GenomeDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        List<DownloadFile> downloadFiles = new ArrayList<>();
        downloadFiles.addAll(downloadReferenceGenome());
        downloadFiles.addAll(downloadConservation());
        downloadFiles.addAll(downloadRepeats());

        // cytobands
        runGenomeInfo();
        return downloadFiles;
    }

    public List<DownloadFile> downloadReferenceGenome() throws IOException, InterruptedException {
        logger.info("Downloading genome information ...");
        Path sequenceFolder = downloadFolder.resolve("genome");
        Files.createDirectories(sequenceFolder);

        // Reference genome sequences are downloaded from Ensembl
        // New Homo sapiens assemblies contain too many ALT regions, so we download 'primary_assembly' file instead
        String url = ensemblHostUrl + "/" + ensemblRelease;
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            url = url + "/fasta/" + speciesShortName + "/dna/*.dna.primary_assembly.fa.gz";
        } else {
            if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
                url = ensemblHostUrl + "/" + ensemblRelease + "/" + getPhylo(speciesConfiguration);
            }
            url = url + "/fasta/";
            if (configuration.getSpecies().getBacteria().contains(speciesConfiguration)) {
                // WARN: assuming there's just one assembly
                url = url + speciesConfiguration.getAssemblies().get(0).getEnsemblCollection() + "/";
            }
            url = url + speciesShortName + "/dna/*.dna.toplevel.fa.gz";
        }

        String outputFileName = StringUtils.capitalize(speciesShortName) + "." + assemblyConfiguration.getName() + ".fa.gz";
        Path outputPath = sequenceFolder.resolve(outputFileName);
        logger.info("Saving reference genome version data at {}", sequenceFolder.resolve("genomeVersion.json"));
        saveVersionData(EtlCommons.GENOME_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(),
                Collections.singletonList(url), sequenceFolder.resolve("genomeVersion.json"));
        List<DownloadFile> downloadFiles = Collections.singletonList(downloadFile(url, outputPath.toString()));
        logger.info("Unzipping file: " + outputFileName);
        EtlCommons.runCommandLineProcess(null, "gunzip", Collections.singletonList(outputPath.toString()), null);
        return downloadFiles;
    }

    /**
     * This method downloads bith PhastCons and PhyloP data from UCSC for Human and Mouse species.
     * @return list of files downloaded
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files
     */
    public List<DownloadFile> downloadConservation() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "conservation")) {
            return null;
        }
        logger.info("Downloading conservation information ...");
        Path conservationFolder = downloadFolder.resolve("conservation");
        List<DownloadFile> downloadFiles = new ArrayList<>();
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            Files.createDirectories(conservationFolder);
            Files.createDirectories(conservationFolder.resolve("gerp"));
            Files.createDirectories(conservationFolder.resolve("phastCons"));
            Files.createDirectories(conservationFolder.resolve("phylop"));

            String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M", };

            if (assemblyConfiguration.getName().equalsIgnoreCase("GRCh38")) {
                String url = configuration.getDownload().getConservation().getHost() + "/hg38";
                List<String> phastconsUrls = new ArrayList<>(chromosomes.length);
                List<String> phyloPUrls = new ArrayList<>(chromosomes.length);
                for (String chromosome : chromosomes) {
                    String phastConsUrl = url + "/phastCons100way/hg38.100way.phastCons/chr" + chromosome
                            + ".phastCons100way.wigFix.gz";
                    downloadFiles.add(downloadFile(phastConsUrl, conservationFolder.resolve("phastCons")
                            .resolve("chr" + chromosome + ".phastCons100way.wigFix.gz").toString()));
                    phastconsUrls.add(phastConsUrl);

                    String phyloPUrl = url + "/phyloP100way/hg38.100way.phyloP100way/chr" + chromosome
                            + ".phyloP100way.wigFix.gz";
                    downloadFiles.add(downloadFile(phyloPUrl, conservationFolder.resolve("phylop")
                            .resolve("chr" + chromosome + ".phyloP100way.wigFix.gz").toString()));
                    phyloPUrls.add(phyloPUrl);
                }
                String gerpUrl = configuration.getDownload().getGerp().getHost();
                downloadFiles.add(downloadFile(gerpUrl, conservationFolder.resolve(EtlCommons.GERP_SUBDIRECTORY)
                        .resolve(EtlCommons.GERP_FILE).toString()));

                saveVersionData(EtlCommons.CONSERVATION_DATA, GERP_NAME, null, getTimeStamp(), Collections.singletonList(gerpUrl),
                        conservationFolder.resolve("gerpVersion.json"));
                saveVersionData(EtlCommons.CONSERVATION_DATA, PHASTCONS_NAME, null, getTimeStamp(), phastconsUrls,
                        conservationFolder.resolve("phastConsVersion.json"));
                saveVersionData(EtlCommons.CONSERVATION_DATA, PHYLOP_NAME, null, getTimeStamp(), phyloPUrls,
                        conservationFolder.resolve("phyloPVersion.json"));
            }
        }

        if (speciesConfiguration.getScientificName().equals("Mus musculus")) {
            Files.createDirectories(conservationFolder);
            Files.createDirectories(conservationFolder.resolve("phastCons"));
            Files.createDirectories(conservationFolder.resolve("phylop"));

            String url = configuration.getDownload().getConservation().getHost() + "/mm10";
            String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "X", "Y", "M", };
            List<String> phastconsUrls = new ArrayList<>(chromosomes.length);
            List<String> phyloPUrls = new ArrayList<>(chromosomes.length);
            for (String chromosome : chromosomes) {
                String phastConsUrl = url + "/phastCons60way/mm10.60way.phastCons/chr" + chromosome + ".phastCons60way.wigFix.gz";
                downloadFiles.add(downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosome
                        + ".phastCons60way.wigFix.gz").toString()));
                phastconsUrls.add(phastConsUrl);
                String phyloPUrl = url + "/phyloP60way/mm10.60way.phyloP60way/chr" + chromosome + ".phyloP60way.wigFix.gz";
                downloadFiles.add(downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosome
                        + ".phyloP60way.wigFix.gz").toString()));
                phyloPUrls.add(phyloPUrl);
            }
            saveVersionData(EtlCommons.CONSERVATION_DATA, PHASTCONS_NAME, null, getTimeStamp(), phastconsUrls,
                    conservationFolder.resolve("phastConsVersion.json"));
            saveVersionData(EtlCommons.CONSERVATION_DATA, PHYLOP_NAME, null, getTimeStamp(), phyloPUrls,
                    conservationFolder.resolve("phastConsVersion.json"));
        }
        return downloadFiles;
    }

    public List<DownloadFile> downloadRepeats() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "repeats")) {
            return null;
        }
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading repeats data ...");
            Path repeatsFolder = downloadFolder.resolve(EtlCommons.REPEATS_FOLDER);
            Files.createDirectories(repeatsFolder);
            List<DownloadFile> downloadFiles = new ArrayList<>();
            String pathParam;
            if (assemblyConfiguration.getName().equalsIgnoreCase("grch38")) {
                pathParam = "hg38";
            } else {
                logger.error("Please provide a valid human assembly {GRCh37, GRCh38)");
                throw new ParameterException("Assembly '" + assemblyConfiguration.getName() + "' is not valid. Please provide "
                        + "a valid human assembly {GRCh37, GRCh38)");
            }

            // Download tandem repeat finder
            String url = configuration.getDownload().getSimpleRepeats().getHost() + "/" + pathParam
                    + "/database/simpleRepeat.txt.gz";
            downloadFiles.add(downloadFile(url, repeatsFolder.resolve(EtlCommons.TRF_FILE).toString()));
            saveVersionData(EtlCommons.REPEATS_DATA, TRF_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    repeatsFolder.resolve(EtlCommons.TRF_VERSION_FILE));

            // Download genomic super duplications
            url = configuration.getDownload().getGenomicSuperDups().getHost() + "/" + pathParam
                    + "/database/genomicSuperDups.txt.gz";
            downloadFiles.add(downloadFile(url, repeatsFolder.resolve(EtlCommons.GSD_FILE).toString()));
            saveVersionData(EtlCommons.REPEATS_DATA, GSD_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    repeatsFolder.resolve(EtlCommons.GSD_VERSION_FILE));

            // Download WindowMasker
            if (!pathParam.equalsIgnoreCase("hg19")) {
                url = configuration.getDownload().getWindowMasker().getHost() + "/" + pathParam
                        + "/database/windowmaskerSdust.txt.gz";
                downloadFiles.add(downloadFile(url, repeatsFolder.resolve(EtlCommons.WM_FILE).toString()));
                saveVersionData(EtlCommons.REPEATS_DATA, WM_NAME, null, getTimeStamp(), Collections.singletonList(url),
                        repeatsFolder.resolve(EtlCommons.WM_VERSION_FILE));
            }
            return downloadFiles;
        }
        return null;
    }

    public void runGenomeInfo() throws IOException {
        logger.info("Downloading genome info ...");

        // TODO don't run this if file already exists
        String dockerImage = "opencb/cellbase-builder:" + configuration.getApiVersion();
        String outputFolder = downloadFolder.getParent().toAbsolutePath().toString() + "/generated_json/";

        AbstractMap.SimpleEntry<String, String> outputBinding = new AbstractMap.SimpleEntry(outputFolder, "/ensembl-data");
        String ensemblScriptParams = "/opt/cellbase/genome_info.pl";

        try {
            DockerUtils.run(dockerImage, null, outputBinding, ensemblScriptParams, null);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
