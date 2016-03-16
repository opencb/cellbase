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

package org.opencb.cellbase.core.db.api.core;

import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;


public interface TranscriptDBAdaptor extends FeatureDBAdaptor {


    QueryResult next(String id, QueryOptions options);

    QueryResult getById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    QueryResult getAllByXref(String id, QueryOptions options);

    List<QueryResult> getAllByXrefList(List<String> idList, QueryOptions options);


    QueryResult getAllByEnsemblExonId(String ensemblExonId, QueryOptions options);

    List<QueryResult> getAllByEnsemblExonIdList(List<String> ensemblExonIdList, QueryOptions options);


    QueryResult getAllTargetsByTf(String tfId, QueryOptions options);

    List<QueryResult> getAllTargetsByTfList(List<String> tfIdList, QueryOptions options);


    List<Transcript> getAllByMirnaMature(String mirnaID);

    List<List<Transcript>> getAllByMirnaMatureList(List<String> mirnaIDList);

}
