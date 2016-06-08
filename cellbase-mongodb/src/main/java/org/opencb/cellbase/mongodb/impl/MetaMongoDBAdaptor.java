package org.opencb.cellbase.mongodb.impl;

import org.bson.BsonDocument;
import org.bson.Document;
import org.opencb.cellbase.core.api.CellBaseDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 07/06/16.
 */
public class MetaMongoDBAdaptor extends MongoDBAdaptor implements CellBaseDBAdaptor<Document> {

    public MetaMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);

        mongoDBCollection = mongoDataStore.getCollection("metadata");

        logger.debug("MetaMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult<Long> update(List objectList, String field) {
        return null;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult distinct(Query query, String field) {
        return null;
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        addPrivateExcludeOptions(options);
        return mongoDBCollection.find(new BsonDocument(), options);
    }

    @Override
    public Iterator iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult rank(Query query, String field, int numResults, boolean asc) {
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, String field, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) { }
}
