package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.cellbase.core.common.GenomicVariant;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;


public interface VariantEffectDBAdaptor {

	
	public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options);
	
	public List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options);
	

	public QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options);
	
	public  List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options);
	
//	public List<GenomicVariantConsequenceType> getAllConsequenceTypeByVariant(GenomicVariant variant);
//	
//	public List<GenomicVariantConsequenceType> getAllConsequenceTypeByVariant(GenomicVariant variant, Set<String> excludeSet);
//
//	
//	public List<GenomicVariantConsequenceType> getAllConsequenceTypeByVariantList(List<GenomicVariant> variants);
//
//	public List<GenomicVariantConsequenceType> getAllConsequenceTypeByVariantList(List<GenomicVariant> variants, Set<String> excludeSet);

	
//	public Map<GenomicVariant, List<GenomicVariantConsequenceType>> getConsequenceTypeMap(List<GenomicVariant> variants);
//
//	public Map<GenomicVariant, List<GenomicVariantConsequenceType>> getConsequenceTypeMap(List<GenomicVariant> variants, Set<String> excludeSet);

}
