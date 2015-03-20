package org.opencb.cellbase.core.lib.api.core;


import org.opencb.cellbase.core.common.Position;

import java.util.List;


public interface GenomicPositionDBAdaptor {

	
	public String getByPosition(String chromosome, int position);
	
	public String getByPosition(Position position);
	
	public List<String> getAllByPositionList(List<Position> positionList);

	
	public String getByPosition(Position position, List<String> sources);	// sources: gene, exon, snp, ...
	
	public List<String> getAllByPositionList(List<Position> positions, List<String> sources);
	

	public String getConsequenceTypeByPosition(String chromosome, int position);
	
	public String getConsequenceTypeByPosition(Position position);
	
	public List<String> getAllConsequenceTypeByPositionList(List<Position> positionList);
	
}
