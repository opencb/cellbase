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

package org.opencb.cellbase.core.db.api.variation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.db.FeatureDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by antonior on 11/18/14.
 */
public interface ClinicalDBAdaptor extends FeatureDBAdaptor {

    QueryResult getAll(QueryOptions options);

//    QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);
//
//    QueryResult getAllByPosition(Position position, QueryOptions options);
//
//    List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);

    QueryResult getAllByGenomicVariant(Variant variant, QueryOptions options);

    List<QueryResult> getAllByGenomicVariantList(List<Variant> variantList, QueryOptions options);

    QueryResult getListClinvarAccessions(QueryOptions queryOptions);

//    public QueryResult getChromosomeById(String id, QueryOptions options);

//    QueryResult getAllClinvar(QueryOptions options);

    QueryResult getByGeneId(String geneId, QueryOptions queryOptions);

    QueryResult updateAnnotations(List<VariantAnnotation> variantAnnotations, QueryOptions queryOptions);

    List<QueryResult> getPhenotypeGeneRelations(QueryOptions queryOptions);

}
