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

package org.opencb.cellbase.core.db.api.systems;

import org.opencb.cellbase.core.db.DBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProteinProteinInteractionDBAdaptor extends DBAdaptor {


    QueryResult getAll(QueryOptions options);

    QueryResult getAllById(String id, QueryOptions options);

    List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    QueryResult getAllByInteractorId(String id, QueryOptions options);

    List<QueryResult> getAllByInteractorIdList(List<String> idList, QueryOptions options);

}
