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

package org.opencb.cellbase.core.lib.api.core;

import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface ProteinDBAdaptor { //extends FeatureDBAdaptor


    public QueryResult getAll(QueryOptions options);


    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    public QueryResult getAllByAccession(String id, QueryOptions options);

    public List<QueryResult> getAllByAccessionList(List<String> idList, QueryOptions options);



//	public List<String> getAllUniprotAccessions();
//
//	public List<String> getAllUniprotNames();
//
//
//	public List<Protein> getAllByUniprotAccession(String uniprotId);
//
//	public List<List<Protein>> getAllByUniprotAccessionList(List<String> uniprotIdList);
//
//
//	public List<Protein> getAllByProteinName(String name);
//
//	public List<List<Protein>> getAllByProteinNameList(List<String> nameList);
//
//
//	public List<Protein> getAllByEnsemblGene(String ensemblGene);
//
//	public List<List<Protein>> getAllByEnsemblGeneList(List<String> ensemblGeneList);
//
//	public List<Protein> getAllByEnsemblTranscriptId(String transcriptId);
//
//	public List<List<Protein>> getAllByEnsemblTranscriptIdList(List<String> transcriptIdList);
//
//	public List<Protein> getAllByGeneName(String geneName);
//
//	public List<List<Protein>> getAllByGeneNameList(List<String> geneNameList);
//
//
//	public List<SequenceType> getAllProteinSequenceByProteinName(String name);
//
//	public List<List<SequenceType>> getAllProteinSequenceByProteinNameList(List<String> nameList);
//
//
//	public List<FeatureType> getAllProteinFeaturesByUniprotId(String name);
//
//	public List<List<FeatureType>> getAllProteinFeaturesByUniprotIdList(List<String> nameList);
//
//	public List<FeatureType> getAllProteinFeaturesByGeneName(String name);
//
//	public List<List<FeatureType>> getAllProteinFeaturesByGeneNameList(List<String> nameList);
//
//	public List<FeatureType> getAllProteinFeaturesByProteinXref(String name);
//
//	public List<List<FeatureType>> getAllProteinFeaturesByProteinXrefList(List<String> nameList);
//
//
//	public List<ProteinInteraction> getAllProteinInteractionsByProteinName(String name);
//
//	public List<ProteinInteraction> getAllProteinInteractionsByProteinName(String name, String source);
//
//	public List<List<ProteinInteraction>> getAllProteinInteractionsByProteinNameList(List<String> nameList);
//
//	public List<List<ProteinInteraction>> getAllProteinInteractionsByProteinNameList(List<String> nameList, String source);
//
//
//	public List<ProteinRegion> getAllProteinRegionByGenomicRegion(Region region);
//
//	public List<List<ProteinRegion>> getAllProteinRegionByGenomicRegionList(List<Region> regionList);
//
//
//	public List<DbReferenceType> getAllProteinXrefsByProteinName(String name);
//
//	public List<List<DbReferenceType>> getAllProteinXrefsByProteinNameList(List<String> nameList);
//
//	public List<DbReferenceType> getAllProteinXrefsByProteinName(String name, String dbname);
//
//	public List<List<DbReferenceType>> getAllProteinXrefsByProteinNameList(List<String> nameList, String dbname);
//
//	public List<DbReferenceType> getAllProteinXrefsByProteinName(String name, List<String> dbname);
//
//	public List<List<DbReferenceType>> getAllProteinXrefsByProteinNameList(List<String> nameList, List<String> dbname);

}
