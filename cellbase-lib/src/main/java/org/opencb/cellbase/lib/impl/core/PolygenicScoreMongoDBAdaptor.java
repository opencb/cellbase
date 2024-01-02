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

import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.pgs.CommonPolygenicScore;
import org.opencb.biodata.models.core.pgs.PolygenicScore;
import org.opencb.biodata.models.core.pgs.VariantPolygenicScore;
import org.opencb.biodata.models.variant.avro.PolygenicScoreAnnotation;
import org.opencb.cellbase.core.api.PolygenicScoreQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.GenericDocumentComplexConverter;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDBIterator;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PolygenicScoreMongoDBAdaptor extends CellBaseDBAdaptor
        implements CellBaseCoreDBAdaptor<PolygenicScoreQuery, CommonPolygenicScore> {

    protected Map<Integer, MongoDBCollection> pgsVariantMongoDBCollectionByRelease;

    private static final GenericDocumentComplexConverter<CommonPolygenicScore> CONVERTER;

    static {
        CONVERTER = new GenericDocumentComplexConverter<>(CommonPolygenicScore.class);
    }

    public PolygenicScoreMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("PolygenicScoreMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap(EtlCommons.PGS_COMMON_COLLECTION);
        pgsVariantMongoDBCollectionByRelease = buildCollectionByReleaseMap(EtlCommons.PGS_VARIANT_COLLECTION);
    }

    public CellBaseDataResult<PolygenicScoreAnnotation> getPolygenicScoreAnnotation(String chromosome, int position, String reference,
                                                                                    String alternate, int dataRelease)
            throws CellBaseException {
        long dbTimeStart = System.currentTimeMillis();

        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.eq("position", position));
        Bson query = Filters.and(andBsonList);

        MongoDBCollection mongoDBCollection = getCollectionByRelease(pgsVariantMongoDBCollectionByRelease, dataRelease);
        DataResult<VariantPolygenicScore> pgsVariantDataResult = mongoDBCollection.find(query, null, VariantPolygenicScore.class, new QueryOptions());

        List<PolygenicScoreAnnotation> results = new ArrayList<>();

        // Search for the right polygenic score, i.e., checking reference and alternate with PGS effectAllele and otherAllele
        if (pgsVariantDataResult.getNumResults() > 0) {
            for (VariantPolygenicScore score : pgsVariantDataResult.getResults()) {
                if ((score.getEffectAllele().equals(reference) && score.getOtherAllele().equals(alternate))
                        || (score.getEffectAllele().equals(alternate) && score.getOtherAllele().equals(reference))) {
                    PolygenicScoreAnnotation pgsAnnotation = new PolygenicScoreAnnotation();
                    List<String> pgsIds = score.getPolygenicScores().stream().map(PolygenicScore::getId).collect(Collectors.toList());
//                    pgsAnnotation.setId(score.get);
                    pgsAnnotation.getVariants().add(new org.opencb.biodata.models.variant.avro.VariantPolygenicScore(
                            score.getEffectAllele(), score.getOtherAllele(), score.getPolygenicScores());
                    results.add(score);
                }
            }
        }
        int dbTime = Long.valueOf(System.currentTimeMillis() - dbTimeStart).intValue();
        final String id = chromosome + ":" + position + ":" + reference + ":" + alternate;
        return new CellBaseDataResult<>(id, dbTime, new ArrayList<>(), results.size(), results, results.size());
    }

    @Override
    public CellBaseIterator<CommonPolygenicScore> iterator(PolygenicScoreQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        MongoDBIterator<CommonPolygenicScore> iterator;
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        iterator = mongoDBCollection.iterator(null, bson, projection, CONVERTER, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<CommonPolygenicScore> aggregationStats(PolygenicScoreQuery query) {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public CellBaseDataResult<CommonPolygenicScore> groupBy(PolygenicScoreQuery query) throws CellBaseException {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(PolygenicScoreQuery query) throws CellBaseException {
        Bson bsonDocument = parseQuery(query);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument, String.class));
    }

    @Override
    public List<CellBaseDataResult<CommonPolygenicScore>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey) throws CellBaseException {
        List<CellBaseDataResult<CommonPolygenicScore>> results = new ArrayList<>();
        Bson projection = getProjection(queryOptions);
        for (String id : ids) {
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            orBsonList.add(Filters.eq("name", id));
            Bson query = Filters.or(orBsonList);
            MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
            results.add(new CellBaseDataResult<>(mongoDBCollection.find(query, projection, CONVERTER, new QueryOptions())));
        }
        return results;
    }

    public Bson parseQuery(PolygenicScoreQuery pharmaQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        try {
            for (Map.Entry<String, Object> entry : pharmaQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "token":
                    case "apiKey":
                    case "dataRelease":
                        // do nothing
                        break;
                    default:
                        createAndOrQuery(value, dotNotationName, QueryParam.Type.STRING, andBsonList);
                        break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        logger.debug("PolygenicScoreQuery parsed query: {}", andBsonList);
        if (CollectionUtils.isNotEmpty(andBsonList)) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }
}
