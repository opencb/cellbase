package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.api.ConservedRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.ProteinFunctionPredictorDBAdaptor;
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

    public VariantDiseaseAssociationDBAdaptor getVariantDiseaseAssociationDBAdaptor();

    public void setVariantDiseaseAssociationDBAdaptor(VariantDiseaseAssociationDBAdaptor variantDiseaseAssociationDBAdaptor);

    public ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor();

    public void setProteinFunctionPredictorDBAdaptor(ProteinFunctionPredictorDBAdaptor proteinFunctionPredictorDBAdaptor);

    public void setConservedRegionDBAdaptor(ConservedRegionDBAdaptor conservedRegionDBAdaptor);

}
