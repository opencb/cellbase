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

package org.opencb.cellbase.core.db.api.variation;

import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variation.StructuralVariation;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.db.DBAdaptor;

import java.util.List;


public interface StructuralVariationDBAdaptor extends DBAdaptor {


    List<StructuralVariation> getAllByRegion(Region region);

    List<List<StructuralVariation>> getAllByRegionList(List<Region> regionList);


    List<StructuralVariation> getAllByRegion(Region region, int minLength, int maxLength);

    List<List<StructuralVariation>> getAllByRegionList(List<Region> regionList, int minLength, int maxLength);


    List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);

}
