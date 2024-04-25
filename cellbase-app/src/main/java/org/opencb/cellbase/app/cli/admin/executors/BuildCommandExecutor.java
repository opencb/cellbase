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

package org.opencb.cellbase.app.cli.admin.executors;

import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.cellbase.lib.builders.*;
import org.opencb.cellbase.lib.builders.clinical.variant.ClinicalVariantBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.core.utils.SpeciesUtils.getSpeciesShortname;
import static org.opencb.cellbase.lib.EtlCommons.*;

/**
 * Created by imedina on 03/02/15.
 */
public class BuildCommandExecutor extends CommandExecutor {
    private AdminCliOptionsParser.BuildCommandOptions buildCommandOptions;

    private Path output;
    private Path buildFolder = null; // <output>/<species>_<assembly>/generated-json
    private Path downloadFolder = null; // <output>/<species>_<assembly>/download
    private boolean normalize = true;

    private SpeciesConfiguration.Assembly assembly;
    private String ensemblRelease;

    private boolean flexibleGTFParsing;
    private SpeciesConfiguration speciesConfiguration;

    private static final List<String> VALID_SOURCES_TO_BUILD = Arrays.asList(GENOME_DATA, GENE_DATA, REFSEQ_DATA,
            VARIATION_FUNCTIONAL_SCORE_DATA, MISSENSE_VARIATION_SCORE_DATA, REGULATION_DATA, PROTEIN_DATA, CONSERVATION_DATA,
            CLINICAL_VARIANTS_DATA, REPEATS_DATA, ONTOLOGY_DATA, SPLICE_SCORE_DATA, PUBMED_DATA, PHARMACOGENOMICS_DATA);

    public BuildCommandExecutor(AdminCliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
        this.output = Paths.get(buildCommandOptions.outputDirectory);
        normalize = !buildCommandOptions.skipNormalize;

        this.flexibleGTFParsing = buildCommandOptions.flexibleGTFParsing;
    }

    /**
     * Parse specific 'build' command options.
     *
     * @throws CellBaseException Exception
     */
    public void execute() throws CellBaseException {
        String data = null;
        try {
            // Check data sources
            List<String> dataList = checkDataSources();

            // Output directory need to be created if it doesn't exist
            if (!Files.exists(output)) {
                Files.createDirectories(output);
            }

            speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, buildCommandOptions.species);
            if (speciesConfiguration == null) {
                throw new CellBaseException("Invalid species: '" + buildCommandOptions.species + "'");
            }

            if (!StringUtils.isEmpty(buildCommandOptions.assembly)) {
                assembly = SpeciesUtils.getAssembly(speciesConfiguration, buildCommandOptions.assembly);
                if (assembly == null) {
                    throw new CellBaseException("Invalid assembly: '" + buildCommandOptions.assembly + "'");
                }
            } else {
                assembly = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
            }

            String ensemblVersion = assembly.getEnsemblVersion();
            ensemblRelease = "release-" + ensemblVersion.split("_")[0];

            String spShortName = getSpeciesShortname(speciesConfiguration);
            String spAssembly = assembly.getName().toLowerCase();
            Path spFolder = output.resolve(spShortName + "_" + spAssembly);
            // <output>/<species>_<assembly>/download
            downloadFolder = output.resolve(spFolder + "/download");
            if (!Files.exists(downloadFolder)) {
                throw new CellBaseException("Download folder not found '" + spShortName + "_" + spAssembly + "/download'");
            }
            // <output>/<species>_<assembly>/generated_json
            buildFolder = output.resolve(spFolder + "/generated_json");
            if (!buildFolder.toFile().exists()) {
                makeDir(buildFolder);
            }

            CellBaseBuilder parser;
            for (int i = 0; i < dataList.size(); i++) {
                data = dataList.get(i);
                switch (data) {
                    case GENOME_DATA:
                        parser = buildGenomeSequence();
                        break;
                    case GENE_DATA:
                        parser = buildGene();
                        break;
                    case REFSEQ_DATA:
                        parser = buildRefSeq();
                        break;
                    case VARIATION_FUNCTIONAL_SCORE_DATA:
                        parser = buildCadd();
                        break;
                    case MISSENSE_VARIATION_SCORE_DATA:
                        parser = buildRevel();
                        break;
                    case REGULATION_DATA:
                        parser = buildRegulation();
                        break;
                    case PROTEIN_DATA:
                        parser = buildProtein();
                        break;
                    case CONSERVATION_DATA:
                        parser = buildConservation();
                        break;
                    case CLINICAL_VARIANTS_DATA:
                        parser = buildClinicalVariants();
                        break;
                    case REPEATS_DATA:
                        parser = buildRepeats();
                        break;
                    case ONTOLOGY_DATA:
                        parser = buildObo();
                        break;
                    case SPLICE_SCORE_DATA:
                        parser = buildSplice();
                        break;
                    case PUBMED_DATA:
                        parser = buildPubMed();
                        break;
                    case PHARMACOGENOMICS_DATA:
                        parser = buildPharmacogenomics();
                        break;
                    default:
                        throw new IllegalArgumentException("Value '" + data + "' is not allowed for the data parameter."
                                + " Valid values are: " + StringUtils.join(VALID_SOURCES_TO_BUILD, ",") + "; or use 'all' to build"
                                + " everything");
                }

                if (parser != null) {
                    parser.parse();
                    parser.disconnect();
                }
            }
        } catch (Exception e) {
            String msg = "Error executing the command 'build'";
            if (StringUtils.isNotEmpty(data)) {
                msg += ". The last data being built was '" + data + "'";
            }
            throw new CellBaseException(msg + ": " + e.getMessage(), e);
        }
    }

    private CellBaseBuilder buildRepeats() throws CellBaseException {
        // Sanity check
        Path repeatsDownloadPath = downloadFolder.resolve(REPEATS_SUBDIRECTORY);
        List<Path> versionPaths = Arrays.asList(repeatsDownloadPath.resolve(TRF_VERSION_FILENAME),
                repeatsDownloadPath.resolve(GSD_VERSION_FILENAME),
                repeatsDownloadPath.resolve(WM_VERSION_FILENAME));
        copyVersionFiles(versionPaths, buildFolder.resolve(REPEATS_SUBDIRECTORY));

        // Create serializer and return the repeats builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder.resolve(REPEATS_SUBDIRECTORY), REPEATS_DATA);
        return new RepeatsBuilder(repeatsDownloadPath, serializer, configuration);
    }

    private CellBaseBuilder buildObo() throws CellBaseException {
        Path oboDownloadPath = downloadFolder.resolve(ONTOLOGY_SUBDIRECTORY);
        Path oboBuildPath = buildFolder.resolve(ONTOLOGY_SUBDIRECTORY);
        List<Path> versionPaths = Arrays.asList(oboDownloadPath.resolve(HPO_OBO_VERSION_FILENAME),
                oboDownloadPath.resolve(GO_OBO_VERSION_FILENAME),
                oboDownloadPath.resolve(DOID_OBO_VERSION_FILENAME),
                oboDownloadPath.resolve(MONDO_OBO_VERSION_FILENAME));
        copyVersionFiles(versionPaths, oboBuildPath);

        // Create serializer and return the ontology builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(oboBuildPath, OBO_BASENAME);
        return new OntologyBuilder(oboDownloadPath, serializer);
    }

    /**
     * @deprecated (when using the new copyVersionFiles)
     */
    @Deprecated
    private void copyVersionFiles(List<Path> pathList) {
        for (Path path : pathList) {
            try {
                Files.copy(path, downloadFolder.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.warn("Version file {} not found - skipping", path);
            }
        }
    }

    private CellBaseBuilder buildGenomeSequence() throws CellBaseException {
        // Sanity check
        Path genomeVersionPath = downloadFolder.resolve(GENOME_SUBDIRECTORY).resolve(GENOME_VERSION_FILENAME);
        copyVersionFiles(Collections.singletonList(genomeVersionPath), buildFolder.resolve(GENOME_SUBDIRECTORY));

        // Get FASTA path
        Path fastaPath = getFastaReferenceGenome();

        // Create serializer and return the genome builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder.resolve(GENOME_SUBDIRECTORY), GENOME_DATA);
        return new GenomeSequenceFastaBuilder(fastaPath, serializer);
    }

    private CellBaseBuilder buildGene() throws CellBaseException {
        Path geneFolderPath = downloadFolder.resolve("gene");
        copyVersionFiles(Arrays.asList(geneFolderPath.resolve("dgidbVersion.json"),
                geneFolderPath.resolve("ensemblCoreVersion.json"), geneFolderPath.resolve("uniprotXrefVersion.json"),
                geneFolderPath.resolve("geneExpressionAtlasVersion.json"),
                geneFolderPath.resolve("hpoVersion.json"), geneFolderPath.resolve("disgenetVersion.json"),
                geneFolderPath.resolve("gnomadVersion.json")));
        Path genomeFastaFilePath = getFastaReferenceGenome();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "gene");
        return new GeneBuilder(geneFolderPath, genomeFastaFilePath, speciesConfiguration, flexibleGTFParsing, serializer);
    }

    private CellBaseBuilder buildRefSeq() {
        Path refseqFolderPath = downloadFolder.resolve("refseq");
        copyVersionFiles(Arrays.asList(refseqFolderPath.resolve("refSeqVersion.json")));
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "refseq");
        return new RefSeqGeneBuilder(refseqFolderPath, speciesConfiguration, serializer);
    }

    private CellBaseBuilder buildCadd() {
        Path variationFunctionalScorePath = downloadFolder.resolve("variation_functional_score");
        copyVersionFiles(Arrays.asList(variationFunctionalScorePath.resolve("caddVersion.json")));
        Path caddFilePath = variationFunctionalScorePath.resolve("whole_genome_SNVs.tsv.gz");
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, "cadd");
        return new CaddScoreBuilder(caddFilePath, serializer);
    }

    private CellBaseBuilder buildRevel() {
        Path missensePredictionScorePath = downloadFolder.resolve(EtlCommons.MISSENSE_VARIATION_SCORE_DATA);
        copyVersionFiles(Arrays.asList(missensePredictionScorePath.resolve("revelVersion.json")));
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, EtlCommons.MISSENSE_VARIATION_SCORE_DATA);
        return new RevelScoreBuilder(missensePredictionScorePath, serializer);
    }

    private CellBaseBuilder buildRegulation() throws CellBaseException {
        // Sanity check
        Path regulationDownloadPath = downloadFolder.resolve(REGULATION_DATA);
        Path regulationBuildPath = buildFolder.resolve(REGULATION_DATA);
        copyVersionFiles(Arrays.asList(regulationDownloadPath.resolve(REGULATORY_BUILD_VERSION_FILENAME),
                regulationDownloadPath.resolve(MOTIF_FEATURES_VERSION_FILENAME)), regulationBuildPath);

        // Create the file serializer and the regulatory feature builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(regulationBuildPath, REGULATORY_REGION_BASENAME);
        return new RegulatoryFeatureBuilder(regulationDownloadPath, serializer);
    }

    private CellBaseBuilder buildProtein() throws CellBaseException {
        // Sanity check
        Path proteinDownloadPath = downloadFolder.resolve(PROTEIN_SUBDIRECTORY);
        Path proteinBuildPath = buildFolder.resolve(PROTEIN_SUBDIRECTORY);
        copyVersionFiles(Arrays.asList(proteinDownloadPath.resolve(UNIPROT_VERSION_FILENAME),
                proteinDownloadPath.resolve(INTERPRO_VERSION_FILENAME)), proteinBuildPath);

        // Create the file serializer and the protein builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(proteinBuildPath, PROTEIN_DATA);
        return new ProteinBuilder(proteinDownloadPath, speciesConfiguration.getScientificName(), serializer);
    }

    private CellBaseBuilder buildConservation() throws CellBaseException {
        // Sanity check
        Path conservationDownloadPath = downloadFolder.resolve(CONSERVATION_SUBDIRECTORY);
        copyVersionFiles(Arrays.asList(conservationDownloadPath.resolve(GERP_VERSION_FILENAME),
                conservationDownloadPath.resolve(PHASTCONS_VERSION_FILENAME), conservationDownloadPath.resolve(PHYLOP_VERSION_FILENAME)),
                buildFolder.resolve(CONSERVATION_SUBDIRECTORY));

        int conservationChunkSize = MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder.resolve(CONSERVATION_SUBDIRECTORY));
        return new ConservationBuilder(conservationDownloadPath, conservationChunkSize, serializer);
    }

    private CellBaseBuilder buildClinicalVariants() throws CellBaseException {
        Path clinicalVariantFolder = downloadFolder.resolve(EtlCommons.CLINICAL_VARIANTS_SUBDIRECTORY);

        List<Path> versionFiles = new ArrayList<>();
        List<String> versionFilenames = Arrays.asList(CLINVAR_VERSION_FILENAME, COSMIC_VERSION_FILENAME, GWAS_VERSION_FILENAME,
                HGMD_VERSION_FILENAME);
        for (String versionFilename : versionFilenames) {
            Path versionFile = clinicalVariantFolder.resolve(versionFilename);
            if (!versionFile.toFile().exists()) {
                throw new CellBaseException("Could not build clinical variants because of the file " + versionFilename + " does not exist");
            }
            versionFiles.add(versionFile);
        }
        copyVersionFiles(versionFiles);

        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder,
                EtlCommons.CLINICAL_VARIANTS_JSON_FILE.replace(".json.gz", ""), true);
        return new ClinicalVariantBuilder(clinicalVariantFolder, normalize, getFastaReferenceGenome(),
                buildCommandOptions.assembly == null ? getDefaultHumanAssembly() : buildCommandOptions.assembly,
                configuration, serializer);
    }

    private String getDefaultHumanAssembly() {
        for (SpeciesConfiguration species : configuration.getSpecies().getVertebrates()) {
            if (species.getId().equals("hsapiens")) {
                return species.getAssemblies().get(0).getName();
            }
        }

        throw new ParameterException("Clinical data can only be built if an hsapiens entry is defined within the "
                + "configuration file. No hsapiens data found within the configuration.json file");
    }

    private Path getFastaReferenceGenome() throws CellBaseException {
        // Check FASTA and unzip if necessary
        String ensemblUrl = getEnsemblUrl(configuration.getDownload().getEnsembl(), ensemblRelease, ENSEMBL_PRIMARY_FA_FILE_ID,
                getSpeciesShortname(speciesConfiguration), assembly.getName(), null);
        String fastaFilename = Paths.get(ensemblUrl).getFileName().toString();
        Path fastaPath = downloadFolder.resolve(GENOME_SUBDIRECTORY).resolve(fastaFilename);
        if (fastaPath.toFile().exists()) {
            // Gunzip
            logger.info("Gunzip file: {}", fastaPath);
            try {
                EtlCommons.runCommandLineProcess(null, "gunzip", Collections.singletonList(fastaPath.toString()), null);
            } catch (IOException e) {
                throw new CellBaseException("Error executing gunzip in FASTA file " + fastaPath, e);
            } catch (InterruptedException e) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                throw new CellBaseException("Error executing gunzip in FASTA file " + fastaPath, e);
            }
        }
        fastaPath = downloadFolder.resolve(GENOME_SUBDIRECTORY).resolve(fastaFilename.replace(".gz", ""));
        if (!fastaPath.toFile().exists()) {
            throw new CellBaseException("FASTA file " + fastaPath + " does not exist after executing gunzip");
        }
        return fastaPath;
    }

    private CellBaseBuilder buildSplice() throws IOException {
        Path spliceInputFolder = downloadFolder.resolve(EtlCommons.SPLICE_SCORE_DATA);
        Path spliceOutputFolder = buildFolder.resolve(EtlCommons.SPLICE_SCORE_DATA);
        if (!spliceOutputFolder.toFile().exists()) {
            spliceOutputFolder.toFile().mkdirs();
        }

        if (spliceInputFolder.resolve(EtlCommons.MMSPLICE_VERSION_FILENAME).toFile().exists()) {
            Files.copy(spliceInputFolder.resolve(EtlCommons.MMSPLICE_VERSION_FILENAME),
                    spliceOutputFolder.resolve(EtlCommons.MMSPLICE_VERSION_FILENAME),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(spliceOutputFolder);
        return new SpliceBuilder(spliceInputFolder, serializer);
    }

    private CellBaseBuilder buildPubMed() throws IOException {
        Path pubmedInputFolder = downloadFolder.resolve(EtlCommons.PUBMED_DATA);
        Path pubmedOutputFolder = buildFolder.resolve(EtlCommons.PUBMED_DATA);
        if (!pubmedOutputFolder.toFile().exists()) {
            pubmedOutputFolder.toFile().mkdirs();
        }

        logger.info("Copying PubMed version file...");
        if (pubmedInputFolder.resolve(EtlCommons.PUBMED_VERSION_FILENAME).toFile().exists()) {
            Files.copy(pubmedInputFolder.resolve(EtlCommons.PUBMED_VERSION_FILENAME),
                    pubmedOutputFolder.resolve(EtlCommons.PUBMED_VERSION_FILENAME),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(pubmedOutputFolder);
        return new PubMedBuilder(pubmedInputFolder, serializer);
    }

    private CellBaseBuilder buildPharmacogenomics() throws IOException {
        Path inFolder = downloadFolder.resolve(EtlCommons.PHARMACOGENOMICS_DATA);
        Path outFolder = buildFolder.resolve(EtlCommons.PHARMACOGENOMICS_DATA);
        if (!outFolder.toFile().exists()) {
            outFolder.toFile().mkdirs();
        }

        logger.info("Copying PharmGKB version file...");
        if (inFolder.resolve(PHARMGKB_DATA).resolve(EtlCommons.PHARMGKB_VERSION_FILENAME).toFile().exists()) {
            Files.copy(inFolder.resolve(PHARMGKB_DATA).resolve(EtlCommons.PHARMGKB_VERSION_FILENAME),
                    outFolder.resolve(EtlCommons.PHARMGKB_VERSION_FILENAME),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(outFolder);
        return new PharmGKBBuilder(inFolder, serializer);
    }

    private void checkVersionFiles(List<Path> versionPaths) throws CellBaseException {
        ObjectReader dataSourceReader = new ObjectMapper().readerFor(DataSource.class);
        for (Path versionPath : versionPaths) {
            if (!versionPath.toFile().exists()) {
                throw new CellBaseException("Version file " +  versionPath + " does not exist: this file is mandatory for version control");
            }
            try {
                DataSource dataSource = dataSourceReader.readValue(versionPath.toFile());
                if (org.apache.commons.lang3.StringUtils.isEmpty(dataSource.getVersion())) {
                    throw new CellBaseException("Version missing version in file " +  versionPath + ": a version must be specified in the"
                            + " file");
                }
            } catch (IOException e) {
                throw new CellBaseException("Error parsing the version file " + versionPath, e);
            }
        }
    }

    private void copyVersionFiles(List<Path> versionPaths, Path targetPath) throws CellBaseException {
        // Check version files before copying them
        checkVersionFiles(versionPaths);
        if (!targetPath.toFile().exists()) {
            try {
                Files.createDirectories(targetPath);
            } catch (IOException e) {
                throw new CellBaseException("Error creating folder " + targetPath, e);
            }
        }

        for (Path versionPath : versionPaths) {
            try {
                Files.copy(versionPath, targetPath.resolve(versionPath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new CellBaseException("Error copying version file " + versionPath + " to " + targetPath, e);
            }
            // Sanity check after copying
            if (!targetPath.resolve(versionPath.getFileName()).toFile().exists()) {
                throw new CellBaseException("Something wrong happened when copying version file " + versionPath + " to " + targetPath);
            }
        }
    }

    private List<String> checkDataSources() {
        if (StringUtils.isEmpty(buildCommandOptions.data)) {
            throw new IllegalArgumentException("Missing data parameter. Valid values are: "
                    + StringUtils.join(VALID_SOURCES_TO_BUILD, ",") + "; or use 'all' to download everything");
        }
        List<String> dataList = Arrays.asList(buildCommandOptions.data.split(","));
        for (String data : dataList) {
            switch (data) {
                case GENOME_DATA:
                case GENE_DATA:
                case REFSEQ_DATA:
                case VARIATION_FUNCTIONAL_SCORE_DATA:
                case MISSENSE_VARIATION_SCORE_DATA:
                case REGULATION_DATA:
                case PROTEIN_DATA:
                case CONSERVATION_DATA:
                case CLINICAL_VARIANTS_DATA:
                case REPEATS_DATA:
                case ONTOLOGY_DATA:
                case SPLICE_SCORE_DATA:
                case PUBMED_DATA:
                case PHARMACOGENOMICS_DATA:
                    break;
                default:
                    throw new IllegalArgumentException("Value '" + data + "' is not allowed for the data parameter. Valid values are: "
                            + StringUtils.join(VALID_SOURCES_TO_BUILD, ",") + "; or use 'all' to build everything");
            }
        }
        return dataList;
    }
}
