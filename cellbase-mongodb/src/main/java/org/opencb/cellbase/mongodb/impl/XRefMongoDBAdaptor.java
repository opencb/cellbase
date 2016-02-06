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

package org.opencb.cellbase.mongodb.impl;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Xref;
import org.opencb.cellbase.core.api.XRefDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Created by imedina on 07/12/15.
 */
public class XRefMongoDBAdaptor extends MongoDBAdaptor implements XRefDBAdaptor<Xref> {

    public XRefMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("XRefMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult startsWith(String id, QueryOptions options) {
        Bson regex = Filters.regex("transcripts.xrefs.id", Pattern.compile("^" + id));
        Bson include = Projections.include("id", "name", "chromosome", "start", "end");
        return mongoDBCollection.find(regex, include, options);
    }

    @Override
    public QueryResult contains(String id, QueryOptions options) {
        Bson regex = Filters.regex("transcripts.xrefs.id", Pattern.compile("\\w" + id + "\\w"));
        Bson include = Projections.include("id", "name", "chromosome", "start", "end");
        return mongoDBCollection.find(regex, include, options);
    }

    @Override
    public QueryResult<Long> update(List objectList, String field) {
        return null;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.count(bson);
    }

    @Override
    public QueryResult distinct(Query query, String field) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.distinct(field, bson);
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult<Xref> get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
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

        return mongoDBCollection.aggregate(Arrays.asList(match, project, unwind, unwind2, project1), options);
    }

    @Override
    public Iterator<Xref> iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
    }

    @Override
    public QueryResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, String field, QueryOptions options) {
        return groupBy(parseQuery(query), field, "name", options);
    }

    @Override
    public QueryResult groupBy(Query query, List<String> fields, QueryOptions options) {
        return groupBy(parseQuery(query), fields, "name", options);
    }

    @Override
    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {

    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createOrQuery(query, XRefDBAdaptor.QueryParams.ID.key(), "transcripts.xrefs.id", andBsonList);
        createOrQuery(query, XRefDBAdaptor.QueryParams.DBNAME.key(), "transcripts.xrefs.dbName", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

}
