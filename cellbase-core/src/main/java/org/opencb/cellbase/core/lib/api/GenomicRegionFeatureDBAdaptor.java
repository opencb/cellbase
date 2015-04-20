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

package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Region;

import java.util.List;


// Como no es una fetaure no deberia implementas FeatureDBAdaptor no?
//public interface FeatureMapDBAdaptor extends FeatureDBAdaptor {	

@Deprecated
public interface GenomicRegionFeatureDBAdaptor {
	
	
	public String getByRegion(String chromosome, int start, int end);
	
	public String getByRegion(Region region);
	
	public List<String> getAllByRegionList(List<Region> regions);

	
	public String getByRegion(Region region, List<String> sources);	// sources: gene, exon, snp, ...
	
	public List<String> getAllByRegionList(List<Region> regions, List<String> sources);

	public List<String> getByVariants(List<GenomicVariant> variants, List<String> sources);

	List<String> getByVariants(List<GenomicVariant> variants);
	
//	public List<FeatureMap> getFeatureMapsByRegion(Region region);

//	public HashMap<String, List<String>> getConsequenceTypeSoAccession(String chromosome, int position);
//
//	HashMap<String, List<String>> getConsequenceTypeSoAccession(String chromosome,int position, String alternativeAllele);
	
	
//	public GenomicRegionFeatures getAllByRegion(Region region, List<String> featureTypes); // types: variation, regulatory, core, ...
//	public List<GenomicRegionFeatures> getAllByRegion(List<Region> regions, List<String> featureTypes);

	
}
