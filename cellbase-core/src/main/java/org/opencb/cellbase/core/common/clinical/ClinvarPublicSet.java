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

package org.opencb.cellbase.core.common.clinical;

import org.opencb.biodata.formats.variant.clinvar.v24jaxb.PublicSetType;

/**
 * Created by parce on 10/29/14.
 */
public class ClinvarPublicSet extends ClinicalVariant {

    private PublicSetType clinvarSet;

    public ClinvarPublicSet(String chromosome, int start, int end, String reference, String alternate, PublicSetType clinvarSet) {
        super(chromosome, start, end, reference, alternate, "clinvar");
        this.clinvarSet = clinvarSet;
    }

    public PublicSetType getClinvarSet() {
        return clinvarSet;
    }

    public void setClinvarSet(PublicSetType clinvarSet) {
        this.clinvarSet = clinvarSet;
    }
}
