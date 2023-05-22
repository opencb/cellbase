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

package org.opencb.cellbase.lib.variant.annotation.futures;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.pharma.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Pharmacogenomics;
import org.opencb.biodata.models.variant.avro.PharmacogenomicsAlleles;
import org.opencb.biodata.models.variant.avro.PharmacogenomicsClinicalAnnotation;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.PharmacogenomicsManager;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FuturePharmacogenomicsAnnotator implements Callable<List<CellBaseDataResult<PharmaChemical>>> {
    private PharmacogenomicsManager pharmacogenomicsManager;

    private List<Variant> variantList;
    private QueryOptions queryOptions;
    private int dataRelease;

    private Logger logger;

    public FuturePharmacogenomicsAnnotator(List<Variant> variantList, QueryOptions queryOptions, int dataRelease,
                                           PharmacogenomicsManager pharmacogenomicsManager, Logger logger) {
        this.pharmacogenomicsManager = pharmacogenomicsManager;

        this.variantList = variantList;
        this.queryOptions = queryOptions;
        this.dataRelease = dataRelease;

        this.logger = logger;
    }

    @Override
    public List<CellBaseDataResult<PharmaChemical>> call() throws Exception {
        long startTime = System.currentTimeMillis();

        List<CellBaseDataResult<PharmaChemical>> cellBaseDataResultList = new ArrayList<>(variantList.size());

        logger.debug("Pharmacogenomics queries...");
        // Want to return only one CellBaseDataResult object per Variant
        List<String> includes = new ArrayList<>();
        includes.add("id");
        includes.add("name");
        includes.add("source");
        includes.add("types");
        includes.add("smiles");
        includes.add("inChI");
        includes.add("variants.variantId");
        includes.add("variants.geneName");
        includes.add("variants.chromosome");
        includes.add("variants.position");
        includes.add("variants.phenotypes");
        includes.add("variants.phenotypeType");
        includes.add("variants.confidence");
        includes.add("variants.score");
        includes.add("variants.url");
        includes.add("variants.evidences.pubmed");
        includes.add("variants.evidences.variantAssociations.description");
        includes.add("variants.evidences.variantAssociations.discussion");
        includes.add("variants.alleles");
        logger.info("Pharmacogenomics variant annotation/search includes: {}", StringUtils.join(includes, ","));
        for (Variant variant : variantList) {
            PharmaChemicalQuery query = new PharmaChemicalQuery();
            query.setLocations(Collections.singletonList(variant.getChromosome() + ":" + variant.getStart()));
            query.setDataRelease(dataRelease);
            query.setIncludes(includes);
            cellBaseDataResultList.add(pharmacogenomicsManager.search(query));
        }
        logger.info("Pharmacogenomics queries performance in {} ms for {} variants", System.currentTimeMillis() - startTime,
                variantList.size());
        return cellBaseDataResultList;
    }

    public void processResults(Future<List<CellBaseDataResult<PharmaChemical>>> pharmaFuture,
                               List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<CellBaseDataResult<PharmaChemical>> pharmaChemicalCellBaseDataResults;
        try {
            pharmaChemicalCellBaseDataResults = pharmaFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pharmaFuture.cancel(true);
            throw new ExecutionException("Unable to finish pharmacogenomics query on time", e);
        }

        if (CollectionUtils.isNotEmpty(pharmaChemicalCellBaseDataResults)) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                CellBaseDataResult<PharmaChemical> pharmaChemicalResult = pharmaChemicalCellBaseDataResults.get(i);
                if (pharmaChemicalResult != null && CollectionUtils.isNotEmpty(pharmaChemicalResult.getResults())) {
                    List<Pharmacogenomics> pharmacogenomicsList = new ArrayList<>();
                    for (PharmaChemical pharmaChemical : pharmaChemicalResult.getResults()) {
                        Pharmacogenomics pharmacogenomics = new Pharmacogenomics();
                        // Basic annotation fields
                        pharmacogenomics.setId(pharmaChemical.getId());
                        pharmacogenomics.setName(pharmaChemical.getName());
                        pharmacogenomics.setSource(pharmaChemical.getSource());
                        pharmacogenomics.setTypes(pharmaChemical.getTypes());
                        pharmacogenomics.setSmiles(pharmaChemical.getSmiles());
                        pharmacogenomics.setInChI(pharmaChemical.getInChI());

                        // Clinical annotation fields
                        if (CollectionUtils.isNotEmpty(pharmaChemical.getVariants())) {
                            String varAnnotChrom = variantAnnotationList.get(i).getChromosome();
                            int varAnnotStart = variantAnnotationList.get(i).getStart();

                            List<PharmacogenomicsClinicalAnnotation> resultClinicalAnnotations = new ArrayList<>();

                            // We must filter out those annotations based on different alternate alleles
                            // 1. Construct the HOM ALT genotype
                            final String queryAllele =
                                    variantAnnotationList.get(i).getAlternate() + variantAnnotationList.get(i).getAlternate();
                            for (PharmaVariantAnnotation pharmaVariantAnnotation : pharmaChemical.getVariants()) {
                                // 2. Check the variant is the same
                                if (!varAnnotChrom.equals(pharmaVariantAnnotation.getChromosome())
                                        || varAnnotStart != pharmaVariantAnnotation.getPosition()) {
                                    continue;
                                }

                                // 3. Check if the 'alleles' contains the alternate homozygous genotype, or 'null' or '*',
                                // otherwise go to next annotation
                                if (CollectionUtils.isNotEmpty(pharmaVariantAnnotation.getAlleles())) {
                                    boolean found = false;
                                    for (PharmaClinicalAllele allele : pharmaVariantAnnotation.getAlleles()) {
                                        if (allele.getAllele().equalsIgnoreCase(queryAllele)
                                                || allele.getAllele().contains("null")
                                                || allele.getAllele().contains("*")) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        continue;
                                    }
                                }

                                // 4. Create, build and add the annotation
                                PharmacogenomicsClinicalAnnotation resultClinicalAnnotation = new PharmacogenomicsClinicalAnnotation();
                                resultClinicalAnnotation.setVariantId(pharmaVariantAnnotation.getVariantId());
                                resultClinicalAnnotation.setGeneName(pharmaVariantAnnotation.getGeneName());
                                resultClinicalAnnotation.setPhenotypes(pharmaVariantAnnotation.getPhenotypes());
                                resultClinicalAnnotation.setPhenotypeType(pharmaVariantAnnotation.getPhenotypeType());
                                resultClinicalAnnotation.setConfidence(pharmaVariantAnnotation.getConfidence());
                                resultClinicalAnnotation.setScore(pharmaVariantAnnotation.getScore());
                                resultClinicalAnnotation.setUrl(pharmaVariantAnnotation.getUrl());

                                if (CollectionUtils.isNotEmpty(pharmaVariantAnnotation.getEvidences())) {
                                    Set<String> pubmeds = new LinkedHashSet<>();
                                    Set<String> summaries = new LinkedHashSet<>();
                                    for (PharmaClinicalEvidence evidence : pharmaVariantAnnotation.getEvidences()) {
                                        if (StringUtils.isNotEmpty(evidence.getPubmed())) {
                                            pubmeds.add(evidence.getPubmed());
                                        }
                                        if (CollectionUtils.isNotEmpty(evidence.getVariantAssociations())) {
                                            for (PharmaVariantAssociation variantAssociation : evidence.getVariantAssociations()) {
                                                summaries.add(variantAssociation.getDescription());
                                                summaries.add(variantAssociation.getDiscussion());
                                            }
                                        }
                                    }
                                    resultClinicalAnnotation.setPubmed(new ArrayList<>(pubmeds));
                                    resultClinicalAnnotation.setSummary(String.join(" ", summaries));
                                }

                                if (CollectionUtils.isNotEmpty(pharmaVariantAnnotation.getAlleles())) {
                                    resultClinicalAnnotation.setAlleles(pharmaVariantAnnotation.getAlleles().stream()
                                            .map(a -> new PharmacogenomicsAlleles(a.getAllele(), a.getAnnotation(), a.getDescription()))
                                            .collect(Collectors.toList())
                                    );
                                }
                                // Add pharmacogenomics clinical annotation to the list
                                resultClinicalAnnotations.add(resultClinicalAnnotation);
                            }
                            // Set pharmacogenomics clinical annotation
                            pharmacogenomics.setAnnotations(resultClinicalAnnotations);
                        }
                        // Add pharmacogenomics to the list if at least one annotation for the same variant has been found
                        if (CollectionUtils.isNotEmpty(pharmacogenomics.getAnnotations())) {
                            pharmacogenomicsList.add(pharmacogenomics);
                        }
                    }
                    // Set the pharmacogenomics data in the variant annotation
                    variantAnnotationList.get(i).setPharmacogenomics(pharmacogenomicsList);
                }
            }
        }
    }
}
