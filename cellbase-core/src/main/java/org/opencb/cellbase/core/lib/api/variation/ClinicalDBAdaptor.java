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

package org.opencb.cellbase.core.lib.api.variation;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by antonior on 11/18/14.
 */
public interface ClinicalDBAdaptor {

    public QueryResult getAll(QueryOptions options);

    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

    public QueryResult getAllByPosition(Position position, QueryOptions options);

    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);

    public QueryResult getAllByGenomicVariant(GenomicVariant variant, QueryOptions options);

    public List<QueryResult> getAllByGenomicVariantList(List<GenomicVariant> variantList, QueryOptions options);

    public QueryResult getListClinvarAccessions(QueryOptions queryOptions);

//    public QueryResult getById(String id, QueryOptions options);

    public QueryResult getAllClinvar(QueryOptions options);

    public QueryResult updateAnnotations(List<VariantAnnotation> variantAnnotations, QueryOptions queryOptions);

    public List<QueryResult> getPhenotypeGeneRelations(QueryOptions queryOptions);

}
