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

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class FutureVariantFunctionalScoreAnnotator  implements Callable<List<CellBaseDataResult<Score>>> {
    private List<Variant> variantList;
    private QueryOptions queryOptions;
    private int dataRelease;
    private VariantManager variantManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public FutureVariantFunctionalScoreAnnotator(List<Variant> variantList, QueryOptions queryOptions, int dataRelease,
                                          VariantManager variantManager) {
        this.variantList = variantList;
        this.queryOptions = queryOptions;
        this.dataRelease = dataRelease;
        this.variantManager = variantManager;
    }

    @Override
    public List<CellBaseDataResult<Score>> call() throws Exception {
        long startTime = System.currentTimeMillis();
        logger.debug("Query variant functional score");
        List<CellBaseDataResult<Score>> variantFunctionalScoreCellBaseDataResultList =
                variantManager.getFunctionalScoreVariant(variantList, queryOptions, dataRelease);
        logger.debug("VariantFunctionalScore query performance is {}ms for {} variants",
                System.currentTimeMillis() - startTime, variantList.size());
        return variantFunctionalScoreCellBaseDataResultList;
    }

    public void processResults(Future<List<CellBaseDataResult<Score>>> variantFunctionalScoreFuture,
                               List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<CellBaseDataResult<Score>> variantFunctionalScoreCellBaseDataResults;
        try {
            variantFunctionalScoreCellBaseDataResults = variantFunctionalScoreFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            variantFunctionalScoreFuture.cancel(true);
            throw new ExecutionException("Unable to finish variant functional score query on time", e);
        }

        if (variantFunctionalScoreCellBaseDataResults != null) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                if (variantFunctionalScoreCellBaseDataResults.get(i).getNumResults() > 0) {
                    variantAnnotationList.get(i)
                            .setFunctionalScore(variantFunctionalScoreCellBaseDataResults.get(i).getResults());
                }
            }
        }
    }
}
