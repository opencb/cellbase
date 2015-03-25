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
