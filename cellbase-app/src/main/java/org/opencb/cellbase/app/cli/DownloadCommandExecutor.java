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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.lang.StringUtils;
import org.opencb.cellbase.core.config.Species;
import org.opencb.commons.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    @Deprecated
    private static final String[] DEPRECATED_REGULATION_FILES = {"AnnotatedFeatures.gff.gz", "MotifFeatures.gff.gz",
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

    private static final String ENSEMBL_NAME = "ENSEMBL";
    private static final String GENE_EXPRESSION_ATLAS_NAME = "Gene Expression Atlas";
    private static final String HPO_NAME = "HPO";
    private static final String DISGENET_NAME = "DisGeNET";
    private static final String DGIDB_NAME = "DGIdb";
    private static final String UNIPROT_NAME = "UniProt";
    private static final String CADD_NAME = "CADD";
    private static final String MIRBASE_NAME = "miRBase";
    private static final String MIRTARBASE_NAME = "miRTarBase";
    private static final String TARGETSCAN_NAME = "TargetScan";
    private static final String INTACT_NAME = "IntAct";
    private static final String INTERPRO_NAME = "InterPro";
    private static final String GERP_NAME = "GERP++";
    private static final String PHASTCONS_NAME = "PhastCons";
    private static final String PHYLOP_NAME = "PhyloP";
    private static final String CLINVAR_NAME = "ClinVar";
    private static final String IARCTP53_NAME = "IARC TP53 Database";
    private static final String DGV_NAME = "DGV";
    private static final String GWAS_NAME = "Gwas Catalog";
    private static final String DBSNP_NAME = "dbSNP";
    private static final String REACTOME_NAME = "Reactome";
    private static final String TRF_NAME = "Tandem repeats finder";
    private static final String GSD_NAME = "Genomic super duplications";
    private static final String WM_NAME = "WindowMasker";

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
                    case EtlCommons.GENOME_DATA:
                        downloadReferenceGenome(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
                        break;
                    case EtlCommons.GENE_DATA:
                        downloadEnsemblGene(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
                        break;
                    case EtlCommons.VARIATION_DATA:
                        if (speciesHasInfoToDownload(sp, "variation")) {
                            downloadVariation(sp, spShortName, spFolder, ensemblHostUrl);
                        }
                        break;
                    case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                        if (speciesHasInfoToDownload(sp, "variation_functional_score")) {
                            downloadCaddScores(sp, assembly.getName(), spFolder);
                        }
                        break;
                    case EtlCommons.REGULATION_DATA:
                        if (speciesHasInfoToDownload(sp, "regulation")) {
                            downloadRegulation(sp, spShortName, assembly.getName(), spFolder, ensemblHostUrl);
                        }
                        break;
                    case EtlCommons.PROTEIN_DATA:
                        if (speciesHasInfoToDownload(sp, "protein")) {
                            downloadProtein();
                        }
                        break;
                    case EtlCommons.CONSERVATION_DATA:
                        if (speciesHasInfoToDownload(sp, "conservation")) {
                            downloadConservation(sp, assembly.getName(), spFolder);
                        }
                        break;
                    case EtlCommons.CLINICAL_VARIANTS_DATA:
                        if (speciesHasInfoToDownload(sp, "clinical_variants")) {
                            downloadClinical(sp, assembly.getName(), spFolder);
                        }
                        break;
                    case EtlCommons.STRUCTURAL_VARIANTS_DATA:
                        if (speciesHasInfoToDownload(sp, "svs")) {
                            downloadStructuralVariants(sp, assembly.getName(), spFolder);
                        }
                        break;
                    case EtlCommons.REPEATS_DATA:
                        if (speciesHasInfoToDownload(sp, "repeats")) {
                            downloadRepeats(sp, assembly.getName(), spFolder);
                        }
                        break;
                    default:
                        System.out.println("Value \"" + data + "\" is not allowed for the data parameter. Allowed values"
                                + " are: {genome, gene, gene_disease_association, variation, variation_functional_score,"
                                + " regulation, protein, conservation, clinical_variants}");
                        break;
                }
            }
        }
    }

    private void downloadStructuralVariants(Species species, String assembly, Path speciesFolder) throws IOException, InterruptedException {
        if (species.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading DGV data ...");

            Path structuralVariantsFolder = speciesFolder.resolve(EtlCommons.STRUCTURAL_VARIANTS_FOLDER);
            makeDir(structuralVariantsFolder);
            String sourceFilename = (assembly.equalsIgnoreCase("grch37") ? "GRCh37_hg19" : "GRCh38_hg38")
                    + "_variants_2016-05-15.txt";
            String url = configuration.getDownload().getDgv().getHost() + "/" + sourceFilename;
            downloadFile(url, structuralVariantsFolder.resolve(EtlCommons.DGV_FILE).toString());

            saveVersionData(EtlCommons.STRUCTURAL_VARIANTS_DATA, DGV_NAME, getDGVVersion(sourceFilename), getTimeStamp(),
                    Collections.singletonList(url), structuralVariantsFolder.resolve(EtlCommons.DGV_VERSION_FILE));

        }
    }

    private String getDGVVersion(String sourceFilename) {
        return sourceFilename.split("\\.")[0].split("_")[3];
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
        } else if (configuration.getSpecies().getVirus().contains(sp)) {
            return "virus";
        } else if (configuration.getSpecies().getBacteria().contains(sp)) {
            return "bacteria";
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
            url = url + "/fasta/";
            if (configuration.getSpecies().getBacteria().contains(sp)) {
                // WARN: assuming there's just one assembly
                url = url + sp.getAssemblies().get(0).getEnsemblCollection() + "/";
            }
            url = url + shortName + "/dna/*.dna.toplevel.fa.gz";
        }

        String outputFileName = StringUtils.capitalize(shortName) + "." + assembly + ".fa.gz";
        Path outputPath = sequenceFolder.resolve(outputFileName);
        downloadFile(url, outputPath.toString());
        logger.info("Saving reference genome version data at {}", sequenceFolder.resolve("genomeVersion.json"));
        saveVersionData(EtlCommons.GENOME_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(),
                Collections.singletonList(url), sequenceFolder.resolve("genomeVersion.json"));
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    private void saveVersionData(String data, String source, String version, String date, List<String> url,
                                 Path outputFilePath) {
        Map versionData = new HashedMap();
        versionData.put("data", data);
        versionData.put("source", source);
        versionData.put("version", version);
        versionData.put("downloadDate", date);
        versionData.put("uRL", url);
        writeVersionDataFile(versionData, outputFilePath);
    }

    private void writeVersionDataFile(Map versionData, Path outputFilePath) {
        try {
            OutputStream os = Files.newOutputStream(outputFilePath);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            ObjectWriter jsonObjectWriter = jsonObjectMapper.writer();
            bw.write(jsonObjectWriter.writeValueAsString(versionData) + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

            saveVersionData(EtlCommons.GENE_DATA, DGIDB_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    geneDrugFolder.resolve("dgidbVersion.json"));

        }
    }

    private void downloadEnsemblData(Species sp, String spShortName, Path geneFolder, String host)
            throws IOException, InterruptedException {
        logger.info("Downloading gene Ensembl data (gtf, pep, cdna, motifs) ...");
        List<String> downloadedUrls = new ArrayList<>(4);

        String ensemblHost = host + "/" + ensemblRelease;
        if (!configuration.getSpecies().getVertebrates().contains(sp)) {
            ensemblHost = host + "/" + ensemblRelease + "/" + getPhylo(sp);
        }

        String bacteriaCollectionPath = "";
        if (configuration.getSpecies().getBacteria().contains(sp)) {
            // WARN: assuming there's just one assembly
            bacteriaCollectionPath =  sp.getAssemblies().get(0).getEnsemblCollection() + "/";
        }

        // Ensembl leaves now several GTF files in the FTP folder, we need to build a more accurate URL
        // to download the correct GTF file.
        String version = ensemblRelease.split("-")[1];
        String url = ensemblHost + "/gtf/" + bacteriaCollectionPath + spShortName + "/*" + version + ".gtf.gz";
        String fileName = geneFolder.resolve(spShortName + ".gtf.gz").toString();
        downloadFile(url, fileName);
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + bacteriaCollectionPath + spShortName + "/pep/*.pep.all.fa.gz";
        fileName = geneFolder.resolve(spShortName + ".pep.all.fa.gz").toString();
        downloadFile(url, fileName);
        downloadedUrls.add(url);

        url = ensemblHost + "/fasta/" + bacteriaCollectionPath + spShortName + "/cdna/*.cdna.all.fa.gz";
        fileName = geneFolder.resolve(spShortName + ".cdna.all.fa.gz").toString();
        downloadFile(url, fileName);
        downloadedUrls.add(url);

        url = ensemblHost + "/regulation/" + spShortName + "/MotifFeatures.gff.gz";
        Path outputFile = geneFolder.resolve("MotifFeatures.gff.gz");
        downloadFile(url, outputFile.toString());
        downloadedUrls.add(url);

        saveVersionData(EtlCommons.GENE_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                geneFolder.resolve("ensemblCoreVersion.json"));
    }

    private void downloadGeneUniprotXref(Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading UniProt ID mapping ...");

        if (GENE_UNIPROT_XREF_FILES.containsKey(sp.getScientificName())) {
            String geneGtfUrl = configuration.getDownload().getGeneUniprotXref().getHost() + "/"
                    + GENE_UNIPROT_XREF_FILES.get(sp.getScientificName());
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

    private void downloadGeneExpressionAtlas() throws IOException, InterruptedException {
        logger.info("Downloading gene expression atlas ...");
        Path expression = common.resolve("expression");

        if (!Files.exists(expression)) {
            makeDir(expression);

            String geneGtfUrl = configuration.getDownload().getGeneExpressionAtlas().getHost();
            downloadFile(geneGtfUrl, expression.resolve("allgenes_updown_in_organism_part.tab.gz").toString());

            saveVersionData(EtlCommons.GENE_DATA, GENE_EXPRESSION_ATLAS_NAME, getGeneExpressionAtlasVersion(), getTimeStamp(),
                    Collections.singletonList(geneGtfUrl), expression.resolve("geneExpressionAtlasVersion.json"));
        }
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
        String readme = configuration.getDownload().getDisgenetReadme().getHost();
        fileName = StringUtils.substringAfterLast(host, "/");
        downloadFile(host, geneFolder.resolve(fileName).toString());
        downloadFile(readme, geneFolder.resolve("disgenetReadme.txt").toString());
        saveVersionData(EtlCommons.GENE_DISEASE_ASSOCIATION_DATA, DISGENET_NAME,
                getVersionFromVersionLine(geneFolder.resolve("disgenetReadme.txt"), "(version"), getTimeStamp(),
                Collections.singletonList(host), geneFolder.resolve("disgenetVersion.json"));
    }

    private String getVersionFromVersionLine(Path path, String tag) {
        Files.exists(path);
        try {
            BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());
            String line = reader.readLine();
            // There shall be a line at the README.txt containing the version.
            // e.g. The files in the current directory contain the data corresponding to the latest release
            // (version 4.0, April 2016). ...
            while (line != null) {
                // tag specifies a certain string that must be found within the line supposed to contain the version
                // info
                if (line.contains(tag)) {
                    String version = line.split("\\(")[1].split("\\)")[0];
                    reader.close();
                    return version;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void runGeneExtraInfo(Species sp, String assembly, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading gene extra info ...");

        String geneExtraInfoLogFile = geneFolder.resolve("gene_extra_info.log").toString();
        List<String> args = new ArrayList<>();
//        if (sp.getScientificName().equals("Homo sapiens") && assembly.equalsIgnoreCase("GRCh37")) {
//            args.addAll(Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toAbsolutePath().toString(),
//                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()
//                            .replace("79", "75")));
//        } else {
//            args.addAll(Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toAbsolutePath().toString(),
//                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()));
//
//        }

            args.addAll(Arrays.asList("--species", sp.getScientificName(), "--assembly", assembly,
                    "--outdir", geneFolder.toAbsolutePath().toString(),
                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs()));

        if (!configuration.getSpecies().getVertebrates().contains(species)
                && !species.getScientificName().equals("Drosophila melanogaster")) {
            args.add("--phylo");
            args.add("no-vertebrate");
        }

        // run gene_extra_info.pl
        boolean geneExtraInfoDownloaded = EtlCommons.runCommandLineProcess(ensemblScriptsFolder,
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
        List<String> downloadedUrls = new ArrayList<>(VARIATION_FILES.length);

        for (String variationFile : VARIATION_FILES) {
            Path outputFile = variationFolder.resolve(variationFile);
            downloadFile(variationUrl + "/" + variationFile, outputFile.toString());
            downloadedUrls.add(variationUrl + "/" + variationFile);
        }

        saveVersionData(EtlCommons.VARIATION_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                variationFolder.resolve("ensemblVariationVersion.json"));

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

        List<String> downloadedUrls = new ArrayList<>(DEPRECATED_REGULATION_FILES.length + 2);
        // TODO: REMOVE
        // >>>>>>>>>>>>>>>>>>>>>>>>>>> DEPRECATED
        for (String regulationFile : DEPRECATED_REGULATION_FILES) {
            Path outputFile = regulationFolder.resolve(regulationFile);
            downloadFile(regulationUrl + "/" + regulationFile, outputFile.toString());
            downloadedUrls.add(regulationUrl + "/" + regulationFile);
        }
        // <<<<<<<<<<<<<<<<<<<<<<<<<<< DEPRECATED

        Path outputFile = regulationFolder.resolve(EtlCommons.REGULATORY_FEATURES_FILE);
        downloadFile(regulationUrl + "/*Regulatory_Build.regulatory_features*.gff.gz", outputFile.toString());
        downloadedUrls.add(regulationUrl + "/*Regulatory_Build.regulatory_features*.gff.gz");

        outputFile = regulationFolder.resolve(EtlCommons.MOTIF_FEATURES_FILE);
        downloadFile(regulationUrl + "/*motiffeatures*.gff.gz", outputFile.toString());
        downloadedUrls.add(regulationUrl + "/*motiffeatures*.gff.gz");

        saveVersionData(EtlCommons.REGULATION_DATA, ENSEMBL_NAME, ensemblVersion, getTimeStamp(), downloadedUrls,
                regulationFolder.resolve("ensemblRegulationVersion.json"));

        // Downloading miRNA info
        String url;
        Path mirbaseFolder = common.resolve("mirbase");
        if (!Files.exists(mirbaseFolder)) {
            makeDir(mirbaseFolder);
            downloadedUrls = new ArrayList<>(2);

            url = configuration.getDownload().getMirbase().getHost() + "/miRNA.xls.gz";
            downloadFile(url, mirbaseFolder.resolve("miRNA.xls.gz").toString());
            downloadedUrls.add(url);

            url = configuration.getDownload().getMirbase().getHost() + "/aliases.txt.gz";
            downloadFile(url, mirbaseFolder.resolve("aliases.txt.gz").toString());
            downloadedUrls.add(url);

            String readmeUrl = configuration.getDownload().getMirbaseReadme().getHost();
            downloadFile(readmeUrl, mirbaseFolder.resolve("mirbaseReadme.txt").toString());
            saveVersionData(EtlCommons.REGULATION_DATA, MIRBASE_NAME,
                    getLine(mirbaseFolder.resolve("mirbaseReadme.txt"), 1), getTimeStamp(),
                    Collections.singletonList(url), mirbaseFolder.resolve("mirbaseVersion.json"));
        }

        if (species.getScientificName().equals("Homo sapiens")) {
            if (assembly.equalsIgnoreCase("GRCh37")) {
                url = configuration.getDownload().getTargetScan().getHost() + "/hg19/database/targetScanS.txt.gz";
                downloadFile(url, regulationFolder.resolve("targetScanS.txt.gz").toString());

                String readmeUrl = configuration.getDownload().getTargetScan().getHost() + "/hg19/database/README.txt";
                saveVersionData(EtlCommons.REGULATION_DATA, TARGETSCAN_NAME, null, getTimeStamp(),
                        Collections.singletonList(url), regulationFolder.resolve("targetScanVersion.json"));

                url = configuration.getDownload().getMiRTarBase().getHost() + "/hsa_MTI.xls";
                downloadFile(url, regulationFolder.resolve("hsa_MTI.xls").toString());
                saveVersionData(EtlCommons.REGULATION_DATA, MIRTARBASE_NAME, url.split("/")[5], getTimeStamp(),
                        Collections.singletonList(url), regulationFolder.resolve("miRTarBaseVersion.json"));
            }
        }
        if (species.getScientificName().equals("Mus musculus")) {
            url = configuration.getDownload().getTargetScan().getHost() + "/mm9/database/targetScanS.txt.gz";
            downloadFile(url, regulationFolder.resolve("targetScanS.txt.gz").toString());

            String readmeUrl = configuration.getDownload().getTargetScan().getHost() + "/mm9/database/README.txt";
            downloadFile(readmeUrl, regulationFolder.resolve("targetScanReadme.txt").toString());
            saveVersionData(EtlCommons.REGULATION_DATA, TARGETSCAN_NAME, null, getTimeStamp(),
                    Collections.singletonList(url), regulationFolder.resolve("targetScanVersion.json"));

            url = configuration.getDownload().getMiRTarBase().getHost() + "/mmu_MTI.xls";
            downloadFile(url, regulationFolder.resolve("mmu_MTI.xls").toString());
            saveVersionData(EtlCommons.REGULATION_DATA, MIRTARBASE_NAME, url.split("/")[5], getTimeStamp(),
                    Collections.singletonList(url),
                    regulationFolder.resolve("miRTarBaseVersion.json"));
        }
    }

    private String getLine(Path readmePath, int lineNumber) {
        Files.exists(readmePath);
        try {
            BufferedReader reader = Files.newBufferedReader(readmePath, Charset.defaultCharset());
            String line = null;
            for (int i = 0; i < lineNumber; i++) {
                line = reader.readLine();
            }
            reader.close();
            return line;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
            String relNotesUrl = configuration.getDownload().getUniprotRelNotes().getHost();
            downloadFile(relNotesUrl, proteinFolder.resolve("uniprotRelnotes.txt").toString());
            saveVersionData(EtlCommons.PROTEIN_DATA, UNIPROT_NAME, getLine(proteinFolder.resolve("uniprotRelnotes.txt"), 1),
                    getTimeStamp(), Collections.singletonList(url), proteinFolder.resolve("uniprotVersion.json"));

            makeDir(proteinFolder.resolve("uniprot_chunks"));
            splitUniprot(proteinFolder.resolve("uniprot_sprot.xml.gz"), proteinFolder.resolve("uniprot_chunks"));

            url = configuration.getDownload().getIntact().getHost();
            downloadFile(url, proteinFolder.resolve("intact.txt").toString());
            saveVersionData(EtlCommons.PROTEIN_DATA, INTACT_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    proteinFolder.resolve("intactVersion.json"));

            url = configuration.getDownload().getInterpro().getHost();
            downloadFile(url, proteinFolder.resolve("protein2ipr.dat.gz").toString());
            relNotesUrl = configuration.getDownload().getInterproRelNotes().getHost();
            downloadFile(relNotesUrl, proteinFolder.resolve("interproRelnotes.txt").toString());
            saveVersionData(EtlCommons.PROTEIN_DATA, INTERPRO_NAME, getLine(proteinFolder.resolve("interproRelnotes.txt"), 5),
                    getTimeStamp(), Collections.singletonList(url), proteinFolder.resolve("interproVersion.json"));

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

            if (assembly.equalsIgnoreCase("GRCh38")) {
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

    private void downloadClinical(Species species, String assembly, Path speciesFolder)
            throws IOException, InterruptedException {

        if (species.getScientificName().equals("Homo sapiens")) {
            if (downloadCommandOptions.assembly == null) {
                throw new ParameterException("Assembly must be provided for downloading clinical variants data."
                        + " Please, specify either --assembly GRCh37 or --assembly GRCh38");
            }

            logger.info("Downloading clinical information ...");

            Path clinicalFolder = speciesFolder.resolve(EtlCommons.CLINICAL_VARIANTS_FOLDER);
            makeDir(clinicalFolder);
            List<String> clinvarUrls = new ArrayList<>(3);
            String url = configuration.getDownload().getClinvar().getHost();

            downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_XML_FILE).toString());
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarEfoTerms().getHost();
            downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_EFO_FILE).toString());
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarSummary().getHost();
            downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_SUMMARY_FILE).toString());
            clinvarUrls.add(url);

            url = configuration.getDownload().getClinvarVariationAllele().getHost();
            downloadFile(url, clinicalFolder.resolve(EtlCommons.CLINVAR_VARIATION_ALLELE_FILE).toString());
            clinvarUrls.add(url);
            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, CLINVAR_NAME, getClinVarVersion(), getTimeStamp(), clinvarUrls,
                    clinicalFolder.resolve("clinvarVersion.json"));

            // TODO: Fix and enable when needed
//            url = configuration.getDownload().getGwasCatalog().getHost();
//            downloadFile(url, clinicalFolder.resolve(EtlCommons.GWAS_FILE).toString());
//            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, GWAS_NAME, getGwasVersion(), getTimeStamp(),
//                     Collections.singletonList(url), clinicalFolder.resolve("gwasVersion.json"));

//            List<String> dbsnpUrls = new ArrayList<>(2);
//            url = configuration.getDownload().getDbsnp().getHost();
//            downloadFile(url, clinicalFolder.resolve(EtlCommons.DBSNP_FILE).toString());
//            dbsnpUrls.add(url);
//
//            url = url + ".tbi";
//            downloadFile(url, clinicalFolder.resolve(EtlCommons.DBSNP_FILE + ".tbi").toString());
//            dbsnpUrls.add(url);
//            saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, DBSNP_NAME, getDbsnpVersion(), getTimeStamp(), dbsnpUrls,
//                    clinicalFolder.resolve("dbsnpVersion.json"));

            List<String> hgvsList = getDocmHgvsList();
            if (!hgvsList.isEmpty()) {
                downloadDocm(hgvsList, clinicalFolder.resolve(EtlCommons.DOCM_FILE));
                downloadFile(configuration.getDownload().getDocmVersion().getHost(),
                        clinicalFolder.resolve("docmIndex.html").toString());
                saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, EtlCommons.DOCM_NAME,
                        getDocmVersion(clinicalFolder.resolve("docmIndex.html")), getTimeStamp(),
                        Arrays.asList(configuration.getDownload().getDocm().getHost() + "v1/variants.json",
                                configuration.getDownload().getDocm().getHost() + "v1/variants/{hgvs}.json"),
                        clinicalFolder.resolve("docmVersion.json"));
            } else {
                logger.warn("No DOCM variants found for assembly {}. Please double-check that this is the correct "
                        + "assembly", assembly);
            }

            if (assembly.equalsIgnoreCase("grch37")) {
                url = configuration.getDownload().getIarctp53().getHost();
                downloadFile(url, clinicalFolder.resolve(EtlCommons.IARCTP53_FILE).toString(),
                        Collections.singletonList("--post-data=dataset-somaticMutationData=somaticMutationData"
                                + "&dataset-germlineMutationData=germlineMutationData"
                                + "&dataset-somaticMutationReference=somaticMutationReference"
                                + "&dataset-germlineMutationReference=germlineMutationReference"));

                ZipFile zipFile = new ZipFile(clinicalFolder.resolve(EtlCommons.IARCTP53_FILE).toString());
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    File entryDestination = new File(clinicalFolder.toFile(), entry.getName());
                    if (entry.isDirectory()) {
                        entryDestination.mkdirs();
                    } else {
                        entryDestination.getParentFile().mkdirs();
                        InputStream in = zipFile.getInputStream(entry);
                        OutputStream out = new FileOutputStream(entryDestination);
                        IOUtils.copy(in, out);
                        IOUtils.closeQuietly(in);
                        out.close();
                    }
                }
                saveVersionData(EtlCommons.CLINICAL_VARIANTS_DATA, IARCTP53_NAME,
                        getVersionFromVersionLine(clinicalFolder.resolve("Disclaimer.txt"),
                                "The version of the database should be identified"), getTimeStamp(),
                        Collections.singletonList(url), clinicalFolder.resolve("iarctp53Version.json"));
            }
        }
    }

    private String getDocmVersion(Path docmIndexHtml) {
        return getVersionFromVersionLine(docmIndexHtml, "<select name=\"version\" id=\"version\"");
    }

    private void downloadDocm(List<String> hgvsList, Path path) throws IOException, InterruptedException {
        BufferedWriter bufferedWriter = FileUtils.newBufferedWriter(path);
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
                    && document.get("reference_version").equalsIgnoreCase(downloadCommandOptions.assembly)) {
                hgvsList.add(document.get("hgvs"));
            }
        }
        logger.info("{} hgvs found", hgvsList.size());

        return hgvsList;
    }

    private List<Map<String, String>> parseResult(String json) throws IOException {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader reader = jsonObjectMapper
                .readerFor(jsonObjectMapper.getTypeFactory()
                        .constructCollectionType(List.class, Map.class));
        return reader.readValue(json);
    }

    private String getDbsnpVersion() {
        // ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b144_GRCh37p13/VCF/All_20150605.vcf.gz
        return configuration.getDownload().getDbsnp().getHost().split("_")[2];
    }

    private String getGwasVersion() {
        // ftp://ftp.ebi.ac.uk/pub/databases/gwas/releases/2016/05/10/gwas-catalog-associations.tsv
        String[] pathParts = configuration.getDownload().getGwasCatalog().getHost().split("/");
        return pathParts[9] + "/" + pathParts[8] + "/" + pathParts[7];
    }

    private String getClinVarVersion() {
        // ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/xml/ClinVarFullRelease_2015-12.xml.gz
        return configuration.getDownload().getClinvar().getHost().split("_")[1].split("\\.")[0];
    }

    private void downloadCaddScores(Species species, String assembly, Path speciesFolder) throws IOException, InterruptedException {
        if (species.getScientificName().equals("Homo sapiens") && assembly.equalsIgnoreCase("GRCh37")) {
            logger.info("Downloading CADD scores information ...");

            Path variationFunctionalScoreFolder = speciesFolder.resolve("variation_functional_score");
            makeDir(variationFunctionalScoreFolder);

            // Downloads CADD scores
            String url = configuration.getDownload().getCadd().getHost();
            downloadFile(url, variationFunctionalScoreFolder.resolve("whole_genome_SNVs.tsv.gz").toString());
            saveVersionData(EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, CADD_NAME, url.split("/")[5], getTimeStamp(),
                    Collections.singletonList(url), variationFunctionalScoreFolder.resolve("caddVersion.json"));
        }
    }

    private void downloadReactomeData() throws IOException, InterruptedException {
        Path proteinFolder = common.resolve("protein");

        String url = configuration.getDownload().getReactome().getHost();
        downloadFile(url, proteinFolder.resolve("biopax.zip").toString());
        saveVersionData(EtlCommons.PROTEIN_DATA, REACTOME_NAME, null, getTimeStamp(), Collections.singletonList(url),
                proteinFolder.resolve("reactomeVersion.json"));
    }

    private void downloadRepeats(Species species, String assembly, Path speciesFolder)
            throws IOException, InterruptedException {

        if (species.getScientificName().equals("Homo sapiens")) {
            logger.info("Downloading repeats data ...");
            Path repeatsFolder = speciesFolder.resolve(EtlCommons.REPEATS_FOLDER);
            makeDir(repeatsFolder);
            String pathParam;
            if (assembly.equalsIgnoreCase("grch37")) {
                pathParam = "hg19";
            } else if (assembly.equalsIgnoreCase("grch38")) {
                pathParam = "hg38";
            } else {
                logger.error("Please provide a valid human assembly {GRCh37, GRCh38)");
                throw new ParameterException("Assembly '" + assembly + "' is not valid. Please provide a valid human "
                        + "assembly {GRCh37, GRCh38)");
            }

            // Download tandem repeat finder
            String url = configuration.getDownload().getSimpleRepeats().getHost() + "/" + pathParam
                    + "/database/simpleRepeat.txt.gz";
            downloadFile(url, repeatsFolder.resolve(EtlCommons.TRF_FILE).toString());
            saveVersionData(EtlCommons.REPEATS_DATA, TRF_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    repeatsFolder.resolve(EtlCommons.TRF_VERSION_FILE));

            // Download genomic super duplications
            url = configuration.getDownload().getGenomicSuperDups().getHost() + "/" + pathParam
                    + "/database/genomicSuperDups.txt.gz";
            downloadFile(url, repeatsFolder.resolve(EtlCommons.GSD_FILE).toString());
            saveVersionData(EtlCommons.REPEATS_DATA, GSD_NAME, null, getTimeStamp(), Collections.singletonList(url),
                    repeatsFolder.resolve(EtlCommons.GSD_VERSION_FILE));

            // Download WindowMasker
            if (!pathParam.equalsIgnoreCase("hg19")) {
                url = configuration.getDownload().getWindowMasker().getHost() + "/" + pathParam
                        + "/database/windowmaskerSdust.txt.gz";
                downloadFile(url, repeatsFolder.resolve(EtlCommons.WM_FILE).toString());
                saveVersionData(EtlCommons.REPEATS_DATA, WM_NAME, null, getTimeStamp(), Collections.singletonList(url),
                        repeatsFolder.resolve(EtlCommons.WM_VERSION_FILE));
            }

        }
    }

    private void downloadFile(String url, String outputFileName) throws IOException, InterruptedException {
        downloadFile(url, outputFileName, null);
    }

    private void downloadFile(String url, String outputFileName, List<String> wgetAdditionalArgs)
            throws IOException, InterruptedException {

        File file = new File(outputFileName);
        if (file.exists()) {
            FTPClient ftpClient = new FTPClient();
            // code to connect and login....

            FTPFile remotefile = ftpClient.mlistFile(outputFileName);
            long remotesize = remotefile.getSize();
            long localsize = file.length();
            long locallastmodified = file.lastModified();
            logger.info("File " + outputFileName + " exists, with size " + localsize + " vs "
                + remotesize + " in remote and will not be downloaded again");
            if (remotesize == localsize) {
                logger.info("Download avoided because the files have the same size");
                return;
            }
            //If the filestamps differ, regardless of the timestamps downloads the file
            Calendar remotets = remotefile.getTimestamp();
            logger.info("local ts " + locallastmodified + "wheras remote ts is " + remotets.toString());
        }
        List<String> wgetArgs = new ArrayList<>(Arrays.asList("--tries=10", url, "-O", outputFileName, "-o",
                outputFileName + ".log"));
        if (wgetAdditionalArgs != null && !wgetAdditionalArgs.isEmpty()) {
            wgetArgs.addAll(wgetAdditionalArgs);
        }

        boolean downloaded = EtlCommons.runCommandLineProcess(null, "wget", wgetArgs, null);

        if (downloaded) {
            logger.info(outputFileName + " created OK");
        } else {
            logger.warn(url + " cannot be downloaded");
        }
    }
}
