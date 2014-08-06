package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created by imedina on 11/07/14.
 */
public interface VariantAnnotationDBAdaptor {

    @Deprecated
    public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options);

    @Deprecated
    public List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options);


    public QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options);

    public  List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options);

}
