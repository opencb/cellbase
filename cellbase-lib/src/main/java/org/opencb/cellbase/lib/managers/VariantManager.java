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

package org.opencb.cellbase.lib.managers;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomicScoreRegion;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.avro.SampleEntry;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.VariantQuery;
import org.opencb.cellbase.core.api.query.CellBaseQueryOptions;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.variant.AnnotationBasedPhasedQueryManager;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.impl.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.lib.impl.core.SpliceScoreMongoDBAdaptor;
import org.opencb.cellbase.lib.impl.core.VariantMongoDBAdaptor;
import org.opencb.cellbase.core.api.key.ApiKeyLicensedDataUtils;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
import org.opencb.cellbase.lib.variant.annotation.CellBaseNormalizerSequenceAdaptor;
import org.opencb.cellbase.lib.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.lib.variant.hgvs.HgvsCalculator;
import org.opencb.commons.datastore.core.Event;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class VariantManager extends AbstractManager implements AggregationApi<VariantQuery, Variant> {

    private static final String PHASE_DATA_URL_SEPARATOR = "\\+";
    private static final String VARIANT_STRING_FORMAT = "(chr)"
            + ":[(cipos_left)<](start)[<(cipos_right)]" + "[-[(ciend_left)<](end)[<(ciend_right)]]"
            + "[:(ref)]"
            + ":[(alt)|(left_ins_seq)...(right_ins_seq)]";
    private VariantMongoDBAdaptor variantDBAdaptor;
    private SpliceScoreMongoDBAdaptor spliceDBAdaptor;

    private CellBaseManagerFactory cellbaseManagerFactory;
    private GenomeManager genomeManager;

    public VariantManager(String species, CellBaseConfiguration configuration) throws CellBaseException {
        this(species, null, configuration);
    }

    public VariantManager(String species, String assembly, CellBaseConfiguration configuration)
            throws CellBaseException {
        super(species, assembly, configuration);

        this.init();
    }

    private void init() throws CellBaseException {
        variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor();
        spliceDBAdaptor = dbAdaptorFactory.getSpliceScoreDBAdaptor();
        cellbaseManagerFactory = new CellBaseManagerFactory(configuration);
        genomeManager = cellbaseManagerFactory.getGenomeManager(species, assembly);
    }

    @Override
    public CellBaseCoreDBAdaptor getDBAdaptor() {
        return variantDBAdaptor;
    }

    public CellBaseDataResult get(Query query, QueryOptions queryOptions, int dataRelease) throws CellBaseException {
        return variantDBAdaptor.nativeGet(query, queryOptions, dataRelease);
    }

    public List<CellBaseDataResult<String>> getHgvsByVariant(String variants, DataRelease dataRelease)
            throws CellBaseException, QueryException, IllegalAccessException {
        List<Variant> variantList = parseVariants(variants);
        HgvsCalculator hgvsCalculator = new HgvsCalculator(genomeManager, dataRelease.getRelease());
        List<CellBaseDataResult<String>> results = new ArrayList<>();
        VariantAnnotationCalculator variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly,
                dataRelease, "", cellbaseManagerFactory);
        List<Gene> batchGeneList = variantAnnotationCalculator.getBatchGeneList(variantList);
        for (Variant variant : variantList) {
            List<Gene> variantGeneList = variantAnnotationCalculator.getAffectedGenes(batchGeneList, variant);
            List<String> hgvsStrings = hgvsCalculator.run(variant, variantGeneList, false);
            results.add(new CellBaseDataResult<>(variant.getId(), 0, new ArrayList<>(), hgvsStrings.size(), hgvsStrings, -1));
        }
        return results;
    }

    /**
     * Normalises a list of variants.
     *
     * @param variants list of variant strings
     * @param decompose boolean to set the decompose MNV behaviour
     * @param leftAlign boolean to set the left alignment behaviour
     * @param dataRelease data release
     * @return list of normalised variants
     * @throws CellBaseException if the species is incorrect
     */
    public CellBaseDataResult<Variant> getNormalizationByVariant(String variants, boolean decompose, boolean leftAlign,
                                                                 DataRelease dataRelease) throws CellBaseException {
        List<Variant> variantList = parseVariants(variants);
        VariantAnnotationCalculator variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly,
                dataRelease, "", cellbaseManagerFactory);


        // Set decompose MNV behaviour
        variantAnnotationCalculator.getNormalizer().getConfig().setDecomposeMNVs(decompose);

        // Set left alignment behaviour
        if (leftAlign) {
            variantAnnotationCalculator.getNormalizer().getConfig().enableLeftAlign(new CellBaseNormalizerSequenceAdaptor(genomeManager,
                    dataRelease.getRelease()));
        } else {
            variantAnnotationCalculator.getNormalizer().getConfig().disableLeftAlign();
        }


        List<Variant> normalisedVariants = variantAnnotationCalculator.normalizer(variantList);
        return new CellBaseDataResult<>(variants, 0, new ArrayList<>(), normalisedVariants.size(), normalisedVariants, -1);
    }

    public List<CellBaseDataResult<VariantAnnotation>> getAnnotationByVariant(QueryOptions queryOptions,
                                                                              String variants,
                                                                              Boolean normalize,
                                                                              Boolean decompose,
                                                                              Boolean leftAlign,
                                                                              Boolean ignorePhase,
                                                                              @Deprecated Boolean phased,
                                                                              Boolean imprecise,
                                                                              Integer svExtraPadding,
                                                                              Integer cnvExtraPadding,
                                                                              Boolean checkAminoAcidChange,
                                                                              String consequenceTypeSource,
                                                                              DataRelease dataRelease,
                                                                              String apiKey)
            throws ExecutionException, InterruptedException, CellBaseException, QueryException, IllegalAccessException {
        List<Variant> variantList = parseVariants(variants);
        logger.debug("queryOptions: " + queryOptions);

        // If ignorePhase (new parameter) is present, then overrides presence of "phased"
        if (ignorePhase != null) {
            queryOptions.put("ignorePhase", ignorePhase);
            // If the new parameter (ignorePhase) is not present but old one ("phased") is, then follow old one - probably
            // someone who has not moved to the new parameter yet
        } else if (phased != null) {
            queryOptions.put("ignorePhase", !phased);
            // Default behavior is to perform phased annotation
        } else {
            queryOptions.put("ignorePhase", false);
        }

        if (normalize != null) {
            queryOptions.put("normalize", normalize);
        }
        if (decompose != null) {
            queryOptions.put("decompose", decompose);
        }
        if (leftAlign != null) {
            queryOptions.put("leftAlign", leftAlign);
        }
        if (imprecise != null) {
            queryOptions.put("imprecise", imprecise);
        }
        if (svExtraPadding != null) {
            queryOptions.put("svExtraPadding", svExtraPadding);
        }
        if (cnvExtraPadding != null) {
            queryOptions.put("cnvExtraPadding", cnvExtraPadding);
        }
        if (checkAminoAcidChange != null) {
            queryOptions.put("checkAminoAcidChange", checkAminoAcidChange);
        }
        if (consequenceTypeSource != null) {
            queryOptions.put("consequenceTypeSource", consequenceTypeSource);
        }

        VariantAnnotationCalculator variantAnnotationCalculator = new VariantAnnotationCalculator(species, assembly,
                dataRelease, apiKey, cellbaseManagerFactory);
        List<CellBaseDataResult<VariantAnnotation>> queryResults = variantAnnotationCalculator.getAnnotationByVariantList(variantList,
                queryOptions);
        return queryResults;
    }

    private List<Variant> parseVariants(String variantsString) {
        List<Variant> variants = null;
        if (variantsString != null && !variantsString.isEmpty()) {
            String[] variantItems = variantsString.split(",");
            variants = new ArrayList<>(variantItems.length);

            for (String variantString: variantItems) {
                variants.add(parseVariant(variantString));
            }
        }
        return variants;
    }

    private Variant parseVariant(String variantString) {
        String[] variantStringPartArray = variantString.split(PHASE_DATA_URL_SEPARATOR);

        VariantBuilder variantBuilder;
        if (variantStringPartArray.length > 0) {
            variantBuilder = new VariantBuilder(variantStringPartArray[0]);
            // Either 1 or 3 parts expected variant+GT+PS
            if (variantStringPartArray.length == 3) {
                List<String> formatList = new ArrayList<>(2);
                // If phase set tag is not provided not phase data is added at all to the Variant object
                if (!variantStringPartArray[2].isEmpty()) {
                    formatList.add(AnnotationBasedPhasedQueryManager.PHASE_SET_TAG);
                    List<String> sampleData = new ArrayList<>(2);
                    sampleData.add(variantStringPartArray[2]);
                    // Genotype field might be empty - just PS would be added to Variant object in that case
                    if (!variantStringPartArray[1].isEmpty()) {
                        formatList.add(AnnotationBasedPhasedQueryManager.GENOTYPE_TAG);
                        sampleData.add(variantStringPartArray[1]);
                    }
                    variantBuilder.setSampleDataKeys(formatList);
                    SampleEntry sampleEntry = new SampleEntry();
                    sampleEntry.setData(sampleData);
                    variantBuilder.setSamples(Collections.singletonList(sampleEntry));
                }
            } else if (variantStringPartArray.length > 3) {
                throw new IllegalArgumentException("Malformed variant string " + variantString + ". "
                        + "variantString+GT+PS expected, where variantString needs 3 or 4 fields separated by ':'. "
                        + "Format: \"" + VARIANT_STRING_FORMAT + "\"");
            }
        } else {
            throw new IllegalArgumentException("Malformed variant string " + variantString + ". "
                    + "variantString+GT+PS expected, where variantString needs 3 or 4 fields separated by ':'. "
                    + "Format: \"" + VARIANT_STRING_FORMAT + "\"");
        }

        return variantBuilder.build();
    }

    public CellBaseDataResult<String> getConsequenceTypes() {
        List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        CellBaseDataResult<String> queryResult = new CellBaseDataResult<>("consequence_types");
        queryResult.setNumResults(consequenceTypes.size());
        queryResult.setResults(consequenceTypes);
        return queryResult;
    }

    public boolean validateRegionInput(String regions) {
        List<Region> regionList = Region.parseRegions(regions);
        // check for regions bigger than 10Mb
        if (regionList != null) {
            for (Region r : regionList) {
                if ((r.getEnd() - r.getStart()) > 10000000) {
                    return false;
                }
            }
        }
        return true;
    }

    @Deprecated
    public List<CellBaseDataResult> getByRegion(Query query, QueryOptions queryOptions, String regions, int dataRelease) {
        query.put(ParamConstants.QueryParams.REGION.key(), regions);
        logger.debug("query = " + query.toJson());
        logger.debug("queryOptions = " + queryOptions.toJson());
        List<Query> queries = createQueries(query, regions, ParamConstants.QueryParams.REGION.key());
        List<CellBaseDataResult> queryResults = variantDBAdaptor.nativeGet(queries, queryOptions, dataRelease);
        for (int i = 0; i < queries.size(); i++) {
            queryResults.get(i).setId((String) queries.get(i).get(ParamConstants.QueryParams.REGION.key()));
        }
        return queryResults;
    }

    public CellBaseDataResult<Score> getFunctionalScoreVariant(Variant variant, QueryOptions queryOptions, String apiKey, int dataRelease)
            throws CellBaseException {
        Set<String> validSources = apiKeyManager.getValidSources(apiKey, null);
        if (validSources.contains(EtlCommons.CADD_DATA)) {
            return variantDBAdaptor.getFunctionalScoreVariant(variant, queryOptions, dataRelease);
        } else {
            return new CellBaseDataResult<>(variant.toStringSimple(), 0, Collections.singletonList(new Event(Event.Type.WARNING,
                    "Your current API key does not grant access to CADD data")), 0);
        }
    }

    public List<CellBaseDataResult<Score>> getFunctionalScoreVariant(List<Variant> variants, QueryOptions options, String apiKey,
                                                                     int dataRelease)
            throws CellBaseException {
        List<CellBaseDataResult<Score>> cellBaseDataResults = new ArrayList<>(variants.size());
        for (Variant variant: variants) {
            if (variant.getType() == VariantType.SNV) {
                cellBaseDataResults.add(getFunctionalScoreVariant(variant, options, apiKey, dataRelease));
            } else {
                cellBaseDataResults.add(new CellBaseDataResult<>(variant.toString(), 0, Collections.emptyList(), 0));
            }
        }
        return cellBaseDataResults;
    }

    public List<CellBaseDataResult<Variant>> getPopulationFrequencyByVariant(List<Variant> variants, QueryOptions queryOptions,
                                                                             int dataRelease) throws CellBaseException {
        return variantDBAdaptor.getPopulationFrequencyByVariant(variants, queryOptions, dataRelease);
    }

    public CellBaseDataResult<SpliceScore> getSpliceScoreVariant(Variant variant, String apiKey, int dataRelease) throws CellBaseException {
        Set<String> validSources = apiKeyManager.getValidSources(apiKey, ApiKeyLicensedDataUtils.UNLICENSED_SPLICE_SCORES_DATA);

        CellBaseDataResult<SpliceScore> result = spliceDBAdaptor.getScores(variant.getChromosome(), variant.getStart(),
                variant.getReference(), variant.getAlternate(), dataRelease);

        if (ApiKeyLicensedDataUtils.needFiltering(validSources, ApiKeyLicensedDataUtils.LICENSED_SPLICE_SCORES_DATA)) {
            return ApiKeyLicensedDataUtils.filterDataSources(result, validSources);
        } else {
            return result;
        }
    }

    public List<CellBaseDataResult<SpliceScore>> getSpliceScoreVariant(List<Variant> variants, String apiKey, int dataRelease)
            throws CellBaseException {
        Set<String> validSources = apiKeyManager.getValidSources(apiKey, ApiKeyLicensedDataUtils.UNLICENSED_SPLICE_SCORES_DATA);

        List<CellBaseDataResult<SpliceScore>> cellBaseDataResults = new ArrayList<>(variants.size());
        if (ApiKeyLicensedDataUtils.needFiltering(validSources, ApiKeyLicensedDataUtils.LICENSED_SPLICE_SCORES_DATA)) {
            for (Variant variant : variants) {
                cellBaseDataResults.add(ApiKeyLicensedDataUtils.filterDataSources(spliceDBAdaptor.getScores(variant.getChromosome(),
                        variant.getStart(), variant.getReference(), variant.getAlternate(), dataRelease), validSources));
            }
        } else {
            for (Variant variant : variants) {
                cellBaseDataResults.add(spliceDBAdaptor.getScores(variant.getChromosome(), variant.getStart(), variant.getReference(),
                        variant.getAlternate(), dataRelease));
            }
        }
        return cellBaseDataResults;
    }

    public CellBaseDataResult<GenomicScoreRegion> getFunctionalScoreRegion(List<Region> regions, CellBaseQueryOptions options,
                                                                           String apiKey, int dataRelease)
            throws CellBaseException {
        Set<String> chunkIdSet = new HashSet<>();

        Set<String> validSources = apiKeyManager.getValidSources(apiKey, null);
        if (validSources.contains(EtlCommons.CADD_DATA)) {
            for (Region region : regions) {
                chunkIdSet.addAll(variantDBAdaptor.getFunctionalScoreChunkIds(region));
            }
            return variantDBAdaptor.getFunctionalScoreRegion(new ArrayList<>(chunkIdSet), options, dataRelease);
        } else {
            return new CellBaseDataResult<>("", 0, Collections.singletonList(new Event(Event.Type.WARNING, "Your current API key does not"
                    + " grant access to CADD data.")), 0);
        }
    }
}
