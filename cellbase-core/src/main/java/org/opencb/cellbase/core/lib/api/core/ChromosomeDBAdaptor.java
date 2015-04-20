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

import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface ChromosomeDBAdaptor {

    public QueryResult speciesInfoTmp(String id, QueryOptions options);

	public QueryResult getAll(QueryOptions options);

	public QueryResult getById(String id, QueryOptions options);

	public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult getAllCytobandsById(String id, QueryOptions options);

    public List<QueryResult> getAllCytobandsByIdList(List<String> id, QueryOptions options);
    
//	List<Cytoband> getCytobandByName(String name);
//	List<List<Cytoband>> getCytobandByNameList(List<String> nameList);
//	List<String> getChromosomeNames();
}
