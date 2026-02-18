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
import org.apache.commons.lang3.time.StopWatch;
import org.opencb.biodata.models.core.GenomeSequenceFeature;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.GenomicSequenceContext;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.lib.managers.GenomeManager;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FutureGenomicSequenceContextAnnotator implements Callable<List<GenomicSequenceContext>> {

    // The context length is: [CONTEX_SIZE <- variant.start -> CONTEXT_SIZE]
    private final int CONTEXT_SIZE = 10;

    private GenomeManager genomeManager;

    private List<Variant> variantList;
    private int dataRelease;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public FutureGenomicSequenceContextAnnotator(List<Variant> variantList, int dataRelease, GenomeManager genomeManager) {
        this.genomeManager = genomeManager;

        this.variantList = variantList;
        this.dataRelease = dataRelease;
    }

    @Override
    public List<GenomicSequenceContext> call() throws Exception {
        StopWatch stopWatch = StopWatch.createStarted();

        GenomicSequenceContext genomicSequenceContext;
        List<GenomicSequenceContext> resultList = new ArrayList<>(variantList.size());

        logger.debug("Genomic sequence context queries ...");
        // Want to return only one CellBaseDataResult object per Variant
        for (Variant variant : variantList) {
            genomicSequenceContext = null;
            if (VariantType.SNV != variant.getType() && VariantType.SNP != variant.getType()) {
                int start = variant.getStart() - CONTEXT_SIZE;
                if (start < 1) {
                    start = 1;
                }
                int end = variant.getStart() + CONTEXT_SIZE;
                Region region = new Region(variant.getChromosome(), start, end);
                logger.debug("Region {} for the genomic sequence context query", region);
                GenomeSequenceFeature genomeSequenceFeature = genomeManager.getSequence(region, QueryOptions.empty(), dataRelease).first();
                if (genomeSequenceFeature != null) {
                    genomicSequenceContext = new GenomicSequenceContext(genomeSequenceFeature.getStart(), genomeSequenceFeature.getEnd(),
                            genomeSequenceFeature.getSequence());
                }
                logger.debug("Genomic sequence context = {}", genomicSequenceContext);
            }
            resultList.add(genomicSequenceContext);
        }
        logger.debug("Genomic sequence context queries performance in {} ms for {} variants", stopWatch.getTime(TimeUnit.MILLISECONDS),
                variantList.size());
        return resultList;
    }

    public void processResults(Future<List<GenomicSequenceContext>> contextFuture, List<VariantAnnotation> variantAnnotationList)
            throws InterruptedException, ExecutionException {
        List<GenomicSequenceContext> contextResults;
        try {
            contextResults = contextFuture.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            contextFuture.cancel(true);
            throw new ExecutionException("Unable to finish genomic sequence context query on time", e);
        }

        if (CollectionUtils.isNotEmpty(contextResults)) {
            for (int i = 0; i < variantAnnotationList.size(); i++) {
                GenomicSequenceContext context = contextResults.get(i);
                if (context != null) {
                    variantAnnotationList.get(i).setGenomicSequenceContext(context);
                }
            }
        }
    }
}
