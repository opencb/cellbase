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

package org.opencb.cellbase.core.lib.api.variation;

import java.util.List;

@Deprecated
public interface SnpDBAdaptor {

	
//	@Override
//	public List<Snp> getAll();
//
//	
//	public List<Snp> getAllBySnpId(String snpId);
//
//	public List<List<Snp>> getAllBySnpIdList(List<String> snpIdList);
//
//	public List<Snp> getAllByGeneName(String externalId);
//	
//	public List<List<Snp>> getAllByGeneNameList(List<String> externalIds);
//	
//	public List<Snp> getAllByEnsemblGeneId(String ensemblGeneId);
//	
//	public List<List<Snp>> getAllByEnsemblGeneIdList(List<String> ensemblGeneIds);
//	
//	public List<Snp> getAllByEnsemblTranscriptId(String ensemblTranscriptId);
//	
//	public List<List<Snp>> getAllByEnsemblTranscriptIdList(List<String> ensemblTranscriptIds);
//
//	public List<String> getAllIdsByRegion(String chromosome, int start, int end);
//
//	
//	public List<ConsequenceType> getAllConsequenceTypes();
//	
//	public List<ConsequenceType> getAllConsequenceTypesBySnpId(String snpId);
	
//	public List<List<SnpToTranscriptConsequenceType>> getAllConsequenceTypesBySnpIdList(List<String> snpId);
//
//	public List<SnpToTranscript> getAllSnpToTranscriptsBySnpId(String snpId);
//	
//	public List<SnpToTranscript> getAllSnpToTranscriptsByTranscriptId(String transcriptId);

	public List<String> getAllIdsBySOConsequenceType(String soConsequenceType);
	
	public List<List<String>> getAllIdsBySOConsequenceTypeList(List<String> soConsequenceTypeList);
	
//	public List<Snp> getAllBySOConsequenceType(String soConsequenceType);
//	
//	public List<Snp> getAllBySOConsequenceTypeList(List<String> soConsequenceTypeList);
//	
//	public List<Snp> getAllByEnsemblConsequenceType(String ensemblConsequenceType);
//	
//	public List<Snp> getAllByEnsemblConsequenceTypeList(List<String> ensemblConsequenceTypeList);
//	
//	
//	public List<Snp> getAllByPosition(String chromosome, int position);
//	
//	public List<Snp> getAllByPosition(Position position);
//	
//	public List<List<Snp>> getAllByPositionList(List<Position> positionList);
//	
//	
//	public List<Snp> getAllByRegion(String chromosome);
//	
//	public List<Snp> getAllByRegion(String chromosome, int start);
//	
//	public List<Snp> getAllByRegion(String chromosome, int start, int end);
//	
//	public List<Snp> getAllByRegion(String chromosome, int start, int end, List<String> consequenceTypeList);
//	
//	public List<Snp> getAllByRegion(Region region);
//	
//	public List<Snp> getAllByRegion(Region region, List<String> consequenceTypeList);
//	
//	public List<List<Snp>> getAllByRegionList(List<Region> regionList);
//	
//	public List<List<Snp>> getAllByRegionList(List<Region> regionList, List<String> consequenceTypeList);
//	
//	public List<Snp> getAllByCytoband(String chromosome, String cytoband);
//	
//	
//	public List<Snp> getAllFilteredByConsequenceType(List<String> snpIds, String consequence);
//
//	public List<Snp> getAllFilteredByConsequenceType(List<String> snpIds, List<String> consequenceTypes);
//
//	public void writeAllFilteredByConsequenceType(String consequence, String outfile);
//	
//	
//	public List<SnpRegulatoryConsequenceType> getAllSnpRegulatoryBySnpName(String name);
//	
//	public List<List<SnpRegulatoryConsequenceType>> getAllSnpRegulatoryBySnpNameList(List<String> nameList);
//	
//	
//	
//	public List<VariationPhenotypeAnnotation> getAllSnpPhenotypeAnnotationBySnpName(String name);
//	
//	public List<List<VariationPhenotypeAnnotation>> getAllSnpPhenotypeAnnotationListBySnpNameList(List<String> nameList);
//	
//	public List<VariationPhenotypeAnnotation> getAllSnpPhenotypeAnnotationByPosition(Position position);
//	
//	public List<List<VariationPhenotypeAnnotation>> getAllSnpPhenotypeAnnotationListByPositionList(List<Position> positionList);
//	
//	
//	
////	public List<SnpPopulationFrequency> getAllSnpPopulationFrequency(String name);
////	
////	public List<List<SnpPopulationFrequency>> getAllSnpPopulationFrequencyList(List<String> nameList);
////	
////	
////	
////	public List<SnpToTranscript> getAllSnpToTranscript(String name);
////	
////	public List<List<SnpToTranscript>> getAllSnpToTranscriptList(List<String> nameList);
//	
//	
//	
//	public List<ConsequenceType> getAllConsequenceType(String name);
//	
//	public List<List<ConsequenceType>> getAllConsequenceTypeList(List<String> nameList);
//	
//	public List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);
	
}
