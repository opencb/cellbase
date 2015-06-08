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

import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by fjlopez on 02/03/15.
 */
public class CellbaseWSVariantAnnotator implements VariantAnnotator {

    private Logger logger;
    private CellBaseClient cellBaseClient;
    private List<VariantAnnotation> variantAnnotationList;


    public CellbaseWSVariantAnnotator(CellBaseClient cellBaseClient) {
        this.cellBaseClient = cellBaseClient;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public boolean open() {
        return true;
    }

    public boolean close() {
        return false;
    }

    public List<VariantAnnotation> run(List<Variant> variantList) {

        List<GenomicVariant> batch = convertVariantsToGenomicVariants(variantList);
        logger.debug("Annotator sends {} new variants for annotation. Waiting for the result", batch.size());
        QueryResponse<QueryResult<VariantAnnotation>> response;
        try {
            response = cellBaseClient.getFullAnnotation(CellBaseClient.Category.genomic,
                            CellBaseClient.SubCategory.variant, batch, new QueryOptions("post", true));
        } catch (IOException e) {
            return null;
        }

        //TODO: assuming CellBase annotation will always be the first and therefore variantAnnotationList will be empty
        for (QueryResult<VariantAnnotation> queryResult : response.getResponse()) {
            variantAnnotationList.add(queryResult.getResult().get(0));
        }
        return variantAnnotationList;
    }


    private List<GenomicVariant> convertVariantsToGenomicVariants(List<Variant> vcfBatch) {
        List<GenomicVariant> genomicVariants = new ArrayList<>(vcfBatch.size());
        for (Variant variant : vcfBatch) {
            GenomicVariant genomicVariant;
            if((genomicVariant = getGenomicVariant(variant))!=null) {
                genomicVariants.add(genomicVariant);
            }
        }
        return genomicVariants;
    }

    // TODO: use a external class for this (this method could be added to GenomicVariant class)
    private GenomicVariant getGenomicVariant(Variant variant) {
        if(variant.getAlternate().equals(".")) {  // reference positions are not variants
            return null;
        } else {
            String ref;
            if (variant.getAlternate().equals("<DEL>")) {  // large deletion
                int end = Integer.valueOf(variant.getSourceEntries().get("_").getAttributes().get("END"));  // .get("_") because studyId and fileId are empty strings when VariantSource is initialized at readInputFile
                ref = StringUtils.repeat("N", end - variant.getStart());
            } else {
                ref = variant.getReference().equals("") ? "-" : variant.getReference();
            }
            return new GenomicVariant(variant.getChromosome(), variant.getStart(),
                    ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
            //        return new GenomicVariant(variant.getChromosome(), ensemblPos, ref, alt);
        }
    }

    public void setVariantAnnotationList(List<VariantAnnotation> variantAnnotationList) {
        this.variantAnnotationList = variantAnnotationList;
    }
}
