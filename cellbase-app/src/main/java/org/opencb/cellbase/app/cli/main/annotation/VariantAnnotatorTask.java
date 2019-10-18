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

package org.opencb.cellbase.app.cli.main.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantType;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotator;
import org.opencb.commons.run.ParallelTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fjlopez on 11/02/16.
 */
public class VariantAnnotatorTask implements
        ParallelTaskRunner.TaskWithException<Variant, Variant, Exception> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<VariantAnnotator> variantAnnotatorList;

    public VariantAnnotatorTask(List<VariantAnnotator> variantAnnotatorList) {
        this.variantAnnotatorList = variantAnnotatorList;
    }

    public void pre() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.open();
        }
    }

    public List<Variant> apply(List<Variant> batch) throws Exception {
        List<Variant> variantListToAnnotate = filterReferenceBlocksOut(batch);
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.run(variantListToAnnotate);
        }
        return variantListToAnnotate;
    }

    private List<Variant> filterReferenceBlocksOut(List<Variant> variantList) {
        List<Variant> filteredVariantList = new ArrayList<>(variantList.size());
        for (Variant variant : variantList) {
            if (!VariantType.NO_VARIATION.equals(variant.getType())) {
                filteredVariantList.add(variant);
            }
        }

        return filteredVariantList;
    }

    public void post() {
        for (VariantAnnotator variantAnnotator : variantAnnotatorList) {
            variantAnnotator.close();
        }
    }

}
