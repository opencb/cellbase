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
import org.opencb.biodata.formats.pubmed.v233jaxb.PubmedArticle;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.pharma.*;
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
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.managers.*;
import org.opencb.commons.datastore.core.QueryOptions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencb.cellbase.lib.EtlCommons.*;

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
    private String token;

    private String database;
    private CellBaseManagerFactory managerFactory;

    private static final int THRESHOLD_LENGTH = 1000;

    public ExportCommandExecutor(AdminCliOptionsParser.ExportCommandOptions exportCommandOptions) {
        super(exportCommandOptions.commonOptions.logLevel, exportCommandOptions.commonOptions.conf);

        this.exportCommandOptions = exportCommandOptions;

        this.dataRelease = exportCommandOptions.dataRelease;
        this.token = exportCommandOptions.token;

        this.output = Paths.get(exportCommandOptions.output);

        this.database = exportCommandOptions.database;
        String[] splits = database.split("_");
        this.species = splits[1];
        this.assembly = splits[2];

        if (exportCommandOptions.data.equals("all")) {
            this.dataToExport = new String[]{EtlCommons.GENOME_DATA, EtlCommons.GENE_DATA, EtlCommons.REFSEQ_DATA,
                    EtlCommons.CONSERVATION_DATA, EtlCommons.REGULATION_DATA, EtlCommons.PROTEIN_DATA,
                    EtlCommons.PROTEIN_FUNCTIONAL_PREDICTION_DATA, EtlCommons.VARIATION_DATA,
                    EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA, EtlCommons.CLINICAL_VARIANTS_DATA, EtlCommons.REPEATS_DATA,
                    OBO_DATA, EtlCommons.MISSENSE_VARIATION_SCORE_DATA, EtlCommons.SPLICE_SCORE_DATA, EtlCommons.PHARMACOGENOMICS_DATA};
        } else {
            this.dataToExport = exportCommandOptions.data.split(",");
        }
    }

    /**
     * Parse specific 'data' command options.
     *
     * @throws CellBaseException CellBase exception
     */
    public void execute() throws CellBaseException {
        logger.info("Exporting from data release {}", dataRelease);
        this.managerFactory = new CellBaseManagerFactory(configuration);

        checkDataRelease();

        if (exportCommandOptions.data != null) {
            // Get genes
            List<String> geneNames = Arrays.asList(exportCommandOptions.gene.split(","));
            GeneManager geneManager = managerFactory.getGeneManager(species, assembly);
            GeneQuery geneQuery = new GeneQuery();
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
            int maxRegionSize = 50000;
            List<Region> regions = new ArrayList<>();
            for (Gene gene : genes) {
                int start = Math.max(1, gene.getStart() - THRESHOLD_LENGTH);
                int end = gene.getEnd() + THRESHOLD_LENGTH;
                logger.info("Gene {}: bounds {}:{}-{}", gene.getName(), gene.getChromosome(), start, end);
                for (int pos = start; pos < end; pos += maxRegionSize) {
                    regions.add(new Region(gene.getChromosome(), pos, Math.min(end, pos + maxRegionSize)));
                }
            }

            // Add input regions
            if (StringUtils.isNotEmpty(exportCommandOptions.region)) {
                regions.addAll(Region.parseRegions(exportCommandOptions.region));
            }

            String strRegions = StringUtils.join(regions.stream().map(Object::toString).collect(Collectors.toList()), ",");
            logger.info("{} regions: {}", regions.size(), strRegions);

            List<Variant> variants = new ArrayList<>();
            if (areVariantsNeeded()) {
                variants = getVariants(regions);
            }

            for (String loadOption : dataToExport) {
                try {
                    int counter = 0;
                    String counterMsg = "";
                    logger.info("Exporting '{}' data...", loadOption);
                    long dbTimeStart = System.currentTimeMillis();
                    switch (loadOption) {
                        case EtlCommons.GENOME_DATA: {
                            GenomeManager genomeManager = managerFactory.getGenomeManager(species, assembly);

                            // Genome sequence
                            CellBaseDataResult<GenomeSequenceChunk> results = genomeManager.getGenomeSequenceRawData(regions, dataRelease);
                            counter = writeExportedData(results.getResults(), "genome_sequence", output);

                            // Genome info
                            CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
                            results = genomeManager.getGenomeInfo(QueryOptions.empty(), dataRelease);
                            writeExportedData(results.getResults(), "genome_info", serializer);
                            serializer.close();

                            counterMsg = counter + " sequences and " + results.getNumResults() + " genome info items";
                            break;
                        }
                        case EtlCommons.GENE_DATA: {
                            // Export data
                            counter = writeExportedData(genes, "gene", output);
                            counterMsg = counter + " Ensembl genes";
                            break;
                        }
                        case EtlCommons.REFSEQ_DATA: {
                            // Export data
                            geneQuery.setSource(Collections.singletonList("refseq"));
                            geneQuery.setDataRelease(dataRelease);

                            CellBaseDataResult<Gene> results = geneManager.search(geneQuery);
                            counter = writeExportedData(results.getResults(), "refseq", output);
                            counterMsg = counter + " RefSeq genes";
                            break;
                        }
                        case EtlCommons.VARIATION_DATA: {
                            // Export data
                            counter = writeExportedData(variants, "variation_chr_all", output);
                            counterMsg = counter + " variants";
                            break;
                        }
                        case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA: {
                            // Export data
                            VariantManager variantManager = managerFactory.getVariantManager(species, assembly);
                            CellBaseDataResult<GenomicScoreRegion> results = variantManager.getFunctionalScoreRegion(regions, null,
                                    dataRelease);
                            counter = writeExportedData(results.getResults(), "cadd", output);
                            counterMsg = counter + " CADD items";
                            break;
                        }
                        case EtlCommons.MISSENSE_VARIATION_SCORE_DATA: {
                            CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output);
                            ProteinManager proteinManager = managerFactory.getProteinManager(species, assembly);
                            Map<String, List<Integer>> positionMap = new HashMap<>();
                            for (Variant variant : variants) {
                                if (!positionMap.containsKey(variant.getChromosome())) {
                                    positionMap.put(variant.getChromosome(), new ArrayList<>());
                                }
                                positionMap.get(variant.getChromosome()).add(variant.getStart());
                                if (positionMap.get(variant.getChromosome()).size() >= 200) {
                                    CellBaseDataResult<MissenseVariantFunctionalScore> results = proteinManager
                                            .getMissenseVariantFunctionalScores(variant.getChromosome(),
                                                    positionMap.get(variant.getChromosome()), null, dataRelease);
                                    counter += writeExportedData(results.getResults(), "missense_variation_functional_score", serializer);
                                    positionMap.put(variant.getChromosome(), new ArrayList<>());
                                }
                            }

                            // Process map
                            for (Map.Entry<String, List<Integer>> entry : positionMap.entrySet()) {
                                if (CollectionUtils.isEmpty(entry.getValue())) {
                                    continue;
                                }
                                CellBaseDataResult<MissenseVariantFunctionalScore> results = proteinManager
                                        .getMissenseVariantFunctionalScores(entry.getKey(), entry.getValue(), null, dataRelease);
                                counter += writeExportedData(results.getResults(), "missense_variation_functional_score", serializer);
                            }
                            serializer.close();

                            counterMsg = counter + " missense variation functional scores";
                            break;
                        }
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

                            counterMsg = counter + " conservation scores";
                            break;
                        }
                        case EtlCommons.REGULATION_DATA: {
                            RegulatoryManager regulatoryManager = managerFactory.getRegulatoryManager(species, assembly);
                            RegulationQuery query = new RegulationQuery();
                            query.setRegions(regions);
                            query.setDataRelease(dataRelease);
                            CellBaseDataResult<RegulatoryFeature> results = regulatoryManager.search(query);
                            counter = writeExportedData(results.getResults(), "regulatory_region", output);
                            counterMsg = counter + " regulatory regions";
                            break;
                        }
                        case EtlCommons.PROTEIN_DATA: {
                            ProteinManager proteinManager = managerFactory.getProteinManager(species, assembly);
                            ProteinQuery query = new ProteinQuery();
                            query.setGenes(geneNames);
                            query.setDataRelease(dataRelease);
                            CellBaseDataResult<Entry> results = proteinManager.search(query);
                            counter = writeExportedData(results.getResults(), "protein", output);
                            counterMsg = counter + " proteins";
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

                            counterMsg = counter + " protein functional predictions";
                            break;
                        }
                        case EtlCommons.CLINICAL_VARIANTS_DATA: {
                            counter = exportClinicalVariantData(regions);
                            counterMsg = counter + " clinical variants";
                            break;
                        }
                        case EtlCommons.REPEATS_DATA: {
                            // Export data
                            RepeatsManager repeatsManager = managerFactory.getRepeatsManager(species, assembly);
                            RepeatsQuery repeatsQuery = new RepeatsQuery();
                            repeatsQuery.setRegions(regions);
                            repeatsQuery.setDataRelease(dataRelease);
                            CellBaseDataResult<Repeat> results = repeatsManager.search(repeatsQuery);
                            counter = writeExportedData(results.getResults(), "repeats", output);
                            counterMsg = counter + " repeats";
                            break;
                        }
                        case OBO_DATA: {
                            counter = exportOntologyData();
                            counterMsg = counter + " ontology items";
                            break;
                        }
                        case EtlCommons.SPLICE_SCORE_DATA: {
                            counter = exportSpliceScoreData(variants);
                            counterMsg = counter + " splice scores";
                            break;
                        }
                        case EtlCommons.PHARMACOGENOMICS_DATA: {
                            counterMsg = exportPharmacogenomicsData(genes);
                            break;
                        }
                        default:
                            logger.warn("Not valid 'data'. We should not reach this point");
                            break;
                    }
                    long dbTimeEnd = System.currentTimeMillis();
                    logger.info("Exported {} in {} ms!", counterMsg, dbTimeEnd - dbTimeStart);
                } catch (IllegalAccessException | IOException | QueryException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String exportPharmacogenomicsData(List<Gene> genes)
            throws QueryException, CellBaseException, IllegalAccessException, IOException {
        String counterMsg;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output.resolve(PHARMACOGENOMICS_DATA), PHARMACOGENOMICS_DATA);

        PharmaChemicalQuery query = new PharmaChemicalQuery();
        List<String> geneNames = new ArrayList<>(new HashSet<>(genes.stream().map(Gene::getName).collect(Collectors.toList())));
        query.setGeneNames(geneNames);
        query.setDataRelease(dataRelease);
        PharmacogenomicsManager pharmacogenomicsManager = managerFactory.getPharmacogenomicsManager(species, assembly);
        CellBaseIterator<PharmaChemical> iterator = pharmacogenomicsManager.iterator(query);
        int counter = 0;
        Set<String> pubmedIds = new HashSet<>();
        Set<String> chemicalIds = new HashSet<>();
        while (iterator.hasNext()) {
            PharmaChemical pharmaChemical = iterator.next();
            if (!chemicalIds.contains(pharmaChemical.getId())) {
                // Add chemical ID to avoid duplicate
                chemicalIds.add(pharmaChemical.getId());

                // Retrieve PubMed IDs from pharma chemical (discarding empty pubmed IDs)
                for (PharmaGeneAnnotation gene : pharmaChemical.getGenes()) {
                    List<String> ids = gene.getPubmed().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(ids)) {
                        pubmedIds.addAll(ids);
                    }
                }
                for (PharmaVariantAnnotation variant : pharmaChemical.getVariants()) {
                    for (PharmaClinicalEvidence evidence : variant.getEvidences()) {
                        if (StringUtils.isNotEmpty(evidence.getPubmed())) {
                            pubmedIds.add(evidence.getPubmed());
                        }
                        for (PharmaVariantAssociation variantAssociation : evidence.getVariantAssociations()) {
                            if (StringUtils.isNotEmpty(variantAssociation.getPubmed())) {
                                pubmedIds.add(variantAssociation.getPubmed());
                            }
                        }
                    }
                }

                // Finally, write and count chemicals
                serializer.serialize(pharmaChemical);
                counter++;
                if (counter % 200 == 0) {
                    logger.info("{} pharma chemicals written....", counter);
                }
            }
        }
        serializer.close();
        counterMsg = counter + " pharma chemicals";

        // Create new JSON serializer for pubmed articles, then retrieve and write pubmed articles
        serializer = new CellBaseJsonFileSerializer(output.resolve(PUBMED_DATA), PUBMED_DATA);

        PublicationManager publicationManager = managerFactory.getPublicationManager();
        List<String> pubmedList = new ArrayList<>(pubmedIds);
        PublicationQuery publicationQuery = new PublicationQuery();
        publicationQuery.setDataRelease(dataRelease);
        counter = 0;

        int subListSize = 10;
        for (int i = 0; i < pubmedList.size(); i += subListSize) {
            int end = Math.min(i + subListSize, pubmedList.size());
            List<String> idList = pubmedList.subList(i, end);
            if (CollectionUtils.isNotEmpty(idList)) {
                publicationQuery.setIds(idList);
                CellBaseDataResult<PubmedArticle> results = publicationManager.search(publicationQuery);
                for (PubmedArticle pubmedArticle : results.getResults()) {
                    // Finally, write and count chemicals
                    serializer.serialize(pubmedArticle);
                    counter++;
                }
            }
        }
        serializer.close();
        counterMsg += " and " + counter + " PubMed articles";

        return counterMsg;
    }

    private int exportClinicalVariantData(List<Region> regions) throws CellBaseException, QueryException, IllegalAccessException,
            IOException {
        String baseFilename = CLINICAL_VARIANTS_DATA + ".full";
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output, baseFilename);
        ClinicalManager clinicalManager = managerFactory.getClinicalManager(species, assembly);
        ClinicalVariantQuery query = new ClinicalVariantQuery();
        query.setDataRelease(dataRelease);
        query.setToken(token);
        int counter = 0;
        for (Region region : regions) {
            query.setRegions(Collections.singletonList(region));
            CellBaseDataResult<Variant> results = clinicalManager.search(query);
            logger.info("{} retrieved clinical variant data from region {}", results.getNumResults(), region);
            for (Variant variant : results.getResults()) {
                serializer.serialize(variant);
                counter++;
                if (counter % 1000 == 0) {
                    logger.info("{} clinical variants written....", counter);
                }
            }
        }
        serializer.close();
        return counter;
    }

    private int exportOntologyData() throws CellBaseException, IOException {
        int counter = 0;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output, OBO_DATA);
        OntologyManager ontologyManager = managerFactory.getOntologyManager(species, assembly);
        CellBaseIterator<OntologyTerm> iterator = ontologyManager.iterator(new OntologyQuery());
        while (iterator.hasNext()) {
            serializer.serialize(iterator.next());
            counter++;
            if (counter % 5000 == 0) {
                logger.info("{} ontology terms written....", counter);
            }
        }
        serializer.close();
        return counter;
    }

    private int exportSpliceScoreData(List<Variant> variants) throws CellBaseException, IOException {
        int counter = 0;
        CellBaseFileSerializer serializer = new CellBaseJsonFileSerializer(output.resolve("splice_score"));
        serializer.getOutdir().resolve("mmsplice").toFile().mkdirs();
        serializer.getOutdir().resolve("spliceai").toFile().mkdirs();
        VariantManager variantManager = managerFactory.getVariantManager(species, assembly);
        int maxNumVariants = 200;
        for (int start = 0; start < variants.size(); start += maxNumVariants) {
            List<Variant> vars = variants.subList(start, Math.min(start + maxNumVariants, variants.size()));
            logger.info("Searching splice scores in variants [{}..{})", start, Math.min(start + maxNumVariants, variants.size()));
            List<CellBaseDataResult<SpliceScore>> resultList = variantManager.getSpliceScoreVariant(vars, null,
                    dataRelease);
            for (CellBaseDataResult<SpliceScore> result : resultList) {
                for (SpliceScore spliceScore : result.getResults()) {
                    switch (spliceScore.getSource()) {
                        case "MMSplice": {
                            serializer.serialize(spliceScore, "mmsplice/splice_score_all");
                            counter++;
                            break;
                        }
                        case "SpliceAI": {
                            serializer.serialize(spliceScore, "spliceai/splice_score_all");
                            counter++;
                            break;
                        }
                        default:
                            logger.info("Splice score unknown, skipping it!");
                            break;
                    }
                    if (counter % 10000 == 0) {
                        logger.info("{} splice scores written....", counter);
                    }
                }
            }
        }
        serializer.close();
        return counter;
    }

    private List<Variant> getVariants(List<Region> regions) throws CellBaseException {
        List<Variant> variants = new ArrayList<>();
        VariantManager variantManager = managerFactory.getVariantManager(species, assembly);
        VariantQuery query = new VariantQuery();
        query.setDataRelease(dataRelease);
        for (Region region : regions) {
            query.setRegions(Collections.singletonList(region));
            try {
                List<Variant> results = variantManager.search(query).getResults();
                logger.info("{} retrieved variants from region {}", results.size(), region);
                variants.addAll(results);
            } catch (QueryException | IllegalAccessException e) {
                throw new CellBaseException("Searching variants: " + e.getMessage());
            }
        }
        logger.info("Total variants retrieved: {}", variants.size());
        return variants;
    }

    private boolean areVariantsNeeded() {
        for (String data : dataToExport) {
            if (data.equals(EtlCommons.VARIATION_DATA)
                    || data.equals(EtlCommons.MISSENSE_VARIATION_SCORE_DATA)
                    || data.equals(EtlCommons.SPLICE_SCORE_DATA)) {
                return true;
            }
        }
        return false;
    }

    private int writeExportedData(List<?> objects, String baseFilename, CellBaseFileSerializer serializer) {
        int counter = 0;
        for (Object object : objects) {
            serializer.serialize(object, baseFilename);
            counter++;
        }
        return counter;
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

    private void checkPath(Path outDir) throws IOException {
        if (!outDir.toFile().exists() && !outDir.toFile().mkdirs()) {
            throw new IOException("Impossible to create output directory: " + outDir);
        }
    }

    private void checkDataRelease() throws CellBaseException {
        // Check data release
        DataReleaseManager dataReleaseManager = managerFactory.getDataReleaseManager(species, assembly);
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
