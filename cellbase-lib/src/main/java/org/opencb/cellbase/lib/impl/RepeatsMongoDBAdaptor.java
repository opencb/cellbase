package org.opencb.cellbase.lib.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.api.RepeatsDBAdaptor;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by fjlopez on 10/05/17.
 */
public class RepeatsMongoDBAdaptor extends MongoDBAdaptor implements RepeatsDBAdaptor {
    private static final String REPEAT_COLLECTION = "repeats";

    public RepeatsMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDatastore) {
        super(species, assembly, mongoDatastore);
        mongoDBCollection = mongoDataStore.getCollection(REPEAT_COLLECTION);

        logger.debug("RepeatsMongoDBAdaptor: in 'constructor'");

    }

    @Override
    public QueryResult<Long> update(List objectList, String field, String[] innerFields) {
        return null;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        return null;
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
        Bson bson = parseQuery(query);
        options = addPrivateExcludeOptions(options);

        logger.debug("query: {}", bson.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return mongoDBCollection.find(bson, null, Repeat.class, options);
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        return null;
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
    public void forEach(Query query, Consumer action, QueryOptions options) {

    }

    @Override
    public QueryResult groupBy(Query query, List fields, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
        return null;
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, RepeatsDBAdaptor.QueryParams.REGION.key(),
                MongoDBCollectionConfiguration.REPEATS_CHUNK_SIZE, andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }

    }

}
