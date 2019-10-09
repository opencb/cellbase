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

package org.opencb.cellbase.app.cli.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 22/09/15.
 */
public class CellBaseLocalVariantAnnotator implements VariantAnnotator {

    private VariantAnnotationCalculator variantAnnotationCalculator;
    private QueryOptions queryOptions;

    private Logger logger;

    public CellBaseLocalVariantAnnotator(VariantAnnotationCalculator variantAnnotationCalculator, QueryOptions queryOptions) {
        this.variantAnnotationCalculator = variantAnnotationCalculator;
        this.queryOptions = queryOptions;
        logger = LoggerFactory.getLogger(this.getClass());
    }


    public boolean open() {
        return true;
    }

    public void run(List<Variant> variantList) throws InterruptedException, ExecutionException {
        logger.debug("Annotator sends {} new variants for annotation. Waiting for the result", variantList.size());

        // getAnnotationByVariantList will not create new Variant objects but modify the ones passed as parameters - no
        // need to go through the queryResultList afterwards
        List<QueryResult<VariantAnnotation>> queryResultList =
                variantAnnotationCalculator.getAnnotationByVariantList(variantList, queryOptions);

    }

    public boolean close() {
        return false;
    }

}
