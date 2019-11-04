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

package org.opencb.cellbase.core.variant;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.EvidenceEntry;
import org.opencb.biodata.models.variant.avro.Property;

import java.util.Collections;
import java.util.List;

public class ClinicalPhasedQueryManager extends AnnotationBasedPhasedQueryManager<EvidenceEntry> {

    private static final String HAPLOTYPE_PROPERTY = "haplotype";

    @Override
    protected List<Variant> getHaplotype(EvidenceEntry evidenceEntry, Variant variant) {
        List<Property> additionalProperties = evidenceEntry.getAdditionalProperties();
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            for (Property property : additionalProperties) {
                // Only one haplotype property expected; will select the first one and discard the rest anyway
                if (property.getName().equals(HAPLOTYPE_PROPERTY)) {
                    return Variant.parseVariants(property.getValue());
                }
            }

        }

        return Collections.emptyList();
    }

    @Override
    protected List<EvidenceEntry> getAnnotationObjectList(Variant variant) {
        return variant.getAnnotation().getTraitAssociation();
    }
}
