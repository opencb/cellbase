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

package org.opencb.cellbase.lib.impl.core;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.api.XrefQuery;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 07/12/15.
 */
public class XRefMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<XrefQuery, Xref> {

    public XRefMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("XRefMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public CellBaseIterator<Xref> iterator(XrefQuery query) {
        QueryOptions queryOptions = query.toQueryOptions();
        List<Bson> pipeline = unwind(query);
        GenericDocumentComplexConverter<Xref> converter = new GenericDocumentComplexConverter<>(Xref.class);
        MongoDBIterator<Xref> iterator = mongoDBCollection.iterator(pipeline, converter, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<Xref>> info(List<String> ids, ProjectionQueryOptions queryOptions) {
        List<CellBaseDataResult<Xref>> results = new ArrayList<>();
        for (String id : ids) {
            XrefQuery query = getInfoQuery(queryOptions);
            query.setIds(Collections.singletonList(id));
            CellBaseIterator<Xref> iterator = iterator(query);
            List<Xref> xrefs = new ArrayList<>();
            while (iterator.hasNext()) {
                xrefs.add(iterator.next());
            }
            results.add(new CellBaseDataResult<>(id, 0, new ArrayList<>(), xrefs.size(), xrefs, -1));
            iterator.close();
        }
        return results;
    }

    private XrefQuery getInfoQuery(ProjectionQueryOptions queryOptions) {
        XrefQuery xrefQuery;
        if (queryOptions == null) {
            xrefQuery = new XrefQuery();
        } else {
            xrefQuery = (XrefQuery) queryOptions;
        }
        return xrefQuery;
    }

    private List<Bson> unwind(XrefQuery query) {
        Bson bson = parseQuery(query);
        Bson match = Aggregates.match(bson);

        Bson project = Aggregates.project(Projections.include("transcripts.xrefs"));
        Bson unwind = Aggregates.unwind("$transcripts");
        Bson unwind2 = Aggregates.unwind("$transcripts.xrefs");

        // This project the three fields of Xref to the top of the object
        Document document = new Document("id", "$transcripts.xrefs.id");
        document.put("dbName", "$transcripts.xrefs.dbName");
        document.put("dbDisplayName", "$transcripts.xrefs.dbDisplayName");
        Bson project1 = Aggregates.project(document);

        List<Bson> aggregateList = new ArrayList<>();

        aggregateList.add(match);
        aggregateList.add(project);
        aggregateList.add(unwind);
        aggregateList.add(unwind2);
        aggregateList.add(project1);

        return aggregateList;
    }

    @Override
    public CellBaseDataResult<Long> count(XrefQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult aggregationStats(XrefQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(XrefQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(XrefQuery query) {
        return null;
    }

    public Bson parseQuery(XrefQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "id":
                        createAndOrQuery(value, "transcripts.xrefs.id", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "dbname":
                        createAndOrQuery(value, "transcripts.xrefs.dbName", QueryParam.Type.STRING, andBsonList);
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
