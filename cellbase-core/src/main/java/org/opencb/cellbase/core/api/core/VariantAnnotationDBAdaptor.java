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

package org.opencb.cellbase.core.api.core;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.cellbase.core.result.CellBaseDataResult;

import java.util.List;

/**
 * Created by imedina on 30/11/15.
 */
@Deprecated
public interface VariantAnnotationDBAdaptor<Q, T> extends CellBaseMongoDBAdaptor<Q, T> {

    CellBaseDataResult<T> getAnnotationByVariant(Variant variant, QueryOptions options);

    List<CellBaseDataResult<T>> getAnnotationByVariantList(List<Variant> variants, QueryOptions options);


    CellBaseDataResult<ConsequenceType> getConsequenceTypes(Query query, QueryOptions options);


    CellBaseDataResult<Score> getFunctionalScore(Variant variant, QueryOptions options);

    List<CellBaseDataResult<Score>> getFunctionalScore(List<Variant> variants, QueryOptions options);

    CellBaseDataResult<Score> getFunctionalScore(Query query, QueryOptions options);

}
