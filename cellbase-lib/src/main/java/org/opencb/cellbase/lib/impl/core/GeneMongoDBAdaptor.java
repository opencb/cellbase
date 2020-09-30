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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.biodata.models.core.Gene;
import org.opencb.cellbase.core.api.core.CellBaseCoreDBAdaptor;
import org.opencb.cellbase.core.api.core.VariantDBAdaptor;
import org.opencb.cellbase.core.api.queries.*;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.CellBaseMongoDBIterator;
import org.opencb.cellbase.lib.MongoDBCollectionConfiguration;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryParam;
import org.opencb.commons.datastore.mongodb.*;

import java.util.*;

/**
 * Created by imedina on 25/11/15.
 */
public class GeneMongoDBAdaptor extends MongoDBAdaptor implements CellBaseCoreDBAdaptor<GeneQuery, Gene> {

    private static final Set<String> CONSTRAINT_NAMES = new HashSet();
    private MongoDBCollection refseqCollection = null;

    public GeneMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("gene");
        refseqCollection = mongoDataStore.getCollection("refseq");
        logger.debug("GeneMongoDBAdaptor: in 'constructor'" + mongoDBCollection.count());
    }

    @Override
    public CellBaseDataResult<Gene> aggregationStats(GeneQuery query) {
        return null;
    }

    @Override
    public List<CellBaseDataResult<Gene>> info(List<String> ids, ProjectionQueryOptions queryOptions) {
        return info(ids, queryOptions, null);
    }

    public List<CellBaseDataResult<Gene>> info(List<String> ids, ProjectionQueryOptions queryOptions, String source) {
        List<CellBaseDataResult<Gene>> results = new ArrayList<>();
        for (String id : ids) {
            Bson projection = getProjection(queryOptions);
            List<Bson> orBsonList = new ArrayList<>(ids.size());
            orBsonList.add(Filters.eq("id", id));
            orBsonList.add(Filters.eq("name", id));
            Bson bson = Filters.or(orBsonList);
            if (StringUtils.isNotEmpty(source) && VariantDBAdaptor.QueryParams.REFSEQ.key().equalsIgnoreCase(source)) {
                results.add(new CellBaseDataResult<Gene>(refseqCollection.find(bson, projection, Gene.class, new QueryOptions())));
            } else {
                results.add(new CellBaseDataResult<Gene>(mongoDBCollection.find(bson, projection, Gene.class, new QueryOptions())));
            }
        }
        return results;
    }

    @Override
    public CellBaseIterator<Gene> iterator(GeneQuery query) {
        Bson bson = parseQuery(query);
        QueryOptions queryOptions = query.toQueryOptions();
        Bson projection = getProjection(query);
        GenericDocumentComplexConverter<Gene> converter = new GenericDocumentComplexConverter<>(Gene.class);
        MongoDBIterator<Gene> iterator = null;
        if (query.getSource() != null && !query.getSource().isEmpty() && VariantDBAdaptor.QueryParams.REFSEQ.key()
                .equalsIgnoreCase(query.getSource().get(0))) {
            iterator = refseqCollection.iterator(null, bson, projection, converter, queryOptions);
        } else {
            iterator = mongoDBCollection.iterator(null, bson, projection, converter, queryOptions);
        }
        return new CellBaseMongoDBIterator<>(iterator);
    }

    @Override
    public CellBaseDataResult<String> distinct(GeneQuery geneQuery) {
        Bson bsonDocument = parseQuery(geneQuery);
        return new CellBaseDataResult<>(mongoDBCollection.distinct(geneQuery.getFacet(), bsonDocument));
    }

    @Override
    public CellBaseDataResult groupBy(GeneQuery geneQuery) {
        Bson bsonQuery = parseQuery(geneQuery);
        logger.info("geneQuery: {}", bsonQuery.toBsonDocument(Document.class, MongoClient.getDefaultCodecRegistry()) .toJson());
        return groupBy(bsonQuery, geneQuery, "name");
    }

    public Bson parseQuery(GeneQuery geneQuery) {
        List<Bson> andBsonList = new ArrayList<>();
        boolean visited = false;
        try {
            for (Map.Entry<String, Object> entry : geneQuery.toObjectMap().entrySet()) {
                String dotNotationName = entry.getKey();
                Object value = entry.getValue();
                switch (dotNotationName) {
                    case "id":
                    case "region":
                        if (!visited) {
                            createIdRegionQuery(geneQuery.getRegions(), geneQuery.getIds(),
                                    MongoDBCollectionConfiguration.GENE_CHUNK_SIZE, andBsonList);
                            visited = true;
                        }
                        break;
                    case "transcripts.id":
                        createTranscriptIdQuery(value, andBsonList);
                        break;
                    case "transcripts.flags":
                        // TODO use unwind to filter out unwanted transcripts
                        createAndOrQuery(value, "transcripts.flags", QueryParam.Type.STRING, andBsonList);
                        break;
                    case "annotation.expression.tissue":
                        createExpressionQuery(geneQuery, andBsonList);
                        break;
                    case "annotation.expression.value":
                        // don't do anything, this value is parsed with the expression tissue
                        break;
                    case "constraints":
                        createConstraintsQuery(geneQuery, andBsonList);
                        break;
                    case "annotation.mirnaTargets":
                        createTargetQuery(value, andBsonList);
                        break;
                    case "ontology":
                        createOntologyQuery(value, andBsonList);
                        break;
                    case "disease":
                        createDiseaseQuery(value, andBsonList);
                        break;
                    case "mirna":
                        createMirnaQuery(value, andBsonList);
                        break;
                    case "source":
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

        logger.debug("gene parsed query: " + andBsonList.toString());
        if (andBsonList.size() > 0) {
            return Filters.and(andBsonList);
        } else {
            return new Document();
        }
    }

    private void createTranscriptIdQuery(Object value, List<Bson> andBsonList) {
        if (value != null) {
            String transcriptId = String.valueOf(value);
            if (transcriptId.contains("\\.")) {
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

    static {
        CONSTRAINT_NAMES.add("exac_oe_lof");
        CONSTRAINT_NAMES.add("exac_pLI");
        CONSTRAINT_NAMES.add("oe_lof");
        CONSTRAINT_NAMES.add("oe_mis");
        CONSTRAINT_NAMES.add("oe_syn");
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
