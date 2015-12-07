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
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.client.CellBaseClient;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by fjlopez on 02/03/15.
 */
public class CellBaseWSVariantAnnotator implements VariantAnnotator {

    private CellBaseClient cellBaseClient;

    private QueryOptions queryOptions;

    private Logger logger;

    public CellBaseWSVariantAnnotator(CellBaseClient cellBaseClient, QueryOptions queryOptions) {
        this.cellBaseClient = cellBaseClient;
        this.queryOptions = new QueryOptions(queryOptions);
        this.queryOptions.put("post", true);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public boolean open() {
        return true;
    }

    public boolean close() {
        return false;
    }

    public void run(List<Variant> variantList) {
        logger.debug("Annotator sends {} new variants for annotation. Waiting for the result", variantList.size());
        QueryResponse<QueryResult<VariantAnnotation>> response;
        try {
            response = cellBaseClient.getAnnotation(CellBaseClient.Category.genomic,
                    CellBaseClient.SubCategory.variant, variantList, queryOptions);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //TODO: assuming CellBase annotation will always be the first and therefore variantAnnotationList will be empty
//        variantAnnotationList = new ArrayList<>(variantList.size());
        List<QueryResult<VariantAnnotation>> queryResultList = response.getResponse();
        for (int i = 0; i < queryResultList.size(); i++) {
            if (queryResultList.get(i).getResult().size() > 0) {
                variantList.get(i).setAnnotation(queryResultList.get(i).getResult().get(0));
            } else {
                logger.warn("Emtpy result for '{}'", queryResultList.get(i).getId());
            }
        }
    }


    // TODO: use a external class for this (this method could be added to GenomicVariant class)
    private Variant getGenomicVariant(Variant variant) {
        if (variant.getAlternate().equals(".")) {  // reference positions are not variants
            return null;
        } else {
            String ref;
            // large deletion
            if (variant.getAlternate().equals("<DEL>")) {
                // .get("_") because studyId and fileId are empty strings when VariantSource is initialized at readInputFile
                int end = Integer.valueOf(variant.getSourceEntries().get("_").getAttributes().get("END"));
                ref = StringUtils.repeat("N", end - variant.getStart());
                return new Variant(variant.getChromosome(), variant.getStart(),
                        ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
                // TODO: structural variants are not yet properly handled. Implement and remove this patch asap
            } else if (variant.getAlternate().startsWith("<")
                    || (variant.getAlternate().length() > 1 && variant.getReference().length() > 1)) {
                return null;
            } else {
                ref = variant.getReference().equals("") ? "-" : variant.getReference();
                return new Variant(variant.getChromosome(), variant.getStart(),
                        ref, variant.getAlternate().equals("") ? "-" : variant.getAlternate());
            }
        }
    }

}
