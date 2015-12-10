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

    private static final Set<String> NO_FILTERING_QUERY_PARAMETERS = new HashSet<>(Arrays.asList(
            "assembly", "include", "exclude", "skip", "limit", "of", "count", "json"));
    private static final String CLINVAR_INCLUDE = "clinvar";
    private static final String COSMIC_INCLUDE = "cosmic";
    private static final String GWAS_INCLUDE = "gwas";


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
        if (query.size() > 0) {
//        if (filteringOptionsEnabled(query)) {
            Bson commonFiltersBson = getCommonFilters(query);
            Set<String> sourceContent = query.getAsStringList(QueryParams.SOURCE.key()) != null
                    ? new HashSet<>(query.getAsStringList(QueryParams.SOURCE.key())) : null;
            List<Bson> sourceSpecificFilterList = new ArrayList<>();
            getClinvarFilters(query, sourceContent, sourceSpecificFilterList);
            getCosmicFilters(query, sourceContent, sourceSpecificFilterList);
            getGwasFilters(query, sourceContent, sourceSpecificFilterList);

//            sourceSpecificFilterList.add(getClinvarFilters(query, sourceContent));
//            sourceSpecificFilterList.add(getCosmicFilters(query, sourceContent));
//            sourceSpecificFilterList.add(getGwasFilters(query, sourceContent));

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

    private void getGwasFilters(Query query, Set<String> sourceContent, List<Bson> sourceBson) {
        // If only clinvar-specific filters are provided it must be avoided to include the source=gwas condition since
        // sourceBson is going to be an OR list
        if (!(query.containsKey(QueryParams.CLINVARRCV.key()) || query.containsKey(QueryParams.CLINVARCLINSIG.key())
                || query.containsKey(QueryParams.CLINVARREVIEW.key())
                || query.containsKey(QueryParams.CLINVARTYPE.key())
                || query.containsKey(QueryParams.CLINVARRS.key()))) {
            if (sourceContent != null && sourceContent.contains("gwas")) {
                sourceBson.add(Filters.eq("source", "gwas"));
            }
        }
    }

    private void getCosmicFilters(Query query, Set<String> sourceContent, List<Bson> sourceBson) {
        // If only clinvar-specific filters are provided it must be avoided to include the source=cosmic condition since
        // sourceBson is going to be an OR list
        if (!(query.containsKey(QueryParams.CLINVARRCV.key()) || query.containsKey(QueryParams.CLINVARCLINSIG.key())
                || query.containsKey(QueryParams.CLINVARREVIEW.key())
                || query.containsKey(QueryParams.CLINVARTYPE.key())
                || query.containsKey(QueryParams.CLINVARRS.key()))) {
            if (sourceContent != null && sourceContent.contains("cosmic")) {
                sourceBson.add(Filters.eq("source", "cosmic"));
            }
        }
    }

    private void getClinvarFilters(Query query, Set<String> sourceContent, List<Bson> sourceBson) {
        List<Bson> andBsonList = new ArrayList<>();

        if (sourceContent != null && sourceContent.contains("clinvar")) {
            andBsonList.add(Filters.eq("source", "clinvar"));
        }

        createOrQuery(query, QueryParams.CLINVARRCV.key(), "clinvarSet.referenceClinVarAssertion.clinVarAccession.acc",
                andBsonList);
        createClinvarRsQuery(query, andBsonList);
        createClinvarTypeQuery(query, andBsonList);
        createClinvarReviewQuery(query, andBsonList);
        createClinvarClinicalSignificanceQuery(query, andBsonList);

        if (andBsonList.size() == 1) {
            sourceBson.add(andBsonList.get(0));
        } else if (andBsonList.size() > 0) {
            sourceBson.add(Filters.and(andBsonList));
        }
    }

    private void createClinvarClinicalSignificanceQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARCLINSIG.key()) != null
                && !query.getString(QueryParams.CLINVARCLINSIG.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARCLINSIG.key()).stream()
                            .map((clinicalSignificanceString) -> clinicalSignificanceString.replace("_", " "))
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.clinicalSignificance.description", andBsonList);
        }
    }

    private void createClinvarReviewQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARREVIEW.key()) != null
                && !query.getString(QueryParams.CLINVARREVIEW.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARREVIEW.key()).stream()
                            .map(String::toUpperCase)
                            .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus", andBsonList);
        }
    }

    private void createClinvarTypeQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.CLINVARTYPE.key()) != null
                && !query.getString(QueryParams.CLINVARTYPE.key()).isEmpty()) {
            createOrQuery(query.getAsStringList(QueryParams.CLINVARTYPE.key()).stream()
                    .map((typeString) -> typeString.replace("_", " "))
                    .collect(Collectors.toList()),
                    "clinvarSet.referenceClinVarAssertion.measureSet.measure.type", andBsonList);
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
        createPhenotypeQuery(query, andBsonList);

        if (andBsonList.size() == 1) {
            return andBsonList.get(0);
        } else if (andBsonList.size() > 1) {
            return Filters.and(andBsonList);
        } else {
            return null;
        }
    }

    private void createPhenotypeQuery(Query query, List<Bson> andBsonList) {
        if (query != null && query.getString(QueryParams.PHENOTYPE.key()) != null
                && !query.getString(QueryParams.PHENOTYPE.key()).isEmpty()) {
            andBsonList.add(Filters.text(query.getString(QueryParams.PHENOTYPE.key())));
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
        while ((i < query.size()) && NO_FILTERING_QUERY_PARAMETERS.contains(keys[i])) {
            i++;
        }
        return (i < query.size());
    }

}
