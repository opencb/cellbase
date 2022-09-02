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

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.formats.pubmed.v233jaxb.PubmedArticle;
import org.opencb.cellbase.core.api.PublicationQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PublicationMongoDBAdaptor extends CellBaseDBAdaptor implements CellBaseCoreDBAdaptor<PublicationQuery, PubmedArticle> {

    private static final GenericDocumentComplexConverter<PubmedArticle> CONVERTER;

    static {
        CONVERTER = new GenericDocumentComplexConverter<>(PubmedArticle.class);
    }

    public PublicationMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        this.init();
    }

    private void init() {
        logger.debug("PublicationMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap("pubmed");
    }

    @Override
    public CellBaseIterator<PubmedArticle> iterator(PublicationQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        Bson projection = getProjection(query);
        QueryOptions queryOptions = query.toQueryOptions();

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        MongoDBIterator<PubmedArticle> iterator = mongoDBCollection.iterator(null, bson, projection, CONVERTER, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public List<CellBaseDataResult<PubmedArticle>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease)
            throws CellBaseException {
        List<CellBaseDataResult<PubmedArticle>> results = new ArrayList<>();
        Bson projection = getProjection(queryOptions);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        for (String id : ids) {
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            Bson bson = Filters.or(orBsonList);
            results.add(new CellBaseDataResult<>(mongoDBCollection.find(bson, projection, CONVERTER, new QueryOptions())));
        }
        return results;
    }

    @Override
    public CellBaseDataResult<Long> count(PublicationQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(PublicationQuery query) throws CellBaseException {
        Bson bsonDocument = parseQuery(query);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument));
    }

    @Override
    public CellBaseDataResult<PubmedArticle> aggregationStats(PublicationQuery query) {
        return null;
    }

    @Override
    public CellBaseDataResult groupBy(PublicationQuery query) throws CellBaseException {
        Bson bsonQuery = parseQuery(query);
        logger.info("geneQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return groupBy(bsonQuery, query, "name", mongoDBCollection);
    }

    public Bson parseQuery(PublicationQuery query) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : query.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                if (!"dataRelease".equals(dotNotationName)) {
                    Object value = entry.getValue();
                    switch (dotNotationName) {
                        case "id": {
                            createAndOrQuery(value, "medlineCitation.pmid.content", QueryParam.Type.STRING, andBsonList);
                            break;
                        }
                        case "author": {
                            createAndOrQuery(value, "medlineCitation.article.content.author.lastName", QueryParam.Type.STRING, andBsonList);
                            break;
                        }
                        case "title": {
                            if (value != null && CollectionUtils.isNotEmpty((Collection<?>) value)) {
                                List<Object> newValue = ((Collection<?>) value).stream().map(v -> "~" + v).collect(Collectors.toList());
                                createAndOrQuery(newValue, "medlineCitation.article.content.title", QueryParam.Type.STRING, andBsonList);
                            }
                            break;
                        }
                        case "keyword": {
                            createAndOrQuery(value, "medlineCitation.keywordList.keyword.content", QueryParam.Type.STRING, andBsonList);
                            break;
                        }
                        case "abstract": {
                            if (value != null && CollectionUtils.isNotEmpty((Collection<?>) value)) {
                                List<Object> newValue = ((Collection<?>) value).stream().map(v -> "~" + v).collect(Collectors.toList());
                                createAndOrQuery(newValue, "medlineCitation.article.content.abstractText.content", QueryParam.Type.STRING,
                                        andBsonList);
                            }
                            break;
                        }
                        default: {
                            // Do nothing
                            break;
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        logger.info("Publication parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
