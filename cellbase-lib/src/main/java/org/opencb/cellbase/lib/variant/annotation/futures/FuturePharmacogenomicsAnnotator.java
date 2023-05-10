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
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.biodata.models.pharma.PharmaClinicalAnnotation;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        for (Variant variant : variantList) {
            PharmaChemicalQuery query = new PharmaChemicalQuery();
            query.setLocations(Collections.singletonList(variant.getChromosome() + ":" + variant.getStart()));
            query.setDataRelease(dataRelease);
            cellBaseDataResultList.add(pharmacogenomicsManager.search(query));
        }
        logger.debug("Pharmacogenomics queries performance in {} ms for {} variants", System.currentTimeMillis() - startTime,
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
                            List<PharmacogenomicsClinicalAnnotation> resultClinicalAnnotations = new ArrayList<>();
                            for (PharmaClinicalAnnotation clinicalAnnotation : pharmaChemical.getVariants()) {
                                PharmacogenomicsClinicalAnnotation resultClinicalAnnotation = new PharmacogenomicsClinicalAnnotation();
                                resultClinicalAnnotation.setVariantId(clinicalAnnotation.getVariantId());
                                resultClinicalAnnotation.setGeneName(clinicalAnnotation.getGene());
                                resultClinicalAnnotation.setPhenotypes(clinicalAnnotation.getPhenotypes());
                                resultClinicalAnnotation.setLevelOfEvidence(clinicalAnnotation.getLevelOfEvidence());
                                resultClinicalAnnotation.setConfidence(clinicalAnnotation.getScore());
                                resultClinicalAnnotation.setUrl(clinicalAnnotation.getUrl());
                                if (CollectionUtils.isNotEmpty(clinicalAnnotation.getEvidences())) {
                                    resultClinicalAnnotation.setPubmed(new ArrayList<>(clinicalAnnotation.getEvidences().stream()
                                            .map(e -> e.getPubmed()).collect(Collectors.toSet())));
                                }
                                if (CollectionUtils.isNotEmpty(clinicalAnnotation.getAlleles())) {
                                    resultClinicalAnnotation.setAlleles(clinicalAnnotation.getAlleles().stream().map(
                                                    a -> new PharmacogenomicsAlleles(a.getAllele(), a.getAnnotation(), a.getDescription()))
                                            .collect(Collectors.toList())
                                    );
                                }
                                // Add pharmacogenomics clinical annotation to the list
                                resultClinicalAnnotations.add(resultClinicalAnnotation);
                            }
                            // Set pharmacogenomics clinical annotation
                            pharmacogenomics.setAnnotations(resultClinicalAnnotations);
                        }
                        // Add pharmacogenomics to the list
                        pharmacogenomicsList.add(pharmacogenomics);
                    }
                    // Set the pharmacogenomics data in the variant annotation
                    variantAnnotationList.get(i).setPharmacogenomics(pharmacogenomicsList);
                }
            }
        }
    }
}
