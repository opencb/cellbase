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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.managers.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jtarraga on 29/05/23.
 */
public class ExportCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.ExportCommandOptions exportCommandOptions;

    private String species;
    private String assembly;

    private Path output;
    private String[] dataToExport;
    private int dataRelease;

    private String database;

    private static final int THRESHOLD_LENGTH = 1000;

    public ExportCommandExecutor(AdminCliOptionsParser.ExportCommandOptions exportCommandOptions) {
        super(exportCommandOptions.commonOptions.logLevel, exportCommandOptions.commonOptions.conf);

        this.exportCommandOptions = exportCommandOptions;

        dataRelease = exportCommandOptions.dataRelease;

        output = Paths.get(exportCommandOptions.output);

        database = exportCommandOptions.database;
        String[] splits = database.split("_");
        species = splits[1];
        assembly = splits[2];

        if (exportCommandOptions.data.equals("all")) {
            dataToExport = new String[]{EtlCommons.GENOME_DATA, EtlCommons.GENE_DATA, EtlCommons.REFSEQ_DATA,
                    EtlCommons.CONSERVATION_DATA, EtlCommons.REGULATION_DATA, EtlCommons.PROTEIN_DATA,
                    EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA, EtlCommons.VARIATION_DATA,
                    EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, EtlCommons.CLINICAL_VARIANTS_DATA, EtlCommons.REPEATS_DATA,
                    EtlCommons.OBO_DATA, EtlCommons.MISSENSE_VARIATION_SCORE_DATA, EtlCommons.SPLICE_SCORE_DATA, EtlCommons.PUBMED_DATA};
        } else {
            dataToExport = exportCommandOptions.data.split(",");
        }
    }

    /**
     * Parse specific 'data' command options.
     *
     * @throws CellBaseException CellBase exception
     */
    public void execute() throws CellBaseException {
        // Check data release
        checkDataRelease();


        logger.info("Exporting from data release {}", dataRelease);

        CellBaseManagerFactory managerFactory = new CellBaseManagerFactory(configuration);

        if (exportCommandOptions.data != null) {
            // Get genes
            List<String> geneNames = Arrays.asList(exportCommandOptions.gene.split(","));
            GeneManager geneManager = managerFactory.getGeneManager(species, assembly);
            GeneQuery geneQuery = new GeneQuery();
//            geneQuery.setIds(Arrays.asList(exportCommandOptions.gene.split(",")));
            geneQuery.setNames(geneNames);
            geneQuery.setSource(Collections.singletonList("ensembl"));
            geneQuery.setDataRelease(dataRelease);
            List<Gene> genes;
            try {
                CellBaseDataResult<Gene> geneResutlts = geneManager.search(geneQuery);
                genes = geneResutlts.getResults();
            } catch (QueryException | IllegalAccessException e) {
                throw new CellBaseException(e.getMessage());
            }
            if (CollectionUtils.isEmpty(genes)) {
                throw new CellBaseException("None gene retrieved from: " + exportCommandOptions.gene);
            }
            // Extract regions from genes
            List<Region> regions = new ArrayList<>();
            for (Gene gene : genes) {
//                regions.add(new Region(gene.getChromosome(), gene.getStart() - THRESHOLD_LENGTH, gene.getEnd() + THRESHOLD_LENGTH));
                boolean first = true;
                if (CollectionUtils.isNotEmpty(gene.getTranscripts())) {
                    for (Transcript transcript : gene.getTranscripts()) {
                        if (CollectionUtils.isNotEmpty(transcript.getExons())) {
                            for (Exon exon : transcript.getExons()) {
//                                if (first) {
                                    regions.add(new Region(exon.getChromosome(), exon.getStart() - THRESHOLD_LENGTH,
                                            exon.getEnd() + THRESHOLD_LENGTH));
//                                    first = false;
//                                    break;
//                                }
                            }
                        }
                    }
                }
            }
            logger.info("{} regions: {}", regions.size(), StringUtils.join(regions.stream().map(r -> r.toString())
                    .collect(Collectors.toList()), ","));
            List<Variant> variants = new ArrayList<>();
            if (areVariantsNeeded()) {
                VariantManager variantManager = managerFactory.getVariantManager(species, assembly);
                VariantQuery query = new VariantQuery();
                query.setRegions(regions);
                query.setDataRelease(dataRelease);
                try {
                    variants = variantManager.search(query).getResults();
                    logger.info("{} variants", variants.size());
                } catch (QueryException | IllegalAccessException e) {
                    throw new CellBaseException("Searching variants: " + e.getMessage());
                }
            }

            for (String loadOption : dataToExport) {
                try {
                    int counter = 0;
                    logger.info("Exporting '{}' data...", loadOption);
                    long dbTimeStart = System.currentTimeMillis();
                    switch (loadOption) {
//                        case EtlCommons.GENOME_DATA: {
//                            break;
//                        }
                        case EtlCommons.GENE_DATA: {
                            // Export data
                            counter = writeExportedData(genes, "gene", output.resolve("gene"));
                            break;
                        }
                        case EtlCommons.REFSEQ_DATA: {
                            // Export data
                            geneQuery.setSource(Collections.singletonList("refseq"));
                            geneQuery.setDataRelease(dataRelease);

                            CellBaseDataResult<Gene> results = geneManager.search(geneQuery);
                            counter = writeExportedData(results.getResults(), "refseq", output.resolve("gene"));
                            break;
                        }
                        case EtlCommons.VARIATION_DATA: {
                            // Export data
                            counter = writeExportedData(variants, "variation", output.resolve("variation"));
                            break;
                        }
                        case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA: {
                            // Export data
                            VariantManager variantManager = managerFactory.getVariantManager(species, assembly);
                            CellBaseDataResult<GenomicScoreRegion> results = variantManager.getFunctionalScoreRegion(regions, null,
                                    dataRelease);
                            counter = writeExportedData(results.getResults(), "cadd", output.resolve("variation"));
                            break;
                        }
//                        case EtlCommons.MISSENSE_VARIATION_SCORE_DATA: {
//                            break;
//                        }
                        case EtlCommons.CONSERVATION_DATA: {
                            // Export data
                            CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
                            GenomeManager genomeManager = managerFactory.getGenomeManager(species, assembly);
                            CellBaseDataResult<GenomicScoreRegion> results = genomeManager.getConservationScoreRegion(regions, null,
                                    dataRelease);
                            for (GenomicScoreRegion scoreRegion : results.getResults()) {
                                String chromosome = scoreRegion.getChromosome();
                                if (chromosome.equals("M")) {
                                    chromosome = "MT";
                                }
                                serializer.serialize(scoreRegion, "conservation_" + chromosome);
                                counter++;
                            }
                            serializer.close();
                            break;
                        }
                        case EtlCommons.REGULATION_DATA: {
                            RegulatoryManager regulatoryManager = managerFactory.getRegulatoryManager(species, assembly);
                            RegulationQuery query = new RegulationQuery();
                            query.setRegions(regions);
                            query.setDataRelease(dataRelease);
                            CellBaseDataResult<RegulatoryFeature> results = regulatoryManager.search(query);
                            counter = writeExportedData(results.getResults(), "regulatory_region", output);
                            break;
                        }
                        case EtlCommons.PROTEIN_DATA: {
                            ProteinManager proteinManager = managerFactory.getProteinManager(species, assembly);
                            ProteinQuery query = new ProteinQuery();
                            query.setGenes(geneNames);
                            query.setDataRelease(dataRelease);
                            CellBaseDataResult<Entry> results = proteinManager.search(query);
                            counter = writeExportedData(results.getResults(), "proteins", output);
                            break;
                        }
                        case EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA: {
                            ProteinManager proteinManager = managerFactory.getProteinManager(species, assembly);
                            Map<String, List<String>> transcriptsMap = new HashMap<>();
                            for (Gene gene : genes) {
                                for (Transcript transcript : gene.getTranscripts()) {
                                    if (!transcriptsMap.containsKey(transcript.getChromosome())) {
                                        transcriptsMap.put(transcript.getChromosome(), new ArrayList<>());
                                    }
                                    transcriptsMap.get(transcript.getChromosome()).add(transcript.getId().split("\\.")[0]);
                                }
                            }
                            CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
                            for (Map.Entry<String, List<String>> entry : transcriptsMap.entrySet()) {
                                CellBaseDataResult<Object> results = proteinManager.getProteinSubstitutionRawData(entry.getValue(), null,
                                        dataRelease);
                                counter += writeExportedData(results.getResults(), "prot_func_pred_chr_" + entry.getKey(), output);
                            }
                            serializer.close();
                            break;
                        }
                        case EtlCommons.CLINICAL_VARIANTS_DATA: {
                            ClinicalManager clinicalManager = managerFactory.getClinicalManager(species, assembly);
                            ClinicalVariantQuery query = new ClinicalVariantQuery();
                            query.setRegions(regions);
                            query.setDataRelease(dataRelease);
                            CellBaseDataResult<Variant> results = clinicalManager.search(query);
                            counter = writeExportedData(results.getResults(), "clinical_variants", output);
                            break;
                        }
                        case EtlCommons.REPEATS_DATA: {
                            // Export data
                            RepeatsManager repeatsManager = managerFactory.getRepeatsManager(species, assembly);
                            RepeatsQuery repeatsQuery = new RepeatsQuery();
                            repeatsQuery.setRegions(regions);
                            repeatsQuery.setDataRelease(dataRelease);
                            CellBaseDataResult<Repeat> results = repeatsManager.search(repeatsQuery);
                            counter = writeExportedData(results.getResults(), "repeats", output.resolve("genome"));
                            break;
                        }
//                        case EtlCommons.OBO_DATA: {
//                            break;
//                        }
//                        case EtlCommons.SPLICE_SCORE_DATA: {
//                            // Load data, create index and update release
//                            loadSpliceScores();
//                            break;
//                        }
//                        case EtlCommons.PUBMED_DATA: {
//                            // Load data, create index and update release
//                            loadPubMed();
//                            break;
//                        }
                        default:
                            logger.warn("Not valid 'data'. We should not reach this point");
                            break;
                    }
                    long dbTimeEnd = System.currentTimeMillis();
                    logger.info("Exported {} '{}' items in {} ms!", counter, loadOption, dbTimeEnd - dbTimeStart);
                } catch (IllegalAccessException | IOException | QueryException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean areVariantsNeeded() {
        for (String data : dataToExport) {
            if (data.equals(EtlCommons.VARIATION_DATA)) { // || data.equals(EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA)) {
                return true;
            }
        }
        return false;
    }

    private int writeExportedData(List<?> objects, String baseFilename, Path outDir) throws IOException {
        checkPath(outDir);
        int counter = 0;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(outDir);
        for (Object object : objects) {
            serializer.serialize(object, baseFilename);
            counter++;
        }
        serializer.close();
        return counter;
    }

    private int writeExportedDataList(List<CellBaseDataResult<?>> results, String baseFilename, Path outDir) throws IOException {
        checkPath(outDir);
        int counter = 0;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(outDir);
        for (CellBaseDataResult<?> result : results) {
            for (Object object : result.getResults()) {
                serializer.serialize(object, baseFilename);
                counter++;
            }
        }
        serializer.close();
        return counter;
    }

    private void checkPath(Path outDir) throws IOException {
        if (!outDir.toFile().exists()) {
            if (!outDir.toFile().mkdirs()) {
                throw new IOException("Impossible to create output directory: " + outDir);
            }
        }
    }
//    private void loadStructuralVariants() {
//        Path path = input.resolve(EtlCommons.STRUCTURAL_VARIANTS_JSON + ".json.gz");
//        if (Files.exists(path)) {
//            try {
//                logger.debug("Loading '{}' ...", path.toString());
//                loadRunner.load(path, EtlCommons.STRUCTURAL_VARIANTS_DATA);
//                loadIfExists(input.resolve(EtlCommons.DGV_VERSION_FILE), "metadata");
//            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
//                    | IllegalAccessException | ExecutionException | IOException | InterruptedException e) {
//                logger.error(e.toString());
//            }
//        }
//    }

//    private void loadIfExists(Path path, String collection) throws NoSuchMethodException, InterruptedException,
//            ExecutionException, InstantiationException, IOException, IllegalAccessException, InvocationTargetException,
//            ClassNotFoundException, LoaderException, CellBaseException {
//        File file = new File(path.toString());
//        if (file.exists()) {
//            if (file.isFile()) {
//                loadRunner.load(path, collection, dataRelease);
//            } else {
//                logger.warn("{} is not a file - skipping", path);
//            }
//        } else {
//            logger.warn("{} does not exist - skipping", path);
//        }
//    }
//
//    private void loadVariationData() throws NoSuchMethodException, InterruptedException, ExecutionException,
//            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
//            IOException, LoaderException, CellBaseException {
//        // First load data
//        // Common loading process from CellBase variation data models
//        if (field == null) {
//            DirectoryStream<Path> stream = Files.newDirectoryStream(input,
//                    entry -> entry.getFileName().toString().startsWith("variation_chr"));
//
//            for (Path entry : stream) {
//                logger.info("Loading file '{}'", entry);
//                loadRunner.load(input.resolve(entry.getFileName()), "variation", dataRelease);
//            }
//
//            // Create index
//            createIndex("variation");
//
//            // Update release (collection and sources)
//            List<Path> sources = new ArrayList<>(Arrays.asList(
//                    input.resolve("ensemblVariationVersion.json")
//            ));
//            dataReleaseManager.update(dataRelease, "variation", EtlCommons.VARIATION_DATA, sources);
//
//            // Custom update required e.g. population freqs loading
//        } else {
//            logger.info("Loading file '{}'", input);
//            loadRunner.load(input, "variation", dataRelease, field, innerFields);
//        }
//    }
//
//    private void loadConservation() throws NoSuchMethodException, InterruptedException, ExecutionException,
//            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
//            IOException, CellBaseException, LoaderException {
//        // Load data
//        DirectoryStream<Path> stream = Files.newDirectoryStream(input,
//                entry -> entry.getFileName().toString().startsWith("conservation_"));
//
//        for (Path entry : stream) {
//            logger.info("Loading file '{}'", entry);
//            loadRunner.load(input.resolve(entry.getFileName()), "conservation", dataRelease);
//        }
//
//        // Create index
//        createIndex("conservation");
//
//        // Update release (collection and sources)
//        List<Path> sources = new ArrayList<>(Arrays.asList(
//                input.resolve("gerpVersion.json"),
//                input.resolve("phastConsVersion.json"),
//                input.resolve("phyloPVersion.json")
//        ));
//        dataReleaseManager.update(dataRelease, "conservation", EtlCommons.CONSERVATION_DATA, sources);
//    }
//
//    private void loadProteinFunctionalPrediction() throws NoSuchMethodException, InterruptedException, ExecutionException,
//            InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
//            IOException, CellBaseException, LoaderException {
//        // Load data
//        DirectoryStream<Path> stream = Files.newDirectoryStream(input,
//                entry -> entry.getFileName().toString().startsWith("prot_func_pred_"));
//
//        for (Path entry : stream) {
//            logger.info("Loading file '{}'", entry);
//            loadRunner.load(input.resolve(entry.getFileName()), "protein_functional_prediction", dataRelease);
//        }
//
//        // Create index
//        createIndex("protein_functional_prediction");
//
//        // Update release (collection and sources)
//        dataReleaseManager.update(dataRelease, "protein_functional_prediction", null, null);
//    }
//
//    private void loadClinical() throws FileNotFoundException {
//        Path path = input.resolve(EtlCommons.CLINICAL_VARIANTS_ANNOTATED_JSON_FILE);
//        if (Files.exists(path)) {
//            try {
//                // Load data
//                logger.info("Loading '{}' ...", path);
//                loadRunner.load(path, "clinical_variants", dataRelease);
//
//                // Create index
//                createIndex("clinical_variants");
//
//                // Update release (collection and sources)
//                List<Path> sources = new ArrayList<>(Arrays.asList(
//                        input.resolve("clinvarVersion.json"),
//                        input.resolve("cosmicVersion.json"),
//                        input.resolve("gwasVersion.json")
//                ));
//                dataReleaseManager.update(dataRelease, "clinical_variants", EtlCommons.CLINICAL_VARIANTS_DATA, sources);
//            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
//                    | IllegalAccessException | ExecutionException | IOException | InterruptedException | CellBaseException e) {
//                logger.error(e.toString());
//            } catch (LoaderException e) {
//                e.printStackTrace();
//            }
//        } else {
//            throw new FileNotFoundException("File " + path + " does not exist");
//        }
//    }
//
//    private void loadRepeats() {
//        Path path = input.resolve(EtlCommons.REPEATS_JSON + ".json.gz");
//        if (Files.exists(path)) {
//            try {
//                // Load data
//                logger.debug("Loading '{}' ...", path);
//                loadRunner.load(path, "repeats", dataRelease);
//
//                // Create index
//                createIndex("repeats");
//
//                // Update release (collection and sources)
//                List<Path> sources = new ArrayList<>(Arrays.asList(
//                        input.resolve(EtlCommons.TRF_VERSION_FILE),
//                        input.resolve(EtlCommons.GSD_VERSION_FILE),
//                        input.resolve(EtlCommons.WM_VERSION_FILE)
//                ));
//                dataReleaseManager.update(dataRelease, "repeats", EtlCommons.REPEATS_DATA, sources);
//            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
//                    | IllegalAccessException | ExecutionException | IOException | InterruptedException | CellBaseException e) {
//                logger.error(e.toString());
//            } catch (LoaderException e) {
//                e.printStackTrace();
//            }
//        } else {
//            logger.warn("Repeats file {} not found", path);
//            logger.warn("No repeats data will be loaded");
//        }
//    }
//
//    private void loadSpliceScores() throws NoSuchMethodException, InterruptedException, ExecutionException, InstantiationException,
//            IllegalAccessException, InvocationTargetException, ClassNotFoundException, IOException, CellBaseException, LoaderException {
//        // Load data
//        logger.info("Loading splice scores from '{}'", input);
//        // MMSplice scores
//        loadSpliceScores(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.MMSPLICE_SUBDIRECTORY));
//        // SpliceAI scores
//        loadSpliceScores(input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.SPLICEAI_SUBDIRECTORY));
//
//        // Create index
//        createIndex("splice_score");
//
//        // Update release (collection and sources)
//        List<Path> sources = new ArrayList<>(Arrays.asList(
//                input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.MMSPLICE_VERSION_FILENAME),
//                input.resolve(EtlCommons.SPLICE_SCORE_DATA + "/" + EtlCommons.SPLICEAI_VERSION_FILENAME)
//        ));
//        dataReleaseManager.update(dataRelease, "splice_score", EtlCommons.SPLICE_SCORE_DATA, sources);
//    }
//
//    private void loadSpliceScores(Path spliceFolder) throws IOException, ExecutionException, InterruptedException,
//            ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
//            LoaderException, CellBaseException {
//        // Get files from folder
//        DirectoryStream<Path> stream = Files.newDirectoryStream(spliceFolder,
//                entry -> entry.getFileName().toString().startsWith("splice_score_"));
//
//        // Load from JSON files
//        for (Path entry : stream) {
//            logger.info("Loading file '{}'", entry);
//            loadRunner.load(spliceFolder.resolve(entry.getFileName()), "splice_score", dataRelease);
//        }
//    }
//
//    private void loadPubMed() throws CellBaseException {
//        Path pubmedPath = input.resolve(EtlCommons.PUBMED_DATA);
//
//        if (Files.exists(pubmedPath)) {
//            // Load data
//            for (File file : pubmedPath.toFile().listFiles()) {
//                if (file.isFile() && (file.getName().endsWith("gz"))) {
//                    logger.info("Loading file '{}'", file.getName());
//                    try {
//                        loadRunner.load(file.toPath(), EtlCommons.PUBMED_DATA, dataRelease);
//                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException
//                            | IllegalAccessException | ExecutionException | IOException | InterruptedException | LoaderException e) {
//                        logger.error("Error loading file '{}': {}", file.getName(), e.toString());
//                    }
//                }
//            }
//            // Create index
//            createIndex(EtlCommons.PUBMED_DATA);
//
//            // Update release (collection and sources)
//            List<Path> sources = Collections.singletonList(pubmedPath.resolve(EtlCommons.PUBMED_VERSION_FILENAME));
//            dataReleaseManager.update(dataRelease, "pubmed", EtlCommons.REPEATS_DATA, sources);
//        } else {
//            logger.warn("PubMed folder {} not found", pubmedPath);
//        }
//    }

    private void checkDataRelease() throws CellBaseException {
        // Check data release
        DataReleaseManager dataReleaseManager = new DataReleaseManager(database, configuration);
        CellBaseDataResult<DataRelease> dataReleaseResults = dataReleaseManager.getReleases();
        if (CollectionUtils.isEmpty(dataReleaseResults.getResults())) {
            throw new CellBaseException("No data releases are available");
        }

        List<Integer> dataReleaseList = new ArrayList<>();
        for (DataRelease dr : dataReleaseResults.getResults()) {
            if (dr.getRelease() == dataRelease) {
                return;
            }
            dataReleaseList.add(dr.getRelease());
        }

        throw new CellBaseException("Invalid data release: " + dataRelease + ". Valid data releases are: "
                + StringUtils.join(dataReleaseList, ","));
    }
}
