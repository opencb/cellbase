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

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.core.ConservedRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.core.ProteinFunctionPredictorDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;

/**
 * Created by imedina on 11/07/14.
 */
public interface VariantAnnotationDBAdaptor {

    public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options);

    public List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options);


    public QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options);

    public  List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options);

    public List<QueryResult> getAnnotationByVariantList(List<GenomicVariant> variantList, QueryOptions queryOptions);

    public VariationDBAdaptor getVariationDBAdaptor();

    public void setVariationDBAdaptor(VariationDBAdaptor variationDBAdaptor);

    public ClinicalDBAdaptor getVariantClinicalDBAdaptor();

    public void setVariantClinicalDBAdaptor(ClinicalDBAdaptor clinicalDBAdaptor);

    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor();

    public void setProteinFunctionPredictorDBAdaptor(ProteinFunctionPredictorDBAdaptor proteinFunctionPredictorDBAdaptor);

    public void setConservedRegionDBAdaptor(ConservedRegionDBAdaptor conservedRegionDBAdaptor);

    public void setGeneDBAdaptor(GeneDBAdaptor geneDBAdaptor);

    public void setRegulatoryRegionDBAdaptor(RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor);

}
