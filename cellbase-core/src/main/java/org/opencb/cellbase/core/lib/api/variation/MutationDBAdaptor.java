package org.opencb.cellbase.core.lib.api.variation;

import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

public interface MutationDBAdaptor extends FeatureDBAdaptor {

    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);


    public QueryResult getAllDiseases(QueryOptions options);

    public QueryResult getAllByDisease(String geneName, QueryOptions options);

    public List<QueryResult> getAllByDiseaseList(List<String> geneName, QueryOptions options);


    public QueryResult getByGeneName(String geneName, QueryOptions options);

    public List<QueryResult> getAllByGeneNameList(List<String> geneNameList, QueryOptions options);


    public QueryResult getByProteinId(String proteinId, QueryOptions options);

    public List<QueryResult> getAllByProteinIdList(List<String> proteinIdList, QueryOptions options);

    public QueryResult getByProteinRegion(String proteinId, int start, int end, QueryOptions options);


    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options);

    public QueryResult getAllByPosition(Position position, QueryOptions options);

    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);


    public QueryResult getAllIntervalFrequencies(Region region, QueryOptions options);

    public List<QueryResult> getAllIntervalFrequencies(List<Region> regions, QueryOptions options);


//	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByGeneName(String geneName);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByGeneNameList(List<String> geneNameList);
//
//	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByEnsemblTranscript(String ensemblTranscript);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByEnsemblTranscriptList(List<String> ensemblTranscriptList);
//
//
//	public List<MutationPhenotypeAnnotation> getAllMutationPhenotypeAnnotationByPosition(Position position);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllMutationPhenotypeAnnotationByPositionList(List<Position> position);
//
//
//
//	public List<MutationPhenotypeAnnotation> getAllByRegion(Region region);
//
//	public List<List<MutationPhenotypeAnnotation>> getAllByRegionList(List<Region> regionList);
//
//
//	public List<IntervalFeatureFrequency> getAllIntervalFrequencies(Region region, int interval);
	
}
