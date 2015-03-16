package org.opencb.cellbase.core.lib.api;

import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.List;


public interface ExonDBAdaptor extends FeatureDBAdaptor {


	public List<String> getAllEnsemblIds();

	public Exon getByEnsemblId(String ensemblId);
	
	public List<Exon> getAllByEnsemblIdList(List<String> ensemblIdList);
	
	public List<Exon> getByEnsemblTranscriptId(String transcriptId);
	
	public List<List<Exon>> getByEnsemblTranscriptIdList(List<String> transcriptIdList);

	public List<Exon> getByEnsemblGeneId(String geneId);
	
	public List<List<Exon>> getByEnsemblGeneIdList(List<String> geneIdList);


    public QueryResult next(String id, QueryOptions options);
	
	
	public List<Exon> getAllByPosition(String chromosome, int position);

	public List<Exon> getAllByPosition(Position position);

	public List<List<Exon>> getAllByPositionList(List<Position> positionList);
	

	public List<Exon> getAllByRegion(String chromosome);

	public List<Exon> getAllByRegion(String chromosome, int start);

	public List<Exon> getAllByRegion(String chromosome, int start, int end);
	

	public List<Exon> getAllByRegion(Region region);

	public List<List<Exon>> getAllByRegionList(List<Region> regionList);
	
	public List<Exon> getAllByCytoband(String chromosome, String cytoband);
	
	
	public List<Exon> getAllBySnpId(String snpId);
	
	public List<List<Exon>> getAllBySnpIdList(List<String> snpIdList);


	List<String> getAllSequencesByIdList(List<String> ensemblIdList, int strand);

    List<List<Exon>> getAllByName(String name, List<String> exclude);

    List<List<List<Exon>>> getAllByNameList(List<String> nameList, List<String> exclude);


//	public List<ExonToTranscript> getAllExonToTranscriptByEnsemblGeneId(String geneId);
//	
//	public List<List<ExonToTranscript>> getAllExonToTranscriptByEnsemblGeneIdList(List<String> geneIdList);
//	
//	public List<ExonToTranscript> getAllExonToTranscriptByEnsemblTranscriptId(String transcriptId);
//	
//	public List<List<ExonToTranscript>> getAllExonToTranscriptByEnsemblTranscriptIdList(List<String> transcriptIdList);
	
	
}
