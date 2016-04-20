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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.annotation.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.*;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class VariantAnnotationCalculator { //extends MongoDBAdaptor implements VariantAnnotationDBAdaptor<VariantAnnotation> {

    private GenomeDBAdaptor genomeDBAdaptor;
    private GeneDBAdaptor geneDBAdaptor;
    private RegulationDBAdaptor regulationDBAdaptor;
    private VariantDBAdaptor variantDBAdaptor;
    private ClinicalDBAdaptor clinicalDBAdaptor;
    private ProteinDBAdaptor proteinDBAdaptor;
    private ConservationDBAdaptor conservationDBAdaptor;

    private DBAdaptorFactory dbAdaptorFactory;
    //    private ObjectMapper geneObjectMapper;
    private final VariantNormalizer normalizer;
    private boolean normalize = true;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

//    public VariantAnnotationCalculator(String species, String assembly, MongoDataStore mongoDataStore) {
////        super(species, assembly, mongoDataStore);
//
//        normalizer = new VariantNormalizer(false);
//        logger.debug("VariantAnnotationMongoDBAdaptor: in 'constructor'");
//    }

    public VariantAnnotationCalculator(String species, String assembly, DBAdaptorFactory dbAdaptorFactory) {
        this(species, assembly, dbAdaptorFactory, true);
    }

    public VariantAnnotationCalculator(String species, String assembly, DBAdaptorFactory dbAdaptorFactory,
                                       boolean normalize) {
        this.normalizer = new VariantNormalizer(false, false, true);
        this.normalize = normalize;

        this.dbAdaptorFactory = dbAdaptorFactory;

        this.genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(species, assembly);
        this.variantDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(species, assembly);
        this.geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
        this.regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(species, assembly);
        this.proteinDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(species, assembly);
        this.conservationDBAdaptor = dbAdaptorFactory.getConservationDBAdaptor(species, assembly);
        this.clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(species, assembly);

        logger.debug("VariantAnnotationMongoDBAdaptor: in 'constructor'");
    }

    @Deprecated
    public QueryResult getAllConsequenceTypesByVariant(Variant variant, QueryOptions queryOptions) {
        long dbTimeStart = System.currentTimeMillis();

        // We process include and exclude query options to know which annotators to use.
        // Include parameter has preference over exclude.
        Set<String> annotatorSet = getAnnotatorSet(queryOptions);

        // This field contains all the fields to be returned by overlapping genes
        String includeGeneFields = getIncludedGeneFields(annotatorSet);
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

    public QueryResult getAnnotationByVariant(Variant variant, QueryOptions queryOptions) {
        return getAnnotationByVariantList(Collections.singletonList(variant), queryOptions).get(0);
    }

    public List<QueryResult<VariantAnnotation>> getAnnotationByVariantList(List<Variant> variantList, QueryOptions queryOptions) {

        logger.debug("Annotating  batch");

        if (variantList == null || variantList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Variant> normalizedVariantList;
        if (normalize) {
            normalizedVariantList = normalizer.apply(variantList);
        } else {
            normalizedVariantList = variantList;
        }

        // We process include and exclude query options to know which annotators to use.
        // Include parameter has preference over exclude.
        Set<String> annotatorSet = getAnnotatorSet(queryOptions);
        logger.debug("Annotators to use: {}", annotatorSet.toString());

        // This field contains all the fields to be returned by overlapping genes
        String includeGeneFields = getIncludedGeneFields(annotatorSet);

        // Object to be returned
        List<QueryResult<VariantAnnotation>> variantAnnotationResultList = new ArrayList<>(normalizedVariantList.size());

        long globalStartTime = System.currentTimeMillis();
        long startTime;
        queryOptions = new QueryOptions();

        /*
         * Next three async blocks calculate annotations using Futures, this will be calculated in a different thread.
         * Once the main loop has finished then they will be stored. This provides a ~30% of performance improvement.
         */
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
        FutureVariationAnnotator futureVariationAnnotator = null;
        Future<List<QueryResult<Variant>>> variationFuture = null;
        if (annotatorSet.contains("variation") || annotatorSet.contains("populationFrequencies")) {
            futureVariationAnnotator = new FutureVariationAnnotator(normalizedVariantList, queryOptions);
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

        /*
         * We iterate over all variants to get the rest of the annotations and to create the VariantAnnotation objects
         */
        List<Gene> geneList;
        Queue<Variant> variantBuffer = new LinkedList<>();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < normalizedVariantList.size(); i++) {
            // Fetch overlapping genes for this variant
            geneList = getAffectedGenes(normalizedVariantList.get(i), includeGeneFields);

            // TODO: start & end are both being set to variantList.get(i).getPosition(), modify this for indels
            VariantAnnotation variantAnnotation = new VariantAnnotation();
            variantAnnotation.setChromosome(normalizedVariantList.get(i).getChromosome());
            variantAnnotation.setStart(normalizedVariantList.get(i).getStart());
            variantAnnotation.setReference(normalizedVariantList.get(i).getReference());
            variantAnnotation.setAlternate(normalizedVariantList.get(i).getAlternate());

            if (annotatorSet.contains("consequenceType")) {
                try {
                    List<ConsequenceType> consequenceTypeList = getConsequenceTypeList(normalizedVariantList.get(i), geneList, true);
                    variantAnnotation.setConsequenceTypes(consequenceTypeList);
                    normalizedVariantList.get(i).setAnnotation(variantAnnotation);
                    checkAndAdjustPhasedConsequenceTypes(normalizedVariantList.get(i), variantBuffer);
                    variantAnnotation
                            .setDisplayConsequenceType(getMostSevereConsequenceType(normalizedVariantList.get(i)
                                    .getAnnotation().getConsequenceTypes()));
                } catch (UnsupportedURLVariantFormat e) {
                    logger.error("Consequence type was not calculated for variant {}. Unrecognised variant format.",
                            normalizedVariantList.get(i).toString());
                } catch (Exception e) {
                    logger.error("Unhandled error when calculating consequence type for variant {}",
                            normalizedVariantList.get(i).toString());
                    throw e;
                }
            }

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
        if (variantBuffer.size() > 1) {
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
        fixedThreadPool.shutdown();


        logger.debug("Total batch annotation performance is {}ms for {} variants", System.currentTimeMillis()
                - globalStartTime, normalizedVariantList.size());
        return variantAnnotationResultList;
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
        for (ConsequenceType consequenceType1 : variant0.getAnnotation().getConsequenceTypes()) {
            ProteinVariantAnnotation newProteinVariantAnnotation = null;
            // Check if this is a coding consequence type. Also this consequence type may have been already
            // updated if there are 3 consecutive phased SNVs affecting the same codon.
            if (isCoding(consequenceType1)
                    && !transcriptAnnotationUpdated(variant0, consequenceType1.getEnsemblTranscriptId())) {
                Variant variant1 = (Variant) variantArray[1];
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
                    Variant variant2 = null;
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
                }
            }
        }
    }

    private void flagTranscriptAnnotationUpdated(Variant variant, String ensemblTranscriptId) {
        Map<String, Object> additionalAttributesMap = variant.getAnnotation().getAdditionalAttributes();
        if (additionalAttributesMap == null) {
            additionalAttributesMap = new HashMap<>();
            Map<String, String> transcriptsSet = new HashMap<>();
            transcriptsSet.put(ensemblTranscriptId, null);
            additionalAttributesMap.put("phasedTranscripts", transcriptsSet);
            variant.getAnnotation().setAdditionalAttributes(additionalAttributesMap);
        } else if (additionalAttributesMap.get("phasedTranscripts") == null) {
            Map<String, String> transcriptsSet = new HashMap<>();
            transcriptsSet.put(ensemblTranscriptId, null);
            additionalAttributesMap.put("phasedTranscripts", transcriptsSet);
        } else {
            ((Map) additionalAttributesMap.get("phasedTranscripts")).put(ensemblTranscriptId, null);
        }
    }

    private boolean transcriptAnnotationUpdated(Variant variant, String ensemblTranscriptId) {
        if (variant.getAnnotation().getAdditionalAttributes() != null
                && variant.getAnnotation().getAdditionalAttributes().get("phasedTranscripts") != null
                && ((Map<String, String>) variant.getAnnotation().getAdditionalAttributes().get("phasedTranscripts"))
                .containsKey(ensemblTranscriptId)) {
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
                    "consequenceType", "expression", "geneDisease", "drugInteraction", "populationFrequencies"));
            List<String> excludeList = queryOptions.getAsStringList("exclude");
            excludeList.forEach(annotatorSet::remove);
        }
        return annotatorSet;
    }

    private String getIncludedGeneFields(Set<String> annotatorSet) {
        String includeGeneFields = "name,id,start,end,transcripts.id,transcripts.start,transcripts.end,transcripts.strand,"
                + "transcripts.cdsLength,transcripts.annotationFlags,transcripts.biotype,transcripts.genomicCodingStart,"
                + "transcripts.genomicCodingEnd,transcripts.cdnaCodingStart,transcripts.cdnaCodingEnd,transcripts.exons.start,"
                + "transcripts.exons.end,transcripts.exons.sequence,transcripts.exons.phase,mirna.matures,mirna.sequence,"
                + "mirna.matures.cdnaStart,mirna.matures.cdnaEnd";

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
        int variantStart = variant.getReference().isEmpty() ? variant.getStart() - 1 : variant.getStart();
        QueryOptions queryOptions = new QueryOptions("include", includeFields);
//        QueryResult queryResult = geneDBAdaptor.getAllByRegion(new Region(variant.getChromosome(),
//                variantStart - 5000, variant.getStart() + variant.getReference().length() - 1 + 5000), queryOptions);

        return geneDBAdaptor
                .getByRegion(new Region(variant.getChromosome(), Math.max(1, variantStart - 5000),
                        variant.getStart() + variant.getReference().length() - 1 + 5000), queryOptions).getResult();

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
            default:
                throw new UnsupportedURLVariantFormat();
        }
    }

    private VariantType getVariantType(Variant variant) throws UnsupportedURLVariantFormat {
        return getVariantType(variant.getReference(), variant.getAlternate());
    }

    private VariantType getVariantType(String reference, String alternate) {
        if (reference.isEmpty()) {
            return VariantType.INSERTION;
        } else if (alternate.isEmpty()) {
            return VariantType.DELETION;
        } else if (reference.length() == 1 && alternate.length() == 1) {
            return VariantType.SNV;
        } else {
            throw new UnsupportedURLVariantFormat();
        }
    }

    private List<RegulatoryFeature> getAffectedRegulatoryRegions(Variant variant) {
        int variantStart = variant.getReference().isEmpty() ? variant.getStart() - 1 : variant.getStart();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add("include", "chromosome,start,end");
//        QueryResult queryResult = regulationDBAdaptor.nativeGet(new Query("region", variant.getChromosome()
//                + ":" + variantStart + ":" + (variant.getStart() + variant.getReference().length() - 1)), queryOptions);
        QueryResult<RegulatoryFeature> queryResult = regulationDBAdaptor.getByRegion(new Region(variant.getChromosome(),
                variantStart, variant.getStart() + variant.getReference().length() - 1), queryOptions);

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
        for (ConsequenceType consequenceType : consequenceTypeList) {
            if (nonSynonymous(consequenceType, variant.getChromosome().equals("MT"))) {
                consequenceType.setProteinVariantAnnotation(getProteinAnnotation(consequenceType));
            }
        }
        return consequenceTypeList;
    }

    private List<Region> variantListToRegionList(List<Variant> variantList) {
        List<Region> regionList = new ArrayList<>(variantList.size());
        for (Variant variant : variantList) {
            regionList.add(new Region(variant.getChromosome(), variant.getStart(), variant.getStart()));
        }
        return regionList;
    }

    /*
     * Future classes for Async annotations
     */
    class FutureVariationAnnotator implements Callable<List<QueryResult<Variant>>> {
        private List<Variant> variantList;
        private QueryOptions queryOptions;

        public FutureVariationAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
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
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResultList, Set<String> annotatorSet) {
            try {
                while (!conservationFuture.isDone()) {
                    Thread.sleep(1);
                }

                List<QueryResult<Variant>> variationQueryResults = conservationFuture.get();
                if (variationQueryResults != null) {
                    for (int i = 0; i < variantAnnotationResultList.size(); i++) {
                        if (variationQueryResults.get(i).first() != null && variationQueryResults.get(i).first().getIds().size() > 0) {
                            variantAnnotationResultList.get(i).first().setId(variationQueryResults.get(i).first().getIds().get(0));

                        }

                        if (annotatorSet.contains("populationFrequencies") && variationQueryResults.get(i).first() != null) {
                            variantAnnotationResultList.get(i).first().setPopulationFrequencies(variationQueryResults.get(i)
                                    .first().getAnnotation().getPopulationFrequencies());
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    class FutureConservationAnnotator implements Callable<List<QueryResult>> {
        private List<Variant> variantList;

        private QueryOptions queryOptions;

        public FutureConservationAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
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
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResultList) {
            try {
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    class FutureVariantFunctionalScoreAnnotator implements Callable<List<QueryResult<Score>>> {
        private List<Variant> variantList;

        private QueryOptions queryOptions;

        public FutureVariantFunctionalScoreAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
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
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResultList) {
            try {
                while (!variantFunctionalScoreFuture.isDone()) {
                    Thread.sleep(1);
                }

                List<QueryResult<Score>> variantFunctionalScoreQueryResults = variantFunctionalScoreFuture.get();
                if (variantFunctionalScoreQueryResults != null) {
                    for (int i = 0; i < variantAnnotationResultList.size(); i++) {
                        variantAnnotationResultList.get(i).getResult().get(0)
                                .setFunctionalScore((List<Score>) variantFunctionalScoreQueryResults.get(i).getResult());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    class FutureClinicalAnnotator implements Callable<List<QueryResult>> {
        private List<Variant> variantList;
        private QueryOptions queryOptions;

        public FutureClinicalAnnotator(List<Variant> variantList, QueryOptions queryOptions) {
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
                                   List<QueryResult<VariantAnnotation>> variantAnnotationResults) {
            try {
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
