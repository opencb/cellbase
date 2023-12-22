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
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.SpliceScore;
import org.opencb.biodata.models.core.SpliceScoreAlternate;
import org.opencb.biodata.models.core.pgs.VariantPolygenicScore;
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
import org.opencb.cellbase.core.api.PolygenicScoreQuery;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.mongodb.MongoDBCollection;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PolygenicScoreMongoDBAdaptor extends CellBaseDBAdaptor
        implements CellBaseCoreDBAdaptor<PolygenicScoreQuery, VariantPolygenicScore> {

    public PolygenicScoreMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        init();
    }

    private void init() {
        logger.debug("SpliceScoreMongoDBAdaptor: in 'constructor'");

        mongoDBCollectionByRelease = buildCollectionByReleaseMap(EtlCommons.SPLICE_SCORE_DATA);
    }

    public CellBaseDataResult<VariantPolygenicScore> getScores(String chromosome, int position, String reference, String alternate)
            throws CellBaseException {
        return getScores(chromosome, position, reference, alternate, 0);
    }

    public CellBaseDataResult<VariantPolygenicScore> getScores(String chromosome, int position, String reference, String alternate, int dataRelease)
            throws CellBaseException {
        long dbTimeStart = System.currentTimeMillis();

//        String ref = StringUtils.isEmpty(reference) ? "-" : reference;
//        String alt = StringUtils.isEmpty(alternate) ? "-" : alternate;
//        List<Bson> andBsonList = new ArrayList<>();
//        andBsonList.add(Filters.eq("chromosome", chromosome));
//        andBsonList.add(Filters.eq("position", position));
//        andBsonList.add(Filters.eq("refAllele", ref));
//        Bson query = Filters.and(andBsonList);
////        System.out.println("\t\tgetScores >>>>>>> " + query);
//
//        final String id = chromosome + ":" + position + ":" + ref + ":" + alt;
//
//        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, dataRelease);
//        DataResult<SpliceScore> spliceScoreDataResult = mongoDBCollection.find(query, null, SpliceScore.class, new QueryOptions());
//
//        List<SpliceScore> results = new ArrayList<>();
//
//        // Search for the right splice score
//        if (spliceScoreDataResult.getNumResults() > 0) {
////            System.out.println("\t\tgetScores >>>>>>> num. results = " + spliceScoreDataResult.getNumResults());
//            for (SpliceScore score : spliceScoreDataResult.getResults()) {
//                for (SpliceScoreAlternate scoreAlternate : score.getAlternates()) {
//                    if (alt.equals(scoreAlternate.getAltAllele())) {
//                        score.setAlternates(Collections.singletonList(scoreAlternate));
////                        System.out.println("\t\t\t\tgetScores, MATCH (" + score.getSource() + "): " + alt + " vs "
////                                + scoreAlternate.getAltAllele());
//                        results.add(score);
//                    }
//                }
//            }
//        }
//        int dbTime = Long.valueOf(System.currentTimeMillis() - dbTimeStart).intValue();
//        return new CellBaseDataResult<>(id, dbTime, new ArrayList<>(), results.size(), results, results.size());
        return null;
    }

    @Override
    public CellBaseIterator<VariantPolygenicScore> iterator(PolygenicScoreQuery query) throws CellBaseException {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public CellBaseDataResult<VariantPolygenicScore> aggregationStats(PolygenicScoreQuery query) {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public CellBaseDataResult<VariantPolygenicScore> groupBy(PolygenicScoreQuery query) throws CellBaseException {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public CellBaseDataResult<String> distinct(PolygenicScoreQuery query) throws CellBaseException {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public List<CellBaseDataResult<VariantPolygenicScore>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease, String apiKey) throws CellBaseException {
        logger.error("Not implemented yet");
        return null;
    }
}
