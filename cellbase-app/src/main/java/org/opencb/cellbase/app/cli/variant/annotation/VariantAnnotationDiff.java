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

import org.opencb.biodata.models.variant.avro.VariantAnnotation;

import java.util.List;

/**
 * Created by fjlopez on 07/04/16.
 */
public class VariantAnnotationDiff {

    private List<SequenceOntologyTermComparisonObject> sequenceOntology = null;
    private VariantAnnotation variantAnnotation = null;

    public VariantAnnotationDiff() {}

    public boolean isEmpty() {
        return (sequenceOntology == null);
    }

    public List<SequenceOntologyTermComparisonObject> getSequenceOntology() {
        return sequenceOntology;
    }

    public void setSequenceOntology(List<SequenceOntologyTermComparisonObject> sequenceOntology) {
        this.sequenceOntology = sequenceOntology;
    }

    public VariantAnnotation getVariantAnnotation() {
        return variantAnnotation;
    }

    public void setVariantAnnotation(VariantAnnotation variantAnnotation) {
        this.variantAnnotation = variantAnnotation;
    }
}
