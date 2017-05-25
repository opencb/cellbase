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

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.annotation.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.*;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.cellbase.core.api.VariantDBAdaptor.CNV_DEFAULT_PADDING;

//import org.opencb.cellbase.core.db.api.core.ConservedRegionDBAdaptor;
//import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
//import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
//import org.opencb.cellbase.core.db.api.core.ProteinDBAdaptor;
//import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
//import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
//import org.opencb.cellbase.core.db.api.variation.VariantFunctionalScoreDBAdaptor;
//import org.opencb.cellbase.core.db.api.variation.VariationDBAdaptor;

/**
 * Created by imedina on 06/02/16.
 */
/**
 * Created by imedina on 11/07/14.
 *
 * @author Javier Lopez fjlopez@ebi.ac.uk;
 */
public class VariantAnnotationCalculator {
    //extends MongoDBAdaptor implements VariantAnnotationDBAdaptor<VariantAnnotation> {

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

    private DBAdaptorFactory dbAdaptorFactory;
    //    private ObjectMapper geneObjectMapper;
    private final VariantNormalizer normalizer;
    private boolean normalize = false;
    private boolean useCache = true;
    private boolean phased = false;
    private Boolean imprecise = true;
    private Integer svExtraPadding = 0;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


//    public VariantAnnotationCalculator(String species, String assembly, MongoDataStore mongoDataStore) {
////        super(species, assembly, mongoDataStore);
//
//        normalizer = new VariantNormalizer(false);
//        logger.debug("VariantAnnotationMongoDBAdaptor: in 'constructor'");
//    }

    public VariantAnnotationCalculator(String species, String assembly, DBAdaptorFactory dbAdaptorFactory) {
//        this(species, assembly, dbAdaptorFactory, true);
//    }
//
//    public VariantAnnotationCalculator(String species, String assembly, DBAdaptorFactory dbAdaptorFactory,
//                                       boolean normalize) {
        this.normalizer = new VariantNormalizer(false, false, true);
//        this.normalize = normalize;

        this.dbAdaptorFactory = dbAdaptorFactory;

        this.genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        this.variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        this.geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
        this.regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(species, assembly);
        this.proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        this.conservationDBAdaptor = dbAdaptorFactory.getConservationDBAdaptor(species, assembly);
        this.clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);
        this.repeatsDBAdaptor = dbAdaptorFactory.getRepeatsDBAdaptor(species, assembly);

        logger.debug("VariantAnnotationMongoDBAdaptor: in 'constructor'");
    }

    @Deprecated
    public QueryResult getAllConsequenceTypesByVariant(Variant variant, QueryOptions queryOptions) {
        long dbTimeStart = System.currentTimeMillis();

        // We process include and exclude query options to know which annotators to use.
        // Include parameter has preference over exclude.
//        Set<String> annotatorSet = getAnnotatorSet(queryOptions);
//
//        // This field contains all the fields to be returned by overlapping genes
//        String includeGeneFields = getIncludedGeneFields(annotatorSet);

        parseQueryParam(queryOptions);
        List<Gene> geneList = getAffectedGenes(variant, includeGeneFields);

        // TODO the last 'true' parameter needs to be changed by annotatorSet.contains("regulatory") once is ready
        List<ConsequenceType> consequenceTypeList = getConsequenceTypeList(variant, geneList, true);

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

        // Object to be returned
        List<QueryResult<VariantAnnotation>> variantAnnotationResultList;
        if (useCache) {
            variantAnnotationResultList = getCachedPreferredAnnotation(normalizedVariantList);
        } else {
            variantAnnotationResultList = runAnnotationProcess(normalizedVariantList);
        }

        return variantAnnotationResultList;
    }

    private List<QueryResult<VariantAnnotation>> getCachedPreferredAnnotation(List<Variant> variantList)
            throws InterruptedException, ExecutionException {

        // Expected to be very few within a batch, no capacity initialized for the array
        List<Integer> mustRunAnnotationPositions = new ArrayList<>();
        List<Variant> mustRunAnnotation = new ArrayList<>();

        // Expected to be most of them, array capacity set to variantList size
        List<Integer> mustSearchVariationPositions = new ArrayList<>(variantList.size());
        List<Variant> mustSearchVariation = new ArrayList<>();

        // Phased variants cannot be annotated using the variation collection
        if (phased) {
            for (int i = 0; i < variantList.size(); i++) {
                if (isPhased(variantList.get(i))) {
                    mustRunAnnotationPositions.add(i);
                    mustRunAnnotation.add(variantList.get(i));
                } else {
                    mustSearchVariationPositions.add(i);
                    mustSearchVariation.add(variantList.get(i));
                }
            }
        } else {
            for (int i = 0; i < variantList.size(); i++) {
                mustSearchVariationPositions.add(i);
                mustSearchVariation.add(variantList.get(i));
            }
        }

        // Search unphased variants within variation collection
        QueryOptions queryOptions = new QueryOptions("include", getCachedVariationIncludeFields());
        List<QueryResult<Variant>> variationQueryResultList = variantDBAdaptor.getByVariant(mustSearchVariation,
                queryOptions);

        // Object to be returned
        List<QueryResult<VariantAnnotation>> variantAnnotationResultList =
                Arrays.asList(new QueryResult[variantList.size()]);

        // mustSearchVariation and variationQueryResultList do have same size, same order
        for (int i = 0; i < mustSearchVariation.size(); i++) {
            // WARNING: variation collection may contain multiple documents for the same variant. ENSEMBL variation
            // often provides multiple entries for the same variant (<1% variants). This line below will select just
            // one of them.
            Variant cacheVariant = getPreferredVariant(variationQueryResultList.get(i));

            // Variant not found in variation collection or the variant was found but not annotated with CellBase - I can
            // distinguish CellBase from ENSEMBL annotation because when CellBase annotates, it includes chromosome, start,
            // reference and alternate fields - TODO: change this.
            // Must be annotated by running the whole process
//            if (variationQueryResultList.get(i).getNumResults() == 0) {
            if (cacheVariant == null) {
//                    || variationQueryResultList.get(i).getResult().get(0).getAnnotation() == null
//                    || variationQueryResultList.get(i).getResult().get(0).getAnnotation().getConsequenceTypes() == null
//                    || variationQueryResultList.get(i).getResult().get(0).getAnnotation().getConsequenceTypes().isEmpty()) {
                mustRunAnnotationPositions.add(mustSearchVariationPositions.get(i));
                mustRunAnnotation.add(mustSearchVariation.get(i));
            } else if (cacheVariant.getAnnotation() != null && cacheVariant.getAnnotation().getChromosome() == null) {
//            } else if (variationQueryResultList.get(i).getResult().get(0).getAnnotation() != null
//                        && variationQueryResultList.get(i).getResult().get(0).getAnnotation().getChromosome() == null) {
                mustSearchVariation.get(i).setId(cacheVariant.getId());
                if (mustSearchVariation.get(i).getAnnotation() == null) {
                    mustSearchVariation.get(i).setAnnotation(new VariantAnnotation());
                }
                mustSearchVariation.get(i).getAnnotation()
                        .setPopulationFrequencies(cacheVariant.getAnnotation().getPopulationFrequencies());
                mustRunAnnotationPositions.add(mustSearchVariationPositions.get(i));
                mustRunAnnotation.add(mustSearchVariation.get(i));
            } else {
                // variantList is the passed by reference argument and reference to objects within variantList are
                // copied within mustSearchVariation. Modifying reference objects within mustSearchVariation will
                // modify user-provided Variant objects. If there's no annotation - just set it; if there's an annotation
                // object already created, let's only overwrite those fields created by the annotator
                VariantAnnotation variantAnnotation;
                if (mustSearchVariation.get(i).getAnnotation() == null) {
                    variantAnnotation =  cacheVariant.getAnnotation();
                    mustSearchVariation.get(i).setAnnotation(variantAnnotation);
                } else {
                    variantAnnotation = mustSearchVariation.get(i).getAnnotation();
                    mergeAnnotation(variantAnnotation, cacheVariant.getAnnotation());
                }
                setGeneAnnotation(mustSearchVariation.get(i));
                variantAnnotationResultList.set(mustSearchVariationPositions.get(i),
                        new QueryResult<>(mustSearchVariation.get(i).toString(),
                        variationQueryResultList.get(i).getDbTime(), 1, 1, null, null,
                        Collections.singletonList(variantAnnotation)));
            }
        }

        if (mustRunAnnotation.size() > 0) {
            List<QueryResult<VariantAnnotation>> uncachedAnnotations = runAnnotationProcess(mustRunAnnotation);
            for (int i = 0; i < mustRunAnnotation.size(); i++) {
                variantAnnotationResultList.set(mustRunAnnotationPositions.get(i), uncachedAnnotations.get(i));
            }
        }

        logger.debug("{}/{} ({}%) variants required running the annotation process", mustRunAnnotation.size(),
                variantList.size(), (mustRunAnnotation.size() * (100.0 / variantList.size())));
        return variantAnnotationResultList;

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

    private List<Gene> setGeneAnnotation(Variant variant) {
        // Fetch overlapping genes for this variant
        List<Gene> geneList = getAffectedGenes(variant, includeGeneFields);
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
            stringBuilder.append(",annotation.id");
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
//        if (annotatorSet.contains("expression")) {
//            stringBuilder.append(",annotation.geneExpression");
//        }
//        if (annotatorSet.contains("geneDisease")) {
//            stringBuilder.append(",annotation.geneTraitAssociation");
//        }
//        if (annotatorSet.contains("drugInteraction")) {
//            stringBuilder.append(",annotation.geneDrugInteraction");
//        }
        if (annotatorSet.contains("populationFrequencies")) {
            stringBuilder.append(",annotation.populationFrequencies");
        }

        return stringBuilder.toString();
    }

    private List<QueryResult<VariantAnnotation>> runAnnotationProcess(List<Variant> normalizedVariantList)
            throws InterruptedException, ExecutionException {
        QueryOptions queryOptions;
        long globalStartTime = System.currentTimeMillis();
        long startTime;
        queryOptions = new QueryOptions();

        // Object to be returned
        List<QueryResult<VariantAnnotation>> variantAnnotationResultList = new ArrayList<>(normalizedVariantList.size());

        /*
         * Next three async blocks calculate annotations using Futures, this will be calculated in a different thread.
         * Once the main loop has finished then they will be stored. This provides a ~30% of performance improvement.
         */
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
        FutureVariationAnnotator futureVariationAnnotator = null;
        Future<List<QueryResult<Variant>>> variationFuture = null;

        // When running using cache: some variants may be in the variation collection (rs and popFrequencies needed)
        // but were not searched before because do contain the PS attribute - allow repetition of this query
        if (annotatorSet.contains("variation") || annotatorSet.contains("populationFrequencies")) {
//        if (!useCache && (annotatorSet.contains("variation") || annotatorSet.contains("populationFrequencies"))) {
            futureVariationAnnotator = new FutureVariationAnnotator(normalizedVariantList, new QueryOptions("include",
                    "id,annotation.populationFrequencies").append("imprecise", imprecise));
            variationFuture = fixedThreadPool.submit(futureVariationAnnotator);
        }

        FutureConservationAnnotator futureConservationAnnotator = null;
        Future<List<QueryResult>> conservationFuture = null;
        if (annotatorSet.contains("conservation")) {
            futureConservationAnnotator = new FutureConservationAnnotator(normalizedVariantList, queryOptions);
            conservationFuture = fixedThreadPool.submit(futureConservationAnnotator);
        }

        FutureVariantFunctionalScoreAnnotator futureVariantFunctionalScoreAnnotator = null;
        Future<List<QueryResult<Score>>> variantFunctionalScoreFuture = null;
        if (annotatorSet.contains("functionalScore")) {
            futureVariantFunctionalScoreAnnotator = new FutureVariantFunctionalScoreAnnotator(normalizedVariantList, queryOptions);
            variantFunctionalScoreFuture = fixedThreadPool.submit(futureVariantFunctionalScoreAnnotator);
        }

        FutureClinicalAnnotator futureClinicalAnnotator = null;
        Future<List<QueryResult>> clinicalFuture = null;
        if (annotatorSet.contains("clinical")) {
            futureClinicalAnnotator = new FutureClinicalAnnotator(normalizedVariantList, queryOptions);
            clinicalFuture = fixedThreadPool.submit(futureClinicalAnnotator);
        }

        FutureRepeatsAnnotator futureRepeatsAnnotator = null;
        Future<List<QueryResult<Repeat>>> repeatsFuture = null;
        if (annotatorSet.contains("repeats")) {
            futureRepeatsAnnotator = new FutureRepeatsAnnotator(normalizedVariantList, queryOptions);
            repeatsFuture = fixedThreadPool.submit(futureRepeatsAnnotator);
        }

        FutureCytobandAnnotator futureCytobandAnnotator = null;
        Future<List<QueryResult<Cytoband>>> cytobandFuture = null;
        if (annotatorSet.contains("cytoband")) {
            futureCytobandAnnotator = new FutureCytobandAnnotator(normalizedVariantList, queryOptions);
            cytobandFuture = fixedThreadPool.submit(futureCytobandAnnotator);
        }

        /*
         * We iterate over all variants to get the rest of the annotations and to create the VariantAnnotation objects
         */
        List<Gene> geneList;
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

            geneList = setGeneAnnotation(normalizedVariantList.get(i));

            if (annotatorSet.contains("consequenceType")) {
                try {
                    List<ConsequenceType> consequenceTypeList = getConsequenceTypeList(normalizedVariantList.get(i), geneList, true);
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
                    variantAnnotation.setConsequenceTypes(Collections.emptyList());
//                    throw e;
                }
            }

            QueryResult queryResult = new QueryResult(normalizedVariantList.get(i).toString());
            queryResult.setDbTime((int) (System.currentTimeMillis() - startTime));
            queryResult.setNumResults(1);
            queryResult.setNumTotalResults(1);
            //noinspection unchecked
            queryResult.setResult(Collections.singletonList(variantAnnotation));
            variantAnnotationResultList.add(queryResult);

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
            futureVariationAnnotator.processResults(variationFuture, variantAnnotationResultList, annotatorSet);
        }
        if (futureConservationAnnotator != null) {
            futureConservationAnnotator.processResults(conservationFuture, variantAnnotationResultList);
        }
        if (futureVariantFunctionalScoreAnnotator != null) {
            futureVariantFunctionalScoreAnnotator.processResults(variantFunctionalScoreFuture, variantAnnotationResultList);
        }
        if (futureClinicalAnnotator != null) {
            futureClinicalAnnotator.processResults(clinicalFuture, variantAnnotationResultList);
        }
        if (futureRepeatsAnnotator != null) {
            futureRepeatsAnnotator.processResults(repeatsFuture, variantAnnotationResultList);
        }
        if (futureCytobandAnnotator != null) {
            futureCytobandAnnotator.processResults(cytobandFuture, variantAnnotationResultList);
        }
        fixedThreadPool.shutdown();


        logger.debug("Total batch annotation performance is {}ms for {} variants", System.currentTimeMillis()
                - globalStartTime, normalizedVariantList.size());
        return variantAnnotationResultList;
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

        // Default behaviour use cache
        useCache = (queryOptions.get("useCache") != null ? (Boolean) queryOptions.get("useCache") : true);

        // Default behaviour - don't calculate phased annotation
        phased = (queryOptions.get("phased") != null ? (Boolean) queryOptions.get("phased") : false);
        logger.debug("phased = {}", phased);

        // Default behaviour - enable imprecise searches
        imprecise = (queryOptions.get("imprecise") != null ? (Boolean) queryOptions.get("imprecise") : true);
        logger.debug("imprecise = {}", imprecise);

        // Default behaviour - no extra padding for structural variants
        svExtraPadding = (queryOptions.get("sv-extra-padding") != null ? (Integer) queryOptions.get("sv-extra-padding") : 0);
        logger.debug("sv-extra-padding = {}", svExtraPadding);
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
//        destination.setGeneExpression(origin.getGeneExpression());
//        destination.setGeneTraitAssociation(origin.getGeneTraitAssociation());
        if (annotatorSet.contains("populationFrequencies")) {
            destination.setPopulationFrequencies(origin.getPopulationFrequencies());
        }
//        destination.setGeneDrugInteraction(origin.getGeneDrugInteraction());
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

//    private void checkAndAdjustPhasedConsequenceTypes(Queue<Variant> variantBuffer) {
//        Variant[] variantArray = (Variant[]) variantBuffer.toArray();
//        // SSACGATATCTT -> where S represents the position of the SNV
//        if (potentialCodingSNVOverlap(variantArray[0], variantArray[1])) {
//            // SSSACGATATCTT -> where S represents the position of the SNV. The three SNVs may affect the same codon
//            if (potentialCodingSNVOverlap(variantArray[1], variantArray[2])) {
//                adjustPhasedConsequenceTypes(variantArray);
//            // SSACGATATCVTT -> where S represents the position of the SNV and V represents the position of the third
//            // variant. Only the two first SNVs may affect the same codon.
//            } else {
//                adjustPhasedConsequenceTypes(Arrays.copyOfRange(variantArray, 0,3));
//            }
//        }
//    }

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
//                    String alternateAA = null;
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
//                            alternateAA = VariantAnnotationUtils.CODON_TO_A.get(alternateCodon);
                        soTerms = updatePhasedSoTerms(consequenceType1.getSequenceOntologyTerms(),
                                String.valueOf(referenceCodon), String.valueOf(alternateCodon),
                                variant1.getChromosome().equals("MT"));

                        // Update consequenceType3
                        consequenceType3.setCdnaPosition(cdnaPosition);
                        consequenceType3.setCdsPosition(cdsPosition);
                        consequenceType3.setCodon(codon);
                        //                        consequenceType3.getProteinVariantAnnotation().setAlternate(alternateAA);
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
//                            alternateAA = VariantAnnotationUtils.CODON_TO_A.get(String.valueOf(alternateCodonArray).toUpperCase());
                        soTerms = updatePhasedSoTerms(consequenceType1.getSequenceOntologyTerms(),
                                String.valueOf(referenceCodonArray).toUpperCase(),
                                String.valueOf(alternateCodonArray).toUpperCase(), variant1.getChromosome().equals("MT"));
                    }

                    // Update consequenceType1 & 2
                    consequenceType1.setCodon(codon);
                    //                    consequenceType1.getProteinVariantAnnotation().setAlternate(alternateAA);
                    consequenceType1.setProteinVariantAnnotation(newProteinVariantAnnotation == null
                            ? getProteinAnnotation(consequenceType1) : newProteinVariantAnnotation);
                    consequenceType1.setSequenceOntologyTerms(soTerms);
                    consequenceType2.setCdnaPosition(cdnaPosition);
                    consequenceType2.setCdsPosition(cdsPosition);
                    consequenceType2.setCodon(codon);
                    //                    consequenceType2.getProteinVariantAnnotation().setAlternate(alternateAA);
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
        if (variant1.getStudies() != null && !variant1.getStudies().isEmpty()) {
            if (variant2.getStudies() != null && !variant2.getStudies().isEmpty()) {
                int psIdx1 = variant1.getStudies().get(0).getFormat().indexOf("PS");
                if (psIdx1 != -1) {
                    int psIdx2 = variant2.getStudies().get(0).getFormat().indexOf("PS");
                    if (psIdx2 != -1 &&  // variant2 does have PS set
                            // same phase set value in both variants
                            variant2.getStudies().get(0).getSamplesData().get(0).get(psIdx2)
                                    .equals(variant1.getStudies().get(0).getSamplesData().get(0).get(psIdx1))
                            // Same genotype call in both variants (e.g. 1|0=1|0).
                            // WARNING: assuming variant1 and variant2 do have Files.
                            && variant1.getStudies().get(0).getFiles().get(0).getCall()
                            .equals(variant2.getStudies().get(0).getFiles().get(0).getCall())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getMostSevereConsequenceType(List<ConsequenceType> consequenceTypeList) {
        int max = -1;
        String mostSevereConsequencetype = null;
        for (ConsequenceType consequenceType : consequenceTypeList) {
            for (SequenceOntologyTerm sequenceOntologyTerm : consequenceType.getSequenceOntologyTerms()) {
                int rank = VariantAnnotationUtils.SO_SEVERITY.get(sequenceOntologyTerm.getName());
                if (rank > max) {
                    max = rank;
                    mostSevereConsequencetype = sequenceOntologyTerm.getName();
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
                    "repeats", "cytoband"));
            List<String> excludeList = queryOptions.getAsStringList("exclude");
            excludeList.forEach(annotatorSet::remove);
        }
        return annotatorSet;
    }

    private String getIncludedGeneFields(Set<String> annotatorSet) {
        String includeGeneFields = "name,id,start,end,transcripts.id,transcripts.start,transcripts.end,transcripts.strand,"
                + "transcripts.cdsLength,transcripts.annotationFlags,transcripts.biotype,transcripts.genomicCodingStart,"
                + "transcripts.genomicCodingEnd,transcripts.cdnaCodingStart,transcripts.cdnaCodingEnd,transcripts.exons.start,"
                + "transcripts.exons.end,transcripts.exons.sequence,transcripts.exons.phase,"
                + "transcripts.exons.exonNumber,mirna.matures,mirna.sequence,mirna.matures.cdnaStart,"
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

    private List<Gene> getAffectedGenes(Variant variant, String includeFields) {
        // reference = "" if insertion, reference = null if CNV for example
        int variantStart = variant.getReference() != null && variant.getReference().isEmpty()
                ? variant.getStart() - 1 : variant.getStart();
        QueryOptions queryOptions = new QueryOptions("include", includeFields);
//        QueryResult queryResult = geneDBAdaptor.getAllByRegion(new Region(variant.getChromosome(),
//                variantStart - 5000, variant.getStart() + variant.getReference().length() - 1 + 5000), queryOptions);

        return geneDBAdaptor
                .getByRegion(new Region(variant.getChromosome(), Math.max(1, variantStart - 5000),
                        variant.getEnd() + 5000), queryOptions).getResult();
//                        variant.getStart() + variant.getReference().length() - 1 + 5000), queryOptions).getResult();

//        return geneDBAdaptor.get(new Query("region", variant.getChromosome()+":"+(variantStart - 5000)+":"
//                +(variant.getStart() + variant.getReference().length() - 1 + 5000)), queryOptions)
//                .getResult();

//        QueryResult queryResult = geneDBAdaptor.getAllByRegion(new Region(variant.getChromosome(),
//                variantStart - 5000, variant.getStart() + variant.getReference().length() - 1 + 5000), queryOptions);
//
//        List<Gene> geneList = new ArrayList<>(queryResult.getNumResults());
//        for (Object object : queryResult.getResult()) {
//            Gene gene = geneObjectMapper.convertValue(object, Gene.class);
//            geneList.add(gene);
//        }
//        return geneList;
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
        switch (getVariantType(variant)) {
            case INSERTION:
                return new ConsequenceTypeInsertionCalculator(genomeDBAdaptor);
            case DELETION:
                return new ConsequenceTypeDeletionCalculator(genomeDBAdaptor);
            case SNV:
                return new ConsequenceTypeSNVCalculator();
            case CNV:
                return new ConsequenceTypeCNVCalculator();
            case MNV:
                return new ConsequenceTypeMNVCalculator(genomeDBAdaptor);
            default:
                throw new UnsupportedURLVariantFormat();
        }
    }

    private VariantType getVariantType(Variant variant) throws UnsupportedURLVariantFormat {
        if (variant.getType() == null) {
            variant.setType(Variant.inferType(variant.getReference(), variant.getAlternate()));
        }
        // FIXME: remove the if block below as soon as the Variant.inferType method is able to differentiate between
        // FIXME: insertions and deletions
        if (variant.getType().equals(VariantType.INDEL) || variant.getType().equals(VariantType.SV)) {
            if (variant.getReference().isEmpty()) {
//                variant.setType(VariantType.INSERTION);
                return VariantType.INSERTION;
            } else if (variant.getAlternate().isEmpty()) {
//                variant.setType(VariantType.DELETION);
                return VariantType.DELETION;
            } else {
                return VariantType.MNV;
            }
        }
        return variant.getType();
//        return getVariantType(variant.getReference(), variant.getAlternate());
    }

//    private VariantType getVariantType(String reference, String alternate) {
//        if (reference.isEmpty()) {
//            return VariantType.INSERTION;
//        } else if (alternate.isEmpty()) {
//            return VariantType.DELETION;
//        } else if (reference.length() == 1 && alternate.length() == 1) {
//            return VariantType.SNV;
//        } else {
//            throw new UnsupportedURLVariantFormat();
//        }
//    }

    private List<RegulatoryFeature> getAffectedRegulatoryRegions(Variant variant) {
        int variantStart = variant.getReference() != null && variant.getReference().isEmpty()
                ? variant.getStart() - 1 : variant.getStart();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("include", "chromosome,start,end");
//        QueryResult queryResult = regulationDBAdaptor.nativeGet(new Query("region", variant.getChromosome()
//                + ":" + variantStart + ":" + (variant.getStart() + variant.getReference().length() - 1)), queryOptions);
        QueryResult<RegulatoryFeature> queryResult = regulationDBAdaptor.getByRegion(new Region(variant.getChromosome(),
                variantStart, variant.getEnd()), queryOptions);
//                variantStart, variant.getStart() + variant.getReference().length() - 1), queryOptions);

        List<RegulatoryFeature> regionList = new ArrayList<>(queryResult.getNumResults());
        for (RegulatoryFeature object : queryResult.getResult()) {
            regionList.add(object);
        }

//        for (Object object : queryResult.getResult()) {
//            Document dbObject = (Document) object;
//            RegulatoryRegion regulatoryRegion = new RegulatoryRegion();
//            regulatoryRegion.setChromosome((String) dbObject.get("chromosome"));
//            regulatoryRegion.setStart((int) dbObject.get("start"));
//            regulatoryRegion.setEnd((int) dbObject.get("end"));
//            regulatoryRegion.setType((String) dbObject.get("featureType"));
//            regionList.add(regulatoryRegion);
//        }

        return regionList;
    }

    private List<ConsequenceType> getConsequenceTypeList(Variant variant, List<Gene> geneList, boolean regulatoryAnnotation) {
        List<RegulatoryFeature> regulatoryRegionList = null;
        if (regulatoryAnnotation) {
            regulatoryRegionList = getAffectedRegulatoryRegions(variant);
        }
        ConsequenceTypeCalculator consequenceTypeCalculator = getConsequenceTypeCalculator(variant);
        List<ConsequenceType> consequenceTypeList = consequenceTypeCalculator.run(variant, geneList, regulatoryRegionList);
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
        List<Region> regionList = new ArrayList<>(variantList.size());
        if (imprecise) {
            for (Variant variant : variantList) {
                if (VariantType.CNV.equals(variant.getType())) {
                    regionList.add(new Region(variant.getChromosome(),
                            variant.getStart() - CNV_DEFAULT_PADDING,
                            variant.getEnd() + CNV_DEFAULT_PADDING));
                } else if (variant.getSv() != null) {
                    regionList.add(new Region(variant.getChromosome(),
                            variant.getSv() != null && variant.getSv().getCiStartLeft() != null
                                    ? variant.getSv().getCiStartLeft() : variant.getStart(),
                            variant.getSv() != null && variant.getSv().getCiEndRight() != null
                                    ? variant.getSv().getCiEndRight() : variant.getEnd()));
                // Insertion
                } else if (variant.getStart() > variant.getEnd()) {
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getStart()));
                // Other but insertion
                } else {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getEnd()));
                }
            }
        } else {
            for (Variant variant : variantList) {
                // Insertion
                if (variant.getStart() > variant.getEnd()) {
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getStart()));
                // Other but insertion
                } else {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getEnd()));
                }
            }
        }
        return regionList;
    }

    private List<Region> breakpointsToRegionList(Variant variant) {
        List<Region> regionList = new ArrayList<>();

        switch (variant.getType()) {
            case SNV:
                regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getStart()));
                break;
            case CNV:
                if (imprecise) {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart() - CNV_DEFAULT_PADDING,
                            variant.getStart() + CNV_DEFAULT_PADDING));
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd() - CNV_DEFAULT_PADDING,
                            variant.getEnd() + CNV_DEFAULT_PADDING));
                } else {
                    regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getStart()));
                    regionList.add(new Region(variant.getChromosome(), variant.getEnd(), variant.getEnd()));
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
            List<QueryResult<Variant>> variationQueryResultList = variantDBAdaptor.getByVariant(variantList, queryOptions);
            logger.debug("Variation query performance is {}ms for {} variants", System.currentTimeMillis() - startTime, variantList.size());
            return variationQueryResultList;
        }

        public void processResults(Future<List<QueryResult<Variant>>> conservationFuture,
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResultList,
                                   Set<String> annotatorSet) throws InterruptedException, ExecutionException {
//            try {
            while (!conservationFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Variant>> variationQueryResults = conservationFuture.get();
            if (variationQueryResults != null) {
                for (int i = 0; i < variantAnnotationResultList.size(); i++) {
                    Variant preferredVariant = getPreferredVariant(variationQueryResults.get(i));
                    if (preferredVariant != null && preferredVariant.getIds().size() > 0) {
                        variantAnnotationResultList.get(i).first().setId(preferredVariant.getIds().get(0));

                    }

                    if (annotatorSet.contains("populationFrequencies") && preferredVariant != null) {
                        variantAnnotationResultList.get(i).first()
                                .setPopulationFrequencies(preferredVariant.getAnnotation().getPopulationFrequencies());
                    }
//                        List<Document> variationDBList = (List<Document>) variationQueryResults.get(i).getResult();
//                        if (variationDBList != null && variationDBList.size() > 0) {
//                            BasicDBList idsDBList = (BasicDBList) variationDBList.get(0).get("ids");
//                            if (idsDBList != null) {
//                                variantAnnotationResultList.get(i).getResult().get(0).setId((String) idsDBList.get(0));
//                            }
//                            if (annotatorSet.contains("populationFrequencies")) {
//                                Document annotationDBObject =  (Document) variationDBList.get(0).get("annotation");
//                                if (annotationDBObject != null) {
//                                    BasicDBList freqsDBList = (BasicDBList) annotationDBObject.get("populationFrequencies");
//                                    if (freqsDBList != null) {
//                                        Document freqDBObject;
//                                        variantAnnotationResultList.get(i).getResult().get(0).setPopulationFrequencies(new ArrayList<>());
//                                        for (int j = 0; j < freqsDBList.size(); j++) {
//                                            freqDBObject = ((Document) freqsDBList.get(j));
//                                            if (freqDBObject != null && freqDBObject.get("refAllele") != null) {
//                                                if (freqDBObject.containsKey("study")) {
//                                                    variantAnnotationResultList.get(i).getResult().get(0)
//                                                            .getPopulationFrequencies()
//                                                            .add(new PopulationFrequency(freqDBObject.get("study").toString(),
//                                                                    freqDBObject.get("population").toString(),
//                                                                    freqDBObject.get("refAllele").toString(),
//                                                                    freqDBObject.get("altAllele").toString(),
//                                                                    Float.valueOf(freqDBObject.get("refAlleleFreq").toString()),
//                                                                    Float.valueOf(freqDBObject.get("altAlleleFreq").toString()),
//                                                                    0.0f, 0.0f, 0.0f));
//                                                } else {
//                                                    variantAnnotationResultList.get(i).getResult().get(0)
//                                                            .getPopulationFrequencies().add(new PopulationFrequency("1000G_PHASE_3",
//                                                            freqDBObject.get("population").toString(),
//                                                            freqDBObject.get("refAllele").toString(),
//                                                            freqDBObject.get("altAllele").toString(),
//                                                            Float.valueOf(freqDBObject.get("refAlleleFreq").toString()),
//                                                            Float.valueOf(freqDBObject.get("altAlleleFreq").toString()),
//                                                            0.0f, 0.0f, 0.0f));
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
                }
            }
//            } catch (ExecutionException e) {
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
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
            List<QueryResult> conservationQueryResultList = conservationDBAdaptor
                    .getAllScoresByRegionList(variantListToRegionList(variantList), queryOptions);
            logger.debug("Conservation query performance is {}ms for {} variants", System.currentTimeMillis() - startTime,
                    variantList.size());
            return conservationQueryResultList;
        }

        public void processResults(Future<List<QueryResult>> conservationFuture,
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResultList)
                throws InterruptedException, ExecutionException {
//            try {
            while (!conservationFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult> conservationQueryResults = conservationFuture.get();
            if (conservationQueryResults != null) {
                for (int i = 0; i < variantAnnotationResultList.size(); i++) {
                    variantAnnotationResultList.get(i).getResult().get(0)
                            .setConservation((List<Score>) conservationQueryResults.get(i).getResult());
                }
            }
//            } catch (ExecutionException e) {
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
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
            List<QueryResult<Score>> variantFunctionalScoreQueryResultList =
                    variantDBAdaptor.getFunctionalScoreVariant(variantList, queryOptions);
            logger.debug("VariantFunctionalScore query performance is {}ms for {} variants",
                    System.currentTimeMillis() - startTime, variantList.size());
            return variantFunctionalScoreQueryResultList;
        }

        public void processResults(Future<List<QueryResult<Score>>> variantFunctionalScoreFuture,
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResultList)
                throws InterruptedException, ExecutionException {
//            try {
            while (!variantFunctionalScoreFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult<Score>> variantFunctionalScoreQueryResults = variantFunctionalScoreFuture.get();
            if (variantFunctionalScoreQueryResults != null) {
                for (int i = 0; i < variantAnnotationResultList.size(); i++) {
                    if (variantFunctionalScoreQueryResults.get(i).getNumResults() > 0) {
                        variantAnnotationResultList.get(i).getResult().get(0)
                                .setFunctionalScore((List<Score>) variantFunctionalScoreQueryResults.get(i).getResult());
                    }
                }
            }
//            } catch (ExecutionException e) {
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
            }
//        }

    }

    class FutureClinicalAnnotator implements Callable<List<QueryResult>> {
        private List<Variant> variantList;
        private QueryOptions queryOptions;

        FutureClinicalAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
            this.variantList = variantList;
            this.queryOptions = queryOptions;
        }

        @Override
        public List<QueryResult> call() throws Exception {
            long startTime = System.currentTimeMillis();
            List<QueryResult> clinicalQueryResultList = clinicalDBAdaptor.getAllByGenomicVariantList(variantList, queryOptions);
            logger.debug("Clinical query performance is {}ms for {} variants", System.currentTimeMillis() - startTime, variantList.size());
            return clinicalQueryResultList;
        }

        public void processResults(Future<List<QueryResult>> clinicalFuture,
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResults)
                throws InterruptedException, ExecutionException {
//            try {
            while (!clinicalFuture.isDone()) {
                Thread.sleep(1);
            }

            List<QueryResult> clinicalQueryResults = clinicalFuture.get();
            if (clinicalQueryResults != null) {
                for (int i = 0; i < variantAnnotationResults.size(); i++) {
                    QueryResult clinicalQueryResult = clinicalQueryResults.get(i);
                    if (clinicalQueryResult.getResult() != null && clinicalQueryResult.getResult().size() > 0) {
                        variantAnnotationResults.get(i).getResult().get(0)
                                .setVariantTraitAssociation((VariantTraitAssociation) clinicalQueryResult.getResult().get(0));
                    }
                }
            }
//            } catch (ExecutionException e) {
////            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
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
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResults)
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
                        variantAnnotationResults.get(i).getResult().get(0)
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

            // Want to return only one QueryResult object per Variant
            for (Variant variant : variantList) {
                List<QueryResult<Cytoband>> tmpQueryResultList = genomeDBAdaptor
                        .getCytoband(breakpointsToRegionList(variant));

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
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResults)
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
                    for (int i = 0; i < variantAnnotationResults.size(); i++) {
                        QueryResult queryResult = queryResultList.get(i);
                        if (queryResult.getResult() != null && queryResult.getResult().size() > 0) {
                            variantAnnotationResults.get(i).getResult().get(0).setCytoband(queryResult.getResult());
                        }
                    }
                }
            }
        }
    }

}

