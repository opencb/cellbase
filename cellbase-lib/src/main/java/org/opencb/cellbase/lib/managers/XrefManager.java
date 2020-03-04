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
import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.XRefMongoDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.List;

public class XrefManager extends AbstractManager {

    private XRefMongoDBAdaptor xRefDBAdaptor;

    public XrefManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        xRefDBAdaptor = dbAdaptorFactory.getXRefDBAdaptor(species, assembly);
    }

    public List<CellBaseDataResult<Document>> info(Query query, QueryOptions queryOptions, String id) {
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

    public CellBaseDataResult getAllXrefsByFeatureId(QueryOptions queryOptions, String ids, String dbname) {
        Query query = new Query();
        query.put(XRefDBAdaptor.QueryParams.ID.key(), ids);
        if (dbname != null && !dbname.isEmpty()) {
            query.put(XRefDBAdaptor.QueryParams.DBNAME.key(), dbname);
        }
        // FIXME
        CellBaseDataResult queryResult = xRefDBAdaptor.query(new GeneQuery());
        queryResult.setId(ids);
        return queryResult;
    }

    public CellBaseDataResult getDBNames(Query query) {
        return xRefDBAdaptor.distinct(query, "transcripts.xrefs.dbName");
    }
}
