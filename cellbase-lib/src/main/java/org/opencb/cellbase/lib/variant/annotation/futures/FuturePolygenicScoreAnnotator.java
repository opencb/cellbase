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
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
import org.opencb.cellbase.core.api.PolygenicScoreQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.PharmacogenomicsManager;
import org.opencb.cellbase.lib.managers.PolygenicScoreManager;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FuturePolygenicScoreAnnotator implements Callable<List<CellBaseDataResult<PolygenicScoreAnnotation>>> {
    private PolygenicScoreManager polygenicScoreManager;

    private List<Variant> variantList;
    private QueryOptions queryOptions;
    private int dataRelease;

    private Logger logger;

    public FuturePolygenicScoreAnnotator(List<Variant> variantList, QueryOptions queryOptions, int dataRelease,
                                         PolygenicScoreManager polygenicScoreManager, Logger logger) {
        this.polygenicScoreManager = polygenicScoreManager;

        this.variantList = variantList;
        this.queryOptions = queryOptions;
        this.dataRelease = dataRelease;

        this.logger = logger;
    }

    @Override
    public List<CellBaseDataResult<PolygenicScoreAnnotation>> call() throws Exception {
        long startTime = System.currentTimeMillis();

        List<CellBaseDataResult<PolygenicScoreAnnotation>> cellBaseDataResultList = new ArrayList<>(variantList.size());

        logger.debug("PolygenicScore queries...");
        // Want to return only one CellBaseDataResult object per Variant
        for (Variant variant : variantList) {
            cellBaseDataResultList.add(polygenicScoreManager.getPolygenicScoreAnnotation(variant.getChromosome(), variant.getStart(),
                    variant.getReference(), variant.getAlternate(), dataRelease));
        }
        logger.info("Pharmacogenomics queries performance in {} ms for {} variants", System.currentTimeMillis() - startTime,
                variantList.size());
        return cellBaseDataResultList;
    }

    public void processResults(Future<List<CellBaseDataResult<PolygenicScoreAnnotation>>> pgsFuture,
                               List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<CellBaseDataResult<PolygenicScoreAnnotation>> pgsCellBaseDataResults;
        try {
            pgsCellBaseDataResults = pgsFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pgsFuture.cancel(true);
            throw new ExecutionException("Unable to finish polygenic scores query on time", e);
        }

        if (CollectionUtils.isNotEmpty(pgsCellBaseDataResults)) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                CellBaseDataResult<PolygenicScoreAnnotation> pgsResult = pgsCellBaseDataResults.get(i);
                if (pgsResult != null && CollectionUtils.isNotEmpty(pgsResult.getResults())) {
                    // Set the polygenic scores in the variant annotation
                    variantAnnotationList.get(i).setPolygenicScores(pgsResult.getResults());
                }
            }
        }
    }
}
