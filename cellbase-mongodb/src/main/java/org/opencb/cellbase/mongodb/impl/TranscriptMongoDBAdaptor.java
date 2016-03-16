package org.opencb.cellbase.mongodb.impl;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.cellbase.core.api.TranscriptDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by swaathi on 27/11/15.
 */
public class TranscriptMongoDBAdaptor extends MongoDBAdaptor implements TranscriptDBAdaptor<Transcript> {

    public TranscriptMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");

        logger.debug("TranscriptMongoDBAdaptor: in 'constructor'");
    }


    @Override
    public QueryResult<String> getCdna(String id) {
        Bson bson = Filters.eq("transcripts.xrefs.id", id);
        Bson elemMatch = Projections.elemMatch("transcripts", Filters.eq("xrefs.id", id));
        Bson include = Projections.include("transcripts.cDnaSequence");
        // elemMatch and include are combined to reduce the data sent from the server
        Bson projection = Projections.fields(elemMatch, include);
        QueryResult<Document> result = mongoDBCollection.find(bson, projection, new QueryOptions());

        String sequence = null;
        if (result != null && !result.getResult().isEmpty()) {
            List<Document> transcripts = (List<Document>) result.getResult().get(0).get("transcripts");
            sequence = transcripts.get(0).getString("cDnaSequence");
        }
        return new QueryResult<>(id, result.getDbTime(), result.getNumResults(), result.getNumTotalResults(),
                result.getWarningMsg(), result.getErrorMsg(), Collections.singletonList(sequence));
    }

    @Override
    public QueryResult<Long> update(List objectList, String field) {
        return null;
    }

    @Override
    public QueryResult<Long> count(Query query) {
        Bson document = parseQuery(query);
        Bson match = Aggregates.match(document);

        List<String> includeFields = new ArrayList<>();
        for (String s : query.keySet()) {
            if (StringUtils.isNotEmpty(query.getString(s))) {
                includeFields.add(s);
            }
        }

        Bson include;
        if (includeFields.size() > 0) {
            include = Aggregates.project(Projections.include(includeFields));
        } else {
            include = Aggregates.project(Projections.include("transcripts.id"));
        }

        Bson unwind = Aggregates.unwind("$transcripts");
        Bson match2 = Aggregates.match(document);
        Bson project = Aggregates.project(new Document("transcripts", "$transcripts.id"));

        Bson group = Aggregates.group("transcripts", Accumulators.sum("count", 1));

        QueryResult queryResult = mongoDBCollection.aggregate(Arrays.asList(match, include, unwind, match2, project, group), null);
        return queryResult;
    }

    @Override
    public QueryResult distinct(Query query, String field) {
        Bson bsonDocument = parseQuery(query);
        return mongoDBCollection.distinct(field, bsonDocument);

    }

    @Override
    public QueryResult stats(Query query) {
        return null;
    }

    @Override
    public QueryResult<Transcript> get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        Bson match = Aggregates.match(bson);

        Bson include = null;
        if (options != null && options.containsKey("include")) {
            List<String> includeList = new ArrayList<>();
            List<String> optionsAsStringList = options.getAsStringList("include");
            for (String s : optionsAsStringList) {
                if (s.startsWith("transcripts")) {
                    includeList.add(s);
                }
            }

            if (includeList.size() > 0) {
                include = Projections.include(includeList);
            }
        }

        if (include == null) {
            include = Projections.include("transcripts");
        }
        Bson excludeAndInclude = Aggregates.project(Projections.fields(Projections.excludeId(), include));
        Bson unwind = Aggregates.unwind("$transcripts");
        Bson match2 = Aggregates.match(bson);

        // This project the three fields of Xref to the top of the object
        Document document = new Document("id", "$transcripts.id");
        document.put("name", "$transcripts.name");
        document.put("biotype", "$transcripts.biotype");
        document.put("chromosome", "$transcripts.chromosome");
        document.put("start", "$transcripts.start");
        document.put("end", "$transcripts.end");
        document.put("strand", "$transcripts.strand");
        document.put("cDnaSequence", "$transcripts.cDnaSequence");
        document.put("xrefs", "$transcripts.xrefs");
        document.put("exons", "$transcripts.exons");
        document.put("annotationFlags", "$transcripts.annotationFlags");
        Bson project = Aggregates.project(document);

        return mongoDBCollection.aggregate(Arrays.asList(match, excludeAndInclude, unwind, match2, project), options);
    }

    @Override
    public Iterator<Transcript> iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.nativeQuery().find(bson, options).iterator();
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
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, field, "name", options);

    }

    @Override
    public QueryResult groupBy(Query query, List fields, QueryOptions options) {
        Bson bsonQuery = parseQuery(query);
        return groupBy(bsonQuery, fields, "name", options);
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
        if (query.getString("region") != null) {
            Region region = Region.parseRegion(query.getString("region"));
            Bson bsonDocument = parseQuery(query);
            return getIntervalFrequencies(bsonDocument, region, intervalSize, options);
        }
        return null;
    }

    private Bson parseQuery(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        createRegionQuery(query, QueryParams.REGION.key(), MongoDBCollectionConfiguration.GENE_CHUNK_SIZE, andBsonList);
        createOrQuery(query, QueryParams.ID.key(), "transcripts.id", andBsonList);
        createOrQuery(query, QueryParams.NAME.key(), "transcripts.name", andBsonList);
        createOrQuery(query, QueryParams.BIOTYPE.key(), "transcripts.biotype", andBsonList);
        createOrQuery(query, QueryParams.XREFS.key(), "transcripts.xrefs.id", andBsonList);
        createOrQuery(query, QueryParams.TFBS_NAME.key(), "transcripts.tfbs.name", andBsonList);

        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

}
