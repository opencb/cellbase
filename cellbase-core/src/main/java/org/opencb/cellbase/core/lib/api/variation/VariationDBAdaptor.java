package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import javax.management.Query;
import java.util.List;


public interface VariationDBAdaptor {

	
    public QueryResult getById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult getAllConsequenceTypes(QueryOptions options);


    public QueryResult getByTranscriptId(String id, QueryOptions options);

    public List<QueryResult> getAllByTranscriptIdList(List<String> idList, QueryOptions options);



    public QueryResult getAllPhenotypes(QueryOptions options);

    public List<QueryResult> getAllPhenotypeByRegion(List<Region> regions, QueryOptions options);

    public QueryResult getAllByPhenotype(String phenotype, QueryOptions options);

    public List<QueryResult> getAllByPhenotypeList(List<String> phenotypeList, QueryOptions options);

    public QueryResult getAllGenesByPhenotype(String phenotype, QueryOptions options);

    public List<QueryResult> getAllGenesByPhenotypeList(List<String> phenotypeList, QueryOptions options);


    
	public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

	public QueryResult getAllByPosition(Position position, QueryOptions options);
	
	public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);
	

	public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);
    
    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);

    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);

    public List<QueryResult> getIdByVariantList(List<GenomicVariant> variations, QueryOptions options);

    public List<QueryResult> getAllByVariantList(List<GenomicVariant> variations, QueryOptions options);

}
