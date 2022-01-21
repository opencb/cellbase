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
import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.core.SpliceScoreAlternate;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpliceScoreMongoDBAdaptor extends MongoDBAdaptor {

    public SpliceScoreMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("SpliceScoreMongoDBAdaptor: in 'constructor'");

        mongoDBCollection = mongoDataStore.getCollection(EtlCommons.SPLICE_SCORE_DATA);
    }

    public CellBaseDataResult<SpliceScore> query(String chromosome, int position, String reference) {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.eq("position", position));
        andBsonList.add(Filters.eq("refAllele", reference));
        Bson query = Filters.and(andBsonList);
        return new CellBaseDataResult<>(mongoDBCollection.find(query, null, SpliceScore.class, new QueryOptions()));

    }

    public CellBaseDataResult<SpliceScore> getScores(String chromosome, int position, String reference, String alternate) {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.eq("position", position));
        andBsonList.add(Filters.eq("refAllele", reference));
        Bson query = Filters.and(andBsonList);

        final String id = chromosome + ":" + position + ":" + reference + ":" + alternate;

        DataResult<SpliceScore> spliceScoreDataResult = mongoDBCollection.find(query, null, SpliceScore.class, new QueryOptions());

        // Search for the right splice score
        if (spliceScoreDataResult.getNumResults() > 0) {
            for (SpliceScore score : spliceScoreDataResult.getResults()) {
                for (SpliceScoreAlternate scoreAlternate : score.getAlternates()) {
                    if (scoreAlternate.getAltAllele().equals(alternate)) {
                        score.setAlternates(Collections.singletonList(scoreAlternate));
                        return new CellBaseDataResult<>(id, -1, new ArrayList<>(), 1, Collections.singletonList(score), 1);
                    }
                }
            }
        }
        return new CellBaseDataResult<>(id, -1, new ArrayList<>(), 0, null, 0);
    }
}
