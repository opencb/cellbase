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
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GeneDownloadManager extends DownloadManager {

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String UNIPROT_NAME = "UniProt";
    private static final String GERP_NAME = "GERP++";
    private static final String PHASTCONS_NAME = "PhastCons";
    private static final String PHYLOP_NAME = "PhyloP";
    private static final String GENE_EXPRESSION_ATLAS_NAME = "Gene Expression Atlas";
    private static final String HPO_NAME = "HPO";
    private static final String DISGENET_NAME = "DisGeNET";
    private static final String GO_ANNOTATION_NAME = "EBI Gene Ontology Annotation";
    private static final String DGIDB_NAME = "DGIdb";
    private static final String GNOMAD_NAME = "gnomAD";

    private static final HashMap GENE_UNIPROT_XREF_FILES = new HashMap() {
        {
            put("Homo sapiens", "HUMAN_9606_idmapping_selected.tab.gz");
            put("Mus musculus", "MOUSE_10090_idmapping_selected.tab.gz");
            put("Rattus norvegicus", "RAT_10116_idmapping_selected.tab.gz");
            put("Danio rerio", "DANRE_7955_idmapping_selected.tab.gz");
            put("Drosophila melanogaster", "DROME_7227_idmapping_selected.tab.gz");
            put("Saccharomyces cerevisiae", "YEAST_559292_idmapping_selected.tab.gz");
        }
    };

    public GeneDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    public GeneDownloadManager(CellBaseConfiguration configuration, Path targetDirectory, SpeciesConfiguration speciesConfiguration,
                               SpeciesConfiguration.Assembly assembly) throws IOException, CellbaseException {
        super(configuration, targetDirectory, speciesConfiguration, assembly);
    }


    public void downloadEnsemblGene()throws IOException, InterruptedException {
        logger.info("Downloading gene information ...");
        Path geneFolder = downloadFolder.resolve("gene");
        Files.createDirectories(geneFolder);

        downloadEnsemblData(geneFolder);
        downloadDrugData(geneFolder);
        downloadGeneUniprotXref(geneFolder);
        downloadGeneExpressionAtlas(geneFolder);
        downloadGeneDiseaseAnnotation(geneFolder);
        downloadGnomadConstraints(geneFolder);
        downloadGO(geneFolder);
        // FIXME
//        runGeneExtraInfo(geneFolder);
    }

    private void downloadGO(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading go annotation...");
            String url = configuration.getDownload().getGoAnnotation().getHost();
            downloadFile(url, geneFolder.resolve("goa_human.gaf.gz").toString());
            saveVersionData(EtlCommons.GENE_DATA, GO_ANNOTATION_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    geneFolder.resolve("goAnnotationVersion.json"));
        }
    }

    public void downloadObo() throws IOException, InterruptedException {
        logger.info("Downloading obo files ...");

        Path oboFolder = downloadFolder.resolve("obo");
        Files.createDirectories(oboFolder);

        String url = configuration.getDownload().getHpoObo().getHost();
        downloadFile(url, oboFolder.resolve("hp.obo").toString());

        url = configuration.getDownload().getGoObo().getHost();
        downloadFile(url, oboFolder.resolve("go-basic.obo").toString());
    }

    private void downloadGnomadConstraints(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading gnomAD constraints data...");
            String url = configuration.getDownload().getGnomadConstraints().getHost();
            downloadFile(url, geneFolder.resolve("gnomad.v2.1.1.lof_metrics.by_transcript.txt.bgz").toString());
            saveVersionData(EtlCommons.GENE_DATA, GNOMAD_NAME, configuration.getDownload().
                            getGnomadConstraints().getVersion(), getTimeStamp(),
                    Collections.singletonList(url), geneFolder.resolve("gnomadVersion.json"));
        }
    }
    private void downloadDrugData(Path geneFolder) throws IOException, InterruptedException {
        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading drug-gene data...");
            String url = configuration.getDownload().getDgidb().getHost();
            downloadFile(url, geneFolder.resolve("dgidb.tsv").toString());
            saveVersionData(EtlCommons.GENE_DATA, DGIDB_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    geneFolder.resolve("dgidbVersion.json"));
        }
    }

    private void downloadEnsemblData(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene Ensembl data (gtf, pep, cdna, motifs) ...");
        List<String> downloadedUrls = new ArrayList<>(4);

        String ensemblHost = ensemblHostUrl + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)) {
            ensemblHost = ensemblHostUrl + "/" + ensemblRelease + "/" + getPhylo(speciesConfiguration);
        }

        String bacteriaCollectionPath = "";
        if (configuration.getSpecies().getBacteria().contains(speciesConfiguration)) {
            // WARN: assuming there's just one assembly
            bacteriaCollectionPath =  speciesConfiguration.getAssemblies().get(0).getEnsemblCollection() + "/";
        }

        // Ensembl leaves now several GTF files in the FTP folder, we need to build a more accurate URL
        // to download the correct GTF file.
        String version = ensemblRelease.split("-")[1];
        String url = ensemblHost + "/gtf/" + bacteriaCollectionPath + speciesShortName + "/*" + version + ".gtf.gz";
        String fileName = geneFolder.resolve(speciesShortName + ".gtf.gz").toString();
        downloadFile(url, fileName);
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + bacteriaCollectionPath + speciesShortName + "/pep/*.pep.all.fa.gz";
        fileName = geneFolder.resolve(speciesShortName + ".pep.all.fa.gz").toString();
        downloadFile(url, fileName);
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + bacteriaCollectionPath + speciesShortName + "/cdna/*.cdna.all.fa.gz";
        fileName = geneFolder.resolve(speciesShortName + ".cdna.all.fa.gz").toString();
        downloadFile(url, fileName);
        downloadedUrls.add(url);
        saveVersionData(EtlCommons.GENE_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                geneFolder.resolve("ensemblCoreVersion.json"));
    }

    private String getUniProtReleaseNotesUrl() {
        return URI.create(configuration.getDownload().getGeneUniprotXref().getHost()).resolve("../../../").toString()
                + "/relnotes.txt";
    }

    private String getUniProtRelease(String relnotesFilename) {
        Path path = Paths.get(relnotesFilename);
        Files.exists(path);
        try {
            // The first line at the relnotes.txt file contains the UniProt release
            BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());
            String release = reader.readLine().split(" ")[2];
            reader.close();
            return  release;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void downloadGeneUniprotXref(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading UniProt ID mapping ...");

        if (GENE_UNIPROT_XREF_FILES.containsKey(speciesConfiguration.getScientificName())) {
            String geneGtfUrl = configuration.getDownload().getGeneUniprotXref().getHost() + "/"
                    + GENE_UNIPROT_XREF_FILES.get(speciesConfiguration.getScientificName());
            downloadFile(geneGtfUrl, geneFolder.resolve("idmapping_selected.tab.gz").toString());
            downloadFile(getUniProtReleaseNotesUrl(), geneFolder.resolve("uniprotRelnotes.txt").toString());

            saveVersionData(EtlCommons.GENE_DATA, UNIPROT_NAME,
                    getUniProtRelease(geneFolder.resolve("uniprotRelnotes.txt").toString()), getTimeStamp(),
                    Collections.singletonList(geneGtfUrl), geneFolder.resolve("uniprotXrefVersion.json"));
        }
    }

    private void downloadGeneExpressionAtlas(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene expression atlas ...");

        String geneGtfUrl = configuration.getDownload().getGeneExpressionAtlas().getHost();
        downloadFile(geneGtfUrl, geneFolder.resolve("allgenes_updown_in_organism_part.tab.gz").toString());

        saveVersionData(EtlCommons.GENE_DATA, GENE_EXPRESSION_ATLAS_NAME, getGeneExpressionAtlasVersion(), getTimeStamp(),
                Collections.singletonList(geneGtfUrl), geneFolder.resolve("geneExpressionAtlasVersion.json"));

    }

    private String getGeneExpressionAtlasVersion() {
        return FilenameUtils.getBaseName(configuration.getDownload().getGeneExpressionAtlas().getHost())
                .split("_")[5].replace(".tab", "");
    }

    private void downloadGeneDiseaseAnnotation(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene disease annotation ...");

        String host = configuration.getDownload().getHpo().getHost();
        String fileName = StringUtils.substringAfterLast(host, "/");
        downloadFile(host, geneFolder.resolve(fileName).toString());
        saveVersionData(EtlCommons.GENE_DATA, HPO_NAME, null, getTimeStamp(), Collections.singletonList(host),
                geneFolder.resolve("hpoVersion.json"));

        host = configuration.getDownload().getDisgenet().getHost();
        List<String> files = configuration.getDownload().getDisgenet().getFiles();
        for (String file : files) {
            String outputFile = file.equalsIgnoreCase("readme.txt") ? "disgenetReadme.txt" : file;
            downloadFile(host + "/" + file, geneFolder.resolve(outputFile).toString());
        }

        saveVersionData(EtlCommons.GENE_DISEASE_ASSOCIATION_DATA, DISGENET_NAME,
                getVersionFromVersionLine(geneFolder.resolve("disgenetReadme.txt"), "(version"), getTimeStamp(),
                Collections.singletonList(host), geneFolder.resolve("disgenetVersion.json"));
    }

    private void runGeneExtraInfo(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene extra info ...");

        String geneExtraInfoLogFile = geneFolder.resolve("gene_extra_info.log").toString();
        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("--species", speciesConfiguration.getScientificName(), "--assembly", assemblyConfiguration.getName(),
                "--outdir", geneFolder.toAbsolutePath().toString(),
                "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()));

        if (!configuration.getSpecies().getVertebrates().contains(speciesConfiguration)
                && !speciesConfiguration.getScientificName().equals("Drosophila melanogaster")) {
            args.add("--phylo");
            args.add("no-vertebrate");
        }

        File ensemblScriptsFolder = new File(System.getProperty("basedir") + "/bin/ensembl-scripts/");

        // run gene_extra_info.pl
        boolean geneExtraInfoDownloaded = EtlCommons.runCommandLineProcess(ensemblScriptsFolder,
                "./gene_extra_info.pl",
                args,
                geneExtraInfoLogFile);

        // check output
        if (geneExtraInfoDownloaded) {
            logger.info("Gene extra files created OK");
        } else {
            logger.error("Gene extra info for " + speciesConfiguration.getScientificName() + " cannot be downloaded");
        }
    }
}
