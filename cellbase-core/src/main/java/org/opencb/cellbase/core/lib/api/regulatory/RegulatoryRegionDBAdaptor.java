package org.opencb.cellbase.core.lib.api.regulatory;

import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.lib.api.FeatureDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert
 * Date: 7/18/13
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */

public interface RegulatoryRegionDBAdaptor extends FeatureDBAdaptor {

    public QueryResult getAllById(String id, QueryOptions options);

    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options);

    public QueryResult getAllByPosition(Position position, QueryOptions options);

    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options);


    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options);

    public QueryResult getAllByRegion(Region region, QueryOptions options);

    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options);


//	public List<Tfbs> getAllByTfGeneName(String tfGeneName, String celltype, int start, int end);
//
//	public List<List<Tfbs>> getAllByTfGeneNameList(List<String> tfGeneNameList, String celltype, int start, int end);

//    public QueryResponse getAllByTargetGeneId(String targetGeneId, QueryOptions options);

//    public QueryResponse getAllByTargetGeneIdList(List<String> targetGeneIdList, QueryOptions options);


//    public QueryResponse getAllByJasparId(String jasparId, QueryOptions options);

//    public QueryResponse getAllByJasparIdList(List<String> jasparIdList, QueryOptions options);


//	public List<Protein> getTfInfoByTfGeneName(String tfGeneName);
//
//	public List<List<Protein>> getTfInfoByTfGeneNameList(List<String> tfGeneNameList);

//	public List<Pwm> getAllPwmByTfGeneName(String tfName);
//
//	public List<List<Pwm>> getAllPwmByTfGeneNameList(List<String> tfNameList);


//	public List<Tfbs> getAllByRegion(String chromosome);
//
//	public List<Tfbs> getAllByRegion(String chromosome, int start);
//
//	public List<Tfbs> getAllByRegion(String chromosome, int start, int end);
//
//	public List<Tfbs> getAllByRegion(Region region);
//
//	public List<List<Tfbs>> getAllByRegionList(List<Region> regionList);

//	public QueryResponse getAllByPosition(String chromosome, int position, QueryOptions options);


//	public List<Tfbs> getAllByInternalIdList(List<String> idList);
//
//	public List<Tfbs> getAllByInternalId(String id);


//    public List<Object> getAllAnnotation();

//    public List<Object> getAllAnnotationByCellTypeList(List<String> cellTypes);


//    public List<IntervalFeatureFrequency> getAllTfIntervalFrequencies(Region region, int interval);
}
