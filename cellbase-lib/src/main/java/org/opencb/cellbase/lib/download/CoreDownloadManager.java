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
import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Deprecated
public class CoreDownloadManager extends DownloadManager {

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String UNIPROT_NAME = "UniProt";
    private static final String INTACT_NAME = "IntAct";
    private static final String INTERPRO_NAME = "InterPro";
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

    public CoreDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellbaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    public CoreDownloadManager(CellBaseConfiguration configuration, Path targetDirectory, SpeciesConfiguration speciesConfiguration,
                               SpeciesConfiguration.Assembly assembly) throws IOException, CellbaseException {
        super(configuration, targetDirectory, speciesConfiguration, assembly);
    }

    public void downloadReferenceGenome() throws IOException, InterruptedException {
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
        downloadFile(url, outputPath.toString());
        logger.info("Saving reference genome version data at {}", sequenceFolder.resolve("genomeVersion.json"));
        saveVersionData(EtlCommons.GENOME_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(),
                Collections.singletonList(url), sequenceFolder.resolve("genomeVersion.json"));
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

        //ftp://ftp.ensembl.org/pub/release-99/regulation/homo_sapiens/MotifFeatures/Homo_sapiens.GRCh38.motif_features.gff.gz
//        url = ensemblHost + "/regulation/" + speciesShortName + "/MotifFeatures/*.motif_features.gff.gz";
//        Path outputFile = geneFolder.resolve("motif_features.gff.gz");
//        downloadFile(url, outputFile.toString());
//        downloadedUrls.add(url);


        saveVersionData(EtlCommons.GENE_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                geneFolder.resolve("ensemblCoreVersion.json"));
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

    private String getUniProtReleaseNotesUrl() {
        return URI.create(configuration.getDownload().getGeneUniprotXref().getHost()).resolve("../../../").toString()
                + "/relnotes.txt";
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

    /**
     * This method downloads bith PhastCons and PhyloP data from UCSC for Human and Mouse species.

     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files
     */
    public void downloadConservation() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "conservation")) {
            return;
        }
        logger.info("Downloading conservation information ...");
        Path conservationFolder = downloadFolder.resolve("conservation");

        if (speciesConfiguration.getScientificName().equals("Homo sapiens")) {
            Files.createDirectories(conservationFolder);
            Files.createDirectories(conservationFolder.resolve("phastCons"));
            Files.createDirectories(conservationFolder.resolve("phylop"));
            Files.createDirectories(conservationFolder.resolve("gerp"));

            String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M", };

            if (assemblyConfiguration.getName().equalsIgnoreCase("GRCh37")) {
                logger.debug("Downloading GERP++ ...");
                downloadFile(configuration.getDownload().getGerp().getHost(),
                        conservationFolder.resolve(EtlCommons.GERP_SUBDIRECTORY + "/" + EtlCommons.GERP_FILE).toAbsolutePath().toString());
                saveVersionData(EtlCommons.CONSERVATION_DATA, GERP_NAME, null, getTimeStamp(),
                        Collections.singletonList(configuration.getDownload().getGerp().getHost()),
                        conservationFolder.resolve("gerpVersion.json"));

                String url = configuration.getDownload().getConservation().getHost() + "/hg19";
                List<String> phastconsUrls = new ArrayList<>(chromosomes.length);
                List<String> phyloPUrls = new ArrayList<>(chromosomes.length);
                for (int i = 0; i < chromosomes.length; i++) {
                    String phastConsUrl = url + "/phastCons46way/primates/chr" + chromosomes[i] + ".phastCons46way.primates.wigFix.gz";
                    downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosomes[i]
                            + ".phastCons46way.primates.wigFix.gz").toString());
                    phastconsUrls.add(phastConsUrl);

                    String phyloPUrl = url + "/phyloP46way/primates/chr" + chromosomes[i] + ".phyloP46way.primate.wigFix.gz";
                    downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosomes[i]
                            + ".phyloP46way.primate.wigFix.gz").toString());
                    phyloPUrls.add(phyloPUrl);
                }
                saveVersionData(EtlCommons.CONSERVATION_DATA, PHASTCONS_NAME, null, getTimeStamp(), phastconsUrls,
                        conservationFolder.resolve("phastConsVersion.json"));
                saveVersionData(EtlCommons.CONSERVATION_DATA, PHYLOP_NAME, null, getTimeStamp(), phyloPUrls,
                        conservationFolder.resolve("phyloPVersion.json"));
            }

            if (assemblyConfiguration.getName().equalsIgnoreCase("GRCh38")) {
                String url = configuration.getDownload().getConservation().getHost() + "/hg38";
                List<String> phastconsUrls = new ArrayList<>(chromosomes.length);
                List<String> phyloPUrls = new ArrayList<>(chromosomes.length);
                for (int i = 0; i < chromosomes.length; i++) {
                    String phastConsUrl = url + "/phastCons100way/hg38.100way.phastCons/chr" + chromosomes[i]
                            + ".phastCons100way.wigFix.gz";
                    downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosomes[i]
                            + ".phastCons100way.wigFix.gz").toString());
                    phastconsUrls.add(phastConsUrl);

                    String phyloPUrl = url + "/phyloP100way/hg38.100way.phyloP100way/chr" + chromosomes[i] + ".phyloP100way.wigFix.gz";
                    downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosomes[i]
                            + ".phyloP100way.wigFix.gz").toString());
                    phyloPUrls.add(phyloPUrl);
                }
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
            for (int i = 0; i < chromosomes.length; i++) {
                String phastConsUrl = url + "/phastCons60way/mm10.60way.phastCons/chr" + chromosomes[i] + ".phastCons60way.wigFix.gz";
                downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosomes[i]
                        + ".phastCons60way.wigFix.gz").toString());
                phastconsUrls.add(phastConsUrl);
                String phyloPUrl = url + "/phyloP60way/mm10.60way.phyloP60way/chr" + chromosomes[i] + ".phyloP60way.wigFix.gz";
                downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosomes[i]
                        + ".phyloP60way.wigFix.gz").toString());
                phyloPUrls.add(phyloPUrl);
            }
            saveVersionData(EtlCommons.CONSERVATION_DATA, PHASTCONS_NAME, null, getTimeStamp(), phastconsUrls,
                    conservationFolder.resolve("phastConsVersion.json"));
            saveVersionData(EtlCommons.CONSERVATION_DATA, PHYLOP_NAME, null, getTimeStamp(), phyloPUrls,
                    conservationFolder.resolve("phastConsVersion.json"));
        }
    }


    /**
     * This method downloads UniProt, IntAct and Interpro data from EMBL-EBI.
     *
     * @throws IOException if there is an error writing to a file
     * @throws InterruptedException if there is an error downloading files
     */
    public void downloadProtein() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, "protein")) {
            return;
        }
        logger.info("Downloading protein information ...");
        Path proteinFolder = downloadFolder.resolve("protein");
        Files.createDirectories(proteinFolder);

        String url = configuration.getDownload().getUniprot().getHost();
        downloadFile(url, proteinFolder.resolve("uniprot_sprot.xml.gz").toString());
        String relNotesUrl = configuration.getDownload().getUniprotRelNotes().getHost();
        downloadFile(relNotesUrl, proteinFolder.resolve("uniprotRelnotes.txt").toString());
        Files.createDirectories(proteinFolder.resolve("uniprot_chunks"));
        splitUniprot(proteinFolder.resolve("uniprot_sprot.xml.gz"), proteinFolder.resolve("uniprot_chunks"));
        saveVersionData(EtlCommons.PROTEIN_DATA, UNIPROT_NAME, getLine(proteinFolder.resolve("uniprotRelnotes.txt"), 1),
                getTimeStamp(), Collections.singletonList(url), proteinFolder.resolve("uniprotVersion.json"));

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
