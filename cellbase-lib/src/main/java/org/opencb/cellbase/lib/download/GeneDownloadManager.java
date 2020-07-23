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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.utils.DockerUtils;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GeneDownloadManager extends AbstractDownloadManager {

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String UNIPROT_NAME = "UniProt";
//    private static final String GERP_NAME = "GERP++";
//    private static final String PHASTCONS_NAME = "PhastCons";
//    private static final String PHYLOP_NAME = "PhyloP";
    private static final String GENE_EXPRESSION_ATLAS_NAME = "Gene Expression Atlas";
    private static final String HPO_NAME = "HPO";
    private static final String DISGENET_NAME = "DisGeNET";
    private static final String GO_ANNOTATION_NAME = "EBI Gene Ontology Annotation";
    private static final String DGIDB_NAME = "DGIdb";
    private static final String GNOMAD_NAME = "gnomAD";
    private static String dockerImage;

    private static final Map<String, String> GENE_UNIPROT_XREF_FILES;

    static {
        GENE_UNIPROT_XREF_FILES = new HashMap<>();
        GENE_UNIPROT_XREF_FILES.put("Homo sapiens", "HUMAN_9606_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Mus musculus", "MOUSE_10090_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Rattus norvegicus", "RAT_10116_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Danio rerio", "DANRE_7955_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Drosophila melanogaster", "DROME_7227_idmapping_selected.tab.gz");
        GENE_UNIPROT_XREF_FILES.put("Saccharomyces cerevisiae", "YEAST_559292_idmapping_selected.tab.gz");
    };

    public GeneDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, targetDirectory, configuration);

        dockerImage = "opencb/cellbase-builder:" + configuration.getApiVersion();
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        logger.info("Downloading gene information ...");
        Path geneFolder = downloadFolder.resolve("gene");
        Files.createDirectories(geneFolder);

        Path refseqFolder = downloadFolder.resolve("refseq");
        Files.createDirectories(refseqFolder);

        List<DownloadFile> downloadFiles = new ArrayList<>();

        downloadFiles.addAll(downloadEnsemblData(geneFolder));
        downloadFiles.add(downloadDrugData(geneFolder));
        downloadFiles.addAll(downloadGeneUniprotXref(geneFolder));
        downloadFiles.add(downloadGeneExpressionAtlas(geneFolder));
        downloadFiles.addAll(downloadGeneDiseaseAnnotation(geneFolder));
        downloadFiles.add(downloadGnomadConstraints(geneFolder));
        downloadFiles.add(downloadGO(geneFolder));
        downloadFiles.addAll(downloadRefSeq(refseqFolder));
        runGeneExtraInfo(geneFolder);

        return downloadFiles;
    }

    private List<DownloadFile> downloadRefSeq(Path refSeqFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading RefSeq...");

            List<DownloadFile> downloadFiles = new ArrayList<>();

            String url = configuration.getDownload().getRefSeq().getHost();
            saveVersionData(EtlCommons.REFSEQ_DATA, "RefSeq", null, getTimeStamp(), Collections.singletonList(url),
                    refSeqFolder.resolve("refSeqVersion.json"));
            downloadFiles.add(downloadFile(url, refSeqFolder.resolve("refSeq.gtf.gz").toString()));


            url = configuration.getDownload().getRefSeqFasta().getHost();
            String outputFileName = StringUtils.capitalize(speciesShortName) + "." + assemblyConfiguration.getName() + ".fna.gz";
            logger.info("downloading " + url);
            Path outputPath = refSeqFolder.resolve(outputFileName);
            saveVersionData(EtlCommons.REFSEQ_DATA, "RefSeq", null, getTimeStamp(),
                    Collections.singletonList(url), refSeqFolder.resolve("refSeqFastaVersion.json"));
            downloadFiles.add(downloadFile(url, outputPath.toString()));
            logger.info("Unzipping file: " + outputFileName);
            EtlCommons.runCommandLineProcess(null, "gunzip", Collections.singletonList(outputPath.toString()), null);
            return downloadFiles;

        }
        return null;
    }

    private DownloadFile downloadGO(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading go annotation...");
            String url = configuration.getDownload().getGoAnnotation().getHost();
            saveVersionData(EtlCommons.GENE_DATA, GO_ANNOTATION_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    geneFolder.resolve("goAnnotationVersion.json"));
            return downloadFile(url, geneFolder.resolve("goa_human.gaf.gz").toString());
        }
        return null;
    }

    private DownloadFile downloadGnomadConstraints(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading gnomAD constraints data...");
            String url = configuration.getDownload().getGnomadConstraints().getHost();
            saveVersionData(EtlCommons.GENE_DATA, GNOMAD_NAME, configuration.getDownload().
                            getGnomadConstraints().getVersion(), getTimeStamp(),
                    Collections.singletonList(url), geneFolder.resolve("gnomadVersion.json"));
            return downloadFile(url, geneFolder.resolve("gnomad.v2.1.1.lof_metrics.by_transcript.txt.bgz").toString());
        }
        return null;
    }

    private DownloadFile downloadDrugData(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading drug-gene data...");
            String url = configuration.getDownload().getDgidb().getHost();
            saveVersionData(EtlCommons.GENE_DATA, DGIDB_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    geneFolder.resolve("dgidbVersion.json"));
            return downloadFile(url, geneFolder.resolve("dgidb.tsv").toString());
        }
        return null;
    }

    private List<DownloadFile> downloadEnsemblData(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene Ensembl data (gtf, pep, cdna, motifs) ...");
        List<String> downloadedUrls = new ArrayList<>(4);
        List<DownloadFile> downloadFiles = new ArrayList<>();

        String ensemblHost = ensemblHostUrl + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
            ensemblHost = ensemblHostUrl + "/" + ensemblRelease + "/" + getPhylo(speciesConfiguration);
        }

        String ensemblCollection = "";
        if (configuration.getSpecies().getBacteria().contains(speciesConfiguration)) {
            // WARN: assuming there's just one assembly
            ensemblCollection =  speciesConfiguration.getAssemblies().get(0).getEnsemblCollection() + "/";
        }

        // Ensembl leaves now several GTF files in the FTP folder, we need to build a more accurate URL
        // to download the correct GTF file.
        String version = ensemblRelease.split("-")[1];
        String url = ensemblHost + "/gtf/" + ensemblCollection + speciesShortName + "/*" + version + ".gtf.gz";
        String fileName = geneFolder.resolve(speciesShortName + ".gtf.gz").toString();
        downloadFiles.add(downloadFile(url, fileName));
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + ensemblCollection + speciesShortName + "/pep/*.pep.all.fa.gz";
        fileName = geneFolder.resolve(speciesShortName + ".pep.all.fa.gz").toString();
        downloadFiles.add(downloadFile(url, fileName));
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + ensemblCollection + speciesShortName + "/cdna/*.cdna.all.fa.gz";
        fileName = geneFolder.resolve(speciesShortName + ".cdna.all.fa.gz").toString();
        downloadFiles.add(downloadFile(url, fileName));
        downloadedUrls.add(url);

        saveVersionData(EtlCommons.GENE_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                geneFolder.resolve("ensemblCoreVersion.json"));

        return downloadFiles;
    }

    private String getUniProtReleaseNotesUrl() {
        return URI.create(configuration.getDownload().getGeneUniprotXref().getHost()).resolve("../../../").toString()
                + "/relnotes.txt";
    }

    private String getUniProtRelease(String relnotesFilename) throws IOException {
        Path path = Paths.get(relnotesFilename);
        FileUtils.checkFile(path);
        // The first line at the relnotes.txt file contains the UniProt release
        BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());
        String release = reader.readLine().split(" ")[2];
        reader.close();
        return release;
    }

    private List<DownloadFile> downloadGeneUniprotXref(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading UniProt ID mapping ...");

        List<DownloadFile> downloadFiles = new ArrayList<>();

        if (GENE_UNIPROT_XREF_FILES.containsKey(speciesConfiguration.getScientificName())) {
            String geneGtfUrl = configuration.getDownload().getGeneUniprotXref().getHost() + "/"
                    + GENE_UNIPROT_XREF_FILES.get(speciesConfiguration.getScientificName());
            downloadFiles.add(downloadFile(geneGtfUrl, geneFolder.resolve("idmapping_selected.tab.gz").toString()));
            downloadFiles.add(downloadFile(getUniProtReleaseNotesUrl(), geneFolder.resolve("uniprotRelnotes.txt").toString()));

            saveVersionData(EtlCommons.GENE_DATA, UNIPROT_NAME,
                    getUniProtRelease(geneFolder.resolve("uniprotRelnotes.txt").toString()), getTimeStamp(),
                    Collections.singletonList(geneGtfUrl), geneFolder.resolve("uniprotXrefVersion.json"));
        }

        return downloadFiles;
    }

    private DownloadFile downloadGeneExpressionAtlas(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene expression atlas ...");
        String geneGtfUrl = configuration.getDownload().getGeneExpressionAtlas().getHost();
        saveVersionData(EtlCommons.GENE_DATA, GENE_EXPRESSION_ATLAS_NAME, getGeneExpressionAtlasVersion(), getTimeStamp(),
                Collections.singletonList(geneGtfUrl), geneFolder.resolve("geneExpressionAtlasVersion.json"));
        return downloadFile(geneGtfUrl, geneFolder.resolve("allgenes_updown_in_organism_part.tab.gz").toString());
    }

    private String getGeneExpressionAtlasVersion() {
        return FilenameUtils.getBaseName(configuration.getDownload().getGeneExpressionAtlas().getHost())
                .split("_")[5].replace(".tab", "");
    }

    private List<DownloadFile> downloadGeneDiseaseAnnotation(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene disease annotation ...");

        List<DownloadFile> downloadFiles = new ArrayList<>();

        String host = configuration.getDownload().getHpo().getHost();
        String fileName = StringUtils.substringAfterLast(host, "/");
        downloadFiles.add(downloadFile(host, geneFolder.resolve(fileName).toString()));
        saveVersionData(EtlCommons.GENE_DATA, HPO_NAME, null, getTimeStamp(), Collections.singletonList(host),
                geneFolder.resolve("hpoVersion.json"));

        host = configuration.getDownload().getDisgenet().getHost();
        List<String> files = configuration.getDownload().getDisgenet().getFiles();
        for (String file : files) {
            String outputFile = file.equalsIgnoreCase("readme.txt") ? "disgenetReadme.txt" : file;
            downloadFiles.add(downloadFile(host + "/" + file, geneFolder.resolve(outputFile).toString()));
        }

        saveVersionData(EtlCommons.GENE_DISEASE_ASSOCIATION_DATA, DISGENET_NAME,
                getVersionFromVersionLine(geneFolder.resolve("disgenetReadme.txt"), "(version"), getTimeStamp(),
                Collections.singletonList(host), geneFolder.resolve("disgenetVersion.json"));

        return downloadFiles;
    }

    private void runGeneExtraInfo(Path geneFolder) throws IOException {
        // TODO skip if we already have these data
        logger.info("Downloading gene extra info ...");

        AbstractMap.SimpleEntry<String, String> outputBinding = new AbstractMap.SimpleEntry(geneFolder.toAbsolutePath().toString(),
                "/ensembl-data");
        String ensemblScriptParams = "/opt/cellbase/gene_extra_info.pl --outdir /ensembl-data";

        try {
            DockerUtils.run(dockerImage, null, outputBinding, ensemblScriptParams, null);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
