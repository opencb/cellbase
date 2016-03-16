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

package org.opencb.cellbase.core.db.api;

import org.opencb.biodata.models.core.Cytoband;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;

import java.util.List;

@Deprecated
public interface CytobandDBAdaptor extends FeatureDBAdaptor {


    List<Cytoband> getAllByRegion(String chromosome);

    List<Cytoband> getAllByRegion(String chromosome, int start);

    List<Cytoband> getAllByRegion(String chromosome, int start, int end);

    List<Cytoband> getAllByRegion(Region region);

    List<List<Cytoband>> getAllByRegionList(List<Region> regionList);


    List<String> getAllChromosomeNames();

    List<Cytoband> getAllByChromosome(String chromosome);

    List<List<Cytoband>> getAllByChromosomeList(List<String> chromosome);

}
