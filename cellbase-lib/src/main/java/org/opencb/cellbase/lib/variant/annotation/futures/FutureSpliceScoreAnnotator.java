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
import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.core.SpliceScoreAlternate;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.SpliceScores;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FutureSpliceScoreAnnotator implements Callable<List<CellBaseDataResult<SpliceScore>>> {
    private List<Variant> variantList;
    private QueryOptions queryOptions;
    private int dataRelease;
    private String apiKey;
    private VariantManager variantManager;

    private static Logger logger = LoggerFactory.getLogger(FutureSpliceScoreAnnotator.class);

    public FutureSpliceScoreAnnotator(List<Variant> variantList, QueryOptions queryOptions, int dataRelease, String apiKey,
                               VariantManager variantManager) {
        this.variantList = variantList;
        this.queryOptions = queryOptions;
        this.dataRelease = dataRelease;
        this.apiKey = apiKey;
        this.variantManager = variantManager;
    }

    @Override
    public List<CellBaseDataResult<SpliceScore>> call() throws Exception {
        long startTime = System.currentTimeMillis();

        List<CellBaseDataResult<SpliceScore>> cellBaseDataResultList = new ArrayList<>(variantList.size());

        logger.debug("Query splice");
        // Want to return only one CellBaseDataResult object per Variant
        for (Variant variant : variantList) {
            cellBaseDataResultList.add(variantManager.getSpliceScoreVariant(variant, apiKey, dataRelease));
        }
        logger.debug("Splice score query performance is {}ms for {} variants", System.currentTimeMillis() - startTime,
                variantList.size());
        return cellBaseDataResultList;
    }

    public void processResults(Future<List<CellBaseDataResult<SpliceScore>>> spliceFuture,
                               List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<CellBaseDataResult<SpliceScore>> spliceCellBaseDataResults;
        try {
            spliceCellBaseDataResults = spliceFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            spliceFuture.cancel(true);
            throw new ExecutionException("Unable to finish splice score query on time", e);
        }

        if (CollectionUtils.isNotEmpty(spliceCellBaseDataResults)) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                CellBaseDataResult<SpliceScore> spliceScoreResult = spliceCellBaseDataResults.get(i);
                if (spliceScoreResult != null && CollectionUtils.isNotEmpty(spliceScoreResult.getResults())) {
                    for (SpliceScore spliceScore : spliceScoreResult.getResults()) {
                        for (ConsequenceType ct : variantAnnotationList.get(i).getConsequenceTypes()) {
                            for (SpliceScoreAlternate spliceScoreAlt : spliceScore.getAlternates()) {
                                String alt = StringUtils.isEmpty(variantAnnotationList.get(i).getAlternate())
                                        ? "-"
                                        : variantAnnotationList.get(i).getAlternate();
                                if (alt.equals(spliceScoreAlt.getAltAllele())) {
                                    if (StringUtils.isEmpty(spliceScore.getTranscriptId())
                                            || StringUtils.isEmpty(ct.getTranscriptId())
                                            || spliceScore.getTranscriptId().equals(ct.getTranscriptId())) {
                                        SpliceScores scores = new SpliceScores(spliceScore.getSource(), spliceScoreAlt.getScores());
                                        if (ct.getSpliceScores() == null) {
                                            ct.setSpliceScores(new ArrayList<>());
                                        }
                                        ct.getSpliceScores().add(scores);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
