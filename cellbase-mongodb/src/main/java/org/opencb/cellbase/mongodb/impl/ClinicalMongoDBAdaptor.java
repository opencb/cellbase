/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.mongodb.impl;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.core.common.clinical.ClinicalVariant;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by imedina on 01/12/15.
 */
public class ClinicalMongoDBAdaptor extends MongoDBAdaptor implements ClinicalDBAdaptor<ClinicalVariant> {

    private static final Set<String> noFilteringQueryParameters = new HashSet<>(Arrays.asList("assembly", "include", "exclude",
            "skip", "limit", "of", "count", "json"));
    private static final String clinvarInclude = "clinvar";
    private static final String cosmicInclude = "cosmic";
    private static final String gwasInclude = "gwas";


    public ClinicalMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("clinical");

        logger.debug("ClinicalMongoDBAdaptor: in 'constructor'");
    }

    @Override
    public QueryResult<ClinicalVariant> next(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeNext(Query query, QueryOptions options) {
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
    public QueryResult groupBy(Query query, List<String> fields, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getIntervalFrequencies(Query query, int intervalSize, QueryOptions options) {
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
    public QueryResult<ClinicalVariant> get(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult nativeGet(Query query, QueryOptions options) {
        Bson bson = parseQuery(query);
        return mongoDBCollection.find(bson, options);
    }

    @Override
    public Iterator<ClinicalVariant> iterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public Iterator nativeIterator(Query query, QueryOptions options) {
        return null;
    }

    @Override
    public void forEach(Query query, Consumer<? super Object> action, QueryOptions options) {

    }

    private Bson parseQuery(Query query) {
        Bson filtersBson = null;

        // No filtering parameters mean all records
        if (query.size()>0) {
//        if (filteringOptionsEnabled(query)) {
            Bson commonFiltersBson = getCommonFilters(query);
            List<Bson> sourceSpecificFilterList = new ArrayList<>();
            Set<String> sourceContent = query.getAsStringList(QueryParams.SOURCE.key()) != null
                    ? new HashSet<>(query.getAsStringList(QueryParams.SOURCE.key())) : null;
            if (sourceContent == null || sourceContent.isEmpty() || sourceContent.contains(clinvarInclude)) {
                sourceSpecificFilterList.add(getClinvarFilters(query));
            }
            if (sourceContent == null || sourceContent.isEmpty() || sourceContent.contains(cosmicInclude)) {
                sourceSpecificFilterList.add(getCosmicFilters(query));
            }
            if (sourceContent == null || sourceContent.isEmpty() || sourceContent.contains(gwasInclude)) {
                sourceSpecificFilterList.add(getGwasFilters(query));
            }
            if (sourceSpecificFilterList.size() > 0 && commonFiltersBson != null) {
                List<Bson> filtersBsonList = new ArrayList<>();
                filtersBsonList.add(commonFiltersBson);
                filtersBsonList.add(Filters.or(sourceSpecificFilterList));
                filtersBson = Filters.and(filtersBsonList);
            } else if (commonFiltersBson != null) {
                filtersBson = commonFiltersBson;
            } else if (sourceSpecificFilterList.size() > 0) {
                filtersBson = Filters.or(sourceSpecificFilterList);
            }
        }

        if (filtersBson != null) {
            return filtersBson;
        } else {
            return new Document();
        }

    }

    private Bson getGwasFilters(Query query) {
        return Filters.eq("source", "gwas");
    }

    private Bson getCosmicFilters(Query query) {
        return Filters.eq("source", "cosmic");
    }

    private Bson getClinvarFilters(Query query) {
        List<Bson> andBsonList = new ArrayList<>();

        andBsonList.add(Filters.eq("source", "clinvar"));
        createOrQuery(query, QueryParams.CLINVARRCV.key(), "clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                andBsonList);
        createClinvarRsQuery(query, andBsonList);
        createClinvarTypeQuery(query, andBsonList);
        createClinvarReviewQuery(query, andBsonList);
        createClinvarClinicalSignificanceQuery(query, andBsonList);

        if (andBsonList.size() == 1) {
            return andBsonList.get(0);
        } else if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return null;
        }

    }

    private void createClinvarClinicalSignificanceQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARCLINSIG.key()) != null
                && !query.getString(QueryParams.CLINVARCLINSIG.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARCLINSIG.key()).stream()
                            .map((clinicalSignificanceString) -> clinicalSignificanceString.replace("_", " "))
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.clinicalSignificance.description",
                    andBsonList);
        }
    }

    private void createClinvarReviewQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARREVIEW.key()) != null
                && !query.getString(QueryParams.CLINVARREVIEW.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARREVIEW.key()).stream()
                            .map((reviewString) -> reviewString.toUpperCase())
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus",
                    andBsonList);
        }
    }

    private void createClinvarTypeQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARTYPE.key()) != null
                && !query.getString(QueryParams.CLINVARTYPE.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARTYPE.key()).stream()
                    .map((typeString) -> typeString.replace("_", " "))
                    .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.measureSet.measure.type",
                    andBsonList);
        }
    }

    private void createClinvarRsQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARRS.key()) != null
                && !query.getString(QueryParams.CLINVARRS.key()).isEmpty()) {
            List<String> queryList = query.getAsStringList(QueryParams.CLINVARRS.key());
            if (queryList.size() == 1) {
                andBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                        queryList.get(0).substring(2)));
                andBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type", "rs"));
            } else {
                List<Bson> orBsonList = new ArrayList<>(queryList.size());
                for (String queryItem : queryList) {
                    List<Bson> innerAndBsonList = new ArrayList<>();
                    innerAndBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.id",
                            queryList.get(0).substring(2)));
                    innerAndBsonList.add(Filters.eq("clinvarSet.referenceClinVarAssertion.measureSet.measure.xref.type", "rs"));
                    orBsonList.add(Filters.and(innerAndBsonList));
                }
                andBsonList.add(Filters.or(orBsonList));
            }
        }
    }

    private Bson getCommonFilters(Query query) {
        List<Bson> andBsonList = new ArrayList<>();
        createRegionQuery(query, QueryParams.REGION.key(), andBsonList);

        createOrQuery(query, QueryParams.SO.key(), "annot.consequenceTypes.soTerms.soName", andBsonList);
        createOrQuery(query, QueryParams.GENE.key(), "_geneIds", andBsonList);
        createOrQuery(query, QueryParams.PHENOTYPE.key(), "_phenotypes", andBsonList);

        if (andBsonList.size() == 1) {
            return andBsonList.get(0);
        } else if (andBsonList.size() > 1) {
            return Filters.and(andBsonList);
        } else {
            return null;
        }
    }

    private void addIfNotNull(List<Bson> bsonList, Bson bson) {
        if (bson != null) {
            bsonList.add(bson);
        }
    }

    private boolean filteringOptionsEnabled(Query query) {
        int i = 0;
        Object[] keys = query.keySet().toArray();
        while ((i < query.size()) && noFilteringQueryParameters.contains(keys[i])) {
            i++;
        }
        return (i < query.size());
    }

}
