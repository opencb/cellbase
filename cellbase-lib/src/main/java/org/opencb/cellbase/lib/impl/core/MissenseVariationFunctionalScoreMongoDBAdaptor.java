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
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;

public class MissenseVariationFunctionalScoreMongoDBAdaptor extends MongoDBAdaptor {


    public MissenseVariationFunctionalScoreMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("missense_variation_functional_score");
        logger.debug("MissenseVariationFunctionalScoreMongoDBAdaptor: in 'constructor'");
    }

    public CellBaseDataResult<MissenseVariantFunctionalScore> getRevelScores(String chromosome, int position, String reference,
                                                                             String alternate) {
        List<Bson> andBsonList = new ArrayList<>();
        andBsonList.add(Filters.eq("chromosome", chromosome));
        andBsonList.add(Filters.eq("position", position));
        andBsonList.add(Filters.eq("reference", reference));
        andBsonList.add(Filters.eq("scores.alternate", alternate));
        Bson query = Filters.and(andBsonList);
        return new CellBaseDataResult<MissenseVariantFunctionalScore>(mongoDBCollection.find(query, null,
                MissenseVariantFunctionalScore.class, new QueryOptions()));

    }

}
