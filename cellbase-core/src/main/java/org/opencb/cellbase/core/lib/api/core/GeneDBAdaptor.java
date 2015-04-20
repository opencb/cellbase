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


public interface GeneDBAdaptor extends FeatureDBAdaptor {


	public QueryResult getAllById(String id, QueryOptions options);

	public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    /**
     * This method search the given 'id' in the XRefs array
     * @param id Any possible XRef id
     * @param options
     * @return Any gene found having that Xref id
     */
    public QueryResult getAllByXref(String id, QueryOptions options);

    public List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options);


	public QueryResult getAllBiotypes(QueryOptions options);

	public QueryResult getAllTargetsByTf(String id);

	public List<QueryResult> getAllTargetsByTfList(List<String> idList);


//	public QueryResult getAllByTf(String id);
//
//	public List<QueryResult> getAllByTfList(List<String> idList);
//
//	public List<Gene> getAllByTfName(String tfName);
//
//	public List<List<Gene>> getAllByTfNameList(List<String> tfNameList);
//
//	public List<Gene> getAllByMiRnaMature(String mirbaseId);
//
//	public List<List<Gene>> getAllByMiRnaMatureList(List<String> mirbaseIds);

}
