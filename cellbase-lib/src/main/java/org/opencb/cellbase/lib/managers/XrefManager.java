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

package org.opencb.cellbase.lib.managers;

import com.google.common.base.Splitter;
import org.bson.Document;
import org.opencb.cellbase.core.api.core.XRefDBAdaptor;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

public class XrefManager extends AbstractManager {

    public XrefManager(CellBaseConfiguration configuration) {
        super(configuration);
    }

    public List<CellBaseDataResult<Document>> info(Query query, QueryOptions queryOptions, String species, String assembly, String id) {
        logger.debug("Querying for Xref info");
        XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(species, assembly);

        List<String> list = Splitter.on(",").splitToList(id);
        List<Query> queries = createQueries(query, id, XRefDBAdaptor.QueryParams.ID.key());

        List<CellBaseDataResult<Document>> dbNameList = xRefDBAdaptor.nativeGet(queries, queryOptions);
        for (int i = 0; i < dbNameList.size(); i++) {
            dbNameList.get(i).setId(list.get(i));
            for (Document document : dbNameList.get(i).getResults()) {
                if (document.get("id").equals(list.get(i))) {
                    List<Document> objectList = new ArrayList<>(1);
                    objectList.add(document);
                    dbNameList.get(i).setResults(objectList);
                    return dbNameList;
                }
            }
        }
        return dbNameList;
    }

    public CellBaseDataResult getAllXrefsByFeatureId(QueryOptions queryOptions, String species, String assembly, String ids,
                                                     String dbname) {
        XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(species, assembly);

        Query query = new Query();
        query.put(XRefDBAdaptor.QueryParams.ID.key(), ids);
        if (dbname != null && !dbname.isEmpty()) {
            query.put(XRefDBAdaptor.QueryParams.DBNAME.key(), dbname);
        }
        CellBaseDataResult queryResult = xRefDBAdaptor.nativeGet(query, queryOptions);
        queryResult.setId(ids);
        return queryResult;
    }

    public CellBaseDataResult getDBNames(Query query, String species, String assembly) {
        XRefDBAdaptor xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(species, assembly);
        CellBaseDataResult queryResults = xRefDBAdaptor.distinct(query, "transcripts.xrefs.dbName");
        return queryResults;
    }
}
