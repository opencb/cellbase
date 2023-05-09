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
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
import org.opencb.cellbase.core.api.query.LogicalList;
import org.opencb.cellbase.core.api.query.ProjectionQueryOptions;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.iterator.CellBaseMongoDBIterator;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jtarraga on 9/4/23.
 */
public class PharmacogenomicsMongoDBAdaptor extends CellBaseDBAdaptor
        implements CellBaseCoreDBAdaptor<PharmaChemicalQuery, PharmaChemical> {

    private static final GenericDocumentComplexConverter<PharmaChemical> CONVERTER;

    static {
        CONVERTER = new GenericDocumentComplexConverter<>(PharmaChemical.class);
    }

    public PharmacogenomicsMongoDBAdaptor(MongoDataStore mongoDataStore) {
        super(mongoDataStore);

        this.init();
    }

    private void init() {
        mongoDBCollectionByRelease = buildCollectionByReleaseMap("pharmacogenomics");

        logger.debug("PharmacogenomicsMongoDBAdaptor initialised");
    }

    @Override
    public CellBaseDataResult<PharmaChemical> aggregationStats(PharmaChemicalQuery query) {
        logger.error("Not implemented yet");
        return null;
    }

    @Override
    public List<CellBaseDataResult<PharmaChemical>> info(List<String> ids, ProjectionQueryOptions queryOptions, int dataRelease,
                                                         String token) throws CellBaseException {
        List<CellBaseDataResult<PharmaChemical>> results = new ArrayList<>();
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

    @Override
    public CellBaseIterator<PharmaChemical> iterator(PharmaChemicalQuery query) throws CellBaseException {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        MongoDBIterator<PharmaChemical> iterator;
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        iterator = mongoDBCollection.iterator(null, bson, projection, CONVERTER, queryOptions);
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<String> distinct(PharmaChemicalQuery query) throws CellBaseException {
        Bson bsonDocument = parseQuery(query);
        MongoDBCollection mongoDBCollection = getCollectionByRelease(mongoDBCollectionByRelease, query.getDataRelease());
        return new CellBaseDataResult<>(mongoDBCollection.distinct(query.getFacet(), bsonDocument, String.class));
    }

    @Override
    public CellBaseDataResult<PharmaChemical> groupBy(PharmaChemicalQuery query) throws CellBaseException {
        throw new CellBaseException("Not implemented yet");
    }

    public Bson parseQuery(PharmaChemicalQuery pharmaQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        boolean visited = false;
        try {
            for (Map.Entry<String, Object> entry : pharmaQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
//                    case "transcripts.id":
//                        createTranscriptIdQuery(value, andBsonList);
//                        break;
//                    case "transcripts.flags":
//                        // TODO use unwind to filter out unwanted transcripts
//                        createAndOrQuery(value, "transcripts.flags", QueryParam.Type.STRING, andBsonList);
//                        break;
//                    case "annotation.expression.value":
//                        // don't do anything, this value is parsed with the expression tissue
//                        break;
//                    case "annotation.mirnaTargets":
//                        createTargetQuery(value, andBsonList);
//                        break;
//                    case "ontology":
//                        createOntologyQuery(value, andBsonList);
//                        break;
//                    case "disease":
//                        createDiseaseQuery(value, andBsonList);
//                        break;
//                    case "mirna":
//                        createMirnaQuery(value, andBsonList);
//                        break;
//                    case "source":
                    case "dataRelease":
                    case "token":
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
        logger.debug("pharmacogenomics parsed query: " + andBsonList);
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createTranscriptIdQuery(Object value, List<Bson> andBsonList) {
        if (value != null) {
            String transcriptId = String.valueOf(value);
            if (transcriptId.contains(".")) {
                // transcript contains version, e.g. ENST00000671466.1
                andBsonList.add(Filters.eq("transcripts.id", transcriptId));
            } else {
                // transcript does not contain version, do a fuzzy query so that ENST00000671466 will match ENST00000671466.1
                andBsonList.add(Filters.regex("transcripts.id", "^" + transcriptId + "\\."));
            }
        }
    }

    // check in both the id and accession field.
    private void createMirnaQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "mirna.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "mirna.accession"));
            orBsonList.add(getLogicalListFilter(queryValues, "mirna.matures.id"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    // check in both the id and accession field.
    private void createDiseaseQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.diseases.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.diseases.name"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    // check in both the id and sourceId field.
    private void createTargetQuery(Object queryValues, List<Bson> andBsonList) {
        if (queryValues != null) {
            List<Bson> orBsonList = new ArrayList<>();
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.mirnaTargets.id"));
            orBsonList.add(getLogicalListFilter(queryValues, "annotation.mirnaTargets.sourceId"));
            andBsonList.add(Filters.or(orBsonList));
        }
    }

    private void createConstraintsQuery(GeneQuery geneQuery, List<Bson> andBsonList) {
        if (geneQuery != null && geneQuery.getAnnotationConstraints() != null) {
            LogicalList<String> paramValues = geneQuery.getAnnotationConstraints();
            // copied from mongoqueryutils. matches all valid operators
            String legalOperators = "(<=?|>=?|!=|!?=?~|==?)";
            // get the constraint name and the numerical value, look behind and look ahead
            String delimiter = "((?<=" + legalOperators + ")|(?=" + legalOperators + "))";

            // exac_oe_lof<=1.0,oe_lof<0.85585
            for (String paramValue : paramValues) {
                String[] expressions = paramValue.split(",");
                // oe_lof<0.85585
                for (String expression : expressions) {
                    final String[] expressionParts = expression.split(delimiter);
                    if (expressionParts.length >= 3) {
                        String constraintName = expressionParts[0];
                        String operator = expressionParts[1];
                        String numericalValue = expressionParts[2];
                        if (expressionParts.length == 4) {
                            numericalValue += expressionParts[3];
                        }
                        Query query = new Query("value", operator + numericalValue);
                        Bson valueFilter = MongoDBQueryUtils.createAutoFilter("value",
                                "value", query, QueryParam.Type.DECIMAL,
                                MongoDBQueryUtils.LogicalOperator.OR);

                        andBsonList.add(Filters.elemMatch("annotation.constraints",
                                Filters.and(Filters.eq("name", constraintName), valueFilter)));
                    }
                }
            }
        }
    }

    private void createExpressionQuery(GeneQuery geneQuery, List<Bson> andBsonList) {
        if (geneQuery != null) {
            List<String> tissues = geneQuery.getAnnotationExpressionTissue();
            if (CollectionUtils.isNotEmpty(tissues)) {
                List<String> expressionValues = geneQuery.getAnnotationExpressionValue();
                if (CollectionUtils.isNotEmpty(expressionValues)) {
                    MongoDBQueryUtils.LogicalOperator tissuesOperator = ((LogicalList) tissues).isAnd()
                            ? MongoDBQueryUtils.LogicalOperator.AND
                            : MongoDBQueryUtils.LogicalOperator.OR;
                    List<Bson> orBsonList = new ArrayList<>();
                    for (String tissue : tissues) {
                        for (String expressionValue : expressionValues) {
                            if (tissuesOperator.equals(MongoDBQueryUtils.LogicalOperator.AND)) {
                                andBsonList.add(Filters.elemMatch("annotation.expression",
                                        Filters.and(Filters.regex("factorValue", "(.)*" + tissue
                                                        + "(.)*", "i"),
                                                Filters.eq("expression", expressionValue))));
                            } else {
                                orBsonList.add(Filters.elemMatch("annotation.expression",
                                        Filters.and(Filters.regex("factorValue", "(.)*" + tissue
                                                        + "(.)*", "i"),
                                                Filters.eq("expression", expressionValue))));
                            }
                        }
                        if (!orBsonList.isEmpty()) {
                            andBsonList.add(Filters.or(orBsonList));
                        }
                    }
                }
            }
        }
    }
}
