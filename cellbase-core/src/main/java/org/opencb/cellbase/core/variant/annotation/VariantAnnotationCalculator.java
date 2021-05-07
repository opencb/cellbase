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

package org.opencb.cellbase.core.variant.annotation;

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantBuilder;
import org.opencb.biodata.models.variant.annotation.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.biodata.tools.variant.exceptions.VariantNormalizerException;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.variant.annotation.hgvs.HgvsCalculator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.opencb.cellbase.core.variant.PhasedQueryManager.*;


/**
 * Created by imedina on 06/02/16.
 */
/**
 * Created by imedina on 11/07/14.
 *
 * @author Javier Lopez fjlopez@ebi.ac.uk;
 */
public class VariantAnnotationCalculator {
    private static final String EMPTY_STRING = "";
    private static final String ALTERNATE = "1";
    private GenomeDBAdaptor genomeDBAdaptor;
    private GeneDBAdaptor geneDBAdaptor;
    private RegulationDBAdaptor regulationDBAdaptor;
    private VariantDBAdaptor variantDBAdaptor;
    private ClinicalDBAdaptor clinicalDBAdaptor;
    private RepeatsDBAdaptor repeatsDBAdaptor;
    private ProteinDBAdaptor proteinDBAdaptor;
    private ConservationDBAdaptor conservationDBAdaptor;
    private Set<String> annotatorSet;
    private String includeGeneFields;

    private final VariantNormalizer normalizer;
    private boolean normalize = false;
    private boolean decompose = true;
    private boolean phased = true;
    private Boolean imprecise = true;
    private Integer svExtraPadding = 0;
    private Integer cnvExtraPadding = 0;
    private Boolean checkAminoAcidChange = false;

    private static Logger logger = LoggerFactory.getLogger(VariantAnnotationCalculator.class);
    private static HgvsCalculator hgvsCalculator;

    private static final String REGULATORY_REGION_FEATURE_TYPE_ATTRIBUTE = "featureType";
    private static final String TF_BINDING_SITE = RegulationDBAdaptor.FeatureType.TF_binding_site.name() + ","
            + RegulationDBAdaptor.FeatureType.TF_binding_site_motif;
    private static final String REGION = "region";
    private static final String MERGE = "merge";


    public VariantAnnotationCalculator(String species, String assembly, DBAdaptorFactory dbAdaptorFactory) {
        this.genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        this.variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        this.geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
        this.regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(species, assembly);
        this.proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        this.conservationDBAdaptor = dbAdaptorFactory.getConservationDBAdaptor(species, assembly);
        this.clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
        this.repeatsDBAdaptor = dbAdaptorFactory.getRepeatsDBAdaptor(species, assembly);

        // Initialises normaliser configuration with default values. HEADS UP: configuration might be updated
        // at parseQueryParam
        this.normalizer = new VariantNormalizer(getNormalizerConfig());

         hgvsCalculator = new HgvsCalculator(genomeDBAdaptor);

        logger.debug("VariantAnnotationMongoDBAdaptor: in 'constructor'");
    }

    private VariantNormalizer.VariantNormalizerConfig getNormalizerConfig() {
        return (new VariantNormalizer.VariantNormalizerConfig())
                .setReuseVariants(false)
                .setNormalizeAlleles(false)
                .setDecomposeMNVs(decompose)
                .enableLeftAlign(new CellBaseNormalizerSequenceAdaptor(genomeDBAdaptor));
    }

    @Deprecated
    public QueryResult getAllConsequenceTypesByVariant(Variant variant, QueryOptions queryOptions) {
        long dbTimeStart = System.currentTimeMillis();

        parseQueryParam(queryOptions);
        List<Gene> batchGeneList = getBatchGeneList(Collections.singletonList(variant));
        List<Gene> geneList = getAffectedGenes(batchGeneList, variant);

        // TODO the last 'true' parameter needs to be changed by annotatorSet.contains("regulatory") once is ready
        List<ConsequenceType> consequenceTypeList = getConsequenceTypeList(variant, geneList, true,
                queryOptions);

        QueryResult queryResult = new QueryResult();
        queryResult.setId(variant.toString());
        queryResult.setDbTime(Long.valueOf(System.currentTimeMillis() - dbTimeStart).intValue());
        queryResult.setNumResults(consequenceTypeList.size());
        queryResult.setNumTotalResults(consequenceTypeList.size());
        queryResult.setResult(consequenceTypeList);

        return queryResult;

    }

    public QueryResult getAnnotationByVariant(Variant variant, QueryOptions queryOptions)
            throws InterruptedException, ExecutionException {
        return getAnnotationByVariantList(Collections.singletonList(variant), queryOptions).get(0);
    }

    public List<QueryResult<VariantAnnotation>> getAnnotationByVariantList(List<Variant> variantList,
                                                                           QueryOptions queryOptions)
            throws InterruptedException, ExecutionException {

        logger.debug("Annotating  batch");
        parseQueryParam(queryOptions);

        if (variantList == null || variantList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Variant> normalizedVariantList;
        if (normalize) {
            normalizedVariantList = normalizer.apply(variantList);
        } else {
            normalizedVariantList = variantList;
        }

        long startTime = System.currentTimeMillis();
        // Normalized variants already contain updated VariantAnnotation objects since runAnnotationProcess will
        // write on them if available (if not will create and set them) - i.e. no need to use variantAnnotationList
        // really
        runAnnotationProcess(normalizedVariantList);
        return generateQueryResultList(variantList, normalizedVariantList, startTime);
    }

    private List<QueryResult<VariantAnnotation>> generateQueryResultList(List<Variant> variantList,
                                                                         List<Variant> normalizedVariantList,
                                                                         long startTime) {

        List<QueryResult<VariantAnnotation>> annotationResultList = new ArrayList<>(variantList.size());

        // Return only one result per QueryResult if either
        //   - size original variant list and normalised one is the same
        //   - MNV decomposition is switched OFF, i.e. queryOptions.skipDecompose = true and therefore
        //   this.decompose = false
        if (!decompose || variantList.size() == normalizedVariantList.size()) {
            for (int i = 0; i < variantList.size(); i++) {
                QueryResult<VariantAnnotation> queryResult = new QueryResult<>(variantList.get(i).toString(),
                        (int) (System.currentTimeMillis() - startTime),
                        1,
                        1,
                        null,
                        null,
                        Collections.singletonList(normalizedVariantList.get(i).getAnnotation()));
                annotationResultList.add(queryResult);
            }
        } else {
            int originalVariantListCounter = 0;
            String previousCall = EMPTY_STRING;
            QueryResult<VariantAnnotation> queryResult = null;
            for (Variant normalizedVariant : normalizedVariantList) {
                if (isSameMnv(previousCall, normalizedVariant)) {
                    queryResult.getResult().add(normalizedVariant.getAnnotation());
                    queryResult.setNumResults(queryResult.getNumResults() + 1);
                    queryResult.setNumTotalResults(queryResult.getNumTotalResults() + 1);
                } else {
                    List<VariantAnnotation> variantAnnotationList = new ArrayList<>(1);
                    variantAnnotationList.add(normalizedVariant.getAnnotation());
                    queryResult = new QueryResult<>(variantList.get(originalVariantListCounter).toString(),
                            (int) (System.currentTimeMillis() - startTime),
                            1,
                            1,
                            null,
                            null,
                            variantAnnotationList);
                    annotationResultList.add(queryResult);
                    previousCall = getCall(normalizedVariant);
                    originalVariantListCounter++;
                }
            }
        }

        return annotationResultList;
    }

    private boolean isSameMnv(String previousCall, Variant variant) {
        if (!StringUtils.isBlank(previousCall)) {
            String call = getCall(variant);
            if (StringUtils.isNotBlank(call)) {
                return previousCall.equals(variant.getStudies().get(0).getFiles().get(0).getCall());
            }
        }

        return false;
    }

    private String getCall(Variant variant) {
        if (variant.getStudies() != null
                && !variant.getStudies().isEmpty()
                && variant.getStudies().get(0).getFiles() != null
                && !variant.getStudies().get(0).getFiles().isEmpty()) {
            return variant.getStudies().get(0).getFiles().get(0).getCall();
        }

        return null;
    }

    private Variant getPreferredVariant(QueryResult<Variant> variantQueryResult) {
        if (variantQueryResult.getNumResults() > 1
                && variantQueryResult.first().getAnnotation().getPopulationFrequencies() == null) {
            for (int i = 1; i < variantQueryResult.getResult().size(); i++) {
                if (variantQueryResult.getResult().get(i).getAnnotation().getPopulationFrequencies() != null) {
                    return variantQueryResult.getResult().get(i);
                }
            }
        }
        return variantQueryResult.first();
    }

    private List<Gene> setGeneAnnotation(List<Gene> batchGeneList, Variant variant) {
        // Fetch overlapping genes for this variant
        List<Gene> geneList = getAffectedGenes(batchGeneList, variant);
        VariantAnnotation variantAnnotation = variant.getAnnotation();

        /*
         * Gene Annotation
         */
        if (annotatorSet.contains("expression")) {
            variantAnnotation.setGeneExpression(new ArrayList<>());
            for (Gene gene : geneList) {
                if (gene.getAnnotation().getExpression() != null) {
                    variantAnnotation.getGeneExpression().addAll(gene.getAnnotation().getExpression());
                }
            }
        }

        if (annotatorSet.contains("geneDisease")) {
            variantAnnotation.setGeneTraitAssociation(new ArrayList<>());
            for (Gene gene : geneList) {
                if (gene.getAnnotation().getDiseases() != null) {
                    variantAnnotation.getGeneTraitAssociation().addAll(gene.getAnnotation().getDiseases());
                }
            }
        }

        if (annotatorSet.contains("drugInteraction")) {
            variantAnnotation.setGeneDrugInteraction(new ArrayList<>());
            for (Gene gene : geneList) {
                if (gene.getAnnotation().getDrugs() != null) {
                    variantAnnotation.getGeneDrugInteraction().addAll(gene.getAnnotation().getDrugs());
                }
            }
        }

        return geneList;

    }

    private boolean isPhased(Variant variant) {
        return (variant.getStudies() != null && !variant.getStudies().isEmpty())
            && variant.getStudies().get(0).getFormat().contains("PS");
    }

    private String getCachedVariationIncludeFields() {
        StringBuilder stringBuilder = new StringBuilder("annotation.chromosome,annotation.start,annotation.reference");
        stringBuilder.append(",annotation.alternate,annotation.id");

        if (annotatorSet.contains("variation")) {
            stringBuilder.append(",annotation.id,annotation.additionalAttributes.dgvSpecificAttributes");
        }
        if (annotatorSet.contains("clinical")) {
            stringBuilder.append(",annotation.variantTraitAssociation");
        }
        if (annotatorSet.contains("conservation")) {
            stringBuilder.append(",annotation.conservation");
        }
        if (annotatorSet.contains("functionalScore")) {
            stringBuilder.append(",annotation.functionalScore");
        }
        if (annotatorSet.contains("consequenceType")) {
            stringBuilder.append(",annotation.consequenceTypes,annotation.displayConsequenceType");
        }
        if (annotatorSet.contains("populationFrequencies")) {
            stringBuilder.append(",annotation.populationFrequencies");
        }

        return stringBuilder.toString();
    }

    private List<VariantAnnotation> runAnnotationProcess(List<Variant> normalizedVariantList)
            throws InterruptedException, ExecutionException {
        long globalStartTime = System.currentTimeMillis();
        long startTime;

        // Object to be returned
        List<VariantAnnotation> variantAnnotationList = new ArrayList<>(normalizedVariantList.size());

        /*
         * Next three async blocks calculate annotations using Futures, this will be calculated in a different thread.
         * Once the main loop has finished then they will be stored. This provides a ~30% of performance improvement.
         */
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
        FutureVariationAnnotator futureVariationAnnotator = null;
        Future<List<QueryResult<Variant>>> variationFuture = null;
        List<Gene> batchGeneList = getBatchGeneList(normalizedVariantList);

        if (annotatorSet.contains("variation") || annotatorSet.contains("populationFrequencies")) {
            futureVariationAnnotator = new FutureVariationAnnotator(normalizedVariantList, new QueryOptions("include",
                    "id,annotation.populationFrequencies,annotation.additionalAttributes.dgvSpecificAttributes")
                    .append("imprecise", imprecise));
            variationFuture = fixedThreadPool.submit(futureVariationAnnotator);
        }

        FutureConservationAnnotator futureConservationAnnotator = null;
        Future<List<QueryResult>> conservationFuture = null;
        if (annotatorSet.contains("conservation")) {
            futureConservationAnnotator = new FutureConservationAnnotator(normalizedVariantList, QueryOptions.empty());
            conservationFuture = fixedThreadPool.submit(futureConservationAnnotator);
        }

        FutureVariantFunctionalScoreAnnotator futureVariantFunctionalScoreAnnotator = null;
        Future<List<QueryResult<Score>>> variantFunctionalScoreFuture = null;
        if (annotatorSet.contains("functionalScore")) {
            futureVariantFunctionalScoreAnnotator = new FutureVariantFunctionalScoreAnnotator(normalizedVariantList, QueryOptions.empty());
            variantFunctionalScoreFuture = fixedThreadPool.submit(futureVariantFunctionalScoreAnnotator);
        }

        FutureClinicalAnnotator futureClinicalAnnotator = null;
        Future<List<QueryResult<Variant>>> clinicalFuture = null;
        if (annotatorSet.contains("clinical")) {
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.add(ClinicalDBAdaptor.QueryParams.PHASE.key(), phased);
            queryOptions.add(ClinicalDBAdaptor.QueryParams.CHECK_AMINO_ACID_CHANGE.key(), checkAminoAcidChange);
            futureClinicalAnnotator = new FutureClinicalAnnotator(normalizedVariantList, batchGeneList, queryOptions);
            clinicalFuture = fixedThreadPool.submit(futureClinicalAnnotator);
        }

        FutureRepeatsAnnotator futureRepeatsAnnotator = null;
        Future<List<QueryResult<Repeat>>> repeatsFuture = null;
        if (annotatorSet.contains("repeats")) {
            futureRepeatsAnnotator = new FutureRepeatsAnnotator(normalizedVariantList, QueryOptions.empty());
            repeatsFuture = fixedThreadPool.submit(futureRepeatsAnnotator);
        }

        FutureCytobandAnnotator futureCytobandAnnotator = null;
        Future<List<QueryResult<Cytoband>>> cytobandFuture = null;
        if (annotatorSet.contains("cytoband")) {
            futureCytobandAnnotator = new FutureCytobandAnnotator(normalizedVariantList, QueryOptions.empty());
            cytobandFuture = fixedThreadPool.submit(futureCytobandAnnotator);
        }

        /*
         * We iterate over all variants to get the rest of the annotations and to create the VariantAnnotation objects
         */
        Queue<Variant> variantBuffer = new LinkedList<>();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < normalizedVariantList.size(); i++) {
            // normalizedVariantList is the passed by reference argument - modifying normalizedVariantList will
            // modify user-provided Variant objects. If there's no annotation - just set it; if there's an annotation
            // object already created, let's only overwrite those fields created by the annotator
            VariantAnnotation variantAnnotation;
            if (normalizedVariantList.get(i).getAnnotation() == null) {
                variantAnnotation = new VariantAnnotation();
                normalizedVariantList.get(i).setAnnotation(variantAnnotation);
            } else {
                variantAnnotation = normalizedVariantList.get(i).getAnnotation();
            }

            variantAnnotation.setChromosome(normalizedVariantList.get(i).getChromosome());
            variantAnnotation.setStart(normalizedVariantList.get(i).getStart());
            variantAnnotation.setReference(normalizedVariantList.get(i).getReference());
            variantAnnotation.setAlternate(normalizedVariantList.get(i).getAlternate());

            List<Gene> variantGeneList = setGeneAnnotation(batchGeneList, normalizedVariantList.get(i));

            // Better not run hgvs calculation with a Future for the following reasons:
            //   * geneList is needed in order to calculate the hgvs for ALL VARIANTS
            //   * hgvsCalculator will raise an additional database query to get the genome sequence JUST FOR INDELS
            //   * If a Future is used and a list of variants is provided to the hgvsCalculator, then the hgvsCalculator
            //   will require to raise an additional query to the database (that would be performed asynchronously)
            //   in order to get the geneList FOR ALL VARIANTS
            //   * If no future is used, then the genome sequence query will be performed synchronously but JUST
            //   FOR INDELS
            // Given that the number of indels is expected to be negligible if compared to the number of SNVs, the
            // decision is to run it synchronously
            if (annotatorSet.contains("hgvs")) {
                try {
                    // Decided to always set normalize = false for a number of reasons:
                    //   * was raising problems with the normalizer - it could potentially fail in weird multiallelic
                    //     cases if the normalizer is called twice over the same variant,
                    //     i.e. normalize(normalize(variant)). Calling the normalizer twice happens when annotating from
                    //     a VCF, since normalization is carried out before sending variant to the VariantAnnotationCalculator.
                    //     Therefore, normalize would be false within the VariantAnnotationCalculator, it kept as it was
                    //     before, !normalize for hgvsCalculator, it'd run normalization twice.
                    //     This incorrect behaviour of the normalizer must and will be fixed in the future, it was decided not to
                    //     include it as a hotfix since touches the very core of the normalizer
                    //   * if normalize = true, the variants in normalizedVariantList are already normalized for sure
                    //     and should not be normalized again.
                    //   * if normalize = false, then we could potentially find things like CT/C. In this case, the
                    //     annotator will consider this as an MNV and the rest of annotation will not exactly be what
                    //     a typical user would expect for the deletion of the T (which is what it is). Thus, we don't
                    //     really care that much at this point if the hgvs is not perfectly normalized. Knowing that
                    //     variants are not normalized the user should always select normalize=true.
                    variantAnnotation.setHgvs(hgvsCalculator.run(normalizedVariantList.get(i), variantGeneList, false));
                } catch (VariantNormalizerException e) {
                    logger.error("Unable to normalize variant {}. Leaving empty HGVS.",
                            normalizedVariantList.get(i).toString());
                }
            }

            if (annotatorSet.contains("consequenceType")) {
                try {
                    List<ConsequenceType> consequenceTypeList = getConsequenceTypeList(normalizedVariantList.get(i),
                        variantGeneList, true, QueryOptions.empty());
                    variantAnnotation.setConsequenceTypes(consequenceTypeList);
                    if (phased) {
                        checkAndAdjustPhasedConsequenceTypes(normalizedVariantList.get(i), variantBuffer);
                    }
                    variantAnnotation
                            .setDisplayConsequenceType(getMostSevereConsequenceType(normalizedVariantList.get(i)
                                    .getAnnotation().getConsequenceTypes()));
                } catch (UnsupportedURLVariantFormat e) {
                    logger.error("Consequence type was not calculated for variant {}. Unrecognised variant format."
                            + " Leaving an empty consequence type list.", normalizedVariantList.get(i).toString());
                    variantAnnotation.setConsequenceTypes(Collections.emptyList());
                } catch (Exception e) {
                    logger.error("Unhandled error when calculating consequence type for variant {}. Leaving an empty"
                            + " consequence type list.", normalizedVariantList.get(i).toString());
                    e.printStackTrace();
                    variantAnnotation.setConsequenceTypes(Collections.emptyList());
                }
            }

            variantAnnotationList.add(variantAnnotation);

        }

        // Adjust phase of two last variants - if still anything remaining to adjust. This can happen if the two last
        // variants in the batch are phased and the distance between them < 3nts
        if (phased && variantBuffer.size() > 1) {
            adjustPhasedConsequenceTypes(variantBuffer.toArray());
        }

        logger.debug("Main loop iteration annotation performance is {}ms for {} variants", System.currentTimeMillis()
                - startTime, normalizedVariantList.size());

        /*
         * Now, hopefully the other annotations have finished and we can store the results.
         * Method 'processResults' has been implemented in the same class for sanity.
         */
        if (futureVariationAnnotator != null) {
            futureVariationAnnotator.processResults(variationFuture, variantAnnotationList, annotatorSet);
        }
        if (futureConservationAnnotator != null) {
            futureConservationAnnotator.processResults(conservationFuture, variantAnnotationList);
        }
        if (futureVariantFunctionalScoreAnnotator != null) {
            futureVariantFunctionalScoreAnnotator.processResults(variantFunctionalScoreFuture, variantAnnotationList);
        }
        if (futureClinicalAnnotator != null) {
            futureClinicalAnnotator.processResults(clinicalFuture, variantAnnotationList);
        }
        if (futureRepeatsAnnotator != null) {
            futureRepeatsAnnotator.processResults(repeatsFuture, variantAnnotationList);
        }
        if (futureCytobandAnnotator != null) {
            futureCytobandAnnotator.processResults(cytobandFuture, variantAnnotationList);
        }
        fixedThreadPool.shutdown();


        logger.debug("Total batch annotation performance is {}ms for {} variants", System.currentTimeMillis()
                - globalStartTime, normalizedVariantList.size());
        return variantAnnotationList;
    }

    private List<Gene> getBatchGeneList(List<Variant> variantList) {
        List<Region> regionList = variantListToRegionList(variantList);
        // Add +-5Kb for gene search
        for (Region region : regionList) {
            region.setStart(Math.max(1, region.getStart() - 5000));
            region.setEnd(region.getEnd() + 5000);
        }

        // Just return required fields
        // MERGE = true essential so that just one query will be raised with all regions
        QueryOptions queryOptions = new QueryOptions(QueryOptions.INCLUDE, includeGeneFields);
        queryOptions.put(MERGE, true);

        return ((QueryResult) geneDBAdaptor.getByRegion(regionList, queryOptions).get(0)).getResult();
    }

    private void parseQueryParam(QueryOptions queryOptions) {
        // We process include and exclude query options to know which annotators to use.
        // Include parameter has preference over exclude.
        annotatorSet = getAnnotatorSet(queryOptions);
        logger.debug("Annotators to use: {}", annotatorSet.toString());

        // This field contains all the fields to be returned by overlapping genes
        includeGeneFields = getIncludedGeneFields(annotatorSet);

        // Default behaviour no normalization
        normalize = (queryOptions.get("normalize") != null && (Boolean) queryOptions.get("normalize"));
        logger.debug("normalize = {}", normalize);

        // Default behaviour decompose
        decompose = (queryOptions.get("skipDecompose") == null || !queryOptions.getBoolean("skipDecompose"));
        logger.debug("decompose = {}", decompose);
        // Must update normaliser configuration since normaliser was created on constructor
        normalizer.getConfig().setDecomposeMNVs(decompose);

        // New parameter "ignorePhase" present overrides presence of old "phased" parameter
        if (queryOptions.get("ignorePhase") != null) {
            phased = !queryOptions.getBoolean("ignorePhase");
        // Old parameter "phased" present but new one ("ignorePhase") absent - use old one. Probably someone who has not
        // yet moved to using the new one.
        } else if (queryOptions.get("phased") != null) {
            phased = queryOptions.getBoolean("phased");
        // Default behaviour - calculate phased annotation
        } else {
            phased = true;
        }
        logger.debug("phased = {}", phased);

        // Default behaviour - enable imprecise searches
        imprecise = (queryOptions.get("imprecise") == null || queryOptions.getBoolean("imprecise"));
        logger.debug("imprecise = {}", imprecise);

        // Default behaviour - no extra padding for structural variants
        svExtraPadding = (queryOptions.get("svExtraPadding") != null ? (Integer) queryOptions.get("svExtraPadding") : 0);
        logger.debug("svExtraPadding = {}", svExtraPadding);

        // Default behaviour - no extra padding for CNV
        cnvExtraPadding = (queryOptions.get("cnvExtraPadding") != null ? (Integer) queryOptions.get("cnvExtraPadding") : 0);
        logger.debug("cnvExtraPadding = {}", cnvExtraPadding);

        checkAminoAcidChange = (queryOptions.get("checkAminoAcidChange") != null && (Boolean) queryOptions.get("checkAminoAcidChange"));
        logger.debug("checkAminoAcidChange = {}", checkAminoAcidChange);
    }

    private void mergeAnnotation(VariantAnnotation destination, VariantAnnotation origin) {
        destination.setChromosome(origin.getChromosome());
        destination.setStart(origin.getStart());
        destination.setReference(origin.getReference());
        destination.setAlternate(origin.getAlternate());

        if (annotatorSet.contains("variation")) {
            destination.setId(origin.getId());
        }
        if (annotatorSet.contains("consequenceType")) {
            destination.setDisplayConsequenceType(origin.getDisplayConsequenceType());
            destination.setConsequenceTypes(origin.getConsequenceTypes());
        }
        if (annotatorSet.contains("conservation")) {
            destination.setConservation(origin.getConservation());
        }
        if (annotatorSet.contains("populationFrequencies")) {
            destination.setPopulationFrequencies(origin.getPopulationFrequencies());
        }
        if (annotatorSet.contains("clinical")) {
            destination.setVariantTraitAssociation(origin.getVariantTraitAssociation());
        }
        if (annotatorSet.contains("functionalScore")) {
            destination.setFunctionalScore(origin.getFunctionalScore());
        }
    }

    private void checkAndAdjustPhasedConsequenceTypes(Variant variant, Queue<Variant> variantBuffer) {
        // Only SNVs are currently considered for phase adjustment
        if (variant.getType().equals(VariantType.SNV)) {
            // Check and manage variantBuffer for dealing with phased variants
            switch (variantBuffer.size()) {
                case 0:
                    variantBuffer.add(variant);
                    break;
                case 1:
                    if (potentialCodingSNVOverlap(variantBuffer.peek(), variant)) {
                        variantBuffer.add(variant);
                    } else {
                        variantBuffer.poll();
                        variantBuffer.add(variant);
                    }
                    break;
                case 2:
                    if (potentialCodingSNVOverlap(variantBuffer.peek(), variant)) {
                        variantBuffer.add(variant);
                        adjustPhasedConsequenceTypes(variantBuffer.toArray());
                        variantBuffer.poll();
                    } else {
                        // Adjust consequence types for the two previous variants
                        adjustPhasedConsequenceTypes(variantBuffer.toArray());
                        // Remove the two previous variants after adjustment
                        variantBuffer.poll();
                        variantBuffer.poll();
                        variantBuffer.add(variant);
                    }
                default:
                    break;
            }
        }
    }

    private void adjustPhasedConsequenceTypes(Object[] variantArray) {
        Variant variant0 = (Variant) variantArray[0];
        Variant variant1 = null;
        Variant variant2 = null;

        boolean variant0DisplayCTNeedsUpdate = false;
        boolean variant1DisplayCTNeedsUpdate = false;
        boolean variant2DisplayCTNeedsUpdate = false;

        for (ConsequenceType consequenceType1 : variant0.getAnnotation().getConsequenceTypes()) {
            ProteinVariantAnnotation newProteinVariantAnnotation = null;
            // Check if this is a coding consequence type. Also this consequence type may have been already
            // updated if there are 3 consecutive phased SNVs affecting the same codon.
            if (isCoding(consequenceType1)
                    && !transcriptAnnotationUpdated(variant0, consequenceType1.getEnsemblTranscriptId())) {
                variant1 = (Variant) variantArray[1];
                ConsequenceType consequenceType2
                        = findCodingOverlappingConsequenceType(consequenceType1, variant1.getAnnotation().getConsequenceTypes());
                // The two first variants affect the same codon
                if (consequenceType2 != null) {
                    // WARNING: assumes variants are sorted according to their coordinates
                    int cdnaPosition = consequenceType1.getCdnaPosition();
                    int cdsPosition = consequenceType1.getCdsPosition();
                    String codon = null;
                    String alternateAA = null;
                    List<SequenceOntologyTerm> soTerms = null;
                    ConsequenceType consequenceType3 = null;
                    variant2 = null;
                    // Check if the third variant also affects the same codon
                    if (variantArray.length > 2) {
                        variant2 = (Variant) variantArray[2];
                        consequenceType3
                                = findCodingOverlappingConsequenceType(consequenceType2, variant2.getAnnotation().getConsequenceTypes());
                    }
                    // The three SNVs affect the same codon
                    if (consequenceType3 != null) {
                        String referenceCodon = consequenceType1.getCodon().split("/")[0].toUpperCase();
                        // WARNING: assumes variants are sorted according to their coordinates
                        String alternateCodon = variant0.getAlternate() + variant1.getAlternate()
                                + variant2.getAlternate();
                        codon = referenceCodon + "/" + alternateCodon;
                        alternateAA = VariantAnnotationUtils.CODON_TO_A.get(alternateCodon);
                        soTerms = updatePhasedSoTerms(consequenceType1.getSequenceOntologyTerms(),
                                String.valueOf(referenceCodon), String.valueOf(alternateCodon),
                                variant1.getChromosome().equals("MT"));

                        // Update consequenceType3
                        consequenceType3.setCdnaPosition(cdnaPosition);
                        consequenceType3.setCdsPosition(cdsPosition);
                        consequenceType3.setCodon(codon);
                        consequenceType3.getProteinVariantAnnotation().setAlternate(alternateAA);
                        newProteinVariantAnnotation = getProteinAnnotation(consequenceType3);
                        consequenceType3.setProteinVariantAnnotation(newProteinVariantAnnotation);
                        consequenceType3.setSequenceOntologyTerms(soTerms);

                        // Flag these transcripts as already updated for this variant
                        flagTranscriptAnnotationUpdated(variant2, consequenceType1.getEnsemblTranscriptId());

                        variant2DisplayCTNeedsUpdate = true;

                        // Only the two first SNVs affect the same codon
                    } else {
                        int codonIdx1 = getUpperCaseLetterPosition(consequenceType1.getCodon().split("/")[0]);
                        int codonIdx2 = getUpperCaseLetterPosition(consequenceType2.getCodon().split("/")[0]);

                        // Set referenceCodon  and alternateCodon leaving only the nts that change in uppercase.
                        // Careful with upper/lower case letters
                        char[] referenceCodonArray = consequenceType1.getCodon().split("/")[0].toLowerCase().toCharArray();
                        referenceCodonArray[codonIdx1] = Character.toUpperCase(referenceCodonArray[codonIdx1]);
                        referenceCodonArray[codonIdx2] = Character.toUpperCase(referenceCodonArray[codonIdx2]);
                        char[] alternateCodonArray = referenceCodonArray.clone();
                        alternateCodonArray[codonIdx1] = variant0.getAlternate().toUpperCase().toCharArray()[0];
                        alternateCodonArray[codonIdx2] = variant1.getAlternate().toUpperCase().toCharArray()[0];

                        codon = String.valueOf(referenceCodonArray) + "/" + String.valueOf(alternateCodonArray);
                        alternateAA = VariantAnnotationUtils.CODON_TO_A.get(String.valueOf(alternateCodonArray).toUpperCase());
                        soTerms = updatePhasedSoTerms(consequenceType1.getSequenceOntologyTerms(),
                                String.valueOf(referenceCodonArray).toUpperCase(),
                                String.valueOf(alternateCodonArray).toUpperCase(), variant1.getChromosome().equals("MT"));
                    }

                    // Update consequenceType1 & 2
                    consequenceType1.setCodon(codon);
                    consequenceType1.getProteinVariantAnnotation().setAlternate(alternateAA);
                    consequenceType1.setProteinVariantAnnotation(newProteinVariantAnnotation == null
                            ? getProteinAnnotation(consequenceType1) : newProteinVariantAnnotation);
                    consequenceType1.setSequenceOntologyTerms(soTerms);
                    consequenceType2.setCdnaPosition(cdnaPosition);
                    consequenceType2.setCdsPosition(cdsPosition);
                    consequenceType2.setCodon(codon);
                    consequenceType2.getProteinVariantAnnotation().setAlternate(alternateAA);
                    consequenceType2.setProteinVariantAnnotation(consequenceType1.getProteinVariantAnnotation());
                    consequenceType2.setSequenceOntologyTerms(soTerms);

                    // Flag these transcripts as already updated for this variant
                    flagTranscriptAnnotationUpdated(variant0, consequenceType1.getEnsemblTranscriptId());
                    flagTranscriptAnnotationUpdated(variant1, consequenceType1.getEnsemblTranscriptId());

                    variant0DisplayCTNeedsUpdate = true;
                    variant1DisplayCTNeedsUpdate = true;
                }
            }
        }

        if (variant0DisplayCTNeedsUpdate) {
            variant0.getAnnotation()
                    .setDisplayConsequenceType(getMostSevereConsequenceType(variant0.getAnnotation()
                            .getConsequenceTypes()));
        }
        if (variant1DisplayCTNeedsUpdate) {
            variant1.getAnnotation()
                    .setDisplayConsequenceType(getMostSevereConsequenceType(variant1.getAnnotation()
                            .getConsequenceTypes()));
        }
        if (variant2DisplayCTNeedsUpdate) {
            variant2.getAnnotation()
                    .setDisplayConsequenceType(getMostSevereConsequenceType(variant2.getAnnotation()
                            .getConsequenceTypes()));
        }
    }

    private void flagTranscriptAnnotationUpdated(Variant variant, String ensemblTranscriptId) {
        Map<String, AdditionalAttribute> additionalAttributesMap = variant.getAnnotation().getAdditionalAttributes();
        if (additionalAttributesMap == null) {
            additionalAttributesMap = new HashMap<>();
            AdditionalAttribute additionalAttribute = new AdditionalAttribute();
            Map<String, String> transcriptsSet = new HashMap<>();
            transcriptsSet.put(ensemblTranscriptId, null);
            additionalAttribute.setAttribute(transcriptsSet);
            additionalAttributesMap.put("phasedTranscripts", additionalAttribute);
            variant.getAnnotation().setAdditionalAttributes(additionalAttributesMap);
        } else if (additionalAttributesMap.get("phasedTranscripts") == null) {
            AdditionalAttribute additionalAttribute = new AdditionalAttribute();
            Map<String, String> transcriptsSet = new HashMap<>();
            transcriptsSet.put(ensemblTranscriptId, null);
            additionalAttribute.setAttribute(transcriptsSet);
            additionalAttributesMap.put("phasedTranscripts", additionalAttribute);
        } else {
            additionalAttributesMap.get("phasedTranscripts").getAttribute().put(ensemblTranscriptId, null);
        }
    }

    private boolean transcriptAnnotationUpdated(Variant variant, String ensemblTranscriptId) {
        if (variant.getAnnotation().getAdditionalAttributes() != null
                && variant.getAnnotation().getAdditionalAttributes().get("phasedTranscripts") != null
                && variant.getAnnotation().getAdditionalAttributes().get("phasedTranscripts")
                    .getAttribute().containsKey(ensemblTranscriptId)) {
            return true;
        }
        return false;
    }

    private int getUpperCaseLetterPosition(String string) {
//        Pattern pat = Pattern.compile("G");
        Pattern pat = Pattern.compile("[A,C,G,T]");
        Matcher match = pat.matcher(string);
        if (match.find()) {
            return match.start();
        } else {
            return -1;
        }
    }

    private ConsequenceType findCodingOverlappingConsequenceType(ConsequenceType consequenceType,
                                                                 List<ConsequenceType> consequenceTypeList) {
        for (ConsequenceType consequenceType1 : consequenceTypeList) {
            if (isCoding(consequenceType1)
                    && consequenceType.getEnsemblTranscriptId().equals(consequenceType1.getEnsemblTranscriptId())
                    && consequenceType.getProteinVariantAnnotation().getPosition()
                    .equals(consequenceType1.getProteinVariantAnnotation().getPosition())) {
                return consequenceType1;
            }
        }
        return null;
    }

    private boolean isCoding(ConsequenceType consequenceType) {
        for (SequenceOntologyTerm sequenceOntologyTerm : consequenceType.getSequenceOntologyTerms()) {
            if (VariantAnnotationUtils.CODING_SO_NAMES.contains(sequenceOntologyTerm.getName())) {
                return true;
            }
        }
        return false;
    }

    private List<SequenceOntologyTerm> updatePhasedSoTerms(List<SequenceOntologyTerm> sequenceOntologyTermList,
                                                           String referenceCodon, String alternateCodon,
                                                           Boolean useMitochondrialCode) {

        // Removes all coding-associated SO terms
        int i = 0;
        do {
            if (VariantAnnotationUtils.CODING_SO_NAMES.contains(sequenceOntologyTermList.get(i).getName())) {
                sequenceOntologyTermList.remove(i);
            } else {
                i++;
            }
        } while(i < sequenceOntologyTermList.size());

        // Add the new coding SO term as appropriate
        String newSoName = null;
        if (VariantAnnotationUtils.isSynonymousCodon(useMitochondrialCode, referenceCodon, alternateCodon)) {
            if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon)) {
                newSoName = VariantAnnotationUtils.STOP_RETAINED_VARIANT;
            } else {  // coding end may be not correctly annotated (incomplete_terminal_codon_variant),
                // but if the length of the cds%3=0, annotation should be synonymous variant
                newSoName = VariantAnnotationUtils.SYNONYMOUS_VARIANT;
            }
        } else if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, referenceCodon)) {
            newSoName = VariantAnnotationUtils.STOP_LOST;
        } else if (VariantAnnotationUtils.isStopCodon(useMitochondrialCode, alternateCodon)) {
            newSoName = VariantAnnotationUtils.STOP_GAINED;
        } else {
            newSoName = VariantAnnotationUtils.MISSENSE_VARIANT;
        }
        sequenceOntologyTermList
                .add(new SequenceOntologyTerm(ConsequenceTypeMappings.getSoAccessionString(newSoName), newSoName));

        return sequenceOntologyTermList;
    }

    private boolean potentialCodingSNVOverlap(Variant variant1, Variant variant2) {
        return Math.abs(variant1.getStart() - variant2.getStart()) < 3
                && variant1.getChromosome().equals(variant2.getChromosome())
                && variant1.getType().equals(VariantType.SNV) && variant2.getType().equals(VariantType.SNV)
                && samePhase(variant1, variant2);
    }

    private boolean samePhase(Variant variant1, Variant variant2) {

        String phaseSet1 = getSampleAttribute(variant1, PHASE_SET_TAG);

        // No PS means not sure it is in phase
        if (phaseSet1 == null) {
            return false;
        }

        // TODO: phase depends on the sample. Phased queries constrained to just one sample. The code below is
        // TODO: arbitrarily selecting the first one
        // No PS means not sure it is in phase
        String phaseSet2 = getSampleAttribute(variant2, PHASE_SET_TAG);
        if (phaseSet2 == null) {
            return false;
        }

        // None of the PS is missing
        if (phaseSet1.equals(phaseSet2)) {
            // TODO: phase depends on the sample. Phased queries constrained to just one sample. The code below is
            // TODO: arbitrarily selecting the first one
            String genotype1 = getSampleAttribute(variant1, GENOTYPE_TAG);
            String genotype2 = getSampleAttribute(variant2, GENOTYPE_TAG);

            // Variants obtained as a result of an MNV decomposition - must just check the original call
            if (genotype1 == null && genotype2 == null) {
                return variant1.getStudies().get(0).getFiles() != null
                        && !variant1.getStudies().get(0).getFiles().isEmpty()
                        && StringUtils.isNotBlank(variant1.getStudies().get(0).getFiles().get(0).getCall())
                        && variant2.getStudies().get(0).getFiles() != null
                        && !variant2.getStudies().get(0).getFiles().isEmpty()
                        && StringUtils.isNotBlank(variant2.getStudies().get(0).getFiles().get(0).getCall())
                        && variant1.getStudies().get(0).getFiles().get(0).getCall()
                        .equals(variant2.getStudies().get(0).getFiles().get(0).getCall());

            // Checks that in both genotypes there's something different than a reference allele, i.e. that none of
            // them is 0/0 (or 0 for haploid)
            } else if (alternatePresent(genotype1) && alternatePresent(genotype2)) {

                if (genotype1.contains(UNPHASED_GENOTYPE_SEPARATOR)) {
                    return false;
                }

                if (genotype2.contains(UNPHASED_GENOTYPE_SEPARATOR)) {
                    return false;
                }

                // None of the genotypes fully missing nor un-phased
                String[] genotypeParts = genotype1.split(PHASED_GENOTYPE_SEPARATOR);
                String[] genotypeParts1 = genotype2.split(PHASED_GENOTYPE_SEPARATOR);

                // TODO: code below might not work for multiallelic positions
                // For hemizygous variants lets just consider that the phase is the same if both are hemizygous
                // First genotype alternate hemizygous
                if (genotypeParts.length == 1) {
                    return genotypeParts1.length == 1;
                // Second genotype alternate hemizygous
                } else if (genotypeParts1.length == 1) {
                    // First genotype diploid, second genotype alternate hemizygous
                    return false;

                // Both genotypes diploid
                } else {
                    return genotypeParts[0].equals(genotypeParts1[0])
                            && genotypeParts[2].equals(genotypeParts1[2]);
                }

            // At least one of the genotypes contains just reference alleles. Clearly, alleles cannot be in phase since
            // one of them is not even present!
            } else {
                return false;
            }

        // If PS is different both variants might not be in phase
        } else {
            return false;
        }
    }

    /**
     * TODO: this code does not work properly for multiallelic positions.
     * @param genotype String codifying for the genotype in VCF-like way, e.g. 0/1, 1|0, 0, ...
     * @return whether an alternate allele is present.
     */
    private boolean alternatePresent(String genotype) {

        return genotype != null && genotype.contains(ALTERNATE);

    }

    private String getMostSevereConsequenceType(List<ConsequenceType> consequenceTypeList) {
        int max = -1;
        String mostSevereConsequencetype = null;
        for (ConsequenceType consequenceType : consequenceTypeList) {
            for (SequenceOntologyTerm sequenceOntologyTerm : consequenceType.getSequenceOntologyTerms()) {
                try {
                    int rank = VariantAnnotationUtils.SO_SEVERITY.get(sequenceOntologyTerm.getName());
                    if (rank > max) {
                        max = rank;
                        mostSevereConsequencetype = sequenceOntologyTerm.getName();
                    }
                } catch (Exception e) {
                    int a = 1;
                }
            }
        }

        return mostSevereConsequencetype;
    }

    private Set<String> getAnnotatorSet(QueryOptions queryOptions) {
        Set<String> annotatorSet;
        List<String> includeList = queryOptions.getAsStringList("include");
        if (includeList.size() > 0) {
            annotatorSet = new HashSet<>(includeList);
        } else {
            annotatorSet = new HashSet<>(Arrays.asList("variation", "clinical", "conservation", "functionalScore",
                    "consequenceType", "expression", "geneDisease", "drugInteraction", "populationFrequencies",
                    "repeats", "cytoband", "hgvs"));
            List<String> excludeList = queryOptions.getAsStringList("exclude");
            excludeList.forEach(annotatorSet::remove);
        }
        return annotatorSet;
    }

    private String getIncludedGeneFields(Set<String> annotatorSet) {
        String includeGeneFields = "name,id,chromosome,start,end,transcripts.id,transcripts.proteinID,"
                + "transcripts.start,transcripts.end,transcripts.cDnaSequence,transcripts.proteinSequence,"
                + "transcripts.strand,transcripts.cdsLength,transcripts.annotationFlags,transcripts.biotype,"
                + "transcripts.genomicCodingStart,transcripts.genomicCodingEnd,transcripts.cdnaCodingStart,"
                + "transcripts.cdnaCodingEnd,transcripts.exons.start,transcripts.exons.cdsStart,transcripts.exons.end,"
                + "transcripts.exons.cdsEnd,transcripts.exons.sequence,transcripts.exons.phase,"
                + "transcripts.exons.exonNumber,mirna.matures,mirna.sequence,mirna.matures.cdnaStart,"
                + "transcripts.exons.genomicCodingStart,transcripts.exons.genomicCodingEnd,"
                + "mirna.matures.cdnaEnd";

        if (annotatorSet.contains("expression")) {
            includeGeneFields += ",annotation.expression";
        }
        if (annotatorSet.contains("geneDisease")) {
            includeGeneFields += ",annotation.diseases";
        }
        if (annotatorSet.contains("drugInteraction")) {
            includeGeneFields += ",annotation.drugs";
        }
        return includeGeneFields;
    }

    private List<Gene> getAffectedGenes(List<Gene> batchGeneList, Variant variant) {
        List<Gene> geneList = new ArrayList<>(batchGeneList.size());
        for (Gene gene : batchGeneList) {
            for (Region region : variantToRegionList(variant)) {
                if (region.getChromosome().equals(gene.getChromosome()) && gene.getStart() <= (region.getEnd() + 5000)
                        && gene.getEnd() >= Math.max(1, region.getStart() - 5000)) {
                    geneList.add(gene);
                }
            }
        }
        return geneList;
    }

    private List<Gene> getGenesInRange(String chromosome, int start, int end, String includeFields) {
        QueryOptions queryOptions = new QueryOptions("include", includeFields);

        return geneDBAdaptor
                .getByRegion(new Region(chromosome, Math.max(1, start - 5000),
                        end + 5000), queryOptions).getResult();
    }

    private boolean nonSynonymous(ConsequenceType consequenceType, boolean useMitochondrialCode) {
        if (consequenceType.getCodon() == null) {
            return false;
        } else {
            String[] parts = consequenceType.getCodon().split("/");
            String ref = String.valueOf(parts[0]).toUpperCase();
            String alt = String.valueOf(parts[1]).toUpperCase();
            return !VariantAnnotationUtils.isSynonymousCodon(useMitochondrialCode, ref, alt)
                    && !VariantAnnotationUtils.isStopCodon(useMitochondrialCode, ref);
        }
    }

    private ProteinVariantAnnotation getProteinAnnotation(ConsequenceType consequenceType) {
        if (consequenceType.getProteinVariantAnnotation() != null) {
            QueryResult<ProteinVariantAnnotation> proteinVariantAnnotation = proteinDBAdaptor.getVariantAnnotation(
                    consequenceType.getEnsemblTranscriptId(),
                    consequenceType.getProteinVariantAnnotation().getPosition(),
                    consequenceType.getProteinVariantAnnotation().getReference(),
                    consequenceType.getProteinVariantAnnotation().getAlternate(), new QueryOptions());

            if (proteinVariantAnnotation.getNumResults() > 0) {
                return proteinVariantAnnotation.getResult().get(0);
            }
        }
        return null;
    }

    private ConsequenceTypeCalculator getConsequenceTypeCalculator(Variant variant) throws UnsupportedURLVariantFormat {
        switch (VariantAnnotationUtils.getVariantType(variant)) {
            case SNV:
                return new ConsequenceTypeSNVCalculator();
            case INSERTION:
                return new ConsequenceTypeInsertionCalculator(genomeDBAdaptor);
            case DELETION:
                return new ConsequenceTypeDeletionCalculator(genomeDBAdaptor);
            case MNV:
                return new ConsequenceTypeMNVCalculator(genomeDBAdaptor);
            case CNV:
                if (variant.getSv().getCopyNumber() == null) {
                    return new ConsequenceTypeGenericRegionCalculator();
                } else if (variant.getSv().getCopyNumber() > 2) {
                    return new ConsequenceTypeCNVGainCalculator();
                } else {
                    return new ConsequenceTypeDeletionCalculator(genomeDBAdaptor);
                }
            case DUPLICATION:
                return new ConsequenceTypeCNVGainCalculator();
            case INVERSION:
                return new ConsequenceTypeGenericRegionCalculator();
            case BREAKEND:
                return new ConsequenceTypeBNDCalculator();
            default:
                throw new UnsupportedURLVariantFormat();
        }
    }

    private boolean[] getRegulatoryRegionOverlaps(Variant variant) {
        // 0: overlaps any regulatory region type
        // 1: overlaps transcription factor binding site
        boolean[] overlapsRegulatoryRegion = {false, false};

        // Variant type checked in expected order of frequency of occurrence to minimize number of checks
        // Most queries will be SNVs - it's worth implementing an special case for them
        if (VariantType.SNV.equals(variant.getType())) {
            return getRegulatoryRegionOverlaps(variant.getChromosome(), variant.getStart());
        } else if (VariantType.INDEL.equals(variant.getType()) && StringUtils.isBlank(variant.getReference())) {
            return getRegulatoryRegionOverlaps(variant.getChromosome(), variant.getStart() - 1, variant.getEnd());
        // Short deletions and symbolic variants except breakends
        } else if (!VariantType.BREAKEND.equals(variant.getType())) {
            return getRegulatoryRegionOverlaps(variant.getChromosome(), variant.getStart(), variant.getEnd());
        // Breakend "variants" only annotate features overlapping the exact positions
        } else  {
            overlapsRegulatoryRegion = getRegulatoryRegionOverlaps(variant.getChromosome(), Math.max(1, variant.getStart()));
            // If already found one overlapping regulatory region there's no need to keep checking
            if (overlapsRegulatoryRegion[0]) {
                return overlapsRegulatoryRegion;
            // Otherwise check the other breakend in case exists
            } else {
                if (variant.getSv() != null && variant.getSv().getBreakend() != null
                    && variant.getSv().getBreakend().getMate() != null) {
                    return getRegulatoryRegionOverlaps(variant.getSv().getBreakend().getMate().getChromosome(),
                            Math.max(1, variant.getSv().getBreakend().getMate().getPosition()));
                } else {
                    return overlapsRegulatoryRegion;
                }
            }
        }

//        List<RegulatoryFeature> regionList = new ArrayList<>(queryResult.getNumResults());
//        for (RegulatoryFeature object : queryResult.getResult()) {
//            regionList.add(object);
//        }


//        return regionList;
    }

    private boolean[] getRegulatoryRegionOverlaps(String chromosome, Integer position) {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("include", REGULATORY_REGION_FEATURE_TYPE_ATTRIBUTE);
        // 0: overlaps any regulatory region type
        // 1: overlaps transcription factor binding site
        boolean[] overlapsRegulatoryRegion = {false, false};

        QueryResult<RegulatoryFeature> queryResult = regulationDBAdaptor
                .get(new Query(REGION, toRegionString(chromosome, position)), queryOptions);

        if (queryResult.getNumTotalResults() > 0) {
            overlapsRegulatoryRegion[0] = true;
            boolean tfbsFound = false;
            for (int i = 0; (i < queryResult.getResult().size() && !tfbsFound); i++) {
                String regulatoryRegionType = queryResult.getResult().get(i).getFeatureType();
                tfbsFound = regulatoryRegionType != null
                        && (regulatoryRegionType.equals(RegulationDBAdaptor.FeatureType.TF_binding_site.name())
                        || queryResult.getResult().get(i).getFeatureType()
                            .equals(RegulationDBAdaptor.FeatureType.TF_binding_site_motif.name()));
            }
            overlapsRegulatoryRegion[1] = tfbsFound;
        }

        return overlapsRegulatoryRegion;
    }

    private boolean[] getRegulatoryRegionOverlaps(String chromosome, Integer start, Integer end) {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("exclude", "_id");
        queryOptions.add("include", "chromosome");
        queryOptions.add("limit", "1");
        // 0: overlaps any regulatory region type
        // 1: overlaps transcription factor binding site
        boolean[] overlapsRegulatoryRegion = {false, false};
        String regionString = toRegionString(chromosome, start, end);
        Query query = new Query(REGION, regionString);

        QueryResult<RegulatoryFeature> queryResult = regulationDBAdaptor
                .get(query.append(REGULATORY_REGION_FEATURE_TYPE_ATTRIBUTE, TF_BINDING_SITE), queryOptions);

        // Overlaps transcription factor binding site - it's therefore a regulatory variant
        if (queryResult.getNumResults() == 1) {
            overlapsRegulatoryRegion[0] = true;
            overlapsRegulatoryRegion[1] = true;
        // Does not overlap transcription factor binding site - check any other regulatory region type
        } else {
            query.remove(REGULATORY_REGION_FEATURE_TYPE_ATTRIBUTE);
            queryResult = regulationDBAdaptor.get(query, queryOptions);
            // Does overlap other types of regulatory regions
            if (queryResult.getNumResults() == 1) {
                overlapsRegulatoryRegion[0] = true;
            }
        }

        return overlapsRegulatoryRegion;
    }

    private String toRegionString(String chromosome, Integer position) {
        return toRegionString(chromosome, position, position);
    }

    private String toRegionString(String chromosome, Integer start, Integer end) {
        StringBuilder stringBuilder = new StringBuilder(chromosome);
        stringBuilder.append(":");
        stringBuilder.append(start);
        stringBuilder.append("-");
        stringBuilder.append(end == null ? start : end);
        return stringBuilder.toString();
    }

    private List<ConsequenceType> getConsequenceTypeList(Variant variant, List<Gene> geneList,
                                                         boolean regulatoryAnnotation, QueryOptions queryOptions) {
        boolean[] overlapsRegulatoryRegion = {false, false};
        if (regulatoryAnnotation) {
            overlapsRegulatoryRegion = getRegulatoryRegionOverlaps(variant);
        }
        ConsequenceTypeCalculator consequenceTypeCalculator = getConsequenceTypeCalculator(variant);
        List<ConsequenceType> consequenceTypeList = consequenceTypeCalculator.run(variant, geneList,
                overlapsRegulatoryRegion, queryOptions);
        if (variant.getType() == VariantType.SNV
                || Variant.inferType(variant.getReference(), variant.getAlternate()) == VariantType.SNV) {
            for (ConsequenceType consequenceType : consequenceTypeList) {
                if (nonSynonymous(consequenceType, variant.getChromosome().equals("MT"))) {
                    consequenceType.setProteinVariantAnnotation(getProteinAnnotation(consequenceType));
                }
            }
        }
        return consequenceTypeList;
    }

    private List<Region> variantListToRegionList(List<Variant> variantList) {
//        return variantList.stream().map((variant) -> variantToRegion(variant)).collect(Collectors.toList());

        // In great majority of cases returned region list size will equal variant list; this will happen except when
        // there's a breakend within the variantList
        List<Region> regionList = new ArrayList<>(variantList.size());

        for (Variant variant : variantList) {
            regionList.addAll(variantToRegionList(variant));
        }

        return regionList;

//        List<Region> regionList = new ArrayList<>(variantList.size());
//        if (imprecise) {
//            for (Variant variant : variantList) {
//                if (VariantType.CNV.equals(variant.getType())) {
//                    regionList.add(new Region(variant.getChromosome(),
//                            variant.getStart() - cnvExtraPadding,
//                            variant.getEnd() + cnvExtraPadding));
//                } else if (variant.getSv() != null) {
//                    regionList.add(new Region(variant.getChromosome(),
//                            variant.getSv() != null && variant.getSv().getCiStartLeft() != null
//                                    ? variant.getSv().getCiStartLeft() : variant.getStart(),
//                            variant.getSv() != null && variant.getSv().getCiEndRight() != null
//                                    ? variant.getSv().getCiEndRight() : variant.getEnd()));
//                // Insertion
//                } else if (variant.getStart() > variant.getEnd()) {
//                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getStart()));
//                // Other but insertion
//                } else {
//                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getEnd()));
//                }
//            }
//        } else {
//            for (Variant variant : variantList) {
//                // Insertion
//                if (variant.getStart() > variant.getEnd()) {
//                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getStart()));
//                // Other but insertion
//                } else {
//                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getEnd()));
//                }
//            }
//        }
//        return regionList;
    }

    private List<Region> variantToRegionList(Variant variant) {
        // Variant type checked in expected order of frequency of occurrence to minimize number of checks
        // SNV
        if (VariantType.SNV.equals(variant.getType())) {
            return Collections.singletonList(new Region(variant.getChromosome(), variant.getStart(), variant.getEnd()));
        // Short insertion
        } else if (VariantType.INDEL.equals(variant.getType()) && StringUtils.isBlank(variant.getReference())) {
            return Collections.singletonList(new Region(variant.getChromosome(), variant.getStart() - 1,
                    variant.getEnd()));
        // CNV
        } else if (VariantType.CNV.equals(variant.getType())) {
            if (imprecise) {
                return Collections.singletonList(new Region(variant.getChromosome(),
                        variant.getStart() - cnvExtraPadding, variant.getEnd() + cnvExtraPadding));
            } else {
                return Collections.singletonList(new Region(variant.getChromosome(), variant.getStart(),
                        variant.getEnd()));
            }
        // BREAKEND
        } else if (VariantType.BREAKEND.equals(variant.getType())) {
            List<Region> regionList = new ArrayList<>(2);
            regionList.add(startBreakpointToRegion(variant));
            Variant breakendMate = VariantBuilder.getMateBreakend(variant);
            if (breakendMate != null) {
                regionList.add(startBreakpointToRegion(breakendMate));
            }
            return regionList;
        // Short deletions and symbolic variants (no BREAKENDS expected althought not checked either)
        } else {
            if (imprecise && variant.getSv() != null) {
                return Collections.singletonList(new Region(variant.getChromosome(),
                        variant.getSv().getCiStartLeft() != null
                            ? variant.getSv().getCiStartLeft() - svExtraPadding : variant.getStart(),
                        variant.getSv().getCiEndRight() != null ? variant.getSv().getCiEndRight() + svExtraPadding
                                : variant.getEnd()));
            } else {
                return Collections.singletonList(new Region(variant.getChromosome(), variant.getStart(),
                        variant.getEnd()));
            }
        }
    }

    private List<Region> breakpointsToRegionList(Variant variant) {
        List<Region> regionList = new ArrayList<>();

        switch (variant.getType()) {
            case SNV:
                regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getStart()));
                break;
            case CNV:
                if (imprecise) {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart() - cnvExtraPadding,
                            variant.getStart() + cnvExtraPadding));
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd() - cnvExtraPadding,
                            variant.getEnd() + cnvExtraPadding));
                } else {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getStart()));
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getEnd()));
                }
                break;
            case BREAKEND:
                regionList.add(startBreakpointToRegion(variant));
                Variant breakendMate = VariantBuilder.getMateBreakend(variant);
                if (breakendMate != null) {
                    regionList.add(startBreakpointToRegion(breakendMate));
                }
                break;
            default:
                if (imprecise && variant.getSv() != null) {
                    regionList.add(new Region(variant.getChromosome(), variant.getSv().getCiStartLeft() != null
                                    ? variant.getSv().getCiStartLeft() - svExtraPadding : variant.getStart(),
                            variant.getSv().getCiStartRight() != null
                                    ? variant.getSv().getCiStartRight() + svExtraPadding : variant.getStart()));
                    regionList.add(new Region(variant.getChromosome(),
                            variant.getSv().getCiEndLeft() != null
                                    ? variant.getSv().getCiEndLeft() - svExtraPadding : variant.getEnd(),
                            variant.getSv().getCiEndRight() != null
                                    ? variant.getSv().getCiEndRight() + svExtraPadding : variant.getEnd()));
                } else {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getStart()));
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getEnd()));
                }
                break;
        }

        return regionList;
    }

    private Region startBreakpointToRegion(Variant variant) {
        if (imprecise && variant.getSv() != null) {
            return new Region(variant.getChromosome(), variant.getSv().getCiStartLeft() != null
                    ? variant.getSv().getCiStartLeft() - svExtraPadding : variant.getStart(),
                    variant.getSv().getCiStartRight() != null
                            ? variant.getSv().getCiStartRight() + svExtraPadding : variant.getStart());
        } else {
            return new Region(variant.getChromosome(), variant.getStart(), variant.getStart());
        }
    }

    /*
     * Future classes for Async annotations
     */
    class FutureVariationAnnotator implements Callable<List<QueryResult<Variant>>> {
        private List<Variant> variantList;
        private QueryOptions queryOptions;

        FutureVariationAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.queryOptions = queryOptions;
        }

        @Override
        public List<QueryResult<Variant>> call() throws Exception {
            long startTime = System.currentTimeMillis();
            logger.debug("Query variation");
            List<QueryResult<Variant>> variationQueryResultList
                    = variantDBAdaptor.getPopulationFrequencyByVariant(variantList, queryOptions);
            logger.debug("Variation query performance is {}ms for {} variants", System.currentTimeMillis() - startTime, variantList.size());
            return variationQueryResultList;
        }

        public void processResults(Future<List<QueryResult<Variant>>> variationFuture,
                                   List<VariantAnnotation> variantAnnotationList,
                                   Set<String> annotatorSet) throws InterruptedException, ExecutionException {

            while (!variationFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Variant>> variationQueryResults = variationFuture.get();
            if (variationQueryResults != null) {
                for (int i = 0; i < variantAnnotationList.size(); i++) {
                    Variant preferredVariant = getPreferredVariant(variationQueryResults.get(i));
                    if (preferredVariant != null) {
                        if (preferredVariant.getIds().size() > 0) {
                            variantAnnotationList.get(i).setId(preferredVariant.getIds().get(0));
                        }
                        if (preferredVariant.getAnnotation() != null
                                && preferredVariant.getAnnotation().getAdditionalAttributes() != null
                                && preferredVariant.getAnnotation().getAdditionalAttributes().size() > 0) {
                            variantAnnotationList.get(i)
                                    .setAdditionalAttributes(preferredVariant.getAnnotation().getAdditionalAttributes());
                        }
                    }

                    if (annotatorSet.contains("populationFrequencies") && preferredVariant != null) {
                        variantAnnotationList.get(i)
                                .setPopulationFrequencies(preferredVariant.getAnnotation().getPopulationFrequencies());
                    }
                }
            }
        }
    }

    class FutureConservationAnnotator implements Callable<List<QueryResult>> {
        private List<Variant> variantList;

        private QueryOptions queryOptions;

        FutureConservationAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.queryOptions = queryOptions;
        }

        @Override
        public List<QueryResult> call() throws Exception {
            long startTime = System.currentTimeMillis();

            List<QueryResult> queryResultList = new ArrayList<>(variantList.size());

            logger.debug("Query conservation");
            // Want to return only one QueryResult object per Variant
            for (Variant variant : variantList) {

                // Truncate region size of SVs to avoid server collapse
                List<Region> regionList
                        = variantToRegionList(variant)
                        .stream()
                        .map(region -> region.size() > 50
                                ? (new Region(region.getChromosome(), region.getStart(), region.getStart() + 49))
                                : region).collect(Collectors.toList());

                List<QueryResult> tmpQueryResultList = conservationDBAdaptor
                    .getAllScoresByRegionList(regionList, queryOptions);

                // There may be more than one QueryResult per variant for breakends
                // Reuse one of the QueryResult objects returned by the adaptor
                QueryResult newQueryResult = tmpQueryResultList.get(0);
                if (tmpQueryResultList.size() > 1) {
                    // Reuse one of the QueryResult objects - new result is the set formed by the scores corresponding
                    // to the two breakpoints
                    newQueryResult.getResult().addAll(tmpQueryResultList.get(1).getResult());
                    newQueryResult.setNumResults(newQueryResult.getResult().size());
                    newQueryResult.setNumTotalResults(newQueryResult.getResult().size());
                }
                queryResultList.add(newQueryResult);
            }

            logger.debug("Conservation query performance is {}ms for {} variants", System.currentTimeMillis() - startTime,
                    variantList.size());
            return queryResultList;
        }

        public void processResults(Future<List<QueryResult>> conservationFuture,
                                   List<VariantAnnotation> variantAnnotationList)
                throws InterruptedException, ExecutionException {
            while (!conservationFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult> conservationQueryResults = conservationFuture.get();
            if (conservationQueryResults != null) {
                for (int i = 0; i < variantAnnotationList.size(); i++) {
                    variantAnnotationList.get(i)
                            .setConservation((List<Score>) conservationQueryResults.get(i).getResult());
                }
            }
        }

    }

    class FutureVariantFunctionalScoreAnnotator implements Callable<List<QueryResult<Score>>> {
        private List<Variant> variantList;

        private QueryOptions queryOptions;

        FutureVariantFunctionalScoreAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.queryOptions = queryOptions;
        }

        @Override
        public List<QueryResult<Score>> call() throws Exception {
            long startTime = System.currentTimeMillis();
//            List<QueryResult> variantFunctionalScoreQueryResultList =
//                    variantFunctionalScoreDBAdaptor.getAllByVariantList(variantList, queryOptions);
            logger.debug("Query variant functional score");
            List<QueryResult<Score>> variantFunctionalScoreQueryResultList =
                    variantDBAdaptor.getFunctionalScoreVariant(variantList, queryOptions);
            logger.debug("VariantFunctionalScore query performance is {}ms for {} variants",
                    System.currentTimeMillis() - startTime, variantList.size());
            return variantFunctionalScoreQueryResultList;
        }

        public void processResults(Future<List<QueryResult<Score>>> variantFunctionalScoreFuture,
                                   List<VariantAnnotation> variantAnnotationList)
                throws InterruptedException, ExecutionException {

            while (!variantFunctionalScoreFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Score>> variantFunctionalScoreQueryResults = variantFunctionalScoreFuture.get();
            if (variantFunctionalScoreQueryResults != null) {
                for (int i = 0; i < variantAnnotationList.size(); i++) {
                    if (variantFunctionalScoreQueryResults.get(i).getNumResults() > 0) {
                        variantAnnotationList.get(i)
                                .setFunctionalScore((List<Score>) variantFunctionalScoreQueryResults.get(i).getResult());
                    }
                }
            }
        }
    }

    class FutureClinicalAnnotator implements Callable<List<QueryResult<Variant>>> {
        private static final String CLINVAR = "clinvar";
        private static final String COSMIC = "cosmic";
        private static final String CLINICAL_SIGNIFICANCE_IN_SOURCE_FILE = "ClinicalSignificance_in_source_file";
        private static final String REVIEW_STATUS_IN_SOURCE_FILE = "ReviewStatus_in_source_file";
        private static final String MUTATION_SOMATIC_STATUS_IN_SOURCE_FILE = "mutationSomaticStatus_in_source_file";
        private static final String SYMBOL = "symbol";
        private List<Variant> variantList;
        private List<Gene> batchGeneList;
        private QueryOptions queryOptions;

        FutureClinicalAnnotator(List<Variant> variantList, List<Gene> batchGeneList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.batchGeneList = batchGeneList;
            this.queryOptions = queryOptions;
        }

        @Override
        public List<QueryResult<Variant>> call() throws Exception {
            long startTime = System.currentTimeMillis();
            List<QueryResult<Variant>> clinicalQueryResultList = clinicalDBAdaptor.getByVariant(variantList,
                    batchGeneList,
                    queryOptions);
            logger.debug("Clinical query performance is {}ms for {} variants", System.currentTimeMillis() - startTime, variantList.size());
            return clinicalQueryResultList;
        }

        public void processResults(Future<List<QueryResult<Variant>>> clinicalFuture,
                                   List<VariantAnnotation> variantAnnotationList)
                throws InterruptedException, ExecutionException {
//            try {
            while (!clinicalFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Variant>> clinicalQueryResults = clinicalFuture.get();
            if (clinicalQueryResults != null) {
                for (int i = 0; i < variantAnnotationList.size(); i++) {
                    QueryResult<Variant> clinicalQueryResult = clinicalQueryResults.get(i);
                    if (clinicalQueryResult.getResult() != null && clinicalQueryResult.getResult().size() > 0) {
                        variantAnnotationList.get(i)
                                .setTraitAssociation(clinicalQueryResult.getResult().get(0).getAnnotation()
                                        .getTraitAssociation());
                        // DEPRECATED
                        // TODO: remove in 4.6
                        variantAnnotationList.get(i)
                                .setVariantTraitAssociation(convertToVariantTraitAssociation(clinicalQueryResult
                                        .getResult()
                                        .get(0)
                                        .getAnnotation()
                                        .getTraitAssociation()));
                    }
                }
            }
//            } catch (ExecutionException e) {
////            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
        }

        private VariantTraitAssociation convertToVariantTraitAssociation(List<EvidenceEntry> traitAssociation) {
            List<ClinVar> clinvarList = new ArrayList<>();
            List<Cosmic> cosmicList = new ArrayList<>(traitAssociation.size());
            for (EvidenceEntry evidenceEntry : traitAssociation) {
                switch (evidenceEntry.getSource().getName()) {
                    case CLINVAR:
                        clinvarList.add(parseClinvar(evidenceEntry));
                        break;
                    case COSMIC:
                        cosmicList.add(parseCosmic(evidenceEntry));
                        break;
                    default:
                        break;
                }
            }
            return new VariantTraitAssociation(clinvarList, null, cosmicList);
        }

        private Cosmic parseCosmic(EvidenceEntry evidenceEntry) {
            String primarySite = null;
            String siteSubtype = null;
            String primaryHistology = null;
            String histologySubtype = null;
            String sampleSource = null;
            String tumourOrigin = null;
            if (evidenceEntry.getSomaticInformation() != null) {
                primarySite = evidenceEntry.getSomaticInformation().getPrimarySite();
                siteSubtype = evidenceEntry.getSomaticInformation().getSiteSubtype();
                primaryHistology = evidenceEntry.getSomaticInformation().getPrimaryHistology();
                histologySubtype = evidenceEntry.getSomaticInformation().getHistologySubtype();
                sampleSource = evidenceEntry.getSomaticInformation().getSampleSource();
                tumourOrigin = evidenceEntry.getSomaticInformation().getTumourOrigin();
            }
            return new Cosmic(evidenceEntry.getId(), primarySite, siteSubtype, primaryHistology, histologySubtype,
                    sampleSource, tumourOrigin, parseGeneName(evidenceEntry),
                    getAdditionalProperty(evidenceEntry, MUTATION_SOMATIC_STATUS_IN_SOURCE_FILE));
        }

        private String parseGeneName(EvidenceEntry evidenceEntry) {
            if (evidenceEntry.getGenomicFeatures() != null && !evidenceEntry.getGenomicFeatures().isEmpty()
                    && evidenceEntry.getGenomicFeatures().get(0).getXrefs() != null) {
                // There may be more than one genomic feature for cosmic evidence entries. However, the actual gene symbol
                // is expected to be found at index 0.
                return evidenceEntry.getGenomicFeatures().get(0).getXrefs().get(SYMBOL);
            }
            return null;
        }

        private ClinVar parseClinvar(EvidenceEntry evidenceEntry) {
            String clinicalSignificance = getAdditionalProperty(evidenceEntry, CLINICAL_SIGNIFICANCE_IN_SOURCE_FILE);
            List<String> traitList = null;
            if (evidenceEntry.getHeritableTraits() != null) {
                traitList = evidenceEntry
                        .getHeritableTraits()
                        .stream()
                        .map((heritableTrait) -> heritableTrait.getTrait())
                        .collect(Collectors.toList());
            }
            List<String> geneNameList = null;
            if (evidenceEntry.getGenomicFeatures() != null) {
                geneNameList = evidenceEntry
                        .getGenomicFeatures()
                        .stream()
                        .map((genomicFeature) -> genomicFeature.getXrefs().get(SYMBOL))
                        .collect(Collectors.toList());
            }
            String reviewStatus = getAdditionalProperty(evidenceEntry, REVIEW_STATUS_IN_SOURCE_FILE);
            return new ClinVar(evidenceEntry.getId(), clinicalSignificance, traitList, geneNameList,
                    reviewStatus);
        }

        private String getAdditionalProperty(EvidenceEntry evidenceEntry, String name) {
            if (evidenceEntry.getAdditionalProperties() != null) {
                for (Property property : evidenceEntry.getAdditionalProperties()) {
                    if (name.equals(property.getName())) {
                        return property.getValue();
                    }
                }
            }
            return null;
        }
    }

    class FutureRepeatsAnnotator implements Callable<List<QueryResult<Repeat>>> {
        private List<Variant> variantList;
        private QueryOptions queryOptions;

        FutureRepeatsAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.queryOptions = queryOptions;
        }

        public List<QueryResult<Repeat>> call() throws Exception {
//            List<QueryResult<Repeat>> queryResultList
//                    = repeatsDBAdaptor.getByRegion(variantListToRegionList(variantList), queryOptions);

            long startTime = System.currentTimeMillis();
            List<QueryResult<Repeat>> queryResultList = new ArrayList<>(variantList.size());

            logger.debug("Query repeats");
            // Want to return only one QueryResult object per Variant
            for (Variant variant : variantList) {
                List<QueryResult<Repeat>> tmpQueryResultList = repeatsDBAdaptor
                        .getByRegion(breakpointsToRegionList(variant), queryOptions);

                // There may be more than one QueryResult per variant for non SNV variants since there will be
                // two breakpoints
                // Reuse one of the QueryResult objects returned by the adaptor
                QueryResult newQueryResult = tmpQueryResultList.get(0);
                if (tmpQueryResultList.size() > 1) {
                    Set<Repeat> repeatSet = new HashSet<>(newQueryResult.getResult());
                    // Reuse one of the QueryResult objects - new result is the set formed by the repeats corresponding
                    // to the two breakpoints
                    repeatSet.addAll(tmpQueryResultList.get(1).getResult());
                    newQueryResult.setNumResults(repeatSet.size());
                    newQueryResult.setNumTotalResults(repeatSet.size());
                    newQueryResult.setResult(new ArrayList(repeatSet));
                }
                queryResultList.add(newQueryResult);
            }

            logger.debug("Repeat query performance is {}ms for {} variants", System.currentTimeMillis() - startTime,
                    variantList.size());

            return queryResultList;

        }

        public void processResults(Future<List<QueryResult<Repeat>>> repeatsFuture,
                                   List<VariantAnnotation> variantAnnotationResults)
                throws InterruptedException, ExecutionException {
//            try {
            while (!repeatsFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Repeat>> queryResultList = repeatsFuture.get();
            if (queryResultList != null) {
                for (int i = 0; i < variantAnnotationResults.size(); i++) {
                    QueryResult<Repeat> queryResult = queryResultList.get(i);
                    if (queryResult.getResult() != null && queryResult.getResult().size() > 0) {
                        variantAnnotationResults.get(i)
                                .setRepeat(queryResult.getResult());
                    }
                }
            }
//            } catch (ExecutionException e) {
////            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
        }
    }

    class FutureCytobandAnnotator implements Callable<List<QueryResult<Cytoband>>> {
        private List<Variant> variantList;
        private QueryOptions queryOptions;

        FutureCytobandAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.queryOptions = queryOptions;
        }

        @Override
        public List<QueryResult<Cytoband>> call() throws Exception {
            long startTime = System.currentTimeMillis();
            List<QueryResult<Cytoband>> queryResultList = new ArrayList<>(variantList.size());

            logger.debug("Query cytoband");
            // Want to return only one QueryResult object per Variant
            for (Variant variant : variantList) {
                List<QueryResult<Cytoband>> tmpQueryResultList = genomeDBAdaptor
                        .getCytobands(breakpointsToRegionList(variant));

                // There may be more than one QueryResult per variant for non SNV variants since there will be
                // two breakpoints
                // Reuse one of the QueryResult objects returned by the adaptor
                QueryResult newQueryResult = tmpQueryResultList.get(0);
                if (tmpQueryResultList.size() > 1) {
                    Set<Cytoband> cytobandSet = new HashSet<>(newQueryResult.getResult());
                    // Reuse one of the QueryResult objects - new result is the set formed by the cytobands corresponding
                    // to the two breakpoints
                    cytobandSet.addAll(tmpQueryResultList.get(1).getResult());
                    newQueryResult.setNumResults(cytobandSet.size());
                    newQueryResult.setNumTotalResults(cytobandSet.size());
                    newQueryResult.setResult(new ArrayList(cytobandSet));
                }
                queryResultList.add(newQueryResult);
            }

            logger.debug("Cytoband query performance is {}ms for {} variants", System.currentTimeMillis() - startTime,
                    variantList.size());
            return queryResultList;
        }

        public void processResults(Future<List<QueryResult<Cytoband>>> cytobandFuture,
                                   List<VariantAnnotation> variantAnnotationList)
                throws InterruptedException, ExecutionException {
            while (!cytobandFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Cytoband>> queryResultList = cytobandFuture.get();
            if (queryResultList != null) {
                if (queryResultList.isEmpty()) {
                    StringBuilder stringbuilder = new StringBuilder(variantList.get(0).toString());
                    for (int i = 1; i < variantList.size(); i++) {
                        stringbuilder.append(",").append(variantList.get(i).toString());
                    }
                    logger.warn("NO cytoband was found for any of these variants: {}", stringbuilder.toString());
                } else {
                    // Cytoband lists are returned in the same order in which variants are queried
                    for (int i = 0; i < variantAnnotationList.size(); i++) {
                        QueryResult queryResult = queryResultList.get(i);
                        if (queryResult.getResult() != null && queryResult.getResult().size() > 0) {
                            variantAnnotationList.get(i).setCytoband(queryResult.getResult());
                        }
                    }
                }
            }
        }
    }

//    private class FutureHgvsAnnotator implements Callable<List<QueryResult<String>>> {
//        private List<Variant> variantList;
//        private QueryOptions queryOptions;
//
//        FutureHgvsAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
//            this.variantList = variantList;
//            this.queryOptions = queryOptions;
//        }
//
//        @Override
//        public List<QueryResult<String>> call() throws Exception {
//            long startTime = System.currentTimeMillis();
//            List<QueryResult<String>> queryResultList = hgvsCalculator.run(variantList);
//            logger.debug("HGVS query performance is {}ms for {} variants", System.currentTimeMillis() - startTime,
//                    variantList.size());
//            return queryResultList;
//        }
//
//        public void processResults(Future<List<QueryResult<Cytoband>>> cytobandFuture,
//                                   List<QueryResult<VariantAnnotation>> variantAnnotationResults)
//                throws InterruptedException, ExecutionException {
//            while (!cytobandFuture.isDone()) {
//                Thread.sleep(1);
//            }
//
//            List<QueryResult<Cytoband>> queryResultList = cytobandFuture.get();
//            if (queryResultList != null) {
//                if (queryResultList.isEmpty()) {
//                    StringBuilder stringbuilder = new StringBuilder(variantList.get(0).toString());
//                    for (int i = 1; i < variantList.size(); i++) {
//                        stringbuilder.append(",").append(variantList.get(i).toString());
//                    }
//                    logger.warn("NO cytoband was found for any of these variants: {}", stringbuilder.toString());
//                } else {
//                    // Cytoband lists are returned in the same order in which variants are queried
//                    for (int i = 0; i < variantAnnotationResults.size(); i++) {
//                        QueryResult queryResult = queryResultList.get(i);
//                        if (queryResult.getResult() != null && queryResult.getResult().size() > 0) {
//                            variantAnnotationResults.get(i).getResult().get(0).setCytoband(queryResult.getResult());
//                        }
//                    }
//                }
//            }
//        }
//
//    }
}

