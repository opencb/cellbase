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

    private static final List<String> VALID_SOURCES_TO_BUILD = Arrays.asList(GENOME_DATA, GENE_DATA, VARIATION_FUNCTIONAL_SCORE_DATA,
            MISSENSE_VARIATION_SCORE_DATA, REGULATION_DATA, PROTEIN_DATA, CONSERVATION_DATA, CLINICAL_VARIANT_DATA, REPEATS_DATA,
            ONTOLOGY_DATA, SPLICE_SCORE_DATA, PUBMED_DATA, PHARMACOGENOMICS_DATA, PGS_DATA);

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
                    case CLINICAL_VARIANT_DATA:
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
                    case PGS_DATA:
                        parser = buildPolygenicScores();
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
        Path repeatsDownloadPath = downloadFolder.resolve(REPEATS_DATA);
        List<Path> versionPaths = Arrays.asList(repeatsDownloadPath.resolve(getDataVersionFilename(TRF_DATA)),
                repeatsDownloadPath.resolve(getDataVersionFilename(GSD_DATA)),
                repeatsDownloadPath.resolve(getDataVersionFilename(WM_DATA)));
        copyVersionFiles(versionPaths, buildFolder.resolve(REPEATS_DATA));

        // Create serializer and return the repeats builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(buildFolder.resolve(REPEATS_DATA), REPEATS_BASENAME);
        return new RepeatsBuilder(repeatsDownloadPath, serializer, configuration);
    }

    private CellBaseBuilder buildObo() throws CellBaseException {
        Path oboDownloadPath = downloadFolder.resolve(ONTOLOGY_DATA);
        Path oboBuildPath = buildFolder.resolve(ONTOLOGY_DATA);
        List<Path> versionPaths = Arrays.asList(oboDownloadPath.resolve(getDataVersionFilename(HPO_OBO_DATA)),
                oboDownloadPath.resolve(getDataVersionFilename(GO_OBO_DATA)),
                oboDownloadPath.resolve(getDataVersionFilename(DOID_OBO_DATA)),
                oboDownloadPath.resolve(getDataVersionFilename(MONDO_OBO_DATA)));
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
        Path genomeVersionPath = downloadFolder.resolve(GENOME_DATA).resolve(getDataVersionFilename(GENOME_DATA));
        copyVersionFiles(Collections.singletonList(genomeVersionPath), buildFolder.resolve(GENOME_DATA));

        // Get FASTA path
        Path fastaPath = getFastaReferenceGenome();

        // Create serializer and return the genome builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder.resolve(GENOME_DATA), GENOME_DATA);
        return new GenomeSequenceFastaBuilder(fastaPath, serializer);
    }

    private CellBaseBuilder buildGene() throws CellBaseException {
        return new GeneBuilder(downloadFolder.resolve(GENE_DATA), buildFolder.resolve(GENE_DATA), speciesConfiguration, flexibleGTFParsing);
    }

    private CellBaseBuilder buildCadd() throws CellBaseException {
        // Sanity check
        Path caddDownloadPath = downloadFolder.resolve(VARIATION_FUNCTIONAL_SCORE_DATA).resolve(CADD_DATA);
        Path caddBuildPath = buildFolder.resolve(VARIATION_FUNCTIONAL_SCORE_DATA).resolve(CADD_DATA);
        copyVersionFiles(Collections.singletonList(caddDownloadPath.resolve(getDataVersionFilename(CADD_DATA))), caddBuildPath);

        // Create the file serializer and the protein builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(caddBuildPath, CADD_DATA);
        return new CaddScoreBuilder(caddDownloadPath, serializer);
    }

    private CellBaseBuilder buildRevel() throws CellBaseException {
        // Sanity check
        Path revelDownloadPath = downloadFolder.resolve(MISSENSE_VARIATION_SCORE_DATA).resolve(REVEL_DATA);
        Path revelBuildPath = buildFolder.resolve(MISSENSE_VARIATION_SCORE_DATA).resolve(REVEL_DATA);
        copyVersionFiles(Collections.singletonList(revelDownloadPath.resolve(getDataVersionFilename(REVEL_DATA))), revelBuildPath);

        // Create the file serializer and the regulatory feature builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(revelBuildPath, REVEL_DATA);
        return new RevelScoreBuilder(revelDownloadPath, serializer);
    }

    private CellBaseBuilder buildRegulation() throws CellBaseException {
        // Sanity check
        Path regulationDownloadPath = downloadFolder.resolve(REGULATION_DATA);
        Path regulationBuildPath = buildFolder.resolve(REGULATION_DATA);
        copyVersionFiles(Arrays.asList(regulationDownloadPath.resolve(getDataVersionFilename(REGULATORY_BUILD_DATA)),
                regulationDownloadPath.resolve(getDataVersionFilename(MOTIF_FEATURES_DATA))), regulationBuildPath);

        // Create the file serializer and the regulatory feature builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(regulationBuildPath, REGULATORY_REGION_BASENAME);
        return new RegulatoryFeatureBuilder(regulationDownloadPath, serializer);
    }

    private CellBaseBuilder buildProtein() throws CellBaseException {
        // Sanity check
        Path proteinDownloadPath = downloadFolder.resolve(PROTEIN_DATA);
        Path proteinBuildPath = buildFolder.resolve(PROTEIN_DATA);
        copyVersionFiles(Arrays.asList(proteinDownloadPath.resolve(getDataVersionFilename(UNIPROT_DATA)),
                proteinDownloadPath.resolve(getDataVersionFilename(INTERPRO_DATA))), proteinBuildPath);

        // Create the file serializer and the protein builder
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(proteinBuildPath, PROTEIN_DATA);
        return new ProteinBuilder(proteinDownloadPath, speciesConfiguration.getScientificName(), serializer);
    }

    private CellBaseBuilder buildConservation() throws CellBaseException {
        // Sanity check
        Path conservationDownloadPath = downloadFolder.resolve(CONSERVATION_DATA);
        Path conservationBuildPath = buildFolder.resolve(CONSERVATION_DATA);
        copyVersionFiles(Arrays.asList(conservationDownloadPath.resolve(getDataVersionFilename(GERP_DATA)),
                        conservationDownloadPath.resolve(getDataVersionFilename(PHASTCONS_DATA)),
                        conservationDownloadPath.resolve(getDataVersionFilename(PHYLOP_DATA))), conservationBuildPath);

        int conservationChunkSize = MongoDBCollectionConfiguration.CONSERVATION_CHUNK_SIZE;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(conservationBuildPath);
        return new ConservationBuilder(conservationDownloadPath, conservationChunkSize, serializer);
    }

    private CellBaseBuilder buildClinicalVariants() throws CellBaseException {
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
            if (species.getId().equals(HSAPIENS_NAME)) {
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
        Path fastaPath = downloadFolder.resolve(GENOME_DATA).resolve(fastaFilename);
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
        fastaPath = downloadFolder.resolve(GENOME_DATA).resolve(fastaFilename.replace(".gz", ""));
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

    private CellBaseBuilder buildPubMed() throws CellBaseException {
        // Sanity check
        Path pubMedDownloadPath = downloadFolder.resolve(PUBMED_DATA);
        Path pubMedBuildPath = buildFolder.resolve(PUBMED_DATA);
        copyVersionFiles(Collections.singletonList(pubMedDownloadPath.resolve(getDataVersionFilename(PUBMED_DATA))), pubMedBuildPath);

        // Create the file serializer and the PubMed builder
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(pubMedBuildPath);
        return new PubMedBuilder(pubMedDownloadPath, serializer, configuration);
    }

    private CellBaseBuilder buildPharmacogenomics() throws CellBaseException {
        // Sanity check
        Path pharmGkbDownloadPath = downloadFolder.resolve(PHARMACOGENOMICS_DATA).resolve(PHARMGKB_DATA);
        Path pharmGkbBuildPath = buildFolder.resolve(PHARMACOGENOMICS_DATA).resolve(PHARMGKB_DATA);
        copyVersionFiles(Arrays.asList(pharmGkbDownloadPath.resolve(getDataVersionFilename(PHARMGKB_DATA))), pharmGkbBuildPath);

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
                case CLINICAL_VARIANT_DATA:
                case REPEATS_DATA:
                case ONTOLOGY_DATA:
                case SPLICE_SCORE_DATA:
                case PUBMED_DATA:
                case PHARMACOGENOMICS_DATA:
                case PGS_DATA:
                    break;
                default:
                    throw new IllegalArgumentException("Value '" + data + "' is not allowed for the data parameter. Valid values are: "
                            + StringUtils.join(VALID_SOURCES_TO_BUILD, ",") + "; or use 'all' to build everything");
            }
        }
        return dataList;
    }

    private CellBaseBuilder buildPolygenicScores() throws IOException {
        Path inFolder = downloadFolder.resolve(EtlCommons.PGS_DATA);
        Path outFolder = buildFolder.resolve(EtlCommons.PGS_DATA);
        if (!outFolder.toFile().exists()) {
            outFolder.toFile().mkdirs();
        }

        logger.info("Copying PGS version file...");
        if (inFolder.resolve(PGS_CATALOG_VERSION_FILENAME).toFile().exists()) {
            Files.copy(inFolder.resolve(PGS_CATALOG_VERSION_FILENAME), outFolder.resolve(PGS_CATALOG_VERSION_FILENAME),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        String basename = PolygenicScoreBuilder.VARIANT_POLYGENIC_SCORE_FILENAME.split("\\.")[0];
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(outFolder, basename);
        return new PolygenicScoreBuilder(PGS_CATALOG_NAME, configuration.getDownload().getPgs().getVersion(), inFolder, serializer);
    }
}
