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
import org.opencb.cellbase.core.db.api.core.ConservedRegionDBAdaptor;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.core.db.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by imedina on 11/07/14.
 */
public interface VariantAnnotationDBAdaptor {


    QueryResult getAllConsequenceTypesByVariant(Variant variant, QueryOptions options);

    List<QueryResult> getAllConsequenceTypesByVariantList(List<Variant> variants, QueryOptions options);

    QueryResult getAnnotationByVariant(Variant variant, QueryOptions queryOptions);

    List<QueryResult> getAnnotationByVariantList(List<Variant> variantList, QueryOptions queryOptions);

    void setVariationDBAdaptor(VariationDBAdaptor variationDBAdaptor);

    void setVariantClinicalDBAdaptor(ClinicalDBAdaptor clinicalDBAdaptor);

    void setProteinDBAdaptor(ProteinDBAdaptor proteinFunctionPredictorDBAdaptor);

    void setConservedRegionDBAdaptor(ConservedRegionDBAdaptor conservedRegionDBAdaptor);

    void setVariantFunctionalScoreDBAdaptor(VariantFunctionalScoreDBAdaptor variantFunctionalScoreDBAdaptor);

    void setGenomeDBAdaptor(GenomeDBAdaptor genomeDBAdaptor);

    void setGeneDBAdaptor(GeneDBAdaptor geneDBAdaptor);

    void setRegulatoryRegionDBAdaptor(RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor);

}
