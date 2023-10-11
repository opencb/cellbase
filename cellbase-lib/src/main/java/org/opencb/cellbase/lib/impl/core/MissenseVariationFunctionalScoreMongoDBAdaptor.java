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
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.MissenseVariantFunctionalScore;
import org.opencb.biodata.models.core.TranscriptMissenseVariantFunctionalScore;
import org.opencb.cellbase.core.api.query.CellBaseQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.variant.VariantAnnotationUtils;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissenseVariationFunctionalScoreMongoDBAdaptor extends CellBaseDBAdaptor {


    public MissenseVariationFunctionalScoreMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("MissenseVariationFunctionalScoreMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap("missense_variation_functional_score");
    }

    public CellBaseDataResult<MissenseVariantFunctionalScore> query(String chromosome, int position, String reference, int dataRelease)
            throws CellBaseException {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.eq("position", position));
        andBsonList.add(Filters.eq("reference", reference));
        Bson query = Filters.and(andBsonList);

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        return new CellBaseDataResult<>(mongoDBCollection.find(query, null, MissenseVariantFunctionalScore.class, new QueryOptions()));

    }

    public CellBaseDataResult<TranscriptMissenseVariantFunctionalScore> getScores(String chromosome, int position, String reference,
                                                                                  String alternate, String aaReference,
                                                                                  String aaAlternate, int dataRelease)
            throws CellBaseException {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.eq("position", position));
        andBsonList.add(Filters.eq("reference", reference));
        Bson query = Filters.and(andBsonList);

        final String id = chromosome + ":" + position + ":" + reference + ":" + alternate;

        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
        DataResult<MissenseVariantFunctionalScore> missenseVariantFunctionalScoreDataResult =
                mongoDBCollection.find(query, null, MissenseVariantFunctionalScore.class, new QueryOptions());

        // Search for the right aa change
        String aaReferenceAbbreviation = VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aaReference);
        String aaAlternateAbbreviation = VariantAnnotationUtils.TO_ABBREVIATED_AA.get(aaAlternate);
        if (missenseVariantFunctionalScoreDataResult.getNumResults() > 0) {
            for (MissenseVariantFunctionalScore score : missenseVariantFunctionalScoreDataResult.getResults()) {
                for (TranscriptMissenseVariantFunctionalScore transcriptScore : score.getScores()) {
                    if (transcriptScore.getAaReference().equalsIgnoreCase(aaReferenceAbbreviation)
                            && transcriptScore.getAaAlternate().equalsIgnoreCase(aaAlternateAbbreviation)) {
                        return new CellBaseDataResult<>(id, -1, new ArrayList<>(), 1,
                                Collections.singletonList(transcriptScore), 1);
                    }
                }
            }
        }
        return new CellBaseDataResult<>(id, -1, new ArrayList<>(), 0, null, 0);
    }

    public CellBaseDataResult<MissenseVariantFunctionalScore> getScores(String chromosome, List<Integer> positions,
                                                                        CellBaseQueryOptions options, int dataRelease)
            throws CellBaseException {
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);

        Bson projection = getProjection(options);

        List<Bson> orBsonList = new ArrayList<>();
        for (int position : positions) {
            orBsonList.add(Filters.eq("position", position));
        }

        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.or(orBsonList));
        Bson query = Filters.and(andBsonList);

        return new CellBaseDataResult<>(mongoDBCollection.find(query, projection, MissenseVariantFunctionalScore.class,
                new QueryOptions()));
    }
}
