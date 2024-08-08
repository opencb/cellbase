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
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;
import static org.opencb.cellbase.lib.builders.AbstractBuilder.BUILDING_DONE_LOG_MESSAGE;
import static org.opencb.cellbase.lib.builders.AbstractBuilder.BUILDING_LOG_MESSAGE;
import static org.opencb.cellbase.lib.builders.EnsemblGeneBuilder.ENSEMBL_GENE_OUTPUT_FILENAME;
import static org.opencb.cellbase.lib.builders.GenomeSequenceFastaBuilder.GENOME_JSON_FILENAME;
import static org.opencb.cellbase.lib.builders.OntologyBuilder.OBO_OUTPUT_BASENAME;
import static org.opencb.cellbase.lib.builders.ProteinBuilder.PROTEIN_OUTPUT_FILENAME;
import static org.opencb.cellbase.lib.builders.RefSeqGeneBuilder.REFSEQ_GENE_OUTPUT_FILENAME;
import static org.opencb.cellbase.lib.builders.RegulatoryFeatureBuilder.*;
import static org.opencb.cellbase.lib.builders.RepeatsBuilder.REPEATS_OUTPUT_FILENAME;
import static org.opencb.cellbase.lib.builders.VariationBuilder.VARIATION_CHR_PREFIX;
import static org.opencb.cellbase.lib.download.GenomeDownloadManager.GENOME_INFO_FILENAME;


public class BuildCommandExecutor extends CommandExecutor {

    private final AdminCliOptionsParser.BuildCommandOptions buildCommandOptions;
    private final Path outputDirectory;

    private Path buildFolder = null;
    private Path downloadFolder = null;
    private boolean normalize = true;

    private SpeciesConfiguration speciesConfiguration;
    private SpeciesConfiguration.Assembly assembly;
    private String ensemblRelease;

    private boolean flexibleGTFParsing;

    public BuildCommandExecutor(AdminCliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
        this.outputDirectory = Paths.get(buildCommandOptions.outputDirectory);
        normalize = !buildCommandOptions.skipNormalize;

        this.flexibleGTFParsing = buildCommandOptions.flexibleGTFParsing;
    }

    /**
     * Parse specific 'build' command options.
     *
     * @throws CellBaseException Exception
     */
    public void execute() throws CellBaseException {
        try {
            // Output directory need to be created if it doesn't exist
            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
            }

            // Get the species
            String species = buildCommandOptions.species;
            speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, species);
            if (speciesConfiguration == null) {
                throw new CellBaseException("Invalid species: '" + buildCommandOptions.species + "'");
            }

            // Get the assembly
            if (StringUtils.isNotEmpty(buildCommandOptions.assembly)) {
                assembly = SpeciesUtils.getAssembly(speciesConfiguration, buildCommandOptions.assembly);
                if (assembly == null) {
                    throw new CellBaseException("Invalid assembly: '" + buildCommandOptions.assembly + "'");
                }
            } else {
                assembly = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
            }

            String ensemblVersion = assembly.getEnsemblVersion();
            ensemblRelease = "release-" + ensemblVersion.split("_")[0];

            String spShortName = SpeciesUtils.getSpeciesShortname(speciesConfiguration);
            String spAssembly = assembly.getName().toLowerCase();
            Path spFolder = outputDirectory.resolve(spShortName + "_" + spAssembly);
            downloadFolder = outputDirectory.resolve(spFolder + "/download");
            if (!Files.exists(downloadFolder)) {
                throw new CellBaseException("Download folder not found '" + spShortName + "_" + spAssembly + "/download'");
            }
            buildFolder = outputDirectory.resolve(spFolder + "/generated_json");
            if (!Files.exists(buildFolder)) {
                Files.createDirectories(buildFolder);
            }

            // Check data sources
            List<String> dataList = getDataList(species, speciesConfiguration);
            AbstractBuilder parser;
            for (String data : dataList) {
                switch (data) {
                    case GENOME_DATA:
                        parser = buildGenomeSequence();
                        break;
                    case CONSERVATION_DATA:
                        parser = buildConservation();
                        break;
                    case REPEATS_DATA:
                        parser = buildRepeats();
                        break;
                    case GENE_DATA:
                        parser = buildGene();
                        break;
                    case PROTEIN_DATA:
                        parser = buildProtein();
                        break;
                    case VARIATION_DATA:
                        parser = buildVariation();
                        break;
                    case REGULATION_DATA:
                        parser = buildRegulation();
                        break;
                    case VARIATION_FUNCTIONAL_SCORE_DATA:
                        parser = buildCadd();
                        break;
                    case MISSENSE_VARIATION_SCORE_DATA:
                        parser = buildRevel();
                        break;
                    case CLINICAL_VARIANT_DATA:
                        parser = buildClinicalVariants();
                        break;
                    case SPLICE_SCORE_DATA:
                        parser = buildSplice();
                        break;
                    case ONTOLOGY_DATA:
                        parser = buildObo();
                        break;
                    case PUBMED_DATA:
                        parser = buildPubMed();
                        break;
                    case PHARMACOGENOMICS_DATA:
                        parser = buildPharmacogenomics();
                        break;
                    default:
                        throw new IllegalArgumentException("Data parameter '" + data + "' is not allowed for '" + species + "'. "
                                + "Valid values are: " + StringUtils.join(speciesConfiguration.getData(), ",")
                                + ". You can use data parameter 'all' to download everything");
                }
                if (parser != null) {
                    parser.parse();
                    parser.disconnect();
                    logger.info(BUILDING_DONE_LOG_MESSAGE);
                }
            }
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new CellBaseException("Error executing command line 'build': " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CellBaseException("Error executing command line 'build': " + e.getMessage(), e);
        }
    }

    private AbstractBuilder buildGenomeSequence() throws CellBaseException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(GENOME_DATA));

        Path genomeDownloadFolder = downloadFolder.resolve(GENOME_DATA);
        Path genomeBuildFolder = buildFolder.resolve(GENOME_DATA);

        if (Files.exists(genomeBuildFolder.resolve(GENOME_JSON_FILENAME))
                && Files.exists(genomeBuildFolder.resolve(GENOME_INFO_FILENAME))
                && Files.exists(genomeBuildFolder.resolve(getDataVersionFilename(GENOME_DATA)))) {
            logger.warn(DATA_ALREADY_BUILT, getDataName(GENOME_DATA));
            return null;
        }

        // Sanity check
        if (!Files.exists(genomeDownloadFolder.resolve(GENOME_INFO_FILENAME))) {
            throw new CellBaseException("Genome info file " + GENOME_INFO_FILENAME + " does not exist at " + genomeDownloadFolder);
        }

        // Copy files if necessary
        if (!Files.exists(genomeBuildFolder.resolve(getDataVersionFilename(GENOME_DATA)))) {
            Path genomeVersionPath = genomeDownloadFolder.resolve(getDataVersionFilename(GENOME_DATA));
            copyVersionFiles(Collections.singletonList(genomeVersionPath), buildFolder.resolve(GENOME_DATA));
        }

        if (!Files.exists(genomeBuildFolder.resolve(GENOME_INFO_FILENAME))) {
            try {
                Files.copy(genomeDownloadFolder.resolve(GENOME_INFO_FILENAME), genomeBuildFolder.resolve(GENOME_INFO_FILENAME));
            } catch (IOException e) {
                throw new CellBaseException("Error copying file " + GENOME_INFO_FILENAME, e);
            }
        }

        // Parse file
        if (!Files.exists(genomeBuildFolder.resolve(GENOME_JSON_FILENAME))) {
            // Get FASTA path
            Path fastaPath = getFastaReferenceGenome();

            // Create serializer and return the genome builder
            CellBaseSerializer serializer = new CellBaseJsonFileSerializer(genomeBuildFolder, GENOME_DATA);
            return new GenomeSequenceFastaBuilder(fastaPath, serializer);
        }
        return null;
    }

    private AbstractBuilder buildGene() throws CellBaseException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(GENE_DATA));

        // Sanity check
        Path geneDownloadPath = downloadFolder.resolve(GENE_DATA);
        Path geneBuildPath = buildFolder.resolve(GENE_DATA);

        List<Path> versionFiles = new ArrayList<>(Arrays.asList(
                geneDownloadPath.resolve(ENSEMBL_DATA).resolve(getDataVersionFilename(ENSEMBL_DATA)),
                geneDownloadPath.resolve(REFSEQ_DATA).resolve(getDataVersionFilename(REFSEQ_DATA))));
        List<String> dataList = GeneBuilder.getCommonDataSources(speciesConfiguration, configuration);
        for (String data : dataList) {
            Path versionFile;
            switch (data) {
                case MIRTARBASE_DATA:
                    versionFile = downloadFolder.resolve(REGULATION_DATA).resolve(MIRTARBASE_DATA).resolve(getDataVersionFilename(data));
                    break;
                case MIRBASE_DATA:
                    versionFile = downloadFolder.resolve(REGULATION_DATA).resolve(MIRBASE_DATA).resolve(getDataVersionFilename(data));
                    break;
                default:
                    versionFile = downloadFolder.resolve(GERP_DATA).resolve(getDataVersionFilename(data));
                    break;
            }
            versionFiles.add(versionFile);
        }

        List<Path> filesToCheck = new ArrayList<>(Arrays.asList(geneBuildPath.resolve(ENSEMBL_GENE_OUTPUT_FILENAME),
                geneBuildPath.resolve(REFSEQ_GENE_OUTPUT_FILENAME)));
        for (Path versionFile : versionFiles) {
            filesToCheck.add(geneBuildPath.resolve(versionFile.getFileName()));
        }
        filesToCheck.addAll(versionFiles);

        if (AbstractBuilder.existFiles(filesToCheck)) {
            logger.warn(DATA_ALREADY_BUILT, getDataName(ENSEMBL_DATA) + " and " + getDataName(REFSEQ_DATA) + " genes");
            return null;
        }

        copyVersionFiles(versionFiles, geneBuildPath);

        return new GeneBuilder(geneDownloadPath, geneBuildPath, speciesConfiguration, flexibleGTFParsing, configuration);
    }

    private AbstractBuilder buildRepeats() throws CellBaseException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(REPEATS_DATA));

        // Sanity check
        Path repeatsDownloadPath = downloadFolder.resolve(REPEATS_DATA);
        Path repeatsBuildPath = buildFolder.resolve(REPEATS_DATA);
        List<String> dataList = EtlCommons.getDataList(REPEATS_DATA, configuration, speciesConfiguration);
        List<Path> filesToCheck = new ArrayList<>(Arrays.asList(repeatsBuildPath.resolve(REPEATS_OUTPUT_FILENAME)));
        for (String data : dataList) {
            filesToCheck.add(repeatsBuildPath.resolve(getDataVersionFilename(data)));
        }
        if (AbstractBuilder.existFiles(filesToCheck)) {
            logger.warn(DATA_ALREADY_BUILT, getDataName(REPEATS_DATA));
            return null;
        }
        for (String data : dataList) {
            checkVersionFiles(Collections.singletonList(repeatsDownloadPath.resolve(data).resolve(getDataVersionFilename(data))));
        }
        for (String data : dataList) {
            copyVersionFiles(Collections.singletonList(repeatsDownloadPath.resolve(data).resolve(getDataVersionFilename(data))),
                    repeatsBuildPath);
        }

        // Create serializer and return the repeats builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder.resolve(REPEATS_DATA), REPEATS_DATA);
        return new RepeatsBuilder(dataList, repeatsDownloadPath, serializer, configuration);
    }

    private AbstractBuilder buildObo() throws CellBaseException {
        // Sanity check
        Path oboDownloadPath = downloadFolder.resolve(ONTOLOGY_DATA);
        Path oboBuildPath = buildFolder.resolve(ONTOLOGY_DATA);
        List<Path> filesToCheck = new ArrayList<>(Arrays.asList(oboBuildPath.resolve(OBO_OUTPUT_BASENAME)));
        List<String> dataList = new ArrayList<>(Arrays.asList(GO_OBO_DATA));
        if (speciesConfiguration.getScientificName().equalsIgnoreCase(HOMO_SAPIENS)) {
            dataList.add(HPO_OBO_DATA);
            dataList.add(DOID_OBO_DATA);
            dataList.add(MONDO_OBO_DATA);
        }

        for (String data : dataList) {
            filesToCheck.add(oboBuildPath.resolve(data).resolve(getDataVersionFilename(data)));
        }

        if (AbstractBuilder.existFiles(filesToCheck)) {
            logger.warn(DATA_ALREADY_BUILT, getDataName(ONTOLOGY_DATA));
            return null;
        }

        for (String data : dataList) {
            checkVersionFiles(Collections.singletonList(oboDownloadPath.resolve(data).resolve(getDataVersionFilename(data))));
        }
        for (String data : dataList) {
            copyVersionFiles(Collections.singletonList(oboDownloadPath.resolve(data).resolve(getDataVersionFilename(data))),
                    oboBuildPath);
        }

        // Create serializer and return the ontology builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(oboBuildPath, OBO_OUTPUT_BASENAME);
        return new OntologyBuilder(oboDownloadPath, speciesConfiguration, serializer);
    }

    private AbstractBuilder buildCadd() throws CellBaseException {
        // Sanity check
        Path caddDownloadPath = downloadFolder.resolve(VARIATION_FUNCTIONAL_SCORE_DATA).resolve(CADD_DATA);
        Path caddBuildPath = buildFolder.resolve(VARIATION_FUNCTIONAL_SCORE_DATA).resolve(CADD_DATA);
        copyVersionFiles(Collections.singletonList(caddDownloadPath.resolve(getDataVersionFilename(CADD_DATA))), caddBuildPath);

        // Create the file serializer and the protein builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(caddBuildPath, CADD_DATA);
        return new CaddScoreBuilder(caddDownloadPath, serializer);
    }

    private AbstractBuilder buildRevel() throws CellBaseException {
        // Sanity check
        Path revelDownloadPath = downloadFolder.resolve(MISSENSE_VARIATION_SCORE_DATA).resolve(REVEL_DATA);
        Path revelBuildPath = buildFolder.resolve(MISSENSE_VARIATION_SCORE_DATA).resolve(REVEL_DATA);
        copyVersionFiles(Collections.singletonList(revelDownloadPath.resolve(getDataVersionFilename(REVEL_DATA))), revelBuildPath);

        // Create the file serializer and the regulatory feature builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(revelBuildPath, REVEL_DATA);
        return new RevelScoreBuilder(revelDownloadPath, serializer);
    }

    private AbstractBuilder buildRegulation() throws CellBaseException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(REGULATION_DATA));

        // Sanity check
        Path regulationDownloadPath = downloadFolder.resolve(REGULATION_DATA);
        Path regulationBuildPath = buildFolder.resolve(REGULATION_DATA);
        List<Path> filesToCheck = Arrays.asList(regulationBuildPath.resolve(REGULATORY_REGION_OUTPUT_FILENAME),
                regulationBuildPath.resolve(REGULATORY_PFM_OUTPUT_FILENAME),
                regulationBuildPath.resolve(getDataVersionFilename(REGULATORY_BUILD_DATA)),
                regulationBuildPath.resolve(getDataVersionFilename(MOTIF_FEATURES_DATA)));
        if (AbstractBuilder.existFiles(filesToCheck)) {
            logger.warn(DATA_ALREADY_BUILT, getDataName(REGULATION_DATA));
            return null;
        }

        copyVersionFiles(Arrays.asList(regulationDownloadPath.resolve(REGULATORY_BUILD_DATA).resolve(getDataVersionFilename(
                REGULATORY_BUILD_DATA)), regulationDownloadPath.resolve(MOTIF_FEATURES_DATA).resolve(getDataVersionFilename(
                MOTIF_FEATURES_DATA))), regulationBuildPath);

        // Create the file serializer and the regulatory feature builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(regulationBuildPath, REGULATORY_REGION_BASENAME);
        return new RegulatoryFeatureBuilder(regulationDownloadPath, serializer);
    }

    private AbstractBuilder buildProtein() throws CellBaseException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(PROTEIN_DATA));

        // Sanity check
        Path proteinDownloadPath = downloadFolder.resolve(PROTEIN_DATA);
        Path proteinBuildPath = buildFolder.resolve(PROTEIN_DATA);
        List<Path> filesToCheck = Arrays.asList(proteinBuildPath.resolve(PROTEIN_OUTPUT_FILENAME),
                proteinBuildPath.resolve(getDataVersionFilename(INTERPRO_DATA)),
                proteinBuildPath.resolve(getDataVersionFilename(INTACT_DATA)),
                proteinBuildPath.resolve(getDataVersionFilename(UNIPROT_DATA)));
        if (AbstractBuilder.existFiles(filesToCheck)) {
            logger.warn(DATA_ALREADY_BUILT, getDataName(PROTEIN_DATA));
            return null;
        }

        copyVersionFiles(Arrays.asList(proteinDownloadPath.resolve(INTERPRO_DATA).resolve(getDataVersionFilename(
                INTERPRO_DATA)), proteinDownloadPath.resolve(INTACT_DATA).resolve(getDataVersionFilename(
                INTACT_DATA)), proteinDownloadPath.resolve(UNIPROT_DATA).resolve(getDataVersionFilename(
                UNIPROT_DATA))), proteinBuildPath);

        // Create the file serializer and the protein builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(proteinBuildPath, PROTEIN_DATA);
        return new ProteinBuilder(proteinDownloadPath, speciesConfiguration.getScientificName(), serializer);
    }

    private AbstractBuilder buildVariation() throws CellBaseException, IOException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(VARIATION_DATA));

        // Sanity check
        Path variationDownloadPath = downloadFolder.resolve(VARIATION_DATA);
        Path variationBuildPath = buildFolder.resolve(VARIATION_DATA);

        if (Files.exists(variationBuildPath)) {
            List<Path> filesToCheck = new ArrayList<>();
            if (!speciesConfiguration.getId().equalsIgnoreCase(HSAPIENS)) {
                filesToCheck.add(variationBuildPath.resolve(getDataVersionFilename(VARIATION_DATA)));
            }

            try (DirectoryStream<Path> vcfPaths = Files.newDirectoryStream(variationBuildPath,
                    entry -> entry.getFileName().toString().startsWith(VARIATION_CHR_PREFIX))) {
                if (AbstractBuilder.existFiles(filesToCheck) && vcfPaths.iterator().hasNext()) {
                    logger.warn(DATA_ALREADY_BUILT, getDataName(VARIATION_DATA));
                    return null;
                }
            }
        }

        // Copy version files
        if (!speciesConfiguration.getId().equalsIgnoreCase(HSAPIENS)) {
            copyVersionFiles(Arrays.asList(variationDownloadPath.resolve(getDataVersionFilename(VARIATION_DATA))), variationBuildPath);
        }

        // Create the file serializer and the variation builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(variationBuildPath);
        return new VariationBuilder(variationDownloadPath, speciesConfiguration.getScientificName(), serializer);
    }

    private AbstractBuilder buildConservation() throws CellBaseException {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(CONSERVATION_DATA));

        // Sanity check
        Path conservationDownloadPath = downloadFolder.resolve(CONSERVATION_DATA);
        Path conservationBuildPath = buildFolder.resolve(CONSERVATION_DATA);
        List<String> dataList = Arrays.asList(GERP_DATA, PHASTCONS_DATA, PHYLOP_DATA);
        for (String data : dataList) {
            checkVersionFiles(Collections.singletonList(conservationDownloadPath.resolve(data).resolve(getDataVersionFilename(data))));
        }
        copyVersionFiles(Arrays.asList(conservationDownloadPath.resolve(GERP_DATA).resolve(getDataVersionFilename(GERP_DATA)),
                conservationDownloadPath.resolve(PHASTCONS_DATA).resolve(getDataVersionFilename(PHASTCONS_DATA)),
                conservationDownloadPath.resolve(PHYLOP_DATA).resolve(getDataVersionFilename(PHYLOP_DATA))), conservationBuildPath);

        int conservationChunkSize = MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(conservationBuildPath);
        return new ConservationBuilder(conservationDownloadPath, conservationChunkSize, serializer);
    }

    private AbstractBuilder buildClinicalVariants() throws CellBaseException {
        // Sanity check
        Path clinicalDownloadPath = downloadFolder.resolve(CLINICAL_VARIANT_DATA);
        Path clinicalBuildPath = buildFolder.resolve(CLINICAL_VARIANT_DATA);
        copyVersionFiles(Arrays.asList(clinicalDownloadPath.resolve(getDataVersionFilename(CLINVAR_DATA)),
                clinicalDownloadPath.resolve(getDataVersionFilename(COSMIC_DATA)),
                clinicalDownloadPath.resolve(getDataVersionFilename(HGMD_DATA)),
                clinicalDownloadPath.resolve(getDataVersionFilename(GWAS_DATA))), clinicalBuildPath);

        // Create the file serializer and the clinical variants builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(clinicalBuildPath, CLINICAL_VARIANTS_BASENAME, true);
        return new ClinicalVariantBuilder(clinicalDownloadPath, normalize, getFastaReferenceGenome(),
                buildCommandOptions.assembly == null ? getDefaultHumanAssembly() : buildCommandOptions.assembly,
                configuration, serializer);
    }

    private String getDefaultHumanAssembly() {
        for (SpeciesConfiguration species : configuration.getSpecies().getVertebrates()) {
            if (species.getId().equals(HSAPIENS)) {
                return species.getAssemblies().get(0).getName();
            }
        }

        throw new ParameterException("Clinical data can only be built if an hsapiens entry is defined within the "
                + "configuration file. No hsapiens data found within the configuration.json file");
    }

    private Path getFastaReferenceGenome() throws CellBaseException {
        // Check FASTA and unzip if necessary
        String ensemblUrl = getEnsemblUrl(configuration.getDownload().getEnsembl(), ensemblRelease, ENSEMBL_PRIMARY_FA_FILE_ID,
                SpeciesUtils.getSpeciesShortname(speciesConfiguration), assembly.getName(), null);
        String fastaFilename = Paths.get(ensemblUrl).getFileName().toString();
        Path gzFastaPath = downloadFolder.resolve(GENOME_DATA).resolve(fastaFilename);

        return EtlCommons.getFastaPath(gzFastaPath);
    }

    private AbstractBuilder buildSplice() throws IOException, CellBaseException {
        Path spliceInputFolder = downloadFolder.resolve(EtlCommons.SPLICE_SCORE_DATA);
        Path spliceOutputFolder = buildFolder.resolve(EtlCommons.SPLICE_SCORE_DATA);
        if (!spliceOutputFolder.toFile().exists()) {
            spliceOutputFolder.toFile().mkdirs();
        }

        if (spliceInputFolder.resolve(getDataVersionFilename(MMSPLICE_DATA)).toFile().exists()) {
            Files.copy(spliceInputFolder.resolve(getDataVersionFilename(MMSPLICE_DATA)),
                    spliceOutputFolder.resolve(EtlCommons.getDataVersionFilename(MMSPLICE_DATA)),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(spliceOutputFolder);
        return new SpliceBuilder(spliceInputFolder, serializer);
    }

    private AbstractBuilder buildPubMed() throws CellBaseException {
        // Sanity check
        Path pubMedDownloadPath = downloadFolder.resolve(PUBMED_DATA);
        Path pubMedBuildPath = buildFolder.resolve(PUBMED_DATA);
        copyVersionFiles(Collections.singletonList(pubMedDownloadPath.resolve(getDataVersionFilename(PUBMED_DATA))), pubMedBuildPath);

        // Create the file serializer and the PubMed builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(pubMedBuildPath);
        return new PubMedBuilder(pubMedDownloadPath, serializer, configuration);
    }

    private AbstractBuilder buildPharmacogenomics() throws CellBaseException {
        // Sanity check
        Path pharmGkbDownloadPath = downloadFolder.resolve(PHARMACOGENOMICS_DATA).resolve(PHARMGKB_DATA);
        Path pharmGkbBuildPath = buildFolder.resolve(PHARMACOGENOMICS_DATA).resolve(PHARMGKB_DATA);
        copyVersionFiles(Collections.singletonList(pharmGkbDownloadPath.resolve(getDataVersionFilename(PHARMGKB_DATA))), pharmGkbBuildPath);

        // Create the file serializer and the PharmGKB feature builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(pharmGkbBuildPath);
        return new PharmGKBBuilder(pharmGkbDownloadPath, serializer);
    }

    private void checkVersionFiles(List<Path> versionPaths) throws CellBaseException {
        ObjectReader dataSourceReader = new ObjectMapper().readerFor(DataSource.class);
        for (Path versionPath : versionPaths) {
            if (!versionPath.toFile().exists()) {
                throw new CellBaseException("Version file " +  versionPath + " does not exist: this file is mandatory for version control");
            }
            try {
                DataSource dataSource = dataSourceReader.readValue(versionPath.toFile());
                if (StringUtils.isEmpty(dataSource.getVersion())) {
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
        copyFiles(versionPaths, targetPath);
    }

    private void copyFiles(List<Path> versionPaths, Path targetPath) throws CellBaseException {
        if (!Files.exists(targetPath)) {
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

    private List<String> getDataList(String species, SpeciesConfiguration speciesConfig) throws CellBaseException {
        // No need to check if 'data' exists since it is declared as required in JCommander
        List<String> dataList;
        if ("all".equalsIgnoreCase(buildCommandOptions.data)) {
            // Download all data sources for the species in the configuration.yml file
            dataList = speciesConfig.getData();
        } else {
            // Check if the data sources requested are valid for the species
            dataList = Arrays.asList(buildCommandOptions.data.split(","));
            for (String data : dataList) {
                if (!speciesConfig.getData().contains(data)) {
                    throw new CellBaseException("Data parameter '" + data + "' does not exist or it is not allowed for '" + species + "'. "
                            + "Valid values are: " + StringUtils.join(speciesConfig.getData(), ",") + ". "
                            + "You can use data parameter 'all' to build everything");
                }
            }
        }
        return dataList;
    }

}
