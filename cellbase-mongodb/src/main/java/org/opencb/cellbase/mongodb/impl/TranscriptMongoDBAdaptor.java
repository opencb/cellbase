package org.opencb.cellbase.mongodb.impl;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by swaathi on 27/11/15.
 */
public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements TranscriptDBAdaptor {

    public TranscriptMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.info("TranscriptMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult<Long> count() {
        return mongoDBCollection.count();
    }

    @Override
    public QueryResult<Long> count(Query query) {
        Bson document = parseQuery(query);

        return mongoDBCollection.count(document);
    }

    @Override
    public QueryResult distinct(Query query, String field) {
        return null;
    }

    @Override
    public QueryResult stats() {
        return null;
    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult first() {
        return null;
    }

    @Override
    public QueryResult get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> get(List list, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public List<QueryResult> nativeGet(List list, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Iterator nativeIterator() {
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
    public void forEach(Consumer action) {

    }

    @Override
    public void forEach(Query query, Consumer action, QueryOptions options) {

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
    public QueryResult next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeNext(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getIntervalFrequencies(Query query, QueryOptions options) {
        return null;
    }
    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, TranscriptDBAdaptor.QueryParams.REGION.key(), andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.ID.key(), "transcripts.id", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.NAME.key(), "transcripts.name", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.BIOTYPE.key(), "transcripts.biotype", andBsonList);
        createOrQuery(query, TranscriptDBAdaptor.QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
