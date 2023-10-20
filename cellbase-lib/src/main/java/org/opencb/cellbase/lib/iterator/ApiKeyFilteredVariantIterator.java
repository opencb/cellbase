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

package org.opencb.cellbase.lib.iterator;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.key.ApiKeyLicensedDataUtils;

import java.util.Set;

public class ApiKeyFilteredVariantIterator extends CellBaseIterator<Variant> {

    private Set<String> validSources;

    public ApiKeyFilteredVariantIterator(CellBaseIterator iterator, Set<String> validSources) {
        super(iterator);
        this.validSources = validSources;
    }

    @Override
    public Variant next() {
        // Check clinical data sources
        Variant variant = iterator.next();
        return ApiKeyLicensedDataUtils.filterDataSources(variant, validSources);
    }

    @Override
    public void close() {
    }
}
