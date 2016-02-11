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

package org.opencb.cellbase.core.api;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.Score;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by imedina on 30/11/15.
 */
@Deprecated
public interface VariantAnnotationDBAdaptor<T> extends CellBaseDBAdaptor<T> {

    QueryResult<T> getAnnotationByVariant(Variant variant, QueryOptions options);

    List<QueryResult<T>> getAnnotationByVariantList(List<Variant> variants, QueryOptions options);


    QueryResult<ConsequenceType> getConsequenceTypes(Query query, QueryOptions options);


    QueryResult<Score> getFunctionalScore(Variant variant, QueryOptions options);

    List<QueryResult<Score>> getFunctionalScore(List<Variant> variants, QueryOptions options);

    QueryResult<Score> getFunctionalScore(Query query, QueryOptions options);

//    void setVariantClinicalDBAdaptor(ClinicalDBAdaptor clinicalDBAdaptor);
//
//    void setProteinDBAdaptor(ProteinDBAdaptor proteinDBAdaptor);
//
//    void setConservationDBAdaptor(ConservationDBAdaptor conservationDBAdaptor);
//
//    void setVariantFunctionalScoreDBAdaptor(VariantFunctionalScoreDBAdaptor variantFunctionalScoreDBAdaptor);
//
//    void setGenomeDBAdaptor(GenomeDBAdaptor genomeDBAdaptor);
//
//    void setGeneDBAdaptor(GeneDBAdaptor geneDBAdaptor);
//
//    void setRegulationDBAdaptor(RegulationDBAdaptor regulationDBAdaptor);


}
