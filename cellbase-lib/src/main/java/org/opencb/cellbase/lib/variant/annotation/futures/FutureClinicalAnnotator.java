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
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.avro.GwasAssociation;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.ClinicalManager;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FutureClinicalAnnotator implements Callable<List<CellBaseDataResult<Variant>>> {
    private List<Variant> variantList;
    private List<Gene> batchGeneList;
    private QueryOptions queryOptions;
    private int dataRelease;
    private ClinicalManager clinicalManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public FutureClinicalAnnotator(List<Variant> variantList, List<Gene> batchGeneList, QueryOptions queryOptions,
                            int dataRelease, ClinicalManager clinicalManager) {
        this.variantList = variantList;
        this.batchGeneList = batchGeneList;
        this.queryOptions = queryOptions;
        this.dataRelease = dataRelease;
        this.clinicalManager = clinicalManager;
    }

    @Override
    public List<CellBaseDataResult<Variant>> call() throws Exception {
        long startTime = System.currentTimeMillis();
        List<CellBaseDataResult<Variant>> clinicalCellBaseDataResultList = clinicalManager.getByVariant(variantList, batchGeneList,
                queryOptions, dataRelease);
        logger.debug("Clinical query performance is {}ms for {} variants", System.currentTimeMillis() - startTime, variantList.size());
        return clinicalCellBaseDataResultList;
    }

    public void processResults(Future<List<CellBaseDataResult<Variant>>> clinicalFuture,
                               List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<CellBaseDataResult<Variant>> clinicalCellBaseDataResults;
        try {
            clinicalCellBaseDataResults = clinicalFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            clinicalFuture.cancel(true);
            throw new ExecutionException("Unable to finish clinical variant query on time", e);
        }

        if (clinicalCellBaseDataResults != null) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                CellBaseDataResult<Variant> clinicalCellBaseDataResult = clinicalCellBaseDataResults.get(i);
                if (clinicalCellBaseDataResult.getResults() != null && clinicalCellBaseDataResult.getResults().size() > 0) {
                    variantAnnotationList.get(i).setTraitAssociation(getAllTraitAssociations(clinicalCellBaseDataResult));
                    // Add GWAS info
                    List<GwasAssociation> gwas = clinicalCellBaseDataResult.getResults().get(0).getAnnotation().getGwas();
                    if (CollectionUtils.isNotEmpty(gwas)) {
                        variantAnnotationList.get(i).setGwas(gwas);
                    }
                }
            }
        }
    }

    private List<EvidenceEntry> getAllTraitAssociations(CellBaseDataResult<Variant> clinicalQueryResult) {
        List<EvidenceEntry> traitAssociations = new ArrayList<>();
        for (Variant variant: clinicalQueryResult.getResults()) {
            traitAssociations.addAll(variant.getAnnotation().getTraitAssociation());
        }
        return traitAssociations;
    }
}
