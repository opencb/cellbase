/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.CellBaseConfiguration.SpeciesProperties.Species;
import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandExecutor extends CommandExecutor {

    private CliOptionsParser.DownloadCommandOptions downloadCommandOptions;

    private Path output = null;
    private Path common = null;

    private File ensemblScriptsFolder;
    private String ensemblVersion;
    private String ensemblRelease;

    private Species species;

    private static final String[] VARIATION_FILES = {"variation.txt.gz", "variation_feature.txt.gz",
            "transcript_variation.txt.gz", "variation_synonym.txt.gz", "seq_region.txt.gz", "source.txt.gz",
            "attrib.txt.gz", "attrib_type.txt.gz", "seq_region.txt.gz", "structural_variation_feature.txt.gz",
            "study.txt.gz", "phenotype.txt.gz", "phenotype_feature.txt.gz", "phenotype_feature_attrib.txt.gz",
            "motif_feature_variation.txt.gz", "genotype_code.txt.gz", "allele_code.txt.gz",
            "population_genotype.txt.gz", "population.txt.gz", "allele.txt.gz", };


    private static final String[] REGULATION_FILES = {"AnnotatedFeatures.gff.gz", "MotifFeatures.gff.gz",
            "RegulatoryFeatures_MultiCell.gff.gz", };

    private static final Map<String, String> GENE_UNIPROT_XREF_FILES = new HashMap() {
        {
            put("Homo sapiens", "HUMAN_9606_idmapping_selected.tab.gz");
            put("Mus musculus", "MOUSE_10090_idmapping_selected.tab.gz");
            put("Rattus norvegicus", "RAT_10116_idmapping_selected.tab.gz");
            put("Danio rerio", "DANRE_7955_idmapping_selected.tab.gz");
            put("Drosophila melanogaster", "DROME_7227_idmapping_selected.tab.gz");
            put("Saccharomyces cerevisiae", "YEAST_559292_idmapping_selected.tab.gz");
        }
    };

    public DownloadCommandExecutor(CliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.verbose,
                downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;

        if (downloadCommandOptions.output != null) {
            output = Paths.get(downloadCommandOptions.output);
        }
        if (downloadCommandOptions.common != null) {
            common = Paths.get(downloadCommandOptions.common);
        } else {
            common = output.resolve("common");
        }

        this.ensemblScriptsFolder = new File(System.getProperty("basedir") + "/bin/ensembl-scripts/");
    }


    /**
     * Execute specific 'download' command options.
     */
    public void execute() {
        try {
            if (downloadCommandOptions.species != null && !downloadCommandOptions.species.isEmpty()) {
                // We need to get the Species object from the CLI name
                // This can be the scientific or common name, or the ID
                //            Species speciesToDownload = null;
                for (Species sp : configuration.getAllSpecies()) {
                    if (downloadCommandOptions.species.equalsIgnoreCase(sp.getScientificName())
                            || downloadCommandOptions.species.equalsIgnoreCase(sp.getCommonName())
                            || downloadCommandOptions.species.equalsIgnoreCase(sp.getId())) {
                        species = sp;
                        break;
                    }
                }

                // If everything is right we launch the download
                if (species != null) {
                    processSpecies(species);
                } else {
                    logger.error("Species '{}' not valid", downloadCommandOptions.species);
                }
            } else {
                logger.error("--species parameter '{}' not valid", downloadCommandOptions.species);
            }
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.error("Error downloading '" + downloadCommandOptions.species + "' files: " + e.getMessage());
        }

    }

    private void processSpecies(Species sp) throws IOException, InterruptedException {
        logger.info("Processing species " + sp.getScientificName());

        // We need to find which is the correct Ensembl host URL.
        // This can different depending on if is a vertebrate species.
        String ensemblHostUrl;
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            ensemblHostUrl = configuration.getDownload().getEnsembl().getUrl().getHost();
        } else {
            ensemblHostUrl = configuration.getDownload().getEnsemblGenomes().getUrl().getHost();
        }

        // Getting the assembly.
        // By default the first assembly in the configuration.json
        Species.Assembly assembly = null;
        if (downloadCommandOptions.assembly == null || downloadCommandOptions.assembly.isEmpty()) {
            assembly = sp.getAssemblies().get(0);
        } else {
            for (Species.Assembly assembly1 : sp.getAssemblies()) {
                if (downloadCommandOptions.assembly.equalsIgnoreCase(assembly1.getName())) {
                    assembly = assembly1;
                    break;
                }
            }
        }

        // Checking that the species and assembly are correct
        if (ensemblHostUrl == null || assembly == null) {
            logger.error("Something is not correct, check the species '{}' or the assembly '{}'",
                    downloadCommandOptions.species, downloadCommandOptions.assembly);
            return;
        }

        // Output folder creation
        String spShortName = sp.getScientificName().toLowerCase()
                .replaceAll("\\.", "")
                .replaceAll("\\)", "")
                .replaceAll("\\(", "")
                .replaceAll("[-/]", " ")
                .replaceAll("\\s+", "_");
        String spAssembly = assembly.getName().toLowerCase();
        Path spFolder = output.resolve(spShortName + "_" + spAssembly);
        makeDir(spFolder);
        makeDir(common);

        ensemblVersion = assembly.getEnsemblVersion();
        ensemblRelease = "release-" + ensemblVersion.split("_")[0];

        if (downloadCommandOptions.data != null && !downloadCommandOptions.data.isEmpty()) {
            List<String> dataList;
            if (downloadCommandOptions.data.equals("all")) {
                dataList = sp.getData();
            } else {
                dataList = Arrays.asList(downloadCommandOptions.data.split(","));
            }

            for (String data : dataList) {
                switch (data) {
                    case "genome":
                        downloadReferenceGenome(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
                        break;
                    case "gene":
                        downloadEnsemblGene(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
                        break;
                    case "gene_disease_association":
                        if (speciesHasInfoToDownload(sp, "gene_disease_association")) {
                            downloadGeneDiseaseAssociation(sp, spFolder);
                        }
                        break;
                    case "variation":
                        if (speciesHasInfoToDownload(sp, "variation")) {
                            downloadVariation(sp, spShortName, spFolder, ensemblHostUrl);
                        }
                        break;
                    case "variation_functional_score":
                        if (speciesHasInfoToDownload(sp, "variation_functional_score")) {
                            downloadCaddScores(sp, assembly.getName(), spFolder);
                        }
                        break;
                    case "regulation":
                        if (speciesHasInfoToDownload(sp, "regulation")) {
                            downloadRegulation(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
                        }
                        break;
                    case "protein":
                        if (speciesHasInfoToDownload(sp, "protein")) {
                            downloadProtein();
                        }
                        break;
                    case "conservation":
                        if (speciesHasInfoToDownload(sp, "conservation")) {
                            downloadConservation(sp, assembly.getName(), spFolder);
                        }
                        break;
                    case "clinical":
                        if (speciesHasInfoToDownload(sp, "clinical")) {
                            downloadClinical(sp, spFolder);
                        }
                        break;
                    default:
                        System.out.println("This data parameter is not allowed");
                        break;
                }
            }
        }
    }


    private boolean speciesHasInfoToDownload(Species sp, String info) {
        boolean hasInfo = true;
        if (sp.getData() == null || !sp.getData().contains(info)) {
            logger.warn("Species '{}' has no '{}' information available to download", sp.getScientificName(), info);
            hasInfo = false;
        }
        return hasInfo;
    }

    private String getPhylo(Species sp) {
        if (configuration.getSpecies().getVertebrates().contains(sp)) {
            return "vertebrates";
        } else if (configuration.getSpecies().getMetazoa().contains(sp)) {
            return "metazoa";
        } else if (configuration.getSpecies().getFungi().contains(sp)) {
            return "fungi";
        } else if (configuration.getSpecies().getProtist().contains(sp)) {
            return "protists";
        } else if (configuration.getSpecies().getPlants().contains(sp)) {
            return "plants";
        } else {
            throw new ParameterException("Species " + sp.getScientificName() + " not associated to any phylo in the configuration file");
        }
    }

    private void downloadReferenceGenome(Species sp, String shortName, String assembly, Path spFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading genome information ...");
        Path sequenceFolder = spFolder.resolve("genome");
        makeDir(sequenceFolder);

        /**
         * Reference genome sequences are downloaded from Ensembl
         */
        String url = host + "/" + ensemblRelease;
        if (sp.getScientificName().equals("Homo sapiens")) {
            // New Homo sapiens assemblies contain too many ALT regions,
            // so we download 'primary_assembly' file
            url = url + "/fasta/" + shortName + "/dna/*.dna.primary_assembly.fa.gz";
        } else {
            if (!configuration.getSpecies().getVertebrates().contains(sp)) {
                url = host + "/" + ensemblRelease + "/" + getPhylo(sp);
            }
            url = url + "/fasta/" + shortName + "/dna/*.dna.toplevel.fa.gz";
        }

        String outputFileName = StringUtils.capitalize(shortName) + "." + assembly + ".fa.gz";
        Path outputPath = sequenceFolder.resolve(outputFileName);
        downloadFile(url, outputPath.toString());
    }

    private void downloadEnsemblGene(Species sp, String spShortName, String assembly, Path speciesFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading gene information ...");
        Path geneFolder = speciesFolder.resolve("gene");
        makeDir(geneFolder);

        downloadEnsemblData(sp, spShortName, geneFolder, host);
        downloadDrugData(sp, speciesFolder);
        downloadGeneUniprotXref(sp, geneFolder);
        downloadGeneExpressionAtlas();
        downloadGeneDiseaseAnnotation(geneFolder);
        runGeneExtraInfo(sp, assembly, geneFolder);
    }

    private void downloadDrugData(Species species, Path speciesFolder) throws IOException, InterruptedException {

        if (species.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading drug-gene data...");

            Path geneDrugFolder = speciesFolder.resolve("gene/geneDrug");
            makeDir(geneDrugFolder);
            String url = configuration.getDownload().getDgidb().getHost();
            downloadFile(url, geneDrugFolder.resolve("dgidb.tsv").toString());

        }
    }

    private void downloadEnsemblData(Species sp, String spShortName, Path geneFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading gene Ensembl data (gtf, pep, cdna, motifs) ...");

        String ensemblHost = host + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(sp)) {
            ensemblHost = host + "/" + ensemblRelease + "/" + getPhylo(sp);
        }

        // Ensembl leaves now several GTF files in the FTP folder, we need to build a more accurate URL
        // to download the correct GTF file.
        String version = ensemblRelease.split("-")[1];
        String url = ensemblHost + "/gtf/" + spShortName + "/*" + version + ".gtf.gz";
        String fileName = geneFolder.resolve(spShortName + ".gtf.gz").toString();
        downloadFile(url, fileName);

        url = ensemblHost + "/fasta/" + spShortName + "/pep/*.pep.all.fa.gz";
        fileName = geneFolder.resolve(spShortName + ".pep.all.fa.gz").toString();
        downloadFile(url, fileName);

        url = ensemblHost + "/fasta/" + spShortName + "/cdna/*.cdna.all.fa.gz";
        fileName = geneFolder.resolve(spShortName + ".cdna.all.fa.gz").toString();
        downloadFile(url, fileName);

        url = ensemblHost + "/regulation/" + spShortName + "/MotifFeatures.gff.gz";
        Path outputFile = geneFolder.resolve("MotifFeatures.gff.gz");
        downloadFile(url, outputFile.toString());
    }

    private void downloadGeneUniprotXref(Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading UniProt ID mapping ...");

        if (GENE_UNIPROT_XREF_FILES.containsKey(sp.getScientificName())) {
            String geneGtfUrl = configuration.getDownload().getGeneUniprotXref().getHost() + "/"
                    + GENE_UNIPROT_XREF_FILES.get(sp.getScientificName());
            downloadFile(geneGtfUrl, geneFolder.resolve("idmapping_selected.tab.gz").toString());
        }
    }

    private void downloadGeneExpressionAtlas() throws IOException, InterruptedException {
        logger.info("Downloading gene expression atlas ...");
//        Path expression = geneFolder.getParent().resolve("common").resolve("expression");
        Path expression = common.resolve("expression");

        if (!Files.exists(expression)) {
            makeDir(expression);

            String geneGtfUrl = configuration.getDownload().getGeneExpressionAtlas().getHost();
            downloadFile(geneGtfUrl, expression.resolve("allgenes_updown_in_organism_part.tab.gz").toString());
        }
    }

    private void downloadGeneDiseaseAnnotation(Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene disease annotation ...");

        String host = configuration.getDownload().getHpo().getHost();
        String fileName = StringUtils.substringAfterLast(host, "/");
        downloadFile(host, geneFolder.resolve(fileName).toString());

        host = configuration.getDownload().getDisgenet().getHost();
        fileName = StringUtils.substringAfterLast(host, "/");
        downloadFile(host, geneFolder.resolve(fileName).toString());
    }


    private void runGeneExtraInfo(Species sp, String assembly, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene extra info ...");

        String geneExtraInfoLogFile = geneFolder.resolve("gene_extra_info.log").toString();
        List<String> args = new ArrayList<>();
        if (sp.getScientificName().equals("Homo sapiens") && assembly.equalsIgnoreCase("GRCh37")) {
            args.addAll(Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toAbsolutePath().toString(),
                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()
                            .replace("79", "75")));
        } else {
            args.addAll(Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toAbsolutePath().toString(),
                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()));

        }
        if (!configuration.getSpecies().getVertebrates().contains(species)
                && !species.getScientificName().equals("Drosophila melanogaster")) {
            args.add("--phylo");
            args.add("no-vertebrate");
        }

        // run gene_extra_info.pl
        boolean geneExtraInfoDownloaded = runCommandLineProcess(ensemblScriptsFolder,
                "./gene_extra_info.pl",
                args,
                geneExtraInfoLogFile);

        // check output
        if (geneExtraInfoDownloaded) {
            logger.info("Gene extra files created OK");
        } else {
            logger.error("Gene extra info for " + sp.getScientificName() + " cannot be downloaded");
        }
    }


    private void downloadVariation(Species sp, String shortName, Path spFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading variation information ...");
        Path variationFolder = spFolder.resolve("variation");
        makeDir(variationFolder);

        String variationUrl = host + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(sp)) {
            variationUrl = host + "/" + ensemblRelease + "/" + getPhylo(sp);
        }
        variationUrl = variationUrl + "/mysql/" + shortName + "_variation_" + ensemblVersion;

        for (String variationFile : VARIATION_FILES) {
            Path outputFile = variationFolder.resolve(variationFile);
            downloadFile(variationUrl + "/" + variationFile, outputFile.toString());
        }
    }


    private void downloadRegulation(Species species, String shortName, String assembly, Path speciesFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading regulation information ...");

        Path regulationFolder = speciesFolder.resolve("regulation");
        makeDir(regulationFolder);

        // Downloading Ensembl Regulation
        String regulationUrl = host + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(species)) {
            regulationUrl = host + "/" + ensemblRelease + "/" + getPhylo(species);
        }
        regulationUrl = regulationUrl + "/regulation/" + shortName;

        for (String regulationFile : REGULATION_FILES) {
            Path outputFile = regulationFolder.resolve(regulationFile);
            downloadFile(regulationUrl + "/" + regulationFile, outputFile.toString());
        }

        // Downloading miRNA info
        String url;
        Path mirbaseFolder = common.resolve("mirbase");
        if (!Files.exists(mirbaseFolder)) {
            makeDir(mirbaseFolder);

            url = configuration.getDownload().getMirbase().getHost() + "/miRNA.xls.gz";
            downloadFile(url, mirbaseFolder.resolve("miRNA.xls.gz").toString());

            url = configuration.getDownload().getMirbase().getHost() + "/aliases.txt.gz";
            downloadFile(url, mirbaseFolder.resolve("aliases.txt.gz").toString());
        }

        if (species.getScientificName().equals("Homo sapiens")) {
            if (assembly.equalsIgnoreCase("GRCh37")) {
                url = configuration.getDownload().getTargetScan().getHost() + "/hg19/database/targetScanS.txt.gz";
                downloadFile(url, regulationFolder.resolve("targetScanS.txt.gz").toString());

                url = configuration.getDownload().getMiRTarBase().getHost() + "/hsa_MTI.xls";
                downloadFile(url, regulationFolder.resolve("hsa_MTI.xls").toString());
            }
        }
        if (species.getScientificName().equals("Mus musculus")) {
            url = configuration.getDownload().getTargetScan().getHost() + "/mm9/database/targetScanS.txt.gz";
            downloadFile(url, regulationFolder.resolve("targetScanS.txt.gz").toString());

            url = configuration.getDownload().getMiRTarBase().getHost() + "/mmu_MTI.xls";
            downloadFile(url, regulationFolder.resolve("mmu_MTI.xls").toString());
        }
    }


    /**
     * This method downloads UniProt, IntAct and Interpro data from EMBL-EBI.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void downloadProtein() throws IOException, InterruptedException {
        logger.info("Downloading protein information ...");
        Path proteinFolder = common.resolve("protein");

        if (!Files.exists(proteinFolder)) {
            makeDir(proteinFolder);
            String url = configuration.getDownload().getUniprot().getHost();
            downloadFile(url, proteinFolder.resolve("uniprot_sprot.xml.gz").toString());

            makeDir(proteinFolder.resolve("uniprot_chunks"));
            splitUniprot(proteinFolder.resolve("uniprot_sprot.xml.gz"), proteinFolder.resolve("uniprot_chunks"));

            url = configuration.getDownload().getIntact().getHost();
            downloadFile(url, proteinFolder.resolve("intact.txt").toString());

            url = configuration.getDownload().getInterpro().getHost();
            downloadFile(url, proteinFolder.resolve("protein2ipr.dat.gz").toString());
        } else {
            logger.info("Protein: skipping this since it is already downloaded. Delete 'protein' folder to force download");
        }
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

    /**
     * This method downloads bith PhastCons and PhyloP data from UCSC for Human and Mouse species.
     *
     * @param species       The Species object to download the data
     * @param assembly      The assembly required
     * @param speciesFolder Output folder to download the data
     * @throws IOException
     * @throws InterruptedException
     */
    private void downloadConservation(Species species, String assembly, Path speciesFolder)
            throws IOException, InterruptedException {
        logger.info("Downloading conservation information ...");
        Path conservationFolder = speciesFolder.resolve("conservation");

        if (species.getScientificName().equals("Homo sapiens")) {
            makeDir(conservationFolder);
            makeDir(conservationFolder.resolve("phastCons"));
            makeDir(conservationFolder.resolve("phylop"));
            makeDir(conservationFolder.resolve("gerp"));

            String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "M", };

            if (assembly.equalsIgnoreCase("GRCh37")) {
                logger.debug("Downloading GERP++ ...");
                downloadFile(configuration.getDownload().getGerp().getHost(),
                        conservationFolder.resolve("gerp/hg19.GERP_scores.tar.gz").toAbsolutePath().toString());

                String url = configuration.getDownload().getConservation().getHost() + "/hg19";
                for (int i = 0; i < chromosomes.length; i++) {
                    String phastConsUrl = url + "/phastCons46way/primates/chr" + chromosomes[i] + ".phastCons46way.primates.wigFix.gz";
                    downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosomes[i]
                            + ".phastCons46way.primates.wigFix.gz").toString());

                    String phyloPUrl = url + "/phyloP46way/primates/chr" + chromosomes[i] + ".phyloP46way.primate.wigFix.gz";
                    downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosomes[i]
                            + ".phyloP46way.primate.wigFix.gz").toString());
                }
            }

            if (assembly.equalsIgnoreCase("GRCh38")) {
                String url = configuration.getDownload().getConservation().getHost() + "/hg38";
                for (int i = 0; i < chromosomes.length; i++) {
                    String phastConsUrl = url + "/phastCons100way/hg38.100way.phastCons/chr" + chromosomes[i]
                            + ".phastCons100way.wigFix.gz";
                    downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosomes[i]
                            + ".phastCons100way.wigFix.gz").toString());

                    String phyloPUrl = url + "/phyloP100way/hg38.100way.phyloP100way/chr" + chromosomes[i] + ".phyloP100way.wigFix.gz";
                    downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosomes[i]
                            + ".phyloP100way.wigFix.gz").toString());
                }
//                String phastConsUrl = url + "/phastCons7way/hg38.phastCons100way.wigFix.gz";
//                Path outFile = conservationFolder.resolve("phastCons").resolve("hg38.phastCons100way.wigFix.gz");
//                downloadFile(phastConsUrl, outFile.toString());
//
//                String phyloPUrl = url + "/phyloP7way/hg38.phyloP100way.wigFix.gz";
//                outFile = conservationFolder.resolve("phylop").resolve("hg38.phyloP100way.wigFix.gz");
//                downloadFile(phyloPUrl, outFile.toString());
            }
        }

        if (species.getScientificName().equals("Mus musculus")) {
            makeDir(conservationFolder);
            makeDir(conservationFolder.resolve("phastCons"));
            makeDir(conservationFolder.resolve("phylop"));

            String url = configuration.getDownload().getConservation().getHost() + "/mm10";
            String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "X", "Y", "M", };
            for (int i = 0; i < chromosomes.length; i++) {
                String phastConsUrl = url + "/phastCons60way/mm10.60way.phastCons/chr" + chromosomes[i] + ".phastCons60way.wigFix.gz";
                downloadFile(phastConsUrl, conservationFolder.resolve("phastCons").resolve("chr" + chromosomes[i]
                        + ".phastCons60way.wigFix.gz").toString());

                String phyloPUrl = url + "/phyloP60way/mm10.60way.phyloP60way/chr" + chromosomes[i] + ".phyloP60way.wigFix.gz";
                downloadFile(phyloPUrl, conservationFolder.resolve("phylop").resolve("chr" + chromosomes[i]
                        + ".phyloP60way.wigFix.gz").toString());
            }
        }
    }

    private void downloadClinical(Species species, Path speciesFolder)
            throws IOException, InterruptedException {

        if (species.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading clinical information ...");

            Path clinicalFolder = speciesFolder.resolve("clinical");
            makeDir(clinicalFolder);
            String url = configuration.getDownload().getClinvar().getHost();
            downloadFile(url, clinicalFolder.resolve("ClinVar.xml.gz").toString());

            url = configuration.getDownload().getClinvarEfoTerms().getHost();
            downloadFile(url, clinicalFolder.resolve("ClinVar_Traits_EFO_Names.csv").toString());

            url = configuration.getDownload().getClinvarSummary().getHost();
            downloadFile(url, clinicalFolder.resolve("variant_summary.txt.gz").toString());

            url = configuration.getDownload().getGwasCatalog().getHost();
            downloadFile(url, clinicalFolder.resolve("gwas_catalog.tsv").toString());

            url = configuration.getDownload().getDbsnp().getHost();
            downloadFile(url, clinicalFolder.resolve("All.vcf.gz").toString());

            url = url + ".tbi";
            downloadFile(url, clinicalFolder.resolve("All.vcf.gz.tbi").toString());
        }
    }

    private void downloadGeneDiseaseAssociation(Species species, Path speciesFolder) throws IOException, InterruptedException {
        if (species.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading gene to disease information ...");

            Path gene2diseaseFolder = speciesFolder.resolve("gene_disease_association");
            makeDir(gene2diseaseFolder);

            // Downloads DisGeNET
            String url = configuration.getDownload().getDisgenet().getHost();
            downloadFile(url, gene2diseaseFolder.resolve("all_gene_disease_associations.txt.gz").toString());

            // Downloads HPO
            url = configuration.getDownload().getHpo().getHost();
            downloadFile(url, gene2diseaseFolder.resolve("ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt").toString());
        }
    }

    private void downloadCaddScores(Species species, String assembly, Path speciesFolder) throws IOException, InterruptedException {
        if (species.getScientificName().equals("Homo sapiens") && assembly.equalsIgnoreCase("GRCh37")) {
            logger.info("Downloading CADD scores information ...");

            Path variationFunctionalScoreFolder = speciesFolder.resolve("variation_functional_score");
            makeDir(variationFunctionalScoreFolder);

            // Downloads CADD scores
            String url = configuration.getDownload().getCadd().getHost();
            downloadFile(url, variationFunctionalScoreFolder.resolve("whole_genome_SNVs.tsv.gz").toString());
        }
    }


    private void downloadFile(String url, String outputFileName) throws IOException, InterruptedException {
        List<String> wgetArgs = Arrays.asList("--tries=10", url, "-O", outputFileName, "-o", outputFileName + ".log");
        boolean downloaded = runCommandLineProcess(null, "wget", wgetArgs, null);

        if (downloaded) {
            logger.info(outputFileName + " created OK");
        } else {
            logger.warn(url + " cannot be downloaded");
        }
    }
}
