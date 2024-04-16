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
import org.opencb.biodata.models.core.Snp;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.Xref;
import org.opencb.cellbase.core.api.SnpQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.VariantManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FutureSnpAnnotator implements Callable<List<CellBaseDataResult<Snp>>> {
    private VariantManager variantManager;

    private List<Variant> variantList;
    private int dataRelease;

    private Logger logger;

    public FutureSnpAnnotator(List<Variant> variantList, int dataRelease, VariantManager variantManager, Logger logger) {
        this.variantManager = variantManager;

        this.variantList = variantList;
        this.dataRelease = dataRelease;

        this.logger = logger;
    }

    @Override
    public List<CellBaseDataResult<Snp>> call() throws Exception {
        long startTime = System.currentTimeMillis();

        List<CellBaseDataResult<Snp>> cellBaseDataResultList = new ArrayList<>(variantList.size());

        logger.debug("SNP queries...");
        // Want to return only one CellBaseDataResult object per Variant
        List<String> includes = new ArrayList<>();
        includes.add("id");
        includes.add("source");
        String logMsg = StringUtils.join(includes, ",");
        logger.info("SNP annotation/search includes: {}", logMsg);
        for (Variant variant : variantList) {
            SnpQuery query = new SnpQuery();
            query.setChromosome(variant.getChromosome());
            query.setPosition(variant.getStart());
            query.setReference(variant.getReference());
            query.setDataRelease(dataRelease);
            query.setIncludes(includes);
            cellBaseDataResultList.add(variantManager.searchSnp(query));
        }
        logger.info("SNP queries performance in {} ms for {} variants", System.currentTimeMillis() - startTime, variantList.size());
        return cellBaseDataResultList;
    }

    public void processResults(Future<List<CellBaseDataResult<Snp>>> snpFuture, List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<CellBaseDataResult<Snp>> snpCellBaseDataResults;
        try {
            snpCellBaseDataResults = snpFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            snpFuture.cancel(true);
            throw new ExecutionException("Unable to finish SNP query on time", e);
        }

        if (CollectionUtils.isNotEmpty(snpCellBaseDataResults)) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                CellBaseDataResult<Snp> snpResult = snpCellBaseDataResults.get(i);
                if (snpResult != null && CollectionUtils.isNotEmpty(snpResult.getResults())) {
                    if (CollectionUtils.isEmpty(variantAnnotationList.get(i).getXrefs())) {
                        // Add all dbSNP to the xrefs
                        variantAnnotationList.get(i).setXrefs(new ArrayList<>());
                        for (Snp snp : snpResult.getResults()) {
                            variantAnnotationList.get(i).getXrefs().add(new Xref(snp.getId(), snp.getSource()));
                        }
                    } else {
                        // Check if the xrefs are already in the annotation (e.g., GWAS builder might add dbSNP IDs)
                        List<Xref> newXrefs = new ArrayList<>();
                        for (Snp snp : snpResult.getResults()) {
                            // Sanity check
                            if (StringUtils.isNotEmpty(snp.getId()) && StringUtils.isNotEmpty(snp.getSource())) {
                                boolean found = false;
                                for (Xref xref : variantAnnotationList.get(i).getXrefs()) {
                                    if (snp.getId().equalsIgnoreCase(xref.getId()) && snp.getSource().equalsIgnoreCase(xref.getSource())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    newXrefs.add(new Xref(snp.getId(), snp.getSource()));
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(newXrefs)) {
                            variantAnnotationList.get(i).getXrefs().addAll(newXrefs);
                        }
                    }
                }
            }
        }
    }
}
