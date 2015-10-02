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

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.db.api.core.ConservedRegionDBAdaptor;
import org.opencb.cellbase.core.db.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.db.api.core.GenomeDBAdaptor;
import org.opencb.cellbase.core.db.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 11/07/14.
 */
public interface VariantAnnotationDBAdaptor {


    QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options);

    List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options);


    QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options);

    List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options);

    List<QueryResult> getAnnotationByVariantList(List<GenomicVariant> variantList, QueryOptions queryOptions);

    VariationDBAdaptor getVariationDBAdaptor();

    void setVariationDBAdaptor(VariationDBAdaptor variationDBAdaptor);

    ClinicalDBAdaptor getVariantClinicalDBAdaptor();

    void setVariantClinicalDBAdaptor(ClinicalDBAdaptor clinicalDBAdaptor);

    ProteinDBAdaptor getProteinDBAdaptor();

    void setProteinDBAdaptor(ProteinDBAdaptor proteinFunctionPredictorDBAdaptor);

    void setConservedRegionDBAdaptor(ConservedRegionDBAdaptor conservedRegionDBAdaptor);

    void setGenomeDBAdaptor(GenomeDBAdaptor genomeDBAdaptor);

    void setGeneDBAdaptor(GeneDBAdaptor geneDBAdaptor);

    void setRegulatoryRegionDBAdaptor(RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor);

}
